package com.mobeon.masp.callmanager;


public interface SubscribeCall extends Call {

	public static final String NOTIFY_REQUEST_URI = "RequestUri";
	public static final String EXPIRY_DATE = "ExpiryDate";

	public String getUserAgentNumber();
	
	public String getDialogInfo();
	
	public String getIsInitial();

	public int getExpires(); 

	public void accept();

	public void reject(String reason);
}
