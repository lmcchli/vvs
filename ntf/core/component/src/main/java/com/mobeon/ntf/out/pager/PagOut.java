/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.pager;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.common.xmp.XmpAttachment;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.userinfo.PagFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;
import java.util.*;
import jakarta.mail.Part;

/**
 * PagOut is NTFs interface to output notifications to pagers.
 */
public class PagOut implements Constants {
    /** Global log handler*/
    private final static Logger log = Logger.getLogger(PagOut.class);
    XmpClient client;

    
    /**
     * Constructor
     */
    public PagOut() {
        client = XmpClient.get();
                
    }

    /**
     * sendNotification is used to request that a pager notification is
     * created and sent to the user.
     *@param ng - the FeedbackHandler that contains the notification contents
     * and collects responses for all receivers of the email.
     *@param user - the information about the receiver.
     *@param info - pager-specific information derived from the users filters.
     *@return the number of requests sent.
     */
    public int sendNotification(UserInfo user,
                                PagFilterInfo info,
                                FeedbackHandler ng,
                                NotificationEmail email) {
        if (info == null) { return 0; }

        
        
        String caller;
        caller = email.getSenderPhoneNumber();
        if (caller == null || "".equals(caller)) {
            caller = Config.getNumberToMessagingSystem();
        }
        int transId = client.nextTransId();

        Properties props = new Properties();
        props.setProperty("pause-time", Integer.toString(Config.getPagerPauseTime() ));
        props.setProperty("hang-up", "" + info.getHangup() );
        props.setProperty("paging-digits", insertCaller(info.getContent(), caller ));
        props.setProperty("paging-system-number", info.getNumber() );
        props.setProperty("mailbox-id", user.getTelephoneNumber() );

        //String request = PagProtocol.get().makeRequest(transId, info, caller, user.getTelephoneNumber());
        String request = new XmpProtocol().makeRequest(transId, IServiceName.PAGER_NOTIFICATION, props );

        XmpNotificationResultHandler resultHandler = new XmpNotificationResultHandler( ng, user, NTF_PAG );

        
        boolean ok = client.sendRequest(transId, request, IServiceName.PAGER_NOTIFICATION, resultHandler, null );
        
        if (!ok) {
            ng.retry( user, NTF_PAG, "Send error in PagOut" );
            log.logMessage("sendNotification: request failed" , log.L_DEBUG);
        }
        
        // always only one send for pager
        return 1;
    }

    public static String insertCaller(String template, String caller) {
        int pos = 0;
        int atIndex = 0;
        if (caller == null) {
            caller = "";
        }
        while ((atIndex = template.indexOf("@", pos)) >= 0) {
            template = template.substring(0, atIndex) + caller + template.substring(atIndex + 1);
            pos = atIndex + caller.length();
        }
        return template;
    }
}
