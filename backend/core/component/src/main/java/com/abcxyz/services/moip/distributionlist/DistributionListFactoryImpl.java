package com.abcxyz.services.moip.distributionlist;

public class DistributionListFactoryImpl implements DistributionListFactory {

	public DistributionListFactoryImpl(){
	}

	public DistributionListManager getDistributionListManager(String anMsid) {
		return new DistributionListManagerImpl(anMsid);
	}
}
