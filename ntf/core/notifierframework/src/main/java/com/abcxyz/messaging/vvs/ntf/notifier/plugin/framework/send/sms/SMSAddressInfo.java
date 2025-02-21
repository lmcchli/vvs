/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2015.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms;

/********************************************************************************
 * SMSAddressInfo carries complete addressing information for an SMS message. The
 * content for address fields is specified by ETSI TS 100 901 <I>Technical
 * Realization of the Short Message Service (SMS)</I>
 ********************************************************************************/
public class SMSAddressInfo {
    /**Type of number, e.g. international, national, alphanumeric.*/
    protected int ton;
    /**Numbering plan indicator, e.g. ISDN, land mobile, internet*/
    protected int npi;
    /**The number (in a general sense, i.e. not necessarily a numeric
       value). This is the number having a type, belonging to a numbering
       plan.*/
    protected String address;


    /****************************************************************
     * Constructs a complete SMSAddress.
     * @param ton - type of number
     * @param npi - numbering plan indicator
     * @param address - telephone number or other.
     */
    public SMSAddressInfo(int ton, int npi, String address) {
        this.ton = ton;
        this.npi = npi;
        this.address = address;
        
        this.address = formatNumber(address);
        
    }

    
    private String formatNumber(String number){
        String tempNumber = number;
        if(number.toLowerCase().startsWith("tel:")){
            tempNumber = number.toLowerCase().replaceAll("tel:", "");
        }
        // Remove the leading '+' if present 
        return stripLeadingPlus(tempNumber);
    }

    /**
     * Will strip the leading '+' of from an international number in E.164 (ISDN) format number if present.
     * ton == 1 means an international number
     * npi == 1 means a number in E.164 (ISDN) format (see SMPP specification)
     * If no leading '+' is present, the received number is returned as is.
     * @param s - Phone number passed as a String
     * @return a phone number as a String without a leading plus.
     */
    private String stripLeadingPlus(String s) {
        if(ton == 1 && npi == 1) {
            if (s.startsWith("+")) {
                return s.substring(1);
            } else {
                return s;
            }
        }
        return s;
    }
    
    /**getTON gets the TON for an address. @return type of number*/
    public int getTON() { return ton; }
    /**setTON sets the TON for an address. @param ton The new type of number*/
    public void setTON(int ton) { this.ton = ton; }

    /**getNPI gets the NPI for an address. @return numbering plan indicator*/
    public int getNPI() { return npi; }
    /**setNPI sets the NPI for an address. @param npi The new numbering plan indicator*/
    public void setNPI(int npi) { this.npi = npi; }

    /**getNumber gets the number for an address. @return number*/
    public String getAddress() { return address; }

    /**setNumber sets the number for an address. @param number The new number*/
    public void setAddress(String number) { this.address = number; }

    /****************************************************************
     * Creates a printable version of the address information
     * @return A string of the form "SMSAddress &#60;number String&#62;,x,x" with the address or
     *         SMSAddress NONE,x,x" if string is empty.
     * information.
     */
    public String toString() {
        if (!address.isEmpty()) {
            return ("{SMSAddress " + address + "," + ton + "," + npi + "}");
        } else {
            return ("{SMSAddress NONE," + ton + "," + npi + "}");
        }
    }
}



