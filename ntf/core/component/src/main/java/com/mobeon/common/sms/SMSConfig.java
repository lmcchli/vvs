/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.sms;


import com.mobeon.ntf.event.PhoneOnEventListener;


public interface SMSConfig {

    /** returns the path to the directory where charconv.* exists */
    public String getCharConvPath();

    /** how long to wait for answers from the smsc */
    public int getSmscTimeout();

    /** return the max length of an sms */
    public int getSmsStringLength();

    /** max number of sms-parts an sms can be */
    public int getNumberOfSms();

    /** The servicetype string to send to smsc */
    public String getSmeServiceType();
    
    /** The vvm servicetype string to send to smsc */
    public String getVvmServiceType();

    /** the service type string for MWI requests */
    public String getSmeServiceTypeForMwi();

    /** Is it GSM */
    public boolean isBearingNetworkGSM();

    /** Is it CDMA */
    public boolean isBearingNetworkCdma2000();

    /** Shall idle connections be kept anyway */
    public boolean isKeepSmscConnections();

    /** max connections to one smsc */
    public int getSmsMaxConnections();

    /** Minimum time between new connections being made to SMSC */
    public int getSmsMinTimeBetweenConnections();

    /** Minimum time between re connections being made to SMSC
     * when all connections are down.
     */
    public int getSmsMinTimeBetweenReConnections();

    /** allowed smsc to use if no smsc is defined in the call, blank for all */
    public String[] getAllowedSmsc();

    /** the backup smsc for "smsc" */
    public String getSmscBackup(String smsc);

    /** a list of mwi servers to use for the smsc */
    public String[] getMwiServer(String smsc);

    /** error codes that are ignored and therefor not really an error */
    public int[] getSmppErrorCodesIgnored();

    /** do loadbalance to the backup if the ordinary is over used */
    public boolean getSmscLoadBalancing();

    /** use replypath in phone on requests */
    public boolean isReplyPath();

    public int getSmscPollInterval();

    /** the priority for all requests */
    public int getSmsPriority();

    /** Where to send phoneon events to */
    public PhoneOnEventListener getPhoneOnEventListener();

    /** what to do if error codes from the smsc, (LOG, IGNORE) or default(handle error) */
    public String getSmscErrorAction();

    /** how big the queue can be */
    public int getSMSQueueSize();

    public int getSmppVersion();

    public boolean isAlternativeFlashDcs();

    /** Source port defined in the UserDataHeader for VVM */
    public int getVvmSourcePort();

    /** Destination port defined in the UserDataHeader for VVM */
    public int getVvmDestinationPort();

}
