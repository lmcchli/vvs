package com.mobeon.ntf.phonedetection;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class PhoneStatusTest {

    private static long phoneno;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        phoneno = System.currentTimeMillis();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIsInCache() {
        System.out.println("\n========= testIsInCache =============");
        PhoneStatus.clear();
        String phoneNumber1 = String.valueOf(++phoneno);
        String phoneNumber2 = String.valueOf(++phoneno);
        PhoneStatus phoneStatus1 = PhoneStatus.getPhoneStatus(phoneNumber1);
        PhoneStatus phoneStatus2 = PhoneStatus.getPhoneStatus(phoneNumber2);
        System.out.println(phoneStatus1);
        System.out.println(phoneStatus2);
        assertTrue(phoneStatus1.isRoaming() == PhoneStatus.State.NONE);
        assertTrue(phoneStatus2.isRoaming() == PhoneStatus.State.NONE);
    }

    @Test
    public void testIsRoaming() {
        System.out.println("\n========= testIsRoaming =============");
        PhoneStatus.clear();
        String phoneNumber = String.valueOf(++phoneno);
        PhoneStatus phoneStatus = PhoneStatus.getPhoneStatus(phoneNumber);
        System.out.println(phoneStatus);
        assertTrue(phoneStatus.isRoaming() == PhoneStatus.State.NONE);

        phoneStatus.setRoaming(PhoneStatus.State.YES);
        System.out.println(phoneStatus);
        phoneStatus = null;
        phoneStatus = PhoneStatus.getPhoneStatus(phoneNumber);
        assertTrue(phoneStatus.isRoaming() == PhoneStatus.State.YES);

        phoneStatus.setRoaming(PhoneStatus.State.NO);
        System.out.println(phoneStatus);
        phoneStatus = null;
        phoneStatus = PhoneStatus.getPhoneStatus(phoneNumber);
        assertTrue(phoneStatus.isRoaming() == PhoneStatus.State.NO);
    }

    @Test
    public void testIsPhoneOn() {
        System.out.println("\n========= testIsPhoneOn =============");
        PhoneStatus.clear();
        String phoneNumber = String.valueOf(++phoneno);
        PhoneStatus phoneStatus = PhoneStatus.getPhoneStatus(phoneNumber);
        System.out.println(phoneStatus);
        
        assertTrue(phoneStatus.isPhoneOn() == PhoneStatus.State.NONE);

        phoneStatus.setPhoneOn(PhoneStatus.State.YES);
        System.out.println(phoneStatus);
        phoneStatus = null;
        phoneStatus = PhoneStatus.getPhoneStatus(phoneNumber);
        assertTrue(phoneStatus.isPhoneOn() == PhoneStatus.State.YES);

        phoneStatus.setPhoneOn(PhoneStatus.State.NO);
        System.out.println(phoneStatus);
        phoneStatus = null;
        phoneStatus = PhoneStatus.getPhoneStatus(phoneNumber);
        assertTrue(phoneStatus.isPhoneOn() == PhoneStatus.State.NO);
    }

    @Test
    public void testCaching() {
        System.out.println("\n========= testCaching ===============");
        PhoneStatus.clear();
        PhoneStatus ps;
        for(int i=0; i<1500; i++){
            ps = PhoneStatus.getPhoneStatus(String.valueOf(i));
            if((i % 3) == 0){
                ps.setRoaming(PhoneStatus.State.YES);
                ps.setPhoneOn(PhoneStatus.State.NO);
            } else if((i % 7) == 0){
                ps.setRoaming(PhoneStatus.State.NO);
                ps.setPhoneOn(PhoneStatus.State.YES);
            }
        }
        
        // Verify the size of the cache is still 1000
        assertTrue(1000 == PhoneStatus.size());
        
        // Sample the content 
        ps = PhoneStatus.getPhoneStatus(String.valueOf(666));
        System.out.println(ps);
        assertTrue(ps.isRoaming() == PhoneStatus.State.YES);
        assertTrue(ps.isPhoneOn() == PhoneStatus.State.NO);
        ps = PhoneStatus.getPhoneStatus(String.valueOf(999));
        System.out.println(ps);
        assertTrue(ps.isRoaming() == PhoneStatus.State.YES);
        assertTrue(ps.isPhoneOn() == PhoneStatus.State.NO);
        ps = PhoneStatus.getPhoneStatus(String.valueOf(1200));
        System.out.println(ps);
        assertTrue(ps.isRoaming() == PhoneStatus.State.YES);
        assertTrue(ps.isPhoneOn() == PhoneStatus.State.NO);
        ps = PhoneStatus.getPhoneStatus(String.valueOf(1497));
        System.out.println(ps);
        assertTrue(ps.isRoaming() == PhoneStatus.State.YES);
        assertTrue(ps.isPhoneOn() == PhoneStatus.State.NO);

        ps = PhoneStatus.getPhoneStatus(String.valueOf(770));
        System.out.println(ps);
        assertTrue(ps.isRoaming() == PhoneStatus.State.NO);
        assertTrue(ps.isPhoneOn() == PhoneStatus.State.YES);
        ps = PhoneStatus.getPhoneStatus(String.valueOf(1400));
        System.out.println(ps);
        assertTrue(ps.isRoaming() == PhoneStatus.State.NO);
        assertTrue(ps.isPhoneOn() == PhoneStatus.State.YES);

        ps = PhoneStatus.getPhoneStatus(String.valueOf(1499));
        System.out.println(ps);
        assertTrue(ps.isRoaming() == PhoneStatus.State.NONE);
        assertTrue(ps.isPhoneOn() == PhoneStatus.State.NONE);
    }
}
