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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * The user data header that can be add inside a message.
 * Each data can be added one or more time
 * 
 * @see 3GPP TS 23.040 (TP-User Data)
 * @author esebpar
 * @since MiO 2.0
 *
 */
public class UserDataHeader {

    private List<IE> ieList = new ArrayList<IE>();
    
    /**
     * Method to add and Information Element to the user data header
     * @param ie The Information Element to add
     */
    public void addIE(IE ie) {
        if(!ie.isRepeatable()){
            //The ie is not repeatable, need to remove the one already there than add the new one
            for(int i = 0; i < ieList.size(); ++i){
                if(ieList.get(i).getIEI() == ie.getIEI()){
                    ieList.remove(i);
                    break;
                }
            }
            
        }
        ieList.add(ie);
    }

    /**
     * Retrieve the user data header as a byte array
     * The first byte is the combine length of the IE than the byte array representing the IE
     * @return the user data header as a byte array
     */
    public byte[] getUDHBytes(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(getIELength());
            baos.write(getIEBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
    
    /**
     * Retrieve the information element as a byte array
     * @return the information element as a byte array
     */
    public byte[] getIEBytes(){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(IE ie : ieList){
            try {
                baos.write(ie.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toByteArray();
    }
    
    /**
     * Retrieve the length of the user data header
     * @return The length of the user data header
     */
    public int getUDHLength() {
        int len = getIELength();
        if(len > 0){
            return len + 1;
        }
        return 0;
    }
    
    /**
     * Retrieve the length of the user data header
     * @return The length of the user data header
     */
    public int getIELength() {
        int len = 0;
        for(IE ie : ieList){
           len += ie.getTotalLength();
        }
        return len;
    }
    
}
