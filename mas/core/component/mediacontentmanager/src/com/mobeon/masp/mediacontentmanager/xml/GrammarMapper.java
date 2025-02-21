/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.xml;

import com.mobeon.masp.mediacontentmanager.grammar.RulesRecord;
import com.mobeon.masp.mediacontentmanager.grammar.NumberRule;
import com.mobeon.masp.mediacontentmanager.grammar.NumberRuleCondition;
import com.mobeon.masp.mediacontentmanager.grammar.ActionElementFactory;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.IActionElement;
import com.mobeon.masp.mediacontentmanager.IActionElementFactory;
import com.mobeon.masp.mediacontentmanager.INumberRule;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.io.CharArrayWriter;

import org.xml.sax.Attributes;

/**
 * XML-to-object mapper that maps a Grammar-XML to
 * a list of {@link com.mobeon.masp.mediacontentmanager.grammar.RulesRecord} objects.
 *
 * @author mmawi
 */
public class GrammarMapper extends SaxMapper<List<RulesRecord>> {
    /**
     * List of mapped <code>RulesRecord</code>s.
     */
    private List<RulesRecord> rulesRecordList;

    /**
     * The <code>RulesRecord</code> currently being created.
     */
    private RulesRecord currentRulesRecord;

    /**
     * The <code>NumberRule</code> currently being created.
     */
    private INumberRule currentNumberRule;

    /**
     * The <code>NumberRuleCondition</code> currently being created.
     */
    private NumberRuleCondition currentNumberRuleCondition;

    /**
     * The <code>IActionElement</code> currently being created.
     */
    private IActionElement currentActionElement;

    private IActionElementFactory actionElementFactory =
            new ActionElementFactory();

    /**
     * Logger used.
     */
    private static final ILogger LOGGER =
            ILoggerFactory.getILogger(GrammarMapper.class);

    /**
     * Returns the list of type {@link RulesRecord}s, mapped from the XML.
     *
     * @return Mapped object.
     */
    public List<RulesRecord> getMappedObject() {
        return rulesRecordList;
    }

    /**
     * Delegates to subclasses to create the tag-tracker
     * network for its' specific XML-format.
     *
     * @return The tracker.
     */
    public TagTracker createTagTrackerNetwork() {
        TagTracker root = new TagTracker() {
            public void onDeactivate() {
                // The root will be deactivated when
                // parsing a new docuemt begins
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("parsing new document, creating new list of RulesRecords.");
                }
                rulesRecordList = new ArrayList<RulesRecord>();
            }
        };

        // action for grammar/rule
        TagTracker ruleTracker = createRuleTracker(root);
        // action for grammar/rule/condition
        TagTracker conditionTracker = createConditionTracker(ruleTracker);
        // action for grammar/rule/condition/action
        createActionTracker(conditionTracker);

        return root;
    }

    /**
     * Creates action for /grammar/rule/condition/action
     *
     * @param conditionTracker The parent tracker
     *
     * @return The tracker for the action.
     */
    private TagTracker createActionTracker(TagTracker conditionTracker) {
        TagTracker actionTracker = new TagTracker() {

            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }
                String type = attr.getValue("type").toLowerCase();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\ttype=" + type);
                }

                // Create IActionElement and add to NumberRuleCondition
                IActionElement.ActionType actionType;
                try {
                    actionType =
                            IActionElement.ActionType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    throw new SaxMapperException(
                            "Failed to map ActionType from string="
                                    + type, e);
                }
                currentActionElement = actionElementFactory.create(actionType);
            }

            public void onEnd(String namespaceURI,
                              String localName,
                              String qName,
                              CharArrayWriter contents ){
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localName + ">");
                }
                char[] characters = contents.toCharArray();

                switch (currentActionElement.getType()) {
                    case mediafile:
                        if (characters.length > 0) {
                            String reference = new String(characters);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("\treference:" + reference);
                            }
                            currentActionElement.setMediaFileName(reference);
                        }
                        break;
                    case skip:
                        //Do noting
                        break;
                    case select:
                        //Do nothing
                        break;
                    case swap:
                        if (characters.length > 0) {
                            String swapStr = new String(characters);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("\tswap:" + swapStr);
                            }
                            int swapValue;
                            try {
                                swapValue = Integer.valueOf(swapStr);
                            } catch (IllegalArgumentException e) {
                                throw new SaxMapperException(
                                        "Failed to map int from string="
                                                + swapStr, e);
                            }
                            currentActionElement.setSwapValue(swapValue);
                        }
                        break;
                }
                currentNumberRuleCondition.addActionElement(currentActionElement);
            }
        };

        conditionTracker.track("condition/action", actionTracker);
        return actionTracker;
    }

    /**
     *
     * @param ruleTracker The parent tracker.
     * @return A tracker for condition elements
     */
    private TagTracker createConditionTracker(TagTracker ruleTracker) {
        TagTracker conditionTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }

                // Get divisor
                String divisorStr = attr.getValue("divisor");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tdivisor=" + divisorStr);
                }
                Long divisor;
                try {
                    divisor = new Long(divisorStr);
                } catch (NumberFormatException e) {
                    throw new SaxMapperException("Failed to get value of divisor, not a number: "
                            + divisorStr, e);
                }
                // Check if there is a rule with this divisor already in currentRulesRecord,
                // then the condition will be added to this.
                // Otherwise a new, empty rule with this divisor is created.
                if ((currentNumberRule = currentRulesRecord.getNumberRule(divisor)) == null) {
                    currentNumberRule = new NumberRule(divisor);
                    currentRulesRecord.addNumberRule(currentNumberRule);
                }

                // Now get the rest of the condition.
                // Get quotientFrom
                String quotientFromStr = attr.getValue("quotientFrom");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tquotientFrom=" + quotientFromStr);
                }
                Long quotientFrom;
                try {
                    quotientFrom = new Long(quotientFromStr);
                } catch (NumberFormatException e) {
                    throw new SaxMapperException("Failed to get value of quotientFrom, not a number: "
                            + quotientFromStr, e);
                }

                // Get quotientTo
                String quotientToStr = attr.getValue("quotientTo");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tquotientTo=" + quotientToStr);
                }
                Long quotientTo;
                try {
                    quotientTo = new Long(quotientToStr);
                } catch (NumberFormatException e) {
                    throw new SaxMapperException("Failed to get value of quotientTo, not a number: "
                            + quotientToStr, e);
                }

                // Get remainderFrom
                String remainderFromStr = attr.getValue("remainderFrom");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tremainderFrom=" + remainderFromStr);
                }
                Long remainderFrom;
                try {
                    remainderFrom = new Long(remainderFromStr);
                } catch (NumberFormatException e) {
                    throw new SaxMapperException("Failed to get value of remainderFrom, not a number: "
                            + remainderFromStr, e);
                }

                // Get remainderTo
                String remainderToStr = attr.getValue("remainderTo");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tremainderTo=" + remainderToStr);
                }
                Long remainderTo;
                try {
                    remainderTo = new Long(remainderToStr);
                } catch (NumberFormatException e) {
                    throw new SaxMapperException("Failed to get value of remainderTo, not a number: "
                            + remainderToStr, e);
                }

                // Get terminal
                String terminalStr = attr.getValue("terminal");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tterminal=" + terminalStr);
                }
                Boolean terminal;
                terminal = Boolean.valueOf(terminalStr);

                // Get atomic
                String atomicStr = attr.getValue("atomic");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tatomic=" + atomicStr);
                }
                Boolean atomic;
                atomic = Boolean.valueOf(atomicStr);

                // Get divide
                String divideStr = attr.getValue("divide");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tdivide=" + divideStr);
                }
                Boolean divide;
                divide = Boolean.valueOf(divideStr);

                // Create the condition and add to the NumberRule
                currentNumberRuleCondition = new NumberRuleCondition(atomic, quotientFrom,
                        quotientTo, remainderFrom, remainderTo, terminal, divide);
                currentNumberRule.addCondition(currentNumberRuleCondition);
            }
        };
        ruleTracker.track("condition", conditionTracker);
        return ruleTracker;
    }

    /**
     * Creates action for tag /grammar/rule
     *
     * @param root The parent tracker.
     * @return The tracker created for the action.
     */
    private TagTracker createRuleTracker(TagTracker root) {
        TagTracker ruleTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }

                // Get type
                String type = attr.getValue("type");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\ttype=" + type);
                }
                IMediaQualifier.QualiferType ruleType;
                try {
                    ruleType = IMediaQualifier.QualiferType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    throw new SaxMapperException("Failed to create RulesRecord with ruleType:"
                            + type + " as the ruleType is not a member of IMediaQualifier.QualiferType "
                            + "enumeration. ", e);
                }
                currentRulesRecord = new RulesRecord(ruleType);

                // Get gender
                // Gender may be a combined string, "Male,Female,None", parse the
                // string and add each gender to the RulesRecord.
                String gender = attr.getValue("gender");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tgender=" + gender);
                }

                StringTokenizer st = new StringTokenizer(gender, ",");
                IMediaQualifier.Gender ruleGender;
                while (st.hasMoreTokens()) {
                    String genderStr = st.nextToken();
                    try {
                        ruleGender = IMediaQualifier.Gender.valueOf(genderStr);
                    } catch (IllegalArgumentException e) {
                        throw new SaxMapperException("Failed to create RulesRecord as the gender:"
                                + genderStr + " is not a member of the IMediaQualifier.Gender "
                                + "enumeration. ", e);
                    }
                    currentRulesRecord.addGender(ruleGender);
                }
                rulesRecordList.add(currentRulesRecord);
            }
        };
        root.track("grammar/rule", ruleTracker);
        return ruleTracker;
    }
}
