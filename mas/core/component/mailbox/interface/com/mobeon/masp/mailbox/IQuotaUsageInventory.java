/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * This class represents a snapshot view of the current Quota usage in the mailbox.
 * @author QHAST
 */
public interface IQuotaUsageInventory {
    /**
     * Tries to fetch the named QuotaUsage object in this inventory.
     * If the named QuotaUsage object has been calculated due it's not
     * configured an exception will be thrown.
     * @param name Quota name.
     * @return the named QuotaUsage object.
     */
    public QuotaUsage getQuota(QuotaName name);

    /**
     * @return all QuotaUsage objects in the inventory.
     */
    public QuotaUsage[] getQuotas();
}
