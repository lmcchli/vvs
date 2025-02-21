/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.scoping;

import com.mobeon.masp.execution_engine.voicexml.runtime.ExecutionContextImpl;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * This interface provides functionality for Scope handling.
 * <p>
 * You may create new scopes, but must remove as many scopes as you add.
 * Adding functions and names will be done to the most recent scope.
 * <p>
 *
 * */

public interface ScopeRegistry {
    /**
     * Creates a new scope. The created scope will be the most recent scope
     * when the method returns.
     */
    public Scope createNewScope(String scopeName);

    /**
     * Deletes the most recent scope. It is an error to try to delete the global
     * scope. That is, you can not call this method more than the number of times
     * you have called createNewScope()
     */
     public boolean deleteMostRecentScope();

    /**
     * Creates a new ECMA scope. The created scope will be the most recent scope
     * when the method returns.
     */
    public Scope createNewECMAScope(String scopeName);


    /**
     * Deletes the most recent ECMA scope. It is an error to try to delete the global
     * scope. That is, you can not call this method more than the number of times
     * you have called createNewScope()
     */
     public boolean deleteMostRecentECMAScope();


    /**
     * Returns the most recent (innermost) scope.
     * @return the most recent scope.
     */
    public Scope getMostRecentScope();


    public Scope getTopLevelScope();

    public void addScopeChangedSubscriber(ScopeChangedSubscriber subscriber);

    public void removeScopeChangedSubscriber(ScopeChangedSubscriber subscriber);

    public void setExecutionContext(ExecutionContext executionContext);
}
