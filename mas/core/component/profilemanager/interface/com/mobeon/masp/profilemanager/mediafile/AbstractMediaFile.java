package com.mobeon.masp.profilemanager.mediafile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;

public class AbstractMediaFile implements IMediaFile {

    private static final String PROPERTY_EXTENSION = ".properties";
    private static final String MEDIA_EXTENSION = ".raw";
    private static final String MEDIA_EXTENSION_PROPERTY = "extension";
    private static final String DEFAULT_FNAME = "mediafile";

    private static final ILogger LOGGER = ILoggerFactory.getILogger(AbstractMediaFile.class);

    private String name;
    private String type;
    private InputStream stream;
    private Properties properties;

    /**
     * Creates a new Greeting object using file and a media extension.
     * @param pathName Path name of the greeting with no extension.
     * @throws IOException If File IO errors.
     */
    public AbstractMediaFile(String pathName) throws Exception {
        this(pathName, null);
    }

    public AbstractMediaFile(String pathName, String extension) throws Exception {
        this.name = pathName;
        if (this.name == null) {
            this.name = DEFAULT_FNAME;
        }
        this.type = extension;
        properties = new Properties();
        load();
    }

    protected void load() throws Exception {
        File propertyFile = new File(propertyFileName());
        if (propertyFile.exists()) {
            // When the property file and the media file exists, they are loaded
            // into memory.
            FileInputStream propertyStream = new FileInputStream(propertyFile);
            properties.load(propertyStream);
            propertyStream.close();

            if (this.type == null) {
                // Determine the media file extension
                this.type = properties.getProperty(MEDIA_EXTENSION_PROPERTY);
                if (this.type == null) {
                    this.type = MEDIA_EXTENSION;
                }
            }
            File mediaFile = new File(mediaFileName());
            long size = mediaFile.length();
            byte[] buffer = new byte[(int)size];
            FileInputStream mediaStream = new FileInputStream(mediaFile);
            size = mediaStream.read(buffer);
            if (LOGGER.isDebugEnabled() && (int)size != buffer.length) {
                LOGGER.warn("Media file " + mediaFileName() + " could not be read completely.");
            }
            mediaStream.close();
            stream = new ByteArrayInputStream(buffer);
        } else {
            if (this.type == null) {
                this.type = MEDIA_EXTENSION;
            }
        }
        properties.setProperty(MEDIA_EXTENSION_PROPERTY, this.type);
    }
    
    @Override
    public InputStream getMedia() {
        return stream;
    }

    @Override
    public void setMedia(InputStream media) {
        stream = media;
    }

    /**
     * When the client changes the name, it is best to reload everything.
     */
    public void setName(String aName){
        name =aName;	
        try {
            load();
        }
        catch (Exception e){
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    @Override
    public void store() throws Exception {

        LOGGER.debug("store() Greeting propertyFileName " + propertyFileName() + " and mediaFileName=" + mediaFileName());

        FileOutputStream propertyStream = null;
        FileOutputStream mediaStream = null;

        try {
            // Save properties
            propertyStream = createFileOutputStream(propertyFileName());
            properties.store(propertyStream, null);

            // Save media
            mediaStream = createFileOutputStream(mediaFileName());
            byte[] buffer = new byte[4094];
            int byteRead = 0;
            while ((byteRead = stream.read(buffer)) != -1) {
                mediaStream.write(buffer, 0, byteRead);
            }

        } catch (IOException ioe) {
            LOGGER.error("");
            throw ioe;
        } finally {
            if (propertyStream != null) {
                propertyStream.close();
            }
            if (mediaStream != null) {
                mediaStream.close();
            }
        }
    }

    /**
     * Create a file by handling potential exception about missing the parent directory
     * Parent directory will be created if the system is allowed to (abcxyz.mfs.userdir.create property)
     * @param fileName
     * @return FileOutputStream
     * @throws IOException
     */
    private FileOutputStream createFileOutputStream(String fileName) throws IOException {

        FileOutputStream propertyStream = null;
        try {
            propertyStream = new FileOutputStream(fileName);
        } catch (FileNotFoundException fnfe) {

            // Check if the MOIP private folder is present
            File path = new File(fileName).getParentFile();
            if(!MfsEventManager.validateOrCreateDirectory(path, true)) {
                throw new IOException("Couldn't create path" + path);
            }

            // Create file 
            propertyStream = tryCreateFileOutputStream(fileName);
        }
        return propertyStream;
    }

    /**
     * Create file with retry mecanism 
     * @param fileName
     * @return FileOutputStream
     * @throws IOException
     */
    private FileOutputStream tryCreateFileOutputStream(String fileName) throws IOException {

        FileOutputStream propertyStream = null;
        int retryCount = 0;

        while (retryCount < 3) {
            try {
                propertyStream = new FileOutputStream(fileName);
                return propertyStream;
            } catch (IOException ioe) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {}
                ++retryCount;
            }
        }
        throw new IOException("Couldn't create file " + fileName);
    }

    /* (non-Javadoc)
     * @see com.mobeon.masp.profilemanager.greetings.IGreeting#exists()
     */
    @Override
    public boolean exists() {
        LOGGER.debug("exists() Check existance of Greeting mediaFileName=" + mediaFileName());
        File mediaFile = new File(mediaFileName());
        return mediaFile.exists();
    }

    /* (non-Javadoc)
     * @see com.mobeon.masp.profilemanager.greetings.IGreeting#delete()
     */
    @Override
    public void delete() {
        String filename = propertyFileName();
        File propertyFile = new File(filename);
        LOGGER.debug("delete() Delete Greeting propertyFileName=" + filename);

        boolean status = propertyFile.delete();
        if (LOGGER.isDebugEnabled() && status == false) {
            LOGGER.error("Cannot delete greeting file " + filename);
        }

        File mediaFile = new File(mediaFileName());
        LOGGER.debug("delete() Delete Greeting mediaFileName=" + mediaFile);
        status = mediaFile.delete();
        if (LOGGER.isDebugEnabled() && status == false) {
            LOGGER.error("Cannot delete greeting file " + mediaFile);
        }
    }

    protected String propertyFileName() {
        return getName() + PROPERTY_EXTENSION;
    }

    protected String mediaFileName() {
        return getName() + type;
    }

    protected ILogger getLogger(){
        return LOGGER;
    }

}
