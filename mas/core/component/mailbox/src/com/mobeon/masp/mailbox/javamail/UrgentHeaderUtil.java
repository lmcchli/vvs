/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.HeaderTerm;
import java.util.regex.Pattern;

/**
 * @author QHAST
 */
public class UrgentHeaderUtil {

    static final String HEADERNAME_X_PRIORITY = "X-Priority";
    static final String HEADERNAME_PRIORITY   = "Priority";
    static final String HEADERNAME_IMPORTANCE = "Importance";


    private static final Pattern URGENT_X_PRIORITY_PATTERN = Pattern.compile("([12])|([12] .*)|([Hh][Ii][Gg][Hh])");

    public static final SearchTerm URGENT_SEARCHTERM = new OrTerm(new SearchTerm[]{
                new HeaderTerm(HEADERNAME_IMPORTANCE,"High"),
                new HeaderTerm(HEADERNAME_PRIORITY,"Urgent"),
                new HeaderTerm(HEADERNAME_X_PRIORITY,"1"),
                new HeaderTerm(HEADERNAME_X_PRIORITY,"2"),
                new HeaderTerm(HEADERNAME_X_PRIORITY,"High")
                }
            );

    public static void setUrgentHeader(Message message) throws MessagingException {
        message.setHeader(HEADERNAME_X_PRIORITY,"2");
        message.setHeader(HEADERNAME_IMPORTANCE,"High");
    }

    public static void unsetUrgentHeader(Message message) throws MessagingException {
        message.removeHeader(HEADERNAME_X_PRIORITY);
        message.removeHeader(HEADERNAME_PRIORITY);
        message.removeHeader(HEADERNAME_IMPORTANCE);
    }

    public static boolean isUrgent(Message message) throws MessagingException {

        String[] xPriorityHeaders = message.getHeader(HEADERNAME_X_PRIORITY);
        if(xPriorityHeaders != null) {
            for(String header: xPriorityHeaders) {
                if(URGENT_X_PRIORITY_PATTERN.matcher(header).matches()) {
                    return true;
                }
            }
        }

        String[] priorityHeaders = message.getHeader(HEADERNAME_PRIORITY);
        if(priorityHeaders != null) {
            for(String header: priorityHeaders) {
                if(header.equalsIgnoreCase("Urgent")) {
                    return true;
                }
            }
        }

        String[] importanceHeaders = message.getHeader(HEADERNAME_IMPORTANCE);
        if(importanceHeaders != null) {
            for(String header: importanceHeaders) {
                if(header.equalsIgnoreCase("High")) {
                    return true;
                }
            }
        }

        return false;
    }

}
