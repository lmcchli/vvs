/*
 * DelayInfoDAOTest.java
 * JUnit based test
 *
 * Created on den 1 september 2004, 14:29
 */

package com.mobeon.common.storedelay.test;


import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import junit.framework.*;

import com.mobeon.ntf.util.DelayLoggerProxy;
import com.mobeon.common.storedelay.*;

/**
 *
 */
public class DelayInfoDAOTest extends TestCase {

    private DelayInfoDAO myDAO = null;

    public DelayInfoDAOTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DelayInfoDAOTest.class);
        return suite;
    }


    public void setUp()
        throws Exception
    {
        super.setUp();
        SDLogger.setLogger(new DelayLoggerProxy());
        myDAO = new DelayInfoDAO("/tmp/ntf/delaytestdao", "delaydb");
    }


    public void tearDown()
        throws Exception
    {
        super.tearDown();
        myDAO.close();
    }

    private static final String TEST_KEY_1  = "TEST_KEY_1";
    private static final short  TEST_TYPE_1 = 1;
    private static final long   WANTTIME_1 = 250000L;
    private static final String STR_DATA_1  = "TEST DATA";
    private static final byte[] BYTE_DATA_1 = new byte[]{32,33,34,35};

    private static final String TEST_KEY_2  = "TEST_KEY_2";
    private static final short  TEST_TYPE_2 = 2;
    private static final long   WANTTIME_2 = 250010L;
    private static final String STR_DATA_2  = "TEST DATA 2";
    private static final byte[] BYTE_DATA_2 = null;

    private static final long LAST_TIME = 1000000; // Must be larger than any wanttime

    private DelayInfo makeDelayInfo1()
    {
        DelayInfo newInfo = new DelayInfo(TEST_KEY_1, TEST_TYPE_1,
                                          STR_DATA_1, BYTE_DATA_1);
        newInfo.setWantTime(WANTTIME_1);
        return newInfo;
    }

    private DelayInfo makeDelayInfo2()
    {
        DelayInfo newInfo = new DelayInfo(TEST_KEY_2, TEST_TYPE_2,
                                          STR_DATA_2, BYTE_DATA_2);
        newInfo.setWantTime(WANTTIME_2);
        return newInfo;
    }

    /**
     * Test of create method, of class storedelay.DelayInfoDAO.
     */
    public void testCreate()
        throws DelayException
    {
        System.out.println("testCreate");
        try {
            DelayInfo newInfo =  makeDelayInfo1();
            myDAO.create(newInfo);

            // Verify that it is in the database

            DelayInfo found = myDAO.find(TEST_KEY_1, TEST_TYPE_1);
            checkInfoEqual(newInfo, found);
        } finally  {
            boolean removed = myDAO.remove(TEST_KEY_1, TEST_TYPE_1);
            String msg = "";
            if (removed) {
                msg = "Removed: ";
            } else {
                msg = "Did not remove: ";
            }
            SDLogger.log(SDLogger.DEBUG,msg+ TEST_KEY_1 + "/" + TEST_TYPE_1);

            // Check that it is not there anymore
            DelayInfo nextFound = myDAO.find(TEST_KEY_1, TEST_TYPE_1);
            assertNull("Remove should have deleted row", nextFound);

        }

    }

    private void checkInfoEqual(DelayInfo expected, DelayInfo actual)
    {
        assertNotNull("Expected must not be null", expected);
        assertNotNull("Actual must not be null", actual);
        assertEquals("Key must be equal", expected.getKey(), actual.getKey());
        assertEquals("Type must be equal", expected.getType(), actual.getType());
        assertEquals("Wanttime must be equal",
                     expected.getWantTime(), actual.getWantTime());
        assertEquals("String data bust be equal",
                     expected.getStrInfo(), actual.getStrInfo());
        byte[] expectedBytes = expected.getByteInfo();
        byte[] foundBytes    = actual.getByteInfo();
        if (expectedBytes == foundBytes) return; // Both null covered here
        assertNotNull("Both byte[] or none must be null", expectedBytes);
        assertNotNull("Both byte[] or none must be null", foundBytes);
        assertEquals("Length must be same", expectedBytes.length, foundBytes.length);
        for (int i=0; i<foundBytes.length; i++) {
            if (expectedBytes[i] != foundBytes[i]) {
                fail("Byte arrays differs at position " + i);
            }
        }

    }

    /**
     * Test of findForTime method, of class storedelay.DelayInfoDAO.
     */
    public void testFindForTime()
        throws DelayException
    {
        System.out.println("testFindForTime");


        DelayInfo newInfo1 =  makeDelayInfo1();
        DelayInfo newInfo2 =  makeDelayInfo2();
        try {

            myDAO.create(newInfo1);
            myDAO.create(newInfo2);

            List list1 = new ArrayList();
            List list2 = new ArrayList();
            List list3 = new ArrayList();
            long nextTime1 = myDAO.findForTime(1, WANTTIME_2, 100, list1);
            assertEquals("Next free time", WANTTIME_2, nextTime1);
            assertEquals("Found elements", 1, list1.size());

            long nextTime2 = myDAO.findForTime(nextTime1, WANTTIME_2, 100, list2);
            assertEquals("Next free time", WANTTIME_2, nextTime2);
            assertEquals("Found elements", 0, list2.size());

            long nextTime3 = myDAO.findForTime(nextTime2, LAST_TIME, 100, list3);
            assertEquals("Next free time", LAST_TIME, nextTime3);
            assertEquals("Found elements", 1, list3.size());
        } finally {
            myDAO.remove(newInfo1.getKey(), newInfo1.getType());
            myDAO.remove(newInfo2.getKey(), newInfo2.getType());

        }

    }

    /**
     * Check the limit handling in findForTime
     */
    public void testFindForTimeLimits()
        throws DelayException
    {

        System.out.println("testFindForTimeLimits");
        String keyBase = "TIMELIMITSKEY-";
        int keySuffix = 0;
        long firstWantTime = 250000;
        long nextWantTime = firstWantTime;
        try {

            for (int i=0; i<10; i++) {
                DelayInfo newInfo = new DelayInfo(keyBase+keySuffix,
                                    (short)1, "DATA",null);
                newInfo.setWantTime(nextWantTime);
                myDAO.create(newInfo);
                keySuffix++;
                nextWantTime++;
            }

            for (int i=0; i<10; i++) {
                DelayInfo newInfo = new DelayInfo(keyBase+keySuffix,
                                    (short)1, "DATA",null);
                newInfo.setWantTime(260000);
                myDAO.create(newInfo);
                keySuffix++;
            }
            List list1 = new ArrayList();
            long found1 = myDAO.findForTime(firstWantTime, nextWantTime,
                                           5, list1);
            assertEquals("Maximum number got, one released", 4, list1.size());
            // We should have firstWant,firstWant+1,firstWant+2,firstWant+3
            //  The next to get is firstWant+4 and that is the number we
            // should have
            assertEquals("Next to find", firstWantTime + 4, found1);

            List list2 = new ArrayList();
            long found2 = myDAO.findForTime(found1, 260000, 10, list2);
            // We should get the remaining 6 now
            assertEquals("The remaining elements", 6, list2.size());

            List list3 = new ArrayList();
            long found3 = myDAO.findForTime(260000, 260001, 5, list3);
            // We should get maximum elements since all are on same ms
            assertEquals("Max elements of those in same ms",5, list3.size());


            List list4 = new ArrayList();
            long found4 = myDAO.findForTime(260000,260010, 20, list4);
            assertEquals("Found all 10 at same millis", 10, list4.size());


        } finally {
            myDAO.removeOlderThan(500000);

        }
    }

    /**
     * Test of update method, of class storedelay.DelayInfoDAO.
     */
    public void testUpdate()
         throws DelayException
    {
        System.out.println("testUpdate");

        DelayInfo newInfo1 = makeDelayInfo1();
        try {
            myDAO.create(newInfo1);

            // Change and then update
            newInfo1.setWantTime(WANTTIME_1 + 20);
            newInfo1.setStrInfo("NEW STRING INFO");
            newInfo1.setByteInfo(new byte[] {1,2,3});
            myDAO.update(newInfo1);

            DelayInfo foundInfo = myDAO.find(newInfo1.getKey(), newInfo1.getType());
            this.checkInfoEqual(newInfo1, foundInfo);


        } finally {
            myDAO.remove(newInfo1.getKey(), newInfo1.getType());
        }
    }



    /**
     * Test of removeOlderThan method, of class storedelay.DelayInfoDAO.
     */

    public void testRemoveOlderThan()
        throws DelayException
    {
        System.out.println("testRemoveOlderThan");


        DelayInfo newInfo1 =  makeDelayInfo1();
        DelayInfo newInfo2 =  makeDelayInfo2();
        DelayInfo infoNoTime = new DelayInfo("NOTIMEKEY",(short)3,"No time",null);

        try {
            myDAO.create(newInfo1);
            myDAO.create(newInfo2);
            myDAO.create(infoNoTime);

            int count1 = myDAO.removeOlderThan(newInfo1.getWantTime());
            assertEquals("No timed info before info1", 0, count1);
            int count2 = myDAO.removeOlderThan(newInfo2.getWantTime()+1);
            assertEquals("Two timed info before info2+1", 2, count2);


        } finally {
            myDAO.remove(newInfo1.getKey(), newInfo1.getType());
            myDAO.remove(newInfo2.getKey(), newInfo2.getType());
            myDAO.remove(infoNoTime.getKey(), infoNoTime.getType());
        }

    }


    /**
     * Test that cleaning is done, and that method calls fails
     * when cleaning.
     * This test is time sensitive so the result of this test
     * should be tested manually.
     */
    public void testCleaning()
        throws DelayException
    {

        System.out.println("testCleaning");
        String keyBase = "KEY-";
        int keySuffix = 0;
        long firstWantTime = 250000;
        long nextWantTime = firstWantTime;
        // Use som large data so defragmentation takes some time
        String dataStr = "DATA - 00000000000011111111111222222222233333" +
           "444444444444444444444444444444444444444444444444444444444444" +
           "555555555555555555555555555555555555555555555555555555555555";
        byte [] dataBytes = new byte[300];
        for (int byteIndex = 0; byteIndex <300; byteIndex++ ){
            dataBytes[byteIndex] = (byte)55;
        }

        try {

            myDAO.setCleaningLimits(5000,0);

            // Fill up DB so a cleaning will be done and take some time
            // Do not TRACE log during fill.
            int origLevel = SDLogger.getLevel();
            SDLogger.setLevel(SDLogger.INFO);
            SDLogger.log(SDLogger.DEBUG,"Start filling up db");
            for (int i=0; i< 10000; i++) {
                DelayInfo newInfo = new DelayInfo(keyBase+keySuffix,
                                    (short)1, dataStr, dataBytes);
                newInfo.setWantTime(nextWantTime);
                myDAO.create(newInfo);
                keySuffix++;
                nextWantTime++;
            }
            SDLogger.setLevel(origLevel);


            myDAO.allowCleaning();
            DelayInfo newInfo = new DelayInfo(keyBase+keySuffix,
                                    (short)1, dataStr, dataBytes);
            try {
                myDAO.update(newInfo);
                fail("We should be in cleaning now");
            } catch (DelayCleaningException expected) {
                System.out.println("Got expected cleaning exc:" + expected);
            }

            while (myDAO.isBusy()) {
                System.out.println("DAO is busy, wait a while...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    System.out.println("Interrupted");
                }
            }
            System.out.println("Not busy anymore!");
            myDAO.update(newInfo);
            System.out.println("Updated info");


        } finally {
            // Delete a few at a time to avoid memory problems
            long startAt = 251000;
            while (startAt < 262000) {
              myDAO.removeOlderThan(startAt);
              startAt += 2000;
            }


        }
    }



}
