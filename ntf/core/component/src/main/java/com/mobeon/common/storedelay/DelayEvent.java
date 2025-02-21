package com.mobeon.common.storedelay;

/**
 * Class used to provide more information when notifying about DelayInfo
 * Objects of this class contains an event name and an event code.
 * Clients can define subclasses of this class or use own event names
 * and codes.
 */
public class DelayEvent {

    private String name;
    private int    code;

    /** Creates a new instance of DelayEvent */
    public DelayEvent(String name, int code)
    {
        this.name = name;
        this.code = code;
    }

    /**
     * String representation, mainly for debugging purposes
     */
    public String toString()
    {
        return name + "(" + code + ")";
    }

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Getter for property code.
     * @return Value of property code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Setter for property code.
     * @param code New value of property code.
     */
    public void setCode(int code) {
        this.code = code;
    }

}
