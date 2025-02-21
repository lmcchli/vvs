/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.outdial;

import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.common.xmp.client.XmpResult;

import java.util.*;

class OdlCaller implements Constants, OdlCallSpec {
    
    private final static Logger log = Logger.getLogger(OdlCaller.class);

	public OdlCaller() {

    }

    /**
     *Sends a call to OutdialNotification server via XMP. Returns at once and result is handled in
     *the listener.
     *@param subscriberNumber - The Number to call.
     *@param notificationNumber - The notified number
     *@param user - The user to handle.
     *@param listener - Where to put callbacks.
     */
    public void sendCall(String subscriberNumber, String notificationNumber, UserInfo user, OdlCallListener listener ) {
        new OdlHandler(subscriberNumber, notificationNumber, user, listener ).send();
    }

    private class OdlHandler implements XmpResultHandler {
        private String subscriberNumber;
        private String notificationNumber;
        private UserInfo user;
        private OdlCallListener listener;
        
        public OdlHandler(String subscriberNumber, String notificationNumber, UserInfo user, OdlCallListener listener) {
            this.subscriberNumber = subscriberNumber;
            this.notificationNumber = notificationNumber;
            this.user = user;
            this.listener = listener;
        }

        public void send() {
            // get hlr data
            try {
                NotifState result = user.getFilter().isNotifTypeDisabledOnUser(NTF_ODL,notificationNumber);
                if( result == NotifState.DISABLED ) {
                    listener.handleResult(subscriberNumber, notificationNumber, user, OdlConstants.EVENT_CODE_NOTIFDISABLED);
                    return;
                } else if( result == NotifState.FAILED  || result == NotifState.RETRY ) {
                    //in this case should leave out-dial state machine to decide what to do (i.e retry or fail)
                    //not sure if should have EVENT_CODE_HLR_ERROR or EVENT_CODE_LOCATION_FAILURE .. maybe one should be removed. I prefer ERROR..
                    listener.handleResult(subscriberNumber, notificationNumber, user, OdlConstants.EVENT_CODE_HLR_ERROR);
                    return;
                }  
                int cfu = user.getDivertAll();
                if( cfu == 1 ) {
                    listener.handleResult(subscriberNumber, notificationNumber, user, OdlConstants.EVENT_CODE_CFU_ON);
                    return;
                }
                if( cfu == 2 ) {
                    listener.handleResult(subscriberNumber, notificationNumber, user, OdlConstants.EVENT_CODE_CFU_FAILURE);
                    return;
                }

                XmpClient client = XmpClient.get();
                int transId = client.nextTransId();
                String request = getRequest(transId);
                boolean sendResult = client.sendRequest(transId, request, IServiceName.OUT_DIAL_NOTIFICATION, this, null);

                if( sendResult == false ) {
                    listener.handleResult(subscriberNumber, notificationNumber, user, OdlConstants.EVENT_CODE_LIMIT_EXCEEDED);
                    return;
                }

            } catch(Exception e) {
                log.logMessage("Exception in OdlCaller " + NtfUtil.stackTrace(e).toString(), Logger.L_ERROR );
                listener.handleResult(subscriberNumber, notificationNumber, user, OdlConstants.EVENT_CODE_INTERRUPTED);
            }
        }

        private String getRequest(int transId) {

            Properties props = new Properties();
            props.put("number", notificationNumber);
            props.put("mailbox-id", user.getTelephoneNumber());
            props.put("type-of-number", "" + Config.getTypeOfNumber());
            props.put("numbering-plan-id", "" + Config.getNumberingPlanIndicator());
            props.put("language", user.getPreferredLanguage());
            if(user.isPrepaid()){
           		props.put("prepaid", "true");
            }else {
           		props.put("prepaid", "false");
            }
            String request = XmpProtocol.makeRequest(transId, IServiceName.OUT_DIAL_NOTIFICATION, props);
            return request;
        }

        public synchronized void handleResult(XmpResult result) {
            listener.handleResult(subscriberNumber, notificationNumber, user, result.getStatusCode());
        }
    }
}
