/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.APlatformAccessPlugin;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.WaitSet;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccess;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessUtil;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.HandlerLocator;
import com.mobeon.masp.execution_engine.runtime.event.Selector;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoFactory;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ExecutionContextBase implements ExecutionContext {

    private static final ILogger log = ILoggerFactory.getILogger(ExecutionContextBase.class);

    private Engine engine;
    private final ValueStack valueStack;
    private boolean interrupted = false;
    private final ScopeRegistry scopeRegistry;
    private final EventStream eventStream;
    private final EventHub eventHub;
    private final HandlerLocator locator;
    private final EventProcessor eventProcessor;
    private final PlatformAccessFactory platformAccessFactory;
    private final ISession session;
    private Module executingModule;
    private ModuleCollection moduleCollection;
    private final Id<ExecutionContext> contextId;
    private final String contextType;
    protected final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private Product product;
    private final WaitSet waitSet;
    private final Statics statics;
    private final PlatformAccess platformaccess;
    private final PlatformAccessUtil platformaccessUtil;
    private final APlatformAccessPlugin platformaccessPlugin;
    private boolean pruneStack = false;
    protected EventProcessor.Entry eventEntry;
    Product productToBeExecuted;
    protected Supervision supervision = null;
    private IServiceRequestManager serviceRequestManager;
    private IConfigurationManager configurationManager;
    protected final AtomicReference<ExecutionResult> executionResult = new AtomicReference<ExecutionResult>(ExecutionResult.RUN_UNCONDITIONALLY);

    public IConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    protected ExecutionContextBase(
            ISession session,
            ValueStack valueStack,
            ScopeRegistry registry,
            EventHub eventHub,
            HandlerLocator locator,
            EventProcessor eventProcessor,
            Statics statics,
            EventStream eventStream,
            PlatformAccessFactory platformAccessFactory,
            String contextType,
            IServiceRequestManager serviceRequestManager,
            IConfigurationManager configurationManager) {

        this.contextType = contextType;
        this.session = session;
        this.valueStack = valueStack;
        this.scopeRegistry = registry;
        this.eventHub = eventHub;
        this.locator = locator;
        this.eventProcessor = eventProcessor;
        this.platformAccessFactory = platformAccessFactory;
        this.waitSet = new WaitSet(this.getEventProcessor());
        this.statics = statics;
        this.eventStream = eventStream;
        contextId = IdGeneratorImpl.CONTEXT_GENERATOR.generateId();
        getEventHub().setOwner(this);
        getHandlerLocator().setScopeRegistry(getScopeRegistry());
        this.serviceRequestManager = serviceRequestManager;
        this.configurationManager = configurationManager;

        getScopeRegistry().createNewScope("session");
        
        // Register the XMLHttpRequest class as a valid object
        Scope scope = getScopeRegistry().getMostRecentScope();
        scope.register("com.mobeon.masp.execution_engine.runtime.xmlhttprequest.XMLHttpRequest");

        if (platformAccessFactory != null) {
            platformaccess = platformAccessFactory.create(this);
            getScopeRegistry().getMostRecentScope().setValue("mas", platformaccess);

            platformaccessUtil = platformAccessFactory.createUtil(this);
            getScopeRegistry().getMostRecentScope().setValue("util", platformaccessUtil);

            platformaccessPlugin = platformAccessFactory.createPlugin(this);
            if (platformaccessPlugin != null) {
                getScopeRegistry().getMostRecentScope().setValue("plugin", platformaccessPlugin);
            }
        } else {
            platformaccess = null;
            platformaccessUtil = null;
            platformaccessPlugin = null;
            if (log.isInfoEnabled())
                log.info("No PlatformAccessFactory specified, mas and util will not be available for scripts");
        }
        getCurrentScope().setValue("_context", this);

    }

    public abstract void setFrameData(Data extraData);

    public abstract Data getFrameData();

    public void registerHandler(String[] states, Selector sel, Predicate predicate) {
        if (states == null) {
            locator.addEventHandler(predicate, sel);
        } else {
            for (String state : states) {
                locator.addEventHandler(predicate, sel, state);
            }
        }
    }


    public void wasInterrupted() {
        interrupted = true;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public EventHub getEventHub() {
        return eventHub;
    }

    public void anonymousCall(List<Executable> operations) {
        engine.push(operations);
    }

    public final void reportFrameEntered(Data extraData, Product product) {
        extraData.depth = getValueStack().size();
        setFrameData(extraData);
        if (product != null) {
            extraData.destructors = product.freezeAndGetDestructors();
            setProduct(product);
        }
        onFrameEntered();
    }

    public final void reportFrameReinstated(Data frameData, Product product) {
        setFrameData(frameData);
        if (product != null) setProduct(product);
        onFrameReinstated();
    }

    public final void reportFrameLeft(Data frameData, Product product) {
        if (frameData.destructors != null) {
            getEngine().executeAtomic(frameData.destructors, 0, frameData.constructorProgress);
        }
        onFrameLeft();
    }

    public void dumpFrames(List<StackFrame> stackFrames) {
        for (int i = 0; i < stackFrames.size(); i++) {
            int end = valueStack.size() - 1;
            int start = stackFrames.get(i).frameData.depth;
            if (i + 1 < stackFrames.size()) {
                end = stackFrames.get(i + 1).frameData.depth;
            }
            if (log.isDebugEnabled()) log.debug(stackFrames.get(i));
            List<Value> values = valueStack.toList();
            if (start != end) {
                for (int j = start; j <= end; j++) {
                    if (log.isDebugEnabled()) log.debug("    " + values.get(j));
                }
            }
        }
    }

    public void onFrameEntered() {
    }

    public void onFrameLeft() {
    }

    public void onFrameReinstated() {
        if (readPruneStack()) {
            getValueStack().prune(getFrameData().depth);
        }
    }

    public void call(List<Executable> operations, Product callable) {
        engine.call(operations, callable);
    }

    public void preProcess() {
        getEventProcessor().processExternalEvents();
    }

    public void postProcess() {
        getEventProcessor().processExternalEvents();
    }

    public boolean processEvents() {

        EventProcessor.Entry eventEntry = getEventProcessor().poll();
        while(eventEntry != null && prepareForEvent(eventEntry)) {
            try {
                if (eventEntry.getHandler() != null) {
                    eventEntry.getHandler().getPredicate().execute(this);
                    if (log.isDebugEnabled()) log.debug("Prepared : " + eventEntry);
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Made unhandled event " + eventEntry.getEvent() + " available for processing");
                }
                return true;
            } catch (InterruptedException e) {
                wasInterrupted();
            }
        }
        return false;
    }



    public Scope getCurrentScope() {
        return scopeRegistry.getMostRecentScope();
    }


    public ValueStack getValueStack() {
        return valueStack;
    }

    public EventProcessor getEventProcessor() {
        return eventProcessor;
    }

    public HandlerLocator getHandlerLocator() {
        return locator;
    }

    public ISession getSession() {
        return session;
    }

    public void setExecutingModule(Module module) {
        this.executingModule = module;
    }

    public Module getExecutingModule() {
        return executingModule;
    }

    public ModuleCollection getModuleCollection() {
        return moduleCollection;
    }

    public void setModuleCollection(ModuleCollection moduleCollection) {
        this.moduleCollection = moduleCollection;
    }

    public String getContextId() {
        return contextId.toString();
    }

    public String getSessionId() {
        return getSession().getId();
    }

    public String getContextType() {
        return contextType;
    }

    public ScopeRegistry getScopeRegistry() {
        return scopeRegistry;
    }

    private void setProduct(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine e) {
        this.engine = e;
    }

    public Statics getStatics() {
        return statics;
    }

    public WaitSet getWaitSet() {
        return waitSet;
    }

    public PlatformAccess getPlatformaccess() {
        return platformaccess;
    }


    protected boolean readPruneStack() {
        if (pruneStack) {
            pruneStack = false;
            return true;
        } else
            return false;
    }

    public void executeAtomic(List<? extends Executable> ops) throws InterruptedException {
        engine.executeAtomic(ops, 0, ops.size());
    }

    public Product getAndResetProductToBeExecuted() {
        Product toReturn = productToBeExecuted;
        productToBeExecuted = null;
        return toReturn;
    }

    public void setProductToBeExecuted(Product p) {
        productToBeExecuted = p;
    }

    public boolean prepareForEvent(EventProcessor.Entry eventEntry) {
        this.eventEntry = eventEntry;
        return true;
    }


    public PlatformAccessFactory getPlatformAccessFactory() {
        return platformAccessFactory;
    }

    public EventStream getEventStream() {
        return eventStream;
    }

    public void setSupervision(Supervision supervision) {
        this.supervision = supervision;
    }

    public SessionInfoFactory getSessionInfoFactory() {
        return (supervision != null ? supervision.getSessionInfoFactory() : null);
    }

    public EventProcessor.Entry getEventEntry() {
        return eventEntry;
    }

    public void dumpPendingEvents() {
        eventProcessor.dumpPendingEvents();
    }

    public IServiceRequestManager getServiceRequestManager() {
        return serviceRequestManager;
    }

    public void setServiceRequestManager(IServiceRequestManager serviceRequestManager) {
        this.serviceRequestManager = serviceRequestManager;
    }

    public ExecutionResult getExecutionResult() {
        return executionResult.get();
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        if(this.executionResult.get() != executionResult && log.isDebugEnabled()) log.debug("Changing state to " + executionResult);
        this.executionResult.set(executionResult);
    }

    public boolean isAlive() {
        return ! isShutdown.get();
    }

    public List<ExecutionContext> getSubContexts() {
        return new ArrayList<ExecutionContext>();
    }


    public void dumpState() {
    }
}
