package com.mobeon.masp.execution_engine.compiler.products;

import static com.mobeon.masp.execution_engine.voicexml.runtime.TransferState.CallState.*;
import static com.mobeon.masp.execution_engine.voicexml.runtime.TransferState.*;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.DialogTransferCompleteEvent;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferState;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.DialogEventFactory;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 18, 2006
 * Time: 10:04:07 PM
 * To change this template use File | Settings | File Templates.
 * <p/>
 * <p/>
 * <p/>
 * The purpose of this class is to start waiting for the transfer related events only if
 * transfer is not already terminated before started. Which can happen via an
 * early DTMF pressed.
 */
public class TransferWaitSetProduct extends WaitSetProduct {

    static ILogger log = ILoggerFactory.getILogger(TransferWaitSetProduct.class);

    public TransferWaitSetProduct(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
    }

    public TransferWaitSetProduct(Product parent, DebugInfo debugInfo) {
        super(parent, debugInfo);
    }

    protected boolean startWaiting(ExecutionContext ex) {
        VXMLExecutionContext vxe = (VXMLExecutionContext) ex;
        if (isInAnyCallState(vxe, INITIATING,ONGOING)) {
            return true;
        } else {
            if (log.isDebugEnabled()) log.debug("Transfer is terminated, didn't active the waitset");
            return false;
        }
    }

    private boolean isInAnyCallState(VXMLExecutionContext vxe, CallState ... states) {
        boolean any = false;
        CallState currentState = getCallState(vxe);
        for(CallState state:states) { any |= state == currentState; }
        return any;
    }

    public void realExecute(ExecutionContext ex, Event event) {

        VXMLExecutionContext context = (VXMLExecutionContext) ex;

        // We have received an event. Our problem is that this event handler must be active until the
        // transfer is over and any transferaudio is cancelled and playFinished reported.
        // It is also possible that we get invoked before the transfer is started

        if (event instanceof SimpleEvent) {
            SimpleEvent es = (SimpleEvent) event;

            String eventString = es.getEvent();
            if (eventString.equals(Constants.Event.PLAY_FINISHED) ||
                    eventString.equals(Constants.Event.PLAY_FAILED) ||
                    eventString.equals(Constants.Event.PLAY_FINISHED_HANGUP)) {
                handlePlayFinished(context);
            } else if (eventString.equals(Constants.Event.DIALOG_TRANSFER_COMPLETE)) {
                handleTransferComplete(context, event);
            } else if (eventString.equals(Constants.VoiceXML.DTMFUTTERANCE_EVENT)) {
                handleDTMF(context);
            } else if (eventString.startsWith(Constants.Event.ERROR_CONNECTION) ||
                    eventString.startsWith(Constants.Event.ERROR_UNSUPPORTED)) {
                handleTerminatingEvent(context, es);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("After handling the event, callState is:" + getCallState(context) + " and transferAudioCancel state is:" + getAudioState(context));
        }
        // Are we done yet?
        if (isInCallState(context, FINALIZING) && isAudioCancelled(context)) {
            // yes we are over
            context.getTransferState().setCallState(IDLE);
            context.getWaitSet().stopWaiting();
            runEngine(context);
        } else if (isInCallState(context, INITIATING)) {
            // transfer not started yet, continue executing!
            runEngine(context);
        } else if (isInCallState(context, FINALIZING_WITH_ERROREVENT) &&
                isAudioCancelled(context)) {
            // Over, and we throw an event
            context.getWaitSet().stopWaiting();
            context.getTransferState().setCallState(IDLE);
            runEngine(context);
            context.getEventHub().fireContextEvent(context.getTransferState().getEvent().getEvent(),
                    context.getTransferState().getEvent().getMessage(), DebugInfo.getInstance());
        } else if( isInCallState(context,IDLE)) {
            // Something is wrong !
            log.debug("TransferWaitSetProduct active while TransferState.callState is IDLE !");
            context.getWaitSet().stopWaiting();
            runEngine(context);
        } else {
            // not over yet
            context.setExecutionResult(ExecutionResult.EVENT_WAIT);
        }
    }

    private boolean isAudioCancelled(VXMLExecutionContext context) {
        return getAudioState(context) == TransferAudioCancelState.IDLE;
    }

    private boolean isInCallState(VXMLExecutionContext context, TransferState.CallState state) {
        return getCallState(context) == state;
    }

    private TransferState.TransferAudioCancelState getAudioState(VXMLExecutionContext context) {
        return context.getTransferState().getTransferAudioCancelState();
    }

    private TransferState.CallState getCallState(VXMLExecutionContext context) {
        return context.getTransferState().getCallState();
    }

    private void runEngine(VXMLExecutionContext context) {
        if (log.isDebugEnabled()) log.debug("Setting Engine state to " + ExecutionResult.RUN_UNCONDITIONALLY);
        context.setExecutionResult(ExecutionResult.RUN_UNCONDITIONALLY);
    }

    private void handleTerminatingEvent(VXMLExecutionContext ex, SimpleEvent event) {
        ex.getTransferState().setCallState(FINALIZING_WITH_ERROREVENT);
        ex.getTransferState().setEvent(event);
        cancelTransferAudio(ex);
    }

    private void handlePlayFinished(VXMLExecutionContext ex) {
        ex.getCall().setIsPlaying(false);
        ex.getCall().setHasReceivedPlayEvent(true);
        if (getAudioState(ex) == TransferState.TransferAudioCancelState.CANCELLING) {
            ex.getTransferState().setTransferAudioCancelState(TransferState.TransferAudioCancelState.IDLE);
        }
    }

    private void handleTransferComplete(VXMLExecutionContext ex, Event event) {
        String formItemName = ex.getFIAState().getNextItem().name;
        Event related;

        if (event != null
                && event instanceof SimpleEvent
                && (related = ((SimpleEvent) event).getRelated()) != null
                && related instanceof DialogTransferCompleteEvent) {

            ex.getTransferState().setCallState(FINALIZING);
            cancelTransferAudio(ex);
            DialogTransferCompleteEvent completeEvent = (DialogTransferCompleteEvent) related;
            setFormItemVariable(completeEvent.getReason(), formItemName, ex);
        }
    }

    private void setFormItemVariable(String reason, String formItemName, VXMLExecutionContext ex) {
        if (reason == null) {
            ex.getCurrentScope().setValue(formItemName, ex.getCurrentScope().getUndefined());
            if (log.isDebugEnabled()) {
                log.debug("set it to undefined");
            }
        } else {
            ex.getCurrentScope().setValue(formItemName, reason);
            if (log.isDebugEnabled()) {
                log.debug("set it to:" + reason);
            }
        }
    }

    private void handleDTMF(VXMLExecutionContext ex) {
        ex.getFIAState().collectDTMFUtterance(ex, true, false, false);
        if (! ex.getFIAState().matchedGrammar()) {
            log.debug("Does NOT terminate transfer, due to no match in grammar.");
            return;
        }

        switch (getCallState(ex)) {
            case ONGOING:
                if(log.isDebugEnabled())
                    log.debug("Matching utterance while transfer ongoing");
                DialogEventFactory factory = new DialogEventFactory();
                CCXMLEvent event = factory.create(
                        ex,
                        Constants.Event.TERMINATE_TRANSFER,
                        "Transfer terminated due to matching DTMF",
                        ex.getCurrentConnection(),
                        ex.getDialog(),
                        DebugInfo.getInstance());
                ex.getEventHub().fireEvent(event);
                cancelTransferAudio(ex);
                break;
            case INITIATING:
                log.debug("Received maching DTMF while initiating transfer. Transfer is considered over");
                setFormItemVariable(Constants.VoiceXML.NEAR_END_DISCONNECT, ex.getFIAState().getNextItem().name, ex);
                cancelTransferAudio(ex);
                ex.getTransferState().setCallState(FINALIZING);
                break;
            case FINALIZING:
                // The fact that we get here must mean that we are waiting for a playFinished.
                log.debug("Received matching DTMF when transfer is over, but we are still cancelling the transfer audio. This DTMF is lost");
                break;
            case IDLE:
                log.debug("Received DTMF through TransferWaitSetProduct while apparently not beeing in an active transfer !");
                break;
        }
    }

    private void cancelTransferAudio(VXMLExecutionContext ex) {
        if (ex.getCall().getIsPlaying()) {
            if (log.isDebugEnabled()) {
                log.debug("Cancelling transfer audio");
            }
            ex.getCall().stopPlay();
            ex.getTransferState().setTransferAudioCancelState(TransferState.TransferAudioCancelState.CANCELLING);
        }
    }

}

