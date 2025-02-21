/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.compiler.Constants;

public class RuntimeFactories {

    static RuntimeFactory CCXMLFactory = new com.mobeon.masp.execution_engine.ccxml.runtime.Factory();
    static RuntimeFactory voiceXMLFactory = new com.mobeon.masp.execution_engine.voicexml.runtime.Factory();

    public static RuntimeFactory getInstance(String mimetype) {
        if(mimetype.equals(Constants.MimeType.VOICEXML_MIMETYPE))
            return voiceXMLFactory;
        if(mimetype.equals(Constants.MimeType.CCXML_MIMETYPE))
            return CCXMLFactory;
        return null;
    }
}
