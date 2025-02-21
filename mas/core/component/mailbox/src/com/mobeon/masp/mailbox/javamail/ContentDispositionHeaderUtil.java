/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.util.string.FileName;
import com.mobeon.masp.mediaobject.MediaProperties;

import jakarta.mail.internet.ContentDisposition;
import jakarta.mail.internet.ParseException;
import jakarta.mail.Part;
import jakarta.activation.MimeTypeParseException;
import jakarta.activation.MimeType;
import java.util.regex.Pattern;
import java.util.*;

/**
 * A collection of compiled Content-Disposition string patterns. 
 * @author qhast
 */
public class ContentDispositionHeaderUtil {

    public static final String HEADERNAME_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String FILENAME_PARAMETER_NAME = "filename";

    private static final String ORIGINATOR_SPOKEN_NAME_STRING = "Originator-Spoken-Name";

    /**
     * Compiled pattern for originator spoken name string.
     */
    public static final Pattern ORIGINATOR_SPOKEN_NAME_PATTERN =
            Pattern.compile(".*\\s(voice|video|category)="+ORIGINATOR_SPOKEN_NAME_STRING+"(;.*|$)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    static ContentDisposition createContentDisposition(boolean spokenNameOfSender,MessageContentProperties contentProperties, MediaProperties mediaProperties) throws ParseException {

        ContentDisposition contentDisposition = new ContentDisposition(Part.INLINE);

        FileName fileName = FileName.createFileName(contentProperties.getFilename(),mediaProperties.getFileExtension());
        contentDisposition.setParameter(FILENAME_PARAMETER_NAME,fileName.getFullname());

        if(mediaProperties.getContentType().getPrimaryType().toLowerCase().equals("audio")) {
            contentDisposition.setParameter("voice",spokenNameOfSender?ORIGINATOR_SPOKEN_NAME_STRING:"Voice-Message");
        } else if(mediaProperties.getContentType().getPrimaryType().toLowerCase().equals("video")) {
            contentDisposition.setParameter("video",spokenNameOfSender?ORIGINATOR_SPOKEN_NAME_STRING:"Video-Message");
        } else if(spokenNameOfSender) {
            contentDisposition.setParameter("category",ORIGINATOR_SPOKEN_NAME_STRING);
        }

        return contentDisposition;
    }



}
