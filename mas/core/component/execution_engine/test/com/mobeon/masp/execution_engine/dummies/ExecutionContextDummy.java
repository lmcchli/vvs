package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.WaitSet;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;
import com.mobeon.masp.execution_engine.runtime.*;
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
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;

import java.util.List;

/**
 * Dummy class that implements all methods of a chosen (set) of
 * interfaces.
 */
public class ExecutionContextDummy implements ExecutionContext {
    private DefaultExpectTarget expectTarget;
    private RuntimeData data;

    public RuntimeData getData() {
        return data;
    }

    public ExecutionContextDummy(DefaultExpectTarget expectTarget, RuntimeData data) {
        this.expectTarget = expectTarget;
        this.data = data;
    }

    protected ExecutionContextDummy() {
    }

    public void anonymousCall(List<Executable> operations) {
    }

    public void call(List<Executable> executables, Product callable) {
    }

    public void executeAtomic(List<? extends Executable> ops) throws InterruptedException {
    }

    @MockAction(DELEGATE)
    public Scope getCurrentScope() {
        return getData().getCurrentScope();
    }

    @MockAction(DELEGATE)
    public ValueStack getValueStack() {
        return getData().getValueStack();
    }

    public boolean isInterrupted() {
        return false;
    }

    public void postProcess() {
    }

    public void preProcess() {
    }

    public void setEngine(Engine e) {
    }

    public void wasInterrupted() {
    }

    @MockAction(DELEGATE)
    public ILogger getLogger(Class aClass) {
        return ILoggerFactory.getILogger(aClass);
    }

    @MockAction(DELEGATE)
    public EventHub getEventHub() {
        return getData().getEventHub();
    }

    @MockAction(DELEGATE)
    public EventProcessor getEventProcessor() {
        return getData().getEventProcessor();
    }

    @MockAction(DELEGATE)
    public HandlerLocator getHandlerLocator() {
        return getData().getHandlerLocator();
    }

    public void registerHandler(String[] states, Selector sel, Predicate predicate) {
    }

    @MockAction(DELEGATE)
    public Engine getEngine() {
        return getData().getEngine();
    }

    public Constants.VoiceXMLState getVoiceXMLState() {
        return null;
    }

    public void setSession(ISession session) {
    }

    public boolean prepareForEvent(EventProcessor.Entry eventEntry) {
        return true;
    }

    public boolean processEvents() {
        return false;
    }

    public void setConnection(Connection connection) {
    }

    public Module getExecutingModule() {
        return null;
    }

    public void setExecutingModule(Module module) {
    }

    public ModuleCollection getModuleCollection() {
        return null;
    }

    public void setModuleCollection(ModuleCollection moduleCollection) {
    }

    public ISession getSession() {
        return null;
    }

    @MockAction(DELEGATE)
    public String getContextId() {
        return "1001";
    }

    @MockAction(DELEGATE)
    public String getSessionId() {
        return "12345";
    }

    @MockAction(DELEGATE)
    public String getContextType() {
        return "mobeon.ccxml";
    }

    public void shutdown(boolean recursive) {
    }

    @MockAction(DELEGATE)
    public ScopeRegistry getScopeRegistry() {
        return getData().getScopeRegistry();
    }

    public Connection getCurrentConnection() {
        return null;
    }

    public void setSupervision(Supervision supervision) {

    }

    public SessionInfoFactory getSessionInfoFactory() {
        return null;
    }

    public void reportFrameReinstated(Data extraData, Product product) {
    }

    public void reportFrameEntered(Data frameData, Product product) {
    }

    public void reportFrameLeft(Data frameData, Product call) {
    }

    public Statics getStatics() {
        return null;
    }

    public WaitSet getWaitSet() {
        return null;
    }

    public void dumpFrames(List<StackFrame> stackFrames) {
    }

    public Data getFrameData() {
        return null;
    }

    public Product getAndResetProductToBeExecuted() {
        return null;
    }

    public void setProductToBeExecuted(Product p) {
    }

    public void setEventHub(EventHub eventStream) {
    }

    public EventStream getEventStream() {
        return null;
    }

    public EventProcessor.Entry getEventEntry() {
        return null;
    }

    public void dumpPendingEvents() {
    }

    public IServiceRequestManager getServiceRequestManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setServiceRequestManager(IServiceRequestManager serviceRequestManager) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public IConfigurationManager getConfigurationManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void shutdownEverything() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void preprocessEvent(Event event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setExecutionResult(ExecutionResult executionResult) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ExecutionResult getExecutionResult() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isAlive() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<ExecutionContext> getSubContexts() {
        return null;
    }

    public void dumpState() {
    }

    public void setMediaTranslationManager(MediaTranslationManager mediaTranslationManager) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public MediaTranslationManager getMediaTranslationManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getState() {
        return null;
    }
}
