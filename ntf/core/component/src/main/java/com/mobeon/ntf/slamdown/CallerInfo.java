/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.slamdown;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.time.NtfTime;

import java.util.*;

/**
 * Caller info remembers information about one subscribers slamdown calls.
 *
 * Note: this class has a natural ordering that is inconsistent with equals:
 * compareTo works by comparing the dates of two CallerInfos, while ignoring the
 * other members.
 */
public class CallerInfo implements Comparable<Object> {   
    /** Time of the call as the number of seconds after the start time.
        The time is of either the first of the last call from the caller.*/
    private int _calltime = -1;
    private int _voiceCount = 0;
    private boolean internal;
    
    /** Indicates if this CallerInfo will be sent out as a generic notification via the Notifier */
    private boolean isSendingAsGenericNotif = false;
    
    private Map<String, String> slamdownInfoProperties = null; 
    
    /**
     * Constructor. Not used. Instances are created with create().
     */
    protected CallerInfo() {
    }

    protected CallerInfo(boolean internal, boolean isSendingAsGenericNotif) {
        this.internal = internal;
        this.isSendingAsGenericNotif = isSendingAsGenericNotif;
    }

    /**
     * Creates a new CallerInfo instance which is of one of the subclass types.
     * It tries to create an object that is as small as possible, since so many
     * CallerInfo instances will be stored in memory.
     *@param caller - the phone number that did slamdown.
     *@param internal - whether the called number is that of a subscriber (internal) or non-subscriber (external)
     *@return an new instance of one of CallerInfo's subclasses.
     */
    public static CallerInfo create(String caller, boolean internal) {
        return create(caller, internal, false);
    }

    /**
     * Creates a new CallerInfo instance which is of one of the subclass types.
     * It tries to create an object that is as small as possible, since so many
     * CallerInfo instances will be stored in memory.
     *@param caller - the phone number that did slamdown.
     *@param internal - whether the called number is that of a subscriber (internal) or non-subscriber (external)
     *@param isSendingAsGenericNotif - whether the caller info will be sent as a generic notification
     *@return an new instance of one of CallerInfo's subclasses.
     */
    public static CallerInfo create(String caller, boolean internal, boolean isSendingAsGenericNotif) {
        Object o = caller;
/*        if (o instanceof Long) { //
            return new Numeric(((Long) o).longValue(), internal, isSendingAsGenericNotif);
        } else {*/
        //TODO Get rid of the specialization - as all will be Other from now on...
            return new Other((String) o, internal, isSendingAsGenericNotif);
/*        }*/
    }
    
    /**
     * Creates a new CallerInfo instance from journal file information.
     *@param caller - the phone number that did slamdown.
     *@param calltime - the time of the slamdown in seconds since the epoch.
     *@param count - the change in number of slamdowns from this caller.
     *@return an new instance of one of CallerInfo's subclasses.
     */
    public static CallerInfo create(String caller, int calltime, int count) {
        Object o = caller;
        CallerInfo ci;
/*        if (o instanceof Long) { //
            ci = new Numeric(((Long) o).longValue(), true, false);
        } else {*/
        //TODO Get rid of the specialization - as all will be Other from now on...        
            ci = new Other((String) o, true, false);
/*        }*/
        ci._calltime = calltime - NtfTime.START_TIME;
        ci._voiceCount += count;
        return ci;
    }

    public void setSlamdownInfoProperties(Map<String, String> slamdownInfoProperties) {
        this.slamdownInfoProperties = slamdownInfoProperties;
    }
    
    public String getSlamdownInfoProperty(String name) {
        if (slamdownInfoProperties != null) {
            return slamdownInfoProperties.get(name);
        } else {
            return null;
        }
    }
    
    /**
     * Records a voice slamdown from this caller.
     * Updates the date and voice slamdown count.
     *@param d - when the slamdown occured.
     */
    public void voiceSlamdown(Date d) {
        _voiceCount++;
        if (d == null) {
            d = new Date();
        }
        if(!isSendingAsGenericNotif) {
            if(internal) {
                if (_calltime == -1 || Config.isSlamdownTimeOfLastCall()) {
                    _calltime = SlamdownStore.packDate(d);
                }
            } else {
                if (_calltime == -1 || Config.isMcnTimeOfLastCall()) {
                    _calltime = SlamdownStore.packDate(d);
                }
            }
        } else {
            _calltime = SlamdownStore.packDate(d);
        }
    }

    /**
     * Records a voice slamdown from this caller.
     * Updates the date and voice slamdown count.
     *@param calltime - when the slamdown occured.
     */
    public boolean restoreVoiceSlamdown(int calltime, int count) {
        _voiceCount += count;
        if (Config.isSlamdownTimeOfLastCall()) {
            _calltime = Math.max(_calltime, calltime - NtfTime.START_TIME);
        } else {
            _calltime = Math.min(_calltime, calltime - NtfTime.START_TIME);
        }
        return _voiceCount == 0;
    }

    /**
     * Gets the callers number
     *@return the callers number.
     */
    public String getNumber() {
        return null;
    }

    /**
     * Gets the time of the callers slamdown.
     *@return the time of the slamdown.
     */
    public Date getCallTime() {
        return SlamdownStore.unpackDate(_calltime);
    }

    /**
     * Gets the time of the callers slamdown.
     *@return the time of the slamdown.
     */
    public int getCallSec() {
        return _calltime + NtfTime.START_TIME;
    }

    /**
     * Gets the number of slamdown calls from this subscriber.
     *@return the number of sladown calls from this subscriber.
     */
    public int getVoiceCount() { return _voiceCount; }

    /**
     * Compares this CallerInfo with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *@param o - the Object to be compared.
     *@return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     *@throws ClassCastException if a CallerInfo is compared to an object that
     * is not a CallerInfo.
     */
    public int compareTo(Object o) throws ClassCastException {
        int result = _calltime - ((CallerInfo) o)._calltime;

        if(isSendingAsGenericNotif) {
            if(internal) {
                return Config.isSlamdownOldestFirst() ? result : -(result);
            } else {
                return Config.isMcnOldestFirst() ? result : -(result);
            }
        } else {
            return result;
        }
    }


    /**
     * Is it a subscriber (internal) or a non-subscriber (external)
     *@return boolean
     */
    public boolean isInternal() {
        return internal;
    }
    
    public boolean getIsSendingAsGenericNotif() {
        return isSendingAsGenericNotif;
    }
    
    /**
     * Makes a printable representation of this CallerInfo.
     *@return a printable representation of this CallerInfo.
     */
    public String toString() {
        return " calltime=" + _calltime
            + " count=" + _voiceCount;
    }


    /**
     * Specialization of CallerInfo that stores the telephone number as a
     * long. This is possible for numeric telephone numbers with 17 digits or
     * less.
     */
/*    private static class Numeric extends CallerInfo {
        private  long _caller = -1;*/

        /**
         * Constructor.
         *@param number - the callers number packed into a long.
         */
/*        public Numeric(long number, boolean internal, boolean isSendingAsGenericNotif) {
            super(internal, isSendingAsGenericNotif);
            _caller = number;
        }*/

        /**
         * Gets the callers number
         *@return the callers number.
         */
/*        public String getNumber() {
            return SlamdownStore.unpackNumber(_caller);
        }*/

        /**
         * Makes a printable representation of this CallerInfo.
         *@return a printable representation of this CallerInfo.
         */
/*        public String toString() {
            return "{CallerInfo$Numeric: number="
                + SlamdownStore.unpackNumber(_caller)
                + super.toString()
                + "}";
        }
    }*/

    /**
     * Specialization of CallerInfo that stores the telephone number as a
     * a String.
     */
    private static class Other extends CallerInfo {
        private  String _caller = null;

        /**
         * Constructor.
         *@param number the callers number in unpacked format.
         */
        public Other(String number, boolean internal, boolean isSendingAsGenericNotif) {
            super(internal, isSendingAsGenericNotif);
            _caller = number;
        }

        /**
         * Gets the callers number
         *@return the callers number.
         */
        public String getNumber() {
            return _caller;
        }

        /**
         * Makes a printable representation of this CallerInfo.
         *@return a printable representation of this CallerInfo.
         */
        public String toString() {
            return "{CallerInfo$Other: number="
                + _caller
                + super.toString()
                + "}";
        }
    }
}

