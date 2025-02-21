package com.mobeon.masp.execution_engine.platformaccess;

import java.io.File;

import com.abcxyz.messaging.common.oam.impl.SimpleXmlFileConfigManager;

public class AuthenticationConfigManager extends SimpleXmlFileConfigManager {
	public AuthenticationConfigManager() throws Exception {
		
	}
	
	public AuthenticationConfigManager(String fileName) throws Exception {
		super(fileName);
	}

	public void init() {
		/*
		try {
			initializeOnFile(File.separator + "opt" + File.separator + "moip" + File.separator + "config" + File.separator + "crpSpecific.conf");
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
}
