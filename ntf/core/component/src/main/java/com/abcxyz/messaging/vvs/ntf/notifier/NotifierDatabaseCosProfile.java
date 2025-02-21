/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.net.URI;
import java.util.Iterator;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseCosProfile;
import com.abcxyz.services.moip.common.directoryaccess.MoipProfile;


public class NotifierDatabaseCosProfile extends ANotifierDatabaseCosProfile {
    MoipProfile cosMoipProfile = null;
    
    public NotifierDatabaseCosProfile(MoipProfile moipProfile) {
        cosMoipProfile = moipProfile;
    }

    @Override
    public String[] getIdentities(String scheme) {
        String[] identitiesString = null;
        URI[] identitiesUri = cosMoipProfile.getIdentities(scheme);
        if(identitiesUri != null && identitiesUri.length > 0) {
            identitiesString = new String[identitiesUri.length];
            for(int i=0; i < identitiesUri.length; i++) {
                identitiesString[i] = identitiesUri[i].getSchemeSpecificPart();
            }
        }
        return identitiesString;
    }
    
    @Override
    public String[] getStringAttributes(String attributeName) {
        return cosMoipProfile.getStringAttributes(attributeName);
    }

    @Override
    public int[] getIntegerAttributes(String attributeName) {
        return cosMoipProfile.getIntegerAttributes(attributeName);
    }

    @Override
    public boolean[] getBooleanAttributes(String attributeName) {
        return cosMoipProfile.getBooleanAttributes(attributeName);
    }

    @Override
    public Iterator<String> getAttributeNameIterator() {            
        return cosMoipProfile.getProfile().attributeIterator();
    }
    
    public String toString() {
        return cosMoipProfile.toString();
    }
}
