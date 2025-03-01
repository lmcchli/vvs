/*
 * @author:     Brett Buckingham
 * @author:     Last modified by: $Author: mranga $
 * @version:    $Date: 2006/10/05 16:53:32 $ $Revision: 1.1 $
 *
 * This source code has been contributed to the public domain.
 */

package gov.nist.javax.sip.stack;

import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * A subclass of TimerTask which runs TimerTask code within a try/catch block to avoid killing the SIPTransactionStack
 * timer thread.  Note: subclasses MUST not override run(); instead they should override runTask().
 * 
 * @author Brett Buckingham
 *
 */
public abstract class SIPStackTimerTask extends TimerTask
{
  private static final Logger log = Logger.getLogger(SIPStackTimerTask.class);

  /// Implements code to be run when the SIPStackTimerTask is executed.
  protected abstract void runTask();

  /// The run() method is final to ensure that all subclasses inherit the exception handling.
  public final void run()
  {
    try
    {
      runTask();
    } catch (Throwable e)
    {
        if (log.isDebugEnabled())
            log.debug("SIP stack timer task failed due to exception.", e);
    }
  }
}
