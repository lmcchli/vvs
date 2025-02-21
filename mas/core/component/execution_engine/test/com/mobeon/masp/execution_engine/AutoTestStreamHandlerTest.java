package com.mobeon.masp.execution_engine;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.net.URL;
import java.net.URLConnection;

import com.mobeon.masp.util.url.GeneratedContentURLConnection;

/**
 * AutoTestStreamHandler Tester.
 *
 * @author <Authors name>
 * @since <pre>01/11/2007</pre>
 * @version 1.0
 */
public class AutoTestStreamHandlerTest extends TestCase {
    public AutoTestStreamHandlerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
    public void testOpenConnection() throws Exception {
        AutoTestHandlerFactory.initialize();
        URL url = new URL("test:///test/com/mobeon/masp/execution_engine/runapp/applications/vxml/catchtag/catch_11.xml");
        URLConnection conn = url.openConnection();
        if(conn == null) {
            fail("No URLConnection was returned from openConnection()");
        }
        if (conn instanceof GeneratedContentURLConnection) {
            fail("Generated content should not override existing files with the same name");
        }
    }

    public static Test suite() {
        return new TestSuite(AutoTestStreamHandlerTest.class);
    }
}
