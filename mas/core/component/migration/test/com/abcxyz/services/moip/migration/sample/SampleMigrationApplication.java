/**
 * 
 */
package com.abcxyz.services.moip.migration.sample;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;

import com.abcxyz.services.moip.migration.configuration.moip.ConfigurationManagerImpl;
import com.abcxyz.services.moip.migration.profilemanager.moip.BaseContext;
import com.abcxyz.services.moip.migration.profilemanager.moip.IProfile;
import com.abcxyz.services.moip.migration.profilemanager.moip.IProfileManager;
import com.abcxyz.services.moip.migration.profilemanager.moip.ProfileManagerImpl;
import com.abcxyz.services.moip.migration.profilemanager.moip.search.ProfileStringCriteria;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.eventnotifier.EventDispatcherStub;
import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.message_sender.IInternetMailSender;
import com.mobeon.common.message_sender.jakarta.JakartaCommonsSmtpInternetMailSender;
import com.mobeon.masp.mailbox.IFolder;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.IMailboxAccountManager;
import com.mobeon.masp.mailbox.IMessageContent;
import com.mobeon.masp.mailbox.IStoredMessage;
import com.mobeon.masp.mailbox.IStoredMessageList;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mailbox.imap.ImapProperties;
import com.mobeon.masp.mailbox.javamail.JavamailContextFactory;
import com.mobeon.masp.mailbox.javamail.JavamailMailboxAccountManager;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.UnknownAttributeException;
import com.mobeon.masp.profilemanager.UserProvisioningException;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;

/**
 * @author lmcraby
 * 
 */
public class SampleMigrationApplication {
	
	static final String backend_xml = "migration/test/cfg/migration.xml";

	private static final String COMPONENT_SERVICES = "migration/test/cfg/componentservices.cfg";
    
    

	private IProfileManager profileManager = null;

	protected IConfiguration configuration;
	protected ILocateService serviceLocator;
	protected IInternetMailSender internetMailSender;

	static {   
        System.setProperty("componentservicesconfig",COMPONENT_SERVICES); 
    }
	
	protected void init() {

		try {
		    configuration = getConfiguration(backend_xml);
		    serviceLocator = getServiceLocator();
		    internetMailSender = getInternetMailSender();
			profileManager = createProfileManager();

		} catch (ProfileManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
        return  ExternalComponentRegister.getInstance();
    }


	private IProfileManager createProfileManager() throws Exception {
		ProfileManagerImpl profileManager = new ProfileManagerImpl();
		profileManager.setContext(getBaseContext());
		profileManager.init();
		return profileManager;
	}

	protected BaseContext getBaseContext() throws Exception {
		BaseContext baseContext = new BaseContext();
		baseContext.setConfiguration(configuration);
		baseContext.setServiceLocator(serviceLocator);
		baseContext.setDirContextEnv(getDirContextEnv());

		baseContext.setSessionProperties(getSessionProperties()); // optional
																	// (for
																	// debug
																	// info)

		baseContext.setMailboxAccountManager(getMailboxAccountManager());

		baseContext.setMediaObjectFactory(getMediaObjectFactory());

		baseContext.setEventDispatcher(new EventDispatcherStub());
		baseContext.init();

		return baseContext;
	}

	protected Hashtable<String, String> getDirContextEnv() {
		Hashtable<String, String> dirContextEnv = new Hashtable<String, String>();
		dirContextEnv.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		dirContextEnv.put(Context.REFERRAL, "follow");
		return dirContextEnv;
	}

	private IMailboxAccountManager getMailboxAccountManager() {
		JavamailMailboxAccountManager javamailMailboxAccountManager = new JavamailMailboxAccountManager();

		JavamailContextFactory javamailContextFactory = new JavamailContextFactory();
		javamailContextFactory.setConfiguration(configuration);
		javamailContextFactory.setInternetMailSender(internetMailSender);
		javamailContextFactory.setMediaObjectFactory(getMediaObjectFactory());
		javamailContextFactory.setImapProperties(new ImapProperties());// check
																		// if
																		// optional

		javamailMailboxAccountManager.setContextFactory(javamailContextFactory);
		return javamailMailboxAccountManager;
	}

	private IMediaObjectFactory getMediaObjectFactory() {
		return new MediaObjectFactory();
	}

	/**
	 * Setup session properties with debug info.
	 * 
	 * @return the session properties
	 */
	private Properties getSessionProperties() {
		Properties sessionProperties = new Properties();
		sessionProperties.put("mail.debug", "true");
		return sessionProperties;
	}

	protected void migrate(String subscriberNumber) {
		try {

			ProfileStringCriteria filter = new ProfileStringCriteria(
					"billingnumber", subscriberNumber);
			IProfile[] profiles = profileManager.getProfile(filter);

			if (profiles != null && profiles.length > 0 && profiles[0] != null) {
				IProfile subscriberProfile = profiles[0];
				
				
				
				try {
					IMailbox mailbox = subscriberProfile.getMailbox();					
					processMailbox(mailbox);

					processGreetings(subscriberProfile);

				} catch (UserProvisioningException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (UnknownAttributeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private void processGreetings(IProfile subscriberProfile) {
		// Fetch and migrate the greetings
		//Do the below for all greeting types
		GreetingSpecification specification = new GreetingSpecification();

		try {
			IMediaObject allCallsGreeting = subscriberProfile
					.getGreeting(specification);
			// Transcode the greetings and store in MFS
			InputStream inputStream = allCallsGreeting.getInputStream();
			MediaProperties mediaProps = allCallsGreeting.getMediaProperties();
			long size = allCallsGreeting.getSize();

		} catch (ProfileManagerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processMailbox(IMailbox mailbox) {

		String folderName = "inbox";
		IFolder folder;
		try {
			folder = mailbox.getFolder(folderName);
			IStoredMessageList messageList = folder.getMessages();
			Iterator<IStoredMessage> it = messageList.iterator();
			while (it.hasNext()) {
				IStoredMessage storedMessage = (IStoredMessage) it.next();
				//Process headers
				storedMessage.getSubject();
				// ...				
				//Process attached spoken name
				IMediaObject spokenname = storedMessage.getSpokenNameOfSender();
				
				// Transcode and store sopken name in message...
				//Process content
				List<IMessageContent> messageContent = storedMessage.getContent();
				Iterator<IMessageContent> itMsgContent = messageContent.iterator();
				while (itMsgContent.hasNext()) {
					IMessageContent content = (IMessageContent) itMsgContent.next();
					MessageContentProperties contentProps = content.getContentProperties();
					IMediaObject mediaObbject = content.getMediaObject();
					MediaProperties mediaProps = content.getMediaProperties();
					//Transcode and store message
								
				}				
			}
		} catch (MailboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SampleMigrationApplication migrationApplication = new SampleMigrationApplication();
		migrationApplication.init();
		
		String subscriberNumber = "16105";
		migrationApplication.migrate(subscriberNumber );
		
		System.exit(0);

	}

}
