package com.mobeon.smsc.smpp;

import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import com.mobeon.smsc.interfaces.SmppConstants;
import java.util.*;

/**
 * SMPP_PDU is a base class for SMPPCom. It knows general things such as
 * constants used in the protocol, how to read a PDU and how to parse the
 * header. See <I>Short Message Peer to Peer Protocol Specification</I> v3.4 for
 * details.
 */
public class SMPP_PDU  implements SmppConstants { 

    /**Version number for SMPP version 3.4.*/
    public static final int SMPP_VERSION= 0x34;

    /**The size of the SMPP header.*/
    public static final int HEADER_SIZE=
	4 + //command_length
	4 + //command_id
	4 + //command_status
	4;  //sequence_number

    /**The number of bytes in the PDU.*/
    protected int commandLength;
    /**The id of this PDU.*/
    protected int commandId;
    /**The status code in response PDUs.*/
    protected int commandStatus;
    /**The number of the PDU. This number starts from 1, and is incremented for
       each request PDU.*/
    protected int sequenceNumber;

    /**The bytes read from the SMS-C. Bytes <I>to</I> the SMS-C are returned
       immediately by the getBuffer method and not stored here.*/
    protected byte[] buffer= null;
    /**Header is a place to read the PDU size, and is also used for the entire
       buffer for PDUs that have no body.*/
    protected byte[] header= new byte[HEADER_SIZE];
    
    /**Position in the buffer when parsing the PDU.*/
    protected int pos= 0;

    /**If there are optional parameters, they are stored here when they buffer
       is parsed.*/
    protected Hashtable optionalParameters; //Objects (Integer, String, byte[])
					    //keyed by int(tag)

    protected EOFException eofe;


    /**
     * Constructor.
     */
    public SMPP_PDU() {
	optionalParameters= new Hashtable();
	eofe= new EOFException("EOF on connection to ESME");
	init();
    }
    
    
    /****************************************************************
     *getBuffer returns a byte array with a raw SMPP message in the form used
     *for communicating with the SMSC. "From-SMSC" PDUs use the default
     *implementation and just return the buffer. "To-SMSC" PDUs create a new
     *buffer by encoding the message parameters into the SMPP format in a
     *PDU-specific way.
     *@return the read or created buffer, or null (if nothing has been read, or
     *there is not enough information in the PDU yet to create a buffer).
     */
    public byte[] getBuffer() {
	return buffer;
    }

    
    /****************************************************************
     * Reads a PDU from the supplied input stream and parses the header
     * parameters.
     * @param is where to read from.
     * @throws SMSComException if the read or parsing fails.
     */
    public boolean read(InputStream is) throws IOException {
	int len;

	init();
	try {
	    len= is.read(header, 0, 4); //Read the PDU length
	} catch (InterruptedIOException e) { //Nothing to read, this is normal
	    return false;
	}
	if (len < 0) throw eofe;
	if (len < 4) return false; //This is probably a normal "nothing to read"
	
	buffer= header; //Parsing always uses buffer, so set buffer to
	//reference header
	pos= 0; //Reset parse position to beginning
	commandLength= getInt(4);
	if (commandLength < 16 || commandLength > 1024) throw new IOException("Impossible PDU length " + commandLength);
	
	if (commandLength > HEADER_SIZE) { //We need a new larger buffer
	    buffer= new byte[commandLength];
	    //Put back the length in the new buffer
	    System.arraycopy(header, 0, buffer, 0, 4);
	} else {
	    //Header-only PDU so we can leave buffer referencing the header buffer
	}
	len= is.read(buffer, 4, commandLength - 4); //Read the rest of the PDU
	if (len < 0) throw eofe;
	if (len < commandLength - 4) {
	    throw new InterruptedIOException("SMPPCom failed to read complete PDU");
	}		
	
	pos= 4; //Start after the length field
	commandId= getInt(4);
	commandStatus= getInt(4);
	sequenceNumber= getInt(4);
	return true;
    }
    

    /****************************************************************
     * This method parses the body part of a buffer from the SMS-C. The buffer
     * is already read by the read() method and the header is parsed. This
     * method is overridden for all PDUs that have a body. The result is stored
     * in member variables and the values are accessed with get-functions
     * specific to each PDU.
     */
    public Object parseBody() {
	pos= 4*4;
        return null;
    }
     
    /****************************************************************
     * This method parses the body part of a buffer from the SMS-C. The buffer
     * is already read into another SMPP_PDU, so the buffer and header
     * parameters are copied from it before the parsing starts.  <P> This method
     * is used to "change" PDU type when bytes are read by a PDU of an expected
     * type and the parsed header contains the id of another command.
     */
    public Object parseBody(SMPP_PDU pdu) {
	buffer= pdu.buffer;
	commandLength= pdu.commandLength;
	commandId= pdu.commandId;
	commandStatus= pdu.commandStatus;
	sequenceNumber= pdu.sequenceNumber;
	return parseBody();
    }
    
    /****************************************************************
     * putInt puts an integer into a byte array.
     * @param buf The target byte array.
     * @param offs The offset in the array to start putting in.
     * @param len The number of bytes to put.
     * @param val The value to put in the array.
     */
    public void putInt(byte[] buf, int offs, int len, int val) {
	for (int i= offs + len - 1; i >= offs; i--) {
	    buf[i]= (byte)(val & 0xFF);
	    val >>= 8;
	}
    }

	
    /****************************************************************
     * getInt extracts an integer from the buffer.
     * @param size The number of bytes in the integer (1, 2 or 4)
     * @return the int extracted from the buffer
     */
    public int getInt(int size) {
	int val= 0;
	int end= pos + size;
	if (end > buffer.length) end= buffer.length;
	for (; pos < end ; pos++) {
	    val <<= 8;
	    val|= ((int)(buffer[pos]) & 0xFF); //Get rid of negative bytes
	}
	return val;
    }

    
    /****************************************************************
     * getNTS extracts a null terminated string from the buffer.
     * @return The string extracted from the buffer.
     */
    public String getNTS() {
	int i= pos;

	while (i < buffer.length && buffer[i] != 0) i++; //Find null terminator
	String s= new String(buffer, pos, i - pos);

	if (i < buffer.length) { //This is the normal case, otherwise the string
				 //ended without terminator
	    i++; //Skip null terminator
	}
	pos= i;

	return s;
    }
   
    /****************************************************************
     * getString extracts a string of specified length from the buffer.
     * @return The string extracted from the buffer.
     */
    public String getString(int length) {
	String s = new String(buffer, pos, length);
	pos = pos+length;
        return s;
    }
 
    /****************************************************************
     * writeNTS writes a null-terminated string to a DataOutput
     * @param do the DataOutput to write to.
     * @param s String to write.
     */
    public void writeNTS(DataOutput o, String s) throws IOException {
	o.writeBytes(s);
	o.writeByte(0);
    }

    
    /****************************************************************
     * getOptionalParameters parses the rest of buffer and returns all optional
     * parameters.
     */
    public void getOptionalParameters() {
	if (pos < buffer.length) {
	    while (pos + 4 < buffer.length) { //Stop when there is not room for
					      //another TLV in the remaining buffer
		int tag= getInt(2);
		int len= getInt(2);
		
		switch (tag) {
		case SMPPTAG_SC_INTERFACE_VERSION:
		    optionalParameters.put(new Integer(tag), new Integer(getInt(1)));
		    break;
		default:
                    optionalParameters.put(new Integer(tag), new Integer(getInt(1)));
		}
	    }
	}
    }


    /****************************************************************
     * init resets the PDU to the initial state, ready for reading and parsing a
     * new buffer.
     */
    protected void init() {
	buffer= null;
	commandLength= 0;
	optionalParameters.clear();
	pos= 0;
    }


    /****************************************************************
     * Encodes the header parameters into a byte array. This method is used for
     * header-only PDUs.
     * @param buf the target byte array.
     */
    public void putHeader(byte[] buf) {
	putInt(buf, 0, 4, commandLength);
	putInt(buf, 4, 4, commandId);
	putInt(buf, 8, 4, commandStatus);
	putInt(buf, 12, 4, sequenceNumber);
    }


    /****************************************************************
     * Encodes and writes the header parameters to a DataOutput.
     * @param o the target DataOutput
     */
    public void writeHeader(DataOutput o) throws IOException {
	o.writeInt(commandLength);
	o.writeInt(commandId);
	o.writeInt(commandStatus);
	o.writeInt(sequenceNumber);
    }
    
    
    public int getCommandLength() {return commandLength;}
    public int getCommandId() {return commandId;}
    public int getCommandStatus() {return commandStatus;}
    public void setCommandStatus(int st) {commandStatus= st;}
    public int getSequenceNumber() {return sequenceNumber;}
    public void setSequenceNumber(int no) {sequenceNumber= no;}
    public String toString() {
	return "{SMPP_PDU length=" + commandLength +
	    " id=0x" + Integer.toHexString(commandId) +
	    " stat=0x" + Integer.toHexString(commandStatus) +
	    " seq=" + sequenceNumber;
    }
}
