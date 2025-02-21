/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */

package com.mobeon.ntf.out.sip;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.SipMwiEvent;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.common.xmp.client.XmpResult;

import java.util.*;

public class SIPCaller implements SipMwiCallSpec {

    private LogAgent log = NtfCmnLogger.getLogAgent(SIPCaller.class);

    /**
     * Constructor
     */
    public SIPCaller() {
        log = NtfCmnLogger.getLogAgent(SIPCaller.class);
    }

    /**
     *Sends a call to OutdialNotification server via XMP. Returns at once and result is handled in
     *the listener.
     *@param info - Info for the notification
     *@param listener - Worker listener.
     */
    public void sendCall(SipMwiEvent sipMwiEvent, SIPInfo sipInfo, UserInfo userInfo, SIPCallListener listener) {
        new SIPHandler(sipMwiEvent, sipInfo, userInfo, listener).send();
    }

    private class SIPHandler implements XmpResultHandler {

        private SipMwiEvent sipMwiEvent;
        private SIPInfo sipInfo;
        private UserInfo userInfo;
        private SIPCallListener listener;

        public SIPHandler(SipMwiEvent sipMwiEvent, SIPInfo sipInfo, UserInfo userInfo, SIPCallListener listener) {
            this.sipMwiEvent = sipMwiEvent;
            this.sipInfo = sipInfo;
            this.userInfo = userInfo;
            this.listener = listener;
        }

        public void send() {
            try {
                XmpClient client = XmpClient.get();
                int transId = client.nextTransId();
                String request = getRequest(transId);
                boolean sendResult = client.sendRequest(transId, request, IServiceName.MWI_NOTIFICATION, this, null);

                if(!sendResult) {
                    // SipMwi was not sent out successfully
                    listener.handleResult(sipMwiEvent, userInfo, SIPInfo.ERR_FAILTOSEND, 0);
                    return;
                }

            } catch(Exception e) {
                log.error("Exception in SIPCaller " + NtfUtil.stackTrace(e).toString());
                listener.handleResult(sipMwiEvent, userInfo, SIPInfo.ERR_FAILTOSEND, 0);
            }
        }

        private String getRequest(int transId) {
            Properties props = new Properties();
            props.put("number", sipMwiEvent.getNotificationNumber());
            props.put("mailbox-id", sipMwiEvent.getSubscriberNumber());

            // check count.
            if( sipInfo.getNewVoiceCount() > SIPInfo.NOCOUNT ) {
                props.put("new-voice", "" + sipInfo.getNewVoiceCount());
            }
            if( sipInfo.getOldVoiceCount() > SIPInfo.NOCOUNT ) {
                props.put("old-voice", "" + sipInfo.getOldVoiceCount());
            }
            if( sipInfo.getNewVideoCount() > SIPInfo.NOCOUNT ) {
                props.put("new-video", "" + sipInfo.getNewVideoCount());
            }
            if( sipInfo.getOldVideoCount() > SIPInfo.NOCOUNT ) {
                props.put("old-video", "" + sipInfo.getOldVideoCount());
            }
            if( sipInfo.getNewFaxCount() > SIPInfo.NOCOUNT ) {
                props.put("new-fax", "" + sipInfo.getNewFaxCount());
            }
            if( sipInfo.getOldFaxCount() > SIPInfo.NOCOUNT ) {
                props.put("old-fax", "" + sipInfo.getOldFaxCount());
            }
            if( sipInfo.getNewEmailCount() > SIPInfo.NOCOUNT ) {
                props.put("new-email", "" + sipInfo.getNewEmailCount());
            }
            if( sipInfo.getOldEmailCount() > SIPInfo.NOCOUNT ) {
                props.put("old-email", "" + sipInfo.getOldEmailCount());
            }
            if( sipInfo.getNewUrgentVoiceCount() > SIPInfo.NOCOUNT ) {
                props.put("new-urgent-voice", "" + sipInfo.getNewUrgentVoiceCount());
            }
            if( sipInfo.getOldUrgentVoiceCount() > SIPInfo.NOCOUNT ) {
                props.put("old-urgent-voice", "" + sipInfo.getOldUrgentVoiceCount());
            }
            if( sipInfo.getNewUrgentVideoCount() > SIPInfo.NOCOUNT ) {
                props.put("new-urgent-video", "" + sipInfo.getNewUrgentVideoCount());
            }
            if( sipInfo.getOldUrgentVideoCount() > SIPInfo.NOCOUNT ) {
                props.put("old-urgent-video", "" + sipInfo.getOldUrgentVideoCount());
            }
            if( sipInfo.getNewUrgentFaxCount() > SIPInfo.NOCOUNT ) {
                props.put("new-urgent-fax", "" + sipInfo.getNewUrgentFaxCount());
            }
            if( sipInfo.getOldUrgentFaxCount() > SIPInfo.NOCOUNT ) {
                props.put("old-urgent-fax", "" + sipInfo.getOldUrgentFaxCount());
            }
            if( sipInfo.getNewUrgentEmailCount() > SIPInfo.NOCOUNT ) {
                props.put("new-urgent-email", "" + sipInfo.getNewUrgentEmailCount());
            }
            if( sipInfo.getOldUrgentEmailCount() > SIPInfo.NOCOUNT ) {
                props.put("old-urgent-email", "" + sipInfo.getOldUrgentEmailCount());
            }

            String request = XmpProtocol.makeRequest(transId, IServiceName.MWI_NOTIFICATION, props);
            return request;
        }

        public synchronized void handleResult(XmpResult result) {
            int retryTime = 0;
            Properties props = result.getProperties();
            if (props != null) {
                String value = props.getProperty("retry-time");
                if (value != null && value.length() > 0) {
                    try {
                        retryTime = Integer.parseInt(value);
                    } catch (Exception e) {
                        log.error("Failed to parse retry-time: " + value);
                        retryTime = 0;
                    }
                }
            }
            listener.handleResult(sipMwiEvent, userInfo, result.getStatusCode(), retryTime);
        }
    }
}

