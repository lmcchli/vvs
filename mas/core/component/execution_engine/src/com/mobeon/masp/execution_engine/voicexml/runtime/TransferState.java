package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 17, 2006
 * Time: 7:17:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransferState {

    public enum CallState {
        IDLE, // intial state. From here we may attempt a transfer
        INITIATING,
        ONGOING, // there is currently a transfer (dialog.transfer has been delivered)
        FINALIZING, // terminating state. there is no longer a transfer. This can be
                    // due to DTMF beingreceived before transfer got into ONGOING state
        FINALIZING_WITH_ERROREVENT
                    //Same as finalizing except that an error has occured and must be delivered
                    //before resuming execution.
    }



    // same as OVER, expect that an event shall be thrown to the application
    public enum TransferAudioCancelState {IDLE, CANCELLING}


    private CallState callState = CallState.IDLE;
    private TransferAudioCancelState transferAudioCancelState = TransferAudioCancelState.IDLE;
    /**
     * This event is used e.g. when TransferEventHandler need sto remember what event to throw at later point
     */
    private SimpleEvent event;

    public SimpleEvent getEvent() {
        return event;
    }

    public void setEvent(SimpleEvent event) {
        this.event = event;
    }

    public CallState getCallState() {
        return callState;
    }

    public void setCallState(CallState callState) {
        this.callState = callState;
    }

    public TransferAudioCancelState getTransferAudioCancelState() {
        return transferAudioCancelState;
    }

    public void setTransferAudioCancelState(TransferAudioCancelState transferAudioCancelState) {
        this.transferAudioCancelState = transferAudioCancelState;
    }

    public void resetAll() {
        callState = CallState.IDLE;
        transferAudioCancelState = TransferAudioCancelState.IDLE;
        event = null;
    }

}
