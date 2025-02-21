package com.mobeon.masp.execution_engine;

import com.mobeon.masp.util.url.TestHandlerFactory;

import java.net.URLStreamHandler;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-11
 * Time: 15:46:54
 * To change this template use File | Settings | File Templates.
 */
public class AutoTestHandlerFactory extends TestHandlerFactory {
    protected URLStreamHandler subclassURLStreamHandler(String protocol) {
        if ("autotest".equals(protocol)) {
            return new AutoTestStreamHandler();
        } else
            return null;
    }

    /**
     * Initializes URL with a StreamHandlerFactory appropriate for
     * testing purposes.
     */
    public synchronized static void initialize() {
        if (!initialized) {
            initialized = true;
            URL.setURLStreamHandlerFactory(new AutoTestHandlerFactory());
        }
    }

}
