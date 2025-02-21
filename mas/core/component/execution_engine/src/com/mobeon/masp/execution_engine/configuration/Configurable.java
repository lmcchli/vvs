/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public abstract class Configurable {

    public abstract ParameterBlock getParameterBlock();

    private static Map<Class<? extends Configurable>, ParameterBlock> defaultBlocks = new HashMap<Class<? extends Configurable>, ParameterBlock>();

    static ILogger logger = ILoggerFactory.getILogger(Configurable.class);

    protected synchronized void initialize() {

        ParameterBlock block = getParameterBlock();

        if (! block.isInitialized()) {
            if (!defaultBlocks.containsKey(getClass()))
                createDefaultBlock();
            block.copyFrom(defaultBlocks.get(getClass()));
            block.setInitialized(true);
        }

    }

    private void createDefaultBlock() {
        ParameterBlock parameterBlock = getParameterBlock();
        defaultBlocks.put(getClass(), parameterBlock);
        Set<ParameterId> parameters = new HashSet<ParameterId>();
        Set<ParameterId> declaredParameters = new HashSet<ParameterId>();
        {
            Annotation[] annotations = getClass().getAnnotations();
            for (Annotation annotation : annotations) {
                if (ConfigurationParameters.class.isInstance(annotation)) {
                    parseParameter(parameters, (ConfigurationParameters) annotation);
                }
            }
        }
        Method [] methods =
                getClass().getMethods();
        for (Method amethod : methods) {
            Annotation[] annotations = amethod.getAnnotations();
            for (Annotation annotation : annotations) {
                if (IntegerParameter.class.isInstance(annotation)) {
                    parseInteger(
                            parameterBlock.getIntegerParameters(),
                            parameters,
                            declaredParameters,
                            amethod,
                            (IntegerParameter) annotation);
                }
                if (BooleanParameter.class.isInstance(annotation)) {
                    parseBoolean(
                            parameterBlock.getBooleanParameters(),
                            parameters,
                            declaredParameters,
                            amethod,
                            (BooleanParameter) annotation);
                }
                if (StringParameter.class.isInstance(annotation)) {
                    parseString(
                            parameterBlock.getStringParameters(),
                            parameters,
                            declaredParameters,
                            amethod,
                            (StringParameter) annotation);
                }
                if (MapParameter.class.isInstance(annotation)) {
                    parseMappingParameter(
                            parameterBlock.getMapParameters(),
                            parameters,
                            declaredParameters,
                            amethod,
                            (MapParameter) annotation);
                }

            }
        }
    }

    private void parseParameter(Set<ParameterId> parameters, ConfigurationParameters configurationParameters) {
        for (ParameterId parameter : configurationParameters.value()) {
            parameters.add(parameter);
        }
    }

    private <T>void validateParameter(Map<ParameterId, T> parameterMap, ParameterId key, Set<ParameterId> parameters, Method amethod, Set<ParameterId> declaredParameters) {
        if (parameterMap.containsKey(key)) {
            throw new RuntimeException("Configuration " + key + " declared twice in " + getClass().getName());
        }
        if (! parameters.contains(key)) {
            throw new RuntimeException("Configuration " + key + " declared in " + amethod.getName() + ", but not listen in " + getClass()
                    .getName());
        }
        if (! declaredParameters.contains(key)) {
            throw new RuntimeException("Configuration " + key + " already declared in " + getClass().getName());
        }
    }

    private void parseMappingParameter(Map<ParameterId, MapParameter> pbParameters, Set<ParameterId> parameters, Set<ParameterId> declaredParameters, Method amethod, MapParameter config) {
        validateConfiguration(pbParameters, config, parameters, amethod, declaredParameters);
        pbParameters.put(config.parameter(), config);
        declaredParameters.add(config.parameter());
    }

    private void validateConfiguration(Map<ParameterId, MapParameter> pbParameters, MapParameter config, Set<ParameterId> parameters, Method amethod, Set<ParameterId> declaredParameters) {
        if (MASTestSwitches.isConfigurationTesting()) {
            ParameterId key = config.parameter();
            validateParameter(pbParameters, key, parameters, amethod, declaredParameters);
        }
    }

    private void parseInteger(Map<ParameterId, IntegerParameter> pbParameters, Set<ParameterId> parameters, Set<ParameterId> declaredParameters, Method amethod, IntegerParameter config) {
        validateConfiguration(pbParameters, config, parameters, amethod, declaredParameters);
        pbParameters.put(config.parameter(), config);
        declaredParameters.add(config.parameter());
    }

    private void validateConfiguration(Map<ParameterId, IntegerParameter> integerParameters, IntegerParameter config, Set<ParameterId> parameters, Method amethod, Set<ParameterId> declaredParameters) {
        if (MASTestSwitches.isConfigurationTesting()) {
            ParameterId key = config.parameter();
            validateParameter(integerParameters, key, parameters, amethod, declaredParameters);
        }
    }

    private void parseBoolean(Map<ParameterId, BooleanParameter> pbParameters, Set<ParameterId> parameters, Set<ParameterId> declaredParameters, Method amethod, BooleanParameter config) {
        validateConfiguration(pbParameters, config, parameters, amethod, declaredParameters);
        pbParameters.put(config.parameter(), config);
        declaredParameters.add(config.parameter());
    }

    private void validateConfiguration(Map<ParameterId, BooleanParameter> parameterMap, BooleanParameter config, Set<ParameterId> parameters, Method amethod, Set<ParameterId> declaredParameters) {
        if (MASTestSwitches.isConfigurationTesting()) {
            ParameterId key = config.parameter();
            validateParameter(parameterMap, key, parameters, amethod, declaredParameters);
        }
    }

    private void parseString(Map<ParameterId, StringParameter> pbParameters, Set<ParameterId> parameters, Set<ParameterId> declaredParameters, Method amethod, StringParameter config) {
        validateConfiguration(pbParameters, config, parameters, amethod, declaredParameters);
        pbParameters.put(config.parameter(), config);
        declaredParameters.add(config.parameter());
    }

    private void validateConfiguration(Map<ParameterId, StringParameter> parameterMap, StringParameter config, Set<ParameterId> parameters, Method amethod, Set<ParameterId> declaredParameters) {
        if (MASTestSwitches.isConfigurationTesting()) {
            ParameterId key = config.parameter();
            validateParameter(parameterMap, key, parameters, amethod, declaredParameters);
        }
    }

    public boolean validate(ParameterId parameter, int value) {
        initialize();
        IntegerParameter config = getParameterBlock().getIntegerParameters().get(parameter);
        return value <= config.max() && value >= config.min();
    }

    public boolean validate(ParameterId parameter, boolean value) {
        initialize();
        return value;
    }

    public boolean validate(ParameterId parameter, String value) {
        initialize();
        StringParameter config = getParameterBlock().getStringParameters().get(parameter);
        return config.validator().validator().isValid(value);
    }

    public String displayName(ParameterId parameter) {
        initialize();
        return getParameterBlock().getDisplayName(parameter);
    }


    @SuppressWarnings("unchecked")
    public <T> T convert(ParameterId parameter, Class<T> returnType, String value) {
        initialize();
        StringParameter config = getParameterBlock().getStringParameters().get(parameter);
        return (T) config.converter().converter().convert(value);
    }

    @SuppressWarnings("unchecked")
    protected <K,V> Map<K, V> convert(ParameterId parameter, Map serviceMap) throws InvalidConfigurationException {
        MapParameter config = getParameterBlock().getMapParameters().get(parameter);

        Converter keyConverter = config.keyConverter().converter();
        Converter valConverter = config.valueConverter().converter();
        Validator keyValidator = config.keyValidator().validator();
        Validator valValidator = config.valueValidator().validator();

        Map<K, V> newServiceMap = new HashMap<K, V>();
        for (Object key : serviceMap.keySet()) {
            K cvtKey = (K) keyConverter.convert(key);
            V cvtValue = (V) valConverter.convert(serviceMap.get(key));
            if (!valValidator.isValid(cvtValue)) {
                throw new InvalidConfigurationException("Invalid value \"" + cvtValue + "\"");
            }
            if (!keyValidator.isValid(cvtKey)) {
                throw new InvalidConfigurationException("Invalid key \"" + cvtKey + "\"");
            }
            newServiceMap.put(cvtKey, cvtValue);
        }
        return newServiceMap;
    }

    @SuppressWarnings("unchecked")
    protected <K,V> Map<K, V> defaultMap(ParameterId parameterId) {
        initialize();
        Map<K, V> result = new HashMap<K, V>();
        MapParameter config = getParameterBlock().getMapParameters().get(parameterId);
        Object[] values = config.defaultValue();

        Converter keyConverter = config.keyConverter().converter();
        Converter valConverter = config.valueConverter().converter();

        for (int i = 0; i < values.length; i += 2) {
            K cvtKey = (K) keyConverter.convert(values[i]);
            V cvtValue = (V) valConverter.convert(values[i + 1]);
            result.put(cvtKey, cvtValue);
        }
        return result;
    }

    public int defaultInteger(ParameterId parameter) {
        initialize();
        IntegerParameter config = getParameterBlock().getIntegerParameters().get(parameter);
        return config.defaultValue();
    }

    protected boolean defaultBoolean(ParameterId parameter) {
        initialize();

        ParameterBlock parameterBlock = getParameterBlock();
        Map<ParameterId, BooleanParameter> booleanParameters = parameterBlock.getBooleanParameters();
        BooleanParameter booleanParameter = booleanParameters.get(parameter);
        if (booleanParameter == null) {
            logger.error("<CHECKOUT> booleanParameter is null! " + parameterBlock + "," + parameter + ", " + booleanParameters);
            return false;
        } else {
            return booleanParameter.defaultValue();
        }
    }

    protected String defaultString(ParameterId parameter) {
        initialize();
        return getParameterBlock().getStringParameters().get(parameter).defaultValue();
    }

    /**
     * @param cfg
     * @param parameterId
     * @param paramName
     * @param log
     * @return
     * @logs.error "Value <s> for config param <paramName> is not allowed, using <v>" -  The configuration parameter <paramName> has an invalid value
     * @logs.error "Failed to retrieve config parameter <paramName>, <message>" - It was not possible to retrieve the value for configuration parameter <paramName>. <message> should give more information about the error.
     */
    protected boolean readBoolean(IConfigurationManager cfg, ParameterId parameterId, String paramName, ILogger log) {
        IConfiguration configuration = cfg.getConfiguration();
        boolean retVal = defaultBoolean(parameterId);

        if (configuration != null) {
            try {
                IGroup group = configuration.getGroup(RuntimeConstants.CONFIG.GROUP_NAME);

                String s = group.getString(paramName);
                if ("true".equals(s)) {
                    retVal = true;
                } else if ("false".equals(s)) {
                    retVal = false;
                } else {
                    log.error("Value " + s + " for config param " + paramName +
                            " is not allowed, using " + retVal);
                }
            } catch (Exception e) {
                log.error("Failed to retrieve config parameter " +
                        paramName, e);
            }
        }
        return retVal;
    }

    /**
     * @param configurationManager
     * @param parameterId
     * @param paramName
     * @param log
     * @return
     * @logs.error "Failed to retrieve config parameter <paramName>, <message>" - It was not possible to retrieve the value for configuration parameter <paramName>. <message> should give more information about the error.
     */
    protected int readInteger(IConfigurationManager configurationManager, ParameterId parameterId, String paramName, ILogger log) {
        IConfiguration configuration = configurationManager.getConfiguration();
        int value = 0;
        if (configuration != null) {
            try {
                IGroup group = configuration.getGroup(RuntimeConstants.CONFIG.GROUP_NAME);
                value = group.getInteger(paramName);
            } catch (Exception e) {
                log.error("Failed to retrieve config parameter " +
                        paramName, e);
                return defaultInteger(parameterId);
            }
        }
        // ConfigurationManager has validated the value using schema,
        // do not validate again
        return value;
    }

    protected int readInteger(IConfigurationManager configurationManager, ParameterId parameterId, ILogger log) {
        String configParameterName = getParameterBlock().getConfigParameterName(parameterId);
        return readInteger(configurationManager, parameterId, configParameterName, log);
    }

    /**
     * @param log
     * @param value
     * @param paramName
     * @param parameterId
     * @param defaultValue
     * @logs.error "Value <s> for config param <paramName> is not allowed, using <v>" -  The configuration parameter <paramName> has an invalid value
     */
    private void logInvalid(ILogger log, String value, String paramName, ParameterId parameterId, String defaultValue) {
        log.error("Value " + value + " for config param " + paramName +
                " is not allowed, using " + defaultValue);
    }

    /**
     * @param configurationManager
     * @param parameterId
     * @param paramName
     * @param log
     * @return
     * @logs.error "Failed to retrieve config parameter <paramName>, , will use value <value>, <message>" - It was not possible to retrieve the value for configuration parameter <paramName>. <message> should give more information about the error.
     */
    protected String readString(IConfigurationManager configurationManager, ParameterId parameterId, String paramName, ILogger log) {
        IConfiguration configuration = configurationManager.getConfiguration();
        String value = null;

        if (configuration != null) {
            try {
                IGroup group = configuration.getGroup(RuntimeConstants.CONFIG.GROUP_NAME);
                value = group.getString(paramName);
            } catch (Exception e) {
                value = defaultString(parameterId);
                log.error("Failed to retrieve config parameter " +
                        paramName + ", will use value " + value, e);
            }
        }
        if (! validate(parameterId, value)) {
            value = defaultString(parameterId);
            logInvalid(log, value, paramName, parameterId, defaultString(parameterId));
        }
        return value;
    }
}
