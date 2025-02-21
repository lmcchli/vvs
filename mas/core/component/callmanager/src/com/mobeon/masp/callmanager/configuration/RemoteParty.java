/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.configuration;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.masp.execution_engine.ServiceEnablerException;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Contains the configured remote party, i.e. the list of SSP's or the SIP proxy.
 * <p>
 * A remote party can be created by parsing the configuration.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Nyfeldt
 */
public class RemoteParty {
	
    private static final ILogger LOG = ILoggerFactory.getILogger(RemoteParty.class);
    //The code for ssplist is commented in case we need to use it later.
    //The parameters for ssplist were removed from xsd because we are not supposed to use for now 
    //(only one instance of Dialogic is currently used).
    
    // Configuration related constants
  //  private static final String SSP_LIST = "ssplist";
    private static final String SIPPROXY = "sipproxy";
    private static final String ADDRESS = "address";
    private static final String HOST = "host";
    private static final String PORT = "port";

   /* private final List<RemotePartyAddress> sspList =
            new ArrayList<RemotePartyAddress>();*/
    private RemotePartyAddress sipproxy = null;

    /**
     * Parses the configuration for remote party and returns a
     * {@link RemoteParty}.
     * @param remotePartyGroup
     * @return a {@link RemoteParty} representing the remote party containing
     * either one sipproxy or a list of SSPs.
     * @throws ServiceEnablerException if neither a sipproxy nor an SSP list
     * could be retrived from configuration.
     */
    public static RemoteParty parseRemoteParty(/*IGroup remotePartyGroup*/ String host, Integer port)
           /* throws ServiceEnablerException*/ {

        RemoteParty remoteParty = new RemoteParty();

        /*try {
            IGroup sspListGroup = remotePartyGroup.getGroup(SSP_LIST);
            List<IGroup> sspAddressList = sspListGroup.getGroups(ADDRESS);

            for (IGroup sspAddress : sspAddressList) {
                String host = sspAddress.getString(HOST);
                Integer port = sspAddress.getInteger(PORT);
                remoteParty.addSsp(host, port);
            }

        } catch (Exception e) {
            if (LOG.isDebugEnabled())
                LOG.debug("Remote party SSP list could not be retrieved from " +
                        "configuration.");
        }*/

       // try {
            //IGroup sipproxyGroup = remotePartyGroup.getGroup(SIPPROXY);
         //   IGroup sipProxyAddress = sipproxyGroup.getGroup(ADDRESS);

            remoteParty.setSipProxy(host, port);
      /*  } catch (Exception e) {
            if (LOG.isDebugEnabled())
                LOG.debug("Remote party sipproxy could not be retrieved from " +
                        "configuration.");
        }*/

        // Check that either sipproxy or SSPs has been configured.
   /*     if ((!remoteParty.isSipProxy()) && (remoteParty.amountOfSsps() <= 0)) {
            String error =
                    "Could not retrieve required remote party configuration.";
            LOG.error(error);
            throw new ServiceEnablerException(error);
        }*/

        return remoteParty;
    }

/*    public synchronized int amountOfSsps() {
        return sspList.size();
    }
*/
    public synchronized List<RemotePartyAddress> getSspList() {
        return Collections.emptyList();
    } 

    public synchronized RemotePartyAddress getSipProxy() {
        return sipproxy;
    }

    public synchronized boolean isSipProxy() {
        return sipproxy != null;
    }

    //=========================== Private methods =========================

    public synchronized void setSipProxy(String host, Integer port) {
        sipproxy = new RemotePartyAddress(host, port);
    }

    // A remote party should only be created by parsing configuration or in
    // basic tests
    public RemoteParty() {
    }

    /**
     * Adds a new SSP address with the given host and port.
     * If an SSP address with that host and port already exists, a new is not
     * added.
     * @param host
     * @param port
     */
    public synchronized void addSsp(String host, Integer port) {
      /*  RemotePartyAddress address = new RemotePartyAddress(host, port);
        if (!sspList.contains(address))
            sspList.add(address);*/
    } 
}
