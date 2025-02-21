/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Date;

/**
 * @author qhast
 */
public class BaseStoredMessage<C extends BaseContext> extends BaseMailboxMessage<C> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(BaseStoredMessage.class);

    protected Date receivedDate;
    protected boolean forward;
    protected DeliveryStatus deliveryStatus;
    protected StoredMessageState state;

    public BaseStoredMessage(C context) {
        super(context);        
    }

    /**
     * Get the date and time when the message arrived in users mailbox.
     *
     * @return the received date.
     */
    public Date getReceivedDate() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getReceivedDate() returns " + receivedDate);
        return receivedDate;
    }

    /**
     * Indicates if this message is a forwarded message.
     *
     * @return true if this message includes another message forwarded through calling {@link IStoredMessage#forward()}.
     */
    public boolean isForward() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("isForward() returns " + forward);
        return forward;
    }

    /**
     * Indicates if this message is a delivery report.
     *
     * @return true if this message includes a delivery report.
     */
    public boolean isDeliveryReport() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("isDeliveryReport() returns " + (deliveryStatus != null));
        return deliveryStatus != null;
    }

    /**
     * Gets the mailbox message state.
     *
     * @return state
     */
    public StoredMessageState getState() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getState() returns " + state);
        return state;
    }

    /**
     * Sets the mailbox message state.
     *
     * @param state
     */
    public void setState(StoredMessageState state) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("setState(state=" + state + ")");
        this.state = state;
        if (LOGGER.isInfoEnabled()) LOGGER.info("setState(StoredMessageState) returns void");
    }

    public DeliveryStatus getDeliveryReport() {
        if (LOGGER.isInfoEnabled()) LOGGER.info("getDeliveryReport() returns " + deliveryStatus);
        return deliveryStatus;
    }

}