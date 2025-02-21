/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.registration.SspInstance;

/**
 * Interface towards a Remote Party controller. This interface is used from the
 * administrative states when accessing the Remote Party controller.
 *
 * @author Malin Flodin
 */
public interface RemotePartyController {

    public void delete();

    public RemotePartyAddress getRandomRemotePartyAddress();
    public int getAmountOfRegisteredRemoteParties();

    public void registerAllSsps();
    public void unregisterAllSsps();

    /**
     * This method is used to re-initialize the Remote Party controller.
     * The configuration is re-read and new SSPs are instantiated
     * while SSPs that have been removed from the configuration since last
     * initialization are unregistered and removed.
     */
    public void reInitialize();

    /**
     * This method is used to blacklist a remote party.
     * If the remote party is an SSP, unregistration is initiated towards the SSP.
     * If the remote party is a GW, it is considered unavailable for a
     * configurable amount of time.
     * @param remotePartyId     Id of the remote party in the format "host:port"
     */
    public void blacklistRemoteParty(String remotePartyId);

    /**
     * This method is used to remove a remote party from the black list.
     * @param remotePartyId Id of the remote party in the format "host:port".
     */
    public void removeBlackListedRemoteParty(String remotePartyId);

    /**
     * This method is used to find out if a remote party is black listed.
     * @param remotePartyId Id of the remote party in the format "host:port".
     */
    public boolean isRemotePartyBlackListed(String remotePartyId);

    /**
     * Public method for basic test purposes only.
     * It is used to add an SSP to the list of SSPs.
     * @param ssp
     */
    public void addSspInstance(SspInstance ssp);
}
