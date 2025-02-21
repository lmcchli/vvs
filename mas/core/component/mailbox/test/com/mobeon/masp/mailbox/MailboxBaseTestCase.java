/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mailbox;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Documentation
 *
 * @author mande
 */
public abstract class MailboxBaseTestCase {
    protected static final String LOG4J_CONFIGURATION = "log4jconf.xml";
    static {
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }
    
    protected Mockery mockery;
    protected IConfiguration configuration;
    

    public MailboxBaseTestCase(String name) {
    	mockery = new JUnit4Mockery();
    }

    protected IConfiguration getMockConfiguration() throws Exception {
    	if (configuration == null) {
        	configuration = mockery.mock(IConfiguration.class);

        	mockery.checking(new Expectations() {{
        		allowing(configuration).getConfiguration();
        		will(returnValue(configuration));
        		allowing(configuration).getGroup("mailbox");
        		will(returnValue(getMockConfigGroup()));
        	}});
    		
    	}
    	
    	return configuration;
    }

    protected abstract IGroup getMockConfigGroup();

}
