
/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;
import com.mobeon.masp.stream.*;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.callmanager.callhandling.states.CallState;
import com.mobeon.masp.callmanager.callhandling.events.CallCommandEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallTimeoutEvent;
import com.mobeon.masp.callmanager.callhandling.events.VideoFastUpdateRequestEvent;
import com.mobeon.masp.callmanager.callhandling.events.PlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.RecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopPlayEvent;
import com.mobeon.masp.callmanager.callhandling.events.StopRecordEvent;
import com.mobeon.masp.callmanager.callhandling.events.AbandonedStreamEvent;
import com.mobeon.masp.callmanager.callhandling.calleventlistener.DroppedPacketsEvent;
import com.mobeon.masp.callmanager.callhandling.calleventlistener.CallEventListener;
import com.mobeon.masp.callmanager.sessionestablishment.PreconditionStatusTable;
import com.mobeon.masp.callmanager.sessionestablishment.UnicastStatus;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.sip.message.SipMessage;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.header.SipContentSubType;
import com.mobeon.masp.callmanager.sip.header.SipWarning;
import com.mobeon.masp.callmanager.queuehandling.SequenceGuaranteedEventQueue;
import com.mobeon.masp.callmanager.queuehandling.CommandExecutor;
import com.mobeon.masp.callmanager.sdp.*;
import com.mobeon.masp.callmanager.events.CloseForcedEvent;
import com.mobeon.masp.callmanager.events.ProxyingEvent;
import com.mobeon.masp.callmanager.events.SendTokenErrorEvent;
import com.mobeon.masp.callmanager.events.RemoveCallEvent;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.events.EventObject;
import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.videofastupdate.MediaControlImpl;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.abcxyz.messaging.oe.common.util.KPIProfiler;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.mediaobject.IMediaObject;

import javax.sip.Dialog;
import javax.sip.message.Request;
import javax.sip.message.Response;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Collection;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.xmlbeans.XmlException;
import gov.nist.javax.sip.header.ims.PChargingVectorHeader;

/**
 * Abstract class representing a call. A call can be either an inbound
 * or an outbound call.
 *
 * TODO: Phase 2! Cleanup, simplify and refactor.
 *
 * <p>
 * The {@link streamLock} object is used for synchronizing events regarding
 * stream modification such as creation, deletion, join and unjoin.
 * Modification of parameters {@link inboundStream},
 * {@link outboundStream}, {@link callIsJoined} and {@link joinedToCall} must
 * be done within such a synchronized block.
 * <em>
 * NOTE:
 * It is important to be restrictive when to use this lock. The reason
 * to be restrictive is that in join/unjoin scenarios one call uses methods in
 * another call, and in order to avoid dead-lock scenarios nested lock
 * synchronizations MUST BE AVOIDED.
 * All methods defined in the {@link CallToCall} interface are such methods
 * that might be called by another call. Currently, one of those methods
 * ({@link CallToCall#unjoin(CallToCall)}) is implemented synchronized and must
 * therefore NOT be called from a synchronized block/method itself.
 * It can be seen that the {@link #deleteStreams()} method has been designed
 * with this in mind.
 * </em>
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public abstract class CallImpl implements
        Call, CallInternal, CallToCall,
        CommandExecutor, VideoFastUpdater, IEventReceiver {

    private static final String SELECTED_CALL_MEDIA_TYPES = "selectedcallmediatypes";
    private static final String CALL_MEDIA_TYPES_ARRAY = "callmediatypesarray";

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final CallManagerConfiguration config;

    // Call Information.
    // Thread-safe due to use of synchronized setters and getters.
    private CallingParty callingParty;
    private CalledParty calledParty;
    // TODO: Phase 2! Document when this is set and that it should always be used from the getter! Clean-up if possible!
    private CallProperties.CallType callType = CallProperties.CallType.UNKNOWN;

    private CallProperties.CallType licenseInUse = CallProperties.CallType.UNKNOWN;

    // SDP related
    // Thread-safe due to use of synchronized setters and getters.
    private SdpSessionDescriptionFactory sdpFactory;

    private SdpIntersection sdpIntersection;

    private String localSdpAnswer;
    private String localSdpOffer;

    private AtomicReference<SdpSessionDescription> remoteSdp = new AtomicReference<SdpSessionDescription>();
    private AtomicReference<String> remoteSdpString = new AtomicReference<String>();

    private AtomicReference<SdpSessionDescription> pendingRemoteSdp = new AtomicReference<SdpSessionDescription>();
    private AtomicReference<String> pendingRemoteSdpString = new AtomicReference<String>();

    private PreconditionStatusTable preconditionStatusTable = new PreconditionStatusTable();
    private UnicastStatus unicastStatus = new UnicastStatus();

    private AtomicReference<CallMediaTypes[]> outboundMediaTypes = new AtomicReference<CallMediaTypes[]>();

    // Stream related, the following variables are protected using streamLock
    private IInboundMediaStream inboundStream = null;
    private IOutboundMediaStream outboundStream = null;
    private AtomicBoolean callIsJoined = new AtomicBoolean(false);
    private AtomicReference<CallToCall> joinedToCall = new AtomicReference<CallToCall>();
    private ConnectionProperties inboundConnectionProperties;

    // NOTE: The streamLock should be used restrictively as described at the top of this class.
    private final Object streamLock = new Object();

    // Call Timers
    // TimerTask to prevent calls hanging while waiting for an ACK.
    private CallTimerTask noAckTimerTask;

    // Dialog related information
    // Thread-safe due to use of synchronized setters and getters.
    private Dialog dialog;
    private String initialDialogId;
    private String establishedDialogId;
    private boolean isEarlyDialogActive = true;

    private PChargingVectorHeader pChargingVectorHeader = null;

    private AtomicReference<CallState> currentState = new AtomicReference<CallState>();

    // Thread-safe due to immutable, i.e set at construction time
    private static final Timer callTimer = new Timer(true);
    static {
        /**
         * Create and schedule recurring task to purge cancelled timer object.
         * The JVM does not release reference to TimerTask until the delay expires even if they are cancelled.
         * So this will actively purge them. This is necessary when TimerTask are scheduled with a long delay.
         */
        TimerTask purgeTimeoutSchedulerTask = new TimerTask() {
            @Override
            public void run() {
                callTimer.purge();
            }
        };
        
        callTimer.schedule(purgeTimeoutSchedulerTask, 90L, 90L);
    }

    // Thread-safe due to use of synchronized setters and getters.
    private IEventDispatcher eventDispatcher;
    protected ISession session;

    // Thread-safe due to immutable, i.e set at construction time and never
    // changed
    private SequenceGuaranteedEventQueue eventQueue =
            new SequenceGuaranteedEventQueue(this, CallImpl.class);

    protected AtomicBoolean callIsRemoved = new AtomicBoolean(false);

    // Thread-safe due to use of synchronized setters and getters.
    // TODO: Phase 2! Can this be done better?
    private boolean isConnected = false;

    private Set<Connection> farEndConnections = Collections.synchronizedSet(new TreeSet<Connection>());


    public abstract String getCallId();

    // Thread-safe due to synchronized get and set methods.
    private Map<String, SipRequestEvent> pendingRequests =
            new HashMap<String, SipRequestEvent>();

    protected CallImpl(CallManagerConfiguration config) {
        this.config = config;
    }

    // Setters required to be called before the call can be used.
    public void setSessionDescriptionFactory (
            SdpSessionDescriptionFactory sdpFactory) {
        this.sdpFactory = sdpFactory;
    }


    // Getters and Setters
    public CallManagerConfiguration getConfig() {
        return config;
    }

    public Timer getCallTimer() {
        return callTimer;
    }

    public synchronized void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public synchronized Dialog getDialog() {
        return dialog;
    }

    public synchronized String getInitialDialogId() {
        return initialDialogId;
    }

    public synchronized String getEstablishedDialogId() {
        return establishedDialogId;
    }

    public synchronized void setInitialDialogId(String initialDialogId) {
        this.initialDialogId = initialDialogId;
    }

    public synchronized void setEstablishedDialogId(String establishedDialogId) {
        this.establishedDialogId = establishedDialogId;
    }

    /**
     * Returns whether the call is an early dialog.
     * Currently, the only reason to keep control of whether the dialog is
     * early or established is for the call dispatching.
     * Thus, this method MUST NOT be used by any class other than CallDispatcher.
     *
     * @return true if the call is an early dialog. False if the call is an
     * established dialog.
     */
    public synchronized boolean isEarlyDialogActive() {
        return isEarlyDialogActive;
    }

    /**
     * Inactivates the "early" call feature, i.e. indicates that the call now is
     * an established dialog.
     * Currently, the only reason to keep control of whether the dialog is
     * early or established is for the call dispatching.
     * Thus, this method MUST NOT be used by any class other than CallDispatcher.
     */
    public synchronized void inactivateEarlyDialog() {
        isEarlyDialogActive = false;
    }

    public CallState getCurrentState() {
        return currentState.get();
    }

    protected void setCurrentState(CallState currentState) {
        this.currentState.set(currentState);
    }

    public synchronized IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public synchronized void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public synchronized ISession getSession() {
        return session;
    }

    public SdpSessionDescription getRemoteSdp() {
        return remoteSdp.get();
    }

    public String getRemoteSdpString() {
        return remoteSdpString.get();
    }

    public void setRemoteSdp(SdpSessionDescription remoteSdp, String sdpString) {
        this.remoteSdp.set(remoteSdp);
        this.remoteSdpString.set(sdpString);
    }

    public SdpSessionDescription getPendingRemoteSdp() {
        return pendingRemoteSdp.get();
    }

    public String getPendingRemoteSdpString() {
        return pendingRemoteSdpString.get();
    }

    public void setPendingRemoteSdp(SdpSessionDescription remoteSdp, String sdpString) {
        this.pendingRemoteSdp.set(remoteSdp);
        this.pendingRemoteSdpString.set(sdpString);
    }

    public PreconditionStatusTable getPreconditionStatusTable() {
        return preconditionStatusTable;
    }

    public void setPreconditionStatusTable(PreconditionStatusTable statusTable) {
        this.preconditionStatusTable = statusTable;
    }

    public UnicastStatus getUnicastStatus() {
        return unicastStatus;
    }

    public void setUnicastStatus(UnicastStatus unicastStatus) {
        this.unicastStatus = unicastStatus;
    }

    public synchronized CallingParty getCallingParty() {
        return callingParty;
    }

    protected synchronized void setCallingParty(CallingParty callingParty) {
        this.callingParty = callingParty;
    }

    public synchronized CalledParty getCalledParty() {
        return calledParty;
    }

    protected synchronized void setCalledParty(CalledParty calledParty) {
        this.calledParty = calledParty;
    }

    public synchronized CallProperties.CallType getCallType() {
        SdpIntersection sdpIntersection = getSdpIntersection();
        if (sdpIntersection != null)
            return sdpIntersection.getCallType();

        return callType;
    }

    public synchronized void setCallType(CallProperties.CallType callType) {
        this.callType = callType;
    }

    public void retrieveCallTypeFromConfiguration() {
        setCallType(getConfig().getCallType());
    }

    public synchronized SdpIntersection getSdpIntersection() {
        return sdpIntersection;
    }

    public synchronized void setSdpIntersection(SdpIntersection sdpIntersection) {
        this.sdpIntersection = sdpIntersection;
    }

    public synchronized String getLocalSdpAnswer() {
        return localSdpAnswer;
    }

    public synchronized void setLocalSdpAnswer(String localSdpAnswer) {
        this.localSdpAnswer = localSdpAnswer;
    }

    public synchronized String getLocalSdpOffer() {
        return localSdpOffer;
    }

    public synchronized void setLocalSdpOffer(String localSdpOffer) {
        this.localSdpOffer = localSdpOffer;
    }

    public String getProtocolName() {
        return CMUtils.getInstance().getProtocol();
    }

    public String getProtocolVersion() {
        return CMUtils.getInstance().getVersion();
    }

    public ConnectionProperties getInboundConnectionProperties() {
        return inboundConnectionProperties;
    }

    public void setInboundConnectionProperties(
            ConnectionProperties inboundConnectionProperties) {
        this.inboundConnectionProperties = inboundConnectionProperties;
    }

    public IInboundMediaStream getInboundStream() {
        return inboundStream;
    }

    public IOutboundMediaStream getOutboundStream() {
        return outboundStream;
    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    public synchronized void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void doEvent(Event event) {
        if (event instanceof StreamAbandonedEvent) {
            if (log.isDebugEnabled())
                log.debug("A stream abandoned event is received.");
            eventQueue.queue(new AbandonedStreamEvent());
        }
    }

    public void doGlobalEvent(Event event) {
        if (log.isDebugEnabled())
            log.debug("Global event is received and ignored: " + event);
    }

    public void registerToReceiveEvents() {
        getEventDispatcher().addEventReceiver(this);
    }

    /**
     * Queues an event in the call event queue. Each event is handled
     * one at a time in the order they arrived.
     * @param event
     */
    public void queueEvent(EventObject event) {
    	Object perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.In.Call.queueEvent");
    	try {
        if (log.isDebugEnabled()) {
            if (event instanceof SipRequestEvent) {
                log.debug("Queueing SIP request event with method: " +
                        ((SipRequestEvent)event).getMethod());
            } else {
                log.debug("Queueing event: " + event);
            }
        }
        eventQueue.queue(event);
    	} finally {
    	  	 if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                 CommonOamManager.profilerAgent.exitCheckpoint(perf);
             }
    	}
    }

    /**
     * Fires an event to the event dispatcher.
     * @param event
     */
    public void fireEvent(Event event) {
        IEventDispatcher eventDispatcher = getEventDispatcher();

        if (log.isDebugEnabled())
            log.debug("Firing event: <EventDispatcher = " +
                    eventDispatcher + ">, <Event = " + event + ">");

        // ProxyingEvent is not sent to Call Manager's client (internal event)
        boolean proxyEvent = event instanceof ProxyingEvent;

        if (eventDispatcher != null && !proxyEvent) {
            try {
                if (log.isInfoEnabled()) log.info("Fired event: " + event);
                eventDispatcher.fireEvent(event);
            } catch (Exception e) {
                log.error(
                        "Exception occurred when firing event: " + e.getMessage(),
                        e);
            }
        }
        updateStatistics(event);
    }

    private void updateStatistics(Event event) {
        List<CallEventListener> callEventListeners = CMUtils.getInstance().getCallEventListeners();
        for (CallEventListener callEventListener : callEventListeners) {
            callEventListener.processCallEvent(event);
        }
    }

    /**
     * Creates a new inbound media stream based on the configured inbound Mime
     * types and call type. Returns the connection properties for the stream.
     *
     * @param sdpIntersection The SDP intersection if one exists,
     * set to null otherwise
     * @return the inbound connection properties for the stream.
     * @throws StackException if it was not possible to create the stream.
     */
    public ConnectionProperties createInboundStream(SdpIntersection sdpIntersection)
    	throws StackException, SdpInternalErrorException, CallManagerLicensingException{

        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {

            String logMessage;
            if (sdpIntersection == null) {
                MediaMimeTypes mediaMimeTypes;
                getLicense(getCallType());
                if (getCallType() == CallProperties.CallType.VIDEO) {
                    mediaMimeTypes = new MediaMimeTypes(
                            getConfig().getInboundAudioMimeType(),
                            getConfig().getInboundVideoMimeType());

                } else {
                    mediaMimeTypes = new MediaMimeTypes(
                            getConfig().getInboundAudioMimeType());
                }

                logMessage = "Media mime types = <" + mediaMimeTypes + ">";
                if (log.isDebugEnabled())
                    log.debug("Creating inbound stream with " + logMessage);


                inboundStream = getInboundMediaStream();
                inboundStream.create(this, mediaMimeTypes);

            } else {
                Collection<RTPPayload> rtpPayloads =
                    sdpIntersection.getSupportedRtpPayloads();

                logMessage = "RTP Payloads = <" + rtpPayloads + ">";
                if (log.isDebugEnabled())
                    log.debug("Creating inbound stream with " + logMessage);
                getLicense(sdpIntersection.getCallType());
                inboundStream = getInboundMediaStream();
                inboundStream.create(this, rtpPayloads);

            }

            ConnectionProperties connectionProperties = new ConnectionProperties();
            connectionProperties.setAudioHost(inboundStream.getHost());
            connectionProperties.setAudioPort(inboundStream.getAudioPort());
            connectionProperties.setPTime(inboundStream.getPTime());
            connectionProperties.setMaxPTime(inboundStream.getMaxPTime());

            if (getCallType() == CallProperties.CallType.VIDEO) {
                connectionProperties.setVideoHost(inboundStream.getHost());
                connectionProperties.setVideoPort(inboundStream.getVideoPort());
            }

            if (log.isInfoEnabled())
                log.info("Inbound stream created. Properties = <" +
                	connectionProperties + ">, " + logMessage);

            setInboundConnectionProperties(connectionProperties);
            return connectionProperties;
        }
    }

    /**
     * @return
     *
     */
    private IInboundMediaStream getInboundMediaStream() {
        IInboundMediaStream inboundStream = CMUtils.getInstance().getStreamFactory().getInboundMediaStream();
        inboundStream.setEventDispatcher(getEventDispatcher());
        inboundStream.setCallSession(getSession());
        inboundStream.setCNAME(getConfig().getRegisteredName() + "@" +  CMUtils.getInstance().getLocalHost());
        return inboundStream;
    }

    /**
     * Creates a new outbound media stream based on the given SDP intersection.
     * @param sdpIntersection
     * @throws StackException if it was not possible to create the stream.
     * @throws SdpInternalErrorException if the RTP payload types for the
     * outbound stream could not be determined.
     * @throws IllegalStateException if the hosts and ports to use for the
     * outbound stream already is in use.
     */
    public void createOutboundStream(SdpIntersection sdpIntersection)
            throws StackException, SdpInternalErrorException, IllegalStateException {

        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {

            ConnectionProperties cp = sdpIntersection.getConnectionProperties();

            outboundStream =
                    CMUtils.getInstance().getStreamFactory().getOutboundMediaStream();
            outboundStream.setEventDispatcher(getEventDispatcher());
            outboundStream.setCallSession(getSession());
            outboundStream.setCNAME(getConfig().getRegisteredName() + "@" +
                            CMUtils.getInstance().getLocalHost());

            if (log.isDebugEnabled())
                log.debug("Creating outbound stream with RTP Payloads: " +
                        sdpIntersection.getSupportedRtpPayloads());

            outboundStream.create(sdpIntersection.getSupportedRtpPayloads(),
                    cp, sdpIntersection.getRTCPFeedback(), getInboundStream());

            // Add stream(s) to far end connections
            addFarEndConnection("RTP", cp.getAudioHost(), cp.getAudioPort());
            if (cp.getVideoHost() != null) {
                addFarEndConnection("RTP", cp.getVideoHost(), cp.getVideoPort());
            }

            if (log.isInfoEnabled())
                log.info("Outbound stream created. Properties = <" + cp +
                        ">, RTP Payloads = <" +
                        sdpIntersection.getSupportedRtpPayloads() + ">");
        }
    }

    /**
     * Adds a <code>Connection</code> object to the far end connections set
     * @param protocol the protocol of the <code>Connection</code>
     * @param host the host of the <code>Connection</code>
     * @param port the protocol of the <code>Connection</code>
     */
    public void addFarEndConnection(String protocol, String host, int port) {
        try {
            farEndConnections.add(new Connection(protocol, InetAddress.getByName(host), port));
        } catch (UnknownHostException e) {
            if (log.isDebugEnabled()) {
                StringBuilder connection = new StringBuilder();
                connection.append(protocol).append("://").append(host).append(":").append(port);
                log.debug("Could not add " + connection + " to far end connections. " + e);
            }
        }
    }

    /**
     * Store the P-Charging-Vector info.
     * @param pChargingVectorHeader
     */
    public void setPChargingVector(PChargingVectorHeader pChargingVectorHeader) {
        this.pChargingVectorHeader = pChargingVectorHeader;
    }

    /**
     * Retrieve the P-Charging-Vector info.
     * @return the stored P-Charging-Vector
     */
    public PChargingVectorHeader getPChargingVector() {
        return pChargingVectorHeader;
    }

    /**
     * Deletes all active media streams.
     * If the call is joined, the calls are unjoined first.
     */
    public void deleteStreams() {
        CallToCall otherCall = null;

        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {

            // Unjoin streams if joined
            if (callIsJoined.get()) {
                otherCall = joinedToCall.get();
                try {
                    unjoinStreams();
                } catch (Exception e) {
                    if (log.isDebugEnabled())
                        log.debug("Could not unjoin the streams of this call.", e);
                }
            }

            // Delete outbound stream
            if (outboundStream != null) {
                outboundStream.delete();
                outboundStream = null;
            }

            // Delete inbound stream
            releaseLicense();
            if (inboundStream != null) {
                try {
                    int packetLoss = inboundStream.getCumulativePacketLost();

                    if (packetLoss > 0) {
                        // Report the event to any call event listeners
                        Event event = new DroppedPacketsEvent(this, packetLoss);
                        updateStatistics(event);
                    }

                } catch (StackException e) {
                    if (log.isDebugEnabled())
                        log.debug("Exception while retrieving the cumulative " +
                                "packet loss for an inbound stream. Probably because " +
                                "the stream already has been deleted.");
                }
                inboundStream.delete();
                inboundStream = null;
            }

        }

        // Unjoin other call if joined.
        // NOTE: This part MUST NOT be done in a synchronized section!
        // The reason for this is since the unjoin on the other call will
        // synchronize on that calls streamLock and that may cause deadlock if
        // this method is called about the same time in both calls.
        if (otherCall != null) {
            try {
                otherCall.unjoin(this);
            } catch (Exception e) {
                if (log.isDebugEnabled())
                    log.debug("Could not unjoin the other call.", e);
            }
        }

    }

    /**
     * Deletes all active outbound media stream.
     * If the call is joined, the calls are unjoined first.
     */
    public void deleteOutboundStream() {
        CallToCall otherCall = null;

        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {

            // Unjoin streams if joined
            if (callIsJoined.get()) {
                otherCall = joinedToCall.get();
                try {
                    unjoinStreams();
                } catch (Exception e) {
                    if (log.isDebugEnabled())
                        log.debug("Could not unjoin the streams of this call.", e);
                }
            }

            // Delete outbound stream
            if (outboundStream != null) {
                outboundStream.delete();
                outboundStream = null;
            }

        }

        // Unjoin other call if joined.
        // NOTE: This part MUST NOT be done in a synchronized section!
        // The reason for this is since the unjoin on the other call will
        // synchronize on that calls streamLock and that may cause deadlock if
        // this method is called about the same time in both calls.
        if (otherCall != null) {
            try {
                otherCall.unjoin(this);
            } catch (Exception e) {
                if (log.isDebugEnabled())
                    log.debug("Could not unjoin the other call.", e);
            }
        }

    } 

    public void playOnOutboundStream(PlayEvent playEvent) {
        try {
            if (playEvent.getMediaObject() != null) {
                getOutboundStream().play(
                        playEvent.getId(), playEvent.getMediaObject(),
                        playEvent.getPlayOption(), playEvent.getCursor());
            } else {
                getOutboundStream().play(
                        playEvent.getId(), playEvent.getMediaObjects(),
                        playEvent.getPlayOption(), playEvent.getCursor());
            }
        } catch (StackException e) {
            if (log.isDebugEnabled())
                log.debug("StackException occurred when playing media on " +
                        "outbound media stream.");
        } catch (RuntimeException e) {
            String message =
                    "RuntimeException occurred when playing media on " +
                            "outbound media stream.";
            if (log.isDebugEnabled())
                log.debug(message, e);
            fireEvent( new PlayFailedEvent(playEvent.getId(), message));
        }
    }

    public void sendTokens(ControlToken[] tokens) {
        try {
            getOutboundStream().send(tokens);
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Error occurred when sending tokens on the outbound " +
                        "media stream.");
            fireEvent(new SendTokenErrorEvent(this));
        }
    }

    public void recordOnInboundStream(RecordEvent recordEvent) {
        try {
            if (recordEvent.getPlayMediaObject() == null) {
                getInboundStream().record(
                        recordEvent.getId(),
                        recordEvent.getRecordMediaObject(),
                        recordEvent.getProperties());
            } else {
                getInboundStream().record(
                        recordEvent.getId(),
                        recordEvent.getPlayMediaObject(),
                        getOutboundStream(),
                        recordEvent.getRecordMediaObject(),
                        recordEvent.getProperties());
            }

        } catch (StackException e) {
            if (log.isDebugEnabled())
                log.debug("StackException occurred when recording media on " +
                        "inbound media stream.");
        } catch (RuntimeException e) {
            String message =
                    "RuntimeException occurred when recoding media on " +
                            "inbound media stream.";
            if (log.isDebugEnabled())
                log.debug(message, e);
            fireEvent( new RecordFailedEvent(
                    recordEvent.getId(), RecordFailedEvent.CAUSE.EXCEPTION,
                    message));
        }
    }

    public void stopOngoingPlay(StopPlayEvent stopPlayEvent) {
        try {
            getOutboundStream().stop(stopPlayEvent.getId());
        } catch (Exception e) {
            log.error("Exception occurred when trying to stop ongoing play.", e);
        }
    }

    public void stopOngoingRecord(StopRecordEvent stopRecordEvent) {
        try {
            getInboundStream().stop(stopRecordEvent.getId());
        } catch (Exception e) {
            log.error("Exception occurred when trying to stop ongoing record.", e);
        }
    }

    /**
     * Retrieve and returns call media types (from Session) to use in
     * negotiation in the call.
     */
    public CallMediaTypes[] getOutboundCallMediaTypes() {

        if (outboundMediaTypes.get() == null) { 

            CallMediaTypes[] callMediaTypes = null;

            // First try to retrieve the "selectedcallmediatypes"
            Object tmp = getSession().getData(SELECTED_CALL_MEDIA_TYPES);
            if (log.isDebugEnabled())
                log.debug("Trying to retrieve from session: " + SELECTED_CALL_MEDIA_TYPES +
                        "=" + tmp);
            if (tmp instanceof CallMediaTypes) {
                callMediaTypes = new CallMediaTypes[] {(CallMediaTypes)tmp};
                if (log.isDebugEnabled())
                    log.debug("Retrieved from session: " +
                            SELECTED_CALL_MEDIA_TYPES +"=" + callMediaTypes);
            }

            // If not found, try to retrieve the "callmediatypesarray"
            if (callMediaTypes == null) {
                tmp = getSession().getData(CALL_MEDIA_TYPES_ARRAY);
                if (log.isDebugEnabled())
                    log.debug("Trying to retrieve from session: " +
                            CALL_MEDIA_TYPES_ARRAY + "=" + tmp);
                if (tmp instanceof CallMediaTypes[]) {
                    callMediaTypes = (CallMediaTypes[])tmp;
                    if (log.isDebugEnabled())
                        log.debug("Retrieved from session: " +
                                CALL_MEDIA_TYPES_ARRAY +"=" +
                                Arrays.toString(callMediaTypes));
                }
            }

            // Store the call media types
            outboundMediaTypes.set(callMediaTypes);
            if (log.isDebugEnabled())
                log.debug("The outbound media types to choose from is: " +
                        Arrays.toString(outboundMediaTypes.get()));
        }

        return outboundMediaTypes.get();
    }

    public CallMediaTypes[] getConfiguredOutboundCallMediaTypes() {
        MediaMimeTypes mediaMimeTypes;
        CallProperties.CallType callType = getCallType();
        if (callType == CallProperties.CallType.UNKNOWN) {
            callType = getConfig().getCallType();
            if (log.isInfoEnabled()) {
                log.info("CallType is not defined. Using default CallType=< "
                        + callType + ">");
            }
        }

        if (callType == CallProperties.CallType.VIDEO) {
            mediaMimeTypes = new MediaMimeTypes(
                    getConfig().getInboundAudioMimeType(),
                    getConfig().getInboundVideoMimeType());
        } else {
            mediaMimeTypes = new MediaMimeTypes(
                    getConfig().getInboundAudioMimeType());
        }

        return new CallMediaTypes[]{new CallMediaTypes(mediaMimeTypes, this)};
    }

    public void reNegotiatedSdpOnInboundStream(SdpIntersection sdpIntersection)
            throws SdpInternalErrorException {
        try {

            Collection<RTPPayload> payloads = sdpIntersection.getSupportedRtpPayloads();

            int numberOfCodecs = payloads.size();


            log.debug("Size of Payloads: " + payloads.size());

            RTPPayload[] payloadsArray;
            payloadsArray = new RTPPayload[numberOfCodecs];
            payloadsArray = payloads.toArray(payloadsArray);

            //int dTMFPayloadType =0;
            RTPPayload dtmfPayLoad = null;
            for (RTPPayload payloadInstance : payloadsArray) {

                if (log.isDebugEnabled()) {
                    log.debug("Payload: " + payloadInstance.toString());
                    log.debug("Payload MimeType: " + payloadInstance.getMimeType());
                }
                if (payloadInstance.getMimeType().toString().equals(RTPPayload.AUDIO_DTMF.toString())) {
                    if (log.isDebugEnabled()) {
                        log.debug("MimeType: " + payloadInstance.getMimeType().toString());
                        log.debug("Equals MimeType: " + RTPPayload.AUDIO_DTMF.toString());
                    }
                    dtmfPayLoad = payloadInstance;
                }
            }
            if (dtmfPayLoad != null) {
                log.debug("CallImpl sending reNegotiatedSdp");
                getInboundStream().reNegotiatedSdp(dtmfPayLoad);
            }
        } catch (StackException e) {
            if (log.isDebugEnabled())
                log.debug("StackException occurred when sending reNegotiatedSdp information on " +
                        "outbound media stream.");
        } catch (RuntimeException e) {
            String message =
                    "RuntimeException occurred when sending reNegotiatedSdp information on " +
                            "outbound media stream.";
            if (log.isDebugEnabled())
                log.debug(message, e);
        }
    }

    public void storeSelectedCallMediaTypesInSession(
            CallMediaTypes callMediaType) {
        if(log.isDebugEnabled()){
            log.debug("Setting in session: " + SELECTED_CALL_MEDIA_TYPES +"=" +
                    callMediaType);
        }
        getSession().setData(SELECTED_CALL_MEDIA_TYPES, callMediaType);
    }

    public void parseMediaControlResponse(SipMessage sipMessage) {
        String mediaControl = sipMessage.getContent(SipContentSubType.MEDIA_CONTROL);

        if (mediaControl != null) {
            try {
                List<String> errors =
                        MediaControlImpl.getInstance().getGeneralErrors(mediaControl);
                if (errors.size() > 0) {
                    log.error("The INFO request (carrying a Video " +
                            "Fast Update request) failed.");
                }
            } catch (XmlException e) {
                log.error("Could not parse Media Control XML " +
                        "document in SIP response to INFO request.");
            }
        }
    }

    /**
     * Investigates the sipRequestEvent to determine whether it contains a media
     * control message body or not.
     * @param sipRequestEvent
     * @return Returns true if the sipRequestEvent contains a media control
     * message body, false otherwise.
     */
    public boolean containsMediaControl(SipRequestEvent sipRequestEvent) {
        return sipRequestEvent.getSipMessage().containsMediaControl();
    }

    /**
     * Investigates the sipRequestEvent to determine whether it contains an SDP
     * message body or not.
     * @param sipRequestEvent
     * @return Returns true if the sipRequestEvent contains an SDP
     * message body, false otherwise.
     */
    public boolean containsSdp(SipRequestEvent sipRequestEvent) {
        return sipRequestEvent.getSipMessage().containsSdp();
    }

    /**
     * Parses the remote SDP from the <param>sipMessage<param> and stores it.
     * @param sipMessage
     * @throws SdpNotSupportedException     if there was a syntax error in the
     *                                      SDP string or if the SDP was a
     *                                      re-negotiation not equal to the
     *                                      previously received remote SDP.
     */
    public void parseRemoteSdp(SipMessage sipMessage)
            throws SdpNotSupportedException {
        String sdp = sipMessage.getContent(SipContentSubType.SDP);
        if ((sdp != null) && sdp.length() != 0) {
            SdpSessionDescription remoteSdp =
                    sdpFactory.parseRemoteSdp(sdp);

            if (getRemoteSdp() == null) {
                setRemoteSdp(remoteSdp, sdp);
            } else {
                setPendingRemoteSdp(remoteSdp, sdp);
            }
        }
    }

    /**
     * Checks a pending remote SDP to see if it is equal to the original
     * remote SDP.
     * @throws SdpNotSupportedException     if the pending remote SDP is not
     *                                      equal to the original remote SDP.
     */
    public void checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp() throws SdpNotSupportedException {

        String previousRemoteSdp = getRemoteSdpString();
        if ((previousRemoteSdp != null) && previousRemoteSdp.length() != 0) {

            SdpSessionDescription remotePreviousSdp =
                    sdpFactory.parseRemoteSdp(previousRemoteSdp);

            // If there already exists a remote sdp, verify that the new SDP
            // only contains allowed "re-negotiations".
            // Currently a re-negotiation is only allowed if it is equivalent
            // to the previosly received SDP offer, therefore equals is used.

            String pendingRemoteSdp = getPendingRemoteSdpString();

            if ((previousRemoteSdp != null) && !pendingRemoteSdp.equals(previousRemoteSdp)) {

                log.debug("checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp()- PREVIOUS and CURRENT SDPs are not SAME");
                log.debug("checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp() - Previous Remote SDP " + previousRemoteSdp);
                log.debug("checkIfPendingRemoteSdpIsEqualToOriginalRemoteSdp() - Pending Remote SDP " + pendingRemoteSdp);

                if ((pendingRemoteSdp != null) && pendingRemoteSdp.length() != 0) {

                    SdpSessionDescription remotePendingSdp =
                            sdpFactory.parseRemoteSdp(pendingRemoteSdp);

                    if (!remotePreviousSdp.getOrigin().getUserName().
                            equals(remotePendingSdp.getOrigin().getUserName())) {
                        log.debug("parseRemoteSdp() - SDP Origin Username doesn't match " +
                                "Previous Origin Username =" + remotePreviousSdp.getOrigin().getUserName() +
                                " Pending Origin Username =" + remotePendingSdp.getOrigin().getUserName());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer equal to the previously received" +
                                " SDP offer can be accepted.");
                    }

                    if (remotePreviousSdp.getOrigin().getSessionId()
                            != remotePendingSdp.getOrigin().getSessionId()) {
                        log.debug("parseRemoteSdp() - SDP Origin SessionID doesn't match " +
                                " Previous Origin Session Id=" + remotePreviousSdp.getOrigin().getSessionId() +
                                " Pending Origin Session Id=" + remotePendingSdp.getOrigin().getSessionId());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer equal to the previously received" +
                                " SDP offer can be accepted.");
                    }

                    if (remotePreviousSdp.getOrigin().getSessionVersion()
                            > remotePendingSdp.getOrigin().getSessionVersion()) {
                        log.debug("parseRemoteSdp() - SDP Origin Session Version decreased. " +
                                "Not acceptable according to RFC 2327 " +
                                "Previous Origin Session Version=" + remotePreviousSdp.getOrigin().getSessionVersion() +
                                " Pending Origin Session Version=" + remotePendingSdp.getOrigin().getSessionVersion());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer equal to the previously received" +
                                " SDP offer can be accepted.");
                    }
                    if (!remotePreviousSdp.getOrigin().getNetworkType().
                            equals(remotePendingSdp.getOrigin().getNetworkType())) {
                        log.debug("parseRemoteSdp() - SDP Origin Network Type doesn't match." +
                                "Previous Origin Network Type=" + remotePreviousSdp.getOrigin().getNetworkType() +
                                " Pending Origin Network Type=" + remotePendingSdp.getOrigin().getNetworkType());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer equal to the previously received" +
                                " SDP offer can be accepted.");
                    }
                    if (!remotePreviousSdp.getOrigin().getAddressType().
                            equals(remotePendingSdp.getOrigin().getAddressType())) {
                        log.debug("parseRemoteSdp() - SDP Origin Address Type doesn't match." +
                                "Previous Origin Address Type=" + remotePreviousSdp.getOrigin().getAddressType() +
                                " Pending Origin Address Type=" + remotePendingSdp.getOrigin().getAddressType());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer equal to the previously received" +
                                " SDP offer can be accepted.");
                    }
                    if (!remotePreviousSdp.getOrigin().getAddress().
                            equals(remotePendingSdp.getOrigin().getAddress())) {
                        log.debug("parseRemoteSdp() - SDP Origin Address doesn't match." +
                                "Previous Origin Address =" + remotePreviousSdp.getOrigin().getAddress() +
                                " Pending Origin Address =" + remotePendingSdp.getOrigin().getAddress());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer equal to the previously received" +
                                " SDP offer can be accepted.");
                    }

                    // Validates the rest of the SDP fields and Attributes.
                    log.debug("parseRemoteSdp() - Current SDPs Rest of the field" + remotePreviousSdp.toString2());
                    log.debug("parseRemoteSdp() - Pending SDPs Rest of the field" + remotePendingSdp.toString2());

                    // In the case of unicast enabled, it is accepted to see a different value, therefore no comparison perform on this attribute 
// DEBUGDEBUG begins
/*
                    boolean skipTransmissionMode = getConfig().isUnicastEnabled();
                    if (!remotePreviousSdp.compareWith(remotePendingSdp, skipTransmissionMode) ) {
                        log.debug("parseRemoteSdp() - SDP Rest of the Fields not matched." +
                                "Previous SDP =" + remotePreviousSdp.toString2() +
                                " Pending SDP =" + remotePendingSdp.toString2());
                        throw new SdpNotSupportedException(SipWarning.RENEGOTIATION_NOT_SUPPORTED, "Only an SDP offer equal to the previously received SDP offer can be accepted.");
                    }
*/
// DEBUGDEBUG ends

                    // Previous implementation kept for history purpose
                    // if (!remotePreviousSdp.toString2().equals(remotePendingSdp.toString2()) ) {
                }
            }
        }
    }

    /**
     * Checks a pending remote SDP to see if it is equal to the original
     * remote SDP.
     * @throws SdpNotSupportedException     if the pending remote SDP is not
     *                                      equal to the original remote SDP.
     */
    public void checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() throws SdpNotSupportedException {

        String previousRemoteSdp = getRemoteSdpString();
        if ((previousRemoteSdp != null) && previousRemoteSdp.length() != 0) {

            SdpSessionDescription remotePreviousSdp =
                    sdpFactory.parseRemoteSdp(previousRemoteSdp);

            // If there already exists a remote sdp, verify that the new SDP
            // only contains allowed "re-negotiations".
            // Currently a re-negotiation is only allowed if it is equivalent
            // to the previosly received SDP offer, therefore equals is used.

            String pendingRemoteSdp = getPendingRemoteSdpString();

            if ((previousRemoteSdp != null) && !pendingRemoteSdp.equals(previousRemoteSdp)) {

                log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp()- PREVIOUS and NEW SDPs are not exactly the SAME");
                log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() - Previous Remote SDP " + previousRemoteSdp);
                log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() - Pending Remote SDP " + pendingRemoteSdp);

                if ((pendingRemoteSdp != null) && pendingRemoteSdp.length() != 0) {

                    SdpSessionDescription remotePendingSdp =
                            sdpFactory.parseRemoteSdp(pendingRemoteSdp);

                    if (!remotePreviousSdp.getOrigin().getUserName().
                            equals(remotePendingSdp.getOrigin().getUserName())) {
                        log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() - SDP Origin Username doesn't match " +
                                "Previous Origin Username =" + remotePreviousSdp.getOrigin().getUserName() +
                                " Pending Origin Username =" + remotePendingSdp.getOrigin().getUserName());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer with User Name equal to the previously received" +
                                " SDP offer can be accepted.");
                    }

                    if (remotePreviousSdp.getOrigin().getSessionId()
                            != remotePendingSdp.getOrigin().getSessionId()) {
                        log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() - SDP Origin SessionID doesn't match " +
                                " Previous Origin Session Id=" + remotePreviousSdp.getOrigin().getSessionId() +
                                " Pending Origin Session Id=" + remotePendingSdp.getOrigin().getSessionId());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer with Session Id equal to the previously received" +
                                " SDP offer can be accepted.");
                    }

                    if (remotePreviousSdp.getOrigin().getSessionVersion()
                            > remotePendingSdp.getOrigin().getSessionVersion()) {
                        log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() - SDP Origin Session Version decreased. " +
                                "Not acceptable according to RFC 2327 " +
                                "Previous Origin Session Version=" + remotePreviousSdp.getOrigin().getSessionVersion() +
                                " Pending Origin Session Version=" + remotePendingSdp.getOrigin().getSessionVersion());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer with Session Version equal to the previously received" +
                                " SDP offer can be accepted.");
                    }
                    if (!remotePreviousSdp.getOrigin().getNetworkType().
                            equals(remotePendingSdp.getOrigin().getNetworkType())) {
                        log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() - SDP Origin Network Type doesn't match." +
                                "Previous Origin Network Type=" + remotePreviousSdp.getOrigin().getNetworkType() +
                                " Pending Origin Network Type=" + remotePendingSdp.getOrigin().getNetworkType());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer with Network Type equal to the previously received" +
                                " SDP offer can be accepted.");
                    }
                    if (!remotePreviousSdp.getOrigin().getAddressType().
                            equals(remotePendingSdp.getOrigin().getAddressType())) {
                        log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() - SDP Origin Address Type doesn't match." +
                                "Previous Origin Address Type=" + remotePreviousSdp.getOrigin().getAddressType() +
                                " Pending Origin Address Type=" + remotePendingSdp.getOrigin().getAddressType());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer with Address Type equal to the previously received" +
                                " SDP offer can be accepted.");
                    }
                    if (!remotePreviousSdp.getOrigin().getAddress().
                            equals(remotePendingSdp.getOrigin().getAddress())) {
                        log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp() - SDP Origin Address doesn't match." +
                                "Previous Origin Address =" + remotePreviousSdp.getOrigin().getAddress() +
                                " Pending Origin Address =" + remotePendingSdp.getOrigin().getAddress());
                        throw new SdpNotSupportedException(
                                SipWarning.RENEGOTIATION_NOT_SUPPORTED,
                                "Only an SDP offer with Address equal to the previously received" +
                                " SDP offer can be accepted.");
                    }

                    log.debug("checkIfPendingRemoteSdpIsMinimallyEqualToOriginalRemoteSdp()- NEW SDP minimal basic parameters are validated the same as the PREVIOUS SDP");

                }
            }
        }
    }

    public SdpIntersection findSdpIntersection(
            CallMediaTypes[] callMediaTypes, boolean clearMediaInSession) throws SdpInternalErrorException{
        if (clearMediaInSession) {
            if(log.isDebugEnabled()){
                log.debug("Clearing " + SELECTED_CALL_MEDIA_TYPES  + " in session.");
            }
            getSession().setData(SELECTED_CALL_MEDIA_TYPES, null);
        }

        SdpSessionDescription remoteSdp = getPendingRemoteSdp();
        if (remoteSdp == null)
            remoteSdp = getRemoteSdp();

        SdpIntersection sdpIntersection =
                SdpMediaComparison.getInstance().getSdpIntersection(
                        remoteSdp,
                        getConfig().getOutboundAudioMimeTypes(),
                        getConfig().getOutboundVideoMimeTypes(),
                        callMediaTypes);

            setSdpIntersection(sdpIntersection);
        if (sdpIntersection != null) {
            storeSelectedCallMediaTypesInSession(sdpIntersection.getCallMediaTypes());
        }

        return sdpIntersection;
    }

    /**
     * Creates an SDP answer based on configured mime types, the SDP intersection
     * and given inbound ConnectionProperties.
     * @param sdpIntersection
     * @param connectionProperties
     * @return The created SDP answer.
     * @throws SdpInternalErrorException
     */
    public String createSdpAnswer(
            SdpIntersection sdpIntersection,
            ConnectionProperties connectionProperties)
            throws SdpInternalErrorException {

        
        
        if (getPendingRemoteSdp() != null) {
            setRemoteSdp(getPendingRemoteSdp(), getPendingRemoteSdpString());
            setPendingRemoteSdp(null, null);
        }

        if (log.isDebugEnabled()) {
            log.debug(" createSdpAnswer InboundAudioMimeType: " +
                    getConfig().getInboundAudioMimeType());
            log.debug(" createSdpAnswer InboundVideoMimeType: " +
                    getConfig().getInboundVideoMimeType());
        }
        
        String sdpAnswer = sdpFactory.createSdpAnswer(
                sdpIntersection,
                getConfig().getInboundAudioMimeType(),
                getConfig().getInboundVideoMimeType(),
                connectionProperties,
                getConfig().getRegisteredName(),
                getPreconditionStatusTable());
        setLocalSdpAnswer(sdpAnswer);
        return sdpAnswer;
    }

    /**
     * Creates an SDP offer based on configured inbound mime types and the
     * given inbound ConnectionProperties.
     */
    public String createSdpOffer(ConnectionProperties connectionProperties)
            throws SdpInternalErrorException {

        if (log.isDebugEnabled()) {
            log.debug(" createSdpOffer InboundAudioMimeType: " +
                    getConfig().getInboundAudioMimeType());
            log.debug(" createSdpOffer InboundVideoMimeType: " +
                    getConfig().getInboundVideoMimeType());
        }

        String sdpOffer = sdpFactory.createSdpOffer(
                getCallType(),
                getConfig().getInboundAudioMimeType(),
                getConfig().getInboundVideoMimeType(),
                connectionProperties,
                getConfig().getRegisteredName());
        setLocalSdpOffer(sdpOffer);
        return sdpOffer;
    }


    //===================== Call methods =======================

    public void play(Object id, IMediaObject mediaObject,
                     PlayOption playOption, long cursor)
            throws IllegalArgumentException {

        if (id == null) {
            throw new IllegalArgumentException("Id may not be null.");
        }
        if (mediaObject == null) {
            throw new IllegalArgumentException("MediaObject may not be null.");
        }
        if (playOption == null) {
            throw new IllegalArgumentException("PlayOption may not be null.");
        }
        if (cursor < 0) {
            throw new IllegalArgumentException("Cursor must be >= 0.");
        }
        if (!mediaObject.isImmutable()) {
            throw new IllegalArgumentException(
                    "Cannot play a mutable MediaObject");
        }

        PlayEvent playEvent = new PlayEvent(id, mediaObject, playOption, cursor);
        queueEvent(playEvent);
    }

    public void play(Object id, IMediaObject mediaObjects[],
                     IOutboundMediaStream.PlayOption playOption, long cursor)
        throws IllegalArgumentException {

        if (id == null) {
            throw new IllegalArgumentException("Id may not be null.");
        }
        if (mediaObjects == null) {
            throw new IllegalArgumentException(
                    "Array of MediaObjects may not be null.");
        }
        for (IMediaObject mediaObject : mediaObjects) {
            if (mediaObject == null) {
                throw new IllegalArgumentException("MediaObject may not be null.");
            }
        }
        if (playOption == null) {
            throw new IllegalArgumentException("PlayOption may not be null.");
        }
        if (cursor < 0) {
            throw new IllegalArgumentException("Cursor must be >= 0.");
        }
        for (IMediaObject mediaObject : mediaObjects) {
            if (!mediaObject.isImmutable()) {
                throw new IllegalArgumentException(
                        "Cannot play a mutable MediaObject");
            }
        }

        PlayEvent playEvent = new PlayEvent(id, mediaObjects, playOption, cursor);
        queueEvent(playEvent);
    }

    public void record(Object id, IMediaObject recordMediaObject,
                       RecordingProperties properties)
            throws IllegalArgumentException {

        validateRecordProperties(id, null, recordMediaObject, properties);
        RecordEvent recordEvent =
                new RecordEvent(id, recordMediaObject, properties);
        queueEvent(recordEvent);
    }

    public void record(Object id, IMediaObject playMediaObject,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties)
            throws IllegalArgumentException {

        validateRecordProperties(id, playMediaObject, recordMediaObject, properties);
        RecordEvent recordEvent = new RecordEvent(
                id, recordMediaObject, properties, playMediaObject);
        queueEvent(recordEvent);
    }

    public void stopPlay(Object id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("Id may not be null.");
        }

        StopPlayEvent stopPlayEvent = new StopPlayEvent(id);
        queueEvent(stopPlayEvent);
    }

    public void stopRecord(Object id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("Id may not be null.");
        }

        StopRecordEvent stopRecordEvent = new StopRecordEvent(id);
        queueEvent(stopRecordEvent);
    }


    private void validateRecordProperties(
            Object id, IMediaObject playMediaObject,
            IMediaObject recordMediaObject,
            RecordingProperties properties)
            throws IllegalArgumentException {

        if (id == null) {
            throw new IllegalArgumentException("Id may not be null.");
        }
        if (recordMediaObject == null) {
            throw new IllegalArgumentException("MediaObject may not be null.");
        }
        if (properties == null) {
            throw new IllegalArgumentException(
                    "RecordingProperties may not be null.");
        }

        if (recordMediaObject.isImmutable()) {
            throw new IllegalArgumentException(
                    "Cannot record to an immutable MediaObject.");
        }
        if (playMediaObject != null) {
            if (!playMediaObject.isImmutable()) {
                throw new IllegalArgumentException(
                        "Cannot play a mutable media object.");
            }
        }
    }

    //======================= VideoFastUpdater Methods ======================

    public void sendPictureFastUpdateRequest() {
        queueEvent(new VideoFastUpdateRequestEvent());
    }



    //======================= CommandExecutor Methods ======================

    /**
     * This method is called when an event in the Calls event queue
     * shall be processed.
     * <p>
     * <ul>
     * <li>
     * SIP requests (except for initial INVITEs) are processed using
     * {@link #processSipRequest(SipRequestEvent)}.
     * </li>
     * <li>
     * SIP responses are processed using
     * {@link CallState#processSipResponse(SipResponseEvent)} on current state.
     * </li>
     * <li>
     * Timeout events from the SIP stack are processed using
     * {@link CallState#processSipTimeout(SipTimeoutEvent)} on current state.
     * </li>
     * <li>
     * Administrator lock requests are handled using
     * {@link CallState#processLockRequest()} on current state.
     * </li>
     * <li>
     * Requests to send a Video Fast Update over SIP is processed using
     * {@link CallState#processVideoFastUpdateRequest()} on current state.
     * </li>
     * <li>
     * Call commands from the system are processed using
     * {@link #processCallCommand(CallCommandEvent)}.
     * </li>
     * Call timeouts are handled using
     * {@link CallState#handleCallTimeout(CallTimeoutEvent)} on the current
     * state.
     * <li>
     * A detected abadoned stream is handled using
     * {@link CallState#handleAbandonedStream()} on current stream.
     * </li>
     * <li>
     * </li>
     * <li>
     * </li>
     * <li>
     * </li>
     * <li>
     * </li>
     * </ul>
     * This method must never throw an exception. Therefore, this method
     * catches all exceptions. If an exception is thrown, it is logged as an
     * error, a FailedEvent is generated and the state is set to disconnected.
     *
     * @param eventObject
     */
    public void doCommand(EventObject eventObject) {
        // Register the session in logger
        setSessionLoggingData();

        if (log.isDebugEnabled()){
            log.debug("DoCommand: " + eventObject);
        }


        Object perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.In.Call.doCommand");
        try {
            if (eventObject instanceof SipRequestEvent) {
                processSipRequest((SipRequestEvent)eventObject);
            } else if (eventObject instanceof SipResponseEvent) {
                getCurrentState().processSipResponse((SipResponseEvent)eventObject);
            } else if (eventObject instanceof SipTimeoutEvent) {
                getCurrentState().processSipTimeout((SipTimeoutEvent)eventObject);
            } else if (eventObject instanceof CloseForcedEvent) {
                getCurrentState().processLockRequest();
            } else if (eventObject instanceof VideoFastUpdateRequestEvent) {
                getCurrentState().processVideoFastUpdateRequest();
            } else if (eventObject instanceof CallCommandEvent) {
                processCallCommand((CallCommandEvent)eventObject);
            } else if (eventObject instanceof CallTimeoutEvent) {
                getCurrentState().
                        handleCallTimeout((CallTimeoutEvent)eventObject);
            } else if (eventObject instanceof AbandonedStreamEvent) {
                getCurrentState().handleAbandonedStream();
            }
        } catch (Throwable e) {
            String errorMsg = "Exception occurred in doCommand. This must " +
                    "never happen! Error in implementation of CallManager. " +
                    "Message: " + e.getMessage();
            log.error(errorMsg, e);
        } finally {
       	 	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
       	 		CommonOamManager.profilerAgent.exitCheckpoint(perf);
       	 	}
        }
    }


    /**
     * Processes a SIP request event by injecting it into the call's state
     * machine.
     *
     * The state machine shall not throw any exceptions when processing SIP
     * requests. No runtime exceptions are therefore handled in this method but
     * passed to the caller (i.e. doCommand).
     *
     * @param sipRequestEvent
     */
    private void processSipRequest(SipRequestEvent sipRequestEvent) {
    	sipRequestEvent.exitCheckPoint();

        String method = sipRequestEvent.getMethod();
    	Object perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.CM.In.Call.processSipRequest."+method);
    	try {


        if (method.equals(Request.INVITE)) {
            if (sipRequestEvent.isRequestInitialInvite()) {
                getCurrentState().processInvite(sipRequestEvent);
            } else {
                getCurrentState().processReInvite(sipRequestEvent);
            }
        } else if (method.equals(Request.ACK)) {
            getCurrentState().processAck(sipRequestEvent);
        } else if (method.equals(Request.BYE)) {
            getCurrentState().processBye(sipRequestEvent);
        } else if (method.equals(Request.CANCEL)) {
            getCurrentState().processCancel(sipRequestEvent);
        } else if (method.equals(Request.OPTIONS)) {
            getCurrentState().processOptions(sipRequestEvent);
        } else if (method.equals(Request.INFO)) {
            getCurrentState().processInfo(sipRequestEvent);
        } else if (method.equals(Request.PRACK)) {
            getCurrentState().processPrack(sipRequestEvent);
        } else if (method.equals(Request.UPDATE)) {
            getCurrentState().processUpdate(sipRequestEvent);
        } else {
            log.error("Unknown SIP request " + method + " received. " +
                    "It should not have come this far. It suggests " +
                    "implementation error in CallManager.");
        }
    	} finally {

    		CommonOamManager.profilerAgent.exitCheckpoint(perf);
    	}
    }

    /**
     * Processes a call command from the system and injects it into the
     * call's state machine.
     *
     * This method is abstract since the available call command differ
     * depending on the type of the call.
     * @param callCommandEvent
     */
    abstract void processCallCommand(CallCommandEvent callCommandEvent);

    /**
     * Processes a play requested by the system by injecting it into the call's
     * state machine.
     * @param playEvent
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    protected void processPlay(PlayEvent playEvent) {
        getCurrentState().play(playEvent);
    }

    /**
     * Processes a record requested by the system by injecting it into the call's
     * state machine.
     * @param recordEvent
     * @throws IllegalStateException is thrown if the event is received in an
     * illegal state.
     */
    protected void processRecord(RecordEvent recordEvent) {
        getCurrentState().record(recordEvent);
    }

    /**
     * Processes a request to stop an ongoing play by injecting it
     * into the call's state machine.
     * @param stopPlayEvent
     */
    protected void processStopPlay(StopPlayEvent stopPlayEvent) {
        getCurrentState().stopPlay(stopPlayEvent);
    }

    /**
     * Processes a request to stop an ongoing record by injecting it
     * into the call's state machine.
     * @param stopRecordEvent
     */
    protected void processStopRecord(StopRecordEvent stopRecordEvent) {
        getCurrentState().stopRecord(stopRecordEvent);
    }


    /**
     * TODO: Phase 2! Document!
     */
    public void timeoutCall(CallTimerTask.Type type) {
        CallTimeoutEvent timeoutEvent = new CallTimeoutEvent(type);
        queueEvent(timeoutEvent);
    }


    public void removeCall() {
        if (log.isDebugEnabled())
            log.debug("Removing call.");

        if (!callIsRemoved.get()) {
            CMUtils.getInstance().getCallDispatcher().removeCall(initialDialogId, establishedDialogId);

            IEventDispatcher eventDispatcher = getEventDispatcher();
            if (eventDispatcher != null) {
                eventDispatcher.removeEventReceiver(this);
            }

            CMUtils.getInstance().getCmController().queueEvent(
                    new RemoveCallEvent(this));
            callIsRemoved.set(true);

            if (log.isDebugEnabled())
                log.debug("Call is removed.");

        } else {
            if (log.isDebugEnabled())
                log.debug("Call was already removed.");
        }
    }

    public abstract void cancelCallTimers();

    public void startNoAckTimer() {
        noAckTimerTask =
            new CallTimerTask(this, CallTimerTask.Type.NO_ACK);
        getCallTimer().schedule(
                noAckTimerTask,
                getConfig().getCallNotAcceptedTimer());

        if (log.isDebugEnabled()) {
            log.debug("No Ack timer has been scheduled to: " +
                    getConfig().getCallNotAcceptedTimer());
        }
    }

    public void cancelNoAckTimer() {
        if (noAckTimerTask != null)
            noAckTimerTask.cancel();
    }

    public void setSession(ISession session) {
        this.session = session;
    }

    //================== CallInternal methods start ====================

    public abstract void errorOccurred(String message, boolean alreadyDisconnected);

    public abstract void setSessionLoggingData();

    // TODO: Phase 2! Document!
    public void sendErrorResponse(int responseType,
                                  SipRequestEvent sipRequestEvent,
                                  String message) {
        try {
            SipResponse sipResponse = CMUtils.getInstance().getSipResponseFactory().
                            createErrorResponse(responseType, sipRequestEvent, message);
            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
        } catch (Exception e) {
            errorOccurred(
                    "Could not send SIP " + responseType + " response for " +
                            sipRequestEvent,
                    true);
            if ( responseType != Response.SERVER_INTERNAL_ERROR ){
                sendErrorResponse(
                    Response.SERVER_INTERNAL_ERROR,
                    sipRequestEvent,
                    e.getMessage());
            }
        }
    }

    // TODO: Phase 2! Document!
    public void sendNotAcceptableHereResponse(
            SipRequestEvent sipRequestEvent, SipWarning warning) {
        try {
            SipResponse sipResponse = CMUtils.getInstance().getSipResponseFactory().
                            createNotAcceptableHereResponse(sipRequestEvent, warning);
            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
        } catch (Exception e) {
            errorOccurred(
                    "Could not send SIP 488 response for " + sipRequestEvent,
                    true);
        }
    }

    // TODO document
    public void sendMethodNotAllowedResponse(
            SipRequestEvent sipRequestEvent) {
        try {
            SipResponse sipResponse =
                    CMUtils.getInstance().getSipResponseFactory().
                            createMethodNotAllowedResponse(sipRequestEvent);
            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);

            if (log.isDebugEnabled()) {
                log.debug("SIP 405 response is sent.");
            }

        } catch (Exception e) {
            errorOccurred("SIP 405 response could not be sent. " +
                    "The call is considered completed. " + e.getMessage(),
                    true);
        }
    }

// TODO: Phase 2! Document that this should not be used for INVITEs
    public void sendOkResponse(SipRequestEvent sipRequestEvent,
                               boolean alreadyDisconnected) {
        try {
            SipResponse sipResponse = CMUtils.getInstance().getSipResponseFactory().
                    createOkResponse(
                            sipRequestEvent, null, getConfig().getRegisteredName());
            CMUtils.getInstance().getSipMessageSender().sendResponse(sipResponse);
        } catch (Exception e) {
            errorOccurred(
                    "Could not send SIP \"Ok\" response for " +
                            sipRequestEvent,
                    alreadyDisconnected);
        }
    }


    /**
     * Returns whether the call is joinable. Whether or not a call is joinable
     * depends upon if both streams have been created, the state that the
     * call is in and if the call already is joined.
     * @return True if the call is joinable and false otherwise.
     */
    public abstract boolean isJoinable();

    /**
     * @return Returns true if call is joined. False otherwise.
     */
    public boolean isCallJoined() {
        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {
            return callIsJoined.get();
        }
    }

    /**
     * @return Returns a call with which this call is joined. Null is returned
     * if this call is not joined.
     */
    public CallToCall getJoinedToCall() {
        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {
            return joinedToCall.get();
        }
    }

    /**
     * This method takes a SipRequestEvent containing a VFU request and
     * forwards it out on this call.
     * Forward is only done if the SipRequestEvent is a VFU request, i.e. if
     * the method is INFO which contains a media control message body.
     * <p>
     * If the VFU request cannot be created or sent,
     * {@link #errorOccurred(String, boolean)} is called and the call is
     * considered completed with an error.
     *
     * @param sipRequestEvent MUST NOT be null
     * @return an identifier for the sent request
     */
    public String forwardVFURequest(SipRequestEvent sipRequestEvent) {
        String tag = null;

        String mediaControlBody = sipRequestEvent.getSipMessage().
                getContent(SipContentSubType.MEDIA_CONTROL);

        // Only forward the request if it really is a VFU request
        if (sipRequestEvent.getMethod().equals(Request.INFO) &&
                (mediaControlBody != null)) {

            try {
                // Copy the original PCV but without term ioi
                PChargingVectorHeader pcv = (PChargingVectorHeader)
                        sipRequestEvent.getRequest().getHeader(
                                PChargingVectorHeader.NAME);
                PChargingVectorHeader newPcv = null;
                if (pcv != null) {
                    newPcv = CMUtils.getInstance().getSipHeaderFactory().
                            createPChargingVectorHeader(pcv.getICID(),
                                    pcv.getICIDGeneratedAt(),
                                    pcv.getOriginatingIOI(), null);
                }

                SipRequest sipRequest =
                        CMUtils.getInstance().getSipRequestFactory().
                                createInfoRequest(getDialog(),
                                        null,
                                        mediaControlBody, newPcv);

                tag = sipRequest.getFromHeaderTag();
                CMUtils.getInstance().getSipMessageSender().sendRequestWithinDialog(
                        getDialog(), sipRequest);

            } catch (Exception e) {
                errorOccurred("Could not send SIP INFO request: " +
                        e.getMessage(), false);
            }
        }
        return tag;
    }

    /**
     * This method checks if there is a pending VFU request that matches the VFU
     * response. If there is, the VFU response is sent on within this call.
     * <p>
     * If the VFU response cannot be created or sent,
     * {@link #errorOccurred(String, boolean)} is called and the call is
     * considered completed with an error.
     *
     * @param sipResponseEvent MUST NOT be null.
     */
    public void forwardVFUResponse(SipResponseEvent sipResponseEvent) {

        // Get the original VFU request from the pending requests.
        SipRequestEvent initialVFURequest = getPendingRequest(
                sipResponseEvent.getSipMessage().getFromHeaderTag());

        // Only forward the response if it matches a pending VFU request
        if (initialVFURequest != null) {
            try {

                SipResponse sipResponse = CMUtils.getInstance().
                        getSipResponseFactory().createForwardedResponse(
                        initialVFURequest, sipResponseEvent);

                // Copy the PCV from reponse
                PChargingVectorHeader pcv = (PChargingVectorHeader)
                        sipResponseEvent.getResponse().getHeader(
                                PChargingVectorHeader.NAME);
                PChargingVectorHeader newPcv = null;
                if (pcv != null) {
                    newPcv = CMUtils.getInstance().getSipHeaderFactory().
                            createPChargingVectorHeader(pcv.getICID(),
                                    pcv.getICIDGeneratedAt(),
                                    pcv.getOriginatingIOI(),
                                    pcv.getTerminatingIOI());
                }

                sipResponse.addPChargingVector(newPcv);

                CMUtils.getInstance().getSipMessageSender().sendResponse(
                        sipResponse);
            } catch (Exception e) {
                errorOccurred("Could not send SIP INFO response: " +
                        e.getMessage(), false);
            }
        }
    }

    /**
     * Add a request to the pending requests.
     * @param id
     * @param sipRequestEvent
     */
    public synchronized void addPendingRequest(
            String id, SipRequestEvent sipRequestEvent) {
        pendingRequests.put(id, sipRequestEvent);
    }

    /**
     * Returns a pending request by its id
     * @param id
     * @return A sip request event or null if no request is found.
     */
    public synchronized SipRequestEvent getPendingRequest(String id) {
        return pendingRequests.remove(id);
    }


    //================== CallInternal methods end ====================

    /**
     * This method joins this call with another call half duplex.
     * This calls inbound stream is joined with the other calls outbound stream.
     *
     * @param anotherCall
     * @throws IllegalStateException if it was not possible to join the calls.
     */
    public void join(CallToCall anotherCall) throws IllegalStateException {
        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {
            if (isJoinable()) {
                try {
                    getInboundStream().join(anotherCall.getOutboundStream());
                    callIsJoined.set(true);
                    joinedToCall.set(anotherCall);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            } else {
                throw new IllegalStateException(
                        "Calls could not be joined. This call is in " +
                                getCurrentState());
            }
        }
    }

    /**
     * This method unjoins this call from another call half duplex.
     * This calls inbound stream is unjoined from the other calls outbound
     * stream.
     * @param anotherCall
     * @throws IllegalStateException if it was not possible to unjoin the calls.
     */
    public void unjoin(CallToCall anotherCall) throws IllegalStateException {
        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {
            if (!(callIsJoined.get()) ||
                    !(joinedToCall.get().equals(anotherCall))) {
                throw new IllegalStateException(
                        "Calls could not be unjoined. Call Is Joined = " +
                                callIsJoined.get() + ", Expected other call = " +
                                joinedToCall.get() + ", Actual other call = " +
                                anotherCall);
            } else {
                unjoinStreams();
            }
        }
    }

    /**
     * This method unjoins this call half duplex from the call it is joined to.
     * This method must only be called if the call is already joined.
     * @throws IllegalStateException if it was not possible to unjoin the streams.
     */
    private void unjoinStreams() {
        // NOTE: The streamLock should be used restrictevly as described at the
        // top of this class.
        synchronized(streamLock) {
            CallToCall otherCall = joinedToCall.get();
            callIsJoined.set(false);
            joinedToCall.set(null);
            try {
                getInboundStream().unjoin(otherCall.getOutboundStream());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
      * Returns an unmodifiable view of the far end connections set
      * @return an unmodifiable view of the far end connections set
      */
     public Set<Connection> getFarEndConnections() {
         return Collections.unmodifiableSet(farEndConnections);
     }

    public int getInboundBitRate() {
        if(inboundStream == null)
            return 0;
        else
            return inboundStream.getInboundBitRate();
    }
    
    // THE METHODS BELOW ARE USED FOR BASIC TESTING ONLY
    public void setInboundStream(IInboundMediaStream stream) {
        inboundStream = stream;
    }

    public void setOutboundStream(IOutboundMediaStream stream) {
        outboundStream = stream;
    }
    private void getLicense(CallType callType) throws CallManagerLicensingException
    {
        if(callType.equals(CallType.VOICE))
        {
            CMUtils.getInstance().getCallManagerLicensing().addOneVoiceCall();
        }
        else if(callType.equals(CallType.VIDEO))
        {
            CMUtils.getInstance().getCallManagerLicensing().addOneVideoCall();
        }
        licenseInUse = callType;


    }
    private void releaseLicense()
    {


        if(licenseInUse.equals(CallType.VOICE))
        {
            CMUtils.getInstance().getCallManagerLicensing().removeOneVoiceCall();
        }
        else if(licenseInUse.equals(CallType.VIDEO))
        {
            CMUtils.getInstance().getCallManagerLicensing().removeOneVideoCall();
        }
        licenseInUse=CallType.UNKNOWN;
    }
    
    /////////////////////////// KPI ////////////////////////////////////////////////////// 
    protected Object sipInviteRingAckKpiCpo = null; // SIP Invite-Ring-Ack KPI Check Point Object
    public void enterSipInviteRingAckKpiCheckpoint() {
    	if (KPIProfiler.isStatsEnabled()) {
    		sipInviteRingAckKpiCpo = KPIProfiler.enterCheckpoint(KPIProfiler.KPI_NAME_SIP_OUTDIAL_INVITE_TO_RINGACK,
    				KPIProfiler.KPI_DISPLAY_SIP_OUTDIAL_INVITE_TO_RINGACK);
    	}
    }    
    public void exitSipInviteRingAckKpiCheckpoint() {
    	if (sipInviteRingAckKpiCpo != null) {
    		KPIProfiler.exitCheckpoint(sipInviteRingAckKpiCpo);
    		sipInviteRingAckKpiCpo = null;
    	}
    }

}
