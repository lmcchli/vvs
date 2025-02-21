package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.common.logging.ILogger;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: etomste
 * Date: 2005-dec-27
 * Time: 14:38:55
 * To change this template use File | Settings | File Templates.
 */
public class TestAppender extends AppenderSkeleton {

    /* Wether or not to save the output */
    private static boolean save = false;

    /**
     * Contains the filtered output with triggers in it
     */
    private static List<String> listOfTestCaseOutput = new LinkedList<String>();

    /**
     * If we have seen end-of log mark.
     */
    private static boolean mark = true;

    /**
     * Cretae the test appender
     */
    public TestAppender() {
        super();
        clear();
    }

    /**
     * Clear the test cases list
     */
    public static synchronized void clear() {
        synchronized (listOfTestCaseOutput) {
            listOfTestCaseOutput.clear();
        }
    }

    /**
     * Returns with the list of the output of triggers
     *
     * @return
     */
    public static synchronized List<String> getOutputList() {
        List<String> lst;

        synchronized (listOfTestCaseOutput) {
            lst = listOfTestCaseOutput;
            listOfTestCaseOutput = new LinkedList<String>();
        }
        return lst;
    }

    /**
     * Starts the save of the log file output
     */
    public static void startSave() {
        synchronized (listOfTestCaseOutput) {
            save = true;
        }
    }

    /**
     * Stops the save of the logfile output
     * @param log
     */
    public static void stopSave(ILogger log) {
        synchronized (listOfTestCaseOutput) {
            save = false;
            mark = false;
        }
        log.fatal("<END-OF-LOG>");
    }

    /**
     * Write the event to stdout and to the list if needed
     *
     * @param loggingEvent
     */
    protected void append(org.apache.log4j.spi.LoggingEvent loggingEvent) {

        synchronized (listOfTestCaseOutput) {
            String s = getLayout().format(loggingEvent);
            System.out.print(s);
            ThrowableInformation throwableInformation;
            if ((throwableInformation = loggingEvent.getThrowableInformation()) != null) {
                Throwable throwable;
                if ((throwable = throwableInformation.getThrowable()) != null) {
                    throwable.printStackTrace(System.out);
                }
            }
            if (!mark) {
                if (s.contains("<END-OF-LOG>") && loggingEvent.getLevel() == Level.FATAL) {
                    mark = true;
                }
            }
            if (mark) {
                if (save) {
                    System.out.flush();

                    // See if we find the trigger for saving this output

                    // Actually we should filter out nonsens here, but I will do this
                    // later
                    listOfTestCaseOutput.add(s);
                }
            }
        }
    }

    /**
     * This appender requires a layout !
     *
     * @return
     */
    public boolean requiresLayout() {
        return true;
    }

    /**
     * Close the appender
     */
    public void close() {
        clear();
    }

}
