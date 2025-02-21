/**
 * Copyright (c) 2014 Abcxyz
 * All Rights Reserved
 */

package com.mobeon.common.smscom.smpp;

import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.*;

import com.abcxyz.messaging.common.oam.PerformanceEvent;
import com.abcxyz.messaging.common.oam.PerformanceEvent.PerfDataType;
import com.abcxyz.messaging.common.oam.impl.GenericPerformanceEvent;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceData;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceManagerExt;
import com.abcxyz.messaging.scriptengine.plugin.ManiFestProps;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.mobeon.common.smscom.SMSCom;
import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.text.ByteArrayUtils;
import com.mobeon.ntf.util.Logger;

/**
 * SMPP_PDU is a base class for SMPP PDUs. It knows general things such as constants used in the protocol, how to read a PDU and how
 * to parse the header. See <I>Short Message Peer to Peer Protocol Specification</I> v3.4 for details.<p>
 * 
 * Supported counters<p>
 * <ul>
 * <li>Only response (resp) PDUs can have a "_ok" or "_error" post-fix.
 * <li>"_sent" indicates that the PDU is sent by the NTF
 * <li>"_received" indicates that the PDU is received by the NTF
 * <li>These counters are "auto-reset", meaning they will be reset to 0 in the NTF component whenever the oam retrieves these counters.
 * </ul>
 * <p>
 * <b>Table 1</b>: SMPP counters and their direction
 * <table border="2">
 * <tr>
 * <th>Counter Name</th>
 * <th>NTF - SMSC</th>
 * </tr>
 * <tr><td>alert_notification_pdu_received</td>           <td><--</td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>bind_receiver_pdu_sent</td>                    <td>--></td></tr>
 * <tr><td>bind_receiver_resp_pdu_received_ok</td>        <td><--</td></tr>
 * <tr><td>bind_receiver_resp_pdu_received_error</td>     <td><--</td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>bind_transceiver_pdu_sent</td>                 <td>--></td></tr>
 * <tr><td>bind_transceiver_resp_pdu_received_ok</td>     <td><--</td></tr>
 * <tr><td>bind_transceiver_resp_pdu_received_error</td>  <td><--</td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>bind_transmitter_pdu_sent</td>                 <td>--></td></tr>
 * <tr><td>bind_transmitter_resp_pdu_received_ok</td>     <td><--</td></tr>
 * <tr><td>bind_transmitter_resp_pdu_received_error</td>  <td><--</td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>data_sm_pdu_sent</td>                          <td>--></td></tr>
 * <tr><td>data_sm_resp_pdu_received_ok</td>              <td><--</td></tr>
 * <tr><td>data_sm_resp_pdu_received_error</td>           <td><--</td></tr>
 * <tr><td>data_sm_pdu_received</td>                      <td><--</td></tr>
 * <tr><td>data_sm_resp_pdu_sent_ok</td>                  <td>--></td></tr>
 * <tr><td>data_sm_resp_pdu_sent_error</td>               <td>--></td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>deliver_sm_pdu_received_ok</td>                <td>--></td></tr>
 * <tr><td>deliver_sm_pdu_received_error</td>             <td><--</td></tr>
 * <tr><td>deliver_sm_resp_pdu_sent</td>                  <td>--></td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>enquire_link_pdu_sent</td>                     <td>--></td></tr>
 * <tr><td>enquire_link_resp_pdu_received_ok</td>         <td><--</td></tr>
 * <tr><td>enquire_link_resp_pdu_received_error</td>      <td><--</td></tr>
 * <tr><td>enquire_link_pdu_received</td>                 <td><--</td></tr>
 * <tr><td>enquire_link_resp_pdu_sent_ok</td>             <td>--></td></tr>
 * <tr><td>enquire_link_resp_pdu_sent_error</td>          <td>--></td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>generic_nack_pdu_sent</td>                     <td>--></td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>submit_sm_pdu_sent</td>                        <td>--></td></tr>
 * <tr><td>submit_sm_resp_pdu_received_ok</td>            <td><--</td></tr>
 * <tr><td>submit_sm_resp_pdu_received_error</td>         <td><--</td></tr>
 * <tr><td>.</td><td>.</td></tr>
 * <tr><td>unbind_pdu_sent</td>                           <td>--></td></tr>
 * <tr><td>unbind_resp_pdu_received_ok</td>               <td><--</td></tr>
 * <tr><td>unbind_resp_pdu_received_error</td>            <td><--</td></tr>
 * <tr><td>unbind_pdu_received</td>                       <td><--</td></tr>
 * <tr><td>unbind_resp_pdu_sent_ok</td>                   <td>--></td></tr>
 * <tr><td>unbind_resp_pdu_sent_error</td>                <td>--></td></tr>
 * </table><p> 
 * 
 */
public class SMPP_PDU implements SmppConstants {

    /** The connection this PDU instance is used with. */
    protected SMPPCom conn = null;

    /** The number of bytes in the PDU. */
    protected int commandLength;
    /** The id of this PDU. */
    protected int commandId;
    /** The status code in response PDUs. */
    protected int commandStatus;
    /**
     * The number of the PDU. This number starts from 1, and is incremented for each request PDU.
     */
    protected int sequenceNumber;

    /**
     * The bytes read from the SMS-C. Bytes <I>to</I> the SMS-C are returned immediately by the getBuffer method and not stored
     * here.
     */
    protected byte[] buffer = null;
    /**
     * Header is a place to read the PDU size, and is also used for the entire buffer for PDUs that have no body.
     */
    protected byte[] header = new byte[HEADER_SIZE];
    
    /** Contains all 3 network error codes. */
    private byte[] networkErrorCodes;

    /** Position in the buffer when parsing the PDU. */
    protected int pos = 0;

    /** Optional parameters (if present) */
    protected Hashtable<Integer, Object> optionalParameters;

    protected EOFException eofe;
    
    //Performance Data
    private static PerformanceManagerExt perfMgr = (PerformanceManagerExt) NtfCmnManager.getInstance().getPerformanceManager();
    protected static final String NB_PENDING_SMPP_REQS = "nb_pending_smpp_reqs";    
    protected String eventName = "other_smpp_pdu";
    protected boolean isResponse = false;

    private com.mobeon.common.smscom.Logger log = null; 
    protected static final String _SENT = "_sent";
    protected static final String _SENT_TOTAL = "_sent_total";
    protected static final String _RECEIVED = "_received";
    protected static final String _RECEIVED_TOTAL = "_received_total";
    protected static final String _OK = "_ok";
    protected static final String _ERROR = "_error";
   
    private static boolean includeTotalCount = Boolean.parseBoolean(System.getProperty("smpp_pdu.include.total.count", "false"));

    /****************************************************************
     * Constructor.
     * 
     * @param conn
     *        - the connection that uses this PDU instance.
     */
    SMPP_PDU(SMPPCom conn) {
        this.conn = conn;
        this.log  = conn.getLogger();
        optionalParameters = new Hashtable<Integer, Object>();
        eofe = new EOFException("EOF on connection to SMS-C");
        init();
    }

    SMPP_PDU(SMPPCom conn, String eventName) {
        this(conn);
        this.eventName = eventName;
        this.isResponse = false;
    }
    
    SMPP_PDU(SMPPCom conn, String eventName, boolean isResponsePDU) {
        this(conn);
        this.eventName = eventName;
        this.isResponse = isResponsePDU;
    }

    
    /**
     * getBuffer returns a byte array with a raw SMPP message in the form used for communicating with the SMSC. "From-SMSC" PDUs use
     * the default implementation and just return the buffer. "To-SMSC" PDUs create a new buffer by encoding the message parameters
     * into the SMPP format in a PDU-specific way.
     * 
     * @return the read or created buffer, or null (if nothing has been read, or there is not enough information in the PDU yet to
     *         create a buffer).
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Reads a PDU from the supplied input stream and parses the header parameters.
     * 
     * @param is - where to read from.
     * @return true if there was something to read
     * @throws IOException if there is something wrong with the request.
     */
    public boolean read(InputStream is) throws IOException { 
        int len;

        init();
        len = read(is,header,0,4);

        if (len == 0 ) {
            //normal case just timed out.
            return false;
        }

        if (len < 4 ) {
            InterruptedIOException e = new  InterruptedIOException("SMPPCom failed to read PDU length field, read the first " + len + " octets only.");
            e.bytesTransferred=len;
            throw e;
        }

        buffer = header; // Parsing always uses buffer, so set buffer to
        // reference header
        pos = 0; // Reset parse position to beginning
        commandLength = getInt(4);
        if (commandLength < 16 || commandLength > 1024) {
            throw new IOException("Impossible PDU length " + commandLength);
        }

        if (commandLength > HEADER_SIZE) { // We need a new larger buffer
            buffer = new byte[commandLength];
            // Put back the length in the new buffer
            System.arraycopy(header, 0, buffer, 0, 4);
        } else {
            // Header-only PDU so we can leave buffer referencing the header buffer
            ;
        }

        //read the rest of the PDU into the buffer.
        len = read(is,buffer, 4, commandLength - 4); // Read the rest of the PDU

        if (len < commandLength - 4) {
            InterruptedIOException e = new  InterruptedIOException("SMPPCom failed to read complete PDU");
            e.bytesTransferred=len + 4;
            throw e;
        }
        if (conn.getSpy() != null) {
            conn.getSpy().fromSMSC(buffer);
        }

        pos = 4; // Start after the length field
        commandId = getInt(4);
        commandStatus = getInt(4);
        sequenceNumber = getInt(4);
        return true;
    }

    /* Read the entire length of buffer unless stream closed or timeout occurs.
     * 
     * Added for TR:HT10919 - MIO NTF disconnects from SMSC
     * 
     * Sometimes read can return less than length if SMPP PDU is split over Ethernet packets, interrupted (shutting down), 
     * or a timeout occurs in the middle of reading a PDU towards the end of the time SoTimeout.
     * 
     * Timeout (Cm.smscTimeout) is by default 30 seconds, this is the maximum time to wait for a complete PDU. 
     * The SoTmeout is set to a smaller figure to allow more frequent state checking.
     * 
     * See SMPPCom.java connect() smscSock.setSoTimeout for how the timeout is set.
     */ 
    private int read(InputStream is, byte[] buff, int offset, int len) throws IOException{
        int bytesRead=0;
        int bytesReadSoFar=0;
        int length = len;
        long timeOut = System.currentTimeMillis()+conn.getTimeout();

        while (length > 0 && timeOut > System.currentTimeMillis() ) {
            try {
                bytesRead=0;
                bytesRead = is.read(buff, offset, length); // Read the PDU length

                if (bytesRead == -1) {
                    throw eofe;    //means the socket/stream has been closed.                    
                }                

                bytesReadSoFar+=bytesRead;
            } catch (InterruptedIOException e) { // a timeout..
                /* Interrupted means the connection is shutting down. 
                 * 
                 * If 0 bytes read this is normal, just means no new data and should return 0 to allow 
                 * state checking, but if already read part of the PDU, we should allow a retry to get
                 * the rest of the current PDU before returning.
                 * 
                 * The exception to this is NTF is in the final steps of exiting during a clean shut down.
                 * 
                 * The thread is interrupted by the main thread on exiting, if this is the case, 
                 * we just need to do a quick cleanup and exit.
                 */                   

                bytesRead+=e.bytesTransferred; // the exception returns how many bytes were actually read.
                bytesReadSoFar+=bytesRead;

                if (bytesReadSoFar > 0) {
                    if (ManagementInfo.get().isAdministrativeStateExit()) {
                        log.logString("SMPP_PDU read : Unable to read complete PDU, due to interupt, read " + bytesReadSoFar + " out of: " + len + ", however NTF is shutting down so will exit and not retry.", com.mobeon.common.email.Logger.LOG_VERBOSE);
                        return bytesReadSoFar;
                    } else if (log.ifLog(Logger.L_DEBUG))  {                        
                        log.logString("SMPP_PDU read: interupted  during partially read PDU: " + bytesReadSoFar + " out of: " + len + " will retry.", com.mobeon.common.email.Logger.LOG_DEBUG);
                    }
                }
            }
            if (bytesReadSoFar == 0) {
                return 0; //normal timeout (nothing read), go back to main loop to do checks.
            }
            if (bytesReadSoFar < len) { 
                /* If did not get all of PDU and not a timeout, adjust offset and length and try again.
                 * sometimes can get some data if one IP packet and some in another, read in this
                 * case returns just what it had in the first packet without waiting.
                 * 
                 * This can cause the socket stream to get out of sync as we start again to read PDU without
                 * data already collected and assume it to be the start of a new packet which results in IO exception
                 * and close of connection.  Instead we try to read the rest of the pdu.
                 */

                if (ManagementInfo.get().isAdministrativeStateExit() ) {
                    log.logString("SMPP_PDU read : Unable to read complete PDU, due to NTF final shutdown, read " + bytesReadSoFar + " out of: " + len, com.mobeon.common.email.Logger.LOG_VERBOSE);
                    return bytesReadSoFar;
                }

                if (log.ifLog(Logger.L_DEBUG) && timeOut > System.currentTimeMillis()) {
                    log.logString("SMPP_PDU read: time-out during partially read PDU: " + bytesReadSoFar + " out of: " + len + " will retry.", com.mobeon.common.email.Logger.LOG_DEBUG);
                }

                //Correct offset and length to read rest of PDU.
                offset+=bytesRead;
                length-=bytesRead;
            } else {
                length=0; //exit loop.
            }

            if ( length > 0 && bytesRead > 0) {
                /* reset timeout if we received something, as we may start receiving at
                 * the end of the time out window, otherwise we can timeout prematurely.
                 */
                if (!conn.isClosing()) { //just to prevent delaying connection close on slow connections, just keep original timeout if closing..
                    timeOut = System.currentTimeMillis()+conn.getTimeout();
                }
            }    
        }           
        return bytesReadSoFar;
    }

    /**
     * This method parses the body part of a buffer from the SMS-C. The buffer is already read by the read() method and the header
     * is parsed. This method is overridden for all PDUs that have a body. The result is stored in member variables and the values
     * are accessed with get-functions specific to each PDU.
     */
    public void parseBody() {
        pos = 4 * 4;
    }

    /**
     * This method parses the body part of a buffer from the SMS-C. The buffer is already read into another SMPP_PDU, so the buffer
     * and header parameters are copied from it before the parsing starts.
     * <P>
     * This method is used to "change" PDU type when bytes are read by a PDU of an expected type and the parsed header contains the
     * id of another command.
     * 
     * @param pdu
     *        - the SMPP_PDU that contains the buffer to parse.
     */
    public void parseBody(SMPP_PDU pdu) {
        buffer = pdu.buffer;
        commandLength = pdu.commandLength;
        commandId = pdu.commandId;
        commandStatus = pdu.commandStatus;
        sequenceNumber = pdu.sequenceNumber;
        parseBody();
    }

    /**
     * putInt puts an integer into a byte array.
     * 
     * @param buf
     *        - the target byte array.
     * @param offs
     *        - the offset in the array to start putting in.
     * @param len
     *        - the number of bytes to put.
     * @param val
     *        - the value to put in the array.
     */
    public void putInt(byte[] buf, int offs, int len, int val) {
        for (int i = offs + len - 1; i >= offs; i--) {
            buf[i] = (byte) (val & 0xFF);
            val >>= 8;
        }
    }

    /**
     * getInt extracts an integer from the buffer.
     * 
     * @param size
     *        - the number of bytes in the integer (0, 1, 2 or 4)
     * @return the int extracted from the buffer
     */
    public int getInt(int size) {
        int val = 0;
        if (size <= 4) {
            int end = pos + size;
            if (end > buffer.length) {
                end = buffer.length;
            }
            for (; pos < end; pos++) {
                val <<= 8;
                val |= ((buffer[pos]) & 0xFF); // Get rid of negative bytes
            }
        } else { // Bad integer size, skip the value
            pos += size;
        }
        return val;
    }

    /**
     * @return The string extracted from the buffer.
     */
    public String getNTS() {
        int i = pos;

        while (i < buffer.length && buffer[i] != 0) {
            i++;
        } // Find null terminator
        String s = new String(buffer, pos, i - pos);

        if (i < buffer.length) { // This is the normal case, otherwise the string
                                 // ended without terminator
            i++; // Skip null terminator
        }
        pos = i;
        return s;
    }

    /**
     * getString extracts a string of known length from the buffer.
     * 
     * @param size
     *        - the size of the string to get.
     * @return The string extracted from the buffer.
     */
    public String getString(int size) {
        int s = Math.min(size, buffer.length - pos);
        String result = new String(buffer, pos, s);
        pos += s;
        return result;
    }

    /**
     * getBytes extracts a byte array of known length from the buffer.
     * 
     * @param size
     *        - the size of the byte array to get.
     * @return The bytes extracted from the buffer.
     */
    public byte[] getBytes(int size) {
        int s = Math.min(size, buffer.length - pos);
        byte[] result = new byte[s];
        System.arraycopy(buffer, pos, result, 0, s);
        pos += s;
        return result;
    }

    /**
     * writeNTS writes a null-terminated string to a DataOutput
     * 
     * @param o
     *        - the DataOutput to write to.
     * @param s
     *        - string to write.
     * @throws IOException
     *         if the write fails.
     */
    public void writeNTS(DataOutput o, String s) throws IOException {
        if (s != null) {
            o.writeBytes(s);
        }
        o.writeByte(0);
    }

    /**
     * getOptionalParameters parses the rest of buffer and returns all optional parameters.
     */
    public void getOptionalParameters() {
        if (pos < buffer.length) {
            while (pos + 4 < buffer.length) { // Stop when there is not room for
                                              // another TLV in the remaining buffer
                int tag = getInt(2);
                int len = getInt(2);

                switch (tag) {
                    case SMPPTAG_DEST_ADDR_SUBUNIT:
                    case SMPPTAG_DEST_NETWORK_TYPE:
                    case SMPPTAG_DEST_BEARER_TYPE:
                    case SMPPTAG_DEST_TELEMATICS_ID:
                    case SMPPTAG_SOURCE_ADDR_SUBUNIT:
                    case SMPPTAG_SOURCE_NETWORK_TYPE:
                    case SMPPTAG_SOURCE_BEARER_TYPE:
                    case SMPPTAG_SOURCE_TELEMATICS_ID:
                    case SMPPTAG_QOS_TIME_TO_LIVE:
                    case SMPPTAG_PAYLOAD_TYPE:
                    case SMPPTAG_MS_MSG_WAIT_FACILITIES:
                    case SMPPTAG_PRIVACY_INDICATOR:
                    case SMPPTAG_USER_MESSAGE_REFERENCE:
                    case SMPPTAG_USER_RESPONSE_CODE:
                    case SMPPTAG_SOURCE_PORT:
                    case SMPPTAG_DESTINATION_PORT:
                    case SMPPTAG_SAR_MSG_REF_NUM:
                    case SMPPTAG_LANGUAGE_INDICATOR:
                    case SMPPTAG_SAR_TOTAL_SEGMENTS:
                    case SMPPTAG_SAR_SEGMENT_SEQNUM:
                    case SMPPTAG_SC_INTERFACE_VERSION:
                    case SMPPTAG_CALLBACK_NUM_PRES_IND:
                    case SMPPTAG_NUMBER_OF_MESSAGES:
                    case SMPPTAG_DPF_RESULT:
                    case SMPPTAG_SET_DPF:
                    case SMPPTAG_MS_AVAILABILITY_STATUS:
                    case SMPPTAG_DELIVERY_FAILURE_REASON:
                    case SMPPTAG_MORE_MESSAGES_TO_SEND:
                    case SMPPTAG_DISPLAY_TIME:
                    case SMPPTAG_SMS_SIGNAL:
                    case SMPPTAG_MS_VALIDITY:
                    case SMPPTAG_ALERT_ON_MESSAGE_DELIVERY:
                    case SMPPTAG_ITS_REPLY_TYPE:
                        optionalParameters.put(new Integer(tag), new Integer(getInt(len)));
                        break;

                    case SMPPTAG_ADDITIONAL_STATUS_INFO_TEXT:
                    case SMPPTAG_RECEIPTED_MESSAGE_ID:
                    case SMPPTAG_SOURCE_SUBADDRESS:
                    case SMPPTAG_DEST_SUBADDRESS:
                    case SMPPTAG_CALLBACK_NUM_ATAG:
                    case SMPPTAG_CALLBACK_NUM:
                    case SMPPTAG_MESSAGE_PAYLOAD:
                    case SMPPTAG_USSD_SERVICE_OP:
                    case SMPPTAG_ITS_SESSION_INFO:
                        optionalParameters.put(new Integer(tag), getString(len));
                        break;

                    case SMPPTAG_MESSAGE_STATE:
                        optionalParameters.put(new Integer(tag), new Integer(getInt(len)));
                        break;

                    case SMPPTAG_NETWORK_ERROR_CODE:
                        optionalParameters.put(new Integer(tag), networkErrorCodeToString(getBytes(len)));
                        break;

                    default:
                        pos += len;
                }
            }
        }
    }
    
    /**
     * Returns a String representing all 3 bytes of the <i>network_error_code</i> using the
     * <code>ByteArrayUtils.byteArrayToHexString(byte[] array)</code> method.
     */
    public String getNetworkErrorCodes()
    {
        if (networkErrorCodes == null) return null;
        return ByteArrayUtils.byteArrayToHexString(networkErrorCodes);
    }

    private String networkErrorCodeToString(byte[] ba) {
        String result;
        if (ba.length != 3) {
            result = "Bad network error code{";
            if (ba.length > 0) {
                result += ba[0] + ",";
            }
        }

        switch (ba[0]) {
            case 1:
                result = "ANSI-136{";
                break;
            case 2:
                result = "IS-95{";
                break;
            case 3:
                result = "GSM{";
                break;
            default:
                result = "other(" + ba[0] + "){";
                break;
        }

        for (int i = 1; i < ba.length; i++) {
            result += ba[i] + ",";
        }
        
        networkErrorCodes = new byte[3];
        for (int i = 0; i < 3; i++)
        {
            networkErrorCodes[i] = ba[i];
        }
        return result + "}";
    }

    /**
     * MessageState action types
     */
    public enum MessageStateActionTypes {
        ok                  ("Ok",                  true,   PhoneOnEvent.PHONEON_OK),
        failedtemporarily   ("failed temporarily",  true,   PhoneOnEvent.PHONEON_FAILED_TEMPORARY),
        failedpermanently   ("failed permanently",  true,   PhoneOnEvent.PHONEON_FAILED),
        discard             ("discard",             false,  0);

        private String name;
        private boolean sendSmsType0ResponseToClient;
        private int phoneOnEvent;

        MessageStateActionTypes(String name, boolean sendSmsType0ResponseToClient, int phoneOnEvent) {
            this.name = name;
            this.sendSmsType0ResponseToClient = sendSmsType0ResponseToClient;
            this.phoneOnEvent = phoneOnEvent;
        }

        public String getName() {
            return this.name;
        }

        public boolean getSendSmsType0ResponseToClient() {
            return this.sendSmsType0ResponseToClient;
        }

        public int getPhoneOnEvent() {
            return this.phoneOnEvent;
        }

        public static MessageStateActionTypes mapToMessageStateActionTypes(String action) {
            MessageStateActionTypes type = MessageStateActionTypes.discard;
            try {
                type = MessageStateActionTypes.valueOf(action);
            } catch (Exception e) {
            }
            return type;
        }
    }

    /**
     * MessageState types Based on SMPP 3.4, section 5.2.28.
     */
    public enum MessageStateTypes {

        /** Message states listed in SMPP 3.4, section 5.2.28. */
        MESSAGE_STATE_ENROUTE("Message state EnRoute", 1, "EnRoute"), MESSAGE_STATE_DELIVERED("Message state Delivered", 2,
                "Delivered"), MESSAGE_STATE_EXPIRED("Message state Expired", 3, "Expired"), MESSAGE_STATE_DELETED(
                "Message state Deleted", 4, "Deleted"), MESSAGE_STATE_UNDELIVERABLE("Message state Undeliverable", 5,
                "Undeliverable"), MESSAGE_STATE_ACCEPTED("Message state Accepted", 6, "Accepted"), MESSAGE_STATE_UNKNOWN(
                "Message state Unknown", 7, "Unknown"), MESSAGE_STATE_REJECTED("Message state Rejected", 8, "Rejected"),

        /** Message states received with a value greater that 8 */
        MESSAGE_STATE_INVALID_VALUE("Message state Invalid value", 9, "Invalid"),

        /** Message states not provided in the SMPP request/response */
        MESSAGE_STATE_NOT_PROVIDED("Message state Not provided", 10, "NotProvided");

        private String name;
        private int value;
        private String type;

        MessageStateTypes(String name, int value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        public String getName() {
            return this.name;
        }

        public int getValue() {
            return this.value;
        }

        public String getType() {
            return this.type;
        }
    }

    /**
     * ms_availability_status values Based on SMPP 3.4, section 5.3.2.30
     */
    public enum MSAvailabilityStatusEventTypes {

        /** Message statuses listed in SMPP 3.4, section 5.3.2.30 are mapped to corresponding phoneOn Events */
        MS_STATUS_AVAILABLE     ("Available",   0,  PhoneOnEvent.PHONEON_OK), // available 
        MS_STATUS_DENIED        ("Denied",      1,  PhoneOnEvent.PHONEON_FAILED), // e.g. suspended, no SMS capability etc
        MS_STATUS_UNAVAILABLE   ("Unavailable", 2,  PhoneOnEvent.PHONEON_FAILED_TEMPORARY); //unavailable

        private String status;
        private int msAvailabilityStatusValue;
        private int phoneOnEventMapping;

        MSAvailabilityStatusEventTypes(String status, int msAvailabilityStatusValue, int phoneOnEventMapping) {
            this.status = status;
            this.msAvailabilityStatusValue = msAvailabilityStatusValue;
            this.phoneOnEventMapping = phoneOnEventMapping;
        }

        public String getStatus() {
            return this.status;
        }
        
        public int getMsAvailabilityStatusValue() {
            return msAvailabilityStatusValue;
        }

        public int getPhoneOnEventMapping() {
            return phoneOnEventMapping;
        }

    }


    /**
     * init resets the PDU to the initial state, ready for reading and parsing a new buffer.
     */
    protected void init() {
        buffer = null;
        commandLength = 0;
        optionalParameters.clear();
        pos = 0;
    }

    
    public void incrementCounterSent() {
        incrementCounter(_SENT, _SENT_TOTAL);
    }
    
    public void incrementCounterReceived() {
        incrementCounter(_RECEIVED, _RECEIVED_TOTAL);
    }
    
    protected void incrementCounter(String label, String labelTotal){
        
        StringBuffer fullEventName = new StringBuffer();
        fullEventName.append(eventName).append(label);
        
        StringBuffer fullEventName_total = null;
        if (SMPP_PDU.includeTotalCount) {
            fullEventName_total = new StringBuffer();
            fullEventName_total.append(eventName).append(labelTotal);
        }
        
        if (isResponse) {
            if (getCommandStatus() == SMPPSTATUS_ROK) {
                fullEventName.append(_OK);
                
                if (fullEventName_total != null && SMPP_PDU.includeTotalCount) {
                    fullEventName_total.append(_OK);
                }
            } else {
                fullEventName.append(_ERROR);

                if (fullEventName_total != null && SMPP_PDU.includeTotalCount) {
                    fullEventName_total.append(_ERROR);
                }
            }
        }
        
        PerformanceEvent smppPduEvent = new GenericPerformanceEvent(fullEventName.toString(), PerfDataType.COUNTER);
        PerformanceData perfData = perfMgr.getPerformanceData(smppPduEvent);
        perfData.setAutoReset(true);
        perfData.setUnits("message");
        perfData.increment();

        if (fullEventName_total != null && SMPP_PDU.includeTotalCount) {
            PerformanceEvent smppPduEvent_total = new GenericPerformanceEvent(fullEventName_total.toString(), PerfDataType.COUNTER);
            PerformanceData perfData_total = perfMgr.getPerformanceData(smppPduEvent_total);
            perfData_total.setUnits("message");
            perfData_total.increment();
        }
    }        

    /**
     * Increment the number of pending SMPP requests counter. The increment is only done for SMPP requests. SMPP responses
     * are not considered here. From a feature point of view, it is important to call this increment method only for
     * outgoing SMPP requests. The logic to determine if the SMPP pdu is outgoing or incomming can't be included in this
     * method. 
     */
    public void incrementNbPendingReqs() {
        if (!isResponse) {
            PerformanceEvent nbSmppReqsEvent = new GenericPerformanceEvent(NB_PENDING_SMPP_REQS, PerfDataType.COUNTER);
            PerformanceData perfData = perfMgr.getPerformanceData(nbSmppReqsEvent);
            perfData.setUnits("message");
            perfData.increment();
        }
    }

    
    /**
     * Decrement the number of pending SMPP requests counter. The decrement is only done for SMPP responses. SMPP requests
     * are not considered here. From a feature point of view, it is important to call this decrement method only for
     * incomming SMPP responses. The logic to determine if the SMPP pdu is outgoing or incomming can't be included in this
     * method. 
     */
    public void decrementNbPendingReqs() {
        if (isResponse) {
            PerformanceEvent nbSmppReqsEvent = new GenericPerformanceEvent(NB_PENDING_SMPP_REQS, PerfDataType.COUNTER);
            PerformanceData perfData = perfMgr.getPerformanceData(nbSmppReqsEvent);
            perfData.setUnits("message");
            perfData.decrement();
        }
    }
    
    
    /**
     * Encodes the header parameters into a byte array. This method is used for header-only PDUs.
     * 
     * @param buf
     *        - the target byte array.
     */
    public void putHeader(byte[] buf) {
        putInt(buf, 0, 4, commandLength);
        putInt(buf, 4, 4, commandId);
        putInt(buf, 8, 4, commandStatus);
        putInt(buf, 12, 4, sequenceNumber);
    }

    /**
     * Encodes and writes the header parameters to a DataOutput.
     * 
     * @param o
     *        - the target DataOutput
     * @throws IOException
     *         if writing fails
     */
    public void writeHeader(DataOutput o) throws IOException {
        o.writeInt(commandLength);
        o.writeInt(commandId);
        o.writeInt(commandStatus);
        o.writeInt(sequenceNumber);
    }

    /**
     * Returns the length header field of this SMPP command.
     * 
     * @return the command length.
     */
    public int getCommandLength() {
        return commandLength;
    }

    /**
     * Returns the command id header field of this SMPP command.
     * 
     * @return the command id.
     */
    public int getCommandId() {
        return commandId;
    }

    /**
     * Returns the command status header field of this SMPP command.
     * 
     * @return the command status.
     */
    public int getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(int st) {
        commandStatus = st;
    }

    /**
     * Returns the sequence number header field of this SMPP command.
     * 
     * @return the sequence number.
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int no) {
        sequenceNumber = no;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("{SMPP_PDU length=" + commandLength + " id=0x" + Integer.toHexString(commandId)
                + " stat=0x" + Integer.toHexString(commandStatus) + " seq=" + sequenceNumber + " optional={");
        for (Enumeration<Integer> e = optionalParameters.keys(); e.hasMoreElements();) {
            Integer tag = e.nextElement();
            sb.append(" 0x" + Integer.toHexString(tag.intValue())).append("=").append(optionalParameters.get(tag));
        }
        return sb.toString() + "}}";
    }
}
