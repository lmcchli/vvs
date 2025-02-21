package com.mobeon.ntf.out.outdial.test;

// Test variant of OdlWorker that does not go to MUR
// This is to do load testing
//


import java.util.*;

import com.mobeon.common.storedelay.DelayHandler;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.outdial.*;

public class TestOdlWorker extends OdlWorker
{

    public TestOdlWorker(DelayHandler delayer, Map commandHandlers,
                         ManagedArrayBlockingQueue<Object> queue,
                         OdlCallSpec caller,
                         PhoneOnRequester phoneon,
                         UserFactory userFactory,
                         String name)
    {
        super(delayer, commandHandlers, queue, caller, phoneon, userFactory,name);
        System.err.println("**** TEST ODL WORKER CREATED ****");

    }


    /*
    protected UserInfo getUserInfo(OdlInfo info)
    {
        return new TestUserInfo(info);
    }
     **/

}
