package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.WaitSet;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.HandlerLocator;
import com.mobeon.masp.execution_engine.runtime.event.Selector;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoFactory;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;

import java.util.List;


/**
 * @author Mikael Andersson
 */
public interface ExecutionContext {

    void anonymousCall(List<Executable> operations);

    void call(List<Executable> executables, Product callable);

    void executeAtomic(List<? extends Executable> ops) throws InterruptedException;

    Scope getCurrentScope();

    ValueStack getValueStack();

    boolean isInterrupted();

    void postProcess();

    void preProcess();

    void setEngine(Engine e);

    void wasInterrupted();

    EventHub getEventHub();

    EventProcessor getEventProcessor();

    HandlerLocator getHandlerLocator();

    void registerHandler(String[] states, Selector sel, Predicate predicate);

    Engine getEngine();

    String getState();

    boolean prepareForEvent(EventProcessor.Entry eventEntry);

    boolean processEvents();

    void setConnection(Connection connection);

    Module getExecutingModule();

    void setExecutingModule(Module module);

    ModuleCollection getModuleCollection();

    void setModuleCollection(ModuleCollection moduleCollection);

    ISession getSession();

    String getContextId();

    String getSessionId();

    String getContextType();

    void shutdown(boolean recursive);

    ScopeRegistry getScopeRegistry();

    void reportFrameReinstated(Data extraData, Product product);

    void reportFrameEntered(Data frameData, Product product);

    void reportFrameLeft(Data frameData, Product call);

    Statics getStatics();

    WaitSet getWaitSet();

    void dumpFrames(List<StackFrame> stackFrames);

    Data getFrameData();

    /**
     * returns a "product to be executed" or null if there is no
     * such product. In either case, this will method will afterwards
     * return null every time until setProductToBeExecuted() is called again.
     *
     * @return the producty to be executed
     */
    Product getAndResetProductToBeExecuted();

    /**
     * Sets the product to be executed. See also
     * getAndResetProductToBeExecuted(),
     */
    void setProductToBeExecuted(Product p);

    EventStream getEventStream();

    Connection getCurrentConnection();

    void setSupervision(Supervision supervision);

    SessionInfoFactory getSessionInfoFactory();

    EventProcessor.Entry getEventEntry();

    void dumpPendingEvents();

    public IServiceRequestManager getServiceRequestManager();

    public void setServiceRequestManager(IServiceRequestManager serviceRequestManager);

    public IConfigurationManager getConfigurationManager();

    void shutdownEverything();

    void setExecutionResult(ExecutionResult executionResult);

    ExecutionResult getExecutionResult();

    boolean isAlive();

    List<ExecutionContext> getSubContexts();

    void dumpState();
}
