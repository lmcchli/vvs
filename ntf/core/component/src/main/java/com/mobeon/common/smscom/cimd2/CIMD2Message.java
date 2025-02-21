/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSComException;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.*;

/****************************************************************
 * CIMD2Message is a base class for CIMD2 Messages. It knows general things such
 * as constants used in the protocol, how to read a Message and how to parse the
 * header. See <I>CIMD Interface Specification</I> Issue 3a-0 en for details.
 ****************************************************************/
public class CIMD2Message
    implements Cimd2Constants {

    /**The connection this message instance is used with.*/
    protected CIMD2Com conn= null;

    /**The id of this Message.*/
    protected int operationCode;
    /**The number of the Message.*/
    protected int packetNumber;

    protected static final int bufSize= 1024;
    /**The bytes from the SMS-C are put in buffer*/
    protected byte[] buffer;
    
    /**Position in the buffer when parsing the Message.*/
    protected int pos= 0;

    protected EOFException eofe;


    /****************************************************************
     * Constructor.
     * @param conn the connection that uses this Message instance.
     */
    CIMD2Message(CIMD2Com conn) {
	this.conn= conn;
	eofe= new EOFException("EOF on connection to SMS-C");
	buffer= new byte[bufSize];
    }
    
    
    /****************************************************************
     *getBuffer returns the byte array with a raw CIMD2 message including STX
     *and ETX, in the form used for communicating with the SMSC. Extra bytes
     *after the ETX must be ignored. "From-SMSC" Messages use the default
     *implementation and just return the buffer. "To-SMSC" Messages create a new
     *buffer by encoding the message parameters into the CIMD2 format in a
     *Message-specific way. These new buffers do not have any bytes after ETX.
     *@return the read or created buffer, or null (if nothing has been read, or
     *there is not enough information in the Message yet to create a buffer).
     */
    public byte[] getBuffer() {
	return buffer;
    }

    
    /****************************************************************
     * Reads a Message from the supplied input stream and parses the header
     * parameters. In principle, it hangs until a real message is found, but in
     * reality it returns when there is an error to allow the caller to
     * reconnect.
     * @param is where to read from.
     * @throws SMSComException if the read or parsing fails.
     */
    public boolean read(InputStream is) throws IOException {
	int singleByte;
	int len= 0;

	//Only data between STX and ETX is valid. There may be data outside
	//those delimiters, that shall be discarded.
	
        do { //Skip bytes until we find STX.
            singleByte= is.read();
            if (singleByte < 0) throw eofe;
        } while (singleByte != CIMD2_STX);
        
        buffer[len++]= (byte)(singleByte & 0xFF);
        
        do {
            singleByte= is.read();
            if (len < bufSize) { //All messages we care about are quite short
                if (singleByte < 0) throw eofe;
                buffer[len++]= (byte)(singleByte & 0xFF);
            }
        } while (singleByte != CIMD2_ETX);
	if (conn.getSpy() != null) {
            byte[] ba = new byte[len];
            for (int i = 0; i < len; i++) {
                ba[i] = buffer[i];
            }
            conn.getSpy().fromSMSC(ba);
        }
        if (buffer[len - 2] != CIMD2_TAB) {
            //There is a check sum, ignore it
            buffer[len - 3]= CIMD2_ETX;
        }
        
	pos= 1; //Reset parse position to beginning
	operationCode= getInt(CIMD2Message.CIMD2_COLON);
	packetNumber= getInt(CIMD2Message.CIMD2_TAB);

	return (len < bufSize); //If message is too long, we lie and say there
				//is no message
    }
    

    /****************************************************************
     * This method parses the body part of a buffer from the SMS-C. The buffer
     * is already read by the read() method and the header is parsed. This
     * method is overridden for all Messages that have a body. The result is stored
     * in member variables and the values are accessed with get-functions
     * specific to each Message.
     */
    public void parseBody() {
	pos= HEADER_SIZE;
    }


    /****************************************************************
     * This method parses the body part of a buffer from the SMS-C. The buffer
     * is already read into another CIMD2Message, so the buffer and header
     * parameters are copied from it before the parsing starts.  <P> This method
     * is used to "change" Message type when bytes are read by a Message of an expected
     * type and the parsed header contains the id of another command.
     */
    public void parseBody(CIMD2Message msg) {
	buffer= msg.buffer;
	operationCode= msg.operationCode;
	packetNumber= msg.packetNumber;
	parseBody();
    }


    /****************************************************************
     * skipPast forwards the buffer position to after the delimiter.
     * @param delim The  delimiter.
     */
    public void skipPast(byte delim) {
	for (; buffer[pos] != delim; pos++) {
	    if (pos >= buffer.length) return;
	}
	pos++; //skip delimiter
    }

    
    /****************************************************************
     * getInt extracts an integer from a sequence of ASCII digits in the buffer.
     * @param delim The byte delimiting the integer.
     * @return the int extracted from the buffer
     */
    public int getInt(byte delim) {
	int val= 0;
	for (; buffer[pos] != delim; pos++) {
	    if (pos >= buffer.length) return Integer.MIN_VALUE;
	    if (val > 1000000000) return Integer.MIN_VALUE; 	    
	    val*= 10;
	    val+= buffer[pos] - '0';
	}
	pos++; //skip delimiter
	return val;
    }

    
    /****************************************************************
     * writeInt writes an integer to a Data Output as a sequence of ASCII digits.
     * @param buf The target byte array.
     * @param offs The offset in the array to start putting in.
     * @param len The number of bytes to put.
     * @param val The value to put in the array.
     */
    public void writeInt(DataOutput os, int size, int val, byte delim) throws IOException {
	switch (size) {
	case 8: os.write(val/10000000 + '0');
	    val%= 10000000;
	case 7: os.write(val/1000000 + '0');
	    val%= 1000000;
	case 6: os.write(val/100000 + '0');
	    val%= 100000;
	case 5: os.write(val/10000 + '0');
	    val%= 10000;
	case 4: os.write(val/1000 + '0');
	    val%= 1000;
	case 3: os.write(val/100 + '0');
	    val%= 100;
	case 2: os.write(val/10 + '0');
	    val%= 10;
	case 1: os.write(val + '0');
	}
	os.write(delim);
    }

	
    /****************************************************************
     * getNTS extracts a tab-terminated string from the buffer.
     * @return The string extracted from the buffer.
     */
    public String getTTS() {
	int i= pos;
	
	while (i < buffer.length && buffer[i] != CIMD2_TAB) i++; //Find tab terminator
	String s= new String(buffer, pos, i - pos);

	if (i < buffer.length) { //This is the normal case, otherwise the string
				 //ended without terminator
	    i++; //Skip null terminator
	}
	pos= i;
	return s;
    }

    
    /****************************************************************
     * writeNTS writes a null-terminated string to a DataOutput
     * @param do the DataOutput to write to.
     * @param s String to write.
     */
    public void writeTTS(DataOutput o, String s) throws IOException {
	o.writeBytes(s);
	o.writeByte(CIMD2_TAB);
    }

    
    /****************************************************************
     * Encodes and writes the header parameters to a DataOutput.
     * @param o the target DataOutput
     */
    public void writeHeader(DataOutput o) throws IOException {
	o.writeByte(CIMD2_STX);
	writeInt(o, 2, operationCode, CIMD2Message.CIMD2_COLON);
	writeInt(o, 3, packetNumber, CIMD2Message.CIMD2_TAB);
    }
    
    
    public void writeTrailer(DataOutput o) throws IOException {
	o.writeByte(CIMD2_ETX);
    }

    /**
     * writeAddress writes an SMS address to the output stream in CIMD2 format,
     * taking care of the NPI and TON aspects.
     */
    protected void writeAddress(DataOutput os, SMSAddress adr, SMSAddress org) throws IOException {
        writeInt(os, 3, CIMD2_DESTINATION_ADDRESS, CIMD2_COLON);
        if (adr.getTON() == 1) { //International
            os.writeByte('+');
        }
        os.writeBytes(adr.getNumber());
        os.write(CIMD2_TAB);

        if (org != null) {
            writeInt(os, 3, CIMD2_ORIGINATING_ADDRESS, CIMD2_COLON);
            if (org.getTON() == 1) { //International
                os.writeByte('+');
            }
            os.writeBytes(org.getNumber());
            os.write(CIMD2_TAB);
        }
    }

    /**
     * parseCimd2Address parses a CIMD2 format address string into an
     * SMSAddress.
     *@param adr - the address string.
     *@return the address in SMSAddress form.
     */
    protected SMSAddress parseCimd2Address(String adr) {
        return  adr.startsWith("+")
            ? (new SMSAddress(1, 1, adr.substring(1).trim()))
            : (new SMSAddress(2, 1, adr));
    }

    public int getOperationCode() {return operationCode;}

    public int getPacketNumber() {return packetNumber;}
    public void setPacketNumber(int no) {packetNumber= no;}

    public String toString() {
	return "{CIMD2Message code=" + operationCode +
	    " seq=" + packetNumber + "}";
    }
}
