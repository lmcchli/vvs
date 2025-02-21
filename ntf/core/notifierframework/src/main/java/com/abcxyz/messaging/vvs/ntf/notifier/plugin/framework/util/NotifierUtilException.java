/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

public class NotifierUtilException extends Exception {

    public enum NotifierUtilMessageException {
        TEMPORARY_ERROR,
        PERMANENT_ERROR
    }

    private NotifierUtilMessageException error = NotifierUtilMessageException.TEMPORARY_ERROR;

    public NotifierUtilException(String message) {
        super(message);
    }

    public NotifierUtilException(String message, NotifierUtilMessageException error) {
        super(message);
        this.error = error;
    }

    public NotifierUtilMessageException getNotifierSendExceptionCause() {
        return error;
    }
}
