package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2012.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

/**
 * Allows the registration of a signal to cause MAS to
 * Shutdown via it's internal management interface.
 * 
 * If SNMP is down a term or hup signal can be sent to terminate the
 * program..
 *
 * @author lmcmajo
 */
@SuppressWarnings("restriction")
class handleTermSignal implements SignalHandler {

    private static ILogger log = ILoggerFactory.getILogger(handleTermSignal.class);
    private SignalHandler oldHandler;
    private static OperateMAS operateMAS;

    // Static method to install the signal handler
    public static SignalHandler install(String signalName) {
        Signal diagSignal = new Signal(signalName);
        handleTermSignal termHandle = new handleTermSignal();
        termHandle.oldHandler = Signal.handle(diagSignal,termHandle);
        return termHandle;
    }

    public static void setOperateMAS(OperateMAS om) {
        operateMAS = om;
    }

    // Signal handler method
    public void handle(Signal sig) {
        log.info("handleTermSignal: Called for signal "+ sig);
        try {
            log.info("handleTermSignal: Initiating MAS shutdown");
            if (operateMAS != null) {
                log.info("handleTermSignal: Calling operateMAS.shutdown");
                operateMAS.shutdown();
            } else {
                log.warn("handleTermSignal: No operateMAS defined, exit");
                System.exit(0);
            }
            if (oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
                log.info("handleTermSignal: Passing to old handler: " + oldHandler.toString());
                oldHandler.handle(sig);
            }
        } catch (Exception e) {
            log.error("handleTermSignal: handler failed, reason ", e);
        }
    }
}
