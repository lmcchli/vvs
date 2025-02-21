package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.operateandmaintainmanager.OperateMAS.AdminState;


/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2012.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * 
 * Should be noted Shutdown hooks have an undefined but limited time to shutdown, or the JVM will crash out anyway.
 * The best way to shutdown is via SNMP in a controlled manner.
 * 
 * For this reason, shutdown signals, TERM/HUP etc are now intercepted and directed to shutdown via the 
 * MAS com.mobeon.masp.operateandmaintainmanager.OperateMAS.  This thread will force a Shutdown.
 * 
 * This is kept for legacy reasons as some classes force a system.exit, mostly in msgcore. Usually it will be
 * called only when the system has cleanly exited, if not will initiate shutdown through OperateMas.
 * 
 * 
 */


public class ShutdownHook extends Thread {
    OperateMAS operateMAS;
    private static ILogger log;
    private int forcedShutdownTime;

    ShutdownHook(OperateMAS opMas, int forcedShutdownTime ) {
        log = ILoggerFactory.getILogger(ShutdownHook.class);
        operateMAS = opMas;
        this.forcedShutdownTime=forcedShutdownTime+1; //small guard time to allow shutdown thread to finish.
    }

    public void run() {

        try {

            log.info("MAS ShutdownHook called.");

            if (operateMAS.getAdminState() == AdminState.SHUTDOWN)
            {
                log.info("Administrative state is Exit, exiting...");
                Runtime.getRuntime().halt(0); 
                return;
            }

            operateMAS.shutdown();

            int timer = forcedShutdownTime;
            while (operateMAS.getAdminState() != AdminState.SHUTDOWN && timer-- > 0) {
                try { sleep(1000);} catch (InterruptedException i) {;} //ignore
            }
            if (forcedShutdownTime <= 0) {
                log.warn ("Forced Shutdown timeout + "+ forcedShutdownTime + ", exiting anyway..");
                return;
            }

        } catch (Throwable t) {
            try {
                log.error("ManagmentInfo, Throwable occured during shutdown, exiting immediatly ",t);
            } catch (Throwable t2) {;;} //ignore second exception.
                                   
        }   finally {
            log.warn("MAS ShutdownHook completed.");
            try{sleep(10);} catch (InterruptedException i) {;} //allow time for log4j to flush..
            Runtime.getRuntime().halt(0); 
        }
    }
}
