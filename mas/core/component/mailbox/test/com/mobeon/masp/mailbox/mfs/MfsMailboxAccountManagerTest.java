package com.mobeon.masp.mailbox.mfs;

import junit.framework.Assert;

import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxProfile;

@RunWith(JMock.class)
public class MfsMailboxAccountManagerTest extends MsfBaseTest {

	
	private MfsMailboxAccountManager mfsMailboxAccountManager;
	
	public MfsMailboxAccountManagerTest() {
		super(MfsMailboxAccountManagerTest.class.getName());
	}

	@Before
	public void setUp() throws Exception {
        super.setUp(); 
        mfsMailboxAccountManager = new MfsMailboxAccountManager();
        MfsContextFactory mfsContextFactory = getMfsContextFactory();
        mfsMailboxAccountManager.setContextFactory(mfsContextFactory);
    }
	
	@Test
	public void testGetMailboxOldMethod() {
		try {
			mfsMailboxAccountManager = new MfsMailboxAccountManager();
			mfsMailboxAccountManager.setContextFactory(getMfsContextFactory());
			Object o = mfsMailboxAccountManager.getMailbox(null, new MailboxProfile("666", null, null));
			Assert.assertNotNull(o);
		    Assert.assertEquals(MfsStoreAdapter.class, o.getClass());
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Got exception... " + ex.toString());
		}
	}

	@Test
	public void testGetMailbox() {
		try
		{			
			MailboxProfile mailboxProfile = new MailboxProfile("666", null, null);			
			IMailbox mailbox = mfsMailboxAccountManager.getMailbox(mailboxProfile);
			System.out.println(mailbox.toString());
		    Assert.assertNotNull("Mailbox should not be null", mailbox);
		    Assert.assertEquals(MfsStoreAdapter.class, mailbox.getClass());
		} 
		catch (Exception ex) {
			Assert.fail("Got exception... " + ex.toString());
		}
	}
}
