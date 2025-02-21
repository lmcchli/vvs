/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wireline;

import com.mobeon.ntf.Config;
import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.ntf.util.Logger;

import java.util.*;

/** SS7GatewayHandler identifies all registered SS7 services in MCR.
 *
 * Each registration is represented as a SS7Unit.
 * SS7GatewayHandler sort each SS7Unit in two categories, local and non local
 * units.Primary only local SS7Units will be used. But if all local
 * SS7Units has connection problem a nonlocal SS7Unit will used.
 * SS7GatewayHandler is also responsible to keep track of SS7Units that have
 * some sort of connection problem. A SS7Unit with connection problem
 * can't be used until the connection problem has been resolved.
 */
class SS7GatewayHandler extends Thread{
    
    private final static Logger log = Logger.getLogger(SS7GatewayHandler.class);
    private boolean nonAndlocalUnitsExist = false;
    private boolean onlyLocalUnitsExist = false;
    private boolean onlyNonLocalUnitsExist = false;
    private int connLimitMsgId;
    private int unitLimitMsgId;
    private int handlerCount;
    private SS7ResponseHandler responseHandler = null;
    private SS7GatewaySorter chooser = null;
    private Vector<IServiceInstance> localHosts = null;
    private Vector<IServiceInstance> nonlocalHosts = null;
    private HashMap<String, Object> notConnectedHosts = null;
    private Hashtable<String, SS7Unit> u_local_with_conn_prob;
    private Hashtable<String, SS7Unit> u_nonlocal_with_conn_prob;
    private Hashtable<String, SS7Unit> localfreeUnits;
    private Hashtable<String, SS7Unit> nonlocalfreeUnits;

    private class UpdateUnits {

        private SS7GatewayHandler center = null;

        public UpdateUnits(SS7GatewayHandler c){
            center = c;
            setName("SS7McrHandler");
            reReadComponets();
        }

        private void reReadComponets(){
            Vector<IServiceInstance> local = null;
            Vector<IServiceInstance> nonlocal = null;

        	List<IServiceInstance> instList;
        	try {
        		instList = ExternalComponentRegister.getInstance().getServiceInstances(IServiceName.FIXED_MWI_NOTIFICATION);
        	}
        	catch (NoServiceFoundException e) {
        		log.logMessage("Could not find MCR info for service: " + IServiceName.FIXED_MWI_NOTIFICATION, Logger.L_ERROR);
        		e.printStackTrace(System.out);
        		return;
        	}

            log.logMessage("Number of SS7Components : " + instList.size(), Logger.L_DEBUG);
            SS7GatewaySorter ch = new SS7GatewaySorter();
            ch.updateClients(instList);
            local = ch.getLocalHosts();
            nonlocal = ch.getNonLocalHosts();
            updateLocalHosts(local);
            updateNonLocalHosts(nonlocal);
        }

        private void updateLocalHosts(Vector<IServiceInstance> local){
            checkPresentVsNewLocalHost(local);
            if(local.size() != 0){
            	String host;
            	int port;
                for(IServiceInstance inst : local){
                    host = inst.getProperty(IServiceInstance.HOSTNAME);
                    try {
                    	port = Integer.parseInt(inst.getProperty(IServiceInstance.PORT));
                    }
                    catch (NumberFormatException e) {
                    	/**
                    	 * skip the current instance
                    	 */
                		log.logMessage("SS7GatewayHandler.initAllUnits: port is not numeric for service:  " + inst.getServiceName(), Logger.L_ERROR);
                		e.printStackTrace(System.out);
                    	continue;
                    }
                    if( !localfreeUnits.containsKey(host) && !u_local_with_conn_prob.containsKey(host)){
                        log.logMessage("Adding new host: " + host + " ,to locale table", Logger.L_DEBUG);
                        SS7Unit ou = new SS7Unit(localfreeUnits.size() + 1, center, responseHandler, host, port, true);
                        localfreeUnits.put(host, ou);
                    }
                }
            }
        }

        private void checkPresentVsNewLocalHost(Vector<IServiceInstance> local){
            Enumeration<String> e = localfreeUnits.keys();
            log.logMessage("Number of local hosts are: " + local.size(), Logger.L_DEBUG);
            boolean remove;
            String ss7Host;
            String newHost;
            while(e.hasMoreElements()){
                remove = true;
                ss7Host = e.nextElement();
                for(IServiceInstance inst : local){
                    newHost  = inst.getProperty(IServiceInstance.HOSTNAME);
                    log.logMessage("checkPresentVsNewLocalHost(): Compare host " + ss7Host + " with " + newHost, Logger.L_DEBUG);
                    if(ss7Host.trim().equalsIgnoreCase(newHost.trim())){
                        remove = false;
                    }
                }
                if(remove){
                    log.logMessage("Remove host " + ss7Host + " from local table, obsolete.", Logger.L_DEBUG);
                    SS7Unit u = localfreeUnits.remove(ss7Host);
                    u.stopUnit(true);
                    if(notConnectedHosts.containsKey(ss7Host)){
                        log.logMessage("Remove host " + ss7Host + " from local not connected table, obsolete.", Logger.L_DEBUG);
                        synchronized(notConnectedHosts){
                            notConnectedHosts.remove(ss7Host);
                        }
                        u_local_with_conn_prob.remove(ss7Host);
                    }
                    e = localfreeUnits.keys();
                }
            }
        }

        private void updateNonLocalHosts(Vector<IServiceInstance> nonlocal){

            checkPresentVsNewNonLocalHost(nonlocal);
            if(nonlocal.size() != 0){
            	String host;
            	int port;
                for(IServiceInstance inst : nonlocal){
                    host = inst.getProperty(IServiceInstance.HOSTNAME);
                    try {
                    	port = Integer.parseInt(inst.getProperty(IServiceInstance.PORT));
                    }
                    catch (NumberFormatException e) {
                    	/**
                    	 * skip the current instance
                    	 */
                		log.logMessage("SS7GatewayHandler.initAllUnits: port is not numeric for service:  " + inst.getServiceName(), Logger.L_ERROR);
                		e.printStackTrace(System.out);
                    	continue;
                    }
                    if( !nonlocalfreeUnits.containsKey(host) && !u_nonlocal_with_conn_prob.containsKey(host)){
                        log.logMessage("Adding new host: " + host + " ,to nonlocale table", Logger.L_DEBUG);
                        SS7Unit ou = new SS7Unit(nonlocalfreeUnits.size() + 1, center, responseHandler, host, port, false);
                        nonlocalfreeUnits.put(host, ou);
                    }
                }
            }
        }

        private void checkPresentVsNewNonLocalHost(Vector<IServiceInstance> nonlocal){
            Enumeration<String> e = nonlocalfreeUnits.keys();
            log.logMessage("Number of nonlocal hosts are: " + nonlocal.size(), Logger.L_DEBUG);
            boolean remove;
            String ss7Host;
            String newHost;
            while(e.hasMoreElements()){
                remove = true;
                ss7Host = e.nextElement();
                for(IServiceInstance inst : nonlocal){
                    newHost  = inst.getProperty(IServiceInstance.HOSTNAME);
                    log.logMessage("checkPresentVsNewNonLocalHost(): Compare host " + ss7Host + " with " + newHost, Logger.L_DEBUG);
                    if(ss7Host.equalsIgnoreCase(newHost)){
                        remove = false;
                    }
                }
                if(remove){
                    log.logMessage("Remove host " + ss7Host + " from nonlocal table, obsolete.", Logger.L_DEBUG);
                    SS7Unit u = nonlocalfreeUnits.remove(ss7Host);
                    u.stopUnit(true);
                    if(notConnectedHosts.containsKey(ss7Host)){
                        log.logMessage("Remove host " + ss7Host + " from nonlocal not connected table, obsolete.", Logger.L_DEBUG);
                        synchronized(notConnectedHosts){
                            notConnectedHosts.remove(ss7Host);
                        }
                        u_nonlocal_with_conn_prob.remove(ss7Host);
                    }
                    e = nonlocalfreeUnits.keys();
                }
            }
        }
    }



    /** SS7GatewayHandler constructor.
     * Initiate a SS7Unit for all SS7 services that could be
     * found in MCR
     * @param r Handler for all responses from a XMP server.
     */
    SS7GatewayHandler(SS7ResponseHandler r) {
        setName("SS7UnitHandler");
        responseHandler               = r;
        connLimitMsgId                = log.createMessageId();
        unitLimitMsgId                = log.createMessageId();
        localfreeUnits                = new Hashtable<String, SS7Unit>();
        nonlocalfreeUnits             = new Hashtable<String, SS7Unit>();
        u_local_with_conn_prob        = new Hashtable<String, SS7Unit>();
        u_nonlocal_with_conn_prob     = new Hashtable<String, SS7Unit>();
        notConnectedHosts             = new HashMap<String, Object>();
        new Boolean("");
        new Boolean("");
        chooser                       = new SS7GatewaySorter();
        log.logMessage("Constructing SS7GatewayHandler " + "for SS7 service.", Logger.L_VERBOSE);
        sortXmpHostsInLocalAndNonlocal();
        new UpdateUnits(this);
    }

    /** Sorts all XMP servers in local and non local categories.
     */
    private void sortXmpHostsInLocalAndNonlocal(){
    	List<IServiceInstance> instList;
    	try {
    		instList = ExternalComponentRegister.getInstance().getServiceInstances(IServiceName.FIXED_MWI_NOTIFICATION);
    	}
    	catch (NoServiceFoundException e) {
    		log.logMessage("Could not find MCR info for service: " + IServiceName.FIXED_MWI_NOTIFICATION, Logger.L_ERROR);
    		e.printStackTrace(System.out);
    		return;
    	}
        
        chooser.updateClients(instList);
        localHosts = chooser.getLocalHosts();
        nonlocalHosts = chooser.getNonLocalHosts();
        if((localHosts.size() != 0 )&& (nonlocalHosts.size() != 0)){
            nonAndlocalUnitsExist = true;
        }
        else if(localHosts.size() != 0){onlyLocalUnitsExist = true;}
        else if(nonlocalHosts.size() != 0){onlyNonLocalUnitsExist = true;}
        initAllUnits();
    }

    /** Initiates all availible SS7Units so that they could be used.
     */
    private void initAllUnits(){
    	String host;
    	int port;
    	int id;
        if(localHosts != null){
        	id = 0;
            for(IServiceInstance inst : localHosts){
            	host = inst.getProperty(IServiceInstance.HOSTNAME);
            	try {
            		port = Integer.parseInt(inst.getProperty(IServiceInstance.PORT));
            	}
            	catch (NumberFormatException e) {
            		/**
            		 * skip that instance
            		 */
            		log.logMessage("SS7GatewayHandler.initAllUnits: port is not numeric for service:  " + inst.getServiceName(), Logger.L_ERROR);
            		e.printStackTrace(System.out);
            		continue;
            	}
                SS7Unit ou = new SS7Unit(id++, this,  responseHandler, host, port, true);
                localfreeUnits.put(host, ou);
            }

        }
        
        if(nonlocalHosts != null){
        	id = 0;
            for(IServiceInstance inst : nonlocalHosts){
            	host = inst.getProperty(IServiceInstance.HOSTNAME);
            	try {
            		port = Integer.parseInt(inst.getProperty(IServiceInstance.PORT));
            	}
            	catch (NumberFormatException e) {
            		/**
            		 * skip that instance
            		 */
            		log.logMessage("SS7GatewayHandler.initAllUnits: port is not numeric for service:  " + inst.getServiceName(), Logger.L_ERROR);
            		e.printStackTrace(System.out);            		
            		continue;
            	}
                SS7Unit ou = new SS7Unit(id++, this,  responseHandler, host, port, true);
                nonlocalfreeUnits.put(host, ou);
            }

        }
    }

    /** Reserves a SS7Unit, unil it has been released,
     * and return a local or a nonlocal SS7Unit.
     * @return a SS7Unit that could be used to send a XMP request.
     */
    public synchronized SS7Unit getUnit(){
        if(nonAndlocalUnitsExist){
            if(!localfreeUnits.isEmpty()){ return getLocalUnit(); }
            else{ return getNonLocalUnit(); }
        }
        else if(onlyLocalUnitsExist){
            return getLocalUnit();
        }
        else if(onlyNonLocalUnitsExist){
            return getNonLocalUnit();
        }
        else
            return null;
    }

    /** Get a local SS77nit from the free "local SS7Unit" list.
     * @return a local SS7Unit
     */
    private SS7Unit getLocalUnit() {
        SS7Unit u;
        try{
        log.logMessage("SS7GatewayHandler.getLocalUnit: Number of free connections is:"
                        + (Config.getMaxXmpConnections() - handlerCount), Logger.L_DEBUG);
        log.logMessage("Number of local free units " + localfreeUnits.size(), Logger.L_DEBUG);
        while(localfreeUnits.isEmpty() || handlerCount >= Config.getMaxXmpConnections()){
            log.logReduced(connLimitMsgId, "SS7GatewayHandler.getLocalUnit: connection limit reached", Logger.L_VERBOSE);
            log.logReduced(unitLimitMsgId, "SS7GatewayHandler.getLocalUnit: no available units at this time.", Logger.L_VERBOSE);
            try {wait();} catch (InterruptedException e) {}
        }
        u = localfreeUnits.remove(localfreeUnits.keys().nextElement());
        handlerCount++;
        log.logMessage("SS7GatewayHandler.getLocalUnit: reusing " + u.getName(), Logger.L_DEBUG);
        return u;
        }
        catch(Exception e){
            log.logMessage("SS7GatewayHandler.getLocalUnit: unknown error " + e, Logger.L_ERROR);
            return null;
        }
    }

    /** Get a non local SS7Unit from the "free non local" list
     * @return a non local SS7Unit
     */
    private SS7Unit getNonLocalUnit() {
        SS7Unit u;
        try{

            log.logMessage("SS7GatewayHandler.getNonLocalUnit: Number of free connections is:"
                            + (Config.getMaxXmpConnections() - handlerCount), Logger.L_DEBUG);
            log.logMessage("SS7GatewayHandler.getNonLocalUnit: Number of local free units "
                            + nonlocalfreeUnits.size(), Logger.L_DEBUG);
            while(nonlocalfreeUnits.isEmpty() || handlerCount >= Config.getMaxXmpConnections()){
                log.logReduced(connLimitMsgId, "SS7GatewayHandler.getNonLocalUnit: connection limit reached", Logger.L_VERBOSE);
                log.logReduced(unitLimitMsgId, "SS7GatewayHandler.getNonLocalUnit: no available units at this time.", Logger.L_VERBOSE);
                try {wait();} catch (InterruptedException e) {}
            }
            u = nonlocalfreeUnits.remove(nonlocalfreeUnits.keys().nextElement());
            handlerCount++;
            log.logMessage("SS7GatewayHandler.getNonLocalUnit: reusing " + u.getName(), Logger.L_VERBOSE);
            return u;
        }
        catch(Exception e){
            log.logMessage("SS7GatewayHandler.getLocalUnit: unknown error " + e, Logger.L_ERROR);
            return null;
        }
    }

    /** Remove the reserved SS7Unit from the reserved list.
     * @param ou The SS7Unit to release.
     */
    public synchronized void releaseUnit(SS7Unit ou){
        synchronized(notConnectedHosts){
            if(notConnectedHosts.containsKey(ou.getHost())){
                log.logMessage("SS7GatewayHandler.releaseUnit: Host  " + ou.getHost() + " cant be released. Host is registred in "
                               + " not connected hosts table.", Logger.L_DEBUG);
                handlerCount--;
                return;
            }
        }
        log.logMessage("SS7GatewayHandler.releaseUnit: Freeing SS7Unit " + ou.getName() + " . Is it local? " + ou.isLocal(), Logger.L_DEBUG);
        handlerCount--;
        if(ou.isLocal()) releaseLocalUnit(ou);
        else releaseNonLocalUnit(ou);
    }

    /** Adds the SS7Uniit to the free local list of SS7Units.
     * @param ou  the SS7Unit to add to the free local list of SS7Units.
     */
    private void releaseLocalUnit(SS7Unit ou){
        synchronized(this) {
            localfreeUnits.put(ou.getHost(), ou);
            this.notifyAll();
        }
    }

    /** Adds the SS7Unit to the free nonlocal list of SS7Units.
     * @param ou the SS7Unit to add to the free local list of SS7Units.
     */
    private void releaseNonLocalUnit(SS7Unit ou){
        synchronized(this) {
            nonlocalfreeUnits.put(ou.getHost(), ou);
            this.notifyAll();
        }
    }

    /** Register the host that has connection problem. If there is
     * a SS7Unit that is connected to this host, the SS7Unit will be
     * removed from the list of free SS7Units.
     * @param host The host that has connection problem.
     */
    public void reportConnectionProblem(String host){
        log.logMessage("reportConnectionProblem: Connection for " + host + " has connectionproblem.", Logger.L_VERBOSE);
        synchronized(notConnectedHosts){
            if(!notConnectedHosts.containsKey(host)){
                notConnectedHosts.put(host, null);
            }
        }
        if(localfreeUnits.containsKey(host)){
            u_local_with_conn_prob.put(host,localfreeUnits.get(host));
            localfreeUnits.remove(host);
        }
        else if(nonlocalfreeUnits.containsKey(host)){
            u_nonlocal_with_conn_prob.put(host,nonlocalfreeUnits.get(host));
            nonlocalfreeUnits.remove(host);
        }
        printLocalHosts();
        printNonLocalHosts();
    }

    /** Update all SS7Units with connection problem (connected to this host).
     * These SS7Units will be availible again to use.
     * @param host The host that had its connection problem solved.
     */
    public void reportConnectionEstablished(String host){
        log.logMessage("reportConnectionEstablished: Connection for " + host + " has been established.", Logger.L_VERBOSE);
        try{
            synchronized(notConnectedHosts){
                if(notConnectedHosts.containsKey(host)){
                    notConnectedHosts.remove(host);
                }
            }
            if(u_local_with_conn_prob.containsKey(host)){
                localfreeUnits.put(host, u_local_with_conn_prob.get(host));
                u_local_with_conn_prob.remove(host);
            }
            else if(u_nonlocal_with_conn_prob.containsKey(host)){
                nonlocalfreeUnits.put(host, u_nonlocal_with_conn_prob.get(host));
                u_nonlocal_with_conn_prob.remove(host);
            }
            printLocalHosts();
            printNonLocalHosts();
            synchronized(this){
                this.notifyAll();
            }
        }
        catch(Exception e){
            log.logMessage("reportConnectionEstablished: unknown error " + e , Logger.L_ERROR);
            return;
        }
    }

    private void printLocalHosts(){
        if(localfreeUnits.size() <= 0) return;
        log.logMessage("printLocalHosts: Local hosts not in use at the moment. ", Logger.L_DEBUG);
        Enumeration<String> e = localfreeUnits.keys();
        while(e.hasMoreElements()){
            String k = e.nextElement();
            log.logMessage("printLocalHosts:  Internal name: " + (localfreeUnits.get(k)).getName() +
                           " Hostname: " + (localfreeUnits.get(k)).getHost(), Logger.L_DEBUG);
        }
    }

    private void printNonLocalHosts(){
        if(nonlocalfreeUnits.size() <= 0) return;
        log.logMessage("printNonLocalHosts: Nonlocal hosts not in use at the moment. ", Logger.L_DEBUG);
        Enumeration<String> e = nonlocalfreeUnits.keys();
        while(e.hasMoreElements()){
            String k = e.nextElement();
            log.logMessage("printNonLocalHosts:  Internal name: " + (nonlocalfreeUnits.get(k)).getName() +
                           " Hostname: " + (nonlocalfreeUnits.get(k)).getHost(), Logger.L_DEBUG);
        }
    }
}
