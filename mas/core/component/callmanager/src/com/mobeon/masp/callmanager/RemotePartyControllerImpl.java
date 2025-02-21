/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.masp.callmanager.configuration.RemoteParty;
import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.registration.SspInstance;
import com.mobeon.masp.callmanager.registration.SspStatus;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO: Document
 * @author Malin Nyfeldt
 */
public class RemotePartyControllerImpl implements RemotePartyController {

    // Thread-safe due to immutable, i.e set at construction time
    private static final Timer blacklistTimer = new Timer();

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private RemotePartyAddress sipproxy;
    private List<SspInstance> ssps = new ArrayList<SspInstance>();

    // Contains all black listed remote parties that are not SSPs, e.g. Gateways
    // The hash set contains the id of the remote parties with the following
    // syntax "host:port".
    private ConcurrentHashMap<String, BlackListedRemotePartyTimerTask>
            blackListedRemoteParties =
            new ConcurrentHashMap<String, BlackListedRemotePartyTimerTask>();

    public RemotePartyControllerImpl() {
        RemoteParty remoteParty =
                ConfigurationReader.getInstance().getConfig().getRemoteParty();

     //   if (remoteParty.isSipProxy()) {
            this.sipproxy = remoteParty.getSipProxy();
     /*   } else {
            this.sipproxy = null;
            List<RemotePartyAddress> sspList = remoteParty.getSspList();
            for (RemotePartyAddress sspAddress : sspList) {
                ssps.add(new SspInstance(sspAddress));
            }
        } */
    }

    public synchronized void delete() {
        // Cancel SSP timers
        SspStatus.getInstance().clear();
        for (SspInstance sspInstance : ssps) {
            sspInstance.cancelTimers();
        }

        // Cancel timers for black listed parties
        Collection<BlackListedRemotePartyTimerTask> timerTasks =
                blackListedRemoteParties.values();
        for (BlackListedRemotePartyTimerTask task : timerTasks) {
            task.cancel();
        }
    }

    public synchronized RemotePartyAddress getRandomRemotePartyAddress() {
        RemotePartyAddress address = null;

        if (ssps.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving sipproxy address");
            }
            if (!isRemotePartyBlackListed(
                    sipproxy.getHost() + ":" + sipproxy.getPort()))
                address = sipproxy;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Retrieving ssp address");
            }
            SspInstance sspInstance = SspStatus.getInstance().getRandomSsp();
            if (sspInstance != null) {
                address = sspInstance.getAddress();
            }
        }

        return address;
    }

    public synchronized int getAmountOfRegisteredRemoteParties() {
        int amount;
        if (sipproxy != null) {
            amount = 1;
        } else {
            amount = SspStatus.getInstance().getNumberOfRegisteredSsp();
        }
        return amount;
    }

    public synchronized int getAmountOfConfiguredRemoteParties() {
        int amount;
        if (sipproxy != null) {
            amount = 1;
        } else {
            amount = ssps.size();
        }
        return amount;
    }

    public synchronized void unregisterAllSsps() {
        for (SspInstance sspInstance : ssps) {
            sspInstance.doUnregister();
        }
    }

    public synchronized void registerAllSsps() {
        for (SspInstance sspInstance : ssps) {
            sspInstance.doRegister();
        }
    }

    /**
     * This method is used to re-initialize the remote party controller.
     * The configured remote parties is fetched from the configuration again.
     * <p>
     * Current active SSPs that are not in the new configuration are unregistered
     * and removed.
     * <p>
     * Those active SSPs that still remains in the configuration are kept as is.
     * <p>
     * New SSPs not previously configured are created and also registered if
     * {@link CallManagerControllerImpl#isOkToRegisterNewSsps()}
     * indicates that it is ok.
     */
    public synchronized void reInitialize() {
        RemoteParty remoteParty =
                ConfigurationReader.getInstance().getConfig().getRemoteParty();

            this.sipproxy = remoteParty.getSipProxy();
            
          /*
           *  old code using the ssp - we don't remove this code for now, maybe it will be needed later.
             if (remoteParty.isSipProxy()) {
           
                this.sipproxy = remoteParty.getSipProxy();
            } else {
                this.sipproxy = null;

                List<RemotePartyAddress> configuredSspAddressList =
                        remoteParty.getSspList();

                List<SspInstance> newSsps = new ArrayList<SspInstance>();
                List<SspInstance> removedSsps = new ArrayList<SspInstance>();

                // Loop over the previous list of ssps.
                // If the SSPs address can be found in the new configuration, add
                // it to the list of new SSPs and remove the found address from
                // the configured list.
                // If the SSPs address cannot be found in the new configuration,
                // the SSP is added to the list of SSPs that shall be unregistered
                // and removed.
                for (SspInstance sspInstance : ssps) {
                    RemotePartyAddress sspAddress = sspInstance.getAddress();
                    if (configuredSspAddressList.contains(sspAddress)) {
                        newSsps.add(sspInstance);
                        configuredSspAddressList.remove(sspAddress);
                    } else {
                        removedSsps.add(sspInstance);
                    }
                }

                // Check too see if there are any SSP addresses left in the
                // configured list. If there is, these are new SSP instances that
                // shall be created and also registered if the
                // CallManagerControllerImpl's method isOkToRegisterNewSsps()
                // indicates that it is ok.
                for (RemotePartyAddress address : configuredSspAddressList) {
                    SspInstance sspInstance = new SspInstance(address);

                    if (CMUtils.getInstance().getCmController().isOkToRegisterNewSsps()) {
                         sspInstance.doRegister();
                    }

                    newSsps.add(sspInstance);
                }

                // Those SSPs that were not in the new configuration shall be
                // unregistered and removed.
                for (SspInstance sspInstance : removedSsps) {
                    sspInstance.doUnregister();
                }

                ssps = newSsps;
            }
			*/

    }

    public void blacklistRemoteParty(String remotePartyId) {
        SspInstance sspInstance = getSspInstance(remotePartyId);
        if (log.isInfoEnabled()) log.info("Black listing remote party: " + remotePartyId);

        if (sspInstance != null) {
            sspInstance.markAsUnregistered();
            if (log.isDebugEnabled()) {
                log.debug("Black listed remote party is an SSP. " +
                      "It is marked as unregistered. <Remote Party = " +
                      remotePartyId + ">");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Black listed remote party is NOT an SSP. " +
                        "<Remote Party = " + remotePartyId + ">");
            }
            BlackListedRemotePartyTimerTask timertask =
                    new BlackListedRemotePartyTimerTask(remotePartyId);
            blackListedRemoteParties.put(remotePartyId, timertask);
            blacklistTimer.schedule(
                    timertask,
                    ConfigurationReader.getInstance().getConfig().getBlackListTimer());
        }
    }

    public void removeBlackListedRemoteParty(String remotePartyId) {
        if (log.isInfoEnabled()) log.info("Remote party: " + remotePartyId + " is no longer black listed.");
        blackListedRemoteParties.remove(remotePartyId);
    }

    public boolean isRemotePartyBlackListed(String remotePartyId) {
        boolean result = blackListedRemoteParties.containsKey(remotePartyId);

        if (log.isDebugEnabled()) {
            log.debug("Is remote party " + remotePartyId +
                    " black listed? Answer: " + result);
        }

        return result;
    }

    /**
     * Used to retrieve one specific SSP instance.
     * This method should only used for basic testing.
     * @param sspId The identifier of the SSP in format <code>host:port</code>.
     * @return  The matching SSP instance or null if none was found.
     */
    public synchronized SspInstance getSspInstance(String sspId) {
        for (SspInstance sspInstance : ssps) {
            if (sspInstance.getAddress().toString().equals(sspId)) {
                return sspInstance;
            }
        }
        return null;
    }

    public synchronized void addSspInstance(SspInstance ssp) {
        ssps.add(ssp);
    }
}
