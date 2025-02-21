package com.abcxyz.services.moip.common.directoryaccess;
/**
 * A IDirectoryAccessProfile is the interface that represents an MCD profile.
 */
public interface IDirectoryAccessProfile {


	public String[] getIdentity(String identityTag);

	public MoipProfile getProfile();

	public String[] getStringAttributes(String attrName);

	public int[] getIntegerAttributes(String attrName);

	public boolean[] getBooleanAttributes(String attrName);

}
