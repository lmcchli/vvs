/**
 * Copyright (c) 2008 Abcxyz AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.test;

import com.mobeon.ntf.util.ErrorLogLimiter;
import com.mobeon.ntf.util.Logger;
import java.util.*;
import junit.framework.*;

public class ErrorLogLimiterTest extends TestCase {

    private ErrorLogLimiter gp;

    public ErrorLogLimiterTest(String name) {
	super(name);
    }

    /*
     *
     */
    public void testErrorLogLimiter() throws Exception {
        int holdtime = 3;
        int next = 10;
	ErrorLogLimiter ell = new ErrorLogLimiter(holdtime, next);
        Exception ex = new Exception("An exception for testing");
        boolean unexpected = true;
        for (int j = 0; j < 5; j++) {
            long nextTime = System.currentTimeMillis();
            for (int i = 0; i < holdtime * 10; i++) {
                nextTime += 100L;
                try { Thread.sleep(nextTime - System.currentTimeMillis()); } catch (InterruptedException e) { ; }
                int count = ell.report("Test message", ex, unexpected);
            }
            nextTime += 100L;
            holdtime = next;
            unexpected = false;
        }
    }
}
