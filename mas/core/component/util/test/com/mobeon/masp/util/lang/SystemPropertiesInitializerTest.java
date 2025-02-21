package com.mobeon.masp.util.lang;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.Properties;

import com.mobeon.common.logging.ILoggerFactory;

/**
 * SystemPropertiesInitializer Tester.
 *
 * @author qhast
 */
public class SystemPropertiesInitializerTest extends TestCase
{
    private SystemPropertiesInitializer initializer;

    public SystemPropertiesInitializerTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        ILoggerFactory.configureAndWatch("test/log4jconf.xml");
        initializer = new SystemPropertiesInitializer();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetSystemProperties() throws Exception
    {
        assertEquals("System Properties should be null!",null,initializer.getSystemProperties());
    }

    public void testSetSystemProperties() throws Exception
    {
        Properties sp = new Properties();
        initializer.setSystemProperties(sp);
        assertEquals("System Properties should be "+sp,sp,initializer.getSystemProperties());
    }

    public void testInit() throws Exception
    {
        final String propertyName = "com.mobeon.masp.util.lang.SystemPropertiesInitializerTest.value";
        final String propertyValue = "Potatis";

        int numberOfPropsBefore = System.getProperties().size();
        Properties sp = new Properties();
        sp.put(propertyName,propertyValue);
        initializer.setSystemProperties(sp);
        initializer.init();
        assertEquals("System Property "+propertyName+" is wrong!",propertyValue,System.getProperty(propertyName));
        assertEquals("Number of System Properties is wrong after init!",numberOfPropsBefore+sp.size(),System.getProperties().size());

    }

    /*
    public void testSpringInit() throws Exception {

        ClassPathResource classPathResource = new ClassPathResource("TestComponentConfig.xml", getClass());
        XmlBeanFactory bf = new XmlBeanFactory(classPathResource);
        Object demoBean1 = bf.getBean("DemoBean1");
        Object demoBean2 = bf.getBean("DemoBean2");
    }
    */

    public static Test suite()
    {
        return new TestSuite(SystemPropertiesInitializerTest.class);
    }
}
