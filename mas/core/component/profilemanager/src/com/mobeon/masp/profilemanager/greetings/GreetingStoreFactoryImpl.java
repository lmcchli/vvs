package com.mobeon.masp.profilemanager.greetings;

import com.abcxyz.messaging.mfs.MsgStoreServerFactory;

public class GreetingStoreFactoryImpl implements GreetingStoreFactory {

    public IGreetingStore getGreetingStore(String userId, String telephone, String folder) {
        if (MsgStoreServerFactory.isTypeNoSQL()) {
            return new GreetingStoreImplNoSQL(userId, telephone);
        } else {
            return new GreetingStoreImpl(userId, folder);
        }
    }
}
