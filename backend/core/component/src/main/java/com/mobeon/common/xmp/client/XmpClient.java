/*
 * XmpClient.java
 *
 * Created on den 7 oktober 2005, 15:59
 */

package com.mobeon.common.xmp.client;

import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.util.logging.ILogger;
import com.mobeon.common.xmp.XmpAttachment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;



public class XmpClient implements XmpResponseHandler {
    /** map of all serviceunitlists. indexed by service name */
    private HashMap<String, XmpServiceUnitList> xmpServices;

    /** map of all units. indexed by "host + : + port" */
    private ConcurrentHashMap<String,XmpUnit> xmpUnits =
            new ConcurrentHashMap<String, XmpUnit>();

    private AtomicInteger unitCount = new AtomicInteger(0);

    private static XmpClient inst;
    private String logicalZone;

    private RefreshThread refreshThread = null;

    private XmpManagementHandler managementHandler = null;

    private XmpPendingRequests pendingRequests;

    private int transId = 1;

    private int timeout = 30;
    private int validity = 90;
    private String clientId = "";
    /** Max number of connections to one server */
    private int maxConnections = 3;
    
    /** time between refresh of unit */
    private int refreshTime = 30;
    
    private int pollInterval = 90;

    private boolean keepRunning = true;

    private ILogger log = null;


    private XmpClient() {
        xmpServices = new HashMap<String, XmpServiceUnitList>();
        pendingRequests = new XmpPendingRequests(this);
    }

    /**
     *Get the singleton instance.
     *@return the only XmpClient.
     */
    public static XmpClient get() {
        if( inst == null ) {
            inst = new XmpClient();
        }
        return inst;
    }

    /**
     *Sets a list of components for a service.
     *The existing list is replaced by this list.
     *Use this method as an alternative to setComponentRegister
     *@param components - A list of components.
     */
    public void setComponents(String service, List<IServiceInstance> instList) {
        // find service, create if not exist
        XmpServiceUnitList list = xmpServices.get(service);
        if( list == null ) {
            list = new XmpServiceUnitList(service);
            xmpServices.put(service, list);
        }

        // loop in components, check if units exist and create otherwise.
        String host, componentName, key;
        int port;
        for(IServiceInstance inst : instList) {
        	try {
        		port = Integer.parseInt(inst.getProperty(IServiceInstance.PORT));
        	}
        	catch (NumberFormatException e) {
        		error("Exception, port is not numeric for service name: " + service);
        		e.printStackTrace(System.out);
        		continue;
        	}
        	
        	host = inst.getProperty(IServiceInstance.HOSTNAME);
        	componentName = inst.getProperty(IServiceInstance.COMPONENT_NAME);
        	key = host + ":" + port;
        	XmpUnit unit = (XmpUnit) xmpUnits.get(key);
        	
        	if( unit == null ) {
        		unit = new XmpUnit(unitCount.getAndIncrement(), host, port, this, isLocal(inst), componentName);
        		unit.setUnitName(service, componentName);
        		xmpUnits.put(key, unit);
        	}
        	
        	if( !list.hasUnit(host, port)) {
        		unit.setUnitName(service, componentName);
        		list.addUnit(unit);
        	} else {
        		unit.setLocal(isLocal(inst));
        	}
        }

        // get units in service unit list. remove units not in components list
        Vector serviceUnits = list.getUnits();
        for( int i=0;i<serviceUnits.size();i++ ) {
        	boolean found = false;
        	XmpUnit u = (XmpUnit) serviceUnits.get(i);

        	for(IServiceInstance inst : instList) {
        		try {
        			port = Integer.parseInt(inst.getProperty(IServiceInstance.PORT));
        		}
        		catch (NumberFormatException e) {
        			error("Exception, port is not numeric for service name: " + service);
        			e.printStackTrace(System.out);
        			port = -1;
        		}
        		host = inst.getProperty(IServiceInstance.HOSTNAME);
        		if( host.equals(u.getHost()) && (port == u.getPort())) {
        			found = true;
        		}
        	}
        	if( !found ) {
        		list.removeUnit(u);
        	}
        }

        list.updateLocals();

        // remove units that no longer exists.
    }

    /**
     *Makes all disabled XmpUnits available again.
     */
    public void refreshStatus() {
        // loop in services. Set unit available.
        Iterator<String> iterator = xmpServices.keySet().iterator();
        while(iterator.hasNext()) {
            String service = iterator.next();
            XmpServiceUnitList list = xmpServices.get(service);
            if( list != null ) {
                Vector<XmpUnit> units = list.getUnits();
                for( int i=0;i<units.size();i++ ) {
                    XmpUnit unit = units.get(i);
                    list.serviceAvailable(unit);
                }
            }
        }

    }

    /**
     *Starts a thread that calls refreshStatus regularly.
     *uses refreshtime as sleeptime between refresh calls.
     */
    public void startRefresher() {
        refreshThread = new RefreshThread();
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
    
    /**
     *Gets a unique transaction id that is used in sendrequest
     */
    public synchronized int nextTransId() {
        return transId++;
    }


    public boolean sendRequest(int transId, String request, String service, XmpResultHandler resultHandler ) {
        return sendRequest(transId, request, service, resultHandler, null);
    }

    /**
     *Sends a request on the specified service.
     *@param transId an unique id, use getNextTransId to get the id.
     *@param service the service to send the reqeuest to.
     *@param request the XmpRequest
     *@param resultHandler where to send result to.
     *@param attachments A number of attachments, can be null if no attachments exist.
     *@return true if the request was sent ok.
     */
    public boolean sendRequest(int transId, String request, String service, XmpResultHandler resultHandler, XmpAttachment[] attachments ) {
        debug("XmpClient received " + service + " request " + transId);
        XmpServiceUnitList ul = xmpServices.get(service);
        synchronized (xmpServices) {
            if (ul == null ) {
                List<IServiceInstance> instList;
                try {
                	instList = ExternalComponentRegister.getInstance().getServiceInstances(service);
                }
                catch (NoServiceFoundException e) {
                	error("Received request for non-existing service: " + service);
                	/**
                	 * initialize an empty array to parse it to the setComponents method
                	 */
                	instList = new ArrayList<IServiceInstance>();
                	e.printStackTrace(System.out);
                }
                
                setComponents(service, instList);
                ul = xmpServices.get(service);
                if (ul == null) {
                    error("Received request for non-existing service: " + service);
                    reportToMema(service, false);
                    return false;
                }
            }
        }

        XmpUnit u = ul.selectUnit();
        if (u == null) {
            warn("No units provide the XMP service " + service);
            return false;
        } else {
            XmpRequestInfo info = new XmpRequestInfo(transId,resultHandler,u,request,service, attachments);
            pendingRequests.put(info);
            info.setXmpUnitTried(u.getName()); // Flag that it has been tried
            if (!u.sendRequest(info)) {
                pendingRequests.remove(Integer.valueOf(transId));
                reportToMema(service, false);
                return false;
            }
        }
        return true;
    }

    public boolean sendRequestToComponent(int transId,
                                          String request,
                                          String service,
                                          XmpResultHandler resultHandler,
                                          IServiceInstance instance ) {
        return sendRequestToComponent(transId, request, service, resultHandler, instance, null);
    }

    public boolean sendRequestToComponent(int transId,
                                          String request,
                                          String service,
                                          XmpResultHandler resultHandler,
                                          IServiceInstance instance,
                                          XmpAttachment[] attachments ) {
        String host = instance.getProperty(IServiceInstance.HOSTNAME);
        String componentName = instance.getProperty(IServiceInstance.COMPONENT_NAME);
        int port;
        try {
        	port = Integer.parseInt(instance.getProperty(IServiceInstance.PORT));
        }
        catch (NumberFormatException e) {
        	log.error("port is not numeric for service name: " + instance.getServiceName());
        	e.printStackTrace(System.out);
        	return false;
        }
        
        String key = host + ":" + port;
        // Atomically check if a unit alreay exist, and if not add it to the map.
        XmpUnit u = xmpUnits.putIfAbsent(key,
                new XmpUnit(unitCount.getAndIncrement(), host, port,
                        this, isLocal(instance), componentName));
        // If the unit did not exist, null is returned so wee must get it again.
        if (u == null) {
            if (log.isDebugEnabled())
                log.debug("No XmpUnit " + key + " was found, added a new.");
            u = xmpUnits.get(key);
        }

        XmpRequestInfo info =
                new XmpRequestInfo(transId, resultHandler, u, request, service, attachments);
        pendingRequests.put(info);
        info.setXmpUnitTried(u.getName()); // Flag that it has been tried
        if (!u.sendRequest(info)) {
            pendingRequests.remove(transId);
            reportToMema(service, false);
            return false;
        }
        return true;
    }

    public int getValidity() {
        return validity;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRefreshTime() {
        return refreshTime;
    }
    
    public String getLogicalZone() {
        return logicalZone;
    }

    public String getClientId() {
        return clientId;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public boolean isKeepRunning() {
        return keepRunning;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public void setLogicalZone(String zone) {
        this.logicalZone = zone;
    }

    public void setClientId(String id) {
        this.clientId = id;
    }

    public void setMaxConnections(int count) {
        this.maxConnections = count;
    }

    public void setLogger(ILogger logger) {
        this.log = logger;
    }

    public ILogger getLog() {
        return log;
    }

    public void setPollInterval(int interval) {
        this.pollInterval = interval;
    }

    public void setManagementHandler(XmpManagementHandler handler) {
        this.managementHandler = handler;
    }

    /**
     *Tries to stop all threads in the client package.
     */
    public void stop() {
        keepRunning = false;
    }

    void debug(String message) {
        if( log != null ) {
            log.debug(message);
        }
    }

    void debug(String message, Throwable t) {
        if( log != null ) {
            log.debug(message,t);
        }
    }

    void info(String message) {
        if( log != null ) {
            log.info(message);
        }
    }

    void info(String message, Throwable t) {
        if( log != null ) {
            log.info(message,t);
        }
    }

    void warn(String message) {
        if( log != null ) {
            log.warn(message);
        }
    }

    void warn(String message, Throwable t) {
        if( log != null ) {
            log.warn(message,t);
        }
    }

    void error(String message) {
        if( log != null ) {
            log.error(message);
        }
    }

    void error(String message, Throwable t) {
        if( log != null ) {
            log.error(message,t);
        }
    }

    void fatal(String message) {
        if( log != null ) {
            log.fatal(message);
        }
    }

    void fatal(String message, Throwable t) {
        if( log != null ) {
            log.fatal(message,t);
        }
    }

    boolean isDebugEnabled() {
        if( log != null && log.isDebugEnabled() ) {
            return true;
        }
        return false;
    }

    void setStatus(boolean up, String service, String instance) {
        if(managementHandler != null ) {
            if( up) {
                managementHandler.statusUp(service, instance);
            } else {
                managementHandler.statusDown(service, instance);
            }
        }
    }

    /**
     * Handles the response to an XMP request. The request information is
     * retrieved from the collection of pending requests and the feedback is
     * given to the requests notification group.
     *@param result - the XML document containing the result.
     */
    public void handleResponse(String result, XmpUnit unit, ArrayList attachments) {
        /* When this class was written, no XMP services had any response parameters,
         * so XmpClient can take care of the complete handling of responses, but in
         * the general case the final handling should be left to the
         * service-dependent "XxxOut" classes.
         */
        XmpResult res = XmpProtocol.parseResponse(result, this, attachments);
        if (res != null && res.getTransactionId() != 0) {
            XmpRequestInfo info =
                (XmpRequestInfo) pendingRequests.remove(Integer.valueOf(res.getTransactionId()));
            if (info == null) {
                error("Response for non-existing or expired transaction " + res.getTransactionId()
                               + ", result=" + result);
                unit.tryEmptyRequest();
            } else {
                String action = XmpErrorCodesConfig.getErrorAction(info.service, res.getStatusCode() );
                debug("Using action " + action + " for code " + res.getStatusCode() );
                if( action.equals("remove")) {
                    reportToMema(info.service, false);
                    handleRemove(info, res);
                } else if( action.equals("next")) {
                    reportToMema(info.service, false);
                    handleNext(info, res);
                } else if( action.equals("report")) {
                    reportToMema(info.service, false);
                    info.resultHandler.handleResult(res);
                } else {
                    // "pass" as default
                    reportToMema(info.service, true);
                    info.resultHandler.handleResult(res);
                }

            }
        }
    }

    /**
     * handles what to do when the code "Service Not Available" is returned.
     **/
    private void handleRemove(XmpRequestInfo info, XmpResult res) {
        // put unit in serviceUnavailableList
        XmpServiceUnitList ul = xmpServices.get(info.service);
        if( ul != null ) {
            ul.serviceUnavailable(info.unit);
            //ManagementStatus mStatus =
            //    ManagementInfo.get().getStatus(info.service, info.unit.getMcrName());
            //if( mStatus != null ) {
            //    mStatus.down();
            //}
            handleNext(info, res);
        } else {
            info.resultHandler.handleResult(res);
        }
    }

    /**
     *Tries the next unit.
     */
    private void handleNext(XmpRequestInfo info, XmpResult res) {


        XmpServiceUnitList ul = xmpServices.get(info.service);

        int nrSelects = 0;
        XmpUnit u = null;
        if( ul != null ) {
          // make new call
          int unitCount = ul.getUnits().size() - ul.getUnAvailableUnits().size();
          int maxSelects = unitCount*10; 
          // To avoid a lot of selectUnit if we are extremely unlucky.
          if (unitCount == 0) {
            // TR 30679 - no use trying anything else.
            info.resultHandler.handleResult(res);
            return;
          }
            /* If the unit is already tried, select another unit.
               Theoretically this can loop for a while, but this is to avoid
               messing with the round robin scheme in selectUnit */
            do {
                u = ul.selectUnit();
                // Update unit count in case something became unavailabe.
                unitCount =  ul.getUnits().size() - ul.getUnAvailableUnits().size();
                if (unitCount == 0 || u == null) {
                  // TR 30679 - no use trying anything else.
                  info.resultHandler.handleResult(res);
                  return;
                }
                nrSelects++;                
            } while (info.isXmpUnitTried(u.getName()) 
                     && info.getTriedUnitsCount() < unitCount 
                     && nrSelects < maxSelects);
            debug("Selected XMP Unit: " +u.getName() + " after " + nrSelects + " retries.");

            if (info.getTriedUnitsCount() >= unitCount || 
                nrSelects == maxSelects) {
                /* No use trying anymore, fall back to "pass" the result to caller */
                info.resultHandler.handleResult(res);
                return;
            }

            if (u != null) {
                try {
                    Thread.sleep(500);
                } catch(Exception e) {}

                info.unit = u;
                info.renewExpiryTime();
                int newId = nextTransId();
                info.request = XmpProtocol.updateTransId(info.request, info.id, newId );
                info.id = newId;
                pendingRequests.put(info);
                info.setXmpUnitTried(u.getName()); // Flag that it has been tried
                if (u.sendRequest(info)) {
                    return;
                } else {
                    pendingRequests.remove(Integer.valueOf(info.id));
                    // if we cant send anything take the resultcode from the original request and send back.
                }
            }
        }
        info.resultHandler.handleResult(res);
    }

    /**
     * Decides what to do when an timeout has occured.
     * @param info, Info about the request that timed out.
     */
    /*package*/ void handleTimeout(XmpRequestInfo info) {
        debug("Timeout when sending id " + info.id  + " in service " + info.service);
        XmpResult res = new XmpResult(info.id, 408, "Internal timeout in XMP", null);
        handleRemove(info, res );

     }


    /*package*/void removeUnit(XmpUnit unit){
        if (log.isDebugEnabled())
             log.debug("Removing unit "+unit);
        String key = unit.getHost() + ":" + unit.getPort();
        xmpUnits.remove(key);
    }


    /**
     *failed to send request from XmpConnection.
     *Set the unit to unavailable.
     *@param unit the unit that sent the request.
     *@param transId the id of the request
     */
    public void sendFailed(XmpUnit unit, int transId) {

        if( transId != 0 ) {
            XmpRequestInfo info = (XmpRequestInfo) pendingRequests.remove(Integer.valueOf(transId));
            if (info == null) {
                error("Send failed for non-existing or expired transaction " + transId );
            } else {
                // treat sendfailed as temporary problem, for now take service unavailable code.
                XmpResult res = new XmpResult(transId, 421, "Failed to send request", null );
                handleNext(info, res);
            }
        }
    }
    
    /**
     * Checks if a component is local. A component is local if it is in the same
     * logical zone as NTF. If the logical zone is missing, it is local if NTF and
     * the component are on the same network.
     *@param comp - the component that shall be checked.
     *@return true iff the component is local to this NTF.
     */
    private boolean isLocal(IServiceInstance inst) {
    	String instLogicalZone = inst.getProperty(IServiceInstance.LOGICALZONE);
        if (logicalZone != null
            && (! "".equals(logicalZone))
            && instLogicalZone != null
            && (! "".equals(instLogicalZone))) {
            return logicalZone.equals(instLogicalZone);
        } else {
            return false;
        }
    }
    
    public XmpServiceUnitList getList(String service) {
        return (XmpServiceUnitList) xmpServices.get(service);
    }
    
    private void reportToMema(String service, boolean success) {
        if( managementHandler != null ) {
            if( success ) {
                managementHandler.sendOk(service);
            } else {
                managementHandler.sendFailed(service);
            }
        }
    }
    
    /**
     * Thread that reoloads components and sets the status to ok
     */
    private class RefreshThread extends Thread {
        
        public RefreshThread() {
            super("XmpRefreshThread");
        }
        
        public void run() {
            while(keepRunning) {
                try {
                    refreshStatus();
                    sleep(getRefreshTime() * 1000 );
                } catch (InterruptedException ire) {
                    // do nothing
                } catch (Exception e) {
                    error("Unknown exception in refreshThread: " + e.toString(), e);
                }
            }
        }
    }
    
}
