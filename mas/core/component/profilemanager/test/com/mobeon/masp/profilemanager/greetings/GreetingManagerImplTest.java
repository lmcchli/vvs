package com.mobeon.masp.profilemanager.greetings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength.LengthUnit;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.profilemanager.BaseContext;
import com.mobeon.masp.profilemanager.ProfileManagerException;

@RunWith(JMock.class)
public class GreetingManagerImplTest {

	/** Should correspond to the ones defined in GreetingManagerImpl */
    private final String CONTENT_DURATION       = "Content-Duration";
    private final String GREETING_FILENAME      = "X-EEMS-Filename";
    private final String GREETING_CONTENTTYPE   = "X-EEMS-ContentType";
    private final String GREETING_SIZE          = "X-EEMS-Size";
    
    private Mockery mockery = new JUnit4Mockery();
    
    @BeforeClass
    public static void runBefore() throws ConfigurationException, ConfigurationDataException {
    	CommonTestingSetup.setup();
    }
    
    @AfterClass
    public static void runAfter() {
    	CommonTestingSetup.tearDown();
    }

    @Test
	public void testSetGreeting() throws ProfileManagerException, MimeTypeParseException {
		GreetingManagerImpl greetingManager = new GreetingManagerImpl(null, "uid", "/apps/mfs/msid");

		GreetingSpecification specification = new GreetingSpecification();
		MediaProperties props = new MediaProperties();
		props.setContentType(new MimeType("text/plain"));
		IMediaObject mediaObject = new MediaObject(props);
		mediaObject.setImmutable();
		greetingManager.setGreeting("12345678", specification , mediaObject);
	}

    @Test
	public void testGetGreeting() throws ProfileManagerException, MediaObjectException {
		// Mock the Greeting
		final IGreeting jmockGreeting = mockery.mock(IGreeting.class);
		mockery.checking(new Expectations(){{
			allowing(jmockGreeting).getProperty(GREETING_CONTENTTYPE);
			will(returnValue("audio/x-wav"));

			allowing(jmockGreeting).getProperty(CONTENT_DURATION);
			will(returnValue("25"));

			allowing(jmockGreeting).getProperty(GREETING_FILENAME);
			will(returnValue("hello.wav"));

			allowing(jmockGreeting).getProperty(GREETING_SIZE);
			will(returnValue("10"));

			allowing(jmockGreeting).getMedia();
			will(returnValue(new ByteArrayInputStream(new byte[10])));
		}});
		
	    // Mock the GreetingStore
		final IGreetingStore jmockGreetingStore = mockery.mock(IGreetingStore.class);
		mockery.checking(new Expectations() {{
			allowing(jmockGreetingStore).search(with(any(GreetingSpecification.class)));
			will(returnValue(jmockGreeting));
		}});

	    // Mock the GreetingStoreFactory
		final GreetingStoreFactory jmockGreetingStoreFactory = mockery.mock(GreetingStoreFactory.class);
		mockery.checking(new Expectations() {{
			allowing(jmockGreetingStoreFactory).getGreetingStore(
					with(any(String.class)), 
					with(any(String.class)));
			will(returnValue(jmockGreetingStore));
		}});
		
	    // Mock the IMediaObject
		final IMediaObject jmockMediaObject = mockery.mock(IMediaObject.class);

	    // Mock the IMediaObjectFactory
		final IMediaObjectFactory jmockMediaObjectFactory = mockery.mock(IMediaObjectFactory.class);
		mockery.checking(new Expectations() {{
			allowing(jmockMediaObjectFactory).create(
					with(any(InputStream.class)), 
					with(any(int.class)), 
					with(any(MediaProperties.class)));
			will(returnValue(jmockMediaObject));
		}});
		
	    BaseContext context = new BaseContext();
	    context.setMediaObjectFactory(jmockMediaObjectFactory);
	    GreetingManagerImpl greetingManager = new GreetingManagerImpl(context, "uid", "/apps/mfs/msid");
		greetingManager.setGreetingStoreFactory(jmockGreetingStoreFactory);

		GreetingSpecification specification = new GreetingSpecification();
		greetingManager.getGreeting(specification);
	}


    @Test
    public void testParseMessage() throws ProfileManagerException, MediaObjectException {

		// Mock the Greeting
    	final IGreeting jmockGreeting = mockery.mock(IGreeting.class);
    	mockery.checking(new Expectations(){{
    		allowing(jmockGreeting).getProperty(GREETING_CONTENTTYPE);
    		will(returnValue("audio/x-wav"));

    		allowing(jmockGreeting).getProperty(CONTENT_DURATION);
    		will(returnValue("25"));

    		allowing(jmockGreeting).getProperty(GREETING_FILENAME);
    		will(returnValue("hello.wav"));

    		allowing(jmockGreeting).getProperty(GREETING_SIZE);
    		will(returnValue("10"));

    		allowing(jmockGreeting).getProperty(GREETING_SIZE);
    		will(returnValue("10"));

    		allowing(jmockGreeting).getMedia();
    		will(returnValue(new ByteArrayInputStream(new byte[10])));
    	}});

	    // Mock the GreetingStore
    	final IGreetingStore jmockGreetingStore = mockery.mock(IGreetingStore.class); 
    	mockery.checking(new Expectations(){{
    		allowing(jmockGreetingStore).search(with(any(GreetingSpecification.class)));
    		will(returnValue(jmockGreeting));
    	}});

	    // Mock the GreetingStoreFactory
    	final GreetingStoreFactory jmockGreetingStoreFactory = mockery.mock(GreetingStoreFactory.class);
    	mockery.checking(new Expectations(){{
    		allowing(jmockGreetingStoreFactory).getGreetingStore(with(any(String.class)), with(any(String.class)));
    		will(returnValue(jmockGreetingStore));
    	}});
    	
	    // Mock the IMediaObject
    	final IMediaObject jmockMediaObject = mockery.mock(IMediaObject.class);

	    // Mock the IMediaObjectFactory
    	final IMediaObjectFactory jmockMediaObjectFactory = mockery.mock(IMediaObjectFactory.class);
    	mockery.checking(new Expectations(){{
    		allowing(jmockMediaObjectFactory).create(
				with(any(InputStream.class)), 
				with(any(int.class)), 
				with(any(MediaProperties.class)));
    		will(returnValue(jmockMediaObject));
    	}});
    	
	    BaseContext context = new BaseContext();
	    context.setMediaObjectFactory(jmockMediaObjectFactory);

		GreetingSpecification specification = new GreetingSpecification();

	    GreetingManagerImpl greetingManager = new GreetingManagerImpl(context, "uid", "/apps/mfs/msid");
	    greetingManager.parseMessage(jmockGreeting, specification);
	}

    @Test
	public void testStore() throws MimeTypeParseException, ProfileManagerException {
        GreetingManagerImpl greetingManager = new GreetingManagerImpl(null, "uid", "/apps/mfs/msid");

        GreetingSpecification specification = new GreetingSpecification();
		MediaProperties props = new MediaProperties();
		props.setContentType(new MimeType("audio/x-wav"));
		List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
		byteBuffers.add(ByteBuffer.allocateDirect(200));
		IMediaObject mediaObject = new MediaObject(byteBuffers, props);
		mediaObject.setImmutable();
		
		greetingManager.store("msid", specification, "12345678", mediaObject);
	}

    @Test
	public void testCreateMessage() throws ProfileManagerException, MimeTypeParseException {
		GreetingManagerImpl greetingManager = new GreetingManagerImpl(null, "uid", "/apps/mfs/msid");

		GreetingSpecification specification = new GreetingSpecification();
		specification.setFormat(GreetingFormat.VIDEO);
		specification.setType("cdg");
		specification.setSubId("1111");
		
		MediaProperties props = new MediaProperties();		
		props.setContentType(new MimeType("video/mov"));
		props.addLengthInUnit(LengthUnit.MILLISECONDS, 2000);
		List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();
		byteBuffers.add(ByteBuffer.allocateDirect(123));
		IMediaObject mediaObject = new MediaObject(byteBuffers, props);
		mediaObject.setImmutable();
		greetingManager.createMessage("12345678", specification , mediaObject, "userId");
	}

}
