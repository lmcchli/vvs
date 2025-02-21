/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.MediaLength;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.profilemanager.BaseContext;

import jakarta.mail.*;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.HeaderTerm;
import jakarta.mail.search.SubjectTerm;

/**
 * GreetingManager Tester.
 *
 * @author mande
 * @version 1.0
 * @since <pre>01/19/2006</pre>
 */
public class GreetingManagerMTest extends GreetingMockObjectBaseTestCase {
    private static final ILogger LOG = ILoggerFactory.getILogger(GreetingManagerMTest.class);

    private static final String MTESTCFG = "../profilemanager/mtest/com/mobeon/masp/profilemanager/tezt.xml";
    private static final String CONFIG_GROUP_NAME = GreetingManagerMTest.class.getSimpleName().toLowerCase();
    private static final String CFGFILE = "mtest/com/mobeon/masp/profilemanager/profilemanager.xml";

    private String host = "ockelbo.lab.mobeon.com";

    private int port = 143;
    private String useradmin = "GrtAdm_33";
    private String password = "Gr8Pw4GA";
    private String folder = "19161/Greeting";
    private GreetingManager greetingManager;
    private String telephonenumber = "19161";
    protected BaseContext profileContext;

    public GreetingManagerMTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        readConfiguration();
        // Setup configuration
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(CFGFILE);
        profileContext = new BaseContext();
        profileContext.setMediaObjectFactory(new MediaObjectFactory());
        profileContext.setConfiguration(configurationManager.getConfiguration());
        profileContext.setDirContextEnv(getDirContextEnv());
        profileContext.setSessionProperties(getSessionProperties());
        profileContext.setEventDispatcher(getEventDispatcher());
        profileContext.init();
        greetingManager = new GreetingManagerImpl(profileContext, useradmin, folder);
    }

    private void readConfiguration() {
        // Setup configuration
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(MTESTCFG);
        try {
            IConfiguration configuration = configurationManager.getConfiguration();
            IGroup group = configuration.getGroup(CONFIG_GROUP_NAME);
            host = group.getString("host", host);
            port = group.getInteger("port", port);
            useradmin = group.getString("useradmin", useradmin);
            password = group.getString("password", password);
            telephonenumber = group.getString("telephonenumber", telephonenumber);
            folder = telephonenumber + "/Greeting";
        } catch (UnknownGroupException e) {
            if (LOG.isDebugEnabled()) LOG.debug(e.getMessage());
        } catch (GroupCardinalityException e) {
            if (LOG.isDebugEnabled()) LOG.debug(e.getMessage());
        } catch (ParameterTypeException e) {
            if (LOG.isDebugEnabled()) LOG.debug(e.getMessage());
        }
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGreeting() throws Exception {
        GreetingSpecification specification;
        for (GreetingType type : GreetingType.values()) {
            for (GreetingFormat format : GreetingFormat.values()) {
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME && format == GreetingFormat.VIDEO) {
                    // No support for video distlistspokenname yet
                    continue;
                }
                if (STRING_REPRESENTED_TYPES.contains(type)) {
                    specification = new GreetingSpecification(greetingMap.get(type), format);
                } else {
                    specification = new GreetingSpecification(type, format);
                }
                if (type == GreetingType.CDG) {
                    specification.setSubId(SUBID);
                }
                if (type == GreetingType.DIST_LIST_SPOKEN_NAME) {
                    specification.setSubId(DIST_LIST_SPOKEN_NAME_SUBID);
                }
                // Remove greeting
                greetingManager.setGreeting(TELEPHONE_NUMBER, specification, null);

                // Greeting should not exist
                try {
                    greetingManager.getGreeting(specification);
                    fail("Expected GreetingNotFoundException");
                } catch (GreetingNotFoundException e) {
                    assertTrue(true); // For statistical purposes
                }

                greetingManager.setGreeting(TELEPHONE_NUMBER, specification, mediaObjectMap.get(type).get(format));
                // Get greeting
                IMediaObject greeting = greetingManager.getGreeting(specification);
                assertNotNull("Greeting should not be null", greeting);
                MediaProperties mediaProperties = greeting.getMediaProperties();
                String contentType = getContentType(format);
                assertTrue("Content type should be " + contentType, mediaProperties.getContentType().match(contentType));
                String fileExtension = getFileExtension(format);
                assertEquals("File extension should be " + fileExtension, fileExtension, mediaProperties.getFileExtension());
                long mediaLength = lengthMap.get(type);
                assertEquals("Media length should be " + mediaLength, mediaLength,
                        mediaProperties.getLengthInUnit(MediaLength.LengthUnit.MILLISECONDS));
                long size = sizeMap.get(type).get(format);
                assertEquals("Size should be " + size, size, mediaProperties.getSize());
                Message message = getMessage(specification);
                testGreetingMessage(message, specification);
            }
        }
    }

    private Message getMessage(GreetingSpecification specification) throws Exception {
        Store store = profileContext.getStoreManager().getStore(host, port, useradmin, password);
        Folder imapFolder = store.getFolder(TELEPHONE_NUMBER + "/Greeting");
        if (imapFolder.exists()) {
            imapFolder.open(Folder.READ_WRITE);
            Message[] messages = imapFolder.search(getGreetingSearchTerm(specification));
            assertEquals("1 greeting message should be found", 1, messages.length);
            return messages[0];
        }
        fail("Greeting folder did not exist");
        return null; // Should not happen (if fail method doesn't fail)
    }

    private SearchTerm getGreetingSearchTerm(GreetingSpecification specification) {
        if (specification.getType() == GreetingType.DIST_LIST_SPOKEN_NAME) {
            return new SubjectTerm(specification.getSubId());
        } else {
            return new AndTerm(
                    new HeaderTerm(GREETING_TYPE, GreetingUtils.getTypeHeader(specification)),
                    new HeaderTerm(GREETING_FORMAT, GreetingUtils.getFormatHeader(specification.getFormat()))
            );
        }
    }

    public static Test suite() {
        return new TestSuite(GreetingManagerMTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
