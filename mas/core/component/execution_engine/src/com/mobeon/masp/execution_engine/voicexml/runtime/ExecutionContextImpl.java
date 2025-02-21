/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.externaldocument.ResourceLocator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.runtime.*;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.HandlerLocator;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.VoiceXMLSelector;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistryImpl;
import com.mobeon.masp.execution_engine.runtime.values.Pair;
import com.mobeon.masp.execution_engine.runtime.wrapper.Call;
import com.mobeon.masp.execution_engine.runtime.wrapper.MediaTranslator;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.voicexml.compiler.DefaultEventHandlerFactory;
import com.mobeon.masp.execution_engine.voicexml.compiler.Exit;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.InputAggregator;
import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.PromptQueue;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.NoInputSender;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.util.Tools;
import org.mozilla.javascript.ScriptableObject;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mikael Andersson, David Looberger et.al
 */
public class ExecutionContextImpl extends ExecutionContextBase implements VXMLExecutionContext {

    private static final ILogger logger = ILoggerFactory.getILogger(ExecutionContextImpl.class);
    private String currentMarkname = null;
    private long markStartTime = 0;
    private Runnable shutdownHook;
    private VXMLExecutionContext parent;
    private String currentFormItem = null;
    private FIAState fiaState = new FIAState();
    private TransferState transferState = new TransferState();

    private boolean isInFinalProcessingState = false;
    private boolean initiatedDisconnect = false;
    private boolean hadInWaitingState = false;
    private MediaTranslator mediatranslator = null;
    /**
     * This ScriptableObject is the "application.lastresult" that gets declared when starting
     * a VXML context
     */
    private ScriptableObject lastResult = new ShadowVarBase();
    private final AtomicReference<WeakReference<ExecutionContextImpl>> subContext = new AtomicReference<WeakReference<ExecutionContextImpl>>();


    private Connection connection;
    private Dialog dialog;
    private Call call = new Call();
    private Constants.VoiceXMLState voiceXMLState = Constants.VoiceXMLState.STATE_TRANSITIONING;
    private IMediaObjectFactory mediaObjectFactory;
    private Product repromptPoint = null;
    private VoiceXMLData frameData;
    private Product executingForm;
    private final PropertyStack properties = new PropertyStack();

    private ValueStack valueStack = new ValueStackImpl();
    private final PromptQueue promptQueue = new PromptQueue();
    private InputAggregator inputAggregator = new InputAggregator();
    private ResourceLocator resourceLocator = new ResourceLocator();

    // TODO: Use the configurable ExecutorServiceManager for this?
    private final NoInputSender noinputSender = new NoInputSender(this);


    private GrammarScopeNode dtmf_grammar = null;
    private GrammarScopeNode asr_grammar = null;

    private String utterance = null;
    private final Redirector redirector;
    private String fieldTargetType;

    public ExecutionContextImpl(ISession session, PlatformAccessFactory platformAccessFactory, IServiceRequestManager serviceRequestManager, IConfigurationManager configurationManager) {
        this(
                session,
                new EventStream(),
                platformAccessFactory,
                serviceRequestManager,
                configurationManager);
        call.setContext(this);

    }

    public ExecutionContextImpl(
            ISession session, EventStream eventStream, PlatformAccessFactory platformAccessFactory, IServiceRequestManager serviceRequestManager, IConfigurationManager configurationManager) {
        super(
                session,
                new ValueStackImpl(),
                new ScopeRegistryImpl(null),
                new VoiceXMLEventHub(),
                new HandlerLocator(VoiceXMLSelector.instance()),
                new VoiceXMLEventProcessor(),
                new VoiceXMLStatics(),
                eventStream,
                platformAccessFactory,
                "VXMLContext",
                serviceRequestManager,
                configurationManager);
        getScopeRegistry().setExecutionContext(this);

        DefaultEventHandlerFactory.registerDefaultHandlers(this);
        getScopeRegistry().addScopeChangedSubscriber(properties);
        call.setContext(this);

        redirector = new Redirector(inputAggregator, this);
        promptQueue.init(redirector);
    }

    public String getFieldTargetType() {
        return fieldTargetType;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
        fieldTargetType = "field:" + dialog.getBridgePartyId();

        dialog.setExecutionContext(this);
        getEventProcessor().setExecutionContext(this);

    }

    public Dialog getDialog() {
        return this.dialog;
    }

    public synchronized void shutdownEverything() {

        // Invoke the master, it will shutdown us later
        Object masterExecutionImpl = getSession().getData(ISession.MASTER_EXECUTION_CONTEXT);
        if (masterExecutionImpl != null && masterExecutionImpl instanceof ExecutionContext) {
            ExecutionContext master = (ExecutionContext) masterExecutionImpl;
            master.shutdownEverything();
        }
    }

    /**
     * Shutdown of the execution context and its associated {@link Engine}.
     *
     * @param recursive - Recursively shutdown all execution contexts or not.
     */
    public void shutdown(boolean recursive) {

        // Both the CCXML and VXML thread may enter here simultaneously, and there
        // was a race condition when the CCXML thread set isShutdown to true and then was swapped
        // out. At that point the VXML thread entered and noticed that isShutdown==true,
        // which had the effect that the VXML thread continued to execute products/operations,
        // which it shouldn't have. The shutdown
        // proceeded eventually when the CCXML thread was swapped in again.
        // By making "getEngine().stopExecuting();" first in the method
        // the VXML thread will not continue to execute when this scenario happens.

        if (logger.isDebugEnabled()) logger.debug("Telling Engine to stop Executing");
        getEngine().stopExecuting();

        if (isShutdown.compareAndSet(false, true)) {
            if (logger.isDebugEnabled()) logger.debug("Shutting down Engine/ExecutionContext");
            shutdownImpl(recursive);
        } else {
            if (logger.isDebugEnabled())
                logger.debug("Shutdown called more than once for " + getExecutingModule().getDocumentURI());
        }
    }

    /**
     * Shutdown of the execution context and its associated {@link Engine}.
     *
     * @param recursive - Recursively shutdown all execution contexts or not.
     * @logs.error "Forcefully stopping any ongoing Engine operations" - The Engine is running even though is should be stopping, and therefore it is stopped forcefully.
     * @logs.error "The shutdown thread was interrupted. Forcefully stopping any onging Engine operation." - The thread performing the shutdown was interrupted, and retries to force the Engine to stop execute
     * @logs.error "Failed to acquire lock and stop Engine, continuing cleanup operation even so" - The shutdown thread failed to accuire the lock guarding the Engine from running during shutdown, but the shutdown is continued even so.
     * @logs.error "Shutdown thread interrupted. Failed to acquire lock and stop Engine, continuing cleanup operation even so" - The shutdown thread was interrupted while trying to accuire the lock guarding the Engine from running during shutdown, but the shutdown is continued even though the lock was not accuired.
     */
    protected void shutdownImpl(boolean recursive) {
        try {
            long gracetime = 10000;
            if (getProperty(Constants.SYSTEM.SHUTDOWNGRACETIME) != null)
                gracetime = Tools.parseCSS2Time(getProperty(Constants.SYSTEM.SHUTDOWNGRACETIME));

            if (logger.isDebugEnabled()) logger.debug("Acquiring lock");
            try {
                if (getEngine().executionLock.tryLock(gracetime, TimeUnit.MILLISECONDS)) {
                    if (logger.isDebugEnabled()) logger.debug("Lock acquired, continuing shutdown");
                } else {
                    logger.error("Forcefully stopping any ongoing Engine operations");
                    wasInterrupted();
                    Thread runner = getEngine().getExecutingThread();
                    if (runner != null) {
                        if (logger.isDebugEnabled()) logger.debug("Interrupting thread " + runner.getName());
                        runner.interrupt();
                    }
                }
            } catch (InterruptedException e) {
                logger.error("The shutdown thread was interrupted. Forcefully stopping any onging Engine operation.");
                wasInterrupted();
                Thread runner = getEngine().getExecutingThread();
                if (runner != null) {
                    if (logger.isDebugEnabled()) logger.debug("Interrupting thread " + runner.getName());
                    runner.interrupt();
                }
            }
            // Try to acquire the lock again
            try {
                if (getEngine().executionLock.isHeldByCurrentThread() ||
                        getEngine().executionLock.tryLock(gracetime, TimeUnit.MILLISECONDS)) {
                    if (logger.isDebugEnabled()) logger.debug("Lock acquired, continuing shutdown");
                } else {
                    logger.error("Failed to acquire lock and stop Engine, continuing cleanup operation even so");
                }
            } catch (InterruptedException e) {
                logger.error("Shutdown thread interrupted. Failed to acquire lock and stop Engine, continuing cleanup operation even so");
            }

            if (getExecutingModule() != null) {
                if (logger.isDebugEnabled()) logger.debug("Shutting down " + getExecutingModule().getDocumentURI());
            } else {
                if (logger.isDebugEnabled()) logger.debug("Shutting down <invalid uri>");
            }
            if (shutdownHook != null) {
                shutdownHook.run();
                shutdownHook = null;
            }
            getNoInputSender().cancelAndLock();

            getEventStream().close();
            getEventHub().close();

            getEventProcessor().stop();

            if (logger.isDebugEnabled()) logger.debug("Shutdown of context " + this.getContextId() + " completed");
            if (getParent() != null) {
                if (recursive) getParent().shutdown(recursive);
            }
        } finally {
            // The problem fixed here is described in  TR 31787.
            // Since during shutdown an EngineDummy may be inserted into the exection context,
            // it does not work to do this:
            // if(getEngine().executionLock.isHeldByCurrentThread())getEngine().executionLock.unlock();
            Engine.MyReentrantLock executionLock = getEngine().executionLock;
            if (executionLock.isHeldByCurrentThread()) executionLock.unlock();
        }
    }

    public void preProcess() {
        //TODO: Implement correct handling och waiting and transistioning
        if (voiceXMLState == Constants.VoiceXMLState.STATE_WAITING) {
            super.preProcess();
        }
        setExecutionResult(ExecutionResult.DEFAULT);
    }

    /*
    *
    */
    public void postProcess() {
        super.postProcess();

        switch (getExecutionResult()) {
            case EVENT_WAIT:
                if (!getEventProcessor().hasEventsInQ()) getEngine().pauseExecuting();
                return;
            default:
               if( getEngine().getWasStopped() && (this.getParent() != null)) {
                  if(getEventProcessor().hasEventsInQ() || (getEventProcessor().getExternalEventQueue().size() > 0) ) {
                      copyEventsNeverHandledInThisSubdialog();  
                  } else {
                      if (logger.isDebugEnabled()) logger.debug("Engine stopped, but no events to copy to parent ");
                  }
               }
                break;
        }
    }

    public void onFrameReinstated() {
        if (readPruneStack()) {
            valueStack.prune(frameData.depth);
        }
    }

    public Call getCall() {
        return call;
    }

    public Connection getCurrentConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        call.setConnection(connection);
        this.connection = connection;

        // Set properties into the current scope.
        // Right after creation, it is the session scope, and
        // they will thus be available in the:
        // "session.connection.redirect.number" style.

        getCurrentScope().declareReadOnlyVariable("connection", connection.getVoiceXMLMirror());
    }

    public void setTransitioningState() {
        if (voiceXMLState != Constants.VoiceXMLState.STATE_TRANSITIONING) {
            voiceXMLState = Constants.VoiceXMLState.STATE_TRANSITIONING;
            if (logger.isDebugEnabled()) logger.debug("Setting state to " + voiceXMLState);
        }
    }


    public void setWaitingState() {
        hadInWaitingState = true;
        if (getFinalProcessingState()) {
            logger.warn("<CHECKOUT>Attempt to enter waiting state, forcefully terminating VoiceXML dialog.");
            anonymousCall(Exit.getExitOperations());
            return;
        } else {
            voiceXMLState = Constants.VoiceXMLState.STATE_WAITING;
            if (logger.isDebugEnabled()) logger.debug("Setting state to " + voiceXMLState);
            PromptQueue(this).playQueuedPlayableObjects();
        }
    }

    public Constants.VoiceXMLState getVoiceXMLState() {
        return voiceXMLState;
    }

    public void setMediaObjectFactory(IMediaObjectFactory factory) {

        this.mediaObjectFactory = factory;
    }

    public IMediaObjectFactory getMediaObjectFactory() {

        return mediaObjectFactory;
    }


    public GrammarScopeNode getDTMFGrammar() {
        return dtmf_grammar;
    }

    public void setDTMFGrammar(GrammarScopeNode grammar) {
        this.dtmf_grammar = grammar;
    }

    public GrammarScopeNode getASR_grammar() {
        return this.asr_grammar;
    }

    public void setASRGrammar(GrammarScopeNode grammar) {
        this.asr_grammar = grammar;
    }

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    public String getUtterance() {
        return utterance;
    }

    public Product getRepromptPoint() {
        return repromptPoint;
    }

    public void setRepromptPoint(Product repromptPoint) {
        this.repromptPoint = repromptPoint;
    }

    public void setFrameData(Data extraData) {
        this.frameData = (VoiceXMLData) extraData;
    }

    public void setCurrentMark(String markname) {
        if (logger.isDebugEnabled()) logger.debug("Setting mark to " + markname);
        currentMarkname = markname;
        markStartTime = System.currentTimeMillis();
    }

    public String getCurrentMark() {
        if (logger.isDebugEnabled()) logger.debug("Getting markname : " + currentMarkname);
        return currentMarkname;
    }

    public long getCurrentMarktime() {
        long ret = 0;
        if (markStartTime != 0)
            ret = System.currentTimeMillis() - markStartTime;
        if (logger.isDebugEnabled()) logger.debug("Getting marktime : " + ret);
        return ret;
    }


    public String getProperty(String prop) {
        return properties.getProperty(prop);
    }

    public void setProperty(String prop, String value) {
        properties.putProperty(prop, value);
    }

    public PropertyStack getProperties() {
        return properties;
    }

    public void setExecutingForm(Product form) {
        executingForm = form;
    }

    public VoiceXMLStatics getStatics() {
        return (VoiceXMLStatics) super.getStatics();
    }

    public VoiceXMLData getFrameData() {
        return frameData;
    }

    private void copyEventsToInvokedSubdialog(VXMLExecutionContext to) {
        EventQueue<SimpleEvent> queue = getEventProcessor().getQueue();
        int num = to.getEventProcessor().getQueue().copyEvents(queue);
        queue.clear();

        Queue<Event> externalEventQueue = getEventProcessor().getExternalEventQueue();
        Queue<Event> toExternalEventQueue = to.getEventProcessor().getExternalEventQueue();
        toExternalEventQueue.addAll(externalEventQueue);
        externalEventQueue.clear();
        if (logger.isDebugEnabled()) {
            logger.debug("Copying " + num + " events and " + toExternalEventQueue.size() + " external events, that were never handled in this context, to invoked subdialog");
        }
    }

    public void subdialog(String src, DebugInfo debugInfo, List<Pair> params, List<PlayableObject> promptQ,
                          String maxage, String fetchTimeout) {

        String docSrc = Tools.documentURI(src);
        String fragURI = Tools.fragmentOfURI(src);
        URI docURI = getExecutingModule().getDocumentURI().resolve(docSrc);
        Module m = resourceLocator.getDocument(docURI.toString(), maxage, fetchTimeout, getModuleCollection());

        Product productToBeExecuted = null;

        if (m == null && (fragURI != null && fragURI.length() > 0)) {
            // subdialog to product in this module
            m = getExecutingModule();
        }
        if (m == null) {
            getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, "Document " + docURI + " was not found", debugInfo);
            return;
        }
        if (fragURI != null && fragURI.length() > 0) {
            // If there is no name of the product it means that we get the first
            // in the module.
            if (Tools.isEmpty(fragURI)) {
                productToBeExecuted = m.getSpecialProduct(Module.FIRST_DIALOG);
            } else {
                productToBeExecuted = m.getNamedProduct(fragURI);
                if (productToBeExecuted == null) {
                    getEventHub().fireContextEvent(Constants.Event.ERROR_BADFETCH, "Dialog " + fragURI + " was not found", debugInfo);
                    return;
                }
            }

        }

        Scope sessionScope = getScopeRegistry().getTopLevelScope();
        final EventStream eventStream = getEventStream().createUpstream(false);
        final ExecutionContextImpl ex = new ExecutionContextImpl(getSession(), eventStream, getPlatformAccessFactory(), getServiceRequestManager(), getConfigurationManager());
        Engine e = getEngine().create(getConfigurationManager());

        e.setExecutionContext(ex);
        ex.setEngine(e);
        ex.setMediaObjectFactory(mediaObjectFactory);
        ex.setDialog(dialog.clone());
        ex.setConnection(connection);
        ex.setParent(this);
        ex.getStatics().setParams(params);
        ex.setExecutingModule(m);
        ex.setModuleCollection(getModuleCollection());
        PromptQueue(ex).setPlayableQueue(promptQ);
        ex.setSupervision(supervision);
        ex.setProductToBeExecuted(productToBeExecuted);
        ex.setServiceRequestManager(getServiceRequestManager());
        ex.setMediaTranslationManager(mediatranslator.getMediaTranslationManager());
        InputAggregator(ex).drainFrom(InputAggregator(this));

        setupEventStreams(ex, eventStream);

        RuntimeFacade runtime = new VoiceXMLRuntime(e, ex, m, getModuleCollection());

        Product appProduct = null;
        String applicationAttr = ex.getExecutingModule().getDocumentAttribute(Constants.VoiceXML.APPLICATION);
        if (applicationAttr == null || applicationAttr.length() == 0) {
            // the executing module is root
        } else {
            Module rootModule = ex.getModuleCollection().get(applicationAttr);
            if (rootModule != null) {
                appProduct = rootModule.getSpecialProduct(
                        Module.VXML_PRODUCT);
            }
        }

        subContext.set(new WeakReference<ExecutionContextImpl>(ex));
        if (appProduct != null) {
            ex.setProductToBeExecuted(ex.getExecutingModule().getProduct());
            runtime.start(appProduct);
        } else {
            runtime.start(runtime.getEntryModule().getProduct());
        }
    }

    private void setupEventStreams(ExecutionContextImpl ex, final EventStream eventStream) {

        // Disable all events by disabling the event processor of the first VXML context created in this session,
        // and locking the corresponding event stream

        VXMLExecutionContext contextParentOfAll = getContextParentOfAll();
        contextParentOfAll.getEventProcessor().setEnabled(false);   // enable again upon return from subdialog
        EventStream parentOfAllEventStream = contextParentOfAll.getEventStream();

        try {
            parentOfAllEventStream.lockEvents(false);
            copyEventsToInvokedSubdialog(ex);

            final EventStream parentStream = getEventStream().getDownstream();
            parentStream.swapUpstream(eventStream);
            ex.shutdownHook(
                    new Runnable() {
                        public void run() {
                            parentStream.unswapUpstream(eventStream);
                            subContext.set(null);
                        }
                    });
        } finally {
            parentOfAllEventStream.unlockEvents(false);
        }
    }

    private void copyEventsNeverHandledInThisSubdialog() {
        
        VXMLExecutionContext contextParentOfAll = getContextParentOfAll();
        EventStream parentOfAllEventStream = contextParentOfAll.getEventStream();
        try {
            parentOfAllEventStream.lockEvents(false);
            VoiceXMLEventProcessor toEventProcessor = getParent().getEventProcessor();
            int num = toEventProcessor.getQueue().copyEvents(getEventProcessor().getQueue());            
            Queue<Event> externalEventQueue = getEventProcessor().getExternalEventQueue();
            Queue<Event> toExternalEventQueue = toEventProcessor.getExternalEventQueue();
            toExternalEventQueue.addAll(externalEventQueue);
            externalEventQueue.clear();

            if (logger.isDebugEnabled()) {
                logger.debug("Copying " + num + " events and " + toExternalEventQueue.size() + " external events that were never handled in this subdialog, back to invoking context");
            }                       
        } finally {
            parentOfAllEventStream.unlockEvents(false);
        }      

    }
    public VXMLExecutionContext getContextParentOfAll() {
        // Loop until we find no more parents

        VXMLExecutionContext parentOfAll = this;
        boolean done = false;
        while (! done) {
            VXMLExecutionContext current = parentOfAll.getParent();
            if (current == null) {
                done = true;
            } else {
                parentOfAll = current;
            }
        }
        return parentOfAll;
    }

    public void shutdownHook(Runnable runnable) {
        shutdownHook = runnable;
    }

    public void setParent(VXMLExecutionContext executionContext) {
        parent = executionContext;
    }

    public VXMLExecutionContext getParent() {
        return parent;
    }

    public void setFIAState(FIAState fiaState) {
        if (fiaState != null)
            this.fiaState = fiaState;
    }

    public FIAState getFIAState() {
        return fiaState;
    }

    public String getCurrentFormItem() {
        return currentFormItem;
    }

    public void setCurrentFormItem(String name) {
        this.currentFormItem = name;
    }

    public Product getExecutingForm() {
        return executingForm;
    }

    public void setEventEntry(EventProcessor.Entry eventEntry) {
        this.eventEntry = eventEntry;
    }

    public VoiceXMLEventProcessor getEventProcessor() {
        return (VoiceXMLEventProcessor) super.getEventProcessor();
    }

    public void setFinalProcessingState(boolean val) {
        TestEventGenerator.generateEvent(TestEvent.FINAL_PROCESSING_STATE);
        this.isInFinalProcessingState = val;
    }

    public boolean getFinalProcessingState() {
        return this.isInFinalProcessingState;
    }


    public Map<String, String> getCurrentPromptProperties() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(Constants.VoiceXML.TIMEOUT, properties.getProperty(Constants.VoiceXML.TIMEOUT));
        ret.put(Constants.VoiceXML.BARGEIN, properties.getProperty(Constants.VoiceXML.BARGEIN));
        ret.put(Constants.VoiceXML.BARGEINTYPE, properties.getProperty(Constants.VoiceXML.BARGEINTYPE));
        ret.put(Constants.VoiceXML.XMLLANG, properties.getProperty(Constants.VoiceXML.XMLLANG));
        ret.put(Constants.VoiceXML.XMLBASE, properties.getProperty(Constants.VoiceXML.XMLBASE));
        ret.put(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET, properties.getProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET));
        return ret;
    }

    public void setInitatedDisconnect() {
        initiatedDisconnect = true;
    }

    public boolean initiatedDisconnect() {
        return initiatedDisconnect;
    }

    public boolean isDisconnectReady() {
        String name = "Disconnected" ; 
        Scope scope = this.getCurrentScope();
        if(!getEngine().getWasStopped()) {
          if (scope != null && scope.isDeclaredInAnyScope(name) && hadInWaitingState ) {
              if (logger.isDebugEnabled())
                  logger.debug("isDisconnectReady returning true");              
             return true;
          } else {
             if (logger.isDebugEnabled()) {
               if(!hadInWaitingState) {
                 logger.debug("isDisconnectReady returning false, hadInWaitingState=false");
               }
             }             
             return false; 
          }
          
        }  else {
            logger.debug("isDisconnectReady returning false because Engine was stopped");
            return false;
        }
        
    }

    public void setMediaTranslationManager(MediaTranslationManager mediaTranslationManager) {
        this.mediatranslator = new MediaTranslator(mediaTranslationManager);

    }

    public MediaTranslator getMediaTranslator() {
        return this.mediatranslator;
    }

    public void waitForEvents() {

        ExecutionResult state = ExecutionResult.EVENT_WAIT;
        // Verify that no events have arived that needs attention
        if (getEventProcessor().hasEventsInQ()) {
            if (logger.isDebugEnabled())
                logger.debug("There are unhandled events in the event queue, setting state to " + ExecutionResult.DEFAULT);
            setExecutionResult(ExecutionResult.DEFAULT);
        } else {
            if (getFinalProcessingState()) {
                //TODO: We should not have to wait while in final processing state !
                if(logger.isInfoEnabled())
                    logger.info("Entered waiting state while in final processing state !");
            }
            setExecutionResult(state);
        }
    }


    public ScriptableObject getLastResult() {
        return lastResult;
    }

    public InputAggregator getInputAggregator() {
        return inputAggregator;
    }

    public TransferState getTransferState() {
        return transferState;
    }


    public void setExecutionResult(ExecutionResult executionResult) {
        if (getFinalProcessingState() && executionResult == ExecutionResult.EVENT_WAIT) {
            //TODO: We should not have to wait while in final processing state !
            if (logger.isInfoEnabled())
                logger.info("Trying to wait for events while in final processing state !, called from :" + Tools.outerCaller(0, true));
        }
        super.setExecutionResult(executionResult);
    }

    public VoiceXMLEventHub getVoiceXMLEventHub() {
        return (VoiceXMLEventHub) getEventHub();
    }

    public List<ExecutionContext> getSubContexts() {
        List<ExecutionContext> subContexts = super.getSubContexts();
        AtomicReference<WeakReference<ExecutionContextImpl>> sub = subContext;
        WeakReference<ExecutionContextImpl> weak;
        ExecutionContextImpl ctx;

// WARNING: Nested assignments ahead !
// These are here because this code is not really synchronized, it works
// on a 'best-effort' basis. This makes it reflect the state an outside
// observer would see, which is not necessarily exactly the same as the
// running application would see. However, when this code is called from
// ApplicationWatchdog, the application has been idle for a looong time.
        while (sub != null && (weak = sub.get()) != null && (ctx = weak.get()) != null) {
            subContexts.add(ctx);
            sub = ctx.subContext;
        }
        return subContexts;
    }

    public void anonymousCall(Product product) {
        getEngine().call(product);
    }

    public PromptQueue getPromptQueue() {
        return promptQueue;
    }

    public NoInputSender getNoInputSender() {
        return noinputSender;
    }

    public String getState() {
        return "default";
    }

    public Redirector getRedirector() {
        return redirector;
    }    

    /**
     * Retrieves the ResourceLocator object.
     *
     * @return the ResourceLocator object
     */
    public ResourceLocator getResourceLocator() {
        return resourceLocator;
    }
}
 
