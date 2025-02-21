/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.ecma;

import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;

/**
 * @author David Looberger
 */
public class MASWrapFactory extends WrapFactory {
     public MASWrapFactory() {
        setJavaPrimitiveWrap(false);
    }

    public Scriptable wrapNewObject(Context cx, Scriptable scope, Object obj)
    {
        if (obj instanceof String) {
            return ScriptRuntime.newObject(cx, scope, "String",
                                           new Object[] { obj });
        }
        return super.wrapNewObject(cx, scope, obj);
    }
}
