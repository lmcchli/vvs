/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.common.eventnotifier.Event;

public class TransitionActions {

    public static class SendAction extends Connection.Action {
        protected String event;
        protected Connection.EventTarget target = BridgeParty.EventTarget.CONTEXT;

        protected SendAction(String event, Connection.EventTarget target) {
            this.event = event;
            this.target = target;
        }
        protected SendAction(String event) {
            this.event = event;
        }


        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related,target);
        }

        public String describe() {
            return describeSend(event);
        }
    }

    private static String describeSend(String event) {
        return "Action{ send=" + event + " }";
    }


    public static final Connection.Action ACCEPTED = new SendAction(Constants.Event.CONNECTION_CONNECTED,BridgeParty.EventTarget.CCXML);
    public static final Connection.Action EVENT_ALERTING = new SendAction(Constants.Event.CONNECTION_ALERTING,BridgeParty.EventTarget.CCXML);
    public static final Connection.Action EVENT_FAILED = new SendAction(Constants.Event.CONNECTION_FAILED,BridgeParty.EventTarget.CCXML);
    public static final Connection.Action EVENT_SIGNAL = new SendAction(Constants.Event.CONNECTION_SIGNAL,BridgeParty.EventTarget.CCXML);
    public static final Connection.Action EVENT_ERROR = new SendAction(Constants.Event.ERROR_CONNECTION,BridgeParty.EventTarget.CCXML);
    public static final Connection.Action EVENT_PROGRESSING = new SendAction(Constants.Event.CONNECTION_PROGRESSING,BridgeParty.EventTarget.CCXML);
    public static final Connection.Action EVENT_DISCONNECTED = new SendAction(Constants.Event.CONNECTION_DISCONNECTED,BridgeParty.EventTarget.CCXML);
    public static final Connection.Action EVENT_CONNECTED = new SendAction(Constants.Event.CONNECTION_CONNECTED,BridgeParty.EventTarget.CCXML);

    public static final Connection.Action EVENT_PROXIED = new Connection.Action() {
        final String event = Constants.Event.CONNECTION_PROXIED ;
        final String event2 = Constants.Event.CONNECTION_DISCONNECTED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related,BridgeParty.EventTarget.CCXML);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };
    
    public static final Connection.Action EVENT_REDIRECTED = new SendAction(Constants.Event.CONNECTION_REDIRECTED,BridgeParty.EventTarget.CCXML);
    
    public static final Connection.Action RECORD_FINISHING = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };
    
    public static final Connection.Action RECORD_FINISHED = new Connection.Action() {
        final String event = Constants.Event.RECORD_FINISHED;

        public void perform(
        Connection connection, Event related) {
            connection.sendEvent(event, related);
        }

        public String describe() {
            return "Action{ send=" + event + " }";
        }
    };
    
    public static final Connection.Action RECORD_FAILED = new Connection.Action() {
        final String event = Constants.Event.RECORD_FAILED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
        }

        public String describe() {
            return "Action{ send=" + event + " }";
        }
    };

    public static final Connection.Action RECORD_FINISHED_AFTER_HANGUP = new Connection.Action() {
        final String event = Constants.Event.RECORD_FINISHED_HANGUP;
        final String event2 = Constants.Event.CONNECTION_DISCONNECTED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action RECORD_FAILED_AFTER_HANGUP = new Connection.Action() {
        final String event = Constants.Event.RECORD_FAILED_HANGUP;
        final String event2 = Constants.Event.CONNECTION_DISCONNECTED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action PLAY_FINISHING = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };

    public static final Connection.Action PLAY_FINISHED = new Connection.Action() {
        final String event = Constants.Event.PLAY_FINISHED;

        public void perform(
        Connection connection, Event related) {
            connection.sendEvent(event, related);
        }

        public String describe() {
            return "Action{ send=" + event + " }";
        }
    };

    public static final Connection.Action PLAY_FAILED = new Connection.Action() {
        final String event = Constants.Event.PLAY_FAILED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
        }

        public String describe() {
            return "Action{ send=" + event + " }";
        }
    };

    public static final Connection.Action PLAY_FINISHED_AFTER_HANGUP = new Connection.Action() {
        final String event = Constants.Event.PLAY_FINISHED_HANGUP;
        final String event2 = Constants.Event.CONNECTION_DISCONNECTED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action PLAY_FAILED_AFTER_HANGUP = new Connection.Action() {
        final String event = Constants.Event.PLAY_FAILED_HANGUP;
        final String event2 = Constants.Event.CONNECTION_DISCONNECTED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action PLAY_FINISHING_FAILED = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };

    public static final Connection.Action PLAY_FINISHED_AFTER_FAILED = new Connection.Action() {
        final String event = Constants.Event.PLAY_FINISHED;
        final String event2 = Constants.Event.CONNECTION_FAILED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action PLAY_FINISHING_ALERTING_FAILED = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };

    public static final Connection.Action PLAY_FINISHING_ERROR = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };

    public static final Connection.Action PLAY_FINISHED_AFTER_ERROR = new Connection.Action() {
        final String event = Constants.Event.PLAY_FINISHED;
        final String event2 = Constants.Event.ERROR_CONNECTION;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action PLAY_FINISHING_ALERTING_ERROR = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };
    
    public static final Connection.Action RECORD_FINISHING_FAILED = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };

    public static final Connection.Action RECORD_FINISHING_ALERTING_FAILED = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };
    
    public static final Connection.Action RECORD_FINISHED_AFTER_FAILED = new Connection.Action() {
        final String event = Constants.Event.RECORD_FINISHED;
        final String event2 = Constants.Event.CONNECTION_FAILED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action RECORD_FINISHING_ERROR = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };

    public static final Connection.Action RECORD_FINISHING_ALERTING_ERROR = new Connection.Action() {
        public void perform(Connection connection, Event related) {
        }

        public String describe() {
            return "Action{ }";
        }
    };
    
    public static final Connection.Action RECORD_FINISHED_AFTER_ERROR = new Connection.Action() {
        final String event = Constants.Event.RECORD_FINISHED;
        final String event2 = Constants.Event.ERROR_CONNECTION;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action PLAY_FAILED_AFTER_FAILED = new Connection.Action() {
        final String event = Constants.Event.PLAY_FAILED;
        final String event2 = Constants.Event.CONNECTION_FAILED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action PLAY_FAILED_AFTER_ERROR = new Connection.Action() {
        final String event = Constants.Event.PLAY_FAILED;
        final String event2 = Constants.Event.ERROR_CONNECTION;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action RECORD_FAILED_AFTER_FAILED = new Connection.Action() {
        final String event = Constants.Event.RECORD_FAILED;
        final String event2 = Constants.Event.CONNECTION_FAILED;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };

    public static final Connection.Action RECORD_FAILED_AFTER_ERROR = new Connection.Action() {
        final String event = Constants.Event.RECORD_FAILED;
        final String event2 = Constants.Event.ERROR_CONNECTION;

        public void perform(Connection connection, Event related) {
            connection.sendEvent(event, related);
            connection.sendEvent(event2, related,BridgeParty.EventTarget.CCXML);
        }

        public String describe() {
            return "Action{ send=" + event + ", send=" + event2 + " }";
        }
    };
}
