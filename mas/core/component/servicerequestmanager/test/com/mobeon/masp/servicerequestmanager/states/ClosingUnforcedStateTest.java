package com.mobeon.masp.servicerequestmanager.states;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManagerController;

/**
 * @author mmawi
 */
public class ClosingUnforcedStateTest extends MockObjectTestCase {
    private static final String LOG4J_CONFIGURATION = "mobeon_log.xml";
    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    private AdministrativeState closingUnforcedState;
    private Mock mockedController;

    public void setUp() throws Exception {
        super.setUp();

        setupMockedController();

        closingUnforcedState = new ClosingUnforcedState((IServiceRequestManagerController)mockedController.proxy());
    }

    // ===== Test cases =====

    public void testCloseForced() throws Exception {
        mockedController.expects(once())
                .method("setClosingForcedState");
        closingUnforcedState.closeForced();
    }

    public void testCloseUnforced() throws Exception {
        mockedController.expects(never());
        closingUnforcedState.closeUnforced();
    }

    public void testOpen() throws Exception {
        mockedController.expects(once())
                .method("setOpenedState");
        mockedController.expects(once())
                .method("openCompleted");
        closingUnforcedState.open();
    }

    public void testRemoveSessionWhenNoSessions() throws Exception {
        mockedController.expects(once())
                .method("getCurrentSessions")
                .will(returnValue(0));
        mockedController.expects(once())
                .method("setClosedState");
        mockedController.expects(once())
                .method("closeCompleted");
        closingUnforcedState.removeSession();
    }

    public void testRemoveSessionWhenSessionsAdded() throws Exception {
        mockedController.expects(once())
                .method("getCurrentSessions")
                .will(returnValue(3));
        closingUnforcedState.removeSession();
    }

    // ===== End of test cases =====

    private void setupMockedController() {
        mockedController = mock(IServiceRequestManagerController.class);
    }
}
