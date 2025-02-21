package com.mobeon.masp.rpcclient;

import java.util.Calendar;
import java.util.Date;
import java.io.IOException;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.configuration.*;
import com.mobeon.masp.monitor.MonitorImpl;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */
import com.mobeon.masp.operateandmaintainmanager.OMMConfiguration;

public class Command {

    private static ILogger log;
    private static RpcClient client;
    private static String hostName;
    private static Integer rpcPort;
    private static IConfiguration config;

    public Command() {
        //init();
    }

    public Command(IConfiguration config) {
        this.config = config;
        init();
    }

    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    private static void init(){
        //ILoggerFactory.configureAndWatch("lib/mobeon_log.xml");
        log = ILoggerFactory.getILogger(MonitorImpl.class);
        if (config==null) {
             IConfigurationManager cfgMgr = new ConfigurationManagerImpl();
             //cfgMgr.addConfigFile("cfg/operateandmaintainmanager.xml");
             cfgMgr.addConfigFile("cfg/mas.xml");
             config = cfgMgr.getConfiguration();
        }

        IGroup mainGroup = null;
        try {
            mainGroup = config.getGroup("operateandmaintainmanager.omm");
            //mainGroup.getGroup("omm").;
            hostName=mainGroup.getString(OMMConfiguration.HOSTNAME,"localhost");
            rpcPort=mainGroup.getInteger(OMMConfiguration.PORT,8081);

            //hostName = OMMConfiguration.getHostName();
            //rpcPort = OMMConfiguration.getPort() ;

        } catch (GroupCardinalityException e) {
            e.printStackTrace();
        } catch (UnknownGroupException e) {
            e.printStackTrace();
        } catch (ParameterTypeException e) {
            e.printStackTrace();
        }


        log = ILoggerFactory.getILogger(Command.class);
        log.debug("Create Command ");

        client = new RpcClient(hostName,rpcPort.toString());

    }
    /**
     * Command makes it possibele to send commands from command promt.
     * @param args
     */
    public static void main(String[] args) {
        //System.out.println("Parse request..");
        init();
        //log.debug("(main)new RpcClient(localhost,8081)");
        //client = new RpcClient("localhost","8081");
        String command;
        if (args.length > 0 ) {

            command = args[0];

            if (command.toUpperCase().equals("LOCK") ) {
                lock();
            }
            else if (command.toUpperCase().equals("UNLOCK")) {
                unlock();
            }
            else if (command.toUpperCase().equals("SHUTDOWN")) {
                shutdown();
            }
            else if (command.toUpperCase().equals("STATUS")) {
                System.out.println(status());
            }

            //else if (command.toUpperCase().equals("VIEWMIB")) {
                //doViewMib();
            //}

      //      else if (command.toUpperCase().equals("SELFDIAG")) {
      //          doDiag();
      //      }


            else {
                System.out.println("Unknown argument.");
            }
        }

    }

    /**
     * Sends close command to the omm.
     */
    public static void lock(){
        try {
            String retValue = client.sendCommand("setAdminState","locked");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * Sends the open command to the omm
     */
    public static void unlock(){
        try {
            String retValue = client.sendCommand("setAdminState","unlocked");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            // e.printStackTrace();
        }
    }

    /**
     * Sends the shutdown command to the omm
     */
    public static void shutdown(){
        try {
            String retValue = client.sendCommand("setAdminState","shutdown");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * Sends the status command to the omm and retreives
     * the status for MAS interpreted by OMM.
     */
    public static String status(){
        String status = "";
        try {
            status = client.sendCommand("getStatus","");
        } catch (IOException e) {
             status = "Not running";
        }

        return status;
    }

    // TODO- Remove this
    //public static void doDiag(){
    //    String status = "";
    //    try {
    //        status = client.sendCommand("selfDiag","");
    //    } catch (IOException e) {
    //        e.printStackTrace();
    //    }

    //}

    /**
     * Converts a milliseconds to a string containing day:hour:min:sec
     * @param  milliSec
     * @return  String containing Day:hour:min:sec
     */
    private static String  convertLongToTime(Long milliSec){
        Long seconds;
        Long day;
        Long hour;
        Long min;
        Long sec;

        seconds = (milliSec / 1000);    // Convert milliseconds to seconds
        day = (seconds / 86400);        // Get days
        seconds = seconds - (86400*day);// remove days
        hour = (seconds / 3600);        // get hours
        seconds = seconds - (3600*hour);// remove days
        min = (seconds / 60);           // get min
        seconds = seconds - (60*min);   // remove days
        sec = seconds;                  // rest
        return day+":"+hour+":"+min+":"+sec;
    }


    private static Date convertLongToDate(Long milliSec){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSec);
        return cal.getTime();
    }

   /* public static void doViewMib(){




        MasMibAttributes mibAttrib;

        try {
            mibAttrib = (MasMibAttributes)client.getMibAttributes();

            System.out.println("GENERAL ATTRIBUTES");
            System.out.println("   NAME                        : "+mibAttrib.masName);
            System.out.println("   VERSION                     : "+mibAttrib.masVersion);
            System.out.println("   OPERATIONAL STATE           : "+mibAttrib.masOperationalState);
            System.out.println("   ADMINISTRATIVE STATE        : "+mibAttrib.masAdministrativeState);
            System.out.println("   CURRENT UP TIME             : "+convertLongToTime(mibAttrib.masCurrentUpTime));
            System.out.println("   ACCUMULATED UP TIME         : "+convertLongToTime(mibAttrib.masAccumulatedUpTime));
            System.out.println("   RELOAD CONFIG TIME          : "+mibAttrib.masReloadConfigurationTime);

            System.out.println("");
            System.out.println("PROVIDED SERVICES :"+ mibAttrib.providedServices.size());


            for (MasMibProvidedServices providedService : mibAttrib.providedServices) {
                System.out.println("");
                System.out.println("   NAME                       : " + providedService.name);
                System.out.println("   STATUS                     : " + providedService.status);
                System.out.println("   HOST NAME                  : " + providedService.hostName);
                System.out.println("   PORT                       : " + providedService.port );
                System.out.println("   ZONE                       : " + providedService.zone );
                System.out.println("   APPLICATION NAME           : " + providedService.applicationName);
                System.out.println("   APPLICATION VERSION        : " + providedService.applicationVersion);
            }

            System.out.println("");
            System.out.println("CONSUMED SERVICES :" + mibAttrib.consumedServices.size());

            for (MasMibConsumedServices consumedServise : mibAttrib.consumedServices) {
                System.out.println("");
                System.out.println("   NAME                       : " + consumedServise.name);
                System.out.println("   STATUS                     : " + consumedServise.status);
                System.out.println("   SUCCESS OPERATIONS         : " + consumedServise.successOperations);
                System.out.println("   FAILED OPERATIONS          : " + consumedServise.failedOperations );
                System.out.println("   STATUS CHANGE TIME         : " + convertLongToDate(consumedServise.statusChangedTime));
            }

            System.out.println("");
            System.out.println("SERVICE ENABLER :" + mibAttrib.serviceEnablers.size());
            for (MasMibServiceEnabler serviceEnabler : mibAttrib.serviceEnablers ) {
            //for (int i = 0 ; i < mibAttrib.serviceEnablers.size() ;i++ ) {

                System.out.println("   NAME                       : "+serviceEnabler.protocol);
                System.out.println("   MAX CONNECTIONS            : "+serviceEnabler.maxConnections);
                //System.out.println("   HOST NAME                  : "+mibAttrib.serviceEnablers.get(i).get(2).toString());

                Vector<MasMibConnection> connections;
                connections = serviceEnabler.connections;

                for (MasMibConnection connection : connections) {
                    System.out.println("  CONNECTION TYPE             : " + connection.connectionType);
                    System.out.println("      CURRENT CONNECTIONS     : " + connection.currentConnections);
                    System.out.println("      PEAK CONNECTIONS        : " + connection.peakConnections);
                    System.out.println("      PEAK TIME               : " + convertLongToDate(connection.peakTime));
                    System.out.println("      TOTAL CONNECTIONS       : " + connection.totalConnections );
                    System.out.println("      ACCUMULATED CONNECTIONS : " + connection.accumulatedConnections );
                }

           }

        } catch (IOException e) {
            // Coud not connect to RPC Server.
            System.out.println(e.getMessage());
        }

    }*/
}

