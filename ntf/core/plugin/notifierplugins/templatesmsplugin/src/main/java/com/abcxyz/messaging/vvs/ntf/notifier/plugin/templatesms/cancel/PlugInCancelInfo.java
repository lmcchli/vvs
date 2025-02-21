package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.cancel;



public class PlugInCancelInfo {

    private String cphrContentName;
    private String serviceType;
    
    
	public String getServiceType() {
		return serviceType;
	}

	
	public String getCphrContentName() {
		return cphrContentName;
	}

	public PlugInCancelInfo(String cphrContentName, String serviceType) {
    	this.cphrContentName = cphrContentName;
    	this.serviceType = serviceType;
        
    }

}
