/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sdp;

/**
 * An internal error that occurs in the SDP handling.
 * It suggests an internal error that should not normally occur and is not
 * dependent upon remote SDP input.
 *
 * @author Malin Flodin
 */
public class SdpInternalErrorException extends Exception {

    public SdpInternalErrorException(String message) {
        super(message);
    }

    public SdpInternalErrorException(String message, Throwable exception) {
        super(message, exception);
    }

}
