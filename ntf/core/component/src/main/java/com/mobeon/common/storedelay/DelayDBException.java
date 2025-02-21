package com.mobeon.common.storedelay;

import java.sql.SQLException;


/**
 * DelayException subclass that encapsulates problems with the
 * database. The nested exception is often an SQLException but
 * not always. (E.g. it is a ClassCastException when the
 * driver class could not be found.)
 */
public class DelayDBException extends DelayException
{
   /**
    * Creates an exception with given message and cause.
    * @param msg Informational message
    * @param cause Original  exception
    */
    public DelayDBException(String msg, Throwable cause)
   {
      super(msg, cause);
   }
}
