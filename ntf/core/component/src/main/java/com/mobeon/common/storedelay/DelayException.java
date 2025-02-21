/*
 * DelayException.java
 *
 * Created on den 12 augusti 2004, 15:46
 */

package com.mobeon.common.storedelay;

/**
 * Exception for the storedelay package.
 * All exceptions, except runtime exceptions, thrown to clients outside the
 * package is a DelayException or a subclass.
 */
public class DelayException extends java.lang.Exception
{
    private Throwable inner;
    /**
     * Creates a DelayException that has been caused by an origin exception.
     */
    public DelayException(String msg, Throwable cause)
    {
        super(msg + (cause == null ? "" : " Reason : " + cause.getMessage() ));
        this.inner = cause;
    }


    /**
     * Constructs an instance of <code>DelayException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DelayException(String msg)
    {
        super(msg);
    }

    public Throwable getInner()
    {
        return inner;
    }

    public void printStackTrace(java.io.PrintWriter pw)
    {
        super.printStackTrace(pw);
        if (inner != null) {
            pw.println("** Reason **");
            inner.printStackTrace(pw);
        }
    }

    public void printStackTrace(java.io.PrintStream ps)
    {
        super.printStackTrace(ps);
        if (inner != null) {
            ps.println("** Reason **");
            inner.printStackTrace(ps);
        }
    }






}
