package com.mobeon.masp.servicerequestmanager.states;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManagerController;

/**
 * @author mmawi
 */
public class ClosedStateTest extends MockObjectTestCase {
    private static final String LOG4J_CONFIGURATION = "mobeon_log.xml";
    static {
        // Initialize console logging
        // Sets the configuration file for the logging
        ILoggerFactory.configureAndWatch(LOG4J_CONFIGURATION);
    }

    private AdministrativeState closedState;
    private Mock mockedController;

    public void setUp() throws Exception {
        super.setUp();

        setupMockedController();

        closedState = new ClosedState((IServiceRequestManagerController)mockedController.proxy());
    }

    // ===== Test cases =====

    public void testCloseForced() throws Exception {
        mockedController.expects(once())
                .method("closeCompleted");
        closedState.closeForced();
    }

    public void testCloseUnforced() throws Exception {
        mockedController.expects(once())
                .method("closeCompleted");
        closedState.closeUnforced();
    }

    public void testOpen() throws Exception {
        mockedController.expects(once())
                .method("setOpenedState");
        mockedController.expects(once())
                .method("openCompleted");
        closedState.open();
    }

    public void testRemoveSession() throws Exception {
        mockedController.expects(never());
        closedState.removeSession();
    }

    // ===== End of test cases =====

    private void setupMockedController() {
        mockedController = mock(IServiceRequestManagerController.class);
    }

}
