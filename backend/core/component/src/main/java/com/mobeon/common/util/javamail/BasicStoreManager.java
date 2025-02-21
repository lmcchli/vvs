/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.util.javamail;

import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.mail.*;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.ConnectionListener;
import java.util.Properties;

/**
 * @author QHAST
 */
public class BasicStoreManager implements StoreManager {

    private static final HostedServiceLogger LOGGER = new HostedServiceLogger(ILoggerFactory.getILogger(BasicStoreManager.class));

    private String protocol = "imap";
    private Session session;
    private Properties sessionProperties;


    public BasicStoreManager(Session session) {
        this.session = session;
    }

    public BasicStoreManager() {
        this(null);
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public Session getSession() {
        if (session == null) {
            session = Session.getDefaultInstance(getSessionProperties());
        }
        return session;
    }

    private Properties getSessionProperties() {
        if (sessionProperties == null) {
            sessionProperties = new Properties();
        }
        return sessionProperties;
    }

    public Store getStore(String host, int port, String accountId, String accountPassword) throws MessagingException {

        URLName url = new URLName(protocol, host, port, null, accountId, accountPassword);
        return getStore(url);

    }

    protected Store getStore(URLName url) throws MessagingException {

        Store store;

        try {
            store = getSession().getStore(url);
            if (LOGGER.isDebugEnabled()) {
                store.addConnectionListener(new ConnectionDebugger());
            }
        } catch (NoSuchProviderException e) {
            LOGGER.fatal("StoreManager JavaMail protocol support for \"" + protocol + "\" is not correct configured: " + e.getMessage(),e);
            throw e;
        }

        try {
            store.connect();
            LOGGER.available(
                    store.getURLName().getProtocol(),
                    store.getURLName().getHost(),
                    store.getURLName().getPort()
            );
        } catch (AuthenticationFailedException e) {
            LOGGER.error(e.getMessage(),e);
            throw e;
        } catch (MessagingException e) {
            LOGGER.notAvailable(
                    store.getURLName().getProtocol(),
                    store.getURLName().getHost(),
                    store.getURLName().getPort(),
                    e.getMessage()
            );
            throw e;
        }
        return store;

    }

    public void returnStore(Store store) {
        try {
            store.close();
        } catch (MessagingException e) {
            LOGGER.debug("Exception while closing store. "+e.getMessage(), e);
        }

    }

    private class ConnectionDebugger implements ConnectionListener {

        /**
         * Logs the event at debug level.
         *
         * @param connectionEvent
         */
        public void opened(ConnectionEvent connectionEvent) {
            LOGGER.debug(connectionEvent.getSource() + " opened!");
        }

        /**
         * Logs the event at debug level.
         *
         * @param connectionEvent
         */
        public void disconnected(ConnectionEvent connectionEvent) {
            LOGGER.debug(connectionEvent.getSource() + " disconnected!");
        }

        /**
         * Logs the event at debug level.
         *
         * @param connectionEvent
         */
        public void closed(ConnectionEvent connectionEvent) {
            LOGGER.debug(connectionEvent.getSource() + " closed!");
        }

    }








}
