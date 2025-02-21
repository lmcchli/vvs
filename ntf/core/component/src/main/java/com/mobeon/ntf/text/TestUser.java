/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.text;

import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.userinfo.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestUser extends UserInfoAdapter implements Constants {
    Vector devices = new Vector();
    String fullId;
    String mail;
    NotificationFilter filter;
    String mmsCenterId;
    int notifExpTime;
    String notifNumber;
    int numberingPlan;
    String preferredDateFormat;
    String preferredTimeFormat;
    String smscId;
    int typeOfNumber;
    boolean[] businessDays = new boolean[8];
    int businessDayStart;
    int businessDayEnd;
    boolean mailboxDelivery;
    boolean administrator;
    String preferredLanguage;
    String login;
    String wapGatewayId;
    boolean multilineUser;
    boolean outdialUser;
    boolean mwiUser;
    boolean[] mailTypes = new boolean[NTF_DEPOSIT_TYPE_MAX];
    String[] services;
    String pnc;
    String userNtd;
    String usersDate;
    String usersTime;
    String cosName = null;

    private void setDefault() {
        int i;

        fullId = "uid=test01, ou=test, o=test";
        mail = "test01@test.test.se";
        telephoneNumber = "9999901";
        filter = null;
        mmsCenterId = "mms01";
        notifExpTime = 1;
        notifNumber = "9999901";
        numberingPlan = 1;
        preferredDateFormat = "yyyy-mm-dd";
        preferredTimeFormat = "24";
        smscId = "smppjunit";
        typeOfNumber = 1;
        mailboxDelivery = true;
        administrator = false;
        preferredLanguage = "en";
        login = "test01";
        wapGatewayId = "wap01";
        multilineUser = false;
        outdialUser = true;
        mwiUser = true;
        pnc = "123456789+++1111++33333+@#H";
        userNtd = "";
        usersDate = "";
        usersTime = "";
        businessDayStart = 800;
        businessDayEnd = 1700;
        cosName = null;
        for (i = Calendar.MONDAY; i <= Calendar.FRIDAY; businessDays[i++] = true);
        for (i = Calendar.SATURDAY; i <= Calendar.SUNDAY; businessDays[i++] = false);
        for (i = 0; i < NTF_DEPOSIT_TYPE_MAX; mailTypes[i++] = true);
    }

    public TestUser() {
        super();
        setDefault();
    }

    public void addDeliveryProfile(String number, String type) {
    }

    public void clearDevices() {
        devices = new Vector();
    }
    
    public void setTON(int ton){
        typeOfNumber = ton;
    }
    
    public void setNPI(int npi){
        typeOfNumber = npi;
    }
    
    public void setTelephoneNumber(String number) {
        telephoneNumber = number;
    }

    public void setFilter(NotificationFilter f) {
        filter = f;
    }

    public void setPreferredLanguage(String lang)
    {
        preferredLanguage = lang;
    }

    public void setPreferredTimeFormat(String format) {
        this.preferredTimeFormat = format;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void disableNotifTypes(String types) {
        userNtd = types;
        Vector olddev = devices;
        clearDevices();
    }

    public void setMultilineUser(boolean f) {
        multilineUser = f;
    }

    public void setNotifNumber(String n) {
        notifNumber = n;
    }

    public String getFullId() {
        return fullId;
    }

    public String getMail() {
        return mail;
    }

    public String getDeliveryProfiles(String telephonenumber) {
        return "";
    }

    public Vector getDevices() {
        return devices;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public NotificationFilter getFilter() {
        return filter;
    }

    public String getMmsCenterId() {
        return mmsCenterId;
    }

    public int getNotifExpTime() {
        return notifExpTime;
    }

    public String getNotifNumber() {
        return notifNumber;
    }

    public int getNumberingPlan() {
        return numberingPlan;
    }

    public String getPreferredDateFormat() {
        return preferredDateFormat;
    }

    public String getPreferredTimeFormat() {
        return preferredTimeFormat;
    }


    public String getUsersDate(Date d) {
        String dateString;
        if (d == null) {
            return "";
        }

        SimpleDateFormat fmt=null;
        fmt = new SimpleDateFormat(preferredDateFormat.replace('m', 'M'));
        String tz=null;
        tz=getTimeZone();


        if (tz != null){
            TimeZone zone=TimeZone.getTimeZone(tz);;
            fmt.setTimeZone(zone);
        }

        return fmt.format(d);
    }

    public String getUsersTime(Date d) {
        if (d == null) {
            return "";
        }

        SimpleDateFormat fmt=null;
        if ("12".equals(preferredTimeFormat)) {
            fmt = new SimpleDateFormat("hh:mm a");
        } else { //24
            fmt = new SimpleDateFormat("HH:mm");
        }

        String tz=null;
        tz=getTimeZone();


        if (tz != null){
            TimeZone zone=TimeZone.getTimeZone(tz);;
            fmt.setTimeZone(zone);
        }

            return fmt.format(d);
    }

    public String getSmscId() {
        return smscId;
    }

    public int getTypeOfNumber() {
        return typeOfNumber;
    }

    public boolean isBusinessTime(Calendar cal) {
        //Hardcoded business times Mon-Fri, 8-17
        return businessDays[cal.get(cal.DAY_OF_WEEK)]
            && cal.get(cal.HOUR_OF_DAY)*100 + cal.get(cal.MINUTE) >= businessDayStart
            && cal.get(cal.HOUR_OF_DAY)*100 + cal.get(cal.MINUTE) < businessDayEnd;
    }

    public boolean isMailboxDelivery() {
        return mailboxDelivery;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getLogin() {
        return login;
    }

    public String getWapGatewayId() {
        return wapGatewayId;
    }

    public boolean isMultilineUser() {
        return multilineUser;
    }

    public boolean isOutdialUser() {
        return outdialUser;
    }

    public boolean isMwiUser() {
        return mwiUser;
    }

    public String getPnc() {
        return pnc;
    }

    public String getUserNtd() {
        return null;
    }

    public void setPnc(String newPnc) {
        pnc = newPnc;
    }

    public NotifState isNotifTypeDisabledOnUser(int type) {
        switch (type) {
            case NTF_PAG:
                if( userNtd.indexOf("PAG") != -1 )
                    return NotifState.DISABLED;
                else
                    return NotifState.ENABLED;

            default:
                return NotifState.FAILED;
        }

    }

    public boolean hasMailType(int type) {
        return mailTypes[type];
    }

    public String[] getServices() {
        return services;
    }

    public int getValidity_temporaryGreetingOnReminder() {
        return 0;
    }

    public void setCosName(String name) {
        cosName = name;
    }

    public String getCosName() {
        return cosName;
    }
}
