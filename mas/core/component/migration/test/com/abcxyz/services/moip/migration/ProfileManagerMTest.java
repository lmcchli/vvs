package com.abcxyz.services.moip.migration;

import com.abcxyz.services.moip.migration.profilemanager.moip.BaseContext;
import com.abcxyz.services.moip.migration.profilemanager.moip.ICos;
import com.abcxyz.services.moip.migration.profilemanager.moip.IProfile;
import com.abcxyz.services.moip.migration.profilemanager.moip.IProfileManager;
import com.abcxyz.services.moip.migration.profilemanager.moip.ProfileManagerImpl;
import com.abcxyz.services.moip.migration.profilemanager.moip.ProfileMetaData;
import com.abcxyz.services.moip.migration.profilemanager.moip.search.ProfileOrCritera;
import com.abcxyz.services.moip.migration.profilemanager.moip.search.ProfileStringCriteria;
import com.mobeon.common.eventnotifier.EventDispatcherStub;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.masp.mailbox.*;
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
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.activation.MimeType;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Manual tests for ProfileManager. Uses the MUR and MS on husqvarna.lab
 * <p/>
 * NOTE! Contains no Assertions!
 *
 * @author emahagl
 */
public class ProfileManagerMTest extends MTestBaseTestCase {

    private IProfileManager profileManager;
	//private String number = "46734611205";
    private String number = "16105";

    public ProfileManagerMTest(String string) {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();
        profileManager = createProfileManager();
    }

    /**
     * Test retrieving a profile.
     *
     * @throws Exception
     */
    public void testGetProfile() throws Exception {
    	
    	
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", number);
        try {
            IProfile[] profiles = profileManager.getProfile(filter);
            System.out.println("mailhost=" + profiles[0].getStringAttribute("mailhost"));
            System.out.println("uniqueidentifier=" + profiles[0].getStringAttribute("uniqueidentifier"));
            System.out.println("uid=" + profiles[0].getStringAttribute("uid"));

            ICos cos = profiles[0].getCos();
            System.out.println("fastloginenabled=" + cos.getBooleanAttribute("fastloginenabled"));
        } catch (HostException e) {
            System.out.println("Error " + e);
            fail("Check configuration of componentservices.cfg" + e.getMessage());
        }
    }
    


//    public void testGetCosById() throws Exception {
////        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "161000");
//        try {
//            ICos cos = profileManager.getCos(202);
//            System.out.println("Attributes: " + cos.getAttributes().toString());
//        } catch (HostException e) {
//            System.out.println("Error " + e);
//        }        
//    }
//    
//    /**
//     * Test to set some attributes.
//     *
//     * @throws Exception
//     */
//    public void testSetAttributes() throws Exception {
//        try {
//            ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "161000");
//            IProfile[] profiles = profileManager.getProfile(filter);
//            profiles[0].setIntegerAttribute("badlogincount", 1);
//        } catch (InvalidAttributeValueException e) {
//            System.out.println("Error " + e);
//        } catch (HostException e) {
//            System.out.println("Error " + e);
//        }
//    }
//
    public void testGetMailbox() throws Exception {
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber",number);
        IProfile[] profiles = profileManager.getProfile(filter);

        assertEquals(1, profiles.length );
        try {
            IMailbox mailbox = profiles[0].getMailbox();
            IFolder folder = mailbox.getFolder("inbox");
            IStoredMessageList storedMessageList = folder.getMessages();
        } catch (MailboxException e) {
            System.out.println("Error " + e);
            fail("Failed to retrieve messages :" + e.getMessage());
        } catch (HostException e) {
            System.out.println("Error " + e);
            fail("Failed to retrieve messages :" + e.getMessage());
        }

        List<IServiceInstance> list = serviceLocator.getServiceInstances("storage");
        for (Iterator<IServiceInstance> iterator = list.iterator(); iterator.hasNext();) {
            IServiceInstance iServiceInstance = iterator.next();
           // System.out.println("Service " + iServiceInstance + " is " + iServiceInstance.getServiceStatus());
        }
    }

    public void testGetGreeting() throws Exception {
        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", number); //uniqueidentifier=um1185,ou=c1,o=mobeon.com
        IProfile[] profiles = profileManager.getProfile(filter);

        try {
            IMediaObject mediaObject = profiles[0].getGreeting(new GreetingSpecification());
            MediaProperties mediaProperties = mediaObject.getMediaProperties();
            System.out.println(mediaProperties);
        } catch (ProfileManagerException e) {
            System.out.println("Error " + e);
            fail("Failed to retrieve greeting manager mailbox: " + e.getMessage());
        }

        List<IServiceInstance> list = serviceLocator.getServiceInstances("storage");
        for (Iterator<IServiceInstance> iterator = list.iterator(); iterator.hasNext();) {
            IServiceInstance iServiceInstance = iterator.next();
        //    System.out.println("Service " + iServiceInstance + " is " + iServiceInstance.getServiceStatus());
        }
    }
//
//    public void testSetGreeting() throws Exception {
//        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "161000");
//        IProfile[] profiles = profileManager.getProfile(filter);
//
//        try {
//            IMediaObject greetingObject = getMediaObjectFactory().create(new File("test", "busy.wav"), new MediaProperties(new MimeType("audio/wav"), "wav"));
//            profiles[0].setGreeting(new GreetingSpecification("cdg", GreetingFormat.VOICE, "1022"), greetingObject);
//            profiles[0].setStringAttribute("emactivecdg", "#1022#,#1023#");
//        } catch (ProfileManagerException e) {
//            System.out.println("Error " + e);
//        }
//
//        List<IServiceInstance> list = serviceLocator.getServiceInstances("storage");
//        for (Iterator<IServiceInstance> iterator = list.iterator(); iterator.hasNext();) {
//            IServiceInstance iServiceInstance = iterator.next();
//        //    System.out.println("Service " + iServiceInstance + " is " + iServiceInstance.getServiceStatus());
//        }
//    }
//
//    public void testSetSpokenName() throws Exception {
//        ProfileStringCriteria filter = new ProfileStringCriteria("billingnumber", "161000");
//        IProfile[] profiles = profileManager.getProfile(filter);
//
//        try {
//            IMediaObject greetingObject = getMediaObjectFactory().create(new File("test", "busy.wav"), new MediaProperties(new MimeType("audio/wav"), "wav"));
//            profiles[0].setSpokenName(GreetingFormat.VOICE, greetingObject);
//        } catch (ProfileManagerException e) {
//            System.out.println("Error " + e);
//        }
//
//        List<IServiceInstance> list = serviceLocator.getServiceInstances("storage");
//        for (Iterator<IServiceInstance> iterator = list.iterator(); iterator.hasNext();) {
//            IServiceInstance iServiceInstance = iterator.next();
//          //  System.out.println("Service " + iServiceInstance + " is " + iServiceInstance.getServiceStatus());
//        }
//    }
//
//    /**
//     * Test the getServiceInstances methhod in ILocateService.
//     * ToDo Put it in an own MTest class in externalcomponentregister
//     *
//     * @throws Exception
//     */
//    public void testGetServiceInstances() throws Exception {
//        List<IServiceInstance> list = serviceLocator.getServiceInstances("userregister");
//        for (Iterator<IServiceInstance> iterator = list.iterator(); iterator.hasNext();) {
//            IServiceInstance iServiceInstance = iterator.next();
//        //    System.out.println("Service " + iServiceInstance + " is " + iServiceInstance.getServiceStatus());
//        }
//
//        list = serviceLocator.getServiceInstances("userregisterwrite");
//        for (Iterator<IServiceInstance> iterator = list.iterator(); iterator.hasNext();) {
//            IServiceInstance iServiceInstance = iterator.next();
//           // System.out.println("Service " + iServiceInstance + " is " + iServiceInstance.getServiceStatus());
//        }
//
//        list = serviceLocator.getServiceInstances("eventreporting");
//        for (Iterator<IServiceInstance> iterator = list.iterator(); iterator.hasNext();) {
//            IServiceInstance iServiceInstance = iterator.next();
//           // System.out.println("Service " + iServiceInstance + " is " + iServiceInstance.getServiceStatus());
//        }
//    }

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

        baseContext.setSessionProperties(getSessionProperties()); // optional (for debug info)

        baseContext.setMailboxAccountManager(getMailboxAccountManager());

        baseContext.setMediaObjectFactory(getMediaObjectFactory());

        baseContext.setEventDispatcher(new EventDispatcherStub());
        baseContext.init();

        return baseContext;
    }

    private IMailboxAccountManager getMailboxAccountManager() {
        JavamailMailboxAccountManager javamailMailboxAccountManager = new JavamailMailboxAccountManager();

        JavamailContextFactory javamailContextFactory = new JavamailContextFactory();
        javamailContextFactory.setConfiguration(configuration);
        javamailContextFactory.setInternetMailSender(internetMailSender);
        javamailContextFactory.setMediaObjectFactory(getMediaObjectFactory());
        javamailContextFactory.setImapProperties(new ImapProperties());// check if optional

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

    /**
     * Returns the metadata for the application attribute
     *
     * @param attribute application attribute to get metadata for
     * @return metadata for the attribute
     * @throws UnknownAttributeException if metadata for attribute can not be found
     */
    private ProfileMetaData getMetaData(String attribute, BaseContext baseContext) throws UnknownAttributeException {
        Map<String, ProfileMetaData> applicationAttributeMap = baseContext.getConfig().getApplicationAttributeMap();
        if (applicationAttributeMap.containsKey(attribute)) {
            return applicationAttributeMap.get(attribute);
        } else {
            String errMsg = "Could not find metadata for " + attribute;
            throw new UnknownAttributeException(errMsg);
        }
    }

 /*   public static Test suite() {
        return new TestSuite(ProfileManagerMTest.class);
    }*/
}

// a test of attributetype
// ProfileMetaData profileMetaData = getMetaData("userlevel", baseContext);
// AttributeType attributeType = profileMetaData.getType();

/*  try {
    //profiles[0].setBooleanAttributes("autoplay", null);
    profiles[0].setIntegerAttributes("badlogincount", null);
    //profiles[0].setStringAttributes("activegreetingid", new String[]{"greeting"});
} catch (InvalidAttributeValueException e) {
    System.out.println("Error " + e);
}*/