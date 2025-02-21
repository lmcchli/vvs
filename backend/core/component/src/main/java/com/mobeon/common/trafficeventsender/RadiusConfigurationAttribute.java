/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender;

/**
 * Models a Radius attribute in the Radius configuration
 *
 * @author ermmaha
 */
public class RadiusConfigurationAttribute {
    private static final String TYPE_STRING = "s";
    private static final String TYPE_INTEGER = "i";
    private static final String TYPE_ENUM = "e";

    private int number;

    /**
     * 0 radius, 1 vendor
     */
    private AttributeType attributeType = AttributeType.RADIUS;

    /**
     * S string, I int
     */
    private DataType dataType = DataType.INT;

    /**
     * Constructor.
     *
     * @param number
     * @param type
     * @param dataTypeStr
     */
    RadiusConfigurationAttribute(int number, int type, String dataTypeStr) {
        this.number = number;
        if (type == 0) attributeType = AttributeType.RADIUS;
        else if (type == 1) attributeType = AttributeType.VENDOR;

        if (dataTypeStr != null) {
            if (dataTypeStr.equalsIgnoreCase(TYPE_STRING)) dataType = DataType.STRING;
            else if (dataTypeStr.equalsIgnoreCase(TYPE_INTEGER) || dataTypeStr.equalsIgnoreCase(TYPE_ENUM)) dataType = DataType.INT;
        }
    }

    int getNumber() {
        return number;
    }

    AttributeType getAttributeType() {
        return attributeType;
    }

    DataType getDataType() {
        return dataType;
    }

    enum AttributeType {
        RADIUS, VENDOR
    }

    enum DataType {
        STRING, INT
    }
}
