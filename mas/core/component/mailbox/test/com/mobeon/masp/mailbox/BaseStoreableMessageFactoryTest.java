/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * BaseStoreableMessageFactory Tester.
 *
 * @author MANDE
 * @since <pre>12/12/2006</pre>
 * @version 1.0
 */
public class BaseStoreableMessageFactoryTest extends MockObjectTestCase {
    private Mock mockStorableMessage;
    private BaseStoreableMessageFactory<BaseContext<BaseConfig>> baseStoreableMessageFactory;
    private ContextFactory<BaseContext<BaseConfig>> contextFactory;

    public BaseStoreableMessageFactoryTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        mockStorableMessage = mock(IStorableMessage.class);
        baseStoreableMessageFactory = new BaseStoreableMessageFactory<BaseContext<BaseConfig>>() {
            public IStorableMessage create() {
                return (IStorableMessage)mockStorableMessage.proxy();
            }
        };
        contextFactory = new ContextFactory<BaseContext<BaseConfig>>() {
            protected BaseContext<BaseConfig> newContext() {
                return null;
            }
        };
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetGetContextFactory() throws Exception {
        assertNull(baseStoreableMessageFactory.getContextFactory());
        baseStoreableMessageFactory.setContextFactory(contextFactory);
        assertSame(contextFactory, baseStoreableMessageFactory.getContextFactory());
    }

    public void testCreate() throws Exception {
        assertSame(getStorableMessage(), baseStoreableMessageFactory.create());
    }

    private IStorableMessage getStorableMessage() {
        return (IStorableMessage)mockStorableMessage.proxy();
    }

    public static Test suite() {
        return new TestSuite(BaseStoreableMessageFactoryTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
