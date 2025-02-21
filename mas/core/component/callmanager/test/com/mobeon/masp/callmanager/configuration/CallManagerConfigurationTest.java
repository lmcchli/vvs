/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import jakarta.activation.MimeType;

import junit.framework.TestCase;

import junit.framework.*;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.RestrictedOutboundHeaders.RestrictedHeader;
import com.mobeon.masp.callmanager.releasecausemapping.ReleaseCauseMapping;

/**
 * Class to test the new configuration files for callManager.
 * @author lmcvcio
 *
 */
public class CallManagerConfigurationTest extends TestCase {

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch("cfg/log4j2.xml");
    }

    public void setUp() throws Exception {
        super.setUp();

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("cfg/callManager.conf");

        // Initialize configuration now to be able to setup SSPs before CM
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());

        ConfigurationReader.getInstance().setInitialConfiguration(
                cm.getConfiguration());
        ConfigurationReader.getInstance().update();
    }

    /**
     * Checks the content of the callManager.xsd/conf.
     * @throws Exception
     */
    public void testConfigurationValues()
            throws Exception {

        assertEquals(ConfigurationReader.getInstance().getConfig().getCallNotAcceptedTimer(), 32000);
        assertEquals(ConfigurationReader.getInstance().getConfig().getRegisterBackoffTimer(), 120000);
        assertEquals(ConfigurationReader.getInstance().getConfig().getRegisterBeforeExpirationTime(), 5000);
        assertEquals(ConfigurationReader.getInstance().getConfig().getBlackListTimer(), 60000);
        assertEquals(ConfigurationReader.getInstance().getConfig().getInboundAudioMimeType().toString(), "audio/pcmu");
        assertEquals(ConfigurationReader.getInstance().getConfig().getInboundVideoMimeType().toString(), "video/h263");
        assertEquals(ConfigurationReader.getInstance().getConfig().getPTime(), 40);
        assertEquals(ConfigurationReader.getInstance().getConfig().getRegisteredName(), "mas");
        assertEquals(ConfigurationReader.getInstance().getConfig().getCallType().toString(), "VIDEO");
        assertEquals(ConfigurationReader.getInstance().getConfig().getOutboundCallCallingParty(), "");
        assertEquals(ConfigurationReader.getInstance().getConfig().getOutboundCallConnectTimer(), 30000);
        assertEquals(ConfigurationReader.getInstance().getConfig().getContactUriOverride(), "");
        assertTrue(ConfigurationReader.getInstance().getConfig().getDisconnectOnSipTimeout());
        assertEquals(ConfigurationReader.getInstance().getConfig().getReliableResponseUsage().toString(), "SDPONLY");
        assertTrue(ConfigurationReader.getInstance().getConfig().getSupportTestInput());
        assertEquals(ConfigurationReader.getInstance().getConfig().getOutboundCallServerPort(), 5060);
        /*
         * Test the values in userAgentWithPhoneInUriButNoUserParameter
         */
        ArrayList<String> userAgents = ConfigurationReader.getInstance().getConfig().getUserAgentWithPhoneInUriButNoUserParameter();
        assertTrue(userAgents.contains("dialogic"));
        assertTrue(userAgents.contains("cisco"));
        assertTrue(userAgents.contains("radvision"));
        assertTrue(userAgents.contains("eyebeam"));
        assertTrue(userAgents.contains("express talk"));
        assertTrue(userAgents.contains("mirial"));

        //sipTimers
        SipTimers sipTimers = ConfigurationReader.getInstance().getConfig().getSipTimers();
        assertEquals(sipTimers.getT2().intValue(), 8);
        assertEquals(sipTimers.getT4().intValue(), 10);
        assertEquals(sipTimers.getTimerB().intValue(), 6);
        assertEquals(sipTimers.getTimerC().intValue(), 360);
        assertEquals(sipTimers.getTimerD().intValue(), 64);
        assertEquals(sipTimers.getTimerF().intValue(), 18);
        assertEquals(sipTimers.getTimerH().intValue(), 64);
        assertEquals(sipTimers.getTimerJ().intValue(), 64);


        assertEquals(ConfigurationReader.getInstance().getConfig().getInitialRampHWM(), 0);
        assertEquals(ConfigurationReader.getInstance().getConfig().getRampFactor(), 1.0);

        assertEquals(ConfigurationReader.getInstance().getConfig().getSupportForRedirectingRtpUserAgents().size(), 1); // this is a list with "" as unique element
        assertEquals(ConfigurationReader.getInstance().getConfig().getSupportForRedirectingRtpTimeout(), 0);
        assertFalse(ConfigurationReader.getInstance().getConfig().isSupportForRedirectingRTPConfigured());

        //RequiredOutboundAudioMedia.List
        Collection<MimeType> outboundAudioMediaTypes = ConfigurationReader.getInstance().getConfig().getOutboundAudioMimeTypes();
        assertEquals(outboundAudioMediaTypes.size(), 2);
        Iterator<MimeType> it = outboundAudioMediaTypes.iterator();
        assertEquals(it.hasNext(), true);
        assertEquals(it.next().toString(), "audio/pcmu");
        assertEquals(it.hasNext(), true);
        assertEquals(it.next().toString(), "audio/telephone-event");

        //RequiredOutboundVideoMedia.List
        Collection<MimeType> outboundMediaTypes = ConfigurationReader.getInstance().getConfig().getOutboundVideoMimeTypes();
        assertEquals(outboundMediaTypes.size(), 1);
        Iterator<MimeType> itVideo = outboundMediaTypes.iterator();
        assertEquals(itVideo.hasNext(), true);
        assertEquals(itVideo.next().toString(), "video/h263");

        //ReleaseCauseMapping - for more detailed testing, there is another class called ReleaseCauseMappingTest
        ReleaseCauseMapping releaseCauseMapping = ConfigurationReader.getInstance().getConfig().getReleaseCauseMapping();
        assertEquals(releaseCauseMapping.getDefaultNetworkStatusCode(), 621);
        assertEquals(releaseCauseMapping.getNetworkStatusCode(600),614);

        //Remote Party
        RemoteParty remoteParty = ConfigurationReader.getInstance().getConfig().getRemoteParty();
        assertTrue(remoteParty.isSipProxy());
        assertNotNull(remoteParty.getSipProxy());
        assertEquals(remoteParty.getSipProxy().getHost(), "sipproxy");
        assertEquals(remoteParty.getSipProxy().getPort(), 5060);
        remoteParty.setSipProxy("localhost", 8888);
        assertEquals(remoteParty.getSipProxy().getHost(), "localhost");
        assertEquals(remoteParty.getSipProxy().getPort(), 8888);

        //RestrictedOutboundHeaders
        RestrictedOutboundHeaders restrictedOutboundHeaders = ConfigurationReader.getInstance().getConfig().getRestrictedOutboundHeaders();
        assertEquals(restrictedOutboundHeaders.getAmountOfRestrictedHeaders(), 0);
        assertFalse(restrictedOutboundHeaders.isRestricted(RestrictedHeader.REMOTE_PARTY_ID));
        assertFalse(restrictedOutboundHeaders.isRestricted(RestrictedHeader.P_ASSERTED_IDENTITY));



    }



}
