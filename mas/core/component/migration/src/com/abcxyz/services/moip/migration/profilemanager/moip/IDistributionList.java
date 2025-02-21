/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.ProfileManagerException;

/**
 * Interface used to create or modify a distribution list
 */
public interface IDistributionList {
    public String getID();

    public void addMember(String member) throws ProfileManagerException;

    public void removeMember(String member) throws ProfileManagerException;

    public String[] getMembers();

    public IMediaObject getSpokenName() throws ProfileManagerException;

    public void setSpokenName(IMediaObject spokenName) throws ProfileManagerException;
}
