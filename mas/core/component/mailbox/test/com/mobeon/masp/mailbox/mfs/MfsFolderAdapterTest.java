package com.mobeon.masp.mailbox.mfs;

import org.jmock.integration.junit4.JMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mobeon.masp.mailbox.IStoredMessageList;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;

@RunWith(JMock.class)
public class MfsFolderAdapterTest extends MsfBaseTest
{
	private MfsMailboxAccountManager mfsMailboxAccountManager;	
	
	public MfsFolderAdapterTest() {
		super(MfsFolderAdapterTest.class.toString());
	}
	
	@Before
	public void setUp() throws Exception 
	{
		super.setUp();

		mfsMailboxAccountManager = new MfsMailboxAccountManager();
		MfsContextFactory mfsContextFactory = getMfsContextFactory();
		mfsMailboxAccountManager.setContextFactory(mfsContextFactory);
	}	
	
	@Test
	public void testSearchMessagesWork()
	{
		try {
			MfsMock mfs = new MfsMock();
			MfsFolderAdapter testFolder = new MfsFolderAdapter("inbox", new MfsContext(null), 
					getMfsStoreAdapterMock(), mfs);
			IStoredMessageList messages = testFolder.getMessages();		
			Assert.assertEquals(messages.isEmpty(),true);
		} catch (MailboxException e) {
			e.printStackTrace();
			Assert.fail("Got exception : " + e.toString());
		}
	}
	
	/**
     * Test get folder name
     * @throws Exception
     */
	@Test
    public void testGetName() throws Exception {   

    	MfsMock mfs = new MfsMock();
    	MfsFolderAdapter testFolder = new MfsFolderAdapter("test",null,getMfsStoreAdapterMock(),mfs);
    	Assert.assertEquals("test",testFolder.getName());
    }
    
	@Test
    public void testGetFolder()
    {
		try {
			MailboxProfile mailboxProfile = new MailboxProfile("666", null, null);
			MfsStoreAdapter store = (MfsStoreAdapter)mfsMailboxAccountManager.getMailbox(null, mailboxProfile);
			MfsFolderAdapter testFolder =  (MfsFolderAdapter)store.getFolder("test");
			Assert.assertNotNull(testFolder);
			Assert.assertEquals("test",testFolder.getName());
		} catch (MailboxException e) {
			e.printStackTrace();
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
    }
    
    private MfsStoreAdapter getMfsStoreAdapterMock()
    {
		MfsStoreAdapter storeAdapter = new MfsStoreAdapter(new MfsContext(null), "666");
		return storeAdapter;
    }  
}
