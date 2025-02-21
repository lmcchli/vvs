package com.mobeon.masp.execution_engine.ccxml.runtime.event.rule;

import com.mobeon.masp.execution_engine.runtime.event.rule.ClassRule;
import com.mobeon.masp.stream.RecordFailedEvent;
import com.mobeon.masp.stream.RecordFinishedEvent;

/**
 * @author Mikael Andersson
 */
public class StreamRecordEventRule extends ClassRule {
    public StreamRecordEventRule() {
        super(RecordFailedEvent.class,
                RecordFinishedEvent.class);
    }
}
