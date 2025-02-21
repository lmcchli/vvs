/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager;

import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.services.moip.distributionlist.DistributionListException;
import com.mobeon.masp.profilemanager.mediafile.IMediaFile;

/**
 * Interface used to create or modify a distribution list
 */
public interface IMultimediaDistributionList extends IMediaFile  {
	public int getId();
	public String[] getMembers() throws DistributionListException;
	public void manipulateListMember(Modification.Operation op, String listMember) throws DistributionListException;
	public String create(int anId) throws DistributionListException;
	public void remove(int anId) throws DistributionListException;
}
