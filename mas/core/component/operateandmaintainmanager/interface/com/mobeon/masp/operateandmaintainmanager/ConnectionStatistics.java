package com.mobeon.masp.operateandmaintainmanager;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class ConnectionStatistics implements Serializable {
    //private static ConnectionMonitorInfo monitorConnectionInfo;                // for monitoring connections in MAS
    //private static ConnectionMonitor connectionMonitor;                        // registered connection monitor (registerConnectionMonitor)
    //private Hashtable<String,ConsumedServiceEntry> lstConsumedServices; // holds list of registered consumed services

    //private Hashtable<String,ServiceEnablerTmp> lstServiceEnablers;        // holds list of registered service enablers
    private ConcurrentHashMap lstServiceEnablers;        // holds list of registered service enablers


    //public void setConsumedServices(Hashtable<String,ConsumedServiceEntry> lstConsumedServices)   {
    //    this.lstConsumedServices = lstConsumedServices;
    //}

//    public void setServiceEnablers(Hashtable<String,ServiceEnablerTmp> lstServiceEnablers)   {
//        this.lstServiceEnablers = lstServiceEnablers;
//    }

    public void setServiceEnablers(ConcurrentHashMap lstServiceEnablers)   {
        this.lstServiceEnablers = lstServiceEnablers;
    }

   // public Hashtable<String,ConsumedServiceEntry> getConsumedServices()   {
   //     return this.lstConsumedServices;
   // }

    public ConcurrentHashMap getServiceEnablers()   {
       return this.lstServiceEnablers;
    }


}
