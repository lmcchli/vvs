package com.mobeon.masp.mailbox.javamail;

import jakarta.mail.search.FlagTerm;
import jakarta.mail.Flags;

/**
 * @author mande
 */
public class MessageIdHeaderUtil {
    static final String HEADERNAME_MESSAGEID = "Message-ID";
    static final FlagTerm NOT_DELETED_SEARCHTERM = new FlagTerm(new Flags(Flags.Flag.DELETED), false);
}
