package com.abcxyz.services.moip.masevent;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.scheduler.handling.AbstractEventStatusListener;
import com.abcxyz.messaging.scheduler.handling.AppliEventHandler;
import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.scheduler.handling.AppliEventOperations;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;

public abstract class AbstractEventHandler extends AbstractEventStatusListener {

    private AppliEventHandler eventHandler;

    public void start(RetryEventInfo info) {
        //instantiate scheduler event handler that handles automatic retries
        eventHandler = new AppliEventHandler(info, this);
    }

	public AppliEventOperations getEventOperator() {
		return eventHandler;
	}

	public AppliEventInfo scheduleEvent(String id, String type, Properties properties) {
		long when = eventHandler.getFirstRetryTimer() + System.currentTimeMillis();
		AppliEventInfo info = eventHandler.scheduleEvent(when, id, type, properties);
		return info;
	}

	public AppliEventInfo scheduleEvent(long when, String id, String type, Properties properties) {
		AppliEventInfo info = eventHandler.scheduleEvent(when, id, type, properties);
		return info;
	}

	public AppliEventInfo scheduleEvent(long when, String id, EventTypes type, Properties properties) {
		return scheduleEvent(when, id, type.getName(), properties);
	}

	public void cancelEvent(AppliEventInfo event) {
		eventHandler.cancelEvent(event);
	}
}
