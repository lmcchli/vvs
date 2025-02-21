package com.mobeon.ntf.util.delayline.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.util.delayline.Delayable;
import com.mobeon.ntf.util.delayline.PersistentDelayItem;
import com.mobeon.ntf.util.delayline.DelayItem;
import com.mobeon.ntf.util.delayline.DelayEventListener;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class PersistentDelayItemTest extends NtfTestCase {

    private static final int[] DELAY_TIMES = { 3, 5, 7, 8, };
    private static final int IN_INTERVAL = 2;

    public PersistentDelayItemTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    private void saveObjects(Object[] o) throws Exception {
        FileOutputStream ostream = new FileOutputStream("objects");
        ObjectOutputStream p = new ObjectOutputStream(ostream);
        log.logMessage("Writing length = " + o.length);
        p.writeInt(o.length);
        for (int i = 0; i < o.length; i++) {
            p.writeObject(o[i]);
        }
        p.flush();
        ostream.close();
    }

    private Object[] readObjects() throws Exception {
        FileInputStream istream = new FileInputStream("objects");
        ObjectInputStream p = new ObjectInputStream(istream);
        int length = p.readInt();
        log.logMessage("File should have " + length + " objects");
        Object[] result = new Object[length];
        for (int i = 0; i < length; i++) {
            result[i] = p.readObject();
            log.logMessage("read object " + i + " class " + result[i].getClass().getName());
        }
        istream.close();
        return result;
    }

    public void test () throws Throwable {
        l("test");
        int count = 5;
        int i;

        PersistentDelayItem[] d = new PersistentDelayItem[count];
        PersistentDelayItem p;
        for (i = 0; i < count; i++) {
            p = new PersistentDelayItem(new Delayed(2 * i, 2 * i + 1));
            p.setQueueNumber((byte) i);
            p.setReQueue((i % 2) == 0);
            p.setOnHold((i % 2) != 0);
            p.setOutTime(i + 100);
            p.setArrivalTime(i + 1000);
            p.setLastArrivalTime(i + 10000);

            d[i] = p;
        }
        saveObjects(d);

        d = null;
        Object[] obj = readObjects();
        assertEquals(count, obj.length);
        PersistentDelayItem it;
        for (i = 0; i < count; i++) {
            assertEquals("com.mobeon.ntf.util.delayline.PersistentDelayItem", obj[i].getClass().getName());
            it = (PersistentDelayItem) obj[i];
            assertEquals(i, it.getQueueNumber());
            assertEquals((i % 2 != 0), it.isOnHold());
            assertEquals(0, it.getOutTime());
            assertEquals(i + 1000, it.getArrivalTime());
            assertEquals(i + 10000, it.getLastArrivalTime());
            assertEquals("com.mobeon.ntf.test.Delayed", it.getItem().getClass().getName());
            log.logMessage("\n" + obj[i]);
        }
    }
}

class Delayed implements Delayable, Serializable {
    public static int id = 1;

    public int _id;
    public int _born;
    public int _del;
    public int _exp;

    public Delayed(int exp, int del) {
        _id = id++;
        _born = NtfTime.now;
        _del = del;
        _exp = exp;
    }

    public Object getKey() {
        return new Integer(_id);
    }

    public int getTransactionId() {
        return _id;
    }

    public int age() {
        return NtfTime.now - _born;
    }

    public String toString() {
        return "{Delayed: " + getKey() + ":" + getTransactionId() + "\t" + _exp + "\t" + _del + ":" + age() + "}";
    }
}
