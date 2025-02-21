/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.util;

import java.util.concurrent.TimeUnit;

/**
 * @author David Looberger
 */
public class TimeValue {
    private long time;
    private TimeUnit unit;

    public TimeValue(long time) {
        this.time = time;
        this.unit = TimeUnit.MILLISECONDS;
    }
    public TimeValue(long time, TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    public long getTime() {
        return time;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public String toString() {
        return time+" "+unit;
    }
}
