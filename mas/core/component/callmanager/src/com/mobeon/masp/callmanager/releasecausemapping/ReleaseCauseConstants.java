/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.releasecausemapping;

/**
 * This class contains constants for release cause mapping.
 *
 * @author Malin Nyfeldt
 */
public interface ReleaseCauseConstants {

    // Network status codes
    public static final int NETWORK_STATUS_CODE_MIN = 601;
    public static final int NETWORK_STATUS_CODE_MAX = 634;
    public static final int NETWORK_STATUS_CODE_BUSY = 603;
    public static final int NETWORK_STATUS_CODE_NO_REPLY = 610;
    public static final int NETWORK_STATUS_CODE_NOT_REACHABLE = 613;
    public static final int NETWORK_STATUS_CODE_SUPPRESSED = 614;
    public static final int NETWORK_STATUS_CODE_CONGESTION = 620;
    public static final int NETWORK_STATUS_CODE_DEFAULT = 621;
    public static final int NETWORK_STATUS_CODE_NO_LICENSE = 622;

    // SIP error response codes
    public static final int SIP_RESPONSE_CODE_MIN = 300;
    public static final int SIP_RESPONSE_CODE_MAX = 699;

    // Q.850 constants
    public static final int Q850_CAUSE_MIN = 0;
    public static final int Q850_CAUSE_MAX = 127;
    public static final int Q850_LOCATION_MIN = 0;
    public static final int Q850_LOCATION_MAX = 15;

    // Default mappings
    public static final MappedNetworkStatusCode BUSY_MAPPED_NSC =
            new MappedNetworkStatusCode("busy", NETWORK_STATUS_CODE_BUSY);

    public static final MappedNetworkStatusCode NO_REPLY_MAPPED_NSC =
            new MappedNetworkStatusCode("noreply", NETWORK_STATUS_CODE_NO_REPLY);

    public static final MappedNetworkStatusCode NOT_REACHABLE_MAPPED_NSC =
            new MappedNetworkStatusCode("notreachable", NETWORK_STATUS_CODE_NOT_REACHABLE);

    public static final MappedNetworkStatusCode SUPPRESSED_MAPPED_NSC =
            new MappedNetworkStatusCode("suppressed", NETWORK_STATUS_CODE_SUPPRESSED);

    public static final MappedNetworkStatusCode CONGESTION_MAPPED_NSC =
            new MappedNetworkStatusCode("congestion", NETWORK_STATUS_CODE_CONGESTION);


    // Configuration names
    public static final String CONF_RELEASE_CAUSE_MAPPINGS_TABLE = "ReleaseCauseMappings.Table";
    public static final String CONF_MAPPING = "mapping";
    public static final String CONF_NAME = "name";
    public static final String CONF_NSC = "networkStatusCode";
    public static final String CONF_RESPONSE_CODE = "sipResponseCodeIntervals";
    public static final String CONF_Q850_CAUSE = "q850CauseIntervals";
    public static final String CONF_Q850_LOCATION = "q850LocationIntervals";
    public static final String CONF_DEFAULT_NSC = "defaultNetworkStatusCode";
    public static final String CONF_NO_LICENSE_NSC = "noLicenseNetworkStatusCode";
    public static final String CONF_Q850_PRIORITY = "q850Priority";
}
