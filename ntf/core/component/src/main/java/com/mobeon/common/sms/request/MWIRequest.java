/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.common.sms.request;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.smscom.udh.SpecialSMSMessageIndication;
import com.mobeon.common.smscom.udh.SpecialSMSMessageIndication.ExtendedMessageType;
import com.mobeon.common.smscom.udh.SpecialSMSMessageIndication.MessageType;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.SMSUnit;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.sms.ServiceTypePidLookupImpl;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 *The Request class stores the request that is sent to the smsc. It is responsible to 
 *make an SMSMessage depending on the protocol and type of message.
 *Each type of message must extend this class and implement the getSMSMessage method.
 */
public class MWIRequest extends Request { 

    /**
     * Return Call Message is set to indicate Message is waiting.
     * The SMS content provides information about the type and number of new
     * messages.
     * The originating address is used to return the call and retrieve
     * new messages.
     * For further info please see:
     * GSM 03.40, V7.4.0, 1999-12, clause 9.2.3.9
     */
    public static final short GSM_PID_RETURN_CALL_MESSAGE = 0x5f;

    /**
     * Data Coding Scheme octet is set to Message waiting indication (Store Message)
     * to indicate message waiting to MS.
     * For further info please see:
     * GSM03.38, V7.2.0, 1999-07, clause 4
     */
    public static final short GSM_DCS_UCS2 = 0x08;
    // Group //Type
    public static final short GSM_DCS_DISCARD_DEFAULT_ALPHABET_ACTIVE_VMN = 0xc8; // 1100  1000
    public static final short GSM_DCS_DISCARD_DEFAULT_ALPHABET_INACTIVE_VMN = 0xc0; // 1100  0000
    public static final short GSM_DCS_STORE_DEFAULT_ALPHABET_ACTIVE_VMN = 0xd8; // 1101  1000
    public static final short GSM_DCS_STORE_DEFAULT_ALPHABET_INACTIVE_VMN = 0xd0; // 1101  0000
    public static final short GSM_DCS_STORE_UCS2_ALPHABET_ACTIVE_VMN = 0xe8; // 1110  1000
    public static final short GSM_DCS_STORE_UCS2_ALPHABET_INACTIVE_VMN = 0xe0; // 1110  0000
    
    private int count;
    private String message;
    private byte[] byteMessage;

    private UserInfo user;
    private UserMailbox inbox;
    
    private static IntfServiceTypePidLookup serviceTypeLookup = ServiceTypePidLookupImpl.get();
    
    public MWIRequest(SMSAddress from, SMSAddress to, UserInfo user, UserMailbox inbox, int validity, SMSResultHandler resultHandler, int id, int count, String message, byte[] byteMessage) { 
        super(from, to, validity, resultHandler, id);  
        this.count = count;
        this.message = message;
        this.byteMessage = byteMessage;
        this.user = user;
        this.inbox = inbox;
    }
    
    public MWIRequest(SMSAddress from, SMSAddress to, UserInfo user, UserMailbox inbox, int validity, SMSResultHandler resultHandler, int id, int count, String message) { 
        this(from, to, user, inbox, validity, resultHandler, id, count, message, null);
    }

    public MWIRequest(SMSAddress from, SMSAddress to, int validity, SMSResultHandler resultHandler, int id, int count, String message) { 
        super(from, to, validity, resultHandler, id);  
        this.count = count;
        this.message = message;
    }

    
    @SuppressWarnings("null")
    public SMSMessage getSMSMessage(SMSUnit unit) {
        SMSMessage msg;
        if (message != null)
        {
            msg = unit.getConverter().unicodeToMessage(message,
                unit.getConfig().getSmsStringLength()
                * unit.getConfig().getNumberOfSms());
        }
        else
        {
            msg = new SMSMessage(byteMessage, 0);
            msg.setConverter(unit.getConverter());
            msg.setPacked(unit.getConverter().getPack());
        }
        
        msg.setExpiryTimeRelative(validity);
        msg.setFragmentSize(unit.getConfig().getSmsStringLength());
        msg.setPriority(unit.getConfig().getSmsPriority());
        
        msg.setAlert(0);
        msg.setReplace(false); 

        if(user != null && inbox != null){
            //Get something in the config related to the cos name //user.getCosName()
            boolean haveSMSMessageIndicatorService = user.hasSpecialSMSMessageIndicationService();

            //Add the new User data header if they have the services and if they have the corresponding type of mail.
            if(haveSMSMessageIndicatorService){
                boolean store = Config.getMwiStoreMessageAfterUpdatingIndication();
 
                if(user.hasMailType(Constants.NTF_VOICE)){
                    msg.addIEtoUDH(new SpecialSMSMessageIndication(MessageType.VOICE, store, inbox.getNewVoiceCount()));
                }
                if(user.hasMailType(Constants.NTF_FAX)){
                    msg.addIEtoUDH(new SpecialSMSMessageIndication(MessageType.FAX, store, inbox.getNewFaxCount()));
                }
                if(user.hasMailType(Constants.NTF_EMAIL)){
                    msg.addIEtoUDH(new SpecialSMSMessageIndication(MessageType.EMAIL, store, inbox.getNewEmailCount()));
                }
                if(user.hasMailType(Constants.NTF_VIDEO)){
                    msg.addIEtoUDH(new SpecialSMSMessageIndication(MessageType.EXTENDED, ExtendedMessageType.VIDEO, store, inbox.getNewVideoCount()));
                }
            }
        }
        
        if (unit.getConfig().isBearingNetworkGSM()) {
            int dcs = GSM_DCS_DISCARD_DEFAULT_ALPHABET_ACTIVE_VMN;

            if (count <= 0) {
                dcs = GSM_DCS_DISCARD_DEFAULT_ALPHABET_INACTIVE_VMN;  
            } 
            msg.setDCS(dcs);
            msg.setPID(GSM_PID_RETURN_CALL_MESSAGE);
        }

        if (unit.getConfig().isBearingNetworkCdma2000()) {
            msg.setCount(count);
        }
        
        String serviceType = null;
        /* Use replace table (service Type) definition for MWI if exists, if not use old legacy settings.
         * This is mainly used for cancel, to allow to cancel a specific service type (content or phrase).
         * but could also be used to just replace an MWI on and not an OFF.
         * For cancel so we  can cancel an MWI_on separately to an MWI OFF if configured that way.
         */
        if (count > 0) {
            serviceType = serviceTypeLookup.getServiceType("mwiontext");
        } else {
            serviceType = serviceTypeLookup.getServiceType("mwiofftext");
        }
        String defaultSrvType = unit.getConfig().getSmeServiceType();
        if (serviceType == null || serviceType.isEmpty() || serviceType.equals(defaultSrvType)) {
                serviceType=unit.getConfig().getSmeServiceTypeForMwi();
        }
        msg.setServiceType(serviceType);

        return msg;
    }
}

