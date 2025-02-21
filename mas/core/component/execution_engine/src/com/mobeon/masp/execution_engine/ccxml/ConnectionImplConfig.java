package com.mobeon.masp.execution_engine.ccxml;

import static com.mobeon.masp.execution_engine.util.TestEventGenerator.*;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.configuration.*;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 11, 2006
 * Time: 1:26:45 PM
 * To change this template use File | Settings | File Templates.
 */

@ConfigurationParameters({ParameterId.ConnectionImpl_AcceptTimeout,
        ParameterId.ConnectionImpl_CallManagerWaitTime,
        ParameterId.ConnectionImpl_createCallAdditionalTimeout,
        ParameterId.ConnectionImpl_toManyConnectionsThreshold})
public class ConnectionImplConfig extends Configurable {

    private static ParameterBlock parameterBlock = new ParameterBlock();
    private long acceptTimeout;
    private int callManagerWaitTime;
    private int createCallAdditionalTimeout;
    private int toManyConnectionsTreshold;

    static ILogger logger = ILoggerFactory.getILogger(ConnectionImplConfig.class);

    public ConnectionImplConfig(IConfigurationManager manager) {
        acceptTimeout = readInteger(manager, ParameterId.ConnectionImpl_AcceptTimeout, RuntimeConstants.CONFIG.ACCEPT_TIMEOUT, logger);
        callManagerWaitTime = readInteger(manager, ParameterId.ConnectionImpl_CallManagerWaitTime, RuntimeConstants.CONFIG.CALL_MANAGER_WAIT_TIME, logger);
        createCallAdditionalTimeout = readInteger(manager, ParameterId.ConnectionImpl_createCallAdditionalTimeout, RuntimeConstants.CONFIG.CREATECALL_ADDITIONAL_TIMEOUT, logger);


        if (isActive()) {
            generateEvent(TestEvent.CONNECTION_CONFIG_ACCEPT_TIMEOUT,acceptTimeout);
            generateEvent(TestEvent.CONNECTION_CONFIG_CALLMGR_WAIT,callManagerWaitTime);
            generateEvent(TestEvent.CONNECTION_CONFIG_CREATECALL_ADDITIONAL,createCallAdditionalTimeout);
        }
    }

    @IntegerParameter(
            description = "Maximum time for the application to invoke <accept> after connection.alerting has been delivered",
            displayName = "Accept timeout (millisecs)",
            configName = RuntimeConstants.CONFIG.ACCEPT_TIMEOUT,
            parameter = ParameterId.ConnectionImpl_AcceptTimeout,
            min = 5000,
            max = 120000,
            defaultValue = 60000)
    public void setAcceptTimeout(int acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }


    public ParameterBlock getParameterBlock() {
        return parameterBlock;
    }

    long getAcceptTimeout() {
        return acceptTimeout;
    }

    public int getCallManagerWaitTime() {
        return callManagerWaitTime;
    }

    public int getCreateCallAdditionalTimeout() {
        return createCallAdditionalTimeout;
    }
}


