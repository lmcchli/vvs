package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.OutboundCall;
import com.mobeon.masp.callmanager.Connection;
import com.mobeon.masp.callmanager.events.ConnectedEvent;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.stream.ControlToken;

import java.util.concurrent.ExecutorService;
import java.util.Set;

/**
 * @author Mikael Andersson
 *
 */
public class OutboundCallMock extends CallMock implements OutboundCall {

    private OutboundCall theCall;

    private CallManagerMock.EventType responseToCreateCall;
    private Object extraDataForResponseToCreateCall;
    private int delayBeforeResponseToCreateCall;
    private boolean earlyMediaInProgressing = false;
    private boolean sendProgressingEvent = true;

    public OutboundCallMock(ExecutorService service,
                            IApplicationManagment applicationManagement,
                            IEventDispatcher eventDispatcher,
                            CallProperties callProperties,
                            final CallManagerMock.EventType responseToCreateCall,
                            final int delayBeforeResponseToCreateCall,
                            final Object extraDataForResponseToCreateCall,
                            final CallManagerMock.EventType outboundCallEventAfterConnected,
                            final int milliSecondsUntilGeneration,
                            boolean earlyMediaInProgressing,
                            Set<Connection> farEndConnections,
                            boolean sendProgressingEvent) {
        super(service, applicationManagement, false, 0, farEndConnections,
                false, 0, 0);  // 0: delay for disconnect not implemented for outbound call
        setEventDispatcher(eventDispatcher);
        setCalledParty(callProperties.getCalledParty());
        setCallingParty(callProperties.getCallingParty());
        setCallType(callProperties.getCallType());
        this.responseToCreateCall = responseToCreateCall;
        this.extraDataForResponseToCreateCall = extraDataForResponseToCreateCall;
        this.delayBeforeResponseToCreateCall = delayBeforeResponseToCreateCall;
        this.earlyMediaInProgressing = earlyMediaInProgressing;
        this.sendProgressingEvent = sendProgressingEvent;

        theCall = this;


        service.submit(new Runnable() {
            public void run() {
                log.info("BABBA "+outboundCallEventAfterConnected);
                if(outboundCallEventAfterConnected == CallManagerMock.EventType.DISCONNECTED_EVENT){
                    log.debug("OutboundCallMock: will fire disconnected event in " + milliSecondsUntilGeneration);
                    try {
                        sleep(milliSecondsUntilGeneration);
                    } catch (InterruptedException e) {
                        Ignore.interruptedException(e);
                    }
                    log.debug("OutboundCallMock: fire disconnected event");
                    fireEvent(new DisconnectedEvent(theCall, DisconnectedEvent.Reason.NEAR_END, false));

                }
            }
        });


    }

    public void sendToken(ControlToken[] tokens) {
    }

    public void dial() {

        if(responseToCreateCall == CallManagerMock.EventType.WITHHOLD){
            log.info("MOCK: OutboundCallMock.dial: withholding the dial");
            return;
        }

        service.submit(new Runnable() {
            public void run() {

                if(sendProgressingEvent){
                    fireEvent(new ProgressingEvent(theCall, earlyMediaInProgressing));
                }
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    Ignore.interruptedException(e);
                }

                log.info("OutboundCallMock: sleeping for " + delayBeforeResponseToCreateCall + " before response to createCall");
                try {
                    sleep(delayBeforeResponseToCreateCall);
                } catch (InterruptedException e) {
                    Ignore.interruptedException(e);
                }

                switch(responseToCreateCall){
                    case FAILED_EVENT:
                        FailedEventInfo f = (FailedEventInfo) extraDataForResponseToCreateCall;
                        FailedEvent f2 = new FailedEvent(theCall,
                                f.getReason(),
                                f.getDirection(),
                                f.getMessage(),
                                f.getNetworkStatusCode() );
                        fireEvent(f2);
                        break;
                    default:
                        fireEvent(new ConnectedEvent(theCall));
                        break;
                }
            }
        });
    }
}
