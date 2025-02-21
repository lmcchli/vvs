package com.mobeon.common.util.executor;

/**
 * Exception wrapper class used for wrapping exceptions for which a retry could be desired.
 */
public class RetryException extends Exception {
    private Exception cause;

    public RetryException(Exception cause) {
        this.cause = cause;
    }

    /**
     * Overridden <code>getCause</code> method returning checked exception (so the rethrowing in the call method
     * throws a checked exception)
     *
     * @return the checked exception wrapped in the <code>RetryException</code>
     */
    public Exception getCause() {
        return cause;
    }
}
