package com.mobeon.masp.monitor;

import com.mobeon.masp.operateandmaintainmanager.FetchTask;
import com.mobeon.masp.operateandmaintainmanager.OMMConfiguration;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.IConfiguration;

import java.util.Timer;

//import com.mobeon.masp.monitor.Console;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class MonitorImpl implements FetchTask  {

    //ILoggerFactory logFactory;
    ILogger log;

    private OMMConfiguration ommconfig;
    private IConfiguration config;
    private Connection connection;

    private ConsoleLayoutConnection connectionLayout;
    private ConsoleLayoutStatistic  statisticLayout;

    private Integer maxRetryes;
    private Console console;

    private static Timer timer;


    public static void main(String[] args) {
        new MonitorImpl();
    }

    public void init(){
        maxRetryes = 10;

        //ILoggerFactory.configureAndWatch("lib/mobeon_log.xml");
        log = ILoggerFactory.getILogger(MonitorImpl.class);

        // loads config
        ommconfig = new OMMConfiguration(config);

        log.debug("Creating MonitorImpl");
        timer = new Timer();
        connection = new Connection();
        connectionLayout = new ConsoleLayoutConnection(connection);
        statisticLayout = new ConsoleLayoutStatistic(connection);
        console = new Console(148,41,"Media Access Server Monitor");
        console.setNoOfChannels(150);
        console.setNoOfChannelsPage(30);
        //console.addLayoutPage(statisticLayout);  // secondary
        console.addLayoutPage(connectionLayout);   // primary
        console.addLayoutPage(statisticLayout);    // secondary
        console.init();


        FetchTimerTask  fetch = new FetchTimerTask(this);
        timer.schedule(fetch,0,1000);  // run every second
        //timer.schedule(fetch,0,4000);  // run every 4 seconds

        // Start updating data
        //connectionLayout.updateData();
        //console.update();


    }

    public void processData(){
        if (console.exit()) {
            connection.stopMonitor();
            timer.cancel();
        }

        if (console.reconnect())
            connection.resetRetryes();

        if(connection.retry()) {
            connectionLayout.updateData();
            //statisticLayout.updateData();
            console.update();
        }


        //connectionLayout.updateData();
        //statisticLayout.updateData();

        // only get data for chosen layout
        console.pageLayout().updateData();

        console.update();
        console.connectionStatus(1,connection.getConnectionMsg() );

    }

    //public void setLogger(ILogger log){
    //    this.log = log;
    //}

    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }
}
