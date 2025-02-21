package com.mobeon.masp.profilemanager.greetings;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.ProfileManagerException;

/**
 * Documentation
 *
 * @author mande
 */
public interface GreetingManager {
    void setGreeting(String telephoneNumber, GreetingSpecification specification, IMediaObject mediaObject) throws ProfileManagerException;

    IMediaObject getGreeting(GreetingSpecification specification) throws ProfileManagerException;
}
