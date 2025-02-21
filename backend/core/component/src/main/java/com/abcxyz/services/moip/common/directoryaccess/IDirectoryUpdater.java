package com.abcxyz.services.moip.common.directoryaccess;

import java.net.URI;
import java.util.List;

import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;

public interface IDirectoryUpdater extends IDirectoryAccess {
	public void createProfile(String profileClass, URI keyId, Profile entity) throws DirectoryAccessException ;
	public void deleteProfile(String profileClass, URI keyId) throws DirectoryAccessException ;
	public void updateProfile(String profileClass, URI keyId, List<Modification> mods) throws DirectoryAccessException;
	public Profile lookup(String profileClass, URI uri);
}
