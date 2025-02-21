package com.mobeon.masp.execution_engine.configuration;

import java.io.File;
import java.net.URI;

/**
 * @author Mikael Andersson
 */

public class ToFileURIConverter extends ToURIConverter {

    public boolean requireDir() {
        return false;
    }

    public Object convert(Object value) {
        Object result = super.convert(value);
        if (!isFileURI(result,requireDir()))
            result = super.convert(new File("").toURI().resolve(value.toString()));
        if (!isFileURI(result,requireDir())) {
            result = null;
        }
        return result;
    }

    public static  boolean isFileURI(Object result,boolean requireDir) {
        boolean isFileURI = result != null && "file".equals(((URI) result).getScheme());
        if(isFileURI && requireDir) {
            URI uri = ((URI) result);
            isFileURI = uri.getPath() != null && uri.getPath().endsWith("/");
        }
        return isFileURI;
    }
}
