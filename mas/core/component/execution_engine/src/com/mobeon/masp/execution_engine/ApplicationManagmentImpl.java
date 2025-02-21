/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.UnknownGroupException;
import com.mobeon.masp.execution_engine.compiler.IApplicationCompiler;
import com.mobeon.masp.execution_engine.components.ApplicationCompilerComponent;
import com.mobeon.masp.execution_engine.components.ApplicationComponent;
import com.mobeon.masp.execution_engine.components.ApplicationManagmentComponent;
import com.mobeon.masp.execution_engine.configuration.*;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;
import com.mobeon.masp.execution_engine.session.ISessionFactory;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.util.component.IComponentManager;
import com.mobeon.masp.util.component.SpringComponentManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.operateandmaintainmanager.IlegalServiceParametersException;
import com.mobeon.masp.operateandmaintainmanager.ProvidedService;
import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerOperate;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author David Looberger
 *         <p/>
 *         TODO:
 */
@ConfigurationParameters({
        ParameterId.ApplicationManagement_MapServiceToApplicationURI,
        ParameterId.ApplicationManagement_AlwaysCompile,
        ParameterId.ApplicationManagement_HostName,
        ParameterId.ApplicationManagement_WatchdogTimeout
        })
public class ApplicationManagmentImpl extends Configurable implements ApplicationManagmentComponent {

    private static ILogger log = ILoggerFactory.getILogger(ApplicationManagmentImpl.class);
    
    private static final String T_IPADDRESS = System.getenv("t_IPADDRESS");

    Context ctx = new Context();

    private static ParameterBlock parameterBlock = new ParameterBlock();
    private Map<String, String> serviceMap;
    private Map<String, ArrayList<String>> protocolMap;   // mapping between Protocol and ProvidedService and
    private Map<String, ServiceEnabler> protocolServiceEnablerMap; // mapping between Protocol and ServiceEnabler and
    private Supervision supervision;
    private MediaTranslationManager mediaTranslationManager;
    private IServiceRequestManager serviceRequestManager;
    private String hostName = "localhost";

    /**
     * We use a future so that several concurrent loads won't start a truckload of
     * competing threads, all trying to compile the application.
     */
    private static ConcurrentHashMap<URI, ApplicationComponent> applications = new ConcurrentHashMap<URI, ApplicationComponent>();
    private Object[] locks = new Object[]{new Object(), new Object(), new Object(), new Object()};
    private final int NUM_LOCKS = locks.length;


    public ParameterBlock getParameterBlock() {
        return parameterBlock;
    }

    public ApplicationManagmentImpl() {
        serviceMap = defaultMap(ParameterId.ApplicationManagement_MapServiceToApplicationURI);
    }

    /**
     * @logs.error "Failed to initialize service <service>: <reason>" - The service <service> could not be initialized. The <reason> should describe what caused this error.
     * @logs.error "Error while creating provided service entry: <reason>" - The <reason> should describe what caused this internal error
     */
    public void init() throws ServiceEnablerException {
        
        hostName = readString(ctx.getConfigurationManager(),
                ParameterId.ApplicationManagement_HostName,
                RuntimeConstants.CONFIG.HOSTNAME,
                log);

        if (hostName == null || hostName.isEmpty()) {
            if (T_IPADDRESS != null && !T_IPADDRESS.isEmpty()) {

                // ex: xmp:172.16.32.73;sip:172.16.32.73
                // Where xmp is the current host's traffic IP address
                // Where sip is the current host's traffic IP address.

                hostName = "xmp:" + T_IPADDRESS + ";sip:" + T_IPADDRESS;
                log.debug("ApplicationManagmentImpl:init() : " + RuntimeConstants.CONFIG.HOSTNAME + "=" + hostName + ". Built with Env. vars. T_IPADDRESS.");
            } else {
                hostName = "localhost";
                log.debug("ApplicationManagmentImpl:init() : " + RuntimeConstants.CONFIG.HOSTNAME + "=" + hostName + ". No configuration, no Env. vars.");
            }
        } else {
            log.debug("ApplicationManagmentImpl:init() : " + RuntimeConstants.CONFIG.HOSTNAME + "=" + hostName + ". Built from configuration.");
        }
        
        
        ApplicationWatchdog.setTimeout(readInteger(ctx.getConfigurationManager(),
                ParameterId.ApplicationManagement_WatchdogTimeout,
                RuntimeConstants.CONFIG.WATCHDOG_TIMEOUT,
                log));
        // set CallManager properties
        ctx.getCallManager().setApplicationManagment(this);
        ctx.getCallManager().setSupervision(supervision);


        for (Map.Entry service : serviceMap.entrySet()) {
            load(service.getKey().toString());
        }

        // register provided services in O&M
        // get all serviceenablers.
        for (Map.Entry<String, ServiceEnabler> protocol : protocolServiceEnablerMap.entrySet()) {
            ServiceEnabler se = protocol.getValue();
            // get list of provided services
            ArrayList<String> providedServices = protocolMap.get(protocol.getKey());
            if (providedServices == null) {
                log.debug("No provided services configured for protocol:" + protocol.getKey());
                continue;
            }
            for (String providedService : providedServices) {
                // providedService is a string with format providedServiceName:port
                StringTokenizer st = new StringTokenizer(providedService, ":");
                String providedServiceName = st.nextToken();
                Integer port = Integer.parseInt(st.nextToken());

                // register the provided service in O&M with the ServiceEnabler

                ServiceEnablerOperate seo = null;
                try {
                    seo = se.initService(providedServiceName, getHostnameForProtocol(protocol.getKey(), hostName), port);
                } catch (ServiceEnablerException e) {
                    log.error("Failed to initialize service " + providedServiceName + ":" + e);
                    throw e;
                }

                try {
                    String hostnameForProtocol = getHostnameForProtocol(protocol.getKey(), hostName);
                    if (log.isDebugEnabled()) log.debug("Creating provided service: "+providedServiceName+":"+hostnameForProtocol+":"+port+":"+seo);
                    ProvidedService ps = supervision.createProvidedServiceEntry(providedServiceName,
                            hostnameForProtocol,
                            port,
                            seo);
                } catch (IlegalServiceParametersException e) {
                    log.error("Error while creating provided service entry:" + e.getMessage());
                }

            }
        }
        
        

    }

    /**
     * Hostname in the config file can have format "xmp:host1;sip:host2" or just "host1".
     * Find the name according to the protocol. For "xmp" in the example above "host1" would be returned
     * for both the string "xmp:host1;sip:host2" and "host1".
     * @param hostName
     * @return the hostName for the service
     */
    private String getHostnameForProtocol(String protocolName, String hostName) {

        if(hostName.indexOf(";") == -1 && hostName.indexOf(":") == -1){
            return hostName;
        } else {
            StringTokenizer tok1 = new StringTokenizer(hostName, ";");
            while(tok1.hasMoreTokens()){
                String oneProtocolAndHostName = tok1.nextToken();
                StringTokenizer tok2 = new StringTokenizer(oneProtocolAndHostName, ":");
                String protocol = "";
                String hostForProtocol = "";
                if(tok2.hasMoreElements()){
                    protocol = tok2.nextToken();
                }
                if(tok2.hasMoreElements()){
                    hostForProtocol = tok2.nextToken();
                }
                if(protocol.equals(protocolName)){
                    return hostForProtocol;
                }
            }
        }
        return "";
    }

    /**
     * It is possible to get a compiled application from an URI.
     * If the the URI is not available in the repository in compiled
     * format it is retrieved using the IApplicationCompiler interface.
     *
     * @param uri The URI of the application description file
     * @return An IApplicationExection instance
     */
    public IApplicationExecution getAppFromURI(final URI uri, String service) {
        try {
            ApplicationComponent application = applications.get(uri);
	            boolean alwaysCompile = readBoolean(ctx.getConfigurationManager(),
	                    ParameterId.ApplicationManagement_AlwaysCompile,
	                    RuntimeConstants.CONFIG.ALWAYS_COMPILE,
	                    log);
	
	            if (null == application || alwaysCompile) {
	                if (log.isDebugEnabled()) {
	                    // A test case relies on this log:
	                    log.debug("Will always compile:" + alwaysCompile);
	                }
	
		            
		                //Enable concurrent compilation of NUM_LOCKS applications on average.
		                synchronized (locks[Math.abs(uri.hashCode() % NUM_LOCKS)]) {
		                    if (!applications.contains(uri)) {
		                    	//TODO compiler should know how to generate the java class
		                        application = (ApplicationComponent) ctx.getApplicationCompiler().compileApplication(uri);
		                        if (application == null) {
		                            return null;
		                        }
		                        applications.put(uri, application);
		                    }
		                } 
	            }
	            return ctx.getApplicationExecutionFactory().create(service,
	                    ctx.toApplicationExecutionFactory(),
	                    application, supervision, mediaTranslationManager,
	                    serviceRequestManager);
        } catch (Throwable t) {
            if (log.isInfoEnabled()) log.info("Compilation failed", t);
        }
        return null;
    }


	/**
     * Loads an application based on a known service-name.
     * <p/>
     * The mapping between a service-name and it's URI is configurable
     * by the {@linkplain #setMapServiceToApplicationURI MapServiceToApplicationURI} parameter
     *
     * @param service The name of the sought service
     * @return An IApplicationExection instance
     */
    public IApplicationExecution load(String service) {
        IApplicationExecution iae = null;
        Object perf = CommonOamManager.profilerAgent.enterCheckpoint("MAS.EE.ApplExec.load");
        
	    try {
	
	        // Do not log error logs here, it is the responsibility of the
	        // calling component to log errors.
	
	        if (service != null) {
	            String url = serviceMap.get(service);
	            if (url == null) {
	                if (log.isInfoEnabled()) log.info("Could not find URL for service " + service);
	            } else {
	                if (url != null) {
	                    url = Tools.relativize(url);
	                }
	                try {
	                    iae = getAppFromURI(new URI(url), service);
	                } catch (URISyntaxException e) {
	                    if (log.isInfoEnabled()) log.info("Requested service has invalid url " + url);
	                }
	                if (iae == null) {
	                    if (log.isInfoEnabled())
	                        log.info("Requested service " + service + " at URI " + url + " couldn't be fetched or executed");
	                }
	            }
	        } else {
	            if (log.isInfoEnabled()) log.info("Received an empty request, nothing to look for");
	        }
	        return iae;
        } finally {
	       	 if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	             CommonOamManager.profilerAgent.exitCheckpoint(perf);
	         }
        }
    }

    public Context getContext() {
        return ctx;
    }

    public Map<String, String> getServiceMap() {
        return serviceMap;
    }

    @MapParameter(
            description = "Mapping of service name to URI's",
            displayName = "Service mapping",
            configName = "serviceurimapping",
            parameter = ParameterId.ApplicationManagement_MapServiceToApplicationURI,
            keyValidator = Validators.SERVICE_VALIDATOR,
            valueValidator = Validators.URI_VALIDATOR,
            defaultValue = {"default", "file:///start.ccxml"}
    )
    /**
     * @logs.warn "Invalid configuration attempted for the service map, this caused a recoverable exception <message>" - The service map (map from services to applications) is misconfigured. <message> should give more information about the problem.
     */
    public void setMapServiceToApplicationURI(Map serviceMap) {
        try {
            this.serviceMap = convert(ParameterId.ApplicationManagement_MapServiceToApplicationURI, serviceMap);
        } catch (InvalidConfigurationException e) {
            log.warn("Invalid configuration attempted for the service map, this caused a recoverable exception", e);
        }
    }

    // @MapParameter(
    // description = "Mapping of protocol name to service",
    // displayName = "Protocol mapping",
    // parameter = ParameterId.ApplicationManagement_MapProtocolToService,
    // keyValidator = Validators.SERVICE_VALIDATOR,
    // valueValidator = Validators.SERVICE_LIST_VALIDATOR
    // )
    public void setMapProtocolToService(Map<String, ArrayList<String>> protocolMap) {
        //     try {
        this.protocolMap = protocolMap;
        //         this.protocolMap = convert(ParameterId.ApplicationManagement_MapProtocolToService, protocolMap);
        //     } catch (InvalidConfigurationException e) {
        //         log.warn("Invalid configuration attempted, this caused a recoverable exception", e);
        //     }
    }

    //  @MapParameter(
    //   description = "Mapping of protocol name to service enabler",
    //   displayName = "ServiceProtocol mapping",
    //   parameter = ParameterId.ApplicationManagement_MapProtocolToServiceEnabler,
    //   keyValidator = Validators.PROTOCOL_VALIDATOR,
    //   valueValidator = Validators.SERVICEENABLER_VALIDATOR
    //   )
    public void setMapProtocolToServiceEnabler(Map<String, ServiceEnabler> protocolMap) {
        //     try {
        this.protocolServiceEnablerMap = protocolMap;
    }


    public void setSupervision(Supervision supervision) {
        this.supervision = supervision;
    }

    @BooleanParameter(
            description = "Should compiled URI's be cached, or always recompiled",
            displayName = "Always Compile",
            configName = "alwayscompile",
            parameter = ParameterId.ApplicationManagement_AlwaysCompile,
            defaultValue = false
    )

    @StringParameter(
            description = "hostname for this host",
            displayName = "hostname",
            configName = "hostname",
            parameter = ParameterId.ApplicationManagement_HostName,
            defaultValue = "localhost"
    )

    public void setApplicationExecutionFactory(ApplicationExecutionFactory applicationExecutionFactory) {
        this.ctx.setApplicationExecutionFactory(applicationExecutionFactory);
    }

    public void setSessionFactory(ISessionFactory sessionFactory) {
        this.ctx.setSessionFactory(sessionFactory);
    }

    public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
        this.ctx.setMediaObjectFactory(mediaObjectFactory);
    }

    public void setApplicationCompiler(IApplicationCompiler applicationCompiler) {
        this.ctx.setApplicationCompiler((ApplicationCompilerComponent) applicationCompiler);
    }

    public void setPlatformAccessFactory(PlatformAccessFactory platformAccessFactory) {
        this.ctx.setPlatformAccessFactory(platformAccessFactory);
    }

    public void setCallManager(CallManager callManager) {
        this.ctx.setCallManager(callManager);
    }


    public MediaTranslationManager getMediaTranslationManager() {
        return mediaTranslationManager;
    }

    public void setMediaTranslationManager(MediaTranslationManager mediaTranslationManager) {
        this.mediaTranslationManager = mediaTranslationManager;
    }

    public void setServiceRequestManager(IServiceRequestManager serviceRequestManager) {
        this.serviceRequestManager = serviceRequestManager;
    }

    public void setConfigurationManager(IConfigurationManager configurationManager) {
        ctx.setConfigurationManager(configurationManager);
    }


    /**
     * Removes all cached applications, used for
     * testing purposes.
     */
    public void clearApplications() {
        applications.clear();
    }

	 /**
     * Reads the configuration for Tools.
     * 
     * @param config The instance that holds the configurations for Tools.
     * @throws UnknownGroupException
     * @throws GroupCardinalityException
     */
    public synchronized void setConfiguration(final IConfiguration config)
            throws GroupCardinalityException, UnknownGroupException {
        ApplicationConfiguration.getInstance().setInitialConfiguration(config);
        ApplicationConfiguration.getInstance().update();
    }
}
