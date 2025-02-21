package com.abcxyz.service.moip.common.cmnaccess;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.mfs.MFS;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.MsgStoreServerFactory;
import com.abcxyz.messaging.mfs.User;
import com.abcxyz.messaging.mfs.data.StateFileHandle;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.services.moip.common.cmnaccess.UserInbox;
import com.mobeon.common.configuration.ConfigurationException;
import static org.junit.Assert.*;


public class UserInboxTest {

	static MSA omsa;
	static MSA user;
	static MFS mfs;

	@BeforeClass
	static public void setup() throws ConfigurationException, ConfigurationDataException, IdGenerationException {
		CommonTestingSetup.setup();
		//create state files
		omsa = MFSFactory.getMSA("mysender", true);
		user = MFSFactory.getMSA("myuser", true);
		mfs = MsgStoreServerFactory.getMfsStoreServer();
	}

	@AfterClass
	static public void teardown() {
		CommonTestingSetup.deleteMfsDir();
	}


	@Test
	public void testQueryStateAttributes() throws MsgStoreException {

		preparestateFiles();

		UserInbox inbox = new UserInbox(user);

		//set filtering
		inbox.addFilter(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
		inbox.addFilter(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");

		inbox.addC1Filter(Container1.Message_class, "voice");
		inbox.addC1Filter(Container1.Message_class, "fax");
		inbox.addC1Filter(Container1.Message_class, "email");

		inbox.queryMfs();
		int count = inbox.countStateFile(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
		assertTrue(count == 2);

		count = inbox.countStateFile(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
		assertTrue(count == 2);

		count = inbox.countC1StateFile(Container1.Message_class, "voice");
		assertTrue(count == 2);

		count = inbox.countC1StateFile(Container1.Message_class, "fax");
		assertTrue(count == 1);

		count = inbox.countC1StateFile(Container1.Message_class, "email");
		assertTrue(count == 1);

		StateAttributesFilter
		filter = new StateAttributesFilter();

		filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
		count = inbox.countStateFile(filter);
		assertTrue(count == 2);

		filter = new StateAttributesFilter();
		filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
		count = inbox.countStateFile(filter);
		assertTrue(count == 2);

		filter = new StateAttributesFilter();
		filter.setC1AttributeValue(Container1.Message_class, "voice");
		count = inbox.countStateFile(filter);
		assertTrue(count == 2);

		filter = new StateAttributesFilter();
		filter.setC1AttributeValue(Container1.Message_class, "fax");
		count = inbox.countStateFile(filter);
		assertTrue(count == 1);

		filter = new StateAttributesFilter();
		filter.setC1AttributeValue(Container1.Message_class, "email");
		count = inbox.countStateFile(filter);
		assertTrue(count == 1);

		filter = new StateAttributesFilter();
		filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
		filter.setC1AttributeValue(Container1.Message_class, "voice");
		count = inbox.countStateFile(filter);
		assertTrue(count == 1);

		filter = new StateAttributesFilter();
		filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
		filter.setC1AttributeValue(Container1.Message_class, "voice");
		count = inbox.countStateFile(filter);
		assertTrue(count == 1);

		filter = new StateAttributesFilter();
		filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
		filter.setC1AttributeValue(Container1.Message_class, "fax");
		count = inbox.countStateFile(filter);
		assertTrue(count == 1);

		filter = new StateAttributesFilter();
		filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
		filter.setC1AttributeValue(Container1.Message_class, "fax");
		count = inbox.countStateFile(filter);
		assertTrue(count == 0);

		filter = new StateAttributesFilter();
		filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
		filter.setC1AttributeValue(Container1.Message_class, "email");
		count = inbox.countStateFile(filter);
		assertTrue(count == 1);

		filter = new StateAttributesFilter();
		filter.setAttributeValue(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
		filter.setC1AttributeValue(Container1.Message_class, "email");
		count = inbox.countStateFile(filter);
		assertTrue(count == 0);
	}

	private void preparestateFiles() throws MsgStoreException {
		User.createPath(user);
		//create voice new,
		StateFile state = new StateFile(omsa, user,
				MFSFactory.getAnyOmsgid("mysender", "mas"),
				MFSFactory.getAnyRmsgid("myuser") );

		state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
		state.setC1Attribute(Container1.Message_class, "voice");
		StateFileHandle handle = mfs.createState(state);
		handle.release();

		//create voice read
		state = new StateFile(omsa, user,
				MFSFactory.getAnyOmsgid("mysender", "mas"),
				MFSFactory.getAnyRmsgid("myuser") );
		state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
		state.setC1Attribute(Container1.Message_class, "voice");
		handle = mfs.createState(state);
		handle.release();

		//create FAX new
		state = new StateFile(omsa, user,
				MFSFactory.getAnyOmsgid("mysender", "mas"),
				MFSFactory.getAnyRmsgid("myuser") );

		state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
		state.setC1Attribute(Container1.Message_class, "fax");
		handle = mfs.createState(state);
		handle.release();

		//create email read
		state = new StateFile(omsa, user,
				MFSFactory.getAnyOmsgid("mysender", "mas"),
				MFSFactory.getAnyRmsgid("myuser") );		//create email read
		state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
		state.setC1Attribute(Container1.Message_class, "email");
		handle = mfs.createState(state);
		handle.release();
}
}
