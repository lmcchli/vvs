package com.abcxyz.services.moip.migration.profilemanager.moip.greetings;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.common.externalcomponentregister.IServiceInstance;

/**
 * Documentation
 *
 * @author mande
 */
public interface GreetingManager {
    void setGreeting(String telephoneNumber, GreetingSpecification specification, IMediaObject mediaObject) throws ProfileManagerException;

    IMediaObject getGreeting(GreetingSpecification specification) throws ProfileManagerException;

    IServiceInstance getServiceInstance();

    void setServiceInstance(IServiceInstance serviceInstance);

    String getGreetingMessageId(GreetingSpecification specification) throws ProfileManagerException;
}
