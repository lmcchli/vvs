/* **********************************************************************
 * Copyright (c) ABCXYZ 2009. All Rights Reserved.
 * Reproduction in whole or in part is prohibited without the
 * written consent of the copyright owner.
 *
 * ABCXYZ MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ABCXYZ SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * **********************************************************************/

package com.abcxyz.services.moip.ntf.coremgmt;

import org.apache.log4j.BasicConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.scheduler.SchedulerConfigMgr;
import com.abcxyz.messaging.scheduler.SchedulerManager;
import com.abcxyz.messaging.scheduler.SchedulerStartFailureException;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnOamManager;


public class NotifEventHandlerTest {

    static SchedulerManager scheduler;
    static OAMManager oam;

    @BeforeClass
    static public void startup() throws ConfigurationDataException {
        BasicConfigurator.configure();

        oam = NtfCmnOamManager.getInstance();

        oam.getConfigManager().setParameter(SchedulerConfigMgr.EventsRootPath, "/tmp/scheduler/events");
        oam.getConfigManager().setParameter(SchedulerConfigMgr.SchedulerID, "22");
        System.setProperty("abcxyz.messaging.scheduler.memory", "true");

        //start scheduler
        try {
            NtfCmnManager.getInstance().startScheduler(oam);
        } catch (SchedulerStartFailureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    @AfterClass
    static public void tearDown() {
        //stop scheduler
        NtfCmnManager.getInstance().stopScheduler();
        System.setProperty("abcxyz.messaging.scheduler.memory", "false");
    }

    public void testDefaultHandler() throws InvalidEventIDException {
    }
}
