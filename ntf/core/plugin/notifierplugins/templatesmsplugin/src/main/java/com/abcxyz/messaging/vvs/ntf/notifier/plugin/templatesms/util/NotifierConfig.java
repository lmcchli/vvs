/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.util;

import java.util.HashMap;
import java.util.Map;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.SMSAddressInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierConfigManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util.INotifierLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.templatesms.TemplateSmsPlugin;

public class NotifierConfig implements NotifierConfigConstants{

    private static INotifierLogger log = TemplateSmsPlugin.getLoggerFactory().getLogger(NotifierConfig.class);
    private static INotifierConfigManager cphrPluginConfigManager = null;
    private static INotifierConfigManager ntfConfigManager = null;
	private static Map<String, SMSAddressInfo> sourceAddressCache = new HashMap<String,SMSAddressInfo>();
	private static Boolean disableSmscReplace = null;
    
	public static final String SOURCE_ADDRESS_DEBUT		= "sourceaddress_";;

    public static void refreshConfig() {
        cphrPluginConfigManager = TemplateSmsPlugin.getNotifierPluginConfigManager();
        ntfConfigManager = TemplateSmsPlugin.getNtfConfigManager();
        sourceAddressCache.clear();
        disableSmscReplace = null;
    }    
    
    /*
     * METHODS TO RETRIEVE TEMPLATE SMS PLUGIN CONFIGURATION PARAMETER VALUES.
     */
    
    public static Map<String, Map<String,String>> getcphrTable() {
        return cphrPluginConfigManager.getTable(TEMPLATE_SMS_TABLE);
    }

    public static Map<String,String> getcphrTypeEntry(String cphrTypeName) {
        Map<String,String> cphrTypeEntry = null;
        try {
            cphrTypeEntry = cphrPluginConfigManager.getTable(TEMPLATE_SMS_TABLE).get(cphrTypeName);
        } catch (NullPointerException npe) {
            ;
        }
        return cphrTypeEntry;
    }

    public static String getNotifierTypeParameterDefaultValue(String paramName) {
        return cphrPluginConfigManager.getParameter(TEMPLATE_SMS_TABLE + "." + paramName);
    }

    public static int getNotifierEventQueueSize() {
        return getNotifierPluginIntValue(NOTIFIER_QUEUE_SIZE);
    }

    public static int getNotifierEventWorkers() {
        return getNotifierPluginIntValue(NOTIFIER_WORKERS);
    }

    public static int getNotifierSenderSmsQueueSize() {
        return getNotifierPluginIntValue(NOTIFIER_SENDER_SMS_QUEUE_SIZE);
    }

    public static int getNotifierSenderSmsWorkers() {
        return getNotifierPluginIntValue(NOTIFIER_SENDER_SMS_WORKERS);
    }
    
    /*
     * METHODS TO RETRIEVE NTF CONFIGURATION PARAMETER VALUES.
     */
    
    public static String getCharsetEncoding() {
        String charsetName = ntfConfigManager.getParameter(CHARSET_ENCODING);
        if(charsetName.isEmpty()) {
            charsetName = null;
        }
        return charsetName;
    }

    public static int getValidity(String name) {
        int result = DEFAULT_VALIDITY_VALUE;
        Map<String, String> validityMapForName = ntfConfigManager.getTable(VALIDITY_TABLE).get(VALIDITY_PREFIX + name);
        if (validityMapForName != null) {
            String validityValue = validityMapForName.get(VALIDITY_VALUE);
            try {
                result = Integer.parseInt(validityValue);
            } catch (NumberFormatException nfex) {
                log.warn("NumberFormatException for the validity of " + name + ": " + nfex.getMessage());
            }
        }
        return result;
    }
    
    public static int getNumberingPlanIndicator() {
        return getNtfIntValue(SMS_NUMBERING_PLAN_INDICATOR);
    }
     
    public static SMSAddressInfo getSourceAddress(String name, String cosName) {
    	    	
		SMSAddressInfo result = null;
		String paramName = SOURCE_ADDRESS_DEBUT ;

		if ((cosName != null) && (name != null)) {
			paramName = paramName + name + "_" + cosName;
		} else if (name != null) {
			paramName = paramName + name;
		}
		
		result = sourceAddressCache .get(paramName);
		
		if (result != null) {
			return result;
		}
			
		if (!paramName.equals(SOURCE_ADDRESS_DEBUT)) {
			Map<String, String> sourceAddressMapForParamName = ntfConfigManager
					.getTable(SOURCE_ADDRESS_TABLE).get(paramName);
			if (sourceAddressMapForParamName != null) {
				String sourceAddressValue = sourceAddressMapForParamName
						.get(SOURCE_ADDRESS_VALUE);
				result = getAddress(sourceAddressValue);
			}else{
			    paramName = SOURCE_ADDRESS_DEBUT + name;
			    sourceAddressMapForParamName = ntfConfigManager
                    .getTable(SOURCE_ADDRESS_TABLE).get(paramName);
			    if (sourceAddressMapForParamName != null) {
	                String sourceAddressValue = sourceAddressMapForParamName
	                        .get(SOURCE_ADDRESS_VALUE);
	                result = getAddress(sourceAddressValue);
	            }
			}
		}
		
		if ( result == null ) {
			result = getSmeSourceAddress(); //return default.
		}
		
		sourceAddressCache.put(paramName,result);
		
		return result;

	}  
    
    public static SMSAddressInfo getSmeSourceAddress() {

    	SMSAddressInfo result = null;
    	String value = ntfConfigManager.getParameter(SME_SOURCE_ADDRESS);
    	
    	if (value == null) {
    		value = "";
    	}

    	value = value.trim();

    	result = getAddress(value);
    	if (result == null) {

    		value = "" + getSmeSourceTon() + ","
    				+ getSmeSourceNpi() + "," + value;
    		result = getAddress(value);

    	}
    	if (result == null) {
    		result = new SMSAddressInfo(getSmeSourceTon(), getSmeSourceNpi(),"");
    		log.warn("Bad value for " + SME_SOURCE_ADDRESS + "=" + value +" substituting " + result.toString());
    	}   	
    	return result;
    }
	


	/**
	 * getAddress creates an SMS address by parsing a string of the form
	 * [ton,npi,]number. If TON and NPI are absent, they are taken from the
	 * parameters smesourceaddresston and smesourceaddresnpi.
	 *
	 * @param adr - the name of the parameter specifying the address.
	 *@return an SMSAddressInfo with the configured number.
	 */
	private static SMSAddressInfo getAddress(String adr) {
		if (adr == null || adr.isEmpty()) {
			return null;
		}
			
        int first = adr.indexOf(",");
        int last = adr.lastIndexOf(",");
        
        if (first < 0) { //TON and NPI missing 
        	//return with default NPI and TON.
        	int ton = getSmeSourceTon();
            int npi = getSmeSourceNpi();
            String parsedAdr=formatNumber(adr, ton, npi);
        	return new SMSAddressInfo(ton, npi, parsedAdr);
        }

        if ( first == last  //TON or NPI missing
            || adr.indexOf(",", first + 1) != last) { //3 or more commas
        	log.warn("Bad sourceAddress in Table: " + adr);
        	return null;            
        }
        try {
            int ton = Integer.parseInt(adr.substring(0, first).trim());
            int npi = Integer.parseInt(adr.substring(first + 1, last).trim());
            String nbr = adr.substring(last + 1).trim();

            // Remove the leading '+' etc if needed
            nbr = formatNumber(nbr, ton ,npi);
            return new SMSAddressInfo(ton, npi, nbr);

        } catch (NumberFormatException e) {
            return null;
        }
	}
	
    /**
     * 
     * Format number, stripping tel: if in string.
     * and stripping plus if not international format.
     * @param number the telephone number string (could be a name or ip depending on ton/npi)
     * @param ton - Type of number
     * @param npi - Numbering plan indicator
     * @return
     */
    private static String formatNumber(String number, int ton, int npi){
        String tempNumber = number;
        if(number.toLowerCase().startsWith("tel:")){
            tempNumber = number.toLowerCase().replaceAll("tel:", "");
        }
        // Remove the leading '+' if present 
        return stripLeadingPlus(tempNumber, ton, npi);
    }
    
    /**
     * Will strip the leading '+' of from an international number in E.164 (ISDN) format number if present.
     * ton == 1 means an international number
     * npi == 1 means a number in E.164 (ISDN) format (see SMPP specification)
     * If no leading '+' is present, the received number is returned as is.
     * @param s - Phone number passed as a String
     * @param ton - the TON type of number
     * @param npi - the NPI, Numbering Plan Indicator
     * @return a phone number as a String without a leading plus.
     */
    private static String stripLeadingPlus(String s, int ton, int npi) {
        if(ton == 1 && npi == 1) {
            if (s.startsWith("+")) {
                return s.substring(1);
            } else {
                return s;
            }
        }
        return s;
    }
       
    public static int getTypeOfNumber() {
        return getNtfIntValue(SMS_TYPE_OF_NUMBER);
    }
    
    public static int getSmeSourceNpi() {
        return getNtfIntValue(SME_SOURCE_NPI);
    }

    public static int getSmeSourceTon() {
        return getNtfIntValue(SME_SOURCE_TON);
    }
    
	public static CancelSmsEnabledForEvent getCancelSmsEnabledForEvent() {
	    CancelSmsEnabledForEvent value = CancelSmsEnabledForEvent.none;
        try {
            value = CancelSmsEnabledForEvent.valueOf(ntfConfigManager.getParameter(CANCEL_SMS_ENABLED_FOR_EVENT));
        }
        catch (IllegalArgumentException ia) {
            log.warn("Illegal value for parameter: "  + CANCEL_SMS_ENABLED_FOR_EVENT + " value: [" + ntfConfigManager.getParameter(CANCEL_SMS_ENABLED_FOR_EVENT) + "], assuming " +  CancelSmsEnabledForEvent.none);
        } catch (NullPointerException ia) {
            log.warn("Illegal value for parameter: "  + CANCEL_SMS_ENABLED_FOR_EVENT + " value: [null] will assuming: "+ CancelSmsEnabledForEvent.none);            
        }
        
        return value;    
    }
	
	public static boolean isCancelEnabled() {
		return (getCancelSmsEnabledForEvent() != CancelSmsEnabledForEvent.none);
	}
	
	public static boolean isDisableSmscReplace() {
		if ( disableSmscReplace == null ) { 
			disableSmscReplace = new Boolean (getBooleanUsingNtfConvention(DISABLE_SMSC_REPLACE));
		}
		return disableSmscReplace;
	}

    
    /*
     * PRIVATE HELPER METHODS
     */

    private static int getNotifierPluginIntValue(String param) {
        return getIntValueFromString(cphrPluginConfigManager.getParameter(param));
    }
    
    private static int getNtfIntValue(String param) {
        return getIntValueFromString(ntfConfigManager.getParameter(param));
    }
    
    private static int getIntValueFromString(String value) {
        if(value != null) {
            try {
                return Integer.parseInt(value);
            }catch(NumberFormatException nfe) {
                return -1;
            }
        }
        return -1;        
    }
    
	/**
	 * Get a boolean parameter.
	 *
	 * @param key
	 *            the name of the boolean config parameter
	 *@return the boolean value named key, or default
	 */
	private static boolean getBooleanUsingNtfConvention(String key) {
		String value = ntfConfigManager.getParameter(key);
		return getBooleanUsingNtfConventionFromValue(value);
	}
	
	public static final String ON 								= "on";
	public static final String OFF 								= "off";
	public static final String YES 								= "yes";
	public static final String NO 								= "no";
	public static final String FALSE 							= "false";
	public static final String TRUE 							= "true";
	public static final String ONE 								= "1";
	public static final String ZERO 							= "0";

	private static boolean getBooleanUsingNtfConventionFromValue(String value) {

		if (value == null) {
			return false;
		}
		
		String trueWords[] = { ON, YES, TRUE, ONE };
		String falseWords[] = { OFF, NO, FALSE, ZERO };
		int i;
		
		for (i = 0; i < trueWords.length; i++) {
			if (value.compareToIgnoreCase(trueWords[i]) == 0) {
				return true;
			}
		}
		for (i = 0; i < falseWords.length; i++) {
			if (value.compareToIgnoreCase(falseWords[i]) == 0) {
				return false;
			}
		}
		log.warn("NotificationConfig: " + value
				+ " should be on, true, yes, off, false or no, not \"" + value
				+ "\"");
		
		return false;
	}



}
