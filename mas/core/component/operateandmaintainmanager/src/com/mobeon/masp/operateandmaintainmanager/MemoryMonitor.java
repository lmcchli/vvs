/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2020.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */
package com.mobeon.masp.operateandmaintainmanager;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import com.abcxyz.messaging.oe.common.perfmgt.PerformanceDataGenerationException;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceManagerExt;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.operateandmaintainmanager.util.MemUsageGenerator;

/**
 * Class that monitors memory usage of MAS/MOIP-tn
 * 
 * If the threshold gets above restartThreshold then a shutdown will be initiated during specified low
 * Traffic Window
 * 
 * If The Threshold gets above criticalrestartThreshold shutdown will immediately be initiated.
 * 
 * @author lmcmajo
 *
 */
public class MemoryMonitor {

	//Constants
	public static final String maintMemoryShutdownMonitorEnabledName ="maintMemoryShutdownMonitorEnabled";
	public static final String maintMemoryShutdownThresholdName="maintMemoryShutdownThreshold";
	public static final String criticalMemoryShutdownThresholdName = "criticalMemoryShutdownThreshold";
	public static final String maintShutdownHourName = "maintShutdownHour";
	public static final boolean maintMemoryShutdownMonitorEnabledDefault=false;
	public static final int maintMemoryShutdownThresholdDefault=85; //percent
	public static final int criticalMemoryShutdownThresholdDefault=95; //percent
	public static final int maintShutdownHourDefault=2; //2 am local time

	protected static ILogger logger = ILoggerFactory.getILogger(MemoryMonitor.class);
	protected static OperateMAS operateMAS;
	protected static IConfigurationManager configManager;

	//The timer and timerTask to do the actual work.
	private Timer memoryCheckPeriodicTimer = null;
	private CheckMemory checkMemTask = null;

	//How often to CHECK - periodic timer in MS.
	private static long CHECK_PERIOD_MS=60000; 

	private class CheckMemory extends TimerTask {
		//default values reflect alarm thresholds for minor and critical alarms (NPC)
		private boolean maintMemoryShutdownMonitorEnabled=maintMemoryShutdownMonitorEnabledDefault;
		private int maintMemoryShutdownThreshold=maintMemoryShutdownThresholdDefault; 
		private int criticalMemoryShutdownThreshold=criticalMemoryShutdownThresholdDefault;
		private int maintShutdownHour=maintShutdownHourDefault; 

		//The instance of the memUsage Generator that gets the percent memory used from the system
		//NOTE this is not the java heap memory but rather the total system memory used.
		private MemUsageGenerator memUsage = new  MemUsageGenerator();
		
		//Timers and related for triggering a shutdown
		private GregorianCalendar lowThresholdTriggerDate = null;
		private String lowThresholdTriggerDateString = "UNKNOWN";
		private Timer initiateShutdownAtLowTraffic = null;
		private GregorianCalendar initiateShutdownSoonDate;
		private Timer initiateShutdownSoon = null;

		private CheckMemory() {
			readConfig(false);
			memUsage.init((PerformanceManagerExt) CommonMessagingAccess.getInstance().getOamManager().getPerformanceManager());
		}

		private void readConfig(boolean refresh) {

			if (refresh) {
				try {
					configManager.reload();
				} catch (ConfigurationException e) {
					//ignore
				}
			} else {
				return;
			}
			//range checking 0 or 100 means off..
			//refresh every check so can change it.
			boolean oldmaintMemoryShutdownMonitorEnabled=maintMemoryShutdownMonitorEnabled;				
			int oldMaintMemoryShutdownThreshold=maintMemoryShutdownThreshold;
			int oldCriticalMemoryShutdownThreshold=criticalMemoryShutdownThreshold;
			int oldMaintShutdownHour=maintShutdownHour;

			try {
				maintMemoryShutdownMonitorEnabled = configManager.getConfiguration().getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getBoolean(maintMemoryShutdownMonitorEnabledName);
			} catch (Exception e) {
				logger.error("Unable to read parameter " + maintMemoryShutdownMonitorEnabledName, e);
				maintMemoryShutdownMonitorEnabled=maintMemoryShutdownMonitorEnabledDefault;
			}

			try {
				maintMemoryShutdownThreshold = configManager.getConfiguration().getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(maintMemoryShutdownThresholdName);
				if (maintMemoryShutdownThreshold < 0 || maintMemoryShutdownThreshold > 100) {
					logger.error("bad value for " + maintMemoryShutdownThresholdName + " " + maintMemoryShutdownThreshold + "reseting to default");
					maintMemoryShutdownThreshold=maintMemoryShutdownThresholdDefault;
				}
			} catch (Exception e) {
				logger.error("Unable to read parameter " + maintMemoryShutdownThresholdName, e);
				maintMemoryShutdownThreshold=maintMemoryShutdownThresholdDefault;
			}

			try {
				criticalMemoryShutdownThreshold = configManager.getConfiguration().getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(criticalMemoryShutdownThresholdName);
				if (criticalMemoryShutdownThreshold < 0 || criticalMemoryShutdownThreshold > 100) {
					logger.error("bad value for " + criticalMemoryShutdownThresholdName + " " + criticalMemoryShutdownThreshold + "reseting to default");
					criticalMemoryShutdownThreshold=criticalMemoryShutdownThresholdDefault;
				}
			} catch (Exception e) {
				logger.error("Unable to read parameter " + criticalMemoryShutdownThresholdName , e);
				criticalMemoryShutdownThreshold=criticalMemoryShutdownThresholdDefault;
			}

			try {
				maintShutdownHour = configManager.getConfiguration().getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(maintShutdownHourName);
				if (maintShutdownHour < 0 || maintShutdownHour > 23 ) 
				{
					logger.error("bad value for " + maintShutdownHourName + " " + maintShutdownHour + "reseting to default");
				}
			} catch (Exception e) {
				logger.error("Unable to read parameter " + maintShutdownHourName , e);
				maintShutdownHour=maintShutdownHourDefault;			
			}

			if (  oldmaintMemoryShutdownMonitorEnabled!=maintMemoryShutdownMonitorEnabled || 
					oldMaintMemoryShutdownThreshold != maintMemoryShutdownThreshold || 
					oldCriticalMemoryShutdownThreshold != criticalMemoryShutdownThreshold ||
					oldMaintShutdownHour != maintShutdownHour) {
				String output= String.format("Configuration updated %s: %s %s: %s %s: %s %s: %s", 
						maintMemoryShutdownMonitorEnabledName,Boolean.toString(maintMemoryShutdownMonitorEnabled),
						maintMemoryShutdownThresholdName, maintMemoryShutdownThreshold,
						criticalMemoryShutdownThresholdName, criticalMemoryShutdownThreshold,
						maintShutdownHourName,maintShutdownHour);
				logger.info(output);	
			}
		}

		@Override
		public void run() {
			if (maintMemoryShutdownMonitorEnabled == false) {
				readConfig(true);
				if (maintMemoryShutdownMonitorEnabled == false) {
					return;
				}
			}
			try {
				memUsage.update();
			} catch (PerformanceDataGenerationException e) {
				logger.error("Unable to update memory usage due to exception: ",e);
				return;
			}
			long usedMemoryPercent = memUsage.getCollectedUsedMem();
			if (usedMemoryPercent >= maintMemoryShutdownThreshold || usedMemoryPercent >= criticalMemoryShutdownThreshold  ) {
				if ( usedMemoryPercent < criticalMemoryShutdownThreshold && initiateShutdownAtLowTraffic == null) {
					initiateShutdownAtLowTraffic = new Timer("Shutdown During Low Traffic");
					lowThresholdTriggerDate = new GregorianCalendar();
					// reset hour, minutes, seconds and millis to triggerHour basically.
					lowThresholdTriggerDate.set(Calendar.HOUR_OF_DAY, maintShutdownHour);
					lowThresholdTriggerDate.set(Calendar.MINUTE, 0);
					lowThresholdTriggerDate.set(Calendar.SECOND, 0);
					lowThresholdTriggerDate.set(Calendar.MILLISECOND, 0); 
					//lowThresholdTriggerDate.add(Calendar.SECOND, 30); //test trigger in 30 seconds.

					GregorianCalendar now = new GregorianCalendar();
					if ( now.after(lowThresholdTriggerDate) == true  || now.equals(lowThresholdTriggerDate) == true ) {
						// check if we are after the time, if so make it tomorrow.
						lowThresholdTriggerDate.add(Calendar.DAY_OF_MONTH, 1); //make it tomorrow.
					}
					//do on a random minute to minimize the risk that all moip restart at same time.
					lowThresholdTriggerDate.set(Calendar.MINUTE, (int)(Math.random() * 30)); //Random Minute in the half hour

					lowThresholdTriggerDateString = DateFormat.getDateTimeInstance().format(lowThresholdTriggerDate.getTime());

					logger.warn("Initiate a shutdown(restart) at " + lowThresholdTriggerDateString + " as memory reached low Threshold of: " + maintMemoryShutdownThreshold  );
					initiateShutdownAtLowTraffic.schedule(new ShutdownAtLowTrafficHour(), lowThresholdTriggerDate.getTime());
				} else {
					if (usedMemoryPercent >= criticalMemoryShutdownThreshold) {		
						try {
						logger.error("Memory is at critical threshold, initiating short controlled shutdown(restart)");
						if (initiateShutdownAtLowTraffic != null) {
							initiateShutdownAtLowTraffic.cancel(); //cancel already initiated low traffic shutdown
							initiateShutdownAtLowTraffic = null;
						}
						memoryCheckPeriodicTimer.cancel(); //cancel self after initializing shutdown.
						initiateShutdownSoon = new Timer("Critical memory shutdown Timer");
						initiateShutdownSoonDate = new GregorianCalendar();
						int shutdowninMinutes=(int)(Math.round(Math.random() * 9)+1); //at least 1 minute so it's not now.
						initiateShutdownSoonDate.add(Calendar.MINUTE,shutdowninMinutes); //Shutdown in a random time up to 10 minutes so all MAS don't restart at same time.
						String triggerDateString = DateFormat.getDateTimeInstance().format(initiateShutdownSoonDate.getTime());
						logger.error("Memory is at critical threshold, initiating short controlled shutdown(restart) within 10 minutes at " + triggerDateString);
						initiateShutdownSoon.schedule(new ShutdownCriticalMemory(), initiateShutdownSoonDate.getTime());
						} catch (Throwable t) {
							logger.error("Exception when trying to create Critical memory shutdown Timer: ",t);
						}
					} else {
						logger.info("Memory " + usedMemoryPercent + "% exceeds lowThreshold " + maintMemoryShutdownThreshold + "% at interval check - will shutdown at " + lowThresholdTriggerDateString);
					} 
				}
			} else {
				logger.info("Memory " + usedMemoryPercent + "% does not exceed threshold " + maintMemoryShutdownThreshold + "% at interval check");
			}
			readConfig(true);
		}

	}

	private class ShutdownCriticalMemory extends TimerTask {

		ShutdownCriticalMemory() {
			logger.debug("ShutdownCriticalMemory() due to low memory initiated.");
		}

		@Override
		public void run() {
			logger.warn("MAS Will shutdown(restart) now at scheduled time due to critical memory overload.");
			MemoryMonitor.operateMAS.shutdown();
		}
	}

	private class ShutdownAtLowTrafficHour extends TimerTask {

		ShutdownAtLowTrafficHour() {
			logger.debug("ShutdownAtLowTrafficHour() due to low memory initiated.");
		}

		@Override
		public void run() {
			logger.warn("MAS will shutdown(restart) now at scheduled time due to memory overload.");
			MemoryMonitor.operateMAS.shutdown();
		}
	}

	public void init(OperateMAS operateMAS, IConfigurationManager configMan) {
		MemoryMonitor.operateMAS=operateMAS; 
		configManager = configMan;

		logger.info("Starting Mas Memory Monitor");

		//we create a periodic timer to monitor the memory and act.
		memoryCheckPeriodicTimer= new Timer("Periodic Memory Check");
		checkMemTask = new CheckMemory();
		memoryCheckPeriodicTimer.scheduleAtFixedRate(checkMemTask,0,CHECK_PERIOD_MS);
	}

}
