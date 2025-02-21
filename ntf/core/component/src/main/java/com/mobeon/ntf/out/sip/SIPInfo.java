/*
 * SIPInfo.java
 *
 * Created on den 22 februari 2007, 12:46
 */

package com.mobeon.ntf.out.sip;

import com.mobeon.ntf.mail.NotificationEmail;

/**
 * SIPInfo container for XMP client
 */
public class SIPInfo {

    public static final int NOCOUNT = -1;

    public static final int ERR_FAILTOSEND = 421;

    private String notifNumber;
    private short retryCount = 0;
    private int newVoice = NOCOUNT;
    private int oldVoice = NOCOUNT;
    private int newVideo = NOCOUNT;
    private int oldVideo = NOCOUNT;
    private int newFax = NOCOUNT;
    private int oldFax = NOCOUNT;
    private int newEmail = NOCOUNT;
    private int oldEmail = NOCOUNT;
    private int newUrgentVoice = NOCOUNT;
    private int oldUrgentVoice = NOCOUNT;
    private int newUrgentVideo = NOCOUNT;
    private int oldUrgentVideo = NOCOUNT;
    private int newUrgentFax = NOCOUNT;
    private int oldUrgentFax = NOCOUNT;
    private int newUrgentEmail = NOCOUNT;
    private int oldUrgentEmail = NOCOUNT;
    private NotificationEmail userNotifEmail;

    /**
     * Create a new SIPInfo.
     * @param notifNumber Number to outdial to.
     * @param userEmail Email of user we are doing the outdial for
     * @param startTime Time the outdial started, milliseconds since 1970
     * @param userDN Identity for user in MUR.
     */
    public SIPInfo(String notifNumber, String userEmail, String userDN, long startTime) {
        this.notifNumber = notifNumber;
    }

    public NotificationEmail getUserNotifEmail() {
    	return userNotifEmail;
    }

    /**
     * Get string respresentation for logging/debugging.
     * @return Descriptive string
     */
    public String toString()
    {
        return notifNumber;
    }

    public String getNotifNumber() {
        return notifNumber;
    }

    public void setNotifNumber( String number ) {
        this.notifNumber = number;
    }

    public short getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(short retryCount) {
        this.retryCount = retryCount;
    }

    public void incrRetryCount() {
        retryCount++;
    }

    public void setNewVoiceCount(int count) {
        this.newVoice = count;
    }

    public void setOldVoiceCount(int count) {
        this.oldVoice = count;
    }

    public int getNewVoiceCount() {
        return newVoice;
    }

    public int getOldVoiceCount() {
        return oldVoice;
    }

    public void setNewVideoCount(int count) {
        this.newVideo = count;
    }

    public void setOldVideoCount(int count) {
        this.oldVideo = count;
    }

    public int getNewVideoCount() {
        return newVideo;
    }

    public int getOldVideoCount() {
        return oldVideo;
    }

    public void setNewFaxCount(int count) {
        this.newFax = count;
    }

    public void setOldFaxCount(int count) {
        this.oldFax = count;
    }

    public int getNewFaxCount() {
        return newFax;
    }

    public int getOldFaxCount() {
        return oldFax;
    }

    public void setNewEmailCount(int count) {
        this.newEmail= count;
    }

    public void setOldEmailCount(int count) {
        this.oldEmail = count;
    }

    public int getNewEmailCount() {
        return newEmail;
    }

    public int getOldEmailCount() {
        return oldEmail;
    }




    public void setNewUrgentVoiceCount(int count) {
        this.newUrgentVoice = count;
    }

    public void setOldUrgentVoiceCount(int count) {
        this.oldUrgentVoice = count;
    }

    public int getNewUrgentVoiceCount() {
        return newUrgentVoice;
    }

    public int getOldUrgentVoiceCount() {
        return oldUrgentVoice;
    }

    public void setNewUrgentVideoCount(int count) {
        this.newUrgentVideo = count;
    }

    public void setOldUrgentVideoCount(int count) {
        this.oldUrgentVideo = count;
    }

    public int getNewUrgentVideoCount() {
        return newUrgentVideo;
    }

    public int getOldUrgentVideoCount() {
        return oldUrgentVideo;
    }

    public void setNewUrgentFaxCount(int count) {
        this.newUrgentFax = count;
    }

    public void setOldUrgentFaxCount(int count) {
        this.oldUrgentFax = count;
    }

    public int getNewUrgentFaxCount() {
        return newUrgentFax;
    }

    public int getOldUrgentFaxCount() {
        return oldUrgentFax;
    }

    public void setNewUrgentEmailCount(int count) {
        this.newUrgentEmail= count;
    }

    public void setOldUrgentEmailCount(int count) {
        this.oldUrgentEmail = count;
    }

    public int getNewUrgentEmailCount() {
        return newUrgentEmail;
    }

    public int getOldUrgentEmailCount() {
        return oldUrgentEmail;
    }
}
