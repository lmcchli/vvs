package com.mobeon.ntf.out.mediaconversion;


public class MCResultHandlerFactory {
	private  static MCResultHandlerFactory instance;
	
	public static MCResultHandlerFactory getInstance() {
		if (instance == null) {
			instance = new MCResultHandlerFactory();
		} 
		return instance;
	}
	
	public MCResultHandler createMCResultHandler() {
		return new MCResultHandler();
	}
}
