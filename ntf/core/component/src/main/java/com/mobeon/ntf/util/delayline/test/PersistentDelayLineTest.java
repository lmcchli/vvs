package com.mobeon.ntf.util.delayline.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.util.delayline.Delayable;
import com.mobeon.ntf.util.delayline.PersistentDelayLine;
import com.mobeon.ntf.util.delayline.DelayItem;
import com.mobeon.ntf.util.delayline.DelayEventListener;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

public class PersistentDelayLineTest extends NtfTestCase
    implements DelayEventListener {

    private static final int[] DELAY_TIMES = { 30000, 5, };
    private static final int TIMEOUT_DELAY = 2;
    private static final int IN_INTERVAL = 2;

    public static int id = 1;

    private PersistentDelayLine dl;
    private int testStart = 0;
    private Vector otherThreadExceptions = new Vector();
    private int completions;

    public PersistentDelayLineTest(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
        dl = new PersistentDelayLine(DELAY_TIMES, TIMEOUT_DELAY, "journal", 3000000);
        dl.setListener(this);
        dl.setTimeoutListener(this);
        completions = 0;

        testStart = NtfTime.now;
    }

    public void delayCompleted(Object o) {
        /* This method is called by another thread than the text thread.
         * To forward assertion errors to the test framework, they are collected
         * in a Vector which is checked at the end of the test case.
         */
        try {
            Delayed d = (Delayed) o;
            ++completions;
            assertEquals(d._exp[d._nextTime++], NtfTime.now - testStart, 1.0);
            Logger.getLogger().logMessage("Completed " + (NtfTime.now - testStart) + "\t" + d);
        } catch (Throwable e) {
            otherThreadExceptions.add(e);
        }
    }

    private void readObjects() throws Exception {
        FileInputStream istream = new FileInputStream("journal");
        ObjectInputStream p = new ObjectInputStream(istream);
        Object o;
        try {
            while ((o = p.readObject()) != null) {
                log.logMessage("read object " + o);
            }
        } catch (EOFException e) { ; }
        finally {
            istream.close();
        }
    }

    public void testJournal () throws Throwable {
        l("testJournal");
        int i;

        Thread.sleep(5000);
        int[] del = {3, 8, 10};
        Delayed d = new Delayed(del);
        Delayed d2 = new Delayed(del);
        Delayed d3 = new Delayed(del);
        Delayed d4 = new Delayed(del);
        dl.in(d);
        dl.in(d2);
        dl.hold(d3);
        dl.cancel(new Integer(1));
        dl.in(d4);
        dl.close();
        dl = null;

        Thread.sleep(5000);
        //        readObjects();
        PersistentDelayLine dl2;
        dl2 = new PersistentDelayLine(DELAY_TIMES, TIMEOUT_DELAY, "journal", 3000000);
        dl2.setListener(this);
        dl2.setTimeoutListener(this);

        assertEquals(3, dl2.size());
        assertFalse(dl2.exists(new Integer(1)));
        assertTrue(dl2.exists(new Integer(2)));
        assertTrue(dl2.exists(new Integer(3)));
        assertTrue(dl2.exists(new Integer(4)));
    }

    private class Delayed implements Delayable, Serializable {

        public int _id;
        public int _born;
        public int[] _exp;
        public int _nextTime;

        public Delayed(int[] exp) {
            _id = id++;
            _born = NtfTime.now;
            _exp = exp;
            _nextTime = 0;
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
            return "{Delayed: " + getKey() + "\t" + _nextTime + "\t" + age() + "}";
        }
    }
}
