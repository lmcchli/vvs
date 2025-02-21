package com.abcxyz.services.moip.ntf.coremgmt.oam;

import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.FaultManager;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.LogManager;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.PerformanceManager;
import com.abcxyz.messaging.common.oam.TrafficLogAgent;
import com.abcxyz.messaging.common.oam.impl.OamManagerDefaultImpl;

/**
 * NTF OAM default implementation
 *
 * class is used only in JUNIT test for now.
 *
 * @author lmchuzh
 *
 */
public class NtfCmnOamManager extends OamManagerDefaultImpl
{
    static OAMManager instance;

    /**
     * @param cmMBean
     */
    private NtfCmnOamManager() {
    }

    /**
     * default implementation
     */
    private void init() {
        //set configuration manager
        //confMgr = new NtfCmnConfig();

        //set log manager
        super.setLogAgent(new NtfCmnLogger());

        //don't care other managers for now
        instance = this;
    }

    /**
     * method to be used internally for resetting the configuration
     *
     * @return
     */
    public static synchronized OAMManager getInstance() {
        if (instance == null) {
            instance = new NtfCmnOamManager();
        }
        return instance;
    }

    /**
     * method for retrieving the existing instance
     *
     * @return
     */
    public ConfigManager getConfigManager()
    {
        return confMgr;
    }

    public FaultManager getFaultManager()
    {
        return faultMgr;
    }

    public LogManager getLoggingManager()
    {
        return logMgr;
    }

    public PerformanceManager getPerformanceManager()
    {
        return perfMgr;
    }

    public TrafficLogAgent getTrafficLogAgent()
    {
        return trafficLogger;
    }

    public void setConfigManager(ConfigManager arg0)
    {
        confMgr = arg0;
    }

    public void setFaultManager(FaultManager arg0)
    {
        faultMgr = arg0;
    }

    public void setLogAgent(LogAgent arg0)
    {
        logger = arg0;
    }

    public void setPerformanceManager(PerformanceManager arg0)
    {
        perfMgr = arg0;
    }

    public void setTrafficLogAgent(TrafficLogAgent arg0)
    {
        trafficLogger = arg0;

    }

}

