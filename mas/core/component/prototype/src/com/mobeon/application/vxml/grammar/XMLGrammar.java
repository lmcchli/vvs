package com.mobeon.application.vxml.grammar;

/**
 * User: kalle
 * Date: Feb 17, 2005
 * Time: 8:05:38 PM
 */
public class XMLGrammar
        implements Grammar
{
    private Rule rule;

    public Rule getRule()
    {
        return rule;
    }

    public void setRule(Rule rule)
    {
        this.rule = rule;
    }
}
