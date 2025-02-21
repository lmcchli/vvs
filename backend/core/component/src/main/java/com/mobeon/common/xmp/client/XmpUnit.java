/**
 * Copyright (c) 2003 Mobeon AB
 * Copyright (c) Abcxyz 2013
 * All Rights Reserved
 * 
 * 24/09/2013   lmcmajo    Updated to remove ObjectQueue as do not want duplicate of ObjectQueue in backend and NTF.
 *                         removed magic numbers.
 *                         Add types to all classes.
 *                         
 * FIXME:       Ideally the XMP threads should be moved back to NTF, as it is only used by NTF and it needs to be NTF state aware i.e locked
 *              unlocked shutting done, via the management interface using ManagedLinkedBlockingQueue and NTFThread.  
 *              This cannot be done in the back-end as it is a common library and should not reference NTF.  
                
                For now this class has been changed to timeout and to allow it to check if it has been asked by NTF to shutdown.
                If there is no current activity.
 *              
 */
package com.mobeon.common.xmp.client;


import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * XmpUnit represents an external unit that supports XMP. It may support one or
 * more XMP services. The XmpUnit is uniquely identified by its host name and
 * port number, i.e. if a host has XMP services on different ports, they are
 * represented by several XmpUnits.
 */
public class XmpUnit {
    private static final int XMP_SEND_TIMEOUT = 15; //Max time to wait when trying to queue XMP request..
    private static final int XMP_RECV_TIMEOUT = 5; //Max time to wait when trying to queue XMP request..
    private String name;
    private String host;    
    private int port;    
    private boolean unitAvailable = true;
    private boolean unitEnabled = true;
    private boolean islocal = true;    
    private XmpClient client = null;
    private String unitName;
    
    private HashMap<String,String> unitNames = new HashMap<String,String>();
    private LinkedBlockingQueue<XmpRequestInfo> queue;
    private ArrayList<XmpConnection> connections;
    
    private long lastSent = 0;
    
    /**
     * Constructor
     *@param host - the host this unit handles,
     *@param port - the port number this unit handles.
     *@param r - the XMP client handling responses from this unit.
     *@param local - true iff this unit is on the local network.
     */
    XmpUnit(int id, String host, int port, XmpClient r, boolean local, String name) {
        int index = host.indexOf(".");
        if( index != -1 ) {
            unitName = "XmpUnit-" + host.substring(0,index) + "-" + id;
        } else {
            unitName = "XmpUnit-" + id;
        }
        client = r;
        client.debug("Creating XMP unit " + host + ":" + port);
        this.host = host;
        this.port = port;
        this.name = name;
        islocal = local;
        client = r;
        queue = new LinkedBlockingQueue<XmpRequestInfo>(20);
        connections = new ArrayList<XmpConnection>();
    }
    
    /*package*/ String getName(String service) {
        String n = unitNames.get(service);
        if( n == null ) {
            return unitName;
        } else {
            return n;
        }
    }
    
    /**
     *@return this units unit name.
     */
    /*package*/ String getName() {
        return unitName;
    }

    /**
     *@return this units host name.
     */
    /*package*/ String getHost() {
        return host;
    }

    /**
     *@return this uniots port number.
     */
    /*package*/ int getPort() {
        return port;
    }
    
    /**
     *@return this units name.
     */
    /*package*/ String getMcrName() {
        return name;
    }

    /**
     *@return true iff this unit is local.
     */
    /*package*/ boolean isLocal() {
        return islocal;
    }
    
    /**
     *setting the unit to local or nonlocal.
     */
    /*package*/ void setLocal(boolean isLocal) {
        this.islocal = isLocal;
    }
    
    /*package*/ void setUnitName(String service, String name) {
        if( unitNames.get(service) == null ) {
            unitNames.put(service, name);
            client.setStatus(true, service, name);
        }
    }
    
    /**
     * Used to disconnect all connections to the unit if the unit misbehaves.
     */
    /*package*/ synchronized void disconnect() {
        client.error("XmpUnit: disconnecting all connections to " + host);
        for( int i=0;i<connections.size();i++ ) {
            XmpConnection conn = connections.get(i);
            conn.disconnect();
        }
        XmpRequestInfo info = null;
            while( (info = queue.poll()) != null && Thread.currentThread().isAlive()) {
                client.sendFailed(info.unit, info.id);
            }
            if (Thread.currentThread().isInterrupted() && !queue.isEmpty()) {
                client.warn("Thread interupted while disconnecting xmp, did not shut down cleanly, exiting...");
            }
       
    }

    /**
     * Stops all activity in this unit and all its connections. As soon as the
     * current activity is completed, all thread stop working (permanently).
     * This method is called when this unit is to be removed.
     */
    /*package*/ void disableUnit() {
        unitEnabled = false;
    }
    
    /**
     * Tells if this unit is useable, i.e if the connection to the other end is
     * working.
     *@return true iff the connection to the external unit is up.
     */
    /*package*/ boolean isAvailable() {
        return unitAvailable;
    }

    /**
     * Used by connections to tell the unit that the host:port refuses connection.
     */
    /*package*/ void connectionDown() {
        client.error("XmpUnit: connection refused.");
        unitAvailable = false;
        disconnect();
        client.removeUnit(this);
    }

   
    
    /*package*/ boolean sendRequest(XmpRequestInfo info) {
        if (!unitAvailable)  { return false; }
        synchronized (connections) {
            if( connections.size() == 0 || 
                connections.size() < client.getMaxConnections() && queue.size() > 5 ) {
                addConnection();
            }
        }
        lastSent = System.currentTimeMillis();
        boolean res;
        try {
            res = queue.offer(info, XMP_SEND_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            client.warn("Thread interupted while queuing xmp request, probably forced shutdown, returning...");
            return false;
        }
        if( !res ) {
            client.warn("Timed out when trying to queue xmp request, after " + XMP_SEND_TIMEOUT + "seconds, queue full.");
        }
        return res;
        
    }
    
    private void addConnection() {
        XmpConnection conn = new XmpConnection(connections.size(), this, client);
        connections.add(conn);
    }
    
    /*package*/ void dropConnection(XmpConnection conn) {
        connections.remove(conn);
        
    }
    
    /**
     *Waits for a request for XMP_RECV_TIMEOUT seconds.
     *@return a request or null if no request exist.
     */
    /*package*/ XmpRequestInfo getRequest() {
        
        XmpRequestInfo info;
        try {
            info = queue.poll(XMP_RECV_TIMEOUT,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
        return info;
    }
    
    /**
     *Tries to send a empty request if no other request has been sent in
     *poll interval.
     */
     synchronized void tryEmptyRequest() {
        long time = System.currentTimeMillis();
        if( time - lastSent > client.getPollInterval()*1000 ) {
            sendEmptyRequest();
        }
    }
    
     boolean sendEmptyRequest() {
        if (!unitAvailable)  { return false; }
        
        try {
            if (!queue.isEmpty())
                 { return false; } //no need to send if something else on queue
            String request = XmpProtocol.makeEmptyRequest();
            XmpRequestInfo info = new XmpRequestInfo(0, null, this, request, "Empty", null );
            lastSent = System.currentTimeMillis();
            return queue.offer(info); //send only if space available on queue. 
        } catch (Exception e) {
            client.error("Unexpected error for empty request" , e);
            return false;
        }
    }

    /**
     * Test method for JUnit
     */
    /*package*/ void setUnitAvailable(boolean available) {
        unitAvailable = available;
    }
    
    /**
     *@return a printable representation of this unit.
     */
    public String toString() {
        return "{XmpUnit host=" + host + " port=" + port
            + (unitEnabled ? " enabled" : " disabled")
            + (unitAvailable ? "" : " not") + " available"
            + (islocal ? " " : " non") + "local}";
    }
}
