/*
 * Copyright (c) 2004 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.session.SIP;
import org.apache.log4j.Logger;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;
import java.text.ParseException;

import com.mobeon.session.SIP.event.InviteEventHandler;
import com.mobeon.session.SIP.event.InfoEventHandler;
import com.mobeon.session.SIP.event.ByeEventHandler;
import com.mobeon.session.SIP.event.AckEventHandler;
import com.mobeon.session.SessionConnection;
import com.mobeon.session.SessionServer;
import com.mobeon.frontend.ControlSignal;
import com.mobeon.frontend.ControlSignalFactory;
import com.mobeon.frontend.DTMFSignal;
import com.mobeon.frontend.Stream;
import com.mobeon.event.MASEventDispatcher;
import com.mobeon.application.graph.GraphFactory;
import com.mobeon.main;
import com.mobeon.executor.Traverser;


public class SIPConnection extends Thread implements SipListener, SessionConnection {
    private SIPServer server;
    private Dialog dialog;
    private LinkedList requestQ;
    private LinkedList responseQ;
    private LinkedList timeoutQ;
    private boolean doWork;
    private Object mutex;
    private String URI;
    private String peerURI = "";
    private String localURI = "";
    private int localPort;
    private int remotePort;
    private String remoteIP;
    // static Logger logger = Logger.getLogger(SIPConnection.class);
    static Logger logger = Logger.getLogger("session");

    private ArrayList inStreamURLs;
    private ArrayList outStreamURLs;
    private String codec;

    private Vector inStreams;
    private Vector outStreams;

    private InviteEventHandler inviteHandler;
    private InfoEventHandler infoHandler;
    private ByeEventHandler byeHandler;
    private AckEventHandler ackHandler;

    private ControlSignal controlSig;
    private Hashtable sessionData;
    private MASEventDispatcher dispatcher;
    private Traverser traverser;
    private boolean hasEnded;


    public SIPConnection(String fromURI, String toURI, SIPServer server) {
        this.server = server;
        requestQ = new LinkedList();
        responseQ = new LinkedList();
        timeoutQ = new LinkedList();
        peerURI = fromURI;
        localURI = toURI;
        inStreamURLs = new ArrayList();
        outStreamURLs = new ArrayList();
        inStreams = new Vector();
        outStreams = new Vector();
        URI = peerURI + ":" + localURI;
        inviteHandler = new InviteEventHandler(server, this);
        infoHandler = new InfoEventHandler(server, this);
        byeHandler = new ByeEventHandler(server, this);
        ackHandler = new AckEventHandler(server, this);
        sessionData = new Hashtable();
        dispatcher = new MASEventDispatcher();
        controlSig = new DTMFSignal(this, dispatcher, 0) ;
        sessionData.put("MESSAGEQ", dispatcher);
        sessionData.put("CONTROLSIGNAL", controlSig);
        // sessionData.put("STREAM", new StdOutStream());
        doWork = true;
        mutex = new Object();
        traverser = new Traverser(this, dispatcher, controlSig);
        traverser.setRoot(GraphFactory.getInstance().getApplication());
        hasEnded = false;
     }

    public void releaseObjects() {
        inStreamURLs = null;
        outStreamURLs = null;
        peerURI = null;
        localURI = null;

        if (traverser != null && traverser.getHasEnded()) {
            traverser = null;
            URI = null;
            server = null;
            controlSig = null;
            dispatcher = null;
            sessionData = null;
            ackHandler = null;
            byeHandler = null;
            inviteHandler = null;
            infoHandler = null;
            requestQ = null;
            responseQ = null;
            timeoutQ = null;
        }
    }

    public boolean isHasEnded() {
        boolean ret = false;
        synchronized(mutex) {
            ret =  hasEnded;
        }
        return ret;
    }

    public void setHasEnded(boolean hasEnded) {
        synchronized(mutex) {
            this.hasEnded = hasEnded;
        }
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }
    
    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public void addInStreamURL(String url) {
        inStreamURLs.add(url);
    }

    public void addOutStreamURL(String url) {
        outStreamURLs.add(url);
    }

    public void addInStream(Stream s) {
        inStreams.add(s);
        traverser.setInputStream(s);
    }

    public void addOutStream(Stream s) {
        outStreams.add(s);
        // traverser.setOutputStream(s);
    }

    public ArrayList getInStreamURLs() {
        return inStreamURLs;
    }

    public ArrayList getOutStreamURLs() {
        return outStreamURLs;
    }

    public List getInStreams() {
        return inStreams;
    }

    public List getOutStreams() {
        return outStreams;
    }

    public String getCaller() {
        return peerURI;
    }

    public String getCallee() {
        return localURI;
    }

    public SessionServer getServer() {
        return server;
    }

    public Traverser getTraverser() {
        return traverser;
    }

    public ControlSignal getControlSig() {
        return controlSig;
    }

    public String getLocalURI() {
        return localURI;
    }

    public String getPeerURI() {
        return peerURI;
    }

    public Hashtable getSessionData() {
        return sessionData;
    }

    public InviteEventHandler getInviteHandler() {
        return inviteHandler;
    }

    public InfoEventHandler getInfoHandler() {
        return infoHandler;
    }

    public ByeEventHandler getByeHandler() {
        return byeHandler;
    }

    public AckEventHandler getAckHandler() {
        return ackHandler;
    }

    public String getURI() {
        return URI;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public boolean  keepRunning() {
        synchronized (mutex) {
            return doWork;
        }
    }

    public synchronized void setRunning(boolean state) {
        synchronized(mutex) {
            doWork = state;
            if (doWork == false) {
                if (traverser != null) {
                    traverser.setStopRunning(true);
                    traverser = null;
                }
                else {
                    logger.debug("TreeTraverser is already null!");
                }
            }
            mutex.notifyAll();
            this.interrupt();
        }
    }
    private void closeStreams() {
        synchronized(mutex){
            logger.debug("Closing streams...");
            for (Iterator it = inStreams.iterator();it.hasNext();) {
                Stream s = (Stream) it.next();
                s.interrupt(true);
                s.close();
            }
            inStreams.clear();
            for (Iterator it = outStreams.iterator();it.hasNext();) {
                Stream s = (Stream) it.next();
                s.interrupt(true);
                s.close();
            }
            outStreams.clear();
            logger.debug("Streams closed!");
        }
    }

    public void endConnection() {
        closeStreams();
        if (keepRunning())  {
            byeHandler.sendBye();
        }
    }

    public Stream getOutputStream(int index) {
        synchronized(mutex) {
            if (index < outStreams.size())
                return (Stream) outStreams.get(index);
            else
                return null;
        }
    }

    public void addRequest(RequestEvent re) {
        if (requestQ == null) // Session has terminated
            return ;
        synchronized(requestQ) {
            requestQ.addLast(re);
            logger.debug("Request added");
        }
        synchronized(mutex) {
            mutex.notifyAll();
        }
    }

    public void addResponse(ResponseEvent re) {
        if (responseQ == null) // Session has terminated
            return ;
        synchronized(responseQ) {
            responseQ.addLast(re);
            logger.debug("Response added");
        }
        synchronized(mutex) {
            mutex.notifyAll();
        }
    }

    public void addTimeout(TimeoutEvent re) {
        if (timeoutQ == null) // Session has terminated
            return ;
        synchronized(timeoutQ) {
            timeoutQ.addLast(re);
            logger.debug("Timeout added");
        }
        synchronized(mutex) {
            mutex.notifyAll();
        }
    }

    public boolean hasEvents() {
        boolean ret = false;
        synchronized (requestQ) {
            ret = (requestQ.size() > 0);
        }
        synchronized (responseQ) {
            ret = ( ret || responseQ.size() > 0);
        }
        synchronized (timeoutQ) {
            ret = ( ret || timeoutQ.size() > 0);
        }
        return ret;
    }

    public boolean hasEvents(LinkedList list) {
        synchronized (list) {
            return  (list.size() > 0);
        }
    }

    public Object getEvent(LinkedList list) {
        synchronized (list) {
            return list.removeFirst();
        }
    }


    public MASEventDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(MASEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    

	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransaction =
			requestEvent.getServerTransaction();

		logger.debug("Request "
				+ request.getMethod()
				+ " received at ");

		if (request.getMethod().equals(Request.INVITE)) {
		    inviteHandler.handle(requestEvent, serverTransaction);
		} else if (request.getMethod().equals(Request.ACK)) {
			ackHandler.handle(requestEvent, serverTransaction);
		} else if (request.getMethod().equals(Request.BYE)) {
			byeHandler.handle(requestEvent,serverTransaction);
		} else if (request.getMethod().equals(Request.INFO)) {
			infoHandler.handle(requestEvent,serverTransaction);
		} else {
            processUnknown(requestEvent, serverTransaction);
        }

	}


     /** Process unknown request.
	 */
	public void processUnknown(RequestEvent requestEvent, ServerTransaction serverTransaction) {
         logger.info("Unknown event received: 405 Method Not Allowed replied");
         Request request = requestEvent.getRequest();
         try {
             Response response=server.messageFactory.createResponse(Response.METHOD_NOT_ALLOWED,request);
             serverTransaction.sendResponse(response);
         } catch (SipException e) {
             logger.error("Failed to send response", e);
         } catch (ParseException e) {
             logger.error("Failed to parse", e);
         }
     }

	public void processResponse(ResponseEvent responseReceivedEvent) {
		Response response = (Response) responseReceivedEvent.getResponse();
		Transaction tid = responseReceivedEvent.getClientTransaction();
		logger.debug(
			"Response received with client transaction id "
				+ tid
				+ ":\n"
				+ response);
        if (response.getStatusCode() == Response.OK)  {
            CSeqHeader cseqheader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
            if (cseqheader.getMethod().equals(Request.INVITE)) {
                Dialog dialog = tid.getDialog();
                Request request = tid.getRequest();
                try {
                    dialog.sendAck(request);
                } catch (SipException e) {
                    logger.error("Fatal exception caught! Closing connection", e);
                    server.removeConnection(URI);
                    setHasEnded(true);
                    setRunning(false);
                }
            }
            else if (cseqheader.getMethod().equals(Request.BYE)) {
                logger.debug("Setting running to false");
                setRunning(false);
                server.removeConnection(URI);
                setHasEnded(true);

            }
            dialog = tid.getDialog();
        }
        else {
            logger.debug("A Non-OK response received ("+response.getStatusCode() + ")");
        }
	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
		Transaction transaction;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
		logger.debug("Transaction Time out");
	}
    /** startApplication
     *  Starts the voice/vidoe application by starting the treetraverser
     *
     */
    public void startApplication() {
        Thread thread = new Thread(traverser, "GraphTraverser");
        thread.start();
    }

	public void run() {
        while (keepRunning()) {
            try {
                while(!hasEvents())  {
                    synchronized(mutex) {
                        if (keepRunning() && !hasEvents())
                            mutex.wait();
                    }
                }
            } catch (InterruptedException e) {
                // Got interrupted
            }
            // Woke up and have work to do
            if (hasEvents(timeoutQ)) {
                processTimeout((TimeoutEvent) this.getEvent(timeoutQ));
            }
            if (hasEvents(responseQ)) {
                processResponse((ResponseEvent) this.getEvent(responseQ));
            }
            if (hasEvents(requestQ)) {
                processRequest((RequestEvent) this.getEvent(requestQ));
            }
        }
       // server.removeConnection(URI);
        logger.debug("SIPConnection exiting");
    }
}
