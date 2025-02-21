package com.mobeon.masp.mailbox.mfs;

import junit.framework.Assert;

import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;

@RunWith(JMock.class)
public class MfsStoreAdapterTest extends MsfBaseTest
{	
	private MfsMailboxAccountManager mfsMailboxAccountManager;
	
	public MfsStoreAdapterTest() {
		super("MfsStoreAdapterTest");	
	}
	
    @Before
	public void setUp() throws Exception {
        super.setUp();
        mfsMailboxAccountManager = new MfsMailboxAccountManager();
        MfsContextFactory mfsContextFactory = getMfsContextFactory();
        mfsMailboxAccountManager.setContextFactory(mfsContextFactory);
    }

    @Test
    public void testGetStore()
    {
    	try
		{			
			MailboxProfile mailboxProfile = new MailboxProfile("666", null, null);			
			IMailbox mailbox = mfsMailboxAccountManager.getMailbox(mailboxProfile);
			System.out.println(mailbox.toString());
		    Assert.assertNotNull("Mailbox should not be null", mailbox);
		    
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Got exception... " + ex.toString());
		}
    }
    
    @Test
	public void testGetFolder() 
	{			
		try {
			MfsFolderAdapter folder = (MfsFolderAdapter)mfsStoreAdapter.getFolder("123");
			Assert.assertEquals("123",folder.getName());
		} catch (MailboxException e) {
			e.printStackTrace();
			Assert.fail("Got error " + e.toString());
		}
		
	}
}
