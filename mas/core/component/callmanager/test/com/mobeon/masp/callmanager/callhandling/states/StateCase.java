/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.states;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.jmock.core.Stub;
import org.jmock.core.Invocation;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sip.SipMessageSender;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.message.SipResponseFactory;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.sip.message.SipRequestFactory;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.sip.header.SipHeaderFactory;
import com.mobeon.masp.callmanager.sdp.SdpSessionDescription;
import com.mobeon.masp.callmanager.sdp.SdpIntersection;
import com.mobeon.masp.callmanager.sdp.SdpInternalErrorException;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.ConnectedEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEventImpl;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.events.ErrorEvent;
import com.mobeon.masp.callmanager.events.NotAllowedEvent;
import com.mobeon.masp.callmanager.events.SendTokenErrorEvent;
import com.mobeon.masp.callmanager.events.AlertingEvent;
import com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.events.EarlyMediaFailedEvent;
import com.mobeon.masp.callmanager.releasecausemapping.ReleaseCauseMapping;
import com.mobeon.masp.callmanager.callhandling.events.PlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.RecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopPlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopRecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.CallToCall;
import com.mobeon.masp.callmanager.callhandling.CallTimerTask;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.RemotePartyControllerImpl;
import com.mobeon.masp.callmanager.CallDirection;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.stream.PlayFailedEvent;
import com.mobeon.masp.stream.RecordFailedEvent;
import com.mobeon.masp.stream.StackException;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

import javax.sip.*;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ReasonHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import jakarta.activation.MimeType;
import java.text.ParseException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Base class for state tests. Implements common methods for callstate tests in
 * both inbound and outbound direction.
 *
 * @author Malin Flodin
 */
public abstract class StateCase extends MockObjectTestCase {

    // Mocked protocol handlers
    protected Mock mockSipMessageSender;
    private Mock mockSipResponseFactory;
    protected Mock mockSipRequestFactory;
    private Mock mockSipProvider;

    // The mocked initial request
    protected Mock mockInitialRequest;
    private Mock mockInitialServerTransaction;
    private Mock mockDialog;
    protected SipRequestEventImpl initialSipRequestEvent;
    private SipResponse initialSipResponse;

    // A mocked additional request
    protected Mock mockAdditionalRequest;
    protected SipRequestEventImpl additionalSipRequestEvent;
    private SipResponse additionalSipResponse;

    // A mocked SIP response
    protected Mock mockResponse;
    protected SipResponseEvent sipResponseEvent;

    protected Mock mockSessionDescription;

    private Mock mockCallToCall;

    protected static final Boolean FAIL = true;
    protected static final Boolean SUCCEED = false;
    protected static final Boolean ALREADY_DISCONNECTED = true;
    protected static final Boolean NOT_DISCONNECTED = false;
    protected static final Boolean EARLY_MEDIA = true;
    protected static final Boolean NO_EARLY_MEDIA = false;


    protected SipTimeoutEvent sipTimeoutEvent;
    protected PlayEvent playEvent;
    protected RecordEvent recordEvent;
    protected StopPlayEvent stopPlayEvent;
    protected StopRecordEvent stopRecordEvent;

    protected final ConnectionProperties connectionProperties = new ConnectionProperties();
    protected final ConnectionProperties inboundConnProps = new ConnectionProperties();
    protected SdpIntersection sdpIntersection;
    private final Collection<MimeType> mandatoryAudio = new ArrayList<MimeType>();
    private final Collection<MimeType> mandatoryVideo = new ArrayList<MimeType>();

    protected CallTimeoutEvent noAckTimeoutEvent =
            new CallTimeoutEvent(CallTimerTask.Type.NO_ACK);

    protected CallTimeoutEvent redirectedRtpTimeoutEvent =
        new CallTimeoutEvent(CallTimerTask.Type.REDIRECTED_RTP);

    protected void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile(CallManagerTestContants.CALLMANAGER_XML);
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());
        ConfigurationReader.getInstance().setInitialConfiguration(
                cm.getConfiguration());
        ConfigurationReader.getInstance().update();
        ConfigurationReader.getInstance().getConfig().setReleaseCauseMapping(
                ReleaseCauseMapping.getDefaultReleaseCauseMappings());

        mockSipMessageSender = mock(SipMessageSender.class);
        mockSipResponseFactory = mock(SipResponseFactory.class);
        mockSipRequestFactory = mock(SipRequestFactory.class);
        mockSessionDescription = mock(SdpSessionDescription.class);
        mockSipProvider = mock(SipProvider.class);

        mockCallToCall = mock(CallToCall.class);

        // Create a CMUtils instance with mock objects
        CMUtils cmUtils = CMUtils.getInstance();
        cmUtils.setSipMessageSender((SipMessageSender)(mockSipMessageSender.proxy()));
        cmUtils.setSipRequestFactory((SipRequestFactory)(mockSipRequestFactory.proxy()));
        cmUtils.setSipResponseFactory((SipResponseFactory)(mockSipResponseFactory.proxy()));

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        cmUtils.setSipHeaderFactory(new SipHeaderFactory(
                sipFactory.createAddressFactory(), sipFactory.createHeaderFactory()));

        cmUtils.setRemotePartyController(new RemotePartyControllerImpl());

        MediaMimeTypes mediaMimeTypes = new MediaMimeTypes(new MimeType("audio/pcmu"));
        CallMediaTypes[] callMediaTypes = new CallMediaTypes[]{
                new CallMediaTypes(mediaMimeTypes, null)};

        mandatoryAudio.add(new MimeType("audio/pcmu"));
        mandatoryVideo.add(new MimeType("video/h263"));

        ConnectionProperties.updateDefaultPTimes(40,40);
        sdpIntersection =
                new SdpIntersection(
                        (SdpSessionDescription)(mockSessionDescription.proxy()),
                        0, null, 0,
                        callMediaTypes[0],
                        mandatoryAudio, mandatoryVideo);

        setupDialog();
        setupResponse();
        setupInitialRequest();
        setupAdditionalRequest();
        setupMockedEvents();
        setupExpectations();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        CMUtils.getInstance().delete();
    }

    protected final void setResponseReceived(int responseType) {
        mockResponse.expects(once()).method("getStatusCode").
                 will( returnValue(responseType));
    }

    protected final void assertRegisterToReceiveEvents(Mock mockedCall) {
        mockedCall.expects(once()).method("registerToReceiveEvents");
    }

    protected final void assertStreamsDeleted(Mock mockedCall) throws Exception {
        mockedCall.expects(once()).method("deleteStreams");
    }

    protected void assertLocalSdpAnswerRetrieved(Mock mockedCall) {
        mockedCall.expects(once()).method("getLocalSdpAnswer").
                will(returnValue(null));
    }

    protected final void assertParsingSdpBody(Mock mockedCall, boolean failed) {
        if (failed) {
            mockedCall.expects(once()).method("parseRemoteSdp").
                    will(throwException(new SdpNotSupportedException(
                            SipWarning.INCOMPATIBLE_BANDWIDTH_UNIT, "Error")));
        } else {
            mockedCall.expects(once()).method("parseRemoteSdp");
        }
    }

    protected final void assertCallHold(Mock mockedCall, boolean isHeld) {
        if (!isHeld) {
            mockedCall.expects(once()).method("isPendingSdpACallHold").
            will(returnValue(false));
        } else {
            mockedCall.expects(once()).method("isPendingSdpACallHold").
            will(returnValue(true));
        }
    }

    protected final void assertSdpEquality(Mock mockedCall, boolean failed) {
        if (failed) {
            mockedCall.expects(once()).method(
                    "checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp").
            will(throwException(new SdpNotSupportedException(
                    SipWarning.RENEGOTIATION_NOT_SUPPORTED, "Error")));
        } else {
            mockedCall.expects(once()).method(
                    "checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp");
        }
    }

    protected final void assertRetrievingSdpBody(Mock mockedCall, boolean failed) {
        if (failed) {
            mockedCall.expects(once()).method("getRemoteSdp").
                    will(returnValue(null));
        } else {
            mockedCall.expects(once()).method("getRemoteSdp").
                    will(returnValue(mockSessionDescription.proxy()));
        }
    }

    protected final void assertGettingSdpIntersection(Mock mockedCall, boolean found) {
        mockedCall.stubs().method("getOutboundCallMediaTypes").
            will(returnValue(null));
        if (found) {
            mockedCall.expects(once()).method("findSdpIntersection").
                    will(returnValue(sdpIntersection));
        } else {
            mockedCall.expects(once()).method("findSdpIntersection").
                    will(returnValue(null));
        }
    }

    protected final void assertErrorOccurred(
            Mock mockedCall, String message, Boolean alreadyDisconnected) {
        mockedCall.expects(once()).method("errorOccurred").
                with(eq(message), eq(alreadyDisconnected));
    }

    protected final void assertFailedEvent(Mock mockedCall,
                                     FailedEvent.Reason reason,
                                     CallDirection direction,
                                     String message,
                                     Integer networkStatusCode)
            throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(FailedEvent.class)).
                will( validateFailedEvent(
                        mockedCall, reason, direction, message,
                        networkStatusCode) );
    }

    protected final void assertErrorEvent(Mock mockedCall,
                                    CallDirection direction,
                                    String message,
                                    boolean alreadyDisconnected)
            throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(ErrorEvent.class)).
                will( validateErrorEvent(
                        mockedCall, direction, message, alreadyDisconnected) );
    }

    protected final void assertNotAllowedEvent(Mock mockedCall,
                                         String message) throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(NotAllowedEvent.class)).
                will( validateNotAllowedEvent(mockedCall, message) );
    }

    protected final void assertDisconnectedEvent(Mock mockedCall,
                                           DisconnectedEvent.Reason reason,
                                           boolean alreadyDisconnected)
            throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(DisconnectedEvent.class)).
                will( validateDisconnectedEvent(
                        mockedCall, reason, alreadyDisconnected) );
    }

    protected final void assertAlertingEvent(Mock mockedCall) throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(AlertingEvent.class) );
    }

    protected final void assertConnectedEvent(Mock mockedCall) throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(ConnectedEvent.class) );
    }

    protected final void assertProgressingEvent(Mock mockedCall, boolean isEarlyMedia)
            throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(ProgressingEvent.class)).
                will( validateProgressingEvent(mockedCall, isEarlyMedia) );
    }

    protected final void assertEarlyMediaAvailableEvent(Mock mockedCall)
            throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(EarlyMediaAvailableEvent.class) );
    }

    protected final void assertEarlyMediaFailedEvent(Mock mockedCall)
            throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(EarlyMediaFailedEvent.class) );
    }

    protected final void assertPlayFailedEvent(Mock mockedCall) throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(PlayFailedEvent.class) );
    }

    protected final void assertRecordFailedEvent(Mock mockedCall) throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(RecordFailedEvent.class) );
    }

    protected final void assertSendTokenErrorEvent(Mock mockedCall) throws Exception {
        mockedCall.expects(once()).method("fireEvent").
                with( isA(SendTokenErrorEvent.class) );
    }

    protected final void assertRecord(Mock mockedCall) {
        mockedCall.expects(once()).method("recordOnInboundStream");
    }

    protected final void assertStopPlay(Mock mockedCall) {
        mockedCall.expects(once()).method("stopOngoingPlay");
    }

    protected final void assertStopRecord(Mock mockedCall) {
        mockedCall.expects(once()).method("stopOngoingRecord");
    }

    protected final void assertNoAckTimerCanceled(Mock mockedCall) {
        mockedCall.expects(once()).method("cancelNoAckTimer");
    }

    protected final void assertNoAckTimerStarted(Mock mockedCall) {
        mockedCall.expects(once()).method("startNoAckTimer");
    }

    protected final void assertSipRequestSent(boolean createFailed, boolean sentFailed, String requestType, boolean initialRequest) {
        SipRequest sipRequest = new SipRequest(initialSipRequestEvent.getRequest());
        String createMethod = "create" + requestType + "Request";
        if (createFailed) {
            mockSipRequestFactory.expects(once()).method(createMethod).will(throwException(new ParseException("Error", 0)));
        } else {
            mockSipRequestFactory.expects(once()).method(createMethod).will(returnValue(sipRequest));
            if (sentFailed) {
                mockSipMessageSender.expects(once()).method("sendRequest").with(eq(sipRequest)).will(throwException(new SipException("Error")));
            } else {
                mockSipMessageSender.expects(once()).method("sendRequest").with(eq(sipRequest));
            }
        }
    }

    protected final void assertResponseSent(boolean createFailed,
                                      boolean sentFailed,
                                      String responseType,
                                      boolean initialRequest) {
        SipResponse sipResponse;
        if (initialRequest) {
            sipResponse = initialSipResponse;
        } else {
            sipResponse = additionalSipResponse;
        }

        String createMethod = "create" + responseType + "Response";
        if (createFailed) {
            mockSipResponseFactory.
                    expects(once()).method(createMethod).
                    will( throwException(new ParseException("Error", 0)));
        } else {
            mockSipResponseFactory.
                    expects(once()).method(createMethod).
                    will( returnValue(sipResponse));

            if (sentFailed) {
                mockSipMessageSender.expects(once()).method("sendResponse").
                        with( eq(sipResponse)).
                        will(throwException(new SipException("Error")));
            } else {
                mockSipMessageSender.expects(once()).method("sendResponse").
                        with( eq(sipResponse));
            }
        }
    }

    protected final void assertReliableResponseSent(
            boolean createFailed,
            boolean sentFailed,
            String responseType,
            boolean initialRequest) {
        SipResponse sipResponse;
        if (initialRequest) {
            sipResponse = initialSipResponse;
        } else {
            sipResponse = additionalSipResponse;
        }

        String createMethod = "create" + responseType + "Response";
        if (createFailed) {
            mockSipResponseFactory.
                    expects(once()).method(createMethod).
                    will( throwException(new ParseException("Error", 0)));
        } else {
            mockSipResponseFactory.
                    expects(once()).method(createMethod).
                    will( returnValue(sipResponse));

            if (sentFailed) {
                mockSipMessageSender.expects(once()).
                        method("sendReliableProvisionalResponse").
                        with( ANYTHING, eq(sipResponse)).
                        will(throwException(new SipException("Error")));
            } else {
                mockSipMessageSender.expects(once()).
                        method("sendReliableProvisionalResponse").
                        with( ANYTHING, eq(sipResponse));
            }
        }
    }

    protected final void assertInfoSent(boolean failed) throws Exception {
        if (failed) {
            mockSipRequestFactory.expects(once()).method("createInfoRequest").
                    will(throwException(new ParseException("Error", 0)));
        } else {
            mockSipRequestFactory.expects(once()).method("createInfoRequest").
                    will(returnValue(null));
            mockSipMessageSender.expects(once()).method("sendRequestWithinDialog");
        }
    }

    protected final void assertErrorResponseSent(Mock mockedCall, int responseType) {
        if (responseType == Response.NOT_ACCEPTABLE_HERE) {
            mockedCall.expects(once()).method("sendNotAcceptableHereResponse").
                    with(ANYTHING, ANYTHING);
        } else if (responseType == Response.METHOD_NOT_ALLOWED) {
            mockedCall.expects(once()).method("sendMethodNotAllowedResponse").
                    with(ANYTHING);
        } else {
            mockedCall.expects(once()).method("sendErrorResponse").
                    with(eq(responseType), ANYTHING, ANYTHING);
        }
    }

    protected final void assertJoinedOtherCall(Mock mockedCall,
                                         boolean failed) {
        if (failed)
            mockedCall.expects(once()).method("getJoinedToCall").
                    will(returnValue(null));
        else
            mockedCall.expects(once()).method("getJoinedToCall").
                    will(returnValue(mockCallToCall.proxy()));
    }

    protected final void assertCallJoined(Mock mockedCall, boolean failed) {
        mockedCall.expects(once()).method("isCallJoined").
                will(returnValue(!failed));
    }

    protected final void assertVFU(Mock mockedCall, boolean failed) {
        mockedCall.expects(once()).method("containsMediaControl").
                will(returnValue(!failed));
    }

    protected final void assertSdpInRequest(Mock mockedCall, boolean failed) {
        mockedCall.expects(once()).method("containsSdp").
                will(returnValue(!failed));
    }

    protected final void assertInfoRequestForwarded(Mock mockedCall) {
        String tag = "tag";
        mockCallToCall.expects(once()).method("forwardVFURequest").
                will(returnValue(tag));
        mockedCall.expects(once()).method("addPendingRequest").
                with(eq(tag), ANYTHING);
    }

    protected final void assertInfoResponseForwarded() {
        mockCallToCall.expects(once()).method("forwardVFUResponse");
    }

    protected final void assertMethodNotAllowedResponseSent(Mock mockedCall) {
        mockedCall.expects(once()).method("sendMethodNotAllowedResponse");
    }

    protected final void assertNotAcceptableResponseSent(
            Mock mockedCall, SipWarning warning) {
        mockedCall.expects(once()).method("sendNotAcceptableHereResponse").
                with(ANYTHING, eq(warning));
    }

    protected final void assertOkResponseSent(Mock mockedCall) {
        mockedCall.expects(once()).method("sendOkResponse");
    }

    protected final void assertResponseCodeRetrievedForMethod(
            String method, int responseCode) throws Exception {
        mockResponse.stubs().method("getStatusCode").will(returnValue(responseCode));
        CSeqHeader cSeq = CMUtils.getInstance().getSipHeaderFactory().
                createCSeqHeader(1, method);
        mockResponse.stubs().method("getHeader").with(eq("CSeq")).will(returnValue(cSeq));
    }

    protected final void assertInboundStreamCreated(Mock mockedCall, boolean failed)
            throws Exception {
        if (failed) {
            mockedCall.expects(once()).method("createInboundStream").
                    will(throwException(new StackException("Error")));
        } else {
            mockedCall.expects(once()).method("createInboundStream").
                    will(returnValue(inboundConnProps));
        }
    }

    protected final void assertOutboundStreamCreated(Mock mockedCall, boolean failed)
            throws Exception {
        if (failed) {
            mockedCall.expects(once()).method("createOutboundStream").
                    with(eq(sdpIntersection)).
                    will(throwException(new StackException("Error")));
        } else {
            mockedCall.expects(once()).method("createOutboundStream").
                    with(eq(sdpIntersection));
        }
    }

    protected final void assertReNegotiatedSdpOnInboundStream(Mock mockedCall) {
        mockedCall.expects(once()).method("reNegotiatedSdpOnInboundStream").
            with(eq(sdpIntersection));
    }

    protected final void assertSdpOfferCreated(Mock mockedCall, boolean failed)
            throws Exception {
        if (failed) {
            mockedCall.expects(once()).method("createSdpOffer").
                    will(throwException(new SdpInternalErrorException("Error")));
        } else {
            mockedCall.expects(once()).method("createSdpOffer").
                    will(returnValue("SdpOffer"));
        }
    }

    protected final void assertSdpOfferRetrieved(Mock mockedCall) throws Exception {
        mockedCall.expects(once()).method("getLocalSdpOffer").
                will(returnValue("SdpOffer"));
    }

    protected final void assertCreateAck(boolean failed) {
        if (failed) {
            mockSipRequestFactory.expects(once()).method("createAckRequest").
                    will(throwException(new SipException("Error")));
        } else {
            mockSipRequestFactory.expects(once()).method("createAckRequest").
                    will(returnValue(null));
        }
    }

    protected final void assertCreateBye(boolean failed) {
        if (failed) {
            mockSipRequestFactory.expects(once()).method("createByeRequest").
                    will(throwException(new SipException("Error")));
        } else {
            mockSipRequestFactory.expects(once()).method("createByeRequest").
                    will(returnValue(null));
        }
    }

    protected final void assertCreateCancel(boolean failed) {
        if (failed) {
            mockSipRequestFactory.expects(once()).method("createCancelRequest").
                    will(throwException(new SipException("Error")));
        } else {
            mockSipRequestFactory.expects(once()).method("createCancelRequest").
                    will(returnValue(null));
        }
    }

    protected final void assertCreatePrack(boolean failed) {
        if (failed) {
            mockSipRequestFactory.expects(once()).method("createPrackRequest").
                    will(throwException(new SipException("Error")));
        } else {
            mockSipRequestFactory.expects(once()).method("createPrackRequest").
                    will(returnValue(null));
        }
    }

    protected final void assertSendRequest(boolean failed) {
        if (failed) {
            mockSipMessageSender.expects(once()).method("sendRequest").
                    will(throwException(new SipException("Error")));
        } else {
            mockSipMessageSender.expects(once()).method("sendRequest");
        }
    }

    protected final void assertSendRequestWithinDialog(boolean failed) {
        if (failed) {
            mockSipMessageSender.expects(once()).method("sendRequestWithinDialog").
                    will(throwException(new SipException("Error")));
        } else {
            mockSipMessageSender.expects(once()).method("sendRequestWithinDialog");
        }
    }

    private void setupResponse() {
        mockResponse = mock(Response.class);
        Mock mockClientTransaction = mock(ClientTransaction.class);
        ResponseEvent responseEvent = new ResponseEvent(mockSipProvider.proxy(),
                (ClientTransaction) mockClientTransaction.proxy(),
                (Dialog) mockDialog.proxy(),
                (Response) mockResponse.proxy());
        sipResponseEvent = new SipResponseEvent(responseEvent);
    }

    private void setupAdditionalRequest() {
        mockAdditionalRequest = mock(Request.class);
        Mock mockAdditionalServerTransaction = mock(ServerTransaction.class);
        RequestEvent additionalRequestEvent = new RequestEvent(mockSipProvider.proxy(),
                (ServerTransaction) mockAdditionalServerTransaction.proxy(),
                (Dialog) mockDialog.proxy(),
                (Request) mockAdditionalRequest.proxy());
        additionalSipRequestEvent = new SipRequestEventImpl(additionalRequestEvent);
        additionalSipResponse =
                new SipResponse((Response)mockResponse.proxy(),
                        (ServerTransaction)mockAdditionalServerTransaction.proxy(),
                        additionalSipRequestEvent.getSipProvider());
    }

    private void setupDialog() {
        mockDialog = mock(Dialog.class);
    }

    private void setupInitialRequest() {
        mockInitialRequest = mock(Request.class);
        mockInitialServerTransaction = mock(ServerTransaction.class);

        mockInitialServerTransaction.stubs().method("getDialog").
                will(returnValue(mockDialog.proxy()));
        mockDialog.stubs().method("isServer").
                will(returnValue(true));
        mockInitialServerTransaction.stubs().method("getRequest").
                will(returnValue(mockInitialRequest.proxy()));

        RequestEvent initialRequestEvent = new RequestEvent(mockSipProvider.proxy(),
                (ServerTransaction) mockInitialServerTransaction.proxy(),
                (Dialog) mockDialog.proxy(),
                (Request) mockInitialRequest.proxy());
        initialSipRequestEvent = new SipRequestEventImpl(initialRequestEvent);
        initialSipResponse =
                new SipResponse((Response)mockResponse.proxy(),
                        (ServerTransaction)mockInitialServerTransaction.proxy(),
                        initialSipRequestEvent.getSipProvider());
    }

    private void setupMockedEvents() {
        Timeout timeout = Timeout.TRANSACTION;
        TimeoutEvent timeoutEvent = new TimeoutEvent(this,
                (ServerTransaction) mockInitialServerTransaction.proxy(), timeout);
        sipTimeoutEvent = new SipTimeoutEvent(timeoutEvent);

        playEvent = new PlayEvent(this, (IMediaObject)null, null, 0);
        recordEvent = new RecordEvent(this, null, null);
        stopPlayEvent = new StopPlayEvent(this);
        stopRecordEvent = new StopRecordEvent(this);
    }

    private void setupExpectations() throws Exception {

        mockInitialRequest.stubs().method("getHeader").with(eq("Reason")).
                will(returnValue(null));
        mockAdditionalRequest.stubs().method("getHeader").with(eq("Reason")).
                will(returnValue(null));

        mockInitialRequest.stubs().method("getMethod").
                will(returnValue("METHOD"));
        mockAdditionalRequest.stubs().method("getMethod").
                will(returnValue("METHOD"));

        mockResponse.stubs().method("getStatusCode").
                will(returnValue(Response.OK));
        mockResponse.stubs().method("getHeader").with(eq("Reason")).
                will(returnValue(null));
    }

    protected final void assertReasonHeader(Mock mockedMessage,
                                      int cause, Integer location)
            throws InvalidArgumentException, ParseException {
        ReasonHeader header = CMUtils.getInstance().getSipHeaderFactory().
                createQ850ReasonHeader(cause, location);
        mockedMessage.expects(once()).method("getHeader").with(eq("Reason")).
                will(returnValue(header));
    }

    /**
     * Returns a stub that shall be used in a jmock will statement
     * in order to validate a failed event.
     */
    private Stub validateFailedEvent(Mock mockedCall,
                                    FailedEvent.Reason reason,
                                    CallDirection direction,
                                    String message,
                                    Integer networkStatusCode) {
        return new FailedEventValidatorStub(
                mockedCall, reason, direction, message, networkStatusCode);
    }

    /**
     * Returns a stub that shall be used in a jmock will statement
     * in order to validate an error event.
     */
    private Stub validateErrorEvent(Mock mockedCall,
                                   CallDirection direction,
                                   String message,
                                   boolean alreadyDisconnected) {
        return new ErrorEventValidatorStub(
                mockedCall, direction, message, alreadyDisconnected);
    }

    /**
     * Returns a stub that shall be used in a jmock will statement
     * in order to validate a progressing event.
     */
    private Stub validateProgressingEvent(Mock mockedCall,
                                         boolean isEarlyMedia) {
        return new ProgressingEventValidatorStub(
                mockedCall, isEarlyMedia);
    }

    /**
     * Returns a stub that shall be used in a jmock will statement
     * in order to validate a disconnected event.
     */
    private Stub validateDisconnectedEvent(Mock mockedCall,
                                          DisconnectedEvent.Reason reason,
                                          boolean alreadyDisconnected) {
        return new DisconnectedEventValidatorStub(
                mockedCall, reason, alreadyDisconnected);
    }

    /**
     * Returns a stub that shall be used in a jmock will statement
     * in order to validate a not allowed event.
     */
    private Stub validateNotAllowedEvent(Mock mockedCall,
                                        String message) {
        return new NotAllowedEventValidatorStub(mockedCall, message);
    }

    protected final void assertGetContactHeaders(Mock mockRequest)
            throws ParseException, InvalidArgumentException {
        ContactHeader contactHeader =
                CMUtils.getInstance().getSipHeaderFactory().createContactHeader(
                        "sipPhone", "localhost", 5090, null);
        mockRequest.expects(once()).method("getHeaders").
                with(eq(ContactHeader.NAME)).
                will(returnValue(Arrays.asList(contactHeader).listIterator()));
    }

    protected final void assertFarEndConnection(Mock mockCall) throws Exception {
        mockCall.expects(once()).method("addFarEndConnection").
                with(eq("SIP"), eq("localhost"), eq(5090));
    }


    /**
     * A jmock stub that validates a failed event.
     *
     * @author Malin Flodin
     */
    static final class FailedEventValidatorStub implements Stub {
        private final Mock mockedCall;
        private final FailedEvent.Reason reason;
        private final CallDirection direction;
        private final String message;
        private final Integer networkStatusCode;

        public FailedEventValidatorStub(Mock mockedCall,
                                        FailedEvent.Reason reason,
                                        CallDirection direction,
                                        String message,
                                        Integer networkStatusCode) {
            this.mockedCall = mockedCall;
            this.reason = reason;
            this.direction = direction;
            this.message = message;
            this.networkStatusCode = networkStatusCode;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            Object methodParameter = invocation.parameterValues.get(0);
            assert(methodParameter instanceof FailedEvent);
            FailedEvent failedParameter = (FailedEvent)methodParameter;
            assertEquals(reason, failedParameter.getReason());
            assertEquals(mockedCall.proxy(), failedParameter.getCall());
            assertEquals(direction, failedParameter.getDirection());
            assertEquals(message, failedParameter.getMessage());
            if (networkStatusCode != null)
                assertEquals(networkStatusCode.intValue(),
                        failedParameter.getNetworkStatusCode());
            return null;
        }

        public StringBuffer describeTo(StringBuffer stringBuffer) {
            return stringBuffer;
        }
    }


    /**
     * A jmock stub that validates an error event.
     *
     * @author Malin Flodin
     */
    static final class ErrorEventValidatorStub implements Stub {
        final Mock mockedCall;
        final CallDirection direction;
        final String message;
        final boolean alreadyDisconnected;

        public ErrorEventValidatorStub(Mock mockedCall,
                                       CallDirection direction,
                                       String message,
                                       boolean alreadyDisconnected) {
            this.mockedCall = mockedCall;
            this.direction = direction;
            this.message = message;
            this.alreadyDisconnected = alreadyDisconnected;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            Object methodParameter = invocation.parameterValues.get(0);
            assert(methodParameter instanceof ErrorEvent);
            ErrorEvent errorParameter = (ErrorEvent)methodParameter;
            assertEquals(mockedCall.proxy(), errorParameter.getCall());
            assertEquals(direction, errorParameter.getDirection());
            assertEquals(message, errorParameter.getMessage());
            assertEquals(alreadyDisconnected,
                    errorParameter.isAlreadyDisconnected());
            return null;
        }

        public StringBuffer describeTo(StringBuffer stringBuffer) {
            return stringBuffer;
        }
    }

    /**
     * A jmock stub that validates a progressing event.
     *
     * @author Malin Flodin
     */
    static final class ProgressingEventValidatorStub implements Stub {
        final Mock mockedCall;
        final boolean isEarlyMedia;

        public ProgressingEventValidatorStub(Mock mockedCall,
                                             boolean isEarlyMedia) {
            this.mockedCall = mockedCall;
            this.isEarlyMedia = isEarlyMedia;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            Object methodParameter = invocation.parameterValues.get(0);
            assert(methodParameter instanceof ProgressingEvent);
            ProgressingEvent progressingParameter =
                    (ProgressingEvent)methodParameter;
            assertEquals(mockedCall.proxy(), progressingParameter.getCall());
            assertEquals(isEarlyMedia, progressingParameter.isEarlyMedia());
            return null;
        }

        public StringBuffer describeTo(StringBuffer stringBuffer) {
            return stringBuffer;
        }
    }

    /**
     * A jmock stub that validates a disconnected event.
     *
     * @author Malin Flodin
     */
    static final class DisconnectedEventValidatorStub implements Stub {
        final Mock mockedCall;
        final DisconnectedEvent.Reason reason;
        final boolean alreadyDisconnected;

        public DisconnectedEventValidatorStub(Mock mockedCall,
                                              DisconnectedEvent.Reason reason,
                                              boolean alreadyDisconnected) {
            this.mockedCall = mockedCall;
            this.reason = reason;
            this.alreadyDisconnected = alreadyDisconnected;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            Object methodParameter = invocation.parameterValues.get(0);
            assert(methodParameter instanceof DisconnectedEvent);
            DisconnectedEvent disconnectedParameter =
                    (DisconnectedEvent)methodParameter;
            assertEquals(reason, disconnectedParameter.getReason());
            assertEquals(mockedCall.proxy(), disconnectedParameter.getCall());
            assertEquals(alreadyDisconnected,
                    disconnectedParameter.isAlreadyDisconnected());
            return null;
        }

        public StringBuffer describeTo(StringBuffer stringBuffer) {
            return stringBuffer;
        }
    }


    /**
     * A jmock stub that validates an error event.
     *
     * @author Malin Flodin
     */
    static final class NotAllowedEventValidatorStub implements Stub {
        final Mock mockedCall;
        final String message;

        public NotAllowedEventValidatorStub(Mock mockedCall,
                                            String message) {
            this.mockedCall = mockedCall;
            this.message = message;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            Object methodParameter = invocation.parameterValues.get(0);
            assert(methodParameter instanceof NotAllowedEvent);
            NotAllowedEvent notAllowedParameter = (NotAllowedEvent)methodParameter;
            assertEquals(mockedCall.proxy(), notAllowedParameter.getCall());
            assertEquals(message, notAllowedParameter.getMessage());
            return null;
        }

        public StringBuffer describeTo(StringBuffer stringBuffer) {
            return stringBuffer;
        }
    }
}
