/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.smtp;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.email.ConnectionStateListener;
import com.mobeon.common.email.EmailClient;
import com.mobeon.common.email.EmailClientFactory;
import com.mobeon.common.email.request.MimeContainer;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.out.email.EmailConfigWrapper; // reuse this
import com.mobeon.ntf.out.email.EmailOut;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.out.mms.MMSCenter;
import com.mobeon.ntf.userinfo.UserInfo;

public class SMTPOut implements Constants, 
                                ConnectionStateListener {

    private MMSCenter mmsCenter = null;
    private static LogAgent log = NtfCmnLogger.getLogAgent(EmailOut.class);
    private String _mmsName = null;
    private EmailClient client = null;
    private SMTPListener listener = null;
    /* Counters for management information */
    public SMTPOut(MMSCenter center) {

        EmailConfigWrapper configWrapper = new EmailConfigWrapper();
        client = EmailClientFactory.getInstance().createEmailClient(log, configWrapper);
        listener = new SMTPListener(); // Mib handling
        _mmsName = center.getName();
        mmsCenter = center;
    }

    public int handleMail(MimeContainer message, FeedbackHandler ng, UserInfo user) throws InterruptedException{
      // Set the mta to use (multimediacenter)
      client.updateMailTransferAgents( new String[]{ mmsCenter.getHost() + ":" + mmsCenter.getPort()} );
        sendSMTPNotification(message, ng, user);
        message = null;
        return RESULT_MAYBE;
    }

    private void sendSMTPNotification(MimeContainer message, FeedbackHandler ng, UserInfo user) throws InterruptedException{
      int id = client.getNextId();
      listener.add(id, user, ng, NTF_MMS);
      int result = client.sendEmailMessage(message,
                                           user.getNotifExpTime(), 
                                           Config.getImapTimeout(), 
                                           listener, 
                                           client.getNextId());
      if (result == EmailClient.SEND_OK ) {
        if (ng != null) {
          ng.ok(user, NTF_MMS);
        }
        ManagementInfo.get().getStatus(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE, _mmsName).up();
        // Ok counter incremented by listener
      } else {
        if (ng != null) {
          ng.failed(user, NTF_MMS, "EmailClient failed"); // No retry ?
        }
        ManagementInfo.get().getStatus(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE, _mmsName).down();
        // Fail counter incremented by listener
      }
    }

  // Implement the connection state listener
  public void connectionDown(String name) {
    if (ManagementInfo.get().getStatus(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE, name).isUp()) {
      ManagementInfo.get().getStatus(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE, name).down();
      log.error("No connection to MMSC " + name);
    }
  }

  public void connectionUp(String name) {
    if (!ManagementInfo.get().getStatus(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE, name).isUp()) {
      ManagementInfo.get().getStatus(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE, name).up();
    }
  }


}
