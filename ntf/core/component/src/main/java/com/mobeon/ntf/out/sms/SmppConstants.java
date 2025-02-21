/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.sms;


public interface SmppConstants {

    /**
     * Return Call Message is set to indicate Message is waiting.
     * The SMS content provides information about the type and number of new
     * messages.
     * The originating address is used to return the call and retrieve
     * new messages.
     * For further info please see:
     * GSM 03.40, V7.4.0, 1999-12, clause 9.2.3.9
     */
    public static final short GSM_PID_RETURN_CALL_MESSAGE = 0x5f;

    /**
     * Data Coding Scheme octet is set to Message waiting indication (Store Message)
     * to indicate message waiting to MS.
     * For further info please see:
     * GSM03.38, V7.2.0, 1999-07, clause 4
     */
    public static final short GSM_DCS_UCS2 = 0x08;
    // Group //Type
    public static final short GSM_DCS_DISCARD_DEFAULT_ALPHABET_ACTIVE_VMN = 0xc8; // 1100  1000
    public static final short GSM_DCS_DISCARD_DEFAULT_ALPHABET_INACTIVE_VMN = 0xc0; // 1100  0000
    public static final short GSM_DCS_STORE_DEFAULT_ALPHABET_ACTIVE_VMN = 0xd8; // 1101  1000
    public static final short GSM_DCS_STORE_DEFAULT_ALPHABET_INACTIVE_VMN = 0xd0; // 1101  0000
    public static final short GSM_DCS_STORE_UCS2_ALPHABET_ACTIVE_VMN = 0xe8; // 1110  1000
    public static final short GSM_DCS_STORE_UCS2_ALPHABET_INACTIVE_VMN = 0xe0; // 1110  0000
}
