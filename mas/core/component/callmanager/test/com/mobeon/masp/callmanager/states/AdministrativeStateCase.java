/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.states;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.masp.callmanager.CallManagerController;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.events.CloseForcedEvent;
import com.mobeon.masp.callmanager.events.OpenEvent;
import com.mobeon.masp.callmanager.events.CloseUnforcedEvent;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulator;
import com.mobeon.masp.callmanager.component.environment.system.mockobjects.CallManagerLicensingMock;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo;

/**
 * Base class for tests of Administrative states. Implements common methods for
 * tests various Administrative states.
 *
 * @author Malin Flodin
 */
public abstract class AdministrativeStateCase extends MockObjectTestCase {

    static {
        // Configure logger with the default log file found in callmanager dir
        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
    }

    protected Mock cmControllerMock = new Mock(CallManagerController.class);
    protected Mock serviceEnablerInfoMock = new Mock(ServiceEnablerInfo.class);

    protected CloseForcedEvent closeForcedEvent = new CloseForcedEvent();
    protected OpenEvent openEvent = new OpenEvent();
    protected CloseUnforcedEvent closeUnforcedEvent = new CloseUnforcedEvent();

    protected LoadRegulator loadRegulator;

    protected void setUp() throws Exception {
        super.setUp();
        CMUtils.getInstance().setCallManagerLicensing(new CallManagerLicensingMock());

        // Create a configuration manager and read the configuration file
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile(CallManagerTestContants.CALLMANAGER_XML);
        ConfigurationReader.getInstance().setInitialConfiguration(
                cm.getConfiguration());
        ConfigurationReader.getInstance().update();
        ConfigurationReader.getInstance().getConfig().setInitialRampHWM(50);
        ConfigurationReader.getInstance().getConfig().setRampFactor(1);
        CMUtils.getInstance().setServiceEnablerInfo(
                (ServiceEnablerInfo)serviceEnablerInfoMock.proxy());
        loadRegulator = new LoadRegulator();

        setupExpectations();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    protected void assertStateClosed() throws Exception {
        cmControllerMock.expects(once()).method("setClosedState");
    }

    protected void assertStateOpened() throws Exception {
        cmControllerMock.expects(once()).method("setOpenedState");
    }

    protected void assertStateClosingForced() throws Exception {
        cmControllerMock.expects(once()).method("setClosingForcedState");
    }

    protected void assertStateClosingUnforced() throws Exception {
        cmControllerMock.expects(once()).method("setClosingUnforcedRejectingState");
    }

    protected void assertStateClosingUnforcedRejecting() throws Exception {
        cmControllerMock.expects(once()).method("setClosingUnforcedRejectingState");
    }

    protected void assertLockingAllCalls() throws Exception {
        cmControllerMock.expects(once()).method("lockAllCalls");
    }

    protected void assertCloseCompleted() throws Exception {
        cmControllerMock.expects(once()).method("closeCompleted");
    }

    protected void assertOpenCompleted() throws Exception {
        cmControllerMock.expects(once()).method("openCompleted");
    }

    protected void assertStartClosingTimer() throws Exception {
        cmControllerMock.expects(once()).method("startClosingTimer");
    }

    protected void assertCancelClosingTimer() throws Exception {
        cmControllerMock.expects(once()).method("cancelClosingTimer");
    }

    protected void expectSspUnregistration() throws Exception {
        cmControllerMock.expects(once()).method("unregisterAllSsps");
    }

    protected void expectSspRegistration() throws Exception {
        cmControllerMock.expects(once()).method("registerAllSsps");
    }

    protected void expectSetMaxConnections(int threshold) throws Exception {
        serviceEnablerInfoMock.expects(once())
                .method("setMaxConnections")
                .with(eq(threshold));
    }

    // ===================== Private methods =========================

    private void setupExpectations() throws Exception {
    }

}
