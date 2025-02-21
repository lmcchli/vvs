/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */

package com.mobeon.masp.callmanager.sessionestablishment;

public class UnicastException extends Exception {

    public enum UnicastExceptionCause {
        BAD_REQUEST           (400),
        FORBIDDEN             (403),
        EXTENSION_REQUIRED    (421),
        SERVER_INTERNAL_ERROR (500);

        private int sipErrorCode;

        UnicastExceptionCause(int sipErrorCode) {
            this.sipErrorCode = sipErrorCode;
        }

        public int getSipErrorCode() {
            return this.sipErrorCode;
        }
    }

    private UnicastExceptionCause unicastExceptionCause = UnicastExceptionCause.BAD_REQUEST;
    private String additionalInfo = null;

    public UnicastException(UnicastExceptionCause unicastCause, String additionalInfo) {
        this.unicastExceptionCause = unicastCause;
        this.additionalInfo = additionalInfo;
    }

    public UnicastException(UnicastExceptionCause unicastCause) {
        this(unicastCause, null);
    }

    public UnicastExceptionCause getUnicastExceptionCause(){
        return unicastExceptionCause;
    }

    public String getAdditionalInfo(){
        return additionalInfo;
    }
}
