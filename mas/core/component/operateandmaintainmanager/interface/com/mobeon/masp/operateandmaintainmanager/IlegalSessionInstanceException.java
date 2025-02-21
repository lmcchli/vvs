package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class IlegalSessionInstanceException extends Exception {
    public IlegalSessionInstanceException(String message) {
        super(message);
    }

    public IlegalSessionInstanceException(String message, Throwable exception) {
        super(message, exception);
    }

}
