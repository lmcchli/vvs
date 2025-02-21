/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit.diagnoseservice;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.diagnoseservice.DiagnoseServiceImpl;
import com.mobeon.masp.callmanager.diagnoseservice.DiagnoseServiceConfiguration;
import com.mobeon.masp.callmanager.component.environment.sipunit.PhoneSimulator;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.ExperiencedOperationalStatus;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.masp.operateandmaintainmanager.Status;
import com.mobeon.masp.operateandmaintainmanager.ServiceInstance;

import junit.framework.TestCase;

import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.RequestEvent;
import javax.sip.SipProvider;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Properties;

import org.cafesip.sipunit.SipStack;

/**
 * Base class used to setup necessary for diagnose service tests using SipUnit.
 * It contains a DiagnoseService and a SimulatedPhone used to simulate the
 * Call Manager behavior.
 *
 * @author Malin Flodin
 */
public abstract class DiagnoseServiceCase extends TestCase {

    // ===================== Constants =====================

    // Toggle this to enable/disable logging of the SIP UNIT stack
    private static final boolean doLogging = false;

    /** Timeout that shall be used when the test case is waiting for a specific
     * input. If the input is not received within this time, the test case
     * should fail. */
    private static final int TIMEOUT_IN_MILLI_SECONDS = 10000;


    // Constants that indicates whether a not to send a response

    /** When in test cases indicating that a resposne should be sent,
     * use this contant. It is used to make test cases more readable. */
    protected static final boolean SEND_RESPONSE = true;

    /** When in test cases indicating that a resposne should NOT be sent,
     * use this contant. It is used to make test cases more readable. */
    protected static final boolean DO_NOT_SEND_RESPONSE = false;


    // ===================== Variables =====================

    /** A logger instance. */
    private final ILogger log = ILoggerFactory.getILogger(getClass());


    // CallManager related

    /** An instance of diagnose service, i.e. the system under test. */
    protected DiagnoseServiceImpl diagnoseService;

    /** A service instance used for a diagnose request. */
    protected ServiceInstance serviceInstance;


    // Simulated phones related

    /** The name of the SIP UNIT stack. Used at creation. */
    private static final String STACK_NAME = "SipUnitStack";

    /** The SIP UNIT stack used in the simulated phones.*/
    private static SipStack sipStack;

    /** A simulated phone.*/
    protected PhoneSimulator simulatedPhone;

    /** The user name in SIP URIs for the first simulated phone. */
    private static final String PHONE_USER = "sipPhone";


    private final AtomicBoolean testCaseException = new AtomicBoolean();

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    public final void setUp() throws Exception {
        System.gc();

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl configMgr = new ConfigurationManagerImpl();
        configMgr.setConfigFile(CallManagerTestContants.CALLMANAGER_XML);

        // Create and initialize the call manager's diagnose service
        diagnoseService = new DiagnoseServiceImpl();
        diagnoseService.setConfiguration(configMgr.getConfiguration());
        diagnoseService.init();
        CMUtils.getInstance().setSipHeaderFactory(diagnoseService.getSipHeaderFactory());

        // Create the simulated phone which in this case represents the
        // "normal" callmanager process.
        RemotePartyAddress diagnoseServiceAddress = new RemotePartyAddress(
                InetAddress.getByName(
                        DiagnoseServiceConfiguration.getInstance().getHostName()).
                        getHostAddress(),
                DiagnoseServiceConfiguration.getInstance().getPort());
        RemotePartyAddress callmanagerAddress = new RemotePartyAddress(
                "localhost", 5060);
        Properties properties = setupSipUnitStackConfiguration(
                callmanagerAddress.getHost());
        sipStack = new SipStack("udp", callmanagerAddress.getPort(), properties);
        simulatedPhone = new PhoneSimulator(sipStack,
                PHONE_USER, callmanagerAddress,
                "diagnosing", diagnoseServiceAddress,
                TIMEOUT_IN_MILLI_SECONDS);
        simulatedPhone.create();

        // Create the service instance
        serviceInstance = new ServiceInstance();
        serviceInstance.setHostName(PHONE_USER + "@" + callmanagerAddress.getHost());
        serviceInstance.setPort(callmanagerAddress.getPort());

        testCaseException.set(false);
    }

    public final void tearDown() throws Exception {
        diagnoseService.delete();
        simulatedPhone.delete();
        sipStack.dispose();
    }


    protected final void waitForOptions(boolean sendResponse, Status status) {
        ExecutorServiceManager.getInstance().
                getExecutorService(DiagnoseServiceCase.class).
                execute(new OptionsProcessor(sendResponse, status));
    }

    private void processOptions(boolean sendResponse, Status status) {
        try {
            RequestEvent requestEvent = assertOptionsReceived();
            if (sendResponse) {
                Response response = simulatedPhone.createOkResponse(requestEvent);
                SipResponse sipResponse =
                        new SipResponse(response, requestEvent.getServerTransaction(),
                                (SipProvider)requestEvent.getSource());
                switch(status) {
                    case UP:
                        sipResponse.addOperationalStatusHeader(
                                ExperiencedOperationalStatus.UP);
                        break;
                    case DOWN:
                        sipResponse.addOperationalStatusHeader(
                                ExperiencedOperationalStatus.DOWN);
                        break;
                    case IMPAIRED:
                        sipResponse.addOperationalStatusHeader(
                                ExperiencedOperationalStatus.IMPAIRED);
                        break;
                }
                simulatedPhone.sendResponse(requestEvent, sipResponse.getResponse());
            }
        } catch (Exception e) {
            testCaseException.set(true);
            log.error("Test case failed due to exception.", e);
        }

    }

    /**
     * This class is used only to simplify the code. It is used to process an
     * options request and respond to it according to indicated parameters.
     * Implements Runnable.
     * @author Malin Flodin
     */
    public final class OptionsProcessor implements Runnable {
        private final boolean sendResponse;
        private final Status status;

        public OptionsProcessor(boolean sendResponse, Status status) {
            this.sendResponse = sendResponse;
            this.status = status;
        }

        public void run() {
            processOptions(sendResponse, status);
        }
    }

    private RequestEvent assertOptionsReceived() {
        // Wait for OPTIONS request
        return simulatedPhone.assertRequestReceived(Request.OPTIONS, false, true);
    }

    protected final void assertNoError() {
        assertFalse("Exception occurred in test case.", testCaseException.get());
    }

    /**
     * This method is used to set-up configuration properties for the SIP UNIT
     * stack.
     * @return  Returns the configuration parameters to use when creating a
     *          SIP UNIT stack.
     */
    private static Properties setupSipUnitStackConfiguration(String host) {
        Properties properties = new Properties();

        properties.setProperty("javax.sip.IP_ADDRESS", host);
        properties.setProperty("javax.sip.STACK_NAME", STACK_NAME);
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");

        if (doLogging) {
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
                    "true");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "simulatedPhone_sipstacklog.txt");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "simulatedPhone_sipstackdebug.txt");
            properties.setProperty("gov.nist.javax.sip.BAD_MESSAGE_LOG",
                    "simulatedPhone_sipstackbadmessages.txt");
        }

        return properties;
    }

}
