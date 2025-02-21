package com.mobeon.masp.execution_engine.runtime.ecma;

/**
 * @author Kenneth Selin
 */
public class ECMAExecutorException extends Exception{
    private String errorDescription;

    public ECMAExecutorException(String errorDescription) {
        StringBuffer buffer = new StringBuffer("ECMAExecutorException: ");
        buffer.append(errorDescription);
        this.errorDescription = buffer.toString();
    }

    public String toString(){
        return errorDescription;
    }
}
