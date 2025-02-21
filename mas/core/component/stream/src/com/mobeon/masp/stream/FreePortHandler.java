/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * FreePortHandler is responsible for keeping a record of available port numbers.
 * The class provide methods for locking (allocate) and releasing (deallocate)
 * port numbers.
 * Port numbers are locked in pairs (N and N+1, where N is an even number within
 * a predfined/configured range).
 * Setting up an RTP session one should use the FreePortHandler to reserve the
 * nescessary ports. Each port pair correspond to one RTP (N) and one RTCP (N+1)
 * port number. Please note that the FreePortHandler only handles the port numbers
 * so you have to ensure that the actual port really is free before the port number(s)
 * is released.
 * FreePortHandler implements the Singleton pattern.
 */
public class FreePortHandler {
    private static ILogger logger = ILoggerFactory.getILogger(FreePortHandler.class);
    private static FreePortHandler singletonInstance = new FreePortHandler();
    private int firstPort = 4712;
    private int numberOfPortPairs = 10;
    private int nextFreeCandidate = 0;
    private boolean[] freePortPairs;
    private boolean initialized;
    private final Object lock = new Object();

    /**
     * Initializes the port handler.
     * Currently the FreePortHandler is initialized by the {@link StreamFactoryImpl}.
     */
    public void initilialize() {
        synchronized (lock) {
            if (initialized) {
                logger.debug("Initalize called more than once, the second call was ignored !");
            } else {
                freePortPairs = new boolean[numberOfPortPairs];
                for (int i = 0; i < numberOfPortPairs; i++) {
                    freePortPairs[i] = true;
                }
                nextFreeCandidate = 0;
                initialized = true;
            }
        }
    }

    public void unInitialize() {
        synchronized (lock) {
            initialized = false;
        }
    }

    /**
     * Private default constructor.
     */
    private FreePortHandler() {
    }

    /**
     * Singleton get instance method.
     *
     * @return a reference to a FreePortHandler singleton object.
     */
    public static FreePortHandler getInstance() {
        return singletonInstance;
    }

    /**
     * Locks and returns the first port number of a free port pair.
     *
     * @return a port number N (the second port number is N+1).
     */
    public int lockPair() {
        synchronized (lock) {
            for (int counter = 0; counter < numberOfPortPairs; ++counter) {
                int index = calculateIndex();
                if (freePortPairs[index]) {
                    freePortPairs[index] = false;
                    int pair = index * 2 + firstPort;
                    if(logger.isDebugEnabled())
                        logger.debug("Allocated RTP ports: " + pair);
                    return pair;
                }
            }
            logger.error("Could not allocate free port");
            return -1;
        }
    }

    /**
     * Releases a port pair.
     * By releasing port N we assume that port N+1 also is free.
     * (Port N is RTP and port N+1 is RTCP).
     *
     * @param port an even integer.
     */
    public void releasePair(int port) {
        if(logger.isDebugEnabled())
            logger.debug("Deallocating RTP ports: " + port);
        // Well this strictly doesn't need to be synchronized, but we
        // still would like to tell the vm that these changes should be
        // effective immediately
        synchronized (lock) {
            // The offset indicates where we are in the port range
            // Ensuring that the offset is an even number it can be 
            // divided by to in order to get a free port index.
            int offset = port - firstPort;
            int index = -1;

            if (offset == 0) index = 0;
            else if (offset > 0 && offset % 2 == 0) index = offset / 2;

            if (index >= 0) freePortPairs[index] = true;
            // TODO: handle invalid port. throw?
        }
    }

    /**
     * Setter for the free port pool size.
     * Defines the number of availble RTP ports.
     *
     * @param numberOfPortPairs the number of RTP port pairs.
     */
    public void setSize(int numberOfPortPairs) {
        synchronized(lock) {
            if(!initialized)
                this.numberOfPortPairs = numberOfPortPairs;
        }
    }

    /**
     * Return the size of the port pool (the size of the range).
     *
     * @return the number of port pairs.
     */
    public int getSize() {
        return numberOfPortPairs;
    }

    /**
     * Sets the base port number (the fist port in the port number range).
     *
     * @param firstPort a port number (first in range).
     */
    public void setBase(int firstPort) {
        synchronized(lock) {
            if(!initialized)
                this.firstPort = firstPort;
        }
    }

    /**
     * Returns the base port number (first port number in port number range).
     *
     * @return an even port number (first in range).
     */
    public int getBase() {
        return firstPort;
    }

    int calculateIndex()
    {
        int index = nextFreeCandidate;
        nextFreeCandidate = (nextFreeCandidate+1)%numberOfPortPairs;
        return index;
    }
}
