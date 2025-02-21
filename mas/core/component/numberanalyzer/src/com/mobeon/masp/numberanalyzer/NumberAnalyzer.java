/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import com.mobeon.common.configuration.*;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes a number with the rules defined in the Rule class. The Rule contains regular expressions that are used for
 * the pattern matching.
 * If a Rule contains several subrules the subrules are matched in the order they appear, i.e the first subrule has
 * highest priority.
 * <p/>
 * Some certain rules exist. Some rules indicate that the length should be checked or the number should be blocked.
 * In those cases an NumberAnalyzerException is thrown if a match is found
 *
 * @author ermmaha
 */
public class NumberAnalyzer implements INumberAnalyzer, IEventReceiver {
        
    private static ILogger log;
    static {
    		ILoggerFactory.configureAndWatch("/opt/moip/config/mas/log4j2.xml");
		log = ILoggerFactory.getILogger(NumberAnalyzer.class);
    }

    /**
     * Sets the configuration
     *
     * @param configuration to get the rules from
     */
    public void setConfiguration(IConfiguration configuration) throws NumberAnalyzerException {
        updateConfiguration(configuration);
    }

    private void updateConfiguration(IConfiguration configuration) throws NumberAnalyzerException {
        try {
            NumberAnalyzerConfiguration numberAnalyzerConfiguration = NumberAnalyzerConfiguration.getInstance();
            numberAnalyzerConfiguration.setConfiguration(configuration);
            numberAnalyzerConfiguration.update();
        } catch (ConfigurationException e) {
            log.error("Exception in updateConfiguration ", e);
            //throw new ServiceEnablerException("Could not configure Number Analyzer");
            throw new NumberAnalyzerException("Could not configure Number Analyzer");
        } catch (NumberAnalyzerException e) {
            log.error("Exception in updateConfiguration ", e);
            //throw new ServiceEnablerException("Could not configure Number Analyzer");
            throw new NumberAnalyzerException("Could not configure Number Analyzer");
        }
    }

    /**
     * Sets the event dispatcher that should be used to receive global events.
     *
     * @param eventDispatcher
     */
    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        eventDispatcher.addEventReceiver(this);
    }

    public void doEvent(Event event) {
    }

    /**
     * This method is used to receive global events fired by any event dispatcher in the system.
     * <p/>
     * Number Analyzer is only interested in the {@link ConfigurationChanged}
     * event which is used to reload the configuration.
     */
    public void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("In doGlobalEvent: ConfigurationChanged event received, reloading configuration");
                }
                ConfigurationChanged configurationChanged = (ConfigurationChanged) event;
                updateConfiguration(configurationChanged.getConfiguration());
            } catch (NumberAnalyzerException e) {
                log.error("Could not reload configuration " + e);
            }
        }
    }

    public IAnalysisInput getAnalysisInput() {
        return new AnalysisInput();
    }

    public String analyzeNumber(IAnalysisInput input) throws NumberAnalyzerException {
        String number = input.getNumber();
        if (number == null) throw new NumberAnalyzerException("Number is null");

        String ruleKey = input.getRule();
        if (!NumberAnalyzerConfiguration.getInstance().getRules().containsKey(ruleKey))
            throw new NumberAnalyzerException("Rule " + ruleKey + " was not found", NumberAnalyzerException.NORULE);

        Rule rule = NumberAnalyzerConfiguration.getInstance().getRules().get(ruleKey);

        return analyze(number, rule, input.getInformationContainingRegionCode());
    }

    /**
     * First checks the Rule and if no match the list of subrules are checked.
     *
     * @param number
     * @param rule
     * @param regionCode (is applied if not null and a rule specified with it exists)
     * @return analysed number
     * @throws NumberAnalyzerException
     */
    private String analyze(String number, Rule rule, String regionCode) throws NumberAnalyzerException {
        String result = evaluateExpressions(number, rule);
        if (result != null) return result;

        List<Rule> subRules = rule.getSubRules();
        Iterator<Rule> it = subRules.iterator();
        while (it.hasNext()) {
            Rule subRule = it.next();

            result = evaluateExpressions(number, subRule);
            if (result != null) {
                // Apply regioncodes if the subrule has a regioncoderule
                if ((regionCode != null) && (subRule.getRegionCodeRuleName() != null)) {
                    return applyRegionCode(result, regionCode, subRule.getRegionCodeRuleName());
                }
                return result;  // If match return, if not test next rule
            }
        }
        if(log.isInfoEnabled()) {
            log.info("No subrule match for number: " + number + "\n" + rule.toString());
        }

        throw new NumberAnalyzerException("No rule matched the number " + number, NumberAnalyzerException.NOMATCH);
    }

    /**
     * Evaluates the expressions in a Rule.
     *
     * @param number
     * @param rule
     * @return result of the analyse. null if no match
     * @throws NumberAnalyzerException
     */
    private String evaluateExpressions(String number, Rule rule) throws NumberAnalyzerException {
        checkAllowedLength(number, rule);
        if (rule.getInputExpr() == null) return null;

        if(log.isDebugEnabled())
            log.debug("Trying to match " + number + " with pattern " + rule.getInputExpr());
        Pattern pattern = rule.getInputExpr();
        Matcher matcher = pattern.matcher(number);
        if (matcher.find()) {
            if(log.isDebugEnabled())
                log.debug("Pattern matched ");
            checkBlockedType(rule);
            return buildReturnString(rule.getReturnExpr(), matcher);
        }
        return null;
    }

    private void checkAllowedLength(String number, Rule rule) throws NumberAnalyzerException {
        ReturnExpression returnExpr = rule.getReturnExpr();
        if (returnExpr != null && returnExpr instanceof LengthReturnExpression) {
            LengthReturnExpression lengthReturnExpression = (LengthReturnExpression) returnExpr;
            if (number.length() < lengthReturnExpression.getMin()) {
                if (lengthReturnExpression.getMin() == lengthReturnExpression.getMax()) {
                    throw new NumberAnalyzerException("Wrong length on number " + number.length(),
                            "EXACTLY=" + lengthReturnExpression.getMin());
                }
                throw new NumberAnalyzerException("Wrong min length on number " +
                        number.length() + " < " + lengthReturnExpression.getMin(),
                        "MIN=" + lengthReturnExpression.getMin());
            }
            if (number.length() > lengthReturnExpression.getMax()) {
                if (lengthReturnExpression.getMin() == lengthReturnExpression.getMax()) {
                    throw new NumberAnalyzerException("Wrong length on number " + number.length(),
                            "EXACTLY=" + lengthReturnExpression.getMin());
                }
                throw new NumberAnalyzerException("Wrong max length on number " +
                        number.length() + " > " + lengthReturnExpression.getMax(),
                        "MAX=" + lengthReturnExpression.getMax());
            }
        }
    }

    private void checkBlockedType(Rule rule) throws NumberAnalyzerException {
        ReturnExpression returnExpr = rule.getReturnExpr();
        if (returnExpr != null && returnExpr instanceof BlockReturnExpression) {
            throw new NumberAnalyzerException("The number is blocked", NumberAnalyzerException.BLOCKED);
        }
    }

    private String buildReturnString(ReturnExpression returnExpr, Matcher matcher)
            throws NumberAnalyzerException {

        if (!(returnExpr instanceof GroupReturnExpression)) return "";

        GroupReturnExpression groupReturnExpression = (GroupReturnExpression) returnExpr;

        String toReturn = "";
        List<String> gList = groupReturnExpression.getGroupList();
        Iterator<String> it = gList.iterator();
        while (it.hasNext()) {
            String exp = it.next();
            if (exp.startsWith(GroupReturnExpression.GROUPIDENTIFIER)) {
                int count = getGroupCount(exp);
                //double check that the rules are configured correctly
                if (count > matcher.groupCount()) {
                    throw new NumberAnalyzerException("Invalid group identifier " + count);
                }
                toReturn += matcher.group(count);
            } else {
                toReturn += exp;
            }
        }
        return toReturn;
    }

    /**
     * String utility function to get int value of the char after the $i string
     * Ex: $i2 --> 2
     *
     * @param expr
     * @return
     * @throws NumberAnalyzerException
     */
    private static int getGroupCount(String expr) throws NumberAnalyzerException {
        String tmp = expr.substring(2);
        try {
            return Integer.parseInt(tmp);
        } catch (NumberFormatException nox) {
            throw new NumberAnalyzerException("Invalid group identifier " + nox);
        }
    }

    /**
     * Checks the configured regioncodes and applies the one that matches the informationContainingRegionCode parameter.
     *
     * @param number
     * @param informationContainingRegionCode
     *
     * @param regionCodeRuleName
     * @return number with applied regioncode (regioncode + number)
     * @throws NumberAnalyzerException if no regioncode with regionCodeRuleName is configured.
     */
    private String applyRegionCode(String number, String informationContainingRegionCode, String regionCodeRuleName)
            throws NumberAnalyzerException {

        if (log.isDebugEnabled()) {
            log.debug("In applyRegionCode, number=" + number + ", informationContainingRegionCode=" +
                    informationContainingRegionCode + ", regionCodeRuleName=" + regionCodeRuleName);
        }

        if (!NumberAnalyzerConfiguration.getInstance().getRules().containsKey(regionCodeRuleName)) {
            throw new NumberAnalyzerException("A RegionCodeRule with name=" + regionCodeRuleName + " is not configured");
        }

        Rule regionCodeRule = NumberAnalyzerConfiguration.getInstance().getRules().get(regionCodeRuleName);
        String pattern = regionCodeRule.getInputExpr().toString();
        String codes[] = pattern.split(",");
        for (int i = 0; i < codes.length; i++) {
            if (log.isDebugEnabled()) {
                log.debug("In applyRegionCode, matching " + codes[i] + " to " + informationContainingRegionCode);
            }
            if (informationContainingRegionCode.startsWith(codes[i])) {
                if (log.isDebugEnabled()) {
                    log.debug("In applyRegionCode, match found, result is " + codes[i] + number);
                }
                return codes[i] + number;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("In applyRegionCode, no regioncode matched, returning " + number);
        }
        return number;
    }
    /**
     * Convinence method to add a Rule to the rules configuration map in NumberAnalyzerConfiguration
     *
     * @param rule
     */
    public void addRulesConfig(Rule rule) {
        NumberAnalyzerConfiguration.getInstance().getRules().put(rule.getName(), rule);
    }

    /**
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        String fileName = null;
        String rule = null;
        String regionCode = null;
        String number = null;

	
        for (int optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-f")) {
                fileName = argv[++optind];
            } else if (argv[optind].equals("-r")) {
                rule = argv[++optind];
            } else if (argv[optind].equals("-n")) {
                number = argv[++optind];
            } else if (argv[optind].equals("-a")) { //optional
                if (optind + 1 < argv.length) regionCode = argv[++optind];
            } else if (argv[optind].startsWith("-")) {
                die("Usage: NumberAnalyzer [-f file] [-r rule] [-n number] [-a aninumber]");
            } else {
                break;
            }
        }

        verifyArgs(fileName, rule, number);

        IConfiguration configuration = loadConfiguration(fileName);

        NumberAnalyzer numberAnalyzer = new NumberAnalyzer();
        try {
            numberAnalyzer.setConfiguration(configuration);
        } catch (NumberAnalyzerException e) {
            die("Exception " + e);
        }
        IAnalysisInput analysisInput = numberAnalyzer.getAnalysisInput();
        analysisInput.setRule(rule);
        analysisInput.setNumber(number);
        analysisInput.setInformationContainingRegionCode(regionCode);

        String result;
        try {
            result = numberAnalyzer.analyzeNumber(analysisInput);
        } catch (NumberAnalyzerException e) {
            result = e.getMessage();
        }
        System.out.println("+-------------------------------------------------");
        System.out.println("| Number Analysis Test Tool v2.0");
        System.out.println("|");
        System.out.println("| Test of number: " + number);
        System.out.println("| Rule: " + rule);
        if (regionCode != null) System.out.println("| Aninumber: " + regionCode);
        System.out.println("| Result: " + result);
        System.out.println("|");
        System.out.println("+-------------------------------------------------");
    }

    private static IConfiguration loadConfiguration(String cfgFile) {
        IConfigurationManager configurationManager = new ConfigurationManagerImpl();
        configurationManager.setConfigFile(cfgFile);
        return configurationManager.getConfiguration();
    }

    private static void verifyArgs(String fileName, String rule, String number) {
        if (fileName == null) {
            die("No ruleconfiguration file specified");
        }
        if (rule == null) {
            die("No rule specified");
        }
        if (number == null) {
            die("No number specified");
        }
    }

    private static void die(String msg) {
        System.out.println("Terminated: " + msg);
        System.exit(1);
    }
}
