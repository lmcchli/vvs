/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.common.sms.request;

import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.SMSUnit;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.smscom.udh.SpecialSMSMessageIndication;
import com.mobeon.common.smscom.udh.SpecialSMSMessageIndication.ExtendedMessageType;
import com.mobeon.common.smscom.udh.SpecialSMSMessageIndication.MessageType;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.UserMailbox;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.mobeon.ntf.out.sms.ServiceTypePidLookupImpl;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 *The Request class stores the request that is sent to the smsc. It is responsible to 
 *make an SMSMessage depending on the protocol and type of message.
 *Each type of message must extend this class and implement the getSMSMessage method.
 */
public class SMSMWIRequest extends Request { 

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
    // Group //Type //decimal
    public static final short GSM_DCS_DISCARD_DEFAULT_ALPHABET_ACTIVE_VMN = 0xc8; // 1100  1000    200
    public static final short GSM_DCS_DISCARD_DEFAULT_ALPHABET_INACTIVE_VMN = 0xc0; // 1100  0000    192
    public static final short GSM_DCS_STORE_DEFAULT_ALPHABET_ACTIVE_VMN = 0xd8; // 1101  1000    216
    public static final short GSM_DCS_STORE_DEFAULT_ALPHABET_INACTIVE_VMN = 0xd0; // 1101  0000    208
    public static final short GSM_DCS_STORE_UCS2_ALPHABET_ACTIVE_VMN = 0xe8; // 1110  1000    232
    public static final short GSM_DCS_STORE_UCS2_ALPHABET_INACTIVE_VMN = 0xe0; // 1110  0000    224
    
    private int count;
    private String message;
    private byte[] byteMessage;
    private int replacePosition;
    
    private static IntfServiceTypePidLookup serviceTypeLookup = ServiceTypePidLookupImpl.get();
   
    private UserInfo user;
    private UserMailbox inbox;
    
    public SMSMWIRequest(SMSAddress from, SMSAddress to, UserInfo user, UserMailbox inbox, int validity, SMSResultHandler resultHandler, int id, int count, String message, int replacePosition) { 
        this(from, to, user, inbox, validity, resultHandler, id, count, message, null, replacePosition);
    }
    
    public SMSMWIRequest(SMSAddress from, SMSAddress to, UserInfo user, UserMailbox inbox, int validity, SMSResultHandler resultHandler, int id, int count, String message, byte[] byteMessage, int replacePosition) {
        super(from, to, validity, resultHandler, id);  
        this.count = count;
        this.message = message;
        this.byteMessage = byteMessage;
        this.replacePosition = replacePosition;
        this.user = user;
        this.inbox = inbox;
    }

    public SMSMWIRequest(SMSAddress from, SMSAddress to, int validity, SMSResultHandler resultHandler, int id, int count, String message, int replacePosition) { 
        super(from, to, validity, resultHandler, id);  
        this.count = count;
        this.message = message;
        this.replacePosition = replacePosition;  
    }

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
        
        if(user != null && inbox != null){
            //Get something in the config related to the cos name //user.getCosName()
            boolean haveSMSMessageIndicatorService = user.hasSpecialSMSMessageIndicationService();
            
            //Add the new User data header if they have the services and if they have the corresponding type of mail.
            if(haveSMSMessageIndicatorService){
                if(user.hasMailType(Constants.NTF_VOICE)){
                    msg.addIEtoUDH(new SpecialSMSMessageIndication(MessageType.VOICE, true, inbox.getNewVoiceCount()));
                }
                if(user.hasMailType(Constants.NTF_FAX)){
                    msg.addIEtoUDH(new SpecialSMSMessageIndication(MessageType.FAX, true, inbox.getNewFaxCount()));
                }
                if(user.hasMailType(Constants.NTF_EMAIL)){
                    msg.addIEtoUDH(new SpecialSMSMessageIndication(MessageType.EMAIL, true, inbox.getNewEmailCount()));
                }
                if(user.hasMailType(Constants.NTF_VIDEO)){
                    msg.addIEtoUDH(new SpecialSMSMessageIndication(MessageType.EXTENDED, ExtendedMessageType.VIDEO, true, inbox.getNewVideoCount()));
                }
            }
        }
        
        
        if (msg.isLargerThanOneFragment()) {
            msg.setReplace(false);
            msg.setServiceType(unit.getConfig().getSmeServiceType());
            msg.setPID(0);
            
        } else {      
                msg.setPID(ServiceTypePidLookupImpl.get().getPid(replacePosition));
                String serviceType = serviceTypeLookup.getServiceType(replacePosition);
                if (serviceType != null) {
                    msg.setReplace(true);
                    msg.setServiceType(serviceType);
                } else {
                    //set to default but no replace.
                    msg.setServiceType(unit.getConfig().getSmeServiceType());
                }                 
        }
        
        if (unit.getConfig().isBearingNetworkGSM()) {
            int dcs = GSM_DCS_STORE_DEFAULT_ALPHABET_ACTIVE_VMN;
            
            if (count <= 0) {
                if (msg.getDCS() == GSM_DCS_UCS2) {
                    dcs = GSM_DCS_STORE_UCS2_ALPHABET_INACTIVE_VMN;
                } else {
                    dcs = GSM_DCS_STORE_DEFAULT_ALPHABET_INACTIVE_VMN;
                }
            } else {
                if (msg.getDCS() == GSM_DCS_UCS2) {
                    dcs = GSM_DCS_STORE_UCS2_ALPHABET_ACTIVE_VMN;
                } else {
                    dcs = GSM_DCS_STORE_DEFAULT_ALPHABET_ACTIVE_VMN;
                }
            }
            msg.setDCS(dcs);
        }
        if (unit.getConfig().isBearingNetworkCdma2000()) {
            msg.setCount(count);
        }

        return msg;
    }
}

