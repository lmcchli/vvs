package com.mobeon.masp.execution_engine.runtime.event.delayed;

import com.mobeon.masp.execution_engine.session.ISession;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


public abstract class DelayedEvent implements Delayed {
    public abstract void fireEvent();

    public abstract String sendId();

    private final long expirationTimeNanos;

    public DelayedEvent(long delayTime) {
        expirationTimeNanos = TimeUnit.NANOSECONDS
                .convert(delayTime + System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(expirationTimeNanos - System.currentTimeMillis() * 1000000, TimeUnit.NANOSECONDS);
    }

    public int compareTo(Delayed rhsObj) {
        DelayedEvent rhs = (DelayedEvent) rhsObj;
        return expirationTimeNanos < rhs.expirationTimeNanos ?
                -1 :
                expirationTimeNanos == rhs.expirationTimeNanos ?
                        0 :
                        1;
    }

    public boolean equals(Object rhsObj) {
        if (rhsObj instanceof Delayed) {
            return compareTo((Delayed) rhsObj) == 0;
        } else return false;
    }

    public abstract ISession getSession();

}

