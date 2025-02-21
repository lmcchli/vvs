/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;

import jakarta.activation.MimeType;

/**
 * Utility methods for creating Java instances.
 * 
 * @author Jörgen Terner
 */
public class TestUtil {
    // (Brage=150.132.5.213");
    protected String remoteHostAddress = "0.0.0.0"; 
        
    protected static final int REMOTE_AUDIO_PORT = 4712;
    
    private static MimeType VIDEO_QUICKTIME = null; 
    private static MimeType AUDIO_WAV = null;
    
    static {
        try {
            VIDEO_QUICKTIME = new MimeType("video/quicktime");
            AUDIO_WAV = new MimeType("audio/wav");
        }
        catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }
    
    /**
     * Only static methods in this class.
     */
    private TestUtil() {
    }
    
    public static StreamContentInfo getVideoContentInfo() {
        try {
            ContentTypeMapperImpl ctm = new ContentTypeMapperImpl();
            ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
            cm.setConfigFile("../../../cfg/contenttypemapper.xml");
            ctm.setConfiguration(cm.getConfiguration());
            ctm.init();
            return StreamContentInfo.getInbound(ctm, getVideoMediaMimeTypes());
        }
        catch (Exception e) {
            System.err.println("Failed to create ContentTypeMapper: " + e);
            return null;
        }
    }

    public static StreamConfiguration getConfig() {
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("../../../cfg/stream.xml");
        StreamConfiguration config = StreamConfiguration.getInstance();
        config.setInitialConfiguration(cm.getConfiguration());
        try {
            config.update();
        }
        catch (Exception e) {
            System.err.println("Exception: " + e);
        }
        return config;
    }

    public static RecordingProperties getRecordingProperties() {
        RecordingProperties prop = new RecordingProperties();
        prop.setWaitForRecordToFinish(true);
        prop.setMaxRecordingDuration(120);
        return prop;
    }    
    
    public static Object getRequestId() {
        return new Object();
    }
    
    public static void save(IMediaObject mo, String name) {
        try {
            InputStream is = mo.getInputStream();
            File f = new File(System.getProperty("user.dir") + 
                    File.separator + name);
            FileOutputStream os = new FileOutputStream(f);
            int b = -1;
            while ((b = is.read()) != -1) {
                os.write(b);
            }
            os.close();
            is.close();
        }
        catch (Exception e) {
            System.err.println("Unexpected exception: " + e);
        }
    }
    
    /**
     * Creates a recordable MediaObject instance.
     * 
     * @return A recordable MediaObject instance.
     */
    public static IMediaObject createRecordableVideoMediaObject() {
        IMediaObject result = null;
        try {
            MediaObjectFactory factory = new MediaObjectFactory(10000);
            result = factory.create();
        }
        catch (Exception e) {
            System.err.println("Unexpected error in createRecordableMediaObject: " + e);
            return null;
        }
        result.getMediaProperties().setContentType(VIDEO_QUICKTIME);
        return result;
    }
    
    public static MediaMimeTypes getAudioMediaMimeTypes() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
        return mediaMimeTypes;
    }
        
    public static MediaMimeTypes getVideoMediaMimeTypes() {
        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes();
        mediaMimeTypes.addMimeType(RTPPayload.AUDIO_PCMU);
        mediaMimeTypes.addMimeType(RTPPayload.VIDEO_H263);
        return mediaMimeTypes;
    }
 }