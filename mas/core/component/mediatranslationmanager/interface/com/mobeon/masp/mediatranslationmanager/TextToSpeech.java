/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * Translates the textual to speech.
 * The text to be spoken is contained in a MediaObject. The speech (audio) is returned
 * through an OutboundMediaStream. It is also possible to pause, resume and stop the
 * speech.
 */
public interface TextToSpeech {
    /**
     * Translates a MediaObject (the textual contents) to speech on a stream.
     * @param mediaObject containts the text to be translated.
     * @param outbound the speech is transmitted through this stream.
     */
    public void translate(IMediaObject mediaObject, IOutboundMediaStream outbound);

    /**
     * Performs control actions on an ongoing text to speech translation.
     * @param action an action can be one of "pause", "resume" or "stop".
     * @param data ignored.
     */
    public void control(String action, String data);

    /**
     * Opens/initalizes a text to speech translation session.
     * The speech will be returned/transmitted through an outbound
     * stream.
     * @param outbound through which the speech is transmitted.
     */
    public void open(IOutboundMediaStream outbound);

    /**
     * Performs text to speech translation.
     * The text contained in the media object is translated to speech
     * and transmitted through the outbound stream from a prior call of
     * the open() method.
     * @param mediaObject containing the text to be translated.
     */
    public void translate(IMediaObject mediaObject);

    /**
     * Closes an open text to speech session.
     * If there is a speech session open, it will be closed.
     */
    public void close();
}
