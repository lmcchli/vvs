/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.profilemanager;

import junit.framework.*;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Hashtable;
import java.util.List;
import java.io.File;
import java.io.StringWriter;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ByteArrayResource;

/**
 * Documentation
 *
 * @author mande
 */
public class SpringConfigurationTest extends TestCase {
    private static final String COMPONENT_CONFIG = "../etc/ComponentConfig.xml";
    {
        ILoggerFactory.configureAndWatch("../log4jconf.xml");
    }

    public void testComponentConfig() throws Exception {
        Resource byteArrayResource = getResource();
        BeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader((BeanDefinitionRegistry)factory);
        // Add a "dummy" ComponentConfig containing only the dependent beans
        FileSystemResource stubComponentConfig = new FileSystemResource("test/com/mobeon/masp/profilemanager/ComponentConfig.xml");
        xmlBeanDefinitionReader.loadBeanDefinitions(new Resource[] { byteArrayResource, stubComponentConfig });
        BaseContext baseContext = (BaseContext)factory.getBean("ProfileManagerContext");
        Hashtable<String, String> dirContextEnv = baseContext.getDirContextEnv();
        assertEquals("com.sun.jndi.ldap.LdapCtxFactory", dirContextEnv.get("java.naming.factory.initial"));
        assertEquals("follow", dirContextEnv.get("java.naming.referral"));
    }

    /**
     * Hack to get a stripped ComponentConfig.xml so ExternalComponentRegisterContext bean in
     * ComponentConfig.xml can be tested
     * Todo: Maybe split ComponentConfig so parts can be tested easier?
     * @return A resource containing only the ExternalComponentRegisterContext bean
     * @throws Exception
     */
    private Resource getResource() throws Exception {
        File file = new File(COMPONENT_CONFIG);
        SAXReader reader = new SAXReader();
        Document xmlDoc = reader.read(file);
        Element rootElement = xmlDoc.getRootElement();
        //noinspection unchecked
        List<Element> list = rootElement.elements();
        for (Element element : list) {
            if (!element.attribute("id").getValue().equals("ProfileManagerContext")) {
                rootElement.remove(element);
            }
        }
        StringWriter stringWriter = new StringWriter();
        xmlDoc.write(stringWriter);
        return new ByteArrayResource(stringWriter.toString().getBytes());
    }

    public static Test suite() {
        return new TestSuite(SpringConfigurationTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}