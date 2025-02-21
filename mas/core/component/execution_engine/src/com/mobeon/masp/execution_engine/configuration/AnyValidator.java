package com.mobeon.masp.execution_engine.configuration;

/**
 * @author Mikael Andersson
 */
public class AnyValidator implements Validator {
    public boolean isValid(Object value) {
        return true;
    }
}
