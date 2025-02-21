/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.IMessageContent;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mediaobject.IMediaObject;
import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.*;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.util.javamail.PartParser;
import com.mobeon.masp.util.content.PageCounter;
import com.mobeon.masp.util.content.ContentSizePredicter;

import jakarta.activation.MimeTypeParseException;
import jakarta.mail.*;
import jakarta.mail.internet.MimePart;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author qhast
 */
public class JavamailPartAdapter implements IMessageContent {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(JavamailPartAdapter.class);

    private MimePart part;
    private JavamailContext context;
    private MediaProperties mediaProperties;
    private MessageContentProperties contentProperties;
    private IMediaObject mediaObject;
    private JavamailFolderAdapter folderAdapter;

    protected JavamailPartAdapter(MimePart part, JavamailContext context, JavamailFolderAdapter folderAdapter) {
        this.part = part;
        this.context = context;
        this.folderAdapter = folderAdapter;
    }

    public MediaProperties getMediaProperties() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMediaProperties()");
        if (mediaProperties == null) {
            parseProperties();
        }
        if (LOGGER.isInfoEnabled()) LOGGER.info("getMediaProperties() returns " + mediaProperties);
        return mediaProperties;
    }

    public MessageContentProperties getContentProperties() throws MailboxException {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getContentProperties()");
        if (contentProperties == null) {
            parseProperties();
        }
        if (LOGGER.isInfoEnabled()) LOGGER.info("getContentProperties() returns " + contentProperties);
        return contentProperties;
    }

    public IMediaObject getMediaObject() throws MailboxException {

        if (LOGGER.isInfoEnabled()) LOGGER.info("getMediaObject()");

        if (mediaObject == null) {
            try {
                context.getMailboxLock().lock();
                folderAdapter.open();
                mediaObject = context.getMediaObjectFactory().create(
                        part.getInputStream(),
                        part.getSize(),
                        getMediaProperties()
                );
                ContentSizePredicter.learn(part.getSize(), mediaObject.getSize(), part.getEncoding(), getMediaProperties()
                        .getContentType().getBaseType());
            } catch (Exception e) {
                throw new MailboxException("Failed to create media object. " + e.getMessage(), e);
            } finally {
                context.getMailboxLock().unlock();
            }
        }

        if (LOGGER.isInfoEnabled()) LOGGER.info("getMediaObject() returns " + mediaObject);
        return mediaObject;
    }

    public MimePart getPart() {
        return part;
    }

    private void parseProperties() throws MailboxException {

        if(LOGGER.isDebugEnabled()) LOGGER.debug("Parsing content properties.");

        contentProperties = new MessageContentProperties();
        mediaProperties = new MediaProperties();

        try {
            context.getMailboxLock().lock();

            PartParser.Result parsedPart = PartParser.parse(part);            

            //Set content type
            mediaProperties.setContentType(parsedPart.getContentType());

            //Set media length in milliseconds
            if(parsedPart.getDuration() != null) {
                mediaProperties.addLengthInUnit(MILLISECONDS, parsedPart.getDuration() * 1000);
            }

            //Set file name and extension.
            if (parsedPart.getFilename() == null) {
                if(LOGGER.isDebugEnabled()) LOGGER.debug("Could not find file name or extension.");
            } else {
                mediaProperties.setFileExtension(parsedPart.getFilename().getExtension());
                contentProperties.setFilename(parsedPart.getFilename().getName());
            }

            //Set description
            contentProperties.setDescription(parsedPart.getDescription());

            //Set media length in pages
            String pageCounterKey = mediaProperties.getContentType().getBaseType();
            PageCounter pc = context.getPageCounterMap().get(pageCounterKey);
            if(pc != null) {
                try {
                    if(LOGGER.isDebugEnabled()) LOGGER.debug("Counting pages in "+pageCounterKey+" content with "+pc);
                    long pages = pc.countPages(new InputStreamReader(part.getInputStream()));                    
                    if(pages >= 0) {
                        mediaProperties.addLengthInUnit(PAGES,pages);
                    }
                } catch (IOException e) {
                    throw new MailboxException("Exception while counting pages in a "+pageCounterKey+" bodypart.",e);
                }
            }

            //Set size
            mediaProperties.setSize(
                    ContentSizePredicter.predict(
                            part.getSize(),
                            part.getEncoding(),
                            parsedPart.getContentType().getBaseType()
                    ));

        } catch (MimeTypeParseException e) {
            throw new MailboxException("Exception while parsing content properties.",e);
        } catch (MessagingException e) {
            throw new MailboxException("Exception while parsing content properties.",e);
        } finally {
            context.getMailboxLock().unlock();
        }

    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("contentProperties=").append(contentProperties);
        sb.append(",mediaProperties=").append(mediaProperties);
        sb.append(",mediaObject=").append(mediaObject);
        sb.append("}");
        return sb.toString();
    }

}
