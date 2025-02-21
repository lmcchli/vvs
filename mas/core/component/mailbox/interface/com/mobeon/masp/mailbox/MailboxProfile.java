/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import java.io.Serializable;

import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;

/**
 * @author Håkan Stolt
 */
public class MailboxProfile implements Serializable {
    private String accountId;
    private String accountPassword;
    private String emailAddress;
    
    public MailboxProfile(String accountId, String accountPassword, String emailAddress) {
        this.accountId = accountId;
        this.accountPassword = accountPassword;
        this.emailAddress = emailAddress;        
    }


    public MailboxProfile() {
        this(null,null,null);        
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }    
   

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{accountId=").append(accountId);
        sb.append(",accountPassword=");
        if(accountPassword!=null) {            
            sb.append("*****");
        } else {
            sb.append(accountPassword);
        }
        sb.append(",emailAddress=").append(emailAddress);
        sb.append("}");
        return sb.toString();
    }


}
