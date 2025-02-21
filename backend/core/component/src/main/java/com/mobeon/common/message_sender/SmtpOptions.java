/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender;

/**
 * This class works as holder SMTP options values to be passed
 * to implementgations of {@link IInternetMailSender}.
 *
 * @author Håkan Stolt
 */
public class SmtpOptions {

    /**
     * The address of the person who is sending the mail message.
     * If value is not null IInternetMailSender implementation must use
     * this value as parameter to the SMTP <tt>MAIL</tt> command.
     */
    private String envelopeFrom;

    public String getEnvelopeFrom() {
        return envelopeFrom;
    }

    public void setEnvelopeFrom(String envelopeFrom) {
        this.envelopeFrom = envelopeFrom;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("envelopeFrom=").append(envelopeFrom);
        sb.append("}");
        return sb.toString();
    }

}
