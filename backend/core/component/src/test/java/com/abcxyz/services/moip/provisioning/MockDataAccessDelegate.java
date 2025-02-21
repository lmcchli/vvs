package com.abcxyz.services.moip.provisioning;

import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.services.moip.provisioning.validation.DataAccessDelegate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Denis Marchesseault
 */
public class MockDataAccessDelegate implements DataAccessDelegate
{
    private final Map<URI, Profile> _profiles = new HashMap<URI, Profile>();


    public Profile lookup(final URI identity,
                          final String profileClass)
    {
        return _profiles.get(identity);
    }


    public Map<URI, Profile> getProfiles()
    {
        return _profiles;
    }


	@Override
	public boolean createProfile(String profileClass, URI keyId, Profile entity) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean deleteProfile(String profileClass, URI keyId) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean updateProfile(String profileClass, URI keyId,
			Modification[] mods) {
		// TODO Auto-generated method stub
		return false;
	}
}

