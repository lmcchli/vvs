package com.abcxyz.services.moip.common.directoryaccess;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.abcxyz.messaging.common.mcd.KeyValues;
import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.mcd.proxy.MCDProxyServiceFactory;
import com.abcxyz.messaging.mcd.proxy.MCDProxyService;
import com.abcxyz.services.moip.common.cmnaccess.DAConstants;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;


public class MoipMCDProv extends TestCase {

	private MCDProxyService mcdProxy;

    private Profile profile = null;
	//private LogAgent logAgent;
	private CommonOamManager oamManager;
	private ConfigManager configMgr;
	private URI msid;
	private URI identity;
	private String phoneNum;
	private String keyId_des8;

    public MoipMCDProv(String name) {
        super(name);

    	oamManager = CommonOamManager.getInstance();

		Collection<String> configFilenames = new LinkedList<String>();
		configFilenames.add(System.getProperty("user.dir") + "\\cfg\\backend.conf");
		IConfiguration configuration = null;
		try{
			configuration = new ConfigurationImpl(null,configFilenames,false);

		    oamManager.setConfiguration(configuration);

	    	configMgr = oamManager.getMcdOam().getConfigManager();
	    	System.out.println("PA config para:" + configMgr.getParameter(MCDConstants.CONFIG_HOST));
	    	System.out.println("PA config para:" + configMgr.getParameter(MCDConstants.CONFIG_PORT));
	    	System.out.println("PA config para:" + configMgr.getParameter(MCDConstants.CONFIG_POOLSIZE));
	    	System.out.println("PA config para:" + configMgr.getParameter(MCDConstants.CONFIG_OPCO));
	    	System.out.println("PA config para:" + configMgr.getParameter(MCDConstants.CONFIG_BIND));
	    	System.out.println("PA config para:" + configMgr.getParameter(MCDConstants.CONFIG_PASSWORD));

			phoneNum   = "2233";
	        keyId_des8 = "msid:123456" + phoneNum + "dddd";  //hexadecimal string

	        msid     = new URI(keyId_des8);
	        identity = new URI(DAConstants.IDENTITY_PREFIX_TEL + phoneNum);

		}catch(Exception e) {
			System.out.println("Config could not be read");
			System.exit(0);
		}

    }

    public static Test suite() {
        //return new TestSuite(MoipMCDProv.class);
    	TestSuite suite= new TestSuite("Specific JUnit Test case");

    	//suite.addTest(new TestSuite().createTest(MoipMCDProv.class, "testCreateProfile"));
    	//suite.addTest(new TestSuite().createTest(MoipMCDProv.class, "testLookupProfile"));
    	//suite.addTest(new TestSuite().createTest(MoipMCDProv.class, "testDeleteProfile"));
    	suite.addTest(new TestSuite().createTest(MoipMCDProv.class, "testLookupProfileViaDA"));

        return suite;
    }

    private void binding() {

        try {

	    	mcdProxy = MCDProxyServiceFactory.getMCDProxyService(oamManager.getMcdOam(), false, null) ;

	    	ConfigManager configMgr = oamManager.getMcdOam().getConfigManager();
	        KeyValues[] credentials= new KeyValues[3];
	        credentials[0] = new KeyValues(MCDConstants.KEYVALUE_BIND_DN_FIELD_NAME, new String[]{configMgr.getParameter(MCDConstants.CONFIG_BIND)});
	        credentials[1] = new KeyValues(MCDConstants.KEYVALUE_PASSWORD_FIELD_NAME, new String[]{configMgr.getParameter(MCDConstants.CONFIG_PASSWORD)});
	        credentials[2] = new KeyValues(MCDConstants.KEYVALUE_OPCO_ID_FIELD_NAME, new String[]{configMgr.getParameter(MCDConstants.CONFIG_OPCO)});

	        mcdProxy.bindOpco(credentials);
          	System.out.println("binding okay");
        } catch ( Exception exc) {
        	exc.printStackTrace(System.out);
        }
    }

    private void initializeCommonAttributeValues() {
    	try {

    			profile = new ProfileContainer();

    			profile.addIdentity(msid);
    			profile.addIdentity(identity);
                profile.addIdentity(keyId_des8);
                profile.addAttributeValue(DAConstants.ATTR_NOTIF_NUMBER, phoneNum);
                profile.addAttributeValue(DAConstants.ATTR_COS_IDENTITY, "cos:1");
                profile.addAttributeValue("CNServices", "MOIP");
                profile.addAttributeValue(DAConstants.ATTR_PREFERRED_LANGUAGE, "en");
                profile.addAttributeValue(DAConstants.ATTR_BAD_LOGIN_COUNT, "0");
                profile.addAttributeValue(DAConstants.ATTR_PREFERRED_DATE_FORMAT, "mm/dd/yyyy");
                profile.addAttributeValue(DAConstants.ATTR_PREFERRED_TIME_FORMAT, "12");
                profile.addAttributeValue(DAConstants.ATTR_SUBSCRIBER_TIME_ZONE, "GMT");
                profile.addAttributeValue(DAConstants.ATTR_ACTIVE_GREETING_ID, "SpokenName,AllCalls");
                profile.addAttributeValue(DAConstants.ATTR_FTL, "00,F");
                profile.addAttributeValue("objectClass", "subscriber");
                profile.addAttributeValue("objectClass", "MOIPSubscriber");


        } catch (Exception e) {
        		e.printStackTrace(System.out);
        		fail(e.toString());
        }
    }


    public void testCreateProfile() {
    	try {
        	System.out.println("JUnit MoipMCDProv.testCreateProfile() ...");

        	initializeCommonAttributeValues();
        	binding();
        	mcdProxy.createProfile("subscriber", msid, profile);
        	System.out.println("JUnit MoipMCDProv.testCreateProfile() done");

        	testLookupProfile();

    	} catch (Exception e) {
    		System.out.println("Exception at testCreateProfile :" + e.getMessage());
    		e.printStackTrace(System.out);
    		fail(e.toString());
    	}
    }

    public void testLookupProfile() {
    	try {
        	System.out.println("JUnit MoipMCDProv.testLookupProfile1() ...");

        	binding();

    		Profile oProfile = mcdProxy.lookupProfile("subscriber", identity, "moip");

    		System.out.println("reading profile from MCD:\n" + oProfile.toString());

    	} catch (Exception e) {
    		System.out.println("Exception at lookupProfile :" + e.getMessage());
    		e.printStackTrace(System.out);
    		fail(e.toString());
    	}
    }

    public void testLookupProfileViaDA() {
    	try {
        	System.out.println("JUnit MoipMCDProv.testLookupProfileViaDA() ...");

        	DirectoryAccess da = DirectoryAccess.getInstance();
        	DirectoryAccessSubscriber daSub =  (DirectoryAccessSubscriber)da.lookupSubscriber("tel:5266");

        	daSub.getSubscriberProfile();

        	//System.out.println("profile:" + subProfile.getProfileContainer().toString());
        	//subProfile.toString();

        	System.out.println("JUnit MoipMCDProv.testLookupProfileViaDA() done");

    	} catch (Exception e) {
    		System.out.println("Exception at lookupProfile :" + e.getMessage());
    		e.printStackTrace(System.out);
    		fail(e.toString());
    	}
    }

    public void testDeleteProfile() {
    	try {

        	System.out.println("JUnit MoipMCDProv.testDeleteProfile() ...");
        	binding();
    		mcdProxy.deleteProfile("subscriber", msid);
        	System.out.println("JUnit MoipMCDProv.testDeleteProfile() done");
    	} catch (Exception e) {
    		System.out.println("Exception at testDeleteProfile :" + e.getMessage());
    		e.printStackTrace(System.out);
    		fail(e.toString());
    	}
    }


}
