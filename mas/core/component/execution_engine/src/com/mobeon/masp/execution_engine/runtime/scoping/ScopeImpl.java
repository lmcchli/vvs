/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.scoping;

import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessException;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ecma.MASWrapFactory;
import com.mobeon.masp.execution_engine.runtime.ecma.WatchdogContextFactory;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.mozilla.javascript.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mikael Andersson
 *         TODO: NOT DONE
 */
public class ScopeImpl implements Scope {
    
    public void register(String name) {
        Class<?> c;
        try {
            c = Class.forName(name);
            Method[] methods = c.getDeclaredMethods();

            for(int i = 0; i < methods.length; i++)
            {
                Method method = methods[i];
                if(!method.getName().equals("register")) {
                    continue;
                }
                Object args[] = {
                        scope
                };  
                method.invoke(null, args);       
            }
        }
        catch(Exception e) {
            log.error("Failed to register class named: " + name);
        }
    }
        

    private String scopeName;
    private ScopeImpl parentScopeImpl;
    private boolean lastEvaluationFailed = false;
    private static Map<String, Script> globalScripts = new ConcurrentHashMap<String, Script>();
    private static ScriptableObject sharedScope;

    static {
        ScopeRegistryImpl.initContext();
        Context staticContext = Context.enter();
        staticContext.setOptimizationLevel(1);
        sharedScope = staticContext.initStandardObjects(null, true);
        Context.exit();
    }

    public Scriptable getScope() {
        return scope;

    }

    private static class OurScriptableObject extends ScriptableObject {
        private static final String teleportName = "__teleport";

        public String getClassName() {
            return "OurScriptableObject";
        }


        public void teleport(Object value) {
            put(teleportName, this, value);
        }

        public String teleportExpr(String name) {
            return name + "=" + teleportName;
        }

        public void unteleport() {
            delete(teleportName);
        }
    }

    private Context context;
    private OurScriptableObject scope;
    private static final ILogger log = ILoggerFactory.getILogger(ScopeImpl.class);
    private Scriptable topScope;
    private ExecutionContext executionContext;

    public ScopeImpl(Scriptable topScope, String scopeName, Context context, ExecutionContext executionContext,
                     boolean isRootScope) {
        this.topScope = topScope;
        this.context = context;
        this.executionContext = executionContext;
        this.context.setWrapFactory(new MASWrapFactory());
        scope = new OurScriptableObject();
        if (topScope == null) this.topScope = this.scope;
        if (scopeName != null) {
            scope.defineProperty(scopeName, scope, ScriptableObject.READONLY);
            this.scopeName = scopeName;
        }
        if (isRootScope) {
            scope.setPrototype(sharedScope);
        }
    }

    public void evaluateAndDeclareVariable(String name, String initialValueExpression) {
        enterContext();
        try {
            Object value;
            if (initialValueExpression != null) {
                value = evaluate(initialValueExpression);
                if (lastEvaluationFailed()) {
                    return;
                }
            } else {
                value = Context.getUndefinedValue();
            }
            ScriptableObject.defineProperty(scope, name, value, ScriptableObject.EMPTY);
        } finally {
            Context.exit();
        }
    }

    public void evaluateAndDeclareVariable(String nameExpr, Object valueExpr, boolean evaluateName, boolean evaluateValue) {
        enterContext();
        try {
            Object value = valueExpr;
            if (valueExpr != null) {
                if (evaluateValue) {
                    value = evaluate(valueExpr.toString());
                    if (lastEvaluationFailed()) {
                        return;
                    }
                }
            } else {
                value = Context.getUndefinedValue();
            }
            if (evaluateName) {
                scope.teleport(value);
                evaluate(scope.teleportExpr(nameExpr));
                scope.unteleport();
            } else {
                ScriptableObject.defineProperty(scope, nameExpr, value, ScriptableObject.EMPTY);
            }
        } finally {
            Context.exit();
        }
    }

    public boolean isDeclaredInAnyScope(String variableName) {
        enterContext();
        try {
            return findScopeOfVariable(scope, variableName) != null;
        } finally {
            Context.exit();
        }
    }

    public Object evaluate(String expression) {
        enterContext();
        WatchdogContextFactory.setExpression(context, expression);
        try {
            /*Script scriptObject = compile(expression, false, false);
           return scriptObject.exec(context,scope);*/
            return context.evaluateString(scope, expression, "script", 1, null);
        } catch (PlatformAccessException pe) {
            if (this.executionContext != null) {
                this.executionContext.getEventHub().fireContextEvent(pe.getMessage(), pe.getDescription(), null);
            } else {
                if (log.isDebugEnabled()) log.debug("Failed to send event. Have no execution context");
            }
            if (log.isDebugEnabled()) log.debug(pe.toString() + " " + pe.getDescription(), pe);
            lastEvaluationFailed = false;
            return Undefined.instance;
        } catch (EvaluatorException eve) {
            String msg = "Evaluation of ECMA script expression '" + expression
                    + "' failed: " + eve.getMessage() + ", Line " +
                    eve.lineNumber() + ": " + eve.lineSource();
            log.debug(msg);
            if (log.isDebugEnabled()) {
                if (log.isDebugEnabled()) log.debug(eve.getCause(), eve);
            }
            lastEvaluationFailed = true;
            return null;
        } catch (Throwable t) {
            String msg = "Evaluation of ECMA script expression '" + expression
                    + "' failed: " + t.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(msg, t);
                log.debug(t.getCause(), t);
            }
            lastEvaluationFailed = true;
            return null;
        } finally {
            Context.exit();
        }
    }

    private void enterContext() {
        lastEvaluationFailed = false;
        Context.enter(context);
    }

    public Object exec(Script script, URI uri, int lineNumber) {
        enterContext();
        WatchdogContextFactory.setLocation(context, uri, lineNumber);

        return doExec(script);
    }

    public Object exec(Script script, String originalExpression) {
        enterContext();
        WatchdogContextFactory.setExpression(context, originalExpression);

        return doExec(script);
    }

    private Object doExec(Script script) {
        try {
            return script.exec(context, scope);
        } catch (PlatformAccessException pe) {
            if (this.executionContext != null) {
                this.executionContext.getEventHub().fireContextEvent(pe.getMessage(), pe.getDescription(), null);
            } else {
                if (log.isDebugEnabled()) log.debug("Failed to send event. Have no execution context");
            }
            if (log.isDebugEnabled()) log.debug(pe.getMessage() + " " + pe.getDescription());
            lastEvaluationFailed = false;
            return Undefined.instance;
        } catch (EvaluatorException eve) {
            String msg = "Evaluation of cached ECMA script  "
                    + "' failed: " + eve.getMessage() + ", Line " +
                    eve.lineNumber() + ": " + eve.lineSource();
            if (log.isDebugEnabled()) {
                log.debug(msg);
                log.debug(eve.getCause(), eve);
            }
            lastEvaluationFailed = true;
            return null;
        } catch (Throwable t) {
            String msg = "Evaluation of cached ECMA script expression '" +
                    "' failed: " + t.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(msg);
                log.debug(t.getCause(), t);
            }
            TestEventGenerator.generateEvent(TestEvent.SCOPE_EVALUATION_FAILED,t.getMessage());
            lastEvaluationFailed = true;
            return null;
        } finally {
            Context.exit();
        }
    }

    public boolean lastEvaluationFailed() {
        return lastEvaluationFailed;
    }

    /**
     * The simplest form of cacheing a script. The script is precompiled and put into a
     * global hashmap. This could easily be refined further.
     *
     * @param script
     * @param cacheKey
     * @return The compiled representation of the script
     */
    public Script compileAndCache(String script, String cacheKey) {
        enterContext();
        Script s = null;
        try {
            s = context.compileString(script, cacheKey, 1, null);
        } catch (PlatformAccessException pe) {
            if (this.executionContext != null) {
                this.executionContext.getEventHub().fireContextEvent(pe.getMessage(), pe.getDescription(), null);
            } else {
                if (log.isDebugEnabled()) log.debug("Failed to send event. Have no execution context");
            }
            if (log.isDebugEnabled()) log.debug(pe.toString() + " " + pe.getMessage() + " " + pe.getDescription(), pe);
            lastEvaluationFailed = false;
            return null;
        } catch (EvaluatorException eve) {
            compilationFailed(eve, script);
            lastEvaluationFailed = true;
            return null;
        } catch (Throwable t) {
            compilationFailed(t, script);
            lastEvaluationFailed = true;
            return null;
        }
        finally {
            Context.exit();
        }
        if (s != null) {
            globalScripts.put(cacheKey, s);
        }
        return s;
    }

    public static Script getScriptFromCache(String cacheKey) {
        return globalScripts.get(cacheKey);
    }


    public static Script compile(String expression, URI uri, int lineNumber) {
        Context cx = Context.enter();
        Script s = null;
        try {
            s = cx.compileString(expression, uri.getPath(), lineNumber, null);
        } catch (EvaluatorException eve) {
            compilationFailed(eve, expression, uri, lineNumber);
            return null;
        } catch (Throwable t) {
            compilationFailed(t, expression);
            return null;
        }
        finally {
            Context.exit();
        }
        return s;
    }

    public void setParentScope(ScopeImpl parent) {
        scope.setParentScope(parent.scope);
        parentScopeImpl = parent;
    }

    public boolean isDeclaredInExactlyThisScope(String name) {
        Context.enter(context);
        try {
            return scope.has(name, scope);
        } finally {
            Context.exit();
        }
    }

    public boolean hasPrefix(String name) {
        if (name == null) {
            return false;
        }
        return name.indexOf('.') != -1;
    }


    public boolean toBoolean(Object object) {
        Context.enter(context);
        try {
            return ScriptRuntime.toBoolean(object);
        } finally {
            Context.exit();
        }
    }

    public String toString(Object value) {
        Context.enter(context);
        try {
            return ScriptRuntime.toString(value);
        } finally {
            Context.exit();
        }
    }

    public Object getUndefined() {
        return Context.getUndefinedValue();
    }

    public void setValue(String name, Object value) {
        Context.enter(context);
        try {
            if (value == null) value = getUndefined();
            Scriptable s = findScopeOfVariable(scope, name);
            s = scopeOrTopScope(s, topScope);
            ScriptableObject.putProperty(s, name, value);
        } finally {
            Context.exit();
        }
    }

    public void setValue(String scopeName, String name, Object value) {
        enterContext();
        try {
            if (value == null) value = getUndefined();
            Scriptable s = findScopeWithName(scope, scopeName);
            if (s == null) {
                lastEvaluationFailed = true;
                return;
            }
            ScriptableObject.putProperty(s, name, value);
        } finally {
            Context.exit();
        }
    }

    private Scriptable findScopeWithName(Scriptable s, String scopeName) {
        ScopeImpl current = this;
        while (current != null) {
            if (current.scopeName != null && current.scopeName.equals(scopeName)) {
                return current.scope;
            }
            current = parentScopeImpl;
        }
        return null;
    }

    public Object getValue(String name) {
        Scriptable s = findScopeOfVariable(scope, name);
        if (s != null) {
            return s.get(name, s);
        } else {
            return Undefined.instance;
        }
    }

    public Object[] getPropertyIds(String s) {
        Object o = evaluate(s);
        if(o instanceof ScriptableObject){
            ScriptableObject scriptableObject = (ScriptableObject) o;
            return scriptableObject.getIds();
        } else
            return new Object[0];
    }

    private Scriptable scopeOrTopScope(Scriptable s, Scriptable topScope) {
        if (s != null) return s;
        return topScope;
    }

    private Scriptable findScopeOfVariable(Scriptable s, String name) {
        boolean has = s.has(name, s);
        if (!has && s.getParentScope() != null) {
            do {
                s = s.getParentScope();
                has = s.has(name, s);
            } while (!has && s.getParentScope() != null);
        }
        if (has) return s;
        else
            return null;
    }

    public void declareReadOnlyVariable(String name, Object value) {
        Context.enter(context);
        try {
            scope.defineProperty(name, value, ScriptableObject.READONLY);
        } finally {
            Context.exit();
        }
    }

    public Object javaToJS(Object value) {
        Object ret = null;
        Context.enter();
        try {
            ret = Context.javaToJS(value, this.scope);
        } finally {
            Context.exit();
        }
        return ret;
    }

    private static void compilationFailed(Throwable t, String script) {
        compilationFailed(t, script, null, 0);
    }

    private static void compilationFailed(Throwable t, String script, URI uri, int lineNumber) {
        if (log.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder();
            msg.append("Compilation of ECMA script expression '").append(script).append("' failed: ").append(t.getMessage());
            if (t instanceof EvaluatorException) {
                EvaluatorException eve = (EvaluatorException) t;
                msg.append(", Line ").append(eve.lineNumber()).append(": ").append(eve.lineSource());
            }
            if (uri != null) {
                msg.append(uri.getPath()).append(":").append(lineNumber);
            }
            log.debug(msg);
            log.debug(t.getCause(), t);
        }
        TestEventGenerator.generateEvent(TestEvent.SCOPE_COMPILATION_FAILED);
    }

}
