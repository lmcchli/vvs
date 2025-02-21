/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.callmanager;

import com.mobeon.masp.callmanager.CallManagerImpl;
import com.mobeon.masp.callmanager.CMUtils;
import com.mobeon.masp.callmanager.component.environment.EnvironmentConstants;
import com.mobeon.masp.callmanager.component.environment.system.SystemSimulator;
import com.mobeon.masp.callmanager.states.OpenedState;
import com.mobeon.masp.callmanager.states.AdministrativeState;
import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * TODO: Document
 * @author Malin Nyfeldt
 */
public class CallManagerToVerify {

    /** A logger instance. */
    private static final ILogger log =
            ILoggerFactory.getILogger(CallManagerToVerify.class);

    /** An instance of callmanager, i.e. the system under test. */
    private final CallManagerImpl   callManager;

    /** The host and port on which Call Manager will listen. */
    private final String            hostAddress;
    private final int               port;

    /** Simulator of the rest of the MAS system. */
    private final SystemSimulator   simulatedSystem;

    private final IConfiguration    configuration;

    static {
    }

    /**
     * Creates and sets up a new Call Manager instance to use for component
     * tests.
     *
     * @param hostAddress       The host address on which Call Manager will
     *                          listen.
     * @param port              The port on which Call Manager will listen.
     * @param configuration     The name of the configuration file to use.
     * @param simulatedSystem   Simulates the rest of the MAS system. It is
     *                          necessary in order to initiate call manager
     *                          with for example a Supervision instance and
     *                          a stream factory.
     */
    public CallManagerToVerify(String hostAddress, int port,
                               String configuration,
                               SystemSimulator simulatedSystem)
            throws ServiceEnablerException {

        this.simulatedSystem = simulatedSystem;
        this.hostAddress = hostAddress;
        this.port = port;

        // Create a configuration manager and read the configuration file
        this.configuration = readConfiguration(configuration);

        // Create and initiate call manager
        callManager = createCallManager();

        // Wait for call manager to be opened
        waitForAdminState(OpenedState.class);
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    // TODO: Remove later!!!
    public CallManagerImpl getCallManager() {
        return callManager;
    }

    public void delete() throws ServiceEnablerException {
        callManager.delete();
    }

    public void waitForAdminState(Class expectedState) {
        long startTime = System.currentTimeMillis();

        while (!expectedState.isInstance(getCurrentAdminState()) &&
            (System.currentTimeMillis() <
                    (startTime + EnvironmentConstants.TIMEOUT_IN_MILLI_SECONDS))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "admin state " + expectedState + ".", e);
                return;
            }
        }

        AdministrativeState state = getCurrentAdminState();
        if (!expectedState.isInstance(state)) {
            throw new RuntimeException(
                    "Timed out when waiting for admin state " + expectedState +
                            ". Current state is " + state.getClass().getName());
        }
    }



    // ===================== Private methods =====================

    private IConfiguration readConfiguration(String configuration) {
        ConfigurationManagerImpl configMgr = new ConfigurationManagerImpl();
        configMgr.setConfigFile(configuration);
        return configMgr.getConfiguration();
    }

    /**
     * This method is used to create and set-up a CallManager instance.
     * A Call Manager instance is created and required information is retrieved
     * from <code>configMgr</code> and <code>simulatedClient</code> and set in
     * the Call Manager instance before initiating it.
     * <p>
     * After required variables has been set, the call manager service is
     * initiated to operate on the given <code>callmanagerHost</code> and
     * <code>callmanagerPort</code>.
     * <p>
     * Finally the threshold is set and call manager is opened for operation.
     *
     * @return  The created and for operation opened call manager instance is
     *          returned.
     * @throws  ServiceEnablerException
     *          Throws {@link ServiceEnablerException} if the
     *          call manager service could not be initiated.
     */
    private CallManagerImpl createCallManager() throws ServiceEnablerException {

        // Create call manager
        CallManagerImpl callManager = new CallManagerImpl();

        // Set all necessary parameters
        callManager.setApplicationManagment(
                simulatedSystem.getApplicationManagement());
        callManager.setStreamFactory(simulatedSystem.getStreamFactory());
        callManager.setSupervision(simulatedSystem.getSupervision());
        callManager.setConfiguration(configuration.getConfiguration());
        callManager.setEventDispatcher(simulatedSystem.getEventDispatcher());
        callManager.setSessionFactory(simulatedSystem.getSessionFactory());
        callManager.setCallManagerLicensing(simulatedSystem.getCallManagerLicensingMock());

        // Initiate call manager service
        ServiceEnablerOperate cmOperate = callManager.initService(
                "Default", hostAddress, port);

        // Set threshold and open call manager for operation
        cmOperate.updateThreshold(40, 20, 50);
        cmOperate.open();

        return callManager;
    }


    private static AdministrativeState getCurrentAdminState() {
        return CMUtils.getInstance().getCmController().getCurrentState();
    }



}
