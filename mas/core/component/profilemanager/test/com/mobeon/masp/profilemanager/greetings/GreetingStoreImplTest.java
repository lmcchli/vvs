package com.mobeon.masp.profilemanager.greetings;

import java.io.File;

import junit.framework.Assert;
import junit.framework.JUnit4TestAdapter;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.McdStub;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.masp.execution_engine.platformaccess.util.GreetingTypeUtil;
import com.mobeon.masp.mediaobject.FileMediaObject;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.ProfileManagerException;


/**
 * @author ealebie
 *
 */
@RunWith(JMock.class)
public class GreetingStoreImplTest {

	static private String strDirectoy = "/tmp/moip/mfs";
	
    /**
     * The file that is loaded by the <code>FileMediaObject</code>'s.
     */
    private static final String FILE_NAME = "mediaobject/test/com/mobeon/masp/mediaobject/gillty.wav";   
    /**
     * The size of each <code>ByteBuffer</code>, used to map file into memory.
     */
    private static final long BUFFER_SIZE = 8 * 1024;
	
	private String phonenumber = "1111111";
	private String folder = phonenumber.concat("/Greetings");
	private IMediaObject mediaObject = null;
	
	@SuppressWarnings("unused")
	private Mockery mockery = new JUnit4Mockery();
	
	static {
		File[] root = File.listRoots();
		if (root != null && root.length > 0) {
			strDirectoy = root[0] + strDirectoy;
		}
	}
	
	@BeforeClass
	public static void setupBefore() throws ConfigurationException, ConfigurationDataException {
		CommonTestingSetup.setup();
	}
	
	@AfterClass
	public static void tearDownAfter() {
		CommonTestingSetup.tearDown();
	}
	
	@Before
	public void setUp() throws Exception {
		
		System.setProperty("-Dabcxyz.mfs.userdir.create", "true");
        CommonMessagingAccess.setMcd(new McdStub());
		mediaObject = new FileMediaObject(new File(FILE_NAME), BUFFER_SIZE);
	}

	/**
	 * Test method for {@link com.mobeon.masp.profilemanager.greetings.GreetingStoreImpl#search(com.mobeon.masp.profilemanager.greetings.GreetingSpecification)}.
	 */
	@Test
	public void testSearch() {

	    String msa = CommonMessagingAccess.getInstance().getMsid(phonenumber).getId();
	    IGreetingStore store =  new GreetingStoreImpl(msa, folder);
	    
	    IGreeting greeting;
	    try {
	        greeting = store.create(GreetingTypeUtil.getGreetingSpecification("allcalls", "voice", "7777777",null), mediaObject);
	        greeting.setMedia(mediaObject.getInputStream());
	        store.store(greeting);
	    } catch (ProfileManagerException e1) {
	    	Assert.fail(e1.getMessage());
	    }
	    try {
	        greeting = store.search(GreetingTypeUtil.getGreetingSpecification("allcalls", "voice", "7777777",null));
	        Assert.assertNotNull(greeting.getMedia());
	    } catch (GreetingNotFoundException e) {
	    	Assert.fail(e.getMessage());
	    }
	}
	
	public static void main(String[] arg) {
		JUnit4TestAdapter testAdapter = new JUnit4TestAdapter(GreetingStoreImplTest.class);
		junit.textui.TestRunner.run(testAdapter);
		System.exit(0);
	}
}
