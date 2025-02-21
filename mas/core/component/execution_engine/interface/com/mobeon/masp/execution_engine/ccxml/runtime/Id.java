/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.session.ISession;

public interface Id<T> {
    public void unregister();
    public String toString();
    String getUnprefixedId();
}
