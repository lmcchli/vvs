/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.shiftone.cache.util.Log;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.util.OSType;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.mobeon.common.logging.LogAgentFactory;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.smscom.SMSComDataException;
import com.mobeon.ntf.out.fax.FaxPrintOut;
import com.mobeon.ntf.util.Logger;

public class Config implements Constants, NotificationConfigConstants {
    private static Logger legacyLogger = Logger.getLogger(Config.class); //log using old Solaris style logger from MoIP..
	private static ConfigManager ntfConfigManager = null;
	private static String configFileName = null;
    protected static String installDir 	= null;
    protected static String ntfHome 	= null;
    protected static String ntfHost 	= null;
    protected static String ntfHostFqdn = null;
    protected static String ntfIp 		= null;
    private static LogAgent logAgent =  LogAgentFactory.getLogAgent(Config.class);
    private static String 	version		= "unknown";
    
    //Cache to prevent re-reading and parsing of the sourceAddress tables which is done for
    //almost all notification types.
    private static Map<String, SMSAddress> sourceAddressCache = new HashMap<String,SMSAddress>();

    static {
        setLocalHost();
        installDir = findInstallDir();
        ntfHome = findNtfHome();
        configFileName = System.getProperty(CONFIG_FILE);
        if (configFileName == null) {
            configFileName = ntfHome + "/cfg/notification.conf";
        }
   }

	public static void loadCfg() throws ConfigurationDataException  {
		initializeNtfConfigManager(configFileName);
        setVersion();
		printConfig();
	}

	/**
	 * Get the configuration from a file.
	 *
	 * @param fileName
	 *            - the name of the file to read parameters from.
	 */
	private static void initializeNtfConfigManager(String fileName) throws ConfigurationDataException {
		if (ntfConfigManager == null) {
			ntfConfigManager = OEManager.getConfigManager(fileName,logAgent);
		}
	}

	public static void reloadConfig() throws ConfigurationDataException {
	    	    
		  ntfConfigManager = OEManager.getConfigManager(configFileName, logAgent);
		  FaxPrintOut.get().updateConfig();
		  sourceAddressCache .clear();

		  printConfig();
	}

	private static void printConfig() {

		//simple attributes
		legacyLogger.logMessage("NTF config parameter: mmsMaxMsgSize = " + Config.getMmsMaxMsgSize(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.AUTO_FORWARDED_MESSAGES + " = " + Config.isAutoforwardedMessagesOn(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: bearingNetworkGsm = " + Config.isBearingNetworkGsm(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: bearingNetworkCdma2000 = "  + Config.isBearingNetworkCdma2000(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: bearingNetworkPstn = "  + Config.isBearingNetworkPstn(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.CALL_MWI_CALLER + " = "  + Config.getCallMwiCaller(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.CANCEL_SMS_AT_RETRIEVAL + " = "  + Config.isCancelSmsAtRetrieval(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.CHECK_QUOTA + " = "  + Config.isCheckQuota(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.CHECK_TERMINAL_CAPABILITY + " = "  + Config.isCheckTerminalCapability(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.DEFAULT_DATE_FORMAT + " = "  + Config.getDefaultDateFormat(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.DEFAULT_LANGUAGE + " = "  + Config.getDefaultLanguage(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.DEFAULT_NOTIFICATION_FILTER + " = "  + Config.getDefaultNotificationFilter(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.DEFAULT_NOTIFICATION_FILTER_2 + " = "  + Config.getDefaultNotificationFilter2(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: isDefaultUserHasMwi = "  + Config.isDefaultUserHasMwi(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: isDefaultUserHasFlash = "  + Config.isDefaultUserHasFlash(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: isDefaultUserHasReplace = "  + Config.isDefaultUserHasReplace(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: phoneOnMethod = " + Config.getPhoneOnMethod(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: checkRoaming = " + Config.getCheckRoaming(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: fallbackWhenRoaming = " + Config.getFallbackWhenRoaming(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: fallbackUseMoipUserNtd = " + Config.getFallbackUseMoipUserNtd(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: checkBusyBeforeOutdialNotification = " + Config.getCheckBusy(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: getSubscriberChargingModel = " + Config.getSubscriberChargingModel(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: roamingTemplatePosition = " + Config.getRoamingTemplatePosition(), Logger.L_VERBOSE);

		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_SMS + " = "  + Config.getFallbackSms(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_SIPMWI + " = "  + Config.getFallbackSipMwi(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_OUTDIAL + " = "  + Config.getFallbackOutdial(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_OUTDIAL_TO_FLS_CONTENT + " = "  + Config.getFallbackOutdialToFlsContent() , Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_OUTDIAL_TO_SMS_CONTENT + " = "  + Config.getFallbackOutdialToSmsContent() , Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_VVM + " = "  + Config.getFallbackVvm(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_VVM_TO_SMS_CONTENT + " = "  + Config.getFallbackVvmToSmsContent() , Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_SMS_ON_URGENT_IF_FLS_SENT + " = " + Config.getFallbackSmsOnUrgentIfFlsSent() , Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FALLBACK_SMS_WHEN_MWI_SMS_ONLY + " = " + Config.getFallbackSmsWhenMwiSmsOnly() , Logger.L_VERBOSE);

		legacyLogger.logMessage("NTF config parameter: emailForwardMaximumSize = " + Config.getEmailForwardMaximumSize(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: emailForwardTranscodeAudio = " + Config.isEmailForwardTranscodeVideoOn(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: emailForwardTanscodeVideo = " + Config.isEmailForwardTranscodeAudioOn(), Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: emailForwardOutputAudioMimeType = " + Config.getEmailForwardOutputAudioMimeType(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: emailForwardOutputVideoMimeType = " + Config.getEmailForwardOutputVideoMimeType(), Logger.L_VERBOSE);

        Logger.getLogger().logMessage("NTF config parameter: " + NotificationConfigConstants.SMSC_SHUTDOWN_PERIOD + " = "+ Config.getSmscShutdownPeriod(), Logger.L_VERBOSE);
                legacyLogger.logMessage("NTF config parameter: getSendDetachOnAssumedUnavailable = " + Config.getSendDetachOnAssumedUnavailable(), Logger.L_VERBOSE);
		//list attributes
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.ALLOWED_SMSC_LIST + " = [" + printStringArray(Config.getAllowedSmsc()) + "]", Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.REPLACE_NOTIFICATIONS_LIST + " = [" + printStringArray(Config.getReplaceNotifications()) + "]", Logger.L_VERBOSE);
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.SMPP_ERROR_CODES_IGNORED_LIST + " = [" + printIntArray(Config.getSmppErrorCodesIgnored()) + "]", Logger.L_VERBOSE);


		//Fax Attribute
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FAX_SUCCESS_NOTIFICATION + " = "  + Config.isFaxSuccessNotification(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FAX_PRINT_NOTIFY_RETRY_SCHEMA + " = "  + Config.getFaxPrintNotifRetrySchema(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FAX_PRINT_EXPIRE_TIME_IN_MIN + " = "  + Config.getFaxprintExpireTimeInMin(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FAX_PRINT_QUEUE_SIZE + " = "  + Config.getFaxPrintQueueSize(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FAX_WORKER_QUEUE_SIZE + " = "  + Config.getFaxWorkerQueueSize(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FAX_WORKERS + " = "  + Config.getFaxWorkers(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FAX_PRINT_MAX_CONN + " = "  + Config.getFaxPrintMaxConn(), Logger.L_VERBOSE);

        //Lock attribute
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.SLAMDOWN_MCN_PHONE_ON_LOCK_FILE_VALIDITY_IN_SECONDS + " = "  + Config.getSlamdownMcnPhoneOnLockFileValidityInSeconds(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.OUTDIAL_PHONE_ON_LOCK_FILE_VALIDITY_IN_SECONDS + " = "  + Config.getOutdialPhoneOnLockFileValidityInSeconds(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.ALERT_SC_REGISTRATION_NUMBER_OF_RETRY + " = "  + Config.getAlertSCRegistrationNumOfRetry(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY + " = "  + Config.getAlertSCRegistrationTimeInSecBetweenRetry(), Logger.L_VERBOSE);

        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.DELAYED_EVENT_QUEUE_SIZE + " = "  + Config.getDelayedEventQueueSize(), Logger.L_VERBOSE);
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.DELAYED_EVENT_WORKERS + " = "  + Config.getDelayedEventWorkers(), Logger.L_VERBOSE);

        //HLR notification error roaming failure actions.
        legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.HLR_ROAM_FAILURE_ACTION + " = "  + Config.getHLRRoamingFailureAction(), Logger.L_VERBOSE);
    	// table attributes
		printTableConfig(SOURCE_ADDRESS_TABLE, SOURCE_ADDRESS_VALUE);
		printTableConfig(VALIDITY_TABLE, VALIDITY_VALUE);
		printMwiServersConfig();
		printTableConfig(SMSC_BACKUP_TABLE, SMSC_BACKUP_VALUE);
		printTableConfig(LANG_TO_MIME_TEXT_CHARSET_TABLE, LANG_TO_MIME_TEXT_CHARSET_VAL);
		printTableConfig(NOTIFIER_PLUGIN_TABLE, NOTIFIER_PLUGIN_CLASS);
		
		// FS timeout
		legacyLogger.logMessage("NTF config parameter: " + NotificationConfigConstants.FS_TIMEOUT + " = "  + Config.getFsTimeout(), Logger.L_VERBOSE);
	}

	private static void printTableConfig(String tableName, String tableValue) {
		Map<String, Map<String, String>> sourceAddressMap = ntfConfigManager.getTable(tableName);
		if (sourceAddressMap.isEmpty()) {
			legacyLogger.logMessage("NTF " + tableName + " is empty.", Logger.L_VERBOSE);
		} else {
			Iterator<String> it = sourceAddressMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				legacyLogger.logMessage("NTF " + tableName + " contains the element " + key + " with the value "  + sourceAddressMap.get(key).get(tableValue) + ".", Logger.L_VERBOSE);
			}
		}
	}

	private static void printMwiServersConfig() {
	    Map<String, Map<String, String>> mwiServersMap = ntfConfigManager.getTable(MWI_SERVERS_TABLE);
	    Iterator<String> it = mwiServersMap.keySet().iterator();
	    while (it.hasNext()) {
	        String key = it.next();
	        legacyLogger.logMessage("NTF mwi servers for the smsc parameter: " + key + " = ["  + printStringArray(getStringArrayLine(mwiServersMap.get(key).get(MWI_SERVERS))) + "]", Logger.L_VERBOSE);
	    }
	}

	private static String printStringArray(String[] stringArray) {
		String result = "";
		if (stringArray != null) {
			int size = stringArray.length;
			if (size >= 1) {
				result = stringArray[0];
				for (int i = 1; i < size ; i++) {
					result = result + ", " + stringArray[i];
				}
			}
		}
		
		return result;
	}

	private static String printIntArray(int[] intArray) {
		String result = "";
		if (intArray != null) {
			int size = intArray.length;
			if (size >= 1) {
				result = result + intArray[0];
				for (int i = 1; i < size ; i++) {
					result = result + ", " + intArray[i];
				}
			}
		}

		return result;
	}

	/**
	 * PRIVATE METHDOS USED BY THE PUBLIC METHODS TO RETURN THE VALUES OF THE
	 * PARAMETERS
	 **/
	/**
	 * This method transforms an ArrayList (msgcore returned object) into a String array (used by the Ntf Config public methods).
	 */
	private static String[] getStringArray(ArrayList<String> value) {
		if (value != null) {
			String[] arg = new String[value.size()];
			arg = value.toArray(arg);

			return arg;
		}
		else {
			return new String[0];
		}
	}

	private static String[] getStringArrayLine(String line) {

		Vector<String> v = new Vector<String>();
		String[] arr;

		if (line == null || line.equals("")) {
			return new String[0];
		} else {
			StringTokenizer st = new StringTokenizer(line, ", ");
			while (st.hasMoreTokens()) {
				v.add(st.nextToken());
			}
			arr = new String[v.size()];
			int i = 0;
			Iterator<String> it = v.iterator();
			while (it.hasNext()) {
				arr[i++] = it.next();
			}
		}
		return arr;

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

	private static boolean getBooleanUsingNtfConventionFromValue(String value) {

		String trueWords[] = { ON, YES, TRUE, ONE };
		String falseWords[] = { OFF, NO, FALSE, ZERO };
		int i;

		if (value == null) {
			return false;
		}
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
		System.err.println("NotificationConfig: " + value
				+ " should be on, true, yes, off, false or no, not \"" + value
				+ "\"");
		return false;
	}


	/**
	 * Get an int array.
	 *
	 *@return the int array with the elements coming from the ArrayList value, or int[0]
	 */
	public static int[] getIntArray(ArrayList<String> value) {

		if ((value != null) && (value.size() > 0)) {
			ArrayList<Integer> result = new ArrayList<Integer>();
			Iterator<String> it = value.iterator();
			while (it.hasNext()) {
				String smppErrorCode = it.next();
				try {
					if (smppErrorCode.indexOf("x") == -1) {
						while (smppErrorCode.startsWith("0") && (smppErrorCode.length() > 1)) {
							smppErrorCode = smppErrorCode.substring(1);
						}
					}
					result.add(Integer.decode(smppErrorCode));
				} catch (Exception e) {
					// This should not happen because the type smppErrorCodeIngnored is defined in the notification.xsd
					// with a regular expression as pattern for the smpp error codes.
					System.err.println("NotificationConfig: SmppErrorCodesIgnored.List" + " contains unparsable integer " + smppErrorCode);
				}
			}
			//convert the ArrayList<Integer> to int[] in order to keep the signature of the public method for SmppErrorCodesIgnored
			int[] intResult = new int[result.size()];
			for (int i = 0; i < result.size(); i++ ) {
				intResult[i] = result.get(i).intValue();
			}
			return intResult;
		} else {
			return new int[0];
		}
	}

	/**
	 * getAddress creates an SMS address by parsing a string of the form
	 * [ton,npi,]number. If TON and NPI are absent, they are taken from the
	 * parameters smesourceaddresston and smesourceaddresnpi.
	 *
	 * @param value
	 *            - the name of the parameter specifying the address.
	 *@return an SMSAddress with the configured number.
	 */
	private static SMSAddress getAddress(String value) {
		SMSAddress result = null;

		if (value != null) {
			value = value.trim();

			try {
				result = new SMSAddress(value);
			} catch (SMSComDataException e) {
				try {
					result = new SMSAddress("" + getSmeSourceTon() + ","
							+ getSmeSourceNpi() + "," + value);
				} catch (SMSComDataException e2) {
					System.err.println("Config: has a bad value: " + e2);
				}
			}
		}
		return result;
	}

	/** METHODS RELATED TO THE INSTALLATION OF THE NTF */

    private static void setLocalHost() {
        try {
            ntfHost = InetAddress.getLocalHost().getHostName();
            ntfIp = InetAddress.getLocalHost().getHostAddress();

        } catch (Exception e) {

            ntfHost = "";
            ntfIp = "";
        }
        setNtfHostFqdn();
    }

    public static void setNtfHostFqdn() {
        ntfHostFqdn = ntfHost;
        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) { return; }
        BufferedReader f = null;
        try {
            f = new BufferedReader(new FileReader("/etc/resolv.conf"));
            String line;
            while ((line = f.readLine()) != null) {
                int ix = line.indexOf("domain");
                if (ix >= 0) {
                    ntfHostFqdn = ntfHost + "." + line.substring(ix + 6).trim();
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to determine fully qualified domain name: " + e);
        } finally {
            try {
                if ( f !=null )
                    f.close();
            } catch (IOException e) {
                //ignore.
            }
        }
    }

    private static File getParentFile(File f) {
        File p = null;
        if (f != null) {
            p = getCanonicalFile(f).getParentFile();
            if (p != null) {
                return p;
            }
        }
        return new File("/");
    }

    private static File getCanonicalFile(File f) {
        try {
            return f.getCanonicalFile();
        } catch (IOException e) {
            return f.getAbsoluteFile();
        }
    }

    private static File makeFile(String name) {
        return getCanonicalFile(new File(name));
    }

    private static boolean isNtfInstanceDir(File d) {
        return new File(d, "logs").isDirectory()
            && new File(d, "cfg").isDirectory()
            && new File(new File(d, "templates"), "en.phr").isFile();
    }

    private static String findInstallDir() {
        String result = null;
        StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), ":");
        String path;
        while (st.hasMoreTokens() && result == null) {
            path = st.nextToken();
            if (path.endsWith("ntf.jar")) {
                result = getParentFile(getParentFile(makeFile(path))).getPath();
            }
        }
        if (result == null) {
        	if (OSType.getInstance().isOSWindows()) {
                result = "config";
        	} else {
                result = "/opt/moip/ntf";
        	}
        }
        return result;
    }


    private static String findNtfHome() {
        String result;
        File dir = null;

		result = System.getProperty(NTF_HOME);
		String configFileName = System.getProperty(CONFIG_FILE);
        if (result == null && configFileName != null) {
            //Derive the NTF home from the config file path, if the configfile
            //seems to be located in an NTF instance directory
            dir = getParentFile(getParentFile(makeFile(configFileName)));
            if (isNtfInstanceDir(dir)) {
                result = dir.getPath();
            }
        }
        if (result == null) {
            //Try the current directory
			dir = makeFile(System.getProperty(USER_DIR));
            if (isNtfInstanceDir(dir)) {
                result = dir.getPath();
            }
        }
        if (result == null)  {
            //Try with the parent directory
            dir = getParentFile(dir);
            if (isNtfInstanceDir(dir)) {
                result = dir.getPath();
            }
        }
        if (result == null) {
            result = installDir;
        }
        return result;
    }

    private static void setVersion() {

        if (OSType.getInstance().isOSWindows()) {
        	return;
        }
        Properties vprops = new Properties();
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(installDir + "/VERSION"));
            vprops.load(bis);
            bis.close();
            version = vprops.getProperty("VERSION", "unknown");
        } catch (Exception e) {
            logAgent.info("Could not find version file, will use version unknown");
        }
    }

	public static String getDataDirectory() {
		return ntfHome + "/data";
	}

	public static String getConfigFileName() {
		return configFileName;
	}

	public static String getNtfHome() {
		return ntfHome;
	}

    public static String getNtfHostFqdn() {
		return ntfHostFqdn;
	}

    public static String getTemplateDir() {
        return getPhraseDirectory();
    }

	public static String getPhraseDirectory() {
		return ntfHome + "/templates";
	}

	public static String getInstallDir() {
		return installDir;
	}

    public static String getVersion() {
		return version;
	}

    /**
     * Sets the loglevel for NTF without restarting.
     *@param level - the new log level.
     */
    public static void setLogLevel(int level) {
        Logger.setLogLevel(level);
    }

    /**
     * Update the configuration from a property file.
     *@param fileName - the name of the file to read properties from.
     */
    public static void updateCfg(String fileName) {
    	configFileName = fileName;
    	updateCfg();
    }

    public static void updateCfg() {
        //updateCfg(configFileName);
    }

    /**
     * Gets the raw configuration value. This method is not type safe and should
     * only used by debugging tools.
     *@return the configuration value
     */
    public static String getConfigValue(String name) {
    	/** TO BE IMPLEMENTED*/
        return ntfConfigManager.getParameter(name);
    }

    /**
     * Set a configuration variable in runtime. This method is not type safe and
     * should only be used by debugging tools.
     *@return true if the parameter was set successfully, false otherwise.
     */
    public static boolean setCfgVar(String parameter, String value) {
    	boolean result = false;
    	try {
    		ntfConfigManager.setParameter(parameter, value);
    		result = true;
    	} catch (Exception ex) {
    		legacyLogger.logMessage("Config.setCfgVar(): couldn't set the parameter " + parameter + ". Exception: " + ex.getMessage(),Logger.L_ERROR);
    	}
        return result;
    }

	/** PUBLIC METHODS THAT RETURN THE PARAMETER' VALUES **/

	public static boolean isAutoforwardedMessagesOn() {
		return getBooleanUsingNtfConvention(AUTO_FORWARDED_MESSAGES);
	}

	public static String[] getAllowedSmsc() {
		return getStringArray(ntfConfigManager.getList(ALLOWED_SMSC_LIST));
	}

	public static boolean isBearingNetworkGsm() {
		boolean result = false;

		String bearingNetwork = ntfConfigManager.getParameter(BEARING_NETWORK);
		if (bearingNetwork != null) {
			result = (bearingNetwork.toLowerCase().indexOf(GSM) >= 0);
		}
		return result;
	}

	public static boolean isBearingNetworkCdma2000() {
		boolean result = false;

		String bearingNetwork = ntfConfigManager.getParameter(BEARING_NETWORK);
		if (bearingNetwork != null) {
			result = (bearingNetwork.toLowerCase().indexOf(CDMA2000) >= 0);
		}
		return result;
	}

	public static boolean isBearingNetworkPstn() {
		boolean result = false;

		String bearingNetwork = ntfConfigManager.getParameter(BEARING_NETWORK);
		if (bearingNetwork != null) {
			result = (bearingNetwork.toLowerCase().indexOf(PSTN) >= 0);
		}
		return result;
	}

	public static int getCallMwiCaller() {
		String v = ntfConfigManager.getParameter(CALL_MWI_CALLER);

		if (SUBSCRIBER.equalsIgnoreCase(v)) {
			return CALLMWI_CALLER_SUBSCRIBER;
		} else if (CALLER.equalsIgnoreCase(v)) {
			return CALLMWI_CALLER_CALLER;
		} else {
			return CALLMWI_CALLER_SYSTEM;
		}
	}

	@Deprecated //as of 3.3 CP03 vfe nl - cancel only introduced for MIO at this point anyway, legacy moip parameter.
	public static boolean isCancelSmsAtRetrieval() {
		return ntfConfigManager.getBooleanValue(CANCEL_SMS_AT_RETRIEVAL);
	}

	public static CancelSmsEnabledForEvent getCancelSmsEnabledForEvent() {
	    CancelSmsEnabledForEvent value = CancelSmsEnabledForEvent.none;
        try {
            value = CancelSmsEnabledForEvent.valueOf(ntfConfigManager.getParameter(CANCEL_SMS_ENABLED_FOR_EVENT));
        }
        catch (IllegalArgumentException ia) {
            logAgent.warn("Illegal value for parameter: "  + CANCEL_SMS_ENABLED_FOR_EVENT + " value: [" + ntfConfigManager.getParameter(CANCEL_SMS_ENABLED_FOR_EVENT) + "], assuming " +  CancelSmsEnabledForEvent.none);
        } catch (NullPointerException ia) {
            logAgent.warn("Illegal value for parameter: "  + CANCEL_SMS_ENABLED_FOR_EVENT + " value: [null] will assuming: "+ CancelSmsEnabledForEvent.none);
        }

        if (value == CancelSmsEnabledForEvent.none) {
            //check legacy if not set
            if (isCancelSmsAtRetrieval()) {
                value = CancelSmsEnabledForEvent.mailboxupdate;
            }
        }
        return value;
    }


	public static CancelSmsMethod getCancelSmsMethod() {
	    try {
	        return CancelSmsMethod.valueOf(ntfConfigManager.getParameter(CANCEL_SMS_METHOD));
	    }
	    catch (IllegalArgumentException ia) {
	        logAgent.warn("Illegal vaslue for parameter: "  + CANCEL_SMS_METHOD + " value: [" + ntfConfigManager.getParameter(CANCEL_SMS_METHOD) + "] will assume default: "+ CancelSmsMethod.sourceAddress);
	    } catch (NullPointerException ia) {
	        logAgent.warn("Illegal value for parameter: "  + CANCEL_SMS_METHOD + " value: [null] will assume default: "+ CancelSmsMethod.sourceAddress);
	    }
	    return CancelSmsMethod.sourceAddress; //return default in case of exception
	}


	public static boolean isCheckQuota() {
		return ntfConfigManager.getBooleanValue(CHECK_QUOTA);
	}

	public static boolean isCheckTerminalCapability() {
		return ntfConfigManager.getBooleanValue(CHECK_TERMINAL_CAPABILITY);
	}



	public static String getDefaultDateFormat() {
		return ntfConfigManager.getParameter(DEFAULT_DATE_FORMAT);
	}

	public static String getDefaultLanguage() {
		return ntfConfigManager.getParameter(DEFAULT_LANGUAGE);
	}

	public static String getDefaultNotificationFilter() {
		return ntfConfigManager.getParameter(DEFAULT_NOTIFICATION_FILTER);
	}

	public static String getDefaultNotificationFilter2() {
		return ntfConfigManager.getParameter(DEFAULT_NOTIFICATION_FILTER_2);
	}



	public static String getDefaultTimeFormat() {
		return ntfConfigManager.getParameter(DEFAULT_TIME_FORMAT);
	}

	public static boolean isDisableSmscReplace() {
		return getBooleanUsingNtfConvention(DISABLE_SMSC_REPLACE);
	}

	public static boolean isDiscardSmsWhenCountIs0() {
		return getBooleanUsingNtfConvention(DISCARD_SMS_WHEN_COUNT_IS_0);
	}

	public static boolean getDoOutdial() {
		return getBooleanUsingNtfConvention(DO_OUTDIAL);
	}

	public static int getOutdialQueueSize() {
		return ntfConfigManager.getIntValue(OUTDIAL_QUEUE_SIZE);
	}

	public static int getOutdialWorkers() {
		return ntfConfigManager.getIntValue(OUTDIAL_WORKERS);
	}

	public static String getOutdialStartRetrySchema() {
	    return ntfConfigManager.getParameter(OUTDIAL_START_RETRY_SCHEMA);
	}

	public static int getOutdialStartExpireTimeInMin() {
	    return ntfConfigManager.getIntValue(OUTDIAL_START_EXPIRE_TIME_IN_MIN);
	}

	public static String getOutdialLoginRetrySchema() {
	    return ntfConfigManager.getParameter(OUTDIAL_LOGIN_RETRY_SCHEMA);
	}

	public static int getOutdialLoginExpireTimeInMin() {
	    return ntfConfigManager.getIntValue(OUTDIAL_LOGIN_EXPIRE_TIME_IN_MIN);
	}

	public static String getOutdialCallRetrySchema() {
	    return ntfConfigManager.getParameter(OUTDIAL_CALL_RETRY_SCHEMA);
	}

	public static int getOutdialCallExpireTimeInMin() {
	    return ntfConfigManager.getIntValue(OUTDIAL_CALL_EXPIRE_TIME_IN_MIN);
	}

	public static int getOutdialExpiryIntervalInMin() {
        return ntfConfigManager.getIntValue(OUTDIAL_EXPIRY_INTERVAL_IN_MIN);
    }

    public static int getOutdialExpiryRetries() {
        return ntfConfigManager.getIntValue(OUTDIAL_EXPIRY_RETRIES);
    }

	public static boolean isOutdialReminderEnabled() {
	    return getBooleanUsingNtfConvention(OUTDIAL_REMINDER_ENABLED);
	}

	public static int getOutdialReminderIntervalInMin() {
	    return ntfConfigManager.getIntValue(OUTDIAL_REMINDER_INTERVAL_IN_MIN);
	}

	public static int getOutdialReminderExpireInMin() {
	    return ntfConfigManager.getIntValue(OUTDIAL_REMINDER_EXPIRE_IN_MIN);
	}

    public static String getOutdialReminderType() {
        return ntfConfigManager.getParameter(OUTDIAL_REMINDER_TYPE);
    }

    public static int getOutdialPhoneOnLockFileValidityInSeconds() {
        return ntfConfigManager.getIntValue(OUTDIAL_PHONE_ON_LOCK_FILE_VALIDITY_IN_SECONDS);
    }

	public static int getOutdialStatusFileValidityInMin() {
        return ntfConfigManager.getIntValue(OUTDIAL_STATUS_FILE_VALIDITY_IN_MIN);
	}

	public static boolean getOutdialWhenInWaitState() {
        return getBooleanUsingNtfConvention(OUTDIAL_WHEN_IN_WAIT_STATE);
    }

    public static boolean getOutdialWhenInWaitOnState() {
        return getBooleanUsingNtfConvention(OUTDIAL_WHEN_IN_WAIT_ON_STATE);
    }

    public static int getSlamdownQueueSize() {
		return ntfConfigManager.getIntValue(SLAMDOWN_QUEUE_SIZE);
	}

	public static int getSlamdownWorkers() {
		return ntfConfigManager.getIntValue(SLAMDOWN_WORKERS);
	}

	public static int getSlamdownMcnSenderWorkers() {
	    return ntfConfigManager.getIntValue(SLAMDOWN_MCN_SENDER_WORKERS);
	}

	public static int getSlamdownMcnSenderQueueSize() {
	    return ntfConfigManager.getIntValue(SLAMDOWN_MCN_SENDER_QUEUE_SIZE);
	}

	public static String getSlamdownMcnSmsUnitRetrySchema() {
		return ntfConfigManager.getParameter(SLAMDOWN_MCN_SMS_UNIT_RETRY_SCHEMA);
	}

	public static int getSlamdownMcnSmsUnitExpireTimeInMin() {
		return ntfConfigManager.getIntValue(SLAMDOWN_MCN_SMS_UNIT_EXPIRE_TIME_IN_MIN);
	}

    public static String getSlamdownMcnSmsType0RetrySchema() {
		return ntfConfigManager.getParameter(SLAMDOWN_MCN_SMS_TYPE_0_RETRY_SCHEMA);
	}

	public static int getSlamdownMcnSmsType0ExpireTimeInMin() {
		return ntfConfigManager.getIntValue(SLAMDOWN_MCN_SMS_TYPE_0_EXPIRE_TIME_IN_MIN);
	}

    public static String getSlamdownMcnSmsInfoRetrySchema() {
		return ntfConfigManager.getParameter(SLAMDOWN_MCN_SMS_INFO_RETRY_SCHEMA);
	}

	public static int getSlamdownMcnSmsInfoExpireTimeInMin() {
		return ntfConfigManager.getIntValue(SLAMDOWN_MCN_SMS_INFO_EXPIRE_TIME_IN_MIN);
	}

    public static int getSlamdownMcnExpiryIntervalInMin() {
        return ntfConfigManager.getIntValue(SLAMDOWN_MCN_EXPIRY_INTERVAL_IN_MIN);
    }

    public static int getSlamdownMcnExpiryRetries() {
        return ntfConfigManager.getIntValue(SLAMDOWN_MCN_EXPIRY_RETRIES);
    }

    public static int getSlamdownMcnPhoneOnLockFileValidityInSeconds() {
        return ntfConfigManager.getIntValue(SLAMDOWN_MCN_PHONE_ON_LOCK_FILE_VALIDITY_IN_SECONDS);
    }

    public static int getSlamdownMcnStatusFileValidityInMin() {
	    return ntfConfigManager.getIntValue(SLAMDOWN_MCN_STATUS_FILE_VALIDITY_IN_MIN);
	}

	public static boolean getDoSmsType0Slamdown() {
		return getBooleanUsingNtfConvention(DO_SMS_TYPE_0_SLAMDOWN);
	}

	public static boolean getDoSmsType0Mcn() {
		return getBooleanUsingNtfConvention(DO_SMS_TYPE_0_MCN);
	}

	public static boolean getDoSmsType0Outdial() {
		return getBooleanUsingNtfConvention(DO_SMS_TYPE_0_OUTDIAL);
	}

    public static String getEmailRetrySchema() {
        return ntfConfigManager.getParameter(EMAIL_RETRY_SCHEMA);
    }

    public static long getEmailForwardMaximumSize() {
        return ntfConfigManager.getLongValue(EMAIL_FORWARD_MAXIMUM_SIZE);
    }

	public static boolean isEmailForwardTranscodeAudioOn() {
		return ntfConfigManager.getBooleanValue(EMAIL_FORWARD_TRANSCODE_AUDIO);
	}

	public static boolean isEmailForwardTranscodeVideoOn() {
		return ntfConfigManager.getBooleanValue(EMAIL_FORWARD_TRANSCODE_VIDEO);
	}

    public static String getEmailForwardOutputAudioMimeType() {
        return ntfConfigManager.getParameter(EMAIL_FORWARD_OUTPUT_AUDIO_MIME_TYPE);
    }

    public static String getEmailForwardOutputVideoMimeType() {
        return ntfConfigManager.getParameter(EMAIL_FORWARD_OUTPUT_VIDEO_MIME_TYPE);
    }

	public static boolean getDoSipMwi() {
		return getBooleanUsingNtfConvention(DO_SIP_MWI);
	}

	public static int getSipMwiQueueSize() {
		return ntfConfigManager.getIntValue(SIP_MWI_QUEUE_SIZE);
	}

	public static int getSipMwiWorkers() {
		return ntfConfigManager.getIntValue(SIP_MWI_WORKERS);
	}

	//get the action to take if an HLR lookup fails
	//FIXME need to make this work everywhere, currently the HLR is queried in many places
	//needs to be centralised to accomplish this.
	public static hlrFailAction getHLRRoamingFailureAction() {
	    hlrFailAction result = hlrFailAction.HOME;
	    String hlrFailureAction = ntfConfigManager
	            .getParameter(HLR_ROAM_FAILURE_ACTION);

	    if (hlrFailureAction != null) {
	        hlrFailureAction = hlrFailureAction.toUpperCase();

	        if (hlrFailureAction.equals("RETRY")) {
	            result = hlrFailAction.RETRY;
	        } else if (hlrFailureAction.equals("FAIL")) {
	            result = hlrFailAction.FAIL;
	        } else if (hlrFailureAction.equals("HOME")) {
	            result = hlrFailAction.HOME;;
	        } else if (hlrFailureAction.equals("ROAM")) {
	            result = hlrFailAction.ROAM;
	        } else {
	            legacyLogger.logMessage("Unknown action for " + HLR_ROAM_FAILURE_ACTION + " [" + hlrFailureAction + "] -default to- " + result, Log.WARNING_LEVEL);
	        }

	    }

	    return result;
	}

	public static boolean isFaxEnabled()
	{
	    return getBooleanUsingNtfConvention(FAX_ENABLED);
	}

	public static boolean isFaxSuccessNotification()
	{
	    return ntfConfigManager.getBooleanValue(FAX_SUCCESS_NOTIFICATION);
	}
    public static String getFaxPrintNotifRetrySchema() {
        return ntfConfigManager.getParameter(FAX_PRINT_NOTIFY_RETRY_SCHEMA);
    }

    public static long getFaxprintExpireTimeInMin() {
        return ntfConfigManager.getLongValue(FAX_PRINT_EXPIRE_TIME_IN_MIN);
    }
    public static int getFaxPrintQueueSize() {
        return ntfConfigManager.getIntValue(FAX_PRINT_QUEUE_SIZE);
    }
    public static int getFaxWorkerQueueSize() {
        return ntfConfigManager.getIntValue(FAX_WORKER_QUEUE_SIZE);
    }

    public static int getFaxWorkers() {
        return ntfConfigManager.getIntValue(FAX_WORKERS);
    }

    public static int getFaxPrintMaxConn() {
        return ntfConfigManager.getIntValue(FAX_PRINT_MAX_CONN);
	}

	public static int getImapTimeout() {
		return ntfConfigManager.getIntValue(IMAP_TIMEOUT);
	}

	public static int getInternalQueueSize() {
		return ntfConfigManager.getIntValue(INTERNAL_QUEUE_SIZE);
	}

	public static boolean isKeepSmscConnections() {
		return ntfConfigManager.getBooleanValue(KEEP_SMS_CONNECTIONS);
	}

	public static String getLogicalZone() {
		return ntfConfigManager.getParameter(LOGICAL_ZONE);
	}

	public static int getLogLevel() {
		return Logger.L_DEBUG; //disable in MIO...
	}

	public static int getLogSize() {
		return ntfConfigManager.getIntValue(LOG_SIZE);
	}

	public static int getMaxTimeBeforeExpunge() {
		return ntfConfigManager.getIntValue(MAX_TIME_BEFORE_EXPUNGE);
	}

	public static int getMaxXmpConnections() {
		return ntfConfigManager.getIntValue(MAX_XMP_CONNECTIONS);
	}

	public static String getMmscPassword() {
		return ntfConfigManager.getParameter(MMSC_PASSWORD);
	}

	public static String getMMSVersion() {
		return ntfConfigManager.getParameter(MMS_VERSION);
	}

	public static String getMMSSystemDomain() {
		return ntfConfigManager.getParameter(MMS_SYSTEM_DOMAIN);
	}

    public static String getMmscVasId() {
		return ntfConfigManager.getParameter(MMSC_VAS_ID);
	}

	public static String getMmscVaspId() {
		return ntfConfigManager.getParameter(MMSC_VASP_ID);
	}

	public static int getMMSMaxVideoLength() {
		return ntfConfigManager.getIntValue(MMS_MAX_VIDEO_LENGTH);
	}

	public static int getMMSMaxConnection() {
		return ntfConfigManager.getIntValue(MMS_MAX_CONNECTION);
	}

	public static String getMMSPostMaster() {
		return ntfConfigManager.getParameter(MMS_POST_MASTER);
	}

	public static String getMmscUser() {
		return ntfConfigManager.getParameter(MMS_USER_NAME);
	}

	public static String getMMSPreferedAudioCodec() {
	    return ntfConfigManager.getParameter(MMS_PREFERED_AUDIO_CODEC);
	}

	public static boolean isMwiOffCheckCount() {
		return getBooleanUsingNtfConvention(MWI_OFF_CHECK_COUNT);
	}

    public static boolean getMwiStoreMessageAfterUpdatingIndication() {
        return getBooleanUsingNtfConvention(MWI_STORE_MESSAGE_AFTER_UPDATING);
    }

    public static String[] getMwiServer(String smsc) {
		return getStringArrayLine(ntfConfigManager.getTableParameter(MWI_SERVERS_TABLE, smsc, MWI_SERVERS));
	}

	public static String getNetmask() {
		return ntfConfigManager.getParameter(NETMASK);
	}

	public static int getNotifThreads() {
		return ntfConfigManager.getIntValue(NOTIF_THREADS);
	}

	public static int getNumberOfSms() {
		return ntfConfigManager.getIntValue(NUMBER_OF_SMS);
	}

	public static String getNumberToMessagingSystem() {
		return ntfConfigManager.getParameter(NUMBER_TO_MESSAGING_SYSTEM);
	}

	public static String getNumberToMessagingSystemForCallMwi() {
		return ntfConfigManager
				.getParameter(NUMBER_TO_MESSAGING_SYSTEM_FOR_CALL_MWI);
	}
    public static String getFallbackMms() {
        return ntfConfigManager.getParameter(FALLBACK_MMS);
    }

	public static int getJournalRefreshInterval() {
		return ntfConfigManager.getIntValue(JOURNAL_REFRESH);
	}

	public static int getPagerPauseTime() {
		return ntfConfigManager.getIntValue(PAGER_PAUSE_TIME);
	}

	public static String getPathToSnmpScript() {
		return ntfConfigManager.getParameter(PATH_TO_SNMP_SCRIPTS);
	}

	public static boolean isWarnWhenQuota() {
		String quotaAction = ntfConfigManager.getParameter(QUOTA_ACTION);
		boolean warnWhenQuota = true;

		if (quotaAction != null) {
			quotaAction = quotaAction.trim().toLowerCase();
			if (quotaAction.equals(NOTIFY_AND_WARN)) {
				warnWhenQuota = true;
			} else if (quotaAction.equals(DISCARD)
					|| quotaAction.equals(NOTIFY)) {
				warnWhenQuota = false;
			} else { // if "warn" or illegal value
				warnWhenQuota = true;
			}
		}
		return warnWhenQuota;
	}


	public static boolean isWarnOnlyWhenQuota() {
		String quotaAction = ntfConfigManager.getParameter(QUOTA_ACTION);

		if (quotaAction != null) {
			quotaAction = quotaAction.trim().toLowerCase();
			if (quotaAction.equals(WARN)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDiscardWhenQuota() {
		String quotaAction = ntfConfigManager.getParameter(QUOTA_ACTION);
		boolean discardWhenQuota = true;

		if (quotaAction != null) {
			quotaAction = quotaAction.trim().toLowerCase();
			if (quotaAction.equals(NOTIFY_AND_WARN)
					|| quotaAction.equals(NOTIFY)) {
				discardWhenQuota = false;
			} else if (quotaAction.equals(DISCARD)) {
				discardWhenQuota = true;
			} else { // if "warn" or illegal value
				discardWhenQuota = true;
			}
		}
		return discardWhenQuota;
	}

	public static String getQuotaTemplate() {
		return ntfConfigManager.getParameter(QUOTA_TEMPLATE);
	}
	public static String getQuotaPerTypeTemplate() {
		return ntfConfigManager.getParameter(QUOTA_PER_TYPE_TEMPLATE);
	}
	public static String getHighQuotaTemplate() {
		return ntfConfigManager.getParameter(HIGH_QUOTA_TEMPLATE);
	}
	public static String getHighQuotaPerTypeTemplate() {
		return ntfConfigManager.getParameter(HIGH_QUOTA_PER_TYPE_TEMPLATE);
	}

    public static String getVVMSystemDeactivatedTemplate() {
        return ntfConfigManager.getParameter(VVM_SYSTEM_DEACTIVATED_TEMPLATE);
    }
    
    public static String getVVMImapFirstDetectedTemplate() {
        return ntfConfigManager.getParameter(VVM_IMAP_FIRST_DETECTED_TEMPLATE);
    }

	public static long getShutdownTime() {
	    return ntfConfigManager.getIntValue(SHUTDOWN_TIME); //Maximum time for NTF to gracefully shutdown.
    }

	public static boolean getSendVVMSystemDeactivatedSMS() {
        return ntfConfigManager.getBooleanValue(SEND_VVM_SYSTEM_DEACTIVATED_SMS);
    }
	
    public static boolean getSendVVMImapFirstDetectedSMS() {
        return ntfConfigManager.getBooleanValue(SEND_VVM_IMAP_FIRST_DETECTED_SMS);
    }

	public static String[] getReplaceNotifications() {
		return getStringArray(ntfConfigManager.getList(REPLACE_NOTIFICATIONS_LIST));
	}

	public static boolean isSendUpdateAfterRetrieval() {
		return ntfConfigManager.getBooleanValue(SEND_UPDATE_AFTER_RETRIEVAL);
	}

	public static boolean isSendUpdateAfterTerminalChange() {
		return ntfConfigManager
				.getBooleanValue(SEND_UPDATE_AFTER_TERMINAL_CHANGE);
	}

	public static boolean isSetReplyPath() {
		return ntfConfigManager.getBooleanValue(SET_REPLY_PATH);
	}

	public static int getSlamdownMaxCallers() {
		return ntfConfigManager.getIntValue(SLAMDOWN_MAX_CALLERS);
	}

	public static int getSlamdownMaxCallsPerCaller() {
		return ntfConfigManager.getIntValue(SLAMDOWN_MAX_CALLS_PER_CALLER);
	}

	public static int getSlamdownMaxDigitsInNumber() {
		return ntfConfigManager.getIntValue(SLAMDOWN_MAX_DIGITS_IN_NUMBER);
	}

	public static boolean isSlamdownOldestFirst() {
	    return getBooleanUsingNtfConvention(SLAMDOWN_OLDEST_FIRST);
	}

	public static boolean isSlamdownTimeOfLastCall() {
	    return getBooleanUsingNtfConvention(SLAMDOWN_TIME_OF_LAST_CALL);
	}

	public static String getSlamdownTruncatedNumberIndication() {
		return ntfConfigManager
				.getParameter(SLAMDOWN_TRUNCATED_NUMBER_INDICATION);
	}

	public static boolean isSlamdownMcnNotificationWhenPhoneOnExpiry() {
		return getBooleanUsingNtfConvention(SLAMDOWN_MCN_NOTIFICATION_WHEN_PHONE_ON_EXPIRY);
	}

    /**
     * Returns the retention duration in minutes for slamdown events.
     * @return Retention duration.
     */
    public static int getSlamdownRetentionDuration() {
        return ntfConfigManager.getIntValue(SLAMDOWN_RETENTION_DURATION);
    }

    /**
     * Returns the number of callers retention value for slamdown events.
     * @return Number of events to keep for notification.
     */
    public static int getSlamdownRetentionNbOfCallers() {
        return ntfConfigManager.getIntValue(SLAMDOWN_RETENTION_NB_OF_CALLERS);
    }

    public static int getMcnMaxDigitsInNumber() {
		return ntfConfigManager.getIntValue(MCN_MAX_DIGITS_IN_NUMBER);
	}

	public static boolean isMcnOldestFirst() {
		return getBooleanUsingNtfConvention(MCN_OLDEST_FIRST);
	}

	public static boolean isMcnTimeOfLastCall() {
		return getBooleanUsingNtfConvention(MCN_TIME_OF_LAST_CALL);
	}

	public static String getMcnTruncatedNumberIndication() {
		return ntfConfigManager.getParameter(MCN_TRUNCATED_NUMBER_INDICATION);
	}

	public static int getMcnMaxCallers() {
		return ntfConfigManager.getIntValue(MCN_MAX_CALLERS);
	}

	public static int getMcnMaxCallsPerCaller() {
		return ntfConfigManager.getIntValue(MCN_MAX_CALLS_PER_CALLER);
	}

	public static String getMcnLanguage() {
		return ntfConfigManager.getParameter(MCN_LANGUAGE);
	}

    /**
     * Returns the retention duration in minutes for MCN events.
     * @return Retention duration.
     */
    public static int getMcnRetentionDuration() {
        return ntfConfigManager.getIntValue(MCN_RETENTION_DURATION);
    }

    /**
     * Returns the number of callers retention value for MCN events.
     * @return Number of events to keep for notification.
     */
    public static int getMcnRetentionNbOfCallers() {
        return ntfConfigManager.getIntValue(MCN_RETENTION_NB_OF_CALLERS);
    }

    public static boolean isMcnSubscribedEnabled() {
	    return getBooleanUsingNtfConvention(MCN_SUBSCRIBED_ENABLED);
	}

	/**
	 * Returns the retention duration in minutes for MCN-Subscribed events.
	 * @return Retention duration.
	 */
	public static int getMcnSubsRetentionDuration() {
	    return ntfConfigManager.getIntValue(MCN_SUBSCRIBED_RETENTION_DURATION);
	}

	/**
	 * Returns the number of callers retention value for MCN-Subscribed events.
	 * @return Number of events to keep for notification.
	 */
	public static int getMcnSubsRetentionNbOfCallers() {
	    return ntfConfigManager.getIntValue(MCN_SUBSCRIBED_RETENTION_NB_OF_CALLERS);
	}

	public static String getSmeServiceType() {
		return ntfConfigManager.getParameter(SME_SERVICE_TYPE);
	}

	public static String getVvmServiceType() {
        return ntfConfigManager.getParameter(VVM_SERVICE_TYPE);
	}

	public static String getSmeServiceTypeForMwi() {
		String smeServiceTypeForMwi = ntfConfigManager.getParameter(SME_SERVICE_TYPE_FOR_MWI);
		if (smeServiceTypeForMwi == null || smeServiceTypeForMwi.length() == 0) {
			smeServiceTypeForMwi = getSmeServiceType();
		}
		return smeServiceTypeForMwi;
	}

	public static SMSAddress getSmeSourceAddress() {
		String smeSourceAddress = ntfConfigManager.getParameter(SME_SOURCE_ADDRESS);
		return getAddress(smeSourceAddress);
	}

	public static SMSAddress getSourceAddress(String name) {
	    if (name.equals(SME_SOURCE_ADDRESS))
	        {return getSmeSourceAddress();}
	    
		return getSourceAddress(name, null);
	}

	public static SMSAddress getSourceAddress(String name, String cosName) {
		
	    SMSAddress result;
	    
		String paramName = SOURCE_ADDRESS_DEBUT;

		if ((cosName != null) && (name != null)) {
			paramName = paramName + name + "_" + cosName;
		} else if (name != null) {
			paramName = paramName + name;
		}
		
		result = sourceAddressCache .get(paramName);
		if (result != null) {
		    return result;
		}
		
		result = getSmeSourceAddress();

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
		
		if (result != null) {
		    sourceAddressCache.put(paramName,result);
		}

		return result;
	}

	public static int getSmeSourceNpi() {
		return ntfConfigManager.getIntValue(SME_SOURCE_NPI);
	}

	public static int getSmeSourceTon() {
		return ntfConfigManager.getIntValue(SME_SOURCE_TON);
	}

	public static int getSmppVersion() {
		return ntfConfigManager.getIntValue(SMPP_VERSION);
	}

    public static boolean isSmsClientAsynchronous() {
        return ntfConfigManager.getBooleanValue(SMS_CLIENT_ASYNCHRONOUS);
    }

	public static int[] getSmppErrorCodesIgnored() {
		return getIntArray(ntfConfigManager.getList(SMPP_ERROR_CODES_IGNORED_LIST));
	}

	public static String getSmscErrorAction() {
		return ntfConfigManager.getParameter(SMCS_ERROR_ACTION);
	}

	public static int getSmscPollInterval() {
		return ntfConfigManager.getIntValue(SMSC_POLL_INTERVAL);
	}

	public static int getSmscTimeout() {
		return ntfConfigManager.getIntValue(SMSC_TIMEOUT);
	}

    public static int getSmscTimeoutSubmitSm() {
        return ntfConfigManager.getIntValue(SMSC_TIMEOUT_SUBMIT_SM);
    }

    public static int getSmscTimeoutDataSm() {
        return ntfConfigManager.getIntValue(SMSC_TIMEOUT_DATA_SM);
    }

    public static int getSmscShutdownPeriod() {

        return ntfConfigManager.getIntValue(SMSC_SHUTDOWN_PERIOD);
    }

    public static String getSmppBinding() {
        return ntfConfigManager.getParameter(SMPP_BINDING);
    }

    public static int getSmsMinConn() {
        return ntfConfigManager.getIntValue(SMS_MIN_CONN);
    }

	public static int getSmsMaxConn() {
		return ntfConfigManager.getIntValue(SMS_MAX_CONN);
	}

    public static int getSmsNumReceiverConn() {
        return ntfConfigManager.getIntValue(SMS_NUM_RECEIVER_CONN);
    }

    public static int getSmsMinTimeBetweenConnections() {
        return ntfConfigManager.getIntValue(SMS_MIN_TIME_BETWEEN_CONN);
    }

    public static int getSmsMinTimeBetweenReconnects() {
        return ntfConfigManager.getIntValue(SMS_MIN_TIME_BETWEEN_RECONN);
    }

	public static long getMmsMaxMsgSize() {
		return ntfConfigManager.getLongValue(MMS_MAX_MSG_SIZE);
	}

    public static int getSlamdownConn() {
		return Math.max((ntfConfigManager.getIntValue(SMS_MAX_CONN) / 2), 1);
	}

	public static int getNumberingPlanIndicator() {
		return ntfConfigManager.getIntValue(SMS_NUMBERING_PLAN_INDICATOR);
	}

	public static int getSmsStringLength() {
		return ntfConfigManager.getIntValue(SMS_STRING_LENGTH);
	}

	public static int getTypeOfNumber() {
		return ntfConfigManager.getIntValue(SMS_TYPE_OF_NUMBER);
	}

	public static String getSmscBackup(String smscName) {
		return ntfConfigManager.getTableParameter(SMSC_BACKUP_TABLE, smscName, SMSC_BACKUP_VALUE);
	}

	public static int getSmsPriority() {
		return ntfConfigManager.getIntValue(SMS_PRIORITY);
	}

	public static int getSmsQueueSize() {
		return ntfConfigManager.getIntValue(SMS_QUEUE_SIZE);
	}

	public static boolean isSmsHandlerLoadBalanced() {
		return ntfConfigManager.getBooleanValue(SMS_HANDLER_LOAD_BALANCING);
	}
	
	public static boolean denormalizeFromTag() {
		return ntfConfigManager.getBooleanValue(FROM_TAG_DENORMALIZE_NUMBER);
	}

    public static boolean isSmsReminderEnabled() {
        return getBooleanUsingNtfConvention(SMS_REMINDER_ENABLED);
    }

    public static int getSmsReminderIntervalInMin() {
        return ntfConfigManager.getIntValue(SMS_REMINDER_INTERVAL_IN_MIN);
    }

    public static int getSmsReminderExpireInMin() {
        return ntfConfigManager.getIntValue(SMS_REMINDER_EXPIRE_IN_MIN);
    }

    public static String getSmsReminderContent() {
        return ntfConfigManager.getParameter(SMS_REMINDER_CONTENT);
    }

    public static String getSmsReminderAllowedType() {
        return ntfConfigManager.getParameter(SMS_REMINDER_ALLOWED_TYPE);
    }

    public static boolean getSmsReminderIgnoreFilters() {
        return getBooleanUsingNtfConvention(SMS_REMINDER_IGNORE_FILTERS);
    }

    /*
     * Indicates should send an SMS instead of a FLS reminder when FLS
     * disabled by filter or MoipUserNtd, when configured to send just a FLS.
     */
    public static boolean getSmsReminderTrySmsOnFLSDisabled() {
        return ntfConfigManager.getBooleanValue(SMS_REMINDER_TRY_SMS_ON_FLS_DISABLED);
    }

    /*
     * Indicates what type of MoipUserNTD (disabling) to respect for a reminder
     * or none.
     */
    public static String getReminderUseNtdType() {
        return ntfConfigManager.getParameter(REMINDER_USE_NTD_TYPE);
    }

    public static String getFlsReminderContent() {
        return ntfConfigManager.getParameter(FLS_REMINDER_CONTENT);
    }

    public static int getSnmpAgentPort() {
		return ntfConfigManager.getIntValue(SNMP_AGENT_PORT);
	}

	public static int getSnmpAgentTimeout() {
		return ntfConfigManager.getIntValue(SNMP_AGENT_TIMEOUT);
	}

	public static InetAddress getSnmpAgentAddress() {
		InetAddress snmpAgentAddress = null;
		try {
			snmpAgentAddress = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			// do nothing; it should not happen
		}
		String snmpAgentIp = ntfConfigManager.getParameter(SNMP_AGENT_ADDRESS);
		if (snmpAgentIp != null && snmpAgentIp.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
			try {
				snmpAgentAddress = InetAddress.getByName(snmpAgentIp);
			} catch (Exception e) {
				legacyLogger.logMessage(
						"Bad format in parameter \"snmpagentaddress="
								+ snmpAgentIp + "\": " + e.getMessage());
			}
		}
		return snmpAgentAddress;
	}

	public static boolean isSplitMwiAndSms() {
		return ntfConfigManager.getBooleanValue(SPLIT_MWI_AND_SMS);
	}

	public static int getUnreadMessageReminderInterval() {
		return ntfConfigManager.getIntValue(UNREAD_MESSAGE_REMINDER_INTERVAL);
	}

	public static int getUnreadMessageReminderMaxTimes() {
		return ntfConfigManager.getIntValue(UNREAD_MESSAGE_REMINDER_MAX_TIMES);
    }

    /**
     * Set Unread Message Reminder config by special parsing of config value.
     */

	public static boolean isUnreadMessageReminder() {
		boolean unreadMessageReminder = false;
		String unreadMessageReminderType = ntfConfigManager
				.getParameter(UNREAD_MESSAGE_REMINDER_TYPE);
		if (unreadMessageReminderType.equals(SMS)
				|| (unreadMessageReminderType.equals(FLASH))) {
			unreadMessageReminder = true;
		}

		return unreadMessageReminder;
	}

	public static boolean isUnreadMessageReminderFlash() {
		boolean unreadMessageReminderFlash = false;
		String unreadMessageReminderType = ntfConfigManager
				.getParameter(UNREAD_MESSAGE_REMINDER_TYPE);
		if ((unreadMessageReminderType.equals(FLASH))) {
            unreadMessageReminderFlash = true;
		}

		return unreadMessageReminderFlash;
	}

    public static boolean isAlternativeFlashDcs() {
		return ntfConfigManager.getBooleanValue(USE_ALTERNATIVE_FLASH_DCS);
	}

	public static boolean shouldUseMMSPostmaster() {
		return ntfConfigManager.getBooleanValue(USE_MMS_POST_MASTER);
	}

	public static boolean shouldUseSmil() {
		return ntfConfigManager.getBooleanValue(USE_SMIL);
	}

	public static boolean shouldUseCallerInEventDescription() {
		return ntfConfigManager.getBooleanValue(USE_CALLER_IN_EVENT_DESCRIPTION);

	}

    public static String getWapPushPasswd() {
		return ntfConfigManager.getParameter(WAP_PUSH_PASSWD);
	}

    public static String getDefaultFromAddress() {
        return ntfConfigManager.getParameter(EMAIL_FROM_DEFAULT);
    }
    
	public static String getWapPushRetrievalHost() {
		return ntfConfigManager.getParameter(WAP_PUSH_RETIEVAL_HOST);
	}

	public static String getWapPushUrlSuffix() {
		return ntfConfigManager.getParameter(WAP_PUSH_URL_SUFFIX);
	}

	public static String getWapPushUserName() {
		return ntfConfigManager.getParameter(WAP_PUSH_USER_NAME);
	}

    public static int getXmpPollInterval() {
		return ntfConfigManager.getIntValue(XMP_POLL_INTERVAL);
	}

	public static int getXmpTimeout() {
		return ntfConfigManager.getIntValue(XMP_TIMEOUT);
	}

	public static int getXmpValidity() {
		return ntfConfigManager.getIntValue(XMP_VALIDITY);
	}

	public static int getXmpRefreshTime() {
		return ntfConfigManager.getIntValue(XMP_REFRESH_TIME);
	}

    public static String getNtfEventsRootPath() {
        return ntfConfigManager.getParameter(NTF_EVENTS_ROOT_PATH);
    }

    public static String getNotifRetrySchema() {
        return ntfConfigManager.getParameter(NOTIFY_RETRY_SCHEMA);
    }

    public static long getNotifExpireTimeInMin() {
        return ntfConfigManager.getLongValue(NOTIFY_EXPIRE_TIME_IN_MIN);
    }

    public static String getSipMwiNotifRetrySchema() {
		return ntfConfigManager.getParameter(SIP_MWI_NOTIFY_RETRY_SCHEMA);
	}

	public static long getSipMwiNotifExpireTimeInMin() {
		return ntfConfigManager.getLongValue(SIP_MWI_EXPIRE_TIME_IN_MIN);
	}

	public static int getSipMwiExpiryIntervalInMin() {
        return ntfConfigManager.getIntValue(SIP_MWI_EXPIRY_INTERVAL_IN_MIN);
    }

    public static int getSipMwiExpiryRetries() {
        return ntfConfigManager.getIntValue(SIP_MWI_EXPIRY_RETRIES);
    }

    public static int getServiceListenerCorePoolSize() {
        return ntfConfigManager.getIntValue(SERVICE_LISTENER_CORE_POOL_SIZE);
    }

    public static int getServiceListenerMaxPoolSize() {
        return ntfConfigManager.getIntValue(SERVICE_LISTENER_MAX_POOL_SIZE);
    }

    public static int getLoginFileValidityPeriod() {
		return ntfConfigManager.getIntValue(LOGIN_FILE_VALIDITY_PERIOD_IN_MIN);
	}

    public static int getValidity_flash() {
    	return getValidity(FLASH);
	}

	public static int getValidity_smsType0() {
		int result = getValidity(SMS_TYPE_0);
		if (result == -1) {
			result = DEFAULT_VALIDITY_SMS_TYPE_0_VALUE;
		}

		return result;
	}

	public static int getValidity_mwiOn() {
		return getValidity(MWI_ON);
	}

	public static int getValidity_mwiOff() {
		return getValidity(MWI_OFF);
	}

	public static int getValidity_mailQuotaExceeded() {
		return getValidity(MAIL_QUOTA_EXCEEDED);
	}
	public static int getValidity_mailQuotaHighLevelExceeded() {
		return getValidity(MAIL_QUOTA_HIGH_LEVEL_EXCEEDED);
	}



	public static int getValidity_temporaryGreetingOnReminder() {
		return getValidity(TEMPORARY_GREETING_ON_REMINDER);
	}

	public static int getValidity_voicemailOffReminder() {
		return getValidity(VOICEMAIL_OFF_REMINDER);
	}

	public static int getValidity_cfuOnReminder() {
		return getValidity(CFU_ON_REMINDER);
	}

	public static int getValidity_slamdown() {
		return getValidity(SLAMDOWN);
	}

	public static int getValidity_mcn() {
		return getValidity(MCN);
	}

    public static int getValidity_vvmSystemDeactivated() {
        return getValidity(VVM_SYSTEM_DEACTIVATED);
    }

	public static int getValidity(String name) {

		int result = DEFAULT_VALIDITY_VALUE;

		Map<String, String> validityMapForName = ntfConfigManager.getTable(
				VALIDITY_TABLE).get(VALIDITY_DEBUT + name);
		if (validityMapForName != null) {
			String validityValue = validityMapForName.get(VALIDITY_VALUE);
			try {
				result = Integer.parseInt(validityValue);
			} catch (NumberFormatException nfex) {
				legacyLogger.logMessage(
						"NumberFormatException : " + nfex.getMessage(),
						Logger.L_VERBOSE);
			}
		}

		return result;
	}

    public static boolean isDefaultUserHasMwi() {
		return ntfConfigManager.getBooleanValue(DEFAULT_TERMINAL_CAPABILITY_FOR_MWI);
	}

	public static boolean isDefaultUserHasFlash() {
		return ntfConfigManager.getBooleanValue(DEFAULT_TERMINAL_CAPABILITY_FOR_FLASH);
	}

	public static boolean isDefaultUserHasReplace() {
		return ntfConfigManager.getBooleanValue(DEFAULT_TERMINAL_CAPABILITY_FOR_REPLACE);
	}

	public static int getVvmQueueSize() {
	    return ntfConfigManager.getIntValue(VVM_QUEUE_SIZE);
	}

	public static int getVvmWorkers() {
	    return ntfConfigManager.getIntValue(VVM_WORKERS);
	}

	public static int getVvmSourcePort() {
	    return ntfConfigManager.getIntValue(VVM_SOURCE_PORT);
	}

	public static int getVvmDestinationPort() {
	    return ntfConfigManager.getIntValue(VVM_DESTINATION_PORT);
	}

    public static String getVvmSmsUnitRetrySchema() {
        return ntfConfigManager.getParameter(VVM_SMS_UNIT_RETRY_SCHEMA);
    }

    public static int getVvmSmsUnitExpireTimeInMin() {
        return ntfConfigManager.getIntValue(VVM_SMS_UNIT_EXPIRE_TIME_IN_MIN);
    }

	public static int getVvmStatusFileValidityInMin() {
        return ntfConfigManager.getIntValue(VVM_STATUS_FILE_VALIDITY_IN_MIN);
    }

	public static int getValidity_vvm() {
	    return getValidity(VVM);
	}

    public static int getValidity_autoUnlockPin() {
        return getValidity(AUTO_UNLOCK_PIN);
    }

	public static String getPhoneOnMethod(){
		return ntfConfigManager.getParameter(PHONE_ON_METHOD);
	}
      
  	public static boolean getSendDetachOnAssumedUnavailable(){
        return ntfConfigManager.getBooleanValue(SEND_DETACH_ON_ASSUMED_UNAVAILABLE);
        }

	public static boolean getCheckRoaming(){
		return ntfConfigManager.getBooleanValue(CHECK_ROAMING);
	}

    /*
     * Fetch if operator allows fall back when roaming/
     */
    public static boolean getFallbackWhenRoaming() {
        return ntfConfigManager.getBooleanValue(FALLBACK_WHEN_ROAMING);
    }

    /*
     * Fetch if should respect the MOIP_USER_NTD when doing fall back.
     */
    public static boolean getFallbackUseMoipUserNtd() {
        return ntfConfigManager.getBooleanValue(FALLBACK_USE_MOIP_USER_NTD);
    }

    /* Indicates that should send a FLASH SMS fall-back only if message is urgent.
     *
     */
    public static boolean getFallbackFlashUrgentOnly() {
        return ntfConfigManager.getBooleanValue(FALLBACK_FLS_URGENT_ONLY);
    }

    /* Indicates that should send an SMS if already sent a flash message when
     * Urgent message i.e. both SMS and FLS should not be sent when urgent.
     */
    public static boolean getFallbackSmsOnUrgentIfFlsSent() {
        return ntfConfigManager.getBooleanValue(FALLBACK_SMS_ON_URGENT_IF_FLS_SENT);
    }
    
    /* 
     * Indicates that an SMS MWI is interpreted as a type of SMS when falling back to SMS.
     * So if an SMS MWI has been sent but no MWI, then SMS fall-back will be dropped.
     */
    public static boolean getFallbackSmsWhenMwiSmsOnly() {
        return ntfConfigManager.getBooleanValue(FALLBACK_SMS_WHEN_MWI_SMS_ONLY);
    }
    
    

	public static boolean getCheckBusy(){
		return ntfConfigManager.getBooleanValue(CHECK_BUSY);
	}

	public static boolean getSubscriberChargingModel(){
		return ntfConfigManager.getBooleanValue(GET_SUBSCRIBER_CHARGING_MODEL);
	}

	public static String getRoamingTemplatePosition(){
		return ntfConfigManager.getParameter(ROAMING_TEMPLATE_POSITION);
	}

	/*
	 * RoamingTemplate indicates where the roaming template message should be placed in
	 * an SMS notification when roaming.
	 * Can be set to none (don't use).
	 *
	 * The following three functions indicate where to appendthe content/phrase from
	 * .cphr to the  SMS notification (c,s,h)
	 * */

	public static boolean isBeginRoamingTemplatePosition(){
		return (ROAMING_TEMPLATE_POSITION_BEGIN.equals(ntfConfigManager.getParameter(ROAMING_TEMPLATE_POSITION)));
	}

	public static boolean isEndRoamingTemplatePosition(){
		return (ROAMING_TEMPLATE_POSITION_END.equals(ntfConfigManager.getParameter(ROAMING_TEMPLATE_POSITION)));
	}

	public static boolean isRoamingTemplateUsed(){
	    return (!ROAMING_TEMPLATE_POSITION_NONE.equals(ntfConfigManager.getParameter(ROAMING_TEMPLATE_POSITION)));
	}

	public static Map<String, Map<String,String>> getExternalEnablers(String serviceName) {
	    return ntfConfigManager.getTable(serviceName);
	}

	public static Map<String,String> getExternalEnabler(String serviceName, String componentName) {
	    Map<String,String> externalEnabler = null;
	    try {
	        externalEnabler = ntfConfigManager.getTable(serviceName).get(componentName);
	    } catch (NullPointerException npe) {
	        ;
	    }
	    return externalEnabler;
	}

	public static String getComponentParameter(String serviceName, String componentName, String componentParameter) {
	    String param = null;
	    try {
	        param = ntfConfigManager.getTable(serviceName).get(componentName).get(componentParameter);
	    } catch (NullPointerException npe) {
	        ;
	    }
	    return param;
	}

	public static String getInstanceComponentName() {
		return ntfConfigManager.getParameter(COMPONENT_INSTANCE_NAME);
	}

	public static String getFallbackSms() {
		return ntfConfigManager.getParameter(FALLBACK_SMS);
	}

	public static String getFallbackSipMwi() {
		return ntfConfigManager.getParameter(FALLBACK_SIPMWI);
	}

	public static String getFallbackOutdial() {
		return ntfConfigManager.getParameter(FALLBACK_OUTDIAL);
	}
	public static String getFallbackVvm() {
        return ntfConfigManager.getParameter(FALLBACK_VVM);
    }

	public static int getFallbackWorkers() {
		return ntfConfigManager.getIntValue(FALLBACK_WORKERS);
	}

	public static int getFallbackQueueSize() {
		return ntfConfigManager.getIntValue(FALLBACK_QUEUE_SIZE);
	}

	public static String getFallbackRetrySchema() {
		return ntfConfigManager.getParameter(FALLBACK_RETRY_SCHEMA);
	}

	public static int getFallbackExpireTimeInMin() {
		return ntfConfigManager.getIntValue(FALLBACK_EXPIRE_TIME_IN_MIN);
	}

	public static String getFallbackOutdialToSmsContent() {
	    return ntfConfigManager.getParameter(FALLBACK_OUTDIAL_TO_SMS_CONTENT);
	}

	public static String getFallbackOutdialToFlsContent() {
	    return ntfConfigManager.getParameter(FALLBACK_OUTDIAL_TO_FLS_CONTENT);
	}

	public static String getFallbackSipMwiToSmsContent() {
	    return ntfConfigManager.getParameter(FALLBACK_SIPMWI_TO_SMS_CONTENT);
	}

	public static String getFallbackVvmToSmsContent() {
	    return ntfConfigManager.getParameter(FALLBACK_VVM_TO_SMS_CONTENT);
	}

	public static int getSipMwiOkXmpCode() {
		return ntfConfigManager.getIntValue(SIP_MWI_OK_XMP_CODE);
	}

	public static int getSipMwiRetryXmpCode() {
		return ntfConfigManager.getIntValue(SIP_MWI_RETRY_XMP_CODE);
	}

	public static int getSipMwiNotSubscribedXmpCode() {
		return ntfConfigManager.getIntValue(SIP_MWI_NOT_SUBSCRIBED_XMP_CODE);
	}

    public static int getSipMwiReminderIntervalInMin() {
        return ntfConfigManager.getIntValue(SIP_MWI_REMINDER_INTERVAL_IN_MIN);
    }

    public static int getSipMwiReminderExpireInMin() {
        return ntfConfigManager.getIntValue(SIP_MWI_REMINDER_EXPIRE_IN_MIN);
    }

    public static boolean isSipMwiReminderEnabled() {
        return getBooleanUsingNtfConvention(SIP_MWI_REMINDER_ENABLED);
    }

    public static int getVvmPhoneOnLockFileValidityInSeconds() {
        return ntfConfigManager.getIntValue(VVM_PHONE_ON_LOCK_FILE_VALIDITY_IN_SECONDS);
    }

    public static String getVvmSimSwapSendingUnitPhoneOnRetrySchema() {
	    return ntfConfigManager.getParameter(SIM_SWAP_SENDING_UNIT_PHONE_ON_RETRY_SCHEMA);
	}

    public static int getVvmSimSwapSendingUnitPhoneOnExpireTimeInMin() {
	    return ntfConfigManager.getIntValue(SIM_SWAP_SENDING_UNIT_PHONE_ON_EXPIRE_TIME_IN_MIN);
	}

    public static String getVvmSimSwapWaitingPhoneOnRetrySchema() {
        return ntfConfigManager.getParameter(SIM_SWAP_WAITING_PHONE_ON_RETRY_SCHEMA);
    }

    public static int getVvmSimSwapWaitingPhoneOnExpireTimeInMin() {
        return ntfConfigManager.getIntValue(SIM_SWAP_WAITING_PHONE_ON_EXPIRE_TIME_IN_MIN);
    }

    public static String getSimSwapTimeout() {
        return ntfConfigManager.getParameter(SIM_SWAP_TIMEOUT);
    }

    public static int getSimSwapTimeoutExpireTimeInMin() {
        return ntfConfigManager.getIntValue(SIM_SWAP_TIMEOUT_EXPIRE_TIME_IN_MIN);
     }

    public static String getAppleImapServerAddress(){
 		return ntfConfigManager.getParameter(APPLE_IMAP_SERVER_ADDRESS);
    }
    /**
     * @deprecated use Config.getPhoneOnMethod() instead
     */
    public static String getSimSwapPhoneOnMode() {
        return ntfConfigManager.getParameter(SIM_SWAP_PHONE_MODE); // simSwapPhoneOnMode
    }

    public static String getAppleImapServerPort(){
    	return ntfConfigManager.getParameter(APPLE_IMAP_SERVER_PORT);
    }

    public static int getVvmExpiryIntervalInMin() {
        return ntfConfigManager.getIntValue(VVM_EXPIRY_INTERVAL_IN_MIN);
    }

    public static int getVvmExpiryRetries() {
        return ntfConfigManager.getIntValue(VVM_EXPIRY_RETRIES);
    }

    public static int getAlertSCRegistrationNumOfRetry() {
        int result = ntfConfigManager.getIntValue(ALERT_SC_REGISTRATION_NUMBER_OF_RETRY);
        if(result==-1)
        {
            result= ALERT_SC_REGISTRATION_NUMBER_OF_RETRY_DEFAULT_VALUE;
        }

        return result;
    }
    public static int getAlertSCRegistrationTimeInSecBetweenRetry() {

        int result = ntfConfigManager.getIntValue(ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY);
        if(result==-1)
        {
            result= ALERT_SC_REGISTRATION_SEC_BETWEEN_RETRY_DEFAULT_VALUE;
        }
        return result;
    }

    public static String getSmsType0MessageState(String type){
        return ntfConfigManager.getParameter(SMS_TYPE_0_MESSAGE_STATE + type);
    }

     public static String getMimeTextCharSet(String preferredLanguage) {
        String result = ntfConfigManager.getParameter(DEFAULT_MIME_TEXT_CHARSET);
        Map<String, String> charSetMapForLanguage = ntfConfigManager.getTable(
                LANG_TO_MIME_TEXT_CHARSET_TABLE).get(preferredLanguage);
        if (charSetMapForLanguage != null) {
            String charSetValue = charSetMapForLanguage.get(LANG_TO_MIME_TEXT_CHARSET_VAL);
            if (charSetValue != null)
                {result = charSetValue;}
        }
        return result;
    }


    /**
     * Inject mock ConfigManager for testing purposes only.
     * @param cfgManager mock ConfigManager
     */
    public static void injectNtfConfigManager(ConfigManager cfgManager){
        ntfConfigManager = cfgManager;
    }

    /**
     * Returns the characterset encoding, if used.
     */
    public static String getCharsetEncoding() {
        return ntfConfigManager.getParameter(CHARSET_ENCODING);
    }

    /**
     * Returns the smpp error code which will redirect error handling to whatever the network error
     * code is. This returns the error code with the preceding "0x".
     * @return
     */
    public static String getLookupNetworkErrorCodeWhenCommandStatusIs()
    {
        return ntfConfigManager.getParameter(LOOKUP_NETWORK_ERROR_CODE_WHEN_COMMAND_STATUS_IS);
    }


	/** MAIN PROGRAM USED FOR TESTING **/
	public static void main(String args[]) {
		try {
			Config.loadCfg();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    /**
     * fetch the notifier plug-in classes.
     * @return a hashMap where the key is the name of the plug-in and the value is the class name, the hash is empty if no items
     */
    public static HashMap<String,String> getNotifierPlugins() {

        HashMap<String,String> result = new HashMap<String,String>();

        Map<String, Map<String, String>> NotifPluginMap = ntfConfigManager.getTable(NOTIFIER_PLUGIN_TABLE);
        if (NotifPluginMap.isEmpty()) {
            return result; //empty
        } else {
            Iterator<String> it = NotifPluginMap.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                //this creates a map with the name of the plug-in as key and the class as the value.
                result.put(key,NotifPluginMap.get(key).get(NOTIFIER_PLUGIN_CLASS));
                //legacyLogger.logMessage("NTF " + tableName + " contains the element " + key + " with the value "  + sourceAddressMap.get(key).get(tableValue) + ".", Logger.L_VERBOSE);
            }
        }
        return result;
    }
    
    public static int getFsTimeout() {
        return ntfConfigManager.getIntValue(FS_TIMEOUT);
    }

    public static String getDelayedEventRetrySchema() {
        return ntfConfigManager.getParameter(DELAYED_EVENT_RETRY_SCHEMA);
    }

    public static Long getDelayedEventRetryExpireTime() {
        return ntfConfigManager.getLongValue(DELAYED_EVENT_RETRY_EXPIRY);
    }

    public static int getDelayedEventQueueSize() {
        return ntfConfigManager.getIntValue(DELAYED_EVENT_QUEUE_SIZE);
    }

    public static int getDelayedEventWorkers() {
        return ntfConfigManager.getIntValue(DELAYED_EVENT_WORKERS);
    }
    
    
    //Special debug for lab use.
    
    public static boolean getWriteMMSToTempFolder() {
        //This will output a copy of sent MMS messages to TEMP_DIRECTORY
        return ntfConfigManager.getBooleanValue(WRITE_MMS_DEBUG_TO_TEMP_FOLDER);
    }
    
    public static String getTempDir() {

        String tempDirectory = ntfConfigManager.getParameter(TEMP_DIRECTORY);
        if (tempDirectory != null &&  !tempDirectory.isEmpty()) {
            if (tempDirectory.endsWith(File.separator) && tempDirectory.length() > 1) {
                tempDirectory = tempDirectory.substring(tempDirectory.length()-1); 
                if (new File(tempDirectory).exists()) {
                    return tempDirectory;
                }

            }
        }       
        //default..
        return "/opt/moip/logs/ntf/";
    }
    
    public static boolean isCancelSmsDebug() {
        return ntfConfigManager.getBooleanValue(CANCEL_SMS_AT_DEBUG);
    }
        
}
