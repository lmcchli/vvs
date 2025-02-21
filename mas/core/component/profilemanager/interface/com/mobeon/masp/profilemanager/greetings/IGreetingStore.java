package com.mobeon.masp.profilemanager.greetings;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.ProfileManagerException;

public interface IGreetingStore {
	
	/**
	 * Search for the greeting object that matches there specifications
	 * 
	 * @param specification
	 * @return
	 * @throws GreetingNotFoundException
	 */
	public IGreeting search(GreetingSpecification specification) throws GreetingNotFoundException;
	
	/**
	 * Creates and returns a greeting object
	 * 
	 * @param specification
	 * @param mediaObject
	 * @return
	 * @throws ProfileManagerException
	 */
	public IGreeting create(GreetingSpecification specification, IMediaObject mediaObject) throws ProfileManagerException;
	
	/**
	 * Stores the greeting object
	 * 
	 * @param greeting
	 * @throws ProfileManagerException
	 */
	public void store(IGreeting greeting) throws ProfileManagerException;

}
