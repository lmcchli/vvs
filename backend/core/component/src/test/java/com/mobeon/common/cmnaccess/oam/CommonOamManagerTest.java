package com.mobeon.common.cmnaccess.oam;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mrd.oamplugin.DispatcherConfigMgr;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;

//@TODO: fix this test!
//@Ignore
public class CommonOamManagerTest {

	static private final Collection<String> configFilenames = new LinkedList<String>();
	static private String configFilename = "cfg/backend.conf";
	static private String mcrFilename = "cfg/componentservices.cfg";

	public CommonOamManagerTest() {
	}

	static public void initOam() throws ConfigurationException {

	    File file = new File(configFilename);
	    if (file.exists() == false) {
	        String curDir = System.getProperty("user.dir");
	        if (!curDir.endsWith("backend")) {
	            configFilename = curDir + "/../ipms_sys2/backend/cfg/backend.conf";
	            mcrFilename = curDir + "/../ipms_sys2/backend/cfg/componentservices.cfg";

	            // TODO: please fix all those file locations. Here is the LOTC one
	            file = new File(configFilename);
	            if (file.exists() == false) {
	                configFilename= "/opt/moip/common/backend.conf";
	                mcrFilename = "/opt/moip/common/componentservices.cfg";
	            }
	        }
	    }

		System.setProperty("componentservicesconfig", mcrFilename);
		CommonOamManager.getInstance().setMnrSsmgConfigFiles();
	    configFilenames.add(configFilename);
	    IConfiguration configuration = new ConfigurationImpl(null,configFilenames,false);
	    CommonOamManager.getInstance().setConfiguration(configuration);
	}

	@BeforeClass
	static public void setUp() throws Exception {
		//configFilenames.add(xsdFilename);
	    initOam();
	}

	//@Test
	public void testFaultManager() {
		assertTrue(CommonOamManager.getInstance().getMrdOam().getFaultManager() != null);

		assertTrue(CommonOamManager.getInstance().getMrdOam().getFaultManager() instanceof MoipFaultManager);
	}

	@Test
	public void testMcrParameterSetup() {

		ConfigManager localConfig = CommonOamManager.getInstance().getLocalConfig();
		assertTrue(localConfig.getParameter(MoipMessageEntities.NtfMrdServiceHost)
				.equalsIgnoreCase("localhost"));
		assertTrue( localConfig.getLongValue(MoipMessageEntities.NtfMrdServicePort) == 10500);

		//test
		//ConfigManager mrdConfig = CommonOamManager.getInstance().getMrdOam().getConfigManager();
		//assertTrue(mrdConfig.getParameter(DispatcherConfigMgr.SchedulerID).equalsIgnoreCase("100"));


	}

	@Test
	public void testGetMfsConfiguration() {
	    ConfigManager config = CommonOamManager.getInstance().getMfsOam().getConfigManager();
	    assertNotNull(config);
	    assertEquals("/opt/mfs", config.getParameter(MfsConfiguration.MfsRootPath));
	}

	@Test
	public void testGetMrdConfiguration() {
	    ConfigManager config = CommonOamManager.getInstance().getMrdOam().getConfigManager();
	    assertNotNull(config);
	    assertEquals("60", config.getParameter(DispatcherConfigMgr.HandoverRetryTimer));
	    assertEquals("3", config.getParameter(DispatcherConfigMgr.MaxHandoverTries));
	    assertEquals("60", config.getParameter(DispatcherConfigMgr.DeliveryRetryTimer));
	    assertEquals("60", config.getParameter(DispatcherConfigMgr.ExpiryRetryTimer));
	    assertEquals("3", config.getParameter(DispatcherConfigMgr.MaxExpiryTries));
	    assertEquals("10", config.getParameter(DispatcherConfigMgr.MaximumPoolSize));
	    assertEquals("4", config.getParameter(DispatcherConfigMgr.CorePoolSize));
	    assertEquals("5000", config.getParameter(DispatcherConfigMgr.KeepAliveTime));
	    assertEquals("20000", config.getParameter(DispatcherConfigMgr.RemoteDispatcherTimeout));
	    assertEquals("10000", config.getParameter(DispatcherConfigMgr.RemoteRetryInterval));
	    assertEquals("3", config.getParameter(DispatcherConfigMgr.RemoteAccessRetries));
	}

	public void testGetShedulerConfiguration() {
		fail("Not yet implemented");
	}



}
