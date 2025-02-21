package com.abcxyz.services.moip.ntf.out.sms;

import com.abcxyz.messaging.common.mnr.SubscriberInfo;
import com.abcxyz.messaging.common.ssmg.AnyTimeInterrogationResult;
import com.abcxyz.messaging.common.ssmg.interfaces.AlertSCHandler;
import com.abcxyz.services.moip.common.ss7.ISs7Manager;
import com.abcxyz.services.moip.common.ss7.Ss7Exception;

public class Ss7ManagerStub implements ISs7Manager {

    @Override
    public SubscriberInfo getSubscriberInfo(String aPhoneNumber) 
            throws Ss7Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cancelConditionalDivertInHlr(String phoneNumber)
            throws Ss7Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void cancelUnconditionalDivertInHlr(String phoneNumber)
            throws Ss7Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean getDivertStatusInHlr(String phoneNumber, String divertType)
            throws Ss7Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getImsi(String phoneNumber) throws Ss7Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRoamingStatus_ATI(String aPhoneNumber){
        return -1;
    }

    @Override
    public Boolean isRoaming(String phoneNumber) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean isRoaming_ATI(String phoneNumber) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean isRoaming_ATI(AnyTimeInterrogationResult result){
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean isRoaming_SRI(String phoneNumber) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean isRoamingSRI(String phoneNumber)throws Ss7Exception {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Boolean registerAlertScHandler(AlertSCHandler alertSCHandler)
            throws Ss7Exception {
        // TODO Auto-generated method stub
        return true;

    }


    @Override
    public Boolean registerAlertScHandlerWithRetry(AlertSCHandler alertSCHandler,int alertSCRegistrationNumOfRetry,int alertSCRegistrationTimeInSecBetweenRetry){
        return true;
    }





    @Override
    public AnyTimeInterrogationResult requestATI(String phoneNumber)
            throws Ss7Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendMtForwardSM(String msisdn, String imsi, String mscAddress) throws Ss7Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void sentReportSMDeliveryStatus(String msisdn) throws Ss7Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void setConditionalDivertInHlr(String phoneNumber)
            throws Ss7Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void setUnconditionalDivertInHlr(String phoneNumber)
            throws Ss7Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean useHlr(){
        return true;
    }

    @Override
    public int getSubscriberRoamingStatus(String s) 
            throws Ss7Exception{return 0;}

    @Override
    public String getSubStatusHlrInterrogationMethod() {return "ati";}



}
