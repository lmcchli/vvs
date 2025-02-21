/**
 * Copyright (c) 2015 Abcxyz
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.mcd;

import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.provisioningagent.utils.PAConstants;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.directoryaccess.MoipProfile;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;
import com.abcxyz.services.moip.provisioning.vvm.AppleProvisioningStatus;
import com.abcxyz.services.moip.provisioning.vvm.OMTPProvisioningStatus;
import com.abcxyz.services.moip.provisioning.vvm.ProvisioningStatus;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.esi.ExternalSubscriberInformation;
import com.mobeon.ntf.util.Logger;
import com.abcxyz.messaging.common.util.crypto.PasswordFactory;

/**
 * McdUserInfo is an implementation of UserInfo that handles the MUR schema
 * version.
 */


public class McdUserInfo implements UserInfo, McdUserFinder, Constants {

    protected final static LogAgent log =  NtfCmnLogger.getLogAgent(McdUserInfo.class); 
    // Constants for terminal capabilities
    private static final int TERMINAL_UNKNOWN = 0;
    private static final int TERMINAL_HAS = 1;
    private static final int TERMINAL_HASNOT = 2;

    protected static final int END_POS = -1;
    private static final int INT_NOT_DEFINE = -1;

    private final String DEFAULT_VVM_CLIENT_PREFIX = "//VVM";
    private final String DEFAULT_VVM_CLIENT_TYPE = "OMTP";
    private final String VVMCLIENT_PASSWORD_ATTRIBUTE_NAME = "MOIPVvmPassword";


    protected MoipProfile cos = null;
    protected NotificationFilter filter;
    protected String subIdentity = null;  // equal to old uid
    //protected String dn = null;
    //protected String uid = null;  // not supported in miab1.0
    protected String mail = null;
    protected String serviceDn = null;
    protected String notifNumber = null;
    protected String preferredDateFormat = null;
    protected String preferredTimeFormat = null;
    protected String preferredLanguage = null;
    protected String brand = null;
    protected String userNtd = null;
    protected String pnc = null;  //not supported in miab1.0
    protected String timeZone = null;
    protected String temporaryGreeting = null;
    protected boolean isVvmActivated = false;
    protected boolean isVvmSystemActivated = false; // Indicates where the subscriber is activated or not by the system because the last VVM notification did not result in an IMAP request back into the server
    protected String vvmClientType = null;
    protected String vvmClientPrefix = null;
    protected boolean isVVMNotificationAllowedWhileRoaming = false;

    protected List<String> cnServices = null;

    protected String businessDayStart = null;
    protected String businessDayEnd = null;
    protected int notifExpTime = -1;
    protected int numberingPlan = 0;
    protected int typeOfNumber = 0;
    protected int mailQuota = -1;
    protected int noOfMailQuota = -1;
    protected int noOfVoiceMailQuota     = -1;
    protected int noOfFaxMailQuota     = -1;
    protected int noOfVideoMailQuota     = -1;
    protected int  quotaWarnLvl     =  -1;


    protected int vvmDestinationPort = 5070;

    protected boolean administrator = false;
    protected boolean valid = false; //True when all data from the user entry are loaded
    protected boolean notifDisabled = false;
    protected boolean billingNumberEntryValid = false; //True when all data from the billing number entry are loaded
    protected boolean unreadMessageReminder = true;
    protected boolean updateSms = true;
    protected boolean replace = true;
    protected boolean isQuotaPerType=false; //Default is global quota
    protected boolean systemMessageSMS=false;
    protected boolean systemMessageTUI=false;

    protected boolean[] businessDays = null;
    protected boolean[] notifTypeDisabled = new boolean[NTF_NO_NOTIF_TYPE];

    protected ExternalSubscriberInformation esi = null;

    protected int terminalReplaceCapability;
    protected int terminalMwiCapability;
    protected int terminalFlashCapability;


    protected String faxPrintNumber = null;
    protected String inboundFaxNo = null;
    protected boolean autoPrintFax = false;
    protected boolean autoprintFaxDisabled = false;

    //Just a default array initialized once for efficiency
    protected static final boolean[] defaultBusinessDays = new boolean[8];

    static {
        defaultBusinessDays[GregorianCalendar.MONDAY] = true;
        defaultBusinessDays[GregorianCalendar.TUESDAY] = true;
        defaultBusinessDays[GregorianCalendar.WEDNESDAY] = true;
        defaultBusinessDays[GregorianCalendar.THURSDAY] = true;
        defaultBusinessDays[GregorianCalendar.FRIDAY] = true;
        defaultBusinessDays[GregorianCalendar.SATURDAY] = false;
        defaultBusinessDays[GregorianCalendar.SUNDAY] = false;
    }

    protected static ThreadLocal<DateFormat> twelveHourFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("hh:mm a");
        }
    };
    protected static ThreadLocal<DateFormat> twentyFourHourFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };
    protected static ThreadLocal<DateFormat> lockoutDateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    protected String [] filterstrings = null;
    protected String [] roamingfilterstrings = null;

    //for Cos attributes
    protected String cosName = null;
    protected String [] services = null;
    protected boolean outdialNotification = false;
    protected boolean multilineService = false;
    protected boolean mwiService = false;
    protected boolean slamdown = false;
    protected boolean videoService = false;
    protected boolean pagerService = false;
    protected boolean voiceService = false;
    protected boolean faxService = false;
    protected boolean notificationService = false;
    protected boolean smsNotificationService = false;
    protected boolean flashNotificationService = false;
    protected boolean mmsNotificationService = false;
    protected boolean emailNotificationService = false;
    protected boolean forwardToEmailService =false;
    protected boolean vvmService = false;
    protected boolean autoPrintFaxService =false;
    protected boolean specialSMSMessageIndicationService = false;
    protected boolean emailService = false;
    protected boolean mcnSubscribedService = false;
    protected boolean ttsEmailEnabled = false;
    protected String outdialSchema = null;

    protected boolean prepaid = false;
    protected String msid = null;


    private IDirectoryAccess da = null;
    private IDirectoryAccessSubscriber subscriberCosProfile = null;

    /**
     * Constructor.
     */
    public McdUserInfo() {
        businessDays = defaultBusinessDays;

        da = CommonMessagingAccess.getInstance().getMcd();
    }

    /**
     * Constructor used for Missed Call Notification purpose only.
     */
    public McdUserInfo(String identity, String language) {
        subIdentity = identity;
        preferredLanguage = language;
        businessDays = defaultBusinessDays;
        da = null;
    }
    
    public McdUserInfo(IDirectoryAccessSubscriber subCosProfile) {
        businessDays = defaultBusinessDays;
        
        this.subscriberCosProfile = subCosProfile;
        msid = subscriberCosProfile.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID);

        String phoneNumber = subscriberCosProfile.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_TEL);
        cos = subscriberCosProfile.getCosProfile();
        if (cos == null) {
            log.debug("McdUserInfo() - phoneNumber:" + phoneNumber + ", does not have a COS");
        }
        loadProfile(phoneNumber != null ? DAConstants.IDENTITY_PREFIX_TEL + phoneNumber : null);
        log.debug("McdUserInfo() - initialized with IDirectoryAccessSubscriber for telephone number:" + phoneNumber);
    }

    /**
     * *************************************************************
     * findByMail looks for a user based on a mail address.
     *
     * @param mail - the email address identifying the user.
     * @return a new UserInfo object for the found user, or null if no user was
     *         found.
     */
    public boolean findByMail(String mail) {
        //SubscriberProfile = entry
        if (subscriberCosProfile == null) {
        	subscriberCosProfile = da.lookupSubscriber(mail);
        }

        if (subscriberCosProfile == null) {
            return false;
        }

        log.debug("McdUserInfo.findByMail() - look for user based on mail address =" + mail);

        log.debug("McdUserInfo.findByMail() - cosId=" +
		                subscriberCosProfile.getStringAttributes(DAConstants.ATTR_COS_IDENTITY)[0]);

        cos = da.lookupCos(subscriberCosProfile.getStringAttributes(DAConstants.ATTR_COS_IDENTITY)[0]);

        if (cos == null) {
        	log.debug("McdUserInfo.findByMail() - mail address:" + mail + ", does not have a COS");
        }

        return loadProfile(mail);
    }


    /**
     * findUserByTelephoneNumber looks for a user based on a telephonenumber.
     *
     * @param subsNumber - the telephonenumber identifying the user.
     * @return a new UserInfo object for the found user, or null if no user was
     *         found.
     */
    public boolean findByTelephoneNumber(String subsNumber) {
        boolean found = false;

        log.debug("McdUserInfo: findByTelephoneNumber() - telephone number:" + subsNumber);

        //SubscriberProfile = entry
        if (subscriberCosProfile == null) {
        	subscriberCosProfile = da.lookupSubscriber(subsNumber);
        }

        if(subscriberCosProfile != null) {
	        log.debug("McdUserInfo.findByTelephoneNumber() - cosId=" +
	        		        subscriberCosProfile.getStringAttributes(DAConstants.ATTR_COS_IDENTITY)[0]);
        	msid = subscriberCosProfile.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID);

        	String phoneNumber = subscriberCosProfile.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_TEL);
	        cos = da.lookupCos(subscriberCosProfile.getStringAttributes(DAConstants.ATTR_COS_IDENTITY)[0]);

	        if (cos == null) {
	        	log.debug("McdUserInfo.findByMail() - phoneNumber:" + phoneNumber + ", does not have a COS");
	        }

	        if (subscriberCosProfile == null) {
	            found = false;
	        } else {
	        	if(phoneNumber!=null) subsNumber=DAConstants.IDENTITY_PREFIX_TEL+phoneNumber;
	            found = loadProfile(subsNumber);
	        }
        }

        return found;
    }


    /**
     * Reads one entry by distingish name(dn)
     *
     * @param subsNumber The subscriber number
     * @return true if entry is found, false if not found.
     */
    public boolean readByIdentity(String subsNumber) {
        log.debug("McdUserInfo: reading " + subsNumber);

        //SubscriberProfile = entry
        subscriberCosProfile = da.lookupSubscriber(subsNumber);

        log.debug("McdUserInfo.readByIdentity() - number=" + subsNumber);

        boolean found = true;

        if (subscriberCosProfile == null) {
            found = false;
        } else {
            found = loadProfile(subsNumber);
        }

        return found;

    }

    protected String getStringValue(String attribute) {
    	String result="ToBeAddedInSchema";
    	try {
	    	String [] temp;
	        temp = subscriberCosProfile.getStringAttributes(attribute);
	        if (temp != null) {
	        	result = temp[0];
	        }
    	} catch (Exception ee) {
        	System.err.println("McdUserInfo.getStringValue() Exception occured - Attribute is not defined in schema: " + attribute);
        	ee.printStackTrace(System.err);
    	}

        // need to take care of a list string too...
        return result;

    }

    private List<String> getLowerCaseStringValueList(String attribute) {
        List<String> valuesList = new ArrayList<String>();

        try {
            String[] values = subscriberCosProfile.getStringAttributes(attribute);

            for (String value : values) {
                if (value != null) {
                    valuesList.add(value.toLowerCase());
                }
            }
        } catch (Exception ee) {
            System.err.println("McdUserInfo.getLowerCaseStringValueList() Exception occured - Attribute is not defined in schema: " + attribute);
            ee.printStackTrace(System.err);
        }

        return valuesList;
    }

    protected boolean getBooleanValue(String attribute) {
    	boolean result  = false; // default
    	try {
	    	boolean [] temp;
	        temp = subscriberCosProfile.getBooleanAttributes(attribute);
	        if ( temp != null ) {
	        	result = temp[0];
	        }
	        else {
	        	log.debug("McdUserInfo.getBooleanValue() - attribute not found in profile :" + attribute);
	        }
	    } catch (Exception ee) {
    		log.debug("McdUserInfo.getBooleanValue() missing attribute " + attribute + "\n" + ee.getMessage());
    	}

        // need to take care of a list string too...
        return result;
    }

    protected int getIntValue(String attribute) {
    	int result = INT_NOT_DEFINE; // default
    	try {
	    	int [] temp;
	        temp = subscriberCosProfile.getIntegerAttributes(attribute);
	        if ( temp != null ) {
	        	result = temp[0];
	        }
    	} catch (Exception ee) {
    		log.debug("McdUserInfo.getIntValue() missing attribute " + attribute + "\n" + ee.getMessage());
    	}
    	return result;
    }

    /**
     * Loads data from an AttributeMap into the more typed data in this class.
     *
     * @return true if the data could be loaded.
     */
    protected boolean loadProfile(String identity) {
    	try {
	        log.debug("McdUserInfo.loadProfile() - extracting attributes of:" + identity);

	    	String[] profileStrings;

	    	subIdentity       = identity;
	        serviceDn         = getStringValue (DAConstants.ATTR_COS_IDENTITY);
	        notifNumber       = getStringValue (DAConstants.ATTR_NOTIF_NUMBER);
	        userNtd           = getStringValue (DAConstants.ATTR_USER_NTD);
	        temporaryGreeting = getStringValue (DAConstants.ATTR_TMP_GRT);
	        notifExpTime      = getIntValue    (DAConstants.ATTR_NOTIF_EXP_TIME);
	        outdialSchema     = getStringValue (DAConstants.ATTR_OUTDIAL_SEQUENCE);
	        notifDisabled     = getBooleanValue(DAConstants.ATTR_NOTIF_DISABLED) || (cos == null);
	        String quotaPerType = getStringValue(DAConstants.ATTR_MAIL_QUOTA_PER_TYPE);
            cnServices        = getLowerCaseStringValueList(DAConstants.ATTR_CN_SERVICES);

	        if (quotaPerType != null) {
	            if (quotaPerType.equalsIgnoreCase(ProvisioningConstants.YES)) {
	            	isQuotaPerType=true;
	            }
	            else
	            {
	            	isQuotaPerType=false;
	            }
	        }
	        noOfMailQuota     = getIntValue    (DAConstants.ATTR_NO_OF_MAIL_QUOTA);
	        noOfVoiceMailQuota     = getIntValue    (DAConstants.ATTR_MAIL_QUOTA_VOICE);
	        noOfFaxMailQuota     = getIntValue    (DAConstants.ATTR_MAIL_QUOTA_FAX);
	        noOfVideoMailQuota     = getIntValue    (DAConstants.ATTR_MAIL_QUOTA_VIDEO);
	        String quotaWarnLvlStr     = getStringValue    (DAConstants.ATTR_QUOTA_WARN_LVL);

	        quotaWarnLvlStr = quotaWarnLvlStr.trim();
	        if (quotaWarnLvlStr != null && quotaWarnLvlStr.length() > 0) {
	        	try
	        	{
		        	quotaWarnLvl = Integer.parseInt(quotaWarnLvlStr);
	        	}
	        	catch(NumberFormatException e)
	        	{
	        		quotaWarnLvl=-1;
			        log.error("Number Format exception while parsing  quotaWarnLvlStr: "+quotaWarnLvlStr);
	        	}

	        }





	        vvmDestinationPort = getIntValue(DAConstants.ATTR_VVM_DESTINATION_PORT);

	        services          = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_SERVICES);
	        filterstrings     = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_FILTER);
	        roamingfilterstrings     = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_ROAMING_FILTER);
	        profileStrings    = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_DELIVERY_PROFILE);
	        mail              = subscriberCosProfile.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MAIL);

	        String vvmActivated = getStringValue(DAConstants.ATTR_VVM_ACTIVATED);
	        if (vvmActivated != null) {
	            if (vvmActivated.equalsIgnoreCase(ProvisioningConstants.YES)) {
	                isVvmActivated = true;
	            }else{
	                isVvmActivated = false;
	            }
	        }
	        String vvmNotificationAllowedWhileRoaming = getStringValue(DAConstants.ATTR_VVM_NOTIFICATIONS_ROAMING);
            if (vvmNotificationAllowedWhileRoaming != null) {
                if (vvmNotificationAllowedWhileRoaming.equalsIgnoreCase(ProvisioningConstants.YES)) {
                    isVVMNotificationAllowedWhileRoaming = true;
                }else{
                    isVVMNotificationAllowedWhileRoaming = false;
                }
            }


	        vvmClientType = getStringValue(DAConstants.ATTR_VVM_CLIENT_TYPE);
            if (vvmClientType == null || vvmClientType.isEmpty()){
                vvmClientType = DEFAULT_VVM_CLIENT_TYPE;
            }

	        vvmClientPrefix = getStringValue(DAConstants.ATTR_VVM_CLIENT_PREFIX);
	        if (vvmClientPrefix == null || vvmClientPrefix.isEmpty()){
	        	vvmClientPrefix = DEFAULT_VVM_CLIENT_PREFIX;
	        }

	        preferredDateFormat = getStringValue(DAConstants.ATTR_PREFERRED_DATE_FORMAT);
	        if (preferredDateFormat == null || preferredDateFormat.isEmpty()) {
	            preferredDateFormat = Config.getDefaultDateFormat();
	        }
	        preferredTimeFormat = getStringValue(DAConstants.ATTR_PREFERRED_TIME_FORMAT);
	        if (preferredTimeFormat == null || preferredTimeFormat.isEmpty()) {
	            preferredTimeFormat = Config.getDefaultTimeFormat();
	        }
	        preferredLanguage = getStringValue(DAConstants.ATTR_PREFERRED_LANGUAGE);
	        if (preferredLanguage == null || preferredLanguage.isEmpty()) {
	            preferredLanguage = Config.getDefaultLanguage();
	        }

	        brand =  getStringValue(DAConstants.ATTR_BRAND);

	        setServiceSetting();
	        setCosServices();

	        String sd = getStringValue(DAConstants.ATTR_USER_SD);
	        if (sd != null) {
	            if (sd.indexOf("replace") >= 0) {
	                replace = false;
	            }
	            if (sd.indexOf("reminder") >= 0) {
	                unreadMessageReminder = false;
	            }
	            if (sd.indexOf("update") >= 0) {
	                updateSms = false;
	            }
            }

	        String strDisabled = getStringValue(DAConstants.ATTR_AUTOPRINT_FAX_DISABLED);

	        if (strDisabled != null && strDisabled.equalsIgnoreCase(ProvisioningConstants.YES)) {
	            autoprintFaxDisabled = true;
	        }

	        faxPrintNumber = getStringValue(DAConstants.ATTR_FAX_PRINT_NUMBER);
	        if(hasMailType(NTF_FAX))
	        {
	            inboundFaxNo = subscriberCosProfile.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_FAX);
	            if(inboundFaxNo!=null)
	            {
	                if(inboundFaxNo.startsWith("+")){
	                    inboundFaxNo =inboundFaxNo.substring(1);
	                }
	            }
	        }


	       String systemMessageSMSStr = getStringValue(DAConstants.ATTR_SYSTEM_MESSAGE_SMS);
           if(systemMessageSMSStr!=null && systemMessageSMSStr.equalsIgnoreCase("yes"))
           {
               systemMessageSMS=true;
           }

           String systemMessageTUIStr = getStringValue(DAConstants.ATTR_SYSTEM_MESSAGE_TUI);

           if(systemMessageTUIStr!=null && systemMessageTUIStr.equalsIgnoreCase("yes"))
           {
               systemMessageTUI=true;
           }

	        // missing in schema - so we use the config one
	        numberingPlan = Config.getNumberingPlanIndicator();
	        typeOfNumber  = Config.getTypeOfNumber();

	        filter = new NotificationFilter(filterstrings, roamingfilterstrings, notifDisabled, this, profileStrings);

	        if (Config.getLogLevel() >= Logger.L_DEBUG) {
	            log.debug("McdUserInfo: profile loaded\n" + toString());
	        }
    	} catch (Exception ee) {
    		ee.printStackTrace(System.out);
    	}
        return true;
    }

    /**
     * Loads data from the billing number entry in an AttributeMap into the more
     * typed data in this class.
     */
    protected void loadBillingNumberEntry() {

    	timeZone = getStringValue(DAConstants.ATTR_SUBSCRIBER_TIME_ZONE);
        setBusinessData();
        billingNumberEntryValid = true;

        log.debug("McdUserInfo: loaded billing number entry " + billingNumberEntryToString());

    }

    private void setServiceSetting() {
    	try {
	        String[] serviceSettings = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_SERVICE_SETTING);
	        if (serviceSettings != null) {
	            for (int i = 0; i < serviceSettings.length; i++) {
	                if (serviceSettings[i].startsWith("replace=")) {
	                    if (serviceSettings[i].endsWith("=on")) {
	                        replace = true;
	                    }
	                } else if (serviceSettings[i].startsWith("reminder=")) {
	                    if (serviceSettings[i].endsWith("=on")) {
	                        unreadMessageReminder = true;
	                    }
	                } else if (serviceSettings[i].startsWith("update=")) {
	                    if (serviceSettings[i].endsWith("=on")) {
	                        updateSms = true;
	                    }
	                }
	            }
	        }
    	} catch (Exception ee) {
    		ee.printStackTrace(System.out);
    	}
    }
    /**
     * Set the businessData from MCD.
     */
    protected void setBusinessData() {
        if (subscriberCosProfile != null) {
            if (!billingNumberEntryValid) {
            	try {
            		String inhoursdow[] = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_BUSINESS_DOW);
            		if(inhoursdow != null && inhoursdow.length > 0) {
            			for(String s: inhoursdow) {
            				if(s.equalsIgnoreCase("1")) {
            					businessDays[Calendar.SUNDAY] = true;
            				}else if(s.equalsIgnoreCase("2")) {
            					businessDays[Calendar.MONDAY] = true;
            				}else if(s.equalsIgnoreCase("3")) {
            					businessDays[Calendar.TUESDAY] = true;
            				}else if(s.equalsIgnoreCase("4")) {
            					businessDays[Calendar.WEDNESDAY] = true;
            				}else if(s.equalsIgnoreCase("5")) {
            					businessDays[Calendar.THURSDAY] = true;
            				}else if(s.equalsIgnoreCase("6")) {
            					businessDays[Calendar.FRIDAY] = true;
            				}else if(s.equalsIgnoreCase("7")) {
            					businessDays[Calendar.SATURDAY] = true;
            				}
            			}
            		} else {
            			businessDays = defaultBusinessDays;
            		}

            		businessDayStart = getStringValue(DAConstants.ATTR_BUSINESS_START_TIME);
            		businessDayEnd = getStringValue(DAConstants.ATTR_BUSINESS_END_TIME);

                    if (businessDayStart == null || businessDayStart.equalsIgnoreCase("ToBeAddedInSchema")) {
                        businessDayStart = "08:00";
                    }
                    if (businessDayEnd == null || businessDayEnd.equalsIgnoreCase("ToBeAddedInSchema")) {
                        businessDayEnd = "17:00";
                    }
            	} catch (Exception ee) {
            		ee.printStackTrace(System.out);
            		businessDays = defaultBusinessDays;
            		businessDayStart = "08:00";
            		businessDayEnd = "17:00";
            	}
            }
        }
    }

    /**
     * Examine the emServiceDn attribute in order to determine if this cos for
     * services, for now only email service.
     */
    protected void setCosServices() {
        boolean tmp_emailService = false;
        boolean tmp_outdialNotification = false;
        boolean tmp_multilineService = false;
        boolean tmp_mwiService = false;
        boolean tmp_slamdown = false;
        boolean tmp_pagerService = false;
        boolean tmp_voiceService = false;
        boolean tmp_faxService = false;
        boolean tmp_videoService = false;
        boolean tmp_notificationService = false;
        boolean tmp_vvmService = false;
        boolean tmp_specialSMSMessageIndicationService = false;
        boolean tmp_autoPrintFaxService = false;
        boolean tmp_smsNotificationService = false;
        boolean tmp_flashNotificationService = false;
        boolean tmp_mmsNotificationService = false;
        boolean tmp_emailNotificationService = false;
        boolean tmp_forwardToEmailService = false;
        boolean tmp_mcnSubscribedService = false;

        for (int i = 0; i < services.length; i++) {
            if (services[i].indexOf("msgtype_email") > END_POS) {
                tmp_emailService = true;
            } else if (services[i].indexOf("outdial_notification") > END_POS) {
                tmp_outdialNotification = true;
            } else if (services[i].indexOf("multiline") > END_POS) {
                tmp_multilineService = true;
            } else if (services[i].indexOf("mwi_notification") > END_POS) {
                tmp_mwiService = true;
            } else if (services[i].indexOf("slamdown_notification") > END_POS) {
                tmp_slamdown = true;
            } else if (services[i].indexOf("pager_notification") > END_POS) {
                tmp_pagerService = true;
            } else if (services[i].indexOf("msgtype_fax") > END_POS) {
                tmp_faxService = true;
            } else if (services[i].indexOf("autoprint_fax") > END_POS) {
                tmp_autoPrintFaxService = true;
            } else if (services[i].indexOf("msgtype_voice") > END_POS) {
                tmp_voiceService = true;
            } else if (services[i].indexOf("msgtype_video") > END_POS) {
                tmp_videoService = true;
            } else if (services[i].indexOf(ProvisioningConstants.SERVICES_AUTO_FWD_MSG_TO_EMAIL) > END_POS) {
                tmp_forwardToEmailService = true;
            } else if (services[i].indexOf("notification_filters") > END_POS) {
                tmp_notificationService = true;
            } else if (services[i].indexOf(ProvisioningConstants.SERVICES_VVM) > END_POS) {
                tmp_vvmService = true;
            } else if (services[i].indexOf(ProvisioningConstants.SERVICES_SPECIAL_SMS_MESSAGE_INDICATION) > END_POS) {
                tmp_specialSMSMessageIndicationService = true;
            } else if (services[i].indexOf("sms_notification") > END_POS ) {
            	tmp_smsNotificationService = true;
            } else if (services[i].indexOf("flash_notification") > END_POS ) {
                tmp_flashNotificationService = true;
            } else if (services[i].indexOf("mms_notification") > END_POS ) {
            	tmp_mmsNotificationService = true;
            } else if (services[i].indexOf("email_notification") > END_POS ) {
            	tmp_emailNotificationService = true;
            } else if (services[i].indexOf(ProvisioningConstants.SERVICES_MCN_SUBSCRIBED) > END_POS) {
                tmp_mcnSubscribedService = true;
            }

        }

        emailService = tmp_emailService;
        outdialNotification = tmp_outdialNotification;
        multilineService = tmp_multilineService;
        mwiService = tmp_mwiService;
        slamdown = tmp_slamdown;
        pagerService = tmp_pagerService;
        voiceService = tmp_voiceService;
        faxService = tmp_faxService;
        videoService = tmp_videoService;
        notificationService = tmp_notificationService;
        vvmService = tmp_vvmService;
        autoPrintFaxService = tmp_autoPrintFaxService;
        specialSMSMessageIndicationService = tmp_specialSMSMessageIndicationService;
        smsNotificationService = tmp_smsNotificationService;
        flashNotificationService = tmp_flashNotificationService;
        mmsNotificationService = tmp_mmsNotificationService;
        emailNotificationService = tmp_emailNotificationService;
        forwardToEmailService = tmp_forwardToEmailService;
        mcnSubscribedService = tmp_mcnSubscribedService;
    }

    /**
     * Gets the name of the cos this user belongs to.
     */
    public String getCosName() {
        if (subscriberCosProfile != null) {
        	return subscriberCosProfile.getStringAttributes(DAConstants.ATTR_COS_IDENTITY)[0];
        } else {
            return null;
        }
    }

    /**
            case NTF_EML:
                return isDisabled("EML");
     * Checks if the user has CFU enabled.
     *
     * @return 0 - No CFU, 1 - CFU enabled, 2 - Error towards HLR
     */
    public int getDivertAll() {
       return 0; //not currently supported, requires fetching call forwarding from hlr via ss7 or custom plug-in
    }

    /**
     * fetches a new ExternalSubscriberInformation object.
     */
    public void fetchEsi() {
           //old moip solaris vfenl specific..
           //no longer supported but soem functionality available through hlr (ss7 or custom).
    }

    /**
     * Gets the users default number for fax printing.
     *
     * @return the fax print number.
     */
    public String getFaxPrintNumber() {
        return faxPrintNumber;
    }

    /**
     * @return the users notification filter.
     */
    public NotificationFilter getFilter() {
        return filter;
    }

    /**
     * @return a full identification of the user, e.g. the distinguished name
     *         in an LDAP user directory.
     */
    public String getFullId() {
        return subIdentity;
    }

    /**
     * Gets the users inbound fax number.
     *
     * @return the users fax  number.
     */
    public String getInboundFaxNumber() {
        return inboundFaxNo;
    }

    /**
     * @return the login name of the user.
     */
    public String getLogin() {
        return subIdentity;
    }

    /**
     * @return the users mail address.
     */
    public String getMail() {
        return mail;
    }

    /**
     * @return the users quota for number of messages in mailbox, -1 if no quota
     */
    public int getNoOfMailQuota() {
        return noOfMailQuota;
    }
    /**
     * @return the users quota for number of voice messages in mailbox, -1 if no quota
     */
    public int getNoOfVoiceMailQuota() {
        return noOfVoiceMailQuota;
    }
    /**
     * @return the users quota for number of fax messages in mailbox, -1 if no quota
     */
    public int getNoOfFaxMailQuota() {
        return noOfFaxMailQuota;
    }
    /**
     * @return the users quota for number of video messages in mailbox, -1 if no quota
     */
    public int getNoOfVideoMailQuota() {
        return noOfVideoMailQuota;
    }

    public int getQuotaWarnLvl()
    {
    	return quotaWarnLvl;
    }



    /**
     * @return the users telephone number for notifications.
     */
    public String getNotifNumber() {
        return notifNumber;
    }

    /**
     * @return The numbering plan of the users telephone number.
     */
    public int getNumberingPlan() {
        return numberingPlan;
    }

    /**
     * @return name of outdial schema from Cos, if none there return false
     */
    public String getOutdialSchema() {
        return outdialSchema;
    }

    /**
     * @return the pager notification controlstring.
     */
    public String getPnc() {
        return pnc;
    }

    /**
     * @return the date format preferred by the user (yyyy/mm/dd or mm/dd/yyy).
     */
    public String getPreferredDateFormat() {
        return preferredDateFormat;
    }

    /**
     * @return returns a string denoting the users preferred language.
     */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * @return returns a string denoting the users language extension or (branding).
     */
    public String getBrand() {
        return brand;
    }


    /**
     * @return the time format preferred by the user (12 or 24).
     */
    public String getPreferredTimeFormat() {
        return preferredTimeFormat;
    }

    /**
     * @return The users mail quota in bytes.
     */
    public int getQuota() {
        return mailQuota;
    }

    /**
     * @return a list of the services available to the user.
     */
    public String[] getServices() {
        if (cos == null) {
            return null;
        }
        return services;
    }

    public boolean hasMOIPCnService() {
        return cnServices != null && cnServices.contains("moip");
    }

    /**
     * @return the users telephone number.
     */
    public String getTelephoneNumber() {
        return subIdentity;
    }

    public String getTemporaryGreeting() {
        return temporaryGreeting;
    }

    public String getTimeZone() {
        loadBillingNumberEntry();
        return timeZone;
    }

    /**
     * @return The type of the users telephone number.
     */
    public int getTypeOfNumber() {
        return typeOfNumber;
    }

    /**
     * @return the emUserNTD value.
     */
    public String getUserNtd() {
        return userNtd;
    }

    /**
     * @return the date formatted according to the users preferences.
     */
    public String getUsersDate(Date d) {
        if (d == null) {
            return "";
        }

        SimpleDateFormat fmt=null;
        fmt = new SimpleDateFormat(preferredDateFormat.replace('m', 'M'));

        String tz=null;
        tz=getTimeZone();

        if (tz != null){
            TimeZone zone=TimeZone.getTimeZone(tz);;
            fmt.setTimeZone(zone);
        }

        return fmt.format(d);
    }

    /**
     * @return the time formatted according to the users preferences.
     */
    public String getUsersTime(Date d) {
        if (d == null) {
            return "";
        }

        DateFormat fmt=null;

        if ("12".equals(preferredTimeFormat)) {
            fmt = twelveHourFormat.get();
        } else { //24
            fmt = twentyFourHourFormat.get();
        }


        String tz=null;
        tz=getTimeZone();

        if (tz != null){
            TimeZone zone=TimeZone.getTimeZone(tz);;
            fmt.setTimeZone(zone);
        }

        return fmt.format(d);
    }

	@Override
	public String getVvmClientPrefix() {
		return vvmClientPrefix;
	}

	public int getVvmDestinationPort() {
		return vvmDestinationPort;
	}

    /**
     * @param type - the requested mail type (email, voice mail, fax mail)
     * @return true iff the user can handle the mail type.
     */
    public boolean hasMailType(int type) {
        if (cos == null) {
        	log.debug("McdUserInfo.hasMailType() cos object is null!!!");
        	return false;
        }

        switch (type) {
            case NTF_EMAIL:
            	return this.emailService;
            case NTF_FAX:
                return this.faxService;
            case NTF_VOICE:
            	return this.voiceService;
            case NTF_VIDEO:
                return this.videoService;
            default:
                return true;
        }
    }

    /**
     * @return true iff the user is an administrator.
     */
    public boolean isAdministrator() {
        return administrator;
    }

    /**
     * @return true if the user has autoprint of fax enabled.
     */
    public boolean isAutoPrintFax() {
        if (autoPrintFax && !autoprintFaxDisabled) {
            if (faxPrintNumber != null && !faxPrintNumber.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a time is business time according to the user settings.
     *
     * @param cal - the time that should be checked.
     * @return true if the supplied time is within the users business time.
     */
    public boolean isBusinessTime(Calendar cal) {

        loadBillingNumberEntry();
        try {
	        boolean isInBusinessDays = businessDays[cal.get(GregorianCalendar.DAY_OF_WEEK)];
	        boolean isAfterBusinessStartTime = false;
	        boolean isBeforeBusinessEndTime = false;

	        Calendar cal2 = new GregorianCalendar();
	        DateFormat df = new SimpleDateFormat("HH:mm");
	        Date businessStart = df.parse(businessDayStart);

	        cal2.setTime(businessStart);
	        cal2.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));

	        if(cal.after(cal2)) {
	        	isAfterBusinessStartTime = true;
	        }

	        Date businessEnd = df.parse(businessDayEnd);
	        cal2.setTime(businessEnd);
	        cal2.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));

	        if(cal.before(cal2)){
	        	isBeforeBusinessEndTime = true;
	        }
        	return isInBusinessDays && isAfterBusinessStartTime && isBeforeBusinessEndTime;
        } catch(Exception e) {
        	log.debug("McdUserInfo.isBusinessTime: error " + e.getMessage());
        }
        return true;


    }

    /**
     * @return true iff the user is enabled for multiline.
     */
    public boolean isMultilineUser() {
        if (cos == null) {
            return false;
        }
        return this.multilineService;
    }

    public boolean hasForwardToEmailService(){
        if (cos == null) {
            return false;
        }
        return forwardToEmailService;
    }

    /**
     * @return true if the user is enabled for mwi notifications.
     */
    public boolean isMwiUser() {
    	if (cos == null) {
    		return false;
    	}
    	return mwiService;
    }

    /**
     * @return true if the user has sms service enabled.
     */
    public boolean hasSmsService() {
    	if (cos == null) {
    		return false;
    	}
    	return smsNotificationService;
    }

    /**
     * @return true if the user has flash sms service enabled.
     */
    public boolean hasFlsService() {
        if (cos == null) {
            return false;
        }
        return flashNotificationService;
    }

    /**
     * @return true if the user has email service enabled.
     */
    public boolean hasEmailService() {
    	if (cos == null) {
    		return false;
    	}
    	return emailNotificationService;
    }

    /**
     * @return true if the user has mms service enabled.
     */
    public boolean hasMmsService() {
    	if (cos == null) {
    		return false;
    	}
    	return mmsNotificationService;
    }


    /**
     * @return true if the user has quota per message type.
     */
    public boolean isQuotaPerType()
    {
    	return isQuotaPerType;
    }

	@Override
	public boolean isNotificationServiceEnabled(int type) {
        switch (type) {
        case NTF_SMS:
            return hasSmsService();
        case NTF_FLS:
            return hasFlsService();
        case NTF_SIPMWI: //fall-through
        case NTF_MWI:
            // delivered SIPMWI or MWI are synonymous in the MOIPUserNtd
            return isMwiUser();
        case NTF_MMS:
            return hasMmsService();
        case NTF_ODL:
            return isOutdialUser();
        case NTF_EML:
            return hasEmailService();
    }
		return false;
	}

    /**
     * Checks if the user has the notification type on his filter.
     * Checks for all active filters for the specified type.
     *
     * @param type the type to check for.
     * @return true if the type exists on a active filter, false otherwise.
     */
    public boolean isNotifTypeOnFilter(String type) {
        return filter.isNotifTypeOnFilter(type);
    }


    public boolean isSystemMessageSMS()
    {
        return systemMessageSMS;
    }

    public boolean isSystemMessageTUI()
    {
        return systemMessageTUI;
    }


    public boolean isNotifTypeUnused(String type) {

           if (filter.isNotifDisabled()) { return true; }

           if (userNtd != null && userNtd.indexOf(type) >= 0 && userNtd.indexOf("-" + type) < 0) {
               return true;
           }

           if (!filter.hasNotifType(type)) { return true; }

           return false;
       }


    /**
     * @return true iff the user is enabled for outdial notifications.
     */
    public boolean isOutdialUser() {
        if (cos == null) {
            return false;
        }
        return this.outdialNotification;
    }

    /**
     *
     */
    public boolean isFaxOnlyUser() {
        return hasMailType(NTF_FAX) && ! hasMailType(NTF_VOICE);
    }

    /**
     * @return the expiration time of the users notifications (in hours).
     */
    public int getNotifExpTime() {
        if (cos == null) {
            return 6;
        }
        return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_flash() {
        int v = Config.getValidity_flash();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_smsType0() {
        int v = Config.getValidity_smsType0();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_mwiOn() {
        int v = Config.getValidity_mwiOn();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_mwiOff() {
        int v = Config.getValidity_mwiOff();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_mailQuotaExceeded() {
        int v = Config.getValidity_mailQuotaExceeded();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_mailQuotaHighLevelExceeded(){
        int v = Config.getValidity_mailQuotaHighLevelExceeded();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }



    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_temporaryGreetingOnReminder() {
        int v = Config.getValidity_temporaryGreetingOnReminder();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_voicemailOffReminder() {
        int v = Config.getValidity_voicemailOffReminder();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_cfuOnReminder() {
        int v = Config.getValidity_cfuOnReminder();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity_slamdown() {
        int v = Config.getValidity_slamdown();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    @Override
    public int getValidity_vvmSystemDeactivated() {
        int v = Config.getValidity_vvmSystemDeactivated();
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    /**
     * Gets a validity by the name of a kind of SMS. If no validity is set for
     * this kind of SMS, the general validity for notification (notifExpTime) is
     * used.
     *
     * @param name - the name of the SMS kind.
     * @return the validity of SMS messages of a certain kind (in hours).
     */
    public int getValidity(String name) {
        int v = Config.getValidity(name);
        if (v < 0) {
            return cos.getIntegerAttribute(DAConstants.ATTR_NOTIF_EXP_TIME);
        } else {
            return v;
        }
    }

    public boolean hasUnreadMessageReminder() {
    	if (cos == null) {
    		return false;
    	}
    	return this.unreadMessageReminder;
    }

    public boolean hasUpdateSms() {
    	if (cos == null) {
    		return false;
    	}
        return this.updateSms;
    }

    public boolean hasReplace() {
    	if (cos == null) {
    		return false;
    	}
        return this.replace;
    }

        public void terminalSupportsReplace(boolean supported) {
            terminalReplaceCapability = supported ? TERMINAL_HAS : TERMINAL_HASNOT;
        }

        public boolean terminalSupportsReplace() {
            if(!Config.isCheckTerminalCapability()) { return true; }

            switch (terminalReplaceCapability) {
            case TERMINAL_UNKNOWN:
                getExternalSubscriberInformation();
                if( esi == null || esi.getStatus() != 2 ) {
                    //  error getting esi data.
                    return Config.isDefaultUserHasReplace();
                } else {
                    terminalReplaceCapability = esi.isHasReplace() ? TERMINAL_HAS : TERMINAL_HASNOT;
                    return terminalReplaceCapability == TERMINAL_HAS;
                }

            case TERMINAL_HASNOT: return false;

            default: return true;
            }
        }

        public void terminalSupportsMwi(boolean supported) {
            terminalMwiCapability = supported ?  TERMINAL_HAS : TERMINAL_HASNOT;
        }

        public boolean terminalSupportsMwi() {
            if(!Config.isCheckTerminalCapability()) { return true; }

            switch (terminalMwiCapability) {
            case TERMINAL_UNKNOWN:
                getExternalSubscriberInformation();
                if( esi == null || esi.getStatus() != 2 ) {
                    //  error getting esi data.
                    return Config.isDefaultUserHasMwi();
                } else {
                    terminalMwiCapability = esi.isHasMwi() ? TERMINAL_HAS : TERMINAL_HASNOT;
                    return terminalMwiCapability == TERMINAL_HAS;
                }

            case TERMINAL_HASNOT: return false;

            default: return true;
            }
        }

        public void terminalSupportsFlash(boolean supported) {
            terminalFlashCapability = supported ?  TERMINAL_HAS : TERMINAL_HASNOT;
        }

        public boolean terminalSupportsFlash() {
            if(!Config.isCheckTerminalCapability()) { return true; }

            switch (terminalFlashCapability) {
            case TERMINAL_UNKNOWN:
                getExternalSubscriberInformation();
                if( esi == null || esi.getStatus() != 2 ) {
                    //  error getting esi data.
                    return Config.isDefaultUserHasFlash();
                } else {
                    terminalFlashCapability = esi.isHasFlash() ? TERMINAL_HAS : TERMINAL_HASNOT;
                    return terminalFlashCapability == TERMINAL_HAS;
                }

            case TERMINAL_HASNOT: return false;

            default: return true;
            }
        }


    /**
     * @return a printable representation of the data in this cos
     */
    public String toString() {
        String s = "";
        if (!valid) s = " not initialized";

        s += "subIdentity=" + subIdentity
                + " cos=" + serviceDn
                + " mail=" + mail
                + " filter=" + filter
                + " notif expiry time=" + notifExpTime
                + " notif number=" + notifNumber
                + " numbering plan=" + numberingPlan
                + " type of number=" + typeOfNumber
                + " date format=" + preferredDateFormat
                + " time format=" + preferredTimeFormat
                + " language=" + preferredLanguage
                + " disabled on user=" + userNtd
                + " no of mail quota= " + noOfMailQuota
                + " temporary greeting=" + temporaryGreeting
                + " unreadMessageReminder=" + (unreadMessageReminder ? "enabled" : "disabled")
                + " updateSms=" + (updateSms ? "enabled" : "disabled")
                + " replace=" + (replace ? "enabled" : "disabled")
                + " inbound fax number=" + faxPrintNumber
                + " autoprintFaxDisabled"+ autoprintFaxDisabled
                + " autoPrintFaxService"+ autoPrintFaxService
                + " faxService"+ faxService
                + " inbound fax number=" + inboundFaxNo


                + " " + billingNumberEntryToString();
        return "{McdUserInfo: " + s + "}";
    }

    /**
     * @return a printable representation of the billing number entry data in this cos
     */
    public String billingNumberEntryToString() {
        String s = "";

        if (billingNumberEntryValid) {
            s += "business days=";
            if (businessDays == null) {
                s += businessDays;
            } else {
                s += (businessDays[Calendar.MONDAY] ? "M" : ".");
                s += (businessDays[Calendar.TUESDAY] ? "T" : ".");
                s += (businessDays[Calendar.WEDNESDAY] ? "W" : ".");
                s += (businessDays[Calendar.THURSDAY] ? "T" : ".");
                s += (businessDays[Calendar.FRIDAY] ? "F" : ".");
                s += (businessDays[Calendar.SATURDAY] ? "S" : ".");
                s += (businessDays[Calendar.SUNDAY] ? "S" : ".");
            }
            s += ",start=" + businessDayStart
                    + ",end=" + businessDayEnd
                    + " timeZone=" + timeZone;
        } else {
            s += "billing number entry not loaded";
        }
        return "{" + s + "}";
    }

    /**
     * hashValue calculates a hash code for the user as the hash code for the
     * users uid.
     *
     * @return the users hash code.
     */
    public int hashCode() {
        return subIdentity.hashCode();
    }

    /**
     * Since all users come from the same database, where the uid is unique, two
     * users are equal if their uids are equal.
     *
     * @param o - the object to compare with.
     * @return true iff the uid of o equals this MurUm4_1UserInfo's uid.
     */
    public boolean equals(Object o) {
        return o != null
                && o.getClass() == getClass()
                && subIdentity.equals(((McdUserInfo) o).subIdentity);
    }


	public static void main(String[] args) {

		System.out.println("Running McdUserInfo test - lookup subscriber profile");
		McdUserInfo moipMcd = new McdUserInfo();
		moipMcd.findByMail("lmcdant@des8.com");

		System.out.println("Running McdUserInfo test - done");

	}

	@Override
	public boolean isPrepaid() {
		return prepaid;
	}

	@Override
	public void setPrePaid(boolean prepaid) {
		this.prepaid = prepaid;
	}

	@Override
	public String getMsid() {
		return this.msid;
	}

    @Override
    public boolean hasVvmService() {
        return vvmService;
    }


    @Override
    public String getVvmClientType() {
        return vvmClientType;
    }

    @Override
    public boolean hasSpecialSMSMessageIndicationService() {
        return specialSMSMessageIndicationService;
	}

    @Override
    public boolean isVVMNotificationAllowed(){
        return getProvisioningStatus().isVVMNotificationAllowed();
    }

    @Override
    public boolean isVVMActivated() {
        return getProvisioningStatus().isVVMActivated();
    }

    @Override
    /** @return true if the user allows VVM "Notifications allowed while roaming" */
    public boolean isVvmNotificationAllowedWhileRoaming() {
        return isVVMNotificationAllowedWhileRoaming;
    }

    @Override
    public boolean isVVMSystemActivated() {
        return getProvisioningStatus().isVVMSystemActivated();
    }

    private ProvisioningStatus getProvisioningStatus() {
        OAMManager commonOam = CommonOamManager.getInstance().getMcdOam();

        ProvisioningStatus st;
        if(isVVMAppleClient()){
            st = new AppleProvisioningStatus(commonOam, subscriberCosProfile.getSubscriberProfile().getProfile(), subscriberCosProfile.getCosProfile().getProfile());
        }else{
            st = new OMTPProvisioningStatus(commonOam, subscriberCosProfile.getSubscriberProfile().getProfile(), subscriberCosProfile.getCosProfile().getProfile());
        }
        return st;
    }

    @Override
    public boolean isVVMAppleClient(){
        String clientTypes = getVvmClientType().toLowerCase();
        if (clientTypes.equalsIgnoreCase(ProvisioningConstants.APPLEVVM_CLIENTTYPE.toLowerCase())){
            //log.debug("isAppleClient: Subscriber " + subscriber + " has a MOIPVvmClientType that indicates an Apple client");
            return true;
        }
        //log.debug("isAppleClient: Subscriber " + subscriber + " has a MOIPVvmClientType that indicates an OMTP client");
        return false;
    }

    @Override
    public boolean hasMcnSubscribedService() {
        return mcnSubscribedService;
    }

    @Override
    public String getMcnSubscribedState() {
        if (subscriberCosProfile != null) {
            return subscriberCosProfile.getStringAttributes(DAConstants.ATTR_MCN_SUBSCRIBED)[0];
        }
        return null;
    }
    
    @Override
    public String getHomeSystemID() {
        if (subscriberCosProfile != null) {
            return subscriberCosProfile.getStringAttributes(PAConstants.HOME_SYSTEM_ID)[0];
        }
        return null;
    }
    
    @Override
    public String getImapPassword() {
        if (subscriberCosProfile != null) {
         try{
            String passwordValue = subscriberCosProfile.getStringAttributes(VVMCLIENT_PASSWORD_ATTRIBUTE_NAME)[0];
            return PasswordFactory.getPassword("RSA").decode(passwordValue);
         }
         catch (NoSuchAlgorithmException e) {
            log.debug("Unable to decode password");
        }
        }
        return null;
    }

    @Override
    public int getBadLoginCount() {
        if (subscriberCosProfile != null) {
            return subscriberCosProfile.getIntegerAttributes(DAConstants.ATTR_BAD_LOGIN_COUNT)[0];
        }
        return 0;
    }

    @Override
    public int getMaxLoginLockout() {
        if (subscriberCosProfile != null) {
            return subscriberCosProfile.getIntegerAttributes(DAConstants.ATTR_MAX_LOGIN_LOCKOUT)[0];
        }
        return 0;
    }

    @Override
    public boolean hasAutoUnlockPinEnabled() {
        boolean result = false;
        if (subscriberCosProfile != null) {
            
            String autoUnlockEnabled = getStringValue(DAConstants.ATTR_AUTO_UNLOCK_PIN_ENABLED);
            if (autoUnlockEnabled != null) {
                if (autoUnlockEnabled.equalsIgnoreCase(ProvisioningConstants.YES)) {
                    result=true;
                }
            }
        }
        return result;
    }

    @Override
    public int getAutoUnlockPinDelay() {
        if (subscriberCosProfile != null) {
            return subscriberCosProfile.getIntegerAttributes(DAConstants.ATTR_AUTO_UNLOCK_PIN_DELAY)[0];
        }
        return 72;
    }
    
    @Override
    public boolean hasAutoUnlockPinSmsEnabled() {
        boolean result = true;
        if (subscriberCosProfile != null) {
            
            String autoUnlockEnabled = getStringValue(DAConstants.ATTR_AUTO_UNLOCK_PIN_SMS_ENABLED);
            if (autoUnlockEnabled != null) {
                if (autoUnlockEnabled.equalsIgnoreCase(ProvisioningConstants.NO)) {
                    result=false;
                }
            }
        }
        return result;
    }

    @Override
    public Date getLastPinLockoutTime() {
        
        if (subscriberCosProfile != null) {
            String date = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_USER_LAST_PIN_LOCKOUT_TIME)[0];
            log.debug("McdUserInfo() - getLastPinLockoutTime:" + date);
            try {
                return lockoutDateFormat.get().parse(date);
            } catch (ParseException e) {
                log.debug("McdUserInfo() - getLastPinLockoutTime: Could not parse date " + date + ", returning null.");
            }
        }
        return null;
    }

    @Override
    public ExternalSubscriberInformation getExternalSubscriberInformation() {
        // not supported anymore.. old VFENL MoiP Solaris code.
        return null;
    }

    @Override
    public boolean hasDeposiType(depositType type) {
        if (cos == null) {
            log.debug("McdUserInfo.hasMailType() cos object is null!!!");
            return false;
        }

        switch (type) {
            case EMAIL:
                return this.emailService;
            case FAX:
                return this.faxService;
            case VOICE:
                return this.voiceService;
            case VIDEO:
                return this.videoService;
            default:
                return true;
        }
    }
    
}
