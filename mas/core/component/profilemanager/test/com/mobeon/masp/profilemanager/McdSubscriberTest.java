package com.mobeon.masp.profilemanager;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.IsEqual;

import com.abcxyz.services.moip.common.cmnaccess.DAConstants;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObject;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingManager;
import com.mobeon.masp.profilemanager.greetings.GreetingManagerFactory;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;

public class McdSubscriberTest extends MockObjectTestCase {

	private IDirectoryAccess mcd;
	
	public McdSubscriberTest() {
		mcd = new McdStub();
		CommonMessagingAccess.setMcd(mcd);
	}
	
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGetGreeting() {
		BaseContext context = new BaseContext();
		
		IMediaObject media = new MediaObject();
		Mock greetingManagerMock = new Mock(GreetingManager.class);
		greetingManagerMock.expects(once()).method("getGreeting").will(returnValue(media));
		
		Mock greetingManagerFactoryMock = new Mock(GreetingManagerFactory.class);
		greetingManagerFactoryMock.expects(once()).method("getGreetingManager")
		.will(returnValue(greetingManagerMock.proxy()));
		
		McdSubscriber subscriber = new McdSubscriber("5555555", context, mcd);
		subscriber.setGreetingManagerFactory((GreetingManagerFactory)greetingManagerFactoryMock.proxy());
		GreetingSpecification specification = 
			new GreetingSpecification("allcalls", GreetingFormat.VOICE);
		
		try {
			IMediaObject rcvMedia = subscriber.getGreeting(specification);
			assertEquals(media, rcvMedia);
		} catch (ProfileManagerException e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e.getMessage());
		}
	}


	public void testSetGreeting() {
		final String phone = "5555555";
		BaseContext context = new BaseContext();
		GreetingSpecification specification = 
			new GreetingSpecification("allcalls", GreetingFormat.VOICE);
		
		IMediaObject media = new MediaObject();
		Mock greetingManagerMock = new Mock(GreetingManager.class);
		Constraint[] constraints = {
				new IsEqual(mcd.lookupSubscriber(phone).getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID)),
				new IsEqual(phone), 
				new IsEqual(specification),
				new IsEqual(media)
		};
		
		greetingManagerMock.expects(once()).method("setGreeting").with(constraints);
		
		Mock greetingManagerFactoryMock = new Mock(GreetingManagerFactory.class);
		greetingManagerFactoryMock.expects(once()).method("getGreetingManager")
		.will(returnValue(greetingManagerMock.proxy()));
		
		try {
			McdSubscriber subscriber = new McdSubscriber(phone, context, mcd);
			subscriber.setGreetingManagerFactory((GreetingManagerFactory)greetingManagerFactoryMock.proxy());
			subscriber.setGreeting(specification, media);
		} catch (ProfileManagerException e) {
			e.printStackTrace();
			fail("Unexpected exception: " + e.getMessage());
		}
	}
}
