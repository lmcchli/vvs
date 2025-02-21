package com.abcxyz.services.moip.ntf.coremgmt;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.SchedulerConfigMgr;
import com.abcxyz.messaging.scheduler.SchedulerManager;
import com.abcxyz.messaging.scheduler.SchedulerOperations;
import com.abcxyz.messaging.scheduler.SchedulerStartFailureException;
import com.abcxyz.service.moip.common.cmnaccess.CommonTestingSetup;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.ConfigurationException;

public class NtfCmnManagerTest{

    static SchedulerManager scheduler;
    static OAMManager oam;

    @BeforeClass
    static public void startup() throws ConfigurationException, ConfigurationDataException {
        CommonTestingSetup.setup();
    }

    @AfterClass
    static public void tearDown() {
        System.setProperty("abcxyz.messaging.scheduler.memory", "false");
    }

    @Test
    public void testStartStop() throws InvalidEventIDException {
        oam = CommonOamManager.getInstance().getMrdOam();
        //oam.getConfigManager().setParameter(SchedulerConfigMgr.EventsRootPath, "/opt/moip/ntf/events");
        //oam.getConfigManager().setParameter(SchedulerConfigMgr.SchedulerID, "0");

        //start
        try {
            NtfCmnManager.getInstance().start();
        } catch (SchedulerStartFailureException e) {
            fail();
        }

        assertTrue(NtfCmnManager.getInstance().getMrdOamManager().getConfigManager().getParameter(
        		SchedulerConfigMgr.EventsRootPath).equalsIgnoreCase("/opt/moip/events/ntf"));
        assertTrue(NtfCmnManager.getInstance().getMrdOamManager().getConfigManager().getParameter(
        		SchedulerConfigMgr.SchedulerID).equalsIgnoreCase("0"));

        //test scheduler started
        assertTrue(NtfCmnManager.getInstance().getSchedulerManager().getState() == SchedulerOperations.STATE_STARTED);

        //test listener started
        assertTrue(NtfCmnManager.getInstance().isMrdListenerStarted());

        //make sure event handler/listener are registered
        assertTrue(NtfEventHandlerRegistry.getEventHandler() != null);
        assertTrue(NtfEventHandlerRegistry.getEventSentListener() != null);

        assertTrue(NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.DEFAULT_NTF.getName()) != null);
        assertTrue(NtfEventHandlerRegistry.getEventHandler(NtfEventTypes.SLAMDOWN.getName()) != null);

        assertTrue(NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.DEFAULT_NTF.getName()) != null);
        assertTrue(NtfEventHandlerRegistry.getEventSentListener(NtfEventTypes.SLAMDOWN.getName()) != null);

        NtfCmnManager.getInstance().stop();

        assertTrue(NtfCmnManager.getInstance().getSchedulerManager().getState() != SchedulerOperations.STATE_STARTED);
        assertFalse(NtfCmnManager.getInstance().isMrdListenerStarted());
    }

}
