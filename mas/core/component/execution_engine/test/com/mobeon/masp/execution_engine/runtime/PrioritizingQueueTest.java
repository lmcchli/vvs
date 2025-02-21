/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventImpl;
import junit.framework.Test;
import junit.framework.TestSuite;

public class PrioritizingQueueTest extends Case {

    public static Test suite() {
        return new TestSuite(PrioritizingQueueTest.class);
    }

    public static class PriorityEvent extends SimpleEventImpl {
        public PriorityEvent(String event, int priority) {
            super(event,null);
            setPriority(priority);
        }

        public String toString() {
            if (getExecutingURI() != null) {
                return getEvent()+"{ uri="+
                getExecutingURI() +
                ", from="+getDebugInfo()+
                ", priority="+priority()+
                " }";
            } else {
                return getEvent()+"{ uri=<unknown>"+
                ", from="+getDebugInfo()+
                ", priority="+priority()+
                " }";
            }
        }

    }

    public PrioritizingQueueTest(String test) {
        super(test);
    }

    public void testOffer() throws Exception {
        PriorityEvent p0 = new PriorityEvent("prio0", 0);
        PriorityEvent p0b = new PriorityEvent("prio0", 0);
        PriorityEvent p1 = new PriorityEvent("prio1", 1);
        PriorityEvent p2 = new PriorityEvent("prio2", 2);

        PrioritizingQueue<PriorityEvent> pq;
        //Test adding prio 0 event first
        pq = new PrioritizingQueue<PriorityEvent>();
        validateOfferPoll(pq,p0, p0);

        //Test adding prio != 0 event first
        pq = new PrioritizingQueue<PriorityEvent>();
        validateOfferPoll(pq,p1, p1);
        validateOfferPoll(pq,p0, p0);
        //Test ordering of several prio 0 elements
        pq.offer(p0b);
        pq.offer(p0);
        pq.offer(p0);
        pq.offer(p0);
        validatePoll(pq,p0b);

        //Validate ordering with emptying queues.
        pq.offer(p1);
        pq.offer(p2);
        validatePoll(pq,p2);
        validatePoll(pq,p1);
        validatePoll(pq,p0);

    }

    private void validatePoll(PrioritizingQueue<PriorityEvent> pq,PriorityEvent expected) {
        PriorityEvent q;
        if ((q = pq.poll()) != expected)
            die("Poll returned " + q + " but " + expected + " was expected ");
    }

    private void validateOfferPoll(PrioritizingQueue<PriorityEvent> pq,PriorityEvent p, PriorityEvent expected) {
        pq.offer(p);
        PriorityEvent q;
        if ((q = pq.poll()) != expected)
            die("Poll returned " + q + " but " + expected + " was expected ");
    }
}