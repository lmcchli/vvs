/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox.search;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import com.mobeon.masp.mailbox.IStoredMessage;

/**
 * RecipientCriteria Tester.
 *
 * @author MANDE
 * @since <pre>12/15/2006</pre>
 * @version 1.0
 */
public class RecipientCriteriaTest extends MockObjectTestCase {
    RecipientCriteria recipientCriteria;
    static final String RECIPIENT = "recipient";

    public RecipientCriteriaTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        recipientCriteria = new RecipientCriteria(RECIPIENT);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAccept() throws Exception {
        Mock mockVisitor = mock(MessagePropertyCriteriaVisitor.class);
        mockVisitor.expects(once()).method("visitRecipientCriteria").with(same(recipientCriteria));
        recipientCriteria.accept((MessagePropertyCriteriaVisitor)mockVisitor.proxy());
    }

    public void testMatch() throws Exception {
        Mock mockStoredMessage = mock(IStoredMessage.class);
        mockStoredMessage.expects(once()).method("getRecipients").will(returnValue(new String[0]));
        assertEquals("Message should not match", false, recipientCriteria.match((IStoredMessage)mockStoredMessage.proxy()));
        mockStoredMessage.expects(once()).method("getRecipients").will(returnValue(new String[] { RECIPIENT }));
        assertEquals("Message should match", true, recipientCriteria.match((IStoredMessage)mockStoredMessage.proxy()));
    }

    public void testClone() throws Exception {
        RecipientCriteria recipientCriteria = this.recipientCriteria.clone();
        assertEquals(this.recipientCriteria, recipientCriteria);
    }

    public static Test suite() {
        return new TestSuite(RecipientCriteriaTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
