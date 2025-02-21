/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

/**
 * @author ermmaha
 */
public interface EventType {
    public static final String SYSTEMERROR = "error.com.mobeon.platform.system";
    public static final String DATANOTFOUND = "error.com.mobeon.platform.datanotfound";
    public static final String NUMBERANALYSIS = "error.com.mobeon.platform.numberanalysis";
    public static final String PROFILEREAD = "error.com.mobeon.platform.profileread";
    public static final String PROFILEWRITE = "error.com.mobeon.platform.profilewrite";
    public static final String MAILBOX = "error.com.mobeon.platform.mailbox";
    public static final String ACCOUNT = "error.com.mobeon.platform.account";
    public static final String HLRERROR = "error.com.mobeon.platform.hlrerror";
    public static final String APPENDERROR = "error.com.mobeon.platform.appenderror";
    /**
     * Errors added for the HLR call errors for SS7 feature
     */
    public static final String 	UNKNOWN_SUBSCRIBER_ERROR= "error.com.mobeon.platform.unknownsub";
    public static final String 	SUPPLEMENTARY_SERVICE_ERROR= "error.com.mobeon.platform.service";
    public static final String 	TEMPORARY_ERROR= "error.com.mobeon.platform.temporary";
    public static final String 	IMSI_NOT_FOUND_ERROR= "error.com.mobeon.platform.imsinotfound";
    public static final String 	DIVERT_FAILED_ERROR= "error.com.mobeon.platform.divertfailed";
    public static final String 	INVALID_PARAMETER= "error.com.mobeon.platform.invalidparam";
    public static final String 	SS7MGR_CREATION_ERROR= "error.com.mobeon.platform.ss7mgrcreationfailed";
    public static final String 	UNKNOWN= "error.com.mobeon.platform.unknown";

}