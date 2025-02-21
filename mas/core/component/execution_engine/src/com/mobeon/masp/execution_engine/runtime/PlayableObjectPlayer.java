/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;
import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.InputAggregator;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.PropertyStack;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.util.TimeValue;
import com.mobeon.masp.util.TimeValueParser;

import java.util.concurrent.TimeUnit;
import java.util.Map;

/**
 * An utility class for managing playing/streaming of {@link PlayableObjectImpl}
 *
 * @author David Looberger
 */
public class PlayableObjectPlayer {

    private static final ILogger log = ILoggerFactory.getILogger(PlayableObjectPlayer.class);

    private static Predicate playFinishedHandler;
    private static Predicate playFinishedHangupHandler;
    private static Predicate playFailedHandler;

    static {
        playFinishedHandler = new PredicateImpl(null, null, DebugInfo.getInstance());
        playFinishedHandler.add(new AtomicExecutable(Ops.onPlayEvent(),Ops.setPlayingObject(null), Ops.waitForEvents()));

        playFinishedHangupHandler = new PredicateImpl(null, null, DebugInfo.getInstance());
        playFinishedHangupHandler.add(new AtomicExecutable(Ops.onPlayEvent(), Ops.setAbortPrompts(), Ops.setPlayingObject(null), Ops.waitForEvents()));

        playFailedHandler = new PredicateImpl(null, null, DebugInfo.getInstance());
        playFailedHandler.add(new AtomicExecutable(Ops.setPlayObjectToAlternative(), Ops.playObject(), Ops.changeExecutionResult(ExecutionResult.DEFAULT)));

    }


    /**
     * Plays the current registed {@link PlayableObject} in the supplied context. It registers
     * event handlers for handling play.finished and play.failed events.
     * If the playable object is null, the funktion returns.
     *
     * @param ex
     */
    public static void play(VXMLExecutionContext ex) {

        PlayableObject playable = PromptQueue(ex).getPlayableObject();
        if (playable == null) {
            if (log.isDebugEnabled()) log.debug("No playable media to play");
            return;
        }
        collectMarkInfo(ex);

        if(bargeInTrueAndBufferedControlToken(ex)){
            if (log.isDebugEnabled()) log.debug("There is buffered control token and bargein is true. Will not play any prompts.");
            ex.getPromptQueue().clearPlayableObjects();
            return;
        }

        // If all prompts are played, start the noinput timeout.
        if (playable instanceof StartNoInputTimer &&
                ex.getFIAState().getUseNoInputTimeout()) {
            doStartNoInputTimer(playable, ex, log);
            return;
        }

        // Check if the object simpy is a Mark
        if (playable.getMarkText() != null) {
            if (log.isDebugEnabled())
                log.debug("Setting mark " + playable.getMarkText());
            ex.setCurrentMark(playable.getMarkText());
            collectMarkInfo(ex);
            return;
        }

        IMediaObject[] mediaObjects = playable.getMedia();
        if (mediaObjects == null ||
                mediaObjects.length == 0) {
            if (log.isDebugEnabled())
                log.debug("No media objects in the playable");
            if (playable.getAlternative() != null) {
                play(playable.getAlternative(), ex);
                return;
            } else {
                return;
            }
        }

        // Register handlers
        addEventHandler(ex, Constants.Event.PLAY_FINISHED_HANGUP, playFinishedHangupHandler);
        addEventHandler(ex, Constants.Event.PLAY_FINISHED, playFinishedHandler);
        addEventHandler(ex, Constants.Event.PLAY_FAILED, playFailedHandler);

        // Call play on stream
        if (log.isDebugEnabled())
            log.debug("Playing media objects (" + mediaObjects.length + " items)");
        // ex.setAbortPrompts(false);
        tryPlay(ex, mediaObjects, true);

    }

    private static boolean bargeInTrueAndBufferedControlToken(VXMLExecutionContext ex){
                Map<String, String> origProps = ex.getCurrentPromptProperties();

        String currentBargeIn = origProps.get(Constants.VoiceXML.BARGEIN);
        return (currentBargeIn != null && currentBargeIn.equals("true")) &&
                (InputAggregator(ex.getRedirector()).hasControlToken());
    }

    private static void doStartNoInputTimer(PlayableObject playable, VXMLExecutionContext ex, ILogger log) {
        if(ex.getFIAState().isExiting()) {
            if (log.isDebugEnabled()) {
                log.debug("Does not start noinput timer since document is exitiing.");
            }
            return;
        }
        StartNoInputTimer noInp = (StartNoInputTimer) playable;
        String timeoutString = noInp.getTimeout();
        if (timeoutString == null) {
            timeoutString = ex.getProperties().getProperty(Constants.VoiceXML.TIMEOUT);
        }
        // The timeout string should already be validated when we get here, so we only bother to retrieve the values
        TimeValue timeout = TimeValueParser.getTime(timeoutString);

        if (timeout == null) {
            timeout = new TimeValue(0, TimeUnit.SECONDS);
        }
        if (log.isInfoEnabled()) log.info("Starting noinput timeout: Waiting for " +
                timeout.getTime() + " " + timeout.getUnit());
        ex.getNoInputSender().start(timeout, ex.getFIAState().getFieldId());
    }

    private static void tryPlay(VXMLExecutionContext ex, IMediaObject[] mediaObjects, boolean doWait) {

        String msg = "Exception from stream when we tried to play";
        try {
            Long offset = null;
            PropertyStack properties = ex.getProperties();
            if (properties != null) {
                String audioOffsetProperty =
                        properties.getProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET);
                if (audioOffsetProperty != null) {
                    offset = Tools.parseCSS2Time(audioOffsetProperty);
                } else {
                    if (log.isInfoEnabled())
                        log.info("No audio_offset found in properties, using 0.");
                }

            } else {
                if (log.isInfoEnabled())
                    log.info("No properties found in this context.");
            }

            if (offset == null) {
                offset = 0L;
            }

            ex.getCall().play(mediaObjects, offset);
            if (doWait) {
                if (log.isDebugEnabled()) log.debug("Setting engine state to " + ExecutionResult.EVENT_WAIT);
                ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
            }
        } catch (IllegalStateException e) {
            ex.getCall().setIsPlaying(false);
            // TODO: Should change this once the CallManager/Stream stuff is refactored.
            if (log.isDebugEnabled())
                log.debug(e.getMessage());
        } catch (java.lang.IllegalArgumentException e) {
            ex.getCall().setIsPlaying(false);
            if (log.isDebugEnabled())
                log.debug(msg);
            if (log.isDebugEnabled())
                log.debug(e);
        } catch (UnsupportedOperationException e) {
            ex.getCall().setIsPlaying(false);
            if (log.isDebugEnabled())
                log.debug(msg);
            if (log.isDebugEnabled())
                log.debug(e);
        } catch (Exception e) {
            ex.getCall().setIsPlaying(false);
            if (log.isDebugEnabled())
                log.debug(e.getMessage());
        }
    }

    /**
     * Plays the suppplied {@link PlayableObject} in the supplied context.
     *
     * @param playable
     * @param ex
     */
    public static void play(PlayableObject playable, VXMLExecutionContext ex) {
        PromptQueue(ex).setPlayingObject(playable);
        play(ex);
    }

    public static void playAndForget(PlayableObject playable, VXMLExecutionContext ex) {
        if (playable.getMedia() != null) {
            tryPlay(ex, playable.getMedia(), false);
        }
    }

    /**
     * Helper method for registering event handlers
     *
     * @param ex
     * @param event
     * @param handler
     */
    private static void addEventHandler(VXMLExecutionContext ex, String event, Predicate handler) {
        if (log.isDebugEnabled())
            log.debug("Adding event handler for " + event + " (" + handler + ")");
        ex.getHandlerLocator().addEventHandler(handler, event);
    }

    private static void collectMarkInfo(VXMLExecutionContext ex) {

        FIAState fiaState = ex.getFIAState();
        if (!fiaState.isInitialFiaState()) {  // null if no FIA has been running yet
            fiaState.getMarkInfo(ex);
        }
    }
}
