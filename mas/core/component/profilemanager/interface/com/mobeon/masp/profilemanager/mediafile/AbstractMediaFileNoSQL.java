package com.mobeon.masp.profilemanager.mediafile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.client.BasicMfs;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

public class AbstractMediaFileNoSQL implements IMediaFile {

    private static final ILogger log = ILoggerFactory.getILogger(AbstractMediaFileNoSQL.class);
    private static BasicMfs mfsClient = BasicMfs.getInstance(null);  // There is no OAM instance available in this part of the code.
    private static final String MOIP_MSGCLASS = "moip";
    private static final String GREETING_KEY2 = "Greeting";

    private static final String PROPERTY_EXTENSION = ".properties";
    private static final String MEDIA_EXTENSION = ".raw";
    private static final String MEDIA_EXTENSION_PROPERTY = "extension";
    private static final String DEFAULT_FNAME = "mediafile";
    
    private MSA msa = null;
    private String telephone = null;

    private String name;
    private String type;
    private InputStream stream;
    private Properties properties;
    
    
    /**
     * Creates a new Greeting object
     * 
     * @param msa associated to the greeting
     * @param telephone associated to the greeting.
     * @param name of the greeting with no extension.
     * @throws Exception If there is any error.
     */
    public AbstractMediaFileNoSQL(MSA msa, String telephone, String name) throws Exception {
        this(msa, telephone, name, null);
    }

    /**
     * Creates a new Greeting object
     * 
     * @param msa associated to the greeting
     * @param telephone associated to the greeting.
     * @param name of the greeting with no extension.
     * @param the extension
     * @throws Exception If there is any error.
     */
    public AbstractMediaFileNoSQL(MSA msa, String telephone, String name, String extension) throws Exception {
        this.name = name;
        if (this.name == null) {
            this.name = DEFAULT_FNAME;
        }
        this.type = extension;
        this.msa = msa;
        this.telephone = telephone;
        properties = new Properties();
        load();
    }

    protected void load() throws Exception {
        log.debug("load() Greeting msa=" + msa.toString() + " telephone=" + telephone +
                " propertyFileName=" + propertyFileName() + " and mediaFileName="+ mediaFileName());

        ByteArrayInputStream propIs = mfsClient.getPrivateFileAsInputStream(msa, MOIP_MSGCLASS, cdgKey1SpecialCase(propertyFileName(), telephone), GREETING_KEY2, propertyFileName());

        if (propIs != null) {
            // When the property artifact and the media artifact exist, they are loaded into memory.
            properties.load(propIs);
            propIs.close();

            if (this.type == null) {
                // Determine the media file extension
                this.type = properties.getProperty(MEDIA_EXTENSION_PROPERTY);
                if (this.type == null) {
                    this.type = MEDIA_EXTENSION;
                }
            }

            stream = mfsClient.getPrivateFileAsInputStream(msa, MOIP_MSGCLASS, cdgKey1SpecialCase(propertyFileName(), telephone), GREETING_KEY2, mediaFileName());
        } else {
            if (this.type == null) {
                this.type = MEDIA_EXTENSION;
            }
        }
        properties.setProperty(MEDIA_EXTENSION_PROPERTY, this.type);
    }

    @Override
    public void store() throws Exception {
        log.debug("store() Greeting msa=" + msa.toString() + " telephone=" + telephone +
                " propertyFileName=" + propertyFileName() + " and mediaFileName="+ mediaFileName());

        // Save properties
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        properties.store(os, null);
        mfsClient.addPrivate(msa, MOIP_MSGCLASS, cdgKey1SpecialCase(propertyFileName(), telephone), GREETING_KEY2, propertyFileName(), os.toByteArray());

        // Save media
        mfsClient.addPrivate(msa, MOIP_MSGCLASS, cdgKey1SpecialCase(propertyFileName(), telephone), GREETING_KEY2, mediaFileName(), BasicMfs.inputStreamToByteArray(stream));
    }

    @Override
    public boolean exists() {
        try {
            log.debug("exists() Check existance of Greeting msa=" + msa.toString() + " telephone=" + telephone +
                    " mediaFileName="+ mediaFileName());
            return mfsClient.privateFileExists(msa, MOIP_MSGCLASS, cdgKey1SpecialCase(propertyFileName(), telephone), GREETING_KEY2, mediaFileName());
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void delete() {
        try {
            log.debug("delete() Delete Greeting msa=" + msa.toString() + " telephone=" + telephone +
                    " propertyFileName=" + propertyFileName() + " and mediaFileName="+ mediaFileName());
            mfsClient.deletePrivate(msa, MOIP_MSGCLASS, cdgKey1SpecialCase(propertyFileName(), telephone), GREETING_KEY2, propertyFileName());
            mfsClient.deletePrivate(msa, MOIP_MSGCLASS, cdgKey1SpecialCase(propertyFileName(), telephone), GREETING_KEY2, mediaFileName());
        } catch (Exception e) {
        }
    }

    @Override
    public InputStream getMedia() {
        return stream;
    }

    @Override
    public void setMedia(InputStream media) {
        stream = media;
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
    public void setName(String aName) {
        this.name = aName;    
        try {
            load();
        } catch (Exception e) {
        }
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    protected String propertyFileName() {
        return getName() + PROPERTY_EXTENSION;
    }

    protected String mediaFileName() {
        return getName() + type;
    }

    /**
     * This method returns the appropriate Key1 that will be used by the MFS NoSQL client
     * for private file methods. The returned key1 argument will be an empty string if the
     * provided {@code s} string (the filename / artifactname) starts with "cdg". The
     * comparison is case sensitive. 
     *  
     * @param s  (must not be null)
     * @param key1  (must not be null)
     * @return  the appropriate key1.
     */
    private static String cdgKey1SpecialCase(String s, String key1) {
        if (s.startsWith("cdg")) {
            return "";
        } else {
            return key1;
        }
    }
}
