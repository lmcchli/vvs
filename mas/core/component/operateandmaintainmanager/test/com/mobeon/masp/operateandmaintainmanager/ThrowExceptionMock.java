package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class ThrowExceptionMock {


    public void throwIllegalParameterException() throws IlegalServiceParametersException {
        throw new IlegalServiceParametersException("test exception",new Exception("exception"));
    }


    public void throwIlegalSessionInstanceException() throws IlegalSessionInstanceException {
        throw new IlegalSessionInstanceException("test exception",new Exception("exception"));
    }

}
