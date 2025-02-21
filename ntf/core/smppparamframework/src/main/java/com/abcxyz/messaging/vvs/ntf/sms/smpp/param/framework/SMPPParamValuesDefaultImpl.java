/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

import java.util.ArrayList;
import java.util.List;

/**
 * The SMPPParamValuesDefaultImpl class contains the default implementation of methods which are invoked by the NTF component 
 * to get values for mandatory and optional parameters for submit_sm and data_sm PDUs.
 * <p>
 * The SMPPParamValuesDefaultImpl methods to retrieve mandatory parameter values simply return the default value unchanged back to NTF.
 * To use a different value for a mandatory parameter, the SMPP Parameter plug-in needs to override the method for the mandatory parameter.
 * The following is an example of overriding the implementation for the destination_addr parameter so that an empty string is used as its value.
 * <PRE>
 * &#064;Override
 * public String getDestinationAddr(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smppSMSInfo){   
 *  return ""; 
 * }
 * </PRE>
 * 
 * The optional parameters included in this class are: sms_signal, number_of_messages, message_payload, and set_dpf.
 * To use a different value for an optional parameter implemented in this class, the SMPP Parameter plug-in needs to override the method for the optional parameter.
 * To add/omit optional parameters or add vendor specific parameters, the SMPP Parameter plug-in needs to override the getOptionalParametersTLVList method 
 * and provide its own implementation to return the values for these parameters. 
 * <p>
 * The following is an example of omitting the number_of_messages parameter which is included in this class and 
 * changing the implementation for getting the getSetDpf parameter value.
 * <PRE>
 * &#064;Override
 * public List&#60;SMPPParamsTLV&#62; getOptionalParametersTLVList(SMPPPduEnum pduEnum,SMPPSMSInfo smsInfo) {
 *      List&#60;SMPPParamsTLV&#62; smppParamsTLVList = new ArrayList&#60;SMPPParamsTLV&#62;();
 *      switch (pduEnum) {
 *          case SUBMIT_SM:
 *              smppParamsTLVList.add(getSmsSignal(pduEnum,smsInfo));
 *              break;
 *          case DATA_SM:
 *              smppParamsTLVList.add(getSetDpf(pduEnum,smsInfo));
 *              smppParamsTLVList.add(getMessagePayload(pduEnum,smsInfo));
 *              break;
 *          default:
 *              break;
 *      }
 *      return smppParamsTLVList;
 * }
 * 
 * protected SMPPParamsTLV getSetDpf(SMPPPduEnum pduEnum,SMPPSMSInfo smsInfo) {
 *      int value = 0;
 *      switch (pduEnum) {
 *          case DATA_SM:
 *              //Hard-coded to always request the setting of delivery pending flag upon delivery failure.
 *              value = 1;
 *              break;
 *          default:
 *              break;
 *      }
 *      return new SMPPParamsTLV(SMPPOptionalParamsEnum.SET_DPF, value);
 * }
 * </PRE>
 * <p>
 * In order for the SMPP Parameter plug-in to be loaded by the NTF component, the SMPP Parameter plug-in class MUST have the following package and class name:<p>
 * <code>com.abcxyz.messaging.vvs.ntf.sms.smpp.param.plugin.SMPPParamValuesCustomImpl</code>
 */
public class SMPPParamValuesDefaultImpl implements SMPPParamValues {
    public SMPPParamValuesDefaultImpl(){
    }
    
    @Override
    public boolean isValidationDoneBeforeWritingTLV(){
        return false;
    }
    
    @Override
    public String getServiceType(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getSourceAddrTon(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getSourceAddrNpi(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public String getSourceAddr(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getDestAddrTon(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getDestAddrNpi(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public String getDestinationAddrOutbound(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public String getDestinationAddrInbound(String number, SMPPSMSInfo smsInfo) {
        return number;
    }

    @Override
    public int getEsmClass(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getProtocolId(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getPriorityFlag(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public String getScheduleDeliveryTime(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public String getValidityPeriod(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getRegisteredDelivery(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getReplaceIfPresentFlag(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getDataCoding(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getSmDefaultMsgId(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public int getSmLength(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public byte[] getShortMessage(SMPPPduEnum pduEnum, byte[] defaultValue, SMPPSMSInfo smsInfo) {
        return defaultValue;
    }

    @Override
    public List<SMPPParamsTLV> getOptionalParametersTLVList(SMPPPduEnum pduEnum,SMPPSMSInfo smsInfo) {
        List<SMPPParamsTLV> smppParamsTLVList = new ArrayList<SMPPParamsTLV>();
        switch (pduEnum) {
            case SUBMIT_SM:
                smppParamsTLVList.add(getSmsSignal(pduEnum,smsInfo));
                smppParamsTLVList.add(getNumberOfMessages(pduEnum,smsInfo));
                break;
            case DATA_SM:
                smppParamsTLVList.add(getSetDpf(pduEnum,smsInfo));
                smppParamsTLVList.add(getMessagePayload(pduEnum,smsInfo));
                break;
            default:
                break;
        }
        return smppParamsTLVList;
    }

    
    /**
     * Retrieves the number_of_messages parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return the value of the number_of_messages parameter
     */
    protected SMPPParamsTLV getNumberOfMessages(SMPPPduEnum pduEnum,SMPPSMSInfo smsInfo) {
        int value = 0;
        switch (pduEnum) {
            case SUBMIT_SM:
                if (smsInfo.getAlert() > 0) {
                    value = 0;
                } else {
                    return null;
                }
                break;
            default:
                break;
        }
        return new SMPPParamsTLV(SMPPOptionalParamsEnum.SMS_SIGNAL, value);
    }

    /**
     * Retrieves the sms_signal parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return the value of the sms_signal parameter
     */
    protected SMPPParamsTLV getSmsSignal(SMPPPduEnum pduEnum,SMPPSMSInfo smsInfo) {
        int value = 0;
        switch (pduEnum) {
            case SUBMIT_SM:
                if (smsInfo.getCount() >= 0) {
                    value = smsInfo.getCount();
                } else {
                    return null;
                }
                break;
            default:
                break;
        }
        return new SMPPParamsTLV(SMPPOptionalParamsEnum.SMS_SIGNAL, value);
    }

    /**
     * Retrieves the set_dpf parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return the value of the set_dpf parameter
     */
    protected SMPPParamsTLV getSetDpf(SMPPPduEnum pduEnum,SMPPSMSInfo smsInfo) {
        int value = 0;
        switch (pduEnum) {
            case DATA_SM:
                value = smsInfo.getSetDpf() ? 1 : 0;
                break;
            default:
                break;
        }
        return new SMPPParamsTLV(SMPPOptionalParamsEnum.SET_DPF, value);
    }

    /**
     * Retrieves the message_payload parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return the value of the message_payload parameter
     */
    protected SMPPParamsTLV getMessagePayload(SMPPPduEnum pduEnum,SMPPSMSInfo smsInfo) {
        byte[] value = null;
        switch (pduEnum) {
            case DATA_SM:
                byte[] udhData = null;
                byte[] udData = null;

                if (smsInfo.getUDHLength() == 0) {
                    udhData = new byte[0];
                } else {
                    udhData = smsInfo.getUDHBytes();
                }
                udData = smsInfo.getUserData(udhData);
                value = new byte[udhData.length + udData.length];
                System.arraycopy(udhData, 0, value, 0, udhData.length);
                System.arraycopy(udData, 0, value, udhData.length, udData.length);
                break;
            default:
                break;
        }
        return new SMPPParamsTLV(SMPPOptionalParamsEnum.MESSAGE_PAYLOAD, value);
    }
}
