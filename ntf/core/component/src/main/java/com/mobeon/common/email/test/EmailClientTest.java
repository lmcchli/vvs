/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email.test;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.email.*;
import com.mobeon.ntf.out.email.EmailTestClient;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.common.email.request.MimeContainer;
import jakarta.mail.Session;
import jakarta.mail.Message;
import java.io.StringBufferInputStream;
import java.util.*;
import junit.framework.*;

/**
 * Test of EmailClient, NTFs new Client-Unit-Connection SMTP protocol.
 */
public class EmailClientTest extends NtfTestCase {

  private MimeContainer msg = null;
  private int ok = 0;
  private int failed = 0;
  private int retry = 0;
  private int queueSize = 10;
  private int pollInterval = 20;
  private int maxConnections = 5;
  private Properties props = null;
  private MyResultHandler rh = null;
  private EmailClient e = null;

  public EmailClientTest(String name) {
    super(name);
  }
  protected void setUp() throws Exception {
    MyConfigWrapper w = new EmailClientTest.MyConfigWrapper();
    LogAgent log = NtfCmnLogger.getLogAgent(EmailTestClient.class);
    e = new EmailClient(log, w);
    props = new Properties();
    props.put("mail.smtp.host", "another.host.lab.mobeon.com");
    msg = new MimeContainer("ipms@localhost",
                            null,
                            null,
                            "emailtestclient",
                            null,
                            "FOO",
                            "Foo testing!",
                            " ");
    rh = new MyResultHandler();
    retry = 0;
    ok = 0;
    failed = 0;
  }

  public void testQueueFullAllDown() throws Exception {
    int synchronousFailures = 0;
     String [] agents = {"renault.lab.mobeon.com:25", "buell.mvas.lab.mobeon.com:25"};
     e.updateMailTransferAgents(agents);
     retry=0; failed=0;
     for (int i=0; i< 20; i++) {
       if ((e.sendEmailMessage(msg, 10, 10, rh, i)) == EmailClient.SEND_FAILED) {
         synchronousFailures++;
       }
     }
     assertEquals(e.sendEmailMessage(msg, 10, 10, rh, 21), EmailClient.SEND_FAILED);
     // 10 put on queueus, and 10 returns SEND_FAILED immediately
     Thread.sleep(5000);
     assertEquals(20, synchronousFailures + retry);
  }





  private  class MyResultHandler implements EmailResultHandler {
    public void ok(int id) {
      ok++;
    }
    public void failed(int id, String errorText) {
      failed++;
    }
    public void retry(int id, String errorText) {
      retry++;
    }
  }

  private  class MyConfigWrapper implements EmailConfig {
    public int getEmailQueueSize() {
      return queueSize;
    }
    public int getEmailPollInterval() {
      return pollInterval;
    }
    public int getEmailMaxConnections() {
      return maxConnections;
    }
    public int getEmailTimeout() {
      return 10000; // unused..
    }
  }
  private  class MyConnectionStateListener implements ConnectionStateListener {
    public void connectionDown(String name) {
      // System.out.println("Conn DOWN");
    }
    public void connectionUp(String name) {
      // System.out.println("Conn UP");
    }
  }

}
