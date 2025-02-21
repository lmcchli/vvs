/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp.client;

import java.util.*;


public class XmpPendingRequests extends Thread {
    private Hashtable requestInfos;
    private LinkedList timeOrderedList;
    private XmpClient client;
    
    
    /** Creates a new instance of XmpPendingRequests */
    public XmpPendingRequests(XmpClient client) {
        super("XmpPendingRequests");
        requestInfos = new Hashtable();
        timeOrderedList = new LinkedList();
        this.client = client;
        start();
    }
    
    
    public void put(XmpRequestInfo info) {
        requestInfos.put(new Integer(info.id), info);
        timeOrderedList.addLast(info);
    }
    
    public XmpRequestInfo get(Integer id) {
        return (XmpRequestInfo) requestInfos.get(id);
    }
    
    public XmpRequestInfo remove(Integer id) {
        return (XmpRequestInfo) requestInfos.remove(id);
    }
    
    public void run() {
        while(client.isKeepRunning()) {
            try {
                sleep(1000);
                removeOldInfos();
            } catch(Exception e) {
                // do nothing
            }
        }
    }
    
    
    private void removeOldInfos() {
        Date now = new Date();
        if( !timeOrderedList.isEmpty() ) {
            XmpRequestInfo info = (XmpRequestInfo) timeOrderedList.getFirst();
            while( info != null && info.expiryTime.before(now) ) {
                timeOrderedList.removeFirst();
                if( requestInfos.remove(new Integer(info.id)) != null ) {
                    client.handleTimeout(info);
                }
                info = (XmpRequestInfo) timeOrderedList.getFirst();
            }
        }
    }
}
