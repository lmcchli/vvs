/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement.
* 
*/
/***************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
 ***************************************************************************/

package gov.nist.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A wrapper around log4j that is used for logging debug and errors. You can replace this 
 * file if you want to change the way in which messages are logged.
 * 
 * @version 1.2
 * 
 * @author M. Ranganathan <br/>
 * @author M.Andrews
 * @author Jeroen van Bemmel
 * 
 */

/* mmath: Now relies on external initialization of log4j.
          Does not count log lines anymore since line number will not
          relevant.
*/

public class LogWriter {

	private static final Logger logger = Logger.getLogger(LogWriter.class);

	/**
	 * Dont trace
	 */
	public static final int TRACE_NONE = 0;

	
	/**
	 * Trace message processing
	 */
	public static final int TRACE_MESSAGES = 16;

	/**
	 * Trace exception processing
	 */
	public static final int TRACE_EXCEPTION = 17;

	/**
	 * Debug trace level (all tracing enabled).
	 */
	public static final int TRACE_DEBUG = 32;

	/**
	 * Flag to indicate that logging is enabled.
	 */
	private volatile boolean needsLogging = false;

	private int lineCount;

	

	/**
	 * trace level
	 */

	protected  int traceLevel = TRACE_NONE;

	/**
	 * log a stack trace. This helps to look at the stack frame.
	 */
	public void logStackTrace() {
		if (needsLogging) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StackTraceElement[] ste = new Exception().getStackTrace();
			// Skip the log writer frame and log all the other stack frames.
			for (int i = 1; i < ste.length; i++) {
				String callFrame = "[" + ste[i].getFileName() + ":"
						+ ste[i].getLineNumber() + "]";
				pw.print(callFrame);
			}
			pw.close();
			String stackTrace = sw.getBuffer().toString();

			this.logDebug(stackTrace);

		}
	}

	public int getLineCount() {
		return lineCount;
	}

	public void logException(Throwable ex) {

		if (needsLogging) {

			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Counts the line number so that the debug log can be correlated to the
	 * message trace.
	 * 
	 * @param message --
	 *            message to count the lines for.
	 */
	private void countLines(String message) {
		char[] chars = message.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\n')
				lineCount++;
		}

	}

	/**
	 * Prepend the line and file where this message originated from
	 * 
	 * @param message
	 * @return re-written message.
	 */
	private String enhanceMessage(String message) {

		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		StackTraceElement elem = stackTrace[2];
		String className = elem.getClassName();
		String methodName = elem.getMethodName();
		String fileName = elem.getFileName();
		int lineNumber = elem.getLineNumber();
		String newMessage = className + "." + methodName + "(" + fileName + ":"
				+ lineNumber + ") [" + message + "]";
		return newMessage;

	}

	/**
	 * Log a message into the log file.
	 * 
	 * @param message
	 *            message to log into the log file.
	 */
	public void logDebug(String message) {
		if (needsLogging) {
			String newMessage = this.enhanceMessage(message);
//	                countLines(newMessage);
			logger.debug(newMessage);
		}

	}

	/**
	 * Set the trace level for the stack.
	 */
	private void setTraceLevel(int level) {
		traceLevel = level;
	}

	/**
	 * Get the trace level for the stack.
	 */
	public int getTraceLevel() {
		return traceLevel;
	}

	/**
	 * Log an error message.
	 * 
	 * @param message --
	 *            error message to log.
	 */
	public void logFatalError(String message) {
		String newMsg = this.enhanceMessage(message);
//		countLines(newMsg);
		logger.fatal(newMsg);

	}

	/**
	 * Log an error message.
	 * 
	 * @param message --
	 *            error message to log.
	 * 
	 */
	public void logError(String message) {
		String newMsg = this.enhanceMessage(message);
//		countLines(newMsg);
		logger.error(newMsg);

	}

	/**
	 * @param configurationProperties
	 */
	public LogWriter(Properties configurationProperties) {
		String logLevel = configurationProperties
				.getProperty("gov.nist.javax.sip.TRACE_LEVEL");

		
        if (logLevel != null) {
			try {
				int ll = 0;
				if (logLevel.equals("DEBUG")) {
					ll = TRACE_DEBUG;
				} else if (logLevel.equals("TRACE")) {
					ll = TRACE_MESSAGES;
				} else if (logLevel.equals("ERROR")) {
					ll = TRACE_EXCEPTION;
				} else if (logLevel.equals("NONE")) {
					ll = TRACE_NONE;
				} else {
					ll = Integer.parseInt(logLevel);
				}

				this.setTraceLevel(ll);
				this.needsLogging = true;
				if (traceLevel == TRACE_DEBUG) {
					logger.setLevel(Level.DEBUG);
				} else if (traceLevel == TRACE_MESSAGES) {
					logger.setLevel(Level.INFO);
				} else if (traceLevel == TRACE_EXCEPTION) {
					logger.setLevel(Level.ERROR);
				} else if (traceLevel == TRACE_NONE) {
					logger.setLevel(Level.OFF);
					this.needsLogging = false;
				}

			} catch (NumberFormatException ex) {
//				System.out.println("LogWriter: Bad integer " + logLevel);
//				System.out.println("logging dislabled ");
				needsLogging = false;
			}
		} else {
			this.needsLogging = false;

		}

	}

	/**
	 * @return flag to indicate if logging is enabled.
	 */
	public boolean isLoggingEnabled() {

		return needsLogging;
	}
	
	public boolean isLoggingEnabled( int logLevel ) {
		return needsLogging && logLevel < traceLevel;
	}

	public void logError(String message, Exception ex) {
		logger.error(message, ex);
	}

	public void logWarning(String string) {
		logger.warn(string);
	}
	
}
