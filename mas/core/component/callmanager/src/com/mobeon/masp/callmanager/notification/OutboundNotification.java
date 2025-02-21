/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager.notification;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.queuehandling.SequenceGuaranteedEventQueue;
import com.mobeon.masp.callmanager.queuehandling.CommandExecutor;
import com.mobeon.masp.callmanager.events.EventObject;
import com.mobeon.masp.callmanager.events.SipMessageResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipErrorResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipOkResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipProvisionalResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.SipConstants;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.notification.events.NotifyEvent;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.CallManagerConfiguration;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.util.NamedValue;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;

import javax.sip.SipException;
import javax.sip.address.URI;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import java.util.Map;
import java.util.Collection;
import java.util.LinkedList;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import gov.nist.javax.sip.header.ims.PChargingVectorHeader;
import gov.nist.javax.sip.parser.URLParser;


/**
 * Copyright 2007 Mobeon AB
 * Date: 2007-mar-09
 *
 * Implements an OutboundNotification. Each instance of an OutboundNotification
 * has an event queue that will handle incoming events such as NotifyEvent's,
 * SIP responses and SIP timeouts.
 *
 * @author Mats Hägg
 */
public class OutboundNotification implements CommandExecutor {

    private final static ILogger log = ILoggerFactory.getILogger(OutboundNotification.class);

    private final static int INTERNAL_ERROR_RESPONSE_CODE = 500;
    private final static long MAX_COUNTER_VALUE = 4294967295L; // 2^32-1

    private SequenceGuaranteedEventQueue eventQueue =
            new SequenceGuaranteedEventQueue(this, OutboundNotification.class);

    private final IEventDispatcher eventDispatcher;
    private final ISession session;
    private SipRequest ongoingSipNotifyRequest = null;
    private CallIdHeader callIdHeader = null;
    private int cSeq = 1;

    private String msgBody = null;
    private String sendTo = null;
    private String outboundCallServerHost;
    private int outboundCallServerPort;
    private String strOutboundCallServerPort;
    private boolean outboundCallServerPortWasSet = false;
    private boolean isSolicited = false;
    private String dialogInfoFile;
    private String mailboxId = null;

    public OutboundNotification(String method, Map<String,String> params,
                                IEventDispatcher eventDispatcher,
                                ISession session)
            throws IllegalArgumentException {


        if (params == null)
            throw new IllegalArgumentException("params must not be null");

        if (eventDispatcher == null)
            throw new IllegalArgumentException("event dispatcher must not be null");

        this.eventDispatcher = eventDispatcher;
        this.session = session;
        if (CallManager.METHOD_MWI.equals(method)) {
            msgBody = createMwiBody(params);
            sendTo = params.get(CallManager.SEND_TO);

            outboundCallServerHost = params.get(CallManager.OUTBOUND_CALL_SERVER_HOST);
            String port = params.get(CallManager.OUTBOUND_CALL_SERVER_PORT);
            if(port != null){
                outboundCallServerPort = (int) Float.parseFloat(port);
                strOutboundCallServerPort = port;
                outboundCallServerPortWasSet = true;
            }
        } else {
            throw new IllegalArgumentException("Unknown notification method: " +
                    method);
        }
        String isSolicitedS = params.get(CallManager.IS_SOLICITED);
        if (isSolicitedS!= null && isSolicitedS.equalsIgnoreCase("yes")) {
        	this.isSolicited = true;
        	mailboxId = params.get(CallManager.MESSAGE_ACCOUNT);
        	this.dialogInfoFile = getSubscriptionFileName(sendTo);
        	if (log.isDebugEnabled()) log.debug("Dialog info file is: " + this.dialogInfoFile);
        }
    }

    public static String getSubscriptionFileName(String userAgent) throws IllegalArgumentException {
        userAgent = CommonMessagingAccess.getInstance().denormalizeNumber(userAgent);      
        String fileName = null;
        try {
            String encodedUA = java.net.URLEncoder.encode(userAgent,"UTF-8");
            fileName = encodedUA + ".subscribe";
        } catch (UnsupportedEncodingException e) {
            //It should never happen
            log.error("getSubscriptionFileName: Encoding error occured for " + userAgent + ": " + e.getMessage(), e);
            throw new IllegalArgumentException("Encoding error occured for " + userAgent + ": " + e.getMessage());
        }
        return fileName;
    }

    public static String getAutodeletionFileName(String userAgent) throws IllegalArgumentException {
        userAgent = CommonMessagingAccess.getInstance().denormalizeNumber(userAgent);      
        String fileName = null;
        try {
            String encodedUA = java.net.URLEncoder.encode(userAgent,"UTF-8");
            fileName = encodedUA + ".autodeletion";
        } catch (UnsupportedEncodingException e) {
            //It should never happen
            log.error("getSubscriptionFileName: Encoding error occured for " + userAgent + ": " + e.getMessage(), e);
            throw new IllegalArgumentException("Encoding error occured for " + userAgent + ": " + e.getMessage());
        }
        return fileName;
    }

    /**
     * Get a new CallId header
     * @return CallIdHeader
     */
    private CallIdHeader getCallIdHeader() {
        if (callIdHeader == null) {
            callIdHeader =
                    CMUtils.getInstance().getSipStackWrapper().getNewCallId();
        }
        return callIdHeader;
    }

    /**
     * Queues an event in the notification event queue. Each event is handled
     * one at a time in the order they arrived.
     * @param event
     */
    public void queueEvent(EventObject event) {
        eventQueue.queue(event);
    }


    /**
     * Queue a new NotifyEvent in the notification event queue.
     * This will trigger the sending of a NOTIFY request.
     */
    public void doNotify() {
        NotifyEvent notifyEvent = new NotifyEvent();
        queueEvent(notifyEvent);
    }



    /**
     * Create a body for sending a Message Waiting indicator using
     * SIP NOTIFY according to RFC 3842.
     * Takes a map of parameter/values and fills a body structure accordingly.
     * The following parameters are supported:
     * <ul>
     *   <li>{@link CallManager.MESSAGE_ACCOUNT} - The mailbox id as a SIP URI.
     *     If unset, the header will not be included in the body.
     *   <li>{@link CallManager.MESSAGES_WAITING} - Could be either "yes" or "no".
     *     "yes" indicates that the there are new messages waiting, "no" means
     *     that there is not.
     *     If left unset, the value will be deduced from the new message counters.
     *     If any of the "xxx-message-new" parameters are greater than zero it will
     *     be set to "yes" otherwise it will be set to "no". If neither the
     *     "messages-waiting" parameter nor any of the counter parameters are set,
     *     the default will be "no".
     *   <li>{@link CallManager.VOICE_MESSAGE_NEW} - Number of new voice messages.
     *   <li>{@link CallManager.VOICE_MESSAGE_OLD} - Number of old voice messages.
     *   <li>{@link CallManager.FAX_MESSAGE_NEW} - Number of new fax messages.
     *   <li>{@link CallManager.FAX_MESSAGE_OLD} - Number of old fax messages.
     *   <li>{@link CallManager.VIDEO_MESSAGE_NEW} - Number of new video messages.
     *   <li>{@link CallManager.VIDEO_MESSAGE_OLD} - Number of old video messages.
     *   <li>{@link CallManager.EMAIL_MESSAGE_NEW} - Number of new email messages.
     *   <li>{@link CallManager.EMAIL_MESSAGE_OLD} - Number of old email messages.
     * </ul>
     * The message counter have a valid range between 0 and 2^32-1.
     * Unknown paramaters will be ignored.
     * Only the message type corresponding to a provided parameter will be included
     * in the body. E.g. If neither fax-message-new nor fax-message-old is set, the
     * corresponding header will not be included in the body.
     * If only one of the counters for a specific message type is given, a default value
     * of 0 (zero) will be assumed for the other one.
     *
     * @param parameterMap - Name/value pairs for the paramaters.
     * @return the created body or null if any of the input parameters is invalid.
     */
    public String createMwiBody(Map<String, String> parameterMap) {

        if (parameterMap == null) {
            log.warn("createMwiBody got a null parametermap");
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating an MWI body with the following parameters: " + parameterMap);
        }

        boolean newMessages = false;

        try {

            // VOICE
            String voiceNew = parameterMap.get(CallManager.VOICE_MESSAGE_NEW);
            String voiceOld = parameterMap.get(CallManager.VOICE_MESSAGE_OLD);
            String voiceSummary = "";
            if ( voiceNew != null || voiceOld != null) {



                long voiceNewCnt = (voiceNew == null) ? 0 : Long.parseLong(voiceNew);
                long voiceOldCnt = (voiceOld == null) ? 0 : Long.parseLong(voiceOld);
                if (    voiceNewCnt >= 0 && voiceNewCnt <= MAX_COUNTER_VALUE &&
                        voiceOldCnt >= 0 && voiceOldCnt <= MAX_COUNTER_VALUE) {
                    String voiceUrgentNew = parameterMap.get(CallManager.VOICE_MESSAGE_URGENT_NEW);
                    String voiceUrgentOld = parameterMap.get(CallManager.VOICE_MESSAGE_URGENT_OLD);
                    long voiceUrgentNewCnt = (voiceUrgentNew == null) ? 0 : Long.parseLong(voiceUrgentNew);
                    long voiceUrgentOldCnt = (voiceUrgentOld == null) ? 0 : Long.parseLong(voiceUrgentOld);

                    if (    voiceUrgentNewCnt >= 0 && voiceUrgentNewCnt <= MAX_COUNTER_VALUE &&
                            voiceUrgentOldCnt >= 0 && voiceUrgentOldCnt <= MAX_COUNTER_VALUE) {
                        voiceSummary = "Voice-Message: " + voiceNewCnt + "/" + voiceOldCnt + " ("+voiceUrgentNewCnt+"/"+voiceUrgentOldCnt+")\r\n";
                    }
                    else
                    {
                        voiceSummary = "Voice-Message: " + voiceNewCnt + "/" + voiceOldCnt + "\r\n";
                    }

                    if (voiceNewCnt > 0) {
                        newMessages = true;
                    }

                } else {

                    log.warn("Parameter out of range " +
                            CallManager.VOICE_MESSAGE_NEW + "=" + voiceNewCnt + " " +
                            CallManager.VOICE_MESSAGE_OLD + "=" + voiceOldCnt);
                    return null;

                }

            }


            // FAX
            String faxSummary = "";
            String faxNew = parameterMap.get(CallManager.FAX_MESSAGE_NEW);
            String faxOld = parameterMap.get(CallManager.FAX_MESSAGE_OLD);
            if ( faxNew != null || faxOld != null) {

                long faxNewCnt = (faxNew == null) ? 0 : Long.parseLong(faxNew);
                long faxOldCnt = (faxOld == null) ? 0 : Long.parseLong(faxOld);

                if (    faxNewCnt >= 0 && faxNewCnt <= MAX_COUNTER_VALUE &&
                        faxOldCnt >= 0 && faxOldCnt <= MAX_COUNTER_VALUE) {
                    String faxUrgentNew = parameterMap.get(CallManager.FAX_MESSAGE_URGENT_NEW);
                    String faxUrgentOld = parameterMap.get(CallManager.FAX_MESSAGE_URGENT_OLD);
                    long faxUrgentNewCnt = (faxUrgentNew == null) ? 0 : Long.parseLong(faxUrgentNew);
                    long faxUrgentOldCnt = (faxUrgentOld == null) ? 0 : Long.parseLong(faxUrgentOld);

                    if (    faxUrgentNewCnt >= 0 && faxUrgentNewCnt <= MAX_COUNTER_VALUE &&
                            faxUrgentOldCnt >= 0 && faxUrgentOldCnt <= MAX_COUNTER_VALUE) {
                    	faxSummary = "Fax-Message: " + faxNewCnt + "/" + faxOldCnt + " ("+faxUrgentNewCnt+"/"+faxUrgentOldCnt+")\r\n";
                    }
                    else
                    {
                        faxSummary = "Fax-Message: " + faxNewCnt + "/" + faxOldCnt + "\r\n";
                    }

                    if (faxNewCnt > 0) {
                        newMessages = true;
                    }

                } else {

                    log.warn("Parameter out of range " +
                            CallManager.FAX_MESSAGE_NEW + "=" + faxNewCnt + " " +
                            CallManager.FAX_MESSAGE_OLD + "=" + faxOldCnt);
                    return null;

                }

            }



            // VIDEO
            String multimediaSummary = "";
            String videoNew = parameterMap.get(CallManager.VIDEO_MESSAGE_NEW);
            String videoOld = parameterMap.get(CallManager.VIDEO_MESSAGE_OLD);
            if ( videoNew != null || videoOld != null) {

                long videoNewCnt = (videoNew == null) ? 0 : Long.parseLong(videoNew);
                long videoOldCnt = (videoOld == null) ? 0 : Long.parseLong(videoOld);

                if (    videoNewCnt >= 0 && videoNewCnt <= MAX_COUNTER_VALUE &&
                        videoOldCnt >= 0 && videoOldCnt <= MAX_COUNTER_VALUE) {
                    String videoUrgentNew = parameterMap.get(CallManager.VIDEO_MESSAGE_URGENT_NEW);
                    String videoUrgentOld = parameterMap.get(CallManager.VIDEO_MESSAGE_URGENT_OLD);
                    long videoUrgentNewCnt = (videoUrgentNew == null) ? 0 : Long.parseLong(videoUrgentNew);
                    long videoUrgentOldCnt = (videoUrgentOld == null) ? 0 : Long.parseLong(videoUrgentOld);

                    if (    videoUrgentNewCnt >= 0 && videoUrgentNewCnt <= MAX_COUNTER_VALUE &&
                            videoUrgentOldCnt >= 0 && videoUrgentOldCnt <= MAX_COUNTER_VALUE) {
                    	multimediaSummary = "Multimedia-Message: " + videoNewCnt + "/" + videoOldCnt + " ("+videoUrgentNewCnt+"/"+videoUrgentOldCnt+")\r\n";
                    }
                    else
                    {
                    	multimediaSummary = "Multimedia-Message: " + videoNewCnt + "/" + videoOldCnt + "\r\n";
                    }
                    if (videoNewCnt > 0) {
                        newMessages = true;
                    }

                } else {

                    log.warn("Parameter out of range " +
                            CallManager.VIDEO_MESSAGE_NEW + "=" + videoNewCnt + " " +
                            CallManager.VIDEO_MESSAGE_OLD + "=" + videoOldCnt);
                    return null;

                }

            }



            // EMAIL
            String textSummary = "";
            String emailNew = parameterMap.get(CallManager.EMAIL_MESSAGE_NEW);
            String emailOld = parameterMap.get(CallManager.EMAIL_MESSAGE_OLD);
            if ( emailNew != null || emailOld != null) {

                long emailNewCnt = (emailNew == null) ? 0 : Long.parseLong(emailNew);
                long emailOldCnt = (emailOld == null) ? 0 : Long.parseLong(emailOld);

                if (    emailNewCnt >= 0 && emailNewCnt <= MAX_COUNTER_VALUE &&
                        emailOldCnt >= 0 && emailOldCnt <= MAX_COUNTER_VALUE) {
                    String emailUrgentNew = parameterMap.get(CallManager.EMAIL_MESSAGE_URGENT_NEW);
                    String emailUrgentOld = parameterMap.get(CallManager.EMAIL_MESSAGE_URGENT_OLD);
                    long emailUrgentNewCnt = (emailUrgentNew == null) ? 0 : Long.parseLong(emailUrgentNew);
                    long emailUrgentOldCnt = (emailUrgentOld == null) ? 0 : Long.parseLong(emailUrgentOld);

                    if (    emailUrgentNewCnt >= 0 && emailUrgentNewCnt <= MAX_COUNTER_VALUE &&
                    		emailUrgentOldCnt >= 0 && emailUrgentOldCnt <= MAX_COUNTER_VALUE) {
                    	textSummary = "Text-Message: " + emailNewCnt + "/" + emailOldCnt + " ("+emailUrgentNewCnt+"/"+emailUrgentOldCnt+")\r\n";
                    }
                    else
                    {
                    	textSummary = "Text-Message: " + emailNewCnt + "/" + emailOldCnt + "\r\n";
                    }
                    if (emailNewCnt > 0) {
                        newMessages = true;
                    }

                } else {

                    log.warn("Parameter out of range " +
                            CallManager.EMAIL_MESSAGE_NEW + "=" + emailNewCnt + " " +
                            CallManager.EMAIL_MESSAGE_OLD + "=" + emailOldCnt);
                    return null;

                }

            }

            // If "messages-waiting" is given as a parameter the given status ("yes" or "no")
            // will be set in the body. If not given as a parameter, it will be set according to
            // the message counters for new messages.
            String messagesWaiting = parameterMap.get(CallManager.MESSAGES_WAITING);
            if (messagesWaiting == null) {
                if (newMessages) {
                    messagesWaiting = "Messages-Waiting: yes\r\n";
                } else {
                    messagesWaiting = "Messages-Waiting: no\r\n";
                }
            } else {
                if (messagesWaiting.equals("yes")) {
                    messagesWaiting = "Messages-Waiting: yes\r\n";
                } else if (messagesWaiting.equals("no")) {
                    messagesWaiting = "Messages-Waiting: no\r\n";
                } else {
                    log.warn("messageswaiting parameter must be either yes or no (if it is present)");
                    return null;
                }
            }


            // Set Message-Account if present
            String messageAccount = parameterMap.get(CallManager.MESSAGE_ACCOUNT);
            if ( messageAccount != null) {
                try {
                    URI accountUri = createMessageAccountUri(messageAccount);
                    messageAccount = "Message-Account: " + accountUri + "\r\n";
                } catch (ParseException e) {
                    log.warn("Cannot create URI for Message-Account from " +
                            messageAccount);
                }

            } else {
                messageAccount = "";
            }

            StringBuffer body = new StringBuffer();
            body.append(messagesWaiting).
                    append(messageAccount).
                    append(voiceSummary).
                    append(faxSummary).
                    append(multimediaSummary).
                    append(textSummary);

            if (log.isDebugEnabled())
                log.debug("MWI body created: \n" + body);

            return body.toString();


        } catch (NumberFormatException e) {
            String msg = "Number format exception in a parameter value";
            log.warn(msg, e);
            return null;
        }

    }

    /**
     * This method is called when an event in the Notification event queue
     * shall be processed.
     * <p>
     * <ul>
     * <li>{@link com.mobeon.masp.callmanager.notification.events.NotifyEvent}
     * is injected using {@link OutboundNotification#doNotify()}
     * </li>
     * <li>{@link com.mobeon.masp.callmanager.sip.events.SipErrorResponseEvent}
     * is processed using {@link com.mobeon.masp.callmanager.notification.OutboundNotification#processSipErrorResponse(SipErrorResponseEvent)}
     * </li>
     * <li>{@link com.mobeon.masp.callmanager.sip.events.SipOkResponseEvent}
     * is processed using {@link com.mobeon.masp.callmanager.notification.OutboundNotification#processSipOkResponse(SipOkResponseEvent)}
     * </li>
     * <li>{@link com.mobeon.masp.callmanager.sip.events.SipTimeoutEvent}
     * is processed using {@link com.mobeon.masp.callmanager.notification.OutboundNotification#processSipTimeout()}
     * </li>
     * </ul>
     * <p>
     * This method must never throw an exception. Therefore, this method
     * catches all exceptions. If an exception is thrown, it is logged as an
     * error.
     *
     * @param eventObject
     */
    public synchronized void doCommand(EventObject eventObject) {

        setSessionLoggingData();

        if (log.isDebugEnabled())
            log.debug("DoCommand: " + eventObject);

        try {
            if (eventObject instanceof NotifyEvent) {
                sendNotify();

            } else if (eventObject instanceof SipErrorResponseEvent) {
                processSipErrorResponse((SipErrorResponseEvent)eventObject);

            } else if (eventObject instanceof SipOkResponseEvent) {
                processSipOkResponse((SipOkResponseEvent)eventObject);

            } else if (eventObject instanceof SipProvisionalResponseEvent) {
                if (log.isDebugEnabled())
                    log.debug("Provisional response is received for SIP " +
                            "NOTIFY request. It is ignored.");

            } else if (eventObject instanceof SipTimeoutEvent) {
                processSipTimeout();
            } else {
                if (log.isDebugEnabled())
                    log.debug("Unknown EventObject ignored in doCommand().");
            }


        } catch (Throwable e) {
            String errorMsg = "Exception occurred in doCommand. This must " +
                    "never happen! Error in implementation of CallManager.";
            log.error(errorMsg, e);
        }
    }

    private void sendNotify() {
    	if (outboundCallServerPortWasSet &&
    			(outboundCallServerPort < 0 || outboundCallServerPort > SipConstants.MAX_PORT)) {
    		internalError("Port for SIP NOTIFY is out of range, is: "+strOutboundCallServerPort);
    		return;
    	}
    	try {
    		if (this.isSolicited) {
    			//TODO - fix the PChargingVector - check rfc if it must be reused from subscribe
    			// Use a new PCharging for now
    			log.debug("Creating solicited Notify");
    			PChargingVectorHeader pChargingVector = CMUtils.getInstance().
    			getSipHeaderFactory().createPChargingVectorHeader(false);
    			ongoingSipNotifyRequest = CMUtils.getInstance().
    			getSipRequestFactory().createNotifyRequest(mailboxId, this.dialogInfoFile,
    					msgBody, pChargingVector);
    		} else {
    			URI toUri;
    			String host;
    			int port;
    			//RouteList routeList = null;
    			if(outboundCallServerHost != null) {
    				host = outboundCallServerHost;
    				port = getOutboundCallServerPort();
    			} else {
    				RemotePartyAddress remotePartyAddress = CMUtils.getInstance().
    				getRemotePartyController().getRandomRemotePartyAddress();
    				if (remotePartyAddress == null) {
    					String errMsg = "No remotePartyAddress found. Can't send NOTIFY request";
    					log.warn(errMsg);
    					sendErrorEvent(INTERNAL_ERROR_RESPONSE_CODE, errMsg);
    					return;
    				}
    				host = remotePartyAddress.getHost();
    				port = remotePartyAddress.getPort();
    			}
    			if (sendTo.toLowerCase().contains("sip:")) {
    				toUri = (SipURI) (new URLParser(sendTo).parse());
    				/*
    		        // TODO - there seem to be no need for Route as the code that follows sets the
    		        // request uri to the IP of the outbound server
    		        // keep it commented out for a while in case there are issues when integrating with the real network
    				routeList = new RouteList();
    				SipUri addressUri = new SipUri();
    				addressUri.setHost(host);
    				addressUri.setPort(port);
    				Address address = SipFactory.getInstance().createAddressFactory().createAddress(addressUri);
    				Route route = new Route();
    				route.setAddress(address);
    				routeList.add(route);
    				*/
    			} else {
    				toUri = createSendToUri(sendTo, host, port);
    			}
    			URI fromUri = createFromUri();
    			log.debug("Creating un-solicited Notify");
    			// Create a new P-Charging-Vector
    			PChargingVectorHeader pChargingVector = CMUtils.getInstance().
    			getSipHeaderFactory().createPChargingVectorHeader(false);
    			ongoingSipNotifyRequest = CMUtils.getInstance().
    			getSipRequestFactory().createNotifyRequest(
    					SipConstants.MWI_EVENT_TYPE, toUri, fromUri,
    					cSeq++, getCallIdHeader(), msgBody, pChargingVector);
    			/*
    			if (routeList != null) {
    				// add the required route to the request
    				ongoingSipNotifyRequest.getRequest().addFirst(routeList);
    			}
    			*/
    		}
    		// Add the notification to be sent
    		CMUtils.getInstance().getNotificationDispatcher().addOngoingNotification(
    				this, ongoingSipNotifyRequest);

    	} catch (Exception e) {
    		String errMsg = "Exception occurred: " + e.getMessage();
    		log.warn(errMsg);
    		sendErrorEvent(INTERNAL_ERROR_RESPONSE_CODE, errMsg);
    		return;
    	}

    	try {

    		CMUtils.getInstance().getSipMessageSender().
    		sendRequest(ongoingSipNotifyRequest);

    	} catch (SipException e) {
    		String errMsg = "SIP exception occurred: " + e.getMessage();
    		log.warn(errMsg);
    		sendErrorEvent(INTERNAL_ERROR_RESPONSE_CODE, errMsg);
    		CMUtils.getInstance().getNotificationDispatcher().
    		removeOngoingNotification(this, ongoingSipNotifyRequest);
    	}

    }

    private void internalError(String errMsg) {
        log.warn(errMsg);
        sendErrorEvent(INTERNAL_ERROR_RESPONSE_CODE, errMsg);
    }

    private int getOutboundCallServerPort() {
       CallManagerConfiguration config =
               ConfigurationReader.getInstance().getConfig();
        if(outboundCallServerPortWasSet)
            return outboundCallServerPort;
        else
            return config.getOutboundCallServerPort();
   }



    //====== Package private methods introduced to simplify basic test  ========

    void processSipErrorResponse(SipErrorResponseEvent event) {

        CMUtils.getInstance().getNotificationDispatcher().removeOngoingNotification(
                this, ongoingSipNotifyRequest);

        SipMessageResponseEvent responseEvent = createSipMessageResponseEvent(
                event.getResponseCode(), event.getReasonPhrase(), event.getRetryAfter());

        if (log.isDebugEnabled()) {
            log.debug("Sending a " + responseEvent.toString());
        }

        eventDispatcher.fireEvent(responseEvent);

    }

    void processSipOkResponse(SipOkResponseEvent event) {
        CMUtils.getInstance().getNotificationDispatcher().removeOngoingNotification(
                this, ongoingSipNotifyRequest);

        SipMessageResponseEvent responseEvent = createSipMessageResponseEvent(
                event.getResponseCode(), event.getReasonPhrase(), null);

        if (log.isDebugEnabled()) {
            log.debug("Sending a " + responseEvent.toString());
        }

        eventDispatcher.fireEvent(responseEvent);

    }

    void processSipTimeout() {
        CMUtils.getInstance().getNotificationDispatcher().removeOngoingNotification(
                this, ongoingSipNotifyRequest);

        SipMessageResponseEvent responseEvent = createSipMessageResponseEvent(
                408, "Request timeout", null);

        if (log.isDebugEnabled()) {
            log.debug("Sending a " + responseEvent.toString());
        }

        eventDispatcher.fireEvent(responseEvent);

    }

    void sendErrorEvent(int statusCode, String statusText) {

        SipMessageResponseEvent responseEvent = createSipMessageResponseEvent(
                statusCode, statusText, null);

        if (log.isDebugEnabled()) {
            log.debug("Sending a " + responseEvent.toString());
        }

        eventDispatcher.fireEvent(responseEvent);

    }

    /**
     * Create a new SipMessageResponseEvent with parameters for
     * <ul>
     *  <li>responsecode (SIP response code)
     *  <li>responsetext (Text corresponing to the SIP response code)
     *  <li>retryafter (if retry after is given in the SIP response)
     * </ul>
     * @param responseCode - Valid value are between 100 and 999 inclusive.
     * @param responseText - A String or null.
     * @param retryAfter - A positive integer or null. (milliseconds)
     * @return a SipMessageResponseEvent
     * @throws IllegalArgumentException if response code is out of range
     */
    SipMessageResponseEvent createSipMessageResponseEvent(int responseCode,
                                                          String responseText,
                                                          Integer retryAfter)
            throws IllegalArgumentException {

        if (responseCode < 100 || responseCode > 999) {
            throw new IllegalArgumentException("Response code is out of range: " + responseCode);
        }

        Collection<NamedValue<String,String>> params = new LinkedList<NamedValue<String,String>>();
        params.add(new NamedValue<String,String>(SipMessageResponseEvent.RESPONSE_CODE,
                Integer.toString(responseCode)));

        if (responseText != null) {
            params.add(new NamedValue<String,String>(SipMessageResponseEvent.RESPONSE_TEXT,
                    responseText));
        }

        if (retryAfter != null) {
            params.add(new NamedValue<String,String>(SipMessageResponseEvent.RETRY_AFTER,
                    Integer.toString(retryAfter)));
        }

        return new SipMessageResponseEvent(params);

    }


// *************** Helper methods ******************

    private URI createMessageAccountUri(String messageAccount)
            throws ParseException {

        try {
            URI uri = CMUtils.getInstance().getSipHeaderFactory().createUri(
                    messageAccount,
                    CMUtils.getInstance().getLocalHost(),
                    CMUtils.getInstance().getLocalPort());

            ((SipURI)uri).setParameter("user", "phone");
            return uri;

        } catch (ParseException e) {
            log.warn("Failed to create URI for message account " + messageAccount);
            throw e;
        }

    }

    private URI createSendToUri(String sendTo,
                                String host,
                                int port)
            throws ParseException {

        try {
            URI uri = CMUtils.getInstance().getSipHeaderFactory().createUri(
                    sendTo,
                    host,
                    port);

            ((SipURI)uri).setParameter("user", "phone");
            return uri;

        } catch (ParseException e) {
            log.warn("Failed to create URI for sendto " + sendTo);
            throw e;
        }

    }

    private URI createFromUri() throws ParseException {
        try {
            return CMUtils.getInstance().getSipHeaderFactory().createUri(
                    ConfigurationReader.getInstance().getConfig().getRegisteredName(),
                    CMUtils.getInstance().getLocalHost(),
                    CMUtils.getInstance().getLocalPort());
        } catch (ParseException e) {
            log.warn("Failed to create from URI");
            throw e;
        }
    }


    /**
     * Registers the session in the logger.
     * If there is no session it registers the session ID in the logger and
     * also sets calling, called and redirecting party in the
     * <param>mdcItems</param> log data and registers the
     * <param>mdcItems</param> in logger.
     */
    private void setSessionLoggingData() {
        if (session != null) {
            session.registerSessionInLogger();
        }
    }



}
