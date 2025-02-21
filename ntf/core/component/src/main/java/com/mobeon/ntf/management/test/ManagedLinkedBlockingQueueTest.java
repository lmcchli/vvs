

package com.mobeon.ntf.management.test;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.util.OSType;
import com.abcxyz.services.moip.alarms.MoipAlarmEvent;
import com.abcxyz.services.moip.alarms.MoipAlarmFactory;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.logging.LogAgentFactory;
import com.mobeon.common.xmp.client.XmpErrorCodesConfig;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NtfMain;
import com.mobeon.ntf.management.ManagedLinkedBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.out.sms.SMSConfigWrapper;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.threads.NtfThread;


public class ManagedLinkedBlockingQueueTest  extends NtfThread {

    ManagedLinkedBlockingQueue<String> queue = new ManagedLinkedBlockingQueue<String>(2);

    public ManagedLinkedBlockingQueueTest(String name) {
        super(name);
    }
    
    static int count = 0;

    @Override
    public boolean ntfRun() {
        
        String s = null;
        try {
            if (count++ %2 == 0)
            {
                s = queue.poll(2,TimeUnit.SECONDS);
            }
            else
            {
                s= queue.take();
            }
        } catch (InterruptedException e1) {
            System.out.println("Interupted while get from queue");
        }
        if (s != null)
            System.out.println("got String:" + s);
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("Interupted while get from queue (sleep");
        }
        return false;
    }

    @Override
    public boolean shutdown() {
        if (isInterrupted()) {
            System.out.println("Interupted while shutdown");
            return true;
        } 
        if (queue.size() == 0) {return true;}
        if (ntfRun() == true ) {return true;}
        return (queue.size() == 0);
    }

    static private LogAgent log;
    
    public static void main(String[] args)
    {
        String log4jPath = Config.getNtfHome() + "/cfg/log4j.xml";
        if (OSType.getInstance().isOSWindows()) {
            log4jPath = Config.getNtfHome() + "/cfg/log4jwin.xml";
        }

        if (new File(log4jPath).exists()) {
            LogAgentFactory.configureAndWatch(log4jPath);
        }
        log = NtfCmnLogger.getLogAgent(NtfMain.class);
        log.info("* **//NTF version " + Config.getVersion() + ", installed in " + Config.getInstallDir() + ", started with home " + Config.getNtfHome());

        try {
            //load NTF configuration from NTF home
            Config.loadCfg();
         
        } catch (ConfigurationDataException cdex) {
            log.error("NTF will be shuting down because it couldn't load the ntf configuration files " +
                    "(notification.conf, xmpErrorCodes.conf, smppErrorCodes.conf) under the directory " +
                    Config.getNtfHome() + ". Error: " + cdex.getMessage());        
            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
        }
        
        
        ManagedLinkedBlockingQueueTest t = new ManagedLinkedBlockingQueueTest("test-s");
        Integer i = 0;
        try {
            
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            t.start();;System.out.println("starting consumer thread.....");
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            t.queue.offer(i.toString(),6,TimeUnit.SECONDS);System.out.println("put String:" + i);i++;
            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.LOCKED); System.out.println("Set lock....");
            System.out.println("trying offer for 6 secs while locked....");
            if (t.queue.offer(i.toString(),6,TimeUnit.SECONDS)) //offer may work once, as item may be taken off the queue before lock occurs.
            {
                if (t.queue.size() == 1 )
                {
                    System.out.println("first offer String put <ok depending on timeing>:" + i);
                    i++;
                    if (t.queue.offer(i.toString(),6,TimeUnit.SECONDS))
                    {
                        System.out.println("Failed, offer when queue full and locked..");
                        ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);;System.out.println("Set Shutdown1....");
                        while(!ManagementInfo.get().isAdministrativeStateExit()){System.out.println("Waiting exit: Queue Size: " + t.queue.size());sleep(1000);}
                        return;
                    }
                } else
                {
                    System.out.println("Failed, offer when queue full and locked..");
                    ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);;System.out.println("Set Shutdown2....");
                    while(!ManagementInfo.get().isAdministrativeStateExit()){System.out.println("Waiting exit: Queue Size: " + t.queue.size());sleep(1000);}
                    return;
                }
                    
            }

            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.UNLOCKED);System.out.println("Set unlock....");
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            t.queue.put(i.toString());System.out.println("put String:" + i);i++;
            ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);System.out.println("Set Shutdown -normal....");
            sleep(2000);
            System.out.println("Queue Size: " + t.queue.size());
            if (t.queue.offer(i.toString(),5))
            {
                System.out.println("offer success while shutdown: " + i );i++;
            } else
            {
               System.out.println("offer fail while shutdown: " + i + " queue size:" + t.queue.size() );
            }
            try {
                if (t.queue.offer(i.toString(),5))
                {
                    System.out.println("offer success while shutdown: " + i );i++;
                } else
                {
                    System.out.println("offer fail while shutdown: " + i + " queue size:" + t.queue.size() );


                    t.queue.put(i.toString());System.out.println("put while shutdown, should work timeout after 5 secs., String:" + i);i++;

                }
            } catch (InterruptedException ie) {System.out.println("interrupted during put when shutdown: " + NtfUtil.stackTrace(ie) );System.out.println("man state:: " + ManagementInfo.get().getNtfAdministrativeState());}
            
            while(!ManagementInfo.get().isAdministrativeStateExit()){System.out.println("Waiting exit: Queue Size: " + t.queue.size());  try {sleep(1000);} catch (InterruptedException ie) {;;} }
            System.out.println("Finished.... Queue Size: " + t.queue.size());
            System.out.println("End....");

        } catch (InterruptedException ie) {System.out.println("main Interrupted: " + NtfUtil.stackTrace(ie) );}

    }

}

