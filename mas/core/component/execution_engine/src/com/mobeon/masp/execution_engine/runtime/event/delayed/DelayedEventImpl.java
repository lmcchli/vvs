package com.mobeon.masp.execution_engine.runtime.event.delayed;

import com.mobeon.masp.execution_engine.runtime.event.EventHubImpl;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * User: QMIAN
 * Date: 2006-okt-20
 * Time: 13:31:51
 */
public class DelayedEventImpl extends DelayedEvent {
    //NOTE: Uses same logger as EventHub to avoid altering
    //logs through this refactor.
    private static final ILogger log = ILoggerFactory.getILogger(EventHubImpl.class);

    final WeakReference<EventHubImpl> hubRef;
    final SimpleEvent event;

    public DelayedEventImpl(long delayTime, SimpleEvent ev, EventHubImpl hub) {
        super(delayTime);
        event = ev;
        hubRef = new WeakReference<EventHubImpl>(hub);
    }

    public void fireEvent() {
        EventHubImpl hub = hubRef.get();
        if (hub != null && hub.getExecutionContext().isAlive()) {
            hub.fireEvent(event);
        } else {
            if (EventHubImpl.log.isDebugEnabled()) {
                log.debug("Did not fire delayed event since context is not alive");
            }
        }
    }

    public String sendId() {
        return event.getSendId();
    }

    public ISession getSession() {
        EventHubImpl hub = hubRef.get();
        if (hub != null) {
            return hub.getExecutionContext().getSession();
        } else {
            return null;
        }
    }

    public String toString() {
        return event.toString()+" in "+getDelay(TimeUnit.MILLISECONDS);
    }
}

