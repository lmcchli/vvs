/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

/**
 * The SMPPOptionalParamConstants interface contains the SMPP Parameter Framework constants associated with the SMPP PDU optional parameters.
 * <p>
 * The supported SMPP PDU optional parameters include those in the SMPP Protocol Specification v3.4.
 * For details about these optional parameters, please refer to the SMPP Protocol Specification v3.4.
 * <p>
 * Currently, this interface includes constants for:<p>
 * - the tag values for all supported optional parameters<p>
 * - the primitive types for the optional parameter values
 */
public interface SMPPOptionalParamConstants{
    /** 
     * Optional parameters and their tag values 
     */
    
    /** The tag value for the dest_addr_subunit optional parameter. */
    public static final int SMPPTAG_DEST_ADDR_SUBUNIT =           0x0005;

    /** The tag value for the dest_network_type optional parameter. */
    public static final int SMPPTAG_DEST_NETWORK_TYPE =           0x0006;
    
    /** The tag value for the dest_bearer_type optional parameter. */
    public static final int SMPPTAG_DEST_BEARER_TYPE =            0x0007;
    
    /** The tag value for the dest_telematics_id optional parameter. */
    public static final int SMPPTAG_DEST_TELEMATICS_ID =          0x0008;
    
    /** The tag value for the source_addr_subunit optional parameter. */
    public static final int SMPPTAG_SOURCE_ADDR_SUBUNIT =         0x000D;
    
    /** The tag value for the source_network_type optional parameter. */
    public static final int SMPPTAG_SOURCE_NETWORK_TYPE =         0x000E;
    
    /** The tag value for the source_bearer_type optional parameter. */
    public static final int SMPPTAG_SOURCE_BEARER_TYPE =          0x000F;
    
    /** The tag value for the source_telematics_id optional parameter. */
    public static final int SMPPTAG_SOURCE_TELEMATICS_ID =        0x0010;
    
    /** The tag value for the qos_time_to_live optional parameter. */
    public static final int SMPPTAG_QOS_TIME_TO_LIVE =            0x0017;
    
    /** The tag value for the payload_type optional parameter. */
    public static final int SMPPTAG_PAYLOAD_TYPE =                0x0019;
    
    /** The tag value for the additional_status_info_text optional parameter. */
    public static final int SMPPTAG_ADDITIONAL_STATUS_INFO_TEXT = 0x001D;
    
    /** The tag value for the receipted_message_id optional parameter. */
    public static final int SMPPTAG_RECEIPTED_MESSAGE_ID =        0x001E;
    
    /** The tag value for the ms_msg_wait_facilities optional parameter. */
    public static final int SMPPTAG_MS_MSG_WAIT_FACILITIES =      0x0030;
    
    /** The tag value for the privacy_indicator optional parameter. */
    public static final int SMPPTAG_PRIVACY_INDICATOR =           0x0201;
    
    /** The tag value for the source_subaddress optional parameter. */
    public static final int SMPPTAG_SOURCE_SUBADDRESS =           0x0202;
    
    /** The tag value for the dest_subaddress optional parameter. */
    public static final int SMPPTAG_DEST_SUBADDRESS =             0x0203;
    
    /** The tag value for the user_message_reference optional parameter. */
    public static final int SMPPTAG_USER_MESSAGE_REFERENCE =      0x0204;
    
    /** The tag value for the user_response_code optional parameter. */
    public static final int SMPPTAG_USER_RESPONSE_CODE =          0x0205;
    
    /** The tag value for the source_port optional parameter. */
    public static final int SMPPTAG_SOURCE_PORT =                 0x020A;
    
    /** The tag value for the destination_port optional parameter. */
    public static final int SMPPTAG_DESTINATION_PORT =            0x020B;
    
    /** The tag value for the sar_msg_ref_num optional parameter. */
    public static final int SMPPTAG_SAR_MSG_REF_NUM =             0x020C;
    
    /** The tag value for the language_indicator optional parameter. */
    public static final int SMPPTAG_LANGUAGE_INDICATOR =          0x020D;
    
    /** The tag value for the sar_total_segments optional parameter. */
    public static final int SMPPTAG_SAR_TOTAL_SEGMENTS =          0x020E;
    
    /** The tag value for the sar_segment_seqnum optional parameter. */
    public static final int SMPPTAG_SAR_SEGMENT_SEQNUM =          0x020F;
    
    /** The tag value for the SC_interface_version optional parameter. */
    public static final int SMPPTAG_SC_INTERFACE_VERSION =        0x0210;
    
    /** The tag value for the callback_num_pres_ind optional parameter. */
    public static final int SMPPTAG_CALLBACK_NUM_PRES_IND =       0x0302;
    
    /** The tag value for the callback_num_atag optional parameter. */
    public static final int SMPPTAG_CALLBACK_NUM_ATAG =           0x0303;
    
    /** The tag value for the number_of_messages optional parameter. */
    public static final int SMPPTAG_NUMBER_OF_MESSAGES =          0x0304;
    
    /** The tag value for the callback_num optional parameter. */
    public static final int SMPPTAG_CALLBACK_NUM =                0x0381;
    
    /** The tag value for the dpf_result optional parameter. */
    public static final int SMPPTAG_DPF_RESULT =                  0x0420;
    
    /** The tag value for the set_dpf optional parameter. */
    public static final int SMPPTAG_SET_DPF =                     0x0421;
    
    /** The tag value for the ms_availability_status optional parameter. */
    public static final int SMPPTAG_MS_AVAILABILITY_STATUS =      0x0422;
    
    /** The tag value for the network_error_code optional parameter. */
    public static final int SMPPTAG_NETWORK_ERROR_CODE =          0x0423;
    
    /** The tag value for the message_payload optional parameter. */
    public static final int SMPPTAG_MESSAGE_PAYLOAD =             0x0424;
    
    /** The tag value for the delivery_failure_reason optional parameter. */
    public static final int SMPPTAG_DELIVERY_FAILURE_REASON =     0x0425;
    
    /** The tag value for the more_messages_to_send optional parameter. */
    public static final int SMPPTAG_MORE_MESSAGES_TO_SEND =       0x0426;
    
    /** The tag value for the message_state optional parameter. */
    public static final int SMPPTAG_MESSAGE_STATE =               0x0427;
    
    /** The tag value for the ussd_service_op optional parameter. */
    public static final int SMPPTAG_USSD_SERVICE_OP =             0x0501;
    
    /** The tag value for the display_time optional parameter. */
    public static final int SMPPTAG_DISPLAY_TIME =                0x1201;
    
    /** The tag value for the sms_signal optional parameter. */
    public static final int SMPPTAG_SMS_SIGNAL =                  0x1203;
    
    /** The tag value for the ms_validity optional parameter. */
    public static final int SMPPTAG_MS_VALIDITY =                 0x1204;
    
    /** The tag value for the alert_on_message_delivery optional parameter. */
    public static final int SMPPTAG_ALERT_ON_MESSAGE_DELIVERY =   0x130C;
    
    /** The tag value for the its_reply_type optional parameter. */
    public static final int SMPPTAG_ITS_REPLY_TYPE =              0x1380;
    
    /** The tag value for the its_session_info optional parameter. */
    public static final int SMPPTAG_ITS_SESSION_INFO =            0x1383;
    

    /**
     * The primitive types for optional parameter values. 
     */

    /** The integer value representing the Integer primitive type. */
    public static final int INT_TYPE = 1;
    
    /** The integer value representing a String primitive type.  Defined by SMPP Parameter Framework for internal use. */
    public static final int STRING_TYPE = 2;

    /** The integer value representing the Octet String primitive type. */
    public static final int OCTET_STRING_TYPE = 3;

    /** The integer value representing the Bit Mask primitive type. */
    public static final int BIT_MASK_TYPE = 4;

    /** The integer value representing the C-Octet String primitive type. */
    public static final int C_OCTET_STRING_TYPE = 5;

    /** The integer value representing the case when a primitive type is not used.  Defined by SMPP Parameter Framework for internal use. */
    public static final int NOT_USED = 6;

    /** The integer value representing a primitive type that is to be defined.  Defined by SMPP Parameter Framework for internal use. */
    public static final int TO_BE_DEFINED = 7;

    /** The integer value representing a primitive type that is variable.  Defined by SMPP Parameter Framework for internal use. */
    public static final int VARIABLE = 8;

    /** The integer value representing any other primitive type.  Defined by SMPP Parameter Framework for internal use. */
    public static final int OTHER = 9;
}
