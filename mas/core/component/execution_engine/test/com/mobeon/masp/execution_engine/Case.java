/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.abcxyz.messaging.vvs.mas.execution_engine.platformaccess.plugin.framework.APlatformAccessPlugin;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccess;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessUtil;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.util.Tools;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.url.TestHandlerFactory;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;

public abstract class Case extends MockObjectTestCase {
    public static final String EXECUTION_ENGINE_TEST_LOG_XML = "execution_engine/test_log.xml";
	public static final ILogger log = ILoggerFactory.getILogger(Case.class);

    public Case(String name) {
        super(name);
        ILoggerFactory.configureAndWatch(Case.EXECUTION_ENGINE_TEST_LOG_XML);
        TestHandlerFactory.initialize();
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public static Constraint stringEq(final Object reference) {
        return new Constraint() {

            public StringBuffer describeTo(StringBuffer stringBuffer) {
                return stringBuffer.append("stringEq");
            }

            public boolean eval(Object object) {
                if(reference == object)
                    return true;
                log.debug("Comparing "+reference+" with "+object);
                return reference.toString().equals(object.toString());
            }
        };
    }
    public static PlatformAccessFactory platformFactoryInstance() {
        return new PlatformAccessFactory() {
            public PlatformAccess create(ExecutionContext executionContext) {
                return null;
            }

            public PlatformAccessUtil createUtil(ExecutionContext ex) {
                return null;
            }

            public APlatformAccessPlugin createPlugin(ExecutionContext ex) {
                return null;
            }
        };
    }

    public static void die(String reason) {
        if(Tools.isTrueProperty(System.getProperty("com.mobeon.junit.unimplemented.ignore"))) {
            String reasonLc = reason.toLowerCase();
            if( ! ( reasonLc.contains("not implemented") ||
                    reasonLc.contains("not fully implemented")))
                fail(reason);
        } else {
            fail(reason);
        }
    }

}
