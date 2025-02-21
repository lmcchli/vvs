package com.mobeon.common.util.content;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.StringReader;

/**
 * PageBreakingStringCounter Tester.
 *
 * @author qhast
 */
public class PageBreakingStringCounterTest extends TestCase {

    private PageBreakingStringCounter notInitializedCounter;
    private PageBreakingStringCounter initializedCounter;

    public PageBreakingStringCounterTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        notInitializedCounter = new PageBreakingStringCounter();
        initializedCounter = new PageBreakingStringCounter("pb");
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests that the constructor initialized value is returned.
     *
     * @throws Exception
     */
    public void testGetPageBreakExpression() throws Exception {
        assertEquals("PageBreaker should be null.", null, notInitializedCounter.getPageBreaker());
        assertEquals("PageBreaker should be \"pb\".", "pb", initializedCounter.getPageBreaker());
    }

    /**
     * Tests that the setter initialized (legal) value is returned.
     *
     * @throws Exception
     */
    public void testSetLegalPageBreaker() throws Exception {
        notInitializedCounter.setPageBreaker("abc");
        assertEquals("PageBreaker should be \"abc\".", "abc", notInitializedCounter.getPageBreaker());
        initializedCounter.setPageBreaker("abc");
        assertEquals("PageBreaker should be \"abc\".", "abc", initializedCounter.getPageBreaker());
    }

    /**
     * Tests that the setter initialized (illegal) value throws an exception.
     *
     * @throws Exception
     */
    public void testSetIllegalPageBreakExpression() throws Exception {
        try {
            notInitializedCounter.setPageBreaker(null);
            fail("Setting page break string to null should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

        try {
            notInitializedCounter.setPageBreaker("");
            fail("Setting page break string to \"\" should throw IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }

    }

    public void testIllegalStateCountPages() throws Exception {
        try {
            notInitializedCounter.countPages(new StringReader("ABBA is good./pb They have a cool sound./pb"));
            fail("Not Initialized Counter should throw an IllegalStateException!");
        } catch (IllegalStateException e) {
            //OK
        }
    }

    public void testIllegalArgumentCountPages() throws Exception {
        try {
            initializedCounter.countPages(null);
            fail("Calling Initialized Counter with null should throw an IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            //OK
        }
    }

    public void testCountPages() throws Exception {
        assertEquals(2, initializedCounter.countPages(new StringReader("/lf ABBA is good./pb They have a cool sound./pb")));
        assertEquals(2, initializedCounter.countPages(new StringReader("/lf ABBA is good./pb They have a cool sound./pb Rock on!")));
        assertEquals(2, initializedCounter.countPages(new StringReader("/pb ABBA is good. pb They have a cool sound. /lf Rockers.")));
        assertEquals(0, initializedCounter.countPages(new StringReader("ABBA is good. They have a cool sound. Rockers.")));
        assertEquals(2, initializedCounter.countPages(new StringReader("/pb/pb")));

        initializedCounter.setPageBreaker("/lf");
        assertEquals(1, initializedCounter.countPages(new StringReader("/lf ABBA is good./pb They have a cool sound./pb")));
        assertEquals(2, initializedCounter.countPages(new StringReader("/lf ABBA is good. pb /lf They have a cool sound./pb Rock on!")));
        assertEquals(1, initializedCounter.countPages(new StringReader("/pb ABBA is good./pb They have a cool sound. /lf Rockers.")));
        assertEquals(0, initializedCounter.countPages(new StringReader("ABBA is good. They have a cool sound. Rockers.")));
        assertEquals(3, initializedCounter.countPages(new StringReader("/lf/pb/pblf/lf//lf")));
    }

    /**
     * Tests that toString not crashes (even if Page break Expression no has been initialized).
     *
     * @throws Exception
     */
    public void testToString() throws Exception {
        PageBreakingStringCounter pc = new PageBreakingStringCounter();
        pc.toString();
        pc = new PageBreakingStringCounter("x");
        pc.toString();
    }

    public static Test suite() {
        return new TestSuite(PageBreakingStringCounterTest.class);
    }
}
