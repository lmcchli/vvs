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
 * SecondaryRecipientCriteria Tester.
 *
 * @author MANDE
 * @since <pre>12/15/2006</pre>
 * @version 1.0
 */
public class SecondaryRecipientCriteriaTest extends MockObjectTestCase {
    SecondaryRecipientCriteria secondaryRecipientCriteria;
    static final String SECONDARY_RECIPIENT = "secondaryrecipient";

    public SecondaryRecipientCriteriaTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        secondaryRecipientCriteria = new SecondaryRecipientCriteria(SECONDARY_RECIPIENT);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAccept() throws Exception {
        Mock mockVisitor = mock(MessagePropertyCriteriaVisitor.class);
        mockVisitor.expects(once()).method("visitSecondaryRecipientCriteria").with(same(secondaryRecipientCriteria));
        secondaryRecipientCriteria.accept((MessagePropertyCriteriaVisitor)mockVisitor.proxy());
    }

    public void testMatch() throws Exception {
        Mock mockStoredMessage = mock(IStoredMessage.class);
        mockStoredMessage.expects(once()).method("getSecondaryRecipients").will(returnValue(new String[0]));
        assertEquals("Message should not match", false, secondaryRecipientCriteria.match((IStoredMessage)mockStoredMessage.proxy()));
        mockStoredMessage.expects(once()).method("getSecondaryRecipients").will(returnValue(new String[] { SECONDARY_RECIPIENT }));
        assertEquals("Message should match", true, secondaryRecipientCriteria.match((IStoredMessage)mockStoredMessage.proxy()));
    }

    public void testClone() throws Exception {
        SecondaryRecipientCriteria secondaryRecipientCriteria = this.secondaryRecipientCriteria.clone();
        assertEquals(this.secondaryRecipientCriteria, secondaryRecipientCriteria);
    }

    public static Test suite() {
        return new TestSuite(SecondaryRecipientCriteriaTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(SecondaryRecipientCriteriaTest.suite());
    }
}
