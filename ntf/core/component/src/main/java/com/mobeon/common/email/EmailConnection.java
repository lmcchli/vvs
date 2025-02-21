/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.LogLevel;
import com.abcxyz.messaging.oe.common.util.KPIProfiler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.email.request.*;
import com.mobeon.ntf.util.threads.NtfThread;
import com.mobeon.ntf.util.time.NtfTime;


import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.Session;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.SendFailedException;

import org.eclipse.angus.mail.smtp.SMTPMessage;
import org.eclipse.angus.mail.smtp.SMTPTransport;



/**
 * The EmailConnection keeps the Session object and
 * handles the actual sending of emails.
 *
 */
public class EmailConnection extends NtfThread
implements com.mobeon.common.email.ConnectionStateListener {

    /**
     * Return value <code>SEND_OK</code> does not guarantee delivery.
     */
    public static final int SEND_OK = 0;
    /**
     * Return value <code>SEND_FAILED</code> indicates no use retrying.
     */
    public static final int SEND_FAILED = 1;
    /**
     * <code>SEND_FAILED_RETRY</code> indicates temporary failure, 
     * retrying might be useful
     */
    public static final int SEND_FAILED_RETRY = 2;

    private boolean breakUp = false;
    private EmailUnit unit;
    private LogAgent log;
    /** Connection id */
    public static int id = 0;
    private Session session;
    private SMTPTransportWrapper smtpTransportWrapper = null;
    private int lastRequestTime;
    private EmailConfig config;
    

    // KPI; we will not include every log as it may be too much/frequent
    // instead we'll do sampling every second
    public static final long KPI_SAMPLE_INTERVAL = 1000; // every 1 second  
    private static long lastSampleTime = System.currentTimeMillis();

    /**
     * Creates a new <code>EmailConnection</code> instance.
     *
     * @param unit an <code>EmailUnit</code> 
     * @param config <code>EmailConfig</code>
     */
    public EmailConnection(EmailUnit unit, EmailConfig config) throws MessagingException {
        super("EmailConn-" + unit.getInstanceName() + "-" + nextId());
        this.unit = unit;
        log = unit.getLog(); 
        unit.getConfig();
        Properties props = new Properties();
        props.put("mail.smtp.host", unit.getHost());
        props.put("mail.smtp.port", "" + unit.getPort());
        session = Session.getInstance(props);
        this.config = config;

        try {
            smtpTransportWrapper = new SMTPTransportWrapper(session,unit.getTimeout());
            smtpTransportWrapper.connect();
            connectionUp("Connected transport");
            lastRequestTime = NtfTime.now;
            start();
        } catch (MessagingException m) {
            log.info("Unable to connect transport <" + m + ">");
            connectionDown("Failed transport connection");
	    throw m;
        }      
    }
    
    /* (non-Javadoc)
     * @see com.mobeon.ntf.util.threads.NtfThread#ntfThreadInit()
     * Check on initial thread startup, if still connected..
     */
    public void ntfThreadInit() {
        if (smtpTransportWrapper == null || !smtpTransportWrapper.isConnected()) {
            connectionDown("Not connected.");
            return;
        }
    }

    /**
     * Get a new id
     * @return an id 0-99
     */
    private static synchronized int nextId() {
        ++id;
        if (id > 99) {
            id = 0;
        }
        return id;
    }

    /**
     * Unused so far
     * @throws InterruptedException exception
     */
    private void sendMultiRequest(MultiRequest request) throws InterruptedException {
        int result = SEND_OK;
        while (request.getCount() > 0) {
            Request sendRequest = request.getNextRequest();
            result = sendMessage(sendRequest);
            if (result == SEND_OK) {
                sendRequest.getResultHandler().ok(sendRequest.getId());
            } else if (result == SEND_FAILED_RETRY) {
                break;
            }
            request.requestDone();
        }
        if (result == SEND_FAILED_RETRY) {
            unit.sendFailed(request);
        } 
    }

    /**
     * Does the actual sending of the request
     *
     * @param request a <code>Request</code> value
     * @return an <code>int</code> value (SEND_FAILED_RETRY or SEND_OK)
     */
    private int sendMessage(Request request) {
    	Object cpo = null; // KPI Check Point Object
    	
    	if (log.isEnabledFor(LogLevel.TRACE)) {
    		log.trace("sendMessage() request: " + request.toString());
    	}
        try {
            if (request == null | session == null) {
                log.error("EmailNotification failed to send message (request or session null)");
                return SEND_FAILED_RETRY;
            }
            if (request instanceof EmailRequest) {
               	////////////////// KPI
            	long currentTime = System.currentTimeMillis();
            	if (KPIProfiler.isStatsEnabled() && (currentTime - lastSampleTime >= KPI_SAMPLE_INTERVAL)) {
            		lastSampleTime = currentTime; 
            		cpo = KPIProfiler.enterCheckpoint(KPIProfiler.KPI_NAME_SMTP_OUTGOING_TIME, KPIProfiler.KPI_DISPLAY_SMTP_OUTGOING_TIME);
            	}
            	//////////////////
                SMTPMessage s = new SMTPMessage(request.getEmailMessage(session));
                s.setEnvelopeFrom(((EmailRequest) request).getEnvelopeFrom()); // No bounce if empty        
                if (((EmailRequest) request).getRcpTo() != null) {
                    // Use other version of send
                    InternetAddress[] ia = { new InternetAddress(((EmailRequest) request).getRcpTo()) };
                    smtpTransportWrapper.sendMessage(s, ia);                     
                } else {
                    //transport.send(s);
                    // compare performance with transport.send 124 or 120 sec/500 req
                    smtpTransportWrapper.sendMessage(s, s.getAllRecipients());
                }
            } 
        } catch (MessagingException me) {
            log.error("Messaging Exception while SendMessage: ",me);        
            return SEND_FAILED_RETRY;
        } finally {
        	if (cpo != null) KPIProfiler.exitCheckpoint(cpo);
        }
        if (request instanceof EmailRequest) {
            request.getResultHandler().ok(request.getId());
        }
        return SEND_OK;
    }


    /**
     * Connection thread
     */
    public boolean ntfRun() {

        if (breakUp)
        {
            log.info("Exiting thread " + this.getName());

            try {
                smtpTransportWrapper.close(); // Clean exit
            } catch (Exception e) {
                ;
            }
            connectionDown("Shutting down, breakup set.");
            return true;
        }

        Request request = null;
        try {
            request = unit.waitForRequest();

            if (request == null) {
                if (lastRequestTime >= NtfTime.now + config.getEmailPollInterval()+5) {
                    log.info("EmailConnection timed out waiting for requests");

                    // Close the session and exit thread..
                    smtpTransportWrapper.close();
                    connectionDown("Timeout");
                    return true;
                } else
                {
                    return false; //go to check management status..
                }
            } else if (request instanceof PollRequest) {

                log.debug("Sending poll request to " + unit.getHost());

                // Service.isConnected will do an SMTP NOP--> 
                // and hopefully receive an <-- OK
                if (smtpTransportWrapper.isConnected()) {
                    connectionUp("PollRequest Connected");
                } else {
                    connectionDown("PollRequest Disconnect");
                    smtpTransportWrapper.close();
                }
            } else if (request instanceof MultiRequest) {  
                sendMultiRequest((MultiRequest) request);
            } else {          // We have an ordinary EmailRequest!
                if (request instanceof EmailRequest) {
                    int result = sendMessage(request);
                    if (result == SEND_FAILED_RETRY) {
                        request.getResultHandler().retry(request.getId(),
                                "Can't send message to Mail Transfer Agent");
                        connectionDown("Send Exception");
                    } else {
                        connectionUp("Sent ok");
                    }
                }
            }
        } catch (InterruptedException i) {
            return true; //exit...
        }
        catch (Exception e) {
            log.error("Exception getting EmailConnection: ",e);
            if ((request != null) && (request.getResultHandler() != null)) {
                request.getResultHandler().retry(request.getId(), "Send Exception");
            }
            connectionDown("Send Exception");
            return true;
        }
        lastRequestTime = NtfTime.now;
        return false;
    }


    // Impl the ConnectionStateListener
    public void connectionUp(String name) {
        unit.connectionUp();
    }

    public void connectionDown(String name) {
        try {
            unit.connectionDown(this);
        } catch (Exception e) {
            log.error("Unexpected exception ",e);
        }
        breakUp = true;
    }

    /**
     *
     */
    @Override
    public boolean shutdown() {
        if (unit.getQueueSize() == 0)
        {
            try {
                sleep(1000); //grace period to allow sender threads to drain there queues.
            } catch (InterruptedException e) {
                return true; //exit...
            }
            if (unit.getQueueSize() == 0)
                return true;
        }
        while (unit.getQueueSize() > 0 && !Thread.interrupted() ) //drain queue as long as we are not forced to shutdown.
        {
            if (ntfRun() == true ) {return true;} // ntfRun tells us to exit now.
        }
        return false;
    }

    /**
     * @author lmcmajo
     * A wrapper for SMTPTtansport that timeouts instead of hanging
     * by using Futures/Executor
     */
    private class SMTPTransportWrapper {

    	private static int SMTP_TIMEOUT_MS = 15000; //Milliseconds to timeout with a SMTP command.
    	private static int SMTP_SEND_TIMEOUT_MS = 60000; //allow LONGER for a send as they can be long.
    	private SMTPTransport transport = null;
    	private Session session = null;
    	private MessagingException msgex = null;
    	private Boolean isConnected = false; //not connected at start.
    	private static LogAgent logger = NtfCmnLogger.getLogAgent(SMTPTransportWrapper.class);
    	
    	ExecutorService transportExecutor = Executors.newCachedThreadPool();

    	/**
    	 * Constructor
    	 * @param _ session the Session to wrap,
    	 * @param timeout - timeout in milliseconds
    	 */
    	public SMTPTransportWrapper(Session _session,int timeout) {    		
    		session = _session;
        	SMTP_TIMEOUT_MS = timeout; //SECONDS to timeout with a SMTP command.
        	if (SMTP_TIMEOUT_MS < 60000 ) {
        		//allow LONGER for a send as they can be long if big voice mail and slow connection
        		SMTP_SEND_TIMEOUT_MS = 60000; 
        	} else {
        		SMTP_SEND_TIMEOUT_MS=timeout;
        	}
    		//do not wait for the QUIT response, makes less likely to hang on close()
    		session.getProperties().put("mail.smtp.quitwait","false"); 
    		try {
    			transport = (SMTPTransport) session.getTransport("smtp");
    		} catch (NoSuchProviderException e) {
    			logger.error("Exception:",e);
    			transport=null;
    		}
    	}

    	public void close() throws MessagingException {
    		if ( transport == null ) {
    			logger.debug("close() Connection already closed");
    			return;
    		}
    		
    		msgex = null;

    		Callable<Boolean> closetask = new Callable<Boolean>() {
    			public Boolean call() {
    				try {   					
    					transport.close();
    					logger.info("close() Connection closed normally");
    					return true;
    				} catch (MessagingException e) {
    					msgex=e;
    					logger.trace("Connection closed abnormally: ",e);

    				} finally {
    					isConnected=false;
    					transport=null;
    				}
    				return false;
    			}
    		};


    		Future<Boolean> closeFuture = transportExecutor.submit(closetask);
    		try {
    			closeFuture.get(SMTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    		} catch (TimeoutException ex) {
    			msgex=new MessagingException("Timeout on close.");
    			logger.warn("Timeout disconnecting, forced closed");
    		} catch (Exception e) {
    			msgex = new MessagingException("Caused by: " + e.getMessage(),e);
    			logger.error("Unexpected exception when closing connection: " + e.getMessage() );
    		} finally {
    			closeFuture.cancel(true);    		   
    		}
    		transport = null;
    		isConnected=false;
    		if (msgex != null) {
    			throw msgex;   			
    		}
    	}

    	public void sendMessage(SMTPMessage s, Address[] allRecipients) throws SendFailedException, MessagingException {
    		if ( !isConnected || transport == null ) {
    			throw new SendFailedException("Not connected"); 
    		}
    		
    		msgex = null;

    		Callable<Boolean> sendMessageTask = new Callable<Boolean>() {
    			public Boolean call() {
    				try {
    					transport.sendMessage(s,allRecipients);
    					if (logger.isDebugEnabled()) {
    						logger.debug("Succesfully sent Message");
    					}
    					return true;
    				} catch (SendFailedException sf) {
    					logger.warn("Failed due to SendFailedException");
    					msgex=sf;
    					return false;
    				} catch (MessagingException me) {
    					logger.warn("Failed due to MessagingException");
    					msgex=me; 
    					return false;
    				}
    			}
    		};
    		   		
    		Future<Boolean> sendMessageFuture = transportExecutor.submit(sendMessageTask);
    		try {
    			sendMessageFuture.get(SMTP_SEND_TIMEOUT_MS, TimeUnit.MILLISECONDS); 
    		} catch (TimeoutException ex) {
    			msgex= new MessagingException("Timeout",ex);
    			logger.warn("Timeout on sendMessage");
    		} catch (Exception e) {
    			msgex= new MessagingException("Caused by: " + e.getMessage(),e);
    		} finally {
    			sendMessageFuture.cancel(true);
    		}
    		if (msgex != null) {
    			try {
					close(); //force closed
					isConnected=false;
					transport=null;
				} catch (MessagingException e) {
					//ignore;
				}
    			throw msgex;   			
    		}
    	}

    	public boolean isConnected() {
    		if ( !isConnected || transport == null ) return false; //prevents waiting for reply if already known disconnected.    		
    		
    		msgex = null; 
    		
    		Callable<Boolean> connectedTask = new Callable<Boolean>() {
    			public Boolean call() {
    				  
    				isConnected = transport.isConnected(); //Sends a NOP and waits for a reply
    				return isConnected;
    			}
    		};
    		
    		Future<Boolean> connectedFuture = transportExecutor.submit(connectedTask);
    		try {
    			connectedFuture.get(SMTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    		} catch (TimeoutException ex) {
    			isConnected=false;
    			logger.warn("Timeout on isConnected, will force closed");
    			try {
					close(); //force closed
					isConnected=false;
					transport=null;
				} catch (MessagingException e) {
					//ignore;
				}
    		} catch (Exception e) {
    			logger.warn("Exception while checking isConnected, will force closed ",e);
    			isConnected=false;
    			transport=null;
    			try {
					close(); //force closed
					isConnected=false;
					transport=null;
				} catch (MessagingException i) {
					//ignore;
				}
    		} finally {
    			connectedFuture.cancel(true);
    		}   		   		
    		return isConnected;
    	}

    	public synchronized void connect() throws MessagingException {
    		if (isConnected()) return;
    		
    		msgex = null;
    		
    		if (transport == null) {
    			transport = (SMTPTransport) session.getTransport("smtp");
    		}
    		
    		ExecutorService connectExecutor = Executors.newCachedThreadPool();
    		Callable<Boolean> task = new Callable<Boolean>() {
    			public Boolean call() {
    				try {
    					transport.connect(); //sends EHLO and waits for a response
    					isConnected=true;
    					logger.info("Succesfully connected");
    				} catch (MessagingException e) {
    					msgex=e;
    					try {
    						close(); ///make sure cleaned up.
    						isConnected=false;
    					} catch (MessagingException e1) {
    						//ignore
    						isConnected=false;
    						transport=null;
    					} 
    				}
    				return isConnected;
    			}    			
    		};
    		
    		Future<Boolean> connectFuture = connectExecutor.submit(task);
    		try {
    			connectFuture.get(SMTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    		} catch (TimeoutException ex) {
    			msgex= new MessagingException("Timeout",ex);
    			logger.warn("Timeout on connect()");
    			close(); ///make sure cleaned up.

    		} catch (Exception e) {
    			msgex= new MessagingException("caused by: " + e.getMessage(),e);
    			logger.warn("Exception on connect() ",e);
    			try {
    				close(); ///make sure cleaned up.
    			} catch (Exception i) {
    				//ignore
    			}

    		} finally {
    			connectFuture.cancel(true);
    		}
    		if (msgex != null) {
    			logger.warn("Exception on connect()");
    			throw msgex;   			
    		}
    	}
    }
}

