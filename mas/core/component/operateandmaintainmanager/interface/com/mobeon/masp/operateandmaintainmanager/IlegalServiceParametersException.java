package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class IlegalServiceParametersException extends Exception{
    public IlegalServiceParametersException(String message) {
        super(message);
    }

    public IlegalServiceParametersException(String message, Throwable exception) {
        super(message, exception);
    }
}
