package com.abcxyz.services.moip.migration.profilemanager.moip.greetings;

import com.abcxyz.services.moip.migration.profilemanager.moip.BaseContext;
import com.mobeon.common.externalcomponentregister.IServiceInstance;

/**
 * GreetingManagerFactory creating GreetingManagerImpl objects.
 *
 * @author mande
 */
public class GreetingManagerFactoryImpl implements GreetingManagerFactory {
    public GreetingManager getGreetingManager(BaseContext context, String host, int port, String userId,
                                              String password, String folder, IServiceInstance iServiceInstance) {
        return new GreetingManagerImpl(context, host, port, userId, password, folder, iServiceInstance);
    }
}
