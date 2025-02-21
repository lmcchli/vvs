/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is the MRCP DEFINE-GRAMMAR request.
 */
public class DefineGrammarRequest extends RtspRequest {
    public DefineGrammarRequest(String mimeType, String grammar, String grammarId) {
        super("ANNOUNCE", true);
        MrcpMessage mrcp = new MrcpRequest("DEFINE-GRAMMAR");
        mrcp.setContent(mimeType, grammar);
        setMrcpMessage(mrcp);
        mrcp.setHeaderField("Content-Id", grammarId);
        setMrcpMessage(mrcp);
    }
}
