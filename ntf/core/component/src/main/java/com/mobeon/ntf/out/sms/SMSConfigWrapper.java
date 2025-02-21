/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.sms;

import com.abcxyz.messaging.common.oam.ConfigManager;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.mobeon.common.sms.SMSConfig;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.event.PhoneOnEventListener;

import java.util.*;


public class SMSConfigWrapper implements SMSConfig  {
	private static final String SMPP_ERROR_CODES_TABLE_TAG 	= "SmppErrorCodes.Table";
    private static final String SMPP_NETWORK_ERROR_CODES_TABLE_TAG  = "SmppNetworkErrorCodes.Table";
	private static final String SMPP_ERROR_CODE_ACTION_TAG	= "smppErrorCodeAction";
    private static final String SMPP_ERROR_CODE_CLIENT_ACTION_TAG  = "smppErrorCodeClientAction";
    private static final String SMPP_NETWORK_ERROR_CODE_ACTION_TAG  = "smppNetworkErrorCodeAction";
	private static final String DEFAULT 					= "default";
    private static final String SMPP_NETWORK_ERROR_CODE_DEFAULT = "smppNetworkErrorCodeDefault";
	private static final String HEXADECIMAL_DEBUT			= "0x";
	private static final String SMSC_TEMPORARY_UNAVAILABLE_PERIOD_IN_SECS = "smscTemporaryUnavailablePeriodInSecs";

    private static Hashtable<Integer, String>  smppErrorCodeClientActions = new Hashtable<Integer, String>();
    private static Hashtable<Integer, String>  smppErrorCodeActions = new Hashtable<Integer, String>();
    private static Hashtable<Integer, String>  smppNetworkErrorCodeActions = new Hashtable<Integer, String>();
        
    private static Logger log = Logger.getLogger(SMSConfigWrapper.class);
	private static ConfigManager smppErrorCodesConfigManager = null;


    public static void initSMPPErrorCodeActions() throws ConfigurationDataException {
        initSMPPErrorCodeActions(Config.getNtfHome() + "/cfg/smppErrorCodes.conf");
    }

    public static void initSMPPErrorCodeActions(String errorCodeFile) throws ConfigurationDataException {
        smppErrorCodesConfigManager = OEManager.getConfigManager(errorCodeFile);
        
        Hashtable<Integer, String> temp_smppErrorCodeClientActions = new Hashtable<Integer, String>();
        Hashtable<Integer, String> temp_smppErrorCodeActions = new Hashtable<Integer, String>();

        //fill the smppErrorCodeActions object with the config values such that the key is an Integer and not a String
        Map<String, Map<String, String>> smppErrorCodesTable = smppErrorCodesConfigManager.getTable(SMPP_ERROR_CODES_TABLE_TAG);
        if (smppErrorCodesTable != null && (smppErrorCodesTable.size() > 0)) {
            Iterator<String> it = smppErrorCodesTable.keySet().iterator();
            while (it.hasNext()) {
                String errorCode = it.next();
                if (errorCode.startsWith(HEXADECIMAL_DEBUT)) {
                    try {
                        String actionCode = smppErrorCodesTable.get(errorCode).get(SMPP_ERROR_CODE_ACTION_TAG);
                        String errorCodeSubstring = errorCode.substring(HEXADECIMAL_DEBUT.length());
                        temp_smppErrorCodeActions.put(Integer.parseInt(errorCodeSubstring, 16), actionCode);
                        
                        String smppClientAction = smppErrorCodesTable.get(errorCode).get(SMPP_ERROR_CODE_CLIENT_ACTION_TAG);
                        if(smppClientAction != null) {
                            temp_smppErrorCodeClientActions.put(Integer.parseInt(errorCodeSubstring, 16), smppClientAction);
                        }
                    } catch (IndexOutOfBoundsException iobe) {
                        log.logMessage("SMSConfigWrapper.initSMPPErrorCodeAction() IndexOutOfBoundsException for " +  errorCode + ": " + iobe, Logger.L_ERROR);
                    } catch (NumberFormatException nfe) {
                        log.logMessage("SMSConfigWrapper.initSMPPErrorCodeAction() NumberFormatException for " +  errorCode + ": " + nfe, Logger.L_ERROR);
                    }
                }
            }
        }
        
        initSMPPNetworkErrorCodeActions();
        
        smppErrorCodeActions = temp_smppErrorCodeActions;
        smppErrorCodeClientActions = temp_smppErrorCodeClientActions;
    }
    
    private static void initSMPPNetworkErrorCodeActions() 
    {
        Hashtable<Integer, String>  temp_smppNetworkErrorCodeActions = new Hashtable<Integer, String>();
        
        //fill the smppErrorCodeActions object with the config values such that the key is an Integer and not a String
        Map<String, Map<String, String>> smppNetworkErrorCodesTable = smppErrorCodesConfigManager.getTable(SMPP_NETWORK_ERROR_CODES_TABLE_TAG);
        if (smppNetworkErrorCodesTable != null && (smppNetworkErrorCodesTable.size() > 0)) {
            Iterator<String> it = smppNetworkErrorCodesTable.keySet().iterator();
            while (it.hasNext()) {
                String errorCode = it.next();
                if (errorCode.startsWith(HEXADECIMAL_DEBUT)) {
                    try {
                        String actionCode = smppNetworkErrorCodesTable.get(errorCode).get(SMPP_NETWORK_ERROR_CODE_ACTION_TAG);
                        String errorCodeSubstring = errorCode.substring(HEXADECIMAL_DEBUT.length());
                        temp_smppNetworkErrorCodeActions.put(Integer.parseInt(errorCodeSubstring, 16), actionCode);
                        log.logMessage("New Network Error Code: Key=" + Integer.parseInt(errorCodeSubstring, 16) + ", Action=" + actionCode, Logger.L_DEBUG);
                    } catch (IndexOutOfBoundsException iobe) {
                        log.logMessage("SMSConfigWrapper.initSMPPNetworkErrorCodeActions() IndexOutOfBoundsException for " +  errorCode + ": " + iobe, Logger.L_ERROR);
                    } catch (NumberFormatException nfe) {
                        log.logMessage("SMSConfigWrapper.initSMPPNetworkErrorCodeActions() NumberFormatException for " +  errorCode + ": " + nfe, Logger.L_ERROR);
                    }
                }
            }
        }
        
        smppNetworkErrorCodeActions = temp_smppNetworkErrorCodeActions;
    }

    /**
     * Returns the string that contains all the content of the smmpErrorCodes.conf file.
     * Only valid error codes are returned.
     * @return error as string.
     */
	public static String getSmppErrorCodesAsString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("\nThe properly configured smpp error codes are : ");
		stringBuffer.append("\n" + DEFAULT + "=" + getDefaultErrorAction());
		if ((smppErrorCodeActions != null) && (smppErrorCodeActions.size() > 0)) {
			Iterator<Integer> it = smppErrorCodeActions.keySet().iterator();
			while (it.hasNext()) {
				Integer errorCode = it.next();
				int errorCodeLength = Integer.toHexString(errorCode).length();
				stringBuffer.append("\n").append(HEXADECIMAL_DEBUT);
				//the SMMP Error Codes are not longer than 8 characters plus "0x"
				for (int i = 0; i < (8-errorCodeLength); i++) {
					stringBuffer.append("0");
				}
				stringBuffer.append(Integer.toHexString(errorCode)).append("=").append(smppErrorCodeActions.get(errorCode));
			}
		} else {
			stringBuffer.append("empty");
		}
		stringBuffer.append("\n");
		return stringBuffer.toString();
	}


    /** Returns an error action configured in file smppErrorCodes.conf
     * @param errorCode - the SMPP error code as a number
     * @return an error action
     */
    public static String getErrorAction(Integer errorCode) {
        log.logMessage("Fetch error code action.", Logger.L_DEBUG);
        String errorAction = smppErrorCodeActions.get(errorCode);
		if (errorAction != null) {
			return errorAction;
		} else {
			return getDefaultErrorAction();
		}
	}
    
    public static String getNetworkErrorAction(Integer errorCode)
    {
        log.logMessage("Fetch network error code action.", Logger.L_DEBUG);
        String action =  smppNetworkErrorCodeActions.get(errorCode);
        if (action != null) {
            return action;
        } else {
            log.logMessage("Default network error code action used.", Logger.L_DEBUG);
            return smppErrorCodesConfigManager.getParameter(SMPP_NETWORK_ERROR_CODE_DEFAULT);
        }
    }
    
    public static String getDefaultNetworkErrorAction()
    {
        return smppErrorCodesConfigManager.getParameter(SMPP_NETWORK_ERROR_CODE_DEFAULT);
    }

    /**
     * Returns the SMPP client action to be taken for the given error code.
     * @param errorCode - the SMPP error code as a number
     * @return the SMPP client action
     */
    public static String getErrorClientAction(Integer errorCode) {
        return smppErrorCodeClientActions.get(errorCode);
    }
    
    /** Returns the default error action configured in file SMPPErrorCodes.cf
     * @return an error action
     */
    public static String getDefaultErrorAction() {
        return smppErrorCodesConfigManager.getParameter(DEFAULT);
    }
    
    /**
     * Returns the time period in seconds for which the SMSC will be made temporarily unavailable.
     * @return time period in seconds
     */
    public static int getSmscTemporaryUnavailablePeriodInSecs() {
        return smppErrorCodesConfigManager.getIntValue(SMSC_TEMPORARY_UNAVAILABLE_PERIOD_IN_SECS);
    }

    public String[] getAllowedSmsc() {
        return Config.getAllowedSmsc();
    }

    public String getCharConvPath() {
        return Config.getNtfHome() + "/cfg";
    }

    public String[] getMwiServer(String smsc) {
        return Config.getMwiServer(smsc);
    }

    public int getNumberOfSms() {
        return Config.getNumberOfSms();
    }

    public PhoneOnEventListener getPhoneOnEventListener() {
        return EventRouter.get();
    }

    public int getSMSQueueSize() {
        return Config.getSmsQueueSize();
    }

    public String getSmeServiceType() {
        return Config.getSmeServiceType();
    }
    
    public String getVvmServiceType() {
        return Config.getVvmServiceType();
    }

    public String getSmeServiceTypeForMwi() {
        return Config.getSmeServiceTypeForMwi();
    }

    public int[] getSmppErrorCodesIgnored() {
        return Config.getSmppErrorCodesIgnored();
    }

    public int getSmsMaxConnections() {
        return Config.getSmsMaxConn();
    }

    public int getSmsPriority() {
        return Config.getSmsPriority();

    }

    public int getSmsStringLength() {
        return Config.getSmsStringLength();
    }

    public String getSmscBackup(String smsc) {
        return Config.getSmscBackup(smsc);
    }

    public String getSmscErrorAction() {
        return Config.getSmscErrorAction();
    }

    public boolean getSmscLoadBalancing() {
        return Config.isSmsHandlerLoadBalanced();
    }

    public int getSmscPollInterval() {
        return Config.getSmscPollInterval();
    }

    public int getSmscTimeout() {
        return Config.getSmscTimeout();
    }

    public boolean isBearingNetworkGSM() {
        return Config.isBearingNetworkGsm();
    }

    public boolean isBearingNetworkCdma2000() {
        return Config.isBearingNetworkCdma2000();
    }

    public boolean isKeepSmscConnections() {
        return Config.isKeepSmscConnections();
    }

    public boolean isReplyPath() {
        return Config.isSetReplyPath();
    }

    public int getSmppVersion() {
        return Config.getSmppVersion();
    }

    public boolean isAlternativeFlashDcs() {
        return Config.isAlternativeFlashDcs();
    }

    public int getVvmSourcePort() {
        return Config.getVvmSourcePort();
    }

    public int getVvmDestinationPort() {
        return Config.getVvmDestinationPort();
    }

    public int getSmsMinTimeBetweenConnections() {
        return Config.getSmsMinTimeBetweenConnections();
    }

    public int getSmsMinTimeBetweenReConnections() {
        return Config.getSmsMinTimeBetweenReconnects();
    }

    public static void refreshConfig() {
        try {
            initSMPPErrorCodeActions();
        } catch (ConfigurationDataException e) {
            log.logMessage("Could not refresh due to ConfigurationDataException", Logger.L_DEBUG);
        }
        
    }
}

