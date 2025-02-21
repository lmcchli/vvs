/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.ecma;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Scriptable;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

public class AsyncResult extends ScriptableObject {
    final String IS_DONE = "isDone";
    final String RESULT = "result";

    private Object result;
    private ResumableCall resumable;
    private int resumeCount;

    private ResumableCall caller;

    public String getClassName() {
        return "AsyncResult";
    }

    public AsyncResult() {
        define();
    }

    private void define() {
        defineProperty(IS_DONE, Boolean.FALSE,0);
        defineProperty(RESULT, Undefined.instance,0);
    }

    public void onNativeDone(ExecutionContext ex, Object nativeResult) {
        try {
            setResult(resumable.resume(nativeResult));
            caller.resume(result);
        } catch (ContinuationAbortedException cae) {
            resumable = cae.getResumable();
        }
    }

    public void setCaller(ResumableCall caller) {
        this.caller = caller;
    }

    public AsyncResult(ResumableCall resumable) {
        this.resumable = resumable;
        define();
    }

    public AsyncResult(Object result) {
        define();
        setResult(result);
    }

    private synchronized void setResult(Object result) {
        this.result = result;
        put(IS_DONE, this, Boolean.TRUE);
        put(RESULT, this, this.result);
    }

    public void put(String event, Scriptable scriptable, Object object) {
        if (event.equals(IS_DONE) && scriptable == this) {
            synchronized(this) {
                super.put(event,scriptable,object);
            }
        } else
            super.put(event, scriptable,object);
    }

    public Object get(String event, Scriptable scriptable) {
        if (event.equals(IS_DONE) && scriptable == this) {
            synchronized(this) {
                return super.get(event,scriptable);
            }
        } else
            return super.get(event, scriptable);
    }

    public Object getResult() {
        return result;
    }
}
