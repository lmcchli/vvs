package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.events.*;
import com.mobeon.masp.callmanager.events.FailedEvent.Reason;
import com.mobeon.masp.callmanager.sip.header.PEarlyMediaHeader;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.stream.*;

import jakarta.activation.MimeType;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * This is the mock class for an inbound call.
 *
 * @author Tomas Stenlund, Mobeon
 */
public class InboundCallMock extends CallMock implements InboundCall {


    /**
     * Record parameters
     */
    private long recordLength = 0L;
    private RecordFailedEvent.CAUSE recordFailedEvent = null;
    private RecordFinishedEvent.CAUSE recordFinishedEvent = null;

    /**
     * The redirecting party of this call.
     */
    private volatile RedirectingParty redirectingParty;

    /**
     * The stream factory used for creating streams for this call.
     */
    private final IStreamFactory streamFactory;

    protected boolean throwIllegalArgumentExceptionAtPlay = false;


    /*
    protected boolean rejectAcceptAttempt = false;
    protected boolean sendDisconnectAsReplyToAccept = false;
    protected boolean withHoldAcceptAttempt = false;



    */
    protected boolean withHoldRejectAttempt = false;
    protected boolean withholdnegotiateEarlyMedia = false;
    CallManagerMock.EventType responseToNegotiateEarlyMedia;
    private CallManagerMock.EventType responseToAccept;
    private CallManagerMock.EventType responseToProxy;
    private CallManagerMock.EventType responseToRecord;
    private int delayBeforeResponseAccept = 0;
    private int delayBeforeResponseToDisconnectInboundCall = 0;
    private Callable toInvokeOnPlayFinished;
    private Callable toInvokeWhenRecord;
    private String rejectEventTypeName = null;
    private String rejectReason = null;

    /**
     * Returns with the redirecting number of this call.
     *
     * @return the redirecting number associated with this call.
     */
    public RedirectingParty getRedirectingParty() {
        return redirectingParty;
    }

    /**
     * Creates a mock object used for unit verification of the execution engines
     * interfaces.
     *
     * @param applicationManagement
     * @param streamFactory
     */
    public InboundCallMock(ExecutorService executorService,
                           IApplicationManagment applicationManagement,
                           IStreamFactory streamFactory,
                           CallManagerMock.EventType responseToAccept,
                           CallManagerMock.EventType responseToProxy,
                           boolean withHoldRejectAttempt,
                           boolean withholdDisconnectAttempt,
                           boolean withholdnegotiateEarlyMedia,
                           CallManagerMock.EventType responseToNegotiateEarlyMedia,
                           CallManagerMock.EventType responseToRecord,
                           int delayBeforeResponseAccept,
                           int delayBeforeResponseToDisconnect,
                           Set<Connection> farEndConnections,
                           boolean sendPlayFailedAfterDelay,
                           int delayBeforePlayFailed,
                           int inboundBitRate) {
        super(executorService, applicationManagement,
                withholdDisconnectAttempt,
                delayBeforeResponseToDisconnect,
                farEndConnections,
                sendPlayFailedAfterDelay,
                delayBeforePlayFailed,
                inboundBitRate);
        log.info("MOCK: InboundCallMock.InboundCallMock");
        this.streamFactory = streamFactory;
        this.responseToAccept = responseToAccept;
        this.responseToProxy = responseToProxy;
        this.withHoldRejectAttempt = withHoldRejectAttempt;
        this.withholdnegotiateEarlyMedia = withholdnegotiateEarlyMedia;
        this.responseToNegotiateEarlyMedia = responseToNegotiateEarlyMedia;
        this.responseToRecord = responseToRecord;
        this.delayBeforeResponseAccept = delayBeforeResponseAccept;
    }

    /**
     * Sets the calling parameters for this mock object.
     *
     * @param type The call type
     */
    public void setCallType(CallType type) {
        this.callType = type;
    }

    /**
     * Sets the calling parameters for this mock object.
     *
     * @param a The calling party (a-number)
     * @param b The called party (b-number)
     * @param c The redirecting number (c-number)
     */
    public void setCallParameters(CallingParty a, CalledParty b, RedirectingParty c) {
        this.callingParty = a;
        this.calledParty = b;
        this.redirectingParty = c;
    }
    
    public void setPEarlyMediaValue(String value){
    	getSession().setData(PEarlyMediaHeader.NAME, value);
    }

    /**
     * Terminates the loaded service for this call
     */
    public void terminateService()

    {
    }


    /**
     * Start the loaded service
     */
    public void startCall() {
        api.start();
        fireEvent(new AlertingEvent(this));
    }
    

    /**
     * Sends a controltoken to the application.
     *
     * @param dtmf     The controltoken to send.
     * @param volume   The strength of the token.
     * @param duration The duration of the token.
     */
    public void sendDTMF(ControlToken.DTMFToken dtmf, int volume, int duration) {
        log.info("InboundCallMock.sendDTMF DTMF sent: " + dtmf);
        fireEvent(new ControlTokenEvent(new ControlToken(dtmf, volume, duration)));
    }

    /**
     * Waits for a play to finish.
     *
     * @param timeout
     * @return Returns true if the play finished, otherwise false.
     */
    public boolean waitForPlay(long timeout) {
        boolean played = false;
        long timer = 0L;
        timeout = ApplicationBasicTestCase.scale(timeout);

        log.info("MOCK: OnboundCallMock.waitForPlay");
        log.info("MOCK: OnboundCallMock.waitForPlay timeout " + timeout);

        // We must wait for an outbound stream before we can wait
        // for a play, same timeout applies !
        while (outboundStream == null && timer < timeout) {
            long shortWait = ApplicationBasicTestCase.scale(250);
            timer += shortWait;
            try {
                Thread.sleep(shortWait);
            } catch (InterruptedException e) {
                log.info("MOCK: OnboundCallMock.waitForPlay Interrupted caused by application finished or stop request");
                return false;
            }
        }

        // Now, wait for the media to be played on this stream
        if (outboundStream != null && timer < timeout)
            played = outboundStream.waitForPlay(timeout - timer);

        if (played)
            log.info("MOCK: OnboundCallMock.waitForPlay Detected end of play");
        else
            log.info("MOCK: OnboundCallMock.waitForPlay Timeout detected");

        // So, did it get played or not ?
        return played;
    }

    /**
     * Wait for the execution of the testcase, i.e. the execution context so
     * we know when the testcase has finished.
     *
     * @param totalTimeToWait The maximum amount in milliseconds to wait before it returns.
     * @return True if the execution has finished before the time ran out, false otherwise.
     */

    /**
     * This method is used to accept the caller's call request.
     */
    public void accept() {
        log.info("MOCK: InboundCallMock.accept");

        // Create the media
        synchronized (this) {
            inboundStream = (InboundMediaStreamMock) streamFactory.getInboundMediaStream();
            inboundStream.setEventDispatcher(this.getEventDispatcher());
            if (recordFailedEvent != null)
                inboundStream.setRecordFailed(this.recordLength, recordFailedEvent);
            if (recordFinishedEvent != null)
                inboundStream.setRecordFinished(this.recordLength, recordFinishedEvent);
            inboundStream.setResponseToRecord(responseToRecord);
            inboundStream.invokeWhenRecord(toInvokeWhenRecord);
        }
        // the outbound stream is either created now, or at negotiateEarlyMedia
        if (outboundStream == null) {
            outboundStream = (OutboundMediaStreamMock) streamFactory.getOutboundMediaStream();
            outboundStream.setEventDispatcher(this.getEventDispatcher());
            outboundStream.setThrowIllegalArgumentAtPlay(throwIllegalArgumentExceptionAtPlay);
            outboundStream.invokeWhenPlayFinished(toInvokeOnPlayFinished);
        }

        if (delayBeforeResponseAccept == 0) {
            doResponseToAccept();
        } else {
            new Thread() {
                public void run() {
                    try {
                        sleep(delayBeforeResponseAccept);
                        doResponseToAccept();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }
    }

    private void doResponseToAccept() {
        switch (responseToAccept) {
            case ERROR_CONNECTION:
                fireEvent(new ErrorEvent(this,
                        CallDirection.INBOUND, "Rejecting the accept since the test case is setup like that", false));
                break;
            case WITHHOLD:
                // OK, we do nothing
                break;
            case DISCONNECTED_EVENT:
                fireEvent(new DisconnectedEvent(this, DisconnectedEvent.Reason.FAR_END, false));
                break;
            case ERROR_NOT_ALLOWED:
                fireEvent(new NotAllowedEvent(this, "Sending NotAllowedEvent since the test case is setup like that"));
                break;
            default:
                log.info("MOCK: InboundCallMock.accept: firing ConnectedEvent");
                // Fire connected event
                fireEvent(new ConnectedEvent(this));
                break;
        }
        TestEventGenerator.generateEvent(TestEvent.ACCEPT);
    }

    /**
     * Hangs up a started call.
     */
    public void disconnectCall() {

        log.info("MOCK: InboundCallMock.disconnect");
        if (withHoldDisconnectAttempt) {
            log.info("MOCK: InboundCallMock.disconnect: withholding the disconnect");
        } else {
            fireEvent(new DisconnectedEvent(
                    this, DisconnectedEvent.Reason.FAR_END, false));
            if (inboundStream != null) {
                try {
                    inboundStream.stop();
                } catch (StackException e) {
                    log.error("InboundCallMock.disconnect: stackexception", e);
                }
            }
            if (outboundStream != null) {
                try {
                    outboundStream.stop();
                } catch (StackException e) {
                    log.error("InboundCallMock.disconnect: stackexception", e);
                }
            }
            inboundStream = null;
            outboundStream = null;
        }
    }

    /**
     * This method is used to reject a call request.
     * A call can only be rejected if it has not yet been accepted.
     *
     * @throws IllegalStateException if used for a call that
     *                               already has been accepted.
     */
    public void reject(String rejectEventTypeName, String reason) {
        log.info("MOCK: InboundCallMock.reject rejectEventTypeName=" + rejectEventTypeName + " reason=" + reason);
        this.rejectEventTypeName = rejectEventTypeName;
        this.rejectReason = reason;
        if (withHoldRejectAttempt) {
            log.info("MOCK: InboundCallMock.reject: withholding the reject");
        } else {
            fireEvent(new FailedEvent(this, FailedEvent.Reason.REJECTED_BY_NEAR_END, CallDirection.INBOUND, "Sending failedEvent as response to reject", 0));
        }
    }
    
    public String getRejectEventTypeName(){
    	return rejectEventTypeName;
    }
    
    public String getRejectReason(){
    	return rejectReason;
    }
    
    /**
     * This method is used to proxy a call request.
     */
    public void proxy(RemotePartyAddress remoteAddress) {
        log.info("MOCK: InboundCallMock.proxy");
        switch (responseToProxy) {
	        case DISCONNECTED_EVENT:
	        	fireEvent(new DisconnectedEvent(this, DisconnectedEvent.Reason.FAR_END, false));
	        	break;
	        case ERROR_CONNECTION:
	        	fireEvent(new ErrorEvent(this, CallDirection.INBOUND, "Sending ErrorEvent since the test case is setup like that.", false));
	        	break;
	        case FAILED_EVENT:
	        	fireEvent(new FailedEvent(this, Reason.REJECTED_BY_FAR_END, CallDirection.INBOUND, "Sending FailedEvent since the test case is setup like that.", 0));
	        	break;
	        case ERROR_NOT_ALLOWED:
	            fireEvent(new NotAllowedEvent(this, "Sending NotAllowedEvent since the test case is setup like that."));
	        	break;
            case WITHHOLD:
                // OK, we do nothing
                break;
	        default:
	            fireEvent(new ProxiedEvent(this));
	        	break;
        }
    }

    /**
     * This method is used to indicate that early media shall be played for the
     * call and that which media type to use must be negotiated before the call
     * is connected.
     */
    public void negotiateEarlyMediaTypes() {
        log.info("MOCK: InboundCallMock.negotiateEarlyMediaTypes");
        if (withholdnegotiateEarlyMedia) {
            log.info("MOCK: InboundCallMock.negotiateEarlyMediaTypes: withholding the negotiateEarlyMediaTypes");
        } else {
            if (outboundStream == null) {
                outboundStream = (OutboundMediaStreamMock) streamFactory.getOutboundMediaStream();
                outboundStream.setEventDispatcher(this.getEventDispatcher());
                outboundStream.setThrowIllegalArgumentAtPlay(throwIllegalArgumentExceptionAtPlay);
                outboundStream.invokeWhenPlayFinished(toInvokeOnPlayFinished);
            }
            if (responseToNegotiateEarlyMedia == CallManagerMock.EventType.EARLYMEDIAAVAILABLE_EVENT) {
                fireEvent(new EarlyMediaAvailableEvent(this));
            } else if (responseToNegotiateEarlyMedia == CallManagerMock.EventType.EARLYMEDIAFAILED_EVENT) {
                fireEvent(new EarlyMediaFailedEvent(this));
            }
        }
    }

    public void hangup() {
        log.info("MOCK: InboundCallMock.hangup is not implemented");
    }

    /**
     * @param length
     * @param cause
     */
    public void setRecordFailed(long length, RecordFailedEvent.CAUSE cause) {
        synchronized (this) {
            this.recordLength = length;
            this.recordFailedEvent = cause;
            this.recordFinishedEvent = null;
            if (inboundStream != null)
                inboundStream.setRecordFailed(length, cause);
        }
    }

    /**
     * @param length
     * @param cause
     */
    public void setRecordFinished(long length, RecordFinishedEvent.CAUSE cause) {
        synchronized (this) {
            length = ApplicationBasicTestCase.scale(length);
            this.recordLength = length;
            this.recordFailedEvent = null;
            this.recordFinishedEvent = cause;
            if (inboundStream != null)
                inboundStream.setRecordFinished(length, cause);
        }
    }

    public void setThrowIllegalArgumentAtPlay(boolean throwIllegalArgumentAtPlay) {
        this.throwIllegalArgumentExceptionAtPlay = throwIllegalArgumentAtPlay;
    }

    public void setMimeTypeInSession(MimeType m) {
        ArrayList<MimeType> mimeTypes = new ArrayList<MimeType>();
        mimeTypes.add(m);

        //setup the selected CallMediaTypes that is put in the session to fake the CallManagers early media selection
        MediaMimeTypes outboundMediaTypes = new MediaMimeTypes(m);
        callMediaTypes = new CallMediaTypes(outboundMediaTypes, null);
    }

    public void invokeWhenPlayFinished(Callable callable) {
        this.toInvokeOnPlayFinished = callable;
    }

    public void invokeWhenRecord(Callable callable) {
         this.toInvokeWhenRecord = callable;
     }

    private SpeechRecognizerMock currentRecognizer() {
        MediaTranslationManagerMock mtm = MediaTranslationManagerMock.instance();
        return (SpeechRecognizerMock)mtm.getSpeechRecognizer(api.getSession());
    }


    public void recognizeSucceded(String grammar, String value, String meaning) {
        currentRecognizer().recognizeSucceded(this,grammar,value,meaning);
    }

    public void recognizeFailed() {
        SpeechRecognizerMock srm = currentRecognizer();
        srm.recognizeFailed(this);
    }

    public void recognizeNoInput() {
        SpeechRecognizerMock srm = currentRecognizer();
        srm.recognizeNoInput(this);
    }

    public void recognizeNoMatch() {
        SpeechRecognizerMock srm = currentRecognizer();
        srm.recognizeNoMatch(this);
    }

    @Override
    public void redirect(RedirectDestination destination, RedirectStatusCode redirectCode) {
        // TODO Auto-generated method stub
        
    }

}
