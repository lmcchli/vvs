/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.chargingaccountmanager;

import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILoggerFactory;
import org.jmock.MockObjectTestCase;

import java.util.Iterator;
import java.util.List;

/**
 * Testcase for the ChargingAccountManagerConfiguration class
 *
 * @author ermmaha
 */
public class ChargingAccountManagerConfigurationTest extends MockObjectTestCase {
    private static final String cfgFile = "../chargingaccountmanager/test/com/mobeon/masp/chargingaccountmanager/chargingaccountmanager.xml";
    private static final String LOG4J_CONFIGURATION = "../chargingaccountmanager/log4jconf.xml";

    static {
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    protected IConfiguration configuration;

    public ChargingAccountManagerConfigurationTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        configuration = getConfiguration();
    }

    /**
     * Verifies all get methods in ChargingAccountManagerConfiguration
     *
     * @throws Exception if test case fails.
     */
    public void testConfiguration() throws Exception {
        ChargingAccountManagerConfiguration c = ChargingAccountManagerConfiguration.getInstance();
        c.setConfiguration(configuration);
        c.update();

        verifyAirNodeList(c);
        verifyElementList(c);
        verifyElementGroupList(c);
    }


    private void verifyAirNodeList(ChargingAccountManagerConfiguration c) throws Exception {
        List<AirNode> list = c.getAirNodeList();
        AirNode airNode = list.get(0);
        assertEquals("http://localhost:4444/Air", airNode.asURL().toString());
        assertEquals("mas", airNode.getUid());
        assertEquals("mas", airNode.getPwd());

        airNode = list.get(1);
        assertEquals("http://brage.mobeon.com:1337/Air", airNode.asURL().toString());
        assertNull(airNode.getUid());
        assertNull(airNode.getPwd());
    }

    private void verifyElementList(ChargingAccountManagerConfiguration c) throws Exception {
        verifyElement(c.getElement("accountActivationFlag"), ParameterType.Boolean);
        verifyElement(c.getElement("accountGroupID"), ParameterType.Integer);
        verifyElement(c.getElement("accumulatorEndDate"), ParameterType.Date);

        assertNull(c.getElement("notfound"));
    }

    private void verifyElement(Element element, ParameterType type) {
        assertEquals(type, element.getType());
    }

    private void verifyElementGroupList(ChargingAccountManagerConfiguration c) throws Exception {
        ElementGroup elementGroup = c.getElementGroup("firstIVRCallSetFlag");
        assertEquals("messageCapabilityFlag", elementGroup.getParent());
        assertEquals(ElementGroup.StructType.Struct, elementGroup.getStructType());

        Element[] elements = elementGroup.getMemberElements();
        assertEquals(3, elements.length);

        assertNull(c.getElementGroup("nogroup"));
    }

    private IConfiguration getConfiguration() throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(cfgFile);
        return configurationManager.getConfiguration();
    }


    /**
     * SPECIAL Remove or move
     *
     * @throws Exception
     */
    public void XtestAsaConfiguration() throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        String cfgFile = "../chargingaccountmanager/test/com/mobeon/masp/chargingaccountmanager/asa.xml";
        configurationManager.setConfigFile(cfgFile);
        IConfiguration configuration = configurationManager.getConfiguration();

        String param =
                systemGetConfigurationGroupParameter(
                        configuration, "asa.basicaccountservice.serviceclasses.serviceclass", "name", "id", "1");

        System.out.println("param= " + param);

        param = systemGetConfigurationGroupParameter(
                configuration, "asa.basicaccountservice.serviceclasses.serviceclass", "scratchcardrecharge", "id", "2");

        System.out.println("param= " + param);

        systemGetConfigurationGroupParameter(
                configuration, "asa.basicaccountservice.serviceclasses.serviceclass", "nameX", "id", "1");

    }

    private String systemGetConfigurationGroupParameter(IConfiguration configuration, String group,
                                                        String parameterName, String groupIdName, String groupIdValue) throws Exception {
        try {
            List<IGroup> groupList = configuration.getGroups(group);
            Iterator<IGroup> it = groupList.iterator();
            while (it.hasNext()) {
                IGroup iGroup = it.next();

                try {
                    String value = iGroup.getString(groupIdName);
                    if (value.equalsIgnoreCase(groupIdValue)) {
                        try {
                            return iGroup.getString(parameterName);
                        } catch (ConfigurationException e) {
                            throw new Exception(
                                    "systemGetConfigurationGroupParameter:parameterName="
                                            + parameterName, e);
                        }
                    }
                } catch (ConfigurationException e) {
                    throw new Exception(
                            "systemGetConfigurationGroupParameter:groupIdName=" + groupIdName, e);
                }
            }
            throw new Exception(
                    "systemGetConfigurationGroupParameter:parameterName="
                            + parameterName + "No groupIdValue " + groupIdValue + " found");
        } catch (ConfigurationException e) {
            throw new Exception("systemGetConfigurationGroupParameter:group=" + group, e);
        }
    }
}

