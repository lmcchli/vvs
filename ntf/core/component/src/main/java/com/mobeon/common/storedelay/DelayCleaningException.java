package com.mobeon.common.storedelay;




/**
 * Exception thrown when database is cleaning/optimizing and operations cannot be done.
 * @author Mikael Eriksson
 */
public class DelayCleaningException extends DelayException
{
   /**
    * Creates a DelayCleaningException with given message.
    */
   public DelayCleaningException(String msg )
   {
      super(msg);
   }
}
