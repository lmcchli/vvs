/*
 * CommandException.java
 *
 * Created on den 26 augusti 2004, 13:34
 */

package com.mobeon.common.commands;

/**
 * Base for all exceptions thrown to clients by the commands package.
 */
public class CommandException extends java.lang.Exception
{


    /**
     * Constructs an instance of <code>CommandException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CommandException(String msg)
    {
        super(msg);
    }
}
