package com.mobeon.masp.execution_engine.runtime.scoping;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ecma.WatchdogContextFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * This implementation of the ECMAExecutor interface is based on Rhino,
 * see http://www.mozilla.org/rhino/.
 *
 * @author Kenneth Selin
 */
public class ScopeRegistryImpl implements ScopeRegistry {
    private static final WatchdogContextFactory contextFactory = new WatchdogContextFactory();

    private ILogger log = ILoggerFactory.getILogger(ScopeRegistryImpl.class);
    private WatchdogContextFactory.WatchdogContext cx;
    private ExecutionContext executionContext = null;

    static {
        ContextFactory.initGlobal(contextFactory);
    }

    @SuppressWarnings("unchecked") private static final IdentityHashMap<Object, Script>[] buckets = new IdentityHashMap[]{
        new IdentityHashMap<Object, Script>(),
        new IdentityHashMap<Object, Script>(),
        new IdentityHashMap<Object, Script>(),
        new IdentityHashMap<Object, Script>(),
    };
    private static final int NUM_LOCKS = buckets.length;


    // The scopes. First elem,ent is the most global scope. etc.
    private List<ScopeImpl> scopes = new ArrayList<ScopeImpl>();
    private List<ScopeChangedSubscriber> subscribers = new ArrayList<ScopeChangedSubscriber>();

    public static void initContext() {
        //This method may seem odd, but it ensures that the static
        //initializer has been run.
    }

    public ScopeRegistryImpl(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        cx = (WatchdogContextFactory.WatchdogContext) Context.enter();
        try {
            createNewScope(null);
        } finally {
            Context.exit();
        }
    }


    public boolean deleteMostRecentScope() {
        if(scopes.size() > 1) {
            int index = scopes.size() - 1;
            Scope scope = scopes.get(index);
            scopes.remove(index);
            for(ScopeChangedSubscriber subscriber:subscribers) {
                subscriber.leftScope(scope);
            }
            return true;
        } else {
            return false;
        }
    }

    public Scope createNewECMAScope(String scopeName) {
        Scriptable topScope = null;
        if(scopes.size() > 0)
            topScope = scopes.get(0).getScope();
        boolean isRootScope = scopes.isEmpty();
        ScopeImpl scope = new ScopeImpl(topScope,scopeName, cx, executionContext, isRootScope);
        // If the new scope is not the first scope, give it a parent scope.
        if(!scopes.isEmpty()){
            ScopeImpl parentScope = scopes.get(scopes.size()-1);
            scope.setParentScope(parentScope);
        }
        scopes.add(scopes.size(), scope);
        return scope;
    }

    public boolean deleteMostRecentECMAScope() {
        if(scopes.size() > 1) {
            int index = scopes.size() - 1;
            Scope scope = scopes.get(index);
            scopes.remove(index);
            return true;
        } else {
            return false;
        }
    }

    public Scope getMostRecentScope() {
        return scopes.get(scopes.size()-1);
    }

    public Scope createNewScope(String scopeName) {

        Scriptable topScope = null;
        if(scopes.size() > 0)
            topScope = scopes.get(0).getScope();

        boolean isRootScope = scopes.isEmpty();
        ScopeImpl scope = new ScopeImpl(topScope,scopeName, cx, executionContext, isRootScope);
        // If the new scope is not the first scope, give it a parent scope.
        if(! scopes.isEmpty()){
            ScopeImpl parentScope = scopes.get(scopes.size()-1);
            scope.setParentScope(parentScope);
        }
        scopes.add(scopes.size(), scope);
        for(ScopeChangedSubscriber subscriber:subscribers) {
            subscriber.enteredScope(scope);
        }
        return scope;
    }

    public Scope getTopLevelScope(){
        return scopes.get(0);
    }

    public void addScopeChangedSubscriber(ScopeChangedSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void removeScopeChangedSubscriber(ScopeChangedSubscriber subscriber) {
        subscribers.remove(subscriber); 
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }
}
