/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import java.util.List;

public class DelayedExceptions extends RuntimeException {
    private List<Throwable> exceptions;

    public DelayedExceptions(List<Throwable> exceptions) {
        super(exceptions.size()+" delayed exceptions(s), the first is: "+exceptions.get(0).getMessage());
        this.exceptions = exceptions;
    }

    public List<Throwable> getExceptions() {
        return exceptions;
    }
}
