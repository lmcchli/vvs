/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

@SuppressWarnings({"ClassWithTooManyFields"})
public enum ParameterId {
    Engine_StackSize(ParameterType.INTEGER),
    Engine_traceEnabled(ParameterType.BOOLEAN),
    ApplicationManagement_MapServiceToApplicationURI(ParameterType.MAP),
    ApplicationManagement_AlwaysCompile(ParameterType.BOOLEAN),
    ApplicationManagement_HostName(ParameterType.STRING),
    ApplicationManagement_WatchdogTimeout(ParameterType.INTEGER),
    ApplicationCompiler_MapExtensionToMimeType(ParameterType.MAP),
    ApplicationCompiler_GenerateOps(ParameterType.BOOLEAN),
    ApplicationCompiler_OpsPath(ParameterType.STRING),
    ConnectionImpl_CallManagerWaitTime(ParameterType.INTEGER),
    ConnectionImpl_AcceptTimeout(ParameterType.INTEGER),
    ConnectionImpl_createCallAdditionalTimeout(ParameterType.INTEGER),
    ConnectionImpl_toManyConnectionsThreshold(ParameterType.INTEGER),
    RuntimeFactoryBase_VxmlEnginePoolSize(ParameterType.INTEGER),
    RuntimeFactoryBase_CcxmlEnginePoolSize(ParameterType.INTEGER);

    public final ParameterType type;

    ParameterId(ParameterType type) {
        this.type = type;
    }

    public enum ParameterType {
        BOOLEAN,
        INTEGER,
        STRING,
        MAP,
    }
}
