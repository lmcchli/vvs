/**
COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.

THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
INC.  ANY COPY MUST INCLUDE THE ABOVE COPYRIGHT NOTICE AS
WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
TO ANY OTHER PERSON OR ENTITY.
TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
*/
package com.mobeon.common.smscom.udh;

/**
 * 
 * The basic class that define the Information Element (IE)
 * 
 * All IE are base as TLV (Tag, Length, Value)
 * @see 3GPP TS 23.040 (TP-User Data)
 * 
 * @author esebpar
 * @since MiO 2.0
 *
 */
public abstract class IE {
    protected byte iei = 0;
    protected byte[] data = null;
    
    /**
     * Get the Information Element Identifier (IEI)
     * @return the Information Element Identifier (IEI)
     * @see 3GPP TS 23.040 (TP-User Data)
     */
    public byte getIEI(){
        return iei;
    }
    
    /**
     * Get the bytes value of this Information Element (IE)
     * @return The bytes value of this Information Element (IE)
     * @see 3GPP TS 23.040 (TP-User Data)
     */
    public byte[] getBytes(){
        int length = getTotalLength();
        if(length == 0){
            return new byte[0];
        }
        
        byte[] bytes = new byte[length];
        bytes[0] = iei;
        bytes[1] = (byte) (data.length & 0xff);
        for(int i = 0; i < data.length; ++i){
            bytes[i + 2] = data[i];
        }
        return bytes;
    }
    
    /**
     * Get the length of this Information Element (IE)
     * @return The length of this Information Element (IE)
     * @see 3GPP TS 23.040 (TP-User Data)
     */
    public int getTotalLength(){
        if(data != null){
            return data.length + 2;
        }
        return 0;
    }
    
    /**
     * Look if the Information Element can be repeat.
     * @return true if the IE is repeatable
     * @see 3GPP TS 23.040 (TP-User Data)
     */
    public abstract boolean isRepeatable();
    
}
