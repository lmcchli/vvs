/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip.header;

/**
 * An enumeration of all content types carried in SIP messages that currently
 * are of interest for the Call Manager.
 *
 * @author Malin Flodin
 */
public enum SipContentSubType {
    MEDIA_CONTROL, SDP, GTD, SIMPLE_MESSAGE_SUMMARY;
}
