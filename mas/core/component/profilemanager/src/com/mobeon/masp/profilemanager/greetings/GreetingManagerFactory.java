package com.mobeon.masp.profilemanager.greetings;

import com.mobeon.masp.profilemanager.BaseContext;

/**
 * Factory for creating GreetingManager objects.
 *
 * @author mande
 */
public interface GreetingManagerFactory {
    GreetingManager getGreetingManager(BaseContext context, String userId, String telephone, String folder);
}
