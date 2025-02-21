package com.mobeon.common.xmp.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.mobeon.common.externalcomponentregister.IServiceName;

public class XmpErrorCodesConfig {
	private static final String PASS 						= "pass";
	private static final String XMP_ERROR_CODES_TABLE_TAG 	= "XmpErrorCodes.Table";
	private static final String XMP_ERROR_CODE_ACTION_TAG	= "xmpErrorCodeAction";
	private static final String COMMON 						= "Common";
	
	private static XmpErrorCodesConfig 	xmpErrorCodesConfig 		= null;
	private static ConfigManager 		xmpErrorCodesConfigManager 	= null;
	private static String				configFilePath 				= null;

	private XmpErrorCodesConfig() throws ConfigurationDataException {
		xmpErrorCodesConfigManager = OEManager.getConfigManager(configFilePath);		
	}
	
	public static void setXmpErrorCodesFilePath(String filePath) {
		configFilePath = filePath;
	}

	public static XmpErrorCodesConfig loadCfg() throws ConfigurationDataException {
		if (xmpErrorCodesConfig == null) {
			xmpErrorCodesConfig = new XmpErrorCodesConfig();
		}
		return xmpErrorCodesConfig;
	}

	public static String getErrorAction( String service, int code ) {
		String action = null;
		String codeAsString = String.valueOf(code);
		
		//first check the error code in the service table
		Map <String, Map<String, String>> serviceTable = xmpErrorCodesConfigManager.getTable(service + XMP_ERROR_CODES_TABLE_TAG);
		if ((serviceTable != null) && serviceTable.containsKey(codeAsString)){
			action = serviceTable.get(codeAsString).get(XMP_ERROR_CODE_ACTION_TAG);
		}
		
		//second, if it was not found, check the error code in the common table
		if (action == null) {
			action = getErrorActionFromCommon(codeAsString);
		}
		
		// third, return a default value
		if (action == null) {
			action = PASS;
		}
		
		return action;        
    }
	
	private static String getErrorActionFromCommon( String code ) {
		String errorAction = null;
		
		Map <String, Map<String, String>> commonServiceTable = xmpErrorCodesConfigManager.getTable(COMMON + XMP_ERROR_CODES_TABLE_TAG);
		if ((commonServiceTable != null) && commonServiceTable.containsKey(code)){
			errorAction = commonServiceTable.get(code).get(XMP_ERROR_CODE_ACTION_TAG);
		}
		
		return errorAction;
	}

	public static String getXmpErrorCodesConfigAsString() {
		StringBuffer stringBuffer = new StringBuffer(); 	
		
		stringBuffer.append("Xmp Error Codes Config has the following values: \n");
		stringBuffer.append(getXmpErrorCodesPerServiceAsString(COMMON));
		stringBuffer.append(getXmpErrorCodesPerServiceAsString(IServiceName.OUT_DIAL_NOTIFICATION));
		stringBuffer.append(getXmpErrorCodesPerServiceAsString(IServiceName.PAGER_NOTIFICATION));
		stringBuffer.append(getXmpErrorCodesPerServiceAsString(IServiceName.CALL_MWI_NOTIFICATION));
		stringBuffer.append(getXmpErrorCodesPerServiceAsString(IServiceName.MWI_NOTIFICATION));
		stringBuffer.append(getXmpErrorCodesPerServiceAsString(IServiceName.MEDIA_CONVERSION));
				
		return stringBuffer.toString();
	}
	
	public static String getXmpErrorCodesPerServiceAsString(String service) {
		StringBuffer stringBuffer = new StringBuffer(); 
		
		Map <String, Map<String, String>> serviceTable = xmpErrorCodesConfigManager.getTable(service + XMP_ERROR_CODES_TABLE_TAG);
	
		stringBuffer.append(service).append(": ");
		if ((serviceTable != null) && (serviceTable.size() > 0)) {
			Iterator<String> it = serviceTable.keySet().iterator();
			while (it.hasNext()) {
				String errorCode = (String) it.next(); 
				stringBuffer.append("\n").append(errorCode).append("=").append(serviceTable.get(errorCode).get(XMP_ERROR_CODE_ACTION_TAG));
			}
		} else {
			stringBuffer.append("empty");
		}
		stringBuffer.append("\n");
		return stringBuffer.toString();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			XmpErrorCodesConfig.setXmpErrorCodesFilePath("C:\\miabmoip_dev20_dp19\\ntf\\config\\xmpErrorCodes.conf");
			XmpErrorCodesConfig.loadCfg();
			
			System.out.print(getXmpErrorCodesConfigAsString());
			
			System.out.println(getErrorAction("Common", 421));
			System.out.println(getErrorAction("FakeService", 421));
			System.out.println(getErrorAction("OutdialNotification", 4621));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
