/*
 * Copyright (c) 2010 Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Loads the configured rules for the NumberAnalyzer. The rules are defined in the numberanalyzer.xml file.
 */
public class NumberAnalyzerConfiguration {

    private static final String[] LIST_OF_RULES = {"INBOUNDCALL.Table",
        "RETRIEVALPREFIXRULE.Table",
        "OUTBOUNDCALL.Table",
        "CLITOSMSCALL.Table",
        "CUTTHROUGHPAGING.Table",
        "FAXPRINTRULE.Table",
        "IVRTOSMS.Table",
        "SLAMDOWNCALL.Table",
        "Office.Table",
        "SUBSCRIBEROUTDIAL.Table",
        "ECHONUMBER.Table",
        "MISSEDCALLNOTIFICATION.Table",
        "MWINOTIFICATION.Table",
        "CALLEROUTDIAL.Table",
        "EXTRARULE1.Table",
        "EXTRARULE2.Table",
        "EXTRARULE3.Table",
        "EXTRARULE4.Table",
        "EXTRARULE5.Table",
        "EXTRARULE6.Table",
        "EXTRARULE7.Table",
        "EXTRARULE8.Table",
        "EXTRARULE9.Table",
        "EXTRARULE10.Table",
        "EXTRARULE11.Table",
        "EXTRARULE12.Table",
        "EXTRARULE13.Table",
        "EXTRARULE14.Table",
        "EXTRARULE15.Table",
        "EXTRARULE16.Table",
        "EXTRARULE17.Table",
        "EXTRARULE18.Table",
        "EXTRARULE19.Table",
        "EXTRARULE20.Table"
    };

    private static final String REGION_CODE_RULE_NAME = "regioncoderule";
    private static final String INPUT_GROUP = "input";
    private static final String RETURN_GROUP = "return";
    private static final String RULE_LEVEL_VALUES = "RuleLevelValues";
    private static final String PRIORITY = "priority";

    private static ILogger log;

    /**
     * Map of Rules keyed by name
     */
    private static HashMap<String, Rule> rules = new HashMap<String, Rule>();
    /**
     * Current configuration reference
     */
    private IConfiguration configuration;
    /**
     * Singleton instance of this class
     */
    private static NumberAnalyzerConfiguration instance = new NumberAnalyzerConfiguration();

    /**
     * Constructor. Setups a default configuration.
     */
    private NumberAnalyzerConfiguration() {
        log = ILoggerFactory.getILogger(NumberAnalyzerConfiguration.class);
    }

    static NumberAnalyzerConfiguration getInstance() {
        return instance;
    }

    /**
     * Sets the configuration. This method should only be called once when the Number Analyzer is initiated.
     *
     * @param config The configuration instance.
     * @throws IllegalArgumentException If <code>config</code> is <code>null</code>.
     */
    void setConfiguration(IConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Parameter config is null");
        }
        configuration = config;
    }

    HashMap<String, Rule> getRules() {
        return rules;
    }

    /**
     * Reads configuration parameters.
     *
     * @throws ConfigurationException if configuration could not be read.
     */
    void update() throws ConfigurationException, NumberAnalyzerException {

        HashMap<String, Rule> newRules = new HashMap<String, Rule>();

        for (String currentRule: LIST_OF_RULES) {
            Map<String, Map<String,String>> ruleMap = configuration.getGroup("numberAnalyzer.conf").getTable(currentRule);

            String ruleInputExpr = null;
            String ruleReturnExpr = null;
            String ruleRegionCodeExpr = null;

            if(ruleMap == null) {
                log.warn("NumberAnalyzerConfiguration.update(): could not find rule " + currentRule);
                continue;
            }

            log.debug(ruleMap.toString());

            // Remove the ".Table" part of the rule name
            String currentRuleName = currentRule.substring(0, currentRule.indexOf("."));
            Rule mainRule = new Rule(currentRuleName);

            // First check if the rule contains a 'RULE_LEVEL_VALUES' sub rule which contain rule-related values
            if (ruleMap.containsKey(RULE_LEVEL_VALUES)) {
                ruleInputExpr = ruleMap.get(RULE_LEVEL_VALUES).get(INPUT_GROUP);
                ruleReturnExpr = ruleMap.get(RULE_LEVEL_VALUES).get(RETURN_GROUP);
                ruleRegionCodeExpr = ruleMap.get(RULE_LEVEL_VALUES).get(REGION_CODE_RULE_NAME);
                mainRule.setExpressions(ruleInputExpr, ruleReturnExpr);
                mainRule.setRegionCodeRuleName(ruleRegionCodeExpr);

                // Remove the RULE_LEVEL_VALUES for the remaining sub rule parsing
                ruleMap.remove(RULE_LEVEL_VALUES);
            }

            // Create the sub rules
            List<Rule> subrules = new ArrayList<Rule>(2);

            Iterator<String> it = ruleMap.keySet().iterator();
            while (it.hasNext()) {
                String subRuleInputExpr = null;
                String subRuleReturnExpr = null;
                String subRuleRegionCodeExpr = null;
                String subRulePriority = null;

                String subRuleName = it.next();
                subRuleInputExpr = ruleMap.get(subRuleName).get(INPUT_GROUP);
                subRuleReturnExpr = ruleMap.get(subRuleName).get(RETURN_GROUP);
                subRuleRegionCodeExpr = ruleMap.get(subRuleName).get(REGION_CODE_RULE_NAME);
                subRulePriority = ruleMap.get(subRuleName).get(PRIORITY);

                Rule subrule = makeRule(subRuleName, subRuleRegionCodeExpr, subRuleInputExpr, subRuleReturnExpr, subRulePriority);
                log.debug("SubRule " + subRuleName + ": Input=" + subRuleInputExpr + " Return: " + subRuleReturnExpr + " Region: " + subRuleRegionCodeExpr + " Priority: " + subRulePriority);
                subrules.add(subrule);
            }
            mainRule.setSubRules(subrules);
            log.debug("NumberAnalyzerConfiguration.update(): After setting sorted subrules of rule: " + currentRuleName + mainRule.toString());

            // Add the rule containing sub rules
            newRules.put(currentRuleName, mainRule);
        }


        rules = newRules;
    }

    private Rule makeRule(String name, String regionCodeRuleName, String inputExpr, String returnExpr, String subRulePriority) throws NumberAnalyzerException {
        Rule rule = new Rule(name);
        rule.setRegionCodeRuleName(regionCodeRuleName);
        rule.setExpressions(inputExpr, returnExpr);
        if(subRulePriority != null) {
            try {
                rule.setPriority(new Integer(subRulePriority));
            } catch (NumberFormatException nme) {
                log.warn("NumberAnalyzerConfiguration.makeRule: unable to parse priority: " + subRulePriority + " " + nme.getMessage());
            }
        }
        return rule;
    }

    public static void main(String args[]) throws ConfigurationException, NumberAnalyzerException {
/*        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("config/numberAnalyzer.conf");
        IConfiguration config = cm.getConfiguration();
        System.out.println(config.toString());

        try {
            Map<String, Map<String,String>> myMap = cm.getConfiguration().getGroup("numberAnalyzer.conf").getTable("INBOUNDCALL.Table");
            System.out.println(myMap.toString());

            Iterator<String> it = myMap.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                System.out.println("key: " + key);
                System.out.println("  " + myMap.get(key));
            }
        } catch (Exception e) {
            System.out.println("Exception");
            ;
        }
        return;*/

        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("config/numberAnalyzer.conf");
        IConfiguration config = cm.getConfiguration();
        NumberAnalyzerConfiguration.getInstance().setConfiguration(config);
        NumberAnalyzerConfiguration.getInstance().update();

        List<Rule> subRules = NumberAnalyzerConfiguration.getInstance().getRules().get("INBOUNDCALL").getSubRules();
        Iterator<Rule> it = subRules.iterator();
        while (it.hasNext()) {
            Rule subRule = it.next();
            System.out.println("subRule "  + subRule.getName());
        }
    }
}
