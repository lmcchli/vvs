/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.header;

/**
 * Contains a Content-Type consisting of a type and a sub type. 
 *
 * @author Malin Flodin
 */
public class SipContentType {
    private final String type;
    private final String subType;

    public SipContentType(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    public String toString() {
        return type + "/" + subType;
    }
}
