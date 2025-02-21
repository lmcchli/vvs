package com.mobeon.masp.profilemanager.distributionlists;

import com.abcxyz.services.moip.distributionlist.DistributionListException;
import com.abcxyz.services.moip.distributionlist.DistributionListManager;
import com.mobeon.masp.profilemanager.IMultimediaDistributionList;
import com.mobeon.masp.mediaobject.IMediaObject;

public interface MultimediaDistributionListManager extends DistributionListManager{
	public String createDistributionList(int anId) throws DistributionListException;
	public void removeDistributionList(int anId) throws DistributionListException;
	public IMultimediaDistributionList[] getDistributionLists() throws DistributionListException;
	public IMultimediaDistributionList getDistributionList(int anId) throws DistributionListException;
	public void setSpokenName(int anId, IMediaObject aSpokenName) throws DistributionListException;
	public IMediaObject getSpokenName(int anId) throws DistributionListException;
	public String getDistributionListsMsid(int anId)throws DistributionListException;
}