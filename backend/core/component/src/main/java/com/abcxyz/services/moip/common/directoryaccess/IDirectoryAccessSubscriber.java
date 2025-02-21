package com.abcxyz.services.moip.common.directoryaccess;
/**
 * A DirectoryAccessSubscriber is composed of a subscriber profile and cos profile in MCD.
 * If an attribute is not present in the subscriber, then it is fetched from the cos
 * of the subscriber.
 *
 * 
 */
public interface IDirectoryAccessSubscriber {

	public String getSubscriberIdentity(String identityTag);
	
	public String[] getSubscriberIdentities(String identityTag);

	public String[] getStringAttributes(String attrName);

	public int[] getIntegerAttributes(String attrName);

	public boolean[] getBooleanAttributes(String attrName);

	public MoipProfile getSubscriberProfile();

	public MoipProfile getCosProfile();

	public MoipProfile getMultilineProfile();

	public String[] subscriberGetCosStringAttribute(String attrName);

	public int[] subscriberGetCosIntegerAttribute(String attrName);

	public boolean[] subscriberGetCosBooleanAttribute(String attrName);
	
	public boolean hasVoiceMailService();
}
