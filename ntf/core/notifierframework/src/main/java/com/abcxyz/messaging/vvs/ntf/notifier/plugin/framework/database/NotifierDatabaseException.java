/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database;

/**
 * The NotifierDatabaseException class signals that an error has occurred while performing an action on a profile in the MiO database.
 * 
 */
public class NotifierDatabaseException extends Exception {

    /**
     * Constructs an NotifierDatabaseException with the specified detail message.
     * @param message the message containing the details of the error
     */
    public NotifierDatabaseException(String message) {
        super(message);
    }

    /**
     * Constructs an NotifierDatabaseException with the specified exception and a detail message of <tt>(e==null ? null : e.toString())</tt> 
     * which typically contains the class and detail message of <tt>e</tt>.
     * @param e the exception
     */
    public NotifierDatabaseException(Exception e) {
        super(e);
    }
}
