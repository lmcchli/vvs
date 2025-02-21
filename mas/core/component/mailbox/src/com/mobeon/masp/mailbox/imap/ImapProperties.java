/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.imap;

import com.mobeon.masp.mailbox.QuotaUsageInventory.ByteUsageUnit;
import com.mobeon.masp.mailbox.imap.ImapQuotaUsageInventory.TotalQuotaNameTemplate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Håkan Stolt
 */
public class ImapProperties implements Serializable {

    private String quotaRootMailboxName = "inbox";
    private String totalQuotaNameTemplateString = TotalQuotaNameTemplate.DEFAULT_TEMPLATE_STRING;
    private String byteUsageQuotaRootResourceName = "STORAGE";
    private ByteUsageUnit byteUsageQuotaRootResourceUnit = ByteUsageUnit.KILOBYTES;
    private String messageUsageQuotaRootResourceName = "MESSAGE";
    private String[] messageUsageFolderNames = new String[]{"inbox"};//Interim "fallback" solution, should be removed when MS supports "MessageUsage" in IMAP QuotaRoot command.

    public String getQuotaRootMailboxName() {
        return quotaRootMailboxName;
    }

    public void setQuotaRootMailboxName(String quotaRootMailboxName) {
        this.quotaRootMailboxName = quotaRootMailboxName;
    }

    public String getTotalQuotaNameTemplate() {
        return totalQuotaNameTemplateString;
    }

    public void setTotalQuotaNameTemplate(String totalQuotaNameTemplate) {
        this.totalQuotaNameTemplateString = totalQuotaNameTemplate;
    }

    public String getByteUsageQuotaRootResourceName() {
        return byteUsageQuotaRootResourceName;
    }

    public void setByteUsageQuotaRootResourceName(String byteUsageQuotaRootResourceName) {
        this.byteUsageQuotaRootResourceName = byteUsageQuotaRootResourceName;
    }

    public ByteUsageUnit getByteUsageQuotaRootResourceUnit() {
        return byteUsageQuotaRootResourceUnit;
    }

    public void setByteUsageQuotaRootResourceUnit(ByteUsageUnit byteUsageQuotaRootResourceUnit) {
        this.byteUsageQuotaRootResourceUnit = byteUsageQuotaRootResourceUnit;
    }

    public String getMessageUsageQuotaRootResourceName() {
        return messageUsageQuotaRootResourceName;
    }

    public void setMessageUsageQuotaRootResourceName(String messageUsageQuotaRootResourceName) {
        this.messageUsageQuotaRootResourceName = messageUsageQuotaRootResourceName;
    }

    //Interim "fallback" solution, should be removed when MS supports "MessageUsage" in IMAP QuotaRoot command.
    public String[] getMessageUsageFolderNames() {
        return messageUsageFolderNames;
    }

    //Interim "fallback" solution, should be removed when MS supports "MessageUsage" in IMAP QuotaRoot command.
    public void setMessageUsageFolderNames(String[] messageUsageFolderNames) {
        this.messageUsageFolderNames = messageUsageFolderNames;
    }

    //Interim "fallback" solution, should be removed when MS supports "MessageUsage" in IMAP QuotaRoot command.
    public void setMessageUsageFolderNames(Set<String> messageUsageFolderNames) {
        setMessageUsageFolderNames(messageUsageFolderNames.toArray(new String[0]));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ImapProperties:{");
        sb.append("quotaRootMailboxName=").append(quotaRootMailboxName);
        sb.append(",totalQuotaNameTemplate=").append(totalQuotaNameTemplateString);
        sb.append(",messageUsageQuotaRootResourceName=").append(messageUsageQuotaRootResourceName);
        sb.append(",byteUsageQuotaRootResourceName=").append(byteUsageQuotaRootResourceName);
        sb.append(",byteUsageQuotaRootResourceUnit=").append(byteUsageQuotaRootResourceUnit);
        sb.append(",messageUsageFolderNames=").append(Arrays.asList(messageUsageFolderNames));//Interim "fallback" solution, should be removed when MS supports "MessageUsage" in IMAP QuotaRoot command.
        sb.append("}");
        return sb.toString();
    }

}
