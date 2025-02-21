/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 *
 * ConsumedService.java
 *
 * Description: This class holds information about Consumed Services for NTF.
 *              A Consumed Service has an index that corresponds to the index
 *              in the MIB. The name corresponds to the service name. Status is the
 *              status of the service and the counters counts the notifications. It
 *              also have a time variable that holds the last time the status changed value.
 * Created on June 4, 2004, 2:51 PM
 *
 * @author  ermjnil
 */

package com.mobeon.ntf.management;

import java.util.*;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.alarms.MoipAlarmFactory.MoipAlarm;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;


public class ConsumedService {
    /* Counter for successfully notifications */
    private ManagementCounter d_succCounter = null;
    /* Counter for NOT successfully notifications */
    private ManagementCounter d_failCounter = null;
    /* A map to store the status of all components in a service */
    private HashMap<String,ManagementStatus> d_StatusMap = null;
    /* The number of instances(statuses) created */
    private int instanceCount = 0;
    /* Service name */
    private String d_name = null;
    /* Counter for time in seconds for last status change */
    private long d_time = 0;
    /* Counter for time in seconds to store the last status update */
    private int d_lastTime = 0;
    /* A flag to indicate if the Consumed Service status has changed value */
    private int d_lastConsumedServiceStatus = 0;

    public static final int STATUS_UP = 1;
    public static final int STATUS_DOWN = 2;
    public static final int STATUS_IMPAIRED = 3;

    private static LogAgent logger = NtfCmnLogger.getLogAgent(ConsumedService.class);

    private String alarmName;

    /**
     * Constructor
     * Creates a new instance of ConsumedService
     *@param name - is the name of the ConsumedService object
     */
    public ConsumedService(String name) {
        d_StatusMap = new HashMap<String, ManagementStatus>();
        d_succCounter = new ManagementCounter("success");
        // 65535 is the max value for the Consumed Service fail counter according to the MIB (Integer 0..65535).
        d_failCounter = new ManagementCounter("failed", 65535);
        d_name = name;
        d_lastTime = NtfTime.now;
        d_time = 0;
        d_lastConsumedServiceStatus = 1;

        if (d_name == NotificationConfigConstants.SHORT_MESSAGE_TABLE){
        	alarmName = MoipAlarm.SMS_CONNECTION.getName();
        }else if(d_name == NotificationConfigConstants.MAIL_TRANSFER_AGENT_TABLE) {
        	alarmName = MoipAlarm.EMAIL_CONNECTION.getName();
        }else if(d_name == NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE) {
        	alarmName = MoipAlarm.MMS_CONNECTION.getName();
        }else if(d_name == NotificationConfigConstants.FAX_SERVER_TABLE) {
            alarmName = MoipAlarm.FAX_CONNECTION.getName();
        }

    }

    /**
     * Returns a ManagementCounter object for successful counts.
     *@return a ManagementCounter object
     */
    public ManagementCounter getSuccessCounter() { return d_succCounter; }

    /**
     * Returns a ManagementCounter object for not successful counts.
     *@return a ManagementCounter object
     */
    public ManagementCounter getFailCounter() { return d_failCounter; }

    /**
     * Returns a ManagementStatus object.
     *@param name - is the key in the table
     *@return a ManagementStatus object
     */
    public synchronized ManagementStatus getStatus(String name) {
        if (d_StatusMap.containsKey(name)) {
            return d_StatusMap.get(name);
        }
        else {
            ManagementStatus mStatus = new ManagementStatus(name, this);
            d_StatusMap.put(name, mStatus);
            setStatusParameters(mStatus);
            instanceCount++;
            if( instanceCount == 1 ) {
                d_lastTime = NtfTime.now;
            }
            return mStatus;
        }
    }

    private void setStatusParameters(ManagementStatus status) {
        String host;
        String logicalzone = null;
        int port;
        int instance = 0;

        try {
            // Services stored under NTF.Config
            if ((d_name == NotificationConfigConstants.SHORT_MESSAGE_TABLE) ||
                    (d_name == NotificationConfigConstants.MAIL_TRANSFER_AGENT_TABLE) ||
                    (d_name == NotificationConfigConstants.FAX_SERVER_TABLE) ||
                    (d_name == NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE)) {

                Map<String, String> component = Config.getExternalEnabler(d_name, status.getName());
                if (component != null) {
                    host = component.get(NotificationConfigConstants.HOST_NAME);
                    port = Integer.parseInt(component.get(NotificationConfigConstants.PORT));
                    logicalzone = component.get(NotificationConfigConstants.LOGICAL_ZONE);
                    String instanceNo = component.get(NotificationConfigConstants.INSTANCE);
                    if(instanceNo == null) {
                        // default value of table element is not read properly, fetch it directly
                        instanceNo = Config.getConfigValue(d_name + "." + NotificationConfigConstants.INSTANCE);
                    }
                    if(instanceNo != null) {
                        instance = Integer.parseInt(instanceNo);
                    }
                } else {
                    logger.debug("No service found for servicename: " + d_name + ", componentname: " + status.getName() + " in config");
                    return;
                }

            } else {
                // Services stored under ExternalComponentRegister
                IServiceInstance inst;
                try {
                    inst = ExternalComponentRegister.getInstance().locateServiceByComponentName(d_name, status.getName());
                    host = inst.getProperty(IServiceInstance.HOSTNAME);
                    port = Integer.parseInt(inst.getProperty(IServiceInstance.PORT));
                    logicalzone = inst.getProperty(IServiceInstance.LOGICALZONE);
                } catch (NoServiceFoundException e) {
                    logger.debug("No service found for servicename: " + d_name + ", componentname: " + status.getName() + "in ExternalComponentRegister");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Port or instanceNo is not numeric for servicename: " + d_name + ", componentname: " + status.getName());
            logger.debug("", e);
            return;
        }

        status.setHostName(host);
        status.setPort(port);
        status.setInstance(instance);
        if( logicalzone != null ) {
        	status.setZone(logicalzone);
        } else {
        	status.setZone("Unspecified");
        }
    }

    /** Returns an aggregated status of all ManagementStatus object in StatusMap
     *@return status, enable(1) or disable(2)
     */
    public int getConsumedServiceStatus() {
        Iterator<ManagementStatus> iter = d_StatusMap.values().iterator();
        boolean allDown = true;
        boolean allUp = true;
        while(iter.hasNext()) {
            ManagementStatus mStatus = iter.next();
            if (mStatus.isUp())
                allDown = false;
            else
                allUp = false;
        }

        if (allUp)
            return STATUS_UP;
        else if (allDown)
            return STATUS_DOWN;
        else
            return STATUS_IMPAIRED;
    }


    public Vector<ManagementStatus> getInstancesDown() {
        Vector<ManagementStatus> instancesDown = new Vector<ManagementStatus>();
        Iterator<ManagementStatus> iter = d_StatusMap.values().iterator();
        while( iter.hasNext() ) {
            ManagementStatus mStatus = iter.next();
            if( mStatus.getStatus() == STATUS_DOWN ) {
                instancesDown.add(mStatus);
            }
        }
        return instancesDown;
    }

    public Vector<ManagementStatus> getInstances() {
        Vector<ManagementStatus> instances = new Vector<ManagementStatus>();
        Iterator<ManagementStatus> iter = d_StatusMap.values().iterator();
        while( iter.hasNext() ) {
            ManagementStatus mStatus = iter.next();
                instances.add(mStatus);
        }
        return instances;
    }

    public boolean hasInstacesDown() {
        return getInstancesDown().size() > 0 ? true : false;
    }


    /**
     * Return the Consumed Service name
     *@return name
     */
    public String getConsumedServiceName() { return d_name;}


    /**
     * Return the last time the Consumed Service status changed
     *@return the time in a ManagementTime format
     */
    public long getConsumedServiceTime() {
        d_time = NtfTime.now - d_lastTime;
        return d_time;
    }

    /**
     * Reseting the status time to zero(0)
     */
    public void resetConsumedServiceTime(){
        int currStatus = getConsumedServiceStatus();
        if (currStatus != d_lastConsumedServiceStatus) {
            d_lastTime = NtfTime.now;
            d_lastConsumedServiceStatus = currStatus;
            //Don't reset the failed counters, if it comes back up you want these to remain the same.
            //d_succCounter.reset();
            //d_failCounter.reset();
        }
    }

    /**
     *@return the number of service instances created.
     */
    public String getAlarmName() {
        return alarmName;
    }

    /**
     *@return the number of service instances created.
     */
    public int getInstanceCount() {
        return instanceCount;
    }

}
