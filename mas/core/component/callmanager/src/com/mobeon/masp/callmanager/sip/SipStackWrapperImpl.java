/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2006.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *
 */
package com.mobeon.masp.callmanager.sip;

import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.callmanager.sip.header.SipHeaderFactory;
import com.mobeon.masp.callmanager.sip.message.SipResponseFactoryImpl;
import com.mobeon.masp.callmanager.sip.message.SipRequestFactoryImpl;
import com.mobeon.masp.callmanager.sip.message.SipRequestValidator;
import com.mobeon.masp.callmanager.sip.message.SipResponseFactory;
import com.mobeon.masp.callmanager.sip.message.SipRequestFactory;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.configuration.SipTimers;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import gov.nist.javax.sip.SipStackImpl;

import javax.sip.SipListener;
import javax.sip.SipFactory;
import javax.sip.PeerUnavailableException;
import javax.sip.SipStack;
import javax.sip.SipProvider;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.ClientTransaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.AddressFactory;
import javax.sip.header.CallIdHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class is a wrapper of the JAIN SIP stack.
 * <p>
 * It contains functionality related to the stack itself, e.g. creation of the
 * stack and helper methods such as creating a call-id or tag.
 * <p>
 * It does not contain functionality regarding creating SIP messages or headers.
 * This is instead found in the packages
 * {@link com.mobeon.masp.callmanager.sip.message} and
 * {@link com.mobeon.masp.callmanager.sip.header}.
 * <p>
 * This class creates and configures the SIP stack. The following stack
 * properties are configured:
 * <ul>
 * <li>javax.sip.STACK_NAME: with the value "NISTv1.2".</li>
 * <li>javax.sip.RETRANSMISSION_FILTER: with the value "true", i.e. filter
 * activation.</li>
 * <li>gov.nist.javax.sip.TRACE_LEVEL: with the value "0" to disable the
 * logging in the stack implementation.</li>
 * <li>javax.sip.CACHE_SERVER_CONNECTIONS: with the value "false"</li>
 * <li>javax.sip.CACHE_CLIENT_CONNECTIONS: with the value "false"</li>
 * <li>gov.nist.javax.sip.READ_TIMEOUT: with the value 1000.</li>
 * </ul>
 * <p>
 * At initialisation, a UDP and a TCP listening point is created.
 *
 * @author Malin Flodin
 */
public class SipStackWrapperImpl implements SipStackWrapper {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final SipListener sipStackListener;
    private final String host;
    private final String hostaddress;
    private final int port;
    private final SipTimers sipTimers;
    private final int maxServerTransactions;

    // Factories and other things necessary to setup and access the SIP stack
    private SipStack sipStack = null;
    private SipFactory sipFactory = null;
    private SipProvider sipProvider = null;
    private SipHeaderFactory sipHeaderFactory = null;
    private SipResponseFactoryImpl sipResponseFactory = null;
    private SipRequestFactoryImpl sipRequestFactory = null;
    private SipRequestValidator sipRequestValidator = null;
    private SipMessageSenderImpl sipMessageSender = null;

    // Generator of cryptographically random numbers. Used for tag generation.
    private SecureRandom random;

    /**
     * Constructor.
     * <p>
     * @param sipStackListener  Listener that shall receive events from the
     *                          SIP stack.
     * @param host              Host to use for the SIP stack.
     * @param port              Port to use for the SIP stack.
     * @param sipTimers         Values to use for the SIP timers.
     *
     * @throws NullPointerException
     *          A NullPointerException is thrown if
     *          <code>sipStackListener</code>, <code>host</code>, or
     *          <code>sipTimers</code> is null.
     *
     * @throws UnknownHostException
     *          An UnknownHostException is thrown if an IP address could not be
     *          retrieved for the <code>host</code>.
     */
    public SipStackWrapperImpl(SipListener sipStackListener,
                               String host, int port,
                               SipTimers sipTimers)
            throws UnknownHostException, NullPointerException {

        if ((sipStackListener == null) || (host == null) || (sipTimers == null)) {
            throw new NullPointerException(
                    "Input parameter must not be null. SipStackListener = " +
                            sipStackListener + ", Host = " + host +
                            ", sipTimers = " + sipTimers);
        }

        this.sipStackListener = sipStackListener;
        this.host = host;
        this.hostaddress = InetAddress.getByName(host).getHostAddress();
        this.port = port;
        this.sipTimers = sipTimers;
        this.maxServerTransactions = 5000;
    }

    /**
     * Constructor.
     * <p>
     * @param sipStackListener  Listener that shall receive events from the
     *                          SIP stack.
     * @param host              Host to use for the SIP stack.
     * @param port              Port to use for the SIP stack.
     * @param sipTimers         Values to use for the SIP timers.
     * @param maxServerTransactions The maximum number of open SERVER transactions that the transaction table can have.
     *
     * @throws NullPointerException
     *          A NullPointerException is thrown if
     *          <code>sipStackListener</code>, <code>host</code>, or
     *          <code>sipTimers</code> is null.
     *
     * @throws UnknownHostException
     *          An UnknownHostException is thrown if an IP address could not be
     *          retrieved for the <code>host</code>.
     */
    public SipStackWrapperImpl(SipListener sipStackListener, String host, int port, SipTimers sipTimers, int maxServerTransactions) throws UnknownHostException, NullPointerException {
        if ((sipStackListener == null) || (host == null) || (sipTimers == null)) {
            throw new NullPointerException("Input parameter must not be null. SipStackListener = " + sipStackListener + ", Host = " + host + ", sipTimers = " + sipTimers);
        }
        this.sipStackListener = sipStackListener;
        this.host = host;
        this.hostaddress = InetAddress.getByName(host).getHostAddress();
        this.port = port;
        this.sipTimers = sipTimers;
        this.maxServerTransactions = maxServerTransactions;
    }

    /**
     * Initializes the SIP stack wrapper.
     * <p>
     * The SIP stack is created and configured and the SIP provider
     * is created and activated.
     * The random seed used for tag generation is initialized.
     *
     * @throws ServiceEnablerException A Service Enabler exception
     */
    public void init() throws ServiceEnablerException {

        if (log.isDebugEnabled())
            log.debug("INIT SIP Stack");

        // Create stacks
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        createSipStack(sipFactory);

        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed((new Date()).getTime());
        } catch (NoSuchAlgorithmException e) {
            String message =
                    "Failed to instantiate random generator for tag generation. ";

            if (log.isDebugEnabled())
                log.debug(message);

            throw new ServiceEnablerException(message);
        }

        // Create listening points
        sipProvider = createProvider();

        if (log.isInfoEnabled()) log.info("Listening for requests on " + host + ":" + port);

        createMessageFactories(sipFactory);
    }

    public void delete() throws ObjectInUseException {
        sipProvider.removeSipListener(sipStackListener);
        sipStack.deleteSipProvider(sipProvider);
        sipFactory.resetFactory();
    }

    /**
     * Generates a new random tag.
     * @return a 32-bit cryptographically random positiv integer as a String.
     */
    public String generateTag() {
        int tag = random.nextInt();
        tag = (tag < 0) ? 0 - tag : tag; // generate a positive number
        if (log.isDebugEnabled())
            log.debug("Generated the tag: " + tag);
        return Integer.toString(tag);
    }


    // Getters
    public SipHeaderFactory getSipHeaderFactory() {
        return sipHeaderFactory;
    }

    public SipResponseFactory getSipResponseFactory() {
        return sipResponseFactory;
    }

    public SipRequestFactory getSipRequestFactory() {
        return sipRequestFactory;
    }

    public SipRequestValidator getSipRequestValidator() {
        return sipRequestValidator;
    }

    public SipMessageSender getSipMessageSender() {
        return sipMessageSender;
    }

    public String getHost() {
        return host;
    }

    public String getHostAddress() {
        return hostaddress;
    }

    public int getPort() {
        return port;
    }

    public CallIdHeader getNewCallId() {
        return sipProvider.getNewCallId();
    }

    public ClientTransaction getNewClientTransaction(Request request)
            throws TransactionUnavailableException {
        return sipProvider.getNewClientTransaction(request);
    }

    public SipProvider getSipProvider() {
        return sipProvider;
    }

    /** 
     * 
     * Audit dialogs and transactions in the SIP stack.
     *
     * @param activeCallIDs Set of all live callId's (in callmanager point of view).
     * @param leakedDialogTimer Minimum time after which a leaked dialog will be removed.
     * @param leakedTransactionTimer Minimum time after which a leaked transaction will be removed.
     * @return a text report of any removed dialogs or transactions or null if nothing was removed.
     */
    public String auditStack(Set<String> activeCallIDs, 
    		long leakedDialogTimer, long leakedTransactionTimer) {
    	
    	if (sipStack instanceof SipStackImpl) {
    		
    		SipStackImpl stack = (SipStackImpl)sipStack;
    		return stack.auditStack(activeCallIDs, leakedDialogTimer, leakedTransactionTimer);
    		
    	}
    	
    	return null;
    }


    // ====================== Private methods ==========================

    /**
     * Creates a SIP stack
     * @throws ServiceEnablerException if the SIP stack could not be created.
     */
    private void createSipStack(SipFactory sipFactory)
            throws ServiceEnablerException
    {
        Properties properties = createConfigurationProperties();
        try {
            sipStack = sipFactory.createSipStack(properties);
            if (log.isDebugEnabled())
                log.debug("Created SIP stack.");
        } catch (PeerUnavailableException e) {
            String message = "Could not create the SIP stack. " +
                    "Ensure that the host is correctly set in installation.";
            if (log.isDebugEnabled())
                log.debug(message, e);
            throw new ServiceEnablerException(message, e);
        }
    }

    /**
     * Creates a SIP provider
     * @return The created SIP provider.
     * @throws ServiceEnablerException if a provider could not be created.
     */
    private SipProvider createProvider()
            throws ServiceEnablerException
    {
        ListeningPoint udpListeningPoint = createListeningPoint("udp");
        ListeningPoint tcpListeningPoint = createListeningPoint("tcp");
        SipProvider provider;

        try {
            provider = sipStack.createSipProvider(udpListeningPoint);
            provider.addListeningPoint(tcpListeningPoint);
            provider.addSipListener(sipStackListener);
        } catch (Exception e) {
            String message = "Could not create a sipProvider in SIP stack: " +
                    e.getMessage();
            if (log.isDebugEnabled())
                log.debug(message);
            throw new ServiceEnablerException(message, e);
        }

        if (log.isDebugEnabled())
            log.debug("Created new sipProvider with UDP and TCP listening points");

        return provider;
    }

    /**
     * Creates a SIP listening point for the given protocol.
     * @param protocol Should be either "udp" or "tcp".
     * @return The created SIP listening point.
     * @throws ServiceEnablerException if a listening point could not be created
     * for the given protocol.
     */
    private ListeningPoint createListeningPoint(String protocol)
            throws ServiceEnablerException
    {
        ListeningPoint listeningPoint;
        try {
            listeningPoint = sipStack.createListeningPoint(host, port, protocol);
            setSendByListeningPoint(listeningPoint);            
        } catch (Exception e) {
            String message =
                    "Could not create " + protocol +
                            " listening point at " + host + ":" + port +
                            " in SIP stack: " + e.getMessage();
            if (log.isDebugEnabled())
                log.debug(message);
            throw new ServiceEnablerException(message, e);
        }

        if (log.isDebugEnabled())
            log.debug("Created " + protocol + " listening point.");
        return listeningPoint;
    }

    private void setSendByListeningPoint(ListeningPoint listeningPoint) {

        try {
            String viaOverride = null;

            // Update the via header only when MAS is used as an end point, not a Proxy/B2BUA.
/*            if (ConfigurationReader.getInstance().getConfig().getApplicationProxyMode()) {
                log.debug("No ListeningPoint update (sentBy setup) performed when in Proxy/B2BUA mode");
                return;
            }
*/
            viaOverride = ConfigurationReader.getInstance().getConfig().getViaOverride();
            if (viaOverride != null && !viaOverride.isEmpty()) {
                log.debug("Listening point (sentBy) from: " + listeningPoint.getSentBy());
                listeningPoint.setSentBy(viaOverride);
                log.debug("Listening point (sentBy) updated to: " + listeningPoint.getSentBy());
            } else {
                log.debug("Listening point (sentBy) not changed: " + listeningPoint.getSentBy());
            }

        } catch (Throwable e) {
            log.error("Error while trying to setup ListeningPoint (sentBy)", e);
        }
    }

    /**
     * Creates header, address and message factories using a SIP stack factory.
     * @param sipFactory The SIP Factory
     * @throws ServiceEnablerException if the factories could not be created.
     */
    private void createMessageFactories(SipFactory sipFactory)
            throws ServiceEnablerException
    {
        try {
            AddressFactory addressFactory = sipFactory.createAddressFactory();
            HeaderFactory headerFactory = sipFactory.createHeaderFactory();
            MessageFactory messageFactory = sipFactory.createMessageFactory();

            sipHeaderFactory =
                    new SipHeaderFactory(addressFactory, headerFactory);
            sipResponseFactory = new SipResponseFactoryImpl(this, messageFactory);
            sipRequestFactory = new SipRequestFactoryImpl(
                    this, messageFactory, sipHeaderFactory, sipProvider);
            sipMessageSender = new SipMessageSenderImpl(this);
            sipRequestValidator = new SipRequestValidator(
                    sipMessageSender, sipResponseFactory);

        } catch (PeerUnavailableException e) {
            String message =
                    "Could not create necessary SIP message factories: " +
                            e.getMessage();
            if (log.isDebugEnabled())
                log.debug(message);
            throw new ServiceEnablerException(message, e);
        }
        if (log.isDebugEnabled())
            log.debug("Created message factories");
    }

    /**
     * Creates configuration properties for the SIP stack.
     * @return The configuration properties to use when creating the SIP stack.
     */
    private Properties createConfigurationProperties() {
        Properties properties = new Properties();

        properties.setProperty("javax.sip.STACK_NAME", "NISTv1.2");

        // The RETRANSMISSION_FILTER set to true indicates that the SIP stack
        // should handle retransmissions. Normally the SIP stack handles some
        // retransmissions, but this makes the SIP stack to handle all
        // necessary retransmissions.
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");

        // Logging properties
        String sipStackTraceLevel = System.getProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", sipStackTraceLevel);


        // Caching server/client connections to boost performance.
        // This opens up to possible TCP based Denial of Service attacks.
        // TODO: getenv only for test purposes. Should NOT be included in build
        log.info("******** SYSTEM PROPERTIES:\r\n" + System.getProperties());
        String cacheServerConnections = System.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS");
        if(cacheServerConnections == null) {
            log.info("Using default gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false");
            properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
        } else {
            log.info("Using system properties to set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=" + cacheServerConnections);
            properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", cacheServerConnections);
        }


        String cacheClientConnections = System.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS");
        if(cacheClientConnections == null) {
            log.info("Using default gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false");
            properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
        } else {
            log.info("Using system properties to set gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=" + cacheClientConnections);
            properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", cacheClientConnections);
        }


        // Guard against TCP read starvation.
        String readTimeout= System.getProperty("gov.nist.javax.sip.READ_TIMEOUT");
        if(readTimeout == null) {
            log.info("Using default gov.nist.javax.sip.READ_TIMEOUT=1000");
            properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
        } else {
            log.info("Using system properities to set gov.nist.javax.sip.READ_TIMEOUT=" + readTimeout);
            properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", readTimeout);
        }



        // Set the listener to re-entrant and the thread pool size to improve
        // performance
        
        properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
        String sipStackThreadPoolSize = System.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE");
        if ( sipStackThreadPoolSize == null ){
        	log.info("Using default gov.nist.javax.sip.REENTRANT_LISTENER=8");
        	sipStackThreadPoolSize = "8";
        }
        properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", sipStackThreadPoolSize);

        // Set the SIP timers, if configured
        if (sipTimers != null) {

            // Configure T2
            Integer t2 = sipTimers.getT2();
            if (t2 != null)
                properties.setProperty("gov.nist.javax.sip.T2", t2.toString());

            // Configure T4
            Integer t4 = sipTimers.getT4();
            if (t4 != null)
                properties.setProperty("gov.nist.javax.sip.T4", t4.toString());

            // Configure Timer B
            Integer timerB = sipTimers.getTimerB();
            if (timerB != null)
                properties.setProperty("gov.nist.javax.sip.TIMER_B", timerB.toString());

            // Configure Timer C
            Integer timerC = sipTimers.getTimerC();
            if (timerC != null)
                properties.setProperty("gov.nist.javax.sip.TIMER_C", timerC.toString());

            // Configure Timer D
            Integer timerD = sipTimers.getTimerD();
            if (timerD != null)
                properties.setProperty("gov.nist.javax.sip.TIMER_D", timerD.toString());

            // Configure Timer F
            Integer timerF = sipTimers.getTimerF();
            if (timerF != null)
                properties.setProperty("gov.nist.javax.sip.TIMER_F", timerF.toString());

            // Configure Timer H
            Integer timerH = sipTimers.getTimerH();
            if (timerH != null)
                properties.setProperty("gov.nist.javax.sip.TIMER_H", timerH.toString());

            // Configure Timer J
            Integer timerJ = sipTimers.getTimerJ();
            if (timerJ != null)
                properties.setProperty("gov.nist.javax.sip.TIMER_J", timerJ.toString());
        }
        log.info("Using system properties to set gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS=" + String.valueOf(this.maxServerTransactions));
        properties.setProperty("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS", String.valueOf(this.maxServerTransactions));

        return properties;
    }

}
