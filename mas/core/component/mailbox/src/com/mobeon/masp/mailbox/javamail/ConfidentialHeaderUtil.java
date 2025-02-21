/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.HeaderTerm;

/**
 * @author QHAST
 */
public class ConfidentialHeaderUtil {

    static final String HEADERNAME_X_SENSITIVITY = "X-Sensitivity";
    static final String HEADERNAME_SENSITIVITY   = "Sensitivity";

    public static final SearchTerm CONFIDENTIAL_SEARCHTERM = new OrTerm(new SearchTerm[]{
                new HeaderTerm(HEADERNAME_SENSITIVITY,"Personal"),
                new HeaderTerm(HEADERNAME_SENSITIVITY,"Private"),
                new HeaderTerm(HEADERNAME_SENSITIVITY,"Company-Confidential"),
                new HeaderTerm(HEADERNAME_X_SENSITIVITY,"Personal"),
                new HeaderTerm(HEADERNAME_X_SENSITIVITY,"Private"),
                new HeaderTerm(HEADERNAME_X_SENSITIVITY,"Company-Confidential")
                }
        );

    public static void setConfidentialHeader(Message message) throws MessagingException {
        message.setHeader(HEADERNAME_SENSITIVITY, "Private");
    }


    public static void unsetConfidentialHeader(Message message) throws MessagingException {
        message.removeHeader(HEADERNAME_SENSITIVITY);
        message.removeHeader(HEADERNAME_X_SENSITIVITY);
    }

    public static boolean isConfidential(Message message) throws MessagingException {

        String[] sensitivityHeaders = message.getHeader(HEADERNAME_SENSITIVITY);
        if(sensitivityHeaders != null) {
            for (String header : sensitivityHeaders) {
                if (header.equalsIgnoreCase("Personal") ||
                        header.equalsIgnoreCase("Private") ||
                        header.equalsIgnoreCase("Company-Confidential")
                        ) {
                    return true;
                }
            }
        }

        String[] xSensitivityHeaders = message.getHeader(HEADERNAME_X_SENSITIVITY);
        if(xSensitivityHeaders != null) {
            for (String header : xSensitivityHeaders) {
                if (header.equalsIgnoreCase("Personal") ||
                        header.equalsIgnoreCase("Private") ||
                        header.equalsIgnoreCase("Company-Confidential")
                        ) {
                    return true;
                }
            }
        }

        return false;
    }


}
