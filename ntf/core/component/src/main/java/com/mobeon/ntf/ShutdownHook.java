package com.mobeon.ntf;


import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;


/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2012.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

/**
 * Shutdown hook to catch system exit
 *
 * A shutdown hook is not always called, and can time out depending on the OS so best not to use it
 * But it's here just in case..
 *
 * Some functions in back end and msgcore use system.exit though so we need to catch them.
 *
 * Should be avoided instead use:
 * managementInfo.get().setNtfAdministrativeState(ManagementInfo.AdministrativeState.SHUTDOWN);
 * to initiate a clean shutdown
 */
public class ShutdownHook extends Thread {
    private static ILogger log;

    ShutdownHook() {
        log = ILoggerFactory.getILogger(ShutdownHook.class);
        log.info("Initializing shutdownhook");

    }

    public void run() {

        try
        {
            log.info("Executing NTF shutdown hook");

            if (ManagementInfo.get().isAdministrativeStateExit())
            {
                log.info("Administrative state is Exit, exiting...");
                return;
            }

            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
            long time = 32;
            try {
                time = Config.getShutdownTime()+2; //shutdown time plus a small guard
            } catch (Throwable t){
                log.info("Unable to get shutdwon time, using default: " + time);
            }
            while (!ManagementInfo.get().isAdministrativeStateExit() && time-- > 0) {
                try { sleep(1000);} catch (InterruptedException i) {;} //ignore
            }
            if (time <= 0) {
                log.warn ("Can't Shutdown gracefully, timeout...");
                Runtime.getRuntime().halt(-1);
            }

            log.info("NTF shutdown hook execution completed.");

        } catch (Throwable t) {
            try {
                log.error("ManagmentInfo, Throwable occured during shutdown, exiting immediatly ",t);
            } catch (Throwable t2) {;;} //ignore second exception.
            Runtime.getRuntime().halt(-1);
        }
    }
}
