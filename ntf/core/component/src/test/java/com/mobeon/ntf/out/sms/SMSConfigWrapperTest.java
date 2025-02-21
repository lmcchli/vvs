package com.mobeon.ntf.out.sms;


import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * This test class was added uniquely to test the change of the format of the file SMPPErrorCodes.cfg.
 * The old file is ntf/instance_template/cfg/SMPPErrorCodes.cfg.
 * The new file is ntf/config/smppErrorCodes.conf.
 * All the defined codes in both files should be the same to pass this test.
 * 
 * The method initSMPPErrorCodeActions is the original method from SMSConfigWrapper,
 * for which the implementation was modified with the use of the msgcore config class.
 * The method that gets the error code action is getErrorAction(Integer errorCode).  
 * @author lmcvcio
 *
 */
public class SMSConfigWrapperTest {
	Hashtable<Integer, String> smppErrorCodeActions = null;
	String defaultErrorAction = "failed";
	
	// FIXME This class needs reviewing - it does not initialize propery on Linux
	
//	@Before
	public void setUp() throws Exception {
		SMSConfigWrapper.initSMPPErrorCodeActions("config/smppErrorCodes.conf");
    	System.out.println(SMSConfigWrapper.getSmppErrorCodesAsString());
    	initSMPPErrorCodeActions("instance_template/cfg/SMPPErrorCodes.cfg");
	}
	
    public void initSMPPErrorCodeActions(String errorCodeFile) {
    	smppErrorCodeActions = new Hashtable<Integer, String>();
        smppErrorCodeActions.put(new Integer(0x0),  "ok");
        Properties props = new Properties();
        int code = 0;
        String action;

        System.out.println("Reading SMPP error code actions from file: " + errorCodeFile);
        try {
            FileInputStream fis = new FileInputStream(errorCodeFile);
            props.load(fis);
        } catch(Exception e) {
        	System.out.println("SMPP error codes can not be loaded: " + e );
        }


        Enumeration keys = props.keys();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String hex = "";
            if (key.matches("0x[0-9a-fA-F]*")) {
                try {
                    hex = key.substring(key.indexOf("x")+1);
                    code = Integer.parseInt(hex, 16);
                }
                catch(IndexOutOfBoundsException iobe) {
                	System.out.println("SMSConfigWrapper.initSMPPErrorCodeAction.IndexOutOfBoundsException " + iobe);
                    continue;
                }
                catch(NumberFormatException nfe) {
                    System.out.println("SMSConfigWrapper.initSMPPErrorCodeAction.NumberFormatException " + nfe);
                    continue;
                }
                action = props.getProperty(key);
                if(code != 0) {
                	System.out.println("Add SMPP error action [" + action + "] for error code [0x" +
                                    hex + "]");
                    smppErrorCodeActions.put(new Integer(code), action);
                }
            }
            else if (key.matches(".*default.*")) {
                action = props.getProperty(key);
                defaultErrorAction = action;
                System.out.println("Add [" + action + "] as default SMPP error action");
            }
        }
    }
    
    public  String getErrorAction(Integer errorCode) {
		if (smppErrorCodeActions.containsKey(errorCode)) {
			return (String) smppErrorCodeActions.get(errorCode);
		} else {
			return defaultErrorAction;
		}
	}
	
    @Test
    @Ignore("Ignored until class initialization is fixed")
    public void testErrorCodesAreEqual_CodeDoesNotExist() throws Exception {
    	
        assertEquals(getErrorAction(513), 
        		SMSConfigWrapper.getErrorAction(513));      
    }
    
    @Test
    @Ignore("Ignored until class initialization is fixed")
    public void testErrorCodesAreEqual_CodeExist() throws Exception {
    	
        assertEquals(getErrorAction(20), 
        		SMSConfigWrapper.getErrorAction(20));      
    }
    
    @Test
    @Ignore("Ignored until class initialization is fixed")
    public void testErrorCodesAreEqual_CodeIs0() throws Exception {
    	
        assertEquals(getErrorAction(0), 
        		SMSConfigWrapper.getErrorAction(0));      
    }
}
