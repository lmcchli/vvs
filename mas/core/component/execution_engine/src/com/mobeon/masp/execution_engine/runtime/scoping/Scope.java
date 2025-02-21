/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.scoping;

import org.mozilla.javascript.Script;

import java.net.URI;

/**
 * @author Mikael Andersson
 */
public interface Scope extends Cloneable{

    public Object getUndefined();

    public void setValue(String name,Object value);

    /**
     * Use this method if you want to support setting values
     * to variables called like transition.test1 where transition is the name
     * of a scope. The method will locate the scope and set the variable.
     *
     * Use lastEvaluationFailed() afterwards.
     *
     * @param scopeName Name of the scope, e.g. transition
     * @param name name of the variable to be set, e.g. test1
     * @param value the value to assign
     */
    public void setValue(String scopeName ,String name, Object value);


    public Object evaluate(String ecmaScript);

    public Object exec(Script script, URI uri, int lineNumber);
    public Object exec(Script script, String originalExpression);    

    public Script compileAndCache(String script, String cacheKey);


    /**
     * Report whether last call to evaluate failed
     * @return true iff failed
     */
    public boolean lastEvaluationFailed();
    /**
     * Check if the variable is declared in any scope. That is, this
     * and all outer scopes.
     * @param name the variable to check.
     * @return true iff the variable is declared in any scope.
     */
    public boolean isDeclaredInAnyScope(String name);

    /**
     * Check if the variable is declared in exactly this scope.
     * NO CHECK IN OUTER SCOPES is done.
     * @param name the variable to check.
     * @return true iff the variable is declared in exactly scope.
     */
    public boolean isDeclaredInExactlyThisScope(String name);

    /**
     * Checks if a variable, according to its syntax, has a scope prefix.
     * @param name name of variable.
     * @return true if the variable has prefix.
     */
    public boolean hasPrefix(String name);

    boolean toBoolean(Object object);

    /**
     * Declare a variable in a scope, using an initial expression.
     * @param name name of the variable to declare.
     * @param initialValueExpression ECMASCript expression. If null, the
     * variable will be given the value "undefined".
     */

    void evaluateAndDeclareVariable(String name, String initialValueExpression);

    public void declareReadOnlyVariable(String name, Object value);

    /**
     * Convenient method to convert java value to its closest representation in JavaScript.
     * @param value
     * @return the Wrapped representation
     */
    public Object javaToJS(Object value);

    String toString(Object value);

    public void evaluateAndDeclareVariable(String nameExpr, Object valueExpr, boolean evaluateName, boolean evaluateValue);

    public Object getValue(String name);


    /**
     * Get an array of property ids.
     * @return
     * an array of Objects. Each entry in the array is either a java.lang.String or a java.lang.Number.
     * an empty array is returned if there were no property ids.
     * @param s: name of the variable
     */
    Object[] getPropertyIds(String s);

    /**
     * Register a class as a valid object name. This name can then be used to create new
     * objects in Javascript execution environment 
     * @param name The name fully qualified name of the class. e.g. the class XMLHttpRequest
     * is registered using "com.mobeon.masp.execution_engine.runtime.xmlhttprequest.XMLHttpRequest"
     */
    public void register(String name);
}
