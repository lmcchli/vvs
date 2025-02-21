/**
 * Copyright (c) 2003, 2004, 2005 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.meragent;

import java.util.HashMap;

import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.common.mdr.MdrEvent;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.SystemTopologyHelper;
import com.mobeon.common.cmnaccess.TopologyException;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.util.Logger;


/**
 * MerAgent bridges the gap between NTF and the Radius API from MER. It knows
 * the API and some NTF stuff, and sends NTF events to MER.
 */
public class MerAgent implements Constants {
    
    private final static Logger log = Logger.getLogger(MerAgent.class); 

    
    
//    public static final int NTF_SMS = 0;
//    public static final int NTF_MWI = 1;
//    public static final int NTF_MMS = 2;
//    public static final int NTF_WAP = 3;
//    public static final int NTF_MWIOff = 4;
//    public static final int NTF_ODL = 5;
//    public static final int NTF_PAG = 6;
//    public static final int NTF_TUI = 7;
//    public static final int NTF_WMW = 8;
//    public static final int NTF_FLS = 9;
//    public static final int NTF_EML = 10;
//    public static final int NTF_SLAM = 11;
//    public static final int NTF_MCNNOTIF = 12;
//    public static final int NTF_VVM = 13;
//    public static final int NTF_FAX_PRINT_NOTIF = 14;
//    public static final int NTF_FAX_RECEIPT_SMS = 15;
//    public static final int NTF_FAX_RECEIPT_TUI = 16;
//    public static final int NTF_FLSSMS = 17; //special type for fall-back, means send both depending on filter
//    public static final int NTF_CMW = 18; //moved due to numbering inconsistency, CMW not currently used anyway.
//    public static final int NTF_DELAYED_NOTIFY = 19;
//    public static final int NTF_SIPMWI = 20;
//    //NTF_NO_NOTIF_TYPE must have the highest number of the notification type constants
//    public static final int NTF_NO_NOTIF_TYPE = 21;
    
    //Conversion table from NTF notification types to Radius-MA SAS port type
    
/*    
 * Conversion table from internal number to SAS port see CPI
 * http://cpistore.internal.abcxyz.com/alexserv?ac=LINKEXT&li=EN/LZN7650137/3R1C&FN=2_1540-HDB10104Uen.J.html&SL=EN/LZN7650137/3R1C
    Value       Name (port type)    Description

    1       SMS         The SMS interface was used.

    3       OUTDIAL     The OUTDIAL interface was used.

    4       MMS         The MMS interface was used.

    5       MWI         The MWI interface was used.

    7       TUI         The Telephony User Interface (TUI) was used.

    10      VUI         The Video User Interface (VUI) was used.

    14      E-mail      The E-mail interface was used.

    15      FAXPrint    A Fax print request was made.

    17      IMAP        An IMAP request was made.
    
*/
    
    private static final int NOTIFTYPE2MERPORT[] = {1,  //SMS                   //0
                                                    5,  //MWI                   //1
                                                    4,  //MMS                   //2 
                                                    2,  //WAP                   //3
                                                    5,  //MWI off               //4
                                                    3,  //Outdial               //5
                                                    8,  //Pager                 //6
                                                    7,  //TUI                   //7
                                                    12, //WMW                   //8
                                                    11, // FLS                  //9
                                                    14, // EML                  //10
                                                    1,  // Slamdown             //11
                                                    1,  // mcn                  //12
                                                    1,  // vvm                  //13
                                                    15, // FAX Print            //14
                                                    1,  // FAX Receipt SMS      //15
                                                    7,  // FAX Receipt TUI      //16
                                                    1,  // FLASH_SMS (not used directly)  //17
                                                    1,  // CMW (not used)       //18
                                                    1,  // DELAYED NOTIFICATION //19
                                                    5   // SIP MWI              //20
    };

    private static final int FAX_PRINT_PORT = 15;
    private static final int SEND = 7; //Radius-MA event type
    private static final int MODIFY = 5; //Radius-MA event type
    private static final int NOTIFIER = 3; //Radius-MA Object type
    private static final int USER = 2; //Radius-MA Object type
    private static final int GENERAL_ERROR = 6; //Radius-MA terminate cause
    private static final int NOTIFICATION_DISCARDED = 7; //Radius-MA terminate cause
    private static final int TIMEOUT = 5; //Radius-MA terminate cause
    private static final int SLAMDOWN = 1; //Radius-MA event reason, slamdown info
    private static final int REMINDER = 2; // Event reason, system reminder
    private static final int OUTDIAL_REPLACEMENT = 3; // Event reason, outdial replace
    private static final int PHONEON = 5; // event reason, phone on
    private static final int MISSED_CALLS = 6; // event reason, missed calls information
    private static final int FAXPRINTDELIVERYRECEIPT = 4; //event reason faxprint failed
    private static final int REMINDERSMS = 13; // event reason unreadmessagereminder
    private static final int UPDATESMS = 14; // event reason update sms
    private static final int CANCELSMS = 15; // event reason cancel sms
    private static final int VVM = 16; // event reason Visual Voice Mail (VVM)
    private static final int MSG_AUTO_FWD_BY_EMAIL = 23; // event reason auto_fwd_msg_by_email
    private static final int EXTERNAL_TRANSCODING = 42; // event type Conversion via FFMPEG
    private static final int FAX_AUTOPRINT_REASON = 17; //Event reason for fax auto print
    private static final int FAX_PRINT_REASON= 18;//Event reason for fax auto print
    private static final int CUSTOMIZED_NOTIFICATION = 25; //Event reason for generic customized notification event
    private static final int UNLOCKED = 10; //Event reason for auto unlock pin

    //Media conversion (from MCC)
    private static final int EVENT_TYPE_CONVERT = 9;
    private static final int OBJECT_TYPE_MESSAGE = 1;
    private static final Integer MESSAGE_TYPE_VOICE = new Integer(10);
    private static final Integer MESSAGE_TYPE_VIDEO = new Integer(20);
    private static final Integer MESSAGE_ENCODING_WAV = new Integer(10);
    private static final Integer MESSAGE_ENCODING_AMR = new Integer(20);
    private static final Integer MESSAGE_ENCODING_MOV = new Integer(40);
    private static final Integer MESSAGE_ENCODING_3GP = new Integer(50);


    static private HashMap<String, Integer> messageTypeMap = new HashMap<String, Integer>();
    static {
        messageTypeMap.put("audio", MESSAGE_TYPE_VOICE);
        messageTypeMap.put("video", MESSAGE_TYPE_VIDEO);
    };

    static private HashMap<String, Integer> messageEncodingMap = new HashMap<String, Integer>();
    static {
        messageEncodingMap.put("audio/wav", MESSAGE_ENCODING_WAV);
        messageEncodingMap.put("audio/3gpp", MESSAGE_ENCODING_AMR);
        messageEncodingMap.put("video/quicktime", MESSAGE_ENCODING_MOV);
        messageEncodingMap.put("video/3gpp", MESSAGE_ENCODING_3GP);
    };

    private static final MerAgent INST = new MerAgent();    

    private String componentName = "Unknown";

    /**
     * Constructor.
     */
    protected MerAgent() {
        log.logMessage("MerAgent created", Logger.L_VERBOSE);
    }

    /**
     * Returns the single MerAgent instance.
     *@return the MerAgent.
     */
    public static MerAgent get() {
        return INST;
    }

    public static MerAgent get(String aComponentName) {
    	INST.setComponentName(aComponentName);
        return INST;
    }

    protected void setComponentName(String aComponentName){
    	componentName = aComponentName;
    }


    /**
     * Logs a message from the EMR API. The log level is controlled by the
     * message content.
     *@param msg - the message to log.
     */
    public void setMessage(String msg) {
        if (msg.startsWith("Error")) {
            log.logMessage("MER " + msg, Logger.L_ERROR);
        } else {
            log.logMessage("MER " + msg, Logger.L_DEBUG);
        }
    }

    /**
     * Makes a basic RADIUS event for NTF.
     *@param mailAddress - the mailbox the event concerns.
     *@return a new MdrEvent with some NTF attributes set.
     */
    private MdrEvent makeNtfMdrEvent(String mailAddress) {
    	MdrEvent ev = new MdrEvent();
        ev.setObjectType(NOTIFIER);

        ev.setNasIdentifier(CommonOamManager.getInstance().getLocalInstanceNameFromTopology(MoipMessageEntities.MESSAGE_SERVICE_NTF));
        ev.setEventType(SEND);
        ev.setUserName(mailAddress);
        return ev;
    }

    /**
     * Creates a RADIUS event without event reason.
     *@param mailAddress - the the mailbox the event concerns.
     *@param notifType - the type of notification the event is about.
     *@return a new MdrEvent.
     */
    private MdrEvent makeEvent(String mailAddress, int notifType) {
        return makeEvent(mailAddress, notifType, 0);
    }

    /**
     * Creates a RADIUS event without event reason.
     *@param mailAddress - the the mailbox the event concerns.
     *@param notifType - the type of notification the event is about.
     *@param reason - the event reason. 0 means no event reason.
     *@return a new MdrEvent.
     */
    private MdrEvent makeEvent(String mailAddress, int notifType, int reason) {
    	MdrEvent ev = makeNtfMdrEvent(mailAddress);

        if (notifType >= 0 && notifType < NTF_NO_NOTIF_TYPE) {
            if ( notifType > NOTIFTYPE2MERPORT.length) {
                // This array can go out of bounds if new notification types are added.
                // So added for safety and debug purposes..
                log.logMessage("Out of bounds in NOTIFTYPE2MERPORT table,  MAE-SAS-Port-Type not set" , Logger.L_ERROR);
                
            } else {
                ev.setSASPortType(NOTIFTYPE2MERPORT[notifType]);
            }  
        }
        
        if (reason > 0) {
            ev.setEventReason(reason);
        }
        String opco = "unknown";
       	try {
       		opco = SystemTopologyHelper.getOpcoName();
       	}
       	catch (TopologyException e){
       		log.logMessage("MerAgent>>Exception received while getting opco name from topology: " + e.getMessage(), Logger.L_VERBOSE);
       	}
        ev.setOpcoId(opco);
		return ev;
    }

    /**
     * Sends an event about a successful Msg Auto Forwarding by Email.
     *@param mail - mailbox the Msg Auto Forwarding came from.
     */
    public void msgAutoFwdByEmail(String mail) {
    	makeEvent(mail, Constants.NTF_EML, MSG_AUTO_FWD_BY_EMAIL).write();
    }
    /**
     * Sends information about a successful faxprint receipt notification delivery to
     * MER.
     *@param mail the receiver of the notification.
     *@param type the type of delivery interface.
     */
    public void faxprintRecieptNotificationDelivered(String mail,int type) {
        makeEvent(mail, type, FAXPRINTDELIVERYRECEIPT).write();
    }
    /**
     * Sends information about a successful faxprint receipt notification delivery to
     * MER.
     *@param mail the receiver of the notification.
     *@param msg the message to put in the event description
     *@param type the type of delivery interface.
     */

    public void faxprintRecieptNotificationFailed(String mail,String msg,int type) {
        MdrEvent ev = makeEvent(mail, type, FAXPRINTDELIVERYRECEIPT);


        ev.setTerminateCause(GENERAL_ERROR);
        if (msg != null) {
            ev.setEventDescription(msg);
        }

        ev.write();



    }
    /**
     * Sends information about a successful faxprint receipt notification delivery to
     * MER.
     *@param mail the receiver of the notification.
     *@param type the type of delivery interface.
     */
    public void faxprintRecieptNotificationExpired(String mail,int type) {
        MdrEvent ev = makeEvent(mail, type, FAXPRINTDELIVERYRECEIPT);
        ev.setTerminateCause(TIMEOUT);
        ev.write();
    }

    /**
     * Sends an event about a successful fax print.
     *@param mail - mailbox the autoprinted fax came from.
     */
    public void faxPrintDelivered(String mail, boolean isAutoPrint) {
    	MdrEvent ev = makeNtfMdrEvent(mail);
        ev.setSASPortType(FAX_PRINT_PORT);
    	if(isAutoPrint)
    	{
    	    ev.setEventReason(FAX_AUTOPRINT_REASON);
    	}
    	else
    	{
    	    ev.setEventReason(FAX_PRINT_REASON);
    	}

        String opco = "unknown";
        try {
            opco = SystemTopologyHelper.getOpcoName();
        }
        catch (TopologyException e){
            log.logMessage("MerAgent>>Exception received while getting opco name from topology: " + e.getMessage(), Logger.L_VERBOSE);
        }
        ev.setOpcoId(opco);
        ev.write();
    }
    /**
     * Sends an event about a failed fax print.
     *@param mail - mailbox the autoprinted fax came from.
     */
    public void faxPrintFailed(String mail, boolean isAutoPrint) {
        MdrEvent ev = makeNtfMdrEvent(mail);
        ev.setSASPortType(FAX_PRINT_PORT);
        if(isAutoPrint)
        {
            ev.setEventReason(FAX_AUTOPRINT_REASON);
        }
        else
        {
            ev.setEventReason(FAX_PRINT_REASON);
        }
        ev.setTerminateCause(GENERAL_ERROR);
        String opco = "unknown";
        try {
            opco = SystemTopologyHelper.getOpcoName();
        }
        catch (TopologyException e){
            log.logMessage("MerAgent>>Exception received while getting opco name from topology: " + e.getMessage(), Logger.L_VERBOSE);
        }
        ev.setOpcoId(opco);
        ev.write();
    }
    /**
     * Sends an event about a successful fax print.
     *@param mail - mailbox the autoprinted fax came from.
     */
    /**
     * Sends an event about a failed fax print.
     *@param mail - mailbox the autoprinted fax came from.
     */
    public void faxPrintTimeout(String mail, boolean isAutoPrint) {
        MdrEvent ev = makeNtfMdrEvent(mail);
        ev.setSASPortType(FAX_PRINT_PORT);
        if(isAutoPrint)
        {
            ev.setEventReason(FAX_AUTOPRINT_REASON);
        }
        else
        {
            ev.setEventReason(FAX_PRINT_REASON);
        }
        ev.setTerminateCause(TIMEOUT);
        String opco = "unknown";
        try {
            opco = SystemTopologyHelper.getOpcoName();
        }
        catch (TopologyException e){
            log.logMessage("MerAgent>>Exception received while getting opco name from topology: " + e.getMessage(), Logger.L_VERBOSE);
        }
        ev.setOpcoId(opco);
        ev.write();
    }




    /**
     * Sends information about a successful notification delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     */
    public void notificationDelivered(String mail, int notifType) {
        makeEvent(mail, notifType).write();
    }

    /**
     *Sends information about a successful notification delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     *@param msg message used in event description.
     */
    public void notificationDelivered(String mail, int notifType, String msg) {
    	MdrEvent ev = makeEvent(mail, notifType);

       if( msg != null ) {
           ev.setEventDescription(msg);
       }

       ev.write();
    }

    /** Sends information about a failed notification delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     *@param msg message describing the reason for failure, or null.
     */
    public void notificationFailed(String mail, int notifType, String msg) {
        MdrEvent ev = makeEvent(mail, notifType);

        ev.setTerminateCause(GENERAL_ERROR);
        if (msg != null) {
            ev.setEventDescription(msg);
        }

        ev.write();
    }

    /** Sends information about an expired notification delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     */
    public void notificationExpired(String mail, int notifType) {
        MdrEvent ev = makeEvent(mail, notifType);

        ev.setTerminateCause(TIMEOUT);
        ev.write();

    }

    /**
     * Sends information that a replacement SMS has been sent.
     * @param mail Users mail address.
     */
    public void replacementSMSSent(String mail) {
        makeEvent(mail, NTF_SMS, OUTDIAL_REPLACEMENT).write();
    }

    /**
     * Sends information about a sent reminderSMS
     * @param mail the mailaddress of the user
     */
    public void reminderSmsSent(String mail) {
        makeEvent(mail, NTF_SMS, REMINDERSMS).write();
    }

    /**
     * Sends information about a sent update SMS
     * @param mail the mailaddress of the user
     */
    public void updateSmsSent(String mail) {
        makeEvent(mail, NTF_SMS, UPDATESMS).write();
    }

    /**
     * Sends information about a sent cancel SMS
     * @param mail the mailaddress of the user
     */
    public void cancelSmsSent(String mail) {
        makeEvent(mail, NTF_SMS, CANCELSMS).write();
    }

    /**
     * Sends information about successful missed call notification (MCN)
     * @param mail Users mail address
     * @param notifType Type of notification for the reminder.
     */
    public void mcnDelivered(String mail)
    {
        makeEvent(mail, NTF_SMS, MISSED_CALLS).write();
    }

    /**
     * Sends information about failed missed call notification (MCN)
     * @param mail the receiver of the notification.
     * @param notifType the type of delivery interface.
     * @param msg message describing the reason for failure, or null.
     */
    public void mcnFailed(String mail, String msg) {
        MdrEvent ev= makeEvent(mail, NTF_SMS, MISSED_CALLS);
        ev.setTerminateCause(GENERAL_ERROR);
        if (msg != null) {
            ev.setEventDescription(msg);
        }
        ev.write();
    }

    /**
     * Sends information about expired missed call notification (MCN)
     * @param mail the receiver of the notification.
     * @param notifType the type of delivery interface.
     */
    public void mcnExpired(String mail) {
        MdrEvent ev= makeEvent(mail, NTF_SMS, MISSED_CALLS);
        ev.setTerminateCause(TIMEOUT);
        ev.write();
    }

    /**
     * Sends information about successful system reminder.
     * The reminders are often for SMS but we take the type
     * for future extendability.
     * @param mail Users mail address
     * @param notifType Type of notification for the reminder.
     */
    public void systemReminderDelivered(String mail, int notifType) {
        makeEvent(mail, notifType, REMINDER).write();
    }


    /**
     * Sends information about successful phone on requests.
     * @param subscriberNumber Users mail address.
     */
    public void phoneOnDelivered(String subscriberNumber) {
        makeEvent(subscriberNumber, NTF_SMS, PHONEON).write();
    }

    /**
     * Sends information about failed phone on requests.
     * @param mail Users mail address.
     */
    public void phoneOnFailed(String mail) {
        MdrEvent mdrEvent = makeEvent(mail, NTF_SMS, PHONEON);
        mdrEvent.setTerminateCause(GENERAL_ERROR);
        mdrEvent.write();
    }

    /**
     * Sends information about timeout phone on requests.
     * @param mail Users mail address.
     */
    public void phoneOnTimeout(String mail) {
        MdrEvent mdrEvent = makeEvent(mail, NTF_SMS, PHONEON);
        mdrEvent.setTerminateCause(TIMEOUT);
        mdrEvent.write();
    }

    /**
     * Sends information about a successful slamdown information delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     */
    public void slamdownInfoDelivered(String mail) {
        makeEvent(mail, NTF_SMS, SLAMDOWN).write();
    }

    /**
     * Sends information about a successful slamdown information delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     */
    public void slamdownInfoDelivered(String mail, int notifType) {
        makeEvent(mail, notifType, SLAMDOWN).write();
    }

    /**
     * Sends information about a failed slamdown information delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     *@param msg message describing the reason for failure, or null.
     */
    public void slamdownInfoFailed(String mail, int notifType, String msg) {
        MdrEvent mdrEvent = makeEvent(mail, notifType, SLAMDOWN);
        mdrEvent.setTerminateCause(GENERAL_ERROR);
        if (msg != null) {
            mdrEvent.setEventDescription(msg);
        }
        mdrEvent.write();
    }

    /**
     * Sends information about a failed slamdown information delivery to MER.
     * @param mail the receiver of the notification.
     *@param msg message describing the reason for failure, or null.
     */
    public void slamdownInfoFailed(String mail, String msg) {
        slamdownInfoFailed(mail, NTF_SMS, msg);
    }

    /**
     * Sends information about an expired slamdown information delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     */
    public void slamdownInfoExpired(String mail, int notifType) {
        MdrEvent mdrEvent = makeEvent(mail, notifType, SLAMDOWN);
        mdrEvent.setTerminateCause(TIMEOUT);
        mdrEvent.write();
    }

    /**
     * Sends information about an expired slamdown information delivery to MER.
     *@param mail the receiver of the notification.
     *@param notifType the type of delivery interface.
     */
    public void slamdownInfoExpired(String mail) {
        slamdownInfoExpired(mail, NTF_SMS);
    }

    /**
     * Sends information about successful Visual Voice Mail (VVM) notification.
     * @param subscriberNumber
     * @param eventDescription
     */
    public void vvmDelivered(String subscriberNumber, String eventDescription) {
        MdrEvent mdrEvent = makeEvent(subscriberNumber, NTF_SMS, VVM);
        if (eventDescription != null) {
            mdrEvent.setEventDescription(eventDescription);
        }
        mdrEvent.write();
    }

    /**
     * Sends information about failed Visual Voice Mail (VVM) notification.
     * @param subscriberNumber
     * @param eventDescription
     */
    public void vvmFailed(String subscriberNumber, String eventDescription) {
        MdrEvent mdrEvent = makeEvent(subscriberNumber, NTF_SMS, VVM);
        mdrEvent.setTerminateCause(GENERAL_ERROR);
        if (eventDescription != null) {
            mdrEvent.setEventDescription(eventDescription);
        }
        mdrEvent.write();
    }

    /**
     * Sends information about timeout Visual Voice Mail (VVM) notification.
     * @param subscriberNumber
     * @param eventDescription
     */
    public void vvmTimeout(String subscriberNumber, String eventDescription) {
        MdrEvent mdrEvent = makeEvent(subscriberNumber, NTF_SMS, VVM);
        mdrEvent.setTerminateCause(TIMEOUT);
        if (eventDescription != null) {
            mdrEvent.setEventDescription(eventDescription);
        }
        mdrEvent.write();
    }
    
    /**
     * Sends information about successful Auto Unlock Pin (AUP) Unlock operation.
     * @param subscriberNumber the subscriber of the mailbox being unlocked
     */
    public void aupUnlockDelivered(String subscriberNumber) {
        MdrEvent mdrEvent = makeAupUnlockMdrEvent(subscriberNumber);
        mdrEvent.write();
    }

    /**
     * Sends information about failed Auto Unlock Pin (AUP) Unlock operation.
     * @param subscriberNumber the subscriber of the mailbox being unlocked
     */
    public void aupUnlockFailed(String subscriberNumber) { 
        MdrEvent mdrEvent = makeAupUnlockMdrEvent(subscriberNumber);
        mdrEvent.setTerminateCause(GENERAL_ERROR);
        mdrEvent.write();
    }

    /**
     * Sends information about expired Auto Unlock Pin (AUP) Unlock operation.
     * @param subscriberNumber the subscriber of the mailbox being unlocked
     */
    public void aupUnlockExpired(String subscriberNumber) {
        MdrEvent mdrEvent = makeAupUnlockMdrEvent(subscriberNumber);
        mdrEvent.setTerminateCause(TIMEOUT);
        mdrEvent.write();
    }
    
    /**
     * Sends information about successful Auto Unlock Pin (AUP) SMS notification.
     * @param subscriberNumber the receiver of the notification.
     */
    public void aupSmsDelivered(String subscriberNumber) {
        MdrEvent mdrEvent = makeEvent(subscriberNumber, Constants.NTF_SMS, UNLOCKED);
        mdrEvent.write();
    }

    /**
     * Sends information about failed Auto Unlock Pin (AUP) SMS notification.
     * @param subscriberNumber the receiver of the notification.
     * @param msg message describing the reason for failure, or null.
     */
    public void aupSmsFailed(String subscriberNumber, String msg) { 
        MdrEvent mdrEvent = makeEvent(subscriberNumber, Constants.NTF_SMS, UNLOCKED);
        mdrEvent.setTerminateCause(GENERAL_ERROR);
        if (msg != null) {
            mdrEvent.setEventDescription(msg);
        }
        mdrEvent.write();
    }

    /**
     * Sends information about expired Auto Unlock Pin (AUP) SMS notification.
     * @param subscriberNumber the receiver of the notification.
     */
    public void aupSmsExpired(String subscriberNumber) {
        MdrEvent mdrEvent = makeEvent(subscriberNumber, Constants.NTF_SMS, UNLOCKED);
        mdrEvent.setTerminateCause(TIMEOUT);
        mdrEvent.write();
    }
    
    /**
     * Makes a basic RADIUS event for AutoUnlockPin CAI3G Unlock.
     *@param mailAddress - the mailbox the event concerns.
     *@return a new MdrEvent with some NTF attributes set.
     */
    private MdrEvent makeAupUnlockMdrEvent(String mailAddress) {
        MdrEvent ev = new MdrEvent();
        ev.setObjectType(USER);

        ev.setNasIdentifier(CommonOamManager.getInstance().getLocalInstanceNameFromTopology(MoipMessageEntities.MESSAGE_SERVICE_NTF));
        ev.setEventType(MODIFY);
        ev.setUserName(mailAddress);
        ev.setEventReason(UNLOCKED);
        
        String opco = "unknown";
        try {
            opco = SystemTopologyHelper.getOpcoName();
        }
        catch (TopologyException e){
            log.logMessage("MerAgent>>Exception received while getting opco name from topology: " + e.getMessage(), Logger.L_VERBOSE);
        }
        ev.setOpcoId(opco);
        return ev;
    }
    
    /**
     *Sends information about a successful conversion MDR event.
     *@param mailboxId the mailbox Id.
     *@param fromFormat the input format.
     *@param toFormat the output format.
     *@param size the size of the input file, in an undefined unit.
     *@param conversionTime the time it took to convert, in milliseconds.
     */
    public void mediaConversionCompleted(String mailboxId, String fromFormat,
            String toFormat,
            int size,
            int conversionTime) {
        makeMediaConversionEvent(mailboxId, fromFormat, toFormat, size, conversionTime, EVENT_TYPE_CONVERT);
    }

    /**
     *Sends information about a successful conversion MDR event.
     *@param mailboxId the mailbox Id.
     *@param fromFormat the input format.
     *@param toFormat the output format.
     *@param size the size of the input file, in an undefined unit.
     *@param conversionTime the time it took to convert, in milliseconds.
     */
    public void trascodingCompleted(String mailboxId, String fromFormat,
            String toFormat,
            int size,
            long conversionTime) {
        makeMediaConversionEvent(mailboxId, fromFormat, toFormat, size, conversionTime, EXTERNAL_TRANSCODING);
    }

    private void makeMediaConversionEvent(String userName, String fromFormat, String toFormat,
                int size, long conversionTime, int transcodingType) {
        MdrEvent ev = new MdrEvent();
        ev.setNasIdentifier(CommonOamManager.getInstance().getLocalInstanceNameFromTopology(MoipMessageEntities.MESSAGE_SERVICE_NTF));
        ev.setEventType(transcodingType);
        ev.setObjectType(OBJECT_TYPE_MESSAGE);
        ev.setMessageSize(size);
        ev.setUserName(userName);

        // Set the OPCO Name
        String opco = "unknown";
        try {
            opco = SystemTopologyHelper.getOpcoName();
        }
        catch (TopologyException e){
            log.logMessage("MerAgent>>Exception received while getting opco name from topology: " + e.getMessage(), Logger.L_VERBOSE);
        }
        ev.setOpcoId(opco);

        String type = fromFormat.substring(0, fromFormat.indexOf("/"));

        Integer tmp = (Integer) messageTypeMap.get(type);
        if (null != tmp) {
            ev.setMessageType(tmp.intValue());
        }
        tmp = (Integer) messageEncodingMap.get(fromFormat);
        if (null != tmp) {
            ev.setMessageEncoding(tmp.intValue());
        }
        ev.setEventDescription("" + fromFormat + " to " + toFormat
                + ", " + conversionTime + "ms");

        ev.write();
    }
    
    private MdrEvent makeCustomizedNotificationMdrEvent(String mail, int portType, String reasonDetail) {
        MdrEvent mdrEvent = makeEvent(mail, NTF_NO_NOTIF_TYPE, CUSTOMIZED_NOTIFICATION);
        mdrEvent.setSASPortType(portType);
        mdrEvent.setEventReasonDetail(reasonDetail);        
        return mdrEvent;
    }
    
    
    /**
     * Sends information about a successful notification information delivery to MER.
     *@param mail the receiver of the notification.
     *@param portType the type of delivery interface.
     */
    public void generateMdrDelivered(String mail, int portType, String reasonDetail) {
        makeCustomizedNotificationMdrEvent(mail, portType, reasonDetail).write();
    }

    /**
     * Sends information about an expired notification information delivery to MER.
     *@param mail the receiver of the notification.
     *@param portType the type of delivery interface.
     */
    public void generateMdrExpired(String mail, int portType, String reasonDetail) {
        MdrEvent mdrEvent = makeCustomizedNotificationMdrEvent(mail, portType, reasonDetail);  
        mdrEvent.setTerminateCause(TIMEOUT);
        
        mdrEvent.write();
    }
    
    /**
     * Sends information about a failed notification information delivery to MER.
     *@param mail the receiver of the notification.
     *@param portType the type of delivery interface.
     *@param msg message describing the reason for failure, or null.
     */
    public void generateMdrFailed(String mail, int portType, String reasonDetail, String msg) {
        MdrEvent mdrEvent = makeCustomizedNotificationMdrEvent(mail, portType, reasonDetail);;
        mdrEvent.setTerminateCause(GENERAL_ERROR);
        
        if (msg != null) {
            mdrEvent.setEventDescription(msg);
        }
        mdrEvent.write();
    }
    
    /**
     * Sends information about a replaced notification information delivery to MER.
     *@param mail the receiver of the notification.
     *@param portType the type of delivery interface.
     *@param msg message describing the reason for failure, or null.
     */
    public void generateMdrDiscarded(String mail, int portType, String reasonDetail, String msg) {
        MdrEvent mdrEvent = makeCustomizedNotificationMdrEvent(mail, portType, reasonDetail); 
        mdrEvent.setTerminateCause(NOTIFICATION_DISCARDED);
        
        if (msg != null) {
            mdrEvent.setEventDescription(msg);
        }
        mdrEvent.write();
    }


    /**
     * Sends information about successful phone on requests.
     *@param mail the receiver of the notification.
     */
    public void generateMdrPhoneOnDelivered(String mail) {
        phoneOnDelivered(mail);
    }

}
