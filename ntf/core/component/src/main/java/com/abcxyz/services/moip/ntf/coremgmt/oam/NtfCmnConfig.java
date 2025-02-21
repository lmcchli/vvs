package com.abcxyz.services.moip.ntf.coremgmt.oam;

/* **********************************************************************
 * Copyright (c) Abcxyz 2009. All Rights Reserved.
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



import java.io.File;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.scheduler.SchedulerConfigMgr;
import com.abcxyz.messaging.scheduler.SchedulerStartFailureException;
import com.mobeon.common.cmnaccess.oam.CommonOamAccess;
import com.mobeon.common.logging.LogAgentFactory;

/**
 * Configuration plug in for using common messaging OAM configuration manager
 * All plug-in parameters are set at bootstrap, so their values are retrieved at that time, not changeable after
 *
 * this class is used for JUNIT test only for now
 *
 * @author lmchuzh
 *
 */
 class NtfCmnConfig extends SchedulerConfigMgr
{
    private static LogAgent logger =  LogAgentFactory.getLogAgent(NtfCmnConfig.class);
    private static ConfigManager config;

    public static final String ServiceListenerCorePoolSize = "ServiceListenerCorePoolSize";
    public static final String ServiceListenerMaxPoolSize = "ServiceListenerMaxPoolSize";

    /**
     *
     * @param cmMBean
     */
    public NtfCmnConfig() {
        config = CommonOamAccess.getInstance().getConfigManager();
    }

    public void init() throws SchedulerStartFailureException {

        //
        //prepare scheduler configurations
        //

        //check scheduler storage path
        String param = config.getParameter(SchedulerConfigMgr.EventsRootPath);

        if (param == null) {
            logger.warn("NtfCmnConfig scheduler path not defined");
            throw new SchedulerStartFailureException("scheduler events root path not defined.");
        } else {
            File path = new File(param);
            if (!path.exists()) {
                logger.info("NtfCmnConfig scheduler path <" + param + "> doesn't exist, to be created");
                if (path.mkdirs() == false) {
                    throw new SchedulerStartFailureException("scheduler events root path not accessible.");
                }
            }
        }

        //
        //TODO read from NTF configuration configuration pool size
        //


        /*super.setParam(SchedulerConfigMgr.EventsRootPath, param);

        param = config.getParameter(SchedulerConfigMgr.SchedulerID);
        super.setParam(SchedulerConfigMgr.SchedulerID, param);

        param = config.getParameter(SchedulerConfigMgr.SchedulerList);
        super.setParam(SchedulerConfigMgr.SchedulerList, param);


        super.setParam(DefaultOpenTimers, "3600000");
        super.setParam(EventsDirPrecreation, "false");

        long ltime = config.getLongValue(SchedulerConfigMgr.MaxFutureDirTime);
        if (ltime != 0) {
            super.setParam(MaxFutureDirTime, "" + ltime);
        }*/
    }

}
