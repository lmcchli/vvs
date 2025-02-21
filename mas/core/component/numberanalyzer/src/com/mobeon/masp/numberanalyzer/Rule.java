/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.numberanalyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class that models a Rule that is used when doing the number analysis. A Rule has a name, an input expression and a
 * return expression. A Rule can also contain a List of Rule objects (subrules)
 *
 * @author ermmaha
 */
public class Rule {
    /**
     * Name for this rule
     */
    private String name;
    /**
     * Name for the regioncode to use if this rule matches
     */
    private String regionCodeRuleName;
    /**
     * java.util.regex.Pattern used for the input-expression (may be null)
     */
    private Pattern inputExpression;
    /**
     * ReturnExpression object used for the return-expression (may be null)
     */
    private ReturnExpression returnExpression;
    /**
     * List of subrules
     */
    private List<Rule> subrules;
    /**
     * Priority of subrules
     */
    private int priority;


    /**
     * Constructor
     *
     * @param name Name on the rule
     */
    Rule(String name) {
        this.name = name;
        subrules = new ArrayList<Rule>(2);
    }

    /**
     * Sets the expressions used for this Rule
     *
     * @param inputExpr inputExpr
     * @param returnExpr returnExpr
     * @throws NumberAnalyzerException if the expression could not be set due to invalid values
     */
    void setExpressions(String inputExpr, String returnExpr) throws NumberAnalyzerException {
        if (inputExpr != null && inputExpr.length() > 0) {
            inputExpression = Pattern.compile(inputExpr);
        }

        if (returnExpr != null && returnExpr.length() > 0) {
            returnExpression = ReturnExpression.createReturnExpression(returnExpr);
            if (inputExpression == null && (returnExpression instanceof GroupReturnExpression)) {
                throw new NumberAnalyzerException("Invalid input-expression for the specified return-expression");
            }
        }
    }

    /**
     * Retrieves name on this Rule
     *
     * @return name
     */
    String getName() {
        return name;
    }

    /**
     * Sets the regionCodeRuleName
     *
     * @param regionCodeRuleName region code rule
     */
    void setRegionCodeRuleName(String regionCodeRuleName) {
        this.regionCodeRuleName = regionCodeRuleName;
    }

    /**
     * Returns the regionCodeRuleName, null if not set
     *
     * @return the regionCodeRuleName
     */
    String getRegionCodeRuleName() {
        return regionCodeRuleName;
    }

    /**
     * Gets the precompiled Pattern used as input expression for this Rule
     *
     * @return inputExpr Pattern
     */
    Pattern getInputExpr() {
        return inputExpression;
    }

    /**
     * Gets the ReturnExpression object for this Rule
     *
     * @return ReturnExpression object
     */
    ReturnExpression getReturnExpr() {
        return returnExpression;
    }

    /**
     * Sets a list of subrules to the Rule, sorting them based on their priority before assigning them to the Rule.
     *
     * @param pSubrules the subrules
     */
    synchronized void setSubRules(List<Rule> pSubrules) {
        Collections.sort(pSubrules, new Comparator<Rule>() {

            @Override
            public int compare(Rule rule1, Rule rule2) {
                if(rule1.getPriority() < rule2.getPriority()) {
                    return -1;
                }else if(rule1.getPriority() > rule2.getPriority()) {
                    return 1;
                }else {
                    return 0;
                }
            }

        });
    	
    	subrules = pSubrules;
    }

    /**
     * Retrieves the list of subrules
     *
     * @return list of Rules
     */
    List<Rule> getSubRules() {
        return subrules;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
    
    public String toString() {
    	StringBuilder result = new StringBuilder();
    	
    	result.append("Main Rule name: ").append(name).append('\n');
    	result.append("Main Rule inputExpression: ").append(inputExpression).append('\n');
    	result.append("Main Rule returnExpression: ").append(returnExpression).append('\n');
    	result.append("Main Rule regionCodeRuleName: ").append(regionCodeRuleName).append('\n');

    	if (!subrules.isEmpty()) {
            Iterator<Rule> it = subrules.iterator();
            while (it.hasNext()) {
                Rule subRule = it.next();
                
                result.append("subRule name: ").append(subRule.name).append('\n');
                result.append(" subRule priority: ").append(subRule.priority).append('\n');
                result.append("  subRule inputExpression: ").append(subRule.inputExpression).append('\n');
                result.append("   subRule returnExpression: ").append(subRule.returnExpression).append('\n');
                if (subRule.regionCodeRuleName != null) {
                    result.append("    subRule regionCodeRuleName: ").append(subRule.regionCodeRuleName).append('\n');
                }
                result.append("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-").append('\n');
            }
    	} else {
            result.append("Rule contains no subrules.").append('\n');
    	}
    	
    	return result.toString();
    }
}

//DOES NOT WORK YET
/*  public boolean equals(Object o) {
    if (!(o instanceof Rule)) return false;

    Rule rule = (Rule) o;
    if (!(rule.getName().equals(name))) {
        return false;
    }

    if (inputExpression != null && rule.getInputExpr() != null) {
        if (!inputExpression.toString().equals(rule.getInputExpr().toString())) {
            return false;
        }
    }

    if (subrules.size() != rule.getSubRules().size()) {
        return false;
    }

    Iterator<Rule> it = subrules.iterator();
    Iterator<Rule> it2 = rule.getSubRules().iterator();
    while (it.hasNext()) {
        Rule r = it.next();
        Rule r2 = it2.next();
        if (!r.equals(r2)) return false;
    }

    return true;
}*/