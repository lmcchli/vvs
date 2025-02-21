package com.mobeon.masp.servicerequestmanager;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.masp.servicerequestmanager.states.OpenedState;
import com.mobeon.masp.servicerequestmanager.states.ClosedState;
import com.mobeon.masp.servicerequestmanager.states.ClosingUnforcedState;
import com.mobeon.masp.servicerequestmanager.states.ClosingForcedState;
import com.mobeon.masp.servicerequestmanager.events.ServiceClosed;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo;
import com.mobeon.masp.operateandmaintainmanager.CallType;
import com.mobeon.masp.operateandmaintainmanager.CallDirection;
import com.mobeon.masp.operateandmaintainmanager.CallResult;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.eventnotifier.IEventDispatcher;

/**
 * @author mmawi
 */
public class ControllerTest extends MockObjectTestCase {
    private static final String LOG4J_CONFIGURATION = "mobeon_log.xml";
    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    private Mock mockServiceEnablerInfo;
    private Mock mockEventDispatcher;
    private IServiceRequestManagerController controller;

    public void setUp() throws Exception {
        super.setUp();

        setupMockServiceEnablerInfo();
        mockEventDispatcher = mock(IEventDispatcher.class);

        controller = ServiceRequestManagerController.getInstance();
        controller.setServiceEnablerInfo((ServiceEnablerInfo) mockServiceEnablerInfo.proxy());
        controller.setEventDispatcher((IEventDispatcher) mockEventDispatcher.proxy());
        controller.clear();
    }

    public void testAddSession() throws Exception {
        // Ensure value
        assertEquals(0, controller.getCurrentSessions());

        // No session is added if threshold is 0
        assertThreshold(0);
        assertSessionAdded(false);
        assertEquals(0, controller.getCurrentSessions());

        // Sessions can be added up to threshold
        assertThreshold(3);
        assertSessionAdded(true);
        assertSessionAdded(true);
        assertSessionAdded(true);
        assertEquals(3, controller.getCurrentSessions());

        // No new session is added when threshold is reached
        assertSessionAdded(false);
        assertEquals(3, controller.getCurrentSessions());

        // The number of sessions is not affected by changed threshold
        assertThreshold(2);
        assertSessionAdded(false);
        assertEquals(3, controller.getCurrentSessions());
    }

    // ===== Test cases =====

    public void testOpenWhenClosed() throws Exception {
        gotoOpenedState();
        assertTrue(controller.getCurrentState() instanceof OpenedState);
    }

    public void testOpenWhenAlreadyOpen() throws Exception {
        gotoOpenedState();

        mockServiceEnablerInfo.expects(once())
                .method("opened");
        controller.open();
    }

    public void testCloseForcedWhenNoSessions() throws Exception {
        gotoOpenedState();

        assertEquals(0, controller.getCurrentSessions());
        mockServiceEnablerInfo.expects(once())
                .method("closed");
        controller.close(true);
        assertTrue(controller.getCurrentState() instanceof ClosedState);
    }

    public void testCloseForcedWhenSessionsAdded() throws Exception {
        gotoOpenedState();
        assertThreshold(50);

        assertSessionAdded(true);
        assertEquals(1, controller.getCurrentSessions());

        mockEventDispatcher.expects(once())
                .method("fireEvent")
                .with(isA(ServiceClosed.class));
        controller.close(true);
        assertTrue(controller.getCurrentState() instanceof ClosingForcedState);


        mockServiceEnablerInfo.expects(once())
                .method("closed");
        assertSessionRemoved();
        assertTrue(controller.getCurrentState() instanceof ClosedState);
    }

    public void testCloseForcedWhenAlreadyClosed() throws Exception {
        assertTrue(controller.getCurrentState() instanceof ClosedState);

        mockServiceEnablerInfo.expects(once())
                .method("closed");
        controller.close(true);
    }

    public void testCloseUnforcedWhenNoSessions() throws Exception {
        gotoOpenedState();

        assertEquals(0, controller.getCurrentSessions());
        mockServiceEnablerInfo.expects(once())
                .method("closed");
        controller.close(false);
        assertTrue(controller.getCurrentState() instanceof ClosedState);
    }

    public void testCloseUnforcedWhenSessionsAdded() throws Exception {
        gotoOpenedState();
        assertThreshold(50);

        assertSessionAdded(true);
        assertSessionAdded(true);
        assertEquals(2, controller.getCurrentSessions());

        controller.close(false);
        assertTrue(controller.getCurrentState() instanceof ClosingUnforcedState);
        assertEquals(2, controller.getCurrentSessions());

        assertSessionRemoved();
        assertTrue(controller.getCurrentState() instanceof ClosingUnforcedState);
        assertEquals(1, controller.getCurrentSessions());


        mockServiceEnablerInfo.expects(once())
                .method("closed");
        assertSessionRemoved();
        assertTrue(controller.getCurrentState() instanceof ClosedState);
        assertEquals(0, controller.getCurrentSessions());
    }

    public void testCloseUnforcedWhenAlreadyClosed() throws Exception {
        assertTrue(controller.getCurrentState() instanceof ClosedState);

        mockServiceEnablerInfo.expects(once())
                .method("closed");
        controller.close(false);
    }

    // ===== End of test cases =====

    private void assertThreshold(int i) throws Exception {
        mockServiceEnablerInfo.expects(once())
                .method("setMaxConnections")
                .with(eq(i));
        controller.updateThreshold(0, 0, i);
    }

    private void assertSessionAdded(boolean success) throws Exception {
        if (success) {
            mockServiceEnablerInfo.expects(once())
                    .method("incrementCurrentConnections")
                    .with(eq(CallType.SERVICE_REQUEST),
                            eq(CallDirection.INBOUND));
            mockServiceEnablerInfo.expects(once())
                    .method("incrementNumberOfConnections")
                    .with(eq(CallType.SERVICE_REQUEST),
                            eq(CallResult.CONNECTED),
                            eq(CallDirection.INBOUND));
            assertTrue(controller.addSession());
        } else {
            assertFalse(controller.addSession());
        }
    }

    private void assertSessionRemoved() throws Exception {
        mockServiceEnablerInfo.expects(once())
                .method("decrementCurrentConnections")
                .with(eq(CallType.SERVICE_REQUEST), eq(CallDirection.INBOUND));
        controller.removeSession();
    }

    private void gotoOpenedState() throws Exception {
        mockServiceEnablerInfo.expects(once())
                .method("opened");
        controller.open();
    }

    private void setupMockServiceEnablerInfo() {
        mockServiceEnablerInfo = mock(ServiceEnablerInfo.class);
        mockServiceEnablerInfo.stubs()
                .method("setProtocol");
        mockServiceEnablerInfo.stubs()
                .method("setMaxConnections");
        mockServiceEnablerInfo.stubs()
                .method("incrementNumberOfConnections");
        mockServiceEnablerInfo.stubs()
                .method("incrementCurrentConnections");
        mockServiceEnablerInfo.stubs()
                .method("decrementCurrentConnections");
        mockServiceEnablerInfo.stubs()
                .method("opened");
        mockServiceEnablerInfo.stubs()
                .method("closed");
    }


}
