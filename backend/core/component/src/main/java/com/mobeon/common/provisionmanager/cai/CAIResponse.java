/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.provisionmanager.cai;

import java.util.Map;
import java.util.HashMap;

/**
 * Class that interprets a CAI response. The response contains of a code and a info-message, and an optional
 * attribute list.
 *
 * @author ermmaha
 */
public class CAIResponse {
    private static final String RESPONSEIDENTIFIER = "RESP";
    private static final String SEMICOLON = ";";
    private static final String COLON = ":";

    private int code;
    private String message;

    private Map<String, String> attributes;

    /**
     * Constructor. Parses the response
     *
     * @param response
     * @throws CAIException if any syntax error in the response
     */
    public CAIResponse(String response) throws CAIException {
        parse(response);
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Retrieves attributes that was read from the response
     *
     * @return Map of attributes, null if not present in response
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    private void parse(String response) throws CAIException {
        if (response == null) throw new CAIException("Response string is null");

        int semicolonPos = response.indexOf(SEMICOLON);
        if (semicolonPos == -1) throw new CAIException("Invalid response string (missing semicolon) " + response);

        response = response.substring(0, semicolonPos);
        String[] tokens = response.split(COLON);
        if (tokens[0] != null) {
            if (tokens[0].equals(RESPONSEIDENTIFIER)) {
                if (tokens[1] != null) {
                    String codeStr = tokens[1].trim();
                    code = Integer.parseInt(codeStr);
                }
                if (tokens[2] != null) {
                    message = tokens[2].trim();
                }
                if (tokens.length > 3) {
                    getAttributesFromResponse(tokens);
                }
            } else {
                throw new CAIException("Invalid response string (wrong responseidentifier) " + response);
            }
        }
    }

    private void getAttributesFromResponse(String[] tokens) throws CAIException {
        attributes = new HashMap<String, String>();

        for (int i = 3; i < tokens.length; i++) {
            int commaPos = tokens[i].indexOf(",");
            if (commaPos == -1) {
                throw new CAIException("Invalid attribute syntax in response " + tokens[i]);
            }
            String name = tokens[i].substring(0, commaPos);
            String value = tokens[i].substring(commaPos + 1);
            attributes.put(name, value);
        }
    }
}
