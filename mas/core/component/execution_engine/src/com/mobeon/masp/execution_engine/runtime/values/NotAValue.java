package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.execution_engine.runtime.ValueBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 8, 2006
 * Time: 12:12:15 PM
 * To change this template use File | Settings | File Templates.
 *
 * The purpose of this class is to represent that something has gone wrong in earlier
 * operations. The first operation finding a problem is supposed to push a NotAValue on the stack and send an event.
 *
 * This is particularly useful in CCXML when events do not take effect until the transition is complete. Many operations may need to run after the problem is found,
 * and are in the case of finding a NotAValue on the stack, supposed to do nothing but push NotAValues on their own.
 *
 *
 */



public class NotAValue extends ValueBase {
    public Object accept(ExecutionContext ex, ValueVisitor visitor) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
