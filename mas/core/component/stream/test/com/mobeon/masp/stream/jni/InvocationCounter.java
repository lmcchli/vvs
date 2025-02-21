package com.mobeon.masp.stream.jni;

import org.jmock.core.Stub;
import org.jmock.core.Invocation;

public class InvocationCounter implements Stub
{
    public int counter = 0;

    public StringBuffer describeTo( StringBuffer buffer ) {
        return buffer.append("increments a counter");
    }

    public Object invoke( Invocation invocation ) throws Throwable {
        counter++;
        return null;
    }
}
