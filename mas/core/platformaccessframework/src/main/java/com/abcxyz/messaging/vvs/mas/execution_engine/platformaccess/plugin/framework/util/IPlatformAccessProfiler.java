/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.util;

/**
 * The interface to provide access to the class used to generate performance statistic. 
 * 
 * Use enterProfilerPoint() and exitProfilerPoint() to get statistics such as average time,
 * max time, min time, number of calls, max number of concurrent calls.
 * <br>
 * <br>
 * <pre>
 * Object o = null;
 * try {
 *      o = IPlatformAccessProfiler.enterProfilerPoint.("myMethod");
 * }
 * finally {
 *      IPlatformAccessProfiler.exitProfilerPoint(o);
 * }
 * </pre>
 *
 */
public interface IPlatformAccessProfiler {

    /**
     * @param pointName checkpoint name - usually a method name
     * @return Object to be passed to exitProfilerPoint() later on
     */
    public Object enterProfilerPoint(String pointName);

    /**
     * @param  objectPoint Object returned by a previous call to enterProfilerPoint()
     */
    public void exitProfilerPoint(Object objectPoint);

}
