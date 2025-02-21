package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

/**
 * Examines a log file for a given criteria and returns with information
 * of the result
 */
public class LogFileExaminer {

    /**
     * The log file
     */
    protected ILogger log = ILoggerFactory.getILogger(getClass());

    /**
     * The reason why the examination of the log file failed
     */
    private String reason;

    /**
     * Contains a list of rexexp that signals a failure of a test
     * case wherever they appear.
     */
    private List<String> listOf1stFailure = new LinkedList<String>();

    /**
     * Contains a list of rexexp that signals a failure of a test case
     * if they do not match the current trigger in the listOfTriggers
     */
    private List<String> listOf3rdFailure = new LinkedList<String>();

    /**
     * Contains a list of rexexp that tells the examiner what must be
     * expected
     */
    private List<String> listOfIgnored = new LinkedList<String>();

    /**
     * If you want some exceptiions to what is being ignored, add it here
     */
    private List<String> listOfIgnoredExceptions = new LinkedList<String>();

    /**
     * Contains a list of rexexp that tells the examiner what must be
     * expected
     */
    private List<String> listOf2ndRequired = new LinkedList<String>();

    /**
     * The order of usage of the triggers are:
     *
     * 0. If the line should be ignored, continue with next line.
     *
     * 1. Check if the line matches any in listOfFailureTriggers, if so the
     * testcase fails
     *
     * 2. Check if the line matches the current trigger in the listOfTriggers.
     * 2.1 If it matches the current trigger is replaced with the next trigger in
     * the listOfTriggers and no check agains the listOfInbetweenFailureTriggers
     * are done.
     * 2,2 If it do not match the current trigger the line is checked against
     * the listOfInbetweenTriggers. If it matches, the test case failes.
     *
     * 3 If no current trigger exist the line is checked against listOfFailireTriggers
     * and listOfInbetweentriggers until no more lines are present.
     *
     * 4 If no failure is found the testcase succeedes.
     */

    /**
     * Adds a trigger to the list of failure triggers
     *
     * @param expr A rexpexpr that describes the failure matching
     */
    public void add1LevelFailureTrigger(String expr) {
        listOf1stFailure.add(expr);
    }

    /**
     * Adds a trigger that signals a failure if no match is made agains the
     * required triggers.
     *
     * @param expr
     */
    public void add3LevelFailureTrigger(String expr) {
        listOf3rdFailure.add(expr);
    }


    public void add3LevelFailureTrigger(TestEvent event) {
        listOf3rdFailure.add(".*" + pacify(event) + ".*");
    }

    private String pacify(Object obj) {
        return Pattern.quote(obj.toString());
    }

    /**
     * Adds a required trigger, note that the triggers are in the
     * order they are required.
     *
     * @param expr A required expr.
     */
    public void addIgnored(String expr) {
        listOfIgnored.add(expr);
    }

    public void addIgnoredException(String expr) {
        listOfIgnoredExceptions.add(expr);
    }

    public void add2LevelRequired(String expr) {
        listOf2ndRequired.add(expr);
    }

    public void add2LevelRequired(TestEvent event) {
        listOf2ndRequired.add(".*" + pacify(event) + ".*");
    }

    public void add2LevelRequired(TestEvent event, Object obj) {
        listOf2ndRequired.add(".*" + pacify(event + "=" + obj) + ".*");
    }

    public void add2LevelRequired(TestEvent event, String name, String value) {
        listOf2ndRequired.add(".*" + pacify(event + "={" + name + ":" + value + "}") + ".*");
    }

    /**
     * Returns with the reason why the examination have failed
     *
     * @return A string that describes the reason for failure.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Constructor for the examiner
     */
    public LogFileExaminer() {
    }

    /**
     * Evaluates a list of logfile outputs according to the criterias
     * in the 1,2,3nd level evaluations.
     *
     * @return True if the test case passed, false otherwise
     */
    public boolean evaluateLogFile(List<String> listOfLines) {
        ListIterator<String> i = listOf2ndRequired.listIterator();
        String required = "";
        boolean haveRequired = false;

        /* Show that inspection has started */
        log.info("Logfile examination started");
        reason = "Ok";

        /* Get the required */
        if (i.hasNext()) {
            required = i.next();
            haveRequired = true;
        }

        /* Loop through all lines */
        for (String line : listOfLines) {

            // Ignore this line?
            boolean ignore = false;
            for (String regexpr : listOfIgnored) {
                // Do a rexexpr match !
                String trimmedLine = line.trim();
                if (trimmedLine.matches(regexpr)) {
                    if (! isIgnoredException(trimmedLine)) {
                        log.info("Ignoring line due to rule:" + regexpr);
                        ignore = true;
                        break;
                    }
                }
            }
            if (ignore) continue;

            // Check against 1st level failures
            for (String regexpr : listOf1stFailure) {
                // Do a rexexpr match !
                String trimmedLine = line.trim();
                if (trimmedLine.matches(regexpr) && ! isIgnoredException(trimmedLine)) {
                    reason = "1st level failure : (" + regexpr + ")" + "Unexpected output (" + line + ")";
                    log.info(reason);
                    return false;
                }
            }

            // Check agains 2nd level required
            if (haveRequired) {

                // Match it !
                if (line.trim().matches(required)) {

                    // Matched
                    log.info("2nd level match : (" + line.trim() + ")");

                    // We matched second level, get next required
                    haveRequired = false;
                    if (i.hasNext()) {
                        required = i.next();
                        haveRequired = true;
                    }
                    continue;
                }
            }
            // We did not match second level, check 3rd
            // line failure !
            String trimmedLine = line.trim();
            for (String regexpr : listOf3rdFailure) {
                // Do a rexexpr match !
                if (trimmedLine.matches(regexpr) && ! isIgnoredException(trimmedLine)) {
                    reason = "3rd level failure : (" + regexpr + ")" + " Unexpected output (" + line + ")";
                    log.info(reason);
                    return false;

                }
            }
        }

        // Check the required lines
        if (haveRequired)

        {
            reason = "2nd level failure : Logfile do not have all required lines, were expection (" + required + ")";
            log.info(reason);
        }

        // Tell we have finished
        log.info("Logfile examination finished, result = " + String.valueOf(!haveRequired));

        // We have examine the entire logfile now, evaluate the
        // result. We have had no 1st and 3rd line failures, so
        // we need to see that we have gotten all expected output,
        // i.e. haveRequired must e false.
        return !haveRequired;

    }

    private boolean isIgnoredException(String trimmedLine) {
        for (String s : listOfIgnoredExceptions) {
            if (trimmedLine.matches(s)) {
                log.info("Line " + trimmedLine + " is not ignored; exception matches");
                return true;
            }
        }
        return false;
    }

    public void failOnUndefinedErrors() {
        failOnUndefinedErrors(false);
    }

    public void failOnUndefinedErrors(boolean ignoreWarnings) {
        add1LevelFailureTrigger(".*ERROR.*");
        add3LevelFailureTrigger(".*(?<!PlatformAccess)[Ee]xception.*");
        addIgnored(".*\\sDEBUG\\s.*");
        add3LevelFailureTrigger(".*(?<!Unhandled |class com.mobeon.masp.callmanager.events.|Adding event handler for |SimpleEventImpl\\{ event=)[Ee]rror(?!(\\.badfetch|\\.com\\.mobeon|Event)).*");
        if (!ignoreWarnings) {
            add3LevelFailureTrigger(".*[Ww]arning.*");
            add3LevelFailureTrigger(".*WARN.*");
        }
    }

    public void ignoreLogElement() {
        addIgnored(".*\\s<log\\s.*?/>.*");
    }

    public void ignoreTransition(String event) {
        event = event.replace(".", "\\.");
        addIgnored(".*<transition event=\"" + event + "\".*");
    }

    public void addIgnored(TestEvent event) {
        addIgnored(".*" + pacify(event) + ".*");
    }
}
