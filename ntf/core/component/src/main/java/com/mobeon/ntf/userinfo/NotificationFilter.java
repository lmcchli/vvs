/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.mail.IMessageDepositInfo;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.phonedetection.PhoneStatus;
import com.mobeon.ntf.phonedetection.PhoneStatus.State;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;
import com.mobeon.ntf.util.CommaStringTokenizer;
import com.mobeon.ntf.NotificationGroup;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Class to parse and check notification filters on a user
 * profile.
 * 
 * Contains both roaming and home filters.
 *
 */
public class NotificationFilter implements Constants {
    private static LogAgent log = NtfCmnLogger.getLogAgent(NotificationFilter.class);
    
    static final String[] enptyStringArray = new String[0]; //saves unnecessary GC.
    private static final FilterPart[] defaultParts;
    static {
        //add default if active.
        FilterPart part1 =  new FilterPart(Config.getDefaultNotificationFilter());
        FilterPart part2 =  new FilterPart(Config.getDefaultNotificationFilter());
        defaultParts=new FilterPart[2];
        defaultParts[0]=part1;
        defaultParts[1]=part2;
    }

    private List<DeliveryProfile> _deliveryProfileList;
    private FilterPart[] homeParts = null;
    private FilterPart[] roamParts = null;
    private boolean notifDisabled = false;
    private FilterPart match= defaultParts[0];
    private IMessageDepositInfo lastEmail = null;
    private Calendar lastWhen = null;
    private UserInfo user = null;

    private boolean usingDefaultFilter = false;

    private boolean usingRoamingSet = false; //indicates we have scanned a roaming filter set (getMatchingFilterPart) at some point.

    private String[] mwiUnsubscribedNumbers; // this is used only for MWI unsubscribed event, and indicates a set of numbers to send MWI off to

    
    /**
     * Extra constructor without delivery profile strings to make the test classes compile
     */
    public NotificationFilter(String[] partStrings, boolean disabled, UserInfo user) {
        this(partStrings, disabled, user, null);
    }
    
    /**
     * Constructor that parses the filter strings for both non-roaming and roaming
     * and prepares to decide about the users notifications, 
     * @param newFilterStrings strings specifying the filter parts.
     * @param roamPartStrings -specific roaming filter , if empty then will ignore them.
     * @param disabled true iff notification is disabled.
     * @param user information about the user.
     * @param deliveryProfileStrings the emDeliveryProfile values for the user.
     */
    public NotificationFilter(String[] newFilterStrings, String[] roamPartStrings, boolean disabled, UserInfo user,
            String[] deliveryProfileStrings) {
        this(newFilterStrings,roamPartStrings,disabled,user,deliveryProfileStrings,null);
    }

   
    
    public NotificationFilter(String[] partStrings, String[] roamPartStrings, boolean disabled, UserInfo user,
            String[] deliveryProfileStrings,String[] mwiUnsubscribedNumbers) {
        
        _deliveryProfileList = new ArrayList<DeliveryProfile>();
        this.user=user;

        log.debug("Creating filter with notification " + (disabled? "disabled": "enabled") + " for user " + user.getLogin());
        notifDisabled= disabled;
        if (notifDisabled) {
            return;
        }
        
        //ensure not null so we don;t need to do a null check later on each test..
        if (partStrings == null) {
            partStrings = enptyStringArray;
        }
        if (roamPartStrings == null) {
            roamPartStrings = enptyStringArray;
        }
        if (deliveryProfileStrings == null) {
            deliveryProfileStrings = enptyStringArray;
        }
        

        if (disabled) return; //filters are null.
        
        //do home filters.
        if (partStrings.length == 0 || partStrings[0].isEmpty()) {
            usingDefaultFilter=true;
            homeParts=new FilterPart[defaultParts.length];
            for (int i = 0; i < defaultParts.length; i++) {
                homeParts[i]=defaultParts[i];
            }         
        } else {
            homeParts=new FilterPart[partStrings.length];
            for (int i= 0; i < partStrings.length; i++) {
                homeParts[i]= new FilterPart(partStrings[i]);
            }
        }
         
        Arrays.sort(homeParts); //Sort home filters by priority
        
        if (roamPartStrings.length > 0 && !roamPartStrings[0].isEmpty()) {
        //do roaming filters.
        roamParts= new FilterPart[roamPartStrings.length]; //create roaming filter parts.
        for (int i= 0; i <roamPartStrings.length; i++) {
            roamParts[i]= new FilterPart(roamPartStrings[i]);
        }
        
        Arrays.sort(roamParts); //Sort roam filters after priority
        
        } else {
            roamParts = new FilterPart[0]; //empty..
        }
        

        
        //do the delivery profiles.
        for(int i = 0; i < deliveryProfileStrings.length; i++) {
            _deliveryProfileList.add(
                    new DeliveryProfile(deliveryProfileStrings[i]));
        }
        
        //This is generally used only for mwi unsubscribed to limit the numbers to send to, when specific number(s) are removed from
        //the mcd profile.
        this.mwiUnsubscribedNumbers = mwiUnsubscribedNumbers;
    }

    /**
     * Constructor that parses the filter strings and prepares to decide about
     * the users notifications.
     * @param partStrings strings specifying the filter parts.
     * @param disabled true iff notification is disabled.
     * @param user information about the user.
     * @param deliveryProfileStrings the emDeliveryProfile values for the user
     */
    public NotificationFilter(String[] partStrings, boolean disabled, UserInfo user,
            String[] deliveryProfileStrings) {

       this(partStrings,null,disabled,user,deliveryProfileStrings);
    }


    /**
     * Tells if notification is completely disabled for this user.
     *@return true iff the user has notification disabled.
     */
    public boolean isNotifDisabled() {
        log.debug("isNotifDisable is " + notifDisabled);
        return notifDisabled;
    }
    
    /**
     * @param type - notification type as defined by Constants.
     * @return NotifState.
     */
    public NotifState isNotifTypeDisabledOnUser(int type,String telNumber) {

        String userNTD = user.getUserNtd();
        if (userNTD != null) {
            switch (type) {
                case NTF_SMS:
                    return isDisabled("SMS",telNumber,userNTD);
                case NTF_MWI:
                    return isDisabled("MWI",telNumber,userNTD);
                case NTF_MMS:
                    return isDisabled("MMS",telNumber,userNTD);
                case NTF_ODL:
                    return isDisabled("ODL",telNumber,userNTD);
                case NTF_WAP:
                    return isDisabled("WAP",telNumber,userNTD);
                case NTF_PAG:
                    return isDisabled("PAG",telNumber,userNTD);
                case NTF_CMW:
                    return isDisabled("CMW",telNumber,userNTD);
                case NTF_FLS:
                    return isDisabled("FLS",telNumber,userNTD);
                case NTF_EML:
                    return isDisabled("EML",telNumber,userNTD);
                default:
                    log.warn("isNotifTypeDisabledOnUser - unknown type assuming enabled.");
                    return NotifState.ENABLED;
            }
            
        } else {
            //non - listed as disabled.
            return NotifState.ENABLED;
        }
    }
    
    /**
     * Checks the users disabled list to see if
     * type exists..
     * 
     * @param value the value to check for
     * @return true if the value exists
     * 
     */
    public boolean doesUserNTDContain(String value)
    {
        String userNTD = user.getUserNtd();
        //regular expression for an exact match between commas if any.
        Pattern typeMatcher = Pattern.compile("(?<=,|^)"+value+"(?=,|$)");
        return(typeMatcher.matcher(userNTD).find());
    }
    
    /*
     * Checks if notification "type" is disabled for a telNumber.
     * If using HLR (SS7 or CUSTOM) for roaming checks also confirm specific home (H-) and roaming (R-) are not disabled.
     * Otherwise Roaming and Home disable are ignored.
     */
    private NotifState isDisabled(String type,String telNumber, String userNtd) {
        NotifState status = NotifState.ENABLED;
        
        /* Explicitly check for type disabled, not prefixed by a -R or -H for first check
         * If doing a a simple indexOf or contains, will match R- H- prefix to mean fully disabled.
         * Instead make sure type starts with nothing comma or ends with , or nothing using regular expression.
         */
        Pattern typeMatcher = Pattern.compile("(?<=,|^)"+type+"(?=,|$)");
        if (typeMatcher.matcher(userNtd).find()) {
            // if fully disabled no need to do roaming check.
            return NotifState.DISABLED;
        }
        
        //check roaming..
        boolean checkRoaming = (telNumber!=null && Config.getCheckRoaming());

        //if using HLR SS7 or custom plug-in to check roaming, check for roaming and home specific disabled.
        if (checkRoaming) {
            if (userNtd.indexOf(MOIPUserNTD_ROAM_PREFIX + type) != -1) {
                State roaming = PhoneStatus.getPhoneStatus(telNumber).isRoaming();
                if (roaming == PhoneStatus.State.ERROR || roaming == PhoneStatus.State.NONE) {
                    log.info("Unable to determine Roaming Status for type R-" + type + "subscriber: " + telNumber);
                    status = checkHlrFailAction(true);
                    if (status != NotifState.ENABLED) {
                        //if status is enabled continue to next check..
                        //otherwise return disabled or retry..
                        return status;
                    }
                }
                else if (roaming == PhoneStatus.State.YES) {
                    log.debug(type+" is roaming and roaming notification disabled " + MOIPUserNTD_ROAM_PREFIX + type + " for " + telNumber);
                    return NotifState.DISABLED;
                } 

            } 
            if (userNtd.indexOf(MOIPUserNTD_HOME_PREFIX + type) != -1) {               
                State roaming = PhoneStatus.getPhoneStatus(telNumber).isRoaming();
                if (roaming == PhoneStatus.State.ERROR || roaming == PhoneStatus.State.NONE ) {
                    log.info("Unable to determine Roaming Status, for type H-" + type + "subscriber: " + telNumber);
                    return(checkHlrFailAction(false));
                }
                
                else if (roaming == PhoneStatus.State.NO) {
                    log.debug(type+" is home and home notification disabled [" + MOIPUserNTD_HOME_PREFIX + type + "] for " + telNumber );
                    return NotifState.DISABLED;
                } 
            }
        }
        return status;
    }
    
  private NotifState checkHlrFailAction(boolean roamingCheck) {
        
        hlrFailAction action = Config.getHLRRoamingFailureAction();
        log.info("checkHlrFailAction " + NotificationConfigConstants.HLR_ROAM_FAILURE_ACTION + "- failure action:" + action );
        switch (action) {
        case RETRY:
            log.info("checkHlrFailAction - notification retry.");
          return NotifState.RETRY;
          
        case FAIL:
            log.info("checkHlrFailAction - notification disabled.");
            return NotifState.FAILED;
            
        //assume subscriber is roaming.    
        case ROAM: 
            if (roamingCheck) {
                log.info("checkHlrFailAction - forced assume ROAMING when roaming check failed - notification disabled.");
                return NotifState.DISABLED;
            }      
        case HOME: //send home only on fail
            if (!roamingCheck) {
                log.info("checkHlrFailAction - forced HOME when Roaming check failed roaming check - notification disabled.");
                return NotifState.DISABLED;
            }
        default:
            log.info("checkHlrFailAction - unknown failure action: " + action + " assuming retry." );
            return NotifState.RETRY;
        }       
    }

    /**
     *Checks if the user has the notification type on any of his filter.
     *Checks for all active filters for the specified type.
     *@param type the type to check for.
     *@return true if the type exists on a active filter, false otherwise.
     */
    public boolean isNotifTypeOnFilter(String type) {
        for(int i=0;i<homeParts.length;i++ ) {
            FilterPart p = homeParts[i];
            if( p.active == true ) {
                if( p.contentForType.getProperty(type) != null ) {
                    log.debug("isNotifTypeOnFilter is true");
                    return true;
                }
            }

        }
        for(int i=0;i<roamParts.length;i++ ) {
            FilterPart p = roamParts[i];
            if( p.active == true ) {
                if( p.contentForType.getProperty(type) != null ) {
                    log.debug("isNotifTypeOnFilter is true");
                    return true;
                }
            }
        }
        log.debug("isNotifTypeOnFilter is false");
        return false;
    }
    
    /**
     *Checks if the user has the notification type on his home filter.
     *Checks for all active filters for the specified type.
     *@param type the type to check for.
     *@return true if the type exists on a active filter, false otherwise.
     */
    public boolean isNotifTypeOnHomeFilter(String type) {
        for(int i=0;i<homeParts.length;i++ ) {
            FilterPart p = homeParts[i];
            if( p.active == true ) {
                if( p.contentForType.getProperty(type) != null ) {
                    log.debug("isNotifTypeOnFilter is true");
                    return true;
                }
            }          
        }
        return false;
    }
    
    /**
     *Checks if the user has the notification type on his roaming filter.
     *Checks for all active filters for the specified type.
     *@param type the type to check for.
     *@return true if the type exists on a active filter, false otherwise.
     */
    public boolean isNotifTypeOnRoamFilter(String type) {
        if (roamParts.length > 0) {
            for(int i=0;i<roamParts.length;i++ ) {
                FilterPart p = roamParts[i];
                if( p.active == true ) {
                    if( p.contentForType.getProperty(type) != null ) {
                        log.debug("isNotifTypeOnFilter is true");
                        return true;
                    }
                }
            }
        } else {
            for(int i=0;i<homeParts.length;i++ ) {
                FilterPart p = homeParts[i];
                if( p.active == true ) {
                    if( p.contentForType.getProperty(type) != null ) {
                        log.debug("isNotifTypeOnFilter is true");
                        return true;
                    }
                }
            }
        }

        log.debug("isNotifTypeOnFilter is false");
        return false;
    }

    /**
     * Checks if the class of service the user belongs to has the service enabled for this notification type
     * @param type The notification type.
     * @return true If the service is active
     */
    public boolean hasNotificationService(int type, IMessageDepositInfo email) {
        if (email == null) {
            log.warn("hasNotificationService: called with null IMessageDepositInfo, return false");
            return false;
        }
        if (!email.isNewMessageNotification()) { return false; } //Only message

        boolean result = user.isNotificationServiceEnabled(type);
        log.debug("isNotificationServiceEnabled for type " + type + " is " + result);
        return result; 
    }

    /**
     *Checks if the notification type is disabled and takes action.
     *@param type - The notification type.
     *@param fh - feedback for retry callback.
     *@return true if disabled or can't determine, false if not disabled.
     */
    public boolean handleDisablings(int type, IMessageDepositInfo email, FeedbackHandler fh, String TelNumber) {
        
        if(email == null) {
            log.warn("handleDisablings called with no IMessageDepositInfo, return disabled true.");
            return true; //assume disabled - no email
        }
        
        if (!email.isNewMessageNotification()) { return false; } //Only message
        //notifications
        //can be disabled
        
        if (email.isMwiOffUnsubscribed()) {
            return false; //don't check if disabled for an unsubscribed as we want to force mwi off and this should for the most part have been checked by the business rule.
        }
        
        boolean reminderNotif=false;
        if (email instanceof NotificationEmail) {
            if ( ((NotificationEmail)email).isReminderNotification() ) {
                reminderNotif=true;
            }
        }
        
        NotifState state;
        if (reminderNotif == true) {
            state = isNotifTypeDisabledOnUser(type,TelNumber);
            String allowedDisablings = Config.getReminderUseNtdType();
            if(NotificationConfigConstants.REMINDER_NTD_FULL_ONLY.equalsIgnoreCase(allowedDisablings)) {
                state = isNotifTypeDisabledOnUser(type,null);  //if telephone null only checks fully disabled.              
            } else if (NotificationConfigConstants.REMINDER_NTD_ALL.equalsIgnoreCase(allowedDisablings)) {
                state = isNotifTypeDisabledOnUser(type,TelNumber);
            } else { //(NotificationConfigConstants.REMINDER_NTD_NONE.equalsIgnoreCase(allowedDisablings) only three at the moment.
                return false; //don't check moipUserNTD so not disabled.
            }
        } else {
           state = isNotifTypeDisabledOnUser(type,TelNumber);
        }
        
        switch (state) {
            case DISABLED:
                log.debug("isNotifTypeDisabledOnUser - returning true");
                return true;
            case ENABLED:
                log.debug("isNotifTypeDisabledOnUser - returning false");
                return false;
            case RETRY:
                if( fh != null ) {
                    log.info("isNotifTypeDisabledOnUser for type " + type + " will retry.");
                    if (fh instanceof NotificationGroup) {
                        NotificationGroup ng = (NotificationGroup) fh;
                        ng.increaseTempCount(user, 1 ); //normal counter will not increase in this case
                    }
                    fh.retry(user, type, "Failed to determine roaming-location");
                } else {
                    log.warn("isNotifTypeDisabledOnUser for type " + type + " failed check, unable to retry this message as feedback handler is null - dropping.");
                    return true;
                }
            default:
                log.error("isNotifTypeDisabledOnUser() Unknown Notification state: " + state + " assuming failed.");
                //fall through.
            case FAILED:
                if( fh != null ) {
                    log.info("isNotifTypeDisabledOnUser for type " + type + " failed.");
                    if (fh instanceof NotificationGroup) {
                        NotificationGroup ng = (NotificationGroup)fh;
                        ng.increaseTempCount(user, 1 ); //normal counter will not increase in this case
                    }
                    fh.failed(user, type, "Failed to determine roaming-location");
                } else {
                    log.warn("isNotifTypeDisabledOnUser for type " + type + " failed check, unable to retry this message as feedback handler is null - dropping.");
                }
                return true;
        }
    }

    /**
     * Gets the numbers that NTF will notify for a given notification type.
     *No check is done if the user would have been notified or not.
     *@param notifType The name of the notification, e.g. "SMS"
     * @param email - the notification email, or null if not used.
     *@return return the numbers to notify to.
     */
    
    public String[] getNotifNumbers(String notifType, IMessageDepositInfo email) {
        if (notifDisabled) {
            log.debug("getNotifNumbers, notifDisabled is TRUE");
            return new String[0];
        }
        

        String[] numbers = getMatchingDeliveryProfileNumbers(notifType);
        if (numbers == null) {
            String notifNumber = user.getNotifNumber();
            if (notifNumber != null  && !notifNumber.isEmpty())
                numbers = new String[] { notifNumber };
            else
                if (email != null) {
                    numbers = new String[] {email.getReceiverPhoneNumber()};
                } else {
                    return new String[0];
                }
            
        }

        log.debug("getNotifNumbers for notifType " + notifType + ": " + Arrays.toString(numbers));
        return numbers;
    }
    
    /**
     * Gets the numbers that NTF will notify for a given notif type.
     *No check is done if the user would have been notified or not.
     *@param notifType The name of the notification, e.g. "SMS"
     *@return return the numbers to notify to as a list.
     */
    public ArrayList<String> getNotifNumberList(String notifType, NotificationEmail email) {
        ArrayList<String> numbers = new ArrayList<String>();
        if (notifDisabled) {
            log.debug("getNotifNumbers, notifDisabled is TRUE");
            return numbers;
        }

        getMatchingDeliveryProfileNumbers(notifType,numbers);
        if (numbers.isEmpty()) {
            String notifNumber = user.getNotifNumber();
            if (notifNumber != null  && !notifNumber.isEmpty())
                numbers.add(notifNumber);
            else
                numbers.add(email.getReceiverPhoneNumber());
        }

        log.debug("getNotifNumbers for notifType " + notifType + ": " + numbers.toString());
        return numbers;
    }
    


    /**
     * Gets the numbers that NTF will notify for a given notif type and transport.
     * @param notifType The name of the notification, e.g. "SMS"
     * @param transport The transport to use, e.g. Mobile
     * @param email The IMessageDepositInfo
     * @return return the numbers to notify to.
     */
    public String[] getNotifNumbers(String notifType, int transport, IMessageDepositInfo email) {
        String[] numbers = getNotifNumbers(notifType, transport, email, true);

        log.debug("getNotifNumbers for notifType: " + notifType + ", transport: " + transport + ": " + Arrays.toString(numbers));
        return numbers;
    }
    
    /**
     * Gets the numbers that NTF will notify for a given notif type and transport.
     * @param notifType The name of the notification, e.g. "SMS"
     * @param transport The transport to use, e.g. Mobile
     * @param email The IMessageDepositInfo
     * @return return the numbers to notify to.
     */
    public ArrayList<String> getNotifNumbersList(String notifType, int transport, IMessageDepositInfo email) {
        ArrayList<String> notifNumbers = new ArrayList<String>();
        
        getNotifNumbers(notifType, transport, email, true, notifNumbers);

        log.debug("getNotifNumbers for notifType: " + notifType + ", transport: " + transport + ": " + notifNumbers.toString());
        return notifNumbers;
    }

    /**
     * Gets the numbers that NTF will notify for a given notif type and transport.
     * @param notifType The name of the notification, e.g. "SMS"
     * @param transport The transport to use, e.g. Mobile
     * @param email IMessageDepositInfo
     * @param checkNotifDisabled to check notification disabled or not
     * @return return the numbers to notify to.
     */
    public String[] getNotifNumbers(String notifType, int transport, IMessageDepositInfo email, boolean checkNotifDisabled) {
        return getNotifNumbers(notifType, transport, email.getReceiverPhoneNumber(), checkNotifDisabled);
    }
    
    /**
     * Gets the numbers that NTF will notify for a given notif type and transport.
     * @param notifType The name of the notification, e.g. "SMS"
     * @param transport The transport to use, e.g. Mobile
     * @param email IMessageDepositInfo
     * @param checkNotifDisabled to check notification disabled or not
     * @param notifNumbers list to populate with notification Numbers.
     */
    public void getNotifNumbers(String notifType, int transport, IMessageDepositInfo email, boolean checkNotifDisabled,
            ArrayList<String> notifNumbers) {
        
        getNotifNumbers(notifType, transport, email.getReceiverPhoneNumber(), checkNotifDisabled,notifNumbers);
    }   

    /**
     * Gets the numbers that NTF will notify for a given notif type and transport.
     * @param notifType The name of the notification, e.g. "SMS"
     * @param transport The transport to use, e.g. Mobile
     * @param receiverPhoneNumber The phone number that received the message
     * @param checkNotifDisabled whether to check notification disabled or not
     * @return return the numbers to notify to.
     */
    public String[] getNotifNumbers(String notifType, int transport, String receiverPhoneNumber, boolean checkNotifDisabled) {
        if (checkNotifDisabled && notifDisabled){
            log.debug("getNotifNumbers, checkNotifDisabled: " + checkNotifDisabled + " notifDisabled: " + notifDisabled);
            return new String[0];
        }

        String[] numbers = getMatchingDeliveryProfileNumbers(notifType, transport, checkNotifDisabled);
        if( numbers == null ) {
            String notifNumber = user.getNotifNumber();
            if ( notifNumber != null  && !notifNumber.isEmpty() )
                numbers = new String[] { notifNumber };
            else
                numbers = new String[] {receiverPhoneNumber};
        }

        log.debug("getNotifNumbers for notifType: " + notifType + ", transport: " + transport + ": " + Arrays.toString(numbers));
        return numbers;
    }
    
    /**
     * Gets the numbers that NTF will notify for a given notif type and transport.
     * @param notifType The name of the notification, e.g. "SMS"
     * @param transport The transport to use, e.g. Mobile
     * @param receiverPhoneNumber The phone number that received the message
     * @param checkNotifDisabled whether to check notification disabled or not
     * @param notifNumbers list to populate with notification Numbers.
     */
    private void getNotifNumbers(String notifType, int transport, String receiverPhoneNumber, boolean checkNotifDisabled,
            ArrayList<String> notifNumbers) {
        if (checkNotifDisabled && notifDisabled){
            log.debug("getNotifNumbers, checkNotifDisabled: " + checkNotifDisabled + " notifDisabled: " + notifDisabled);
            return;
        }

        getMatchingDeliveryProfileNumbers(notifType, transport, checkNotifDisabled,notifNumbers);
        if( notifNumbers.isEmpty() ) {
            String notifNumber = user.getNotifNumber();
            if ( notifNumber != null  && !notifNumber.isEmpty() )
                notifNumbers.add(notifNumber);
            else
                notifNumbers.add(receiverPhoneNumber);
        }

        log.debug("getNotifNumbers for notifType: " + notifType + ", transport: " + transport + ": " + notifNumbers.toString());      
    }
    
    /**
     * Gets information for SMS notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification shall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error), then retry or fail is tagged.
     *@return Information for SMS notification, or null if there shall be no SMS
     * notification.
     */
    public SmsFilterInfo getSmsFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {
        
        if (email == null) {
            return null;
        }
        
        if (notifDisabled) {
            log.debug("getSmsFilterInfo, notifDisabled: " + notifDisabled);
            return null;
        }
        
        boolean hasSMS=hasNotificationService(NTF_SMS,email);
        boolean hasMWI=hasNotificationService(NTF_MWI,email);
        if (!hasSMS && !hasMWI ) {
            return null; //no checking if both types are disabled.
        }

        Properties contentType=null;
        
        //get list of smsNumbers that are currently enabled. NOTE: does not check roaming at this point
        String[] smsNumbers = null;
        if (hasSMS && (!handleDisablings(NTF_SMS, email, fh, null))) {
            smsNumbers = getMatchingDeliveryProfileNumbers("SMS", TRANSPORT_MOBILE);
            if (smsNumbers == null) {
                String notifNumber = user.getNotifNumber();
                if (notifNumber != null && !notifNumber.isEmpty())
                {smsNumbers = new String[] { notifNumber };}
                else
                {smsNumbers = new String[] { email.getReceiverPhoneNumber() };}
            }
        }
        
        String[] mwiNumbers = null;
        
      //get list of MWI SMS numbers that are enabled. NOTE: does not check roaming at this point
        if (hasMWI && (!handleDisablings(NTF_MWI, email, fh, null))) {
            mwiNumbers = getMatchingDeliveryProfileNumbers("MWI", TRANSPORT_MOBILE);
            if (mwiNumbers == null) {
                String[] ipMwiNumbers = getMatchingDeliveryProfileNumbers("MWI", TRANSPORT_IP);
                if (ipMwiNumbers == null) {
                    // There is no delivery profile defined for MWI, use the notification number/receiver phone number
                    String notifNumber = user.getNotifNumber();
                    if (notifNumber != null && !notifNumber.isEmpty()) {
                        mwiNumbers = new String[] { notifNumber };
                    } else {
                        mwiNumbers = new String[] { email.getReceiverPhoneNumber() };
                    }
                    log.debug(email.getReceiver() + " will use SMSMWI number " + Arrays.toString(mwiNumbers));
                } else {
                    // There is a delivery profile defined for MWI (an IP MWI (TRANSPORT_IP)), no SMSMWI to be sent.
                    log.debug(email.getReceiver() + ", no SMSMWI will be sent since IP MWI number found and SMS MWI mobile number not defined in Delivery Profile " + Arrays.toString(ipMwiNumbers));
                }
            }            
        }
                  
        boolean roaming=false; 
        if (Config.getCheckRoaming() && !email.isMwiOffUnsubscribed()) {
                //Make a unique list of all numbers to check for roaming, containing both MWI and SMS..            
                Set<String> numbers = new HashSet<String>();

                if (smsNumbers !=null ) {
                    for (int i=0;i<smsNumbers.length;i++) {
                        numbers.add(smsNumbers[i]);
                    }
                }

                if (mwiNumbers !=null ) {
                    for (int i=0;i<mwiNumbers.length;i++) {
                        numbers.add(mwiNumbers[i]);
                    }
                }

                //If any numbers are roaming all are considered roaming for the sake of using roaming filter if defined.
                //Otherwise we could end up with complex combinations here.
                String[] numbersA = numbers.toArray(new String[numbers.size()]);             
                switch (areRoaming(numbersA,fh)) {
                    case FALSE:
                        roaming=false;
                        break;
                    case TRUE:
                        roaming=true;
                        break;
                    case ERROR:
                        return null;                      
                    default:
                        log.error("getSmsFilterInfo() Unknown roaming state:" + roaming); 
                        return null;
                }
        }
        
        //get the first matching filter for time of day etc, use roaming optional roaming filters if needed.
        getFirstMatchingFilterPart(email,when,roaming);
        
        if (!match.notify) {
            //filter is turned off - probably for time of day or possibly roaming.
            return null;
        }
        
        contentType = match.contentForType;
        
        //if SMS is in filter
        if (hasSMS && contentType.getProperty("SMS") != null) {

            if (smsNumbers != null) {
                ArrayList<String> numberList = new ArrayList<String>(smsNumbers.length);
                for (String number : smsNumbers) {
                    if (!handleDisablings(NTF_SMS, email, fh, number)) {
                        numberList.add(number);
                    }
                }

                if (!numberList.isEmpty()) {
                    smsNumbers = numberList.toArray(new String[numberList.size()]);
                } else {
                    smsNumbers = null;
                }
            }
        } else {
            smsNumbers=null;
        }        
              
       if (hasMWI && mwiNumbers != null ) {
           //if mwi is in filter.
           if (contentType.getProperty("MWI") != null) {
               ArrayList<String> numberList = null;
                   numberList = new ArrayList<String>(mwiNumbers.length);
                   for (String number : mwiNumbers) {
                       //check disabled with roaming for MWI.
                       if (!handleDisablings(NTF_MWI, email, fh, number )) {
                           numberList.add(number);
                       }              
                   }
               if (!numberList.isEmpty()) {
                   mwiNumbers = numberList.toArray(new String[numberList.size()]);
               } else {
                   mwiNumbers = null;
               }
           } else {
               mwiNumbers=null;
           }

           // if not voice mail system, change all MWI numbers to SMS. as only MWI for Voice and Video for now.
           if(email.getDepositType() != depositType.VOICE
                   && email.getDepositType() != depositType.VIDEO) {
               if( smsNumbers == null ) {
                   smsNumbers = mwiNumbers;
                   mwiNumbers = null;
               } else {
                   Vector<String> tempNumbers = new Vector<String>();
                   for( int i=0;i<smsNumbers.length;i++ ) {
                       tempNumbers.add( smsNumbers[i] );
                   }
                   if( mwiNumbers != null ) {
                       for( int i=0;i<mwiNumbers.length;i++ ) {
                           if( !tempNumbers.contains( mwiNumbers[i] ))
                               tempNumbers.add( mwiNumbers[i] );
                       }
                   }
                   smsNumbers = tempNumbers.toArray(new String[0]);
                   mwiNumbers = null;
               }
               contentType.remove("MWI");
               if (!contentType.containsKey("SMS")) {
                   contentType.setProperty("SMS", "s"); //default subject..
               }
           }
       }



        if (smsNumbers != null ) {
            log.debug("getSmsFilterInfo, smsNumbers: " + Arrays.toString(smsNumbers));
        }

        if (mwiNumbers != null ) {
            log.debug(email.getReceiver() + " will use MWI number(s) " + Arrays.toString(mwiNumbers));
            if (email.isMwiOffUnsubscribed() && mwiUnsubscribedNumbers != null && mwiUnsubscribedNumbers.length > 0) {
                //we need to prune the MWI numbers according to the list sent in MWI unsubscribe.
                Vector<String> prunedMwi = new Vector<String>(mwiUnsubscribedNumbers.length);
                for (String number:mwiNumbers) {
                    for (String uNumber:mwiUnsubscribedNumbers) {
                        if(number.equals(uNumber)) {
                            prunedMwi.add(number);
                            break;
                        }
                    }
                }
                mwiNumbers = prunedMwi.toArray(mwiNumbers);                
            }
        }

        if(smsNumbers != null || mwiNumbers != null ) {
            if (email instanceof NotificationEmail) {
                //this checks if we should send a roaming sms instead of an sms (a kind of fall-back from out-dial)
                //when roaming is set in the SmsFilterInfo, smsout will append/prepend an additional template if configured
                //to do so for roaming.
                roaming = roaming || ((NotificationEmail)email).isRoaming();
            }
            SmsFilterInfo info = new SmsFilterInfo(contentType, smsNumbers, mwiNumbers, null, roaming);

            return info;
        } else
        {
            return null;
        }
    }

 
    /**
     * Gets matching flash number
     *@param email the email notification is about.
     *@param when the time notification shall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error).
     * @return SmsFilterInfo with flash or null if user does not have flash
     */
    public SmsFilterInfo getFlashFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {
        
        if (email == null) {
            return null;
        }

        if (notifDisabled) {
            log.debug("getFlashFilterInfo() notifDisabled: " + notifDisabled);
            return null;
        }

        if (!hasNotificationService(NTF_FLS, email)) {
            return null;
        }
        
        if(handleDisablings( NTF_FLS, email, fh, null) ) {
            return null;
        }
        
        String[] flashNumbers = getMatchingDeliveryProfileNumbers("FLS");
        if( flashNumbers == null ) {
            String notifNumber = user.getNotifNumber();
            if ( notifNumber != null  && !notifNumber.isEmpty() )
                flashNumbers = new String[] { notifNumber };
            else
                flashNumbers = new String[] {email.getReceiverPhoneNumber()};
        }

        boolean roaming=false;
        /* we only need to check roaming at this point if
         * - we are configured to check roaming and
         * - have a roaming filter
         * If so, we only need to decide which filter set to use
         * (i.e. the roaming or non-roaming one).
         */
        if (Config.getCheckRoaming() && roamParts.length > 0) {
            switch (areRoaming(flashNumbers,fh)) {
                case FALSE:
                    roaming=false;
                    break;
                case TRUE:
                    roaming=true;
                    break;
                case ERROR:
                    return null;                      
                default:
                   log.error("getFlashFilterInfo() Unknown roaming state:" + roaming); 
                    return null;
            }
        }
        
        //get correct filter based on roaming.
        getFirstMatchingFilterPart(email, when, roaming);
        if (!match.notify) {
            log.debug("getFlashFilterInfo() no match.notify");
            return null;
        }
        
        if(match.contentForType.getProperty("FLS") == null) {
            log.debug("getFlashFilterInfo() no FLS in filter");
            return null;
        }
        

        // if roaming is to be checked, we shall handle disablings for each number,
        // based on its 'roamingness'
        if (Config.getCheckRoaming() ) {
            ArrayList<String> numberList = new ArrayList<String>(flashNumbers.length);
            for (String number : flashNumbers) {
                if(!handleDisablings( NTF_FLS, email, fh, number) ) {
                    numberList.add(number);
                }
            }
            
            if (!numberList.isEmpty()) {
                // Overwrite numbers with newly created (validated) list
                flashNumbers = numberList.toArray(new String[numberList.size()]);
                log.debug("getFlashFilterInfo() flashNumbers: " + Arrays.toString(flashNumbers));
            } else {
                log.debug("getFlashFilterInfo() flashNumbers array is empty after handling disablings.");
                return null;
            }
        }
        
        return (new SmsFilterInfo( match.contentForType, null, null, flashNumbers,roaming));

    }

    /**
     * Gets information for outdial notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification chall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error).
     *@return Information for outdial notification, or null if there shall be no
     * outdial notification.
     */
    public OdlFilterInfo getOdlFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {
        
        if (email == null) {
            return null;
        }
        
        if (notifDisabled) {
            log.debug("getOdlFilterInfo() notifDisabled: " + notifDisabled);
            return null;
        }
        
    	if (!hasNotificationService(NTF_ODL, email)) {
    		return null;
    	}
    	
    	//this type is disabled completely.
    	if (handleDisablings(NTF_ODL, email, fh, null)) {
            return null;
        }

        OdlFilterInfo info = null;

        //get list of smsNumbers that are currently enabled.
        String[] numbers = getMatchingDeliveryProfileNumbers("ODL");
        if (numbers == null) {
            String notifNumber = user.getNotifNumber();
            if (notifNumber != null && !notifNumber.isEmpty())
                {numbers = new String[] { notifNumber };}
            else
                {numbers = new String[] { email.getReceiverPhoneNumber() };}
        }
        
        
        boolean roaming=false;
        if (Config.getCheckRoaming() && roamParts.length > 0) {
            /* we only need to check roaming at this point if we have a roaming
             * filter and are configured to check roaming,
             * Because at this point we only need to decide which filter set to use.
             */
            switch (areRoaming(numbers,fh)) {
                case FALSE:
                    roaming=false;
                    break;
                case TRUE:
                    roaming=true;
                    break;
                case ERROR:
                    return null;                      
                default:
                    log.error("getOdlFilterInfo() Unknown roaming state:" + roaming); 
                    return null;
            }
        }
        
        //get correct filter based on roaming.
        getFirstMatchingFilterPart(email, when, roaming);
        
        if (!match.notify) {
            //filter no notify.
            return null; 
        } 
        
        if (match.contentForType.getProperty("ODL") == null) {
            return null; //no ODL in filter.
        } 
        
        if (Config.getCheckRoaming() ) {
            ArrayList<String> numberList = new ArrayList<String>(numbers.length);
            for (String number : numbers) {
                //disable for each number based on roaming.
                if (!handleDisablings(NTF_ODL, email, fh, number )) {
                    numberList.add(number);
                }                         
            }


            if (!numberList.isEmpty()) {
                numbers = numberList.toArray(new String[numberList.size()]);
            } else {
                numbers = null;
            }
        }

        
        if (numbers != null) {
            info = new OdlFilterInfo(match.contentForType, numbers);
            return info;
        }
        
        return null;
    }

    /*
     * Checks if any numbers in array numbers are roaming.
     * if fails roaming check takes aproriate action based on config
     * and indicates so failed or retry in feedBackHandler when approriate.
     */
    private triState areRoaming(String[] numbers, FeedbackHandler fh) {
        if (!Config.getCheckRoaming() || numbers.length == 0 ) {
            return triState.FALSE; //no roaming check assume needed assume roaming false.
        }

        for (int i=0;i<numbers.length;i++) {
            State roaming = PhoneStatus.getPhoneStatus(numbers[i]).isRoaming();
            if (roaming == PhoneStatus.State.YES) {
                return triState.TRUE;
            } else if (roaming == PhoneStatus.State.ERROR || roaming == PhoneStatus.State.NONE) {
                log.info("Unable to determine Roaming Status for number: " + numbers[i]);
                hlrFailAction action = Config.getHLRRoamingFailureAction();
                log.info("checkHlrFailAction " + NotificationConfigConstants.HLR_ROAM_FAILURE_ACTION + "- failure action:" + action );
                switch (action) {
                    case FAIL:    
                        if (fh !=null ) {
                            if (fh instanceof NotificationGroup ) {
                                //special case for ng, indicate failed for this notification type only.
                                NotificationGroup ng = (NotificationGroup) fh;
                                ng.increaseTempCount(user, 1);
                            }
                            fh.failed(user, NTF_NO_NOTIF_TYPE, "Failed to determine roaming-location");
                        }
                        return triState.FALSE;    
                    case ROAM: //send roam only on fail
                        return triState.TRUE; //as soon as anyone is roaming return true.  
                    case HOME: //send home only on fail
                        //if considered home then check other numbers..
                        continue;
                    case RETRY:
                        log.info("isRoamingCheckOK() notification retry.");  
                        if (fh !=null ) {
                            if (fh instanceof NotificationGroup ) {
                                //special case for ng, indicate retry for this notification type only.
                                NotificationGroup ng = (NotificationGroup) fh;
                                ng.increaseTempCount(user, 1);
                            }
                            fh.retry(user, NTF_NO_NOTIF_TYPE, "Failed to determine roaming-location");
                        }
                        return triState.FALSE;        
                    default:
                        log.info("isRoamingCheckOK() unknown failure action: " + action + " assuming retry." );
                        if (fh !=null ) {
                            if (fh instanceof NotificationGroup ) {
                                //special case for ng, indicate retry for this notification type only.
                                NotificationGroup ng = (NotificationGroup) fh;
                                ng.increaseTempCount(user, 1);
                            }
                            fh.retry(user, NTF_NO_NOTIF_TYPE, "Failed to determine roaming-location");
                        }
                        return triState.FALSE;
                }       
            } 
        }
        return triState.FALSE; //not roaming if looped through all and no roaming numbers found.
        
    }


    /**
     * Gets information for wireline MWI notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification chall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error).
     *@return Information for wireline MWI notification, or null if there shall
     * be no wireline MWI notification.
     */
    public WmwFilterInfo getWmwFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {
        
        //currently not supported
        return null;

//        if (notifDisabled) {
//            log.debug("getWmwFilterInfo, notifDisabled: " + notifDisabled);
//            return null;
//        }
//
//        getFirstMatchingFilterPart(email, when);
//        if (match.notify && match.contentForType.getProperty("WMW") != null ) {
//            //no roaming for wire line..
//            if( handleDisablings( NTF_WMW, email, fh, null ) )
//                return null;
//            this.email = email;
//
//            WmwFilterInfo info = null;
//            String[] numbers = getMatchingDeliveryProfileNumbers("WMW");
//
//            log.debug("getWmwFilterInfo, numbers: " + Arrays.toString(numbers));
//
//            if (numbers != null) {
//                info = new WmwFilterInfo( match.contentForType, numbers );
//            } else if( user.getNotifNumber() != null ) {
//                info = new WmwFilterInfo( match.contentForType, new String[] { user.getNotifNumber() } );
//            }
//            return info;
//        } else {
//            log.debug("getWmwFilterInfo, no match.notify");
//        }
//        return null;
    }


    /**
     * Gets information for pager PAG notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification chall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error).
     *@return Information for pager PAG notification, or null if there shall
     * be no pager PAG notification.
     */
    public PagFilterInfo getPagFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {

         return null; //not currently supported.
         
//        if (notifDisabled) {
//            log.debug("getPagFilterInfo, notifDisabled: " + notifDisabled);
//            return null;
//        }
//
//        getFirstMatchingFilterPart(email, when);
//        if (match.notify && match.contentForType.getProperty("PAG") != null) {
//            this.email = email;
//
//            //no roaming for email
//            if (!handleDisablings(NTF_PAG, email, fh, null)) {
//                if( null != user.getPnc()
//                    && !"".equals(user.getPnc().trim())
//                    && user.getPnc().indexOf("+") != -1 ) {
//
//                    //Pager notifications does not use any delivery profiles.
//                    //The notification number is included in the emPNC string.
//                    return new PagFilterInfo(user);
//                } else {
//                    log.debug("User " + user.getFullId() + " has no or invalid emPNC attribute");
//                }
//            }
//        } else {
//            log.debug("getPagFilterInfo, no match.notify");
//        }
//        return null;
    }

    /**
     * Gets information for Call MWI notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification shall be checked against, typically the
     * current time.
     * @param fh feed back of result if desired.
     *@return Information for call MWI notification, or null if there shall
     * be no call MWI notification.
     */
    public CmwFilterInfo getCmwFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {

        return null; //not currently supported.
//        if (notifDisabled) {
//            log.debug("getCmwFilterInfo, notifDisabled: " + notifDisabled);
//            return null;
//        }
//
//        getFirstMatchingFilterPart(email, when);
//        if (match.notify && match.contentForType.getProperty("CMW") != null ) {
//            
//            if( handleDisablings(NTF_CMW, email, fh, null))
//                return null;
//            this.email = email;
//
//            CmwFilterInfo info = null;
//            String[] numbers = getMatchingDeliveryProfileNumbers("CMW");
//
//            log.debug("getCmwFilterInfo, numbers: " + Arrays.toString(numbers));
//            
//            if (numbers != null) {
//                info = new CmwFilterInfo( numbers );
//            } else if( user.getNotifNumber() != null ) {
//                info = new CmwFilterInfo( new String[] { user.getNotifNumber() } );
//            }
//            return info;
//        } else {
//            log.debug("getCmwFilterInfo, no match.notify");
//        }
//        return null;
    }

    /**
     * Gets information for Email notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification shall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error).
     *@return Information for Email notification, or null if there shall be no
     * Email notification.
     */
    public EmailFilterInfo getEmailFilterInfo(IMessageDepositInfo email,
                                              Calendar when,
                                              FeedbackHandler fh) {
        //no roaming check for email..
        
        if (email == null) {
            return null;
        }
        
        if (notifDisabled) {
            log.debug("getEmailFilterInfo, notifDisabled: " + notifDisabled);
            return null;
        }
        
    	if (!hasNotificationService(NTF_EML, email)) {
    		return null;
    	}

        getFirstMatchingFilterPart(email, when);
        if (match.notify && match.contentForType.getProperty("EML") != null ) {
            //no roaming for email
            if( handleDisablings( NTF_EML, email, fh, null ) )
                return null;

            EmailFilterInfo info = null;
            String[] addresses = getMatchingDeliveryProfileNumbers("EML",TRANSPORT_IGNORE);

            log.debug("getEmailFilterInfo, addresses: " + Arrays.toString(addresses));
            
            Properties p = new Properties();
            p.setProperty("EML", match.contentForType.getProperty("EML"));
            if (addresses != null) {
                info = new EmailFilterInfo( p, addresses );
            }
            return info;
        } else {
            log.debug("getEmailFilterInfo, no match.notify");
        }
        return null;
    }

    /**
     * Gets information for MMS notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification shall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error).
     *@return Information for MMS notification, or null if there shall be no
     * MMS notification.
     */
    public MmsFilterInfo getMmsFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {
        
        if (email == null) {
            return null;
        }
        
        if (notifDisabled) {
            log.debug("getMmsFilterInfo(), notifDisabled: " + notifDisabled);
            return null;
        }

        
        if (!hasNotificationService(NTF_MMS, email)) {
            return null;
        }
        
        //this type is disabled completely.
        if (handleDisablings(NTF_MMS, email, fh, null)) {
            return null;
        }

        MmsFilterInfo info = null;

        //get list of smsNumbers that are currently enabled.
        String[] numbers = getMatchingDeliveryProfileNumbers("MMS");
        if (numbers == null) {
            String notifNumber = user.getNotifNumber();
            if (notifNumber != null && !notifNumber.isEmpty())
                {numbers = new String[] { notifNumber };}
            else
                {numbers = new String[] { email.getReceiverPhoneNumber() };}
        }
        
        
        boolean roaming=false;
        if (Config.getCheckRoaming() && roamParts.length > 0) {
            /* we only need to check roaming at this point if we have a roaming
             * filter and are configured to check roaming,
             * Because at this point we only need to decide which filter set to use.
             */
            switch (areRoaming(numbers,fh)) {
                case FALSE:
                    roaming=false;
                    break;
                case TRUE:
                    roaming=true;
                    break;
                case ERROR:
                    return null;                      
                default:
                    log.error("getMmsFilterInfo() Unknown roaming state:" + roaming); 
                    return null;
            }
        }
        
        //get correct filter based on roaming.
        getFirstMatchingFilterPart(email, when, roaming);
        
        if (!match.notify) {
            //filter no notify.
            return null; 
        } 
        
        if (match.contentForType.getProperty("MMS") == null) {
            return null; //no ODL in filter.
        } 
        
        if (Config.getCheckRoaming() ) {
            ArrayList<String> numberList = new ArrayList<String>(numbers.length);
            for (String number : numbers) {
                //disable for each number based on roaming.
                if (!handleDisablings(NTF_MMS, email, fh, number )) {
                    numberList.add(number);
                }                         
            }


            if (!numberList.isEmpty()) {
                numbers = numberList.toArray(new String[numberList.size()]);
            } else {
                numbers = null;
            }
        }

        
        if (numbers != null) {
            info = new MmsFilterInfo(numbers);
            return info;
        }
        
        return null;
    }

    /**
     * Gets information for WAP push notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification shall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error).
     *@return Information for WAP push notification, or null if there shall be no
     * WAP push notification.
     */
    public WapFilterInfo getWapFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {

        return null; //not currently supported.
//        if (notifDisabled) {
//            log.debug("getWapFilterInfo, notifDisabled: " + notifDisabled);
//            return null;
//        }
//
//        getFirstMatchingFilterPart(email, when);
//        if (match.notify && match.contentForType.getProperty("WAP") != null ) {
//            if( handleDisablings( NTF_WAP, email, fh, null ) )
//                return null;
//            this.email = email;
//
//            WapFilterInfo info = null;
//            String[] numbers = getMatchingDeliveryProfileNumbers("WAP");
//
//            log.debug("getWapFilterInfo, numbers: " + Arrays.toString(numbers));
//
//            if (numbers != null) {
//                info = new WapFilterInfo( numbers );
//            } else if( user.getNotifNumber() != null ) {
//                info = new WapFilterInfo( new String[] { user.getNotifNumber() } );
//            }
//            return info;
//        } else {
//            log.debug("getWapFilterInfo, no match.notify");
//        }
//        return null;
    }

    /**
     * Gets information for SIP MWI notification as defined by filter settings,
     * email contents and the time.
     *@param email the email notification is about.
     *@param when the time notification shall be checked against, typically the
     * current time.
     *@param fh - feedback handler, if problem determining if type is disabled (HLR error).
     *@return Information for SIP MWI notification, or null if there shall be no
     * SIP MWI notification.
     */
    public SIPFilterInfo getSIPFilterInfo(IMessageDepositInfo email, Calendar when, FeedbackHandler fh) {
        //currently does not support roaming check as there is no number to check.
        //but IP phone in theory should not need to check.
        
        if (email == null) {
            return null;
        }

        if (notifDisabled) {
            log.debug("getSIPFilterInfo(), notifDisabled: " + notifDisabled);
            return null;
        }

        getFirstMatchingFilterPart(email, when);
        if (match.notify && match.contentForType.getProperty("MWI") != null ) {
            if( handleDisablings( NTF_MWI, email, fh, null ) )
               { return null; }

            SIPFilterInfo info = null;
            String[] numbers = getMatchingDeliveryProfileNumbers("MWI", TRANSPORT_IP);

            log.debug("getSIPFilterInfo(), numbers: " + Arrays.toString(numbers));

            if (numbers != null) {
                info = new SIPFilterInfo( numbers );
            }
            return info;
        } else {
            log.debug("getSIPFilterInfo(), no match.notify");
        }
        return null;
    }

    /**
     * Returns a printable representation of this filter.
     *@return String with the parts constituting this filter.
     */
    public String toString() {
        String s="";
        if (homeParts == null) {
            s="{NotificationFilter:}";
            return s; //not initialised.
        }
        if (!usingDefaultFilter) {
            s+= "{NotificationFilter:\n";
            for (int i= 0; i < homeParts.length; i++) {
                s+= "  " + homeParts[i].toString();
            }

            s=s+"}";

        } else
        {
            s="{NotificationFilter:}";
        }
        if (roamParts == null || roamParts.length == 0) {
            return s;
        } 
            
        s+= "\n{RoamingFilter:\n"; 
        for (int i= 0; i < roamParts.length; i++) {
            s+= "  " + roamParts[i].toString();
        }
        s=s+"}";
        
        return s;
    }

    public String[] getMatchingDeliveryProfileNumbers(String type) {
        return getMatchingDeliveryProfileNumbers(type,TRANSPORT_MOBILE);
    }
    
    public void getMatchingDeliveryProfileNumbers(String type, ArrayList<String> numbers) {
        getMatchingDeliveryProfileNumbers(type, TRANSPORT_MOBILE,numbers);
    }

   


    /**
     * @return the numbers in matching delivery profiles, null if no profile
     * or the types does not match. No duplicates numbers are returned.
     */
    public String[] getMatchingDeliveryProfileNumbers(String type, int transportType) {
        return getMatchingDeliveryProfileNumbers (type,transportType,true);
    }
    
    /**
     * fills numbers with the DeliveryNumbers for transportType and notification type.
     * No duplicate numbers will be added.
     */
    public void getMatchingDeliveryProfileNumbers(String type, int transportType, ArrayList<String> numbers) {
        getMatchingDeliveryProfileNumbers (type,transportType,true,numbers);
    }
    
    /**
     * @param checkNotifDisabled Check whether notification is disabled in subscriber or not
     * @return the numbers in matching delivery profiles, null if no profile
     * or the types does not match. No duplicates numbers are returned.
     */
    public String[] getMatchingDeliveryProfileNumbers(String type, int transportType, boolean checkNotifDisabled ) {
        if(_deliveryProfileList.isEmpty()) return null;
        if (checkNotifDisabled && notifDisabled) { return null; }

        HashSet<String> temp = new HashSet<String>();

        Iterator<DeliveryProfile> it = _deliveryProfileList.iterator();
        while( it.hasNext() ) {
            DeliveryProfile profile = it.next();
            if( profile != null && profile._notifTypes.contains(type) && profile.matchesTransport(transportType)) {
                if(profile._numbers.size() > 0) {
                    for(int i = 0; i < profile._numbers.size(); i++) {
                        String num = profile._numbers.get(i);
                            temp.add( num );
                    }
                }
            }
        }
        
        if(temp.isEmpty()) return null;

        return temp.toArray(new String[temp.size()]);
    }
    
    
    /**
     * @param checkNotifDisabled Check whether notification is disabled in subscriber or not
     * populates the numbers List with  the numbers in matching delivery profiles, null if no profile
     * or the types does not match. No duplicates numbers are returned.
     */
    public void getMatchingDeliveryProfileNumbers(String type, int transportType, boolean checkNotifDisabled, ArrayList<String> numbers) {
        if(_deliveryProfileList.isEmpty()) return;
        if (checkNotifDisabled && notifDisabled) { return; }

        HashSet<String> temp= new HashSet<String>(_deliveryProfileList.size());

        Iterator<DeliveryProfile> it = _deliveryProfileList.iterator();
        while( it.hasNext() ) {
            DeliveryProfile profile = it.next();
            if( profile != null && profile._notifTypes.contains(type) && profile.matchesTransport(transportType)) {
                if(profile._numbers.size() > 0) {
                    for(int i = 0; i < profile._numbers.size(); i++) {
                        String num = profile._numbers.get(i);
                            temp.add( num );
                    }
                }
            }
        }
        numbers.addAll(temp);       
    }
    
    
    /**
     * Checks if the user has a delivery profile for a specified notification type.
     * @param type the type to search for
     * @return true if the user has the type otherwise false
     */
    public boolean hasDeliveryProfileForType( String type ) {
        if(_deliveryProfileList.isEmpty()) return false;

        Iterator<DeliveryProfile> it = _deliveryProfileList.iterator();
        while( it.hasNext() ) {
            DeliveryProfile profile = it.next();
            if( profile != null && profile._notifTypes.contains(type)) {
                if(profile._numbers.size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the user has the mailType and notifType and should therefore be getting a notification
     * @param type the depositType to check for
     * @param notifType the notifType
     * @return true if the user has the types. False otherwise
     */
    public boolean filterMatchesMailType(depositType type, String notifType ) {
        if( notifDisabled ) { return false; }
        for( int i=0;i<homeParts.length;i++) {
            FilterPart part = homeParts[i];
            if (part.notify && part.active &&
                    part.depType.indexOf(type.filtAbrev()) != -1 &&
                    part.contentForType.getProperty(notifType) != null) {
                return true;
            }
        }
        for( int i=0;i<roamParts.length;i++) {
            FilterPart part = roamParts[i];
            if (part.notify && part.active &&
                    part.depType.indexOf(type.filtAbrev()) != -1 &&
                    part.contentForType.getProperty(notifType) != null) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns all templates that a user can have for a specified mailType and notification type.
     * @param type the DepositType to check for
     * @param notifType the notificationType
     * @return a list with templates or null if the user has no templates.
     */
    public ArrayList<String> getTemplatesForType(depositType type, String notifType) {
        if( notifDisabled ) { return null; }
        ArrayList<String> res = new ArrayList<String>();
        for( int i=0;i<homeParts.length;i++) {
            FilterPart part = homeParts[i];
            if (part.notify && part.active &&
                    part.depType.indexOf(type.filtAbrev()) != -1 &&
                    part.contentForType.getProperty(notifType) != null) {
                res.add(part.contentForType.getProperty(notifType));
            }
        }
        for( int i=0;i<roamParts.length;i++) {
            FilterPart part = roamParts[i];
            if (part.notify && part.active &&
                    part.depType.indexOf(type.filtAbrev()) != -1 &&
                    part.contentForType.getProperty(notifType) != null) {
                res.add(part.contentForType.getProperty(notifType));
            }
        }
        
        if( res.size() > 0 ) {
            return res;
        } else {
            return null;
        }

    }

    /**
     * Check if a type of message could be notified with a certain notification
     * type and template.
     *@param type the DepositType to check for
     *@param template the template, e.g. "c".
     *@param notifType the notification type, e.g. "SMS".
     *@return true if a message of the mailType could be notified with the
     * notifType using the template.
     */
    public boolean hasTemplateForDepositType(depositType type, String template, String notifType) {
        if( notifDisabled ) { return false; }
        for( int i=0;i<homeParts.length;i++) {
            FilterPart part = homeParts[i];
            if (part.notify && part.active &&
                    part.depType.indexOf(type.filtAbrev()) != -1 &&
                        template.equals(part.contentForType.getProperty(notifType)) ) {
                return true;
            }
        }
        for( int i=0;i<roamParts.length;i++) {
            FilterPart part = roamParts[i];
            if (part.notify && part.active &&
                    part.depType.indexOf(type.filtAbrev()) != -1 &&
                        template.equals(part.contentForType.getProperty(notifType)) ) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if the filter has the notifType in the filter strings
     * @param notifType the notification type to check for
     * @return true if the user has the type
     */
    public boolean hasNotifType(String notifType) {
       if( notifDisabled ) { return false; }
       for( int i=0;i<homeParts.length;i++) {
            FilterPart part = homeParts[i];
            if (part.notify && part.active &&
                    part.contentForType.getProperty(notifType) != null) {
                 return true;
            }
        }
       for( int i=0;i<roamParts.length;i++) {
           FilterPart part = roamParts[i];
           if (part.notify && part.active &&
                   part.contentForType.getProperty(notifType) != null) {
                return true;
           }
       }
        return false;
    }
    
    
    /**
     * Scans the filter parts in sorting order until one is found that matches
     * the email and time. <I>match</I> contains the matching part, or null if
     * none was found.  Assume not roaming.
     *@param email the email notification is about.
     *@param when the time notification shall be checked again, typically the
     * current time.
     */
    private void getFirstMatchingFilterPart(IMessageDepositInfo email, Calendar when) {
        getFirstMatchingFilterPart(email,when,false);
    }

    /**
     * Scans the filter parts in sorting order until one is found that matches
     * the email and time. <I>match</I> contains the matching part, or null if
     * none was found.
     *@param email the email notification is about.
     *@param when the time notification shall be checked again, typically the
     *@param roaming set to true if want to scan roaming filter set
     * current time.
     */
    private void getFirstMatchingFilterPart(IMessageDepositInfo email, Calendar when, boolean roaming) {
        
        if (email == null) {
            log.warn("getFirstMatchingFilterPart: called with null IMessageDepositInfo, no match");
            match =null;
            return;
        }
        
        
        FilterPart[] parts = homeParts;
        boolean usingRoamingFilters = false;
        if (roaming && roamParts!=null  && roamParts.length > 0) {
            //if we have specific roaming filters and roaming use those otherwise we use homeParts.
            usingRoamingFilters=true;
            parts = roamParts;
        }
         
        boolean forceRecheck=false;
        if (usingRoamingFilters && !usingRoamingSet  && roaming) {
            /* This is to be sure we have checked the roaming filters, if at some point we
             * find we are roaming where previous checks have not looked at the roaming filters.
             * So that notification types that need a roaming check use a roaming filter if set.
             * Ideally it would be nice if we could get rid of the roaming filter and just use
             * the disable H- (home), R- (roam) types in the moipNTD parameter but for legacy reasons
             * we are forced to check the roaming filter if it exists.
             */
            usingRoamingSet=true;
            forceRecheck=true;
        }
        if (!forceRecheck && email == lastEmail && when == lastWhen) { //Same match as before
            return;
        }
        
        lastEmail= email;
        lastWhen= when;
        match=null ; //If no match, use default notification.
        FilterPart part= null;
        for (int i= 0; i < parts.length; i++) {
            part= parts[i];
            log.debug("NotificationFilter: matching " + part);
            if (part.active
                && matchMail(part, email)
                && matchTime(part, when)) {
                log.debug("NotificationFilter: match found");
                match=part;
                return;
            }
        }
        log.debug("NotificationFilter: no match found, using default notification");
        match = defaultParts[0];
    }


    /**
     * Matches the filter parts Voice/Fax From list against the MVASSender mail
     * header.
     *@param part the filter part to check.
     *@param email the mail notification is about.
     *@return true iff the voice/Fax-specific information matches.
     */
    private boolean matchVoiceFax(FilterPart part, IMessageDepositInfo email) {
        
        if (email == null) {
            log.warn("matchVoiceFax: called with null IMessageDepositInfo, return false");
            return false;
        }
        if (part.voiceFaxFrom == null) return true;

        String from= email.getSenderPhoneNumber();
        StringTokenizer st= new StringTokenizer(part.voiceFaxFrom, ",");
        while (st.hasMoreTokens()) {
            if (st.nextToken().trim().toLowerCase().equals(from)) {
                return true;
            }
        }

        log.debug("NotificationFilter: sender did not match");
        return false;
    }

    /**
     * Matches the subject and matches the filter parts From list against the From
     * mail header.
     *@param part the filter part to check.
     *@param email the mail notification is about.
     *@return true iff the email-specific information matches.
     */
    private boolean matchEmail(FilterPart part, IMessageDepositInfo email) {
        if (part.subject != null
            && email.getSubject().toLowerCase().indexOf(part.subject) < 0) {
            log.debug("NotificationFilter: subject did not match");
            return false;
        }

        if (part.from == null) return true;
        
        if (email == null) {
            return false;
        }
        

        String from= email.getSender();
        StringTokenizer st= new StringTokenizer(part.from, ",");
        while (st.hasMoreTokens()) {
            if (st.nextToken().trim().toLowerCase().equals(from)) {
                return true;
            }
        }

        log.debug("NotificationFilter: sender did not match");
        return false;
    }

    /**
     * Matches email contents against the mail constraints in the filter part.
     *@param part the filter part to check.
     *@param email the email notification is about.
     *@return true iff the mail contents matches the constraints in the filter
     * part.
     */
    private boolean matchMail(FilterPart part, IMessageDepositInfo email) {
        
        if (email == null) {
            log.warn("matchMail: called with null IMessageDepositInfo, return false");
            return false;
        }
        
        depositType et = email.getDepositType();

        if (part.depType.indexOf(et.filtAbrev()) < 0) {
            log.debug("NotificationFilter: deposit type did not match");
            return false;
        }

        switch (et) {
        case VOICE:
        case FAX:
        case VIDEO:
            if (!matchVoiceFax(part, email)) return false;
            break;

        case EMAIL:
            if (!matchEmail(part, email)) return false;
            break;

        default:
        }

        //Deposit type matches. Sender matches. Subject matches. Check mail priority.
        if (!(part.urgent) || email.isUrgent()) {
            return true;
        } else {
            log.debug("NotificationFilter: priority did not match");
            return false;
        }
    }

    /**
     * Matches a specified time agains time constraints in the filter part.
     *@param part the filter part to check.
     *@param when the time to natch against.
     */
    private boolean matchTime(FilterPart part, Calendar when) {
        if (part.time.equals("a")) return true; //All times match

        //If no business hour info is available, assume all times match
        if (user == null) return true;

        boolean inBusiness= user.isBusinessTime(when);
        if (inBusiness && part.time.equals("b") //Matches business hours
            || !inBusiness && part.time.equals("nb")) { //Matches outside business hours
            return true;
        } else {
            log.debug("NotificationFilter: business time did not match");
            return false;
        }
    }

    /**
     * FilterPart is a simple class that parses data from the string specifying
     * a filter part and provides a structured way to store this data.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     * This means that two FilterParts that are not equal can still give the
     * result 0 when compared. This is intentional and is because we want to
     * sort (i.e. compare) objects based only on the prio field.
     */
    private static class FilterPart implements Comparable<FilterPart> {
        private static final String encodedSemicolon= "%3b";
        private static final String encodedPercent= "%25";
        public String name;
        public int prio;
        public boolean active;
        public boolean notify;
        public String time;
        public String depType;
        public Properties contentForType;
        public String from;
        public String subject;
        public boolean urgent;
        public String voiceFaxFrom;
        public String readOnly;

        /**
         * Decodes the %3b and %25 special character sequences used to store
         * semicolon and percent signs in the subject part of a filter string.
         *@param encoded the encoded value from the filter string.
         *@return the decoded value.
         */
        private String murDecode(String encoded) {
            int percentIndex;
            String decoded= encoded;

            while((percentIndex= decoded.indexOf(encodedSemicolon)) >= 0) {
                decoded= decoded.substring(0, percentIndex)
                    + ";"
                    + decoded.substring(percentIndex + 3);
            }
            while((percentIndex= decoded.indexOf(encodedPercent)) >= 0) {
                decoded= decoded.substring(0, percentIndex)
                    + "%"
                    + decoded.substring(percentIndex + 3);
            }
            return decoded;
        }

        /**
         * Constructor that parses the filter strings.
         * @param filterString the string specifying a filter part.
         */
        public FilterPart(String filterString) {
            StringTokenizer st = new StringTokenizer(filterString, ";", true);
            active = getBoolean(st, false);
            notify = getBoolean(st, true);
            time = getString(st, "a");
            depType = getString(st, "evfm");
            contentForType = CommaStringTokenizer.getPropertiesFromLists(getString(st, "SMS"), getString(st, ""));
            prio = getInt(st);
            from = getString(st, null);
            if (from != null) from = from.toLowerCase();
            subject = getString(st, null);
            if (subject != null) subject = murDecode(subject.toLowerCase());
            urgent = getBoolean(st, false);
            voiceFaxFrom = getString(st, null);
            name = getString(st, "name");
            readOnly = getString(st, null);
        }

        /**
         * Compares two FilterPart objects, so an array of such objects can be
         * sorted according to priority.
         *@param part FilterPart to compare to.
         *@return &lt;0 if this FilterPart has smaller prio than o<BR>
         *        &gt;0 if this FilterPart has larger prio than o<BR>
         *        0 if this FilterPart and o have the same prio.
         */
        public int compareTo(FilterPart part) {
            return prio - part.prio;
        }

        /**
         * Parses a boolean value from the filter string (y and 1 is true).
         *@param st a StringTokenizer that is eating its way through the string.
         *@param def a default value to return if the field is empty.
         *@return a boolean from the string.
         */
        private boolean getBoolean(StringTokenizer st, boolean def) throws NoSuchElementException {
            String s = getString(st, null);
            if (s == null) {
                return def;
            } else {
                return (s.equalsIgnoreCase("y") ||
                        s.equals("1"));
            }
        }

        /**
         * Parses an integer value from the filter string.
         *@param st a StringTokenizer that is eating its way through the string.
         *@return an integer from the string.
         */
        private int getInt(StringTokenizer st) throws NoSuchElementException {
            String s = getString(st, "0");
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        /**
         * Parses a string value from the filter string;
         *@param st a StringTokenizer that is eating its way through the string.
         *@param def a default value to return if the field is empty.
         *@return a string from the string.
         */
        private String getString(StringTokenizer st, String def) {
            String s;
            try {
                s = st.nextToken();
                if (";".equals(s)) {
                    s = def;
                } else {
                    try {
                        st.nextToken(); //Discard the delimiter
                    } catch (NoSuchElementException e) { ; }
                }
            } catch (NoSuchElementException e) {
                s = def;
            }
            return s;
        }

        /**
         * Returns a printable representation of this FilterPart
         *@return This FilterPart as a String
         */
        public String toString() {
            return "{FilterPart:"
                + " name=" + name
                + ",prio=" + prio
                + ",active=" + active
                + ",notify=" + notify
                + ",time=" + time
                + ",depType=" + depType
                + ",contentForType=" + contentForType
                + ",from=" + from
                + ",subject=" + subject
                + ",urgent=" + urgent
                + ",voiceFaxFrom=" + voiceFaxFrom
                + ",readonly=" + readOnly
                + "}";
        }
    }

   /**
    * Models the Delivery Profile which is used by the Notification Filters.
    * A Profile contains numbers and notification types.
    */
   class DeliveryProfile {

       /**
        * List of numbers (Strings)
        */
       public List<String> _numbers;
       /**
        * notification type list (Strings)
        */
       public List<String> _notifTypes;
       /**
        * False if we know the number is fixed, true otherwise
        */
       public boolean _mobile = false;
       public boolean _fixed = false;
       public boolean _ip = false;

       /**
        * Creates a DeliveryProfile from the string in MUR
        */
       public DeliveryProfile(String s) {
           StringTokenizer st = new StringTokenizer(s, ";", true);
           getNumbers(getString(st, ""));
           getNotficationTypes(getString(st, ""));
           getMobileInfo(getString(st, "M"));
       }

       private void getNumbers(String commaString) {
           _numbers = new ArrayList<String>();
           if (commaString.length() == 0) return; //no numbers

           StringTokenizer z = new StringTokenizer(commaString, ",");
           while (z.hasMoreElements()) {
               _numbers.add(z.nextToken());
           }
       }

       private void getNotficationTypes(String commaString) {
           _notifTypes = new ArrayList<String>();
           if (commaString.length() == 0) return; //no types

           StringTokenizer z = new StringTokenizer(commaString, ",");
           while (z.hasMoreElements()) {
               _notifTypes.add(z.nextToken());
           }
       }

       private void getMobileInfo(String info) {
           info = info.toUpperCase();
           if (info.contains("F")) {
               _fixed = true;
           }
           if (info.contains("M")) {
               _mobile = true;
           }
           if (info.contains("I")) {
               _ip = true;
           }


       }

       private boolean matchesTransport( int transportType ) {
           if( transportType == TRANSPORT_MOBILE && _mobile ) {
               return true;
           }
           if( transportType == TRANSPORT_FIXED && _fixed ) {
               return true;
           }
           if( transportType == TRANSPORT_IP && _ip ) {
               return true;
           }
           if( transportType == TRANSPORT_IGNORE ) {
               return true;
           }
           return false;
       }

       private String getString(StringTokenizer st, String def) {
           String s;
           try {
               s = st.nextToken();
               if (";".equals(s)) {
                   s = def;
               } else {
                   try {
                       st.nextToken(); //Discard the delimiter
                   } catch (NoSuchElementException e) {
                       ;
                   }
               }
           } catch (NoSuchElementException e) {
               s = def;
           }
           return s;
       }
   }

/* indicates the user has am explicit roaming filter defined in the COS */
public boolean hasExplicitRoamingFilter() {
    return (roamParts.length > 0);
}


/**
 * @param notifType - The notification type according to Constants.
 * @param telNumber - The telephone number to check if roaming.
 * @param fh - the feedback handler if something goes wrong.
 * @return true if disabled due to roaming, check feedback handler for error if true.
 */
public boolean isNotifTypeDisabledDueToRoaming(int notifType, String telNumber, FeedbackHandler fh) {
    if (Config.getCheckRoaming() == false ) {
        return false;
    }
    
    String [] numbers = null;
    if (telNumber!=null){
        numbers = new String[] {telNumber};
    } else
    {
        return false;
    }
    
    //check if roaming.
    switch (areRoaming(numbers,fh)) {
        case FALSE:
            return false; //if not roaming could not have been disabled due to roaming.
        case TRUE:
            //continue to next check if roaming.
            break;
        case ERROR:
            return false; //report false ,but let the retry handler take care of it.                      
        default:
            log.error("isNotifTypeDisabledDueToRoaming() Unknown roaming state."); 
            return false;
    }
    
    String notifString = Constants.notifTypeAbbrev[notifType];
    if (doesUserNTDContain(Constants.MOIPUserNTD_ROAM_PREFIX+notifString)) {
        //if was roaming and disabled roaming disabled.
        return true;
    }
        
    
    if (roamParts.length > 0) {
        if (isNotifTypeOnRoamFilter(notifString) && !isNotifTypeOnHomeFilter(notifString)) {
            //if is not on roam filter but on home filter then was disabled due to roaming.
            return true;
        }
    }
    return false;
}

/**
 * @return the mwiUnsubscribedNumbers
 */
public String[] getMwiUnsubscribedNumbers() {
    return mwiUnsubscribedNumbers;
}


}
