/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.masp.execution_engine.platformaccess.util.MediaUtil;
import com.mobeon.masp.execution_engine.platformaccess.util.TimeUtil;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.IMediaQualifierFactory;
import com.mobeon.masp.mediacontentmanager.MediaQualifierException;
import com.mobeon.masp.mediahandler.MediaHandler;
import com.mobeon.masp.mediahandler.MediaHandlerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.util.xml.SsmlDocument;
import com.mobeon.masp.util.markup.Detagger;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;

/**
 * Utility class that contain some "help" functions. For example getting and formatting a time string or retrieve
 * strings used in different MediaObjects.
 *
 * @author ermmaha
 */
public class PlatformAccessUtilImpl implements PlatformAccessUtil {
    private static ILogger log = ILoggerFactory.getILogger(PlatformAccessUtilImpl.class);

    private IMediaQualifierFactory iMediaQualifierFactory;
    private IMediaObjectFactory iMediaObjectFactory;
    private MediaTranslationManager mediaTranslationManager;
    private MediaHandlerFactory mediaHandlerFactory;

    /**
     * Constructor.
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * </dl>
     * @param iMediaQualifierFactory
     * @param iMediaObjectFactory
     */
    public PlatformAccessUtilImpl(IMediaQualifierFactory iMediaQualifierFactory, IMediaObjectFactory iMediaObjectFactory,
                                  MediaTranslationManager mediaTranslationManager,
                                  MediaHandlerFactory mediaHandlerFactory) {
        this.iMediaQualifierFactory = iMediaQualifierFactory;
        this.iMediaObjectFactory = iMediaObjectFactory;
        this.mediaTranslationManager = mediaTranslationManager;
        this.mediaHandlerFactory = mediaHandlerFactory;
    }

    public String getCurrentTime(String timezone) {
        return TimeUtil.getCurrentTime(timezone);
    }

    public String convertTime(String vvaTime, String fromTimezone, String toTimezone) {
        try {
            return TimeUtil.convertTime(vvaTime, fromTimezone, toTimezone);
        } catch (ParseException e) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "convertTime:vvaTime=" + vvaTime, e);
        }
    }

    public String formatTime(String vvaTime, String pattern) {
        try {
            return TimeUtil.formatTime(vvaTime, pattern);
        } catch (ParseException e) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "formatTime:vvaTime=" + vvaTime, e);
        }
    }

    public String stringDateToVvaTime(String dateStr, String timeZone) {
        return TimeUtil.dateToVvaTime(TimeUtil.stringToDate(dateStr),timeZone);
    }

    public IMediaQualifier getMediaQualifier(String type, String value) {
        IMediaQualifier.QualiferType[] tArr = IMediaQualifier.QualiferType.class.getEnumConstants();
        for (int i = 0; i < tArr.length; i++) {
            String cName = tArr[i].toString();
            if (cName.equalsIgnoreCase(type)) {
                try {
                    return iMediaQualifierFactory.create(tArr[i], null, value, null);
                } catch (MediaQualifierException e) {
                    throw new PlatformAccessException(
                            EventType.SYSTEMERROR, "getMediaQualifier:type=" + type + ", value=" + value, e);
                }
            }
        }
        throw new PlatformAccessException(
                EventType.SYSTEMERROR, "getMediaQualifier:type=" + type + ", value=" + value, "Could not find a IMediaQualifier");
    }

    public IMediaQualifier getMediaQualifier(IMediaObject iMediaObject) {
        return iMediaQualifierFactory.create(null, iMediaObject, null);
    }

    public IMediaObject getMediaObject(String value) {
        if (log.isDebugEnabled()) {
            log.debug("In getMediaObject: value=" + value);
        }

        try {
            MimeType mimeType = new MimeType("text/plain");
            MediaProperties mediaProperties = new MediaProperties(mimeType);
            return iMediaObjectFactory.create(value, mediaProperties);
        } catch (MimeTypeParseException e) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "getMediaObject", e.getMessage());
        } catch (MediaObjectException e) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "getMediaObject", e.getMessage());
        }
    }

    public String convertMediaObjectsToString(IMediaObject[] iMediaObject) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < iMediaObject.length; i++) {
            MediaProperties mediaProperties = iMediaObject[i].getMediaProperties();
            if (isTextMimeType(mediaProperties.getContentType())) {
                try {
                    buf.append(MediaUtil.convertMediaObjectToString(iMediaObject[i]));
                } catch (IOException e) {
                    throw new PlatformAccessException(
                            EventType.SYSTEMERROR, "convertMediaObjectsToString", e.getMessage());
                }
            } else {
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "convertMediaObjectsToString", "The MediaObject is not a text-mediaobject");
            }
        }
        return buf.toString();
    }

    private boolean isTextMimeType(MimeType mimeType) {
        // MimeType-API
        // ex: text/plain;charset=utf-8
        // getPrimaryType() --> text
        // getSubType() --> plain
        // toString() --> text/plain;charset=utf-8
        String typeStr = mimeType.getPrimaryType();
        return typeStr.equals("text");
    }

    public IMediaObject setMediaObjectProperty(IMediaObject mediaObject, String[] propertyNames, String[] propertyValues) {
        MediaProperties mediaProperties = mediaObject.getMediaProperties();
        if (isTextMimeType(mediaProperties.getContentType())) {
            try {
                String text = MediaUtil.convertMediaObjectToString(mediaObject);
                if (log.isDebugEnabled()) log.debug("In setMediaObjectProperty: text from mediaObject " + text);

                SsmlDocument ssmlDocument = new SsmlDocument();
                ssmlDocument.initialize();
                ssmlDocument.addSentence(text.trim());
                for (int i = 0; i < propertyNames.length; i++) {
                    ssmlDocument.setParameter(propertyNames[i], propertyValues[i]);
                }

                String xmlText = ssmlDocument.getXmlText();
                if (log.isDebugEnabled()) log.debug("In setMediaObjectProperty: xmltext from SsmlDocument " + xmlText);

                MediaProperties mp = new MediaProperties(SsmlDocument.SSML_MIME_TYPE);
                IMediaObject mo = iMediaObjectFactory.create(xmlText, mp);
                return mo;

            } catch (IllegalArgumentException e) {
                throw new PlatformAccessException(EventType.SYSTEMERROR, "setMediaObjectProperty", e);
            } catch (IOException e) {
                throw new PlatformAccessException(EventType.SYSTEMERROR, "setMediaObjectProperty", e);
            } catch (MediaObjectException e) {
                throw new PlatformAccessException(EventType.SYSTEMERROR, "setMediaObjectProperty", e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("In setMediaObjectProperty: mediaObject has no text part, the properties can't be set");
        }

        return mediaObject;
    }

    public String getMediaObjectProperty(IMediaObject mediaObject, String propertyName) {
        MediaProperties mediaProperties = mediaObject.getMediaProperties();
        if (isTextMimeType(mediaProperties.getContentType())) {
            try {
                String text = MediaUtil.convertMediaObjectToString(mediaObject);
                SsmlDocument ssmlDocument = new SsmlDocument();
                ssmlDocument.initialize();
                ssmlDocument.parse(text);
                String parameter = ssmlDocument.getParameter(propertyName);
                if (parameter != null) return parameter;
            } catch (IOException e) {
                throw new PlatformAccessException(EventType.SYSTEMERROR, "convertMediaObjectsToString", e.getMessage());
            }
        }
        return "";
    }

    public String[] getSupportedTTSLanguages() {
        Collection<String> textToSpeechLanguages = mediaTranslationManager.getTextToSpeechLanguages();
        if(textToSpeechLanguages == null){
            return new String[0];
        }
        return textToSpeechLanguages.toArray(new String[0]);
    }

    public String deTag(String s) {
        return Detagger.removeMarkup(s);
    }
    
    
    /**
     * @see PlatformAccessUtil interface for description
     */
    public IMediaObject appendMediaObjects(IMediaObject mo1, IMediaObject mo2) {
    	
    	MimeType mt1 = mo1.getMediaProperties().getContentType();
    	MimeType mt2 = mo2.getMediaProperties().getContentType();
    	
    	if (mt1 == null || mt2 == null) {
    		throw new PlatformAccessException(EventType.SYSTEMERROR, 
    				"AppendMediaObjects", "Content-Type must be set for both media objects to append");
    	}
    	
    	if (!mt1.getBaseType().equals(mt2.getBaseType())) {
    		throw new PlatformAccessException(EventType.SYSTEMERROR, 
    				"AppendMediaObjects", "Content-Type of media objects differs. ("
    				+ mt1.getBaseType() + " vs " + mt2.getBaseType() + ")");
    	}

    	MediaHandler mediaHandler = mediaHandlerFactory.getMediaHandler(mt1);
    	if (mediaHandler != null && mediaHandler.hasConcatenate()) {
        	try {
        		return mediaHandler.concatenate(mo1, mo2);
        	} catch (PlatformAccessException pe){
        	    	throw pe;
		} 
		catch (Exception e) { 
				log.warn("Exception while triyng to append media: ",e);
                throw new PlatformAccessException(EventType.SYSTEMERROR, 
                		"AppendMediaObjects", e.getMessage());
        	}
    	} else { 
    		log.warn("Append for Content-Type=" + 
    				mt1.getBaseType() + " is not supported, throwing PlatformAccessException");
    		throw new PlatformAccessException(EventType.SYSTEMERROR, 
    				"AppendMediaObjects", "Append for Content-Type=" + 
    				mt1.getBaseType() + " is not supported.");
    	}
    }

    public IMediaObject amrTo3gp(IMediaObject mo1) {
        MimeType mt1 = mo1.getMediaProperties().getContentType();

        if (mt1 == null) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "amrTo3gp", "Content-Type must be set");
        }

        MediaHandler mediaHandler = mediaHandlerFactory.getMediaHandler(mt1);
        if (mediaHandler != null && mediaHandler.hasConcatenate()) {
            try {
                return mediaHandler.ThreegppCleanup(mo1);
            } catch (PlatformAccessException pe){
                throw pe;
            } catch (Exception e) {
                throw new PlatformAccessException(EventType.SYSTEMERROR, "amrTo3gp", e.getMessage());
            }
        } else {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "amrTo3gp");
        }
    }

    public IMediaObject appendMediaObjectsAmr(IMediaObject mo1, IMediaObject mo2) {
    	log.info("This function is depricated, Please use appendMediaObjectsAmr, this one now calls it.");
        MimeType mt1 = mo1.getMediaProperties().getContentType();
        MimeType mt2 = mo2.getMediaProperties().getContentType();
        
        if (mt1 == null || mt2 == null) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "AppendMediaObjects", "Content-Type must be set for both media objects to append");
        }
        
        if (!mt1.getBaseType().equals(mt2.getBaseType())) {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "AppendMediaObjects", "Content-Type of media objects differs. ("+ mt1.getBaseType() + " vs " + mt2.getBaseType() + ")");
        }
        String mc1c = mt1.getParameter("codec");
        if (mc1c == null) {
        	mc1c="samr"; //amr-nb
        }
        String mc2c = mt2.getParameter("codec");
        if (mc2c == null) {
        	mc2c="samr"; //amr-nb
        } 
        
        if(!mc1c.equalsIgnoreCase(mc2c)) {
        	throw new PlatformAccessException(EventType.SYSTEMERROR, "AppendMediaObjects", "Content-Type codec of media objects differ. ("+ mc1c + " vs " + mc2c+ ")");
        }
        
        //use the common append to keep aligned
        //HY12623 VVS MAS exception on long duration calls for specific customized call flows
        return appendMediaObjects(mo1, mo2);

        //NOTE currently this does not work, there is no need to use a specific method anyway as
        //appendMediaObjects works anyway..
/*        MediaHandler mediaHandler = mediaHandlerFactory.getMediaHandler(mt1);
        if (mediaHandler != null && mediaHandler.hasConcatenate()) {
            try {
                return mediaHandler.concatenateAmr(mo1, mo2,mc1c);
            } catch (PlatformAccessException pe){
                throw pe;
            } catch (Exception e) {
                throw new PlatformAccessException(EventType.SYSTEMERROR, "appendMediaObjectsAmr", e.getMessage());
            }
        } else {
            throw new PlatformAccessException(EventType.SYSTEMERROR, "appendMediaObjectsAmr", "Append for Content-Type=" + mt1.getBaseType() + " is not supported.");
        }*/
    }
}
