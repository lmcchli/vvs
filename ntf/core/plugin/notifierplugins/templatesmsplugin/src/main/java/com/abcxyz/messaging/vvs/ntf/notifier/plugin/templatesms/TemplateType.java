/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.NotifierEventRetryInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms.NotifierPhoneOnMethod;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.ANotifierSendInfoSms.NotifierSmppPduType;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.IntfServiceTypePidLookup;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.schedule.NotifierEventHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfig;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util.NotifierConfigConstants;

/**
 * 
 */
public class TemplateType implements NotifierConfigConstants {

    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(TemplateType.class);
    public static final String SERVICE_NAME_SEPARATOR = "@";
	  
    public static final String IN_PROGRESS_FILENAME_POSTFIX = ".IN.PROGRESS";
    public static final String IN_PROGRESS_LOCK_FILENAME_POSTFIX = ".LOCK.IN.PROGRESS"; //used to lock the in progress check before read write..
    
    public static final String DEFAULT_TEMPLATE_TYPE_NAME="default"; //the default notifier type from the config - to use if specific not defined.


    private static volatile Map<String, TemplateType> templateTypeTable = new HashMap<String, TemplateType>();
    private static volatile Map<String, TemplateType> templateTypeDefaultTable = new HashMap<String, TemplateType>();
    
    private static volatile boolean PlugInUsingCancel = false; //if any type are using cancel this is true.

    public static boolean isPlugInUsingCancel() {
    	if (templateTypeTable.size() > 0) { //cancel only for specific types defined in the config.
    		return PlugInUsingCancel;
    	}
    	return false;    	
	}

	private String notifierTypeName = null;
    private NotifierSmppPduType notifierTypePdu = null;
    private CPHRNotifTYPE cphrNotifType = null;
    private int statusFileValidityInMin = 0;
    private NotifierTypeNotificationNumber notifierTypeNotificationNumber = null;
    private String serviceType = "";
    private int replacePosition = -1; //indicates position in NTF replace list if on for type.
    private int mdrPortType = 1;
    private String mdrName = null;
    private String cphr_template_name=null; 
    private boolean cancelEnabledForType=false;
    
   
    public boolean isCancelEnabled() {
		return cancelEnabledForType;
	}

	// Scheduler schemas
    private int schedulerInitialDelayInSeconds = 0;
    private NotifierEventRetryInfo schedulerSendingSchema = null;
    private NotifierEventRetryInfo schedulerInitialSchema = null;
    
    //in progress file name and validity. These are used to indicate that a template of particular name are in progress.
	private int inProgressFileValidityTime = 0; //0 indicates never expires..
	private String inProgressFilename ="";
	private String inProgressLockFileName;

    // COS-specific parameters
    private String enabledCos = "true";
    
    private boolean defaultTemplateType = false; //indicates this type matched the default template configuration in the config.
    private boolean isLoaded = false;
	private IntfServiceTypePidLookup ntfServiceLookup = null;
	
	
	   
    public boolean isDefaultTemplateType() {
		return defaultTemplateType;
	}

	public static synchronized boolean refreshConfig()  {
    	PlugInUsingCancel=false;
        Map<String, TemplateType> newLoadedNotifierTypes = new HashMap<String, TemplateType>();
        
        Map<String, Map<String, String>> notifierTypesFromConfig = NotifierConfig.getcphrTable();        
        if (notifierTypesFromConfig != null && !notifierTypesFromConfig.isEmpty()) {
            Iterator<String> iter = notifierTypesFromConfig.keySet().iterator();
            while(iter.hasNext()) {
                String newNotifierTypeName = iter.next();
                TemplateType notifierType = new TemplateType(newNotifierTypeName);
                if(notifierType.isLoaded) {
                    newLoadedNotifierTypes.put(newNotifierTypeName.toLowerCase(), notifierType);
                    log.debug("refreshConfig: added NotifierType for " + newNotifierTypeName);
                }
            }
        } else {
            log.error("no template types found in config - cphrTypes map will be empty.");
            return false;
        }
        
        if (!newLoadedNotifierTypes.containsKey(DEFAULT_TEMPLATE_TYPE_NAME))
        {
        	log.error("No default type defined in config for templateSms table - will not be able to send generic templates.");
        	return false;
        }
        
        
        if (templateTypeTable.isEmpty()) {
        	//first time or no types were loaded first time. we don't need to prune the templateTypeDefaultTable
        	templateTypeTable = newLoadedNotifierTypes;
            return true;
        }
        	
        //prune the default Table if any new types are the same as default types.
        if (!newLoadedNotifierTypes.get(DEFAULT_TEMPLATE_TYPE_NAME).equals(templateTypeTable.get(DEFAULT_TEMPLATE_TYPE_NAME))) {
        	templateTypeDefaultTable.clear(); //default values changed - need to rebuild it as the new event come in. with the new values. 	
        } else {
        	Iterator<String> iter = newLoadedNotifierTypes.keySet().iterator();
        	while (iter.hasNext()) {
        		String key=iter.next();
        		templateTypeDefaultTable.remove(key);
        	}
        }
        
        templateTypeTable = newLoadedNotifierTypes;
        return true;
    }
    
    public static synchronized TemplateType getTemplateType(String templateEntryName) {
    	TemplateType result = templateTypeTable.get(templateEntryName);
    	if (result == null) {
    		result = templateTypeDefaultTable.get(templateEntryName);
    	}
    	return result;
    }
    
    public static boolean isTemplateTypeDefined(String templateEntryName) {
        if (!templateTypeTable.containsKey(templateEntryName)) {
        	return templateTypeDefaultTable.containsKey(templateEntryName);
        } 
        return true;
        
    }
    
    public static synchronized void addTemplateTypeFromDefault(String templateEntryName) {
    	templateEntryName=templateEntryName.toLowerCase();
    	if (isTemplateTypeDefined(templateEntryName)) {
    		return; //already defined..
    	}
    	
    	TemplateType template = new TemplateType(getTemplateType(DEFAULT_TEMPLATE_TYPE_NAME)); //get a copy of the default.
    	
    	//update type specific variables
    	template.cphr_template_name=templateEntryName;
    	template.notifierTypeName=templateEntryName;
    	  	
    	template.inProgressFilename= templateEntryName + IN_PROGRESS_FILENAME_POSTFIX;;
        template.inProgressLockFileName=templateEntryName + IN_PROGRESS_LOCK_FILENAME_POSTFIX;
        template.cancelEnabledForType=false; //must be false for all default types, to enable cancel must be specifically defined.
    	
        //add it to the default template table.
    	templateTypeDefaultTable.put(templateEntryName,template);
    	NotifierEventHandler.get().addnewSchedule(template); //create a new schedule for it if needed.
    	return;
    }

    /**
     * Constructor
     * @param notifierTypeName Name of the notification type
     */
    private TemplateType(String notifierTypeName) {
        this.notifierTypeName = notifierTypeName;
        loadConfiguration();
    }

    /* copy constructor */
    private TemplateType(TemplateType templateType) {
		this.cphr_template_name=templateType.cphr_template_name;
		this.notifierTypeName=templateType.notifierTypeName;
		this.notifierTypePdu=templateType.notifierTypePdu;
		this.cphrNotifType=templateType.cphrNotifType;
		this.statusFileValidityInMin=templateType.statusFileValidityInMin;
		this.notifierTypeNotificationNumber=templateType.notifierTypeNotificationNumber;
		this.serviceType=templateType.serviceType;
		this.mdrPortType=templateType.mdrPortType;
		this.mdrName=templateType.mdrName;
		this.cphr_template_name=templateType.cphr_template_name; 
		this.schedulerInitialDelayInSeconds=templateType.schedulerInitialDelayInSeconds;
		this.schedulerSendingSchema=templateType.schedulerSendingSchema;
		this.schedulerInitialSchema=templateType.schedulerInitialSchema;
		this.inProgressFileValidityTime=templateType.inProgressFileValidityTime;
		this.inProgressFilename=templateType.inProgressFilename;
		this.inProgressLockFileName=templateType.inProgressLockFileName;
		this.isLoaded=templateType.isLoaded;
		this.enabledCos=templateType.enabledCos;
		this.cancelEnabledForType=templateType.cancelEnabledForType;
		this.replacePosition=templateType.replacePosition;
	}
    
    //returns if the value of templateType is the same rather than just the same object.
    boolean equals(TemplateType templateType) {
    	    	
    	if (templateType==this) return true; //if same object return true;   	
    	
    	boolean result = this.cphr_template_name==templateType.cphr_template_name &&
		this.notifierTypeName.equals(templateType.notifierTypeName) &&
		this.notifierTypePdu.equals(templateType.notifierTypePdu) &&
		this.cphrNotifType.equals(templateType.cphrNotifType) &&
		this.statusFileValidityInMin==templateType.statusFileValidityInMin &&
		this.notifierTypeNotificationNumber.equals(templateType.notifierTypeNotificationNumber) &&
		this.serviceType.equals(templateType.serviceType) &&
		this.mdrPortType==templateType.mdrPortType &&
		this.mdrName.equals(templateType.mdrName) &&
		this.cphr_template_name.equals(templateType.cphr_template_name) && 
		this.schedulerInitialDelayInSeconds==templateType.schedulerInitialDelayInSeconds &&
		this.schedulerSendingSchema.equals(templateType.schedulerSendingSchema) &&
		this.schedulerInitialSchema.equals(templateType.schedulerInitialSchema) &&
		this.inProgressFileValidityTime==templateType.inProgressFileValidityTime &&
		this.inProgressFilename.equals(templateType.inProgressFilename) &&
		this.inProgressLockFileName.equals(templateType.inProgressLockFileName) &&
		this.isLoaded==templateType.isLoaded &&
		this.enabledCos.equals(templateType.enabledCos) &&
    	this.cancelEnabledForType==templateType.cancelEnabledForType;
    	this.replacePosition=templateType.replacePosition;
    	
    	return result;
    }
    
	public static NotifierSmppPduType mapToNotifierSmppPduType(String notifierTypePdu) {
        NotifierSmppPduType type = NotifierSmppPduType.SUBMIT_SM;
        try {
            type = NotifierSmppPduType.valueOf(notifierTypePdu.toUpperCase());
        } catch(Exception e) {
            log.debug("No map to notifierTypePdu " + notifierTypePdu + ", " + type.toString() + " will be used) for table entry ");
        }
        return type;
    }

    public static NotifierPhoneOnMethod mapToNotifierPhoneOnMethod(String notifierTypePhoneOn) {
        NotifierPhoneOnMethod type = NotifierPhoneOnMethod.NONE;
        try {
            type = NotifierPhoneOnMethod.valueOf(notifierTypePhoneOn.toUpperCase());
        } catch(Exception e) {
            log.debug("No map to notifierTypePhoneOn " + notifierTypePhoneOn + ", " + type.toString() + " will be used)");
        }
        return type;
    }

    
    /** NotifierType - HandledNotification */
    public enum CPHRNotifTYPE {
    	/**
    	 * the action the template SMS will take i.e. does it need to check subscriber profile and mailbox, subscriber or nothing.
    	 */
    	//TODO implement NONSUBSCRIBER - currently not in xsd file.  The intention of this is to use a fake profile or similar to allow sms to be sent to a non subscriber.
        MAILBOXSUBSCRIBER      	("mailboxSubscriber"),  		// need to check mailbox and subscriber
        SUBSCRIBERONLY			("subscriberOnly"), 			// needs to check subscriber but no mailbox. 
        NONSUBSCRIBER			("nonSubscriber"); 				// a message to a non subscriber. (not fully implemented, planned for IVR etc.).
        
        // add additional type here.

        private String name;

        CPHRNotifTYPE(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    
    /** NotifierType - notificationNumber */
    public enum NotifierTypeNotificationNumber {
        // Supported types
        RECIPIENT      		("recipient"),
        DELIVERY_PROFILE	("delivery_profile");

        private String name;

        NotifierTypeNotificationNumber(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    
    public static NotifierTypeNotificationNumber mapToNotifierTypeNotificationNumber(String notifierTypeNotificationNumber) {
        NotifierTypeNotificationNumber type = NotifierTypeNotificationNumber.RECIPIENT;
        try {
            type = NotifierTypeNotificationNumber.valueOf(notifierTypeNotificationNumber.toUpperCase());
        } catch(Exception e) {
            log.debug("No map to notifierTypeNotificationNumber " + notifierTypeNotificationNumber + ", " + type.getName() + " will be used)");
        }
        return type;
    }

    /** NotifierType - States */
    public enum NotifierTypeState {
        STATE_INITIAL                 ("initial"),
        STATE_SENDING                 ("sending"),
        STATE_DONE                    ("done");

        private String name;

      NotifierTypeState(String name) {
      this.name = name;
  }
        
        
        public String getName() {
            return name;
        }

    }

    public static NotifierTypeState mapToNotifierTypeState(String notifierTypeState) {
        NotifierTypeState type = NotifierTypeState.STATE_INITIAL;

        try {
            type = NotifierTypeState.valueOf(notifierTypeState.toUpperCase());
        } catch(Exception e) {
            log.debug("No map to notifierTypeState " + notifierTypeState + ", " + type.getName() + " will be used)");
        }
        return type;
    }

    /** NotifierType - Events */
    public enum NotifierTypeEvent {
        EVENT_NEW_NOTIF                           ("New notification"),
        EVENT_SCHEDULER_RETRY                     ("Scheduler Retry"),
        EVENT_SCHEDULER_EXPIRY                    ("Scheduler Expiry"),
        EVENT_CLIENT_RETRY                        ("SMS Client Retry"),
        EVENT_CLIENT_FAILED                       ("SMS Client Failed"),
        EVENT_SMS_INFO_RESPONSE_SUCCESSFUL        ("SMS-Info Response successful");
        
        private String name;

            NotifierTypeEvent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static NotifierTypeEvent mapToNotifierTypeEvent(String notifierTypeEvent) {
        NotifierTypeEvent type = NotifierTypeEvent.EVENT_NEW_NOTIF;
        try {
            type = NotifierTypeEvent.valueOf(notifierTypeEvent.toUpperCase());
        } catch(Exception e) {
            log.debug("No map to notifierTypeEvent " + notifierTypeEvent + ", " + type.getName() + " will be used)");
        }
        return type;
    }

    public String getCphrTypeName() {
        return this.notifierTypeName;
    }

    public NotifierSmppPduType getNotifierTypePdu() {
        return this.notifierTypePdu;
    }
    
    public NotifierTypeNotificationNumber getNotifierTypeNotificationNumber() {
        return this.notifierTypeNotificationNumber;
    }
    
    public CPHRNotifTYPE getCphrNotifType() {
      return this.cphrNotifType;
    }
    
    public int getStatusFileValidityInMin() {
        return this.statusFileValidityInMin;
    }

    public String getServiceType() {
        return this.serviceType;
    }
    
    public int getMdrPortType() {
        return this.mdrPortType;
    }

    public String getMdrName() {
        return this.mdrName;
    }
    
    public int getSchedulerInitialDelayInSeconds() {
        return schedulerInitialDelayInSeconds;
    }
    
    public NotifierEventRetryInfo getSchedulerInitialSchema() {
        return schedulerInitialSchema;
    }

    public NotifierEventRetryInfo getSchedulerSendingSchema() {
        return schedulerSendingSchema;
    }

    public String getCphr_template_name() {
		return cphr_template_name;
	}

	public boolean isLoaded() {
        return isLoaded;
    }
    
    /**
     * Returns whether or not this notifier type is enabled for the given COS.
     *
     */
    public boolean isEnabledforCos(String cos) {
        return getCosSpecificParameterValueBoolean(cos, enabledCos,  ENABLED_COS);
    }
      
    /**
     * Returns the value of a parameter boolean per cos
     */
    private boolean getCosSpecificParameterValueBoolean(String cos, String parameterString, String paramName) {
        String result = getCosSpecificParameterValue(cos, parameterString, paramName);
        try {
            return Boolean.parseBoolean(result);
        } catch (NumberFormatException nfe) {
            log.debug("Could not parse boolean value. Value is \"" + result + "\". Returning false.");
            return false;
        }
    }
    
    /**
     * Returns the value of a parameter integer per cos
     */
    @SuppressWarnings("unused")
	private int getCosSpecificParameterValueInteger(String cos, String parameterString, String paramName) {
        String result = getCosSpecificParameterValue(cos, parameterString, paramName);
        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException nfe) {
            log.debug("Could not parse integer value. Value is \"" + result + "\". Returning 0.");
            return 0;
        }
    }
    
    /**
     * Returns the value of a parameter string per cos
     */
    private static String getCosSpecificParameterValue(String cos, String parameterString, String paramName) {
        String[] keyValuePairs = parameterString.split(",");
        boolean defaultFound = false;
        String defaultValue = null;
        
        for (String keyValuePair : keyValuePairs) {
            String[] keyValue = keyValuePair.split("=");

            // Case - default validity
            if (keyValue.length == 1) {
                if (!defaultFound) {
                    defaultValue = keyValue[0];
                    defaultFound = true;
                    continue;
                } else {
                    log.debug("Previous " + paramName + " default value already found, " + defaultValue + " will be kept instead of " + Integer.parseInt(keyValue[0]));
                    continue;
                }
            }

            // Case - key-value pair
            String cos2Check = keyValue[0].toLowerCase();
            if (!cos2Check.startsWith("cos:")) {
            	cos2Check = "cos:" + cos2Check;
            }
            if (cos != null && cos2Check.equalsIgnoreCase(cos) && keyValue.length == 2) {
                String value = keyValue[1];
                log.debug("Found value of " + paramName + " for cos " + cos + ", value is " + value);
                return value;
            }
        }
        
        log.debug("Did not find cos-specific value for cos " + cos + " in parameter value String " + parameterString + " for paramname: " + paramName + " - returning default value " + defaultValue);
        
        return defaultValue;
    }
    
    private void loadConfiguration() {
    	
    	ntfServiceLookup  = TemplateSmsPlugin.getNtfServiceTypePidLookup();

        // Get the configuration for the given notifierType
        Map<String, String> notifierType = NotifierConfig.getcphrTypeEntry(this.notifierTypeName);
        if (this.notifierTypeName.equalsIgnoreCase(DEFAULT_TEMPLATE_TYPE_NAME)) {
        	defaultTemplateType =true;
        }
        if (notifierType != null) {
        	try {
        		
        		// COS-specific parameters
        		enabledCos = getNotifierTypeParameterValue(notifierType,  ENABLED_COS); 
        		
        		//This is now not strictly necessary, as used to be was loaded from configuration, now it's the same as the eventName, but to
        		//avoid changing other code that relies on it, just set it to the event name here.
        		cphr_template_name = this.notifierTypeName.toLowerCase(); //Content/template name are always lower case internally.
        		       		
        		if ( !this.defaultTemplateType && (!TemplateSmsPlugin.getMessageGenerator().doesCphrTemplateExist(cphr_template_name)))
        		{
        			log.warn("Unable to load notifierType: " + notifierTypeName + " the cphr template phrase [" + cphr_template_name +"] does not exist, the notification type will not be enabled!");
        			log.warn("Please define the phrase in /opt/moip/config/ntf/templates/*.cphr.");
        			isLoaded=false;     
        			return;
        		}
        		
        		String strNotifierNotification = NotifierConfig.getNotifierTypeParameterDefaultValue( CPHR_NOTIF_TYPE);
        		try {
        			cphrNotifType = CPHRNotifTYPE.valueOf(strNotifierNotification.toUpperCase());
        		} catch (Exception e ) {
        			log.warn("Invlalid value set for TemplateType [" + strNotifierNotification +"] - in templateSms.Table for entry " + this.notifierTypeName);
        			cphrNotifType = CPHRNotifTYPE.MAILBOXSUBSCRIBER;
        		}
        		
        		
        		notifierTypePdu = mapToNotifierSmppPduType(getNotifierTypeParameterValue(notifierType,  PDU_TYPE));
        		statusFileValidityInMin = Integer.parseInt(getNotifierTypeParameterValue(notifierType,  STATUS_FILE_VALIDITY_IN_MIN));
        		determineServiceType(notifierType,cphr_template_name);
        		if (!NotifierConfig.isDisableSmscReplace() && Boolean.parseBoolean((getNotifierTypeParameterValue(notifierType,  REPLACE_SMS_ENABLED)))) {
        			//depends on table will be checked before sending if disabled in MCD profile.
        			//REPLACE_SMS_ENABLED is false by default.
        			int positions[] = ntfServiceLookup.getPositionByServiceType(serviceType);
        			if (positions.length>1)
        			{
        				//try to determine which one for this template/content/phrase, if not assume first and print warning.
        				int pos = ntfServiceLookup.getPosition(this.cphr_template_name);
        				if (pos == -1 ) {
        					log.warn("There are duplicate service Type in NTF notification.conf replace.list for serviceType/templatType[" + serviceType +"] for template phrase [ " + cphr_template_name +"], assuming the first one as specific content/phrase not defined in list.");
        					log.warn("This may result in the wrong SMS being replaced in mobile as PID could be different, best to specifically define the phrase in the replace.list ");
        					replacePosition=positions[0];
        				} else {
        					//out content phrase is defined in the replace Table so we know which one to use.
        					replacePosition = pos;
        				}       					        				
        			} else {
        				replacePosition=positions[0];
        			}
        		} else {
        			replacePosition=-1; //no replace.
        		}
        		mdrPortType = Integer.parseInt(getNotifierTypeParameterValue(notifierType,  MDR_PORT_TYPE));
        		mdrName = getNotifierTypeParameterValue(notifierType,  MDR_NAME);
        		if (mdrName == null || mdrName.isEmpty()) mdrName = this.notifierTypeName;

        		notifierTypeNotificationNumber = mapToNotifierTypeNotificationNumber(getNotifierTypeParameterValue(notifierType,  NOTIFICATION_NUMBER_TYPE));
        		
                // Scheduler schema - Initial
                schedulerInitialSchema = new NotifierEventRetryInfo(this.notifierTypeName + TemplateType.SERVICE_NAME_SEPARATOR + NotifierTypeState.STATE_INITIAL.getName());
                schedulerInitialSchema.setEventRetrySchema(getNotifierTypeParameterValue(notifierType,  INTITIAL_RETRY_SCHEMA));
                schedulerInitialSchema.setExpireTimeInMinute(Integer.parseInt(getNotifierTypeParameterValue(notifierType,  INTITIAL_EXPIRE_TIME_IN_MIN)));
                schedulerInitialSchema.setExpireRetryTimerInMinute(Integer.parseInt(getNotifierTypeParameterValue(notifierType,  EXPIRY_INTERVAL_IN_MIN)));
                schedulerInitialSchema.setMaxExpireTries(Integer.parseInt(getNotifierTypeParameterValue(notifierType,  EXPIRY_RETRIES)));

        		// Scheduler schema sending
        		schedulerSendingSchema = new NotifierEventRetryInfo(this.notifierTypeName + TemplateType.SERVICE_NAME_SEPARATOR + NotifierTypeState.STATE_SENDING.getName());
        		schedulerSendingSchema.setEventRetrySchema(getNotifierTypeParameterValue(notifierType, SENDING_RETRY_SCHEMA));
        		int sendingExpiryTime = Integer.parseInt(getNotifierTypeParameterValue(notifierType, SENDING_EXPIRE_TIME_IN_MIN));
        		schedulerSendingSchema.setExpireTimeInMinute(sendingExpiryTime);
        		int sendingExpiryInterval =  Integer.parseInt(getNotifierTypeParameterValue(notifierType, EXPIRY_INTERVAL_IN_MIN));
        		schedulerSendingSchema.setExpireRetryTimerInMinute(sendingExpiryInterval);
        		int sendingExpiryTries = Integer.parseInt(getNotifierTypeParameterValue(notifierType, EXPIRY_RETRIES));
        		schedulerSendingSchema.setMaxExpireTries(sendingExpiryTries);
        		cancelEnabledForType = Boolean.parseBoolean(getNotifierTypeParameterValue(notifierType, CANCEL_SMS_ON_NTF_CANCEL));
        		if (this.defaultTemplateType == false) {
        			if (cancelEnabledForType == true) {
        				PlugInUsingCancel=true;
        			}
        		} else {
        			cancelEnabledForType = false; //forced false for default.
        		}

        		//we need to figure out an expire value for the template sending and create a filename based on name of the template notifier.
        	   	inProgressFilename=this.notifierTypeName + IN_PROGRESS_FILENAME_POSTFIX;
        	   	//Maximum expire is the expire of the sending of the notification + a small guard time.
            	inProgressFileValidityTime = sendingExpiryInterval+1; //validity in minutes.
            	inProgressLockFileName=this.notifierTypeName + IN_PROGRESS_LOCK_FILENAME_POSTFIX;
            	
            	
            	NotifierEventHandler.get().addnewSchedule(this); //add self to scheduler.
        		
        		isLoaded = true;
        		
        	} catch (Exception e) {
        		log.error("Unable to load notifierType: " + notifierTypeName + " configuration due to exception exception: ",e);
        		log.error("Shutting down NTF, please correct problem and restart.");
        		TemplateSmsPlugin.getnotifierServicesManager().shutdownNtf();
        	}
        } else {
            log.debug("NotifierType " + notifierTypeName + " not found in notification.conf");
        }
    }
    
    private void determineServiceType(Map<String, String> notifierType, String content) {
    	serviceType = getNotifierTypeParameterValue(notifierType,  SERVICE_TYPE);
    	log.debug("determineServiceType: determine ServiceType for *.cphr Template: " + content); 
    	if (serviceType.equals(USE_NTF_SERVICE_TYPE)) {
    		log.debug("determineServiceType: Service type will be determined by ReplaceNotifications.List as set to " + USE_NTF_SERVICE_TYPE  );
    		serviceType = ntfServiceLookup.getServiceType(content);
    		log.debug("determineServiceType: set to [" + serviceType + "]");
    	} else {
    		log.debug("determineServiceType: Service type set by plug-in configuration: [" + serviceType + "]"  );
    	}
		
	}

	private String getNotifierTypeParameterValue(Map<String, String> notifierType, String paramName) {
        String paramValue = notifierType.get(paramName);
        if (paramValue == null) {
            paramValue = NotifierConfig.getNotifierTypeParameterDefaultValue(paramName);
        }
        return paramValue;
    }


	public int getInProgressFileValidityTime() {
		return inProgressFileValidityTime;
	}

	public String getInProgressFilename() {
		return inProgressFilename;
	}

	public String getInProgressLockFileName() {
		return inProgressLockFileName;
	}

	public static Iterator<TemplateType> iteratorNonDefault() {
		return templateTypeTable.values().iterator();
		
	}

	public int getReplacePosition() {
		return replacePosition;
	}


}
