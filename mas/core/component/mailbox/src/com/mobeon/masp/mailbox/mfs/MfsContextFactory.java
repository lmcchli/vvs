package com.mobeon.masp.mailbox.mfs;

import java.util.Properties;

import com.mobeon.masp.mailbox.ContextFactory;


public class MfsContextFactory extends ContextFactory<MfsContext>{

	private Properties defaultSessionProperties;

	@Override
	protected MfsContext newContext() {
		return new MfsContext(defaultSessionProperties);
	}
	
    public void setDefaultSessionProperties(Properties defaultSessionProperties) {
        this.defaultSessionProperties = defaultSessionProperties;
    }
}
