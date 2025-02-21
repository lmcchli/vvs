/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.masagent;

import com.snmp.agent.lib.Subagent;
import com.snmp.agent.lib.SRSNMPAgent;
import com.snmp.common.VarBind;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.configuration.*;
import com.mobeon.masp.rpcclient.MasMibAttributes;
import com.mobeon.masp.operateandmaintainmanager.OperationalState;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * User: eperber
 * Date: 2006-feb-06
 * Time: 11:39:07
 */
public class MasAgent implements ApplicationContextAware, InitializingBean, Runnable {
    private static DiagnoseHandler diagHandler;
    private ApplicationContext ctx = null;
    private IConfiguration cfg = null;
    //private String logConfiguration = "lib/mobeon_log.xml";
    private String logConfiguration = "/opt/moip/config/mas/logmanageragent.xml";
     /** The SRSNMP subagent */
    static SRSNMPAgent agent = null;
    private static ILogger log;

    //log = ILoggerFactory.getILogger(MasAgent.class);

    public MasAgent() {
    }

    public void start() {
	diagHandler = (DiagnoseHandler)ctx.getBean("DiagnoseHandler");
	diagHandler.start();
    }


    public void startSRSNMPAgent() {
        //log.info("Initilize the SRSNMPAgent");
        // Start the SRSNMP subagent
        //Emanate should be run on the localhost. But iy could be sonfigurable this host and port that are set.
        //But it is for the future.
        agent = new SRSNMPAgent("localhost", 7162, "MAS-MIB");
        agent.registerVariables("com.mobeon.masp.masagent.masOID");
        agent.execute(true);
    }

     /**
     * Sends an SNMP trap. The current operational state and administrative state of
     * MAS is included in the trap.
     * @param OID The OID of the trap
     */
    public static void sendTrap(String OID) {
         diagHandler.sendTrap(OID, agent, "start");
    }

     /**
     * Shutdown hook. This run method is called automatically 1 time when the subagent
     * is exiting. It sends the stop trap. After
     * this method is finished the subagent exits.
     */
    public void run() {
        String masstoppedOID = "1.3.6.1.4.1.24261.1.1.2.2";
        diagHandler.sendTrap(masstoppedOID, agent, "stop");
        //log.info("SNMPStart trap sent(masStarted)");
    }



    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	ctx = applicationContext;
    }

    public void setLogConfiguration(String logConfiguration) {
	this.logConfiguration = logConfiguration;
    }

    public void setConfiguration(IConfiguration cfg) {
	this.cfg = cfg;
    }

    public void afterPropertiesSet() throws Exception {
	ILoggerFactory.configureAndWatch(logConfiguration);
	MasConnection.init(cfg);
    }
}
