/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.search;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.masp.mailbox.IStoredMessage;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * ReplyToAddressCriteria Tester.
 *
 * @author MANDE
 * @since <pre>12/15/2006</pre>
 * @version 1.0
 */
public class ReplyToAddressCriteriaTest extends MockObjectTestCase {
    ReplyToAddressCriteria replyToAddressCriteria;
    static final String REPLY_TO_ADDRESS = "replytoaddress";

    public ReplyToAddressCriteriaTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        replyToAddressCriteria = new ReplyToAddressCriteria(REPLY_TO_ADDRESS);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAccept() throws Exception {
        Mock mockVisitor = mock(MessagePropertyCriteriaVisitor.class);
        mockVisitor.expects(once()).method("visitReplyToAddressCriteria").with(same(replyToAddressCriteria));
        replyToAddressCriteria.accept((MessagePropertyCriteriaVisitor)mockVisitor.proxy());
    }

    public void testMatch() throws Exception {
        Mock mockStoredMessage = mock(IStoredMessage.class);
        mockStoredMessage.expects(once()).method("getReplyToAddress").will(returnValue(""));
        assertEquals("Message should not match", false, replyToAddressCriteria.match((IStoredMessage)mockStoredMessage.proxy()));
        mockStoredMessage.expects(once()).method("getReplyToAddress").will(returnValue(REPLY_TO_ADDRESS));
        assertEquals("Message should match", true, replyToAddressCriteria.match((IStoredMessage)mockStoredMessage.proxy()));
    }

    public void testClone() throws Exception {
        ReplyToAddressCriteria replyToAddressCriteria = this.replyToAddressCriteria.clone();
        assertEquals(this.replyToAddressCriteria, replyToAddressCriteria);
    }

    public static Test suite() {
        return new TestSuite(ReplyToAddressCriteriaTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(ReplyToAddressCriteriaTest.suite());
    }
}
