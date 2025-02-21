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
package com.mobeon.masp.operateandmaintainmanager.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.abcxyz.messaging.common.oam.PerformanceEvent;
import com.abcxyz.messaging.common.oam.impl.GenericPerformanceEvent;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceData;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceDataGenerationException;
import com.abcxyz.messaging.oe.common.perfmgt.PerformanceManagerExt;
import com.abcxyz.messaging.oe.common.util.MMSRuntime;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;


/**
 * Updates a performance counter that keeps track of the node's used
 * memory.
 *
 * @author lmcmajo
 */
public class MemUsageGenerator {
	//~ Instance fields ================================================

	/** Counter for used memory */

	/**
	 * @counter
	 *  Counter Name: UsedMemory;
	 *  Type of Measurement: NPC Component System Counter;
	 *  Output Units: percentage;
	 *  Definition: This counter indicates the percentage of real memory used on the node.;
	 */
	private PerformanceData counter = null;
	private static MMSRuntime runtime = null;
	private ILogger logger = null;
	private final static String COUNTER = "UsedMemory";
	private final static String NAME = "Memory usage generator (" + COUNTER + ")";
	private final static double OCTETS_TO_KO = 1024.0f;
	private static final long CMD_TIMEOUT = 30000;
	private static double totalMemory = -1;

	//~ Constructors ===================================================

	public MemUsageGenerator() {
		logger = ILoggerFactory.getILogger(MemUsageGenerator.class);
		runtime = MMSRuntime.getMMSRuntime();
	}


	//~ Methods ========================================================

	/* (non-Javadoc)
	 * @see abcxyz.services.mms.common.bpmanagement.perfmgt.PerformanceDataGenerator#init(abcxyz.services.mms.common.bpmanagement.perfmgt.PerformanceDataFactory)
	 */
	public void init(PerformanceManagerExt perfMgr) {
		PerformanceEvent memCtrEvent = new GenericPerformanceEvent(COUNTER, PerformanceEvent.PerfDataType.COUNTER);
		counter = perfMgr.getPerformanceData(memCtrEvent);
		counter.setValue(0);
		counter.setUnits("%");
	}

	public void cleanup(PerformanceManagerExt perfMgr) {
		PerformanceEvent ctrEvent = new GenericPerformanceEvent(COUNTER, PerformanceEvent.PerfDataType.COUNTER);
		perfMgr.removePerformanceData(ctrEvent);
	}

	/* (non-Javadoc)
	 * @see abcxyz.services.mms.common.bpmanagement.perfmgt.PerformanceDataGenerator#update()
	 */
	public void update() throws PerformanceDataGenerationException {
		long percentUsed = counter.getValue().getLong(); 
		if (logger != null) logger.debug("MemUsageGenerator.update()");
		try {
			if (runtime.isOSLinux()) {
				percentUsed = collectLinux();
			} else if (runtime.isOSUnix()) {
				percentUsed = collectUnix();
			} else if (runtime.isOSWindows()) {
				percentUsed = collectWindows();
			}
		}
		catch (PerformanceDataGenerationException e) {
			logger.warn("Unable to update used memory this run due to ",e);
		}
		finally {
			if (logger != null) logger.info("MemUsageGenerator.update(): counter percentUsed: " + percentUsed);
			// Set counter value even if an exception occurred.
			counter.setValue(percentUsed);
		}
	}

	/* (non-Javadoc)
	 * @see abcxyz.services.mms.common.bpmanagement.perfmgt.PerformanceDataGenerator#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * Collects % used memory on a Windows platform by executing the
	 * 4nt.exe "memory" sub-command.
	 *
	 * @return The % used memory, between 0 and 100.
	 *
	 * @throws PerformanceDataGenerationException If the command cannot
	 *         be executed
	 */
	private int collectWindows()
			throws PerformanceDataGenerationException {
		int percentUsed = 0;

		String[] buffer;

		try {
			buffer = runtime.execCmd("4nt /c memory", CMD_TIMEOUT);
		} catch (IOException e1) {
			throw new PerformanceDataGenerationException("I/O error: " +
					e1.getMessage());
		}

		/*
           Sample output:

                   56 % Memory load

            535,216,128 bytes total physical RAM
            234,635,264 bytes available physical RAM

            905,240,576 bytes total page file
            601,899,008 bytes available page file

            65,536 bytes total alias
            65,263 bytes free

            1,024 bytes total history

		 */
		for (int i = 0; i < buffer.length; i++) {
			int idx = buffer[i].toLowerCase().indexOf('%');

			if (idx > 0) {
				try {
					percentUsed = Integer.parseInt(buffer[i].substring(
							0, idx - 1).trim());
				} catch (NumberFormatException e) {
					throw new PerformanceDataGenerationException(
							"Error parsing the command result: " +
									e.getMessage());
				}
			}
		}

		return percentUsed;
	}
	
	public long getVmstatResult(int column)
            throws PerformanceDataGenerationException {
        long value = -1;

        String[] buffer;

        String cmd = "/usr/bin/vmstat 2 2";

        try {
            buffer = runtime.execCmd(cmd, CMD_TIMEOUT);
            for (int i = 0; i < buffer.length; i++) {
                String line = buffer[i];
                if (logger != null) logger.debug("CPUUsageGenerator.getVmstatResult() : vmstat: " + line.toString());
            }

        } catch (IOException e) {
            throw new PerformanceDataGenerationException("I/O error: "
                    + e.getMessage());
        }

        String line = null;

        if (buffer.length >= 4) {
            line = buffer[3];
        } else {
            throw new PerformanceDataGenerationException(cmd
                    + " output doesn't contain enough lines (got "
                    + buffer.length + ", expected at least 4.");
        }

        try {
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(line);

            for (int i = 1; i <= column; i++) {
                if (!matcher.find()) {
                    throw new PerformanceDataGenerationException(
                            "Error parsing command result: Requested value in column "
                                    + column + ", but only " + i
                                    + " columns available.");
                }
            }

            value = Long.parseLong(matcher.group());

        } catch (NumberFormatException e) {
            throw new PerformanceDataGenerationException(
                    "Error parsing command result: " + e.getMessage());
        }
        return value;
    }

	/**
	 * Collects % used memory on a Unix platform by executing the
	 * command vmstat
	 *
	 * @return The % used memory
	 *
	 * @throws PerformanceDataGenerationException If the command cannot
	 *         be executed
	 */
	private int collectUnix() throws PerformanceDataGenerationException {
		int percentUsed = 0;

		String[] buffer;

		try {
			if (totalMemory == -1) {
				// Get total memory on the system only once, using prtdiag
				String uname_cmd = "uname -i";
				buffer = runtime.execCmd(uname_cmd, CMD_TIMEOUT);
				if ((buffer == null) || (buffer.length < 1)) {
					throw new PerformanceDataGenerationException("'" + uname_cmd + "' command returned no output. Cannot determine OS. Cannot get memory statistics.");
				}
				String prtdiagCmd = "/usr/platform/" + buffer[0] + "/sbin/prtdiag";
				buffer = runtime.execCmd(prtdiagCmd, CMD_TIMEOUT);
				totalMemory = parseTotalMemoryUnix(buffer);
			}
		} catch (IOException ioe) {
			throw new PerformanceDataGenerationException("I/O error: " + ioe.getMessage());
		}

		// Get free memory (in bytes) using vmstat
		double freeMem = getVmstatResult(5) * OCTETS_TO_KO;
		if (logger != null) logger.debug("MemUsageGenerator.collectUnix() : freeMem (from vmstat, adjusted to bytes) = " + freeMem);

		// Calculate the percent free value
		if ((totalMemory > 0) && (totalMemory >= freeMem)) {
			double percentFree = (freeMem / totalMemory) * 100.0f;
			if (logger != null) {
				logger.debug("MemUsageGenerator.collectUnix() : percentFree = " + percentFree);
				logger.debug("MemUsageGenerator.collectUnix() : (int)percentFree = " + (int)percentFree);
			}

			percentUsed = (100 - ((int)percentFree));
		}

		return percentUsed;
	}

	// default visibility for unit test purposes
	double parseTotalMemoryUnix(String[] buffer) throws PerformanceDataGenerationException {
		double totalMem = -1;

		if (logger != null) logger.debug("MemUsageGenerator.parseTotalMemoryUnix() : prtdiag buffer:\n" + Arrays.asList(buffer));


		/* prtdiag sample output, third line is total memory:
		 *
		 * System Configuration: Sun Microsystems  sun4u Sun Blade 1500
		 * System clock frequency: 177 MHZ
		 * Memory size: 2GB
		 * [...]
		 *
		 * On some platforms, Memory size is shown as follows:
		 * Memory size: 4096 Megabytes
		 */

		try {
			if ((buffer == null) || (buffer.length < 3)) {
				throw new PerformanceDataGenerationException("Can't determine total system memory. System command did not return expected output.");
			}
			Pattern pattern = Pattern.compile("(\\d+)\\s*([KMG])");
			Matcher matcher = pattern.matcher(buffer[2]);
			if (!matcher.find()) {
				throw new PerformanceDataGenerationException("Can't determine total system memory - unexpected format of system command output: " + buffer[2]);
			}

			totalMem = new Double(matcher.group(1)).doubleValue();
			if (logger != null) logger.debug("MemUsageGenerator.parseTotalMemoryUnix() : Raw totalMemory = " + totalMemory);

			char qualifier = matcher.group(2).charAt(0);

			// Get the units of the free memory (Mega/Kilo bytes)
			if (qualifier == 'K') {
				totalMem *= OCTETS_TO_KO;
			} else if (qualifier == 'M') {
				totalMem *= OCTETS_TO_KO * OCTETS_TO_KO;
			} else if (qualifier == 'G') {
				totalMem *= OCTETS_TO_KO * OCTETS_TO_KO * OCTETS_TO_KO;
			} else {
				throw new PerformanceDataGenerationException("Can't determine total system memory: Unknown memory size units '" + qualifier + "'");
			}
		} catch (NumberFormatException nfe) {
			throw new PerformanceDataGenerationException("Can't interpret number: " +
					nfe.getMessage());
		}

		return totalMem;
	}

	/**
	 * Collects % used memory on a Linux platform by executing the
	 * command {@code /usr/bin/free}.<p>
	 *
	 * In RedHat and more recent OS versions, the {@code /usr/bin/free} command
	 * provides a true estimate for the amount of free memory. The column is
	 * called {@code available}. To calculate the percentage of used memory, this
	 * formula is used, with values taken from the line "Mem:" of the 
	 * {@code free} command : <p>
	 * <pre>(Total - available) / Total * 100</pre>
	 * 
	 * For a more detailed and technical description of what Linux counts as
	 * "available", see the commit that added the field :
	 * {@link https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/commit/?id=34e431b0ae398fc54ea69ff85ec700722c9da773}
	 * 
	 * <p>Otherwise, if the "available" field is present, the percentage of memory
	 * used as calculated the old way. For this calculation, note that Linux is taking
	 * up all available memory for the kernel/buffers/cache. Hence the need to take
	 * the number from the -/+ line under the free header. (the top tool would just
	 * report 95-100% memory used after a while...)
	 *
	 * @return The % used memory
	 *
	 * @throws PerformanceDataGenerationException If the command cannot
	 *         be executed
	 */
	private int collectLinux() throws PerformanceDataGenerationException {
		String[] buffer;
		try {
			buffer = runtime.execCmd("/usr/bin/free", CMD_TIMEOUT);
		} catch (IOException e1) {
			throw new PerformanceDataGenerationException("I/O error: " + e1.getMessage());
		}

		if (buffer.length > 0) {
			if (buffer[0].contains("available")) {
				return memUsageWithAvailableField(buffer);
			} else {
				return memUsageWithoutAvailableField(buffer);
			}
		} else {
			throw new PerformanceDataGenerationException("No output retrieved from the linux free command. Returned buffer is empty.");
		}
	}

	private int memUsageWithAvailableField(String[] buffer) throws PerformanceDataGenerationException {
		int percentUsed = 0;
		String totalMem = null;
		String availableMem = null;

		for (int i = 0; i < buffer.length; i++) {
			String line = buffer[i];

			/**
			 * Sample output (with the "available" field):
			 * 
			 *               total        used        free      shared  buff/cache   available
			 * Mem:       14202572     2699724     1659216       24260     9843632     9861472
			 * Swap:       3354620        7476     3347144
			 * 
			 */

			if (line.startsWith("Mem:")) {
				Pattern pattern;
				Matcher matcher;

				if (logger != null) logger.debug("MemUsageGenerator.memUsageWithAvailableField() : Mem line is : " + line);
				//search for the first occurrence of a digit, which is the total memory
				pattern = Pattern.compile("\\d+");
				matcher = pattern.matcher(line);

				if (matcher.find()) {
					totalMem = matcher.group();
					if (logger != null) logger.debug("MemUsageGenerator.memUsageWithAvailableField() : Matched String (total mem): " + totalMem);
				} else {
					throw new PerformanceDataGenerationException("Error parsing output of free command : Could the get the total memory.");
				}

				// Skip next 4 digits
				for (int j = 0; j <= 3; j++) {
					if (!matcher.find()) {
						throw new PerformanceDataGenerationException("Error parsing output of free command : Unexpected number of columns.");
					}
				}
				if (matcher.find()) {
					availableMem = matcher.group();
					if (logger != null) logger.debug("MemUsageGenerator.memUsageWithAvailableField() : Matched String (available mem): " + availableMem);
				} else {
					throw new PerformanceDataGenerationException("Error parsing output of free command : Could not get the available memory.");
				}
			}
		}

		if( (totalMem !=null) && (availableMem !=null) ) {
			double total = Double.valueOf(totalMem).doubleValue();
			double available = Double.valueOf(availableMem).doubleValue();
			if ((total != 0) ) {
				percentUsed = (int)(((total-available) / total) * 100.0f);
			}
		}
		return percentUsed;
	}


	private int memUsageWithoutAvailableField(String[] buffer) throws PerformanceDataGenerationException {
		int percentUsed = 0;
		String totalMem = null;
		String freeMem = null;

		for (int i = 0; i < buffer.length; i++) {
			String line = buffer[i];

			/**
			 * Sample output (without the "available" field):
			 * 
			 *            total       used       free     shared    buffers     cached
			 * Mem:       3960796     562676    3398120          0       1828     366528
			 * -/+ buffers/cache:     194320    3766476
			 * Swap:      4198312          0    4198312
			 * 
			 */

			if (line.startsWith("Mem:")) {
				Pattern pattern;
				Matcher matcher;

				if (logger != null) logger.debug("MemUsageGenerator.memUsageWithoutAvailableField() : Mem line is : " + line);
				//search for the first occurrence of a digit, which is the total memory
				pattern = Pattern.compile("\\d+");
				matcher = pattern.matcher(line);

				if (matcher.find()) {
					totalMem = matcher.group();
					if (logger != null) logger.debug("MemUsageGenerator.memUsageWithoutAvailableField() : Matched String (total mem): " + totalMem);
				} else {
					throw new PerformanceDataGenerationException("Error parsing output of free command : Could the get the total memory.");
				}
			}
			if (line.startsWith("-/+")) {
				Pattern pattern;
				Matcher matcher;
				//search for the first occurrence of a digit, which is the total memory
				pattern = Pattern.compile("\\d+");
				matcher = pattern.matcher(line);
				matcher.find(); //Skip the first match
				if (matcher.find()) {
					freeMem = matcher.group();
					if (logger != null) logger.debug("MemUsageGenerator.memUsageWithoutAvailableField() : Matched String (free mem): " + freeMem);
				} else {
					throw new PerformanceDataGenerationException("Error parsing output of free command : Could not get the free memory.");
				}
			}
		}

		if( (totalMem !=null) && (freeMem !=null) ) {
			double total = Double.valueOf(totalMem).doubleValue();
			double free = Double.valueOf(freeMem).doubleValue();
			if ((total != 0) ) {
				percentUsed = (int)(((total-free) / total) * 100.0f);
			}
		}
		return percentUsed;
	}


	/**
	 * Gets the current value of the used memory counter, as was set by
	 * the last call to the update() method.
	 *
	 * @return The amount of used memory on the node, as a percentage.
	 */
	public long getCollectedUsedMem() {
		return counter.getValue().getLong();
	}

	/**
	 * Test stub. Jar this file with dependent classes and run it on different
	 * platforms to test the commands.
	 * <p>
	 * Arguments:
	 * <ul>
	 * <li> interval: time interval in seconds between each execution of the
	 * command (defaults to 5 seconds)
	 * </ul>
	 * </p>
	 *
	 * @param args
	 *        command-line arguments
	 */
	/*
    public static void main(String args[]) {
        int interval = 5;

        try {
            if (args.length >= 1) {
                interval = Integer.parseInt(args[0]);
            }

            MemUsageGenerator gen = new MemUsageGenerator();
            gen.init(new PerformanceDataFactory());
            int sleeptime = interval * 1000;

            System.out.println("Used memory (%) (CTRL-C to quit)");
            System.out.println("--------------");

            while (true) {
                gen.update();
                System.out.println(gen.getCollectedUsedMem());
                Thread.sleep(sleeptime);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (PerformanceDataGenerationException e) {
            e.printStackTrace();
        }
    } */
}
