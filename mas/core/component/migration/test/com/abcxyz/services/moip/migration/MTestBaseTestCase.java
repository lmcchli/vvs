package com.abcxyz.services.moip.migration;

import com.abcxyz.services.moip.migration.configuration.moip.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.eventnotifier.EventDispatcherStub;
import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.message_sender.IInternetMailSender;
import com.mobeon.common.message_sender.jakarta.JakartaCommonsSmtpInternetMailSender;
import org.jmock.MockObjectTestCase;

import javax.naming.Context;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Date: 2007-aug-07
 *
 * @author emahagl
 */
public abstract class MTestBaseTestCase extends MockObjectTestCase {

    static final String backend_xml = "migration/test/cfg/migration.xml";
    
    static final String LOG4J_CONFIGURATION = "migration/test/cfg/log4j2conf.xml";

	private static final String COMPONENT_SERVICES = "migration/test/cfg/componentservices.cfg";
    
  /*  static final String backend_xml = "/opt/moip/config/backend/backend.conf";
    static final String trafficevents_xml = "/opt/moip/config//backend/trafficevents.conf";
    static final String LOG4J_CONFIGURATION = "log4jconf.xml";*/

    static {
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
        System.setProperty("componentservicesconfig",COMPONENT_SERVICES); 
    }

    protected IConfiguration configuration;
    protected ILocateService serviceLocator;
   protected IInternetMailSender internetMailSender;

    public MTestBaseTestCase(String string) {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();
       configuration = getConfiguration(backend_xml/*, trafficevents_xml*/);
        serviceLocator = getServiceLocator();
        internetMailSender = getInternetMailSender();
    }

    private IInternetMailSender getInternetMailSender() {
        JakartaCommonsSmtpInternetMailSender jakartaCommonsSmtpInternetMailSender =
                new JakartaCommonsSmtpInternetMailSender();
        jakartaCommonsSmtpInternetMailSender.setConfiguration(configuration);
        jakartaCommonsSmtpInternetMailSender.setEventDispatcher(new EventDispatcherStub());
        jakartaCommonsSmtpInternetMailSender.setServiceLocator(serviceLocator);

        return jakartaCommonsSmtpInternetMailSender;
    }

    private IConfiguration getConfiguration(String... files) throws Exception {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(files);
        return configurationManager.getConfiguration();
    }

    /**
     * Setup the ILocateService needed for MCR information.
     *
     * @return an ILocateService object
     * @throws Exception
     */
    private ILocateService getServiceLocator() throws Exception {
  //  	System.setProperty("componentservicesconfig", "test/com/mobeon/common/externalcomponentregister/componentservices.config");
        return  ExternalComponentRegister.getInstance();
    }

    protected Hashtable<String, String> getDirContextEnv() {
        Hashtable<String, String> dirContextEnv = new Hashtable<String, String>();
        dirContextEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        dirContextEnv.put(Context.REFERRAL, "follow");
        return dirContextEnv;
    }



    protected void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            System.out.println("Exception in sleep " + e);
        }
    }
}
