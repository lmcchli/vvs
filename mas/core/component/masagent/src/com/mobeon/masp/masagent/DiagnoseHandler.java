package com.mobeon.masp.masagent;

import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.masp.rpcclient.MasMibAttributes;
import com.mobeon.masp.rpcclient.MasMibProvidedServices;
import com.mobeon.masp.operateandmaintainmanager.ServiceInstance;
import com.mobeon.masp.operateandmaintainmanager.DiagnoseService;
import com.mobeon.masp.operateandmaintainmanager.OperationalState;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.snmp.common.VarBind;
import com.snmp.common.ObjectSyntax;
import com.snmp.common.SRSNMP;
import com.snmp.agent.lib.SRSNMPAgent;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;

/**
 * User: eperber
 * Date: 2006-feb-28
 * Time: 15:56:02
 */

/**
 * Service diagnostic handler.
 */
public class DiagnoseHandler implements Runnable, ApplicationContextAware, InitializingBean {
    private static ILogger log;// = ILoggerFactory.getILogger(DiagnoseHandler.class);
    private Map<String, DiagnoseService> diagService = new HashMap<String, DiagnoseService>();
    private List<String> services = new LinkedList<String>();
    private ApplicationContext ctx = null;
    private IConfiguration cfg = null;
    private String logConfiguration = null;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static String administrativeState = null;
    private static OperationalState operationalState = null;
    public void setServices(List<String> services) {
    this.services = services;

    }


      /**
     * Sends an SNMP trap. The current operational state and administrative state of
     * MAS is included in the trap.
     * @param OID The OID of the trap
     */
    public static void sendTrap(String OID, SRSNMPAgent agent, String operation ) {
        ILoggerFactory.configureAndWatch("/opt/moip/config/mas/logmanageragent.xml");
        log = ILoggerFactory.getILogger(DiagnoseHandler.class);
        MasMibAttributes attribs2 = null;
        VarBind[] vblist = new VarBind[2];
        int number= 0;
        String masadminstateOID = "1.3.6.1.4.1.24261.1.1.1.4";
        String masopstateOID = "1.3.6.1.4.1.24261.1.1.1.3";
        String adminstate= null;
        OperationalState opstate=null;
        String adminstate2="";
        String operationalstate2="";

        if(operation.equalsIgnoreCase("stop")) {
            //Use the cached values if they exists otherwise send a trap without admin/operational state.
            // Run when stoppiong MAS.
            if(administrativeState != null && operationalState != null) {
                   adminstate2 = getAdminState(administrativeState);
                   operationalstate2 = getOperationalState(operationalState);

                   ObjectSyntax adminsyntax = ObjectSyntax.newInstance(SRSNMP.INTEGER_TYPE, adminstate2);
                   vblist[0] = new VarBind(masadminstateOID,adminsyntax );
                   ObjectSyntax opsyntax = ObjectSyntax.newInstance(SRSNMP.INTEGER_TYPE,operationalstate2);
                   vblist[1] = new VarBind(masopstateOID,opsyntax);
                   agent.sendNotificationsSMIv2Params(OID, null, vblist);
                   log.info("Stop trap sent..");
            }
            else { //Send a trap without admin/operational state
                   vblist = null;
                   agent.sendNotificationsSMIv2Params(OID, null, vblist);
                   log.info("Stop trap sent.");
               }
         }
         else { //Start trap

              //Iterate until it is possible to get MIB values from MAS.
              //MAS is set do disabled during start up, so wait until the operational state is changed to ENABLED before sending the trap.
             //After 80s just send the trap anyway
              while ((attribs2 == null || opstate.equals(OperationalState.DISABLED)) && number < 20) {
                  try {
                      Thread.sleep(4000); //Sleep 4s
                      MasConnection masConnection = MasConnection.getInstance();
                      attribs2 = masConnection.getValues();
                      number++;
                      if(attribs2 != null) {  //If MAS connection set up, get adminstate.
                          opstate = attribs2.masOperationalState;    //Get admin state of MAS
                          log.debug("OperationalState = " + opstate);
                      }
                      log.debug("Waiting for MAS to start up...");
                   }
                   catch (InterruptedException e) {;}
              }


              if(attribs2 != null) {   //If MIB values found, get operational and admin state
                  adminstate = attribs2.masAdministrativeState;
                  adminstate2 = getAdminState(adminstate);
                  opstate = attribs2.masOperationalState;
                  operationalstate2 = getOperationalState(opstate);

                  ObjectSyntax adminsyntax = ObjectSyntax.newInstance(SRSNMP.INTEGER_TYPE, adminstate2);
                  vblist[0] = new VarBind(masadminstateOID,adminsyntax );
                  ObjectSyntax opsyntax = ObjectSyntax.newInstance(SRSNMP.INTEGER_TYPE,operationalstate2);
                  vblist[1] = new VarBind(masopstateOID,opsyntax);

                  //Send Trap
                  agent.sendNotificationsSMIv2Params(OID, null, vblist);
                  log.info("Start trap sent");
             }
             else { //send a trap without admin/operational state.
                   vblist = null;
                   agent.sendNotificationsSMIv2Params(OID, null, vblist);
                   log.info("Start trap sent.");
             }
         }

    }

    private static String getAdminState(String adminstate) {
        String new_value="";
        log.debug("AdminState=" + adminstate);
            if(adminstate.equals("unlocked")) {
                 new_value="1";
            }
            else if(adminstate.equals("locked")){
                 new_value="2";
            }
            else {  //Shutdown
                 new_value="3";
            }
        return new_value;
    }

     private static String getOperationalState(OperationalState opstate) {
        String new_value="";
        log.debug("OperationalState=" + opstate);
        if(opstate.equals(OperationalState.ENABLED)) {
              new_value="1";
        }
        else {    //Disabled
               new_value="2";
        }
         return new_value;
     }



    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	ctx = applicationContext;
    }

    public void setConfiguration(IConfiguration cfg) {
	this.cfg = cfg;
    }

    public void setLogConfiguration(String logConfiguration) {
	this.logConfiguration = logConfiguration;
    }


    /**
     * Called by the spring framework after all parameter injection is done.
     */
    public void afterPropertiesSet() throws Exception {
    	ILoggerFactory.configureAndWatch("/opt/moip/config/mas/logmanageragent.xml");
    	log = ILoggerFactory.getILogger(DiagnoseHandler.class);
	try {
	    for (String svc : services) {
    		DiagnoseService svcImpl = (DiagnoseService)ctx.getBean(svc);
		diagService.put(svc, svcImpl);
	    }
	} catch (Exception e) {
	    log.error("Unable to set up service diagnostics: " + e.getMessage());
	}
    }
    /**
     * The constructor.
     */
    public DiagnoseHandler() {
    }

    public void start() {
        // schedule first timer        
        scheduledExecutorService.scheduleWithFixedDelay(this, 60000, 50000, TimeUnit.MILLISECONDS);
        //diagThread.schedule(this, 60000, 60000);
    }
    /**
     * The main loop of the service diagnostic handler. Loops through all
     * services and sends a service request to them.
     */
    public void run() {
        MasConnection masConnection = MasConnection.getInstance();
        MasMibAttributes attribs = masConnection.getValues();

        if (attribs != null && attribs.providedServices != null) {
            for (MasMibProvidedServices svc : attribs.providedServices) {
                DiagnoseService diag = diagService.get(svc.protocol);
                if (diag != null) {
                    log.debug("Diagnose :"+svc.name);

                    ServiceInstance si = new ServiceInstance();
                    si.setHostName(svc.hostName);
                    si.setPort((int) svc.port);
                    DiagnoseRequest request = new DiagnoseRequest(svc.name,svc.hostName,svc.port, diag, si, masConnection);
                    ExecutorService pooledExecutorService = 
                        ExecutorServiceManager.getInstance().getExecutorService(getClass());
                    pooledExecutorService.execute(request);
                } else {
                    log.warn("Unable to match service: " + svc.name + " to a DiagnoseService object.");
                }
            }
        }
        //Set the Operational and Adminstate, so they could be used when sending a stop trap when MAS is stopped
        administrativeState = attribs.masAdministrativeState;
        operationalState = attribs.masOperationalState;
    }
}
