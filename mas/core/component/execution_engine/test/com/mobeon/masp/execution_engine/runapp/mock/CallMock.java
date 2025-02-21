package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.SessionImpl;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.*;

import java.util.concurrent.ExecutorService;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Mikael Andersson
 */
public class CallMock extends ServiceEnablerMock implements Call {
    /**
     * The type of this call.
     */
    protected volatile CallProperties.CallType callType;
    /**
     * The calling party of this call.
     */
    protected volatile CallingParty callingParty;
    /**
     * The called party in this call.
     */
    protected volatile CalledParty calledParty;
    /**
     * Holds the name of the protocol used for call control in this call.
     */
    protected volatile String protocolName;
    /**
     * Holds the version of the protocol used in this call.
     */
    protected volatile String protocolVersion;
    /**
     * The inbound stream of the call.
     */
    protected volatile InboundMediaStreamMock inboundStream;
    /**
     * The outbound stream of the call.
     */
    protected volatile OutboundMediaStreamMock outboundStream;

    protected ISession session;
    
    protected boolean withHoldDisconnectAttempt = false;
    private int delayBeforeResponseToDisconnect = 0;
    private Set<Connection> farEndConnections = new HashSet<Connection>();

    private boolean sendPlayFailedAfterDelay;
    private int delayBeforePlayFailed;
    private int inboundBitRate;


    public CallMock(ExecutorService service,
                    IApplicationManagment applicationManagement,
                    boolean withHoldDisconnectAttempt,
                    int delayBeforeResponseToDisconnect,
                    Set<Connection> farEndConnections,
                    boolean sendPlayFailedAfterDelay,
                    int delayBeforePlayFailed,
                    int inboundBitRate) {
        super(service, applicationManagement);
        protocolVersion = "1.0";
        protocolName = "SIP";
        this.withHoldDisconnectAttempt = withHoldDisconnectAttempt;
        this.delayBeforeResponseToDisconnect = delayBeforeResponseToDisconnect;
        this.farEndConnections = farEndConnections;
        this.sendPlayFailedAfterDelay = sendPlayFailedAfterDelay;
        this.delayBeforePlayFailed = delayBeforePlayFailed;
        this.inboundBitRate = inboundBitRate;
        this.session = new SessionImpl();
    }


    /**
     * Returns with the type of this call.
     *
     * @return the type of this call.
     */
    public CallProperties.CallType getCallType()
    {
        return callType;
    }

    /**
     * Returns with the calling party of this call.
     *
     * @return the called party associated with this call.
     */
    public CallingParty getCallingParty()
    {
        return callingParty;
    }

    /**
     * Returns with the called party of this call.
     *
     * @return the called party associated with this call.
     */
    public CalledParty getCalledParty()
    {
        return calledParty;
    }

    /**
     * Returns with the protocol name of the call.
     *
     * @return the name of the protocol
     */
    public String getProtocolName()
    {
        return protocolName;
    }

    /**
     * Returns with the version of the protocol.
     *
     * @return the version of the protocol.
     */
    public String getProtocolVersion()
    {
        return protocolVersion;
    }

    /**
     * Returns the with the inbound stream associated with the call.
     *
     * @return the inbound stream for this call.
     */
    public IInboundMediaStream getInboundStream()
    {
        return inboundStream;
    }

    /**
     * Returns with the outbound stream for this call.
     *
     * @return the outbound stream for this call.
     */
    public IOutboundMediaStream getOutboundStream()
    {
        return outboundStream;
    }

    public ISession getSession() {
        return session;
    }

    public void play(Object id, IMediaObject mediaObject,
                     IOutboundMediaStream.PlayOption playOption,
                     long cursor) throws IllegalArgumentException {

        if(this.sendPlayFailedAfterDelay){
            final CallMock thisMock = this;
            fireDelayEvent(thisMock, this.delayBeforePlayFailed, new PlayFailedEvent(id, "PlayFailed event after delay"));
        } else
            try {
                outboundStream.play(id, mediaObject, playOption, cursor);
            } catch (StackException e) {
                e.printStackTrace();
            }
    }

    public void play(Object id, IMediaObject mediaObjects[],
                     IOutboundMediaStream.PlayOption playOption, long cursor)
            throws IllegalArgumentException {

        if(this.sendPlayFailedAfterDelay){
            final CallMock thisMock = this;
            fireDelayEvent(thisMock, this.delayBeforePlayFailed, new PlayFailedEvent(id, "PlayFailed event after delay"));
        } else {
            try {
                if(outboundStream != null){
                    outboundStream.play(id, mediaObjects, playOption, cursor);
                } else {
                    fireEvent(new PlayFailedEvent(id, "No outbound stream exists"));
                }
            } catch (StackException e) {
                e.printStackTrace();
            }
        }
    }

    public void record(Object id, IMediaObject recordMediaObject,
                       RecordingProperties properties)
            throws IllegalArgumentException {
        try {
            if(inboundStream == null){
                fireEvent(new RecordFailedEvent(id, RecordFailedEvent.CAUSE.EXCEPTION, "No inbound stream exists"));
            } else {
                inboundStream.record(id, recordMediaObject,  properties);
            }
        } catch (StackException e) {
            e.printStackTrace();
        }
    }

    public void record(Object id, IMediaObject playMediaObject,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties)
            throws IllegalArgumentException {
        try {
            outboundStream.play(id, playMediaObject, null, 0);
            inboundStream.record(id, recordMediaObject, properties);
        } catch (StackException e) {
            e.printStackTrace();
        }
    }

    public void stopPlay(Object id) throws IllegalArgumentException {
        try {
            outboundStream.stop(id);
        } catch (StackException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord(Object id) throws IllegalArgumentException {
        try {
            inboundStream.stop(id);
        } catch (StackException e) {
            e.printStackTrace();
        }
    }


    public void sleep(int i) throws InterruptedException {
        long millis = ApplicationBasicTestCase.scale(i);
        log.info("MOCK: Sleeping for "+millis);
        Thread.sleep(millis);
    }

    /**
     * This method is used to disconnect the call.
     */
    public void disconnect()

    {
        log.info ("MOCK: CallMock.disconnect");
        if(withHoldDisconnectAttempt){
            log.info("MOCK: CallMock.disconnect: withholding the disconnect");
        } else {

            if(delayBeforeResponseToDisconnect == 0){
                fireEvent(new DisconnectedEvent(
                        this, DisconnectedEvent.Reason.NEAR_END, false));
            } else {
                final CallMock thisMock = this;
                fireDelayEvent(thisMock, delayBeforeResponseToDisconnect, new DisconnectedEvent(
                            thisMock, DisconnectedEvent.Reason.NEAR_END, false));
            }

            /*
            if (inboundStream != null)
                inboundStream.notifyAll();
            if (outboundStream != null)
               outboundStream.notifyAll();
            */
            if(inboundStream != null){
                inboundStream.delete();
            }
            inboundStream = null;
            outboundStream = null;
        }
    }

    private void fireDelayEvent(final CallMock thisMock, final int delay, final Event event) {
        new Thread() {
            public void run() {
                try {
                    log.info("MOCK: CallMock.disconnect: sleeping for " + delay +"before response");
                    sleep(delay);
                    fireEvent(event);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void setCallingParty(CallingParty callingParty) {
        this.callingParty = callingParty;
    }

    public void setCalledParty(CalledParty calledParty) {
        this.calledParty = calledParty;
    }

    public static void setupCNumber(RedirectingParty c, String cNumber, CallPartyDefinitions.PresentationIndicator presentationIndicator, RedirectingParty.RedirectingReason redirectingReason) {
        c.setPresentationIndicator(presentationIndicator);
        c.setTelephoneNumber(cNumber);
        c.setSipUser(cNumber+"@mobeon.com");
        c.setUri("sip:"+cNumber+"@mobeon.com;user=phone");
        c.setRedirectingReason(redirectingReason);
    }

    public static void setupBNumber(CalledParty b, String bNumber) {
        b.setTelephoneNumber(bNumber);
        b.setSipUser(bNumber+"@mobeon.com");
        b.setUri("sip:"+bNumber+"@mobeon.com;user=phone");
    }

    public static void setupANumber(CallingParty a,
                                    String aNumber,
                                    CallPartyDefinitions.PresentationIndicator presentationIndicator,
                                    NumberCompletion numberCompletion) {
        a.setPresentationIndicator(presentationIndicator);
        a.setTelephoneNumber(aNumber);
        a.setSipUser(aNumber+"@mobeon.com");
        a.setUri("sip:"+aNumber+"@mobeon.com;user=phone");
        a.setNumberCompletion(numberCompletion);
    }

    public void setCallType(CallProperties.CallType callType) {
        this.callType = callType;
    }

    public Set<Connection> getFarEndConnections() {
        return farEndConnections;
    }

    public int getInboundBitRate() {
        return inboundBitRate;
    }

    public void setSendPlayFailedAfterDelay(boolean sendPlayFailedAfterDelay) {
        this.sendPlayFailedAfterDelay = sendPlayFailedAfterDelay;
    }

    public void setDelayBeforePlayFailed(int delayBeforePlayFailed) {
        this.delayBeforePlayFailed = delayBeforePlayFailed;
    }
}
