package com.mobeon.masp.chargingaccountmanager;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Date: 2007-nov-27
 *
 * @author emahagl
 */
public class ChargingAccountResponse implements IChargingAccountResponse {

    protected int responseCode;
    private HashMap<String, Object> parameters = new HashMap<String, Object>();

    public ChargingAccountResponse(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Returns the responsecode
     *
     * @return the responsecode
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Sets parameters
     *
     * @param parameters
     */
    public void setParameters(HashMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Retrieves a parameter from the response
     *
     * @param name
     * @return the value of the parameter, null if not found
     */
    public String getParameter(String name) {
        Object value = parameters.get(name);
        if (value != null) {
            return convert(value);
        }

        // lookup the param
        return getParameterFromMap(name, parameters);
    }

    private String getParameterFromMap(String name, HashMap<String, Object> map) {
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Object value = map.get(key);

            if (value instanceof HashMap) {
                String s = getParameterFromMap(name, (HashMap<String, Object>) value);
                if (s != null) return s;
            }

            if (key.equals(name)) {
                return convert(value);
            }
        }
        return null;
    }

    private String convert(Object value) {
        if (value instanceof Object[]) {
            return convert((Object[]) value);
        }

        if (value instanceof Date) {
            return DateUtil.getStringFromDate((Date) value);
        }
        return value.toString();
    }

    // Temporary fix to serialize array values (may contain structs...)
    private String convert(Object[] values) {
        String buf = "";
        for (int i = 0; i < values.length; i++) {
            Object o = values[i];
            if (o instanceof HashMap) {
                buf += convert((HashMap) o);
                if (i < values.length - 1) buf += ",";
            }
        }
        return buf;
    }

    private String convert(HashMap value) {
        String buf = value.toString();
        return buf;
    }
}

