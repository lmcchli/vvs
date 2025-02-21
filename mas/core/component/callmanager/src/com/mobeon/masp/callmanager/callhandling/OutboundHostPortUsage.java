package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashSet;

/**
 * Singleton class responsible for keeping track of all host:port currently in
 * use for outbound streams. This class shall be used to check whether a
 * host and port combination already is in use when creating a new outbound
 * stream. This check is done to prevent two calls to accidently be setup with
 * the same host and port.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class OutboundHostPortUsage {

    private static final ILogger log =
            ILoggerFactory.getILogger(OutboundHostPortUsage.class);

    private static final OutboundHostPortUsage INSTANCE = new OutboundHostPortUsage();

    private final HashSet<String> usedHostPortCombinations = new HashSet<String>();

    /** @return The single instance. */
    public static OutboundHostPortUsage getInstance() {
        return INSTANCE;
    }

    /** Creates the single instance. */
    private OutboundHostPortUsage() {
    }

    /**
     * Adds a new host and port that shall be used for outbound communication.
     * @param host
     * @param port
     * @throws IllegalStateException if the host and port combination was
     * already in use.
     */
    public synchronized void addNewHostAndPort(String host, int port)
            throws IllegalStateException {

        if (log.isDebugEnabled())
            log.debug("Trying to add host <" + host + "> and port <" + port +
                    "> for outbound communication.");

        if ((host != null) && (port > 0 )) {
            // Adding a new element to the set returns true if the set did not
            // already contain the specified element
            boolean newInSet = usedHostPortCombinations.add(host + ":" + port);

            if (!newInSet) {
                String message = "Host <" + host + "> and port <" + port +
                        "> is already in use.";

                if (log.isDebugEnabled())
                    log.debug(message);

                throw new IllegalStateException(message);
            }
        }
    }

    /**
     * Used when a host and port combination no longer shall be used for
     * outbound communication. It is then free to use for new connections.
     * @param host
     * @param port
     */
    public synchronized void removeHostAndPort(String host, int port) {
        if (log.isDebugEnabled())
            log.debug("Trying to remove host <" + host + "> and port <" + port +
                    "> from outbound communication.");
        if ((host != null) && (port > 0 )) {
            usedHostPortCombinations.remove(host + ":" + port);
        }
    }

    /**
     * Removes all used host and port combinations.
     */
    public synchronized void clear() {
        if (log.isDebugEnabled())
            log.debug("Clearing used connections.");
        usedHostPortCombinations.clear();
    }
}
