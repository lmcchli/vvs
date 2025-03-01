/*
 * Conditions Of Use 
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *  
 * .
 * 
 */
package gov.nist.javax.sip;

import java.util.*;
import java.util.StringTokenizer;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;

import gov.nist.javax.sip.stack.*;

import java.lang.reflect.*;
import java.net.InetAddress;

import gov.nist.core.*;
import gov.nist.core.net.AddressResolver;
import gov.nist.core.net.NetworkLayer;
import org.apache.log4j.Logger;

/**
 * Implementation of SipStack.
 *
 * The JAIN-SIP stack is initialized by a set of properties (see the JAIN SIP
 * documentation for an explanation of these properties
 * {@link javax.sip.SipStack} ). In addition to these, the following are
 * meaningful properties for the NIST SIP stack (specify these in the property
 * array when you create the JAIN-SIP statck):
 * <ul>
 *
 * <li><b>gov.nist.javax.sip.TRACE_LEVEL = integer </b><br/> Currently only TRACE
 * (16) and DEBUG is meaningful. If this is set to 16 or above, then incoming valid
 * messages are logged in SERVER_LOG. If you set this to 32 and specify a
 * DEBUG_LOG then vast amounts of trace information will be dumped in to the
 * specified DEBUG_LOG. The server log accumulates the signaling trace. <a
 * href="{@docRoot}/tools/tracesviewer/tracesviewer.html"> This can be viewed
 * using the trace viewer tool .</a> Please send us both the server log and
 * debug log when reporting non-obvious problems. You can also use the strings
 * DEBUG or INFO for level 32 and 16 respectively</li>
 *
 * <li><b>gov.nist.javax.sip.SERVER_LOG = fileName </b><br/> Log valid
 * incoming messages here. If this is left null AND the TRACE_LEVEL is above 16
 * then the messages are printed to stdout. Otherwise messages are logged in a
 * format that can later be viewed using the trace viewer application which is
 * located in the tools/tracesviewer directory. <font color=red> Mail this to us
 * with bug reports. </font> </li>
 *
 * <li><b>gov.nist.javax.sip.LOG_MESSAGE_CONTENT = true|false </b><br/> Set
 * true if you want to capture content into the log. Default is false. A bad
 * idea to log content if you are using SIP to push a lot of bytes through TCP.
 * </li>
 *
 *
 *
 * <li><b>gov.nist.javax.sip.DEBUG_LOG = fileName </b><br/> Where the debug
 * log goes. <font color=red> Mail this to us with bug reports. </font> </li>
 *
 * <li><b>gov.nist.javax.sip.MAX_MESSAGE_SIZE = integer</b> <br/> Maximum size
 * of content that a TCP connection can read. Must be at least 4K. Default is
 * "infinity" -- ie. no limit. This is to prevent DOS attacks launched by
 * writing to a TCP connection until the server chokes. </li>
 *
 *
 *
 * <li><b>gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS = [true|false] </b> <br/>
 * Default value is true. Setting this to true makes the Stack close the server
 * socket after a Server Transaction goes to the TERMINATED state. This allows a
 * server to protectect against TCP based Denial of Service attacks launched by
 * clients (ie. initiate hundreds of client gransactions). If false (default
 * action), the stack will keep the socket open so as to maximize performance at
 * the expense of Thread and memory resources - leaving itself open to DOS
 * attacks. </li>
 *
 * <li><b>gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS = [true|false] </b> <br/>
 * Default value is true. Setting this to true makes the Stack close the server
 * socket aftera Client Transaction goes to the TERMINATED state. This allows a
 * client release any buffers threads and socket connections associated with a
 * client transaction after the transaction has terminated at the expense of
 * performance. </li>
 *
 * <li> <b>gov.nist.javax.sip.THREAD_POOL_SIZE = integer </b> <br/> Concurrency
 * control for number of simultaneous active threads. If unspecificed, the
 * default is "infinity". This feature is useful if you are trying to build a
 * container.
 * <ul>
 * <li>
 * <li> If this is not specified, <b> and the listener is re-entrant</b>, each
 * event delivered to the listener is run in the context of a new thread. </li>
 * <li>If this is specified and the listener is re-entrant, then the stack will
 * run the listener using a thread from the thread pool. This allows you to
 * manage the level of concurrency to a fixed maximum. Threads are pre-allocated
 * when the stack is instantiated.</li>
 * <li> If this is specified and the listener is not re-entrant, then the stack
 * will use the thread pool thread from this pool to parse and manage the state
 * machine but will run the listener in its own thread. </li>
 * </ul>
 *
 * <li> <b>gov.nist.javax.sip.REENTRANT_LISTENER = true|false </b> <br/> Default
 * is false. Set to true if the listener is re-entrant. If the listener is
 * re-entrant then the stack manages a thread pool and synchronously calls the
 * listener from the same thread which read the message. Multiple transactions
 * may concurrently receive messages and this will result in multiple threads
 * being active in the listener at the same time. The listener has to be written
 * with this in mind. <b> If you want good performance on a multithreaded
 * machine write your listener to be re-entrant and set this property to be true
 * </b></li>
 *
 * <li> <b>gov.nist.javax.sip.MAX_CONNECTIONS = integer </b> <br/> Max number of
 * simultaneous TCP connections handled by stack. </li>
 *
 * <li><b>gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS = integer </b> <br/> Maximum
 * size of server transaction table. The low water mark is 80% of the high water
 * mark. Requests are selectively dropped in the lowater mark to highwater mark
 * range. Requests are unconditionally accepted if the table is smaller than the
 * low water mark. The default highwater mark is 5000 </li>
 *
 * <li><b>gov.nist.javax.sip.PASS_INIVTE_NON2XX_ACK_TO_LISTENER = true|false
 * </b> <br/> If true then the listener will see the ACK for non-2xx responses
 * for server transactions. This is not standard behavior per RFC 3261 (INVITE
 * server transaction state machine) but this is a useful flag for testing. The
 * TCK uses this flag for example. </li>
 *
 * <li><b>gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME = Integer </b>
 * <br/>Max time (seconds) before sending a response to a server transaction.
 * If a response is not sent within this time period, the transaction will
 * be deleted by the stack. Default time is "infinity" - i.e. if the listener
 * never responds, the stack will hang on to a reference for the transaction
 * and result in a memory leak.
 *
 * <li><b>gov.nist.javax.sip.USE_TLS_ACCELERATOR = true|false
 * </b> <br/>Default value is false. Setting this to true permits the
 * delegation of TLS encryption/decryption to an external, non SIP-aware, TLS
 * accelerator hardware. Such deployment requires the SIP stack to make its TLS
 * traffic goes over un-encrypted TCP connections to the TLS accelerator. So
 * all TLS listening points will be listening for plain TCP traffic, and
 * outgoing messages sent with a TLS provider will not be encrypted. Note that
 * this does not affect the transport value in the Via header. Another
 * deployment strategy for TLS acceleration would be to use one or a cluster of
 * outbound proxies that transform the TCP or UDP SIP connection to TLS
 * connections. This scenario does not need the USE_TLS_ACCELERATOR switch, as
 * the messages will be sent using a plain TCP or UDP provider.
 * </li>
 *
 * <li><b>gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_ACK = [true|false]
 * <b></b><br/>Default is false. ACK Server Transaction is a Pseuedo-transaction.
 * If you want termination notification on ACK transactions (so all server
 * transactions can be handled uniformly in user code during cleanup), then
 * set this flag to <it>true</it>.
 * </li>
 *
 * <li> <b>gov.nist.javax.sip.READ_TIMEOUT = integer </b> <br/> This is relevant
 * for incoming TCP connections to prevent starvation at the server. This
 * defines the timeout in miliseconds between successive reads after the first
 * byte of a SIP message is read by the stack. All the sip headers must be
 * delivered in this interval and each successive buffer must be of the content
 * delivered in this interval. Default value is -1 (ie. the stack is wide open
 * to starvation attacks) and the client can be as slow as it wants to be. </li>
 *
 * <li> <b>gov.nist.javax.sip.NETWORK_LAYER = classpath </b> <br/> This is an
 * EXPERIMENTAL property (still under active devlopment). Defines a network
 * layer that allows a client to have control over socket allocations and
 * monitoring of socket activity. A network layer should implement
 * gov.nist.core.net.NetworkLayer. The default implementation simply acts as a
 * wrapper for the standard java.net socket layer. This functionality is still
 * under active development (may be extended to support security and other
 * features). </li>
 *
 * <li><b>gov.nist.javax.sip.ADDRESS_RESOLVER = classpath </b><br/> The fully
 * qualified class path for an implementation of the AddressResolver interface.
 * The AddressResolver allows you to support lookup schemes for addresses
 * that are not directly resolvable to IP adresses using getHostByName. Specifying
 * your own address resolver allows you to customize address lookup.
 * </li>
 *
 * <li><b>gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP= [true| false] </b><br/> Automatically
 * generate a getTimeOfDay timestamp for a retransmitted request if the original request contained
 * a timestamp. This is useful for profiling.
 * </li>
 *
 * <li> <b>gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS = long </b> <br/> Defines
 * how often the application intends to audit the SIP Stack about the health of its internal
 * threads (the property specifies the time in miliseconds between successive audits).
 * The audit allows the application to detect catastrophic failures like an internal
 * thread terminating because of an exception or getting stuck in a deadlock condition.
 * Events like these will make the stack inoperable and therefore require immediate action
 * from the application layer (e.g., alarms, traps, reboot, failover, etc.)
 * Thread audits are disabled by default. If this property is not specified, audits will
 * remain disabled. An example of how to use this property is in src/examples/threadaudit. </li>
 *
 * </ul>
 *
 *
 * @version 1.2 $Revision: 1.55 $ $Date: 2006/11/16 16:17:11 $
 *
 * @author M. Ranganathan <br/>
 *
 *
 *
 *
 */
public class SipStackImpl extends SIPTransactionStack implements
		javax.sip.SipStack {

    private static final Logger log = Logger.getLogger(SipStackImpl.class);

	EventScanner eventScanner;

	private Hashtable<String, ListeningPointImpl> listeningPoints;

	private LinkedList<SipProvider> sipProviders;

	// Flag to indicate that the listener is re-entrant and hence
	// Use this flag with caution.
	boolean reEntrantListener;

	SipListener sipListener;

	// If set to true then a transaction terminated event is
	// delivered for ACK transactions.
	boolean deliverTerminatedEventForAck = false;

	// If set to true then the application want to receive
	// unsolicited NOTIFYs, ie NOTIFYs that don't match any dialog
	boolean deliverUnsolicitedNotify = false;

	/**
	 * Creates a new instance of SipStackImpl.
	 */

	protected SipStackImpl() {
		super();
		this.eventScanner = new EventScanner(this);
		this.listeningPoints = new Hashtable<String, ListeningPointImpl>();
		this.sipProviders = new LinkedList<SipProvider>();

	}

	/**
	 * ReInitialize the stack instance.
	 */
	private void reInitialize() {
		super.reInit();
		this.eventScanner = new EventScanner(this);
		this.listeningPoints = new Hashtable<String, ListeningPointImpl>();
		this.sipProviders = new LinkedList<SipProvider>();
		this.sipListener = null;

	}

	/**
	 * Return true if automatic dialog support is enabled for this stack.
	 *
	 * @return boolean, true if automatic dialog support is enabled for this stack
	 */
	boolean isAutomaticDialogSupportEnabled() {
		return super.isAutomaticDialogSupportEnabled;
	}

	/**
	 * Constructor for the stack.
	 *
	 * @param configurationProperties --
	 *            stack configuration properties including NIST-specific
	 *            extensions.
	 * @throws PeerUnavailableException
	 */
	public SipStackImpl(Properties configurationProperties)
			throws PeerUnavailableException {
		this();
		String address = configurationProperties
				.getProperty("javax.sip.IP_ADDRESS");
		try {
			/** Retrieve the stack IP address */
			if (address != null) {
				// In version 1.2 of the spec the IP address is
				// associated with the listening point and
				// is not madatory.
				super.setHostAddress(address);

			}
		} catch (java.net.UnknownHostException ex) {
			throw new PeerUnavailableException("bad address " + address);
		}

		/** Retrieve the stack name */
		String name = configurationProperties
				.getProperty("javax.sip.STACK_NAME");
		if (name == null)
			throw new PeerUnavailableException("stack name is missing");
		super.setStackName(name);
		// To log debug messages.
		this.logWriter = new LogWriter(configurationProperties);

		// Server log file.
		this.serverLog = new ServerLog(this, configurationProperties);

		// Default router -- use this for routing SIP URIs.
		// Our router does not do DNS lookups.

		this.defaultRouter = new DefaultRouter(this, outboundProxy);

		/** Retrieve the router path */
		String routerPath = configurationProperties
				.getProperty("javax.sip.ROUTER_PATH");
		if (routerPath == null)
			routerPath = "gov.nist.javax.sip.stack.DefaultRouter";
		String outboundProxy = configurationProperties
				.getProperty("javax.sip.OUTBOUND_PROXY");

		try {
			Class routerClass = Class.forName(routerPath);
			Class[] constructorArgs = new Class[2];
			constructorArgs[0] = javax.sip.SipStack.class;
			constructorArgs[1] = String.class;
			Constructor cons = routerClass.getConstructor(constructorArgs);
			Object[] args = new Object[2];
			args[0] = this;
			args[1] = outboundProxy;
			Router router = (Router) cons.newInstance(args);
			super.setRouter(router);
		} catch (InvocationTargetException ex1) {
			getLogWriter().
                            logError("could not instantiate router -- " +
                                     "invocation target problem",
                                     (Exception) ex1.getCause());
			throw new PeerUnavailableException(
					"Cound not instantiate router - check constructor", ex1);
		} catch (Exception ex) {
			getLogWriter().logError("could not instantiate router",
                                                (Exception) ex.getCause());
			throw new PeerUnavailableException("Could not instantiate router",
					ex);
		}

        /* gets the T2 timer, if any */
        final String T2 = "gov.nist.javax.sip.T2";

        String t2Str = configurationProperties.getProperty(T2);
        if (t2Str != null) {
            try {
                setT2(Integer.parseInt(t2Str));
            } catch (NumberFormatException ex) {
                log.warn("T2 is not numeric: " + t2Str);
            }
        }

        /* gets the T4 timer, if any */
        final String T4 = "gov.nist.javax.sip.T4";

        String t4Str = configurationProperties.getProperty(T4);
        if (t4Str != null) {
            try {
                setT4(Integer.parseInt(t4Str));
            } catch (NumberFormatException ex) {
                log.warn("T4 is not numeric: " + t4Str);
            }
        }

        /* gets the timer B, if any */
        final String TIMER_B = "gov.nist.javax.sip.TIMER_B";

        String timerBStr = configurationProperties.getProperty(TIMER_B);
        if (timerBStr != null) {
            try {
                setTimerB(Integer.parseInt(timerBStr));
            } catch (NumberFormatException ex) {
                log.warn("Timer B is not numeric: " + timerBStr);
            }
        }

        /* gets the timer C, if any */
        final String TIMER_C = "gov.nist.javax.sip.TIMER_C";

        String timerCStr = configurationProperties.getProperty(TIMER_C);
        if (timerCStr != null) {
            try {
                setTimerC(Integer.parseInt(timerCStr));
            } catch (NumberFormatException ex) {
                log.warn("Timer C is not numeric: " + timerCStr);
            }
        }

        /* gets the timer D, if any */
        final String TIMER_D = "gov.nist.javax.sip.TIMER_D";

        String timerDStr = configurationProperties.getProperty(TIMER_D);
        if (timerDStr != null) {
            try {
                setTimerD(Integer.parseInt(timerDStr));
            } catch (NumberFormatException ex) {
                log.warn("Timer D is not numeric: " + timerDStr);
            }
        }

        /* gets the timer F, if any */
        final String TIMER_F = "gov.nist.javax.sip.TIMER_F";

        String timerFStr = configurationProperties.getProperty(TIMER_F);
        if (timerFStr != null) {
            try {
                setTimerF(Integer.parseInt(timerFStr));
            } catch (NumberFormatException ex) {
                log.warn("Timer F is not numeric: " + timerFStr);
            }
        }

        /* gets the timer H, if any */
        final String TIMER_H = "gov.nist.javax.sip.TIMER_H";

        String timerHStr = configurationProperties.getProperty(TIMER_H);
        if (timerHStr != null) {
            try {
                setTimerH(Integer.parseInt(timerHStr));
            } catch (NumberFormatException ex) {
                log.warn("Timer H is not numeric: " + timerHStr);
            }
        }

        /* gets the timer J, if any */
        final String TIMER_J = "gov.nist.javax.sip.TIMER_J";

        String timerJStr = configurationProperties.getProperty(TIMER_J);
        if (timerJStr != null) {
            try {
                setTimerJ(Integer.parseInt(timerJStr));
            } catch (NumberFormatException ex) {
                log.warn("Timer J is not numeric: " + timerJStr);
            }
        }


		// The flag that indicates that the default router is to be ignored.
		String useRouterForAll = configurationProperties
				.getProperty("javax.sip.USE_ROUTER_FOR_ALL_URIS");
		this.useRouterForAll = true;
		if (useRouterForAll != null) {
			this.useRouterForAll = "true".equalsIgnoreCase(useRouterForAll);
		}

		/*
		 * Retrieve the EXTENSION Methods. These are used for instantiation of
		 * Dialogs.
		 */
		String extensionMethods = configurationProperties
				.getProperty("javax.sip.EXTENSION_METHODS");

		if (extensionMethods != null) {
			java.util.StringTokenizer st = new java.util.StringTokenizer(
					extensionMethods);
			while (st.hasMoreTokens()) {
				String em = st.nextToken(":");
				if (em.equalsIgnoreCase(Request.BYE)
						|| em.equalsIgnoreCase(Request.INVITE)
						|| em.equalsIgnoreCase(Request.SUBSCRIBE)
						|| em.equalsIgnoreCase(Request.NOTIFY)
						|| em.equalsIgnoreCase(Request.ACK)
						|| em.equalsIgnoreCase(Request.OPTIONS))
					throw new PeerUnavailableException("Bad extension method "
							+ em);
				else
					this.addExtensionMethod(em);
			}
		}

		// Set the auto dialog support flag.
		super.isAutomaticDialogSupportEnabled = configurationProperties
				.getProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "on")
				.equalsIgnoreCase("on");

		if ( configurationProperties.getProperty
			("gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME") != null ) {
			super.maxListenerResponseTime =
			Integer.parseInt( configurationProperties.getProperty
			("gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME"));
			if (super.maxListenerResponseTime <= 0)
				throw new PeerUnavailableException
				("Bad configuration parameter gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME : should be positive");
		} else {
			super.maxListenerResponseTime = -1;
		}

		this.useTlsAccelerator = false;
		String useTlsAcceleratorFlag = configurationProperties
				.getProperty("gov.nist.javax.sip.USE_TLS_ACCELERATOR");

		if (useTlsAcceleratorFlag != null && "true".equalsIgnoreCase(useTlsAcceleratorFlag.trim())) {
			this.useTlsAccelerator = true;
		}

		this.deliverTerminatedEventForAck = configurationProperties.getProperty
		("gov.nist.javax.sip.DELIVER_TERMINATED_EVENT_FOR_ACK", "false").equalsIgnoreCase("true");

		this.deliverUnsolicitedNotify = configurationProperties.getProperty
		("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "false").equalsIgnoreCase("true");


		String forkedSubscriptions = configurationProperties
				.getProperty("javax.sip.FORKABLE_EVENTS");
		if (forkedSubscriptions != null) {
			StringTokenizer st = new StringTokenizer(forkedSubscriptions);
			while (st.hasMoreTokens()) {
				String nextEvent = st.nextToken();
				this.forkedEvents.add(nextEvent);
			}
		}

		// The following features are unique to the NIST implementation.

		/*
		 * gets the NetworkLayer implementation, if any. Note that this is a
		 * NIST only feature.
		 */

		final String NETWORK_LAYER_KEY = "gov.nist.javax.sip.NETWORK_LAYER";

		if (configurationProperties.containsKey(NETWORK_LAYER_KEY)) {
			String path = configurationProperties
					.getProperty(NETWORK_LAYER_KEY);
			try {
				Class clazz = Class.forName(path);
				Constructor c = clazz.getConstructor(new Class[0]);
				networkLayer = (NetworkLayer) c.newInstance(new Object[0]);
			} catch (Exception e) {
				throw new PeerUnavailableException(
						"can't find or instantiate NetworkLayer implementation: "
								+ path);
			}
		}

		final String ADDRESS_RESOLVER_KEY = "gov.nist.javax.sip.ADDRESS_RESOLVER";

		if (configurationProperties.containsKey(ADDRESS_RESOLVER_KEY)) {
			String path = configurationProperties
					.getProperty(ADDRESS_RESOLVER_KEY);
			try {
				Class clazz = Class.forName(path);
				Constructor c = clazz.getConstructor(new Class[0]);
				this.addressResolver = (AddressResolver) c.newInstance(new Object[0]);
			} catch (Exception e) {
				throw new PeerUnavailableException(
						"can't find or instantiate AddressResolver implementation: "
								+ path);
			}
		}

		String maxConnections = configurationProperties
				.getProperty("gov.nist.javax.sip.MAX_CONNECTIONS");
		if (maxConnections != null) {
			try {
				this.maxConnections = new Integer(maxConnections);
			} catch (NumberFormatException ex) {
                            if (log.isDebugEnabled())
				log.debug("max connections - bad value "
                                          + ex.getMessage());
			}
		}

		String threadPoolSize = configurationProperties
				.getProperty("gov.nist.javax.sip.THREAD_POOL_SIZE");
		if (threadPoolSize != null) {
			try {
				this.threadPoolSize = new Integer(threadPoolSize);
			} catch (NumberFormatException ex) {
                            if (log.isDebugEnabled())
				log.debug("thread pool size - bad value "
                                          + ex.getMessage());
			}
		}

		String transactionTableSize = configurationProperties
				.getProperty("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
		if (transactionTableSize != null) try {
            this.serverTransactionTableHighwaterMark =
                    new Integer(transactionTableSize);
            this.serverTransactionTableLowaterMark =
                    this.serverTransactionTableHighwaterMark * 80 / 100;
            // Lowater is 80% of highwater
        } catch (NumberFormatException ex) {
            if (log.isDebugEnabled())
                log.debug("transaction table size - bad value "
                        + ex.getMessage());
        }

		super.cacheServerConnections = true;
		String flag = configurationProperties
				.getProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS");

		if (flag != null && "false".equalsIgnoreCase(flag.trim())) {
			super.cacheServerConnections = false;
		}

		super.cacheClientConnections = true;
		String cacheflag = configurationProperties
				.getProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS");

		if (cacheflag != null && "false".equalsIgnoreCase(cacheflag.trim())) {
			super.cacheClientConnections = false;
		}

		String readTimeout = configurationProperties
				.getProperty("gov.nist.javax.sip.READ_TIMEOUT");
		if (readTimeout != null) {
			try {

				int rt = Integer.parseInt(readTimeout);
				if (rt >= 100) {
					super.readTimeout = rt;
				} else {
                                    if (log.isDebugEnabled())
					log.debug("Value too low " +
                                                  readTimeout);
				}
			} catch (NumberFormatException nfe) {
				// Ignore.
                            if (log.isDebugEnabled())
				log.debug("Bad read timeout " + readTimeout);
			}
		}

		// Get the address of the stun server.

		String stunAddr = configurationProperties
				.getProperty("gov.nist.javax.sip.STUN_SERVER");

		if (stunAddr != null)
			this.logWriter.logWarning("Ignoring obsolete property "
					+ "gov.nist.javax.sip.STUN_SERVER");

		String maxMsgSize = configurationProperties
				.getProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE");

		try {
			if (maxMsgSize != null) {
				super.maxMessageSize = new Integer(maxMsgSize);
				if (super.maxMessageSize < 4096)
					super.maxMessageSize = 4096;
			} else {
				// Allow for "infinite" size of message
				super.maxMessageSize = 0;
			}
		} catch (NumberFormatException ex) {
                    if (log.isDebugEnabled())
			log.debug("maxMessageSize - bad value " +
                                  ex.getMessage());
		}

		String rel = configurationProperties
				.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
		this.reEntrantListener = (rel != null && "true".equalsIgnoreCase(rel));

		// Check if a thread audit interval is specified
		String interval = configurationProperties.getProperty("gov.nist.javax.sip.THREAD_AUDIT_INTERVAL_IN_MILLISECS");
		if (interval != null) {
			try {
				// Make the monitored threads ping the auditor twice as fast as the audits
				getThreadAuditor().setPingIntervalInMillisecs(
                        Long.valueOf(interval) / 2);
			} catch (NumberFormatException ex) {
                            if (log.isDebugEnabled())
				log.debug("THREAD_AUDIT_INTERVAL_IN_MILLISECS" +
                                          " - bad value [" + interval + "] " +
                                          ex.getMessage());
			}
		}

		// JvB: added property for testing
		this.non2XXAckPassedToListener = Boolean.valueOf(
                configurationProperties
                        .getProperty(
                        "gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER",
                        "false"));
		
		this.generateTimeStampHeader = Boolean.valueOf(
                configurationProperties
                        .getProperty(
                        "gov.nist.javax.sip.AUTO_GENERATE_TIMESTAMP",
                        "false"));


            String tmp;
            tmp = configurationProperties.getProperty(
                    "gov.nist.javax.sip.CLIENT_CONNECTION_TIMEOUT");
            if (tmp != null) {
                try {
                    setClientConnectionTimeout(Integer.parseInt(tmp));
                } catch (NumberFormatException e) {
                    log.warn("CLIENT_CONNECTION_TIMEOUT is not numeric: " + tmp);
                } catch (IllegalArgumentException e) {
                    log.warn("CLIENT_CONNECTION_TIMEOUT is out of range: " +
                            tmp + " (" + e.getMessage() + ")");
                }
            }

            tmp = configurationProperties.getProperty(
                    "gov.nist.javax.sip.SERVER_CONNECTION_TIMEOUT");
            if (tmp != null) {
                try {
                    setServerConnectionTimeout(Integer.parseInt(tmp));
                } catch (NumberFormatException e) {
                    log.warn("SERVER_CONNECTION_TIMEOUT is not numeric: " + tmp);
                } catch (IllegalArgumentException e) {
                    log.warn("SERVER_CONNECTION_TIMEOUT is out of range: " +
                            tmp + " (" + e.getMessage() + ")");
                }
            }

        }

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#createListeningPoint(java.lang.String, int,
	 *      java.lang.String)
	 */
	public synchronized ListeningPoint createListeningPoint(String address,
			int port, String transport) throws TransportNotSupportedException,
			InvalidArgumentException {
		getLogWriter().logDebug(
				"createListeningPoint : address = " + address + " port = "
						+ port + " transport = " + transport);

		if (address == null)
			throw new NullPointerException(
					"Address for listening point is null!");
		if (transport == null)
			throw new NullPointerException("null transport");
		if (port <= 0)
			throw new InvalidArgumentException("bad port");

		if (!transport.equalsIgnoreCase("UDP")
				&& !transport.equalsIgnoreCase("TLS") // Added by Daniel J.
				// Martinez Manzano
				// <dani@dif.um.es>
				&& !transport.equalsIgnoreCase("TCP"))
			throw new TransportNotSupportedException("bad transport "
					+ transport);

		/** Reusing an old stack instance */
		if (!this.isAlive()) {
			this.toExit = false;
			this.reInitialize();
		}

		String key = ListeningPointImpl.makeKey(address, port, transport);

		ListeningPointImpl lip = listeningPoints.get(key);
		if (lip != null) {
			return lip;
		} else {
			try {
				InetAddress inetAddr = InetAddress.getByName(address);
                lip = new ListeningPointImpl(this, inetAddr, port, transport);

				MessageProcessor messageProcessor =
                        this.createMessageProcessor(lip);
				if (this.isLoggingEnabled()) {
					this.getLogWriter().logDebug(
							"Created Message Processor: " + address
									+ " port = " + port + " transport = "
									+ transport);
				}

				lip.setMessageProcessor(messageProcessor);
				this.listeningPoints.put(key, lip);
				// start processing messages.
				messageProcessor.start();
				return lip;
			} catch (java.io.IOException ex) {
				getLogWriter().logError(
						"Invalid argument address = " + address + " port = "
								+ port + " transport = " + transport);
				throw new InvalidArgumentException(ex.getMessage(),ex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#createSipProvider(javax.sip.ListeningPoint)
	 */
	public SipProvider createSipProvider(ListeningPoint listeningPoint)
			throws ObjectInUseException {
		if (listeningPoint == null)
			throw new NullPointerException("null listeningPoint");
		if (this.getLogWriter().isLoggingEnabled())
			this.getLogWriter()
					.logDebug("createSipProvider: " + listeningPoint);
		ListeningPointImpl listeningPointImpl = (ListeningPointImpl) listeningPoint;
		if (listeningPointImpl.getSipProvider() != null)
			throw new ObjectInUseException("Provider already attached!");

		SipProviderImpl provider = new SipProviderImpl(this);

		provider.setListeningPoint(listeningPointImpl);
		listeningPointImpl.setSipProvider(provider);
		this.sipProviders.add(provider);
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#deleteListeningPoint(javax.sip.ListeningPoint)
	 */
	public void deleteListeningPoint(ListeningPoint listeningPoint)
			throws ObjectInUseException {
		if (listeningPoint == null)
			throw new NullPointerException("null listeningPoint arg");
		ListeningPointImpl lip = (ListeningPointImpl) listeningPoint;
		// Stop the message processing thread in the listening point.
		super.removeMessageProcessor(lip.getMessageProcessor());
		String key = lip.getKey();
		this.listeningPoints.remove(key);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#deleteSipProvider(javax.sip.SipProvider)
	 */
	public void deleteSipProvider(SipProvider sipProvider)
			throws ObjectInUseException {

		if (sipProvider == null)
			throw new NullPointerException("null provider arg");
		SipProviderImpl sipProviderImpl = (SipProviderImpl) sipProvider;

		// JvB: API doc is not clear, but in_use ==
		// sipProviderImpl.sipListener!=null
		// so we should throw if app did not call removeSipListener
		// sipProviderImpl.sipListener = null;
		if (sipProviderImpl.sipListener != null) {
			throw new ObjectInUseException(
					"SipProvider still has an associated SipListener!");
		}

		sipProviderImpl.removeListeningPoints();

		// Bug reported by Rafael Barriuso
		sipProviderImpl.stop();
		sipProviders.remove(sipProvider);
		if (sipProviders.isEmpty()) {
			this.stopStack();
		}
	}

	/**
	 * Get the IP Address of the stack.
	 * 
	 * @see javax.sip.SipStack#getIPAddress()
	 * @deprecated
	 */
	public String getIPAddress() {
		return super.getHostAddress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#getListeningPoints()
	 */
	public java.util.Iterator getListeningPoints() {
		return this.listeningPoints.values().iterator();
	}

	/**
	 * Return true if retransmission filter is active.
	 * 
	 * @see javax.sip.SipStack#isRetransmissionFilterActive()
	 * @deprecated
	 */
	public boolean isRetransmissionFilterActive() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#getSipProviders()
	 */
	public java.util.Iterator getSipProviders() {
		return this.sipProviders.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#getStackName()
	 */
	public String getStackName() {
		return this.stackName;
	}

	/**
	 * Finalization -- stop the stack on finalization. Exit the transaction
	 * scanner and release all resources.
	 * 
	 * @see java.lang.Object#finalize()
	 */
	public void finalize() {
		this.stopStack();
	}

	/**
	 * This uses the default stack address to create a listening point.
	 * 
	 * @see javax.sip.SipStack#createListeningPoint(java.lang.String, int,
	 *      java.lang.String)
	 * @deprecated
	 */
	public ListeningPoint createListeningPoint(int port, String transport)
			throws TransportNotSupportedException, InvalidArgumentException {
		if (super.stackAddress == null)
			throw new NullPointerException(
					"Stack does not have a default IP Address!");
		return this.createListeningPoint(super.stackAddress, port, transport);
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#stop()
	 */
	public void stop() {
		if (getLogWriter().isLoggingEnabled()) {
			getLogWriter().logDebug("stopStack -- stoppping the stack");
		}
		this.stopStack();
		this.sipProviders = new LinkedList<SipProvider>();
		this.listeningPoints = new Hashtable<String, ListeningPointImpl>();
		this.eventScanner.forceStop();
		this.eventScanner = null;

	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipStack#start()
	 */
	public void start() throws ProviderDoesNotExistException, SipException {
		// Start a new event scanner if one does not exist.
		if ( this.eventScanner == null ) {
			this.eventScanner = new EventScanner(this);
		}
		
	}

	/**
	 * Get the listener for the stack. A stack can have only one listener. To
	 * get an event from a provider, the listener has to be registered with the
	 * provider. The SipListener is application code.
	 * 
	 * @return -- the stack SipListener
	 * 
	 */
	protected SipListener getSipListener() {
		return this.sipListener;
	}

	

}
