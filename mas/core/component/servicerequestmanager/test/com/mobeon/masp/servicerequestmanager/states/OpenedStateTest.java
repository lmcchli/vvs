package com.mobeon.masp.servicerequestmanager.states;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManagerController;

/**
 * @author mmawi
 */
public class OpenedStateTest extends MockObjectTestCase {
    private static final String LOG4J_CONFIGURATION = "mobeon_log.xml";
    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    private AdministrativeState openedState;
    private Mock mockedController;

    public void setUp() throws Exception {
        super.setUp();

        setupMockedController();

        openedState = new OpenedState((IServiceRequestManagerController)mockedController.proxy());
    }

    // ===== Test cases =====

    public void testCloseForcedWhenNoSessions() throws Exception {
        mockedController.expects(once())
                .method("getCurrentSessions")
                .will(returnValue(0));
        mockedController.expects(once())
                .method("setClosedState");
        mockedController.expects(once())
                .method("closeCompleted");
        openedState.closeForced();
    }

    public void testCloseForcedWhenSessionsAdded() throws Exception {
        mockedController.expects(once())
                .method("getCurrentSessions")
                .will(returnValue(3));
        mockedController.expects(once())
                .method("setClosingForcedState");
        openedState.closeForced();
    }

    public void testCloseUnforcedWhenNoSessions() throws Exception {
        mockedController.expects(once())
                .method("getCurrentSessions")
                .will(returnValue(0));
        mockedController.expects(once())
                .method("setClosedState");
        mockedController.expects(once())
                .method("closeCompleted");
        openedState.closeUnforced();
    }

    public void testCloseUnforcedWhenSessionsAdded() throws Exception {
        mockedController.expects(once())
                .method("getCurrentSessions")
                .will(returnValue(3));
        mockedController.expects(once())
                .method("setClosingUnforcedState");
        openedState.closeUnforced();
    }

    public void testOpen() throws Exception {
        mockedController.expects(once())
                .method("openCompleted");
        openedState.open();
    }

    public void testRemoveSession() throws Exception {
        mockedController.expects(never());
        openedState.removeSession();
    }

    // ===== End of test cases =====

    private void setupMockedController() {
        mockedController = mock(IServiceRequestManagerController.class);
    }
}
