/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.imap;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import com.mobeon.masp.mailbox.BaseConfig;

/**
 * ImapContext Tester.
 *
 * @author MANDE
 * @since <pre>12/07/2006</pre>
 * @version 1.0
 */
public class ImapContextTest extends TestCase {
    public ImapContextTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetImapProperties() throws Exception {
        ImapProperties imapProperties = new ImapProperties();
        ImapContext<BaseConfig> imapContext = new ImapContext<BaseConfig>(imapProperties) {
            protected BaseConfig newConfig() {
                return null;
            }
        };
        assertSame(imapProperties, imapContext.getImapProperties());
    }

    public static Test suite() {
        return new TestSuite(ImapContextTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
