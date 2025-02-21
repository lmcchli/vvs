package com.mobeon.masp.stream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.stream.RTPPayload.RtpValidationResult;

/*
 * TODO: This class should not be inside the interface, it is a temporary solution.
 * Preferably RTPPayload and ConnectionProperties would be pure
 * interfaces with their implementations outside interface and with
 * factory methods for instantiating them.
 */

/**
 * Immutable class for validating a received FMTP string against a
 * configured reference.
 */
class FormatSpecificParametersValidator {

    private static final ILogger LOGGER =
        ILoggerFactory.getILogger(FormatSpecificParametersValidator.class);

    private final String refFmtp;
    private final HashMap<String,String> refFmtpMap;




    /**
     * Constructs a PayloadFormatParameterValidator with the given
     * string as reference for the format specific parameters.
     * Example: a=fmtp:<format> <format specific parameters>
     * The object is immutable.
     *
     * @param fmtp The reference to test against for the <format specific parameters>.
     *
     */
    public FormatSpecificParametersValidator(String fmtp) {
	refFmtp = fmtp;
	refFmtpMap = parseFmtp(fmtp);
    }

    /**
     * Get the reference format parameters that this object was constructed with.
     * @return String containing the fmtp.
     */
    public String getReferenceFmtp() {
	return refFmtp;
    }

    /**
     * Validate the payload format parameters in a string with the reference
     * string that this object was constructed with.
     * The following parameters are checked:
     * 	octet-align (interpreted as 0 if not existing)
     *  crc (interpreted as 0 if not existing)
     *  robust-sorting (interpreted as 0 if not existing)
     *  interleaving (interpreted as 0 if not existing)
     *  channels (interpreted as 1 if not existing)
     *  mode-set (interpreted as "all allowed" if not existing)
     *
     * @param fmtp Format parameters to test
     * @return true if all known paramaters matched, false otherwise.
     */
    public RtpValidationResult validateFormatParameters(String fmtp) {
        HashMap<String,String> fmtpMap = parseFmtp(fmtp);

        // Validate all known parameters
        // Unknown parameters are ignored
        String[] params = {
            "octet-align",
            "crc",
            "robust-sorting",
            "interleaving",
            "channels"
        };
        String[] defaultValues = {
            "0",
            "0",
            "0",
            null,
            "1"
        };
        boolean allOk = true;
        for (int i = 0; i < params.length; i++) {
            boolean isOk = checkParameterValues(params[i], fmtpMap, defaultValues[i]);
            if (LOGGER.isDebugEnabled()) {
            if (isOk)
                LOGGER.debug("FMTP parameter " + params[i] + " matches");
            else
                LOGGER.debug("FMTP parameter " + params[i] + " does not match");
            }
            if (!isOk)
            allOk = false;
        }
        if(allOk)
        {
            return checkModeSet(fmtpMap);
        }
        else
        {
            return RtpValidationResult.NO_MATCH;
        }
	}

    /**
     * Parse the given string into a HashMap with parameter/value pairs.
     *
     * @param fmtp String containing the fmtp parameters to parse
     * @return HashMap with the parameter name as key and value as value.
     */
    protected HashMap<String, String> parseFmtp(String fmtp) {
	if (LOGGER.isDebugEnabled())
	    LOGGER.debug("Parsing fmtp: " + fmtp);

	HashMap<String,String> pvMap = new LinkedHashMap<String,String>();

	if (fmtp == null)
	    return pvMap;

	String[] pvs = fmtp.trim().split(" *; *");
	for (String pv : pvs) {
	    String[] paramValue = pv.split("=",2);
	    if (paramValue.length == 1)
		pvMap.put(paramValue[0],null);
	    else if (paramValue.length == 2)
		pvMap.put(paramValue[0],paramValue[1]);
	    else
		LOGGER.warn("Could not parse fmtp attribute: " + fmtp);
	}

	return pvMap;
    }

    /**
     * Compare given parameter from fmtpMap with the reference value of the parameter.
     *
     * @param param The parameter name to compare
     * @param fmtpMap Map containing the parameters to test.
     * @param defaultValue The default value to compare with if the parameter is missing.
     * @return true if the parameter values matches, false otherwise.
     */
    protected boolean checkParameterValues(
	    String param, HashMap<String,String> fmtpMap, String defaultValue) {

	String refValue = refFmtpMap.get(param);
	String value = fmtpMap.get(param);

	if (refValue == null)
	    refValue = defaultValue;

	if (value == null)
	    value = defaultValue;

	if (refValue == null && value == null)
	    return true;
	else if (refValue != null && refValue.equals(value)) {
	    if (LOGGER.isDebugEnabled())
		LOGGER.debug("FMTP parameter " + param + " matches");
	    return true;
	} else {
	    if (LOGGER.isDebugEnabled())
		LOGGER.debug("FMTP parameter " + param + " does not match");
	    return false;
	}

    }

    protected RtpValidationResult checkModeSet(HashMap<String,String> fmtp) {

        String localModeSet = refFmtpMap.get("mode-set");
        String remoteModeSet = fmtp.get("mode-set");
        RtpValidationResult result = RtpValidationResult.NO_MATCH;

        if (localModeSet == null || remoteModeSet == null)         {
            result  = RtpValidationResult.EXCACT_MATCH;
        } else {
            if (localModeSet.equals(remoteModeSet)) {
                result =  RtpValidationResult.EXCACT_MATCH;
            } else {
                Set<String> intersectionModeSet = new HashSet<String>();
                String[] localModeSetValues = localModeSet.split(",");
                String[] remoteModeSetValues = remoteModeSet.split(",");

                // Loop through the list of the local mode-set (configuration)
                for (String localModeSetValue : localModeSetValues) {
                    localModeSetValue = localModeSetValue.trim();

                    // Loop through the list of the remote mode-set (client's fmtp values)
                    for (String remoteModeSetValue : remoteModeSetValues) {
                        remoteModeSetValue = remoteModeSetValue.trim();

                        if (localModeSetValue.equalsIgnoreCase(remoteModeSetValue)) {
                            intersectionModeSet.add(localModeSetValue);
                            result = RtpValidationResult.PARTIAL_MATCH;
                        }
                    }
                }
            }
        }

        String serverModeSet = (localModeSet == null ? "no restriction" : localModeSet);
        String clientModeSet = (remoteModeSet == null ? "no restriction" : remoteModeSet);
        LOGGER.debug("mode-set validation " + result + ". Local mode-set has " + serverModeSet + " and remote mode-set has " + clientModeSet);

        return result;
    }
}
