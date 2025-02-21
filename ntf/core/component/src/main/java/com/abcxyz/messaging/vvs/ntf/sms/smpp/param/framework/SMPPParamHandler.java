package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

import static com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPOptionalParamConstants.*;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class SMPPParamHandler {
    private static SMPPParamHandler smppParamHandler = null;
    private static SMPPParamValues smppParamValues = null;
    private static LogAgent logAgent = null;

    private SMPPParamHandler() {
        logAgent = NtfCmnLogger.getLogAgent("com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework");
        smppParamValues = createSMPPParamValuesInstance();
        SMPPParamsTLV.setLogAgent(logAgent);
        SMPPParamsTLV.setIsValidationDoneBeforeWritingTLV(smppParamValues.isValidationDoneBeforeWritingTLV());
    }

    public static SMPPParamHandler get() {
        if (smppParamHandler == null) {
            smppParamHandler = new SMPPParamHandler();
        }

        return smppParamHandler;
    }

    /**
     * 
     * @return An instance of SMPPParamValues.
     */
    public SMPPParamValues getSMPPParamValuesInstance() {
        return smppParamValues;
    }

    /**
     * This method is responsible for appending the TLV values of the optional parameters as well as additional parameters (a.k.a.
     * vendor specific parameters) to the data output stream passed as an argument. It does this by going through the list of
     * optional/additional parameters that are assigned to this PDU and then writes the TLV values for each of these parameters to
     * the data output stream. The data output stream that is passed to this method already contains the values for the header and
     * mandatory parameters for the passed PDU.
     * 
     * @param pduEnum
     *        The PDU type e.g. (data_sm, submit_sm, etc)
     * @param dos
     *        The data output stream that already contains the header parameters and mandatory parameters prior to this method being
     *        called.
     * @param smsInfo provides necessary methods for constructing an TLV object for a given optional/vendor specific parameter.       
     * @return Returns a data output stream which contains all of the parameters that are used for this PDU (header, mandatory, and
     *         optional).
     * @throws IOException
     *         If an I/O error occurs while writing to the data output stream.
     */
    public DataOutputStream appendOptionalAndVendorSpecificParamToPdu(SMPPPduEnum pduEnum, DataOutputStream dos, SMPPSMSInfo smsInfo)
            throws IOException {
        /*
         * Get a list of optional parameters that are supported for the given PDU.
         */
        List<SMPPParamsTLV> supportedParams = smppParamValues.getOptionalParametersTLVList(pduEnum, smsInfo);
        int tag;
        int length;
        int intValue;
        byte[] stringValue;

        /*
         * Go through the list of optional parameters supported for the given PDU and write it to the dos.
         */
        for (SMPPParamsTLV supportedParam : supportedParams) {
            try {
                if (supportedParam != null) {
                    // Check if the optional parameter has a valid tag, length, and value. Validation
                    // should only be performed during debugging.
                    if (supportedParam.isValid()) {
                        tag = supportedParam.getTag();
                        length = supportedParam.getLength();
                        String stringTag = "";
                        SMPPOptionalParamsEnum optionalParamEnum = supportedParam.getOptionalParamEnum();

                        if (optionalParamEnum != null) {
                            stringTag = optionalParamEnum.toString();
                        } else {
                            stringTag = "" + tag;
                        }

                        if (supportedParam.getValueType() == STRING_TYPE || supportedParam.getValueType() == OTHER) {
                            stringValue = supportedParam.getStringAsByteArray();
                            if (logAgent.isDebugEnabled()) {
                                logAgent.debug("SMPPParamHandler.appendOptionalAndVendorSpecificParamToPdu():About to write optional parameter where the tag is "
                                        + stringTag
                                        + ", length is "
                                        + length
                                        + ", value (in bytes) is "
                                        + convertBAToString(stringValue) + ".");
                            }
                            dos.writeShort(tag);
                            dos.writeShort(length);
                            dos.write(stringValue);
                        } else {
                            intValue = supportedParam.getIntValue();
                            if (logAgent.isDebugEnabled()) {
                                logAgent.debug("SMPPParamHandler.appendOptionalAndVendorSpecificParamToPdu():About to write optional parameter where the tag is "
                                        + stringTag + ", length is " + length + ", value is " + intValue + ".");
                            }
                            dos.writeShort(tag);
                            dos.writeShort(length);

                            if (length == 1) {
                                dos.writeByte(intValue);
                            } else if (length == 2) {
                                dos.writeShort(intValue);
                            } else if (length == 4) {
                                dos.writeInt(intValue);
                            }
                        }
                    }
                }
            } catch (SMPPException e) {
                logAgent.debug("SMPPParamHandler.appendOptionalAndVendorSpecificParamToPdu():" + e.getMessage());
                logAgent.debug("SMPPParamHandler.appendOptionalAndVendorSpecificParamToPdu():" + getStackTraceString(e).toString());
            } catch (NullPointerException e) {
                logAgent.debug(e.getMessage());
                logAgent.debug("SMPPParamHandler.appendOptionalAndVendorSpecificParamToPdu():" + getStackTraceString(e).toString());
            }
        }

        return dos;
    }

    /**
     * This class creates an instance of the SMPPParamValues interface.
     * 
     * @return The concrete implementation of the SMPPParamValues interface.
     */
    private SMPPParamValues createSMPPParamValuesInstance() {
        SMPPParamValues smppParamValues = null;
        String className = "com.abcxyz.messaging.vvs.ntf.sms.smpp.param.plugin.SMPPParamValuesCustomImpl";
        Exception exception = null;
        try {
            /*
             * Attempt to load the plugin class if it exists. If it exists, create an instance of it.
             */
            logAgent.debug("SMPPParamHandler.createSMPPParamValuesInstance():Attempting to find the plugin class and create an instance of it.");
            Class<?> smppParamValuesClass = Class.forName(className);
            smppParamValues = (SMPPParamValues) smppParamValuesClass.newInstance();
        } catch (ClassNotFoundException ex) {
            exception = ex;
        } catch (InstantiationException ex) {
            exception = ex;
        } catch (IllegalAccessException ex) {
            exception = ex;
        }

        if (exception != null) {
            // The plugin class does not exist, thus we will use the class in ntf which contains the default values.
            logAgent.debug("SMPPParamHandler.createSMPPParamValuesInstance():Plugin class was not found so the SMPPDefaultParamValues instance will be created and used.");
            smppParamValues = new SMPPParamValuesDefaultImpl();
        }

        return smppParamValues;
    }

    /**
     * 
     * @param ba
     *        The byte array whose String value is to be retrieved.
     * @return A String of all the bytes in the byte array.
     */
    private String convertBAToString(byte[] ba) {
        String baToString = "[ ";
        for (Byte b : ba) {
            baToString += b + " ";
        }
        baToString += "]";
        return baToString;
    }

    /**
     * 
     * @param e
     *        stackTrace
     * @return A string representation of the stackTrace.
     */
    private static StringWriter getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw;
    }
}
