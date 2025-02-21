package com.mobeon.masp.callmanager.registration;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.jmock.core.Constraint;
import com.mobeon.masp.callmanager.sip.SipMessageSender;
import com.mobeon.masp.callmanager.sip.SipStackWrapper;
import com.mobeon.masp.callmanager.sip.message.SipRequestFactory;
import com.mobeon.masp.callmanager.sip.message.SipRequest;
import com.mobeon.masp.callmanager.sip.message.SipRequestMock;
import com.mobeon.masp.callmanager.registration.states.RegistrationState;
import com.mobeon.masp.callmanager.registration.states.UnregisteredState;
import com.mobeon.masp.callmanager.registration.states.RegisteredState;
import com.mobeon.masp.callmanager.registration.states.RegisteringState;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.callhandling.OutboundHostPortUsage;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.configuration.ConfigurationManagerImpl;

import javax.sip.SipException;
import javax.sip.header.CallIdHeader;

/**
 * SspInstance Tester.
 *
 * @author Malin Flodin
 */
public class SspInstanceTest extends MockObjectTestCase
{
    private static final int TIMEOUT_IN_MILLI_SECONDS = 10000;
    private ILogger log = ILoggerFactory.getILogger(getClass());

    protected static final Boolean FAIL = true;
    protected static final Boolean SUCCEED = false;
    protected static final int EXPIRE_TIME_OK = 2;
    protected static final int EXPIRE_TIME_MALFORMED = 0;
    protected static final int BACKOFF_TIMER = 2000 * EXPIRE_TIME_OK;
    protected static final int SIP_ERROR_CODE = 403;

    private static final String SSP_HOST = "ssp1.mobeon.com";
    private static final int SSP_PORT = 5060;

    protected SspInstance sspInstance = null;
    protected RegistrationDispatcher registrationDispatcher = null;
    protected Mock mockSipStackWrapper;
    protected Mock mockSipMessageSender;
    protected Mock mockSipRequestFactory;
    protected Mock mockCallIdHeader;

    protected int cSeq;

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    public SspInstanceTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();

        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile(CallManagerTestContants.CALLMANAGER_XML);
        ConfigurationReader.getInstance().setInitialConfiguration(
                cm.getConfiguration());
        ConfigurationReader.getInstance().update();
        ConfigurationReader.getInstance().getConfig().
                setRegisterBackoffTimer(BACKOFF_TIMER);

        cSeq = 1;

        mockSipStackWrapper = mock(SipStackWrapper.class);
        mockSipMessageSender = mock(SipMessageSender.class);
        mockSipRequestFactory = mock(SipRequestFactory.class);
        mockCallIdHeader = mock(CallIdHeader.class);
        setupExpectations();

        registrationDispatcher = new RegistrationDispatcher();

        // Create a CMUtils instance with mock objects
        CMUtils cmUtils = CMUtils.getInstance();
        cmUtils.setSipStackWrapper((SipStackWrapper)mockSipStackWrapper.proxy());
        cmUtils.setSipMessageSender((SipMessageSender)(mockSipMessageSender.proxy()));
        cmUtils.setSipRequestFactory((SipRequestFactory)(mockSipRequestFactory.proxy()));
        cmUtils.setRegistrationDispatcher(registrationDispatcher);

        sspInstance = new SspInstance(new RemotePartyAddress(SSP_HOST, SSP_PORT));
    }

    public void tearDown() throws Exception {
        super.tearDown();
        sspInstance.cancelRegisterTimer();
        sspInstance.cancelBackoffTimer();
        CMUtils.getInstance().delete();
        OutboundHostPortUsage.getInstance().clear();
        SspStatus.getInstance().clear();
        registrationDispatcher.clearOngoingRegistrations();
    }

    /**
     * Verifies that creating an {@link SspInstance} results in
     * IllegalArgumentException if any argument is null.
     * @throws Exception if the test case fails.
     */
    public void testConstructor() throws Exception {

        // Set sspHost to null
        try {
            new SspInstance(null);
            fail("Exception not thrown when expected");
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage()) ;
        }
    }

    //=========================== Protected Methods =====================

    protected void expectRegisterSent(int cSeq, boolean shallFail) {
        SipRequest sipRequest = new SipRequestMock().getSipRequest();

        Constraint[] constraints = new Constraint[] {
                eq("mas"), eq(SSP_HOST), eq(SSP_PORT), eq(cSeq), NOT_NULL, NULL};
        mockSipRequestFactory.expects(once()).method("createRegisterRequest").
                with(constraints).will(returnValue(sipRequest));
        if (shallFail) {
            mockSipMessageSender.expects(once()).method("sendRequest").
                    with(eq(sipRequest)).
                    will(throwException(new SipException()));
        } else {
            mockSipMessageSender.expects(once()).method("sendRequest").
                    with(eq(sipRequest));
        }
    }

    protected void expectUnregisterSent(int cSeq, boolean shallFail) {
        SipRequest sipRequest = new SipRequestMock().getSipRequest();

        Constraint[] constraints = new Constraint[] {
                eq("mas"), eq(SSP_HOST), eq(SSP_PORT), eq(cSeq), NOT_NULL, eq(0)};
        mockSipRequestFactory.expects(once()).method("createRegisterRequest").
                with(constraints).will(returnValue(sipRequest));
        if (shallFail) {
            mockSipMessageSender.expects(once()).method("sendRequest").
                    with(eq(sipRequest)).
                    will(throwException(new SipException()));
        } else {
            mockSipMessageSender.expects(once()).method("sendRequest").
                    with(eq(sipRequest));
        }
    }

    protected void assertPendingRegister(boolean pendingRegister) {
        waitForPendingRegister(pendingRegister);
    }

    protected void assertPendingUnregister(boolean pendingUnregister) {
        waitForPendingUnregister(pendingUnregister);
    }

    protected void assertCurrentState(Class expectedState) {
        waitForState(expectedState);
        if ((expectedState == UnregisteredState.class) ||
            (expectedState == RegisteredState.class)) {
            assertEquals(0, registrationDispatcher.amountOfOngoingRegistrations());
        } else if (expectedState == RegisteringState.class) {
            assertTrue(
                    (registrationDispatcher.amountOfOngoingRegistrations() == 0) ||
                    (registrationDispatcher.amountOfOngoingRegistrations() == 1));
        } else {
            assertEquals(1, registrationDispatcher.amountOfOngoingRegistrations());
        }
    }

    protected void waitForPendingUnregister(boolean pendingUnregister) {
        long startTime = System.currentTimeMillis();

        while ((pendingUnregister != sspInstance.isUnregisterPending()) &&
            (System.currentTimeMillis() <
                    (startTime + TIMEOUT_IN_MILLI_SECONDS))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "pending unregister " + pendingUnregister + ".", e);
                return;
            }
        }

        if (pendingUnregister != sspInstance.isUnregisterPending()) {
            fail("Timed out when waiting for pending unregister " +
                    pendingUnregister + ". ");
        }
    }

    protected void waitForPendingRegister(boolean pendingRegister) {
        long startTime = System.currentTimeMillis();

        while ((pendingRegister != sspInstance.isRegisterPending()) &&
            (System.currentTimeMillis() <
                    (startTime + TIMEOUT_IN_MILLI_SECONDS))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "pending register " + pendingRegister + ".", e);
                return;
            }
        }

        if (pendingRegister != sspInstance.isRegisterPending()) {
            fail("Timed out when waiting for pending register " +
                    pendingRegister + ". ");
        }
    }

    protected void waitForState(Class expectedState) {
        long startTime = System.currentTimeMillis();

        while (!expectedState.isInstance(sspInstance.getCurrentState()) &&
            (System.currentTimeMillis() <
                    (startTime + TIMEOUT_IN_MILLI_SECONDS))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "state " + expectedState + ".", e);
                return;
            }
        }

        RegistrationState state = sspInstance.getCurrentState();
        if (!expectedState.isInstance(state)) {
            fail("Timed out when waiting for state " + expectedState +
                ". Current state is " + state.getClass().getName());
        }
    }

    //=========================== Private Methods =====================

    private void setupExpectations() throws Exception {
        mockSipStackWrapper.stubs().method("getNewCallId").
                will(returnValue(mockCallIdHeader.proxy()));
    }

}
