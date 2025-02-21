package com.mobeon.masp.masagent;

import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.snmp.agent.lib.*;
import com.snmp.common.*;
//import com.snmp.common.DateAndTime;

/**
 * User: eperber
 * Date: 2006-maj-02
 * Time: 16:13:12
 */
public class Main{
    private final static String config = "//opt/moip/config/mas/SubagentConfig.xml";
    private static ApplicationContext ctx;
    private static MasAgent agent;
    private static ILogger log;
     /** The SRSNMP subagent */
    //static SRSNMPAgent agent2 = null;


    /**
     * Sends an SNMP trap. The current operational state and administrative state of
     * MAS is included in the trap.
     * @param OID The OID of the trap
     */
  /*  private static void sendTrap(String OID) {
        log.info("SNMPStart send trap function");
        //Mib mib = Mib.getInstance();
         VarBind[] vblist = null;
        //VarBind[] vblist = new VarBind[2];
        //if ( mib.getMupOperationalState() != null
        //     && mib.getMupAdministrativeState() != null
         //    && agent2 != null ){
        //  vblist[0] = new VarBind(mib.getMupOperationalStateOID(), mib.getMupOperationalState())
          //vblist[1] = new VarBind(mib.getMupAdministrativeStateOID(), mib.getMupAdministrativeState());
          agent2.sendNotificationsSMIv2Params(OID, null, vblist);

    }   */



    public static void main(String[] args) {
    try {
        ILoggerFactory.configureAndWatch("/opt/moip/config/mas/logmanageragent.xml");
        log = ILoggerFactory.getILogger(Main.class);

        // Shutdown handling. Create a MASAgent object and register it as a shutdown hook.
        // The run method in MASAgent will be automatically called when the subagent exits.
        MasAgent shutdownHook = new MasAgent();
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));

        ctx = new FileSystemXmlApplicationContext(config);
        agent = (MasAgent)ctx.getBean("MasAgent");
        agent.start();
    } catch (Exception e) {
        //ILoggerFactory.configureAndWatch("lib/mobeon_log.xml");
        log.error("Failed to create MasAgent, bailing out", e);
        System.exit(0);
    }

    //Check that the JSA process has started. The process between MAs subagent and emanate.
    while( Subagent.isJSAListening() != true ) {
       try {
          Thread.sleep(250);
       } catch (Exception e) {
          break;
       }
    }
    log.debug("JSA started.");

    //Subagent subAgent = new Subagent(args, "com.mobeon.masp.masagent.masOID", "MAS-MIB");

      agent.startSRSNMPAgent();
      log.info("SRSNMPAgent started and initilized");

     String masstartedOID = "1.3.6.1.4.1.24261.1.1.2.1";
     agent.sendTrap(masstartedOID);   //Send start trap
     log.info("SNMPStart trap sent(masStarted)");


    }
}
