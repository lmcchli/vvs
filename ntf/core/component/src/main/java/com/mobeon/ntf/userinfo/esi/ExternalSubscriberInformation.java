/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.esi;


import java.util.Properties;


/**
 * Stores information from the ESI. Status comes from XMP with some changes. 2 is ok, 4 is temporary error
 * 5 is permanent error. 
 */
public class ExternalSubscriberInformation {
    public static final int STATUS_OK = 2;
    public static final int STATUS_RETRY = 4;
    public static final int STATUS_FAIL = 5;
    
    private String msisdn;
    private String roamingStatus;
    private String cfBusy;
    private String cfNotReachable;
    private String cfNoReply;
    private String cfUnconditional;
    private String subscriberExternalPrefix;
    private boolean hasMwi;
    private boolean hasFlash;
    private boolean hasReplace;
    private int status;
    
    
    
    
    public ExternalSubscriberInformation() { 
    }
    
    public ExternalSubscriberInformation(int status, String msisdn, String roamingStatus, String cfb, String cfu, String cfnrc, String cfnry, String sep, boolean hasMwi, boolean hasFlash, boolean hasReplace) {
        this.status = status;
        this.msisdn = msisdn;
        this.roamingStatus = roamingStatus;
        this.cfBusy = cfb;
        this.cfNoReply = cfnry;
        this.cfNotReachable = cfnrc;
        this.cfUnconditional = cfu;
        subscriberExternalPrefix = sep;
        this.hasMwi = hasMwi;
        this.hasFlash = hasFlash;
        this.hasReplace = hasReplace;
    }
    
    /**
     * Getter for property cfDivertAll.
     * @return Value of property cfDivertAll.
     */
    public java.lang.String getCfUnconditional() {
        return cfUnconditional;
    }    
    
    /**
     * Setter for property cfDivertAll.
     * @param cfDivertAll New value of property cfDivertAll.
     */
    public void setCfUnconditional(java.lang.String cfUnconditional) {
        this.cfUnconditional = cfUnconditional;
    }    
    
    /**
     * Getter for property cfNoReply.
     * @return Value of property cfNoReply.
     */
    public java.lang.String getCfNoReply() {
        return cfNoReply;
    }    
    
    /**
     * Setter for property cfNoReply.
     * @param cfNoReply New value of property cfNoReply.
     */
    public void setCfNoReply(java.lang.String cfNoReply) {
        this.cfNoReply = cfNoReply;
    }
    
    /**
     * Getter for property cfNotReachable.
     * @return Value of property cfNotReachable.
     */
    public java.lang.String getCfNotReachable() {
        return cfNotReachable;
    }
    
    /**
     * Setter for property cfNotReachable.
     * @param cfNotReachable New value of property cfNotReachable.
     */
    public void setCfNotReachable(java.lang.String cfNotReachable) {
        this.cfNotReachable = cfNotReachable;
    }
    
    /**
     * Getter for property cfBusy.
     * @return Value of property cfBusy.
     */
    public java.lang.String getCfBusy() {
        return cfBusy;
    }
    
    /**
     * Setter for property cfBusy.
     * @param cfBusy New value of property cfBusy.
     */
    public void setCfBusy(java.lang.String cfBusy) {
        this.cfBusy = cfBusy;
    }
    
    /**
     * Getter for property location.
     * @return Value of property location.
     */
    public java.lang.String getRoamingStatus() {
        return roamingStatus;
    }
    
    /**
     * Setter for property location.
     * @param location New value of property location.
     */
    public void setRoamingStatus(java.lang.String roamingStatus) {
        this.roamingStatus = roamingStatus;
    }
    
    /**
     * Getter for property msisdn.
     * @return Value of property msisdn.
     */
    public java.lang.String getMsisdn() {
        return msisdn;
    }
    
    /**
     * Setter for property msisdn.
     * @param msisdn New value of property msisdn.
     */
    public void setMsisdn(java.lang.String msisdn) {
        this.msisdn = msisdn;
    }
    
    /**
     * Getter for property status.
     * @return Value of property status.
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Setter for property status.
     * @param status New value of property status.
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    /**
     * Getter for the subscriber external prefix.
     *@return the subscribers external prefix
     */
    public String getSubscriberExternalPrefix() {
        return subscriberExternalPrefix;
    }

    /**
     * Setter for the subscriber external prefix.
     *@param sep - the subscribers external prefix
     */
    public void setSubscriberExternalPrefix(String sep) {
        subscriberExternalPrefix = sep;
    }

    public String toString() {
        return "\nStatus: " + status + "\nMsisdn: " + msisdn + "\nRoaming Status: " +
        roamingStatus + "\nCFU: " + cfUnconditional + "\nCFB: " + 
        cfBusy + "\nCFNRC: " + cfNotReachable + "\nCFNRY: " + 
        cfNoReply + "\nSEP: " + subscriberExternalPrefix + "\n";
    }

    public boolean isHasMwi() {
        return hasMwi;
    }

    public void setHasMwi(boolean hasMwi) {
        this.hasMwi = hasMwi;
    }

    public boolean isHasFlash() {
        return hasFlash;
    }

    public void setHasFlash(boolean hasFlash) {
        this.hasFlash = hasFlash;
    }

    public boolean isHasReplace() {
        return hasReplace;
    }

    public void setHasReplace(boolean hasReplace) {
        this.hasReplace = hasReplace;
    }
}
