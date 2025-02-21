/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager;

import com.mobeon.common.configuration.*;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.provisionmanager.cai.CAIException;
import com.mobeon.masp.provisionmanager.cai.CreateCommand;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.ArrayList;

/**
 * Testcase for the Provisioning class
 *
 * @author ermmaha
 */
public class ProvisioningTest extends MockObjectTestCase {
    private static final String cfgFile = "../provisionmanager/test/com/mobeon/masp/provisionmanager/provisionmanager.xml";
    private static final String LOG4J_CONFIGURATION = "../provisionmanager/log4jconf.xml";

    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    protected EventDispatcherStub eventDispatcherStub;
    protected IConfiguration configuration;
    protected Mock jmockServiceLocator;
    protected Mock jmockServiceInstance;
    protected String adminUid = "cai";
    protected String adminUid2 = "cai2";
    protected String adminPwd = "root";

    public ProvisioningTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        setupServiceLocator();
        configuration = getConfiguration(cfgFile);
        eventDispatcherStub = new EventDispatcherStub();
    }

    /**
     * Verifies the create method
     *
     * @throws Exception if test case fails.
     */
    public void testCreate() throws Exception {
        Provisioning provisioning = createProvisioning();

        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "171011");
        s.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        s.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");

        provisioning.create(s, adminUid, adminPwd);
        assertTrue("Telephonenumber attribute should not be removed", s.getAttributes().containsKey("TELEPHONENUMBER"));

        Subscription s2 = new Subscription();
        s2.addAttribute("TELEPHONENUMBER", "181011");
        s2.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");

        provisioning.create(s2, adminUid, adminPwd);
        assertTrue("Telephonenumber attribute should not be removed", s2.getAttributes().containsKey("TELEPHONENUMBER"));
    }

     /**
     * Verifies the createCommand method
     *
     * @throws Exception if test case fails.
     */
    public void testCreateCommand() throws Exception {
        Provisioning provisioning = createProvisioning();
        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "181011");
        s.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");
        ProvisioningConfiguration config = ProvisioningConfiguration.getInstance();


        // Test that default mailhost found in config is handled
        String mailhost = "configuredmailhost";
        config.setDefaultMailhost(mailhost);
        CreateCommand createCommand = provisioning.makeCreateCommand(s);
        String command = createCommand.toCommandString();
        int i = command.indexOf("MAILHOST");
        assertTrue("MAILHOST parameter should exist", i != -1);
        assertTrue("Should use localhost", command.indexOf(mailhost, i) != -1);

        // Test that supplied mailhost is handled when config is empty
        config.setDefaultMailhost("");
        mailhost = "suppliedmailhost";
        s.addAttribute("MAILHOST", mailhost);
        createCommand = provisioning.makeCreateCommand(s);
        command = createCommand.toCommandString();
        i = command.indexOf("MAILHOST");
        assertTrue("MAILHOST parameter should exist", i != -1);
        assertTrue("Should use specified mailhost", command.indexOf(mailhost, i) != -1);

        // Test that supplied mailhost is used even if configured host is available
        mailhost = "suppliedmailhost";
        config.setDefaultMailhost("configuredmailhost");
        s.addAttribute("MAILHOST", mailhost);
        createCommand = provisioning.makeCreateCommand(s);
        command = createCommand.toCommandString();
        i = command.indexOf("MAILHOST");
        assertTrue("MAILHOST parameter should exist", i != -1);
        assertTrue("Should use specified mailhost", command.indexOf(mailhost, i) != -1);
    }

    /**
    * Verifies the createCommand method when the method fails for some reason
    *
    * @throws Exception if test case fails.
    */
   public void testFailedCreateCommand() throws Exception {
       Provisioning provisioning = createProvisioning();
       Subscription s = new Subscription();
       s.addAttribute("TELEPHONENUMBER", "181012");
       s.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");
       ProvisioningConfiguration config = ProvisioningConfiguration.getInstance();

       // Test that no default mailhost is found in config is handled
       config.setDefaultMailhost(null);
       CreateCommand createCommand = new CreateCommand(null);
        try {
           createCommand = provisioning.makeCreateCommand(s);
           fail("Expected ProvisioningException");
       } catch (ProvisioningException e) {
           assertTrue(true); // For statistical purposes
           assertEquals("Expected \"No default mailhost configured,can't create the subscriber.\"",
                   "No default mailhost configured,can't create the subscriber.", e.getMessage());
           // because the makeCreateCommand failed we have to restore the Telno
           s.addAttribute("TELEPHONENUMBER", "181012");
       }
       String command = createCommand.toCommandString();
       int i = command.indexOf("MAILHOST");
       assertTrue("MAILHOST parameter should not exist", i == -1);

       // Test that default mailhost is an empty string in config is handled
       config.setDefaultMailhost("");
       try {
          createCommand = provisioning.makeCreateCommand(s);
          fail("Expected ProvisioningException");
       } catch (ProvisioningException e) {
           assertTrue(true); // For statistical purposes
           assertEquals("Expected \"No default mailhost configured,can't create the subscriber.\"",
                  "No default mailhost configured,can't create the subscriber.", e.getMessage());
           // because the makeCreateCommand failed we have to restore the Telno
           s.addAttribute("TELEPHONENUMBER", "181012");
       }
       command = createCommand.toCommandString();
       i = command.indexOf("MAILHOST");
       assertTrue("MAILHOST parameter should not exist", i == -1);
   }

    /**
     * Verifies the create method when the the create command fails for some reason
     *
     * @throws Exception if test case fails.
     */
    public void testFailedCreate() throws Exception {
        Provisioning provisioning = createProvisioning();

        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "191011");
        s.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        s.addAttribute("COSDN", "cos=99,ou=C22,o=mobeon.com");

        try {
            provisioning.create(s, adminUid, adminPwd);
            fail("Expected ProvisioningException");
        } catch (ProvisioningException e) {
            Throwable cause = e.getCause();
            assertEquals(5014, ((CAIException) cause).getErrorCode());
        }
        assertTrue("Telephonenumber attribute should not be removed", s.getAttributes().containsKey("TELEPHONENUMBER"));
    }

    /**
     * Verifies the delete method
     *
     * @throws Exception if test case fails.
     */
    public void testDelete() throws Exception {
        Provisioning provisioning = createProvisioning();

        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "171011");
        s.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        s.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");

        provisioning.delete(s, adminUid, adminPwd);
        assertTrue("Telephonenumber attribute should not be removed", s.getAttributes().containsKey("TELEPHONENUMBER"));
        Subscription s2 = new Subscription();
        s2.addAttribute("TELEPHONENUMBER", "181011");

        provisioning.delete(s2, adminUid2, adminPwd);
        assertTrue("Telephonenumber attribute should not be removed", s2.getAttributes().containsKey("TELEPHONENUMBER"));
    }

    /**
     * Verifies the delete method when the the delete command fails for some reason
     *
     * @throws Exception if test case fails.
     */
    public void testFailedDelete() throws Exception {
        Provisioning provisioning = createProvisioning();

        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "171012");
        s.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        s.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");

        try {
            provisioning.delete(s, adminUid, adminPwd);
            fail("Expected ProvisioningException");
        } catch (ProvisioningException e) {
            Throwable cause = e.getCause();
            assertEquals(5008, ((CAIException) cause).getErrorCode());
        }
        assertTrue("Telephonenumber attribute should not be removed", s.getAttributes().containsKey("TELEPHONENUMBER"));
    }

    public void testChangedPassword() throws Exception {
        Provisioning provisioning = createProvisioning();

        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "161916");
        s.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        s.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");

        // Test for create
        try {
            provisioning.create(s, adminUid, "x" + adminPwd);
            fail("Expected ProvisioningException");
        } catch (ProvisioningException e) {
            assertTrue(true); // For statistical purposes
            CAIException cause = (CAIException)e.getCause();
            assertEquals("Expected error code indicating invalid credentials", 5002, cause.getErrorCode());
        }

        provisioning.create(s, adminUid, adminPwd);

        provisioning = createProvisioning();
        // Test for delete\
        // Telephonenumber is removed by Provision...
        s.addAttribute("TELEPHONENUMBER", "161916");
        try {
            provisioning.delete(s, adminUid, "x" + adminPwd);
            fail("Expected ProvisioningException");
        } catch (ProvisioningException e) {
            assertTrue(true); // For statistical purposes
            CAIException cause = (CAIException)e.getCause();
            assertEquals("Expected error code indicating invalid credentials", 5002, cause.getErrorCode());
        }
        provisioning.delete(s, adminUid, adminPwd);
    }

    /**
     * Verifies the createAsync method
     *
     * @throws Exception if test case fails.
     */
    public void testCreateAsync() throws Exception {
        Provisioning provisioning = createProvisioning();

        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "271011");
        s.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        s.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");

        int transactionId = provisioning.createAsync(s, adminUid, adminPwd);
        while (!provisioning.isFinished(transactionId)) {
            System.out.println("provisioning " + transactionId + " not finished sleeping...");
            sleep(1);
        }
        provisioning.create(transactionId);
    }

    /**
     * Verifies the create method when the the create command fails for some reason
     *
     * @throws Exception if test case fails.
     */
    public void testFailedCreateAsync() throws Exception {
        Provisioning provisioning = createProvisioning();

        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "291011");
        s.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        s.addAttribute("COSDN", "cos=99,ou=C22,o=mobeon.com"); // test invalid cos

        int transactionId = provisioning.createAsync(s, adminUid, adminPwd);
        try {
            // will block until finished
            provisioning.create(transactionId);
            fail("Expected ProvisioningException");
        } catch (ProvisioningException e) {
            Throwable cause = e.getCause();
            assertEquals(5014, ((CAIException) cause).getErrorCode());
        }

        // test same transactionid again, should get exception
        try {
            provisioning.create(transactionId);
            fail("Expected ProvisioningException");
        } catch (ProvisioningException e) {
        }

        // should return false if transactionid is not in use
        assertFalse(provisioning.isFinished(transactionId));
    }

    /**
     * Verifies the deleteAsync method
     *
     * @throws Exception if test case fails.
     */
    public void testDeleteAsync() throws Exception {
        Provisioning provisioning = createProvisioning();

        Subscription s = new Subscription();
        s.addAttribute("TELEPHONENUMBER", "271011");
        s.addAttribute("MAILHOST", "ockelbo.lab.mobeon.com");
        s.addAttribute("COSDN", "cos=48,ou=C22,o=mobeon.com");

        int transactionId = provisioning.deleteAsync(s, adminUid, adminPwd);
        while (!provisioning.isFinished(transactionId)) {
            System.out.println("provisioning " + transactionId + " not finished sleeping...");
            sleep(1);
        }
        provisioning.delete(transactionId);
    }

    /**
     * Verifies that correct exceptions are thrown (and handled, and logged) if the init fails
     *
     * @throws Exception if test case fails.
     */
    public void testInitFailure() throws Exception {
        Mock jmockConfiguration = mock(IConfiguration.class);
        // test configuration read failure
        jmockConfiguration.expects(once()).method("getGroup").will(throwException(new UnknownGroupException("Error", null)));

        Provisioning provisioning = new Provisioning();
        provisioning.setConfiguration((IConfiguration) jmockConfiguration.proxy());
        provisioning.setServiceLocator((ILocateService) jmockServiceLocator.proxy());
        provisioning.init();
    }

    /**
     * Verifies that correct exceptions are thrown (and handled, and logged) if the LocateService fails
     *
     * @throws Exception if test case fails.
     */
    public void testLocateServiceFailure() throws Exception {
        Provisioning provisioning = createProvisioning();

        jmockServiceLocator.expects(once()).method("locateService").with(eq(IServiceName.PROVISIONING)).
                will(throwException(new NoServiceFoundException("No service found")));
        try {
            Subscription s = new Subscription();
            s.addAttribute("TELEPHONENUMBER", new String[]{"171012"}); // to get 100% coverage...
            provisioning.delete(s, adminUid, adminPwd);
            fail("Expected ProvisioningException");
        } catch (ProvisioningException e) {
        }

        jmockServiceInstance.expects(once()).method("getProperty");
        try {
            Subscription s = new Subscription();
            s.addAttribute("TELEPHONENUMBER", "171012");
            provisioning.delete(s, adminUid, adminPwd);
            fail("Expected ProvisioningException");
        } catch (ProvisioningException e) {
        }
    }

    /**
     * Test the Configuration Changed Event
     *
     * @throws Exception if test case fails.
     */
    public void testConfigurationChangedEvent() throws Exception {
        createProvisioning();

        // then change and reload the configuration
        IConfiguration changedConfiguration = getConfiguration(cfgFile);
        eventDispatcherStub.fireGlobalEvent(new ConfigurationChanged(changedConfiguration));
    }

    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            System.out.println("Exception in sleep " + e);
        }
    }

    private Provisioning createProvisioning() {
        Provisioning provisioning = new Provisioning();
        provisioning.setConfiguration(configuration);
        provisioning.setServiceLocator((ILocateService) jmockServiceLocator.proxy());
        provisioning.setEventDispatcher(eventDispatcherStub);
        provisioning.init();
        return provisioning;
    }

    private IConfiguration getConfiguration(String configFile) throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(configFile);
        return configurationManager.getConfiguration();
    }

    private void setupServiceLocator() {
        jmockServiceInstance = mock(IServiceInstance.class);
        jmockServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.HOSTNAME)).will(returnValue("ockelbo.lab.mobeon.com"));
        jmockServiceInstance.stubs().method("getProperty").with(eq(IServiceInstance.PORT)).will(returnValue("2400"));

        jmockServiceLocator = mock(ILocateService.class);
        jmockServiceLocator.stubs().method("locateService").with(eq(IServiceName.PROVISIONING)).will(returnValue(jmockServiceInstance.proxy()));
//        jmockServiceLocator.stubs().method("locateService").with(isA(String.class), eq("provision")).will(returnValue(jmockServiceInstance.proxy()));
        jmockServiceLocator.stubs().method("reportServiceError");
    }
}

class EventDispatcherStub implements IEventDispatcher {
    private ArrayList<IEventReceiver> eventReceivers = new ArrayList<IEventReceiver>();

    public void addEventReceiver(IEventReceiver rec) {
        eventReceivers.add(rec);
    }

    public void removeEventReceiver(IEventReceiver rec) {
        eventReceivers.remove(rec);
    }

    public void removeAllEventReceivers() {
    }

    public ArrayList<IEventReceiver> getEventReceivers() {
        return eventReceivers;
    }

    public int getNumReceivers() {
        return eventReceivers.size();
    }

    public void fireEvent(Event e) {
        for (IEventReceiver eventReceiver : eventReceivers) {
            eventReceiver.doEvent(e);
        }
    }

    public void fireGlobalEvent(Event e) {
        for (IEventReceiver eventReceiver : eventReceivers) {
            eventReceiver.doGlobalEvent(e);
        }
    }
}
