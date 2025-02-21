package com.mobeon.masp.execution_engine.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-feb-21
 * Time: 11:49:12
 * To change this template use File | Settings | File Templates.
 */
public class EventRuleUtil {
    ILogger log = ILoggerFactory.getILogger(getClass());

    final static Map<EventRule.Category, EventRule.Category> notMapping = new EnumMap<EventRule.Category, EventRule.Category>(EventRule.Category.class);
    final static Map<EventRule.Category, Boolean> toBooleanMapping = new EnumMap<EventRule.Category, Boolean>(EventRule.Category.class);

    static {
        notMapping.put(EventRule.Category.FALSE, EventRule.Category.TRUE);
        notMapping.put(EventRule.Category.TRUE, EventRule.Category.FALSE);
        notMapping.put(EventRule.Category.INVALID, EventRule.Category.INVALID);
        toBooleanMapping.put(EventRule.Category.TRUE, Boolean.TRUE);
        toBooleanMapping.put(EventRule.Category.FALSE, Boolean.FALSE);
        toBooleanMapping.put(EventRule.Category.INVALID, Boolean.FALSE);
    }

    final protected EventRule.Category logIfNotFalse(EventRule.Category category, Event event) {
        if (category != EventRule.Category.FALSE)
            if (log.isDebugEnabled())
                log.debug("Expression " + toString() + " evaluated as " + category + " for event " + event);
        return category;
    }

    final protected boolean logIfValid(boolean valid, Event event) {
        if (valid)
            if (log.isDebugEnabled())
                log.debug("Expression " + toString() + " evaluated TRUE for event " + event);
        return valid;
    }

    final EventRule.Category categoryNot(EventRule.Category r) {
        return notMapping.get(r);
    }

    final boolean collapse(EventRule.Category r) {
        return toBooleanMapping.get(r);
    }
}
