/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.scriptingmirrors;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.Map;
import java.util.concurrent.Callable;

public abstract class MirrorBase extends ScriptableObject {

    private Map<String,Object> hash;

    public abstract Map<String,Object> getHash();

    private ILogger log;

    public ILogger getLog() {
        if (log == null) log = ILoggerFactory.getILogger(getClass());
        return log;
    }

    private Map<String,Object> hash() {
        if (hash == null) hash = getHash();
        return hash;
    }

    public Object get(String name, Scriptable start) {
        Object result = hash().get(name);
        if (result != null) {
            if (result instanceof Callable) {
                try {
                    return ((Callable<Object>) result).call();
                } catch (Exception e) {
                    if (getLog().isDebugEnabled())
                        getLog().debug("Spurious exception when retrieving property " + name + " of " + getClassName(), e);
                }
            }
            return result;
        } else {
            return super.get(name, start);
        }
    }

    public boolean has(String name, Scriptable start) {
        if (hash().containsKey(name)) return true;
        else
            return super.has(name, start);
    }

    public void put(String name, Scriptable start, Object value) {
        if (hash().containsKey(name)) {
            return;
        }
        super.put(name, start, value);
    }

    public void delete(String name) {
        if (hash().containsKey(name)) {
            return;
        }
        super.delete(name);
    }

}
