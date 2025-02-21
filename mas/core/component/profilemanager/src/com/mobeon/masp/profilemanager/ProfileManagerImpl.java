/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import java.net.URI;
import java.net.URISyntaxException;

import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.provisioningagent.utils.PAConstants;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessException;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryUpdater;



/**
 * Documentation
 *
 * @author mande
 */
public class ProfileManagerImpl implements IProfileManager {


    private BaseContext context;


    public ProfileManagerImpl() {
    }

    public BaseContext getContext() {
        return context;
    }

    public void setContext(BaseContext context) {
        this.context = context;
        this.context.setProfileManager(this);
    }

    public IProfile getProfile(String phoneNumber)
    {
        IDirectoryAccessSubscriber subscriber = DirectoryAccess.getInstance().lookupSubscriber(phoneNumber);

        if (subscriber != null)
        {
            VmSubscriber sub = new VmSubscriber(phoneNumber, getContext(),subscriber);
            return sub;
        }
        return null;

    }


    public void init() {

    }

    @Override
    public boolean isProfileUpdatePossible(String phoneNumber) {

        return DirectoryAccess.getInstance().isProfileUpdatePossible();
    }

    public boolean removeProfileFromCache(String profileClass, String phoneNumber) {
        return DirectoryAccess.getInstance().removeFromCache(profileClass, phoneNumber);
    }

    public boolean deleteProfile(String muid) {

        String profileClass = "subscriber";
        URI keyId = null;

        // Only identity type supported is muid:
        if ((muid) == null) {
            return false;
        }
        if (!muid.startsWith("muid:")) {
            return false;
        }

        try {
            keyId = new URI(muid);
        }
        catch (URISyntaxException e) {
            return false;
        }

        try {
            DirectoryUpdater.getInstance().deleteProfile(profileClass, keyId);
        }
        catch (DirectoryAccessException e) {
            return false;
        }

        return true;
    }

    public boolean autoprovisionProfile(String phoneNumber, String subscriberTemplate) {


        String profileClass = "subscriber";
        URI keyId = null;
        Profile entity = new ProfileContainer();

        entity.addAttributeValue("CNServices", "MOIP");

        String strippedPhoneNumber = null;

        // Only identity type supported (if specified) is tel:
        if (phoneNumber != null && phoneNumber.contains(":")) {
            if (!phoneNumber.contains("tel:")) {
                return false;
            }
        }

        String telId = null;
        if (!phoneNumber.startsWith("tel:")) {
            telId = "tel:" + phoneNumber;
        } else {
            telId = phoneNumber;
        }

        try {
            keyId = new URI(telId);

        }
        catch (URISyntaxException e) {
            return false;
        }

        // subscriberTemplate is optional;
        // if none provided, PA will use Cm.activeTemplateMOIPsubscriber
        if (subscriberTemplate != null) {
            if (subscriberTemplate.contains(":")) {
                if (!subscriberTemplate.startsWith("muid:")) {
                    return false;
                }
            } else {
                subscriberTemplate = "muid:" + subscriberTemplate;
            }
            entity.addAttributeValue(PAConstants.AUTOPROVISION_FORCE_TEMPLATE, subscriberTemplate);
        }

        try {
            DirectoryUpdater.getInstance().autoprovisionProfile(profileClass, keyId, entity);
        }
        catch (DirectoryAccessException e) {
            return false;
        }

        return true;
    }


}
