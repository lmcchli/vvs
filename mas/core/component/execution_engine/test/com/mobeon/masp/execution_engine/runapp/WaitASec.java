/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.util.Ignore;

public class WaitASec {
    public void waitASec() {
        try {
            Thread.sleep(ApplicationBasicTestCase.scale(1000));
        } catch (InterruptedException e) {
            Ignore.interruptedException(e);
        }
    }
}
