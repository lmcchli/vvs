package com.mobeon.masp.execution_engine.runapp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author David Looberger
 */
public abstract class RunappCCXML extends RunappTotal{
    public RunappCCXML(String event) {
        super(event);
    }

        public static Test suite() {
        TestSuite ts = new TestSuite();

        buildSuite(ts, "ApplicationCCXML");
        return ts;
    }
}
