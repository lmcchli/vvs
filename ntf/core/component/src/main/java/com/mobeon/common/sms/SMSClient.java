/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.common.sms;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.Logger;
import com.mobeon.common.smscom.ConnectionStateListener;
import com.mobeon.common.sms.SMSUnit.ShutdownType;
import com.mobeon.common.sms.request.FormattedSMSRequest;
import com.mobeon.common.sms.request.Request;
import com.mobeon.common.sms.request.MultiRequest;
import com.mobeon.common.sms.request.SMSRequest;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.UserInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SMSClient is the starting point to send SMS-messages to. SMSClient is responsible to select
 * a suitable SMSUnit(smsc) and transfer the request to the unit.
 */
public class SMSClient {
    public static final int SEND_OK = 0;
    /* means a totally unrecovaable failure in MIO VVS terminology.
     * so be carefull when this is sent or will cause a failed message.
     *
     * In MoIP this meant that the failure was not immediately recoverable.
     * Mio designers did not understand this and assumed it meant as above.
     * So, I have changed it's meaning.
     *
     * In Moip it went back on the mail queue and was retried at a smaller
     * internal.
    */
    public static final int SEND_FAILED = 1;
    public static final int SEND_FAILED_TEMPORARY = 2;

    public static final int TYPE_SMS = 0;
    public static final int TYPE_MWI = 1;
    public static final int TYPE_SMSMWI = 2;
    public static final int TYPE_FLASH = 3;
    public static final int TYPE_PHONEON = 4;
    public static final int TYPE_FORMATTED = 5;
    public static final int TYPE_VVM_DEP = 6;
    public static final int TYPE_VVM_GRE = 7;
    public static final int TYPE_VVM_EXP = 8;
    public static final int TYPE_VVM_LOG = 9;
    public static final int TYPE_APPLEVVM_DEP = 10;
    public static final int TYPE_APPLEVVM_GRE = 11;
    public static final int TYPE_APPLEVVM_EXP = 12;
    public static final int TYPE_APPLEVVM_LOG = 13;

    public static final String CONFIGURED_SMSC_AVAILABILITY_AVAILABLE = "available";
    public static final String CONFIGURED_SMSC_AVAILABILITY_UNAVAILABLE = "unavailable";
    public static final String CONFIGURED_SMSC_AVAILABILITY_FORCEDUNAVAILABLE = "forcedUnavailable";

    /** A list of all available SMSUnits */
    private volatile Map<String, SMSUnit> allSmsUnits = new ConcurrentHashMap<String, SMSUnit>();
    private volatile Map<String, Integer> mwiCounts;
    private int autoIndex = 0;
    private int autoStringIndex = 0;

    private ConnectionStateListener connectionStateListener;

    private Logger logger;
    private static SMSConfig config;

    private int nextId = 0;

    private static SMSClient inst = null;

    /**
     * Retrieve the SMSClient singleton instance.
     * If not yet instantiated, this method will instantiate the SMSClient instance.
     * @param logger Logger
     * @param config SMSConfig
     * @return SMSClient instance
     */
    public static SMSClient get(Logger logger, SMSConfig config) {
        if (inst == null) {
            inst = new SMSClient(logger, config);
        }
        return inst;
    }

    /**
     * Retrieve the existing SMSClient singleton instance which could be null if not yet instantiated.
     * @return SMSClient instance
     */
    public static SMSClient get() {
        return inst;
    }
    
    protected SMSClient(Logger logger, SMSConfig config) {
        this.logger = logger;
        SMSClient.config = config;
        mwiCounts = new HashMap<String, Integer>();
    }

    public void setConnectionStateListener(ConnectionStateListener listener) {
        this.connectionStateListener = listener;
    }

    public synchronized int getNextId() {
        return nextId++;
    }

    public static Request makeRequest(SMSAddress from, SMSAddress to, UserInfo user, UserMailbox inbox, int validity, SMSResultHandler rh, int id, int type, String text, byte[] payload, int replacePosition, int value, int delay ) {
        return SMSUnit.makeRequest(from, to, user, inbox, validity, rh, id, type, text, payload, replacePosition, value, delay);
    }

    /**
     * Connect to all configured SMS units at startup.
     */
    public void connectSmsUnits() {
        refreshSmsUnits();
    }

    /**
     * Disconnect from all SMS units at shutdown.
     */
    public void disconnectSmsUnits() {
        Iterator<String> allUnitsIter = allSmsUnits.keySet().iterator();
        while(allUnitsIter.hasNext()) {
            String instanceName = allUnitsIter.next();
            SMSUnit smsUnit = allSmsUnits.remove(instanceName);
            if(smsUnit != null) {
                logger.logString("SMSClient.disconnectSmsUnits: Disconnecting sms unit: " + instanceName, Logger.LOG_DEBUG);
                smsUnit.shutdownViaClient(1, ShutdownType.UNGRACEFUL);
                mwiCounts.remove(instanceName);
            }
        }
    }

    /**
     * If ShortMessage.Table availability is used, it takes precedence over the AllowedSmsc.List<p>
     * So, we would have the following cases and outcomes:
     * <table border="2">
     * <tr> <th>AllowedSmsc.List </th> <th>ShortMessage.Table availability</th> <th>Configuration used</th> </tr>
     * <tr> <td>empty</td> <td>not used</td> <td>ShortMessage.Table (no availability defaults to "available")</td> </tr>
     * <tr> <td>empty</td> <td>used</td> <td>ShortMessage.Table</td> </tr>
     * <tr> <td>not empty</td> <td>not used</td> <td>AllowedSmsc.List</td> </tr>
     * <tr> <td>not empty</td> <td>used</td> <td>ShortMessage.Table</td> </tr>
     * </table><p>
     * 
     * @return true if the ShortMessage.Table should be used; false if the AllowedSmsc.List should be used
     */
    private boolean useConfigShortMessageTable() {
        boolean useShortMsgTable = false;

        if(config.getAllowedSmsc().length == 0) {
            useShortMsgTable = true;
        } else {
            Map<String, Map<String, String>> shortMessageInstances = Config.getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
            boolean isAvailabilityUsed = false;
            Collection<Map<String, String>> allInstancesInfo = shortMessageInstances.values();
            Iterator<Map<String, String>> instancesInfoIter = allInstancesInfo.iterator();
            while(instancesInfoIter.hasNext()) {
                Map<String, String> instanceInfo = instancesInfoIter.next();
                if(instanceInfo.get(NotificationConfigConstants.AVAILABILITY) != null) {
                    isAvailabilityUsed = true;
                    break;
                }
            }
            if(isAvailabilityUsed) {
                useShortMsgTable = true;
            }
        }
        return useShortMsgTable;
    }
    
    public void refreshSmsUnits() {
        Map<String, Map<String, String>> shortMessageInstances = Config.getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);

        if (shortMessageInstances == null || shortMessageInstances.isEmpty()) {
            logger.logString("SMSClient.refreshSmsUnits: No SMSc found in config", Logger.LOG_DEBUG);
            Iterator<String> allUnitsIter = allSmsUnits.keySet().iterator();
            while(allUnitsIter.hasNext()) {
                String instanceName = allUnitsIter.next();
                SMSUnit smsUnit = allSmsUnits.remove(instanceName);
                if(smsUnit != null) {
                    logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Removing from available sms units since it is no longer in ShortMessage.Table config: " + instanceName, Logger.LOG_DEBUG);
                    //To preserve previous behaviour, no limit on grace period and process queued requests.
                    smsUnit.shutdownViaClient(0, ShutdownType.GRACEFUL);
                    mwiCounts.remove(instanceName);
                }
            }

        } else {
            if(useConfigShortMessageTable()) {
                refreshSmsUnitsUsingShortMessageTable();
            } else {
                refreshSmsUnitsUsingAllowedSmscList();
            }
            
            //Since all SMS units allowed/available are now in allUnits map, 
            //add back up SMS units that were missed before because they were not yet in allUnits map.
            //At the same time, configured back up SMS units are refreshed for those SMS units whose status did not change from available.
            Iterator<SMSUnit> allUnitsIter = allSmsUnits.values().iterator();
            while(allUnitsIter.hasNext()) {
                SMSUnit unit = allUnitsIter.next();
                if(unit.getBackupSMSUnit() == null) {
                    unit.addBackup();
                }
            }            
        }        
    }

    private void refreshSmsUnitsUsingShortMessageTable() {
        //When using ShortMessage.Table always look at the availability of each instance.
        //No availability value defaults to "available".
        Map<String, Map<String, String>> shortMessageInstances = Config.getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
        Iterator<String> instanceIter = shortMessageInstances.keySet().iterator();
        while(instanceIter.hasNext()) {
            String instanceName = instanceIter.next();
            Map<String, String> instanceInfo = shortMessageInstances.get(instanceName);
            
            String availability = instanceInfo.get(NotificationConfigConstants.AVAILABILITY);
            if(availability == null || availability.equalsIgnoreCase(CONFIGURED_SMSC_AVAILABILITY_AVAILABLE)) {
                if(!allSmsUnits.containsKey(instanceName)) {
                    try {
                        SMSUnit smsUnit = new SMSUnit(logger, config, instanceName, connectionStateListener);
                        allSmsUnits.put(instanceName, smsUnit);
                        logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Added to available sms units: " + instanceName, Logger.LOG_DEBUG);
                    } catch (SMSUnitException e) {
                        logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Unable to add to available sms units: " + instanceName + ".  Exception: " + e.getMessage(), Logger.LOG_ERROR);
                    }
                } else {
                    logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Already in available sms units: " + instanceName, Logger.LOG_DEBUG);
                }
                
            } else {
                SMSUnit smsUnit = allSmsUnits.remove(instanceName);
                if(smsUnit != null) {
                    logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Removed from available sms units: " + instanceName, Logger.LOG_DEBUG);
                    if(availability.equalsIgnoreCase(CONFIGURED_SMSC_AVAILABILITY_UNAVAILABLE)) {
                        logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Shutdown graceful " + instanceName, Logger.LOG_DEBUG);
                        smsUnit.shutdownViaClient(Config.getSmscShutdownPeriod(), ShutdownType.GRACEFUL);
                    } else if(availability.equalsIgnoreCase(CONFIGURED_SMSC_AVAILABILITY_FORCEDUNAVAILABLE)) {
                        logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Shutdown forced " + instanceName, Logger.LOG_DEBUG);
                        smsUnit.shutdownViaClient(Config.getSmscShutdownPeriod(), ShutdownType.UNGRACEFUL);
                    }
                    mwiCounts.remove(instanceName);
                } else {
                    logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Already removed from available sms units: " + instanceName, Logger.LOG_DEBUG);
                }
            }
        }
        
        //Remove all SMS units that are no longer in the ShortMessage.Table
        Set<String> shortMessageTableSmsUnits = shortMessageInstances.keySet();        
        Iterator<String> allUnitsIter = allSmsUnits.keySet().iterator();
        while(allUnitsIter.hasNext()) {
            String instanceName = allUnitsIter.next();
            if(!shortMessageTableSmsUnits.contains(instanceName)) {
                SMSUnit smsUnit = allSmsUnits.remove(instanceName);
                if(smsUnit != null) {
                    logger.logString("SMSClient.refreshSmsUnitsUsingShortMessageTable: Removing from available sms units since it is no longer in ShortMessage.Table config: " + instanceName, Logger.LOG_DEBUG);
                    //To preserve previous behaviour, no limit on grace period and process queued requests.
                    smsUnit.shutdownViaClient(0, ShutdownType.GRACEFUL);
                    mwiCounts.remove(instanceName);
                }
            }
        }
    }

    private void refreshSmsUnitsUsingAllowedSmscList() {
        String[] allowedSmsc = config.getAllowedSmsc();
        Map<String, Map<String, String>> shortMessageInstances = Config.getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
        
        //To ensure that we have SMS units available to send traffic, add allowed SMS units before removing those that are no longer allowed.
        //To facilitate removal of SMS units that are no longer allowed, put the allowed SMSC in this ArrayList.
        ArrayList<String> allowedSmscArrayList = new ArrayList<String>();
        
        //Add all allowed SMS units.
        for(int i=0; i < allowedSmsc.length; i++) {
            String instanceName = allowedSmsc[i];
            allowedSmscArrayList.add(instanceName);
            if(!allSmsUnits.containsKey(instanceName)) {
                if(shortMessageInstances.containsKey(instanceName)) {
                    try {
                        SMSUnit smsUnit = new SMSUnit(logger, config, instanceName, connectionStateListener);
                        allSmsUnits.put(instanceName, smsUnit);
                        logger.logString("SMSClient.refreshSmsUnitsUsingAllowedSmscList: Added to available sms units: " + instanceName, Logger.LOG_DEBUG);
                    } catch (SMSUnitException e) {
                        logger.logString("SMSClient.refreshSmsUnitsUsingAllowedSmscList: Unable to add to available sms units: " + instanceName + ".  Exception: " + e.getMessage(), Logger.LOG_ERROR);
                    }
                } else {
                    logger.logString("SMSClient.refreshSmsUnitsUsingAllowedSmscList: Smsc " + instanceName + " specified in the AllowedSmsc.List config not found in ShortMessage.Table config.", Logger.LOG_ERROR);
                }
            } else {
                logger.logString("SMSClient.refreshSmsUnitsUsingAllowedSmscList: Already in available sms units: " + instanceName, Logger.LOG_DEBUG);
            }
        }

        //Remove all SMS units that are no longer allowed.        
        Iterator<String> allUnitsIter = allSmsUnits.keySet().iterator();
        while(allUnitsIter.hasNext()) {
            String instanceName = allUnitsIter.next();
            if(!allowedSmscArrayList.contains(instanceName)) {
                SMSUnit smsUnit = allSmsUnits.remove(instanceName);
                if(smsUnit != null) {
                    logger.logString("SMSClient.refreshSmsUnitsUsingAllowedSmscList: Removing from available sms units since it is no longer in AllowedSmsc.List config: " + instanceName, Logger.LOG_DEBUG);
                    //To preserve previous behaviour, no limit on grace period and process queued requests. 
                    smsUnit.shutdownViaClient(0, ShutdownType.GRACEFUL);
                    mwiCounts.remove(instanceName);
                }
            }
        }
    }

    private String[] getAllowedConfiguredSmsUnits() {
        String[] allowedSmsc = null;
        
        Map<String, Map<String, String>> shortMessageInstances = Config.getExternalEnablers(NotificationConfigConstants.SHORT_MESSAGE_TABLE);
        if (shortMessageInstances == null || shortMessageInstances.isEmpty()) {
            logger.logString("SMSClient.getAllowedConfiguredSmsUnits: No SMSc found in config", Logger.LOG_DEBUG);
        } else {
            if(useConfigShortMessageTable()) {
                ArrayList<String> allowedSmsUnits = new ArrayList<String>();
                Iterator<String> instanceIter = shortMessageInstances.keySet().iterator();
                while(instanceIter.hasNext()) {
                    String instanceName = instanceIter.next();
                    Map<String, String> instanceInfo = shortMessageInstances.get(instanceName);
                    String availability = instanceInfo.get(NotificationConfigConstants.AVAILABILITY);
                    if(availability == null || availability.equalsIgnoreCase(CONFIGURED_SMSC_AVAILABILITY_AVAILABLE)) {
                        allowedSmsUnits.add(instanceName);
                    } 
                }
                allowedSmsc = new String[allowedSmsUnits.size()] ;
                allowedSmsUnits.toArray(allowedSmsc);
            } else {
                allowedSmsc = config.getAllowedSmsc();
            }
        }
        return allowedSmsc;
    }
    
    
    public SMSUnit getSMSUnit(String smsUnitName) {
        return allSmsUnits.get(smsUnitName);
    }
  
    public int sendMulti(MultiRequest request) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }
        return unit.sendMulti(request);
     }

    /**
     *Sends a reqular sms message.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param message - The String that will be sent.
     *@param byteContent - The content, in bytes, of the payload. May be null.
     *@param replacePosition - The position to put the message, -1 if no replace.
     *@param delay - The time the message shall first be attempt to be delivered from the SMS-C
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendSMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message, byte[] byteContent, int replacePosition, int delay) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }

        return unit.sendSMS(from, to, validity, rh, id, message, byteContent, replacePosition, delay);
    }
    
    public int sendSMS(SMSRequest request) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }

        return unit.sendSMS(request);
    }

    /**
     * Sends a cancel request to the SMSC by replace position.
     * @param from the from address to cancel on
     * @param to the to address to cancel on
     * @param replacePosition used to determine serviceType if serviceTypeReplace is true
     * @return  SEND_OK if the message was sent ok, or SEND_FAILED_TEMPORARY otherwise.
     */
    public int sendCancel(SMSAddress from, SMSAddress to, int replacePosition) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }

        return unit.sendCancel(from, to, replacePosition);
    }
    
    
    /**
     * Sends a cancel request to the SMSC by service Type
     * @param from the from address to cancel on
     * @param to the to address to cancel on
     * @param serviceType the serviceType to cancel.
     * @return  SEND_OK if the message was sent ok, or SEND_FAILED_TEMPORARY otherwise.
     */
    public int sendCancel(SMSAddress from, SMSAddress to, String serviceType) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }

        return unit.sendCancel(from, to, serviceType);
        
    }


    /**
     *Sends a mwi message.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param message - An optional String message.
     *@param count - how many mails exists, 0 means mwioff.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendMWI(SMSAddress from, SMSAddress to, UserInfo user, UserMailbox inbox, int validity, SMSResultHandler rh, int id, int count, String message) {
        SMSUnit unit = getAutoMWIUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }
        return unit.sendMWI(from, to, user, inbox, validity, rh, id, count, message);
    }

    /**
     *Sends a combined sms and mwi message.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param count - how many mails exists, 0 means mwioff.
     *@param message - The String that will be sent.
     *@param replacePosition - The position to put the message, -1 if no replace.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendMWISMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, int count, String message, int replacePosition) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }
        return unit.sendMWISMS(from, to, validity, rh, id, count, message, replacePosition);
    }

    /**
     *Sends a phone on message.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param message - an optional message.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendPhoneOn(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }
        return unit.sendPhoneOn(from, to, validity, rh, id, message);
    }

    /**
     *Sends a multi lines message. This is a message that contains a header and a footer on each sms
     *and a couple of lines in between.
     *@param request - Contains all the info needed to send the formatted message.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendFormattedSMS(FormattedSMSRequest request) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }
        return unit.sendFormattedSMS(request);
    }
    
    /**
     *Sends a multi lines message. This is a message that contains a header and a footer on each sms
     *and a couple of lines in between.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - InfoResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param lines - An array of lines to be sent.
     *@param maxLinesPerSms - How many lines per sms.
     *@param header - the header on each message.
     *@param footer - the footer on each message.
     *@param callers - a list of calling numbers.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendFormattedSMS(SMSAddress from, SMSAddress to, int validity, SMSInfoResultHandler rh, int id,
            String[] lines, int maxLinesPerSms, String header, String footer, String[] callers) {
        FormattedSMSRequest request = new FormattedSMSRequest(from, to, validity, rh, id, maxLinesPerSms, callers, null);
        return sendFormattedSMS(request);
    }


    /**
     *Sends a flash SMS, i.e. an SMS class 0.
     *@param from - the source address from the sender.
     *@param to - where to send the message.
     *@param validity - how long the message is valid in the smsc.
     *@param rh - ResultHandler to place callbacks on.
     *@param id - the id for the call, used in callbacks.
     *@param message - The String that will be sent.
     *@return - SEND_OK if the message was sent ok, or SEND_FAILED otherwise.
     */
    public int sendFlashSMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }
        return unit.sendFlashSMS(from, to, validity, rh, id, message);
    }

    /**
     * Sends a Visual Voice Mail (VVM) SMS message
     *
     * @param request - the VVMRequest
     * @return - SEND_OK if the message was successfully put in the SMSUnit queue, SEND_FAILED or SEND_FAILED_TEMPORARY otherwise.
     */
    public int sendVvm(Request request) {
        SMSUnit unit = getAutoSMSUnit();

        if (unit == null) {
            return SEND_FAILED_TEMPORARY;
        }
        return unit.sendVvm(request);
    }

    /**
     * getAutoSMSString finds the name of the next smsc.
     *This will not care if the unit is ok or not.
     *@return the name of one smsc.
     */
    private synchronized String getAutoSMSString() {
        String[] allowedSmsUnitNames = getAllowedConfiguredSmsUnits();
        if(allowedSmsUnitNames == null || allowedSmsUnitNames.length == 0) {
            logger.logString("SMSClient.getAutoSMSString: No connection to any SMSc", Logger.LOG_DEBUG);
            return null;
        }

        int numUnits = allowedSmsUnitNames.length;
        if (autoStringIndex >= numUnits) {
            autoStringIndex = 0;
        }

        String unitName = allowedSmsUnitNames[autoStringIndex];
        autoStringIndex++;

        return unitName;
    }


    /**
     * Gets one ok SMSUnit.
     * @return SMSUnit SmsUnit instance.  Null if no SMSUnit found.
     */
    private synchronized SMSUnit getAutoSMSUnit() {
        String[] allowedSmsUnitNames = getAllowedConfiguredSmsUnits();
        if(allowedSmsUnitNames == null || allowedSmsUnitNames.length == 0) {
            logger.logString("SMSClient.getAutoSMSUnit: No connection to any SMSc", Logger.LOG_DEBUG);
            return null;
        }
        
        int numUnits = allowedSmsUnitNames.length;
        if (autoIndex >= numUnits) {
            autoIndex = 0;
        }
        int startIndex = autoIndex;
        boolean first = true;

        while (first || startIndex != autoIndex) {
            String smsUnitName = allowedSmsUnitNames[autoIndex];
            SMSUnit unit = allSmsUnits.get(smsUnitName);
            autoIndex++;

            if ((unit != null) && unit.isOk()) {
                return unit;
            }

            if (autoIndex >= numUnits) {
                autoIndex = 0;
            }
            first = false;
        }
        
        return null;
    }

    /**
     *Gets an smsunit to be used to send mwi requests on. If no mwi-alternative exists
     *the original smsunit is returned.
     *@return SMSUnit to send mwi request on.
     */
    private synchronized SMSUnit getAutoMWIUnit() {
        String smsc = getAutoSMSString();
        if( smsc == null ) {
            return null;
        }
        String[] mwiUnits = config.getMwiServer(smsc);

        if (mwiUnits == null || mwiUnits.length == 0) {
            return allSmsUnits.get(smsc);
        }
        if (mwiUnits.length == 1) {
            String name = mwiUnits[0];

            return allSmsUnits.get(name);
            // return mwiUnits[0]
        }
        synchronized (this) {
            Integer integer = mwiCounts.get(smsc);

            if (integer == null) {
                integer = new Integer(0);
            }
            int count = integer.intValue();

            if (count >= mwiUnits.length) {
                count = 0;
            }

            String name = mwiUnits[count++];

            mwiCounts.put(smsc, new Integer(count));
            return allSmsUnits.get(name);
        }
    }


}
