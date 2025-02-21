/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.smtp;

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
 * Listens to callbacks from MMS SMTP sendings
 */
public class SMTPListener implements EmailResultHandler {
    
  private final static Logger log = Logger.getLogger(SMTPListener.class); 
  private HashMap events;

  private ManagementCounter successCounter = ManagementInfo.get().getCounter(
            "MultimediaMessage", ManagementCounter.CounterType.SUCCESS);
  private ManagementCounter failCounter = ManagementInfo.get().getCounter(
            "MultimediaMessage", ManagementCounter.CounterType.FAIL);

  public SMTPListener() {
    events = new HashMap();
  }
  
  public void add(int id, UserInfo user, FeedbackHandler handler, int notifType) {
    SMTPListenerEvent event = new SMTPListenerEvent(notifType, user, handler);     
    events.put(new Integer(id), event);
  }

  public void failed(int id, String errorText) {
    failCounter.incr();
    SMTPListenerEvent event = 
      (SMTPListenerEvent) events.remove(new Integer(id));
    
    if (event != null) {
      if (event.getHandler() != null) {
        event.getHandler().failed(event.getUser(), event.getNotifType(),
                                  errorText);
      }
    } else {
      log.logMessage("Multimedia-Callback to non-existing event " + id, Logger.L_VERBOSE);
    }
  }

  public void ok(int id) {
    successCounter.incr();
    SMTPListenerEvent event = 
      (SMTPListenerEvent) events.remove(new Integer(id));
    
    if (event != null) {
      if (event.getHandler() != null) { 
        event.getHandler().ok(event.getUser(), event.getNotifType());
      }
    } else {
      log.logMessage("Multimedia-Callback to non-existing event " + id, Logger.L_VERBOSE);
    }
  }

  public void retry(int id, String errorText) {
        failCounter.incr();
        SMTPListenerEvent event = (SMTPListenerEvent) events.
          remove(new Integer(id));

        if (event != null) {
            if (event.getHandler() != null) {
                event.getHandler().retry(event.getUser(), event.getNotifType(),
                        errorText);
            }
        } else {
            log.
              logMessage("Multimedia-Callback to non-existing event " + id, Logger.L_VERBOSE);
                    
        }
    }
 

  private class SMTPListenerEvent {
    private int notifType;
    private UserInfo user;
    private FeedbackHandler handler;
        
    public SMTPListenerEvent(int notifType, UserInfo user, FeedbackHandler handler) {
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
