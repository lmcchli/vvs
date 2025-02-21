package com.mobeon.ntf.out.fax;

import java.util.concurrent.TimeUnit;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.FaxPrintEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.FaxPrintEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.management.ManagementInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;
import com.mobeon.ntf.util.threads.NtfThread;

public class FaxPrintWorker extends NtfThread {

    private static LogAgent log = NtfCmnLogger.getLogAgent(FaxPrintWorker.class);
    private ManagedArrayBlockingQueue<Object> faxWorkerQueue;
    private ManagedArrayBlockingQueue<Object> faxSenderQueue;
    private FaxPrintEventHandler faxPrintEventHandler;

    /**
     * Constructor
     * @param faxWorkerQueue fax queue
     * @param faxSenderQueue Outgoing calls are made through this
     * @param threadName Thread Name
     */
    public FaxPrintWorker(ManagedArrayBlockingQueue<Object> faxWorkerQueue,ManagedArrayBlockingQueue<Object> faxSenderQueue,
                     String threadName)
    {
        super(threadName);
        this.faxWorkerQueue = faxWorkerQueue;
        this.faxSenderQueue = faxSenderQueue;
        faxPrintEventHandler = (FaxPrintEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.FAX_L3.getName());
    }

    /**
     * Do one step of the work.
     * @return False if the work should continue, true if the worker wants to stop.
     */
    public boolean ntfRun()
    {
        FaxPrintEvent faxPrintEvent = null;

        try {
            // Get an event from the working queue
            Object obj = faxWorkerQueue.poll(10, TimeUnit.SECONDS);
            if (obj == null) return false;

            faxPrintEvent = (FaxPrintEvent)obj;
            if (log.isDebugEnabled())log.debug("Handle new event in FAX Print worker: " + faxPrintEvent);

            switch(faxPrintEvent.getCurrentEvent()) {

                case FaxPrintEvent.FAXPRINT_EVENT_NOTIFICATION:
                {
                    //Start sending the fax only if there's no fax onging transmission
                    faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_SENDING);
                    sendFax(faxPrintEvent);
                    break;
                }
                case FaxPrintEvent.FAXPRINT_EVENT_EXPIRED:
                {
                    faxPrintEventHandler.handleFaxPrintExpiry(faxPrintEvent);
                    break;
                }
                case FaxPrintEvent.FAXPRINT_EVENT_SEND_FAILED:
                {
                    //Permanent error we will not continue
                    faxPrintEventHandler.handleFaxPrintFailled(faxPrintEvent);
                    break;
                }

                case FaxPrintEvent.FAXPRINT_EVENT_SEND_OK:
                {
                    faxPrintEventHandler.handleFaxPrintSuccess(faxPrintEvent);
                    break;
                }
                case FaxPrintEvent.FAXPRINT_EVENT_SEND_RETRY:
                {
                    //If the fax print expired during transmission we need to stop there.
                    if(faxPrintEvent.isExpiry())
                    {
                        faxPrintEventHandler.handleFaxPrintExpiryInTransmission(faxPrintEvent);
                    }
                    else
                    {
                        //Just wait for the scheduler to notify us when it will be completed.
                        faxPrintEvent.setCurrentEvent(FaxPrintEvent.FAXPRINT_EVENT_WAIT);
                    }
                    break;
                }

                default:
                {
                    log.error("Unable to determine action " + faxPrintEvent);
                    faxPrintEventHandler.handleFaxPrintFailled(faxPrintEvent);

                    break;

                }
            }
        }  catch (OutOfMemoryError me) {
            try {
                ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
                log.error("NTF out of memory, shutting down... ", me);
                faxPrintEventHandler.handleFaxPrintFailled(faxPrintEvent);
            } catch (OutOfMemoryError me2) {;} //ignore second exception
            return true; //exit.
        } catch (Exception e) {
            log.error("Exception in Fax Print worker " ,e);
            if (faxPrintEvent != null) {
                faxPrintEventHandler.handleFaxPrintFailled(faxPrintEvent);
            }
        }
        return false;
    }

    public boolean shutdown() {
        if (isInterrupted()) {
            return true;
        } //exit immediately if interrupted..

        if (faxWorkerQueue.size() == 0)
        {
                //give a short time for new items to be queued in workers, to allow other threads to empty there queues.
                if (faxWorkerQueue.isIdle(2,TimeUnit.SECONDS)) {
                    return true;
                }
                else
                {
                    if (faxWorkerQueue.waitNotEmpty(2, TimeUnit.SECONDS)) {
                        return(ntfRun());
                    } else
                    {
                        return true;
                    }

                }
        } else {
            return(ntfRun());
        }
    }

    /**
     * Attempt to make a call to user.
     * @param faxPrintEvent Information about the call.
     */
    private void sendFax(FaxPrintEvent faxPrintEvent)
    {
        //Send fax
        try {
            faxSenderQueue.put(faxPrintEvent);
        } catch (Throwable t) {
            log.info("sendFax: queue full or state locked while handling event, will retry");
        }
    }
}
