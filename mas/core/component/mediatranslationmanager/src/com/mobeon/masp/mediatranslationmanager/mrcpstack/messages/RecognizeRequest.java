/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack.messages;

/**
 * This is the MRCP RECOGNIZE request message.
 */
public class RecognizeRequest extends RtspRequest {
    /**
     * A constructor.
     * This message is a request for recognition according to a given grammar.
     * @param mimeType grammar mime type
     * @param grammar grammar text
     * @param grammarId grammar ID
     */
    public RecognizeRequest(String mimeType, String grammar, String grammarId) {
        super("ANNOUNCE", true);
        MrcpMessage mrcp = new MrcpRequest("RECOGNIZE");
        mrcp.setContent(mimeType, grammar);
        mrcp.setHeaderField("Content-Id", grammarId);
        setMrcpMessage(mrcp);
    }

    /**
     * A constructor.
     * This message is refering to a previously defined grammar (see {@link DefineGrammarRequest}).
     * @param grammarIds the ID of the defined grammar.
     */
    public RecognizeRequest(String ... grammarIds) {
        super("ANNOUNCE", true);
        String uriList = "";
        MrcpMessage mrcp = new MrcpRequest("RECOGNIZE");

        for (int i = 0; i < grammarIds.length; i++) {
            uriList += "session:" + grammarIds[i];
            if (i < grammarIds.length-1) uriList += nl;
        }

//        for (String grammarId : grammarIds) {
//            uriList += "session:" + grammarId + nl;
//        }
//
        mrcp.setContent("text/uri-list", uriList);

        setMrcpMessage(mrcp);
    }
}
