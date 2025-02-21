/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import java.util.EnumMap;
import java.util.Map;

public class ParameterBlock {

    private boolean initialized;

    private Map<ParameterId, IntegerParameter> integerParameters = new EnumMap<ParameterId, IntegerParameter>(ParameterId.class);
    private Map<ParameterId, MapParameter> mapParameters = new EnumMap<ParameterId, MapParameter>(ParameterId.class);
    private Map<ParameterId, BooleanParameter> booleanParameters = new EnumMap<ParameterId, BooleanParameter>(ParameterId.class);
    private Map<ParameterId, StringParameter> stringParameters = new EnumMap<ParameterId, StringParameter>(ParameterId.class);

    public synchronized boolean isInitialized() {
        return initialized;
    }

    public synchronized void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public Map<ParameterId, IntegerParameter> getIntegerParameters() {
        return integerParameters;
    }

    public Map<ParameterId, MapParameter> getMapParameters() {
        return mapParameters;
    }

    public Map<ParameterId, BooleanParameter> getBooleanParameters() {
        return booleanParameters;
    }

    public Map<ParameterId, StringParameter> getStringParameters() {
        return stringParameters;
    }

    protected String getConfigParameterName(ParameterId parameter) {
        String result;
        switch (parameter.type) {
            case INTEGER:
                result = integerParameters.get(parameter).configName();
                break;
            case MAP:
                result = stringParameters.get(parameter).configName();
                break;
            case STRING:
                result = mapParameters.get(parameter).configName();
                break;
            case BOOLEAN:
                result = mapParameters.get(parameter).configName();
                break;
            default:
                result = null;
        }
        return result;
    }


    public String getDisplayName(ParameterId parameterId) {
        {
            IntegerParameter value = integerParameters.get(parameterId);
            if (value != null) return value.displayName();
        }
        {
            MapParameter value = mapParameters.get(parameterId);
            if (value != null) return value.displayName();
        }
        {
            BooleanParameter value = booleanParameters.get(parameterId);
            if (value != null) return value.displayName();
        }
        {
            StringParameter value = stringParameters.get(parameterId);
            if (value != null) return value.displayName();
        }
        return "<displaynamne unknown for: " + parameterId + '>';
    }

    public void copyFrom(ParameterBlock parameterBlock) {
        integerParameters = new EnumMap<ParameterId, IntegerParameter>(parameterBlock.integerParameters);
        mapParameters = new EnumMap<ParameterId, MapParameter>(parameterBlock.mapParameters);
        booleanParameters = new EnumMap<ParameterId, BooleanParameter>(parameterBlock.booleanParameters);
        stringParameters = new EnumMap<ParameterId, StringParameter>(parameterBlock.stringParameters);

    }
}
