package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IGroup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains a configured rule on valid values on an attribute.
 *
 * @author emahagl
 */
public class AttributeValueControl {
    private static final String RE_GROUP_NAME = "re";
    private static final String RANGE_GROUP_NAME = "range";
    private static final String EXPR_ATTR_NAME = "expr";
    private static final String MIN_ATTR_NAME = "min";
    private static final String MAX_ATTR_NAME = "max";

    private Pattern pattern;
    private int minRange = 0;
    private int maxRange = 0;

    /**
     * Constructor.
     *
     * @param syntaxGroup    to get configured syntax information
     * @param patternControl
     * @throws MetaDataException if there is something wrong with the configured syntax group.
     */
    public AttributeValueControl(IGroup syntaxGroup, boolean patternControl) throws MetaDataException {
        if (patternControl) {
            try {
                IGroup reGroup = syntaxGroup.getGroup(RE_GROUP_NAME);
                String regParam = reGroup.getString(EXPR_ATTR_NAME);
                pattern = Pattern.compile(regParam);
            } catch (ConfigurationException e) {
                throw new MetaDataException("Could not create AttributeValueControl " + e.getMessage());
            }
        } else {

            try {
                IGroup rangeGroup = syntaxGroup.getGroup(RANGE_GROUP_NAME);
                minRange = rangeGroup.getInteger(MIN_ATTR_NAME);
                maxRange = rangeGroup.getInteger(MAX_ATTR_NAME);

                if (maxRange < minRange) {
                    throw new MetaDataException("Could not create AttributeValueControl, minRange=" + minRange + " is larger than maxRange=" + maxRange);
                }

            } catch (ConfigurationException e) {
                throw new MetaDataException("Could not create AttributeValueControl " + e.getMessage());
            }
        }
    }

    /**
     * Checks a value against the rules setup in this class.
     *
     * @param value
     * @param attrName
     * @throws InvalidAttributeValueException
     */
    public void checkValue(String value, String attrName) throws InvalidAttributeValueException {
        if (pattern != null) {
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                throw new InvalidAttributeValueException("Value " + value + " is not a valid value for " + attrName);
            }
        } else {
            try {
                int intValue = Integer.parseInt(value);
                if (intValue < minRange || intValue > maxRange) {
                    throw new InvalidAttributeValueException("Value " + value + " is not a valid value for " + attrName);
                }
            } catch (NumberFormatException e) {
                throw new InvalidAttributeValueException("Invalid value " + e.getMessage());
            }
        }
    }
}
