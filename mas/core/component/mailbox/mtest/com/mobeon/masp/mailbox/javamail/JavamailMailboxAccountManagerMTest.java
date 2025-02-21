/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.externalcomponentregister.IServiceInstance;
import static com.mobeon.common.externalcomponentregister.IServiceInstance.HOSTNAME;
import static com.mobeon.common.externalcomponentregister.IServiceInstance.PORT;
import com.mobeon.masp.mailbox.BaseMailboxTestCase;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.common.configuration.IConfiguration;
import org.jmock.Mock;

/**
 * @author qhast
 */
public class JavamailMailboxAccountManagerMTest extends BaseMailboxTestCase {

    private Mock serviceInstance;
    private Mock corruptServiceInstance;
    private JavamailMailboxAccountManager accountManager;

    public JavamailMailboxAccountManagerMTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {

        super.setUp();

        serviceInstance = mock(IServiceInstance.class);
        serviceInstance.stubs().method("getProperty").with(eq(HOSTNAME)).will(returnValue("127.0.0.1"));
        serviceInstance.stubs().method("getProperty").with(eq(PORT)).will(returnValue("123"));
        serviceInstance.expects(never()).method("getProperty").with(not(or(eq(HOSTNAME),eq(PORT))));

        corruptServiceInstance = mock(IServiceInstance.class);
        corruptServiceInstance.expects(never()).method("getProperty").with(not(or(eq(HOSTNAME),eq(PORT))));

        accountManager = new JavamailMailboxAccountManager();


        JavamailContextFactory f = new JavamailContextFactory();
        //f.setConfiguration((IConfiguration)configurationMock.proxy());
        f.setConfiguration((IConfiguration)configurationMock.proxy());
        accountManager.setContextFactory(f);

    }

    /**
     * Tests that a corrupt service instance, not providing {@link com.mobeon.common.externalcomponentregister.IServiceInstance#HOSTNAME}
     * and {@link com.mobeon.common.externalcomponentregister.IServiceInstance#PORT}, throws a MailBoxException.
     * @throws Exception
     */
    public void testCorruptServiceInstance() throws Exception {
        try {
            accountManager.getMailbox((IServiceInstance)corruptServiceInstance.proxy(), new MailboxProfile("12345","abcde","u12345@a.b"));
            fail("Should not enter here - MailboxException should hade caught.");
        } catch(MailboxException e) {
            //OK
        }
    }

    /**
     *
     * @throws Exception
     */
    public void testServiceInstance() throws Exception {
        try {
            accountManager.getMailbox((IServiceInstance)serviceInstance.proxy(),new MailboxProfile("12345","abcde","u12345@a.b"));
            fail("Should not enter here - MailboxException should hade caught.");
        } catch(MailboxException e) {
            //OK
        }
    }


}
