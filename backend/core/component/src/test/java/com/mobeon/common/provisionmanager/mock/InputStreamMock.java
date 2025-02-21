package com.mobeon.common.provisionmanager.mock;

import junit.framework.Assert;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Documentation
 *
 * @author mande
 */
public class InputStreamMock extends InputStream  {
    private List<Object> dataList = new ArrayList<Object>();

    public int read() throws IOException {
        return 0;
    }

    public int read(byte b[]) throws IOException {
        if (dataList.size() == 0) {
            return -1;
        } else {
            Object o = dataList.remove(0);
            if (o instanceof String) {
                byte[] data = ((String)o).getBytes();
                if (b.length > data.length) {
                    System.arraycopy(data, 0, b, 0, data.length);
                    return data.length;
                } else {
                    System.arraycopy(data, 0, b, 0, b.length);
                    // Replace the remaining data
                    dataList.add(0, new String(data, b.length, data.length - b.length));
                    return b.length;
                }
            } else if (o instanceof IOException) {
                throw (IOException)o;
            } else if (o == null) {
                return -1;
            } else {
                Assert.fail("Did not expect type " + o.getClass().getCanonicalName());
                // This is only because Intellij does not identify the fail method as a final statement
                return -1;
            }
        }
    }

    public void willReturn(String data) {
        dataList.add(data);
    }

    public void willThrow(IOException exception) {
        dataList.add(exception);
    }
}
