/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.Event;

/**
 * This event indicates that a recognition has completed successfully.
 */
public class RecognitionCompleteEvent implements Event {
    private String nlsmlDocument;

    /**
     * The constructor.
     * @param nslmlDocument the recignized "utterance".
     */
    public RecognitionCompleteEvent(String nslmlDocument) {
        this.nlsmlDocument = nslmlDocument;
    }

    /**
     * Utterance getter.
     * @return the utterance in NLSML (Natural Language Semantics Markup Language)
     */
    public String getNlsmlDocument() {
        return nlsmlDocument;
    }
}
