/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

/**
 * The interface to provide access to the class used to generate performance statistic. 
 * <ul>
 * <li>Use enterProfilerPoint() and exitProfilerPoint() to get statistics such as average time,
 * </ul>
 * max time, min time, number of calls, max number of concurrent calls.
 * <br>
 * <br>
 * <pre>
 * Object o = null;
 * try {
 *      if (notifierProfiler.isProfilerEnabled()) {
 *          o = INotifierProfiler.enterProfilerPoint.("myMethod");
 *      }
 * }
 * finally {
 *      if (o != null) {
 *          INotifierProfiler.exitProfilerPoint(o);
 *      }
 * }
 * </pre>
 *
 */
public interface INotifierProfiler {

    /**
     * 
     * @return true if the profiler is enabled, false otherwise
     */
    public boolean isProfilerEnabled();

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
