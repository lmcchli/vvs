/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.management.test;

import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.util.threads.NtfThread;
import java.lang.InterruptedException;

/**
 * This class tests ManagementInfo
 */
public class ManagementInfoTest extends NtfTestCase {

    private class PrintLoop extends NtfThread {
            private int countShutdownLoops = 0;
            private int countLockedLoops = 0;

            public PrintLoop(String name) { super(name); }

            public boolean ntfRun() {
                try {
                    sleep(1000);
                }
                catch(InterruptedException ie) { System.err.println(ie.getMessage());}
                countLockedLoops++;
                if(countLockedLoops == 20)
                    return true;
                else
                    return false;
            }

            public boolean shutdown() {
                boolean firstCheck = true;
                if(firstCheck) {
                    ManagementInfo.get();
                    firstCheck = false;
                }
                 try {
                    sleep(1000);
                 } catch(InterruptedException ie) { System.err.println(ie.getMessage());}

                if (countShutdownLoops == 5) {
                    ManagementInfo.get().threadDone();
                    return true;
                }
                else {
                    countShutdownLoops++;
                    return false;
                }
            }
    }

    private ManagementInfo info;

    public ManagementInfoTest(String name) {
        super(name);
    }

    protected void setUp() {
        //        Config.setInstallDir(".");
        info = ManagementInfo.get();
        NtfTime ntftime = new NtfTime();
        info.setNtfAdministrativeState(AdministrativeState.UNLOCKED);
    }

   /* protected void tearDown() {
        EventHandler.get().closeEventPort();
    }*/

    public void test() throws Exception {
        l("test");
        assertNotNull(info);
    }


    public void testAdministrativeStateUnlocked() throws Exception {
        l("testAdministrativeStateUnlocked");
        ManagementInfo.get().setNtfAdministrativeState(ManagementInfo.AdministrativeState.UNLOCKED);
        assertEquals(ManagementInfo.AdministrativeState.UNLOCKED, ManagementInfo.get().getNtfAdministrativeState());
    }

   /* public void testInstallDate() throws Exception {
        Long lv = new Long(1086702236);
        ManagementInfo.get().setNtfInstallDate(lv);
        assertEquals("1970-01-13 14:51:42:00", ManagementInfo.get().getNtfInstallDate().dateToString());
    }
    */

    /*
    public void testConsumedServiceName() throws Exception {
        l("testConsumedServiceName");
        ManagementCounter mCounter = ManagementInfo.get().getCounter("sms", ManagementCounter.CounterType.FAIL);
        assertEquals("sms", ManagementInfo.get().getConsumedService("sms").getConsumedServiceName());
    }

    public void  testConsumedServiceStatusImpaired1() throws Exception {
        l("testConsumedServiceStatusImpaired1");
        ConsumedService service = new ConsumedService("kalle");
        ManagementStatus mStatus1 = new ManagementStatus("sms1", service);
        ManagementStatus mStatus2 = new ManagementStatus("sms2", service);
        mStatus1 = ManagementInfo.get().getStatus("sms", "sms1");
        mStatus2 = ManagementInfo.get().getStatus("sms", "sms2");

        if (mStatus1 != null && mStatus2 != null) {
            mStatus1.down();
            mStatus2.up();
        }
        assertEquals(ConsumedService.STATUS_IMPAIRED, ManagementInfo.get().getConsumedService("sms").getConsumedServiceStatus());

    }

    public void testConsumedServiceStatusImpaired2() throws Exception {
        l("testConsumedServiceStatusImpaired2");
        ManagementInfo.get().setConsumedServiceStatus(10, 5, "mms");
        assertEquals(ConsumedService.STATUS_IMPAIRED, ManagementInfo.get().getConsumedService("mms").getConsumedServiceStatus());
    }

     public void testConsumedServiceStatusUp() throws Exception {
        l("testConsumedServiceStatusUp");
        ManagementInfo.get().setConsumedServiceStatus(10, 0, "mms");
        assertEquals(ConsumedService.STATUS_UP, ManagementInfo.get().getConsumedService("mms").getConsumedServiceStatus());
    }

    public void testConsumedServiceStatusDown() throws Exception {
        l("testConsumedServiceStatusDown");
        ManagementInfo.get().setConsumedServiceStatus(10, 10, "mms");
        assertEquals(ConsumedService.STATUS_DOWN, ManagementInfo.get().getConsumedService("mms").getConsumedServiceStatus());
    }

    public void  testConsumedServiceTime1() throws Exception {
        l("testConsumedServiceTime1");
        ConsumedService service = new ConsumedService(10, "kalle");
        ManagementStatus mStatus1 = new ManagementStatus("sms1", service, 1);
        mStatus1 = ManagementInfo.get().getStatus("sms", "sms1");
        long diffTime = 0;
        if (mStatus1 != null)
            mStatus1.down();
        Thread.sleep(10000);
        diffTime = 10 - ManagementInfo.get().getConsumedService("sms").getConsumedServiceTime();
        assertTrue(diffTime < 2);

    }

    public void  testConsumedServiceTime2() throws Exception {
        l("testConsumedServiceTime2");
        ConsumedService service = new ConsumedService(10, "kalle");
        ManagementStatus mStatus1 = new ManagementStatus("sms1", service, 1);
        mStatus1 = ManagementInfo.get().getStatus("sms", "sms1");
        long diffTime = 0;
        if (mStatus1 != null)
            mStatus1.down();
        else
            return;
        Thread.sleep(5000);
        mStatus1.down();
        Thread.sleep(15000);
        diffTime = 15 - ManagementInfo.get().getConsumedService("sms").getConsumedServiceTime();
        assertTrue(diffTime < 2);

    }

    public void testConsumedServiceNotifSent() throws Exception {
        l("testConsumedServiceNotifSent");
        ManagementCounter mCounter = new ManagementCounter("sms1");
        mCounter = ManagementInfo.get().getCounter("sms", ManagementCounter.CounterType.SUCCESS);
        for(int i=0; i<100; i++)
            mCounter.incr();
        assertTrue(mCounter.getCount() == 100);
    }

    */

    /*

   public void testSendStartEvent() throws Exception {
       l("testSendStartEvent");
       CMProtocol cmprotocol = new CMProtocol(false);
       int ret = cmprotocol.sendStart(1, "sms");
       cmprotocol.closeSocket();
       assertTrue(ret != 0);
   }

   public void testSendStopEvent() throws Exception {
       l("testSendStopEvent");
       CMProtocol cmprotocol = new CMProtocol(false);
       int ret = cmprotocol.sendStop(1, "sms");
       cmprotocol.closeSocket();
       assertTrue(ret != 0);
   }

   public void testSendResponseEvent() throws Exception {
       l("testSendResponseEvent");
       CMProtocol cmprotocol = new CMProtocol(false);
       String[] mibarray = new String[2];
       mibarray[0] = "ntfName=ntf@host";
       mibarray[1] = "ntfVersion=R10A";
       int ret = cmprotocol.sendResponse(1, "sms", mibarray);
       cmprotocol.closeSocket();
       assertTrue(ret != 0);
   }

   public void testReceiveEvent() throws Exception {
       CMProtocol cmprotocol = new CMProtocol(18002);

       Thread cmdthread = new Thread() {
           public void run() {
               try {
                sleep(2000);
               }
               catch(InterruptedException ie) { ; }
               String cmd = "./testclient 18002 Test localhost";
               // start command running
               try {
               Process proc = Runtime.getRuntime().exec(cmd);
               // wait for command to terminate
               proc.waitFor();
               }
               catch (IOException ioe) { ; }
               catch (InterruptedException ie) { ;}
           }
       };
       cmdthread.start();
       byte[] packet = cmprotocol.listenToEvent();
       cmprotocol.closeSocket();
       String testPacket = new String("Test");
       String str1 = new String(testPacket.getBytes(), "ISO-8859-1");
       String str2 = new String(packet, "ISO-8859-1");
       assertEquals(str1, str2);
   }*/

   public void testLockThread() throws Exception {
        l("testLockThread");
        PrintLoop p = new PrintLoop("lockThread");
        p.start();
        Thread.sleep(5000);
        ManagementInfo.get().setNtfAdministrativeState(ManagementInfo.AdministrativeState.LOCKED);
        Thread.sleep(5000);
        p.stop();
        assertEquals(NtfThread.InternalState.LOCKED, p.getInternalState());
   }

   public void testShutdownThread() throws Exception {
         l("testShutdownThread");
         ManagementInfo.get().setNtfAdministrativeState(ManagementInfo.AdministrativeState.UNLOCKED);
         PrintLoop p = new PrintLoop("shutdownThread");
         p.start();
         Thread.sleep(5000);
         ManagementInfo.get().setNtfAdministrativeState(ManagementInfo.AdministrativeState.SHUTDOWN);
         Thread.sleep(20000);
         p.stop();
         assertEquals(NtfThread.InternalState.EXIT, p.getInternalState());
   }

    public void testUnlockThread() throws Exception {
        l("testUnlockThread");
        ManagementInfo.get().setNtfAdministrativeState(ManagementInfo.AdministrativeState.LOCKED);
        PrintLoop p = new PrintLoop("unlockthread");
        p.start();
        Thread.sleep(1000);
        ManagementInfo.get().setNtfAdministrativeState(ManagementInfo.AdministrativeState.UNLOCKED);
        Thread.sleep(5000);
        p.stop();
        assertEquals(NtfThread.InternalState.UNLOCKED, p.getInternalState());
    }

//    public void testGetManagedObject() throws Exception {
//        l("testGetManagedObject");
//        int firstCount = ManagementInfo.get().getManagedObject().size();
//
//        ManagementInfo.get().getStatus("testSms2", "instanceA");
//        HashMap mibobj = ManagementInfo.get().getManagedObject();
//        assertEquals(firstCount + 1, mibobj.size());
//    }

}
