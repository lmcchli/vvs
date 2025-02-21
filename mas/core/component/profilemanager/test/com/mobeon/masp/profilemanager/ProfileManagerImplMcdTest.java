package com.mobeon.masp.profilemanager;

import org.jmock.MockObjectTestCase;

import com.mobeon.masp.mailbox.IMailbox;

public class ProfileManagerImplMcdTest extends MockObjectTestCase{
	
	public void testGetProfileManagerImplMcd()
	{
		String phoneNumber = "513457900";
		try
		{
		ProfileManagerImpl manager = new ProfileManagerImpl();
		IProfile profile = manager.getProfile(phoneNumber);
		IMailbox mailbox =  profile.getMailbox();
		assertEquals(McdSubscriber.class, profile.getClass());
		assertNull(mailbox);
		}
		catch(HostException e)
		{
			fail("Subscriber " + phoneNumber +  " not found" + e.toString() );
		}
	}

}
