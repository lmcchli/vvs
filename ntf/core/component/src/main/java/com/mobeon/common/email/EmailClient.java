/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.email.request.Request;
import com.mobeon.common.email.request.MimeContainer;


/**
 * EmailClient is the starting point to send Email-messages to.
 * EmailClient is responsible to select
 * a suitable EmailUnit(MTA) and transfer the request to the unit.
 */
public class EmailClient {
  public static final int SEND_OK = 0;
  public static final int SEND_FAILED = 1;
  public static final int SEND_QUEUE_FULL = 2;

  private ConnectionStateListener connectionStateListener;
 
  private LogAgent logger = NtfCmnLogger.getLogAgent(EmailClient.class);
  private EmailConfig config;

  private int nextId = 0;

  private String [] agents = null;
  private int agentsSize;

  /**
   * @return If no alive agent (unit) is found -1 is returned
   */
  private synchronized int getNextAliveAgentId() {
    int nextAgent = 0;
      if (agents == null) {
        // Not initialized
        return -1;
      }
      int currentAgentId = nextAgent;
      do {
        nextAgent++;
        if (nextAgent >= agentsSize) {
          nextAgent = 0;
        }
        if (currentAgentId == nextAgent) { // We have tried all.
          if (!isOk(agents[currentAgentId])) {
            return -1;
          } else {
            return currentAgentId; // Only current ok
          }
        }
      } while (!isOk(agents[nextAgent]));
    return nextAgent;
  }

  /**
   * Checks if mail transfer agent is up and queue to it is not full
   *
   * @param agent a <code>String</code> value on form host:port
   * @return true if ok, false if full or down
   */
  private boolean isOk(String agent) {
    String [] parts = agent.split(":");
    if (parts.length != 2 || parts[0] == null || parts[1] == null) {
      return false;
    }
    String host = parts[0];
    String port = parts[1];
    EmailUnit unit = EmailUnit.get(host,
                                   Integer.parseInt(port),
                                   config.getEmailTimeout(),
                                   config,
                                   connectionStateListener);
    
    return (unit.isOk() && !unit.isQueueFull());
  }

  /**
   * Creates a new <code>EmailClient</code> instance.
   *
   * @param logger a <code>Logger</code> value
   * @param config an <code>EmailConfig</code> value
   */
  public EmailClient(LogAgent logger, EmailConfig config) {
        this.logger = logger;
        this.config = config;
  }

  /**
   * Set the state listener for the client
   *
   * @param listener a <code>ConnectionStateListener</code> value
   */
  public void setConnectionStateListener(ConnectionStateListener listener) {
    this.connectionStateListener = listener;
  }

  /**
   * Next id
   *
   * @return an <code>int</code> value
   */
  public synchronized int getNextId() {
    return nextId++;
  }

  /**
   * Construct a request (unused, only for multi-requests)
   *
   * @param msg a <code>MimeContainer</code> value
   * @param validity an <code>int</code> value
   * @param rh an <code>EmailResultHandler</code> value
   * @param id an <code>int</code> value
   * @return a <code>Request</code> value
   */
  public static Request makeRequest(MimeContainer msg, int validity, EmailResultHandler rh, int id) {
    return EmailUnit.makeRequest(msg, validity, rh, id);
  }

  /**
   *Sends a reqular mime message.
   *@param msg - mimeMessage
   *@param validity - how long the message is valid in the queue.
   *@param rh - ResultHandler to place callbacks on.
   *@param id - the id for the call, used in callbacks.
   *@param timeout - milliseconds (unused)
   *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
 * @throws InterruptedException exception
   */
  public synchronized int sendEmailMessage(MimeContainer msg,
                                           int validity,
                                           int timeout,
                                           EmailResultHandler rh,
                                           int id) throws InterruptedException {

    // Only use resulthandler.failed if content of msg is bad

    // Find a living SMTP server (MTA) round robin wise.

    int nextAliveAgent = getNextAliveAgentId();
    if (nextAliveAgent == -1) {
      // No units available, return failure
      if (logger != null) {
        logger.
          debug("EmailClient.sendEmailMessage(): No units available (or all queues full). Send failed for id "
                    + id);
      }
      return SEND_FAILED;
    }
    String [] parts = agents[nextAliveAgent].split(":");

    if (parts.length != 2 || parts[0] == null || parts[1] == null) {
      if (logger != null) {
        logger.
          debug("EmailClient.sendEmailMessage(): No alive agents. Send failed for id "
                  + id);
      }
      return SEND_FAILED; // Should not happen, checked before
    }
    String host = parts[0];
    String port = parts[1];
    int p = 25;
    try {
      p = Integer.parseInt(port);
    } catch (Exception e) {
      ;
    }

    EmailUnit unit = EmailUnit.get(host, p, timeout, config, connectionStateListener);
    if (unit == null) {
      if (logger != null) {
        logger.
         debug("EmailClient.sendEmailMessage(): Null unit. Send failed for id "
                    + id);
      }
      return SEND_FAILED; // Again, it should not happen
    }
    return unit.sendEmail(msg, validity, rh, id);
  }

  /**
   * Add agents as an array of mailhost:port
   * @param agents a <code>String[]</code> value of current MTAs (mailhost:port)
   * @return 0 if all ok
   */
  public synchronized int updateMailTransferAgents(String [] agents) {
      if (agents == null) {
          return -1;
      }

      if (logger != null) {
          for (int i = 0; i < agents.length; i++) {
              logger.debug("EmailClient.updateMailTransferAgents(): Found MailTransferAgent :" + agents[i]);
          }
      }

      synchronized (this) {
          this.agents = agents;
          this.agentsSize = agents.length;
      }
      return 0;
  }
}