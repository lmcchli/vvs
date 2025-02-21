package com.mobeon.masp.monitor;

import com.mobeon.masp.rpcclient.MonitorOperations;
import com.mobeon.masp.rpcclient.RpcMonitor;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
//import com.mobeon.masp.operateandmaintainmanager.MonitorOperations;
import com.mobeon.masp.operateandmaintainmanager.OMMConfiguration;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoRead;
import com.mobeon.masp.operateandmaintainmanager.ConnectionStatistics;

import java.util.HashMap;
import java.io.IOException;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class Connection {
    ILogger log;

    private MonitorOperations mo;
   // private boolean connected;
    private boolean monitorStarted;
    private Integer attempt;
    private Boolean retry;
    private String connectionMsg;
    private OMMConfiguration ommconfig;

    

    Connection(){
        retry = true;
        log = ILoggerFactory.getILogger(Connection.class);
        log.debug("Create Connection");
        attempt=0;
        connectionMsg = "Connecting.";
        ommconfig = new OMMConfiguration();

        //ommconfig.getHostName(),ommconfig.getPort()
        mo = new RpcMonitor(ommconfig.getHostName(),ommconfig.getPort());

    }

    public void resetRetryes(){
        attempt = 0;
        retry = true;
    }

    public boolean retry(){
        return retry;
    }

    public HashMap<String, SessionInfoRead> getConnectionData(){
        HashMap<String, SessionInfoRead> data = new HashMap<String, SessionInfoRead>();
        String result;
        // If monitor not started. start
        if (retry ){
            if (!mo.monitorStarted()){
                try {
                    result = mo.startMonitor();
                    attempt++;
                    //monitorStarted=true;
                    monitorStarted=result.contains("started");

                    if(result.contains("noconnectionmonitor")) {
                        retry = false;

                        connectionMsg = "No monitor registered.";
                    }

                    if(monitorStarted)
                        connectionMsg = "Connected.";

                } catch (IOException e) {
                    //TODO. log message
                    //e.printStackTrace();
                    monitorStarted=false;
                }
            }

            // MonitorImpl startedt.
            // Retreive data.

            if (mo.monitorStarted()){
                try {
                    data = mo.getMonitorConnectionData();
                } catch (IOException e) {
                    log.error("Error retreiving connection data from RPC Server.");
                    // TODO Log message
                    e.printStackTrace();
                }
            }

            if (attempt > ommconfig.getRpcMaxNoOfRetries()) {
                retry = false;
                connectionMsg = "Connection failed.";
            }
        }

        return data;
    }


    //public Hashtable<String, Vector> getStatisticData(){
    public ConnectionStatistics getStatisticData(){

        //Hashtable<String, Vector> data = new Hashtable<String, Vector>();
        ConnectionStatistics data = new ConnectionStatistics();
        // If monitor not started. start
        //if (!mo.monitorStarted()){
        //    try {
        //        monitorStarted = Boolean.getBoolean(mo.startMonitor());
        //        //monitorStarted=true;
        //    } catch (IOException e) {
        //        //TODO. log message
        //        //e.printStackTrace();
        //        monitorStarted=false;
        //    }
        //}

        // MonitorImpl startedt.
        // Retreive data.

        //if (monitorStarted){
            try {
                data = mo.getMonitorStatisticData();
            } catch (IOException e) {
                // TODO Log message
                e.printStackTrace();
            }
        //}
        return data;
    }


    private void connected(){
        mo.connected();
    }

    public String getConnectionMsg(){
        return connectionMsg;
    }

    public void stopMonitor(){
        try {
            mo.stopMonitor();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

}
