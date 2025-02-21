/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2014.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.userinfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.userinfo.esi.ExternalSubscriberInformation;
import com.mobeon.ntf.userinfo.mcd.McdUserInfo;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.complexattributes.ComplexAttributeException;
import com.abcxyz.services.moip.common.complexattributes.ComplexAttributeHelperFactory;
import com.abcxyz.services.moip.common.complexattributes.IComplexAttributeHelper;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;


/**
 * MwiUnsubscribedUserInfo contains the information of a deleted subscriber.
 * It contains the minimal information to be able to send a MWIOFF message
 * to the subscriber's device.
 * <p>
 * This class is necessary to avoid calling the user registry to get the user's
 * profile because it has been deleted. It is used in the context where a 
 * subscriber has been deleted but the system needs to turn off the message
 * waiting indicator.
 *</p>
 *
 * @author egeobli
 */
public class MwiUnsubscribedUserInfo implements UserInfo, Constants {
    
    /** Subscriber telephone number */
    private String telephoneNumber;
    
    /** Notification filter */
    private NotificationFilter filter;
    
    /** Subscriber notification number */
    private String notifNumber;
    
    /** Message Logger */
    private static final LogAgent logAgent = NtfCmnLogger.getLogAgent(MwiUnsubscribedUserInfo.class.getName());
    
    /**
     * Constructs a MwiUnsubscribedUserInfo using a email information.
     * 
     *  @param email Notification email containing the user information.
     */
    public MwiUnsubscribedUserInfo(NotificationEmail email) {
        //
        // Set phone number
        //
        telephoneNumber = email.getReceiverPhoneNumber();
        NtfEvent ntfEvent = email.getNtfEvent();



        //
        // Setting notification numbers
        //

        String[] notifNumbers = getProperties(ntfEvent, DAConstants.ATTR_NOTIF_NUMBER);
        if (notifNumbers == null) {
            notifNumber = telephoneNumber;
        } else {
            notifNumber = notifNumbers[0];
        }
        
        
        // Create MoIP formated notification filter  
        String[] unformatedFilterStrings = getProperties(ntfEvent, DAConstants.ATTR_FILTER);
        String[] unformatedRoamFilterStrings = getProperties(ntfEvent, DAConstants.ATTR_ROAMING_FILTER);

        String[] mwiUnsubscribedNumbers = getProperties(ntfEvent, MoipMessageEntities.SERVICE_TYPE_MWI_OFF_UNSUBSCRIBED_NUMBERS);

        String[] formatedFilterStrings = null;
        if (unformatedFilterStrings != null ) {
            // We need to convert filterStrings to the internal format understood by NTF.
            HashMap<String, String[]> map = new HashMap<String, String[]>(unformatedFilterStrings.length);
            map.put(DAConstants.ATTR_FILTER, unformatedFilterStrings);
            ArrayList<HashMap<String, String[]>> list = new ArrayList<HashMap<String, String[]>>();
            list.add(map);

            IComplexAttributeHelper helper = ComplexAttributeHelperFactory.getInstance().createHelper(DAConstants.ATTR_FILTER);

            //format the non roaming filter
            try {
                formatedFilterStrings = helper.assembleComplexAttribute(list);
            } catch (ComplexAttributeException ex) {
                // Can't do much. Report the error and create a dummy filter
                if (logAgent.isDebugEnabled()) {
                    // Concatenate filter strings
                    StringBuilder filters = new StringBuilder();
                    for (String s : unformatedFilterStrings) {
                        filters.append("[");
                        filters.append(s);
                        filters.append("]");
                    }
                    logAgent.debug("Filter string is invalid for unsubscribed subscriber: " + filters.toString());
                }
            } 
        }
        
        
        String[] formatedRoamFilterStrings = null;
        if (unformatedRoamFilterStrings != null) {
            // We need to convert filterStrings to the internal format understood by NTF.
            HashMap<String, String[]> map = new HashMap<String, String[]>(unformatedRoamFilterStrings.length);
            map.put(DAConstants.ATTR_FILTER, unformatedRoamFilterStrings);
            ArrayList<HashMap<String, String[]>> list = new ArrayList<HashMap<String, String[]>>();
            list.add(map);

            IComplexAttributeHelper helper = ComplexAttributeHelperFactory.getInstance().createHelper(DAConstants.ATTR_FILTER);

            //format the roaming filter(s).
            try {
                formatedRoamFilterStrings = helper.assembleComplexAttribute(list);
            } catch (ComplexAttributeException ex) {
                // Can't do much. Report the error and create a dummy filter
                if (logAgent.isDebugEnabled()) {
                    // Concatenate filter strings
                    StringBuilder filters = new StringBuilder();
                    for (String s : unformatedRoamFilterStrings) {
                        filters.append("[");
                        filters.append(s);
                        filters.append("]");
                    }
                    logAgent.debug("Roaming filter string is invalid for unsubscribed subscriber: " + filters.toString());
                }
            }
        }

        //Note the delivery Profile is already sent to us in MoIP/NTF format. As the Business rule 
        //needs to figure out what numbers if any changed in the profile.
        String[] deliveryprofiles = getProperties(ntfEvent, DAConstants.ATTR_DELIVERY_PROFILE);

        // Create the notification filter.
        filter = new NotificationFilter(formatedFilterStrings, formatedRoamFilterStrings, false, this, deliveryprofiles, mwiUnsubscribedNumbers);        
    }
    
    /**
     * Returns profile properties in a string array.
     * @param NtfEvent NTF event to extract the properties from.
     * @param key Property name.
     * @return Array of strings or null if no property exists.
     */
    private static String[] getProperties(NtfEvent ntfEvent, String key) {        
        return ntfEvent.getMultilineProperty(key);
    }

    /**
     * Returns an empty string.
     * 
     * @return Empty string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getFullId()
     */
    @Override
    public String getFullId() {
        return "";
    }

    /**
     * Returns a tag for the deleted subscriber.
     * 
     * @return Subscriber mail tag.
     * @see com.mobeon.ntf.userinfo.UserInfo#getMail()
     */
    @Override
    public String getMail() {
        return telephoneNumber + "-deleted";
    }

    /**
     * Returns the user's telephone number.
     * @return Telephone number 
     * @see com.mobeon.ntf.userinfo.UserInfo#getTelephoneNumber()
     */
    @Override
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    /**
     * Returns the notification filter.
     * @return Notification filter object.
     * @see com.mobeon.ntf.userinfo.UserInfo#getFilter()
     */
    @Override
    public NotificationFilter getFilter() {
        return filter;
    }

    /**
     * Returns notification expiry time.
     * @return Returns 6 hours
     * @see com.mobeon.ntf.userinfo.UserInfo#getNotifExpTime()
     */
    @Override
    public int getNotifExpTime() {
        return 6;
    }

    /**
     * Returns validity flash time
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_flash()
     */
    @Override
    public int getValidity_flash() {
        return 0;
    }

    /**
     * Returns SMS type 0 validity period
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_smsType0()
     */
    @Override
    public int getValidity_smsType0() {
        return 0;
    }

    /**
     * Returns MWI ON validity period.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_mwiOn()
     */
    @Override
    public int getValidity_mwiOn() {
        return 0;
    }

    /**
     * Returns validity period for MWI OFF
     * @return 6
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_mwiOff()
     */
    @Override
    public int getValidity_mwiOff() {
        return 6;
    }

    /**
     * Returns validity for Mail Quota Exceeded SMS.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_mailQuotaExceeded()
     */
    @Override
    public int getValidity_mailQuotaExceeded() {
        return 0;
    }

    /**
     * Returns mail quota high level exceeded SMS validity period
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_mailQuotaHighLevelExceeded()
     */
    @Override
    public int getValidity_mailQuotaHighLevelExceeded() {
        return 0;
    }

    /**
     * Returns temporary greeting on reminder validity period.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_temporaryGreetingOnReminder()
     */
    @Override
    public int getValidity_temporaryGreetingOnReminder() {
        return 0;
    }

    /**
     * Returns voice mail off reminder validity period.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_voicemailOffReminder()
     */
    @Override
    public int getValidity_voicemailOffReminder() {
        return 0;
    }

    /**
     * Returns CFU ON reminder validity period.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_cfuOnReminder()
     */
    @Override
    public int getValidity_cfuOnReminder() {
        return 0;
    }

    /**
     * Returns slamdown validity period.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_slamdown()
     */
    @Override
    public int getValidity_slamdown() {
        return 0;
    }

    /**
     * Returns VVM System Deactivated validity period.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity_vvmSystemDeactivated()
     */
    @Override
    public int getValidity_vvmSystemDeactivated() {
        return 0;
    }

    /**
     * Returns 0
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getValidity(java.lang.String)
     */
    @Override
    public int getValidity(String name) {
        return 0;
    }

    /**
     * Returns null
     * Returns the subscriber's notification number.
     * @return Notification number.
     * @see com.mobeon.ntf.userinfo.UserInfo#getNotifNumber()
     */
    @Override
    public String getNotifNumber() {
        return notifNumber;
    }

    /**
     * Returns Numbering Plan.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getNumberingPlan()
     */
    @Override
    public int getNumberingPlan() {
        return 0;
    }

    /**
     * Returns the quota.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getQuota()
     */
    @Override
    public int getQuota() {
        return 0;
    }

    /**
     * Returns mail quota
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getNoOfMailQuota()
     */
    @Override
    public int getNoOfMailQuota() {
        return 0;
    }

    /**
     * Returns voice mail quota.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getNoOfVoiceMailQuota()
     */
    @Override
    public int getNoOfVoiceMailQuota() {
        return 0;
    }

    /**
     * Returns fax mail quota.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getNoOfFaxMailQuota()
     */
    @Override
    public int getNoOfFaxMailQuota() {
        return 0;
    }

    /**
     * Returns video mail quota.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getNoOfVideoMailQuota()
     */
    @Override
    public int getNoOfVideoMailQuota() {
        return 0;
    }

    /**
     * Returns quota warning level.
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getQuotaWarnLvl()
     */
    @Override
    public int getQuotaWarnLvl() {
        return 0;
    }

    /**
     * Returns preferred date format.
     * @return Empty string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getPreferredDateFormat()
     */
    @Override
    public String getPreferredDateFormat() {
        return "";
    }

    /**
     * Returns preferred time format.
     * @return Empty string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getPreferredTimeFormat()
     */
    @Override
    public String getPreferredTimeFormat() {
        return "";
    }

    /**
     * Returns brand.
     * @return Empty string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getBrand()
     */
    @Override
    public String getBrand() {
        return "";
    }

    /**
     * Returns user date.
     * @return Empty string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getUsersDate(java.util.Date)
     */
    @Override
    public String getUsersDate(Date d) {
        return "";
    }

    /**
     * Returns user time.
     * @return Empty string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getUsersTime(java.util.Date)
     */
    @Override
    public String getUsersTime(Date d) {
        return "";
    }

    /**
     * Returns type of number.
     * @return 0.
     * @see com.mobeon.ntf.userinfo.UserInfo#getTypeOfNumber()
     */
    @Override
    public int getTypeOfNumber() {
        return 0;
    }

    /**
     * Returns true if in business time.
     * @return Always return true.
     * @see com.mobeon.ntf.userinfo.UserInfo#isBusinessTime(java.util.Calendar)
     */
    @Override
    public boolean isBusinessTime(Calendar cal) {
        return true;
    }

    /**
     * Returns true if the user is an administrator.
     * @return false.
     * @see com.mobeon.ntf.userinfo.UserInfo#isAdministrator()
     */
    @Override
    public boolean isAdministrator() {
        return false;
    }

    /**
     * Returns if the user is a fax user.
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isFaxOnlyUser()
     */
    @Override
    public boolean isFaxOnlyUser() {
        return false;
    }

    /**
     * Returns the preferred language.
     * @return Empty string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getPreferredLanguage()
     */
    @Override
    public String getPreferredLanguage() {
        return "";
    }

    /**
     * Returns the same value as getMail().
     * @return Identity string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getLogin()
     */
    @Override
    public String getLogin() {
        return getMail();
    }

    /**
     * Returns whether the user is a multi-line user.
     * @return false.
     * @see com.mobeon.ntf.userinfo.UserInfo#isMultilineUser()
     */
    @Override
    public boolean isMultilineUser() {
        return false;
    }

    /**
     * Returns whether the user is an outdial user.
     * @return false.
     * @see com.mobeon.ntf.userinfo.UserInfo#isOutdialUser()
     */
    @Override
    public boolean isOutdialUser() {
        return false;
    }

    /**
     * Returns the outdial schema.
     * @return Empty String.
     * @see com.mobeon.ntf.userinfo.UserInfo#getOutdialSchema()
     */
    @Override
    public String getOutdialSchema() {
        return "";
    }

    /**
     * Returns whether the user is a NWI user.
     * @return true.
     * @see com.mobeon.ntf.userinfo.UserInfo#isMwiUser()
     */
    @Override
    public boolean isMwiUser() {
        return true;
    }

    /**
     * Returns true if the user has autoprint of faxes enabled. 
     * @return false.
     * @see com.mobeon.ntf.userinfo.UserInfo#isAutoPrintFax()
     */
    @Override
    public boolean isAutoPrintFax() {
        return false;
    }

    /**
     * Returns whether the user should receive system messages via SMS. 
     * @return false.
     * @see com.mobeon.ntf.userinfo.UserInfo#isSystemMessageSMS()
     */
    @Override
    public boolean isSystemMessageSMS() {
        return false;
    }

    /**
     * Returns whether the user should receive system messages via TUI. 
     * @return false.
     * @see com.mobeon.ntf.userinfo.UserInfo#isSystemMessageTUI()
     */
    @Override
    public boolean isSystemMessageTUI() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isQuotaPerType()
     */
    @Override
    public boolean isQuotaPerType() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isNotificationServiceEnabled(int)
     */
    @Override
    public boolean isNotificationServiceEnabled(int type) {
        if (type == NTF_MWI) {
            //this class is used to force send an MWI off so indicate service is available.
            return true;
        }
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isNotifTypeUnused(java.lang.String)
     */
    @Override
    public boolean isNotifTypeUnused(String type) {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isNotifTypeOnFilter(java.lang.String)
     */
    @Override
    public boolean isNotifTypeOnFilter(String type) {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasMailType(int)
     */
    @Override
    public boolean hasMailType(int type) {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasUnreadMessageReminder()
     */
    @Override
    public boolean hasUnreadMessageReminder() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasUpdateSms()
     */
    @Override
    public boolean hasUpdateSms() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasReplace()
     */
    @Override
    public boolean hasReplace() {
        return false;
    }

    /**
     * Does nothing.
     * @see com.mobeon.ntf.userinfo.UserInfo#terminalSupportsReplace(boolean)
     */
    @Override
    public void terminalSupportsReplace(boolean supported) {
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#terminalSupportsReplace()
     */
    @Override
    public boolean terminalSupportsReplace() {
        return false;
    }

    /**
     * Does nothing.
     * @see com.mobeon.ntf.userinfo.UserInfo#terminalSupportsMwi(boolean)
     */
    @Override
    public void terminalSupportsMwi(boolean supported) {

    }

    /**
     * Returns whether the terminal support MWI.
     * @return true
     * @see com.mobeon.ntf.userinfo.UserInfo#terminalSupportsMwi()
     */
    @Override
    public boolean terminalSupportsMwi() {
        return true;
    }

    /**
     * Does nothing.
     * @see com.mobeon.ntf.userinfo.UserInfo#terminalSupportsFlash(boolean)
     */
    @Override
    public void terminalSupportsFlash(boolean supported) {

    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#terminalSupportsFlash()
     */
    @Override
    public boolean terminalSupportsFlash() {
        return false;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getServices()
     */
    @Override
    public String[] getServices() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getPnc()
     */
    @Override
    public String getPnc() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getUserNtd()
     */
    @Override
    public String getUserNtd() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getExternalSubscriberInformation()
     */
    @Override
    public ExternalSubscriberInformation getExternalSubscriberInformation() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getTemporaryGreeting()
     */
    @Override
    public String getTemporaryGreeting() {
        return null;
    }

    /**
     * @return Empty string.
     * @see com.mobeon.ntf.userinfo.UserInfo#getTimeZone()
     */
    @Override
    public String getTimeZone() {
        return "";
    }

    /**
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getDivertAll()
     */
    @Override
    public int getDivertAll() {
        return 0;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getCosName()
     */
    @Override
    public String getCosName() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getFaxPrintNumber()
     */
    @Override
    public String getFaxPrintNumber() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getInboundFaxNumber()
     */
    @Override
    public String getInboundFaxNumber() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getVvmClientType()
     */
    @Override
    public String getVvmClientType() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getVvmClientPrefix()
     */
    @Override
    public String getVvmClientPrefix() {
        return null;
    }

    /**
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getVvmDestinationPort()
     */
    @Override
    public int getVvmDestinationPort() {
        return 0;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isPrepaid()
     */
    @Override
    public boolean isPrepaid() {
        return false;
    }

    /**
     * Does nothing
     * @see com.mobeon.ntf.userinfo.UserInfo#setPrePaid(boolean)
     */
    @Override
    public void setPrePaid(boolean prepaid) {

    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getMsid()
     */
    @Override
    public String getMsid() {
        return null;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasVvmService()
     */
    @Override
    public boolean hasVvmService() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isVVMNotificationAllowed()
     */
    @Override
    public boolean isVVMNotificationAllowed() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isVVMActivated()
     */
    @Override
    public boolean isVVMActivated() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isVVMSystemActivated()
     */
    @Override
    public boolean isVVMSystemActivated() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isVVMAppleClient()
     */
    @Override
    public boolean isVVMAppleClient() {
        return false;
    }

    /**
     * @return true
     * @see com.mobeon.ntf.userinfo.UserInfo#hasSpecialSMSMessageIndicationService()
     */
    @Override
    public boolean hasSpecialSMSMessageIndicationService() {
        return true;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasMOIPCnService()
     */
    @Override
    public boolean hasMOIPCnService() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#isVvmNotificationAllowedWhileRoaming()
     */
    @Override
    public boolean isVvmNotificationAllowedWhileRoaming() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasForwardToEmailService()
     */
    @Override
    public boolean hasForwardToEmailService() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasMcnSubscribedService()
     */
    @Override
    public boolean hasMcnSubscribedService() {
        return false;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#getMcnSubscribedState()
     */
    @Override
    public String getMcnSubscribedState() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getHomeSystemID()
     */
    @Override
    public String getHomeSystemID() {
        return null;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getImapPassword()
     */
    @Override
    public String getImapPassword() {
        return null;
    }

    /**
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getBadLoginCount()
     */
    @Override
    public int getBadLoginCount() {
        return 0;
    }

    /**
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getMaxLoginLockout()
     */
    @Override
    public int getMaxLoginLockout() {
        return 0;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasAutoUnlockPinEnabled()
     */
    @Override
    public boolean hasAutoUnlockPinEnabled() {
        return false;
    }

    /**
     * @return 0
     * @see com.mobeon.ntf.userinfo.UserInfo#getAutoUnlockPinDelay()
     */
    @Override
    public int getAutoUnlockPinDelay() {
        return 0;
    }

    /**
     * @return false
     * @see com.mobeon.ntf.userinfo.UserInfo#hasAutoUnlockPinSmsEnabled()
     */
    @Override
    public boolean hasAutoUnlockPinSmsEnabled() {
        return false;
    }

    /**
     * @return null
     * @see com.mobeon.ntf.userinfo.UserInfo#getLastPinLockoutTime()
     */
    @Override
    public Date getLastPinLockoutTime() {
       return null;
    }

    @Override
    public boolean hasDeposiType(depositType type) {
        return false;
    }

}
