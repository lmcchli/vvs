package com.mobeon.ntf.util;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentDisposition;
import java.text.DecimalFormat;

/**
 * Date: 2007-okt-04
 *
 * @author emahagl
 */
public class PartParser {

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
                } catch (java.text.ParseException e) {
                    System.out.println("Could not parse " + HEADER_NAME_DURATION + " to a Number." + e);
                }
            }
        } catch (MessagingException e) {
            System.out.println("Exception while getting header " + HEADER_NAME_DURATION + "." + e);
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
                System.out.println("Could not find file extension in either \"Content-Type\" or \"" + HEADER_NAME_DISPOSITION + "\".");
            }

        } catch (Exception e) {
            System.out.println("File extension could not be set, due Exception while determining file extension." + e);
        }

        try {
            //Set description
            String[] descriptions = part.getHeader(HEADER_NAME_DESCRIPTION);
            if (descriptions != null && descriptions.length > 0) {
                result.description = descriptions[0];
            }
        } catch (MessagingException e) {
            System.out.println("Exception while getting header " + HEADER_NAME_DESCRIPTION + "." + e);
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
