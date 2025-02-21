/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.callmanager;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.util.NamedValue;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.operateandmaintainmanager.Supervision;

import java.util.Collection;

/**
 * Provides call management functionality not related to an active call.
 *
 * @author Malin Flodin
 */
public interface CallManager {

    public final String METHOD_MWI = "messagewaiting";
    public final String SEND_TO = "sendto";
    public final String MESSAGE_ACCOUNT = "messageaccount";
    public final String MESSAGES_WAITING = "messageswaiting";
    public final String VOICE_MESSAGE_NEW = "voicemessagenew";
    public final String VOICE_MESSAGE_OLD = "voicemessageold";
    public final String FAX_MESSAGE_NEW = "faxmessagenew";
    public final String FAX_MESSAGE_OLD = "faxmessageold";
    public final String VIDEO_MESSAGE_NEW = "videomessagenew";
    public final String VIDEO_MESSAGE_OLD = "videomessageold";
    public final String EMAIL_MESSAGE_NEW = "emailmessagenew";
    public final String EMAIL_MESSAGE_OLD = "emailmessageold";
    public final String VOICE_MESSAGE_URGENT_NEW = "voicemessageurgentnew";
    public final String VOICE_MESSAGE_URGENT_OLD = "voicemessageurgentold";
    public final String FAX_MESSAGE_URGENT_NEW = "faxmessageurgentnew";
    public final String FAX_MESSAGE_URGENT_OLD = "faxmessageurgentold";
    public final String VIDEO_MESSAGE_URGENT_NEW = "videomessageurgentnew";
    public final String VIDEO_MESSAGE_URGENT_OLD = "videomessageurgentold";
    public final String EMAIL_MESSAGE_URGENT_NEW = "emailmessageurgentnew";
    public final String EMAIL_MESSAGE_URGENT_OLD = "emailmessageurgentold";
    public final String OUTBOUND_CALL_SERVER_HOST = "outboundcallserverhost";
    public final String OUTBOUND_CALL_SERVER_PORT = "outboundcallserverport";
    public final String IS_SOLICITED = "issolicited";
    public final String SUBSCRIPTION_STATE = "subscriptionstate";

    /**
     * Creates an outbound call.
     * <p>
     * This method creates an outbound call.
     * @param callProperties Specifies the call properties to use for the
     * outbound call. Possible properties to set are specified in
     * {@link CallProperties}.
     * @param eventDispatcher The event dispatcher to use when generating
     * events for the new outbound call.
     * @param session The session this call belongs to.
     * @return The initiated call.
     * @throws IllegalArgumentException if the call properties lacks a
     * mandatory attribute, the eventDispatcher is null or session is null.
     * @throws RuntimeException if an error occurs while creating the call.
     */
    public OutboundCall createCall(CallProperties callProperties,
                                   IEventDispatcher eventDispatcher,
                                   ISession session)
            throws IllegalArgumentException, RuntimeException;

    /**
     * This method is used to join the media streams of two calls.
     * The join is full duplex.
     * <p>
     * A {@link com.mobeon.masp.callmanager.events.JoinedEvent} is generated
     * when the join has completed.
     * A {@link com.mobeon.masp.callmanager.events.JoinErrorEvent} is generated
     * if the join fails.
     * The generated event is sent to the given eventDispatcher.
     *
     * @param firstCall
     * @param secondCall
     * @param eventDispatcher
     */
    public void join(Call firstCall, Call secondCall,
                     IEventDispatcher eventDispatcher);

    /**
     * This method is used to unjoin the media streams of two calls.
     * The unjoin is full duplex.
     * <p>
     * A {@link com.mobeon.masp.callmanager.events.UnjoinedEvent} is generated
     * when the unjoin has completed.
     * A {@link com.mobeon.masp.callmanager.events.UnjoinErrorEvent} is
     * generated if the unjoin fails.
     * The generated event is sent to the given eventDispatcher.
     *
     * @param firstCall
     * @param secondCall
     * @param eventDispatcher
     */
    public void unjoin(Call firstCall, Call secondCall,
                       IEventDispatcher eventDispatcher);




    /**
     * Make an outbound SIP request using specified method. Currently supported
     * methods are:
     * <ul>
     *  <li>{@link METHOD_MWI} - Send a message waiting indication. The following parameters
     *  are supported for this method, all values must be of object type String.
     *  <ul>
     *      <li>{@link SEND_TO} - The recipient of the notify request.
     *      Should be a E.164 phone number. (Mandatory)
     *      <li>{@link MESSAGE_ACCOUNT} - The account to which this request corresponds.
     *      Should be a E.164 phone number. (Mandatory)
     *      <li>{@link MESSAGES_WAITING} - Indicate if there are any new messages.
     *      Must be either "yes" or "no". (Optional) If this parameter is unset the default behaviour
     *      is to set the messages waiting indicator to "yes" if any of the given xxx_message_new
     *      parameters is not 0 (zero) otherwise it will be set to "no".
     *      <li>{@link VOICE_MESSAGE_NEW} - The number of new voice messages. (Optional, default = "0")
     *      <li>{@link VOICE_MESSAGE_OLD} - The number of old voice messages. (Optional, default = "0")
     *      <li>{@link FAX_MESSAGE_NEW} - The number of new fax messages. (Optional, default = "0")
     *      <li>{@link FAX_MESSAGE_OLD} - The number of old fax messages. (Optional, default = "0")
     *      <li>{@link VIDEO_MESSAGE_NEW} - The number of new video messages. (Optional, default = "0")
     *      <li>{@link VIDEO_MESSAGE_OLD} - The number of old video messages. (Optional, default = "0")
     *      <li>{@link EMAIL_MESSAGE_NEW} - The number of new email messages. (Optional, default = "0")
     *      <li>{@link EMAIL_MESSAGE_OLD} - The number of old email messages. (Optional, default = "0")
     *      <li>{@link OUTBOUND_CALL_SERVER_HOST} - The outbound call server (hostname or IP address). (Optional, default = "0")
     *      <li>{@link OUTBOUND_CALL_SERVER_PORT} - The outbound call server port. (Optional, if omitted,
     *                        a default port will be used.)
     * </ul>
     *
     * @param method - A String specifying the method. Currently only {@link METHOD_MWI} is supported.
     * @param eventDispatcher - The event dispatcher to use when generating a
     *  response event for the request.
     * @param session - The session this outbound sip request belongs to.
     * @param parameters - A collection of parameter name/value pairs as specified above.
     *
     * @throws IllegalArgumentException is thrown if the method is unknown,
     * if eventDispatcher is null or if any of the parameters is unknown or
     * contain an illegal value or if a mandatory parameter is missing.
     */
    public void sendSipMessage(String method, IEventDispatcher eventDispatcher,
                               ISession session,
                               Collection<NamedValue<String,String>> parameters)
            throws IllegalArgumentException;


    public void setApplicationManagment(IApplicationManagment applicationManagment);

    public void setSupervision(Supervision supervision);

}
