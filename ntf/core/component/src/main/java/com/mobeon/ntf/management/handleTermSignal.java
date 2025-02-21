package com.mobeon.ntf.management;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;

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
 * allows the registration of a signal to cause NTF to
 * Shutdown via it's internal management interface.
 *
 * @author lmcmajo
 */
@SuppressWarnings("restriction")
class  handleTermSignal implements SignalHandler {

    private static LogAgent log = NtfCmnLogger.getLogAgent(handleTermSignal.class);
    private SignalHandler oldHandler;

    // Static method to install the signal handler
    public static SignalHandler install(String signalName) {
        Signal diagSignal = new Signal(signalName);
        handleTermSignal termHandle = new handleTermSignal();
        termHandle.oldHandler = Signal.handle(diagSignal,termHandle);
        return termHandle;
    }

    // Signal handler method
    public void handle(Signal sig) {
        log.info("called for signal "+ sig);
        try {
               log.info("Initiating shutdown");
               ManagementInfo.get().setNtfAdministrativeState(AdministrativeState.SHUTDOWN);
               if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
                   log.info("Passing to old handler: " + oldHandler.toString());
                   oldHandler.handle(sig);
               }
        } catch (Exception e) {
           log.error("handler failed, reason ", e);
        }
    }
}


