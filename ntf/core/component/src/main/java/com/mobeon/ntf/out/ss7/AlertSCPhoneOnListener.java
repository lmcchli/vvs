package com.mobeon.ntf.out.ss7;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.ssmg.interfaces.AlertSCHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.event.PhoneOnEvent;

public class AlertSCPhoneOnListener implements AlertSCHandler {
	private static AlertSCPhoneOnListener instance = new AlertSCPhoneOnListener();
	LogAgent log = NtfCmnLogger.getLogAgent(AlertSCPhoneOnListener.class);

	public static AlertSCPhoneOnListener getInstance() {
        if (instance == null) {
            instance = new AlertSCPhoneOnListener();
        }
        return instance;
    }

	@Override
	public void handleResult(String address) {
		if(log.isDebugEnabled()){
			log.debug("AlertSCPhoneOnListener.handleResult: received alert for " + address);
		}
		PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_OK, "Received alert sc event");
		EventRouter.get().phoneOn(phoneOnEvent);

	}

}
