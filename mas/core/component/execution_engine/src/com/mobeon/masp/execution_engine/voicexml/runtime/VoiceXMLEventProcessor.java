/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.ApplicationWatchdog;
import com.mobeon.masp.execution_engine.compiler.Constants;
import static com.mobeon.masp.execution_engine.compiler.Constants.VoiceXMLState.*;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.EventProcessorBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.StateAwareQueue;
import com.mobeon.masp.execution_engine.runtime.event.DialogTransferCompleteEvent;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventImpl;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRule;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;
import static com.mobeon.masp.execution_engine.runtime.event.rule.EventRules.*;
import com.mobeon.masp.execution_engine.runtime.event.rule.MutatableEventRule;
import com.mobeon.masp.execution_engine.runtime.event.rule.TargetRule;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.ReturnEvent;
import com.mobeon.masp.execution_engine.voicexml.runtime.event.rule.*;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.RecognitionCompleteEvent;
import com.mobeon.masp.mediatranslationmanager.RecognitionFailedEvent;
import com.mobeon.masp.mediatranslationmanager.RecognitionNoInputEvent;
import com.mobeon.masp.mediatranslationmanager.RecognitionNoMatchEvent;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.ControlTokenEvent;

public class VoiceXMLEventProcessor extends EventProcessorBase {
    private EventStream.Extractor extractor;
    private static final ILogger logger = ILoggerFactory.getILogger(VoiceXMLEventProcessor.class);
    private final MutatableEventRule testFilter = new MutatableEventRule(false, EventRules.TRUE_RULE);

    public VoiceXMLEventProcessor() {
        // The executionContext is NULL at this time, and the correct context
        // must be set in the queue at a later stage
        // TODO: Investigate WHY the StateAwareQueue did not work in det deployment environment
        // TODO: Investigate WHY the StateAwareQueue did not work in det deployment environment
        eventQueue = new StateAwareQueue<SimpleEvent>();
        // eventQueue = new PrioritizingQueue<SimpleEvent>();

    }

    public void processEvent(Event event) {

        if (event instanceof ControlTokenEvent) {
            VXMLExecutionContext ex = getExecutionContext();
            if (!ex.getFinalProcessingState()) {
                // Do not queue tokens if playing and bargein is false
                final boolean debugEnabled = logger.isDebugEnabled();
                if (ex.getCall().getIsPlaying() && ex.getProperties().getProperty(Constants.VoiceXML.BARGEIN).equals("false")) {
                    if (debugEnabled)
                        logger.debug("Bargein is false. Discarding DTMF token " + ((ControlTokenEvent) event).getToken()
                                .getTokenDigit());
                    return;
                }
                ApplicationWatchdog.instance().signalInputEvent(getExecutionContext().getSession().getIdentity());
                ControlToken ct = ((ControlTokenEvent) event).getToken();
                ex.getInputAggregator().addControlToken(new ControlTokenData(ct));
                if (ex.getVoiceXMLState() == STATE_WAITING) {
                    FIAState f = ex.getFIAState();
                    if (f == null) { // null if we get event before first FIA
                        super.processEvent(event);
                    } else {
                        if (!ex.getFIAState().isCollecting) {
                            super.processEvent(new SimpleEventImpl(Constants.VoiceXML.DTMFUTTERANCE_EVENT, null));
                        } else {
                            if (debugEnabled)
                                logger.debug("Already in the DTMF handler, or in State TRANSITIONING. Does not call event handler for DTMF token " + ct.getToken());
                        }
                    }
                }
            }

        } else if (isRecognitionEvent(event)) {
            handleRecognitionEvent(event);
        } else if (event instanceof DialogTransferCompleteEvent) {
            DialogTransferCompleteEvent completeEvent = (DialogTransferCompleteEvent) event;
            SimpleEventImpl se = new SimpleEventImpl(Constants.Event.DIALOG_TRANSFER_COMPLETE,
                    null,
                    completeEvent.getTarget(),
                    completeEvent.getTargetType());

            se.setRelated(event);
            super.processEvent(se);
        } else if (event instanceof SimpleEvent && ((SimpleEvent) event).getEvent().startsWith("connection.disconnect")) {
            FIAState fiaState = getExecutionContext().getFIAState();
            if (!fiaState.isInitialFiaState()) {
                fiaState.getMarkInfo(getExecutionContext());
            }
            super.processEvent(event);
        } else {
            super.processEvent(event);
        }
    }

    /**
     * @param event
     * @logs.error "State is <state> but we are in Final processing state!" - This is an internal error regarding states.
     */
    private void handleRecognitionEvent(Event event) {
        VXMLExecutionContext ex = getExecutionContext();
        final boolean debugEnabled = logger.isDebugEnabled();
        if (debugEnabled)
            logger.debug("Received ASR response " + event.getClass().toString());
        if (ex.getVoiceXMLState() == STATE_TRANSITIONING) {
            if (debugEnabled)
                logger.debug("In state " + STATE_TRANSITIONING + ", discarding speech input");
            return;
        }
        if (ex.getFinalProcessingState()) { // Should be in TRANSITIONING state if we are in Final Processing. Hence, should not be here...
            logger.error("State is " + ex.getVoiceXMLState() + " but we are in Final processing state!");
            return;
        }

        // Do not queue tokens if playing and bargein is false
        if (ex.getCall().getIsPlaying() && ex.getProperties().getProperty(Constants.VoiceXML.BARGEIN).equals("false")) {
            if (debugEnabled)
                logger.debug("Bargein is false. Discarding ASR response ");
            return;
        }


        FIAState f = ex.getFIAState();
        if (f == null) { // null if we get event before first FIA
            super.processEvent(event);
            return;
        }

        if (event instanceof RecognitionCompleteEvent) {
            f.inputReceived(ex);
            RecognitionCompleteEvent rce = (RecognitionCompleteEvent) event;
            f.setNLSMLResponse(rce.getNlsmlDocument());
            f.clearUtterance();
            //Clear all control tokens on ASR match
            Redirector.InputAggregator(ex).clearControlTokenQ();
            super.processEvent(new SimpleEventImpl(Constants.VoiceXML.ASRUTTERANCE_EVENT, DebugInfo.getInstance()));
        } else if (event instanceof RecognitionNoInputEvent) {
            super.processEvent(prepareFieldEvent(ex, new SimpleEventImpl(Constants.VoiceXML.NOINPUT, DebugInfo.getInstance())));
        } else if (event instanceof RecognitionNoMatchEvent) {
            super.processEvent(prepareFieldEvent(ex, new SimpleEventImpl(Constants.VoiceXML.NOMATCH, DebugInfo.getInstance())));
        } else if (event instanceof RecognitionFailedEvent) {
            if (ex.getVoiceXMLState() == STATE_WAITING) {
                // For now, simpy fire a error.semantic event
                if (debugEnabled)
                    logger.debug("RecognitionFailedEvent received from ASR engine, fired error.semantic");
                super.processEvent(prepareContextEvent(ex, new SimpleEventImpl(Constants.Event.ERROR_SEMANTIC, DebugInfo.getInstance())));

            }
        }
    }

    private Event prepareContextEvent(ExecutionContext ex, SimpleEventImpl simpleEvent) {
        simpleEvent.defineTarget("context", ex.getContextId());
        return simpleEvent;
    }

    private Event prepareFieldEvent(VXMLExecutionContext ex, SimpleEventImpl simpleEvent) {
        final FIAState state = ex.getFIAState();
        if (state.isInitialFiaState()) {
            logger.warn("<CHECKOUT>No FIAState existed when trying to send the field event: " + simpleEvent + " sending as context related");
        } else {
            simpleEvent.defineTarget(ex.getFieldTargetType(), state.getFieldId());
        }
        return simpleEvent;
    }

    private boolean isRecognitionEvent(Event event) {
        return event instanceof RecognitionCompleteEvent ||
                event instanceof RecognitionNoMatchEvent ||
                event instanceof RecognitionNoInputEvent ||
                event instanceof RecognitionFailedEvent;
    }

    /**
     * If necessary, create an {@link com.mobeon.masp.execution_engine.runtime.event.EventStream.Extractor}
     * with the following rules:
     * </br>
     * Any event valid under at least one of the following rules:
     * <ol>
     * <li>A record.finished or record.failed event from CCXML</li>
     * <li>A DialogStart or DialogTerminate event from CCXML</li>
     * <li>A play.finished or play.failed event from CCXML</li>
     * <li>Any event sent with this dialogs dialogId as Target</li>
     * <li>Any event sent with this contexts contextId as Target</li>
     * <li>A ControlTokenEvent from stream</li>
     * </ol>
     *
     * @logs.error "SetExecutionContext called twice !" - This is an internal error.
     */
    protected void onSetExecutionContext() {

        if (eventQueue instanceof StateAwareQueue) {
            if (logger.isDebugEnabled())
                logger.debug("Setting context to StateAwareQueue");
            ((StateAwareQueue<SimpleEvent>) eventQueue).setContext(getExecutionContext());
        }

        if (extractor == null) {
            EventStream stream = getExecutionContext().getEventStream();
            String dialogId = getExecutionContext().getDialog().getBridgePartyId();
            String contextId = getExecutionContext().getContextId();

            EventRule compoundRule = and(testFilter, or(
                    new ConnectionDisconnectRule(getExecutionContext(), dialogId, "dialog"),
                    new RecordRule(),
                    new DialogControlRule(),
                    new PlayRule(),
                    new ControlTokenRule(),
                    new TargetRule(dialogId, "dialog"),
                    new TargetRule(contextId, "context"),
                    new TargetRule(null, getExecutionContext().getFieldTargetType()),
                    new SpeechRecognizerRule()));

            extractor = stream.new Extractor(
                    FALSE_RULE, compoundRule, this);
            stream.add(extractor);
        } else {
            if (logger.isDebugEnabled()) logger.error("SetExecutionContext called twice !");
        }
    }

    public VXMLExecutionContext getExecutionContext() {
        return (VXMLExecutionContext) super.getExecutionContext();
    }

    public void postEvent(ReturnEvent event) {
        getQueue().offer(event);
    }

    public void insertTestFilter(EventRule eventRule) {
        testFilter.setRule(eventRule);
    }
}
