/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail.imap;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.management.ManagementInfo; 
import com.mobeon.ntf.management.ManagementCounter; 
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.mail.MailboxPoller;
import org.eclipse.angus.mail.imap.*;
import java.util.*;
import jakarta.activation.*;
import jakarta.mail.*;
import com.mobeon.ntf.util.time.NtfTime;

/**
 * ImapClient encapsulates java mail and provides a generic IMAP interface
 * to its users.
 * Only a subset of IMAP client interface is implemented in this iteration.
 */
public class ImapClient {
    /**
     * IPMS Logger is used for logging
     */
    private final static Logger log = Logger.getLogger(ImapClient.class); 
    static final int LOGGED_OUT_STATE = 0;
    static final int NON_AUTHENTICATED_STATE = 1;
    static final int AUTHENTICATED_STATE = 2;
    static final int SELECTED_STATE = 3;
    
    /**
     * Store is an object representing the Message Store
     */
    private Store store = null;
    
    /**
     * Session represents an IMAP session
     */
    private Session session = null;
    
    /**
     * Folder represents a mail box
     */
    private Folder folder = null;
    
    /**
     * is used by java mail to setup a session with the IMAP server
     */
    private Properties props;
    
    /**
     * The name of the inbox folder
     */
    private String mbox = "INBOX";
    
    /**
     * Entity class containting the attributes pertaining to the IMAP server
     */
    private ImapServer server = null;
    

    
    /**
     * is used to maintain the client state
     */
    private int clientState;
    
    private MailboxPoller mailboxPoller = null;
    
    
    /** Message identities for reduced logging*/
    private int noSessionMessageId;
    private int expungeFolderException;
    private int closeFolderException;
    private int openFolderException;
    private int disconnectFromStore;
    private int noFolderExist;
    private int noStoreExist;
    private int connectToStoreException;
    private int expungeMessagingException;
    private int getMessageException;
    private int storeIsNull;
    private int connectException;
    private int lastConnectTime; //Time of last connect attempt
    
    private int mailboxnumber;
    
    private static ManagementCounter successCounter = null;
    private static ManagementCounter failCounter = null;
    
    // This class is not used anymore but kept as historical reference
    public static ImapClient[] imapClientArray = new ImapClient[1];//Config.getImapThreads()];
    
     /**
     * Creates an instance of ImapClient.
     */
    public ImapClient(int mailboxnumber, MailboxPoller poller) {
	this.mailboxnumber= mailboxnumber;
	server = new ImapServer();
	noSessionMessageId= log.createMessageId(100);
	expungeFolderException= log.createMessageId(100);
	closeFolderException= log.createMessageId(100);
	openFolderException= log.createMessageId(100);
	noFolderExist= log.createMessageId(100);
	disconnectFromStore= log.createMessageId(100);
	noStoreExist= log.createMessageId(100);
	connectToStoreException= log.createMessageId(100);
	expungeMessagingException= log.createMessageId(100);
	getMessageException= log.createMessageId(100);
	storeIsNull= log.createMessageId(100);
	connectException= log.createMessageId(100);
	setClientState(LOGGED_OUT_STATE);
	lastConnectTime= 0;
	imapClientArray[mailboxnumber] = this;    
        this.mailboxPoller = poller;
        
        synchronized (this) {
            if( successCounter == null ) {
                successCounter = ManagementInfo.get().getCounter("Storage"
                , ManagementCounter.CounterType.SUCCESS );
            }
            if( failCounter == null ) {
                failCounter = ManagementInfo.get().getCounter("Storage"
                , ManagementCounter.CounterType.FAIL );
            }
        }
        
    }
    
    public static Message getMessage(int messageUid, int threadNumber){
	try{
	    
	    if(imapClientArray[threadNumber].folder == null){
		imapClientArray[threadNumber].getFolder();
	    }
	    return ((IMAPFolder) imapClientArray[threadNumber].getFolder()).getMessageByUID(messageUid);
	}catch (MessagingException e) {
	    return null;	
	}
    }

    /**
     * Get the age of the current connection. This will return the time since
     * the last connection attempt was started, even if there is no connection
     * right now.
     *@return the number of seconds since the last connection.
     */
    public int getConnectionAge() {
        return (NtfTime.now - lastConnectTime);
    }

    /**
     * getFolder gets a Folder doing all necessary connections and logins in the
     * process. This method does not return until it succeeds.
     * @return a Folder that can be used to communicate with the mail store.
     */
    public synchronized Folder getFolder() {
	
	log.logMessage("ImapClient getting Folder", log.L_VERBOSE);
	
	while (true) {
	    lastConnectTime= NtfTime.now;
	    dropFolder();
	    
	    if (getSession() &&
		connectToStore() &&
		openFolder(mbox)) {
                return folder;
	    } 
	}
    }
    
    
    /**
     * closes the connection to the IMAP store and logs out
     * @return void
     */
    public synchronized void dropFolder() {
	//Note that the switch statement is designed to fall through all statements
	//below the selected entry point, to clean up all steps of a getFolder that 
	//have succeeded
	switch (getClientState()) {
	case SELECTED_STATE:
	    closeFolder();
	case AUTHENTICATED_STATE:
	    disconnectFromStore();
	case NON_AUTHENTICATED_STATE:
	    dropSession();
	case LOGGED_OUT_STATE:
	default:
	}
    }


    /**
     * returns the current state of the IMAP client.
     * This method can be accessed from multiple threads.
     *
     * the return value can be one of the following values:
     * 0: Logged out.
     * 1: Non-authenticated
     * 2: Authenticated
     * 3: Selected
     */
    public int getClientState() {
	return clientState;
    }


    /**
     * set the current state of the client
     * @param state the new value of the client state.
     */
    public void setClientState(int state) {
	clientState = state;
    }
    
    
    /**
     * permanently removes messages with their DELETED flag set from the IMAP store.
     * @param none
     */
    public void expunge() throws MessagingException {
	Message[] msgs = null;
	if (folder == null) {
	    folder= getFolder();
	}
	try {
	    msgs = folder.expunge();
	    if(log.isActive(expungeMessagingException))
		log.logReducedOff(expungeMessagingException,"OK ImapClient: expunging messages",
				  log.L_ERROR);
	    log.logMessage("ImapClient expunged " + msgs.length + " messages", log.L_VERBOSE);
	}
	catch (MessagingException e) {
	    log.logReduced(expungeMessagingException, "ImapClient: expunge exception " + e, log.L_VERBOSE);
	    mailboxPoller.reportError("expunge()", e, false);
	    folder= null;
	}
    }


    /**
     * creates a session instance to be used to connect to the IMAP store.
     * @return true if succeeds and false if fails
     */
    private boolean getSession() {
	props = System.getProperties();
	
	props.setProperty("mail.imap.timeout", "" + Config.getImapTimeout());
	props.setProperty("mail.imap.connectiontimeout", "" + Config.getImapTimeout());
	
	props.setProperty("mail.smtp.port", "25");
	props.setProperty("mail.smtp.timeout", "" + Config.getImapTimeout());
	props.setProperty("mail.smtp.connectiontimeout", "" + Config.getImapTimeout());
	
	session = Session.getDefaultInstance(props, null);
	if (session == null) {
	    log.logReduced(noSessionMessageId,
			   "ImapClient: could not get a Session for" +
			   "\n\tmail.imap.host=" + props.getProperty("mail.imap.host") +
			   "\n\tmail.imap.port=" + props.getProperty("mail.imap.port") +
			   "\n\tmail.imap.timeout=" + props.getProperty("mail.imap.timeout") +
			   "\n\tmail.imap.connectiontimeout=" + props.getProperty("mail.imap.connectiontimeout") +
			   "\n\tmail.smtp.host=" + props.getProperty("mail.smtp.host") +
			   "\n\tmail.smtp.port=" + props.getProperty("mail.smtp.port") +
			   "\n\tmail.smtp.timeout" + props.getProperty("mail.smtp.timeout") +
			   "\n\tmail.smtp.connectiontimeout" + props.getProperty("mail.smtp.connectiontimeout") +
			   log.L_ERROR);
	    return false;
	} else {
	    log.logReducedOff(noSessionMessageId, "OK ImapClient: could get session", log.L_ERROR);
	}
	
	setClientState(NON_AUTHENTICATED_STATE);
	return true;
    }

    /****************************************************************
     * Drops the current session
     */
    private void dropSession() {
	session= null;
	setClientState(LOGGED_OUT_STATE);
    }
    
    
    /**
     * Creates a store object, connects to the store and authenticate with the
     * server. This method can not be used until you have a session, and getSession
     * is responsible for updating the server object with connection parameters.
     * @param userName and passWord for the user to be authenticated with the
     * server
     * @return true upon succeeding and false otherwise.
     */
    private boolean connectToStore() {
	
	if (session == null) return false;
	if (server == null) return false;
	
	try {
	    store= session.getStore(server.getProtocol());
	    if(log.isActive(connectToStoreException))
		log.logReducedOff(connectToStoreException, "OK ImapClient: Could get store", 
				  log.L_ERROR);
	}
	catch (NoSuchProviderException e) {
	    log.logReduced(connectToStoreException, 
			   "ImapClient: exception when getting Store for" +
			   server.getProtocol() + ": " + e, log.L_ERROR);
	    store= null;
            return false;
	}
	
	if (store == null) {
	    log.logReduced(storeIsNull, "ImapClient: could not get Store for" + 
			   server.getProtocol(), log.L_ERROR);
            return false;
	}
	
	if(log.isActive(storeIsNull))
	    log.logReducedOff(storeIsNull, "OK ImapClient: store is valid ",
			   log.L_ERROR);
	
        // Authenticate with the IMAP server
	try {
	    if (server.getUserName().compareToIgnoreCase("gnotification") == 0) {
		//Log in to a WE1.0 system
		store.connect(server.getHostName(), server.getPortNumber(),
			      server.getUserName(), server.getPassWd());
	    } else {
		// Log in to newer systems
		store.connect(server.getHostName(), server.getPortNumber(),
			      server.getUserName() + "_" + mailboxnumber, server.getPassWd());
	    }
            mailboxPoller.statusUp();
            successCounter.incr();
            if(log.isActive(connectException))
		log.logReducedOff(connectException,"OK ImapClient: restored connection", log.L_ERROR);
	}
	catch (MessagingException e) {
	    log.logReduced(connectException,"ImapClient: exception when connecting to store: " +
			   e, log.L_VERBOSE);
            mailboxPoller.reportError("connectToStore()", e, false);
	    store= null;
            mailboxPoller.statusDown();
            failCounter.incr();
            
	    return false;
	}
	catch (IllegalStateException e) {
	    log.logMessage("ImapClient: connecting to store when already connected: " + e, log.L_ERROR);
	    store= null;
	    return false;
	}
	
	setClientState(AUTHENTICATED_STATE);
	return true;
    }
    
    /**
     * closes the connection to the IMAP server and change the client state
     * to logged out.
     *
     * @param none
     * @return void
     */
    private void disconnectFromStore() {
	if (store != null) {
	    try {
		store.close();
		if(log.isActive(disconnectFromStore))
		    log.logReducedOff(disconnectFromStore, "OK ImapClient: could close store",log.L_ERROR);
	    }
	    catch (MessagingException e) {
		log.logReduced(disconnectFromStore, "ImapClient: exception closing store: " + 
			       e, log.L_ERROR);
	    }
	}
	
	store = null;
	setClientState(NON_AUTHENTICATED_STATE);
	return;
    }
    
    
    /**
     * selects the inbox folder. This method can only be used in authenticated state.
     * If it is called in any other state, it will return false
     * @param name of the mail box to connect to.
     * @return true if OK and false otherwise.
     */
    private boolean openFolder(String mBox) {
	if (store == null) return false;

	try {
	    folder= store.getDefaultFolder();
	    if (folder == null) {
		log.logMessage("ImapClient: could not get default folder", log.L_ERROR);
		return false;
	    }
	    if(log.isActive(openFolderException))
		log.logReducedOff(openFolderException, "OK ImapClient: could get default folder", 
				  log.L_ERROR);
	} catch (FolderNotFoundException e) {
	    folder= null;
	    return false;
	} catch (MessagingException e) {
	    log.logReduced(openFolderException, "ImapClient: exception getting default folder : " +
			   e, log.L_VERBOSE);
            mailboxPoller.reportError("openFolder()", e, false);
	    folder= null;
	    return false;
	}

	try {
	    folder= folder.getFolder(mBox);
	    if (folder == null) {
		log.logMessage("ImapClient: could not get folder " + mBox, log.L_ERROR);
		return false;
	    }
	} catch (FolderNotFoundException e) {
	    folder= null;
	    return false;
	} catch (MessagingException e) {
	    log.logMessage("ImapClient: exception getting folder " + mBox + ": " + e, log.L_VERBOSE);
	    mailboxPoller.reportError("openFolder() failed to get " + mBox, e, false);
	    folder= null;
	    return false;
	}
	
	try {
	    folder.open(Folder.READ_WRITE);
	} catch (FolderNotFoundException e) {
	    folder= null;
	    return false;
	} catch (MessagingException e) {
	    log.logMessage("ImapClient: exception when opening folder: " + e, log.L_VERBOSE);
	    mailboxPoller.reportError("openFolder() failed to open", e, false);
	    folder= null;
	    return false;
	} catch (IllegalStateException e) {
	    log.logMessage("ImapClient: opening an open folder: " + e, log.L_VERBOSE);
            mailboxPoller.reportError("openFolder() in wrong state", e, false);
	    folder= null;
	    return false;
	}

	setClientState(SELECTED_STATE);
	return true;
    }

    /**
     * Closes the selected mail box and returns to Authenticated state.
     *
     * @param none
     * @return void
     */
    private void closeFolder() {
	if (folder != null && folder.isOpen()) {
	    // OBS! because of a bug in java mail 1.2 close(true) does not expunge
	    // the messages with DELETED flag set.
	    // therefore, expunge must be called explicitly.
	    try {
		folder.expunge();
		if(log.isActive(expungeFolderException))
		    log.logReducedOff(expungeFolderException,
				      "OK ImapClient: expunging folder: ", log.L_ERROR);
	    }
	    catch (MessagingException e) {
		log.logReduced(expungeFolderException, "ImapClient: exception when expunging folder: " +
			       e, log.L_ERROR);
	    }
	    
	    try {
		folder.close(false);
		if(log.isActive(closeFolderException))
		    log.logReducedOff(closeFolderException, "OK ImapClient: closing folder: ",
				      log.L_ERROR);
	    }
	    catch (MessagingException e) {
		log.logReduced(closeFolderException, "ImapClient: exception when closing folder: " + 
			       e, log.L_ERROR);
	    }
	}

	folder= null;
	setClientState(AUTHENTICATED_STATE);
	return;
    }
} // ImapClient




