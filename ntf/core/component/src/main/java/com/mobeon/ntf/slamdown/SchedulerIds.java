package com.mobeon.ntf.slamdown;

/**
 * Inner class that encapsulates the various scheduler ids for Slamdown/Mcn.
 * 1) SMS-Unit:   Timer for sending SMS-Type-0 request towards NTF's SMS client;
 * 2) SMS-Type-0: Timer for validity period of the SMS-Type-0 request;
 * 3) SMS-Info:   Timer for sending SMS-Info request (the Slamdown/Mcn notification itself) towards NTF's SMS client. 
 */
public class SchedulerIds {

    public static final String SMS_UNIT_EVENT_ID = "unit";
    public static final String SMS_TYPE0_EVENT_ID = "type0";
    public static final String SMS_INFO_EVENT_ID = "info";

    public String smsUnitEventId = null;
    public String smsType0EventId = null;
    public String smsInfoEventId = null;

    /** Default constructor */
    public SchedulerIds() {
    }

    /** Constructor */
    public SchedulerIds(String smsUnitEventId, String smsType0EventId, String smsInfoEventId) {
        this.smsUnitEventId = smsUnitEventId;
        this.smsType0EventId = smsType0EventId;
        this.smsInfoEventId = smsInfoEventId;
    }

    public String getSmsUnitEventId() {
        return smsUnitEventId;
    }
    public String getSmsType0EventId() {
        return smsType0EventId;
    }
    public String getSmsInfoEventId() {
        return smsInfoEventId;
    }
    public void setSmsUnitEventId(String smsUnitEventId) {
        this.smsUnitEventId = smsUnitEventId;
    }
    public void setSmsType0EventId(String smsType0EventId) {
        this.smsType0EventId = smsType0EventId;
    }
    public void setSmsInfoEventId(String smsInfoEventId) {
        this.smsInfoEventId = smsInfoEventId;
    }

    public boolean isEmtpy() {
        if ((smsUnitEventId != null && smsUnitEventId.length() > 0) ||
            (smsType0EventId != null && smsType0EventId.length() > 0) ||
            (smsInfoEventId != null && smsInfoEventId.length() > 0)) {
            return false;
        }
        return true;
    }

    public void nullify() {
        this.smsUnitEventId = null;
        this.smsType0EventId = null;
        this.smsInfoEventId = null;
    }

    public String toString() {
        return "SchedulerIds: \n" +
            "smsUnitEventId: " + this.smsUnitEventId + "\n" +
            "smsType0EventId: " + this.smsType0EventId + "\n" +
            "smsInfoEventId: " + this.smsInfoEventId + "\n";
    }
}
