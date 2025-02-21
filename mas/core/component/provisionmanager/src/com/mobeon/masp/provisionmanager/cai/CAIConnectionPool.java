/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.provisionmanager.cai;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

/**
 * Implements a connection pool towards a CAI server.
 *
 * @author ermmaha
 */
public class CAIConnectionPool {
    private static final ILogger log = ILoggerFactory.getILogger(CAIConnectionPool.class);
    /**
     * The max number of allowed connections
     */
    private int maxSize = 5;
    /**
     * The timelimit (ms) to wait for a free connection if the pool is full. Same value is used for socket timeout on
     * CAIConnection too.
     */
    private int timeoutLimit = 10 * 1000;
    /**
     * The timelimit (ms) for a connection to be idle before it is physically closed.
     */
    private int idleTimeoutLimit = 60 * 1000; // 60 sec
    /**
     * Free CAI connections not used.
     */
    private List<CAIConnection> freeConnections = new ArrayList<CAIConnection>();
    /**
     * CAI connections in use.
     */
    private List<CAIConnection> usedConnections = new ArrayList<CAIConnection>();
    /**
     * For debug purposes
     */
    private CAICommSpy caiCommSpy;

    private String host;
    private int port;
    private String adminUid;
    private String adminPwd;

    public CAIConnectionPool(String host, int port, String adminUid, String adminPwd) {
        this.host = host;
        this.port = port;
        this.adminUid = adminUid;
        this.adminPwd = adminPwd;
    }

    /**
     * Set a CAICommSpy object to get callbacks for debugging the pool
     *
     * @param caiCommSpy
     */
    public void setCommSpy(CAICommSpy caiCommSpy) {
        this.caiCommSpy = caiCommSpy;
    }

    /**
     * Gets a connection from the pool
     *
     * @return a connection that is free to use
     */
    public synchronized CAIConnection getConnection() throws CAIException, IOException {
        if (log.isDebugEnabled()) log.debug("getConnection()");
        if (caiCommSpy != null) debug("In getConnection: usedConnections.size=" + usedConnections.size() + ", freeConnections.size=" + freeConnections.size());
        checkForUnbehavingClients();

        CAIConnection conn = removeConnection();
        if (conn == null) {
            conn = obtainNew();
        }

        if (conn == null) {
            try {
                conn = waitForConnection();
            }
            catch (TimeoutException exc) {
                throw new CAIException("Timeout. No free connections are available to server " + printConnection());
            }
        }

        usedConnections.add(conn);

        if (log.isDebugEnabled()) log.debug("getConnection() returns " + conn);
        return conn;
    }

    /**
     * Returns a connection back to the pool
     *
     * @param conn CAIConnection to be put back into the pool
     */
    public synchronized void returnConnection(CAIConnection conn) {
        if (log.isDebugEnabled()) log.debug("returnConnection(conn=" + conn + ")");
        if (caiCommSpy != null) debug("In returnConnection: usedConnections.size=" + usedConnections.size() + ", freeConnections.size=" + freeConnections.size());

        usedConnections.remove(conn);
        if (conn.isConnected()) {
            freeConnections.add(conn);
        } else {
            // Invalid (not connected) connection, let go
            if (caiCommSpy != null) debug("Releasing connection to " + printConnection() + "...");
        }

        //Release lock for the threads that are waiting for a free connection
        notify();
        if (log.isDebugEnabled()) log.debug("returnConnection(CAIConnection) returns void");
    }

    /**
     * @return int Number of connections in pool.
     */
    public int getSize() {
        return usedConnections.size() + freeConnections.size();
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * @param timeoutLimit in milliseconds
     */
    public void setTimeoutLimit(int timeoutLimit) {
        this.timeoutLimit = timeoutLimit;
    }

    /**
     * @param idleTimeoutLimit in milliseconds
     */
    public void setIdleTimeoutLimit(int idleTimeoutLimit) {
        this.idleTimeoutLimit = idleTimeoutLimit;
    }

    /**
     * Creates a new CAIConnection and then connects it.
     *
     * @return A new connected CAIConnection object.
     */
    private CAIConnection obtainNew() throws IOException, CAIException {
        if (getSize() >= maxSize) {
            if (caiCommSpy != null) debug("Max Poolsize (" + maxSize + ") reached, no more connections can be added");
            return null;
        }

        if (caiCommSpy != null) debug("Connecting to " + printConnection() + "...");

        CAIConnection conn = new CAIConnection(adminUid, adminPwd);
        conn.setSocketTimeout(timeoutLimit);
        conn.connect(host, port);

        if (caiCommSpy != null) debug("...connected");
        return conn;
    }

    /**
     * Removes the connection from the pool and disconnects it. The CAIConnection is closed.
     *
     * @param conn CAIConnection to release
     */
    private void releaseConnection(CAIConnection conn) {
        if (caiCommSpy != null) debug("Releasing connection " + printConnection() + "...");
        conn.disconnect();
    }

    private CAIConnection waitForConnection() throws TimeoutException {
        long tStart = System.currentTimeMillis();

        for (; ;) {
            try {
                while (freeConnections.size() == 0) {
                    // timeLeft throws TimeoutException.
                    if (caiCommSpy != null) debug("Waiting for a free connection...");
                    wait(timeLeft(tStart));
                    if (caiCommSpy != null) debug("...done waiting");
                }
                return removeConnection();
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    private long timeLeft(long tStart) throws TimeoutException {
        long result = timeoutLimit - (System.currentTimeMillis() - tStart);
        if (caiCommSpy != null) debug("Time left to wait: " + result);

        if (result > 0)
            return result;
        else
            throw new TimeoutException("Timeout on CAIConnection " + printConnection());
    }

    private synchronized CAIConnection removeConnection() {
        if (freeConnections.isEmpty()) {
            return null;
        }
        return freeConnections.remove(0);
    }

    /**
     * A check is made to look for users who do not return a connection.
     * This is done by checking the lastTouchedTime in the CAIConnection class.
     */
    private synchronized void checkForUnbehavingClients() {
        Iterator<CAIConnection> it = usedConnections.iterator();
        while (it.hasNext()) {
            CAIConnection conn = it.next();
            printTouchedInfo(conn);

            if (conn.getSinceTouched() > idleTimeoutLimit) {
                if (caiCommSpy != null) debug("Connection " + conn + " has been idle longer than " + idleTimeoutLimit + " ms");
                it.remove();
                releaseConnection(conn);
            }
        }
    }

    private String printConnection() {
        StringBuffer buf = new StringBuffer();
        buf.append(host);
        buf.append(":");
        buf.append(port);
        return buf.toString();
    }

    private void printTouchedInfo(CAIConnection conn) {
        if (conn != null) {
            debug("Time since last used: " + conn.getSinceTouched() + " ms");
        }
    }

    private void debug(String msg) {
        if (caiCommSpy != null) {
            caiCommSpy.debug("CAIConnectionPool: " + msg);
        }
    }

    public synchronized void setPassword(String password) {
        if (log.isDebugEnabled()) log.debug("setPassword(password=" + password + ")");
        this.adminPwd = password;
        if (log.isDebugEnabled()) log.debug("setPassword(String) returns void");
    }
}
