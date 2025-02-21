package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

/**
 * Disconnects the referenced connection when called.
 * <p/>
 * User: ermkese
 * Date: May 15, 2006
 * Time: 6:26:53 PM
 */
public class Disconnecter implements Callable {
    private WeakReference<Connection> connectionRef;
    static ILogger logger = ILoggerFactory.getILogger(Disconnecter.class);

    public Disconnecter(Connection conn) {
        connectionRef = new WeakReference<Connection>(conn);
    }

    public Object call() throws Exception {
        TestEventGenerator.generateEvent(TestEvent.DISCONNECTER_DISCONNECT,connectionRef);
        if (logger.isInfoEnabled()) {
            logger.info("Disconnecting connection " + connectionRef);
        }
        Connection connection = connectionRef.get();
        if (connection != null) {
            connection.disconnect();
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}

