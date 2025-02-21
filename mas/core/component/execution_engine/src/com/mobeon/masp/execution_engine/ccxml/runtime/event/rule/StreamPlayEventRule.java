package com.mobeon.masp.execution_engine.ccxml.runtime.event.rule;

import com.mobeon.masp.execution_engine.runtime.event.rule.ClassRule;
import com.mobeon.masp.stream.PlayFailedEvent;
import com.mobeon.masp.stream.PlayFinishedEvent;

/**
 * @author Mikael Andersson
 */
public class StreamPlayEventRule extends ClassRule {
    public StreamPlayEventRule() {
        super(PlayFinishedEvent.class, PlayFailedEvent.class);
    }
}
