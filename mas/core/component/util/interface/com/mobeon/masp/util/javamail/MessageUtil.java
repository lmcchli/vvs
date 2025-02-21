/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.javamail;

import org.eclipse.angus.mail.imap.IMAPMessage;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.mail.Message;

/**
 * @author Håkan Stolt
 */
public class MessageUtil {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(MessageUtil.class);

    public static void enableStorageFlagKeeping(Message message) {
        if(message instanceof IMAPMessage) {
            ((IMAPMessage) message).setPeek(true);
        } else {
            LOGGER.debug("Enabling \"Store Flag Keeping\" is unsupported for "+message.getClass().getName());
        }

    }
}
