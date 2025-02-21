/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.management;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.Config;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.threads.NtfThread;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * SendGenericAlarm is a wrapper around the MEMA scripts sendAlarmNew and
 * sendAlarmClear. It allows clients to set and clear generic alarms.
 */
public class SendGenericAlarm {
    /**The \"<action>\" parameter value string to include in the alarm*/
    private String action = "Check logfile for more information";
    /**Use to log traffic and debug information.*/
    private static LogAgent log =  NtfCmnLogger.getLogAgent(SendGenericAlarm.class);
    /**Table of received alarms*/
    private Map<String,Alarm> receivedAlarms = new HashMap<String,Alarm>();
    /**A private class used to send and clear received alarms.*/
    private SendAlarm sendAlarm = null;
    /**A private instance of this class.*/
    private static SendGenericAlarm thisClass = null;

    /** Creates a new instance of SendGenericAlarm*/
    public SendGenericAlarm() {
        sendAlarm = new SendAlarm();
        sendAlarm.start();
    }

    /**Gets an instance of this class.
     *@return a SendGenericAlarm instance.
     */
    public static SendGenericAlarm get() {
        if (thisClass == null) {
            thisClass = new SendGenericAlarm();
        }
        return thisClass;
    }

    /**
     * Sends an alarm to MEMA. If this is a new alarm, it is added to the cache
     * and the send alarm queue. If the alarm exists, nothing is done.
     *@param key is the indentifier of this alarm.
     *@param errorMessage specifies the \"<Description>\" parameter in the sendAlarmNew script.
     *@param severity specifies the \"<Severity>\" parameter in the sendAlarmNew script.
     */
    public void sendAlarm(String key, String errorMessage, String severity) {
        if (key == null || receivedAlarms.containsKey(key)) {
            return;
        }

        if (Config.getLogLevel() == Logger.L_DEBUG) {
            log.debug("Creating alarm " + key + " (" + errorMessage + ")");
        }
        Alarm a = new Alarm(key, errorMessage, severity);
        synchronized (receivedAlarms) {
            if (!receivedAlarms.containsKey(key)) {
                receivedAlarms.put(key, a);
                sendAlarm.send(a);
            }
        }
    }

    /**
     * Clears an alarm from MEMA. If the alarm exists, it is removed from the
     * cache and a remove request is added to the send alarm queue.
     *@param key is the indentifier of this alarm.
     */
    public void clearAlarm(String key) {
        if (key == null || !receivedAlarms.containsKey(key)) {
            return;
        }

        if (Config.getLogLevel() == Logger.L_DEBUG) {
            log.debug("Clearing alarm " + key);
        }
        synchronized (receivedAlarms) {
            Alarm a = receivedAlarms.remove(key);
            if (a != null) {
                a.clear();
                sendAlarm.send(a);
            }
        }
    }

    /**
     * Clears a list of alarms, wihtout regard to whether they are in the cache
     * or not.
     *@param keys is the identifiers of the alarms.
     */
    public void clearAlarms(String[] keys) {
        if (keys == null) {
            return;
        }

        synchronized (receivedAlarms) {
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                if (key != null) {
                    if (Config.getLogLevel() == Logger.L_DEBUG) {
                        log.debug("Clearing alarm " + key);
                    }
                    receivedAlarms.remove(key);
                    Alarm a = new Alarm(key);
                    sendAlarm.send(a);
                }
            }
        }
    }


    /**
     * Class with information about an alarm
     */
    private class Alarm {
        public String _id;
        public String _msg;
        public String _severity;
        public boolean _clear;

        /**
         * Constructor for an Alarm object used when an alarm is detected.
         *@param id - the alarm identity.
         *@param msg - the alarm message.
         *@param severity - the severity of the alarm
         */
        public Alarm(String id, String msg, String severity) {
            _id = id;
            _msg = msg;
            _severity = severity;
            _clear = false;
        }

        /**
         * Constructor for an Alarm object used when an alarm shall be cleared.
         *@param id the alarm identity.
         */
        public Alarm(String id) {
            _id = id;
            _msg = null;
            _severity = null;
            _clear = true;
        }

        /**
         * Changes an Alarm so it clears the alarm in MEMA.
         */
        public void clear() {
            _clear = true;
        }
    }


    /**
     * Sends alarm requests to MEMA. Alarm objects are entered in a queue, and
     * are send to MEMA one at a time, in the order they arrive.
     *
     * There is no synchronization in this class. The alarm cache outside this
     * class should be kept in sync with the alarm status in MEMA, so adding
     * requests to the queue is tightly coupled to updating the cache. Thus,
     * synchronization relies on external synchronization on the alarm cache.
     *
     * This class will use the script sendAlarmNew to send new alarms to MEMA
     * and sendAlarmClear to clear alarms. The scripts are located in the
     * directory determined by the parameter PathToSnmpScripts
     */
    private class SendAlarm extends NtfThread {

        public SendAlarm() {
            super("AlarmSender");
        }

        /**Alarms to send*/
        private SortedMap<String,Alarm> alarmToSend = new TreeMap<String,Alarm>();

        /**Adds the alarm to send to a cache a notifies the run method that there has
         * received a new alarm.
         *@param a - the alarm request to send.
         */
        public void send(Alarm a) {
            synchronized (this) {
                alarmToSend.put(a._id, a);
                this.notifyAll();
            }
        }

        /**
         *
         */
        public boolean ntfRun() {
            Alarm a;

                try {
                a = alarmToSend.remove(alarmToSend.firstKey());

                if (a != null) {
                    if (a._clear) {
                        clear(a);
                    } else {
                        set(a);
                    }
                }

                synchronized (this) {
                    //Wait maximum of 2 seconds before checking management state, locked, shutdown.
                    try { this.wait(2000); } catch (InterruptedException e) { return false; }

                }
                } catch (OutOfMemoryError me) {
                    try {
                        ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                        log.error("NTF out of memory, shutting down... ", me);
                    } catch (OutOfMemoryError me2) {;} //ignore second exception
                    return true; //exit.
                } catch (Exception e) {
                    log.error("Unexpected Exception: ", e); //log exception.
                }

            return false;
        }

        /**
         * Send an alarm clear request.
         *@param a - the request to send.
         */
        private void clear (Alarm a) {
            if (Config.getLogLevel() == Logger.L_DEBUG) {
                log.debug("Sending alarm clear: " + a._id);
            }
            String[] parameters = {
                Config.getPathToSnmpScript() + "/sendAlarmClear",
                a._id + "@" + Config.getNtfHostFqdn(),
            };
            try {
                Runtime.getRuntime().exec(parameters);
            } catch (IOException e) {
                log.error("Could not clear alarm. " + a._id + " ",e);
            } catch (Exception e) {
                log.error("Unknown error. Could not clear alarm. " + a._id + " ",e);
            }
        }

        /**
         * Send an alarm set request.
         *@param a - the request to send.
         */
        private void set(Alarm a) {
            if (Config.getLogLevel() == Logger.L_DEBUG) {
                log.debug("Sending alarm set: " + a._id);
            }
            String[] parameters = {
                Config.getPathToSnmpScript() + "/sendAlarmNew",
                a._id,
                a._severity,
                a._msg,
                action,
            };

            try {
                Runtime.getRuntime().exec(parameters);
            } catch (IOException e) {
                log.error("Could not send alarm. ",e);
            } catch (Exception e) {
                log.error("Unknown error. Could not send alarm. ",e);
            }
        }

        @Override
        public boolean shutdown() {
            return true;
        }
    }

}
