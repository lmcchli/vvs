/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.email.request.*;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.util.threads.NtfThread;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;

import jakarta.mail.MessagingException;

/**
 * EmailUnit represents one MTA.
 * One EmailUnit can have several EmailConnections.
 */
public class EmailUnit extends NtfThread {

  /** A list of all active EmailUnits */
  public static HashMap<String, EmailUnit> allUnits = new HashMap<String, EmailUnit>();

  /** A queue with requests */
  private RequestQueue queue;

  /** All connections that works */
  private ArrayList<EmailConnection> connections;

  private static LogAgent log = NtfCmnLogger.getLogAgent(EmailUnit.class);

  /** The host of the MTA */
  private String host;

  /* the port of the MTA */
  private int port;

  /* the connection timeouts for the MTA */
  private int timeout;

  /** Name of the MTA */
  private String instanceName;

  /** where to send connection status callbacks */
  private ConnectionStateListener connectionStateListener = null;

  /** the status of the unit(MTA) */
  private boolean ok = true;

  /** how long since the last message was sent */
  private long lastSent;

  private EmailConfig config;

  /**
   * Gets an EmailUnit. First looks for a stored unit,
   * if no stored unit exists a new one is created.
   * If the unit is created ok the unit is added to the list of units.
   *
   * @param mailhost - the name of the mail transfer agent
   * @param port - mail transfer agent portnumber (usually 25)
   * @param timeout - mail.smtp.timeout/mail.smtp.connectiontimeout setting for this unit
   * @param config - an instance of EmailConfig.
   * @param listener - This is used for callbacks when the unit is down or up, can be null.
   * @return - An EmailUnit or null if no unit could be created.
   */
  public static synchronized EmailUnit get(String mailhost,
                                           int port,
                                           int timeout,
                                           EmailConfig config,
                                           ConnectionStateListener listener) {
    EmailUnit unit = allUnits.get(mailhost + ":" + port);

    if (unit == null) {
      // unit will put itself in the list if it is ok.
      unit = new EmailUnit(mailhost, port, timeout, config, listener);
    }
    unit = allUnits.get(mailhost + ":" + port);
    return unit;
  }

  /**
   * Creates a new instance of SmsUnit.
   *
   * @param mailhost a <code>String</code> mailhost
   * @param port an <code>int</code> porn nr
   * @param timeout an <code>int</code> value
   * @param config an <code>EmailConfig</code> value
   * @param listener a <code>ConnectionStateListener</code> value
   */
  private EmailUnit(String mailhost,
          int port,
          int timeout,
          EmailConfig config,
          ConnectionStateListener listener) {

      super("EmailUnit-" + mailhost + ":" + port);
      instanceName = mailhost + ":" + port;
      queue = new EmailUnit.RequestQueue(config.getEmailQueueSize());    
      connections = new ArrayList<EmailConnection>();

      this.config = config;
      this.host = mailhost;
      this.port = port;
      this.timeout = timeout;
      this.connectionStateListener = listener;
      allUnits.put(instanceName, this);

      addConnection(); //add initial connection
      start();
  }

  /**
   * The run method adds poll requests to the queue when the queue is empty and no messages
   * have been sent for the poll interval.
   */
  public boolean ntfRun() {

      try {
          sleep(500);

          long diff = System.currentTimeMillis() - lastSent;

          if (diff > config.getEmailPollInterval() * 1000) {
              try {
                  if (connections.size() == 0)
                  {
                      addConnection(); //reconnect at poll interval...
                  }
                  sendPollRequest();
              } catch (InterruptedException ie) {
                  log.debug("EmailUnit.run(): Poll request not sent due to Managment state change.");
              }
          }            
      } catch (InterruptedException e1) {
          log.error("EmailUnit.run(): Thread interupted, exiting.. ");
          return true;
      } catch (Exception e) {
          log.error("EmailUnit.run(): Unexpected exception! - ",e);
      }
      return false;
  }

  /**
   * Creates an EmailConnection and starts it.
   */
  private synchronized void addConnection() {
      log.debug("EmailUnit.addConnection(): " + instanceName + " adding connection");
      try {
          EmailConnection conn = new EmailConnection(this,config);
          connections.add(conn);
      } catch (MessagingException m) {
          log.info("Could not add new Email Connection");
      }
  }

  /**
   * Gets the instance name
   *
   * @return instance name
   */
  public String getInstanceName() {
    return instanceName;
  }

  /**
   * EmailConnections use this function to wait for a request in the queue.
 * @throws InterruptedException exception thrown
   */
  public Request waitForRequest() throws InterruptedException {
    log.debug("EmailUnit.waitForRequest(): " + instanceName + " waiting for requests");
    Request request = queue.
      getNextObject(5);
    return request;
  }


  /**
   * Returns the email config object used
   *
   * @return an <code>EmailConfig</code> object
   */
  public EmailConfig getConfig() {
    return config;
  }

  /**
   * Get the host name this unit is associated with
   *
   * @return a <code>String</code> value
   */
  public String getHost() {
    return host;
  }
  
  /**
   * Gets the logger object
   *
   * @return a <code>LogAgent</code> value
   */
  public LogAgent getLog() {
    return log;
  }


  /**
   * Get the port nr this unit is associated with
   *
   * @return an <code>int</code> value
   */
  public int getPort() {
    return port;
  }

  /**
   * Get connection timeout value (seconds)
   *
   * @return an <code>int</code> value
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * checks if the unit is up and running.
   * @return true if unit is up and running
   */
  public boolean isOk() {
    return ok;
  }

  /**
   * checks if the unit's queue is full
   * @return true if queue is full
   */
  public boolean isQueueFull() {
    return (queue.getSize() >= config.getEmailQueueSize());
  }

  public int getQueueSize() {
      return (queue.getSize());
  }


  /**
   * Sends an email using this unit
   *
   * @param msg a <code>MimeContainer</code> value
   * @param validity an <code>int</code> value
   * @param rh an <code>EmailResultHandler</code> value
   * @param id an <code>int</code> value
   * @return an <code>int</code> value
 * @throws InterruptedException exception
   */
  public int sendEmail(MimeContainer msg, int validity,
                       EmailResultHandler rh, int id) throws InterruptedException {
    if ( connections.size()==0 )
        { addConnection(); }
    EmailRequest request = new EmailRequest(msg, validity, rh, id);
    return send(request);
  }

  /**
   * Unused so far
   *
   * @param request a <code>MultiRequest</code> value
   * @return an <code>int</code> value
 * @throws InterruptedException exception
   */
  public int sendMulti(MultiRequest request) throws InterruptedException {
    return send(request);
  }

  /**
   * Puts the request on the queue. If the unit is unavailable or the queue is full
   * the request (and the queue) should be transferred to an available unit.
   * This is done by the client when return value is either SEND_QUEUE_FULL
   * or SEND_FAILED.
   *
   * @param request - the request to put on queue.
   *
   * @return SEND_FAILED or SEND_QUEUE_FULL
 * @throws InterruptedException exception
   */
    private int send(Request request) throws InterruptedException {
      if (!ok) {

        log.debug("EmailUnit.send(): Send failed (unit down) for " + instanceName);

        return EmailClient.SEND_FAILED;
      };

      if (queue.getSize() >= config.getEmailQueueSize()) {

        log.debug("EmailUnit.send(): Queue already full for " + instanceName);

        return EmailClient.SEND_QUEUE_FULL;
      };

      // Add connection if queue is more than 1/4 of max
      if ((queue.getSize() > 0
           && queue.getSize() >= config.getEmailQueueSize() / 4
           && connections.size() < config.getEmailMaxConnections())
          || connections.size() == 0) {
        addConnection();
      }

      // Add the request to the q

      log.debug("EmailUnit.send() Adding request to " + instanceName + " queue");
      if (!queue.putObject(request, 30)) {

          log.debug("EmailUnit.send(): Queue full for " + instanceName);
          return EmailClient.SEND_QUEUE_FULL;
      }

      lastSent = System.currentTimeMillis();
      return EmailClient.SEND_OK;
    }


  /**
   * Puts a new PollRequest on the queue
 * @throws InterruptedException exception
   */
  private void sendPollRequest() throws InterruptedException {

    log.debug("EmailUnit.sendPollRequest(): " + instanceName + " adding poll request to queue");

    PollRequest request = new PollRequest();
    if (connections.size() == 0) {
      addConnection();
    }
    queue.expand(request);
    lastSent = System.currentTimeMillis();
  }

  /**
   * Unused so far - creates a request
   *
   * @param msg a <code>MimeContainer</code> value
   * @param validity an <code>int</code> value
   * @param rh an <code>EmailResultHandler</code> value
   * @param id an <code>int</code> value
   * @return a <code>Request</code> value
   */
  public static Request makeRequest(MimeContainer msg, int validity,
                                    EmailResultHandler rh, int id) {
    Request request = new EmailRequest(msg, validity, rh, id);
    return request;
  }

  /**
   *callback when a connection is coming up.
   */
  public void connectionUp() {

    log.debug("EmailUnit.connectionUp(): " + instanceName + " received connection up");

    ok = true;
    //connectionAddSuspension = false;
    if (connectionStateListener != null) {
      connectionStateListener.connectionUp(instanceName);
    }
  }

  
  
  /**
   *callback when a connection is coming down.
   *If no connections are left the status of the unit becomes unavailable
   *and a pollrequest is added to the queue.
   *
   *@param connection - the connection that stopped.
   */
  public synchronized void connectionDown(EmailConnection connection) {

      log.debug("EmailUnit.connectionDown(): " + instanceName + " received connection down");

      connections.remove(connection);

      // If connection is down, unit is unusable and we need to send the unsent
      // reqests using another unit
      if (connections.size() <= 0) {

          log.debug("EmailUnit.connectionDown(): No more connections available");

          ok = false;
          if (connectionStateListener != null) {
              connectionStateListener.connectionDown(instanceName);
          }
          if (queue.getSize() > 0) {
              try {

                  log.debug("EmailUnit.connectionDown(): Queue contains unsent request");

                  while (!queue.isEmpty()) {
                      Request request = queue.getNextObject(1);
                      if (request != null ) {
                          if (request.getResultHandler() != null) {
                              if (send(request) != EmailClient.SEND_OK) {
                                  request.getResultHandler().
                                  retry(request.getId(),
                                          "Cant send message to Mail Transfer Agent <"
                                                  + instanceName + "> (retrying)");
                              }
                          }
                      }
                  }
              } catch (Exception e) {
                  log.error("EmailUnit.sendFailed(): Failed to resend requests in queue due to: ",e);
                  queue.clear();
              }
          }
      }
  }


  /**
   * Sending of request failed. Send the request again
   * @param request - the request that failed.
   * @throws InterruptedException exception
   */
  public void sendFailed(Request request) throws InterruptedException {

    log.debug("EmailUnit.sendFailed(): Failed to send request, resending the request");

    if (send(request) != EmailClient.SEND_OK) {
      if (request instanceof MultiRequest) {
        MultiRequest multi = (MultiRequest) request;
        while (multi.getCount() > 0) {
          Request req = multi.getNextRequest();
          if (req.getResultHandler() != null) {
            req.getResultHandler().retry(req.getId(), "Can't send message to MTA");
          }
          multi.requestDone();
        }
      } else if (request.getResultHandler() != null) {
        request.getResultHandler().retry(request.getId(), "Can't send message to MTA");
      }
    }
  }


  /**
   * stackTrace puts the stack trace of a Throwable into a StringBuffer
   * suitable for logging to file.
   *@param e the Throwable.
   *@return a StringBuffer with the stack trace in.
   */
  public static StringBuffer stackTrace(Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    return sw.getBuffer();
  }

  /**
   * The queue of requests (for this unit) towards mail transfer agent
   */
  private class RequestQueue {
    private ManagedArrayBlockingQueue<Request> requestQueue;

    /**
     * Creates a new <code>RequestQueue</code> instance.
     *
     * @param queueSize an <code>int</code> size of queue
     */
    public RequestQueue(int queueSize) {
      requestQueue = new ManagedArrayBlockingQueue<Request>(queueSize);
    }
    /**
     * Expands queue with object
     *
     * @param r a <code>Request Object</code> value
     * @throws InterruptedException exception
     */
    public void expand(Request r) throws InterruptedException {
      requestQueue.put(r);
    }
    /**
     * Returns next object
     *
     * @param maxTime wait this number of milliseconds
     * @return an <code>Object</code> value
     * @throws InterruptedException exception
     */
    public Request getNextObject(int maxTime) throws InterruptedException {

       Request r = requestQueue.poll(maxTime,TimeUnit.SECONDS);
       return r;

    }

    /**
     * Put object on queue
     *
     * @param r a <code>Request</code> value to queue.
     * @param maxTime an <code>int</code> time in seconds to wait.
     * @return a <code>boolean</code> value return true
     * @throws InterruptedException exception
     */
    public boolean putObject(Request r, int maxTime) throws InterruptedException {
       return requestQueue.offer(r,maxTime);
    }
    /**
     * Clear Q
     */
    public void clear() {
      requestQueue.clear();
    }
    /**
     * Is it empty?
     * @return empty
     */
    public  boolean isEmpty() {
      return requestQueue.isEmpty();
    }

    /**
     * Get the Q size
     * @return Q size
     */
    public  int getSize() {
      return requestQueue.size();
    }
    
    /**
     * Check q is idle for period of time
     * @param time amount of time
     * @param unit of time to check i.e. seconds..
     * @return true if idle
     */
    public boolean isIdle(int time, TimeUnit unit) {
        return requestQueue.isIdle(time, unit);
    }
    
    /**
     * wait q empty for specified time
     * @param time amount of time
     * @param unit of time to check i.e. seconds..
     * @return true if empty at end of period.
     */
    public boolean waitNotEmpty(int time, TimeUnit unit)  {
        return requestQueue.waitNotEmpty(time, unit);
    }
  }

  
  /**
   * The shutdown loop stops after the ntfRun method is finished.
   *
   * @return true when ok to shutdown
   */
  @Override
  public boolean shutdown() {
      if (isInterrupted()) {
          return true;
      } //exit immediately if interrupted..

      if (queue.getSize() == 0)
      {
          //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
          if (queue.isIdle(2,TimeUnit.SECONDS)) {
              return true;
          }
          else
          {
              if (queue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                  return(ntfRun());
              } else
              {
                  return true;
              }

          }
      } else {
          return(ntfRun());
      }
  }
}
