/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.util.regex.Pattern;

/**
 * @author Håkan Stolt
 */
public class Address {

    public static final String COMMON_NAME_PATTERN_STRING = "[^\\s\"()]+(\\s+[^\\s\"()]+)*";
    public static final Pattern COMMON_NAME_PATTERN = Pattern.compile("^"+COMMON_NAME_PATTERN_STRING+"$");

    public static final String PHONE_NUMBER_PATTERN_STRING = "\\+?[0-9]+";
    public static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^"+PHONE_NUMBER_PATTERN_STRING+"$");

    public static final String EMAILADDRESS_PATTERN_STRING = "([a-zA-Z0-9=_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})";
    public static final Pattern EMAILADDRESS_PATTERN = Pattern.compile("^"+EMAILADDRESS_PATTERN_STRING+"$");

    public static final String SENDER_ADDRESS_PATTERN_STRING =
            "^(((\""+COMMON_NAME_PATTERN_STRING+"(\\s+\\("+PHONE_NUMBER_PATTERN_STRING+"\\))?\")|("+COMMON_NAME_PATTERN_STRING+"))\\s+)?<("+EMAILADDRESS_PATTERN_STRING+")?>$";
    public static final Pattern SENDER_ADDRESS_PATTERN = Pattern.compile(SENDER_ADDRESS_PATTERN_STRING);

    private String commonName;
    private String phoneNumber;
    private String emailAddress;

    public Address(String commonName, String phoneNumber, String emailAddress) {
        this.commonName = commonName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public Address() {
        this(null,null,null);
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String stringRepresentation() {
        StringBuilder sb = new StringBuilder();
        if(commonName != null && commonName.length()>0) {
            sb.append(commonName);
        }
        if(phoneNumber != null && phoneNumber.length()>0) {
            if(sb.length() == 0) {
                sb.append(phoneNumber);
            } else {
                sb.insert(0,'"');
                sb.append(" (").append(phoneNumber).append(")\"");

            }
        }
        sb.append(" <");
        if(emailAddress != null && EMAILADDRESS_PATTERN.matcher(emailAddress).matches()){
            sb.append(emailAddress);
        }
        sb.append(">");
        return sb.toString().trim();
    }

     public static void validate(String stringRepresentation) throws AddressParseException {
         if(stringRepresentation == null) {
            throw new IllegalArgumentException("Address stringRepresentation cannot be null!");
        }
        if(!SENDER_ADDRESS_PATTERN.matcher(stringRepresentation.trim()).matches()) {
            throw new AddressParseException(stringRepresentation);
        }
     }

    public static Address parse(String stringRepresentation) throws AddressParseException {
        validate(stringRepresentation);
        String s = stringRepresentation.trim();
        Address result = new Address();
        int emailStart = s.indexOf('<');
        int emailEnd = s.indexOf('>');
        if(emailEnd-emailStart>1) {
            result.setEmailAddress(s.substring(emailStart+1,emailEnd).trim());
        }
        int phoneNumberStart = s.indexOf('(');
        int phoneNumberEnd = s.indexOf(')');
        if(phoneNumberStart>0) {
            result.setPhoneNumber(s.substring(phoneNumberStart+1,phoneNumberEnd).trim());
            result.setCommonName(s.substring(1,phoneNumberStart).trim());
        } else {
            if(s.startsWith("\"")){
                String cName = s.substring(0,emailStart).trim();
                cName = cName.substring(1,cName.length()-1);
                result.setCommonName(cName);
            } else {
                result.setCommonName(s.substring(0,emailStart).trim());
            }
            if(PHONE_NUMBER_PATTERN.matcher(result.getCommonName()).matches()) {
                result.setPhoneNumber(result.getCommonName());
                result.setCommonName(null);
            }
        }

        return result;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(commonName);
        sb.append(" (");
        sb.append(phoneNumber);
        sb.append(") <");
        sb.append(emailAddress);
        sb.append(">");
        return sb.toString().trim();
    }


}

