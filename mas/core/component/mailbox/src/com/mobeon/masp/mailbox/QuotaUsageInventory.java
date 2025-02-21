/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.search.MessagePropertyCriteriaVisitor;
import com.mobeon.masp.util.criteria.Criteria;

import java.math.BigInteger;
import java.util.*;

/**
 * @author QHAST
 */
public abstract class QuotaUsageInventory<T extends IMailbox, C extends BaseContext> implements IQuotaUsageInventory {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(QuotaUsageInventory.class);

    public enum ByteUsageUnit {
        BYTES,KILOBYTES,MEGABYTES,GIGABYTES}

    private final Map<QuotaName, QuotaUsage> quotaUsageMap = new EnumMap<QuotaName, QuotaUsage>(QuotaName.class);

    private T mailbox;
    private C context;

    protected QuotaUsageInventory(T mailbox, C context) {
        this.mailbox = mailbox;
        this.context = context;
        for(QuotaName name : QuotaName.values()) {
            useQuota(name);
        }
    }

    public final QuotaUsage getQuota(QuotaName name)  {
        return quotaUsageMap.get(name);
    }

    public final QuotaUsage[] getQuotas() {
        return quotaUsageMap.values().toArray(new QuotaUsage[]{});
    }

    protected T getMailbox() {
        return mailbox;
    }

    protected C getContext() {
        return context;
    }

    protected final void setMessageUsage(QuotaName name, long usage) {
        QuotaUsage quotaUsage = useQuota(name);
        quotaUsage.messageUsage = usage < 0 ? -1 : usage;
    }

    private QuotaUsage useQuota(QuotaName name) {
        QuotaUsage quotaUsage = quotaUsageMap.get(name);
        if (quotaUsage == null) {
            quotaUsage = new QuotaUsage(name);
            quotaUsageMap.put(name, quotaUsage);
        }
        return quotaUsage;
    }

    protected abstract void init() throws MailboxException;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{mailbox=").append(mailbox);
        sb.append(",quotaUsage=").append(quotaUsageMap.values());
        sb.append("}");
        return sb.toString();
    }


}
