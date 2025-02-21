package com.mobeon.masp.execution_engine.configuration;

import com.mobeon.masp.util.Ignore;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Mikael Andersson
 */

public class ToURIConverter implements Converter {

    public Object convert(Object value) {
        Object result = null;
        if(value != null)
            try {
                result = new URI(value.toString());
            } catch (URISyntaxException e) {
                Ignore.uriSyntaxException(e);
            }
        return result;
    }
}
