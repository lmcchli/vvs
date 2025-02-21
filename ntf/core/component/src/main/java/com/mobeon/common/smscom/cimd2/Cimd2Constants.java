/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

/**
 * CIMD2Constants contains constants defined in the CIMD2 protocol.
 * See <I>CIMD Interface Specification</I> Issue 3a-0 en for details.
 */
interface Cimd2Constants  {

    /**CIMD2 delimiters*/
    public static final byte CIMD2_STX =                       2;    
    public static final byte CIMD2_ETX =                       3;    
    public static final byte CIMD2_TAB =                       9;    
    public static final byte CIMD2_COLON =                     58;    

    /**CIMD2 operations*/
    public static final int CIMD2_LOGIN =                      1;
    public static final int CIMD2_LOGOUT =                     2;
    public static final int CIMD2_SUBMIT_MESSAGE =             3;
    public static final int CIMD2_ENQUIRE_MESSAGE_STATUS =     4;
    public static final int CIMD2_DELIVERY_REQUEST =           5;
    public static final int CIMD2_CANCEL_MESSAGE =             6;
    public static final int CIMD2_SET =                        8;
    public static final int CIMD2_GET =                        9;
    public static final int CIMD2_DELIVER_MESSAGE =            20;
    public static final int CIMD2_DELIVER_STATUS_REPORT =      23;
    public static final int CIMD2_ALIVE =                      40;

    public static final int CIMD2_LOGIN_RESP =                 51;
    public static final int CIMD2_LOGOUT_RESP =                52;
    public static final int CIMD2_SUBMIT_MESSAGE_RESP =        53;
    public static final int CIMD2_ENQUIRE_MESSAGE_STATUS_RESP =54;
    public static final int CIMD2_DELIVERY_REQUEST_RESP =      55;
    public static final int CIMD2_CANCEL_MESSAGE_RESP =        56;
    public static final int CIMD2_SET_RESP =                   58;
    public static final int CIMD2_GET_RESP =                   59;
    public static final int CIMD2_DELIVER_MESSAGE_RESP =       70;
    public static final int CIMD2_DELIVER_STATUS_REPORT_RESP = 73;
    public static final int CIMD2_ALIVE_RESP =                 90;
    public static final int CIMD2_GENERAL_ERROR_RESP =         98;
    public static final int CIMD2_NACK =                       99;

    /**CIMD2 parameters*/
    public static final int CIMD2_USER_IDENTITY                     = 10;
    public static final int CIMD2_PASSWORD                          = 11;
    public static final int CIMD2_DESTINATION_ADDRESS               = 21;
    public static final int CIMD2_ORIGINATING_ADDRESS               = 23;
    public static final int CIMD2_ORIGINATING_IMSI                  = 26;
    public static final int CIMD2_ALPHANUMERIC_ORIGINATING_ADDRESS  = 27;
    public static final int CIMD2_ORIGINATED_VISITED_MSC_ADDRESS    = 28;
    public static final int CIMD2_DATA_CODING_SCHEME                = 30;
    public static final int CIMD2_USER_DATA_HEADER                  = 32;
    public static final int CIMD2_USER_DATA                         = 33;
    public static final int CIMD2_USER_DATA_BINARY                  = 34;
    public static final int CIMD2_TRANSPORTTYPE                     = 41;
    public static final int CIMD2_MESSAGE_TYPE                      = 42;
    public static final int CIMD2_MORE_MESSAGES_TO_SEND             = 44;
    public static final int CIMD2_OPERATION_TIMER                   = 45;
    public static final int CIMD2_DIALOGUE_ID                       = 46;
    public static final int CIMD2_USSD_PHASE                        = 47;
    public static final int CIMD2_SERVICE_CODE                      = 48;
    public static final int CIMD2_VALIDITY_PERIOD_RELATIVE          = 50;
    public static final int CIMD2_VALIDITY_PERIOD_ABSOLUTE          = 51;
    public static final int CIMD2_PROTOCOL_IDENTIFIER               = 52;
    public static final int CIMD2_FIRST_DELIVERY_TIME_RELATIVE      = 53;
    public static final int CIMD2_FIRST_DELIVERY_TIME_ABSOLUTE      = 54;
    public static final int CIMD2_REPLY_PATH                        = 55;
    public static final int CIMD2_STATUS_REPORT_REQUEST             = 56;
    public static final int CIMD2_CANCEL_ENABLED                    = 58;
    public static final int CIMD2_CANCEL_MODE                       = 59;
    public static final int CIMD2_SERVICE_CENTRE_TIME_STAMP         = 60;
    public static final int CIMD2_STATUS_CODE                       = 61;
    public static final int CIMD2_STATUS_ERROR_CODE                 = 62;
    public static final int CIMD2_DISCHARGE_TIME                    = 63;
    public static final int CIMD2_TARIFF_CLASS                      = 64;
    public static final int CIMD2_SERVICE_DESCRIPTION               = 65;
    public static final int CIMD2_MESSAGE_COUNT                     = 66;
    public static final int CIMD2_PRIORITY                          = 67;
    public static final int CIMD2_DELIVERY_REQUEST_MODE             = 68;
    public static final int CIMD2_GET_PARAMETER                     = 500;
    public static final int CIMD2_MC_TIME                           = 501;
    public static final int CIMD2_ERROR_CODE                        = 900;
    public static final int CIMD2_ERROR_TEXT                        = 901;

    /**CIMD2 error codes*/
    public static final int CIMD2_NO_ERROR =                   0;
    public static final int CIMD2_UNEXPECTED_OPERATION =       1;
    public static final int CIMD2_SYNTAX =                     2;
    public static final int CIMD2_UNSUPPORTED_PARAMETER =      3;
    public static final int CIMD2_CONNECTION_TO_MC_LOST =      4;
    public static final int CIMD2_NO_RESPONSE_FROM_MC =        5;
    public static final int CIMD2_GENERAL_SYSTEM =             6;
    public static final int CIMD2_PARAMETER_FORMATTING =       8;
    public static final int CIMD2_REQUESTED_OPERATION_FAILED = 9;
    /**CIMD2 login error codes*/
    public static final int CIMD2_INVALID_LOGIN =              100 ;
    public static final int CIMD2_TOO_MANY_LOGINS =            102;
    public static final int CIMD2_LOGIN_REFUSED =              103;
    /**CIMD2 submit error codes*/
    public static final int CIMD2_DESTINATION_ADDRESS_ERR =    300;
    public static final int CIMD2_NUMBER_OF_DESTINATIONS =     301;
    public static final int CIMD2_USER_DATA_SYNTAX =           302;
    public static final int CIMD2_DCS_USAGE =                  304;
    public static final int CIMD2_VALIDITY_PERIOD =            305;
    public static final int CIMD2_ORIGINATOR_ADDRESS_ERR =     306;

    /**The size of the CIMD2 header.*/
    public static final int HEADER_SIZE = 8; /*<STX>OO:NNN<TAB>*/;

    /**The size of the CIMD2 trailer.*/
    public static final int TRAILER_SIZE = 1; /*<ETX>*/;
}
