/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.management;

import com.abcxyz.services.moip.alarms.MoipAlarmEvent;
import com.abcxyz.services.moip.alarms.MoipAlarmFactory;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.NtfUtil;

/**
 * This class keeps track of the status of a part of NTF. The part is identified
 * with a name, e.g. "smsc_3" and the status can be up or down.
 */
public class ManagementStatus {
    private static final Logger legacyLogger = Logger.getLogger(ManagementStatus.class); // legacy Solaris style logger...
    private boolean statusUp;
    private String name;
    private String hostName;
    private int port;
    private int instance;
    private String zone;
    private ConsumedService consumedService = null;
    private boolean startup = true;

    /**
     * Constructor.
     *@param name - the name of the NTF part.
     */
    public ManagementStatus(String name, ConsumedService service) {
        this.name = name;
        this.statusUp = true;
        this.consumedService = service;
    }

    /**
     * Sets the status of the NTF part to up.
     */
    public void up() {
        if( statusUp == false || startup ) {
            statusUp = true;
            startup = false;
            consumedService.resetConsumedServiceTime();
            legacyLogger.logMessage("Instance is OK for " + name + " in service " + consumedService.getConsumedServiceName(), Logger.L_DEBUG );

            MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(consumedService.getAlarmName());
            if(alarm.getAlarmID().equalsIgnoreCase("dummy"))
            {
            	legacyLogger.logMessage("Dummy alarm: " + alarm.getAlarmID() + " is not supported, no need to raise it.", Logger.L_DEBUG );
            } else {
            	alarm.setDescription("An instance of " + consumedService.getConsumedServiceName() + " is up: " + name);
            	alarm.setInstanceNo(instance);
            	try {
                    ManagementInfo.get().getOamManager().getFaultManager().clearAlarm(alarm);
                } catch (NullPointerException npex) {
                    legacyLogger.logMessage("NTF ERROR: "  + NtfUtil.stackTrace(npex), Logger.L_ERROR);
            	} catch (Exception e) {
            		legacyLogger.logMessage("Unable to clear alarm for " + name + " in service " + consumedService.getConsumedServiceName() + " Exception: " + NtfUtil.stackTrace(e), Logger.L_ERROR);
            	}
            }
        }
    }

    /**
     * Sets the status of the NTF part to down.
     */
    public void down() {
        if( statusUp == true ) {
            statusUp = false;
            consumedService.resetConsumedServiceTime();
            legacyLogger.logMessage("Instance is down for " + name + " in service " + consumedService.getConsumedServiceName(), Logger.L_ERROR );

            MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(consumedService.getAlarmName());
            if(alarm.getAlarmID().equalsIgnoreCase("dummy"))
            {
            	legacyLogger.logMessage("Dummy alarm: " + alarm.getAlarmID() + " is not supported, no need to raise it.", Logger.L_DEBUG );
            } else {
            	alarm.setDescription("An instance of " + consumedService.getConsumedServiceName() + " is down: " + name);
            	alarm.setInstanceNo(instance);
            	try {
                    ManagementInfo.get().getOamManager().getFaultManager().raiseAlarm(alarm);
                } catch (NullPointerException npex) {
                    legacyLogger.logMessage("NTF ERROR: "  + NtfUtil.stackTrace(npex), Logger.L_ERROR);
            	} catch (Exception e) {
                    legacyLogger.logMessage("Unable to raise alarm for " + name + " in service " + consumedService.getConsumedServiceName() + " Exception: " + NtfUtil.stackTrace(e), Logger.L_ERROR);
            	}
            }
        }
    }

    /**
     * Returns the status of the NTF part.
     *@return true if the NTF part, associated with this management status, is
     * up.
     */
    public boolean isUp() {
        return statusUp;
    }

    /**
     *Return an int representation of the status.
     *@return 1=UP, 2= DOWN.
     */
    public int getStatus() {
        if( statusUp ) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Gets the name of the NTF part.
     *@return the name of the NTF part associated with this management status.
     */
    public String getName() {
        return name;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZone() {
        return zone;
    }

    public void setInstance(int instance) {
        this.instance = instance;
    }

    public int getInstance() {
        return instance;
    }
    /**
     * Makes a printable representation of this management status.
     *@return a String with this management status.
     */
    public String toString() {
        return "{ManagementStatus: " + name + " is "
            + (statusUp ? "up" : "down")
            + " Host: " + hostName
            + " Port: " + port
            + " Zone: " + zone
            + "}";
    }
}
