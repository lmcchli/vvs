package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.plugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPParamValuesDefaultImpl;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPOptionalParamsEnum;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPPduEnum;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPParamsTLV;
import com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPSMSInfo;

public class SMPPParamValuesCustomImpl extends SMPPParamValuesDefaultImpl {
    public SMPPParamValuesCustomImpl(){
    }

    @Override
    public String getSourceAddr(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo){
        return null;
    }
    
    @Override
    public String getDestinationAddrOutbound(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo){
        String normalizedValue = smsInfo.normalize(defaultValue, "kddi", false);
        if(normalizedValue == null){
            return defaultValue;
        }
        return normalizedValue;
    }
    
    @Override
    public String getDestinationAddrInbound(String number, SMPPSMSInfo smsInfo){
        String normalizedValue = smsInfo.normalize(number, "kddi_reverse", false);
        if(normalizedValue == null){
            return number;
        }
        return normalizedValue;
    }
    
    @Override
    public int getEsmClass(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo){
        int newValue = 0;
        switch(pduEnum){
            case SUBMIT_SM:
                 newValue = (defaultValue | 0x3);
                 break;
            case DATA_SM:
                 newValue = (defaultValue | 0x2);
                 break;
            default:
                break;
        }
        return newValue;
    }
    
    @Override
    public List<SMPPParamsTLV> getOptionalParametersTLVList(SMPPPduEnum smppPDU,SMPPSMSInfo smsInfo) {
        List<SMPPParamsTLV> smppParamsTLVList = new ArrayList<SMPPParamsTLV>();
        switch (smppPDU) {
            case SUBMIT_SM:
                smppParamsTLVList.add(getSmsSignal(smppPDU,smsInfo));
                smppParamsTLVList.add(getNumberOfMessages(smppPDU,smsInfo));
                smppParamsTLVList.add(getCallbackNum(smppPDU,smsInfo));
                smppParamsTLVList.add(getCallbackNumPresInd(smppPDU,smsInfo));
                break;
            case DATA_SM:
                smppParamsTLVList.add(getCallbackNum(smppPDU,smsInfo));
                smppParamsTLVList.add(getCallbackNumPresInd(smppPDU,smsInfo));
                smppParamsTLVList.add(getMoreMessagesToSend(smppPDU,smsInfo));
                smppParamsTLVList.add(getSetDpf(smppPDU,smsInfo));
                smppParamsTLVList.add(getMcTimestamp(smppPDU,smsInfo));
                smppParamsTLVList.add(getMessagePayload(smppPDU,smsInfo));
                break;
            default:
                break;
        }
        return smppParamsTLVList;
    }

    public SMPPParamsTLV getCallbackNum(SMPPPduEnum pduType,SMPPSMSInfo smsInfo) {
        byte[] value = null;
        byte[] firstThreeOctetsByteArray = new byte[3];
        byte[] remainingOctetsByteArray = null;
        String remainingOctetString = "";
        String from = smsInfo.getSourceNumber();
        String normalizedFrom = smsInfo.normalize(from, "kddi", false);
        int ton = smsInfo.getSourceTON();
        int npi = smsInfo.getSourceNPI();
        String type = smsInfo.getServiceType();
        switch (pduType) {
            case SUBMIT_SM:
                firstThreeOctetsByteArray[0] = (byte) 0;
                firstThreeOctetsByteArray[1] = (byte) ton;
                firstThreeOctetsByteArray[2] = (byte) npi;
                if (type.equalsIgnoreCase("sdm")) {
                    // service type = textmail
                    remainingOctetString = normalizedFrom==null?from + "\0":normalizedFrom + "\0";
                } else {
                    return null;
                }
                remainingOctetsByteArray = remainingOctetString.getBytes();
                value = new byte[firstThreeOctetsByteArray.length + remainingOctetsByteArray.length];
                System.arraycopy(firstThreeOctetsByteArray, 0, value, 0, firstThreeOctetsByteArray.length);
                System.arraycopy(remainingOctetsByteArray, 0, value, firstThreeOctetsByteArray.length,
                        remainingOctetsByteArray.length);
                break;
            case DATA_SM:
                firstThreeOctetsByteArray[0] = (byte) 0;
                firstThreeOctetsByteArray[1] = (byte) ton;
                firstThreeOctetsByteArray[2] = (byte) npi;
                if (type.equalsIgnoreCase("cmt")) {
                    // service type = textmail
                    remainingOctetString = normalizedFrom==null?from + "\0":normalizedFrom + "\0";
                } else if (type.equalsIgnoreCase("nvn")||type.equalsIgnoreCase("rnm")) {
                    // service type = message notification with CLI
                    remainingOctetString = "1417\0";
                } else if (type.equalsIgnoreCase("sdm")) {
                    // service type = incoming call notification
                    remainingOctetString = "141\0";
                } else {
                    return null;
                }
                remainingOctetsByteArray = remainingOctetString.getBytes();
                value = new byte[firstThreeOctetsByteArray.length + remainingOctetsByteArray.length];
                System.arraycopy(firstThreeOctetsByteArray, 0, value, 0, firstThreeOctetsByteArray.length);
                System.arraycopy(remainingOctetsByteArray, 0, value, firstThreeOctetsByteArray.length,
                        remainingOctetsByteArray.length);
                break;
            default:
                break;
        }
        return new SMPPParamsTLV(SMPPOptionalParamsEnum.CALLBACK_NUM, value);
    }

    public SMPPParamsTLV getCallbackNumPresInd(SMPPPduEnum pduType,SMPPSMSInfo smsInfo) {
        byte[] value = new byte[1];
        String type = smsInfo.getServiceType();
        switch (pduType) {
            case SUBMIT_SM:
                if (type.equalsIgnoreCase("sdm")) {
                    value[0] = (byte) 1;
                } else {
                    return null;
                }
                break;
            case DATA_SM:
                if (type.equalsIgnoreCase("cmt") || type.equalsIgnoreCase("nvn") || type.equalsIgnoreCase("sdm") || type.equalsIgnoreCase("rnm")) {
                    value[0] = (byte) 1;
                } else {
                    return null;
                }
                break;
            default:
                break;
        }
        return new SMPPParamsTLV(SMPPOptionalParamsEnum.CALLBACK_NUM_PRES_IND, value);
    }

    public SMPPParamsTLV getMoreMessagesToSend(SMPPPduEnum pduType,SMPPSMSInfo smsInfo) {
        int value = 0;
        switch (pduType) {
            case BIND_TRANSMITTER_RESP:
                break;
            case SUBMIT_SM:
                break;
            case DATA_SM:
                value = smsInfo.hasMoreFragments() ? 1 : 0;
                break;
            default:
                break;
        }
        return new SMPPParamsTLV(SMPPOptionalParamsEnum.MORE_MESSAGES_TO_SEND, value);
    }

    public SMPPParamsTLV getMcTimestamp(SMPPPduEnum pduType,SMPPSMSInfo smsInfo) {
        byte[] mc_timestamp = null;
        Date currentDateAndTime = null;
        SimpleDateFormat simpleDateFormat = null;
        String dateString = "";
        switch (pduType) {
            case SUBMIT_SM:
                break;
            case DATA_SM:
                currentDateAndTime = new Date();
                simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
                dateString = simpleDateFormat.format(currentDateAndTime);
                mc_timestamp = new byte[6];
                for (int i = 0; i < 12; i += 2) {
                    mc_timestamp[i / 2] = (byte) ((Integer.parseInt("" + dateString.charAt(i)) & 0XF) << 4);
                    mc_timestamp[i / 2] += (byte) ((Integer.parseInt("" + dateString.charAt(i + 1)) & 0XF));
                }
                break;
            default:
                break;
        }
        return new SMPPParamsTLV(0x1400, 6, 6, mc_timestamp);
    }
}
