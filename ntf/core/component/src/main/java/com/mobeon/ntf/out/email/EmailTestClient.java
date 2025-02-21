/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.email; // XXX

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.email.EmailResultHandler;
import com.mobeon.common.email.EmailConfig;
import com.mobeon.common.email.EmailClient;
import com.mobeon.common.email.ConnectionStateListener;
import com.mobeon.common.email.request.MimeContainer;

public class EmailTestClient  {

  private  int ok = 0;
  private  int failed = 0;
  private  int retry = 0;
  private static String [] agents = new String [1];
  private static String to;

  public static void main(String[] args) {
    System.out.println("Agent: " + args[0] + ":25");
    agents[0] = args[0] + ":25";
    System.out.println("TO: " + args[1] );
    to = args[1];
    EmailTestClient e = new EmailTestClient();
  }

  public EmailTestClient() {
    MyConfigWrapper w = new EmailTestClient.MyConfigWrapper();

    LogAgent log = NtfCmnLogger.getLogAgent(EmailTestClient.class);
    EmailClient e = new EmailClient(log, w);
    
    MyConnectionStateListener l = new EmailTestClient.MyConnectionStateListener();
    e.setConnectionStateListener(l);

    try {
      MyResultHandler rh = new MyResultHandler();
      e.updateMailTransferAgents(agents);
      Thread.sleep(500);

      for (int i=0; i< 10; i++) {
        MimeContainer msg = new MimeContainer(to,
                                              null,
                                              null,
                                              "testclient",
                                              null,
                                              "FOO " + i,
                                              "This is the FOO BAR nr " + i,
                                              "");
        Thread.sleep(50);
        int result = e.sendEmailMessage(msg, 100, 5000, rh, i);
        System.out.println("Result " + i + " is: " + result);
      }
    } catch (Exception ex) {
      System.out.println("Doh!");
      ex.printStackTrace();
    }
  }


  private  class MyResultHandler implements EmailResultHandler {

    public void ok(int id) {
      ok++;
    }    
    public void failed(int id, String errorText) {
      failed++;
      System.out.println("ID "+ id + " failed with text: " + errorText);
    }    
    public void retry(int id, String errorText) {
      retry++;
      System.out.println("ID "+ id + " retried with text: " + errorText);
    }

  }

  private  class MyConfigWrapper implements EmailConfig {
    public int getEmailQueueSize() {
      return 10; // XXX test size
    }      
    public int getEmailPollInterval() {
      return 30; // XXX test seconds
    }      
    public int getEmailMaxConnections() {
      return 4; // XXX test nr of connections
    }
    public int getEmailTimeout() {
      return 2000; // XXX test smtp timeout
    }
  }
  
  private  class MyConnectionStateListener implements ConnectionStateListener {
    public void connectionDown(String name) {
      System.out.println("Conn DOWN : " + name);
    }          
    public void connectionUp(String name) {
      System.out.println("Conn UP : " + name);
    }         
  }  
  
  public boolean ifLog(int level) {
    return true;
  }
    
  public void logString(String msg, int l) {
    System.out.println(msg);
  }
  
}
