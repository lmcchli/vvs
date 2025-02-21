package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 13, 2006
 * Time: 9:35:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class RuntimeConstants {

    public class CONFIG {
        public static final String GROUP_NAME = CommonOamManager.MAS_SPECIFIC_CONF;
        public static final String ENGINE_STACK_SIZE = "executionEngineStackSize";
        public static final String TRACE_ENABLED = "executionEngineTraceEnabled";
        public static final String ALWAYS_COMPILE = "executionEngineAlwaysCompile";
        public static final String GENERATE_OPS = "executionEngineGenerateOps";
        public static final String OPSPATH = "executionEngineOpsPath";
        public static final String HOSTNAME = "executionEngineHostname";
        public static final String CALL_MANAGER_WAIT_TIME = "executionEngineCallManagerWaitTime";
        public static final String ACCEPT_TIMEOUT = "executionEngineAcceptTimeout";
        public static final String CREATECALL_ADDITIONAL_TIMEOUT = "executionEngineCreateCallAdditionalTimeout";
        public static final String WATCHDOG_TIMEOUT = "executionEngineWatchdogTimeout";
        public static final String ENGINE_VXML_POOL_SIZE = "executionEngineVxmlPoolSize";
        public static final String ENGINE_CCXML_POOL_SIZE = "executionEngineCcxmlPoolSize";
        public static final String ALERT_SC_REGISTRATION_NUMBER_OF_RETRY = "alertSCRegistrationNumOfRetry";
        public static final String ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY  = "alertSCRegistrationTimeInSecBetweenRetry";
        public static final int ALERT_SC_REGISTRATION_NUMBER_OF_RETRY_DEFAULT_VALUE = 30;
        public static final int ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY_DEFAULT_VALUE  = 2;

    }
}
