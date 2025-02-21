/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.masp.stream.IInboundMediaStream;

import java.util.Map;

/**
 * Performs speech recognition (ASR) according to a predefined grammar.
 * The result of the recognition is distributed by events. Upon success the
 * {@link RecognitionCompleteEvent} and otherwise the {@link RecognitionFailedEvent}.
 */
public interface SpeechRecognizer {
    /**
     * Informs the SpeechRecognizer that it's services will soon be needed.
     * Gives the implementation time to set up necessary sessions etc.
     * <strong>Note:</strong> This method is informational only, not calling
     * it before recognize must not be an error.
     */
    void prepare();


    /**
     * Activates the recognizer with the given IInboundMediaStream as dataSource.
     * Calling recognize several times in sequence should have the same effect
     * as calling <code>cancel()</code> followed by recognize.
     * @param inboundStream
     */
    void recognize(IInboundMediaStream inboundStream);

    /**
     * Cancels an ongoing recognition and sends an appropriate recognition event.
     */
    void cancel();
}
