package com.mobeon.masp.execution_engine.configuration;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;

public class ExecutionEngineConfigurationTest {

	private IConfiguration configuration;

	public ExecutionEngineConfigurationTest() throws Exception {
		setUp();
	}

    @BeforeClass
    static public void startup() throws Exception {
        BasicConfigurator.configure();
    }


    @AfterClass
    static public void tearDown() {
    }

    protected void setUp() throws Exception {
    	configuration = getConfiguration("cfg/" + CommonOamManager.MAS_SPECIFIC_CONF);
    }

    /**
    *
    * @throws Exception if test case fails.
    */
   @Test
   public void testConfiguration() throws Exception {

   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(RuntimeConstants.CONFIG.ENGINE_STACK_SIZE), 100);
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(RuntimeConstants.CONFIG.CALL_MANAGER_WAIT_TIME), 60000);
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(RuntimeConstants.CONFIG.ACCEPT_TIMEOUT), 30000);
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(RuntimeConstants.CONFIG.CREATECALL_ADDITIONAL_TIMEOUT), 30000);
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getBoolean(RuntimeConstants.CONFIG.TRACE_ENABLED), false);
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getBoolean(RuntimeConstants.CONFIG.ALWAYS_COMPILE), false);
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getBoolean(RuntimeConstants.CONFIG.GENERATE_OPS), false);
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getString(RuntimeConstants.CONFIG.OPSPATH), ".");
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getString(RuntimeConstants.CONFIG.HOSTNAME), "xmp:localhost;sip:localhost");
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(RuntimeConstants.CONFIG.ENGINE_VXML_POOL_SIZE), 0);
   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(RuntimeConstants.CONFIG.ENGINE_CCXML_POOL_SIZE), 0);

   }

    public IConfiguration getConfig() {
    	return configuration;
    }

    private IConfiguration getConfiguration(String... files) throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(files);
        return configurationManager.getConfiguration();
    }
}


