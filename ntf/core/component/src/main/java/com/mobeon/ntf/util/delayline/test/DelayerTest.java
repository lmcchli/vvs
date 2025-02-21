package com.mobeon.ntf.util.delayline.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.util.delayline.Delayable;
import com.mobeon.ntf.util.delayline.Delayer;
import com.mobeon.ntf.util.delayline.DelayItem;
import com.mobeon.ntf.util.delayline.DelayEventListener;
import java.util.*;

public class DelayerTest extends NtfTestCase
    implements DelayEventListener {

    private static int id = 1;
    private static final int[] DELAY_TIMES = { 3, 5, 7, 8, };
    private static final int IN_INTERVAL = 2;

    private Delayer[] del;
    private int testStart = 0;
    private Vector otherThreadExceptions = new Vector();

    public DelayerTest(String name) {
	super(name);
        del = new Delayer[DELAY_TIMES.length];
        for (int i = 0; i < DELAY_TIMES.length; i++) {
            del[i] = new Delayer(Thread.currentThread().getThreadGroup(),
                                 "DelayerTest-" + i,
                                 DELAY_TIMES[i],
                                 this);
        }
    }

    protected void setUp() {
        int i;

        for (i = 0; i < DELAY_TIMES.length; i++) {
            assertEquals(DELAY_TIMES[i], del[i].getDelayTime());
        }

        testStart = NtfTime.now;
    }

    public void delayCompleted(Object o) {
        /* This method is called by another thread than the text thread.
         * To forward assertion errors to the test framework, they are collected
         * in a Vector which is checked at the end of the test case.
         */
        try {
            Delayed d = (Delayed) ((DelayItem) o).getItem();
            Logger.getLogger().logMessage("Completed " + (NtfTime.now - testStart) + "\t" + d);
            assertEquals(d._del, d.age(), 1.0);
            assertEquals(d._exp, NtfTime.now - testStart, 1.0);
        } catch (Throwable e) {
            otherThreadExceptions.add(e);
        }
    }

    public void test () throws Throwable {
        l("test");
        int i;

	for (int inserts = 0; inserts < 5; inserts++) {
            for (i = 0; i < DELAY_TIMES.length; i++) {
                del[i].add(new DelayItem(new Delayed(inserts * IN_INTERVAL + DELAY_TIMES[i], DELAY_TIMES[i])));
            }
	    Thread.sleep(IN_INTERVAL * 1000);
	}
        try { Thread.sleep((DELAY_TIMES[DELAY_TIMES.length -1] + 2) * 1000); } catch (InterruptedException e) { ; }
        for (i = 0; i < otherThreadExceptions.size(); i++) {
            throw (Throwable) (otherThreadExceptions.elementAt(i));
        }
    }

    private class Delayed implements Delayable {
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
}
