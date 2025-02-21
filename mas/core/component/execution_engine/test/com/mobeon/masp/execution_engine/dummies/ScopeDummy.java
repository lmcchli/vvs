package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Undefined;

import java.net.URI;

/**
 *
 */
public class ScopeDummy implements Scope
{
    @MockAction(DELEGATE)
    public Object getUndefined() {
        return Undefined.instance;
    }

    public void setValue(String name, Object value) {
    }

    public void setValue(String scopeName, String name, Object value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object evaluate(String ecmaScript) {
        return null;
    }

    public Object exec(Script script, URI uri, int lineNumber) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object exec(Script script, String originalExpression) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Script compileAndCache(String script, String cacheKey) {
        return null;
    }

    public boolean lastEvaluationFailed() {
        return false;
    }

    public boolean isDeclaredInAnyScope(String name) {
        return false;
    }

    public boolean isDeclaredInExactlyThisScope(String name) {
        return false;
    }

    public boolean hasPrefix(String name) {
        return false;
    }

    public boolean toBoolean(Object object) {
        return false;
    }

    public void evaluateAndDeclareVariable(String name, String initialValueExpression) {
    }

    public void declareReadOnlyVariable(String event, Object connection) {
    }

    public void evaluateAndDeclareVariable(String nameExpr, Object valueExpr, boolean evaluateName, boolean evaluateValue) {
    }

    public Object javaToJS(Object value) {
        return null;
    }

    public String toString(Object value) {
        return null;
    }

    public Object getValue(String name) {
        return null;
    }

    public Object[] getPropertyIds(String s) {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public void register(String name) {
		// TODO Auto-generated method stub
		
	}
}
