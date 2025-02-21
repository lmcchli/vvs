/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.wap;

import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.out.wap.papapi.WapPerson;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.WapFilterInfo;
import com.mobeon.ntf.util.Logger;

/**
 * WAPOut is NTFs interface to sending WAP messages.
 */
public class WapOut implements Constants {
    private final static Logger log = Logger.getLogger(WapOut.class);

    /* Counters for management information */
    private static ManagementCounter successCounter =
        ManagementInfo.get().getCounter("WAPGateway", ManagementCounter.CounterType.SUCCESS);
    private static ManagementCounter failCounter =
        ManagementInfo.get().getCounter("WAPGateway", ManagementCounter.CounterType.FAIL);

    /**
     * Constructor.
     */
    public WapOut() {
    }


    /**
     * sendNotification is used to request that an WAP notification is created
     * and sent to the user.
     *@param ng the FeedbackHandler that contains the notification contents
     * and collects responses for all receivers of the email.
     *@param user the information about the receiver.
     *@param info WAP-specific information derived from the users filters.
     */
    public synchronized int sendNotification(UserInfo user,
                                             WapFilterInfo info,
                                             FeedbackHandler ng,
                                             NotificationEmail email,
                                             UserMailbox inbox) {

        int count = 0;
        if (NotificationHandler.hasExpired(user.getNotifExpTime(), email)) {
            log.logMessage("Notification to " + user.getMail() + " has expired.", Logger.L_VERBOSE);
            ng.expired(user, NTF_WAP);
            return 1;
        }
        
    	IServiceInstance inst;
    	String compName;
        String host;
        String protocol;
        int port = -1;
        
    	try {
    		inst = ExternalComponentRegister.getInstance().locateService(IServiceName.WAP_GATEWAY);
    		compName = inst.getProperty(IServiceInstance.COMPONENT_NAME);
    		host = inst.getProperty(IServiceInstance.HOSTNAME);
    		protocol = inst.getProperty(IServiceInstance.PROTOCOL);
    		port = Integer.parseInt(inst.getProperty(IServiceInstance.PORT));
    	}
    	catch (NoServiceFoundException e) {
    		ng.failed(user, NTF_WAP, "No service found for service name: " + IServiceName.WAP_GATEWAY);
    		e.printStackTrace(System.out);
    		return 1;
    	}
    	catch (NumberFormatException e) {
    		log.logMessage("Port is not numeric for service name: " + IServiceName.WAP_GATEWAY, Logger.L_ERROR);
    		e.printStackTrace(System.out);
    		return 1;
    	}
        
        String[] numbers = info.getNumbers();
        for( int i=0;i<numbers.length;i++ ) {
            forwardWapPush(user, ng, email, inbox, compName, host, protocol, port, numbers[i]);
            count++;
        }
        
        return count;
    }

    private void forwardWapPush(UserInfo user,
                                FeedbackHandler ng,
                                NotificationEmail email,
                                UserMailbox inbox,
                                String componentName,
                                String host,
                                String protocol,
                                int port,
                                String notifNumber){

        WapPerson wp = new WapPerson(user, email, inbox);
        wp.setUserName(Config.getWapPushUserName());
        wp.setHostName(host);
        wp.setPortNumber(port);
        wp.setProtocol(protocol);
        wp.setPassWd(Config.getWapPushPasswd());
        wp.setUrlSuffix(Config.getWapPushUrlSuffix());
        wp.setMessageType(email.getEmailType());
        wp.setPushData(makePushUrl(notifNumber, email));

        log.logMessage("Pushing WAP notification to " + notifNumber, Logger.L_DEBUG);
        if(wp.pushNotifyRequest()) {
            ng.ok(user, NTF_WAP);
            successCounter.incr();
            ManagementInfo.get().getStatus(IServiceName.WAP_GATEWAY, componentName).up();
        }
        else {
            ng.failed(user, NTF_WAP, "Failed to send WAP push notification.");
            failCounter.incr();
            ManagementInfo.get().getStatus(IServiceName.WAP_GATEWAY, componentName).down();
        }
    }

    private String makePushUrl(String nofifNumber, NotificationEmail email) {
        if (email.getEmailType() == NTF_VOICE) return "wtai://wp/mc;" + nofifNumber;
        else return "http://" + Config.getWapPushRetrievalHost();
    }
}
