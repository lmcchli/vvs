package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.runapp.ApplicationBasicTestCase;
import com.mobeon.masp.execution_engine.runapp.TestAppender;
import com.mobeon.masp.execution_engine.ApplicationExecutionImpl;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.session.ISessionFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;

/**
 * The base class for all mock objects.
 *
 * @author Tomas Stenlund
 */
public class BaseMock {

    private ISessionFactory sessionFactory;

    /**
     * The log object.
     */
    protected ILogger log = ILoggerFactory.getILogger (getClass());
    /**
     * Holds the application manager for this mock object.
     */

    /**
     * The constructor for the base mock object.
     */
    public BaseMock ()
    {
        // Nothing to do
        log = ILoggerFactory.getILogger (getClass());

    }

    public ISessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(ISessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
