/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.abcxyz.messaging.provisioningagent.utils.PAConstants;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;
import com.mobeon.ntf.userinfo.esi.ExternalSubscriberInformation;

/**
 * This class is an empty implementation of the UserInfo interface. Test classes
 * and other classes which provide a small subset of the data, can extend this
 * class to simplify implementation.
 */
public class UserInfoAdapter implements UserInfo {
    static int id = 0;
    protected int myId;
    protected String telephoneNumber;
    protected boolean terminalCapabilityReplace = true;
    protected boolean terminalCapabilityMwi = true;
    protected boolean terminalCapabilityFlash = true;


    public UserInfoAdapter() {
        myId = ++id;
        telephoneNumber = "123456" + id;
    }

    /** @return a full identification of the user, e.g. the distinguished name
     * in an LDAP user directory. */
    public String getFullId() {
        return "userinfoadapter" + myId;
    }

    /** @return the users mail address. */
    public String getMail() {
        return "userinfoadapter" + myId;
    }

    /** @return the users telephone number. */
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    /** @return the users notification filter. */
    public NotificationFilter getFilter() {
        return null;
    }

    /** @return the users notification filter. */
    public NotificationFilter getRoamingFilter(boolean subFilter) {
        return null;
    }

    /** @return the id of the users MMS center. */
    public String getMmsCenterId() {
        return null;
    }

    /** @return the expiration time of the users notifications (in hours). */
    public int getNotifExpTime() {
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_flash() {
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_smsType0() {
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_mwiOn() {
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_mwiOff() {
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_mailQuotaExceeded() {
        return 6;
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_mailQuotaHighLevelExceeded(){
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_temporaryGreetingOnReminder() {
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_voicemailOffReminder() {
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_cfuOnReminder() {
        return 6;
    }

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_slamdown() {
        return 6;
    }

    @Override
    public int getValidity_vvmSystemDeactivated() {
        return 6;
    }

    /**
     * Gets a validity by the name of a kind of SMS.
     *@param name - the name of the SMS kind.
     *@return the validity.
     */
    public int getValidity(String name) {
        return 6;
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

    /** @return the users telephone number for notifications. */
    public String getNotifNumber() {
        return telephoneNumber;
    }

    /** @return The numbering plan of the users telephone number. */
    public int getNumberingPlan() {
        return Config.getNumberingPlanIndicator();
    }

    /** @return the date format preferred by the user (yyyy/mm/dd or mm/dd/yyy). */
    public String getPreferredDateFormat() {
        return Config.getDefaultDateFormat();
    }

    /** @return the time format preferred by the user (12 or 24). */
    public String getPreferredTimeFormat() {
        return Config.getDefaultTimeFormat();
    }

     public String getUsersDate(Date d) {
         if (d == null) {
             return "";
         }

        SimpleDateFormat fmt=null;
        fmt = new SimpleDateFormat(Config.getDefaultDateFormat().replace('m', 'M'));

        String tz=null;
        tz=getTimeZone();

        if (tz != null){
            TimeZone zone=TimeZone.getTimeZone(tz);;
            fmt.setTimeZone(zone);
        }

        return fmt.format(d);
    }

    /**@return the time formatted according to the users preferences.*/
    public String getUsersTime(Date d) {
        if (d == null) {
            return "";
        }

        SimpleDateFormat fmt=null;
        if ("12".equals(Config.getDefaultTimeFormat())) {
            fmt = new SimpleDateFormat("hh:mm a");
        } else { //24
            fmt = new SimpleDateFormat("HH:mm");
        }

        String tz=null;
        tz=getTimeZone();

        if (tz != null){
            TimeZone zone=TimeZone.getTimeZone(tz);;
            fmt.setTimeZone(zone);
        }

            return fmt.format(d);
    }

    /**
     * Find the next SMSC to use. If the allowed SMSCs are in the configuration file,
     * the SMSC is selected from one of those, otherwise it is selected from one of all
     * ShortMessage components in MCR.
     *@return the id of the users SMS center.
     */
    public String getSmscId() {
        return null;
    }

    /** @return The type of the users telephone number. */
    public int getTypeOfNumber() {
        return Config.getTypeOfNumber();
    }

    /** @return -1 denoting unlimited quota. */
    public int getQuota() {
        return -1;
    }

    /** @return -1 denoting unlimited quota. */
    public int getNoOfMailQuota() {
        return -1;
    }


    public int getNoOfVoiceMailQuota() {
        return -1;
    }
    public int getNoOfFaxMailQuota() {
        return -1;
    }
    public int getNoOfVideoMailQuota() {
        return -1;
    }

    public int getQuotaWarnLvl()
    {
        return -1;
    }

    public boolean isQuotaPerType(){
    	return false;
    }

    /** @param cal a time.
     * @return true iff cal is a business time according to the users business
     * weekday and time settings. */
    public boolean isBusinessTime(Calendar cal) {
        return false;
    }
    public boolean hasForwardToEmailService()
    {
        return false;
    }

    /** @return true iff the users mails are delivered to the mailbox, (and not
     * only forwarded for example). */
    public boolean isMailboxDelivery() {
        return true;
    }

    public boolean isFaxOnlyUser() { return false; }

    /** @return true iff the user is an administrator. */
    public boolean isAdministrator() {
        return false;
    }

    /** @return returns a string denoting the users preferred language. */
    public String getPreferredLanguage() {
        return "en";
    }

    /** @return returns the login name of the user. */
    public String getLogin() {
        return "Non-mur";
    }

    /** @return the id of the users WAP gateway. */
    public String getWapGatewayId() {
        return null;
    }

    /** @return true iff the user is enabled for outdial notifications. */
    public boolean isOutdialUser() {
        return false;
    }

    /** @return Name of schema, for mailusers always assume "default". */
    public String getOutdialSchema()
    {
        return "default";
    }

    /** @return true iff the user is enabled for multiline. */
    public boolean isMultilineUser() {
        return false;
    }

    /** @return true if the user is enabled for mwi notifications. */
    public boolean isMwiUser() {
        return false;
    }

    /** @return true if the user has autoprint of faxes enabled. */
    public boolean isAutoPrintFax() {
        return false;
    }

    /** @return the subscribers external prefix, if any */
    public String getSubscriberExternalPrefix() {
        return "";
    }

    /**
     *@param type notification type as defined by Constants.
     *@return NotifState enabled.
     */
    public NotifState isNotifTypeDisabledOnUser(int type) {
        return NotifState.ENABLED;
    }

    public boolean isNotifTypeOnFilter(String type) {
        return false;
    }

    /**
     * Checks if a notif type would never be used for the user.  If the notif
     * type is not selected in any filter or it is unconditionally disabled, it
     * is unused.
     *@param type - notification type as defined by Constants.
     *@return true if it is certain that the notif type is never used for the
     * user, false if it is used or might be used in some situations.
     */
    public boolean isNotifTypeUnused(String type) {
        return false;
    }

    /**
     *@param type the requested mail type (email, voice mail, fax mail)
     *@return true iff the user can handle the mail type.
    */
    public boolean hasMailType(int type) {
        return true;
    }

    public boolean hasUnreadMessageReminder() {
        return true;
    }

    public boolean hasUpdateSms() {
        return true;
    }

    public boolean hasReplace() {
        return true;
    }

    public void terminalSupportsReplace(boolean supported) {
        terminalCapabilityReplace = supported;
    }

    public boolean terminalSupportsReplace() {
        return terminalCapabilityReplace;
    }

    public void terminalSupportsMwi(boolean supported) {
        terminalCapabilityMwi = supported;
    }

    public boolean terminalSupportsMwi() {
        return terminalCapabilityMwi;
    }

    public void terminalSupportsFlash(boolean supported) {
        terminalCapabilityFlash = supported;
    }

    public boolean terminalSupportsFlash() {
        return terminalCapabilityFlash;
    }

    /** @return a list of the services available to the user. */
    public String[] getServices() {
        return new String[0];
    }

    /** @return the pager notification controlstring. */
    public String getPnc() {
        return "";
    }

    /** @return the emUserNTD value. */
    public String getUserNtd() {
        return "";
    }

    public ExternalSubscriberInformation getExternalSubscriberInformation() {
        return null;
    }

    public String getTemporaryGreeting() {
        return null;
    }

    public String getTimeZone() {
        return null;
    }

    public int getDivertAll() {
        return 0;
    }

    public String getCosName() {
        return null;
    }

    public String getFaxPrintNumber() {
        return null;
    }

    /**
     * Gets the users inbound fax number.
     *@return the users fax  number.
     */
    public String getInboundFaxNumber() {
        return null;
    }

	@Override
	public String getBrand() {
		return null;
	}

	public String getVvmClientPrefix() {
		return "//VVM";
	}

	public int getVvmDestinationPort() {
		return 5070;
	}

	@Override
	public boolean isPrepaid() {
		return false;
	}

	@Override
	public void setPrePaid(boolean prepaid) {

	}

	@Override
	public String getMsid() {
		return "";
	}

    @Override
    public boolean hasVvmService() {
        return true;
    }



    @Override
    public String getVvmClientType() {
        return "OMTP";
    }

    @Override
    public boolean hasSpecialSMSMessageIndicationService() {
        return true;
	}

	@Override
	public boolean isNotificationServiceEnabled(int type) {
		return true;
	}

    @Override
    public boolean isVVMNotificationAllowed(){
        return true;
    }

    @Override
    public boolean isVVMActivated() {
        return true;
    }

    @Override
    public boolean isVVMSystemActivated() {
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
        return 6;
    }

    @Override
    public boolean hasAutoUnlockPinEnabled() {
        return false;
    }

    @Override
    public int getAutoUnlockPinDelay() {
        return 72;
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
        return true;
    }
    
}
