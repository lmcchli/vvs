package com.mobeon.ntf.out.outdial;

import java.util.TimerTask;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;

public class NtfTimerTask extends TimerTask{
    OdlEvent odlEvent;
    ManagedArrayBlockingQueue<Object> queue;
    private static LogAgent log = NtfCmnLogger.getLogAgent(NtfTimerTask.class);

    public NtfTimerTask(OdlEvent odlEvent, ManagedArrayBlockingQueue<Object> queue){
        this.odlEvent = odlEvent;
        this.queue = queue;
    }

    public void run(){
        odlEvent.setFromNotify(false);
        odlEvent.setOdlTrigger(null);
        //submit back event to event queue
        try {
            queue.put(odlEvent);
        } catch (Throwable t) {
            log.info("NtfTimerTask.run: queue full or state locked while handling event, will retry");
        }
    }
}
