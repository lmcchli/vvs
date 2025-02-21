/**
 * Copyright (c) 2010, Abcxyz
 * All Rights Reserved
 */
package com.mobeon.common.sms.request;

import com.mobeon.common.sms.SMSClient;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.SMSUnit;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.smscom.udh.ApplicationPort16Bits;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Visual Voice Mail (VVM) request. 
 */
public class VVMRequest extends Request { 

    private String message;
    private int notificationType;
    private UserInfo user;
    private final byte[] byteMessage;
        
    public VVMRequest(SMSAddress from, SMSAddress to, UserInfo user, int validity, SMSResultHandler resultHandler, int id, String message, byte[] payload, int delay, int notificationType) { 
        super(from, to, validity, resultHandler, id);  
        this.message = message;
        this.byteMessage = payload;
        this.delay = delay;
        this.notificationType = notificationType;
        this.user = user;
    }

    public SMSMessage getSMSMessage(SMSUnit unit) {
        //SMSMessage msg = unit.getConverter().unicodeToMessage(message, unit.getConfig().getSmsStringLength() * unit.getConfig().getNumberOfSms());
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
        if(notificationType == SMSClient.TYPE_APPLEVVM_DEP || notificationType == SMSClient.TYPE_APPLEVVM_GRE){
            msg.setPID(0x7d);
        }else{
            msg.setPID(0x00);
        }
        msg.setDCS(Request.GSM_DCS_GENERAL_DATA_CODING_INDICATION_8_BIT_DATA);
        msg.setPriority(unit.getConfig().getSmsPriority());
        
        if (notificationType == SMSClient.TYPE_VVM_DEP || notificationType == SMSClient.TYPE_VVM_GRE || notificationType == SMSClient.TYPE_APPLEVVM_DEP || notificationType == SMSClient.TYPE_APPLEVVM_GRE ){
            msg.setReplace(true);
        } else {
            msg.setReplace(false);
        }
        msg.setServiceType(unit.getConfig().getVvmServiceType()); 
        msg.addIEtoUDH(new ApplicationPort16Bits(unit.getConfig().getVvmSourcePort(), user.getVvmDestinationPort()));
        msg.adjustFragziseForUDH();
        return msg;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
