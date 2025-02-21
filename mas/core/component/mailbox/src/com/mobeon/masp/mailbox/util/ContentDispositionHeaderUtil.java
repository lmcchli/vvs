/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.util;

import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.util.string.FileName;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mailbox.mfs.Constants;

import jakarta.mail.internet.ContentDisposition;
import jakarta.mail.internet.ParseException;
import jakarta.mail.Part;
import java.util.regex.Pattern;

/**
 * A collection of compiled Content-Disposition string patterns. 
 * @author qhast
 */
public class ContentDispositionHeaderUtil {


    /**
     * Compiled pattern for originator spoken name string.
     */
    public static final Pattern ORIGINATOR_SPOKEN_NAME_PATTERN =
            Pattern.compile(".*\\s(voice|video|category)="+Constants.ORIGINATOR_SPOKEN_NAME_STRING+"(;.*|$)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public static ContentDisposition createContentDisposition(boolean spokenNameOfSender,MessageContentProperties contentProperties, MediaProperties mediaProperties) throws ParseException {

        ContentDisposition contentDisposition = new ContentDisposition(Part.INLINE);

        FileName fileName = FileName.createFileName(contentProperties.getFilename(),mediaProperties.getFileExtension());
        contentDisposition.setParameter(Constants.FILENAME_PARAMETER_NAME,fileName.getFullname());

        if(mediaProperties.getContentType().getPrimaryType().toLowerCase().equals("audio")) {
            contentDisposition.setParameter("voice",spokenNameOfSender?Constants.ORIGINATOR_SPOKEN_NAME_STRING:"Voice-Message");
        } else if(mediaProperties.getContentType().getPrimaryType().toLowerCase().equals("video")) {
            contentDisposition.setParameter("video",spokenNameOfSender?Constants.ORIGINATOR_SPOKEN_NAME_STRING:"Video-Message");
        } else if(spokenNameOfSender) {
            contentDisposition.setParameter("category",Constants.ORIGINATOR_SPOKEN_NAME_STRING);
        }

        return contentDisposition;
    }



}
