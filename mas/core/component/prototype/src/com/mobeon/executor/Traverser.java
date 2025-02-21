/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.executor;

import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.frontend.Stream;
import com.mobeon.frontend.ControlSignal;
import com.mobeon.event.MASEventListener;
import com.mobeon.event.MASEventDispatcher;
import com.mobeon.event.types.MASEvent;
import com.mobeon.event.types.*;
import com.mobeon.application.graph.*;
import com.mobeon.session.SessionConnection;
import com.mobeon.executor.eventhandler.*;
import com.mobeon.util.PromptManager;
import com.mobeon.util.ErrorCodes;
import com.mobeon.backend.TerminalSubscription;
import com.mobeon.backend.exception.NoUserException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Stack;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to travers the graph nodes, calling the execute method
 * of each node.
 */
public class Traverser implements MASEventListener, Runnable {
    public static Logger logger = Logger.getLogger(Traverser.class);

    private ECMAExecutor ecmaExecutor;
    private Scope exceptionScope;
    private Scope grammarScope;
    private Scope fields;
    private int docScopeCounter = 0;
    private int rootScopeCounter = 0;
    private boolean inRootDoc = false;
    private String rootURI = null;

    private HashMap dialogGotoables = new HashMap();
    private HashMap docGotoables = new HashMap();

    private HashMap subdialogParamerters = null;

    Stack suspensionStack = new Stack();


    private boolean continueRunning;
    private boolean hasEnded;
    private Object mutex;
    private Node current;
    private Node retakeNode = null;
    private Node root;

    private Stream outputStream;
    private Stream inputStream;
    private ControlSignal controlSignalQ;

    private SessionConnection session;
    private MASEventDispatcher dispatcher;
    // Event handlers
    private ErrorHandler errorHander;
    private ExitHandler exitHandler;
    private HangupHandler hangupHandler;
    private HelpHandler helpHandler;
    private NoInputHandler noInputHandler;
    private NoMatchHandler noMatchHandler;
    private UserInputHandler userInputHandler;
    private TimeoutHandler timeoutHandler;
    private int repromptCount = 1;
    private boolean promptPlayed = false;


    public Traverser(SessionConnection session, MASEventDispatcher dispatcher, ControlSignal cs)  {
        init();
        this.session = session;
        this.dispatcher = dispatcher;
        dispatcher.addMASEventListener(this);
        this.controlSignalQ = cs;
    }

    public Traverser(SessionConnection session, MASEventDispatcher dispatcher)  {
        init();
        this.session = session;
        this.dispatcher = dispatcher;
        dispatcher.addMASEventListener(this);
    }

    public Traverser(Node root)  {
        init();
        this.root = root;
        this.dispatcher = new MASEventDispatcher();
    }

    public Traverser()  {
        init();
        this.dispatcher = new MASEventDispatcher();        
    }


    public String getRootURI() {
        return rootURI;
    }

    public void setRootURI(String rootURI) {
        this.rootURI = rootURI;
    }

    private void init() {
        ecmaExecutor = new ECMAExecutor(this);
        exceptionScope = new Scope();
        grammarScope = new Scope();
        fields = new Scope();
        continueRunning = false;
        hasEnded = false;
        mutex = new Object();
        current = null;
        root = null;
        inputStream = null;
        outputStream = null;
        controlSignalQ = null;
        session = null;
        errorHander = new ErrorHandler(this);
        exitHandler = new ExitHandler(this);
        hangupHandler = new HangupHandler(this);
        helpHandler = new HelpHandler(this);
        noInputHandler = new NoInputHandler(this);
        noMatchHandler = new NoMatchHandler(this);
        userInputHandler = new UserInputHandler(this);
        timeoutHandler = new TimeoutHandler(this);
        dispatcher = null;
    }

    public void releaseObjects() {
        setHasEnded(true);
        ecmaExecutor = null;
        exceptionScope = null;
        grammarScope = null;
        suspensionStack = null;
        fields = null;
        current = null;
        root = null;
        rootURI = null;
        inputStream = null;
        outputStream = null;
        controlSignalQ = null;
        errorHander = null;
        exitHandler = null;
        hangupHandler = null;
        helpHandler = null;
        noInputHandler = null;
        noMatchHandler = null;
        userInputHandler = null;
        timeoutHandler = null;
        dispatcher = null;
        dialogGotoables = null;
        docGotoables = null;
        subdialogParamerters = null;
        session.releaseObjects();
        session = null;
        mutex = null;
    }

    public boolean getHasEnded() {
        boolean ret = false;
        if (mutex == null)
            return true;
        synchronized(mutex){
            ret = hasEnded;
        }
        return ret;
    }

    public void setHasEnded(boolean hasEnded) {
        if (mutex == null)
            return ;
        synchronized(mutex){
            this.hasEnded = hasEnded;
        }
    }

    public Node getRetakeNode() {
        Node ret = null;
        synchronized(mutex) {
            ret =  retakeNode;
        }
        return ret;
    }

    public void setRetakeNode(Node retakeNode) {
        synchronized(mutex) {
            this.retakeNode = retakeNode;
        }
    }

    public MASEventDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(MASEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        dispatcher.addMASEventListener(this);
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public ECMAExecutor getEcmaExecutor() {
        return ecmaExecutor;
    }

    public void setEcmaExecutor(ECMAExecutor e) {
        ecmaExecutor = e;
    }

    public Stream getInputStream() {
        List l =  session.getInStreams();
        if (l.size() > 0 ) {
            return (Stream) l.get(0);
        }
        return null;
    }

    public void setInputStream(Stream inputStream) {
        this.inputStream = inputStream;
    }

    public ControlSignal getControlSignalQ() {
        return controlSignalQ;
    }

    public void setControlSignalQ(ControlSignal controlSignalQ) {
        this.controlSignalQ = controlSignalQ;
    }

    public void setOutputStream(Stream outputStream) {
        this.outputStream = outputStream;
    }

    public Stream getOutputStream() {
        return session.getOutputStream(0);
    }

    public SessionConnection getSession() {
        return session;
    }

    public void setSession(SessionConnection session) {
        this.session = session;
    }

    public Scope getExceptionScope() {
        return exceptionScope;
    }

    public void setExceptionScope(Scope exceptionScope) {
        this.exceptionScope = exceptionScope;
    }

    public void newScope(boolean applicationScope) {
        ecmaExecutor.newScope();
        exceptionScope.newScope();
        grammarScope.newScope();
        fields.newScope();
        if (applicationScope && inRootDoc ){
            rootScopeCounter++;
            logger.debug("--- Enter new application scope (Scope depth = " + (rootScopeCounter + docScopeCounter) + ")");
        }
        else {
            docScopeCounter++;
            logger.debug("--- Enter new doc scope (Scope depth = " + (rootScopeCounter + docScopeCounter) + ")");
        }
    }

    public void leaveScope(boolean applicationScope) {
        ecmaExecutor.leaveScope();
        exceptionScope.leaveScope();
        grammarScope.leaveScope();
        fields.leaveScope();
        if (inRootDoc && rootScopeCounter > 0)
            if (rootScopeCounter > 0)
                 rootScopeCounter--;
            else
                docScopeCounter--;
        else
            docScopeCounter--;

         logger.debug("--- Leaving scope (Scope depth = " + (rootScopeCounter + docScopeCounter) + ")");
    }

    public int getScopeCount() {
        return rootScopeCounter + docScopeCounter;
    }

    public Scope getFields() {
        return fields;
    }

    public void setFields(Scope fields) {
        this.fields = fields;
    }


    public void putGrammar(GrammarMatcher gm) {
        grammarScope.addSymbol("CURRENT", gm);
    }

    public GrammarMatcher getGrammar() {
        return (GrammarMatcher) grammarScope.getSymbol("CURRENT");
    }

    private void addUtilsToECMA() {
        String remoteURI = null;
        String localURI = null;
        if (session != null) {
            remoteURI = session.getCaller();
            localURI = session.getCallee();
        }
        // Extract the number between the sip: and the @ of the URIValidator
        String remoteURIshort = "";
        if (remoteURI != null && remoteURI.indexOf('@') > 0)
            remoteURIshort = remoteURI.substring(4,remoteURI.indexOf('@'));
        String localURIshort = "";
        if (localURI != null && localURI.indexOf('@') > 0)
            localURIshort = localURI.substring(4,localURI.indexOf('@'));
        // todo: What to do, when client does not send a user identity?
        if (localURIshort == null || localURIshort.equals(""))
            localURIshort = remoteURIshort;

        try {
            TerminalSubscription ts = new TerminalSubscription(localURIshort);
            logger.debug("Adding terminalsubscription to ecma space");
            ecmaExecutor.putIntoScope("terminalsubscription",ts);

        } catch (NoUserException e) {
            logger.error("The requested account (" + localURIshort + ") does not exist",e);
        }

        ecmaExecutor.putIntoScope("MAS_A_NUM", remoteURIshort);
        ecmaExecutor.putIntoScope("MAS_C_NUM", localURIshort);
        ecmaExecutor.putIntoScope("PromptManager",PromptManager.getInstance());
        ecmaExecutor.putIntoScope("traverser",this);


    }

    public void execute(Node rootNode) {
        logger.error("Starting application for session " + session.getCaller());
        current = rootNode;
        addUtilsToECMA();
        dispatcher.addMASEventListener(this);
        try {
            while(!stopRunning() && (this.current = this.current.execute(this)) != null ) {
                Node retake = null;
                if ((retake = getRetakeNode()) != null) {
                    this.current = retake;
                }
                setRetakeNode(null);
            }
        }
        catch (Exception e) {
            // todo: handle the exception in a more orderly fashion?
            logger.error("Exception caught in session ! Bailing out.", e);

            // System.exit(ErrorCodes.GENERAL_ERROR);
        }
        logger.debug("Leaving execute");
        logger.debug("Ending application for session " + session.getCaller());
        // Session should be removed once the OK responce to the BYE request is
        // received?
        // session.getServer().removeConnection(session.getURI());
        dispatcher.fire(new MASExit(this));
        setHasEnded(true);
        releaseObjects();
    }

    public boolean stopRunning() {
        boolean ret = false;
        synchronized(mutex) {
            ret = continueRunning;
        }
        return ret;
    }

    public void setStopRunning(boolean val) {
      // If no mutex, the application has already stoped running.
      if (mutex == null)
          return;
      synchronized(mutex) {
            continueRunning = val;
        }
    }

    public Scope getGrammarScope() {
        return grammarScope;
    }

    public void setGrammarScope(Scope grammarScope) {
        this.grammarScope = grammarScope;
    }

    public void notify(MASEvent e) {

      // Check that we are still running.
      if (getHasEnded()){
          return ;
      }
     // Check event type in order to performe the correct action
      if (e instanceof MASUserInput){
          userInputHandler.handle((MASUserInput) e);
      }
      else if (e instanceof MASCancel){
          // todo: add implementation
          logger.error("No event handler installed for MASCancel!");
      }
      else if (e instanceof MASError){
          errorHander.handle((MASError) e);
      }
      else if (e instanceof MASExit){
            exitHandler.handle((MASExit) e);
      }
        else if (e instanceof MASHangup){
          hangupHandler.handle((MASHangup) e);
      }
        else if (e instanceof MASHelp){
          helpHandler.handle((MASHelp) e);
      }
        else if (e instanceof MASNoInput){
            noInputHandler.handle((MASNoInput) e);
        }
        else if (e instanceof MASNoMatch){
          noMatchHandler.handle((MASNoMatch) e);
      }
        else if (e instanceof MASTimeout){
            timeoutHandler.handle((MASTimeout) e);
      }
        else if (e instanceof MASTransfer){
          // todo: add implementation
          logger.error("No event handler installed for MASTransfer!");
      }

    }

    public int getRepromptCount() {
        return repromptCount;
    }

    public void setRepromptCount(int repromptCount) {
        this.repromptCount = repromptCount;
    }

    public boolean isPromptPlayed() {
        return promptPlayed;
    }

    public void setPromptPlayed(boolean promptPlayed) {
        this.promptPlayed = promptPlayed;
    }

    public void run() {
        execute(root);
    }


    public static void main(String argv[]) {


        if(argv.length > 0)
            PropertyConfigurator.configure(argv[0]);

        Traverser traverser = new Traverser();
        traverser.setDispatcher(new MASEventDispatcher());

        traverser.setRoot(GraphFactory.getInstance().getApplication());

        traverser.run();
    }

    public void addDialogGotoable(String name, Node node) {
        logger.debug("Adding " + name + " to dialogGotoables");
        dialogGotoables.put(name, node);
    }

    public HashMap getDialogGotoables() {
        return dialogGotoables;
    }

    public void setDialogGotoables(HashMap dialogGotoables) {
        this.dialogGotoables = dialogGotoables;
    }

    public Node getDialogGotoable(String name) {
        return (Node) dialogGotoables.get(name);
    }

    public void clearDialogGotoables() {
        dialogGotoables.clear();
    }

    public void addDocGotoable(String name, Node node) {
        logger.debug("Adding " + name + " to docGotoables");
        docGotoables.put(name, node);
    }

    public Node getDocGotoable(String name) {
        return (Node) docGotoables.get(name);
    }

    public HashMap getDocGotoables() {
        return docGotoables;
    }

    public void setDocGotoables(HashMap docGotoables) {
        this.docGotoables = docGotoables;
    }

    public void clearDocGotoables() {
        docGotoables.clear();
    }

    public int getDocScopeCounter() {
        return docScopeCounter;
    }

    public void setDocScopeCounter(int docScopeCounter) {
        this.docScopeCounter = docScopeCounter;
    }

    public int getRootScopeCounter() {
        return rootScopeCounter;
    }

    public void setRootScopeCounter(int rootScopeCounter) {
        this.rootScopeCounter = rootScopeCounter;
    }

    public boolean isInRootDoc() {
        return inRootDoc;
    }

    public void setInRootDoc(boolean inRootDoc) {
        this.inRootDoc = inRootDoc;
    }

    public HashMap getSubdialogParamerters() {
        return subdialogParamerters;
    }

    public void setSubdialogParamerters(HashMap subdialogParamerters) {
        this.subdialogParamerters = subdialogParamerters;
    }

    public void suspend(Node nextNode, String subDialogName) {
        SuspendHandler sh = new SuspendHandler(this, nextNode, subDialogName);
        suspensionStack.push(sh);
    }

    public SuspendHandler resume() {
      SuspendHandler sh = (SuspendHandler) suspensionStack.pop();
        if (sh != null) {
            sh.resume(this);
            return sh;
        }
        return null;
    }
}

