package com.mobeon.common.cmnaccess;


/**
 * Singleton Wrapper class for Common Messaging Access to ebable JNUIT testing
 * @author lmcraby
 *
 */
public class CommonMessagingAccessTestWrapper  {

	private static CommonMessagingAccessTestWrapper instance  = new CommonMessagingAccessTestWrapper();
	
	private CommonMessagingAccessTestWrapper(){
		
	}
	
	public static CommonMessagingAccessTestWrapper getInstance(){
		return instance;
	}
	
	
    public void setSystemReady() {
    	CommonMessagingAccess.getInstance().setSystemReady();
    }

}
