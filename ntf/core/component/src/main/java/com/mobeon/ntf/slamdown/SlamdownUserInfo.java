package com.mobeon.ntf.slamdown;

import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;
import com.mobeon.ntf.userinfo.esi.ExternalSubscriberInformation;

import java.util.Date;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: enikfyh
 * Date: 2007-dec-03
 * Time: 16:00:41
 */
public class SlamdownUserInfo implements UserInfo {

    private UserInfo murUser = null;

    private String cosName;
    private String origDestinationNumber;
    private String notifNumber;
    private String preferredLanguage;
    private boolean prepaid = false;



    private void getMurUser() {
        murUser = UserFactory.findUserByTelephoneNumber(origDestinationNumber);
    }

    public String getFullId() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getFullId();
    }

    public String getTelephoneNumber() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getTelephoneNumber();
    }

    public NotificationFilter getFilter() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getFilter();
    }

    public int getNotifExpTime() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getNotifExpTime();
    }

    public int getValidity_flash() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_flash();
    }

    public int getValidity_smsType0() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_smsType0();
    }

    public int getValidity_mwiOn() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_mwiOn();
    }

    public int getValidity_mwiOff() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_mwiOff();
    }

    public int getValidity_mailQuotaExceeded() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_mailQuotaExceeded();
    }

    public int getValidity_mailQuotaHighLevelExceeded(){
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_mailQuotaHighLevelExceeded();
    }



    public int getValidity_temporaryGreetingOnReminder() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_temporaryGreetingOnReminder();
    }

    public int getValidity_voicemailOffReminder() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_voicemailOffReminder();
    }

    public int getValidity_cfuOnReminder() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_cfuOnReminder();
    }
    public boolean isQuotaPerType()
    {
        if( murUser == null ) { getMurUser(); }
        return murUser.isQuotaPerType();

    }
   public int getNoOfVoiceMailQuota() {
       if( murUser == null ) { getMurUser(); }
       return murUser.getNoOfVoiceMailQuota();
   }
   public int getNoOfFaxMailQuota() {
       if( murUser == null ) { getMurUser(); }
       return murUser.getNoOfFaxMailQuota();
   }
   public int getNoOfVideoMailQuota() {
       if( murUser == null ) { getMurUser(); }
       return murUser.getNoOfVideoMailQuota();
   }

   public int getQuotaWarnLvl()
   {
       if( murUser == null ) { getMurUser(); }
       return murUser.getQuotaWarnLvl();
   }
    public int getValidity_slamdown() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_slamdown();
    }

    @Override
    public int getValidity_vvmSystemDeactivated() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity_vvmSystemDeactivated();
    }

    public int getValidity(String name) {
        if( murUser == null ) { getMurUser(); }
        return murUser.getValidity(name);
    }

    public String getNotifNumber() {
        return notifNumber;
    }

    public int getNumberingPlan() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getNumberingPlan();
    }

    public int getQuota() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getQuota();
    }

    public int getNoOfMailQuota() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getNoOfMailQuota();
    }

    public String getPreferredDateFormat() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getPreferredDateFormat();
    }

    public String getPreferredTimeFormat() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getPreferredTimeFormat();
    }

    public String getUsersDate(Date d) {
        if( murUser == null ) { getMurUser(); }
        return murUser.getUsersDate(d);
    }

    public String getUsersTime(Date d) {
        if( murUser == null ) { getMurUser(); }
        return murUser.getUsersTime(d);
    }

    public int getTypeOfNumber() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getTypeOfNumber();
    }

    public boolean isBusinessTime(Calendar cal) {
        if( murUser == null ) { getMurUser(); }
        return murUser.isBusinessTime(cal);
    }

    public boolean isAdministrator() {
        if( murUser == null ) { getMurUser(); }
        return murUser.isAdministrator();
    }

    public boolean isFaxOnlyUser() {
        if( murUser == null ) { getMurUser(); }
        return murUser.isFaxOnlyUser();
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getLogin() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getLogin();
    }

    public boolean isMultilineUser() {
        if( murUser == null ) { getMurUser(); }
        return murUser.isMultilineUser();
    }

    public boolean isOutdialUser() {
        if( murUser == null ) { getMurUser(); }
        return murUser.isOutdialUser();
    }

    public String getOutdialSchema() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getOutdialSchema();
    }

    public boolean isMwiUser() {
        if( murUser == null ) { getMurUser(); }
        return murUser.isMwiUser();
    }

    public boolean isAutoPrintFax() {
        if( murUser == null ) { getMurUser(); }
        return murUser.isAutoPrintFax();
    }

    public NotifState isNotifTypeDisabledOnUser(int type) {
        if( murUser == null ) { getMurUser(); }
        //FIXME does this need to check roaming, if so which number(s).
        //I don't think this is used anyway.
        return murUser.getFilter().isNotifTypeDisabledOnUser(type,null);
    }

    public boolean isNotifTypeUnused(String type) {
        if( murUser == null ) { getMurUser(); }
        return murUser.isNotifTypeUnused(type);
    }

    public boolean isNotifTypeOnFilter(String type) {
        if( murUser == null ) { getMurUser(); }
        return murUser.isNotifTypeOnFilter(type);
    }

    public boolean hasMailType(int type) {
        if( murUser == null ) { getMurUser(); }
        return murUser.hasMailType(type);
    }
    
    public boolean hasDepositType(depositType type) {
        if( murUser == null ) { getMurUser(); }
        return murUser.hasDeposiType(type);
    }


    public boolean hasUnreadMessageReminder() {
        if( murUser == null ) { getMurUser(); }
        return murUser.hasUnreadMessageReminder();
    }

    public boolean hasUpdateSms() {
        if( murUser == null ) { getMurUser(); }
        return murUser.hasUpdateSms();
    }

    public boolean hasReplace() {
        if( murUser == null ) { getMurUser(); }
        return murUser.hasReplace();
    }

    public void terminalSupportsReplace(boolean supported) {
        if( murUser == null ) { getMurUser(); }
        murUser.terminalSupportsReplace(supported);
    }

    public boolean terminalSupportsReplace() {
        if( murUser == null ) { getMurUser(); }
        return murUser.terminalSupportsReplace();
    }

    public void terminalSupportsMwi(boolean supported) {
        if( murUser == null ) { getMurUser(); }
        murUser.terminalSupportsMwi(supported);
    }

    public boolean terminalSupportsMwi() {
        if( murUser == null ) { getMurUser(); }
        return murUser.terminalSupportsMwi();
    }

    public void terminalSupportsFlash(boolean supported) {
        if( murUser == null ) { getMurUser(); }
        murUser.terminalSupportsFlash(supported);
    }

    public boolean terminalSupportsFlash() {
        if( murUser == null ) { getMurUser(); }
        return murUser.terminalSupportsFlash();
    }

    public String[] getServices() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getServices();
    }

    public String getPnc() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getPnc();
    }

    public String getUserNtd() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getUserNtd();
    }

    public ExternalSubscriberInformation getExternalSubscriberInformation() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getExternalSubscriberInformation();
    }

    public String getTemporaryGreeting() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getTemporaryGreeting();
    }

    public String getTimeZone() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getTimeZone();
    }

    public int getDivertAll() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getDivertAll();
    }

    public String getCosName() {
        return cosName;
    }

    public String getFaxPrintNumber() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getFaxPrintNumber();
    }

    public String getInboundFaxNumber() {
        if( murUser == null ) { getMurUser(); }
        return murUser.getInboundFaxNumber();
    }

    public void setCosName(String cosName) {
        this.cosName = cosName;
    }

    @SuppressWarnings("unused")
    public void setMail(String mail) {
    }

    public void setOrigDestinationNumber(String number){
    	origDestinationNumber = number;
    }

    public void setNotifNumber(String notifNumber) {
        this.notifNumber = notifNumber;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
    /** @return true if the user should receive system messages via SMS. */
    public boolean isSystemMessageSMS()
    {
        return false;
    }

    /** @return true if the user should receive system messages via TUI. */
    public boolean isSystemMessageTUI()
    {
        return false;

    }
    public boolean hasForwardToEmailService()
    {
        return false;
    }

	@Override
	public String getMail() {
		return null;
	}

	@Override
	public String getBrand() {
        if( murUser == null ){
        	getMurUser();
        }
        if(murUser != null) {
		return murUser.getBrand();
        }else {
        	return null;
        }
	}

	@Override
	public String getVvmClientPrefix() {
		return null;
	}

	@Override
	public int getVvmDestinationPort() {
		return 0;
	}

	@Override
	public boolean isPrepaid() {
		return prepaid;
	}

	@Override
	public void setPrePaid(boolean prepaidStatus) {
		prepaid = prepaidStatus;
	}

	@Override
	public String getMsid() {
		return murUser.getMsid();
	}

    @Override
    public boolean hasVvmService() {
        return false;
    }



    @Override
    public String getVvmClientType() {
        return null;
    }

    @Override
    public boolean hasSpecialSMSMessageIndicationService() {
        return false;
	}

	@Override
	public boolean isNotificationServiceEnabled(int type) {
        if( murUser == null ) { getMurUser(); }
        return murUser.isNotificationServiceEnabled(type);
	}
    @Override
    public boolean isVVMNotificationAllowed(){
        return true;
    }
    @Override
    public boolean isVVMActivated(){
        return true;
    }

    @Override
    public boolean isVVMSystemActivated(){
        return true;
    }

    @Override
    public boolean isVVMAppleClient(){
        return false;
    }

    @Override
    public boolean hasMOIPCnService() {
        return true;
    }

    @Override
    public boolean isVvmNotificationAllowedWhileRoaming() {
        return false;
    }
    
    @Override
    public boolean hasMcnSubscribedService() {
        return false; 
    }

    @Override
    public String getMcnSubscribedState() {
        return null;
    }
    
    @Override
    public String getHomeSystemID() {
        return null;
    }
    
    @Override
    public String getImapPassword() {
        return null;
    }

    @Override
    public int getBadLoginCount() {
        return 0;
    }

    @Override
    public int getMaxLoginLockout() {
        return 0;
    }

    @Override
    public boolean hasAutoUnlockPinEnabled() {
        return false;
    }

    @Override
    public int getAutoUnlockPinDelay() {
        return 0;
    }
    
    @Override
    public boolean hasAutoUnlockPinSmsEnabled() {
        return true;
    }

    @Override
    public Date getLastPinLockoutTime() {
        return null;
    }

    @Override
    public boolean hasDeposiType(depositType type) {
        if( murUser == null ) { getMurUser(); }
        return murUser.hasDeposiType(type);
    }

}
