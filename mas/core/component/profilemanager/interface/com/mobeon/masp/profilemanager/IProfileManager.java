/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

/**
 * Interface used to retrieve subscriber profiles. Profiles can be requested synchronously and asynchronously.
 */
public interface IProfileManager
{
	/**
     * Retrieves subscriber profiles
     * @return the subscriber profiles matching filter
     */
	public IProfile getProfile(String phoneNumber);


	/**
	 * Tests whether update is possible in MCD.
	 * @param phoneNumber the phone number
	 * @return true is MCD updates are possible
	 */
	public boolean isProfileUpdatePossible(String phoneNumber);


	/**
	 * Removes the profile from the MCD client cache.
	 * @param profileClass The profileClass type. Must not be null or empty.
	 * @param phoneNumber The profile identity. Must not be null or empty.
	 * @return  True if the profile was removed from the MCD client cache.
	 */
    public boolean removeProfileFromCache(String profileClass, String phoneNumber);
    
    public boolean deleteProfile(String muid);
    
    public boolean autoprovisionProfile(String phoneNumber, String subscriberTemplate);

}
