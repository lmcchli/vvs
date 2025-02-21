/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.callmanager.events.*;
import com.mobeon.masp.execution_engine.ccxml.runtime.Bridge;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.EndSessionEvent;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.LifecycleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.stream.PlayFailedEvent;
import com.mobeon.masp.stream.PlayFinishedEvent;
import com.mobeon.masp.stream.RecordFailedEvent;
import com.mobeon.masp.stream.RecordFinishedEvent;

import java.util.HashMap;
import java.util.Map;

public class ExternalEventTranslator {
    private EventSourceManager manager;


    public ExternalEventTranslator(EventSourceManager manager) {
        this.manager = manager;
    }

    public void translate(Event event) {
        Class c = event.getClass();
        ExternalEventAdapter ea = mapAdapterByClass.get(c);
        if (ea != null) {
            ea.perform(manager, event);
        }
    }

    public static final Map<Class, ExternalEventAdapter> mapAdapterByClass = new HashMap<Class, ExternalEventAdapter>();

    static {
        mapAdapterByClass.put(ConnectedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                Call call = ((ConnectedEvent) related).getCall();
                manager.safeFindConnectionByCall(call)
                        .receiveEvent(Connection.AutomatonEvent.EVENT_CONNECTED, related);
                manager.safeFindConnectionByCall(call).onConnected();
            }
        });
        

        mapAdapterByClass.put(DisconnectedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                manager.safeFindConnectionByCall(((DisconnectedEvent) related).getCall()).
                        receiveEvent(Connection.AutomatonEvent.EVENT_DISCONNECTED, related);
            }
        });

        mapAdapterByClass.put(FailedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                manager.safeFindConnectionByCall(((FailedEvent) related).getCall()).
                        receiveEvent(Connection.AutomatonEvent.EVENT_FAILED, related);
            }
        });

        mapAdapterByClass.put(PlayFinishedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                PlayFinishedEvent realEvent = (PlayFinishedEvent) related;
                Connection connection = manager.safeFindConnection(realEvent.getId().toString());
                connection.onPlayEnded();
                connection.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FINISHED,
                                related);
            }

        });

        mapAdapterByClass.put(PlayFailedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                PlayFailedEvent realEvent = (PlayFailedEvent) related;
                Connection connection = manager.safeFindConnection(realEvent.getId().toString());
                connection.onPlayEnded();
                connection.receiveEvent(Connection.AutomatonEvent.EVENT_PLAY_FAILED,
                                related);
            }

        });

        mapAdapterByClass.put(RecordFailedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                RecordFailedEvent realEvent = (RecordFailedEvent) related;
                Connection connection = manager.safeFindConnection(realEvent.getId().toString());
                connection.onRecordEnded();
                connection.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FAILED,
                                related);
            }
        });
        mapAdapterByClass.put(RecordFinishedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                RecordFinishedEvent realEvent = (RecordFinishedEvent) related;
                Connection connection = manager.safeFindConnection(realEvent.getId().toString());
                connection.onRecordEnded();
                connection.receiveEvent(Connection.AutomatonEvent.EVENT_RECORD_FINISHED,
                                related);
            }
        });
        mapAdapterByClass.put(ErrorEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                ErrorEvent realEvent = (ErrorEvent) related;
                manager.safeFindConnectionByCall(realEvent.getCall())
                        .receiveEvent(Connection.AutomatonEvent.EVENT_ERROR,
                                related);
            }
        });

        mapAdapterByClass.put(AlertingEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                AlertingEvent realEvent = (AlertingEvent) related;
                manager.createConnection(realEvent.getCall(), false);
                Connection connection = manager.safeFindConnectionByCall(realEvent.getCall());
                connection.onAlerting();
                connection.receiveEvent(Connection.AutomatonEvent.EVENT_ALERTING, related);
            }
        });

        mapAdapterByClass.put(ProgressingEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                ProgressingEvent realEvent;
                realEvent = (ProgressingEvent) related;
                Connection connection = manager.safeFindConnectionByCall(realEvent.getCall());
                connection.onProgressing(realEvent);
                connection.receiveEvent(Connection.AutomatonEvent.EVENT_PROGRESSING,
                                related);
            }
        });

        mapAdapterByClass.put(JoinedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                JoinedEvent joined = (JoinedEvent) related;

                Connection conn1 = manager.findConnectionByCall(joined.getFirstCall());
                Connection conn2 = manager.findConnectionByCall(joined.getSecondCall());
                if (conn1 != null && conn2 != null) {
                    Bridge b = manager.findBridge(conn1, conn2);
                    if (b != null)
                        b.onJoin();
                } else {
                    manager.logInvalidEvent("JoinedEvent", joined, conn1, conn2);
                }
            }
        });

        mapAdapterByClass.put(UnjoinedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                UnjoinedEvent unjoined = (UnjoinedEvent) related;

                Connection conn1 = manager.findConnectionByCall(unjoined.getFirstCall());
                Connection conn2 = manager.findConnectionByCall(unjoined.getSecondCall());
                if (conn1 != null && conn2 != null) {
                    Bridge b = manager.findBridge(conn1, conn2);
                    if (b != null) {
                        manager.forgetBridge(b);
                        b.onUnjoin();
                    }
                } else {
                    manager.logInvalidEvent("JoinedEvent", unjoined, conn1, conn2);
                }
            }
        });

        mapAdapterByClass.put(EarlyMediaAvailableEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                EarlyMediaAvailableEvent earlyMedia = (EarlyMediaAvailableEvent) related;
                Connection conn1 = manager.safeFindConnectionByCall(earlyMedia.getCall());
                conn1.onEarlyMedia();
            }
        });

        mapAdapterByClass.put(EarlyMediaFailedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                EarlyMediaFailedEvent earlyMedia = (EarlyMediaFailedEvent) related;

                Connection conn1 = manager.safeFindConnectionByCall(earlyMedia.getCall());
                conn1.onEarlyMediaFailed();
            }
        });

        mapAdapterByClass.put(NotAllowedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                manager.getExecutionContext().getEventHub().fireContextEvent(Constants.Event.ERROR_NOTALLOWED, "", DebugInfo.getInstance());
            }
        });


        mapAdapterByClass.put(JoinErrorEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                JoinErrorEvent error = (JoinErrorEvent) related;

                Connection conn1 = manager.findConnectionByCall(error.getFirstCall());
                Connection conn2 = manager.findConnectionByCall(error.getSecondCall());
                if (conn1 != null && conn2 != null) {
                    Bridge b = manager.findBridge(conn1, conn2);
                    if (b != null)
                        b.onJoinError(error.getErrorMessage());
                } else {
                    manager.logInvalidEvent("JoinedEvent", error, conn1);
                }
            }
        });
        mapAdapterByClass.put(UnjoinErrorEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                UnjoinErrorEvent error = (UnjoinErrorEvent) related;

                Connection conn1 = manager.findConnectionByCall(error.getFirstCall());
                Connection conn2 = manager.findConnectionByCall(error.getSecondCall());
                if (conn1 != null && conn2 != null) {
                    Bridge b = manager.findBridge(conn1, conn2);
                    if (b != null)
                        b.onUnjoinError(error.getErrorMessage());
                }
            }
        });

        mapAdapterByClass.put(LifecycleEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                if (LifecycleEvent.START.equals(related)) {
                } else {
                    //TODO: Shutdown not implemented
                }
            }
        });

        mapAdapterByClass.put(EndSessionEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                CCXMLExecutionContext executionContext = manager.getExecutionContext();
                EventHub eventHub = executionContext.getEventHub();
                SimpleEvent e = CCXMLEvent.create(Constants.Event.CCXML_KILL, "Kill requested by platform", executionContext, null, DebugInfo.getInstance());
                eventHub.fireContextEvent(e);
            }
        });

        mapAdapterByClass.put(SipMessageResponseEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                CCXMLExecutionContext executionContext = manager.getExecutionContext();
                EventHub eventHub = executionContext.getEventHub();
                SimpleEvent e = CCXMLEvent.create(Constants.Event.MOBEON_PLATFORM_SIPMESSAGERESPONSEEVENT,
                        executionContext, null, DebugInfo.getInstance(), related);
                eventHub.fireContextEvent(e);
            }
        });
        
        mapAdapterByClass.put(SubscribeEvent.class, new ExternalEventAdapter() {

			@Override
			public void perform(EventSourceManager manager, Event related) {
				// TODO Auto-generated method stub
				
				SubscribeEvent realEvent = (SubscribeEvent) related;
                Connection connection = manager.safeFindConnectionByCall(realEvent.getCall());
                connection.onAlerting();
                connection.receiveEvent(Connection.AutomatonEvent.EVENT_CONNECTED, related);
				
				
			/*	CCXMLExecutionContext executionContext = manager.getExecutionContext();
				EventHub eventHub = executionContext.getEventHub();
				SimpleEvent event = CCXMLEvent.create(Constants.Event.SUBSCRIBE_RECEIVED, executionContext, null, DebugInfo.getInstance(), related);
				
				eventHub.fireContextEvent(event);*/
			}
        	
        });

        mapAdapterByClass.put(ProxiedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related) {
                manager.safeFindConnectionByCall(((ProxiedEvent) related).getCall()).
                        receiveEvent(Connection.AutomatonEvent.EVENT_PROXIED, related);
            }
        });
        
        mapAdapterByClass.put(RedirectedEvent.class, new ExternalEventAdapter() {
            public void perform(EventSourceManager manager, Event related ){
                RedirectedEvent event = (RedirectedEvent)related;
                Call call = event.getCall();
                Connection connection = manager.safeFindConnectionByCall(call);
                
                connection.receiveEvent(Connection.AutomatonEvent.EVENT_REDIRECTED, related);
            }
        });
        
        
    }

}
