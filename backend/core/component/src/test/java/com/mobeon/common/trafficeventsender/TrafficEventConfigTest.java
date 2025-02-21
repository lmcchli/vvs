package com.mobeon.common.trafficeventsender;


import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;

public class TrafficEventConfigTest {

    private RadiusConfigurationAttribute.AttributeType RADIUS = RadiusConfigurationAttribute.AttributeType.RADIUS;
    private RadiusConfigurationAttribute.DataType STRING = RadiusConfigurationAttribute.DataType.STRING;
    private IConfiguration configuration;

	public TrafficEventConfigTest() throws Exception {
		setUp();
	}

    @BeforeClass
    static public void startup() throws Exception {
        BasicConfigurator.configure();
    }


    @AfterClass
    static public void tearDown() {
    }

    /**
     * Test the get methods on the RadiusConfiguration class
     *
     * @throws Exception if test case fails.
     */
    @Ignore("This test needs reviewing - it fails on Windows")
    @Test
    public void testConfiguration() throws Exception {
        TrafficEventSenderConfiguration c = TrafficEventSenderConfiguration.getInstance();
        c.setConfiguration(new TrafficEventConfigTest().getConfig());
        c.update();

        verifyRadiusConfigurationAttributeMap(c.getRadiusConfiguration());
    }

    private void verifyRadiusConfigurationAttributeMap(RadiusConfiguration c) throws Exception {
        Map<String, RadiusConfigurationAttribute> map = c.getRadiusAttributeMap();

        RadiusConfigurationAttribute attr = map.get("sessionid");
        verifyRadiusConfigurationAttribute(attr, 50, RADIUS, STRING);

        attr = map.get("username");
        verifyRadiusConfigurationAttribute(attr, 1, RADIUS, STRING);

        attr = map.get("ownername");
        verifyRadiusConfigurationAttribute(attr, 16, RadiusConfigurationAttribute.AttributeType.VENDOR, STRING);

        attr = map.get("sssporttype");
        verifyRadiusConfigurationAttribute(attr, 11, RadiusConfigurationAttribute.AttributeType.VENDOR, RadiusConfigurationAttribute.DataType.INT);

        attr = map.get("accountcurrency");
        verifyRadiusConfigurationAttribute(attr, 30, RadiusConfigurationAttribute.AttributeType.VENDOR, RadiusConfigurationAttribute.DataType.STRING);
        attr = map.get("accountmoney");
        verifyRadiusConfigurationAttribute(attr, 31, RadiusConfigurationAttribute.AttributeType.VENDOR, RadiusConfigurationAttribute.DataType.STRING);
        attr = map.get("accounttype");
        verifyRadiusConfigurationAttribute(attr, 32, RadiusConfigurationAttribute.AttributeType.VENDOR, RadiusConfigurationAttribute.DataType.INT);
        attr = map.get("accountreason");
        verifyRadiusConfigurationAttribute(attr, 33, RadiusConfigurationAttribute.AttributeType.VENDOR, RadiusConfigurationAttribute.DataType.STRING);

        Assert.assertNull(map.get("notfound"));
    }

    private void verifyRadiusConfigurationAttribute(RadiusConfigurationAttribute attr, int number,
                                                    RadiusConfigurationAttribute.AttributeType attributeType,
                                                    RadiusConfigurationAttribute.DataType dataType) throws Exception {
        Assert.assertSame(number, attr.getNumber());
        Assert.assertSame(attributeType, attr.getAttributeType());
        Assert.assertSame(dataType, attr.getDataType());
    }

    protected void setUp() throws Exception {
    	File dir = new File(".");
    	String backendConfig = dir.getAbsolutePath() +  File.separator + "src" + File.separator + "main" + File.separator + "config" + File.separator + "backend.conf";
    	
    	String trafficEventsConfig = dir.getAbsolutePath() +  File.separator + "src" + File.separator + "main" + File.separator + "config" + File.separator + "trafficevents.conf";
    	
        configuration = getConfiguration(backendConfig, trafficEventsConfig);
    }

    public IConfiguration getConfig() {
    	return configuration;
    }

    private IConfiguration getConfiguration(String... files) throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(files);
        return configurationManager.getConfiguration();
    }

    public static void main(String args[]) throws Exception{
    	TrafficEventConfigTest t = new TrafficEventConfigTest();
    	t.testConfiguration();
    }
}
