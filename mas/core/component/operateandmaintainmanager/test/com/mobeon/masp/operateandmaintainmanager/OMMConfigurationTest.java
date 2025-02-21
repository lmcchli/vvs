package com.mobeon.masp.operateandmaintainmanager;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;

public class OMMConfigurationTest {

    private IConfiguration configuration;

	public OMMConfigurationTest() throws Exception {
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
	   new OMMConfiguration(configuration);

	   assertEquals(OMMConfiguration.getHostName(), "localhost");
	   assertEquals(OMMConfiguration.getPort(), new Integer(8081));

	   assertEquals(OMMConfiguration.getProvidedServiceInitialThreshold("sip"), new Integer(500));
	   assertEquals(OMMConfiguration.getserviceEnablerHighWaterMark("sip"), new Integer(499));
	   assertEquals(OMMConfiguration.getserviceEnablerLowWaterMark("sip"), new Integer(240));

	   assertEquals(OMMConfiguration.getProvidedServiceInitialThreshold("xmp"), new Integer(200));
	   assertEquals(OMMConfiguration.getserviceEnablerHighWaterMark("xmp"), new Integer(0));
	   assertEquals(OMMConfiguration.getserviceEnablerLowWaterMark("xmp"), new Integer(0));

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
