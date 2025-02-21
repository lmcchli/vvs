/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.javamail;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.string.FileName;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentDisposition;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * @author qhast
 */
public class PartParser {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(PartParser.class);

    public static final DecimalFormat DURATION_FORMAT = new DecimalFormat("0");

    public static String HEADER_NAME_DURATION = "Content-Duration";
    public static String HEADER_NAME_DESCRIPTION = "Content-Description";
    public static String HEADER_NAME_DISPOSITION = "Content-Disposition";

    public static Result parse(Part part) throws MessagingException, MimeTypeParseException {

        Result result = new Result();

        result.contentType = new MimeType(part.getContentType().toLowerCase());

        try {
            //Set length
            String[] durations = part.getHeader(HEADER_NAME_DURATION);
            if (durations != null && durations.length > 0) {
                try {
                    result.duration = DURATION_FORMAT.parse(durations[0]).longValue();
                } catch (ParseException e) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("Could not parse " + HEADER_NAME_DURATION + " to a Number.", e);
                }
            }
        } catch (MessagingException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Exception while getting header " + HEADER_NAME_DURATION + "."+e.getMessage(), e);
        }

        try {

            //Try Content-Disposition
            String[] dispositions = part.getHeader(HEADER_NAME_DISPOSITION);
            if (dispositions != null && dispositions.length > 0) {
                ContentDisposition disposition = new ContentDisposition(dispositions[0]);
                String dispositionFilename = disposition.getParameter("filename");
                if (dispositionFilename != null) {
                    result.filename = FileName.createFileName(dispositionFilename);
                }
            }

            if (result.filename == null) {
                //Try Content-Type
                String contentTypeFilename = result.contentType.getParameter("name");
                if (contentTypeFilename != null) {
                    result.filename = FileName.createFileName(contentTypeFilename);
                }
            }

            if (result.filename == null) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Could not find file extension in either \"Content-Type\" or \"" + HEADER_NAME_DISPOSITION + "\".");
            }

        } catch (Exception e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("File extension could not be set, due Exception while determining file extension.", e);
        }

        try {
            //Set description
            String[] descriptions = part.getHeader(HEADER_NAME_DESCRIPTION);
            if (descriptions != null && descriptions.length > 0) {
                result.description = descriptions[0];
            }
        } catch (MessagingException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Exception while getting header " + HEADER_NAME_DESCRIPTION + ".", e);
        }

        return result;
    }


    public static class Result {

        private String description = null;
        private FileName filename = null;
        private MimeType contentType;
        private Long duration = null;

        private Result() {
        }

        public String getDescription() {
            return description;
        }

        public FileName getFilename() {
            return filename;
        }

        public MimeType getContentType() {
            return contentType;
        }

        public Long getDuration() {
            return duration;
        }

    }


}
