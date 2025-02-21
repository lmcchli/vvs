/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.greetings;

import com.abcxyz.services.moip.migration.profilemanager.moip.BaseContext;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaLength;
import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.MILLISECONDS;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.jaf.DataSourceAdapter;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.masp.profilemanager.greetings.GreetingType;
import com.mobeon.common.util.javamail.PartParser;
import com.mobeon.common.util.string.FileName;

import jakarta.activation.DataHandler;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.*;
import java.io.IOException;

/**
 * Manages getting and setting greetings
 *
 * @author mande
 */
public class GreetingManagerImpl implements GreetingManager {
    private static final ILogger LOG = ILoggerFactory.getILogger(GreetingManagerImpl.class);

    private static final String GREETING_TYPE = "X-M3-Greeting-Type";
    private static final String GREETING_FORMAT = "X-M3-Greeting-Format";
    private static final String GREETING_SUBTYPE = "Greeting-Message";
    private static final String HEADERNAME_MESSAGE_ID = "Message-ID";
    private static final String CONTENT_DESCRIPTION = "Content-Description";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_DURATION = "Content-Duration";

    private BaseContext context;
    private String host;
    private int port;
    private String userId;
    private String password;
    private String folder;
    private IServiceInstance serviceInstance;

    public GreetingManagerImpl(BaseContext context, String host, int port, String userId, String password, String folder, IServiceInstance serviceInstance) {
        this.context = context;
        this.host = host;
        this.port = port;
        this.userId = userId;
        this.password = password;
        this.folder = folder;
        this.serviceInstance = serviceInstance;
    }

    public IServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(IServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public void setGreeting(String telephoneNumber, GreetingSpecification specification, IMediaObject mediaObject) throws ProfileManagerException {
        store(specification, telephoneNumber, mediaObject);
    }

    public IMediaObject getGreeting(GreetingSpecification specification) throws ProfileManagerException {
        Store store = getStore(host, port, userId, password);
        try {
            Folder imapFolder = store.getFolder(folder);
            if (imapFolder.exists()) {
                imapFolder.open(Folder.READ_WRITE);
                Message[] messages = imapFolder.search(getGreetingSearchTerm(specification));
                if (messages.length == 0) {
                    throw new GreetingNotFoundException(specification);
                }
                // Todo: what to do with OwnRecorded? Can have multiple hits.
                if (messages.length > 1) {
                    LOG.info("More than 1 " + specification + " greeting exists");
                }
                // Always use "latest"
                Message message = messages[messages.length - 1];
                return parseMessage(message, specification);
            } else {
                throw new GreetingNotFoundException("Greeting folder does not exist");
            }
        } catch (MessagingException e) {
            throw new ProfileManagerException("Exception when getting greeting from " + store.getURLName() + ": " + e.getMessage());
        } finally {
            returnStore(store);
        }
    }

    public String getGreetingMessageId(GreetingSpecification specification) throws ProfileManagerException {
        Store store = getStore(host, port, userId, password);
        try {
            Folder imapFolder = store.getFolder(folder);
            if (imapFolder.exists()) {
                imapFolder.open(Folder.READ_WRITE);
                Message[] messages = imapFolder.search(getGreetingSearchTerm(specification));
                if (messages.length == 0) {
                    throw new GreetingNotFoundException(specification);
                }
                // Todo: what to do with OwnRecorded? Can have multiple hits.
                if (messages.length > 1) {
                    LOG.info("More than 1 " + specification + " greeting exists");
                }
                // Always use "latest"
                Message message = messages[messages.length - 1];
                String[] headers = message.getHeader(HEADERNAME_MESSAGE_ID);
                if ( headers != null ) {
                        return headers[0];
                } else {
                        return null;
                }
            } else {
                throw new GreetingNotFoundException("Greeting folder does not exist");
            }
        } catch (MessagingException e) {
            throw new ProfileManagerException("Exception when getting greeting from "+store.getURLName()+": "+e.getMessage());
        } finally {
            returnStore(store);
        }
    }

    IMediaObject parseMessage(Message message, GreetingSpecification specification) throws ProfileManagerException {
        try {
            Object content = message.getContent();
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                BodyPart bodyPart = multipart.getBodyPart(0);
                PartParser.Result result = PartParser.parse(bodyPart);
                MimeType mimeType = result.getContentType();
                MediaProperties mediaProperties = new MediaProperties();
                mediaProperties.setContentType(mimeType);
                Long duration = result.getDuration();
                if (duration != null) {
                    mediaProperties.addLength(
                            new MediaLength(
                                    MediaLength.LengthUnit.MILLISECONDS,
                                    duration * 1000
                            )
                    );
                }
                FileName fileName = result.getFilename();
                String extension;
                if (fileName == null) {
                    extension = getDefaultExtension(specification.getFormat());
                } else {
                    extension = fileName.getExtension();
                }
                mediaProperties.setFileExtension(extension);
                return getContext().getMediaObjectFactory().create(
                        bodyPart.getInputStream(),
                        bodyPart.getSize(),
                        mediaProperties);
            } else {
                throw new ProfileManagerException("Invalid greeting message");
            }
        } catch (IOException e) {
            throw new ProfileManagerException(e.getMessage());
        } catch (MimeTypeParseException e) {
            throw new ProfileManagerException(e.getMessage());
        } catch (MediaObjectException e) {
            throw new ProfileManagerException(e.getMessage());
        } catch (MessagingException e) {
            throw new ProfileManagerException(e.getMessage());
        }
    }

    private String getDefaultExtension(GreetingFormat format) {
        switch (format) {
            case VOICE:
                return "wav";
            case VIDEO:
                return "mov";
            default:
                return null;
        }
    }

    private BaseContext getContext() {
        return context;
    }

    /**
     * Stores the greeting message in a folder
     *
     * @param specification
     * @param telephoneNumber
     * @param mediaObject
     * @throws ProfileManagerException if problems occur with message or store.
     */
    public void store(GreetingSpecification specification, String telephoneNumber, IMediaObject mediaObject) throws ProfileManagerException {
        Store store = getStore(host, port, userId, password);
        try {
            Folder imapFolder = store.getFolder(folder);
            if (!imapFolder.exists()) {
                imapFolder.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
            }
            imapFolder.open(Folder.READ_WRITE);
            Message[] messages = imapFolder.search(getGreetingSearchTerm(specification));
            // If greeting message(s) already exists, delete it/them
            // Todo: what to do with OwnRecorded?
            for (Message message : messages) {
                message.setFlag(Flags.Flag.DELETED, true);
            }
            imapFolder.expunge();
            if (mediaObject == null) {
                // Only delete greeting, return
                return;
            }
            imapFolder.appendMessages(new Message[]{createMessage(telephoneNumber, specification, mediaObject, userId)});
        } catch (MessagingException e) {
            throw new ProfileManagerException("Exception when storing greeting to " + store.getURLName() + ": " + e.getMessage());
        } finally {
            returnStore(store);
        }
    }

    /**
     * Gets a store from the StoreManager
     *
     * @param host     the host to store the message at
     * @param port     the port the host uses
     * @param userId   the mailbox to store the message at
     * @param password the password to that mailbox
     * @return a store
     * @throws ProfileManagerException if the store could not be created
     */
    private Store getStore(String host, int port, String userId, String password) throws ProfileManagerException {
        try {
            return getContext().getStoreManager().getStore(host, port, userId, password);
        } catch (MessagingException e) {
            throw new ProfileManagerException("Exception when trying to access " + host + ":" + port + ": " + e.getMessage());
        }
    }

    /**
     * Returns a store to the StoreManager
     *
     * @param store the store to return
     * @logs.warn "Could not return store to StoreManager" - if store could not be returned to the StoreManager,
     * see MessagingException for further information
     */
    private void returnStore(Store store) {
        try {
            getContext().getStoreManager().returnStore(store);
        } catch (MessagingException e) {
            LOG.warn("Could not return store to StoreManager");
        }
    }

    
    /**
     * Overwirte this method if migrated system is not MoIP
     * @param specification
     * @return
     * @throws GreetingNotFoundException
     */
    protected SearchTerm getGreetingSearchTerm(GreetingSpecification specification) throws GreetingNotFoundException {
        SearchTerm result;

        SearchTerm formatSearchTerm = getSearchTerm(specification.getFormat());

        SearchTerm oldTypeSearchTerm = null;
        SearchTerm newTypeSearchTerm;

        SearchTerm notDeletedTerm = new FlagTerm(new Flags(Flags.Flag.DELETED), false);
        switch (specification.getType()) {
            case ALL_CALLS:
                oldTypeSearchTerm = new SubjectTerm("allcalls");
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "allcalls");
                break;
            case BUSY:
                oldTypeSearchTerm = new SubjectTerm("busy");
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "busy");
                break;
            case EXTENDED_ABSENCE:
                oldTypeSearchTerm = new SubjectTerm("extended_absence");
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "extended_absence");
                break;
            case NO_ANSWER:
                oldTypeSearchTerm = new SubjectTerm("noanswer");
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "noanswer");
                break;
            case OUT_OF_HOURS:
                oldTypeSearchTerm = new SubjectTerm("outofhours");
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "outofhours");
                break;
            case OWN_RECORDED:
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "ownrecorded");
                break;
            case CDG:
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "cdg" + specification.getSubId() + "#");
                break;
            case TEMPORARY:
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "temporary");
                break;
            case SPOKEN_NAME:
                oldTypeSearchTerm = new SubjectTerm("spokenname");
                newTypeSearchTerm = new HeaderTerm(GREETING_TYPE, "spokenname");
                break;
            case DIST_LIST_SPOKEN_NAME:
                return new AndTerm(notDeletedTerm, new SubjectTerm(specification.getSubId()));
            default:
                throw new GreetingNotFoundException("Unknown type " + specification.getType());
        }

        if (specification.getFormat() == GreetingFormat.VOICE) {
            if (oldTypeSearchTerm != null) {
                SearchTerm newGreetingHeadersSearchTerm = new AndTerm(formatSearchTerm, newTypeSearchTerm);
                SearchTerm notNewGreetingHeaders = new NotTerm(
                        new OrTerm(
                                new HeaderTerm(GREETING_FORMAT, ""),
                                new HeaderTerm(GREETING_TYPE, "")
                        ));
                SearchTerm oldGreetingHeadersSearchTerm = new AndTerm(notNewGreetingHeaders, oldTypeSearchTerm);
                result = new AndTerm(notDeletedTerm, new OrTerm(newGreetingHeadersSearchTerm, oldGreetingHeadersSearchTerm));
            } else {
                result = new AndTerm(notDeletedTerm, new AndTerm(formatSearchTerm, newTypeSearchTerm));
            }
        } else {
            result = new AndTerm(notDeletedTerm, new AndTerm(formatSearchTerm, newTypeSearchTerm));
        }
        return result;
    }

    private SearchTerm getSearchTerm(GreetingFormat format) throws GreetingNotFoundException {
        switch (format) {
            case VOICE:
                return new HeaderTerm(GREETING_FORMAT, "voice");
            case VIDEO:
                return new HeaderTerm(GREETING_FORMAT, "video");
            default:
                throw new GreetingNotFoundException("Unknown format " + format);
        }
    }

    /**
     * Creates a greeting message for storing in greeting administrator's mailbox or subscriber's mailbox.
     * Intended to be used for test purposes.
     *
     * @param telephoneNumber
     * @param specification   the greeting specification for the greeting to store
     * @param object          the mediaobject containing the greeting. No check is done of the validity of the media object,
     *                        i.e. it is possible to store an audio media object as a video greeting
     * @param from
     * @throws ProfileManagerException if information cannot be added to the message
     */
    protected Message createMessage(String telephoneNumber, GreetingSpecification specification, IMediaObject object, String from) throws ProfileManagerException {
        Message javaMailMessage = new MimeMessage(getContext().getStoreManager().getSession());
        try {
            if (specification.getType() != GreetingType.DIST_LIST_SPOKEN_NAME) {
                javaMailMessage.setHeader(GREETING_TYPE, GreetingUtils.getTypeHeader(specification));
                javaMailMessage.setHeader(GREETING_FORMAT, GreetingUtils.getFormatHeader(specification.getFormat()));
            }
            javaMailMessage.setSubject(GreetingUtils.getSubjectString(specification));
            javaMailMessage.setFrom(new InternetAddress(from));
            javaMailMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(telephoneNumber));
            MimeMultipart mimeMultipart = new MimeMultipart(GREETING_SUBTYPE);
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDataHandler(new DataHandler(new DataSourceAdapter(object, "greeting")));
            MediaProperties mediaProperties = object.getMediaProperties();
            if (mediaProperties.hasLengthInUnit(MILLISECONDS)) {
                long duration = mediaProperties.getLengthInUnit(MILLISECONDS) / 1000;
                // Todo: get description from application?
                mimeBodyPart.setHeader(CONTENT_DESCRIPTION, "Greeting Message (" + duration + " seconds)");
                mimeBodyPart.setHeader(CONTENT_DURATION, Long.toString(duration));
            }

            mimeBodyPart.setHeader(CONTENT_DISPOSITION, GreetingUtils.getDispositionString(specification.getFormat()));
            mimeBodyPart.setFileName(GreetingUtils.getGreetingFileName(specification, mediaProperties.getContentType()));
            mimeMultipart.addBodyPart(mimeBodyPart);
            javaMailMessage.setContent(mimeMultipart);
            javaMailMessage.saveChanges();
        } catch (MessagingException e) {
            throw new ProfileManagerException(e.getMessage());
        }
        return javaMailMessage;
    }
}
