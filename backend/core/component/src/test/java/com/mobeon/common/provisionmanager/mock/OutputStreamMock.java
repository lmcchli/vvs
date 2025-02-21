package com.mobeon.common.provisionmanager.mock;

import org.jmock.core.Verifiable;

import java.io.OutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Assert;

/**
 * Documentation
 *
 * @author mande
 */
public class OutputStreamMock extends OutputStream implements Verifiable {
    private List<String> expectations = new ArrayList<String>();

    public void write(int b) throws IOException {
    }

    public void write(byte b[]) throws IOException {
        String data = new String(b);
        Assert.assertFalse("OutputStreamMock expected expectation", expectations.isEmpty());
        Assert.assertEquals(expectations.remove(0), data);
    }

    public void verify() {
        Assert.assertEquals("OutputStreamMock expectations should be empty", 0, expectations.size());
    }

    public void expects(String expectation) {
        expectations.add(expectation);
    }
}
