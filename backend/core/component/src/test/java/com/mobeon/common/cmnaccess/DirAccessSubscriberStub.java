package com.mobeon.common.cmnaccess;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.exception.IdGenerationException;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.directoryaccess.MoipProfile;

public class DirAccessSubscriberStub extends DirectoryAccessSubscriber {

    public DirAccessSubscriberStub(MoipProfile subscriberProfile, MoipProfile cos, MoipProfile multiline, LogAgent logAgent)
    {
        super(subscriberProfile, cos, multiline, logAgent);
    }

    public String getMSID() {
        String id;
        try {
            id = MFSFactory.getMsid("demouser@abcxyz.com" );
            return id;
        } catch (IdGenerationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "id";
    }
}
