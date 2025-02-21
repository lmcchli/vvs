/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

import java.io.EOFException;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.sms.SMSConnection;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.SMSUnit;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.oe.common.util.KPIProfiler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.event.PhoneOnEventListener;

/**
 * SMSCom is the interface class that hides all details of communication
 * with an SMS Center from clients. It is subclassed for each implemented
 * protocol.<P>
 * A protocol is assumed to be handled by the class
 * com.mobeon.common.smscom.<i>protname.PROTNAME</i>Com,
 * i.e. the package name is the protocol name in lower-case letters and the name
 * of the protocol connection class begins with the protocol name in upper-case
 * letters. The SMPP protocol for example, is implemented by the class
 * <code>com.mobeon.common.smscom.smpp.SMPPCom</code>
 * <P>
 * SMSCom implementations will automatically connect and log in if necessary,
 * when <i>sendMessage()</i> is called, and will stay connected and logged in, at
 * least for some time. To disconnect immediately, you use the <i>close()</i>
 * method. To check with a test message, that the connection is OK you use the
 * <i>poll()</i> method. If you just want to know if the connection
 * was OK the last time a message was sent, use <i>isOk()</i>.
 * <P>
 * <B>Errors</B> are reported through SMSComException. There are a number of
 * subclasses with different meaning:
 * <TABLE BORDER=1>
 * <TR><TH>Exception</TH><TH>Description</TH><TH>Example</TH></TR>
 * <TR><TD>SMSComConfigException</TD><TD>This exception signals a
 * configuration error that will not go away by itself. Someone has to change
 * The configuration to make things work.</TD><TD>Wrong password.</TD></TR>
 * <TR><TD>SMSComTempException</TD><TD>This exception signals an error
 * of temporary nature. The operation may succeed if retried at a later
 * time.</TD><TD>The SMS-C has stopped.</TD></TR>
 * <TR><TD>SMSComDataException</TD><TD>There is something wrong with the
 * data in a particular request. Retrying the same request is useless, but other
 * requests may succeed.</TD><TD>Invalid destination address.</TD></TR>
 * <TR><TD>SMSComConnectionException</TD><TD>The request could not be fulfilled
 * due to connection problems.</TD></TR>
 * </TABLE>
 * </P>
 *
 * <P>
 * <B>Keeping an eye on smscom</B><BR>
 * To get information on what smscom is doing, there are two listener interfaces
 * you can register with an SMSCom instance:<DL>
 * <DT>CommSpy<DD>Lets you listen on the raw byte buffers from all communication
 * with the SMS-C.
 * <DT>Logger<DD>Lets SMSCom send log messages when something interesting
 * happens.
 * </DL>
 * </P>
 * <P>
 * <B>These are the steps needed for sending an SMS</B>
 * <OL>
 * <LI>Create an SMSCom for the wanted protocol with SMSCom.get.
 * <LI>Set the host name and port number of the SMS-C in the SMSCom object.
 * <LI>Set the user name and password for logging in to the SMS-C, in the SMSCom object.
 * <LI>Create an SMSAddress object for the destination mobile.
 * <LI>Create an SMSMessage object with the message text, converted to bytes.
 * <LI>Set any extra message parameters needed.
 * <LI>Call SMSCom.sendMessage.
 * </OL>
 * Example:
 * <PRE>
 * Properties props= new Properties();
 * props.load(new FileInputStream("charconv.cfg"))
 * SMSCom com= SMSCom.get("SMPP");
 * com.setHostName("smsc");
 * com.setPortNumber(1234);
 * com.setUserName("vms");
 * com.setPassword("secret");
 * Date exp= new Date();
 * exp.setTime(exp.getTime() + 1000*60*60*24);
 * try {
 *     com.sendMessage(new SMSAddress("46709999999", 1, 1),
 *                     new SMSMessage(Converter.get(props).unicodeToBytes("Hello world", 0), exp, false));
 * } catch (SMSComException e) {
 * }
 * </PRE>
 */
public abstract class SMSCom {

    /**String that could be used to identify the system to the SMSC*/
    protected String systemType;
    /**User name for logging on to the SMSC*/
    protected String user;
    /**Password for logging on to the SMSC*/
    protected String password;
    /**The address range for incoming messages*/
    protected SMSAddress addressRange = new SMSAddress(0, 0, "");
    /**The SMSC host name*/
    protected String hostname;
    /**The SMSC port number*/
    protected int portnumber;
    /**Class that shall receive copies of all messages exchanged with the SMSC.*/
    protected CommSpy spy;
    /**Class that shall receive log messages about the SMSC communication.*/
    protected Logger log;
    /**Communication timeout.*/
    protected int timeout = 0;
    /**Protocol.*/
    protected String protocol = "Unknown";
    /**The object that handles delivery receipts coming in from the MS.*/
    protected PhoneOnEventListener receiptReceiver = null;
    /** Lock controlling the producing and consuming of response messages */
    protected Object responseLock = new Object();
    /** Socket for communication with the SMSC */
    protected Socket smscSock = null;
    /** InputStream for messages from the SMSC */
    protected InputStream fromSmsc = null;
    /** OutputStream for emssages to the SMSC */
    protected OutputStream toSmsc = null;
    /** Shall NTF receive messages (other than responses) from the SMSC? */
    protected boolean _receive = false;
    /** Shall NTF send messages (other than responses) to the SMSC? */
    protected boolean _send = true;
    /** Reader that reads all messages coming from the SMSC and forwards them to
        a handling method defined in the protocol subclass.*/
    protected SmscReader reader = null;
    /** Listener to tell about changes in the connection state*/
    protected ConnectionStateListener connectionHandler = null;
    /** The connection is newly created and has not received a request.*/
    public static final int SMSCOM_CLOSED = 0;
    /** A request has been received and a connect is in progress*/
    public static final int SMSCOM_CONNECTING = 1;
    /** Connection has succeeded and a login request has been sent*/
    public static final int SMSCOM_BINDING = 2;
    /** A request has been sent and a response is expected*/
    public static final int SMSCOM_SENDING = 3;
    /** A session is established but there is no request to send*/
    public static final int SMSCOM_WAITING = 4;
    /** The logout procedure has been started*/
    public static final int SMSCOM_UNBINDING = 5;
    /** The connection is being closed*/
    public static final int SMSCOM_DISCONNECTING = 6;
    /** The connection is being killed*/
    public static final int SMSCOM_EXITING = 7;
    /** The current connection state*/
    protected int state = SMSCOM_CLOSED;
    /** Texts for the different states*/
    protected static final String[] _stateStrings = {"closed",
                                                   "connecting",
                                                   "binding",
                                                   "sending",
                                                   "waiting",
                                                   "unbinding",
                                                   "disconnecting",
                                                   "exiting", };
    /**errCodeAction specifies how SMSCom shall handle error codes from the
     * SMSC.
     * <TABLE><TR><TH>Value</TH><TH>Description</TH></TR>
     * <TR><TD>HANDLE_ERRORS</TD><TD>SMSCom throws an appropriate exception when
     * it receives an error code.</TD></TR>.
     * <TR><TD>LOG_ERRORS</TD><TD>SMSCom ignores the error code, but sends a log
     * message to the Logger (if a Logger has been set with setLogger).
     * <TR><TD>IGNORE_ERRORS</TD><TD>Error codes are ignored completely</TD></TR></TABLE>
     * The default is HANDLE_ERRORS.<P>
     * You do not normally change this variable, but if you will ignore the error
     * code anyway, you can use this variable to stop a lot of useless exception
     * throwing.<P>
     * NOTE that this does not stop SMSCom from throwing exceptions altogether,
     * only when it is caused by error codes.
     */
    protected int errCodeAction;
    /**HANDLE_ERROR is the default value for errCodeAction and makes SMSCom throw
     * exceptions when there are error codes from the SMS-C*/
    public static final int HANDLE_ERRORS = 0;
    /**LOG_ERROR is the value for errCodeAction that makes SMSCom log errors
     * instead of throwing exceptions.*/
    public static final int LOG_ERRORS = 1;
    /**IGNORE_ERROR is the value for errCodeAction that makes SMSCom do nothing
     * at all when there are error codes from the SMS-C.*/
    public static final int IGNORE_ERRORS = 2;
    /* The time in seconds before forcing a close during a shutdown of a socket. 
     * IE waiting for the close ACK from the other end. Clean shutdown. */
    protected static final int SO_LINGER_TIME = 5;   
    /* MAX time to wait for new data to be in the read buffer -
     * Time out frequently to allow checking of management state.*/
    protected static final int MAX_SO_TIMEOUT = 5000; //milliseconds. 5 seconds..
    /* MIN time to wait for new data to be in the read buffer -
     * Time out frequently to allow checking of management state.*/
    protected static final int MIN_SO_TIMEOUT = 1000; //don't time out less than 1 second.

    
    private static Integer SmscReaderinst=0;

    private SMSConnection requestSenderThread = null;
    
    /* indicates that the connections has been asked to be closed. 
     */
    private boolean closing = false; 

    //CLIENT INTERFACE


    // KPI; we will not include every log as it may be too much/frequent
    // instead we'll do sampling every second
    public static final long KPI_SAMPLE_INTERVAL = 1000; // every 1 second  
    private static long lastSampleTime = System.currentTimeMillis();
    
    
    /**
     * Create an SMSCom for the requested protocol.
     *@param protocol The name of the protocol the SMSC uses.
     *@return a subclass of SMSCom, specialized for the requested
     * protocol. Null is returned if no class can be found for the protocol.
     *@throws SMSComException if there is no class for the protocol found
     * in MCR.
     */
    public static SMSCom get(String protocol, boolean asynchronousMode, ConnectionStateListener connectionHandler) throws SMSComException {
        SMSCom conn = null;

        //Find the protocol class
        String className = "com.mobeon.common.smscom."
            + protocol.toLowerCase() + "." + protocol.toUpperCase() + "Com";
        
        //Currently, asynchronousMode flag applies only for SMPP protocol
        if("SMPP".equalsIgnoreCase(protocol)){
            if(asynchronousMode) {
                className = className + "Asynchronous";
            } else {
                className = className + "Synchronous";
            }
        }
        

        try {
            Class<?> protocolClass = Class.forName(className);
            conn = (SMSCom) protocolClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new SMSComConfigException("SMSCom has no right to access " + className
                                            + " or its constructor");
        } catch (ClassNotFoundException e) {
            throw new SMSComConfigException("SMSCom can not find " + className);
        } catch (InstantiationException e) {
            throw new SMSComConfigException("SMSCom can not instantiate " + className);
        }

        conn.connectionHandler = connectionHandler;
        return conn;
    }

    /**
     * Checks that the connection to the SMS-C is up or can be
     * re-established. All exceptions are swallowed and just result in a false
     * return value.
     * @return true if it is possible to communicate with the SMS-C.
     */
    public boolean poll() {
    	try {
    		if (state == SMSCOM_SENDING || state == SMSCOM_WAITING) {
    		    return protocolPoll();
    		}
    		else
    		{
    		    log.logString("Unable to send poll, due to incorrect state: " + state ,Logger.LOG_VERBOSE);
    		    if (ManagementInfo.get().isAdministrativeStateLocked() || ManagementInfo.get().isAdministrativeStateShutdown()) {
    		        return true; // Normal to not send poll - if locked or shutting down and not connected state.
    		    } else
    		    {
    		        return false;
    		    }
    		}
		} catch (Exception e) {
		    log.logString("Exception while polling connection: " +  NtfUtil.stackTrace(e), Logger.LOG_ERROR);
            return false;
        }
    }

    /**
     * Used to check if the SMSCom is still waiting for the responses from the SMSC.
     * <p>
     * There is never any outstanding responses for synchronous mode.
     * This method needs to be overridden for asynchronous mode.
     * @return true if there are still outstanding responses.
     */
    public boolean isWaitingForResponse() {
        return false;
    }
    
    public boolean isReadyForNextRequest() {
        try {
            checkState();
            return true;
        } catch (SMSComException e) {
           return false;
        }
    }

    private void checkState() throws SMSComException {

    	switch (state) {

    	case SMSCOM_CONNECTING:
    	case SMSCOM_BINDING:
    		throw new SMSComConnectionException("Can not accept request. Connection is "
    				+ _stateStrings[state]);

    	case SMSCOM_WAITING:
    	case SMSCOM_SENDING:
    		//we are in a good state.
    		break;

    	default:
    		throw new SMSComException("Can not accept request. Connection is "
    				+ _stateStrings[state]);
    	}

    	}

    /**
     * Submits a message to the SMS-C for sending to a receiver. If necessary,
     * it creates a connection, logs in to the SMS-C and keeps the connection
     * until the close() method is called or the connection is idle too long.
     *@param adr - The address of the receiver.
     *@param msg - The message to send.
     *@throws SMSComException
     */
    public void sendMessage(SMSAddress adr, SMSMessage msg, int smsRequestId, SMSResultHandler rh) throws SMSComException {
        sendMessage(adr, null, msg, smsRequestId, rh);
    }

    /**
     * Submits a message to the SMS-C for sending to a receiver. If necessary,
     * it creates a connection, logs in to the SMS-C and keeps the connection
     * until the close() method is called or the connection is idle too long.
     *@param adr - The address of the receiver.
     *@param org - The address of the sender.
     *@param msg - The message to send.
     *@throws SMSComException
     */
    public int sendMessage(SMSAddress adr,
                            SMSAddress org,
                            SMSMessage msg, 
                            int smsRequestId,
                            SMSResultHandler rh) throws SMSComException {

    	checkState();

        if (adr == null || adr.getNumber() == null) {
            setState(SMSCOM_WAITING);
            throw new SMSComDataException("Send message requested, without receiver");
        }

    	Object cpo = null; // KPI Check Point Object
        try {
        	////////////////// KPI
        	long currentTime = System.currentTimeMillis();
        	if (KPIProfiler.isStatsEnabled() && (currentTime - lastSampleTime >= KPI_SAMPLE_INTERVAL)) {
        		lastSampleTime = currentTime; 
        		cpo = KPIProfiler.enterCheckpoint(KPIProfiler.KPI_NAME_SMPP_SEND_TIME, KPIProfiler.KPI_DISPLAY_SMPP_SEND_TIME);
        	}
        	//////////////////
            protocolSend(adr, org, msg, smsRequestId, rh);
        } catch (SMSComException e) {
    		if (e instanceof SMSComReBindException)
    		{
    			//re-send immediately
    			setState(SMSCOM_WAITING);
    			try{
    				 if (willLog(Logger.LOG_ERROR)) {
    			            log.logString("Rebind exception, re-sending message immediatly " , Logger.LOG_ERROR);
    			        }
    				protocolSend(adr, org, msg, smsRequestId, rh);
    			} catch (SMSComException e2)
    			{
    				setState(SMSCOM_WAITING);
    				throw e2;
    			}
    		}
    		else
    		{
    			setState(SMSCOM_WAITING);
    			throw e;
    		}
    	} finally {
        	if (cpo != null) KPIProfiler.exitCheckpoint(cpo);
    	}
        return getSuccessfulSendCode();
    }


    /**
     * Return the send status indicating that the send was successful.
     * For synchronous, the status is SMSConnection.SEND_OK.
     * For asynchronous, the status is SMSConnection.SEND_RESULT_PENDING.
     * @return successful send status
     */
    protected int getSuccessfulSendCode() {
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Returning SEND_OK successful send code", Logger.LOG_DEBUG);
        }
        return SMSConnection.SEND_OK;
    }

    /**
     * Submits a message to the SMS-C that cancels SMS by the specified from and to
     * address. ServiceType can be used to further specify what to cancel. If necessary,
     * it creates a connection, logs in to the SMS-C and keeps the connection
     * until the close() method is called or the connection is idle too long.
     *@param to - The address of the receiver.
     *@param from - The address of the sender.
     *@param serviceType - The serviceType to cancel or null.
     *@throws SMSComException
     */
    public void sendCancel(SMSAddress to, SMSAddress from, String serviceType) throws SMSComException {

    	checkState();

        if (to == null || to.getNumber() == null) {
            setState(SMSCOM_WAITING);
            throw new SMSComDataException("Cancel message requested, without receiver");
        }

        try {
            protocolCancel(to, from, serviceType);

        } catch (SMSComException e) {
    		if (e instanceof SMSComReBindException)
    		{
    			//re-send immediately
    			setState(SMSCOM_WAITING);
    			try{
    				 if (willLog(Logger.LOG_ERROR)) {
    			            log.logString("Rebind exception, re-sending cancel message immediatly " , Logger.LOG_ERROR);
    			        }
    				 protocolCancel(to, from, serviceType);
    			} catch (SMSComException e2)
    			{
    				setState(SMSCOM_WAITING);
    				throw e2;
    			}
    		}
    		else
    		{
    			setState(SMSCOM_WAITING);
    			throw e;
    		}
    	}
    }

    /**
     * Used to re-bind (re-login) when a connection had already been established but the bind (login) is lost.
     * @throws SMSComException if binding fails
     */
    public void bind() throws SMSComException {
        setState(SMSCOM_BINDING);
        connectandBind();
    }
    
    public void connectandBind() throws SMSComException
    {
        switch (state) {
        case SMSCOM_CLOSED:
            if (!connect()) {
                //if (connectionHandler != null) { connectionHandler.connectionDown(""); }
                throw new SMSComConnectionException("SMPPCom could not establish SMSC session.");
            }
        case SMSCOM_BINDING:
        	if (!protocolLogin()) {
        		disconnect();
                //if (connectionHandler != null) { connectionHandler.connectionDown(""); }
                throw new SMSComConnectionException("SMPPCom could not Bind to SMSC " + hostname);
            }
            if (connectionHandler != null) { connectionHandler.connectionUp(""); }
            break;

        case SMSCOM_WAITING:
        case SMSCOM_SENDING:
            //if (connectionHandler != null) { connectionHandler.connectionUp(""); }
        	//we are in a good state.
            break;

        default:
            throw new SMSComException("Can not accept request. Connection is "
                                                + _stateStrings[state]);
        }
    }

    /**
     * Logs out from the SMS-C and closes the connection to it.
     */
    public void close() {
        closing  = true;
        switch (state) {
        case SMSCOM_EXITING:
        case SMSCOM_CLOSED:
            break;
        case SMSCOM_WAITING:
        case SMSCOM_SENDING:
        	setState(SMSCOM_UNBINDING);
            protocolLogout();
            setState(SMSCOM_DISCONNECTING);
            //No break, continue with next case
        case SMSCOM_UNBINDING:
        default:
        	setState(SMSCOM_DISCONNECTING);
            disconnect();
        }
        if (connectionHandler != null) { connectionHandler.connectionDown(""); }
        protocolCleanup();
    }

    /**
     * Logs out from the SMS-C and closes the connection to it.
     */
    protected void reset() {
        switch (state) {
        case SMSCOM_EXITING:
        case SMSCOM_CLOSED:
            break;
        case SMSCOM_WAITING:
        case SMSCOM_SENDING:
        	setState(SMSCOM_UNBINDING);
            protocolLogout();
            setState(SMSCOM_DISCONNECTING);
        default:
            disconnect();
        }
        if (connectionHandler != null) { connectionHandler.connectionReset(""); }
        protocolCleanup();
    }

    /**
     * Constructor called by the implementing subclasses to initialize
     * protocol-independent information.
     */
    protected SMSCom() {
        systemType = "";
        user = null;
        password = "";
        hostname = "";
        portnumber = 0;
        spy = null;
        log = null;
        errCodeAction = HANDLE_ERRORS;
    }


    //CONNECTION AND DISCONNECTION

    /**
     * connect connects to the SMS-C.
     *@return true if a connection could be established, false otherwise.
     */
    private boolean connect() {
        if (willLog(Logger.LOG_VERBOSE)) {
            log.logString("SMSCom connecting to " + hostname, Logger.LOG_VERBOSE);
        }
        try {
            /* This allows time outs at reasonable intervals to check if connection is 
             * Shutting down, locking, but not to short as would add extra load.
             * The socket time out has to be less than or equal to the Cm.smscTimeout.
             * Preferably half or less.
             * 
             * See com.mobeon.common.smscom.smpp.SMPP_PDU.read(InputStream, byte[], int, int).
             * 
            */
            int soTimeout=timeout/3;
            smscSock = new Socket(hostname, portnumber);
            if (soTimeout < MAX_SO_TIMEOUT) {
                if (soTimeout < MIN_SO_TIMEOUT) {
                    //In theory should not be less than a (MIN_SO_TIMEOUT) anyway as minimum timeout is 3 in notificaiton.xsd
                    //but just to cover if this is changed in the future put a minimum value here. So we don't let the connection threads go 
                    //crazy waking up to often.
                soTimeout = MIN_SO_TIMEOUT;
                }                
                smscSock.setSoTimeout(soTimeout);
            }
            else {
                //wake up at least this often to check NTF && connection state.
                smscSock.setSoTimeout(MAX_SO_TIMEOUT); 
            }
                        
            smscSock.setSoLinger(true, SO_LINGER_TIME);
            int buffsize = smscSock.getSendBufferSize();
            smscSock.setSendBufferSize(buffsize*5); //increase buffer size
            buffsize = smscSock.getSendBufferSize();
            fromSmsc = smscSock.getInputStream();
            toSmsc = smscSock.getOutputStream();
            if (willLog(Logger.LOG_VERBOSE)) {
                log.logString("SMSCom connected", Logger.LOG_VERBOSE);
            }
            setState(SMSCOM_BINDING);
            reader = new SmscReader();
            reader.setDaemon(true);
            reader.start();
            return true;

        } catch (UnknownHostException e) {
            if (willLog(Logger.LOG_ERROR)) {
                log.logString("SMSCom could not connect. Unknown host "
                              + hostname, Logger.LOG_ERROR);
            }
        } catch (IOException e) {
            if (willLog(Logger.LOG_VERBOSE)) {
                log.logString("SMSCom connect failed:"
                              + e, Logger.LOG_VERBOSE);
            }
        }
        return false;
    }

    /**
     * Called when some part of the code detects that the SMSC has closed the
     * connection.
     */
    public void disconnected() {
        if (state < SMSCOM_DISCONNECTING) {
            if (willLog(Logger.LOG_VERBOSE) && state < SMSCOM_UNBINDING) {
                log.logString("Connection closed by the SMSC " + hostname, Logger.LOG_VERBOSE);
                closing = true;
            }
            disconnect();
        }
    }

    /**
     * Disconnects from the SMSC.
     */
    protected void disconnect() {

    	//only log once in case called more than once.
        if (willLog(Logger.LOG_VERBOSE) && getState() != SMSCOM_EXITING) {
            log.logString("disconnecting from " + hostname, Logger.LOG_VERBOSE);
        }
        try { if (fromSmsc != null) { fromSmsc.close(); }} catch (IOException e) { ; }
        try { if (toSmsc != null) { toSmsc.close(); }} catch (IOException e) { ; }
        try { if (smscSock != null) { smscSock.close(); }} catch (IOException e) { ; }
        smscSock = null;
        setState(SMSCOM_EXITING); // this connection is now unusable.
        reader = null;

        if (connectionHandler != null) { connectionHandler.connectionDown(""); }
        protocolCleanup();
    }


    //PROTOCOL-SPECIFIC METHODS

    /**
     * Logs in to the SMSC using the login parameters in SMSCom.
     * Protocol-specific.
     *@return true iff login succeeded.
     */
    protected abstract boolean protocolLogin();

    /**
     * Logs out from the SMSC.
     * Protocol-specific.
     */
    protected abstract void protocolLogout();

    /**
     * Sends a poll message to the SMSC and waits for a reply.
     * Protocol-specific.
     *@return true iff the connection to the SMSC is working.
     */
    protected abstract boolean protocolPoll();

    /**
     * Submits a short message to the SMSC.
     * Protocol-specific.
     *@param adr - the receiver address.
     *@param org - the sender address.
     *@param msg - the message to send.
     *@throws SMSComException if the sending fails for some reason.
     */
    protected abstract void protocolSend(SMSAddress adr, SMSAddress org, SMSMessage msg, int smsRequestId, SMSResultHandler rh) throws SMSComException;

    /**
        * Submits a short message to the SMSC.
        * Protocol-specific.
        *@param to - the receiver address.
        *@param from - the sender address.
        *@param serviceType - the service type to cancel.
        *@throws SMSComException if the sending fails for some reason.
        */
       protected abstract void protocolCancel(SMSAddress to, SMSAddress from, String serviceType) throws SMSComException;


    /**
     * Reads a message from the SMSC and handles it.
     * Protocol-specific.
     */
    protected abstract void protocolReadAndHandleMessage() throws IOException;

    /**
     * This method is called after a session is closed, in case a subclass
     * needs to do some cleanup.
     * Protocol-specific.
     */
    protected abstract void protocolCleanup();


    //UTILITY METHODS

    /**
     * Convenience method to check if a message of a particular level shall be
     * logged.
     * @param level the log level to check.
     * @return true if the message shall be logged.
     */
    public boolean willLog(int level) {
        return log != null && log.ifLog(level);
    }

    /**
     * Sets the connection state. The transition is logged if log is on.
     */
    protected void setState(int st) {
        if (state == SMSCOM_EXITING) { return; }
        if (willLog(Logger.LOG_DEBUG)) {
            log.logString("Connection state " + _stateStrings[state] + "->" + _stateStrings[st], Logger.LOG_DEBUG);
        }
        state = st;
    }

    /**
     * Handles error in the message data.
     *@param msg - message describing the error.
     */
    protected void dataError(String msg) throws SMSComException {
        switch (errCodeAction) {
        case HANDLE_ERRORS:
            throw new SMSComDataException(msg);
        case LOG_ERRORS:
            log.logString(msg, Logger.LOG_ERROR);
            break;
        default:
        }
    }

    /**
     * Handles phone off error.
     *@param msg - message describing the error.
     */
    protected void phoneOffError(String msg) throws SMSComException {
        switch (errCodeAction) {
        case HANDLE_ERRORS:
            throw new SMSComPhoneOffException(msg);
        case LOG_ERRORS:
            log.logString(msg, Logger.LOG_ERROR);
            break;
        default:
        }
    }

    /**
     * Handles errors when the SMSC is overloaded.
     *@param msg - message describing the error.
     */
    protected void loadError(String msg) throws SMSComException {
        switch (errCodeAction) {
        case HANDLE_ERRORS:
            throw new SMSComLoadException(msg);
        case LOG_ERRORS:
            log.logString(msg, Logger.LOG_ERROR);
            break;
        default:
        }
    }

    /**
     * Handles errors when the SMSC has successfully rebound after a bind error
     *@param msg - message describing the error.
     */
    protected void rebindError(String msg) throws SMSComException {
        switch (errCodeAction) {
        case HANDLE_ERRORS:
            throw new SMSComReBindException(msg);
        case LOG_ERRORS:
            log.logString(msg, Logger.LOG_ERROR);
            break;
        default:
        }
    }

    /**
     * Handles errors that are not load or data errors.
     *@param msg - message describing the error.
     */
    protected void generalError(String msg) throws SMSComException {
        switch (errCodeAction) {
        case HANDLE_ERRORS:
            throw new SMSComException(msg);
        case LOG_ERRORS:
            log.logString(msg, Logger.LOG_ERROR);
            break;
        default:
        }
    }

    /**
     * writeBuffer writes a byte array to the SMS-C. This method acts as a
     * single place to put the spy functionality.
     *@param buf - the bytes to write.
     *@throws IOException if writing fails
     */
    synchronized protected void writeBuffer(byte[] buf) throws IOException  {

        if (spy != null) {
            spy.toSMSC(buf);
        }

        try {
            if (toSmsc != null)
                { toSmsc.write(buf); }
            else
                { throw new IOException("Output Stream not initialized.");}
            
        } catch (IOException e) {
        	disconnected(); //if you don't do this the senders never get notified until timeout
        	throw e;
        }
        }


    // GETTERS AND SETTERS

    /**
     * Get the thread that is sending requests to the SMSC.
     * @return SMSConnection
     */
    public SMSConnection getRequestSenderThread() {
        return requestSenderThread;
    }
    
    /**
     * Set the thread that is sending requests to the SMSC.
     * @param requestSenderThread  the sending thread
     */
    public void setRequestSenderThread(SMSConnection requestSenderThread) {
        this.requestSenderThread = requestSenderThread;
    }
    
    /**
     * Get the value of errCodeAction.
     * @return value of errCodeAction.*/
    public int getErrCodeAction() { return errCodeAction; }
    /**
     * Set the value of errCodeAction.
     * @param v  Value to assign to errCodeAction.*/
    public void setErrCodeAction(int  v) {
        switch (v) {
        case IGNORE_ERRORS:
        case LOG_ERRORS:
            break;
        default:
            v = HANDLE_ERRORS;
        }
        this.errCodeAction = v;
    }

    /**Get the system type of the client.
     * @return the system type*/
    public String getSystemType() { return systemType; }
    /**Set the system type of the client.
     * @param systemType the new system type*/
    public void setSystemType(String systemType) { this.systemType = systemType; }

    /**Get the user name for logging in to the SMS-C.
     * @return the user name*/
    public String getUserName() { return user; }
    /**Set the user name for logging in to the SMS-C.
     * @param user the new user name*/
    public void setUserName(String user) { this.user = user; }

    /**Get the password for logging in to the SMS-C.
     * @return the password*/
    public String getPassword() { return password; }
    /**Set the password for logging in to the SMS-C.
     * @param password the new password*/
    public void setPassword(String password) { this.password = password; }

    /**Get the address range for received messages.
     *@return the address range*/
    public SMSAddress getAddressRange() { return addressRange; }
    /**Set the address range for received messages.
     *@param range - the address range.*/
    public void setAddressRange(SMSAddress range) { this.addressRange = range; }

    /**Get the host name of the SMS-C.
     * @return the host name*/
    public String getHostName() { return hostname; }
    /**Set the host name of the SMS-C.
     * @param hostname the new host name*/
    public void setHostName(String hostname) { this.hostname = hostname; }

    /**Get the port number of the SMS-C.
     * @return the port number*/
    public int getPortNumber() { return portnumber; }
    /**Set the port number of the SMS-C.
     * @param portnumber the new port number*/
    public void setPortNumber(int portnumber) { this.portnumber = portnumber; }

    /**
     * Tells if the SMSCom handles messages delivered from the SMSC.
     *@return true if messages shall be received.
     */
    public boolean isReceive() { return _receive; }
    /**
     * Determine if SMSCom shall handle messages delivered from the SMSC.
     *@param receive - tells if SMSCom shall receive messages from the SMSC.
     */
    public void setReceive(boolean receive) { _receive = receive; }
    
    /**
     * Tells if the SMSCom sends messages to the SMSC.
     *@return true if messages shall be sent.
     */
    public boolean isSend() { return _send; }
    /**
     * Determine if SMSCom shall send messages to the SMSC.
     *@param send - tells if SMSCom shall send messages to the SMSC.
     */
    public void setSend(boolean send) { _send = send; }

    /**Get the timeout for communication with the SMS-C.
     * @return the timeout.*/
    public int getTimeout() { return timeout; }
    /**Set the timeout for communication with the SMS-C. Note that if the wait
     * for connect flag is true, connection attempts will not give up just
     * because the timeout expires.
     * @param timeout the new timeout.*/
    public void setTimeout(int timeout) { this.timeout = timeout; }

    /**Get the receiver for incoming delivery receipts.
     * @return the SMS receiver.*/
    public PhoneOnEventListener getReceiptReceiver() { return receiptReceiver; }
    /**Set the receiver of incoming delivery receipts. If this is set to non-null, it
     * also make SMSCom login to the SMSC in bidirectional mode.
     * @param recevier - the delivery receipt receiver.*/
    public void setReceiptReceiver(PhoneOnEventListener r) { this.receiptReceiver = r; }

    /**Get the spy that eavesdrops on all SMS-C communication.
     * @return the spy*/
    public CommSpy getSpy() { return spy; }
    /**Set the spy that eavesdrops on all SMS-C communication. If null, SMSCom
     * will not copy the SMS-C communication anywhere.
     * @param spy the new spy*/
    public void setSpy(CommSpy spy) { this.spy = spy; }

    /**Get the log handler for messages about the SMS-C communication.
     * @return the log handler*/
    public Logger getLogger() { return log; }
    /**Set the log handler for messages about the SMS-C communication. If null, SMSCom
     * will not send log messages anywhere.
     * @param log the new log handler*/
    public void setLogger(Logger log) {
        this.log = log;
    }

    /**
     * Get the state .
     *@return the state*/
    public int getState() { return state; }
    
    /* indicates the connection is shutting down/shut down
     * return true if connection is closing/closed.
    */
    public boolean isClosing() {
        return closing;
    }


    /**
     * Get the SMSCom state in printable form.
     * @return String of the format <code>
     * {SMSCom&nbsp;systemType=&nbsp;user=john&nbsp;password=secret&nbsp;
     * hostname=smsc&nbsp;portnumber=5016&nbsp;isOk=true&nbsp;spy=null&nbsp;
     * log=null}
     * </code>
     */
    public String toString() {
        return "{SMSCom"
            + " protocol=" + protocol
            + " user=" + user
            + " password=" + password
            + " systemtype=" + systemType
            + " address range=" + addressRange
            + " hostname=" + hostname
            + " portnumber=" + portnumber
            + " timeout=" + timeout
            + " state=" + _stateStrings[state]
            + " receive=" + _receive
            + " spy=" + spy
            + " log=" + log + "}";
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


    //READER THREAD

    /**
     * Loops forever, reading protocol messages, and forwarding them to the
     * protocol handler.
     */
    private class SmscReader extends Thread {



        /**
         * Constructor
         */
        public SmscReader() {
            super("SmscReader-"+SmscReaderinst++);
        }

        /**
         * run waits for someone else to connect. Then it reads all protocol messages
         * that arrive, and forward them to the protocol handler.
         */
        public void run() {

        	if (willLog(Logger.LOG_VERBOSE)) {
        		log.logString("Thread starting: " + this.getName(), Logger.LOG_VERBOSE);
        		/* Run this at higher priority so responses get read as soon as
        		 * possible, because IO is slow, most of the time this won't
        		 * be running anyway.
        		 *
        		 * This speeds up the bottle neck of processing the sms socket.
        		 */
        		setPriority(Thread.NORM_PRIORITY+1);
            }
            while (state != SMSCOM_EXITING) { //Loop until exit, no matter what
                try {
                    while (state != SMSCOM_EXITING) { //Loop until exit, as long as nothing unexpected happens
                        try {
                            protocolReadAndHandleMessage();
                        } catch (InterruptedIOException e) {
                            if (willLog(Logger.LOG_DEBUG) && state < SMSCOM_DISCONNECTING) {
                                log.logString("Nothing to read. ", Logger.LOG_DEBUG);
                            }
                        } catch (EOFException e) {
        					if (state < SMSCOM_UNBINDING) {
        						log.logString("EOFException, disconnected from " + hostname + "!", Logger.LOG_ERROR);
        						closing = true;
        					}
                            disconnected();
                        } catch (SocketException e) {
        					if (state < SMSCOM_UNBINDING) {
        						log.logString("SocketException, disconnected from " + hostname + "!", Logger.LOG_ERROR);
        						closing = true;
        					}
        					disconnected();
        				} catch (IOException e) {
        					if (state < SMSCOM_UNBINDING) {
                                    log.logString("Unexpected (" + hostname + "): " + e, Logger.LOG_ERROR);
                                }
                            disconnected();
                        }
                    }
                } catch (Exception e) {
                    if (willLog(Logger.LOG_ERROR)) {
                        log.logString("Unexpected (" + hostname + "): " + SMSCom.stackTrace(e), Logger.LOG_ERROR);
                    }
                } catch (OutOfMemoryError e) {
                    if (willLog(Logger.LOG_ERROR)) {
                        log.logString("Out of memory, shutting down..."
                                      + SMSCom.stackTrace(e), Logger.LOG_ERROR);
                    }
        			ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
        			break;
                }
            }
            if (willLog(Logger.LOG_VERBOSE)) {
                log.logString("Thread stopping", Logger.LOG_VERBOSE);
            }
        	while(true)
        	{
        		try {
        			sleep(2000); //allow things to stop properly before exiting.
        		} catch (InterruptedException e) {;}
        		break;
        	}

        }
    }
    
    /**
     * Profiling method.
     * This method does not check if profiling is enabled; the calling method is responsible for checking.
     * This was done to prevent unnecessary String object creation for check point name when profiling is not enabled (normal live traffic case).
     * Only if profiling is enabled, calling method should create check point name and call this method.
     * @param checkPoint - name of profiling check point
     */
    protected static void profilerAgentCheckPoint(String checkPoint) {
        Object perf = null;
        try {
            perf = CommonOamManager.profilerAgent.enterCheckpoint(checkPoint);
        } finally {
            CommonOamManager.profilerAgent.exitCheckpoint(perf);
        }
    }
}
