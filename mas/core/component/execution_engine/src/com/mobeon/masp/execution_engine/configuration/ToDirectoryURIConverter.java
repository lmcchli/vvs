package com.mobeon.masp.execution_engine.configuration;

/**
 * @author Mikael Andersson
 */
public class ToDirectoryURIConverter extends ToFileURIConverter {
    public boolean requireDir() {
        return true;
    }
}
