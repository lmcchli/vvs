package com.abcxyz.services.moip.ntf.mail;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.MFS;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.MsgStoreServerFactory;
import com.abcxyz.messaging.mfs.User;
import com.abcxyz.messaging.mfs.data.StateFileHandle;
import com.abcxyz.messaging.mfs.exception.CollisionException;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;

import com.mobeon.ntf.mail.UserMailbox;
import static org.junit.Assert.*;

public class UserMailboxTest {

	static MSA omsa;
	static MSA user;
	static MFS mfs;

	// FIXME This test class needs reviewing - it does not initialize properly on Linux
//    @BeforeClass
    public static void setup() throws Exception {
    	CommonTestingSetup.setup();
		omsa = MFSFactory.getMSA("mysender", true);
		user = MFSFactory.getMSA("myuser", true);
		mfs = MsgStoreServerFactory.getMfsStoreServer();
		preparestateFiles();
    }

//    @AfterClass
    public static void teardown() {
    	CommonTestingSetup.tearDown();
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testUserMailboxWithMail() {
    	UserMailbox userbox = new UserMailbox(user, true, false, false, false);

    	assertTrue(userbox.getAllTotalCount() == 12);
    	assertTrue(userbox.getNewTotalCount() == 7);
    	assertTrue(userbox.getOldTotalCount() == 5);
    	assertTrue(userbox.getAllUrgentTotalCount() == 6);
    	assertTrue(userbox.getNewUrgentTotalCount() == 4);
    	assertTrue(userbox.getOldUrgentTotalCount() == 2);

    	assertTrue(userbox.getNewVideoCount() == 2);
    	assertTrue(userbox.getNewUrgentVideoCount() == 1);
    	assertTrue(userbox.getOldVideoCount() == 2);
    	assertTrue(userbox.getOldUrgentVideoCount() == 1);

    	assertTrue(userbox.getNewFaxCount() == 2);
    	assertTrue(userbox.getNewUrgentFaxCount() == 1);
    	assertTrue(userbox.getOldFaxCount() == 0);
    	assertTrue(userbox.getOldUrgentFaxCount() == 0);

    	assertTrue(userbox.getNewVoiceCount() == 2);
    	assertTrue(userbox.getOldVoiceCount() == 2);
    	assertTrue(userbox.getNewUrgentVoiceCount() == 1);
    	assertTrue(userbox.getOldUrgentVoiceCount() == 1);


    	assertTrue(userbox.getNewEmailCount() == 1);
    	assertTrue(userbox.getNewUrgentEmailCount() == 1);
    	assertTrue(userbox.getOldEmailCount() == 1);
    	assertTrue(userbox.getOldUrgentEmailCount() == 0);
    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testUserMailboxWithoutMail() {
    	UserMailbox userbox = new UserMailbox(user, false, true, true, true);

    	assertTrue(userbox.getAllTotalCount() == 12);
    	assertTrue(userbox.getNewTotalCount() == 7);
    	assertTrue(userbox.getOldTotalCount() == 5);
    	assertTrue(userbox.getAllUrgentTotalCount() == 6);
    	assertTrue(userbox.getNewUrgentTotalCount() == 4);
    	assertTrue(userbox.getOldUrgentTotalCount() == 2);

    	assertTrue(userbox.getNewVideoCount() == 2);
    	assertTrue(userbox.getNewUrgentVideoCount() == 1);
    	assertTrue(userbox.getOldVideoCount() == 2);
    	assertTrue(userbox.getOldUrgentVideoCount() == 1);

    	assertTrue(userbox.getNewFaxCount() == 2);
    	assertTrue(userbox.getNewUrgentFaxCount() == 1);
    	assertTrue(userbox.getOldFaxCount() == 0);
    	assertTrue(userbox.getOldUrgentFaxCount() == 0);

    	assertTrue(userbox.getNewVoiceCount() == 2);
    	assertTrue(userbox.getOldVoiceCount() == 2);
    	assertTrue(userbox.getNewUrgentVoiceCount() == 1);
    	assertTrue(userbox.getOldUrgentVoiceCount() == 1);


    	assertTrue(userbox.getNewEmailCount() == 0);
    	assertTrue(userbox.getNewUrgentEmailCount() == 0);
    	assertTrue(userbox.getOldEmailCount() == 0);
    	assertTrue(userbox.getOldUrgentEmailCount() == 0);

    }

    @Ignore("Ignored until class initialization is fixed")
    @Test
    public void testUserMailboxWithoutService() {
    	UserMailbox userbox = new UserMailbox(user, false, false, false, false);

    	assertTrue(userbox.getAllTotalCount() == 12);
    	assertTrue(userbox.getNewTotalCount() == 7);
    	assertTrue(userbox.getOldTotalCount() == 5);
    	assertTrue(userbox.getAllUrgentTotalCount() == 6);
    	assertTrue(userbox.getNewUrgentTotalCount() == 4);
    	assertTrue(userbox.getOldUrgentTotalCount() == 2);

    	assertTrue(userbox.getNewVideoCount() == 0);
    	assertTrue(userbox.getOldVideoCount() == 0);
    	assertTrue(userbox.getNewUrgentVideoCount() == 0);
    	assertTrue(userbox.getOldUrgentVideoCount() == 0);

    	assertTrue(userbox.getNewFaxCount() == 0);
    	assertTrue(userbox.getOldFaxCount() == 0);
    	assertTrue(userbox.getNewUrgentFaxCount() == 0);
    	assertTrue(userbox.getOldUrgentFaxCount() == 0);

    	assertTrue(userbox.getNewVoiceCount() == 0);
    	assertTrue(userbox.getOldVoiceCount() == 0);
    	assertTrue(userbox.getNewUrgentVoiceCount() == 0);
    	assertTrue(userbox.getOldUrgentVoiceCount() == 0);

    	assertTrue(userbox.getNewEmailCount() == 0);
    	assertTrue(userbox.getOldEmailCount() == 0);
    	assertTrue(userbox.getNewUrgentEmailCount() == 0);
    	assertTrue(userbox.getOldUrgentEmailCount() == 0);
    }


	static private void preparestateFiles() throws MsgStoreException {
		int tried = 0;
		while (tried ++ < 3) {
			try {
				User.createPath(user);
				//create voice new,
				StateFile state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );

				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
				state.setC1Attribute(Container1.Message_class, "voice");
				StateFileHandle handle = mfs.createState(state);
				handle.release();


				//create voice new,
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );

				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
				state.setC1Attribute(Container1.Message_class, "voice");
				state.setC1Attribute(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
				handle = mfs.createState(state);
				handle.release();



				//create voice read
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );
				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
				state.setC1Attribute(Container1.Message_class, "voice");
				handle = mfs.createState(state);
				handle.release();

				//create voice read
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );
				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
				state.setC1Attribute(Container1.Message_class, "voice");
				state.setC1Attribute(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
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


				//create FAX new
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );

				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
				state.setC1Attribute(Container1.Message_class, "fax");
				state.setC1Attribute(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
				handle = mfs.createState(state);
				handle.release();

				//create email read
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );
				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
				state.setC1Attribute(Container1.Message_class, "email");
				handle = mfs.createState(state);
				handle.release();

				//create email read
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );
				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
				state.setC1Attribute(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
				state.setC1Attribute(Container1.Message_class, "email");
				handle = mfs.createState(state);
				handle.release();

				//create video new
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );
				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
				state.setC1Attribute(Container1.Message_class, "video");
				handle = mfs.createState(state);
				handle.release();

				//create video read
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );
				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
				state.setC1Attribute(Container1.Message_class, "video");
				handle = mfs.createState(state);
				handle.release();

				//create video new urgent
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );
				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "new");
				state.setC1Attribute(Container1.Message_class, "video");
				state.setC1Attribute(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
				handle = mfs.createState(state);
				handle.release();

				//create video read urgent
				state = new StateFile(omsa, user,
						MFSFactory.getAnyOmsgid("mysender", "mas"),
						MFSFactory.getAnyRmsgid("myuser") );
				state.setAttribute(StateAttributes.GLOBAL_MSG_STATE_KEY, "read");
				state.setC1Attribute(Container1.Message_class, "video");
				state.setC1Attribute(Container1.Priority, Integer.toString(MoipMessageEntities.MFS_URGENT_PRIORITY));
				handle = mfs.createState(state);
				handle.release();

				break;
			} catch (CollisionException e) {
				CommonTestingSetup.deleteMfsDir();
			}
		}
	}
}
