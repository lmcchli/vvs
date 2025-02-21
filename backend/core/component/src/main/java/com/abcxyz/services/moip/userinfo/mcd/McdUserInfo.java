package com.abcxyz.services.moip.userinfo.mcd;

import java.util.GregorianCalendar;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.common.directoryaccess.MoipProfile;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.abcxyz.services.moip.userinfo.NotificationFilter;
import com.abcxyz.services.moip.userinfo.UserInfo;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;

/**
 * McdUserInfo is an implementation of UserInfo that handles the MUR schema
 * version.
 */
public class McdUserInfo implements UserInfo, McdUserFinder {

    protected static final int END_POS = -1;
    private static final int INT_NOT_DEFINE = -1;

    protected MoipProfile cos = null;
    protected NotificationFilter filter;
    protected NotificationFilter roamingFilter;
    protected String subIdentity = null;  // equal to old uid
    protected String mail = null;
    protected String telephoneNumber = null;  // not supported in miab1.0
    protected String serviceDn = null;
    protected String notifNumber = null;
    protected String preferredDateFormat = null;
    protected String preferredTimeFormat = null;
    protected String preferredLanguage = null;
    protected String userNtd = null;
    protected String pnc = null;  //not supported in miab1.0
    protected String timeZone = null;
    protected String temporaryGreeting = null;
    protected String faxPrintNumber = null;  // not supported in miab1.0
    protected String inboundFaxNo = null;    // not supported in miab1.0

    protected int businessDayStart = -1;
    protected int businessDayEnd = -1;
    protected int notifExpTime = -1;
    protected int numberingPlan = 0;
    protected int typeOfNumber = 0;
    protected int mailQuota = -1;
    protected int noOfMailQuota = -1;

    protected boolean administrator = false;
    protected boolean valid = false; //True when all data from the user entry are loaded
    protected boolean notifDisabled = false;
    protected boolean billingNumberEntryValid = false; //True when all data from the billing number entry are loaded
    protected boolean autoPrintFax = false;  // not supported in miab1.0
    protected boolean unreadMessageReminder = true;
    protected boolean updateSms = true;
    protected boolean replace = true;

    protected boolean[] businessDays = null;

    protected int terminalReplaceCapability;
    protected int terminalMwiCapability;
    protected int terminalFlashCapability;
    private static final LogAgent logger = CommonOamManager.getInstance().getLogger();

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

    protected String [] filterstrings = null;

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
    protected boolean notificationService = false;
    protected boolean emailService = false;
    protected boolean ttsEmailEnabled = false;
    protected String outdialSchema = null;
    protected int maxSlamdownInfoFiles = 200;

    protected String [] roamingfilterstrings = null;

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
     * @param identity
     * @param language
     */
    public McdUserInfo(String identity, String language) {
        subIdentity = identity;
        preferredLanguage = language;
        businessDays = defaultBusinessDays;
        da = null;
    }

    /**
     * findUserByTelephoneNumber looks for a user based on a telephonenumber.
     *
     * @param nbr - the telephonenumber identifying the user.
     * @return a new UserInfo object for the found user, or null if no user was
     *         found.
     */
    public boolean findByTelephoneNumber(String subsNumber) {
        boolean found = false;

        //SubscriberProfile = entry
        if (subscriberCosProfile == null) {
        	subscriberCosProfile = da.lookupSubscriber(subsNumber);
        }

        if(subscriberCosProfile != null) {
	        cos = da.lookupCos(subscriberCosProfile.getStringAttributes(DAConstants.ATTR_COS_IDENTITY)[0]);

	        if (subscriberCosProfile == null) {
	            found = false;
	        } else {
	            found = loadProfile(subsNumber);
	        }
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
    	    logger.warn("McdUserInfo.getStringValue() Exception occured - Attribute is not defined in schema: " + attribute+" "+ee,ee);
    	}

        // need to take care of a list string too...
        return result;

    }

    protected boolean getBooleanValue(String attribute) {
    	boolean result  = false; // default
    	try {
	    	boolean [] temp;
	        temp = subscriberCosProfile.getBooleanAttributes(attribute);
	        if ( temp != null ) {
	        	result = temp[0];
	        }
	    } catch (Exception ee) {
	           logger.warn("McdUserInfo.getBooleanValue() Exception occured - Attribute is not defined in schema: " + attribute+" "+ee,ee);
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
            logger.warn("McdUserInfo.getIntValue() Exception occured - Attribute is not defined in schema: " + attribute+" "+ee,ee);

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

	    	String[] profileStrings;

	    	subIdentity       = identity;
	        serviceDn         = getStringValue (DAConstants.ATTR_COS_IDENTITY);
	        notifNumber       = getStringValue (DAConstants.ATTR_NOTIF_NUMBER);
	        userNtd           = getStringValue (DAConstants.ATTR_USER_NTD);
	        temporaryGreeting = getStringValue (DAConstants.ATTR_TMP_GRT);
	        notifExpTime      = getIntValue    (DAConstants.ATTR_NOTIF_EXP_TIME);
	        outdialSchema     = getStringValue (DAConstants.ATTR_OUTDIAL_SEQUENCE);
	        notifDisabled     = getBooleanValue(DAConstants.ATTR_NOTIF_DISABLED) || (cos == null);
	        noOfMailQuota     = getIntValue    (DAConstants.ATTR_NO_OF_MAIL_QUOTA);

	        services          = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_SERVICES);
	        filterstrings     = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_FILTER);
	        roamingfilterstrings = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_ROAMING_FILTER);
	        profileStrings    = subscriberCosProfile.getStringAttributes(DAConstants.ATTR_DELIVERY_PROFILE);
	        mail              = subscriberCosProfile.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MAIL);
	        if (null != subscriberCosProfile.getIntegerAttributes(DAConstants.ATTR_MAX_SLAMDOWN_INFO_FILES)) {
		        maxSlamdownInfoFiles = subscriberCosProfile.getIntegerAttributes(DAConstants.ATTR_MAX_SLAMDOWN_INFO_FILES)[0];
		        if (maxSlamdownInfoFiles == 0) {
		        	maxSlamdownInfoFiles = 200;
		        }
	        }

	        preferredDateFormat = getStringValue(DAConstants.ATTR_PREFERRED_DATE_FORMAT);
	        preferredTimeFormat = getStringValue(DAConstants.ATTR_PREFERRED_TIME_FORMAT);
	        preferredLanguage = getStringValue(DAConstants.ATTR_PREFERRED_LANGUAGE);

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

	        // missing in schema - so we use the config one
	        numberingPlan = 0;//Config.getNumberingPlanIndicator();
	        typeOfNumber  = 0;//Config.getTypeOfNumber();

	        filter = new NotificationFilter(filterstrings, notifDisabled, this, profileStrings);
	        roamingFilter = new NotificationFilter(roamingfilterstrings, notifDisabled, this, profileStrings);

	        valid = true;

    	} catch (Exception ee) {
            logger.warn("McdUserInfo.loadProfile() Exception occured - identity: " + identity+" "+ee,ee);
    	}
        return true;
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
            logger.warn("McdUserInfo.setServiceSetting() Exception occured - "+ee,ee);

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
        boolean tmp_videoService = false;
        boolean tmp_notificationService = false;

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
            } else if (services[i].indexOf("msgtype_voice") > END_POS) {
                tmp_voiceService = true;
            } else if (services[i].indexOf("msgtype_video") > END_POS) {
                tmp_videoService = true;
            } else if (services[i].indexOf("notification_filters") > END_POS) {
                tmp_notificationService = true;
            }
        }

        emailService = tmp_emailService;
        outdialNotification = tmp_outdialNotification;
        multilineService = tmp_multilineService;
        mwiService = tmp_mwiService;
        slamdown = tmp_slamdown;
        pagerService = tmp_pagerService;
        voiceService = tmp_voiceService;
        videoService = tmp_videoService;
        notificationService = tmp_notificationService;
    }

    /**
     * @return the users notification filter.
     */
    public NotificationFilter getFilter() {
        return filter;
    }

    /**
     * @return the users roaming notification filter.
     */
    public NotificationFilter getRoamingFilter() {
        return roamingFilter;
    }

    /**
     * @return the users telephone number for notifications.
     */
    public String getNotifNumber() {
        return notifNumber;
    }
    
    /**
     * @return the users cos maximum number of Slamdown Information files that can be accumulated.
     */
    public int getMaxSlamdownInfoFiles () {
    	return maxSlamdownInfoFiles;
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
}
