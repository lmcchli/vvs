/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.smsc.interfaces;

/**
 * SmppConstants contains constants defined in the SMPP protocol.
 * See <I>Short Message Peer to Peer Protocol Specification</I> v3.4 for
 * details.
 */
public interface SmppConstants  {

    /**
     * SMPP commands
     */
    public static final int SMPPCMD_GENERIC_NACK =                0x80000000;
    public static final int SMPPCMD_BIND_RECEIVER =               0x00000001;
    public static final int SMPPCMD_BIND_RECEIVER_RESP =          0x80000001;
    public static final int SMPPCMD_BIND_TRANSMITTER =            0x00000002;
    public static final int SMPPCMD_BIND_TRANSMITTER_RESP =       0x80000002;
    public static final int SMPPCMD_QUERY_SM =                    0x00000003;
    public static final int SMPPCMD_QUERY_SM_RESP =               0x80000003;
    public static final int SMPPCMD_SUBMIT_SM =                   0x00000004;
    public static final int SMPPCMD_SUBMIT_SM_RESP =              0x80000004;
    public static final int SMPPCMD_DELIVER_SM =                  0x00000005;
    public static final int SMPPCMD_DELIVER_SM_RESP =             0x80000005;
    public static final int SMPPCMD_UNBIND =                      0x00000006;
    public static final int SMPPCMD_UNBIND_RESP =                 0x80000006;
    public static final int SMPPCMD_REPLACE_SM =                  0x00000007;
    public static final int SMPPCMD_REPLACE_SM_RESP =             0x80000007;
    public static final int SMPPCMD_CANCEL_SM =                   0x00000008;
    public static final int SMPPCMD_CANCEL_SM_RESP =              0x80000008;
    public static final int SMPPCMD_BIND_TRANSCEIVER =            0x00000009;
    public static final int SMPPCMD_BIND_TRANSCEIVER_RESP =       0x80000009;
    public static final int SMPPCMD_OUTBIND =                     0x0000000B;
    public static final int SMPPCMD_ENQUIRE_LINK =                0x00000015;
    public static final int SMPPCMD_ENQUIRE_LINK_RESP =           0x80000015;
    public static final int SMPPCMD_SUBMIT_MULTI =                0x00000021;
    public static final int SMPPCMD_SUBMIT_MULTI_RESP =           0x80000021;
    public static final int SMPPCMD_ALERT_NOTIFICATION =          0x00000102;
    public static final int SMPPCMD_DATA_SM =                     0x00000103;
    public static final int SMPPCMD_DATA_SM_RESP =                0x80000103;

    /**
     * Optional (TLV) parameter tags
     */
    public static final int SMPPTAG_DEST_ADDR_SUBUNIT =           0x0005;
    public static final int SMPPTAG_DEST_NETWORK_TYPE =           0x0006;
    public static final int SMPPTAG_DEST_BEARER_TYPE =            0x0007;
    public static final int SMPPTAG_DEST_TELEMATICS_ID =          0x0008;
    public static final int SMPPTAG_SOURCE_ADDR_SUBUNIT =         0x000D;
    public static final int SMPPTAG_SOURCE_NETWORK_TYPE =         0x000E;
    public static final int SMPPTAG_SOURCE_BEARER_TYPE =          0x000F;
    public static final int SMPPTAG_SOURCE_TELEMATICS_ID =        0x0010;
    public static final int SMPPTAG_QOS_TIME_TO_LIVE =            0x0017;
    public static final int SMPPTAG_PAYLOAD_TYPE =                0x0019;
    public static final int SMPPTAG_ADDITIONAL_STATUS_INFO_TEXT = 0x001D;
    public static final int SMPPTAG_RECEIPTED_MESSAGE_ID =        0x001E;
    public static final int SMPPTAG_MS_MSG_WAIT_FACILITIES =      0x0030;
    public static final int SMPPTAG_PRIVACY_INDICATOR =           0x0201;
    public static final int SMPPTAG_SOURCE_SUBADDRESS =           0x0202;
    public static final int SMPPTAG_DEST_SUBADDRESS =             0x0203;
    public static final int SMPPTAG_USER_MESSAGE_REFERENCE =      0x0204;
    public static final int SMPPTAG_USER_RESPONSE_CODE =          0x0205;
    public static final int SMPPTAG_SOURCE_PORT =                 0x020A;
    public static final int SMPPTAG_DESTINATION_PORT =            0x020B;
    public static final int SMPPTAG_SAR_MSG_REF_NUM =             0x020C;
    public static final int SMPPTAG_LANGUAGE_INDICATOR =          0x020D;
    public static final int SMPPTAG_SAR_TOTAL_SEGMENTS =          0x020E;
    public static final int SMPPTAG_SAR_SEGMENT_SEQNUM =          0x020F;
    public static final int SMPPTAG_SC_INTERFACE_VERSION =        0x0210;
    public static final int SMPPTAG_CALLBACK_NUM_PRES_IND =       0x0302;
    public static final int SMPPTAG_CALLBACK_NUM_ATAG =           0x0303;
    public static final int SMPPTAG_NUMBER_OF_MESSAGES =          0x0304;
    public static final int SMPPTAG_CALLBACK_NUM =                0x0381;
    public static final int SMPPTAG_DPF_RESULT =                  0x0420;
    public static final int SMPPTAG_SET_DPF =                     0x0421;
    public static final int SMPPTAG_MS_AVAILABILITY_STATUS =      0x0422;
    public static final int SMPPTAG_NETWORK_ERROR_CODE =          0x0423;
    public static final int SMPPTAG_MESSAGE_PAYLOAD =             0x0424;
    public static final int SMPPTAG_DELIVERY_FAILURE_REASON =     0x0425;
    public static final int SMPPTAG_MORE_MESSAGES_TO_SEND =       0x0426;
    public static final int SMPPTAG_MESSAGE_STATE =               0x0427;
    public static final int SMPPTAG_USSD_SERVICE_OP =             0x0501;
    public static final int SMPPTAG_DISPLAY_TIME =                0x1201;
    public static final int SMPPTAG_SMS_SIGNAL =                  0x1203;
    public static final int SMPPTAG_MS_VALIDITY =                 0x1204;
    public static final int SMPPTAG_ALERT_ON_MESSAGE_DELIVERY =   0x130C;
    public static final int SMPPTAG_ITS_REPLY_TYPE =              0x1380;
    public static final int SMPPTAG_ITS_SESSION_INFO =            0x1383;

    /**
     * Status codes
     */
    public static final int SMPPSTATUS_ROK =                      0x00000000;
    public static final int SMPPSTATUS_RINVMSGLEN =               0x00000001;
    public static final int SMPPSTATUS_RINVCMDLEN =               0x00000002;
    public static final int SMPPSTATUS_RINVCMDID =                0x00000003;
    public static final int SMPPSTATUS_RINVBNDSTS =               0x00000004;
    public static final int SMPPSTATUS_RALYBND =                  0x00000005;
    public static final int SMPPSTATUS_RINVPRTFLG =               0x00000006;
    public static final int SMPPSTATUS_RINVREGDLVFLG =            0x00000007;
    public static final int SMPPSTATUS_RSYSERR =                  0x00000008;
    public static final int SMPPSTATUS_RINVSRCADR =               0x0000000A;
    public static final int SMPPSTATUS_RINVDSTADR =               0x0000000B;
    public static final int SMPPSTATUS_RINVMSGID =                0x0000000C;
    public static final int SMPPSTATUS_RBINDFAIL =                0x0000000D;
    public static final int SMPPSTATUS_RINVPASWD =                0x0000000E;
    public static final int SMPPSTATUS_RINVSYSID =                0x0000000F;
    public static final int SMPPSTATUS_RCANCELFAIL =              0x00000011;
    public static final int SMPPSTATUS_RREPLACEFAIL =             0x00000013;
    public static final int SMPPSTATUS_RMSGQFUL =                 0x00000014;
    public static final int SMPPSTATUS_RINVSERTYP =               0x00000015;
    public static final int SMPPSTATUS_RINVNUMDESTS =             0x00000033;
    public static final int SMPPSTATUS_RINVDLNAME =               0x00000034;
    public static final int SMPPSTATUS_RINVDESTFLAG =             0x00000040;
    public static final int SMPPSTATUS_RINVSUBREP =               0x00000042;
    public static final int SMPPSTATUS_RINVESMCLASS =             0x00000043;
    public static final int SMPPSTATUS_RCNTSUBDL =                0x00000044;
    public static final int SMPPSTATUS_RSUBMITFAIL =              0x00000045;
    public static final int SMPPSTATUS_RINVSRCTON =               0x00000048;
    public static final int SMPPSTATUS_RINVSRCNPI =               0x00000049;
    public static final int SMPPSTATUS_RINVDSTTON =               0x00000050;
    public static final int SMPPSTATUS_RINVDSTNPI =               0x00000051;
    public static final int SMPPSTATUS_RINVSYSTYP =               0x00000053;
    public static final int SMPPSTATUS_RINVREPFLAG =              0x00000054;
    public static final int SMPPSTATUS_RINVNUMMSGS =              0x00000055;
    public static final int SMPPSTATUS_RTHROTTLED =               0x00000058;
    public static final int SMPPSTATUS_RINVSCHED =                0x00000061;
    public static final int SMPPSTATUS_RINVEXPIRY =               0x00000062;
    public static final int SMPPSTATUS_RINVDFTMSGID =             0x00000063;
    public static final int SMPPSTATUS_RX_T_APPN =                0x00000064;
    public static final int SMPPSTATUS_RX_P_APPN =                0x00000065;
    public static final int SMPPSTATUS_RX_R_APPN =                0x00000066;
    public static final int SMPPSTATUS_RQUERYFAIL =               0x00000067;
    public static final int SMPPSTATUS_RINVOPTPARSTREAM =         0x000000C0;
    public static final int SMPPSTATUS_ROPTPARNOTALLWD =          0x000000C1;
    public static final int SMPPSTATUS_RINVPARLEN =               0x000000C2;
    public static final int SMPPSTATUS_RMISSINGOPTPARAM =         0x000000C3;
    public static final int SMPPSTATUS_RINVOPTPARAMVAL =          0x000000C4;
    public static final int SMPPSTATUS_RDELIVERYFAILURE =         0x000000FE;
    public static final int SMPPSTATUS_RUNKNOWNERR =              0x000000FF;

    /**Version number for SMPP version 3.4.*/
    public static final int SMPP_VERSION = 0x34;

    /**The size of the SMPP header.*/
    public static final int HEADER_SIZE =
        4 + //command_length
        4 + //command_id
        4 + //command_status
        4;  //sequence_number
};
