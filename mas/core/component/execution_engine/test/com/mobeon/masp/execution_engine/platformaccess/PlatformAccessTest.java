/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.execution_engine.session.SessionMdcItems;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.IStorableMessageFactory;
import com.mobeon.masp.mediacontentmanager.IMediaContentManager;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.mediacontentmanager.IMediaQualifierFactory;
import com.mobeon.masp.mediahandler.MediaHandlerFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.numberanalyzer.INumberAnalyzer;
import com.mobeon.masp.profilemanager.IProfile;
import com.mobeon.masp.profilemanager.IProfileManager;
import com.mobeon.masp.profilemanager.search.ProfileStringCriteria;
import com.mobeon.masp.profilemanager.search.ProfileOrCritera;
import com.mobeon.common.trafficeventsender.ITrafficEventSender;
import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.callmanager.CallManager;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all the unit tests in platformaccess. Contains mocked version of all the interfaces the PlatformAccessImpl
 * needs.
 * <p/>
 * User: ermmaha
 * Date: 2005-okt-18
 */
public abstract class PlatformAccessTest extends MockObjectTestCase {
    private static final String LOG4J_CONFIGURATION = "execution_engine/./test/com/mobeon/masp/execution_engine/platformaccess/log4jconf.xml";
    protected static final String SUBSCRIBER_NOT_FOUND = "55512345";
    
    public final static String VOICE = "voice";
    public final static String VIDEO = "video";
    public final static String FAX = "fax";
    public final static String EMAIL = "email";
    public final static String NEW = "new";
    public final static String READ = "read";
    public final static String DELETED = "deleted";
    public final static String URGENT = "urgent";
    public final static String NONURGENT = "nonurgent";
    public final static String TYPE = "type";
    public final static String STATE = "state";
    public final static String PRIO = "prio";
    public final static String FIFO = "fifo";
    public final static String LIFO = "lifo";

    protected PlatformAccessFactory platformAccessFactory;

    /**
     * default PlatformAccess to use in the tests
     */
    protected PlatformAccess platformAccess;

    protected PlatformAccessUtil platformAccessUtil;

    //session
    protected StubSession stubSession;

    //executioncontext
    protected Mock jmockExecutionContext;

    //numberanalyzer
    protected Mock jmockNumberAnalyzer;

    //profile
    protected Mock jmockProfileManager;
    protected Mock jmockProfileId0;
    protected Mock jmockProfileId1;

    //message sender
    protected Mock jmockIStorableMessageFactory;
    //configuration
    protected Mock jmockConfiguration;
    protected Mock jmockConfigurationGroup;

    //media
    protected Mock jmockMediaQualifierFactory;
    //events
    protected Mock jmockEventHub;
    //mediacontent
    protected Mock jmockMediaContentManager;
    //trafficeventsender
    protected Mock jmockTrafficEventSender;
    //mediaobjectfactory
    protected Mock jmockMediaObjectFactory;

    //mediahandlerfactory
    protected Mock jmockMediaHandlerFactory;
    protected Mock jmockContentTypeMapper;

    protected Mock jmockMediaTranslationManager;
    protected Mock jmockCallManager;

    protected Mock jmockChargingAccountManager;
    protected Mock jmockConfigManager;

    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    public PlatformAccessTest(String name) {
        super(name);
        MASTestSwitches.enableUnitTesting();

        jmockEventHub = mock(EventHub.class);

        jmockExecutionContext = mock(VXMLExecutionContext.class);
        jmockExecutionContext.stubs().method("getEventHub").will(returnValue(jmockEventHub.proxy()));
        setupSession();

        //numberanalyzer
        jmockNumberAnalyzer = mock(INumberAnalyzer.class);

        //profile
        jmockProfileManager = mock(IProfileManager.class);

        setupProfile();

        //message sender
        jmockIStorableMessageFactory = mock(IStorableMessageFactory.class);

        //configuration
        jmockConfiguration = mock(IConfiguration.class);
        jmockConfigurationGroup = mock(IGroup.class);
        jmockConfiguration.stubs().method("getGroup").will(returnValue(jmockConfigurationGroup.proxy()));

        //mediacontent
        jmockMediaContentManager = mock(IMediaContentManager.class);

        //trafficeventsender
        jmockTrafficEventSender = mock(ITrafficEventSender.class);

        // media factories
        jmockMediaQualifierFactory = mock(IMediaQualifierFactory.class);
        jmockMediaObjectFactory = mock(IMediaObjectFactory.class);

        jmockMediaHandlerFactory = mock(MediaHandlerFactory.class);
        jmockContentTypeMapper = mock(ContentTypeMapper.class);
        jmockConfigManager = mock(ConfigManager.class);
        
        // media translation manager
        jmockMediaTranslationManager = mock(MediaTranslationManager.class);

        setupPlatformAccessFactory();

        platformAccess = createPlatformAccess();

        platformAccessUtil = createPlatformAccessUtil();

        jmockCallManager = mock(CallManager.class);
    }

    /**
     * Creates the platformAccessFactory by loading it with the mocked interfaces.
     */
    private void setupPlatformAccessFactory() {
        INumberAnalyzer iNumberAnalyzer = (INumberAnalyzer) jmockNumberAnalyzer.proxy();
        IProfileManager iProfileManager = (IProfileManager) jmockProfileManager.proxy();
        IStorableMessageFactory iStorableMessageFactory = (IStorableMessageFactory) jmockIStorableMessageFactory.proxy();
        IConfiguration iConfiguration = (IConfiguration) jmockConfiguration.proxy();
        IMediaContentManager iMediaContentManager = (IMediaContentManager) jmockMediaContentManager.proxy();
        ITrafficEventSender iTrafficEventSender = (ITrafficEventSender) jmockTrafficEventSender.proxy();
        IMediaQualifierFactory iMediaQualifierFactory = (IMediaQualifierFactory) jmockMediaQualifierFactory.proxy();
        IMediaObjectFactory iMediaObjectFactory = (IMediaObjectFactory) jmockMediaObjectFactory.proxy();
        MediaTranslationManager mediaTranslationManager = (MediaTranslationManager) jmockMediaTranslationManager.proxy();
        MediaHandlerFactory mediaHandlerFactory = (MediaHandlerFactory) jmockMediaHandlerFactory.proxy();
        ContentTypeMapper contentTypeMapper = (ContentTypeMapper) jmockContentTypeMapper.proxy();
        ConfigManager configManager = (ConfigManager) jmockConfigManager.proxy();

        platformAccessFactory = new PlatformAccessFactoryImpl(iNumberAnalyzer, iProfileManager, iConfiguration,
                iStorableMessageFactory, iMediaContentManager, iTrafficEventSender, iMediaQualifierFactory,
                iMediaObjectFactory, mediaTranslationManager,
                mediaHandlerFactory, contentTypeMapper, configManager);
    }

    /**
     * Creates a new PlatformAccessImpl object
     *
     * @return new PlatformAccessImpl object
     */
    protected PlatformAccess createPlatformAccess() {
        ExecutionContext executionContext = (ExecutionContext) jmockExecutionContext.proxy();
        jmockConfiguration.expects(once()).method("getConfiguration").will(returnValue(jmockConfiguration.proxy()));
        return platformAccessFactory.create(executionContext);
    }

    /**
     * Creates a new PlatformAccessImpl object
     *
     * @param executionContext
     * @return new PlatformAccessImpl object
     */
    protected PlatformAccess createPlatformAccess(ExecutionContext executionContext) {
        jmockConfiguration.expects(once()).method("getConfiguration").will(returnValue(jmockConfiguration.proxy()));
        return platformAccessFactory.create(executionContext);
    }

    /**
     * Creates a new PlatformAccessUtil object
     *
     * @return new PlatformAccessUtil object
     */
    protected PlatformAccessUtil createPlatformAccessUtil() {
        VXMLExecutionContext executionContext = (VXMLExecutionContext) jmockExecutionContext.proxy();

        return platformAccessFactory.createUtil(executionContext);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public static String commaSeparate(String... strings) {
        String temp = "";
        for (int i = 0; i < strings.length; i++) {
            temp += strings[i];
            if ((i + 1) < strings.length) temp += ",";
        }
        return temp;
    }

    /**
     * Setup the jmockProfileManager to return some profiles.
     */
    private void setupProfile() {
        jmockProfileId0 = mock(IProfile.class);
        jmockProfileId1 = mock(IProfile.class);

        IProfile[] profiles0 = new IProfile[]{(IProfile) jmockProfileId0.proxy()};
        IProfile[] profiles1 = new IProfile[]{(IProfile) jmockProfileId1.proxy()};

        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", "161074"),
                        new ProfileStringCriteria("emmin", "161074")
                )),
                        eq(false)).
                will(returnValue(profiles0));

        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", "161075"),
                        new ProfileStringCriteria("emmin", "161075")
                        )), eq(false)).
                will(returnValue(profiles1));
        //setup a user that will not be found
        jmockProfileManager.stubs().method("getProfile").
                with(eq(new ProfileOrCritera(
                        new ProfileStringCriteria("billingnumber", SUBSCRIBER_NOT_FOUND),
                        new ProfileStringCriteria("emmin", "55512345")
                        )), eq(false)).
                will(returnValue(null));
    }

    private void setupSession() {
        stubSession = new StubSession();
        jmockExecutionContext.stubs().method("getSession").will(returnValue(stubSession));
    }

    protected void assertIntArray(int[] expected, int[] testArr) throws Exception {
        assertNotNull(testArr);
        assertEquals(expected.length, testArr.length);

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], testArr[i]);
        }
    }

    protected void assertArray(Object[] expected, Object[] testArr) throws Exception {
        assertNotNull(testArr);
        assertEquals(expected.length, testArr.length);

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], testArr[i]);
        }
    }
}

class StubSession implements ISession {

    private Map<String, Object> map = new HashMap<String, Object>();
    private Map<String, Object> MDCitems = new HashMap<String, Object>();
    private static ILogger log = ILoggerFactory.getILogger(StubSession.class);
    private Id<ISession> id = IdGeneratorImpl.SESSION_GENERATOR.generateId(1234);

    public String getId() {
        return id.toString();
    }

    public String getUnprefixedId() {
        return id.toString();
    }

    public Id<ISession> getIdentity() {
        return id;
    }

    public void setId(Id<ISession> id) {
        // Not implemented
    }

    public void setMdcItems(SessionMdcItems sessionMdcItems) {
        // Not implemented
    }

    public void dispose() {
        map.clear();
    }

    public synchronized void setData(String name, Object value) {
        map.put(name, value);
    }

    public synchronized Object getData(String name) {
        return map.get(name);
    }

    public void setSessionLogData(String name, Object value) {
        MDCitems.put(name, value);
    }

    public void registerSessionInLogger() {
        log.registerSessionInfo("session", id);
        for (String key : MDCitems.keySet()) {
            log.registerSessionInfo(key, MDCitems.get(key));
        }
    }
}
