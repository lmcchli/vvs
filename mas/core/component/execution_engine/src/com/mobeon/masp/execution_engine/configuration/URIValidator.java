/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import java.net.URI;
import java.net.URISyntaxException;

public class URIValidator implements Validator {
    public boolean isValid(Object value) {
        try {
            return value != null && new URI(value.toString()) != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
