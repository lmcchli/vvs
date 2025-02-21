/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.email;

import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.common.email.EmailResultHandler;
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;
import java.util.*;

/**
 * Listens to callbacks from EML sendings
 */
public class EmailListener implements EmailResultHandler {
    
  private final static Logger log = Logger.getLogger(EmailListener.class); 
     
  private HashMap events;

  private ManagementCounter successCounter = ManagementInfo.get().getCounter(
            "MailTransferAgent", ManagementCounter.CounterType.SUCCESS);
  private ManagementCounter failCounter = ManagementInfo.get().getCounter(
            "MailTransferAgent", ManagementCounter.CounterType.FAIL);


  public EmailListener() {
    events = new HashMap();
  }
  
  public void add(int id, UserInfo user, FeedbackHandler handler, int notifType) {
    EmailListenerEvent event = new EmailListenerEvent(notifType, user, handler);     
    events.put(new Integer(id), event);
  }

  public void failed(int id, String errorText) {
    failCounter.incr();
    EmailListenerEvent event = 
      (EmailListenerEvent) events.remove(new Integer(id));
    
    if (event != null) {
      if (event.getHandler() != null) {
        event.getHandler().failed(event.getUser(), event.getNotifType(),
                                  errorText);
      }
    } else {
      log.
        logMessage("Email-Callback to non-existing event " + id, Logger.L_VERBOSE);
    }
  }

  public void ok(int id) {
    successCounter.incr();
    EmailListenerEvent event = 
      (EmailListenerEvent) events.remove(new Integer(id));
    if (event == null) {
      log.
        logMessage("Email-Callback to non-existing event " + id, Logger.L_VERBOSE);
    }
  }

  public void retry(int id, String errorText) {
        failCounter.incr();
        EmailListenerEvent event = (EmailListenerEvent) events.
          remove(new Integer(id));

        if (event != null) {
            if (event.getHandler() != null) {
                event.getHandler().retry(event.getUser(), event.getNotifType(),
                        errorText);
            }
        } else {
            log.logMessage(
                    "Email-Callback to non-existing event " + id, Logger.L_VERBOSE);
        }
    }
 

  private class EmailListenerEvent {
    private int notifType;
    private UserInfo user;
    private FeedbackHandler handler;
        
    public EmailListenerEvent(int notifType, UserInfo user, FeedbackHandler handler) {
      this.notifType = notifType;
      this.user = user;
      this.handler = handler;
    }
        
    public int getNotifType() {
      return notifType;
    }
    
    public UserInfo getUser() {
      return user;
    }
        
    public FeedbackHandler getHandler() {
      return handler;
    }
  }
}
