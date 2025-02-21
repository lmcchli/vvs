package com.mobeon.masp.profilemanager.greetings;

import com.abcxyz.messaging.mfs.MsgStoreServerFactory;
import com.mobeon.masp.profilemanager.BaseContext;

/**
 * GreetingManagerFactory creating GreetingManagerImpl objects.
 *
 * @author mande
 */
public class GreetingManagerFactoryImpl implements GreetingManagerFactory {
    public GreetingManager getGreetingManager(BaseContext context, String userId, String telephone, String folder) {
        if (MsgStoreServerFactory.isTypeNoSQL()) {
            return new GreetingManagerImplNoSQL(context, userId, telephone, folder);
        } else {
            return new GreetingManagerImpl(context, userId, folder);
        }
    }
}
