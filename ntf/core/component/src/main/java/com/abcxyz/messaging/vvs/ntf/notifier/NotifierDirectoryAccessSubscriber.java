/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Iterator;

import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseCosProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.directoryaccess.MoipProfile;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;

/**
 * Wrapper class to allow an INotifierDatabaseSubscriberProfile instance to provide the information
 * that should be provided by an IDirectoryAccessSubscriber instance.
 * 
 * The INotifierDatabaseSubscriberProfile instance can be an implementation from a plug-in.
 */
public class NotifierDirectoryAccessSubscriber implements IDirectoryAccessSubscriber {
        
    private static LogAgent log = NtfCmnLogger.getLogAgent(NotifierDirectoryAccessSubscriber.class);
    
    ANotifierDatabaseSubscriberProfile notifierDatabaseSubcriberProfile = null;

    
    public NotifierDirectoryAccessSubscriber(ANotifierDatabaseSubscriberProfile notifierDatabaseSubProfile) {
        notifierDatabaseSubcriberProfile = notifierDatabaseSubProfile;
    }
    
    @Override
    public String getSubscriberIdentity(String scheme) {
        //Remove ending colon to get a true scheme (e.g. "tel") and not an "identityTag" (e.g. "tel:")
        //since a plug-in implementation of INotifierDatabaseSubscriberProfile might not support it.
        if(scheme.endsWith(":")) {
            scheme = scheme.substring(0, scheme.length()-1);
        }
        String identity = null;
        String[] identityArray = notifierDatabaseSubcriberProfile.getSubscriberIdentities(scheme);
        if(identityArray != null && identityArray.length > 0) {
            identity = identityArray[0];
        } 
        return identity;
    }

    @Override
    public String[] getSubscriberIdentities(String scheme) {
        //Remove ending colon to get a true scheme (e.g. "tel") and not an "identityTag" (e.g. "tel:")
        //since a plug-in implementation of INotifierDatabaseSubscriberProfile might not support it.
        if(scheme.endsWith(":")) {
            scheme = scheme.substring(0, scheme.length()-1);
        }
        return notifierDatabaseSubcriberProfile.getSubscriberIdentities(scheme);
    }

    @Override
    public String[] getStringAttributes(String attrName) {
        return notifierDatabaseSubcriberProfile.getStringAttributes(attrName);
    }

    @Override
    public int[] getIntegerAttributes(String attrName) {
        return notifierDatabaseSubcriberProfile.getIntegerAttributes(attrName);
    }

    @Override
    public boolean[] getBooleanAttributes(String attrName) {
        return notifierDatabaseSubcriberProfile.getBooleanAttributes(attrName);
    }

    @Override
    public MoipProfile getSubscriberProfile() {
        Profile subProfile = new ProfileContainer();
        
        //Add minimum identities.  If needed in the future, add method in INotifierDatabaseSubscriberProfile to get all identities.
        String[] telIdentity = notifierDatabaseSubcriberProfile.getSubscriberIdentities(MCDConstants.IDENTITY_SCHEME_TEL);
        if(telIdentity != null && telIdentity.length > 0) {
            subProfile.addIdentity(DAConstants.IDENTITY_PREFIX_TEL + telIdentity[0]);
        }
        String[] msidIdentity = notifierDatabaseSubcriberProfile.getSubscriberIdentities(MCDConstants.IDENTITY_SCHEME_MSID);
        if(msidIdentity != null && msidIdentity.length > 0) {
            subProfile.addIdentity(DAConstants.IDENTITY_PREFIX_MSID + msidIdentity[0]);
        }
        
        Iterator<String> attributeIterator = notifierDatabaseSubcriberProfile.getAttributeNameIterator();
        if(attributeIterator != null) {
            while(attributeIterator.hasNext()) {
                String attributeName = attributeIterator.next();
                String[] attributeValues = notifierDatabaseSubcriberProfile.getStringAttributes(attributeName);
                for(int i=0; i < attributeValues.length; i++) {
                    subProfile.addAttributeValue(attributeName, attributeValues[i]);
                }
            }
        }
        log.debug("getSubscriberProfile: Built subscriber Profile object: " + subProfile);
        return new MoipProfile(subProfile, log);
    }

    @Override
    public MoipProfile getCosProfile() {
        Profile cosProfile = new ProfileContainer();
        ANotifierDatabaseCosProfile notifierCosProfile = notifierDatabaseSubcriberProfile.getCosProfile();

        if(notifierCosProfile != null) {
            //Add minimum identities.  If needed in the future, add method in INotifierDatabaseSubscriberProfile to get all identities.
            String[] telIdentity = notifierCosProfile.getIdentities(MCDConstants.IDENTITY_SCHEME_CLASSOFSERVICE);
            if(telIdentity != null && telIdentity.length > 0) {
                cosProfile.addIdentity(DAConstants.IDENTITY_PREFIX_COS + telIdentity[0]);
            }
            String[] msidIdentity = notifierCosProfile.getIdentities(MCDConstants.IDENTITY_SCHEME_MSID);
            if(msidIdentity != null && msidIdentity.length > 0) {
                cosProfile.addIdentity(DAConstants.IDENTITY_PREFIX_MSID + msidIdentity);
            }

            Iterator<String> attributeIterator = notifierCosProfile.getAttributeNameIterator();
            if(attributeIterator != null) {
                while(attributeIterator.hasNext()) {
                    String attributeName = attributeIterator.next();
                    String[] attributeValues = notifierCosProfile.getStringAttributes(attributeName);
                    for(int i=0; i < attributeValues.length; i++) {
                        cosProfile.addAttributeValue(attributeName, attributeValues[i]);
                    }
                }
            }
        }
        log.debug("getCosProfile: Built cos Profile object: " + cosProfile);
        return new MoipProfile(cosProfile, log);
    }

    @Override
    public MoipProfile getMultilineProfile() {
        //Not implemented since this method is not currently used and 
        //implementing it would require adding a method to the INotifierDatabaseSubscriberProfile interface.
        return null;
    }

    @Override
    public String[] subscriberGetCosStringAttribute(String attrName) {
        return notifierDatabaseSubcriberProfile.getCosProfile().getStringAttributes(attrName);
    }

    @Override
    public int[] subscriberGetCosIntegerAttribute(String attrName) {
        return notifierDatabaseSubcriberProfile.getCosProfile().getIntegerAttributes(attrName);
    }

    @Override
    public boolean[] subscriberGetCosBooleanAttribute(String attrName) {
        return notifierDatabaseSubcriberProfile.getCosProfile().getBooleanAttributes(attrName);
    }

    @Override
    public boolean hasVoiceMailService() {
        boolean hasVoiceMailService = false;        
        String[] services = notifierDatabaseSubcriberProfile.getStringAttributes(MCDConstants.CNSERVICES_ATTRIBUTE_NAME);        
        if(services != null) {
            for(String s: services) {
                if(s.equalsIgnoreCase(ProvisioningConstants.MOIP_SERVICE_NAME)){
                    hasVoiceMailService = true;
                    break;
                }
            }
        }        
        return hasVoiceMailService;
    }
}
