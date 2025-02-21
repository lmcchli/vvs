/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;

import java.util.Properties;

import junit.framework.Assert;

import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.mobeon.common.configuration.IGroup;
import com.mobeon.masp.mailbox.IStorableMessage;
import com.mobeon.masp.mailbox.MailboxBaseTestCase;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

/**
 * @author egeobli
 *
 */
@RunWith(JMock.class)
public class MfsStorableMessageFactoryTest extends MailboxBaseTestCase {
	
	public MfsStorableMessageFactoryTest() {
		super("MfsStorableMessageFactoryTest");
	}
	
	/**
	 * Test method for {@link com.mobeon.masp.mailbox.mfs.MfsStorableMessageFactory#create()}.
	 */
	@Test
	public void testCreate() {
		try {
			MfsStorableMessageFactory messageFactory = new MfsStorableMessageFactory();
			messageFactory.setContextFactory(getMfsContextFactory());
			
			IStorableMessage storableMessage = messageFactory.create();
			Assert.assertNotNull(storableMessage);
			Assert.assertTrue(storableMessage instanceof MfsStorableMessage);
		} catch (Exception e) {
			Assert.fail("Unexpected exception: " + e.getMessage());
		}
	}

	@Override
	protected IGroup getMockConfigGroup() {
		final IGroup configGroup = mockery.mock(IGroup.class, "mockConfigGroup");
		return configGroup;
	}

    private MfsContextFactory getMfsContextFactory() throws Exception {
        MfsContextFactory mfsContextFactory = new MfsContextFactory();
        mfsContextFactory.setDefaultSessionProperties(new Properties());
        mfsContextFactory.setConfiguration(getMockConfiguration());
        mfsContextFactory.setMediaObjectFactory(new MediaObjectFactory());
        return mfsContextFactory;
    }
    
}
