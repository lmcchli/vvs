/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.masp.callmanager.sessionestablishment;

public class PreconditionException extends Exception {

    public enum PreconditionExceptionCause {
        BAD_REQUEST           (400),
        FORBIDDEN             (403),
        BAD_EXTENSION         (420),
        EXTENSION_REQUIRED    (421),
        SERVER_INTERNAL_ERROR (500),
        PRECONDITION_FAILURE  (580);

        private int sipErrorCode;

        PreconditionExceptionCause(int sipErrorCode) {
            this.sipErrorCode = sipErrorCode;
        }

        public int getSipErrorCode() {
            return this.sipErrorCode;
        }
    }

    private PreconditionExceptionCause exceptionCause = PreconditionExceptionCause.BAD_REQUEST;
    private String additionalInfo = null;

    public PreconditionException(PreconditionExceptionCause exceptionCause, String additionalInfo) {
        this.exceptionCause = exceptionCause;
        this.additionalInfo = additionalInfo;
    }

    public PreconditionException(PreconditionExceptionCause exceptionCause) {
        this(exceptionCause, null);
    }

    public PreconditionExceptionCause getExceptionCause(){
        return exceptionCause;
    }

    public String getAdditionalInfo(){
        return additionalInfo;
    }

}
