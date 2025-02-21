package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.RtspConnectionMock;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.OutputStreamMock;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.InputStreamMock;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class RtspConnectionTest extends TestCase {
    protected static ILogger logger = ILoggerFactory.getILogger(RtspConnectionTest.class);
    private RtspConnectionMock rtspConnection;

    public void setUp() {
        rtspConnection = new RtspConnectionMock();
    }

    public void testCreateSession() {
        assertNotNull("Check input stream not null", rtspConnection.getInputStream());
        assertNotNull("Check output stream not null", rtspConnection.getOutputStream());
    }

    public void testOutputStream() {
        OutputStreamMock output = (OutputStreamMock)rtspConnection.getOutputStream();

        try {
            output.write("APSKALLE".getBytes());
        } catch (IOException e) {
            fail("Got exception ...");
        }
        assertEquals("APSKALLE", output.getBuffer());
    }

    public void testInputStream() {
        InputStreamMock input = (InputStreamMock)rtspConnection.getInputStream();
        byte[] data = null;
        String message = "APSKALLE";
        input.setBuffer(message);
        try {
            int nOfBytes = input.available();
            data = new byte[nOfBytes];
            assertEquals(message.length(), input.read(data));
        } catch (IOException e) {
            fail("Got exception ...");
        }
        assertEquals("Verify message string", message, new String(data));
    }

    public void testSocketTimeout() throws IOException {
        ServerSocket server = null;
        Socket client = null;

        try {
            server = new ServerSocket(4711);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not create server socket.");
        }
        RtspConnection connection = new RtspConnectionImpl("localhost", 4711);
        connection.open();
        try {
            client = server.accept();
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not get client socket.");
        }

        client.getOutputStream().write(12);
        assertEquals(12, connection.getInputStream().read());
        connection.setReceiveTimeout(100);
        boolean gotException = false;
        try {
            int dummy = connection.getInputStream().read();
        } catch (SocketTimeoutException e) {
            gotException = true;
        }
        assert(gotException);
    }
}
