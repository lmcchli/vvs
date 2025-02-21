package com.mobeon.masp.operateandmaintainmanager;

import com.abcxyz.messaging.cdrgen.CDRRecord;
import com.abcxyz.messaging.mlm.LicenseManager;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.operateandmaintainmanager.OperateMAS;
import com.mobeon.masp.operateandmaintainmanager.OperateMAS.AdminState;
/*
 * 
 */

public class ShutDownSupervisor extends Thread {
    private ILogger log = ILoggerFactory.getILogger(ShutDownSupervisor.class);
    private int shutdownTime;
    private OperateMAS operateMAS;
    private boolean shutdown = false;
    private int forcedShutdownTime;

    @SuppressWarnings("unused")
    private ShutDownSupervisor(){};

    public ShutDownSupervisor(int shutdownTime, int forcedShutdownTime, OperateMAS operateMAS) {
        this.shutdownTime=shutdownTime;
        this.forcedShutdownTime = forcedShutdownTime;
        this.operateMAS = operateMAS;
        
    }

    public void run () {
        Thread.currentThread().setName("Shutdown-Thread");

        try {
            int shutDownTimeBeforeForce=shutdownTime-forcedShutdownTime;
            if (shutDownTimeBeforeForce < 0) { shutDownTimeBeforeForce = 0;}
            for(int i=0;i<shutDownTimeBeforeForce && !shutdown ;i++)
            {
                try{
                    if ( i % 5 == 0) {
                        log.info("Waiting for service enablers to close for " + (shutdownTime - i) + " seconds.");
                    }
                    Thread.sleep(1000);
                }
                catch(InterruptedException e)
                {              
                    return;
                }
                shutdown = operateMAS.hasMasShutdown();
            }
            if(!shutdown)
            {
                log.warn("Calls are still active forcing the shutdown in the last" + forcedShutdownTime + " seconds!");

                //Forcing shutdown of the enabler
                operateMAS.setAdminState(AdminState.SHUTDOWN); // send force shutdown to services..
                //allow forcedShutdownTime seconds for every thing to close
                for(int i=0;i<forcedShutdownTime && !shutdown ;i++)
                {
                    try {
                        Thread.sleep(1000);
                        if (operateMAS.hasMasShutdown()) {
                            operateMAS.setAdminState(AdminState.SHUTDOWN);
                            return;
                        }
                    } catch(InterruptedException ie) {
                        break;
                    }
                }
            }


        } finally {
            try
            { 

                ConfigurationReader configReader = ConfigurationReader.getInstance();
                if (configReader != null && configReader.getConfig().getApplicationProxyMode()) {
                    log.info("Call manager is in proxy mode - will not release licenses");
                } else {
                    log.info("Releasing licenses");
                    // lmcantl: null is ok for the cleanup: someone else initialized this singleton a long time ago 
                    try {
                        LicenseManager.getInstance(null).cleanup();
                    }
                    // NPE means LicenseManager was never used
                    catch (NullPointerException npe) {
                        log.info("License Manager was never initialized - will not release licenses");
                    }
                }

            }
            catch (Throwable e)
            {
                log.warn ("Could not release licenses during shutdown. Exception: ",e);
            }
            try
            {
                CDRRecord cdrRecord = new CDRRecord(CommonOamManager.getInstance().getCdrGenOam());
                cdrRecord.flushAllPendingRecords();
            }
            catch (Throwable e)
            {
                log.warn ("Could not flush CDRs during shutdown. Exception: ",e);

            }
            operateMAS.setAdminState(AdminState.SHUTDOWN);
            log.warn("MAS Shutdown complete.");
            System.exit(0);
        }                              
    }
}






