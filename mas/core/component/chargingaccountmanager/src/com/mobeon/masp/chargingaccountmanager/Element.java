package com.mobeon.masp.chargingaccountmanager;

/**
 * Models an element in the UICP spec.
 * <p/>
 * Date: 2007-dec-05
 *
 * @author emahagl
 */
public class Element {
    private String name;
    private ParameterType type;
    // valid values
    // request or response

    /**
     * Constructor.
     *
     * @param name
     * @param type
     */
    public Element(String name, ParameterType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Constructor.
     *
     * @param name
     * @param typeStr string, integer, boolean or date
     */
    public Element(String name, String typeStr) {
        this.name = name;
        if (typeStr.equalsIgnoreCase("integer")) type = ParameterType.Integer;
        else if (typeStr.equalsIgnoreCase("boolean")) type = ParameterType.Boolean;
        else if (typeStr.equalsIgnoreCase("date")) type = ParameterType.Date;
        else
            type = ParameterType.String;
    }

    /**
     * Returns name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ParameterType
     *
     * @return the ParameterType
     */
    public ParameterType getType() {
        return type;
    }
}
