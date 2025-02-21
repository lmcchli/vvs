package com.mobeon.masp.mediacontentmanager;

	import static org.junit.Assert.assertEquals;

	import org.apache.log4j.BasicConfigurator;
	import org.junit.AfterClass;
	import org.junit.BeforeClass;
	import org.junit.Test;

	import com.mobeon.common.cmnaccess.oam.CommonOamManager;
	import com.mobeon.common.configuration.ConfigurationManagerImpl;
	import com.mobeon.common.configuration.IConfiguration;
	import com.mobeon.common.configuration.IConfigurationManager;

	public class MediaContentManagerConfigurationTest {

		private IConfiguration configuration;

		public MediaContentManagerConfigurationTest() throws Exception {
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
	   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getString(MediaContentManager.RESOURCE_PATH_PARAM), "applications/mediacontentpackages");
	   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getString(MediaContentManager.CACHE_POLICY), "lfu");
	   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(MediaContentManager.CACHE_ELEMENT_TIMEOUT), 3600000);
	   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(MediaContentManager.CACHE_MAX_SIZE), 100);
	   	assertEquals(configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getBoolean(MediaContentManager.CACHE_MEMORY_SENSITIVE), false);


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


