package com.mobeon.masp.profilemanager.distributionlists;

import com.abcxyz.services.moip.distributionlist.DistributionListFactoryImpl;

public class MultimediaDistributionListFactoryImpl extends DistributionListFactoryImpl implements MultimediaDistributionListFactory{
	String userMsid = null;
	public MultimediaDistributionListFactoryImpl(String aUserMsid){
		super();
		userMsid = aUserMsid;
	}
	public MultimediaDistributionListManager getDistributionListManager(String aUserMsid){
		return new MultimediaDistributionListManagerImpl(aUserMsid);
	}
}
