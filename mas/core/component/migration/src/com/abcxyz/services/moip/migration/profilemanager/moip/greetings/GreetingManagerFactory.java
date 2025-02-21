package com.abcxyz.services.moip.migration.profilemanager.moip.greetings;

import com.abcxyz.services.moip.migration.profilemanager.moip.BaseContext;
import com.mobeon.common.externalcomponentregister.IServiceInstance;

/**
 * Factory for creating GreetingManager objects.
 *
 * @author mande
 */
public interface GreetingManagerFactory {
    GreetingManager getGreetingManager(BaseContext context, String host, int port, String userId, String password,
                                       String folder, IServiceInstance iServiceInstance);
}
