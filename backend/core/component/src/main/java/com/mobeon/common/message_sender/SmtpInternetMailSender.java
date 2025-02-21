/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.message_sender;

import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Håkan Stolt
 */
public abstract class SmtpInternetMailSender<T extends SmtpInternetMailSenderConfig> extends AbstractInternetMailSender implements IEventReceiver {

    private static final ILogger log = ILoggerFactory.getILogger(SmtpInternetMailSender.class);

    public static final String CONFIG_GROUP_NAME = "messagesender";
    private IConfiguration configuration;
    protected ILocateService serviceLocator;
    protected AtomicReference<T> atomicConfig;
    private IEventDispatcher eventDispatcher;

    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    public ILocateService getServiceLocator() {
        return serviceLocator;
    }

    public void setServiceLocator(ILocateService serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public IEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void init() throws InternetMailSenderException {
        eventDispatcher.addEventReceiver(this);
    }

    protected abstract T newConfig();

    protected T getConfig() throws InternetMailSenderException {

        if (atomicConfig.get() == null) {
            try {
                if (log.isDebugEnabled()) log.debug("Creating new JavaMailSenderContext!");
                IGroup messageSenderConfigRoot;
                messageSenderConfigRoot = configuration.getConfiguration().getGroup(CONFIG_GROUP_NAME);
                T newConfig = newConfig();
                newConfig.init(messageSenderConfigRoot);
                atomicConfig.set(newConfig);

            } catch (Exception e) {
                InternetMailSenderException e2 = new InternetMailSenderException("Unable to setup message sender config root: \"" + CONFIG_GROUP_NAME + "\"");
                log.fatal(e2.getMessage(), e);
                throw e2;
            }
        }
        return atomicConfig.get();
    }

    public void doEvent(Event event) {
        doGlobalEvent(event);
    }

    public void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            atomicConfig.set(null);
        }
    }


    protected SmtpServiceInstanceDecorator getSmtpService(String host, T currentContext) throws InternetMailSenderException {

        //Get SMTP Service Instance
        IServiceInstance smtpService;
        try {
            if (host == null || host.length() == 0) {
                smtpService = serviceLocator.locateService(currentContext.getSmtpServiceName());
            } else {
                try {
                    smtpService = serviceLocator.locateService(currentContext.getSmtpServiceName(), host);
                } catch (NoServiceFoundException e) {
                    // If no specified service instance can be found, locate any instance
                    smtpService = serviceLocator.locateService(currentContext.getSmtpServiceName());
                }
            }
        } catch (NoServiceFoundException e) {
        	log.error("No SMTP Service could be found: " + currentContext.getSmtpServiceName());
        	e.printStackTrace(System.out);
            throw new InternetMailSenderException("No SMTP Service could be found.", e);
        }

        return SmtpServiceInstanceDecorator.createSmtpServiceInstanceDecorator(smtpService);

    }


    protected SmtpServiceInstanceDecorator getSmtpService(T currentContext) throws InternetMailSenderException {
        return getSmtpService(null, currentContext);
    }

    /**
     * Adds a submitted domain to "To:"-addresses without a domain
     *
     * @param message The message containing the "To:"-addresses
     * @param domain  The domain to add to addresses without a domain
     * @throws InternetMailSenderException if an error occurs
     */
    protected void addDomainToAddresses(MimeMessage message, String domain) throws InternetMailSenderException {
        if (log.isDebugEnabled()) log.debug("addDomainToAddresses(message=" + message + ", domain=" + domain + ")");
        try {
            Address[] adresses = message.getRecipients(Message.RecipientType.TO);
            if (adresses == null) {
                // No addresses to add domain to, return
                return;
            }
            boolean modified = false;
            for (Address address : adresses) {
                if (address instanceof InternetAddress) {
                    InternetAddress internetAddress = (InternetAddress) address;
                    // Only add domain to addresses without domain
                    String strAddress = internetAddress.getAddress();
                    if (strAddress.indexOf("@") == -1) {
                        if (log.isDebugEnabled()) log.debug("Adding domain <" + domain + "> to <" + strAddress + ">");
                        internetAddress.setAddress(internetAddress.getAddress() + "@" + domain);
                        modified = true;
                    }
                }
            }
            // Only necessary to update message if changes has occured
            if (modified) {
                // Clear all "To:"-addresses
                message.setRecipients(Message.RecipientType.TO, (Address[]) null);
                // Set each address again
                for (Address address : adresses) {
                    message.addRecipient(Message.RecipientType.TO, address);
                }
            }
        } catch (MessagingException e) {
            String errmsg = "Could not add domain to addresses without domain";
            if (log.isDebugEnabled()) log.debug(errmsg + ": " + e);
            throw new InternetMailSenderException(errmsg, e);
        }
        if (log.isDebugEnabled()) log.debug("addDomainToAddresses(MimeMessage, String) returns void");
    }

    protected String getMailSender(MimeMessage message, SmtpOptions options) throws InternetMailSenderException {
        String sender = null;
        if (options != null && options.getEnvelopeFrom() != null) {
            sender = options.getEnvelopeFrom();
        } else {
            try {
                Address[] from = message.getFrom();
                if (from != null) {
                    for (Address a : from) {
                        if (a instanceof InternetAddress) {
                            sender = ((InternetAddress) from[0]).getAddress();
                            break;
                        }
                    }
                }
            } catch (MessagingException e) {
                throw new InternetMailSenderException("Could not determine Mail Sender.", e);
            }

        }
        if (sender == null) {
            throw new InternetMailSenderException("Could not determine Mail Sender!");
        }
        return sender;
    }

    protected String[] getRecipientsEmailAddresses(Message message) throws InternetMailSenderException {
        ArrayList<String> list = new ArrayList<String>();
        try {
            Address[] recipients = message.getAllRecipients();
            if (recipients != null) {
                for (Address a : recipients) {
                    if (a instanceof InternetAddress) {
                        list.add(((InternetAddress) a).getAddress());
                    } else {
                        if (log.isInfoEnabled()) log.info(a + " is not an " + InternetAddress.class);
                    }
                }
            }
        } catch (MessagingException e) {
            throw new InternetMailSenderException("Could not make recipients e-mail address list.", e);
        }
        return list.toArray(new String[list.size()]);
    }

    protected static class SmtpServiceInstanceDecorator implements IServiceInstance {
        private static final String PROTOCOL = "smtp";

        private String host;
        private int port;
        private IServiceInstance decoratedServiceInstance;

        private SmtpServiceInstanceDecorator() {
        }

        public String getProperty(String s) {
            return decoratedServiceInstance.getProperty(s);
        }

        public void setProperty(String s1, String s2) {
            decoratedServiceInstance.setProperty(s1, s2);
        }

        public String getServiceName() {
            return decoratedServiceInstance.getServiceName();
        }

        public void setServiceName(String s) {
            decoratedServiceInstance.setServiceName(s);
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getProtocol() {
            return PROTOCOL;
        }

        public IServiceInstance getDecoratedServiceInstance() {
            return decoratedServiceInstance;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getServiceName());
            sb.append("@");
            sb.append(getHost());
            sb.append(":");
            sb.append(getPort());
            return sb.toString();
        }

        /**
         * @param serviceInstance
         * @return
         * @throws InternetMailSenderException if service instance not provides host and port values.
         */
        private static SmtpServiceInstanceDecorator createSmtpServiceInstanceDecorator(IServiceInstance serviceInstance) throws InternetMailSenderException {
            SmtpServiceInstanceDecorator smtpServiceInstanceDecorator = new SmtpServiceInstanceDecorator();
            smtpServiceInstanceDecorator.init(serviceInstance);
            return smtpServiceInstanceDecorator;
        }

        private void init(IServiceInstance serviceInstance) throws InternetMailSenderException {

        	decoratedServiceInstance = serviceInstance;
        	host = decoratedServiceInstance.getProperty(IServiceInstance.HOSTNAME);
        	if (host == null) {
        		log.error("Could not found service instance property \"" + IServiceInstance.HOSTNAME + "\".");
        		throw new InternetMailSenderException("Could not found service instance property \"" + IServiceInstance.HOSTNAME + "\".");
        	}
        	
        	try {
        		port = Integer.valueOf(decoratedServiceInstance.getProperty(IServiceInstance.PORT));
        	}
        	catch (NumberFormatException e) {
        		log.error("Could not found service instance property \"" + IServiceInstance.PORT + "\".");
        		e.printStackTrace(System.out);
        		throw new InternetMailSenderException("Could not found service instance property \"" + IServiceInstance.PORT + "\".");        		
        	}
        }
    }
}
