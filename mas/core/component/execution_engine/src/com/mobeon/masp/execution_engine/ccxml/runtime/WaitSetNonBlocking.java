package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.runtime.EventProcessor;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Detour;
import com.mobeon.masp.execution_engine.runtime.event.rule.SimpleEventRule;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRule;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.products.WaitSetProduct;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 8, 2006
 * Time: 5:04:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class WaitSetNonBlocking implements Detour {

    private CCXMLExecutionContext ec;
    static ILogger logger = ILoggerFactory.getILogger(WaitSetNonBlocking.class);

    public WaitSetNonBlocking(EventProcessor eventProcessor, CCXMLExecutionContext ec, IConfigurationManager manager){
        eventProcessor.addDetour(this);
        this.ec = ec;
    }
    protected LinkedList<WaitInstance> waitQueue = new LinkedList<WaitInstance>();

    /**
     * TR HN87685
     * 
     * detoutrEvent now scans the whole waitQueue instead of simply peeking at the first element.
     * It is possible to have more than one WaitInstance in the waitingQueue.
     * Ex: While setting up an outbound call (SIP 200 OK has not been received) MAS receives
     * SIP 183 asking for early media. At that point we will have two entries in the waitQueue
     *  1 - Supervision for the call setup (SIP INVITE up to 200 OK) waiting for event connection.connected
     *  2 - Supervision of the early media 
     *  
     *  It has been decided to scan all instances of the waitQueue and process multiple matches.
     *  Not stopping after the first match. 
     *  In some extreme cases, we might end up cancelling a time supervision that should not be cancelled.
     *  This depends on the ccxml application.
     */
    public synchronized boolean detourEvent(Event event) {
        
        if (waitQueue.size() <= 0) return false;
        
        if (waitQueue.size() > 1) {
           logger.debug("There are " + waitQueue.size() + " WaitInstance in the queue before processing: " + event);
        }
        
        Iterator<WaitInstance> it = waitQueue.iterator();
        while(it.hasNext()) {
            WaitInstance instance = it.next();
            if (instance != null) {
                if (!instance.shouldTerminate(event)) {
                    if (instance.tryMatch(event)) {
                        it.remove();
                        onMatchedEvent(instance, event);
                    }
                } else {
                    it.remove();
                }
            }
        }
        return false;
    }
   
    public synchronized boolean detourGlobalEvent(Event event) {
        return false;
    }

    public synchronized void waitFor(
    boolean forAll, List<Entry> entries, List<Entry> terminateEntriess, ExecutionContext ec, WaitSetProduct e, boolean stopWaitingOnMatch, boolean injectOnMatch) {
        waitQueue.add(new WaitInstance(forAll, entries,terminateEntriess, ec, e, stopWaitingOnMatch, injectOnMatch));
    }

    private boolean onMatchedEvent(WaitInstance instance, Event event) {
        if(event instanceof SimpleEvent){
            List<Entry> entries = instance.getEntries();
            if(entries.size() > 0){
                Entry e = entries.get(0);

                // "es" is what we inserted as "toFire" below
                SimpleEvent es = (SimpleEvent) e.getEvent();
                TestEventGenerator.generateEvent(TestEvent.WAITSET_REQUIRED,event);
                ec.getEventHub().cancel(es.getSendId(), null);
            }
        }
        return false;
    }

    public void addWaitFor(String eventToFire, String messageForFiredEvent, int waitTime, Callable toInvokeWhenDelivered, Connection connection, String ... eventNames){
        // wait for the "eventName" event
        EventRule rule = new SimpleEventRule(true, eventNames);
        List<Entry> entries = new ArrayList<Entry>();
        // Fire the "toFire" event with a delay. It will be cancelled if we get the "eventName" event eventually.
        CCXMLEvent toFire = CCXMLEvent.create(eventToFire, messageForFiredEvent + ", session "+ec.getSessionId() , ec, connection, DebugInfo.getInstance(), toInvokeWhenDelivered);

        entries.add(new Entry(1,rule, toFire));
        waitFor(true, entries, new ArrayList<Entry>(), ec, null, true, true);
        EventHub eh = ec.getEventHub();
        if(logger.isDebugEnabled()){
            logger.debug("Firing delayed event (" + toFire + ") with ID: "+toFire.getSendId()+", delay "+waitTime);
        }

        eh.fireContextEvent(toFire, waitTime);
    }
}
