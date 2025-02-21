/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.pager;

import com.mobeon.common.xmp.client.XmpResult;
import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;

import java.util.ArrayList;


/**
 * XmpNotificationResultHandler handles xmpresults for notificationgroups.
 */
public class XmpNotificationResultHandler implements XmpResultHandler {
    private final static Logger log = Logger.getLogger(XmpNotificationResultHandler.class);
    public FeedbackHandler ng;
    public UserInfo user;
    public int notifType;
    
    public XmpNotificationResultHandler(FeedbackHandler ng, UserInfo user, int notifType) {
        this.ng = ng;
        this.user = user;
        this.notifType = notifType;
    }

    public void handleResult( XmpResult result ) {
        
        switch (result.getStatusCode() / 100) { //First digit in response determines general
            //result
            case 2: //success
                ng.ok(user, notifType);
                break;
            case 4: //temporary error
                if (result.getStatusCode() == 408) {
                    ng.expired(user, notifType);
                } else {
                    ng.retry(user, notifType, "" + result.getStatusCode()
                    + " " + result.getStatusText());
                }
                break;
            case 5: //permanent error
                ng.failed(user, notifType, "" + result.getStatusCode()
                + " " + result.getStatusText());
                break;
            default:
                ng.failed(user, notifType, "" + result.getStatusCode()
                + " " + result.getStatusText());
                log.logMessage("XmpNotificationResultHandler: unknown status code in XMP result: "
                + result.getStatusCode() + " " + result.getStatusText(), log.L_ERROR);
        }
    }

    
}
