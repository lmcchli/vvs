/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.MILLISECONDS;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.profilemanager.BaseContext;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.util.string.FileName;

/**
 * Manages getting and setting greetings
 *
 * @author mande,estberg(eems update)
 */
public class GreetingManagerImpl implements GreetingManager {
    private static final ILogger log = ILoggerFactory.getILogger(GreetingManagerImpl.class);

    private static final String GREETING_TYPE          = "X-M3-Greeting-Type";
    private static final String GREETING_FORMAT        = "X-M3-Greeting-Format";
    private static final String CONTENT_DESCRIPTION    = "Content-Description";
    private static final String CONTENT_DISPOSITION    = "Content-Disposition";
    private static final String CONTENT_DURATION       = "Content-Duration";
    private static final String GREETING_SUBJECT       = "X-EEMS-Subject";
    private static final String GREETING_FROM          = "X-EEMS-From";
    private static final String GREETING_TO            = "X-EEMS-To";
    private static final String GREETING_FILENAME      = "X-EEMS-Filename";
    private static final String GREETING_CONTENTTYPE   = "X-EEMS-ContentType";
    private static final String GREETING_SIZE          = "X-EEMS-Size";

    private BaseContext context;
    protected String folder;
    /**
     * Default GreetingStoreFactory. This can be changed by setGreetingStoreFactory (for testing purposes).
     */
    GreetingStoreFactory greetingStoreFactory = new GreetingStoreFactoryImpl();

    protected String userId;

    public GreetingManagerImpl(BaseContext context, String userId, String folder) {
        this.context = context;
        this.folder = folder;        
        this.userId = userId;
    }

    public void setGreetingStoreFactory(GreetingStoreFactory factory) {
        greetingStoreFactory = factory;
    }

    public void setGreeting(String telephoneNumber, GreetingSpecification specification, IMediaObject mediaObject) throws ProfileManagerException {
        store(userId, specification, telephoneNumber, mediaObject);
    }

    public IMediaObject getGreeting(GreetingSpecification specification) throws ProfileManagerException {
        if (log.isDebugEnabled()) {
            log.debug("getGreeting() specification type=" + specification.getType() + ", specification format=" + specification.getFormat());
            log.debug("getGreeting() userId=" + userId + ", folder=" + folder);
        }
        String str = folder;
        if (str.startsWith("tel:")) {
            str = str.substring(4);
        }
        if (str.startsWith("+")) {
            str = str.substring(1);
        }
        if (log.isDebugEnabled()) {
            log.debug("getGreeting() search for greeting file from folder=" + str);
        }
        IGreetingStore store = getStore(userId, "n/a", str);
        IGreeting greeting = store.search(specification);
        if (log.isDebugEnabled()) {
            log.debug("getGreeting() greeting=" + greeting.getName());
        }
        return parseMessage(greeting, specification);
    }

    IMediaObject parseMessage(IGreeting greeting, GreetingSpecification specification) throws ProfileManagerException {
        if (log.isDebugEnabled()) {
            log.debug("parseMessage() greeting=" + greeting.getName());
        }
        MediaProperties mediaProperties = new MediaProperties();
        try {
            mediaProperties.setContentType(new MimeType(greeting.getProperty(GREETING_CONTENTTYPE)));
        } catch (MimeTypeParseException e) {
            throw new ProfileManagerException(e.getMessage());
        }

        String duration = greeting.getProperty(CONTENT_DURATION);
        if (log.isDebugEnabled()) {
            log.debug("parseMessage() duration=" + duration);
        }
        if (duration != null) {
            Long lduration = Long.parseLong(duration);
            mediaProperties.addLength(
                    new MediaLength(
                            MediaLength.LengthUnit.MILLISECONDS,
                            lduration * 1000
                            )
                    );
        }

        FileName fileName = FileName.createFileName(greeting.getProperty(GREETING_FILENAME));
        if (log.isDebugEnabled()) {
            log.debug("parseMessage() fileName=" + fileName);
        }
        String extension;
        if (fileName == null) {
            extension = getDefaultExtension(specification.getFormat());
        } else {
            extension = fileName.getExtension();
        }
        if (log.isDebugEnabled()) {
            log.debug(".parseMessage() extension=" + extension);
        }
        mediaProperties.setFileExtension(extension);
        IMediaObject imo = null;

        try {
            imo = getContext().getMediaObjectFactory().create(
                    greeting.getMedia(),
                    Integer.parseInt(greeting.getProperty(GREETING_SIZE)),
                    mediaProperties);
            if (log.isDebugEnabled()) {
                log.debug(".parseMessage() IMediaObject returned has size=" + imo.getSize());
            }
            if (log.isDebugEnabled()) {
                log.debug("parseMessage() IMediaObject returned has mediaProperties().toString()=" + imo.getMediaProperties().toString());
            }
            return imo;
        } catch (NumberFormatException e) {
            throw new ProfileManagerException(e.getMessage());
        } catch (MediaObjectException e) {
            throw new ProfileManagerException(e.getMessage());
        }
    }

    private String getDefaultExtension(GreetingFormat format) {
        switch (format) {
        case VOICE: return "wav";
        case VIDEO: return "mov";
        default:    return null;
        }
    }

    private BaseContext getContext() {
        return context;
    }

    /**
     * Stores the greeting message in a folder
     * @param specification
     * @param telephoneNumber
     * @param mediaObject
     * @throws ProfileManagerException if problems occur with message or store.
     */
    public void store(String msid, GreetingSpecification specification, String telephoneNumber, IMediaObject mediaObject) throws ProfileManagerException {
        if (mediaObject == null) {
            try {
                // Delete current greeting and return
                IGreetingStore store = getStore(msid, telephoneNumber, folder);
                IGreeting greeting = store.search(specification);
                greeting.delete();
            } 
            catch (GreetingNotFoundException e) {
                // No greeting found, that's OK
            }
            return;
        }

        // Create and store the greeting
        IGreeting greeting = createMessage(telephoneNumber, specification, mediaObject, userId);
        try {
            greeting.store();
        } catch (Exception e) {
            throw new ProfileManagerException(e.getMessage());
        }
    }

    /**
     * Gets a store from the StoreManager
     * @param host the host to store the message at
     * @param port the port the host uses
     * @param userId the mailbox to store the message at
     * @param password the password to that mailbox
     * @return a store
     * @throws ProfileManagerException if the store could not be created
     */
    protected IGreetingStore getStore(String userId, String telephone, String folder) {
        return greetingStoreFactory.getGreetingStore(userId, telephone, folder);
    }

    /**
     * Creates a greeting message for storing in the subscriber's private folder.
     * @param telephoneNumber
     * @param specification the greeting specification for the greeting to store
     * @param object the mediaobject containing the greeting. No check is done of the validity of the media object,
     * i.e. it is possible to store an audio media object as a video greeting
     * @param from
     * @throws ProfileManagerException if information cannot be added to the message
     */
    protected IGreeting createMessage(String telephoneNumber, GreetingSpecification specification, IMediaObject object, String from) throws ProfileManagerException {
        IGreetingStore store = getStore(userId, telephoneNumber, folder);
        IGreeting greeting = store.create(specification, object);

        if (specification.getType() != GreetingType.DIST_LIST_SPOKEN_NAME) {
            greeting.setProperty(GREETING_TYPE, GreetingUtils.getTypeHeader(specification));
            greeting.setProperty(GREETING_FORMAT, GreetingUtils.getFormatHeader(specification.getFormat()));
        }
        greeting.setProperty(GREETING_SUBJECT, GreetingUtils.getSubjectString(specification));
        greeting.setProperty(GREETING_FROM, from);
        greeting.setProperty(GREETING_TO, telephoneNumber);

        MediaProperties mediaProperties = object.getMediaProperties();
        if (specification.getDuration() != null) {
            greeting.setProperty(CONTENT_DESCRIPTION, "Greeting Message (" + specification.getDuration() + " seconds)");
            greeting.setProperty(CONTENT_DURATION, specification.getDuration());
        } else if (mediaProperties.hasLengthInUnit(MILLISECONDS)) {
            long duration = mediaProperties.getLengthInUnit(MILLISECONDS)/1000;            
            greeting.setProperty(CONTENT_DESCRIPTION, "Greeting Message (" + duration + " seconds)");
            greeting.setProperty(CONTENT_DURATION, Long.toString(duration));
        }

        greeting.setProperty(CONTENT_DISPOSITION, GreetingUtils.getDispositionString(specification.getFormat()));
        greeting.setProperty(GREETING_FILENAME, GreetingUtils.getGreetingFileName(specification, mediaProperties.getContentType()));        
        greeting.setProperty(GREETING_CONTENTTYPE, mediaProperties.getContentType().toString());
        greeting.setProperty(GREETING_SIZE, Long.toString(mediaProperties.getSize()));
        greeting.setMedia(object.getInputStream());        

        return greeting;
    }
}
