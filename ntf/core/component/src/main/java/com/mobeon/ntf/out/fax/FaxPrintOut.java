/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.fax;

import java.util.Iterator;
import java.util.Map;

import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.FaxPrintEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.NtfEventHandlerRegistry;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.FaxPrintEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.ntf.management.ManagedArrayBlockingQueue;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.NotificationGroup;

public class FaxPrintOut implements com.mobeon.ntf.Constants {
    private LogAgent log = NtfCmnLogger.getLogAgent(FaxPrintOut.class);
    private ManagedArrayBlockingQueue<Object> faxWorkerQueue;
    private ManagedArrayBlockingQueue<Object> faxSenderQueue;
    private boolean isStarted;
    private FaxPrintWorker[] faxPrintWorkers;
    private FaxSender[] faxPrintSender=null;
    private FaxPrintEventHandler faxPrintHandler;
    private static FaxPrintOut instance = null;
    private MerAgent mer;
    private String faxServerHost=null;
    private String faxServerPort=null;
    private boolean faxEnabled=false;


    /**
     * Constructor
     */
    public FaxPrintOut() {
        isStarted = start();
        if(isStarted)
        {
            log.info("FAX Print is active");
        }
        else
        {
            log.info("FAX Print is not active");
        }
        mer = MerAgent.get(Config.getInstanceComponentName());
        instance = this;

    }

    public static FaxPrintOut get() {
        if (instance == null) {
            instance = new FaxPrintOut();
        }
        return instance;
    }


    public void updateConfig()
    {
        if (faxEnabled && faxPrintSender != null) {
            Map<String, Map<String, String>> faxServersMap = Config.getExternalEnablers(NotificationConfigConstants.FAX_SERVER_TABLE);
            if (faxServersMap == null || faxServersMap.isEmpty()) {
                log.debug("No Fax Server found in config");
                faxServerHost=null;
                faxServerPort=null;
            } else {
                Iterator<String> it = faxServersMap.keySet().iterator();
                while (it.hasNext()) {
                    //For now we only support one fax server ip address
                    String faxServer = it.next();
                    faxServerHost = faxServersMap.get(faxServer).get(NotificationConfigConstants.HOST_NAME);
                    faxServerPort = faxServersMap.get(faxServer).get(NotificationConfigConstants.PORT);
                    break;
                }
            }
            refreshSenderConfig();
        }
    }

    void refreshSenderConfig()
    {
        for (int i = 0; i<faxPrintSender.length; i++) {
            faxPrintSender[i].updateConfig(faxServerHost,faxServerPort);
        }
    }

    private boolean start() {
        try {
            // Create working queue for FaxPrintOut and FaxPrintWorkers
            faxSenderQueue = new ManagedArrayBlockingQueue<Object>(Config.getFaxPrintQueueSize());

            // Create Fax Print event handler
            faxPrintHandler = (FaxPrintEventHandler)NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.FAX_L3.getName());
            faxWorkerQueue = faxPrintHandler.getWorkingQueue();

            faxEnabled = Config.isFaxEnabled();
            int NO_WORKERS = Config.getFaxWorkers();
            int NO_SENDERS = Config.getFaxPrintMaxConn();
            if(!faxEnabled) {
                log.info("Fax Print service configuration is not enabled");
                return false;
            }
            else if (NO_WORKERS<=0 || NO_SENDERS<=0) {
                log.error("Fax Print service is not enabled since number of workers or senders is set to 0");
                return false;
            }


            createWorkers(NO_WORKERS);
            createSender(NO_SENDERS);


            return true;
        } catch (Exception e) {
            if (log != null) {
                log.error("Could not start Fax Print out interface. Message: ", e);
            }
            return false;
        }
    }

    /**
     * Create the workers
     */
    private void createWorkers(int NO_WORKERS) {
        faxPrintWorkers = new FaxPrintWorker[NO_WORKERS];
        for (int i = 0; i<NO_WORKERS; i++) {
            faxPrintWorkers[i] = new FaxPrintWorker(faxWorkerQueue,faxSenderQueue, "FaxPrintWorker-" + i);
            faxPrintWorkers[i].setDaemon(true);
            faxPrintWorkers[i].start();
        }
    }
    /**
     * Create the workers
     */
    private void createSender(int NO_SENDERS) {
        faxPrintSender = new FaxSender[NO_SENDERS];
        for (int i = 0; i<NO_SENDERS; i++) {

            faxPrintSender[i] = new FaxSender(faxSenderQueue,faxWorkerQueue, "FaxPrintSender-" + i,i==0/*Only the first fax server will pool connection*/);
            faxPrintSender[i].setDaemon(true);
            faxPrintSender[i].start();
        }
        updateConfig();
    }

    /**
     * This method is invoked by a notification handler send fax auto print.
     * @param user subscriber
     * @param ng notification group
     * @return Number of Fax Print sent out
     */
    public int handleFaxPrint(UserInfo user, NotificationGroup ng) {
        FaxPrintEvent faxPrintEvent = null;
        MessageInfo msgInfo =null;

        if (log.isDebugEnabled())log.debug("handleFaxPrint for "+ng.getEmail().getMessageId());
        String autoprint= ng.getEmail().getNtfEvent().getEventProperties().getProperty(MoipMessageEntities.FAX_AUTOPRINT_ENABLE_PROPERTY);

        // Create new event
        String userPhoneNumber=ng.getEmail().getReceiverPhoneNumber();
        String faxPrintNumber=ng.getEmail().getNtfEvent().getEventProperties().getProperty(MoipMessageEntities.FAX_PRINT_NUMBER_PROPERTY);

        //Getting original fax message from extra properties
        //TODO in future we should use inform event instead.  For now inform event are not working with extra properties
        String rmsa = ng.getEmail().getNtfEvent().getEventProperties().getProperty(MoipMessageEntities.FAX_PRINT_FAXMSG_RMSA);
        String rmsgid =ng.getEmail().getNtfEvent().getEventProperties().getProperty(MoipMessageEntities.FAX_PRINT_FAXMSG_RMSGID);
        String omsa = ng.getEmail().getNtfEvent().getEventProperties().getProperty(MoipMessageEntities.FAX_PRINT_FAXMSG_OMSA);
        String omsgid =ng.getEmail().getNtfEvent().getEventProperties().getProperty(MoipMessageEntities.FAX_PRINT_FAXMSG_OMSGID);

        if(rmsa!=null && rmsgid!=null && omsa!=null && omsgid!=null)
        {
            msgInfo = new MessageInfo(new MSA(omsa), new MSA(rmsa), omsgid, rmsgid);
        }
        else
        {
            log.error("handleFaxPrint: unable to find message information");
        }


        //If missing information stop the print
        if(rmsa==null || rmsgid==null || omsa==null || omsgid==null || userPhoneNumber==null || faxPrintNumber==null || autoprint==null)
        {
            log.error("handleFaxPrint: Unable to print message missing information userPhoneNumber: "+ userPhoneNumber + " faxPrintNumber: "+faxPrintNumber +
                    " autoprint: "+autoprint +
                    " rmsa: "+rmsa +
                    " rmsgid: "+rmsgid +
                    " omsa: "+omsa +
                    " omsgid: "+omsgid);
            handleFaxPrintFailure(user, faxPrintEvent, msgInfo, autoprint);

            return 0;
        }

        faxPrintEvent = new FaxPrintEvent( userPhoneNumber, faxPrintNumber,autoprint, msgInfo);

        if (!isStarted()) {
            log.error("FAX PRINT is not started, return");
            handleFaxPrintFailure(user, faxPrintEvent, msgInfo, autoprint);

            return 0;
        }



        if(CommonMessagingAccess.getInstance().denormalizeNumber(user.getInboundFaxNumber()).equalsIgnoreCase(CommonMessagingAccess.getInstance().denormalizeNumber(faxPrintNumber)))
        {
            log.info("handleFaxPrint: Unable to print message fax is being printed to user InboundFaxNumber: "+user.getInboundFaxNumber()+  " faxPrintNumber: "+faxPrintNumber);
            handleFaxPrintFailure(user, faxPrintEvent, msgInfo, autoprint);
           return 0;

        }

        // Add event to the scheduler (backup event)
        faxPrintHandler.scheduleBackup(faxPrintEvent);

        // Add to the working queue for initial processing
        try {
            faxWorkerQueue.put(faxPrintEvent);
        } catch (Throwable t) {
            log.info("handleFaxPrint: queue full or state locked while handling event, will retry");
        }

        return 1;
    }

    private void handleFaxPrintFailure(UserInfo user, FaxPrintEvent faxPrintEvent, MessageInfo msgInfo, String autoprint) {
        if(msgInfo!=null)  faxPrintHandler.setFaxPrintDone( msgInfo);

        if(autoprint.endsWith("true"))
        {
            mer.faxPrintFailed(user.getTelephoneNumber(),true);
        }
        else
        {
            mer.faxPrintFailed(user.getTelephoneNumber(),false);

        }
        if(faxPrintEvent!=null)
        {
            faxPrintHandler.sendFaxDeliveryReceipt(user,msgInfo,faxPrintEvent.getFaxEventUniqueId(),faxPrintEvent.getFaxPrintNumber(),false);

        }
        else
        {
            faxPrintHandler.sendFaxDeliveryReceipt(user,msgInfo,null,null, false);

        }
    }

    public boolean isStarted() {
        return isStarted;
    }




}
