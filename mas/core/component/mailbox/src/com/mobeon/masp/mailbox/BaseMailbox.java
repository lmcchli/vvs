/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Arrays;

/**
 * @author QHAST
 */
public abstract class BaseMailbox<C extends BaseContext> {

    private ILogger LOGGER = ILoggerFactory.getILogger(BaseMailbox.class);

    private C context;

    protected BaseMailbox(C context) {
        this.context = context;
    }

    protected C getContext() {
        return context;
    }

    protected abstract QuotaUsageInventory createQuotaUsageInventory();

    public final IQuotaUsageInventory getQuotaUsageInventory() throws MailboxException {
    	Object perf = null;
        try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("BaseMailbox.getQuotaUsageInventory()");
	        }
	        if (LOGGER.isInfoEnabled()) LOGGER.info("getQuotaUsageInventory()");
	        if (LOGGER.isDebugEnabled()) LOGGER.debug("Creating QuotaUsageInventory for " + this);
	        QuotaUsageInventory quotaUsageInventory = createQuotaUsageInventory();
	        quotaUsageInventory.init();
	        if (LOGGER.isInfoEnabled()) LOGGER.info("getQuotaUsageInventory() returns " + quotaUsageInventory);
	        return quotaUsageInventory;
        } finally {
        	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
        }
    }

}
