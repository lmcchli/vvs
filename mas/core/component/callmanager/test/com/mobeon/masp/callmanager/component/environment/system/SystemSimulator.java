/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system;

import com.mobeon.common.cmnaccess.CommonMessagingAccessTestWrapper;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.OutboundCall;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.EventDispatcherMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.SessionMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.SessionFactoryMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.ApplicationExecutionMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.ApplicationManagementMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.ServiceEnablerInfoMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.SupervisionMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.InboundMediaStreamMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.OutboundMediaStreamMock;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.StreamFactoryMock;
import com.mobeon.masp.callmanager.component.environment.EnvironmentConstants;
import com.mobeon.masp.callmanager.component.environment.callmanager.CallManagerToVerify;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.sip.events.SipRequestEventImpl;
import com.mobeon.masp.callmanager.callhandling.InboundCallImpl;
import com.mobeon.masp.callmanager.callhandling.OutboundCallImpl;
import com.mobeon.masp.callmanager.callhandling.CallParameters;
import com.mobeon.masp.callmanager.callhandling.CallImpl;
import com.mobeon.masp.callmanager.callhandling.events.RejectEvent.RejectEventTypes;
import com.mobeon.masp.callmanager.callhandling.states.CallState;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.masp.stream.ControlToken;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.RecordingProperties;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.StreamAbandonedEvent;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.ArrayList;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.stub.ReturnStub;

import javax.sip.message.Request;
import javax.sip.ServerTransaction;
import javax.sip.RequestEvent;
import javax.sip.SipProvider;
import javax.sip.Dialog;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;

/**
 * This class simulates the system part of a phone call, i.e. the
 * Execution Engine and Streams.
 */
public class SystemSimulator extends MockObjectTestCase implements IEventReceiver {

    private ILogger log = ILoggerFactory.getILogger(getClass());

    // Event related
    private LinkedBlockingQueue<Event> receivedEvents =
            new LinkedBlockingQueue<Event>();
    private EventDispatcherMock eventDispatcherMock = new EventDispatcherMock();

    // Service related
    private SessionMock sessionMock = new SessionMock();
    private SessionFactoryMock sessionFactoryMock = new SessionFactoryMock(sessionMock);
    private ApplicationExecutionMock applicationExecutionMock =
            new ApplicationExecutionMock(sessionMock, eventDispatcherMock);
    private ApplicationManagementMock applicationManagementMock =
            new ApplicationManagementMock(applicationExecutionMock);
    private CallManagerLicensingMock callManagerLicensingMock= new CallManagerLicensingMock();

    // O&M related
    private ServiceEnablerInfoMock serviceEnablerInfoMock = new ServiceEnablerInfoMock();
    private SupervisionMock supervisionMock = new SupervisionMock(serviceEnablerInfoMock);

    // Stream related
    private InboundMediaStreamMock inboundMediaStreamMock =
            new InboundMediaStreamMock();
    private OutboundMediaStreamMock outboundMediaStreamMock =
            new OutboundMediaStreamMock();
    private StreamFactoryMock streamFactoryMock = new StreamFactoryMock(
            outboundMediaStreamMock, inboundMediaStreamMock);
    private static final int IDLE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 10;
    private Mock playMediaObjectMock = new Mock(IMediaObject.class);
    private Mock recordMediaObjectMock = new Mock(IMediaObject.class);

    // Call Manager related
    private CallManager callManager;
    private AtomicReference<OutboundCall> activeOutboundCall =
            new AtomicReference<OutboundCall>();

    public enum SessionData {
        CALL_MEDIA_TYPES_ARRAY, SELECTED_CALL_MEDIA_TYPES
    }

    public SystemSimulator() throws Exception {
        eventDispatcherMock.addEventReceiver(this);
        recordMediaObjectMock.stubs().method("isImmutable").
                will(new ReturnStub(false));
        playMediaObjectMock.stubs().method("isImmutable").
                will(new ReturnStub(true));

        // Setup thread pool used for execution-engine simulation
        ExecutorServiceManager.getInstance().
                addCachedThreadPool("SimulateExecutionEngine",
                        IDLE_POOL_SIZE, MAX_POOL_SIZE);


        // Setup the RTP payload types that shall be available during the
        // test cases.
        List<RTPPayload> rtppayloads = new ArrayList<RTPPayload>();
        rtppayloads.add(new RTPPayload(
                0, RTPPayload.AUDIO_PCMU, "PCMU", 8000, 1, 64000, null));
        rtppayloads.add(new RTPPayload(
                96, RTPPayload.AUDIO_AMR, "AMR", 8000, 1, 12200, "mode-set=7; octet-align=1; robust-sorting=0"));
        rtppayloads.add(new RTPPayload(
                101, RTPPayload.AUDIO_DTMF, "telephone-event", 8000, 1, 0, null));
        rtppayloads.add(new RTPPayload(
                34, RTPPayload.VIDEO_H263, "H263", 8000, 1, 0, null));
        RTPPayload.updateDefs(rtppayloads);


        // Set default values for ptime and maxptime
        ConnectionProperties.updateDefaultPTimes(20,40);

    }

    public void create(CallManagerToVerify callManager) {
        this.callManager = callManager.getCallManager();
        inboundMediaStreamMock.setHost(callManager.getHostAddress());
        CommonMessagingAccessTestWrapper.getInstance().setSystemReady();
    }

    public ISession getSessionMock() {
        return sessionMock;
    }

    public ISessionFactory getSessionFactory() {
        return sessionFactoryMock.getSessionFactory();
    }
    public CallManagerLicensingMock getCallManagerLicensingMock()
    {
        return callManagerLicensingMock;

    }

    public void delete() {
        eventDispatcherMock.removeAllEventReceivers();
        receivedEvents.clear();
    }

    // Getters
    public IApplicationManagment getApplicationManagement() {
        return applicationManagementMock;
    }

    public IEventDispatcher getEventDispatcher() {
        return eventDispatcherMock;
    }

    public Supervision getSupervision() {
        return supervisionMock;
    }

    public IStreamFactory getStreamFactory() {
        return streamFactoryMock;
    }

    public ServiceEnablerInfoMock getServiceEnablerInfo() {
        return serviceEnablerInfoMock;
    }

    public IInboundMediaStream getInboundStream() {
        return inboundMediaStreamMock;
    }

    public IOutboundMediaStream getOutboundStream() {
        return outboundMediaStreamMock;
    }

    public void fireStreamAbandonedEvent() {
        StreamAbandonedEvent event =
                new StreamAbandonedEvent(inboundMediaStreamMock);
        if (getCall() instanceof InboundCall) {
            ((InboundCallImpl)getInboundCall()).doEvent(event);
        } else {
            ((OutboundCallImpl)getOutboundCall()).doEvent(event);
        }
    }

    public void lock() {
        supervisionMock.lock();
    }

    public void unlock() {
        supervisionMock.unlock();
    }

    public void updateThreshold(int hwm, int lwm, int max) {
        supervisionMock.updateThreshold(hwm, lwm, max);
    }

    public void clear() {
        activeOutboundCall.set(null);
        eventDispatcherMock.clearCall();
        sessionMock.clearSessionData();
        inboundMediaStreamMock.clear();
        outboundMediaStreamMock.clear();
        serviceEnablerInfoMock.clear();
        receivedEvents.clear();
    }

    public Call getActiveCall() {
        Call inboundCall = eventDispatcherMock.getActiveCall();
        if (inboundCall != null) {
            return inboundCall;
        } else {
            return activeOutboundCall.get();
        }
    }

    public void setSessionData(SessionData data, Object value) {
        String name = "";
        switch(data) {
            case CALL_MEDIA_TYPES_ARRAY:
                name = "callmediatypesarray";
                break;
            case SELECTED_CALL_MEDIA_TYPES:
                name = "selectedcallmediatypes";
                break;
        }
        sessionMock.setData(name, value);
    }

    // Event dispatching
    public void doEvent(Event event) {
        receivedEvents.add(event);
        log.debug("Adding event: " + event);
    }

    public void doGlobalEvent(Event event) {
        doEvent(event);
    }

    public Event getReceivedEvent(long milliSeconds)
        throws InterruptedException {
        return receivedEvents.poll(milliSeconds, TimeUnit.MILLISECONDS);
    }

    // Call setup related
    public void accept() {
        getInboundCall().accept();
    }

    public void proxy(RemotePartyAddress uas) {
        getInboundCall().proxy(uas);
    }

    public void negotiateEarlyMediaTypes() {
        getInboundCall().negotiateEarlyMediaTypes();
    }

    public void waitForOutboundTokens(long milliSeconds) {
        outboundMediaStreamMock.waitForOutboundTokens(milliSeconds);
    }

    public void waitForPlay(long milliSeconds) {
        outboundMediaStreamMock.waitForPlay(milliSeconds);
    }

    public void waitForRecord(long milliSeconds) {
        inboundMediaStreamMock.waitForRecord(milliSeconds);
    }

    public void waitForStopPlay(long milliSeconds) {
        outboundMediaStreamMock.waitForStopPlay(milliSeconds);
    }

    public void waitForStopRecord(long milliSeconds) {
        inboundMediaStreamMock.waitForStopRecord(milliSeconds);
    }

    private InboundCall getInboundCall() {
        return ((InboundCall)eventDispatcherMock.getActiveCall());
    }

    private OutboundCall getOutboundCall() {
        return activeOutboundCall.get();
    }

    public void reject(String rejectEventTypeName, String reason) {
        getInboundCall().reject(rejectEventTypeName, reason);
    }

    public void disconnect() {
        if (getCall() instanceof InboundCall) {
            getInboundCall().disconnect();
        } else {
            getOutboundCall().disconnect();
        }
    }

    public void play(boolean multipleMOs) {
        playMediaObjectMock.stubs().method("isImmutable").
                will(new ReturnStub(true));
        IMediaObject mo = (IMediaObject)playMediaObjectMock.proxy();
        if (multipleMOs) {
            IMediaObject[] moArray = new IMediaObject[] {mo, mo};
            getCall().play(this, moArray,
                    IOutboundMediaStream.PlayOption.DO_NOT_WAIT, 10);
        } else {
            getCall().play(this, mo,
                    IOutboundMediaStream.PlayOption.DO_NOT_WAIT, 10);
        }
    }

    public void record() {
        Call call;
        if (getCall() instanceof InboundCall) {
            call = getInboundCall();
            call.record(this,
                    (IMediaObject)playMediaObjectMock.proxy(),
                    (IMediaObject)recordMediaObjectMock.proxy(),
                    new RecordingProperties());
        } else {
            call = getOutboundCall();
            call.record(this,
                    (IMediaObject)recordMediaObjectMock.proxy(),
                    new RecordingProperties());
        }
    }

    public void stopPlay() {
        getCall().stopPlay(this);
    }

    public void stopRecord() {
        getCall().stopRecord(this);
    }

    public void join(Call otherCall, boolean fail) {
        if (fail) {
            inboundMediaStreamMock.throwExceptionNextMethod();
        }
        callManager.join(getCall(), otherCall, eventDispatcherMock);
    }

    public void unjoin(Call otherCall, boolean fail) {
        if (fail) {
            inboundMediaStreamMock.throwExceptionNextMethod();
        }
        callManager.unjoin(getCall(), otherCall, eventDispatcherMock);
    }

    public void initiateVideoFastUpdate() {
        inboundMediaStreamMock.initiateVideoFastUpdate();
    }

    public void sendToken() {
        ControlToken[] tokens = new ControlToken[] {
                new ControlToken(ControlToken.DTMFToken.FOUR, 5, 5)};
        getOutboundCall().sendToken(tokens);
    }

    public void createCall(CallProperties callProperties) {
        activeOutboundCall.set(callManager.createCall(
                callProperties, eventDispatcherMock, sessionMock));
    }

    public void createInboundStreamException() {
        inboundMediaStreamMock.throwExceptionNextMethod();
    }

    public void createOutboundStreamException() {
        outboundMediaStreamMock.throwExceptionNextCreate();
    }

    // The StringMsgParser comes from the NIST SIP implementation and is not
    // part of the JAIN SIP interface. It is used for testing purposes only.
    private static final StringMsgParser stringMsgParser = new StringMsgParser();

    private static String msgString =
        "INVITE sip:masUser@10.16.2.97:5060;transport=udp SIP/2.0\r\n" +
        "Call-ID: 31bdedbd1aa49d0ea3b3dc5b6e19dbda@localhost\r\n" +
        "From: <sip:sipPhone@localhost>;tag=1219378555\r\n" +
        "To: <sip:masUser@10.16.2.97>\r\n" +
        "Diversion: displayname <sip:1234@localhost;user=phone>;privacy=full;reason=unavailable\r\n" +
        "Route: <sip:masUser@10.16.2.97:5060>\r\n" +
        "CSeq: 1 INVITE\r\n" +
        "Contact: <sip:sipPhone@localhost:5090>\r\n" +
        "Via: SIP/2.0/UDP localhost:5090;branch=z9hG4b881ebfd4acd3837cd0d4e9a54d16c031\r\n" +
        "Content-Type: application/sdp\r\n" +
        "Content-Length: 200\r\n" +
        "\r\n" +
        "v=0\r\n" +
        "o=userXXX 0 0 IN IP4 localhost\r\n" +
        "s=MAS prompt session\r\n" +
        "c=IN IP4 localhost\r\n" +
        "t=0 0\r\n" +
        "m=audio 1111 RTP/AVP 0 101\r\n" +
        "a=rtpmap:0 PCMU/8000\r\n" +
        "a=rtpmap:101 telephone-event/8000\r\n" +
        "a=fmtp:101 0-15\r\n" +
        "a=ptime:40\r\n";

    public InboundCallImpl createInboundCall() throws Exception {

        SIPMessage sipMessage =
                stringMsgParser.parseSIPMessage(msgString);

        Mock mockServerTransaction = new Mock(ServerTransaction.class);
        Mock mockDialog = new Mock(Dialog.class);
        mockDialog.stubs().method("getDialogId").will(new ReturnStub("DialogId"));
        mockServerTransaction.stubs().method("getDialog").
                will(new ReturnStub(mockDialog.proxy()));
        mockDialog.stubs().method("isServer").will(new ReturnStub(true));
        Mock mockSipProvider = new Mock(SipProvider.class);
        RequestEvent requestEvent = new RequestEvent(
                mockSipProvider.proxy(),
                (ServerTransaction)mockServerTransaction.proxy(),
                (Dialog)mockDialog.proxy(),
                (Request)sipMessage);
        SipRequestEventImpl sipRequestEvent = new SipRequestEventImpl(requestEvent);
        return new InboundCallImpl(null, null, (Dialog)mockDialog.proxy(),
                new CallParameters(), null, null, null, null, sipRequestEvent,
                null);
    }

    private Call getCall() {
        Call call = getInboundCall();
        return call != null ? call : getOutboundCall();
    }

    public Event assertEventReceived(Class expectedEvent, Class skipEvent)
            throws InterruptedException {

        Event event = getReceivedEvent(EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS);
        assertNotNull("Timed out while waiting to receive event.", event);

        if (skipEvent != null) {
            long startTime = System.currentTimeMillis();

            while ((event.getClass() == skipEvent) &&
                (System.currentTimeMillis() <
                        (startTime + EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS))) {
                Thread.sleep(5);
                event = getReceivedEvent(EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS);
                assertNotNull("Timed out while waiting to receive event.", event);
            }
        }

        assertTrue("Expected event: " + expectedEvent +
                " Received event: " + event.getClass(),
                expectedEvent.isInstance(event));

        return event;
    }

    private CallState getCurrentState() {
        return ((CallImpl)getActiveCall()).getCurrentState();
    }

    public void waitForState(Class expectedState) {

        long startTime = System.currentTimeMillis();

        while (!expectedState.isInstance(getCurrentState()) &&
            (System.currentTimeMillis() <
                    (startTime + EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Time out while waiting for state " +
                        expectedState + ".", e);
                return;
            }
        }

        CallState state = getCurrentState();
        if (!expectedState.isInstance(state)) {
            fail("Timed out when waiting for state " + expectedState +
                ". Current state is " + state.getClass().getName());
        }
    }

    // This is only included to make IntelliJ happy. Not used at all.
    public void testDoNothing() throws Exception {
    }
}
