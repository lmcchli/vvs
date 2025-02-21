/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import java.util.Map;
import java.util.HashMap;

public enum Msgs {

    UNDECLARED_VAR,
    CALL_REJECTED;

    public static final Map<Msgs,String> messages = new HashMap<Msgs,String>();

    static {
        messages.put(Msgs.UNDECLARED_VAR,"Assign to undeclared variable");
        messages.put(Msgs.CALL_REJECTED,"Call rejected");
    }

    public static String message(Msgs key) {
        return (String)messages.get(key);
    }
}
