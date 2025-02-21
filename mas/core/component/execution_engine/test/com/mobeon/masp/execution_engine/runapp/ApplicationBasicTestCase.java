package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.callmanager.*;
import com.mobeon.masp.callmanager.events.SipMessageResponseEvent;
import com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator;
import com.mobeon.masp.callmanager.CallProperties.CallType;
import com.mobeon.masp.execution_engine.ApplicationManagmentImpl;
import com.mobeon.masp.execution_engine.AutoTestHandlerFactory;
import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.runapp.mock.*;
import com.mobeon.masp.execution_engine.runtime.event.delayed.DelayedEventProcessor;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.util.component.IComponentManager;
import com.mobeon.masp.util.component.SpringComponentManager;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.masp.util.test.MASTestSwitches;
import junit.framework.Test;
import org.jmock.MockObjectTestCase;
import org.springframework.beans.BeansException;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import jakarta.activation.MimeType;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Base class used to create test cases with the runapp testbed.
 */
public abstract class ApplicationBasicTestCase<T extends ApplicationBasicTestCase> extends MockObjectTestCase {

    protected int testCaseTimeout = 5000;

    protected String aNumber = "4660161084";
    protected String bNumber = "4660161085";
    protected String cNumber = "4660161086";

    protected int sleepBeforeStopSaveLog = 0;
    /**
     * The call manager, from here we can initiate calls and control
     * the application.
     */
    protected static CallManagerMock callMgr = null;
    protected static ServiceRequestManagerMock serviceMgr = null;
    protected MimeType mimeType;
    protected static String applicationProtocol = "test";
    protected static String applicationRoot = "/test/com/mobeon/masp/execution_engine/runapp/applications/";
    private static String testLanguage = "ccxml";
    private static String testSubdir = "transition";
    private String path = "/test/com/mobeon/masp/execution_engine/runapp/";

    private static ExecutorService executorService = ExecutorServiceManager.getInstance().getExecutorService(CallManagerMock.class);

    /**
     * The list of all testcases that we need to execute
     */
    private static ApplicationTestCase[] tempTestCaseArray;
    private static boolean scaling = true;

    /**
     * SessionInfoMock, can be queired how EE so far has dealt with it
     */
    static private SessionInfoMock sessionInfoMock;
    protected ApplicationTestSetup<T> suite;
    protected static boolean runningSuite = false;
    public NumberCompletion a_party_completion;
    private static HashMap<Class<?>, ApplicationTestCase[]> allCases;
    private boolean initialized;
    private String logFileName;
    public int inboundBitRate;

    /**
     * @param lfe
     */

    protected void validateTest(LogFileExaminer lfe) {
        List<String> l = TestAppender.getOutputList();
        boolean success = lfe.evaluateLogFile(l);
        if (!success) fail(lfe.getReason());
    }

    protected LogFileExaminer runSimpleTest(String testCase, boolean ignoreWarnings) {
        boolean exited = createCallAndWaitForCompletion(testCase);
        if (!exited) {
            fail("The application timed out!");
        }

        // Verify the output
        LogFileExaminer lfe = new LogFileExaminer();
        lfe.failOnUndefinedErrors(ignoreWarnings);
        return lfe;
    }

    protected LogFileExaminer runSimpleTest(String testCase) {
        return runSimpleTest(testCase, false);
    }

    protected static URI getRootURI() {
        return URI.create(applicationProtocol + ":" + applicationRoot);
    }

    protected static void enableAutomaticXML() {
        applicationProtocol = "test";
    }

    protected static ApplicationTestCase testCase(String name) {
        return new ApplicationTestCase(name, getRootURI() + testLanguage + "/" + testSubdir + "/" + name + ".xml");
    }

    protected static void testCases(ApplicationTestCase ... cases) {
        tempTestCaseArray = cases;
    }

    protected static void testSubdir(String s) {
        testSubdir = s;
    }

    protected static void testLanguage(String s) {
        testLanguage = s;
    }

    public static void setSessionInfoMock(SessionInfoMock sessionInfoMock2) {
        sessionInfoMock = sessionInfoMock2;
    }

    public static SessionInfoMock getSessionInfoMock() {
        return sessionInfoMock;
    }

    public void setResponseToNegotiateEarlyMedia(CallManagerMock.EventType e) {
        callMgr.setResponseToNegotiateEarlyMedia(e);
    }

    public void setResponseToRecord(CallManagerMock.EventType e) {
        callMgr.setResponseToRecord(e);
    }

    public void setResponseToAccept(CallManagerMock.EventType e) {
        callMgr.setResponseToAccept(e);
    }

    public void setResponseToProxy(CallManagerMock.EventType e) {
        callMgr.setResponseToProxy(e);
    }
    
    public void setSendProgressingEvent(boolean b) {
        callMgr.setSendProgressingEvent(b);
    }

    protected static <T extends ApplicationBasicTestCase> Test genericSuite(Class<T> clazz) {
        runningSuite = true;
        return new ApplicationTestSetup<T>(clazz);
    }


    protected void genericSetUp(Class<T> clazz) throws Exception {
        if (!runningSuite) {
            //Timeout disabled when running single test
            MASTestSwitches.disableTestTimeout();
            //Debug enabled when running single test
            MASTestSwitches.enableForcedLogging();
            suite = new ApplicationTestSetup<T>(clazz);
            super.setUp();
            suite.setUp();
        }
        try {
            String fullPath = path + this.logFileName;
            ILoggerFactory.configureAndWatch(MASTestSwitches.currentMasDir() + fullPath);
            log = ILoggerFactory.getILogger(ApplicationBasicTestCase.class);
            log.info("MOCK: Log initialized using config "+ logFileName);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Error while trying to initialize testcase !",t);
        }
    }

    protected void tearDown() throws Exception {
        genericTearDown();
    }

    private void genericTearDown() throws Exception {
        if (!runningSuite && suite != null) {
            suite.tearDown();
            super.tearDown();
        }
    }

    protected void waitSimpleInteractiveTest(InboundCallMock icm) {
        boolean exited = icm.waitForExecutionToFinish((long) testCaseTimeout);
        icm.terminateService();

        // Stop the saving of the log, we do not need anymore
        // for the analysis
        TestAppender.stopSave(log);

        if (!exited) {
            fail("The application timed out!");
        }
    }

    protected InboundCallMock runSimpleInteractiveTest(String testCase) {
        TestAppender.clear();
        log.info("TESTCASE " + testCase + "STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up this tests calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        RedirectingParty c = new RedirectingParty();
        setupABCInformation(a, b, c);

        // Make the call
        InboundCallMock icm = callMgr.createInboundCall(testCase, a, b, c, callType, mimeType, inboundBitRate);
        icm.startCall();
        return icm;
    }

    protected static void store(Class<?> aClass) {
        if (allCases == null)
            allCases = new HashMap<Class<?>, ApplicationTestCase[]>();
        allCases.put(aClass, tempTestCaseArray);
        tempTestCaseArray = null;
    }

    public static ApplicationTestCase[] testCaseForClass(Class<?> cls) {
        return allCases.get(cls);
    }

    protected Set<Connection> makeEmptyInboundProp() {
        return new TreeSet<Connection>();
    }

    public static void addTestFilter(String expr) {
        FilteringTestSuite.addTestFilter(expr);
    }

    protected void waitForVxmlStart() {
        TestEventGenerator.declareWait(TestEvent.VXML_MODULE_STARTED);
        TestEventGenerator.waitFor(TestEvent.VXML_MODULE_STARTED, 10000);
        TestEventGenerator.declareNoWait(TestEvent.VXML_MODULE_STARTED);
    }

    /**
     * A list of all testcase xml files
     */
    public static class ApplicationTestCase {

        /**
         * Name of a testcase, which is the mapping from a service to an application URI.
         *
         * @param name Name of the service.
         * @param file Location of the application xml file.
         */
        public ApplicationTestCase(String name, String file) {
            this.name = name;
            this.configurationFile = file;
        }

        /**
         * Name of the service
         */
        public String name;

        /**
         * Name of the application xml file.
         */
        public String configurationFile;
    }


    /**
     * The log object.
     */
    protected static ILogger log = null;

    protected PresentationIndicator c_party_presentationIndicator = PresentationIndicator.RESTRICTED;
    protected PresentationIndicator a_party_presentationIndicator = PresentationIndicator.ALLOWED;


    protected CallType callType = CallType.VOICE;
    protected RedirectingParty.RedirectingReason redirectingReason =
            RedirectingParty.RedirectingReason.NO_REPLY;

    /**
     * Constructor for this test suite, must be called from the testclass that inherits
     * this class through a super(event) call.
     *
     * @param event
     */
    public ApplicationBasicTestCase(String event, String configFileName) {
        super(event);

        if(! initialized){
            initialized = true;
            AutoTestHandlerFactory.initialize();
            MASTestSwitches.enableUnitTesting();
        }

        if(!configFileName.equals(logFileName)){
            this.logFileName = configFileName;
            try {
                String fullPath = path + this.logFileName;
                ILoggerFactory.configureAndWatch(MASTestSwitches.currentMasDir() + fullPath);
                log = ILoggerFactory.getILogger(ApplicationBasicTestCase.class);
                log.info("MOCK: Log initialized using config "+ logFileName);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new RuntimeException("Error while trying to initialize testcase !",t);
            }

        }
    }

    public ApplicationBasicTestCase(String event) {
        this(event, "test_log.xml");
    }

    /**
     * Returns with information to the junit why things did not go well
     *
     * @param reason The reason why the test fails.
     */
    public static void die(String reason) {
        if (Tools.isTrueProperty(System.getProperty("com.mobeon.junit.unimplemented.ignore"))) {
            String reasonLc = reason.toLowerCase();
            if (! (reasonLc.contains("not implemented") ||
                    reasonLc.contains("not fully implemented")))
                fail(reason);
        } else {
            fail(reason);
        }
    }

    /**
     * Sets up the a,b and c numbers as a default redirection.
     *
     * @param a
     * @param b
     * @param c
     */
    protected void setupABCInformation(CallingParty a, CalledParty b, RedirectingParty c) {
    	setupAInformation(a, aNumber);
    	setupBInformation(b, bNumber);
    	setupCInformation(c, cNumber);
    }
    
    protected void setupAInformation(CallingParty a, String aNum){
    	CallMock.setupANumber(a, aNum, a_party_presentationIndicator, a_party_completion);
    }

    protected void setupBInformation(CalledParty b, String bNum){
    	CallMock.setupBNumber(b, bNum);
    }

    protected void setupCInformation(RedirectingParty c, String cNum){
    	CallMock.setupCNumber(c, cNum, c_party_presentationIndicator, redirectingReason);
    }

    /**
     * Sets up an inbound call, starts the service specified and returns with the inbound
     * call as soon as it is created. The call can then be used for recording, sending
     * DTMF etc. To wait for the call to finish use the waitForExecutionToFinish on the call
     * object.
     *
     * @param service The name of the service to start.
     * @return The inboundcall.
     */

    protected InboundCallMock createCall(String service) {
        // Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up this tests calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        RedirectingParty c = new RedirectingParty();
        setupABCInformation(a, b, c);

        // Make the call
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, c, callType, mimeType, inboundBitRate);

        // It has been started, return with the call object.
        return icm;
    }
    
    protected SubscribeCallMock createSubscribe(String service) {
        // Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up this tests calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        RedirectingParty c = new RedirectingParty();
        setupABCInformation(a, b, c);

        // Make the call
        SubscribeCallMock icm = callMgr.createSubscribeCall(service, a, b, c, callType, mimeType, inboundBitRate);

        // It has been started, return with the call object.
        return icm;
    }
    

    protected ServiceRequestRunner createServiceRequestRunner(String service) {
        // Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        ServiceRequestRunner r = serviceMgr.createRunner(service);

        // It has been started, return with the call object.
        return r;
    }

    /**
     * Sets up an inbound call and starts the service listed. It uses default numbers and
     * passes on the A,B and C numbers to the application. It waits for the application to
     * terminate.
     *
     * @param service The name of the service to run, must match the testcases given.
     * @param timeout The maximum number of milliseconds to wait for a testcase to finish.
     * @return True if the application terminated, false if it timed out.
     * @deprecated Please use createCallAndWaitForCompletion() instead.
     */
    protected boolean setupAndMakeCall(String service, long timeout) {
        return createCallAndWaitForCompletion(service, timeout);

    }

    public boolean createCallAndWaitForCompletion(String testCase) {
        return createCallAndWaitForCompletion(testCase, testCaseTimeout);
    }

    /**
     * Sets up an inbound call and starts the service listed. It uses default numbers and
     * passes on the A,B and C numbers to the application. It waits for the application to
     * terminate.
     *
     * @param service The name of the service to run, must match the testcases given.
     * @param timeout The maximum number of milliseconds to wait for a testcase to finish.
     * @return True if the application terminated, false if it timed out.
     */
    protected boolean createCallAndWaitForCompletion(String service, long timeout) {
        // Start saving the log
        TestAppender.clear();
        log.info("TESTCASE " + service + " STARTED ------------------------------------------------------------------------------");
        TestAppender.startSave();

        // Set up this tests calling parameters
        CallingParty a = new CallingParty();
        CalledParty b = new CalledParty();
        RedirectingParty c = new RedirectingParty();
        setupABCInformation(a, b, c);

        // Make the call
        InboundCallMock icm = callMgr.createInboundCall(service, a, b, c, callType, mimeType, inboundBitRate);
        boolean exited = waitForCallCompletion(icm, timeout);
        
        // Stop the saving of the log, we do not need anymore
        // for the analysis
        TestAppender.stopSave(log);

        // Return with the flag
        return exited;
    }
    
    public boolean waitForCallCompletion(InboundCallMock icm, long timeout){
    	icm.startCall();
        boolean exited = icm.waitForExecutionToFinish(timeout);
        icm.terminateService();

        // Sleep may be needed, otherwise log is stopped before all events are sent to log
        if (sleepBeforeStopSaveLog != 0) {
            try {
                Thread.sleep(sleepBeforeStopSaveLog);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return exited;
    }

    public static long configureTimeout(long timeout) {
        if (! MASTestSwitches.canTestTimeout()) {
            return 0;
        }

        Long newTimeout = Long.getLong("com.mobeon.junit.runapp.timeout");
        if (newTimeout != null) {
            timeout = newTimeout;
        }
        if (timeout > 0) {
            timeout = 500 + scale(timeout);
            log.info("Test will time-out after " + timeout + "ms");
        } else
            log.info("Test will never time-out ( timeout is set to: " + timeout + ")");
        return timeout;

    }

    public static void disableScaling() {
        scaling = false;
    }

    public static void enableScaling() {
        scaling = true;
    }

    public static long scale(long timeout) {
        return scaling ? MASTestSwitches.scale(timeout) : timeout;
    }

    /**
     * Per suite setup !
     */
    public static void oneTimeSetUp(ApplicationTestCase[] testCases) {
        log.info("MOCK: Setting up the test suite environment");
        enableScaling();
        setupEnvironment(testCases);
    }

    public void runBare() throws Throwable {
        beforeEachTest();
        try {
            super.runBare();
            doAfterEachTest();
        } catch (Throwable t) {
            doAfterEachTest();
            throw t;
        }
    }

    private void doAfterEachTest() {
        try {
            afterEachTest();
        } finally {
        }
    }

    protected void afterEachTest() {
        TestEventGenerator.declareNoWait();
        DelayedEventProcessor.reset();
        if (callMgr != null) {  //Check that we haven't torn down the environment already
            setDelayBeforeResponseToCreateCall(0);
            setDelayBeforeResponseToAccept(0);
            setDelayBeforeResponseToDisconnect(0);
            setDelayBeforeResponseToPlay(0);
            setOutboundCallEventAfterConnected(CallManagerMock.EventType.NONE, 0);
            setInboundFarEndConnections(makeEmptyInboundProp());
            setResponseToAccept(CallManagerMock.EventType.CONNECTED_EVENT);
            setResponseToRecord(CallManagerMock.EventType.RECORD_FINISHED);            
            setWithholdDisconnectAttempt(false);
            setWithholdJoinAttempt(false);
            setWithholdnegotiateEarlyMedia(false);
            setWithholdRejectAttempt(false);
            setWithholdsendSIPMessage(false);
            setWithholdUnjoinAttempt(false);
            setSendPlayFailedAfterDelay(false);
            setDelayBeforePlayFailed(0);
        }
    }

    protected void beforeEachTest() {
        TestEventGenerator.declareNoWait();
        enableScaling();
        MASTestSwitches.enableUnitTesting();
        TestAppender.clear();
        SessionInfoFactoryMock.reset();
        IdGeneratorImpl.resetAll();
        System.gc();
    }

    /**
     * Per suite teardown !
     */
    public static void oneTimeTearDown() {
        log.info("MOCK: Tearing down the test suite environment");
        teardownEnvironment();
        MASTestSwitches.reset();
        reset();
    }

    private static void reset() {
        callMgr = null;
        serviceMgr = null;
        applicationProtocol = "test";
        applicationRoot = "/test/com/mobeon/masp/execution_engine/runapp/applications/";
        testLanguage = "ccxml";
        testSubdir = "transition";
        executorService = ExecutorServiceManager.getInstance().getExecutorService(CallManagerMock.class);
        scaling = true;
        sessionInfoMock = null;
    }

    /**
     * Tear down the environment !
     */
    public static void teardownEnvironment() {
        callMgr = null;
        serviceMgr = null;
    }

    /**
     * Create the testbed for the testcases using the default component configuration file for
     * the testbed. It also replaces the service to application uri mapping to match the testcases.
     *
     * @param testCases List of all testcases (mapping from service to application URI)
     */
    public static void setupEnvironment(ApplicationTestCase[] testCases) {
        setupEnvironmentWithComponentConfig("/"+MASTestSwitches.currentMasDir() + "/test/com/mobeon/masp/execution_engine/runapp/test_componentconfig.xml",
                testCases);
    }

    /**
     * Create the testbed for the testcases using the given component configuration file. It also
     * replaces the service to application uri mapping to match the testcases.
     *
     * @param componentConfig Name of the component configuration file.
     * @param testCases       List of all testcases (mapping from service to application URI)
     */
    public static void setupEnvironmentWithComponentConfig(String componentConfig, ApplicationTestCase[] testCases) {

        // Save the output
        TestAppender.startSave();
        log.info("MOCK: Beginning startup...");

        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
            threadPoolExecutor.setCorePoolSize(30);
        }

        // Create an ComponentManager
        IComponentManager compManager = null;

        log.info("MOCK: Initiating the spring framework");
        log.info("MOCK: Using " + componentConfig);
        try {
            // Create our context
            FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(componentConfig);
            SpringComponentManager.initialApplicationContext(ctx);
            compManager = SpringComponentManager.getInstance();

        } catch (Exception e) {
            log.error("Failed to create ComponentManager, bailing out" + e.toString());
            e.printStackTrace();
            die("Error!" + e);
        }

        // First instanciate the ExecutorServiceManager in order to configure it
        log.info("MOCK: Creating the ExecutorService Manager");
        try {
            ExecutorServiceManager esm = (ExecutorServiceManager) compManager.create("ExecutorServiceManager", ExecutorServiceManager.class);
        } catch (Exception e) {
            log.error("Failed to create ExecutorServiceManager, bailing out", e);
            die("Error! " + e);
        }

        // Instantiate a CallManager
        log.info("MOCK: Creating the CallManager");
        try {
            callMgr = (CallManagerMock) compManager.create("CallManager", CallManager.class);
            callMgr.setExecutorService(executorService);
        } catch (BeansException e) {
            log.fatal("Could not create CallManager.", e);
            die("Error! " + e);
        }

        // Instantiate a SericeRequestManager
        log.info("MOCK: Creating the ServiceRequestManager");
        try {
            serviceMgr = (ServiceRequestManagerMock) compManager.create("ServiceRequestManager", IServiceRequestManager.class);
            serviceMgr.setExecutorService(executorService);
        } catch (BeansException e) {
            log.fatal("Could not create ServiceRequestManager.", e);
            die("Error! " + e);
        }

        // Replace the servicemap with the one required by the testclass
        log.info("MOCK: Replacing the service to application URI mapping");
        ApplicationManagmentImpl ami = (ApplicationManagmentImpl) callMgr.getApplicationManagment();
        Map<String, String> serviceMap = new HashMap<String, String>();
        for (ApplicationTestCase testCase : testCases)
            serviceMap.put(testCase.name, testCase.configurationFile);
        ami.setMapServiceToApplicationURI(serviceMap);

        // Inform the user that the MAS has started
        log.info("MOCK: Started!");
        
        
    }

    protected void rememberMimeType(MimeType m) {
        mimeType = m;
    }


    protected void setOutboundCallEventAfterConnected(CallManagerMock.EventType e, int milliSecondsUntilGeneration) {
        callMgr.setOutboundCallEventAfterConnected(e, milliSecondsUntilGeneration);
    }

    protected void setResponseToCreateCall(CallManagerMock.EventType e, Object extraData) {
        callMgr.setResponseToCreateCall(e, extraData);
    }

    public void setDelayBeforeResponseToCreateCall(int delayBeforeResponseToCreateCall) {
        callMgr.setDelayBeforeResponseToCreateCall(delayBeforeResponseToCreateCall);
    }

    public void setDelayBeforeResponseToDisconnect(int delayBeforeResponseToDisconnect) {
        callMgr.setDelayBeforeResponseToDisconnect(delayBeforeResponseToDisconnect);
    }

    public void setTestCaseTimeout(int testCaseTimeout) {
        this.testCaseTimeout = testCaseTimeout;
    }

    public void setEarlyMediaAtProgressing(boolean earlyMediaAtProgressing) {
        callMgr.setEarlyMediaInProgressing(earlyMediaAtProgressing);
    }

    public void setWithholdJoinAttempt(boolean withholdJoinAttempt) {
        callMgr.setWithholdJoinAttempt(withholdJoinAttempt);
    }

    public void setWithholdUnjoinAttempt(boolean withholdUnjoinAttempt) {
        callMgr.setWithholdUnjoinAttempt(withholdUnjoinAttempt);
    }

    public void setWithholdRejectAttempt(boolean withholdRejectAttempt) {
        callMgr.setWithholdRejectAttempt(withholdRejectAttempt);
    }

    public void setWithholdDisconnectAttempt(boolean withholdDisconnectAttempt) {
        callMgr.setWithholdDisconnectAttempt(withholdDisconnectAttempt);
    }

    public void setWithholdnegotiateEarlyMedia(boolean withholdnegotiateEarlyMedia) {
        callMgr.setWithholdnegotiateEarlyMedia(withholdnegotiateEarlyMedia);
    }

    public void setWithholdsendSIPMessage(boolean withholdsendSIPMessage) {
        callMgr.setWithholdsendSIPMessage(withholdsendSIPMessage);
    }

    public void setDelayBeforeResponseToAccept(int delayBeforeResponseToAccept) {
        callMgr.setDelayBeforeResponseToAccept(delayBeforeResponseToAccept);
    }

    public void setDelayBeforeResponseToPlay(int delayBeforeResponseToPlay) {
        callMgr.setDelayBeforeResponseToPlay(delayBeforeResponseToPlay);
    }

    public void setOutboundFarEndConnections(Set<Connection> outboundFarEndConnections) {
        callMgr.setOutboundFarEndConnections(outboundFarEndConnections);
    }

    public void setInboundFarEndConnections(Set<Connection> inboundFarEndConnections) {
        callMgr.setInboundFarEndConnections(inboundFarEndConnections);
    }

    public void setSipMessageResponseEvent(SipMessageResponseEvent sipMessageResponseEvent) {
        callMgr.setSipMessageResponseEvent(sipMessageResponseEvent);
    }

    public void setSendPlayFailedAfterDelay(boolean sendPlayFailedAfterDelay) {
        callMgr.setSendPlayFailedAfterDelay(sendPlayFailedAfterDelay);
    }

    public void setDelayBeforePlayFailed(int delayBeforePlayFailed) {
        callMgr.setDelayBeforePlayFailed(delayBeforePlayFailed);
    }
}