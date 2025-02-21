/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.scoping;

public interface ScopeChangedSubscriber {
    public void enteredScope(Scope newScope);
    public void leftScope(Scope oldScope);
}
