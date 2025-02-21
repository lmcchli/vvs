/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;


import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.esi.ExternalSubscriberInformation;

import java.util.*;

/**
 * This interface specifies the API available to NTF for accessing user
 * data. It should be the "perfect" and most convenient view of user data, so it
 * will probably change somewhat with each major version of NTF.
 *
 * The implementations will provide the information requested by this interface,
 * by getting data from different sources and combine, convert or just fill in
 * defaults as well as possible.
 *
 * A new NTF with a new version of this interface that shows the possibilities
 * of a new user directory can still be used with an old version of the user
 * directory by updating the implementation for that user directory to the new
 * UserInfo interface,
 */
public interface UserInfo {
    
    public enum NotifState {ENABLED,DISABLED,RETRY,FAILED}
    
    /** @return a full identification of the user, e.g. the distinguished name
     * in an LDAP user directory. */
    public String getFullId();

    /** @return the users mail address. */
    public String getMail();

    /** @return users UsersDevice object for a specific number. */
    //public UserDevice getDeviceInformation(String telephonenumber);

    /** @return UserDevice for all the users billing numbers. */
    //public Vector getDevices();

    /** @return the users telephone number. */
    public String getTelephoneNumber();

    /** @return the users notification filter. */
    public NotificationFilter getFilter();

    /** @return the expiration time of the users notifications (in hours). */
    public int getNotifExpTime();

    /** @return the validity of flash SMS messages. */
    public int getValidity_flash();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_smsType0();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_mwiOn();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_mwiOff();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_mailQuotaExceeded();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_mailQuotaHighLevelExceeded();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_temporaryGreetingOnReminder();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_voicemailOffReminder();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_cfuOnReminder();

    /** @return the validity of SMS messages of a certain kind (in hours). */
    public int getValidity_slamdown();

    /** @return the validity of SMS messages of kind VVM System Deactivated (in hours). */
    public int getValidity_vvmSystemDeactivated();

    /**
     * Gets a validity by the name of a kind of SMS.
     *@param name - the name of the SMS kind.
     *@return the validity.
     */
    public int getValidity(String name);

    /** @return the users telephone number for notifications. */
    public String getNotifNumber();

    /** @return The numbering plan of the users telephone number. */
    public int getNumberingPlan();

    /** @return the users mail quota in bytes */
    public int getQuota();

    /** @return the users quota for number of messages in mailbox, -1 if no quota */
    public int getNoOfMailQuota();


    /** @return the users quota for number of voice  messages in mailbox, -1 if no quota */
   public int getNoOfVoiceMailQuota();
   /** @return the users quota for number of fax  messages in mailbox, -1 if no quota */
   public int getNoOfFaxMailQuota();
   /** @return the users quota for number of video  messages in mailbox, -1 if no quota */
   public int getNoOfVideoMailQuota();
   /** @return The warning level for quota, -1 if not configured */
   public int getQuotaWarnLvl();

    /** @return the date format preferred by the user (yyyy/mm/dd or mm/dd/yyy). */
    public String getPreferredDateFormat();

    /** @return the time format preferred by the user (12 or 24). */
    public String getPreferredTimeFormat();

    /**@return a string denoting the users language extension */
    public String getBrand();

    /**@return the date formatted according to the users preferences.*/
    public String getUsersDate(Date d);

    /**@return the time formatted according to the users preferences.*/
    public String getUsersTime(Date d);

    /** @return The type of the users telephone number. */
    public int getTypeOfNumber();

    /** @param cal a time.
     * @return true iff cal is a business time according to the users business
     * weekday and time settings. */
    public boolean isBusinessTime(Calendar cal);

    /** @return true iff the user is an administrator. */
    public boolean isAdministrator();

    /** @return true iff the user is a fax-only used. */
    public boolean isFaxOnlyUser();

    /** @return returns a string denoting the users preferred language. */
    public String getPreferredLanguage();

    /** @return returns the login name of the user. */
    public String getLogin();

    /** @return true iff the user is enabled for multiline. */
    public boolean isMultilineUser();

    /** @return true iff the user is enabled for outdial notifications. */
    public boolean isOutdialUser();

    /** @return name of users outdial schema, "default" if none is specified */
    public String getOutdialSchema();

    /** @return true if the user is enabled for mwi notifications. */
    public boolean isMwiUser();

    /** @return true if the user has autoprint of faxes enabled. */
    public boolean isAutoPrintFax();

    /** @return true if the user should receive system messages via SMS. */
    public boolean isSystemMessageSMS();

    /** @return true if the user should receive system messages via TUI. */
    public boolean isSystemMessageTUI();

    /** @return true if the user quota per message type enable enabled. */
    public boolean isQuotaPerType();

    /**
     *@param type notification type as defined by Constants.
     *@return true if the notification type is part of the Services.
     */
    public boolean isNotificationServiceEnabled(int type);

    /**
     * Checks if a notif type would never be used for the user.  If the notif
     * type is not selected in any filter or it is unconditionally disabled, it
     * is unused.
     *@param type - notification type as defined by Constants.
     *@return true if it is certain that the notif type is never used for the
     * user, false if it is used or might be used in some situations.
     */
    public boolean isNotifTypeUnused(String type);

    /**
     *Checks if the user has the notification type on his filter.
     *Checks for all active filters for the specified type.
     *@param type the type to check for.
     *@return true if the type exists on a active filter, false otherwise.
     */
    public boolean isNotifTypeOnFilter(String type);

    /**
     *@param type the requested mail type (email, voice mail, fax mail)
     *@return true iff the user can handle the mail type.
     *@deprecated use hadDepositType instead.
    */
    public boolean hasMailType(int type);
    
    /**
     *@param type the requested mail type (email, voice mail, fax mail)
     *@return true iff the user can handle the mail type.
    */
    public boolean hasDeposiType(depositType type);

    /** @return true if the sum of all service and user settings is that the
        user shall have reminders of unread messages. */
    public boolean hasUnreadMessageReminder();

    /** @return true if the sum of all service and user settings is that the
        user shall have update SMS when appropriate. */
    public boolean hasUpdateSms();

    /** @return true if the sum of all service and user settings is that SMS
        replace is not disabled for the user. */
    public boolean hasReplace();

    /** Set the replace capability of the users terminal.
     *@param supported whether replace is supported or not. */
    public void terminalSupportsReplace(boolean supported);

    /** @return true if the users terminal supports replace. */
    public boolean terminalSupportsReplace();

    /** Set the MWI capability of the users terminal.
     *@param supported whether MWI is supported or not. */
    public void terminalSupportsMwi(boolean supported);

    /** @return true if the users terminal supports MWI. */
    public boolean terminalSupportsMwi();

    /** Set the flash capability of the users terminal.
     *@param supported whether flash is supported or not. */
    public void terminalSupportsFlash(boolean supported);

    /** @return true if the users terminal supports SMS class 0. */
    public boolean terminalSupportsFlash();

    /** @return a list of the services available to the user. */
    public String[] getServices();

    /** @return the pager notification controlstring. */
    public String getPnc();

    /** @return the emUserNTD value. */
    public String getUserNtd();

    /** @return ExternalSubscriberInformation for the user */
    public ExternalSubscriberInformation getExternalSubscriberInformation();

    /** @return the emTmpGrt attribute */
    public String getTemporaryGreeting();

    /** @return the prefered timezone for the user, subscribertimezone in MUR */
    public String getTimeZone();

    /** @return true if the user has cfu enabled to MoIP */
    public int getDivertAll();

    /** @return the name of the cos */
    public String getCosName();

    /** @return the number to the users default fax */
    public String getFaxPrintNumber();

    /** @return the users fax  number. */
    public String getInboundFaxNumber();

    /** @return the users vvm client type */
    public String getVvmClientType();

    /** @return the users vvm client prefix string. */
    public String getVvmClientPrefix();

    /** @return the users vvm destination */
    public int getVvmDestinationPort();

    /** @return whether the user is prepaid **/
    public boolean isPrepaid();

    /** Sets the user to prepaid or postpaid **/
    public void setPrePaid(boolean prepaid);

    /** @return the msid of the subscriber **/
    public String getMsid();

    /** @return true if the user has the VVM services. */
    public boolean hasVvmService();

    public boolean isVVMNotificationAllowed();

    /** @return true if the user has the VVM feature activated */
    public boolean isVVMActivated();

    /**
     * @return true if the VVM feature is "system activated" for this user. false means the VVM feature has been temporarily disabled
     *         by the system for this user.
     */
    public boolean isVVMSystemActivated();

    /**
     *
     * @return true if the user has an Apple phone
     */
    public boolean isVVMAppleClient();

    /** @return true if the user has the Special SMS Message Indication services. */
    public boolean hasSpecialSMSMessageIndicationService();

    /** @return true if the user has teh CNService MOIP */
    public boolean hasMOIPCnService();

    /** @return true if the user allows VVM "Notifications allowed while roaming" */
    boolean isVvmNotificationAllowedWhileRoaming();

    /** @return true if the user allows to forward message to email" */
    public boolean hasForwardToEmailService();
    
    /**
     * @return true if the user has MCN-Subscribed service enabled.
     */
    public boolean hasMcnSubscribedService();

    /**
     * Returns the user's MCN-Subscribed configuration value (off, single caller
     * per SMS or Multiple caller per SMS). 
     * @return Returns a string representing the state of the MCN-Subscribed or null
     *         if not available.
     */
    public String getMcnSubscribedState();
    
    /** @return the subscriber home system id */
    public String getHomeSystemID();
    
    /** @return the imap server password */
    public String getImapPassword();
    
    /** @return the current number of failed subscriber login attempts since last successful login */
    public int getBadLoginCount();
    
    /** @return the subscriber's maximum number of login attempts allowed */
    public int getMaxLoginLockout();
    
    /** @return true if the user is enabled for Auto-Unlock Pin  */
    public boolean hasAutoUnlockPinEnabled();
    
    /** @return the delay in hours until a user's TUI account is unlocked if {@link UserInfo#hasAutoUnlockPinEnabled()} is enabled*/
    public int getAutoUnlockPinDelay();
    
    /** @return true if the user is enabled SMS notification for Auto-Unlock Pin  */
    public boolean hasAutoUnlockPinSmsEnabled();
    
    /** @return the last time the user was locked out or null if the attribute does not exist or can't be parsed. 
     * This value is not adjusted for the user's timezone. */
    public Date getLastPinLockoutTime();
    
}
