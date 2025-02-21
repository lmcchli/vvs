package com.mobeon.common.provisionmanager.mock;

import org.jmock.core.Verifiable;

import java.net.SocketImpl;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.io.*;

/**
 * Mock Socket class used for testing purposes
 *
 * @author mande
 */
public class SocketMock extends SocketImpl implements Verifiable {
    private InputStreamMock mockInputStream = new InputStreamMock();
    private OutputStreamMock mockOutputStream = new OutputStreamMock();
    private IOException throwOnClose;

    protected void create(boolean stream) throws IOException {
    }

    protected void connect(String host, int port) throws IOException {
    }

    protected void connect(InetAddress address, int port) throws IOException {
    }

    protected void connect(SocketAddress address, int timeout) throws IOException {
    }

    protected void bind(InetAddress host, int port) throws IOException {
    }

    protected void listen(int backlog) throws IOException {
    }

    protected void accept(SocketImpl s) throws IOException {
    }

    protected InputStream getInputStream() throws IOException {
        return mockInputStream;
    }

    protected OutputStream getOutputStream() throws IOException {
        return mockOutputStream;
    }

    protected int available() throws IOException {
        return 0;
    }

    protected void close() throws IOException {
        if (throwOnClose != null) {
            throw throwOnClose;
        }
    }

    protected void sendUrgentData(int data) throws IOException {
    }

    public void setOption(int optID, Object value) throws SocketException {
    }

    public Object getOption(int optID) throws SocketException {
        return null;
    }

    public void verify() {
        mockOutputStream.verify();
    }

    public void willReturn(String data) {
        mockInputStream.willReturn(data);
    }

    public void expects(String data) {
        mockOutputStream.expects(data);
    }

    public void willThrow(IOException exception) {
        mockInputStream.willThrow(exception);
    }

    public void setThrowOnClose(IOException throwOnClose) {
        this.throwOnClose = throwOnClose;
    }
}
