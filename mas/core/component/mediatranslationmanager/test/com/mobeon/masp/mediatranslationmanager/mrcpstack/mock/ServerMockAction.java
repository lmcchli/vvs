package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspMessage;

public class ServerMockAction {
    enum Action { PEND, SEND, SLEEP }
    Action action = Action.SLEEP;
    RtspMessage message;
    int delay = 1;

    public ServerMockAction(RtspMessage message, boolean pend) {
        this.message = message;
        if (pend) action = Action.PEND;
        else action = Action.SEND;
    }

    public ServerMockAction(int delay) {
        this.delay = delay;
    }
}
