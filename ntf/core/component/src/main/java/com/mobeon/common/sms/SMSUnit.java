/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */

package com.mobeon.common.sms;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.Logger;
import com.mobeon.common.smscom.SMSCom;
import com.mobeon.common.smscom.charset.Converter;
import com.mobeon.common.smscom.*;
import com.mobeon.common.sms.request.*;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.out.sms.SMSConfigWrapper;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.threads.NtfThread;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;

/**
 *SMSUnit represents one smsc.
 *One SMSUnit can have several SMSConnections.
 */
public class SMSUnit extends NtfThread {


    // In progress connection timeout default value (milliseconds)
    private static final int INPROGRESS_CONNECTION_TIMEOUT = 120000;


    private static final String CONFIGURED_SMPP_BINDING_TRANSCEIVER = "transceiver";
    private static final String CONFIGURED_SMPP_BINDING_TRANSMITTER_RECEIVER = "transmitter_receiver";
    private static final int NUM_CONNECTIONS_CHECK_INTERVAL = 1000;

    /** A queue with requests */
    private SMSQueue queue;

    private SMSUnit backupSMSUnit;

    /** All working sender connections (connections that send to SMSC and may or may not receive from SMSC) */
    private volatile Vector<SMSConnection> connections; //Use vector as thread safe.
    /** All working receiver-only connections (connections that only receive from SMSC) */
    private volatile Vector<SMSConnection> receiverOnlyConnections;
    
    /** Track the time elapse since we initiated a receiver-only reconnection so we know when it's time to raise an alarm if it has not succeeded. */ 
    int reconnectingReceiverTime = -1;
    /** Track the time elapse since we initiated a sender reconnection so we know when it's time to raise an alarm if it has not succeeded. */ 
    int reconnectingSenderTime = -1;
    
    /** Used to indicate the number of idle sender connections that are in the process of disconnecting but is still in connections vector */
    private AtomicInteger numIdleConnectionsDisconnecting = new AtomicInteger(0);
    /** Used to indicate the number of idle receiver-only connections that are in the process of disconnecting but is still in receiverOnlyConnections vector */
    private AtomicInteger numIdleReceiverOnlyConnectionsDisconnecting = new AtomicInteger(0);
    private int logDebugCount = 0; //used to print number of connections every so many iterations

    /* the protocol for the smsc, (SMPP or CIMD2) */
    private String protocol;

    /** A converter used to translate messages to protocol specific messages. */
    private Converter converter;

    private Logger log;
    private SMSConfig config;

    /** The host of the smsc */
    private String host;

    /* the port of the smsc */
    private int port;

    /** Name of the smsc */
    private String instanceName;

    /** where to send connection status callbacks */
    private ConnectionStateListener connectionStateListener = null;
    
    private AtomicBoolean isUnavailableForNewRequests = new AtomicBoolean(false);

    /** Indicates if this unit is unbinding from the SMSC gracefully or ungracefully.
     *  Currently, graceful shutdown means an attempt is made to process as many queued requests possible before the shutdown grace period expires. 
     *  An ungraceful shutdown means no attempt is made to process the queued requests. */
    protected static enum ShutdownType {
        NONE,
        GRACEFUL,
        UNGRACEFUL;        
    }
    
    /** Indicates if this unit is unbinding from the SMSC; if so, indicates if gracefully or ungracefully. */
    private ShutdownType shutdownType = ShutdownType.NONE;
    
    /** Indicates if the shutdown grace period expired. */
    private boolean isShutdownTimedOut = false;
     
    /** Indicates if the queued requests is in the process of being discarded */
    private boolean isDiscardingQueuedRequests = false;
        
    
    /* Class to make sure only one connection can open at a time
     * synchronized to ensure only one can grab this at a time.
     * 
     * After a delay, if for some reasons the connection in progress is not
     * reset, this class will automatically resets the progress status. This is 
     * necessary when some threads that should reset this variable die before
     * being able to. 
    */
    private class ConnnectionInProgress
    {
    	private boolean inProgress = false;
    	private boolean isConnectingReceiverOnly = false;
    	private Long autoResetTime = 0L;

		synchronized public boolean check() {
            if (0L < autoResetTime && autoResetTime < System.currentTimeMillis()) {
                // Connection is in progress for too long
                log.logString("Resetting connection in progress status for SMSUnit " + 
                        instanceName + 
                        " because it has been trying to connect for too long.", 
                        Logger.LOG_DEBUG);
                reset();
            }
			return inProgress;
		}

		//You must set this and check to see if you set it at the same
		// time to make sure your thread is the only one to set it.
		synchronized public boolean set(boolean isConnectingReceiver) {
			if (!check()) {
			    /*
			     * Connection in progress is reset after 2 times the minimum time between
			     * connections if it is not cleared by a connection. 
			     * 
			     * The minimum time between connections is calculated using the maximum of 
			     * time between connection and reconnection configuration parameters.
			     *  
			     * If these parameters are 0, the connection in progress is reset after 
			     * 2 minutes if it is not cleared by a connection.
			     */
				inProgress = true;
				this.isConnectingReceiverOnly = isConnectingReceiver;
				int connectionRetry = config.getSmsMinTimeBetweenConnections();
				int reconnectionRetry = config.getSmsMinTimeBetweenReConnections();
				int retryTime = Math.max(connectionRetry, reconnectionRetry);
				int connectionInProgressResetTime = retryTime <= 0 ? INPROGRESS_CONNECTION_TIMEOUT : retryTime * 1000 * 2;  
                autoResetTime = System.currentTimeMillis() + connectionInProgressResetTime;
                log.logString("Connection in progress timeout set to " + connectionInProgressResetTime + 
                        "ms for SMSUnit " + instanceName, Logger.LOG_DEBUG);
				return true;
			}
			else {
				return false;
			}
		}

		synchronized public void reset() {
			inProgress = false;
			autoResetTime = 0L;
		}
    }

    /** Do not create new connections if true */
	private ConnnectionInProgress connnectionInProgress = new ConnnectionInProgress();

    private String userName = null;
    private String password = null;
    private String systemType = null;

	private static int send_count = 0;

	//The next time smsunit is allowed to reconnect since the last reconnection.
	private Long nextAllowedReconnectTime = 0L;

	/* The next time smsunit is allowed to allow a new connection.
	 * Used to limit the speed of connections ramping up.
	 */
	private Long nextTimeToAllowNewConnection = 0L;


    /**
     * Creates a new instance of SmsUnit.
     *
     */
    public SMSUnit(Logger log, SMSConfig config, String smsc, ConnectionStateListener listener) throws SMSUnitException {
        super("SMSUnit-" + smsc);
        Map<String, String> shortMessageComponent = null;

        try {
            shortMessageComponent = Config.getExternalEnabler(NotificationConfigConstants.SHORT_MESSAGE_TABLE, smsc);
            if (shortMessageComponent != null) {
                this.port = Integer.parseInt(shortMessageComponent.get(NotificationConfigConstants.PORT));
                this.protocol = shortMessageComponent.get(NotificationConfigConstants.PROTOCOL);
                this.host = shortMessageComponent.get(NotificationConfigConstants.HOST_NAME);
                this.userName = shortMessageComponent.get(NotificationConfigConstants.USER_NAME);
                this.password = shortMessageComponent.get(NotificationConfigConstants.PASSWORD);
                this.systemType = shortMessageComponent.get(NotificationConfigConstants.SYSTEM_TYPE);

                instanceName = smsc;
                queue = new SMSUnit.SMSQueue(config.getSMSQueueSize());
                connections = new Vector<SMSConnection>();
                receiverOnlyConnections = new Vector<SMSConnection>();
                this.connectionStateListener = listener;
                this.log = log;
                this.config = config;
            } else {
                throw new SMSUnitException("Smsc " + smsc + " not found in ShortMessage.Table config");
            }
        } catch (NumberFormatException e) {
            throw new SMSUnitException("Port is not numeric for service <ShortMessage> with component name: " + smsc);
        }

        log.logString("SMSUnit " + instanceName + " building character conversion tables", Logger.LOG_DEBUG);

        File dir = new File(config.getCharConvPath());

        File charFile = new File(dir, "charconv." + getProtocol().toLowerCase());

        if (!charFile.canRead()) {
            charFile = new File(dir, "charconv.cfg");
        }
        log.logString("Using character conversion file " + charFile.toString(), Logger.LOG_DEBUG);
        Properties props = new Properties();

        if (!charFile.canRead()) {
            log.logString("Could not find character conversion file", Logger.LOG_ERROR);
        } else {
            try {
                props.load(new BufferedInputStream(new FileInputStream(charFile)));
            } catch (IOException e) {
                log.logString("Unexpected: " + e + ". Using default values", Logger.LOG_ERROR);
            }
        }
        try {
            converter = Converter.get(props);
        } catch (SMSComConfigException e) {
            log.logString("Bad char conversion file: " + e, Logger.LOG_ERROR);
        }
        if (converter == null) {
            throw new SMSUnitException("Unable to convert character conversion file ");
        }
        log.logString("SMSUnit " + instanceName + " character conversion tables built", Logger.LOG_DEBUG);

        //At this point, the configured backup might not be known as allowed/available but try anyway.
        //If the backup is not set now, another attempt to set the backup will be done once the SMSClient has added all allowed/available SMS units.
        addBackup();
        start();
    }
    
    /**
     * This method is called once, when the thread is started, before the loop
     * that calls ntfRun begins.
     */
    public void ntfThreadInit() {
        //Setup initial connections: bring up 1 receiver-only connection (if used) and 1 sender connection to just establish communication with SMSC.
        //Let the methods to maintain number of sender/receiver connections bring up the rest of the connections.
        try {
            int minTimeBetweenFirstConnections = config.getSmsMinTimeBetweenConnections()*1000;
            if(minTimeBetweenFirstConnections == 0) {
                //If minimum time between connections is not configured, 
                //use 3 seconds which was the time for waiting for a connection to come up during reconnection (previous implementation).
                minTimeBetweenFirstConnections = 3000;
            }

            if(isUsingReceiverOnlyConnections()) {
                addReceiverConnection(false);
                sleep(minTimeBetweenFirstConnections);
            }

            addConnection(false);
            sleep(minTimeBetweenFirstConnections);
        } catch (Exception e) {
            log.logString("SMSUnit.run: Unexpected exception in SMSUnit(run), " + SMSCom.stackTrace(e), Logger.LOG_ERROR);
        }
    }
    

        
    /**
     * The run method ensures that there is always the configured minimum number of connections. 
     */
    public boolean ntfRun() {
                
        //Ensure that there is always at least the configured minimum number of connections.  
        
            try {
                if (isBiengShutDownViaClient()) {
                    log.logString("SMSUnit.run: Shutting down, removed from config?" + instanceName, Logger.LOG_DEBUG );
                    return true;
                }
                if (logDebugCount++ == 60) { //log number of connections every minute
                    log.logString("SMSUnit.run: " + instanceName + " number of receiver connections: " + receiverOnlyConnections.size() + "; number of sender connections: " + connections.size(), Logger.LOG_DEBUG);
                    logDebugCount = 0;
                }

                //Check receiver-only connections if applicable.
                if(isUsingReceiverOnlyConnections()) {
                    maintainNumReceiverOnlyConnections();
                }
                
                //Check sender connections.
                maintainNumSenderConnections();
                
                sleep(NUM_CONNECTIONS_CHECK_INTERVAL);
            }  catch (InterruptedException i) {
                return true; //exit immediately, NTF is exiting..
            }
            catch (OutOfMemoryError me) {
                try {
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                    log.logString("NTF out of memory, shutting down... " + SMSCom.stackTrace(me), Logger.LOG_ERROR);
                } catch (OutOfMemoryError me2) {;} //ignore second exception
                return true; //exit.
                
            } catch (Exception e) {
                log.logString("SMSUnit.run: Unexpected exception, " + SMSCom.stackTrace(e), Logger.LOG_ERROR);
            }
            return false;
    }
    
    
    /**
     * The shutdown loop stops when no more activity in queue for more than
     * 3 seconds.
     *
     * @return true when shutdown.
     */
    public boolean shutdown() {
        //attempt to wait for queue to empty. Until forced to shutdown.
        boolean exit = false;
        if (!isInterrupted() )
        {
            if (getDelayedSize() > 0)
            {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    exit=true;
                }
            } else if ( !isIdle(3000) ) {
                if ( queue.waitNotEmpty(3000)) {
                   ntfRun(); //keep connections up until queue is idle or forced to exit.
                   exit=false; // still active don't shutdown yet.
                } 
            } else {
                return true;
            }
        } else {
            exit=true;
        }
        
        if (exit)
        {
            // Complete shutdown...
            /* note : connections shut themselves down on management shutdown... */
            discardQueuedRequests();
        }
        return exit;        
    }
    
    private void maintainNumReceiverOnlyConnections() {
        //If in the process of reconnecting to SMSC, check if outcome was successful or we need to raise an alarm.
        if(reconnectingReceiverTime != -1) {
            //Wait a few seconds for connection to come up before raising an alarm (3 seconds from previous implementation).
            if(receiverOnlyConnections.isEmpty()) {
                if(reconnectingReceiverTime >= 3000) {
                    connectionStateListener.connectionDown(instanceName);
                    reconnectingReceiverTime = -1;
                    log.logString("SMSUnit.maintainNumReceiverOnlyConnections: Failed receiver-only reconnection to " + instanceName, Logger.LOG_DEBUG);
                } else {
                    reconnectingReceiverTime += NUM_CONNECTIONS_CHECK_INTERVAL;
                }
            } else {
                //Reconnection was successful.
                reconnectingReceiverTime = -1;
                log.logString("SMSUnit.maintainNumReceiverOnlyConnections: Successful receiver-only reconnection to " + instanceName, Logger.LOG_DEBUG);
            }
        //If no receiver-only connections, initiate reconnection to SMSC.    
        } else if(receiverOnlyConnections.isEmpty()) {
            if(reconnectReceiver()) {
                //The reconnection was initiated but the outcome is not yet know; start a timer to wait for outcome.
                reconnectingReceiverTime = 0;
                log.logString("SMSUnit.maintainNumReceiverOnlyConnections: Initiated receiver-only reconnection to " + instanceName, Logger.LOG_DEBUG);
            } else {
                //Unable to initiate reconnection, raise an alarm.
                connectionStateListener.connectionDown(instanceName);
                log.logString("SMSUnit.maintainNumReceiverOnlyConnections: Failed to initiate receiver-only reconnection to " + instanceName, Logger.LOG_DEBUG);
            }
        //Having at least one sender connection has priority over increasing number of receiver-only connections to configured number.    
        } else if(!connections.isEmpty() && receiverOnlyConnections.size() < Config.getSmsNumReceiverConn()) {
            log.logString("SMSUnit.maintainNumReceiverOnlyConnections: Adding receiver-only connection for " + instanceName + " since number of connections (" +  receiverOnlyConnections.size() +
                    ") is less than the configured number of connections (" + Config.getSmsNumReceiverConn() + ")", Logger.LOG_DEBUG );
            addReceiverConnection(false);
        }
    }
    
    private void maintainNumSenderConnections() {
        //If in the process of reconnecting to SMSC, check if outcome was successful or we need to raise an alarm.
        if(reconnectingSenderTime != -1) {
            //Wait a few seconds for connection to come up before raising an alarm.
            if(connections.isEmpty()) {
                if(reconnectingSenderTime >= 3000) {
                    log.logString("SMSUnit.maintainNumSenderConnections: Failed sender reconnection to " + instanceName, Logger.LOG_DEBUG);
                    connectionStateListener.connectionDown(instanceName);
                    reconnectingSenderTime = -1;
                    //Discard queue (sending retry result) since unable to reconnect.
                    discardQueuedRequests();
                } else {
                    reconnectingSenderTime += NUM_CONNECTIONS_CHECK_INTERVAL;
                }
            } else {
                //Reconnection was successful.
                reconnectingSenderTime = -1;
                log.logString("SMSUnit.maintainNumSenderConnections: Successful sender reconnection to " + instanceName, Logger.LOG_DEBUG);
            }
        //If no sender connections, initiate reconnection to SMSC.    
        } else if(connections.isEmpty()) {
            if(reconnect()) {
                //The reconnection was initiated but the outcome is not yet know; start a timer to wait for outcome.
                reconnectingSenderTime = 0;
                log.logString("SMSUnit.maintainNumSenderConnections: Initiated sender reconnection to " + instanceName, Logger.LOG_DEBUG);
            } else {
                //Unable to initiate reconnection, raise an alarm.
                log.logString("SMSUnit.maintainNumSenderConnections: Failed to initiate sender reconnection to " + instanceName, Logger.LOG_DEBUG);
                connectionStateListener.connectionDown(instanceName);
            }
        //Having at least one receiver-only connection has priority over increasing number of sender connections to configured minimum.
        } else if(!(isUsingReceiverOnlyConnections() && receiverOnlyConnections.isEmpty())){
            if(connections.size() < Config.getSmsMinConn()) {
                log.logString("SMSUnit.maintainNumSenderConnections: Adding sender connection for " + instanceName + " since number of connections (" +  connections.size() +
                        ") is less than the configured minimum number of connections (" + Config.getSmsMinConn() + ")", Logger.LOG_DEBUG );
                addConnection(false);
            } 
                 
            //if the buffer is larger than 1/4 and we have not reached connection limit then attempt to add a connection.
            if (queue.getSize() > 0 && queue.getSize() >= config.getSMSQueueSize() / 4 && connections.size() < config.getSmsMaxConnections()) {
                log.logString("SMSUnit.maintainNumSenderConnections: Increased traffic load; initiating sender connection to " + instanceName, Logger.LOG_DEBUG);
                addConnection(false);
            }                    
        }
    }

    /**
     * @return true if this SMS unit is using connections that only receive requests from the SMSC
     */
    private boolean isUsingReceiverOnlyConnections() {
        return protocol.equalsIgnoreCase("SMPP") && CONFIGURED_SMPP_BINDING_TRANSMITTER_RECEIVER.equalsIgnoreCase(Config.getSmppBinding());
    }
    
    /**
     * @return true if this SMS unit is using connections that sends and receives requests from the SMSC
     */
    private boolean isSenderConnectionsAlsoReceiving() {
        return !protocol.equalsIgnoreCase("SMPP") || CONFIGURED_SMPP_BINDING_TRANSCEIVER.equalsIgnoreCase(Config.getSmppBinding());
    }
    
    /**
     * Creates a new connection that will not send short message requests but only receives from the SMSC.
     * <p>
     * rules:
     * Only if this SMS unit is not shutting down
     * Only start a new connection if one is not already in progress
     * Only allow a new connection every minimum time between connections
     * Only allows up to the maximum SMS receiver connections
     * <p>
     * If retry is indicated and current connections are not in progress
     * and number of connections is 0, will always attempt to connect.
     * <p>
     * Retry is used to override the minimum time between connections when
     * reconnecting after an error.  Usually at least one connection is
     * always up.  During high traffic more connection can be added.
     * <p>
     * See reconnect.
     *
     * Returns false if it attempted to connect and failed, true otherwise.
     */
    private boolean addReceiverConnection(boolean retry) {

        if(isBiengShutDownViaClient()) {
            log.logString(instanceName + " is shutting down; will not add receiver-only connection", Logger.LOG_DEBUG);
            return false;
        }
        
        if (!connnectionInProgress.set(true)) {
            //Connection is in progress; if it is for a receiver-only connection, return true to not send an alarm yet.
            return connnectionInProgress.isConnectingReceiverOnly;
        }

        long currentTime = System.currentTimeMillis();

        /*
         * Connect if we have no exceeded maximum connections and we
         * are within the minimum time to add a new connection or
         * if a retry is indicated and no connections.
         */
        if (receiverOnlyConnections.size() < Config.getSmsNumReceiverConn() && ( (retry && receiverOnlyConnections.isEmpty()) || (currentTime > nextTimeToAllowNewConnection)) ) {
            log.logString(instanceName + " adding receiver-only connection", Logger.LOG_DEBUG);
            SMSConnection conn = new SMSConnection(this, false, true);

            log.logString("SMSUnit.addReceiverConnection: Adding new receiver-only connection " + instanceName + "; current receiverOnlyConnections: " + receiverOnlyConnections.size(), Logger.LOG_DEBUG);
            if (conn.connect() == true) {
                nextTimeToAllowNewConnection = System.currentTimeMillis() + config.getSmsMinTimeBetweenConnections()*1000;
                return true;
                //note: the connection itself will clear the in progress flag when it has bound, or if fails.
                //This is to prevent several binding connections outstanding.
            } else {
                connnectionInProgress.reset();
                log.logString(instanceName + " Unable to connect receiver-only!", Logger.LOG_ERROR);
                return false;
            }
        }
        connnectionInProgress.reset();
        return true;
    }

    /**
     * Creates a new connection to the smsc if the
     * rules allow it.
     * <p>
     * rules:
     * Only if this SMS unit is not shutting down
     * Only start a new connection if one is not already in progress
     * Only allow a new connection every minimum time between connections
     * Only allows upto the maximum sms connections
     * <p>
     * If retry is indicated and current connections are not in progress
     * and number of connections is 0, will always attempt to connect.
     * <p>
     * Retry is used to override the minimum time between connections when
     * reconnecting after an error.  Usually at least one connection is
     * always up.  During high traffic more connection can be added.
     * <p>
     * See reconnect.
     *
     * Returns false if it attempted to connect and failed, true otherwise.
     */
    private boolean addConnection(boolean retry) {
        
        if (ManagementInfo.get().isAdministrativeStateLocked()) {
            log.logString(instanceName + " is locked; will not add connection", Logger.LOG_DEBUG);
            return false;
        }
        
        if (ManagementInfo.get().isAdministrativeStateExit()) {
            log.logString(instanceName + " is exiting; will not add connection", Logger.LOG_DEBUG);
            return false;
        } 

        if(isBiengShutDownViaClient()) {
            log.logString(instanceName + " is shutting down; will not add connection", Logger.LOG_DEBUG);
            return false;
        }
        
        if (!connnectionInProgress.set(false)) {
            //Connection is in progress; if it is for a sender connection, return true to not send an alarm yet.
            return !connnectionInProgress.isConnectingReceiverOnly;
        }

        long currentTime = System.currentTimeMillis();

        /*
         * Connect if we have not exceeded maximum connections and we
         * are within the minimum time to add a new connection or
         * if a retry is indicated and no connections.
         */
        if (connections.size() < config.getSmsMaxConnections() && ( (retry && connections.isEmpty()) || (currentTime > nextTimeToAllowNewConnection)) ) {
            log.logString(instanceName + " adding connection", Logger.LOG_DEBUG);
            SMSConnection conn = new SMSConnection(this, true, isSenderConnectionsAlsoReceiving());

            log.logString("SMSUnit.addConnection: Adding new connection " + instanceName + "; current connections: " + connections.size(), Logger.LOG_DEBUG);
            if (conn.connect() == true) {
                nextTimeToAllowNewConnection = System.currentTimeMillis() + config.getSmsMinTimeBetweenConnections()*1000;
                return true;
                //note: the connection itself will clear the in progress flag when it has bound, or if fails.
                //This is to prevent several binding connections outstanding.
            }
            else {
                connnectionInProgress.reset();
                log.logString(instanceName + " Unable to connect sender!", Logger.LOG_ERROR);
                return false;
            }
        }
        connnectionInProgress.reset();
        return true;
    }





    /**
     * Creates the backup unit for this unit.
     */
    protected void addBackup() {
        log.logString("Adding backup to " + instanceName, Logger.LOG_DEBUG);
        String backupSmscName = config.getSmscBackup(instanceName);

        log.logString("Using backup " + backupSmscName, Logger.LOG_DEBUG);
        if (backupSmscName != null && backupSmscName.length() > 0) {
            backupSMSUnit = SMSClient.get(log, config).getSMSUnit(backupSmscName);
        }
    }

    /**
     * @return true if the calling idle receiver-only connection is allowed to disconnect
     */
    protected synchronized boolean canIdleReceiverOnlyConnectionDisconnect() {
        boolean canDisconnect = false;
        int actualNumConnectionsUp = receiverOnlyConnections.size() - numIdleReceiverOnlyConnectionsDisconnecting.get();
        if(!config.isKeepSmscConnections() && actualNumConnectionsUp > Config.getSmsNumReceiverConn()) {
            numIdleReceiverOnlyConnectionsDisconnecting.incrementAndGet();
            canDisconnect = true;
        }
        return canDisconnect;
    }
    
    /**
     * @return true if the calling idle sender connection is allowed to disconnect
     */
    protected synchronized boolean canIdleConnectionDisconnect() {
        boolean canDisconnect = false;
        int actualNumConnectionsUp = connections.size() - numIdleConnectionsDisconnecting.get();
        if(!config.isKeepSmscConnections() && actualNumConnectionsUp > Config.getSmsMinConn()) {
            numIdleConnectionsDisconnecting.incrementAndGet();
            canDisconnect = true;
        }
        return canDisconnect;
    }

    /**
     * Callback method for when an idle receiver-only connection has finished disconnecting from the SMSC.
     */
    protected void idleReceiverOnlyConnectionDisconnected() {
        numIdleReceiverOnlyConnectionsDisconnecting.decrementAndGet();
    }

    /**
     * Callback method for when an idle sender connection has finished disconnecting from the SMSC.
     */
    protected void idleConnectionDisconnected() {
        numIdleConnectionsDisconnecting.decrementAndGet();
    }

    /**
     * checks if the unit is up and running.
     */
    public boolean isOk() {
        return !connections.isEmpty() && !(isUsingReceiverOnlyConnections() && receiverOnlyConnections.isEmpty()) && !isUnavailableForNewRequests.get() && !isBiengShutDownViaClient();
    }

    /**
     * Call back to set this SMS unit to be temporarily unavailable for new requests.
     * This means we do not want to send any other requests to the SMSC for now; 
     * so, new requests should not be added to the SMSQueue and the requests already in
     * the SMSQueue should be discarded (retried later).
     */
    public void setTemporaryUnavailableForNewRequests() {
        if(isUnavailableForNewRequests.compareAndSet(false, true)) {
            int unavailablePeriod = SMSConfigWrapper.getSmscTemporaryUnavailablePeriodInSecs();
            Timer unavailableForNewRequestsTimer = new Timer();
            unavailableForNewRequestsTimer.schedule(new UnavailableForNewRequestsTimeoutTask(), unavailablePeriod * 1000);
            discardQueuedRequests();
            log.logString("SMSUnit.setUnavailableForNewRequests: " + instanceName + " has been made temporarily unavailable for new requests for " + unavailablePeriod + " seconds", Logger.LOG_DEBUG);
        } else {
            log.logString("SMSUnit.setUnavailableForNewRequests: " + instanceName + " was already temporarily unavailable for new requests", Logger.LOG_DEBUG);
        }
    }
    
    /**
     * callback when a connection has come up.
     * @param conn Connection that is up.
     */
    public void connectionUp(SMSConnection conn) {
        log.logString("SMSUnit " + instanceName + " received connection up: " + conn.getName(), Logger.LOG_DEBUG);
        if(conn.isSender()){
            if(!connections.contains(conn)) {
                connections.add(conn);
            }
        }
        else {
            if(!receiverOnlyConnections.contains(conn)) {
                receiverOnlyConnections.add(conn);
            }
        }
        connnectionInProgress.reset();
        if (connectionStateListener != null && !connections.isEmpty() && !(isUsingReceiverOnlyConnections() && receiverOnlyConnections.isEmpty()) ) {
            connectionStateListener.connectionUp(instanceName);
        }
    }

    /**
     * callback when a new connection failed to connect or bind.
     */
    public void connectionFailed()
    {
        connnectionInProgress.reset();
    }

    /**
     *callback when a connection is coming down.
     *If no connections are left and error is true, the status of the unit becomes unavailable
     *and a pollrequest is added to the queue.
     *
     *@param connection - the connection that stopped.
     *@param error - did the connection stop from an error or not.
     */
    public void connectionDown(SMSConnection connection, boolean error) {

        if (!connections.remove(connection) && !receiverOnlyConnections.remove(connection))
            return; //if the connection was already removed just return;

        if (error) {
            log.logString("SMSUnit " + instanceName + " received connection down", Logger.LOG_DEBUG);
        } else {
            log.logString("SMSUnit " + instanceName + " received connection down by due to protocol error", Logger.LOG_DEBUG);
        }        
    }
    
    private boolean reconnectReceiver() {
        if (receiverOnlyConnections.size() > 0 || (connnectionInProgress.check() && connnectionInProgress.isConnectingReceiverOnly)) {
            log.logString("SMSUnit.reconnectReceiver: Already connected/connecting receiver-only to SMSUnit, while trying to reconnect: " + instanceName, Logger.LOG_VERBOSE);
            return true; //already connected..
        }
        synchronized (nextAllowedReconnectTime) {
            if (nextAllowedReconnectTime > System.currentTimeMillis()) {
                log.logString("SMSUnit.reconnectReceiver: Unable to reconnect, too soon since last time. " + instanceName, Logger.LOG_VERBOSE);
                return false;
            }

            nextAllowedReconnectTime = System.currentTimeMillis() +  config.getSmsMinTimeBetweenReConnections()*1000;
        }

        log.logString("SMSUnit.reconnectReceiver: Attempting to reconnect to SMSUnit " + instanceName, Logger.LOG_VERBOSE);
        if (addReceiverConnection(true)) {
            return true;
        } else {
            log.logString("SMSUnit.reconnectReceiver: Failed to reconnect to SMSUnit " + instanceName, Logger.LOG_ERROR);
            return false;
        }
    }

    /*
     * Do a reconnect if possible.
     * Conditions are there is not already a new connection in progress.
     * We do not already have at least one good connection.
     * And we have not already retried within the the reconnection time limit,
     * used to prevent cyclic reconnects in error stated.
     */
    private boolean reconnect() {
    	if (connections.size() > 0 || (connnectionInProgress.check() && !connnectionInProgress.isConnectingReceiverOnly)) {
    		log.logString("SMSUnit.reconnect: Already connected/connecting sender to SMSUnit, while trying to reconnect: " + instanceName, Logger.LOG_VERBOSE);
    		return true; //already connected..
    	}
    	synchronized (nextAllowedReconnectTime) {
    		if (nextAllowedReconnectTime > System.currentTimeMillis()) {
    			log.logString("SMSUnit.reconnect: Unable to reconnect, too soon since last time. " + instanceName, Logger.LOG_VERBOSE);
    			return false;
    		}

    		nextAllowedReconnectTime = System.currentTimeMillis() +  config.getSmsMinTimeBetweenReConnections()*1000;
    	}

    	log.logString("SMSUnit.reconnect: Attempting to reconnect to SMSUnit " + instanceName, Logger.LOG_VERBOSE);
    	if (addConnection(true)) {
    	    return true;
    	} else {
    	    log.logString("SMSUnit.reconnect: Failed to reconnect to SMSUnit " + instanceName, Logger.LOG_ERROR);
    		return false;
    	}
    }


    /**
     *Sending of request failed. Send the request again, if the unit is down it will use the backup.
     *@param request - the request that failed.
     */
    public void sendFailed(Request request) {
        log.logString("Failed to send request, will retry later.",
        		Logger.LOG_VERBOSE);
        if( request.getResultHandler() != null ) {
          request.getResultHandler().retry(request.getId(),
          "Cant send message to smsc");
      }
    }

    public static Request makeRequest(SMSAddress from, SMSAddress to, UserInfo user, UserMailbox inbox, int validity, SMSResultHandler rh, int id, int type, String text, byte[] byteContent, int replacePosition, int value, int delay) {
        Request request = null;
        switch (type) {
            case SMSClient.TYPE_SMS:
                request = new SMSRequest(from, to, validity, rh, id, text, byteContent, replacePosition, delay );
                break;
            case SMSClient.TYPE_MWI:
                request = new MWIRequest(from, to, user, inbox, validity, rh, id, value, text, byteContent );
                break;
            case SMSClient.TYPE_FLASH:
                request = new SMSFlashRequest(from, to, validity, rh, id, text );
                break;
            case SMSClient.TYPE_SMSMWI:
                request = new SMSMWIRequest(from, to, user, inbox, validity, rh, id, value, text, byteContent, replacePosition );
                break;
            case SMSClient.TYPE_VVM_DEP:
            case SMSClient.TYPE_VVM_GRE:
            case SMSClient.TYPE_VVM_EXP:
            case SMSClient.TYPE_VVM_LOG:
            case SMSClient.TYPE_APPLEVVM_DEP:
            case SMSClient.TYPE_APPLEVVM_GRE:
            case SMSClient.TYPE_APPLEVVM_EXP:
            case SMSClient.TYPE_APPLEVVM_LOG:
                request = new VVMRequest(from, to, user, validity, rh, id, text, byteContent, delay, type);
                break;
            default:
                // no other types can be created.
                break;
        }
        return request;
    }

    public int sendMulti(MultiRequest request) {
        return send(request, "");
    }

    /**
     *Sends a reqular sms message.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param message - The String that will be sent.
     *@param replacePosition - The position to put the message, -1 if no replace.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendSMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message, byte[] byteContent, int replacePosition, int delay) {
        SMSRequest request = new SMSRequest(from, to, validity, rh, id, message,
                byteContent, replacePosition, delay);

        return send(request, "");
    }
    
    public int sendSMS(SMSRequest request) {
        return send(request, "");
    }

    /**
     * Sends a cancel request to the SMSC.
     * @param from the from address to cancel on
     * @param to the to address to cancel on
     * @param replacePosition used to determine serviceType if serviceTypeReplace is true
     * @return  SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendCancel(SMSAddress from, SMSAddress to, int replacePosition) {
        CancelRequest request = new CancelRequest(from, to, replacePosition);

        return send(request, "");
    }
    
    /**
     * Sends a cancel request to the SMSC.
     * @param from the from address to cancel on
     * @param to the to address to cancel on
     * @param serviceType used to cancel cancel
     * @return  SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendCancel(SMSAddress from, SMSAddress to, String serviceType) {
        CancelRequest request = new CancelRequest(from, to, serviceType);

        return send(request, "");
    }

    /**
     *Sends a mwi message.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param message - An optional String message.
     *@param count - how many mails exists, 0 means mwioff.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendMWI(SMSAddress from, SMSAddress to, UserInfo user, UserMailbox inbox, int validity, SMSResultHandler rh, int id, int count, String message) {
        MWIRequest request = new MWIRequest(from, to, user, inbox, validity, rh, id, count,
                message);

        return send(request, "");
    }

    /**
     *Sends a combined sms and mwi message.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param count - how many mails exists, 0 means mwioff.
     *@param message - The String that will be sent.
     *@param replacePosition - The position to put the message, -1 if no replace.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendMWISMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, int count, String message, int replacePosition) {
        SMSMWIRequest request = new SMSMWIRequest(from, to, validity, rh, id,
                count, message, replacePosition);

        return send(request, "");
    }

    /**
     *Sends a sms-class0 message. This is a message that pops up on the phone
     *or flashes.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param message - The String that will be sent.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendFlashSMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message) {
        SMSFlashRequest request = new SMSFlashRequest(from, to, validity, rh, id,
                message);

        return send(request, "");
    }



    /**
     *Sends a multi lines message. This is a message that contains a header and a footer on each sms
     *and a couple of lines in between.
     *@param request - Contains all the info needed to send the formatted message.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendFormattedSMS(FormattedSMSRequest request) {
        return send(request, "");
    }


    /**
     *Sends a phone on message.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param message - an optional message.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendPhoneOn(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message) {
        PhoneOnRequest request = new PhoneOnRequest(from, to, validity, rh, id,
                message);

        return send(request, "");
    }

    /**
     * Sends a Visual Voice Mail (VVM) request
     * @param vvmRequest - vvm request
     * @return - SEND_OK if the message was successfully put in the SMSUnit queue, SEND_FAILED or SEND_FAILED_TEMPORARY otherwise.
     */
    public int sendVvm(Request vvmRequest) {
        return send(vvmRequest, "");
    }

    /**
     *Puts the request on the queue. If the unit is unavailable or the queue is full and loadbalancing
     *is set, the request is transfered to the backup if one exists.
     *If no backup exists SEND_FAILED_TEMPORARY is returned.
     *
     *@param request - the request to put on queue.
     *@param visitedSmsc - used to prevent backupcalls to self.
     */
    private int send(Request request, String visitedSmsc) {

    	if (send_count++ == 100)
    	{
    	    //report every so often in the log, how many connections are up.
    		log.logString("Number of connections: " + connections.size(), Logger.LOG_DEBUG);
    		send_count = 0;
    	}



        if (visitedSmsc.indexOf(";" + instanceName + ";") != -1) {
            log.logString("Message coming back to original unit", Logger.LOG_DEBUG);
            return SMSClient.SEND_FAILED_TEMPORARY;
        }
        // check backupcall to self.
        if (!isOk()) {
            if (backupSMSUnit != null) {
                return backupSMSUnit.send(request, visitedSmsc + ";" + instanceName + ";");
            } else {
                return SMSClient.SEND_FAILED_TEMPORARY;
            }
        }
        if (queue.getSize() >= config.getSMSQueueSize()) {
            if (config.getSmscLoadBalancing() && backupSMSUnit != null) {
                return backupSMSUnit.send(request, visitedSmsc + ";" + instanceName);
            }
        }

        log.logString("Adding request to " + instanceName + " queue" , Logger.LOG_DEBUG);
        if (!queue.put(request, 100, request.getDelay())) {
            log.logString("Queue full for " + instanceName, Logger.LOG_VERBOSE);
            return SMSClient.SEND_FAILED_TEMPORARY;
        }

        return SMSClient.SEND_OK;
    }

    /**
     *SMSConnections use this function to wait for a request in the queue.
     * @throws InterruptedException - if interrupted while waiting, , ntf shutting down..
     */
    public Request waitForRequest() throws InterruptedException {
        Request request = null;
        //Connections should not get request from the queue if the queue is in the process of being discarded.
        //Connections should not block waiting if this SMS unit is being shutdown ungracefully.
        //Connections should not block waiting if shutdown is graceful and the queue is empty or the shutdown grace period expired.
        if( !isDiscardingQueuedRequests  && shutdownType != ShutdownType.UNGRACEFUL &&  !(shutdownType == ShutdownType.GRACEFUL && (queue.isEmpty() || isShutdownTimedOut)) ) {
            log.logString("Waiting for requests", Logger.LOG_DEBUG);
            //Wait time needs to be the poll interval because if there is no request, it is time to send a poll request.
            request = queue.get(config.getSmscPollInterval(),TimeUnit.SECONDS);
        }        
        return request;
    }
    
    /**
     *SMSConnections use this function to wait for a request in the queue.
     * @throws InterruptedException - if interrupted while waiting, ntf shutting down..
     */
    public Request waitForRequest(int timeInSec) throws InterruptedException {
        Request request = null;
        //Connections should not get request from the queue if the queue is in the process of being discarded.
        //Connections should not block waiting if this SMS unit is being shutdown ungracefully.
        //Connections should not block waiting if shutdown is graceful and the queue is empty or the shutdown grace period expired.
        if( !isDiscardingQueuedRequests  && shutdownType != ShutdownType.UNGRACEFUL &&  !(shutdownType == ShutdownType.GRACEFUL && (queue.isEmpty() || isShutdownTimedOut)) ) {
            log.logString("Waiting for requests", Logger.LOG_DEBUG);
            //Wait time needs to be the poll interval because if there is no request, it is time to send a poll request.
            request = queue.get(timeInSec,TimeUnit.SECONDS);
        }        
        return request;
    }
    
    
    public String getProtocol() {
        return protocol;
    }

    public Converter getConverter() {
        return converter;
    }

    public Logger getLog() {
        return log;
    }

    public SMSConfig getConfig() {
        return config;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getSystemType() {
        return systemType;
    }

    public SMSUnit getBackupSMSUnit() {
        return backupSMSUnit;
    }

    protected boolean isDiscardingQueuedRequests() {
        return isDiscardingQueuedRequests;
    }
    
    /**
     * @return true if this SMS unit is shutting down
     * NOTE: this is used if the ESME was removed from the configuration or forced unavailable.
     * If NTF is shutdown via the management interface the unit is shutdown via ntfThread.
     */
    protected boolean isBiengShutDownViaClient() {
        return shutdownType != ShutdownType.NONE;
    }
    
    /**
     * Shuts down this SMS unit (i.e a configuration change, this unit is removed from ntf configuration.).
     * @param shutdownGracePeriodInSeconds - grace period given to process pending requests; after this period, shutdown is forced. 
     *                  Value of zero means that there is no limit on grace period (SMS unit will be shut down whenever all connections finish pending requests)
     * @param type - ungraceful: discard the queued requests; graceful: try to send queued requests before shutting down
     */
    public void shutdownViaClient(int shutdownGracePeriodInSeconds, ShutdownType type) {
        log.logString("SMSUnit.shutdown(): Called for sms unit: " + instanceName + ", shutdown grace period: " + shutdownGracePeriodInSeconds + ", shutdown type: " + type.toString(), Logger.LOG_DEBUG);
        this.shutdownType = type;
        
        if(shutdownGracePeriodInSeconds > 0) {
            Timer timer = new Timer(true);
            timer.schedule(new ShutdownTimeoutTask(), shutdownGracePeriodInSeconds * 1000);
            log.logString("SMSUnit.shutdown(): Started timer for shutdown grace period (in seconds) : " + shutdownGracePeriodInSeconds, Logger.LOG_DEBUG);
        }
        
        if(shutdownType == ShutdownType.UNGRACEFUL) {
            //The queued requests will not be sent.
            discardQueuedRequests();
        }
    }
    
    /**
     * Discards the queued requests because they will not be sent out.
     * For each discarded request, an attempt is made to send a retry result to the request's result handler.
     */
    private void discardQueuedRequests() {
        log.logString("SMSUnit.discardQueuedRequests(): Sending retry result to result handlers of queued requests.  Number of queued requests: " + queue.getSize(), Logger.LOG_DEBUG);
        isDiscardingQueuedRequests = true;
        while(!queue.isEmpty() && !isOk()) {
            try {
                Request request = queue.get(1);
                if(request != null) {
                    if(request.getResultHandler() != null) {
                        request.getResultHandler().retry(request.getId(), "Cant send message to smsc");
                    } else if (request instanceof MultiRequest) {
                        MultiRequest multiRequest = (MultiRequest) request;
                        Request req = null;
                        while( (req = multiRequest.getNextRequest()) != null ) {
                            req.getResultHandler().retry(req.getId(), "Cant send message to smsc");
                            multiRequest.requestDone();
                        }
                    }
                }
            } catch (Exception e) {
                log.logString("SMSUnit.discardQueuedRequests(): Failed to send retry result for request on queue: " + e.getMessage(), Logger.LOG_ERROR );
            }
        }
        isDiscardingQueuedRequests = false;
    }
    
    /**
     * Executes shutdown when the shutdown grace period has expired.
     */
    private class ShutdownTimeoutTask extends TimerTask {
        @Override
        public void run() {
            log.logString("SMSUnit.ShutdownTimeoutTask.run(): : Shutdown grace period has expired, forcing unbind for any connections that are still bound.", Logger.LOG_DEBUG);
            
            isShutdownTimedOut = true;

            //To avoid concurrency issues, take a snapshot of the connections and use that to iterate through the connections.
            SMSConnection[] receiverOnlyConnectionsArray = new SMSConnection[receiverOnlyConnections.size()];
            receiverOnlyConnections.toArray(receiverOnlyConnectionsArray);
            for(int i=0; i < receiverOnlyConnectionsArray.length; i++) {
                if(receiverOnlyConnectionsArray[i] != null) {
                    boolean isConnectionUp = receiverOnlyConnections.remove(receiverOnlyConnectionsArray[i]);
                    if(isConnectionUp) {
                        receiverOnlyConnectionsArray[i].signalMandatoryUnbind();
                    }
                }
            }
            
            SMSConnection[] connectionsArray = new SMSConnection[connections.size()];
            connections.toArray(connectionsArray);
            for(int i=0; i < connectionsArray.length; i++) {
                if(connectionsArray[i] != null) {
                    boolean isConnectionUp = connections.remove(connectionsArray[i]);
                    if(isConnectionUp) {
                        connectionsArray[i].signalMandatoryUnbind();
                    }
                }
            }
            
            //In the case of a "graceful" shutdown, clean up the queued requests that we did not have time to send out.
            //In the case of an "ungraceful" shutdown, make sure we got the requests that might have been put in the queue after the first call to discard the queue
            discardQueuedRequests();
        }
        
    }
    
    /**
     * Resets this SMS unit to be available for new requests when the unavailability period expires.
     */
    private class UnavailableForNewRequestsTimeoutTask extends TimerTask {
        @Override
        public void run() {
            isUnavailableForNewRequests.set(false);       
            log.logString("SMSUnit.UnavailableForNewRequestsTimeoutTask.run: " + instanceName + " is once again available for new requests", Logger.LOG_DEBUG);     
        }
        
    }
    
    /**
    *
    *
    * @author lmcmajo
    */
    private class SMSQueue {
        private int fMaxQueueSize;
        private ManagedArrayBlockingQueue<Request> smscSenderQueue;;
        private Integer delayedItems = 0;
        private boolean blocked = false;

        public SMSQueue(int queueSize) {
            fMaxQueueSize = queueSize;
            smscSenderQueue = new ManagedArrayBlockingQueue<Request>(queueSize);
        }

        @SuppressWarnings("unused")
        public Iterator<Request> iterator() {
            return smscSenderQueue.iterator();
        }

        @SuppressWarnings("unused")
        public int drainTo(Collection<Request> c) {
            return smscSenderQueue.drainTo(c);
        }

        @SuppressWarnings("unused")
        public void expand(Request request) throws InterruptedException {
            if (blocked)
            {
                fail(request);
                return;
            }
            smscSenderQueue.put(request);
        }

        public Request get(int maxTime) {

            Request item = smscSenderQueue.poll(maxTime);
            return (item);
        }

        public Request get(int time, TimeUnit unit) {
            return (smscSenderQueue.poll(time,unit));
        }

        public boolean put(Request request, int maxTime, int delay) {
            if (blocked)
            {
                return false;
            }

            //if not connected
            if(connections.isEmpty())
            {
                //not connected
                log.logString("New request when not connected, connecting, message will be retried.", Logger.LOG_VERBOSE);
                reconnect();
                return false;
            }

            if (smscSenderQueue.size() >= fMaxQueueSize && delay > 0) {
                    //wait for queue to empty..
                    if (smscSenderQueue.offer(request,maxTime))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
            }

            if( delay > 0) {
                if ( ManagementInfo.get().isAdministrativeStateShutdown() )
                {
                    fail(request); // can't delay if shutting down.
                    return false;
                }

                if (delay > 30)
                {
                    log.logString("Tried to delay longer than 30 seconds on smsQueue, delaying for 30!", Logger.LOG_ERROR);
                    delay = 30;
                }

                final int delayTime = delay;
                final Request delayRequest = request;
                synchronized (delayedItems) {
                    delayedItems++;
                }
                new Thread() {

                    public void run() {
                        try {

                            sleep(delayTime*1000);
                            if (blocked)
                            {
                                fail(delayRequest);
                            } else
                            {
                                smscSenderQueue.put(delayRequest);
                            }

                        } catch (InterruptedException e)
                        {
                            //state change like to locked or shutdown when queue full.
                            //Unlikely to happen in locked state unless queue was full before the lock.
                            delayRequest.getResultHandler().failed(delayRequest.getId(),"retry");
                        }
                        synchronized (delayedItems) {
                            delayedItems--;
                        }
                    }
                }.start();

                return true;
            } else {
                if (smscSenderQueue.offer(request,maxTime,TimeUnit.MILLISECONDS))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        private void fail(Request request) {
            request.getResultHandler().failed(request.getId(),"retry");
        }

        public synchronized boolean isEmpty() {
            return smscSenderQueue.isEmpty();
        }

        public synchronized boolean isIdle(int millis)
        {
            return(smscSenderQueue.isIdle(millis,TimeUnit.MILLISECONDS) && delayedItems == 0);
        }

        public synchronized int getDelayedSize() {
            return queue.getSize()+delayedItems;
        }

        public synchronized int getSize() {
            return smscSenderQueue.size();
        }
        public boolean waitNotEmpty(long time) {
            synchronized (delayedItems) {
                if (delayedItems > 0)
                    return true;
            }

            boolean res = smscSenderQueue.waitNotEmpty(time, TimeUnit.MILLISECONDS);
            if (res == false) {
                synchronized (delayedItems) {
                    if (delayedItems > 0) {
                        return true;
                    }
                }
            }
            return res;
        }
    }

   public int queueSize() {
       return queue.getSize();
   }

   public boolean isIdle(int millis) {
       return queue.isIdle(millis);
   }

   public int getDelayedSize() {
       return queue.getDelayedSize();
   }

   public int drainTo(Collection<Request> c) {
       return queue.smscSenderQueue.drainTo(c);
   }

   public boolean waitNotEmpty(long time) {
       return (queue.waitNotEmpty(time));
   }


}
