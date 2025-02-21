package com.mobeon.masp.chargingaccountmanager;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Date: 2007-nov-27
 *
 * @author emahagl
 */
public class ChargingAccountRequest implements IChargingAccountRequest {

    private static int transactionId;
    private String name;
    private HashMap<String, Object> parameters = new HashMap<String, Object>();

    public ChargingAccountRequest() {
        parameters.put("originNodeType", "IMP");
        parameters.put("originHostName", "MAS");
        parameters.put("originTransactionID", "" + transactionId++);
        parameters.put("originTimeStamp", new Date());
    }

    /**
     * Sets name on the request
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a parameter to the request.
     *
     * @param name
     * @param value
     * @throws ChargingAccountException
     */
    public void addParameter(String name, String value) throws ChargingAccountException {
        ChargingAccountManagerConfiguration c = ChargingAccountManagerConfiguration.getInstance();

        ElementGroup elementGroup = c.getElementGroup(name);
        if (elementGroup != null) {
            setElementToGroup(name, value, elementGroup);
        } else {
            Element element = c.getElement(name);
            // ToDo remove when (if) String elements are put in the list
            ParameterType type = ParameterType.String;
            if (element != null) {
                type = element.getType();
            }
            parameters.put(name, getValueFromType(value, type));
        }
    }

    /**
     * Adds a parameter struct into an array
     *
     * @param arrayName name of the array
     * @param params
     * @param values
     * @throws ChargingAccountException
     */
    public void addArrayParameter(String arrayName, String[] params, String[] values)
            throws ChargingAccountException {
        ChargingAccountManagerConfiguration c = ChargingAccountManagerConfiguration.getInstance();

        Hashtable<String, Object> struct = new Hashtable<String, Object>();

        for (int i = 0; i < params.length; i++) {
            Element element = c.getElement(params[i]);
            ParameterType type = ParameterType.String;
            if (element != null) {
                type = element.getType();
            }
            struct.put(params[i], getValueFromType(values[i], type));
        }

        addArrayParameter(arrayName, struct);
    }

    private void addArrayParameter(String arrayName, Hashtable<String, Object> struct) {
        Object arrayValue = parameters.get(arrayName);
        if (arrayValue != null) {
            Object[] array = (Object[]) arrayValue;
            Object[] newArray = new Object[array.length + 1];
            System.arraycopy(array, 0, newArray, 0, array.length);
            newArray[array.length] = struct;
            parameters.put(arrayName, newArray);
        } else {
            Object[] array = new Object[1];
            array[0] = struct;
            parameters.put(arrayName, array);
        }
    }

    private void setElementToGroup(String name, String value, ElementGroup elementGroup) throws ChargingAccountException {
        String parentName = elementGroup.getParent();
        Object parentValue = parameters.get(parentName);
        Element element = null;
        Element[] elements = elementGroup.getMemberElements();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getName().equals(name)) {
                element = elements[i];
                break;
            }
        }

        if (element == null) return;

        if (parentValue != null) {
            Hashtable<String, Object> struct = (Hashtable<String, Object>) parentValue;
            struct.put(name, getValueFromType(value, element.getType()));
        } else {
            Hashtable<String, Object> struct = new Hashtable<String, Object>();
            struct.put(name, getValueFromType(value, element.getType()));
            parameters.put(parentName, struct);
        }
    }

    private Object getValueFromType(String value, ParameterType type) throws ChargingAccountException {
        if (type == ParameterType.Integer) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new ChargingAccountException(e);
            }
        } else if (type == ParameterType.Boolean) {
            if (value.equalsIgnoreCase("false")) return Boolean.FALSE;
            else if (value.equalsIgnoreCase("true")) return Boolean.TRUE;
            else
                throw new ChargingAccountException("Invalid datatype " + value + " for " + name);
        } else if (type == ParameterType.Date) {
            return DateUtil.getDateFromValue(value);
        }
        // String here
        return value;
    }

    /**
     * Returns name on the request
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }
}
