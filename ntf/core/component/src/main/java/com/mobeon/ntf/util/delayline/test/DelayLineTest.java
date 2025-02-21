package com.mobeon.ntf.util.delayline.test;

import com.mobeon.ntf.test.NtfTestCase;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.time.NtfTime;
import com.mobeon.ntf.util.delayline.Delayable;
import com.mobeon.ntf.util.delayline.DelayLine;
import com.mobeon.ntf.util.delayline.DelayItem;
import com.mobeon.ntf.util.delayline.DelayEventListener;
import java.util.*;

public class DelayLineTest extends NtfTestCase
    implements DelayEventListener {

    private static int id = 1;
    private static final int[] DELAY_TIMES = { 3, 5, };
    private static final int TIMEOUT_DELAY = 2;
    private static final int IN_INTERVAL = 2;

    private DelayLine dl;
    private int testStart = 0;
    private Vector otherThreadExceptions = new Vector();
    private int completions;

    public DelayLineTest(String name) {
	super(name);
    }

    protected void setUp() {
        id = 1;
        dl = new DelayLine(DELAY_TIMES, TIMEOUT_DELAY);
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

    public void testStore () throws Throwable {
        l("testStore");
        int i;

        int[] del = {3, 8, 10};
        Delayed d = new Delayed(del);
        Delayed d2 = new Delayed(del);
        Delayed d3 = new Delayed(del);
        dl.in(d);
        assertTrue(dl.exists(new Integer(1)));
        assertFalse(dl.exists(new Integer(2)));
        assertFalse(dl.exists(new Integer(3)));
        assertEquals(1, dl.size());
        dl.in(d2);
        assertTrue(dl.exists(new Integer(1)));
        assertTrue(dl.exists(new Integer(2)));
        assertFalse(dl.exists(new Integer(3)));
        assertEquals(2, dl.size());
        dl.in(d3);
        assertTrue(dl.exists(new Integer(1)));
        assertTrue(dl.exists(new Integer(2)));
        assertTrue(dl.exists(new Integer(3)));
        assertEquals(3, dl.size());

        dl.cancel(new Integer(3));
        assertTrue(dl.exists(new Integer(1)));
        assertTrue(dl.exists(new Integer(2)));
        assertFalse(dl.exists(new Integer(3)));
        assertEquals(2, dl.size());
        dl.cancel(new Integer(1));
        assertFalse(dl.exists(new Integer(1)));
        assertTrue(dl.exists(new Integer(2)));
        assertFalse(dl.exists(new Integer(3)));
        assertEquals(1, dl.size());
        dl.cancel(new Integer(2));
        assertFalse(dl.exists(new Integer(1)));
        assertFalse(dl.exists(new Integer(2)));
        assertFalse(dl.exists(new Integer(3)));
        assertEquals(0, dl.size());

        assertEquals(0, completions);
    }

    public void testBasicDelay () throws Throwable {
        l("testBasicDelay");
        int i;

        int[] del = {3, 8, 10};
        int[] del2 = {4, 9, 11};
        Delayed d = new Delayed(del);
        dl.in(d);
        Thread.sleep(1000);
        dl.in(new Delayed(del2));


        try { Thread.sleep(13000); } catch (InterruptedException e) { ; }
        assertEquals(del.length, d._nextTime);
        for (i = 0; i < otherThreadExceptions.size(); i++) {
            throw (Throwable) (otherThreadExceptions.elementAt(i));
        }

        assertEquals(6, completions);
    }

    public void testShortDelay () throws Throwable {
        l("testShortDelay");
        int i;

        int[] del0 = {1,1};
        int[] del = {1, 2, 3};
        dl = new DelayLine(del0, 1);
        dl.setListener(this);
        dl.setTimeoutListener(this);

        Delayed d = new Delayed(del);
        dl.in(d);
        assertTrue(dl.exists(new Integer(1)));
        assertEquals(1, dl.size());

        try { Thread.sleep(5000); } catch (InterruptedException e) { ; }
        assertEquals(del.length, d._nextTime);
        for (i = 0; i < otherThreadExceptions.size(); i++) {
            throw (Throwable) (otherThreadExceptions.elementAt(i));
        }

        assertEquals(3, completions);
    }

    public void testReplace () throws Throwable {
        l("testReplace");
        int i;

        int[] del = {3, 8, 10};
        int[] del2 = {7, 12, 14};
        Delayed d = new Delayed(del);
        dl.in(d);
        Thread.sleep(4000);
        d._exp = del2; //Update expected times
        d._nextTime = 0;
        assertTrue(dl.exists(new Integer(1)));
        assertEquals(1, dl.size());
        dl.in(d);
        assertTrue(dl.exists(new Integer(1)));
        assertEquals(1, dl.size());


        try { Thread.sleep(13000); } catch (InterruptedException e) { ; }
        assertEquals(del.length, d._nextTime);
        for (i = 0; i < otherThreadExceptions.size(); i++) {
            throw (Throwable) (otherThreadExceptions.elementAt(i));
        }

        assertEquals(4, completions);
    }

    public void testHold () throws Throwable {
        l("testHold");
        int i;

        int[] del = {13, 18, 20};
        Delayed d = new Delayed(del);
        dl.hold(d);
        assertTrue(dl.exists(new Integer(1)));
        assertEquals(1, dl.size());

        Thread.sleep(10000);
        assertEquals(0, completions);

        dl.start(new Integer(1));

        try { Thread.sleep(13000); } catch (InterruptedException e) { ; }
        assertEquals(del.length, d._nextTime);
        for (i = 0; i < otherThreadExceptions.size(); i++) {
            throw (Throwable) (otherThreadExceptions.elementAt(i));
        }

        assertEquals(3, completions);
    }

    public void testHoldAndReplace () throws Throwable {
        l("testHoldAndReplace");
        int i;

        int[] del = {3, 8, 10};
        int[] del2 = {7, 12, 14};
        Delayed d = new Delayed(del);
        dl.hold(d);
        Thread.sleep(4000);
        d._exp = del2; //Update expected times
        d._nextTime = 0;
        assertTrue(dl.exists(new Integer(1)));
        assertEquals(1, dl.size());
        dl.in(d);
        assertTrue(dl.exists(new Integer(1)));
        assertEquals(1, dl.size());


        try { Thread.sleep(13000); } catch (InterruptedException e) { ; }
        assertEquals(del.length, d._nextTime);
        for (i = 0; i < otherThreadExceptions.size(); i++) {
            throw (Throwable) (otherThreadExceptions.elementAt(i));
        }

        assertEquals(3, completions);
    }

    public void testReplaceByHold () throws Throwable {
        l("testReplaceByHold");
        int i;

        int[] del = {3, 8, 10};
        int[] del2 = {17, 22, 24};
        Delayed d = new Delayed(del);
        dl.in(d);
        assertTrue(dl.exists(new Integer(1)));
        assertEquals(1, dl.size());
        Thread.sleep(4000);
        d._nextTime = 0;
        assertEquals(1, completions);
        d._exp = del2; //Update expected times
        dl.hold(d);
        assertTrue(dl.exists(new Integer(1)));
        assertEquals(1, dl.size());

        Thread.sleep(10000);
        assertEquals(1, completions);

        dl.start(new Integer(1));

        try { Thread.sleep(13000); } catch (InterruptedException e) { ; }
        assertEquals(del.length, d._nextTime);
        for (i = 0; i < otherThreadExceptions.size(); i++) {
            throw (Throwable) (otherThreadExceptions.elementAt(i));
        }

        assertEquals(5, completions);
    }

    private class Delayed implements Delayable {
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
