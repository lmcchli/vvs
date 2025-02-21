package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;

/**
 * This class is composed of an SMSAddress object and a SMSMessage object. It's purpose is to provide methods that are needed to
 * construct a TLV object for a given optional/vendor specific parameter.
 */
public class SMPPSMSInfoFacade implements SMPPSMSInfo {

    private SMSAddress smsOrgAddress = null;
    private SMSAddress smsDestAddress = null;
    private SMSMessage smsMessage = null;
    private CommonMessagingAccess commonMessagingAccess = null;

    /**
     * 
     * @param smsOrgAddress
     *        The origin address.
     * @param smsDestAddress
     *        The destination address.
     * @param smsMessage
     *        The sms message.
     */
    public SMPPSMSInfoFacade(SMSAddress smsOrgAddress, SMSAddress smsDestAddress, SMSMessage smsMessage) {
        this.smsOrgAddress = smsOrgAddress;
        this.smsDestAddress = smsDestAddress;
        this.smsMessage = smsMessage;
        this.commonMessagingAccess = CommonMessagingAccess.getInstance();
    }

    @Override
    public boolean hasMoreFragments() {
        return this.smsMessage.hasMoreFragments();
    }

    @Override
    public int getCount() {
        return this.smsMessage.getCount();
    }

    @Override
    public int getAlert() {
        return this.smsMessage.getAlert();
    }

    @Override
    public String getServiceType() {
        return this.smsMessage.getServiceType();
    }

    @Override
    public boolean getSetDpf() {
        return this.smsMessage.getSetDpf();
    }

    @Override
    public int getUDHLength() {
        return this.smsMessage.getUDHLength();
    }

    @Override
    public byte[] getUserData(byte[] udhData) {
        return this.smsMessage.getUserData(udhData);
    }

    @Override
    public byte[] getUDHBytes() {
        return this.smsMessage.getUDHBytes();
    }

    @Override
    public int getSourceTON() {
        return this.smsOrgAddress.getTON();
    }

    @Override
    public int getSourceNPI() {
        return this.smsOrgAddress.getNPI();
    }

    @Override
    public String getSourceNumber() {
        return this.smsOrgAddress.getNumber();
    }

    @Override
    public String normalize(String number, String context, boolean useDefaultContext) {
        return this.commonMessagingAccess.normalize(number, context, useDefaultContext);
    }
}
