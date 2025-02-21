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
 *  This class represent the Information Element of the Application Port Addressing 16 bit address
 * @author esebpar
 * @since MiO 2.0
 * @see 3GPP TS 23.040 v6.7.0 (9.2.3.24.4 Application Port Addressing 16 bit address)
 */
public class ApplicationPort16Bits extends IE{
    
    public ApplicationPort16Bits(int originPort, int destinationPort){
        //The IEI of the Application Ports Addressing Scheme 16 bits is 0x05 
        iei = 0x05;
        //The length is 4 bytes
        data = new byte[4];
        //The data, see 3GPP TS 23.040 v6.7.0 (9.2.3.24.4 Application Port Addressing 16 bit address)
        data[0] = (byte) ((destinationPort >> 8) & 0x00ff);
        data[1] = (byte) (destinationPort & 0x00ff);
        data[2] = (byte) ((originPort >> 8) & 0x00ff);
        data[3] = (byte) (originPort & 0x00ff);;
    }
    
    /**
     * The Application Port Addressing 16 bit address is not repeatable
     */
    @Override
    public boolean isRepeatable(){
        return false;
    }
}
