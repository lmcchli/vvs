package com.mobeon.masp.profilemanager.greetings;

public interface GreetingStoreFactory {
	IGreetingStore getGreetingStore(String userId, String telephone, String folder);
}
