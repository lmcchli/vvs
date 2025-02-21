/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediatranslationmanager.configuration.TextToSpeechConfiguration;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.SpeakSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.MrcpEventListener;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.MrcpEventId;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.StackException;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.util.executor.ExecutorServiceManager;

import jakarta.activation.MimeType;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Translates text to streamed speech by calling 3PP over MRCP.
 * The translation comes in two flawors:
 * 1) The client (an OutboundStream) calls translate(mediaObject, outboundStream).
 *    A translate is intitated and the method returns.
 *    Eventually the MRCP handling will call the method handleMrcpEvent() which
 *    in turn will call speakComplete() wich in turn will notify the outbound
 *    stream whether the translation failed or not.
 *    The client/caller is supposed to call control("teardown", "") in order to
 *    tear down the speak session.
 * 2) The client calls open(outboundStream) to intiate the speak session.
 *    Translation is performed by calling translate(mediaObject). And now we should
 *    be in the same state as in the previous case. Upon speakComplete() (translation
 *    failed/done) the caller can either issue another translate(mediaObject) or close()
 *    which will tear down the speak session.
 */
public class MrcpTextToSpeech implements TextToSpeech, MrcpEventListener, Runnable {
    private static ILogger logger = ILoggerFactory.getILogger(MrcpTextToSpeech.class);
    private MediaTranslationFactory factory = MediaTranslationFactory.getInstance();
    private IOutboundMediaStream outbound = null;
    private IInboundMediaStream inbound = null;
    private static final String mrcpMimeType = "application/synthesis+ssml";
    SpeakSession speakSession = null;
    private ISession session;
    private String errorMessage = null;

    public MrcpTextToSpeech(ISession session) {
        this.session = session;
    }

    /**
     * Translates text to speech (see {@link TextToSpeech}).
     * @param mediaObject contains the text.
     * @param outbound the stream to send the speech to.
     */
    public void translate(IMediaObject mediaObject, IOutboundMediaStream outbound) {
        if (logger.isDebugEnabled()) logger.debug("--> translate(mediaObject, outbound)");
        open(outbound);
        translate(mediaObject);
        if (logger.isDebugEnabled()) logger.debug("<-- translate(mediaObject, outbound)");
    }

    /**
     * Controls an ongoing translation (see {@link TextToSpeech}).
     * @param action "pause", "resume" or "stop".
     * @param data ignored.
     */
    public void control(String action, String data) {
        if ("teardown".equals(action)) close();
    }

    /**
     * Opens/initalizes a text to speech translation session.
     * The speech will be returned/transmitted through an outbound
     * stream.
     *
     * @param outbound through which the speech is transmitted.
     */
    public void open(IOutboundMediaStream outbound) {
        if (logger.isDebugEnabled()) logger.debug("--> open()");
        MediaMimeTypes mimeType = new MediaMimeTypes(factory.getPcmuMimeType());
        this.outbound = outbound;
        inbound = factory.getStreamFactory().getInboundMediaStream();
        if (logger.isInfoEnabled()) logger.info("Open MRCP TTS session");
        try {
            inbound.setEventDispatcher(outbound.getEventDispatcher());
            inbound.create(mimeType);
        } catch (StackException e) {
            logger.error("Failed to create inbound media stream: " + e);
            outbound.translationFailed("Failed to create inbound media stream: " + e);
            return;
        }
        try {
            inbound.join(outbound);
        } catch (StackException e) {
            logger.error("Failed to join inbound media stream: " + e);
            outbound.translationFailed("Failed to join inbound media stream: " + e);
            return;
        }
        int portnumber = inbound.getAudioPort();
        TextToSpeechConfiguration configuration =
                MediaTranslationConfiguration.getInstance().getTextToSpeechConfiguration();
        try {
            logger.error("PlayOutbound: Host: " + configuration.getHost() + " Port: " + configuration.getPort());
            RtspSession rtspSession = factory.createRtspSession(configuration.getHost(), configuration.getPort());
            rtspSession.setSession(session);
            speakSession = new SpeakSession(rtspSession, portnumber);
            speakSession.setup();
            speakSession.attachMrcpEventListener(this);
        } catch (Exception e) {
            logger.error("Failed to set up speech session: " + e);
            outbound.translationFailed("Failed to set up speech session: " + e);
            return;
        }
        if (logger.isDebugEnabled()) logger.debug("<-- open()");
    }

    /**
     * Performs text to speech translation.
     * The text contained in the media object is translated to speech
     * and transmitted through the outbound stream from a prior call of
     * the open() method.
     *
     * @param mediaObject containing the text to be translated.
     */
    public void translate(IMediaObject mediaObject) {
        if (logger.isDebugEnabled()) logger.debug("--> translate(mediaObject)");
        MimeType mediaObjectMimeType = mediaObject.getMediaProperties().getContentType();
        boolean shouldWrapText = false;

        // Ensure that we have an ssml
        if (logger.isDebugEnabled()) logger.debug("Mime type is: [" + mediaObjectMimeType +"]");
        if (logger.isDebugEnabled()) logger.debug("Valid types: [" + factory.getSsmlMimeType() +
                "," + factory.getPlainMimeType() + "]");
        if (factory.getPlainMimeType().toString().equals(mediaObjectMimeType.toString())) {
            shouldWrapText = true;
        } else if (!factory.getSsmlMimeType().toString().equals(mediaObjectMimeType.toString())) {
            logger.error("Unknown mime type: [" + mediaObjectMimeType + "]");
            outbound.translationFailed("Unknown mime type: [" + mediaObjectMimeType + "]");
            return;
        }

        if (speakSession == null || !speakSession.isOk()) {
            logger.error("Speak session failure");
            outbound.translationFailed("Speak session failure");
            return;
        }

        int size = (int)mediaObject.getSize();
        byte[] bytes = new byte[size];
        try {
            int readBytes = mediaObject.getInputStream().read(bytes, 0, size);
            if (logger.isDebugEnabled()) logger.debug("Read " + readBytes + " of " + size);
            String mediaObjectText = "";
            if (shouldWrapText) {
                mediaObjectText += "<?xml version='1.0'?>";
                mediaObjectText += "<speak version='1.0'>";
                mediaObjectText += "<s>";
            }
            mediaObjectText += new String(bytes);
            if (shouldWrapText) {
                mediaObjectText += "</s>";
                mediaObjectText += "</speak>";
            }
            if (logger.isInfoEnabled()) logger.info("Performing MRCP TTS on [" + mediaObjectText + "]");
            speakSession.speak(mrcpMimeType, mediaObjectText);
        } catch (IOException e) {
            logger.error("Failed to initiate speech session : " + e);
        }

        if (logger.isDebugEnabled()) logger.debug("<-- translate(mediaObject)");
    }

    /**
     * Closes an open text to speech session.
     * If there is a speech session open, it will be closed.
     */
    public void close() {
        if (logger.isDebugEnabled()) logger.debug("--> close()");
        if (logger.isInfoEnabled()) logger.info("Closing MRCP TTS session");
        if (inbound != null) {
            try {
                inbound.unjoin(outbound);
                inbound.delete();
            } catch (StackException e) {
                logger.error("Failed to unjoin streams: " + e);
            }
        } else {
            if (logger.isInfoEnabled()) logger.info("Null pointer inbound stream");
        }
        if (speakSession != null) {
            speakSession.teardown();
        } else {
            if (logger.isInfoEnabled()) logger.info("Null pointer session");
        }
        if (logger.isDebugEnabled()) logger.debug("<-- close()");
    }

    /**
     * This method handles MRCP event notifications.
     * @param event
     * @param reason
     */
    public void handleMrcpEvent(MrcpEventId event, String reason) {
        speakComplete(event == MrcpEventId.SPEAK_COMPLETE ? null : reason);
        // TODO: on failure close()
    }

    /**
     * This is an internal method for handling speakComplete actions taken upon
     * speak complete events.
     * @param reason
     */
    public void speakComplete(String reason) {
        if (logger.isDebugEnabled()) logger.debug("--> speakComplete(" + reason + ")");
        errorMessage = reason;
        ExecutorService service =
                ExecutorServiceManager.getInstance().getExecutorService(MrcpTextToSpeech.class);
        service.execute(this);
        if (logger.isDebugEnabled()) logger.debug("<-- speakComplete()");
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        if (logger.isDebugEnabled()) logger.debug("--> run");
        if (errorMessage == null) {
            if (logger.isInfoEnabled()) logger.info("MRCP TTS success");
            outbound.translationDone();
        } else {
            if (logger.isInfoEnabled()) logger.info("MRCP TTS failed");
            outbound.translationFailed(errorMessage);
        }
        if (logger.isDebugEnabled()) logger.debug("<-- run");
    }
}
