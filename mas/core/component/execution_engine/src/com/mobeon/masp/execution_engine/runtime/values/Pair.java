/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.util.NamedValue;

public class Pair extends NamedValue<String,Object> {
    public Pair(String name, Object value) {
        super(name, value);
    }
}
