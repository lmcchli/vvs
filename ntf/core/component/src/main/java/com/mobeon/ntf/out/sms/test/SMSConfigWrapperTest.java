/*
 * SMSConfigWrapperTest.java
 * JUnit based test
 *
 * Created on den 23 december 2005, 13:48
 */

package com.mobeon.ntf.out.sms.test;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.mobeon.ntf.test.*;
import com.mobeon.ntf.out.sms.SMSConfigWrapper;
import junit.framework.*;

/**
 *
 * @author ermjnil
 */
public class SMSConfigWrapperTest extends NtfTestCase {
    
    public SMSConfigWrapperTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() {
    	try {
        SMSConfigWrapper.initSMPPErrorCodeActions("config/smppErrorCodes.conf");
    	} catch (ConfigurationDataException ex) {
    		ex.printStackTrace();
    	}
    }
    
    public void testGetFailed() throws Exception {
        assertEquals("failed", SMSConfigWrapper.getErrorAction(Integer.valueOf("1",16)));
        assertEquals("failed", SMSConfigWrapper.getErrorAction(Integer.valueOf("2",16)));
    }
    
    public void testGetRetry() throws Exception {
        assertEquals("retry", SMSConfigWrapper.getErrorAction(Integer.valueOf("58", 16)));
        assertEquals("retry", SMSConfigWrapper.getErrorAction(new Integer(0x58)));
        assertEquals("retry", SMSConfigWrapper.getErrorAction(new Integer(0x00000058)));
    }
    /* the value 0x000000FF is not defined in the default config file and thus the test doesn't pass
    public void testGetHexValueFF() throws Exception {
        assertEquals("HexValueFF", SMSConfigWrapper.getErrorAction(Integer.valueOf("FF",16)));
        assertEquals("HexValueFF", SMSConfigWrapper.getErrorAction(new Integer(0xFF)));
    }*/
    
    public void testWithBadValues() throws Exception {
        assertEquals("failed", SMSConfigWrapper.getErrorAction(Integer.valueOf("-1000", 16)));
    }
    
     public void testGetOk() throws Exception {
        assertEquals("ok", SMSConfigWrapper.getErrorAction(new Integer(0x0)));
    }
    
}
