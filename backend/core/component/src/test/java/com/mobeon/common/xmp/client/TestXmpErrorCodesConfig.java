package com.mobeon.common.xmp.client;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.util.GroupedProperties;



/**
 * This class tests the fact that the new XmpErrorCodesConfig class returns the same result 
 * as the methods that were in the XmpClient before.
 * The method we need to test is getErrorAction(String service, int code).
 * The old config file is XmpErrorCodes.cfg in the instance_templates.
 * The new config file is xmpErrorCodes.conf under the ntf/config directory (it will be validated against the xmpErrorCodes.xsd schema).
 * 
 * Important: To have successful test case, the files ntf/instance_templates/xmpErrorCodes.cfg and ntf/config/xmpErrorCodes.conf 
 * should have the same error code and the same services defined.
 * @author lmcvcio
 *
 */
public class TestXmpErrorCodesConfig {
	HashMap errorCodesActions;
	
	@Before
	public void setUp() throws Exception {
		System.out.print(System.getProperty("user.dir"));
		loadErrorCodeFile("../../ntf/instance_template/cfg/XmpErrorCodes.cfg");
    	XmpErrorCodesConfig.setXmpErrorCodesFilePath("../../ntf/config/xmpErrorCodes.conf");
    	XmpErrorCodesConfig.loadCfg();
    	System.out.println(XmpErrorCodesConfig.getXmpErrorCodesConfigAsString());
	}

	
    private String getErrorAction( String service, int code ) {
        if( errorCodesActions == null ) {
            return "pass";
        }
        Integer integer = new Integer(code);
        HashMap serviceMap = (HashMap)errorCodesActions.get(service);
        if( serviceMap == null ) {
            serviceMap = (HashMap)errorCodesActions.get("common");
        }
        String action = "pass";

        if( serviceMap != null ) {
            action = (String)serviceMap.get(integer);
            if( action == null ){
                action = "pass";
            }
        }
        return action;
        
    }
    
    /**
     *Loads error codes from a file. 
     *@param path where to find the file.
     */
    public void loadErrorCodeFile(String path) {
        errorCodesActions = new HashMap();
        GroupedProperties groupProps = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            groupProps = new GroupedProperties();
            groupProps.load(fis);
        
        } catch(Exception e) {
            System.out.println("Xmp error codes can not be loaded: " + e );
        }
        if( groupProps == null ) {
            return;
        }
        
        Properties commonProps = groupProps.getProperties("common");
        
        HashMap common = new HashMap();
        if( commonProps != null ) {
            Enumeration keys = commonProps.keys();
            while( keys.hasMoreElements() ) {
                String key = (String) keys.nextElement();
                try {
                    int code = Integer.parseInt(key);
                    String action = commonProps.getProperty(key).toLowerCase();
                    System.out.println("Adding common xmp action: " + code + "=" + action );
                    common.put(new Integer(code), action );
                } catch(Exception e) {
                	System.out.println("Unknown exception" + e);
                }
            }
        }
        errorCodesActions.put("common", common );
        Enumeration groups = groupProps.groupNames();
        while( groups.hasMoreElements() ) {
            String service = (String) groups.nextElement();
            service = service.toLowerCase();
            if( !service.equals("common") ) {
            	System.out.println("Creating actions for service " + service  );
                HashMap map = new HashMap(common);
                Properties props = groupProps.getProperties(service);
                Enumeration keys = props.keys();
                while( keys.hasMoreElements() ) {
                    String key = (String) keys.nextElement();
                    try {
                        int code = Integer.parseInt(key);
                        String action = props.getProperty(key).toLowerCase();
                        System.out.println("Adding xmp action: " + code + "=" + action );
                        map.put(new Integer(code), action );
                    } catch(Exception e) {
                    	System.out.println("Unknown exception" + e);
                    }
                }
                errorCodesActions.put(service, map);
            }
        }
    }
    
    @Test
    public void testErrorCodesAreEqual_ServiceAndCodeExist() throws Exception {
    	
        assertEquals(getErrorAction(IServiceName.CALL_MWI_NOTIFICATION.toLowerCase(),513 ), 
        		     XmpErrorCodesConfig.getErrorAction(IServiceName.CALL_MWI_NOTIFICATION,513));      
    }
    
    @Test
    public void testErrorCodesAreEqual_ServiceDoesNotExistAndCodeExistsInCommon() throws Exception {
        assertEquals(getErrorAction("FakeService".toLowerCase(),501 ), 
   		     XmpErrorCodesConfig.getErrorAction("FakeService",501));      
    }
    
    @Test
    public void testErrorCodesAreEqual_ServiceExistAndErrorCodeDoesNotExist() throws Exception {

        assertEquals(getErrorAction(IServiceName.PAGER_NOTIFICATION.toLowerCase(),523 ), 
   		     XmpErrorCodesConfig.getErrorAction(IServiceName.PAGER_NOTIFICATION,523));
      
    }
    
    @Test
    public void testErrorCodesAreEqual_ServiceDoesNotExistAndCodeDoesNotExistInCommon() throws Exception {
        assertEquals(getErrorAction("FakeService".toLowerCase(),555 ), 
   		     XmpErrorCodesConfig.getErrorAction("FakeService",555));      
    }
	
}
