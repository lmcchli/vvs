package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.trafficeventsender.ITrafficEventSender;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.Restrictions;

/**
 * Mock object for the traffic event sender.
 */
public class TrafficEventSenderMock extends BaseMock implements ITrafficEventSender {

    public TrafficEventSenderMock ()
    {
        super();
        log.info ("MOCK: TrafficEventSenderMock.TrafficEventSenderMock");
    }

    public void reportTrafficEvent(TrafficEvent trafficEvent)
    {
        log.info ("MOCK: TrafficEventSenderMock.reportTrafficEvent");
        log.info ("MOCK: TrafficEventSenderMock.reportTrafficEvent unimplemented!");
        log.info ("MOCK: TrafficEventSenderMock.reportTrafficEvent "+trafficEvent.getName());
    }

    public void reportTrafficEvent(TrafficEvent trafficEvent, Restrictions restrictions)
    {
        log.info ("MOCK: TrafficEventSenderMock.reportTrafficEvent");
        log.info ("MOCK: TrafficEventSenderMock.reportTrafficEvent unimplemented!");
        log.info ("MOCK: TrafficEventSenderMock.reportTrafficEvent "+trafficEvent.getName());
    }
}
