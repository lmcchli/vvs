/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * Thrown from the Mailbox address parser when an Address is not valid.
 * @author qhast
 * @see Address
 */
public class AddressParseException extends IllegalArgumentException {

    private String invalidAddressString;

    public AddressParseException(String invalidAddressString) {
        this(null,invalidAddressString);
    }

    public AddressParseException(String message, String invalidAddressString) {
        super(((message==null || message.length()==0)?"":message+": ")+"Invalid address string \""+invalidAddressString+"\"!");
        this.invalidAddressString = invalidAddressString;
    }

    public String getInvalidAddressString() {
        return invalidAddressString;
    }

}
