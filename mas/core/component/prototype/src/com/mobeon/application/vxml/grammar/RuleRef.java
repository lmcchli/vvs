package com.mobeon.application.vxml.grammar;

/**
 * User: kalle
 * Date: Feb 17, 2005
 * Time: 8:34:54 PM
 */
public class RuleRef  //todo: everything when more grammar needed.
                implements RuleContent,
                           ItemContent
{
    /**
     * The special attribute denotes rulenames which are disallowed for redefinition within a grammar. Allowable values are:
     *
     *   * $NULL -- A rule which is automatically matched
     *   * $VOID --  A rule that can never be spoken
     *   * $GARBAGE -- A rule that may match any speech up until the next rule match
     *
     * Note that either special or uri may be defined for the Ruleref element, but not both.
     */
    private int special; // todo: (NULL|VOID|GARBAGE)

    /**
     * The uri attribute specifies the URIValidator of the external grammar that is to be referenced. Note that existing rules within the same file can be referenced:
     * <ruleref uri="#MYRULE"/>
     * or rules from an external file may be referenced:
     *
     * <ruleref uri="MyGrammar.xml#MYRULE"
     */
    private String uri;



}
