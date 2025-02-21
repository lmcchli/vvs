package com.mobeon.masp.execution_engine.runapp.mock;

import java.io.IOException;
import java.text.ParseException;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.sip.InvalidArgumentException;

import com.mobeon.masp.callmanager.Connection;
import com.mobeon.masp.callmanager.SubscribeCall;
import com.mobeon.masp.callmanager.events.ConnectedEvent;
import com.mobeon.masp.callmanager.events.SubscribeEvent;
import com.mobeon.masp.execution_engine.IApplicationManagment;

public class SubscribeCallMock extends CallMock implements SubscribeCall {

	public SubscribeCallMock(ExecutorService service,
			IApplicationManagment applicationManagement,
			boolean withHoldDisconnectAttempt,
			int delayBeforeResponseToDisconnect,
			Set<Connection> farEndConnections,
			boolean sendPlayFailedAfterDelay, int delayBeforePlayFailed,
			int inboundBitRate) {
		super(service, applicationManagement, withHoldDisconnectAttempt,
				delayBeforeResponseToDisconnect, farEndConnections,
				sendPlayFailedAfterDelay, delayBeforePlayFailed, inboundBitRate);
	}

	@Override
	public void accept() {
		fireEvent(new ConnectedEvent(this));
		
	}

	

	@Override
	public int getExpires() {
		return 86400;
	}

	@Override
	public String getUserAgentNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reject(String reason) {
		// TODO Auto-generated method stub

	}
	
    /**
     * Start the loaded service
     */
    public void startSubscribe() {
        api.start();
        fireEvent(new SubscribeEvent(this));
        //fireEvent(new AlertingEvent(this));
    }

	@Override
	public String getDialogInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIsInitial() {
		// TODO Auto-generated method stub
		return null;
	}


}
