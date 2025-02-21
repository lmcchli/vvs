/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.SMSAddressInfo;


/****************************************************************
 * SMSAddress carries complete addressing information for an SMS message. The
 * content for address fields is specified by ETSI TS 100 901 <I>Technical
 * Realization of the Short Message Service (SMS)</I>
 ****************************************************************/
public class SMSAddress {
    /**Type of number, e.g. international, national, alphanumeric.*/
    public int ton;
    /**Numbering plan indicator, e.g. ISDN, land mobile, internet*/
    public int npi;
    /**The number (in a general sense, i.e. not necessarily a numeric
       value). This is the number having a type, belonging to a numbering
       plan.*/
    public String nbr;


    /****************************************************************
     * Constructs a complete SMSAddress.
     * @param ton - type of number
     * @param npi - numbering plan indicator
     * @param number - telephone number
     */
    public SMSAddress(int ton, int npi, String number) {
        this.ton = ton;
        this.npi = npi;
        this.nbr = number;
        
        this.nbr = formatNumber(number);
        
    }
    
    /**
     * @param address - a framework address equivalent to this one.
     * Constructs an SMSAddress based upon a framework SMSAddressInfo/
     * Basically a convenience function to convert between the two types.
     */
    public SMSAddress( SMSAddressInfo address) {
        this.ton = address.getTON();
        this.npi = address.getNPI() ;
        this.nbr = address.getAddress();
        
    }

    /**
     * Constructs an SMSAddress by parsing a string of the form
     * ton,npi,number. If TON and NPI are absent, they are taken from the
     * parameters smesourceaddresston and smesourceaddresnpi.
     *@param name - the name of the parameter specifying the address.
     *@return an SMSAddress with the configured number.
     *@throws SMSComDataException if name has not 2 commas or if TON or NPI are
     * not integers 0-256.
     */
    public SMSAddress(String adr) throws SMSComDataException {
        int first = adr.indexOf(",");
        int last = adr.lastIndexOf(",");

        if (first < 0 //TON and NPI missing
            || first == last //TON or NPI missing
            || adr.indexOf(",", first + 1) != last) { //3 or more commas
            throw new SMSComDataException("SMS address string \""
                                          + adr
                                          + "\" does not have format TON,NPI,number");
        }
        try {
            ton = Integer.parseInt(adr.substring(0, first).trim());
            npi = Integer.parseInt(adr.substring(first + 1, last).trim());
            nbr = adr.substring(last + 1).trim();

            // Remove the leading '+' if present  
            nbr = formatNumber(nbr);

        } catch (NumberFormatException e) {
            throw new SMSComDataException("NPI or TON in SMS address string \""
                                          + adr
                                          + "\" are not numeric");
        }
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
    public String getNumber() { return nbr; }

    /**setNumber sets the number for an address. @param number The new number*/
    public void setNumber(String number) { this.nbr = number; }

    /****************************************************************
     * Creates a printable version of the address information
     * @return A string of the form "SMSAddress <number String>,x,x" with the address or
     *         SMSAddress NONE,x,x" if string is empty.
     * information.
     */
    public String toString() {
        if (!nbr.isEmpty()) {
            return ("{SMSAddress " + nbr + "," + ton + "," + npi + "}");
        } else {
            return ("{SMSAddress NONE," + ton + "," + npi + "}");
        }
    }
}


