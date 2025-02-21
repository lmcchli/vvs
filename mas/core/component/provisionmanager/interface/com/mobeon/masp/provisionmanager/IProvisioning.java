/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager;

/**
 * This interface is used to manage Subscribers in the user registry.
 *
 * @author ermmaha
 */
public interface IProvisioning {

    /**
     * Creates a subscriber in the user directory
     *
     * @param sub
     * @param adminUid
     * @param adminPwd
     */
    public void create(Subscription sub, String adminUid, String adminPwd) throws ProvisioningException;

    /**
     * Creates a subscriber in the user directory. Asynchronous version.
     *
     * @param sub
     * @param adminUid
     * @param adminPwd
     * @return transactionid used for identifying the request
     */
    public int createAsync(Subscription sub, String adminUid, String adminPwd) throws ProvisioningException;

    /**
     * Waits for the asynchronous method of create to finish.
     *
     * @param transactionid
     */
    public void create(int transactionid) throws ProvisioningException;

    /**
     * Deletes a subscriber from the user directory
     *
     * @param sub
     * @param adminUid
     * @param adminPwd
     */
    public void delete(Subscription sub, String adminUid, String adminPwd) throws ProvisioningException;

    /**
     * Deletes a subscriber in the user directory. Asynchronous version.
     *
     * @param sub
     * @param adminUid
     * @param adminPwd
     * @return transactionid used for identifying the request
     */
    public int deleteAsync(Subscription sub, String adminUid, String adminPwd) throws ProvisioningException;

    /**
     * Waits for the asynchronous method of delete to finish.
     *
     * @param transactionid
     */
    public void delete(int transactionid) throws ProvisioningException;

    /**
     * Returns the status of the asynchronous request identified with transactionid
     *
     * @param transactionid
     * @return true if finished false if not.
     */
    public boolean isFinished(int transactionid);
}
