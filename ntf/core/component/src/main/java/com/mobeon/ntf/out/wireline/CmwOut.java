/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.wireline;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.out.pager.XmpNotificationResultHandler;
import com.mobeon.ntf.userinfo.CmwFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;
import java.util.*;

/**
 * CmwOut is NTFs interface to output notifications to cmwers.
 */
public class CmwOut implements Constants {

	/** Global log handler*/
    private final static Logger log = Logger.getLogger(CmwOut.class);
    XmpClient client;

    

    /**
     * Constructor
     */
    public CmwOut() {
        client = XmpClient.get();
       
       
    }

    /**
     * sendNotification is used to request that a call-MWI notification is
     * created and sent to the user.
     *@param ng - the FeedbackHandler that contains the notification contents
     * and collects responses for all receivers of the email.
     *@param user - the information about the receiver.
     *@param info - call-MWI-specific information derived from the users filters.
     *@return the number of requests sent.
     */
    public int sendNotification(UserInfo user,
                                CmwFilterInfo info,
                                FeedbackHandler ng,
                                NotificationEmail email) {
        if (info == null) { return 0; }

       
        
        String caller;
        switch(Config.getCallMwiCaller()) {
        case CALLMWI_CALLER_CALLER:
            caller = email.getSenderPhoneNumber();
            if (caller == null || "".equals(caller)) {
                caller = Config.getNumberToMessagingSystemForCallMwi();
            }
            break;
        case CALLMWI_CALLER_SUBSCRIBER:
            caller = user.getTelephoneNumber();
            break;
        case CALLMWI_CALLER_SYSTEM:
        default:
            caller = Config.getNumberToMessagingSystemForCallMwi();
        }
        
        int count = 0;
        String[] numbers = info.getNumbers();
        for( int i=0;i<numbers.length;i++ ) {
            int transId = client.nextTransId();

            Properties props = new Properties();
            props.setProperty("mailbox-id", user.getTelephoneNumber() );
            props.setProperty("called-number", numbers[i] );
            props.setProperty("called-type-of-number", "" + Config.getTypeOfNumber() );
            props.setProperty("called-numbering-plan-id", "" + Config.getNumberingPlanIndicator() );
            props.setProperty("calling-number", caller );
            props.setProperty("calling-type-of-number", "" + Config.getTypeOfNumber() );
            props.setProperty("calling-numbering-plan-id", "" + Config.getNumberingPlanIndicator() );

            String request = new XmpProtocol().makeRequest(transId, IServiceName.CALL_MWI_NOTIFICATION, props );

            XmpNotificationResultHandler resultHandler = new XmpNotificationResultHandler(ng, user, NTF_CMW );
            boolean ok = client.sendRequest(transId, request, IServiceName.CALL_MWI_NOTIFICATION, resultHandler, null );
            if (!ok) {
                // temporary error, retry
                ng.retry( user, NTF_CMW, "Send error in CMWOut" );
                log.logMessage("sendNotification: request failed" , log.L_DEBUG);
            }
            
            count++;
        }
       
        
        return count;
    }
}
