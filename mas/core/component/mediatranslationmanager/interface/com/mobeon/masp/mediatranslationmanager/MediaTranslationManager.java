/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.masp.execution_engine.session.ISession;

import java.util.Map;
import java.util.Collection;

public interface MediaTranslationManager {
    /**
     * Creates a recognizer given a set of grammars.
     * @param session the call session
     * @param grammar a set of grammar ID and grammar pairs
     * @return a speech recognizer
     */
    public SpeechRecognizer getSpeechRecognizer(ISession session, Map<String,String> grammar);

    /**
     * Creates a new recognizer given a recognizer to use as prototype.
     * @param session the call session
     * @param prototype
     * @return a speech recognizer
     */
    public SpeechRecognizer getSpeechRecognizer(ISession session, SpeechRecognizer prototype);

    /**
     * Creates a speech styntesizer
     * @param session the call session
     * @return a speech syntesizer
     */
    public TextToSpeech getTextToSpeech(ISession session);

    /**
     * Returns the languages available for TTS.
     * @return a collection of strings (eg. "se-SV", "en-GB")
     */
    public Collection<String> getTextToSpeechLanguages();

}
