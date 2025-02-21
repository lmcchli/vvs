/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.MulticastDispatcher;
import com.mobeon.masp.execution_engine.components.ApplicationComponent;
import com.mobeon.masp.execution_engine.components.ApplicationExecutionComponent;
import com.mobeon.masp.execution_engine.runtime.RuntimeControl;
import com.mobeon.masp.execution_engine.runtime.RuntimeFacade;
import com.mobeon.masp.execution_engine.runtime.RuntimeFactories;
import com.mobeon.masp.execution_engine.runtime.RuntimeFactory;
import com.mobeon.masp.execution_engine.runtime.event.EndSessionEvent;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;

public class ApplicationExecutionImpl implements ApplicationExecutionComponent {
    private static ILogger log = ILoggerFactory.getILogger(ApplicationExecutionImpl.class);


    private Context ctx;

    private ApplicationComponent application;
    private ISession session;
    private MulticastDispatcher eventSender = new MulticastDispatcher();
    private RuntimeFacade master;
    private boolean initialized = false;
    private EventStream parentStream;
    private Supervision supervision = null;
    private MediaTranslationManager mediaTranslationManager = null;
    private IServiceRequestManager serviceRequestManager;
    private String serviceName;

    public ApplicationExecutionImpl(String serviceName, Context ctx, ApplicationComponent app, Supervision supervision, MediaTranslationManager mediaTranslationManager, IServiceRequestManager serviceRequestManager) {
        this.serviceName = serviceName;
        this.ctx = ctx;
        this.application = app;
        this.supervision = supervision;
        this.mediaTranslationManager = mediaTranslationManager;
        this.serviceRequestManager = serviceRequestManager;
    }

    public RuntimeFacade getMaster() {
        return master;
    }

    /**
     * @logs.error "ApplicationImpl root document not specified, unable to create master execution engine" - None of the documents in the application is specified with root="true" attribute
     */
    public boolean createRuntimes() {
        if (application == null
                || ctx.getMediaObjectFactory() == null
                || eventSender == null)
            return false;
        if (initialized)
            return true;
        if (application.getRoot() == null) {
            log.error("ApplicationImpl root document not specified, unable to create master execution engine");
            return false;
        }
        RuntimeFactory factory = RuntimeFactories.getInstance(application.getRoot().getMimeType());
        master = factory.createRuntime(ctx.getConfigurationManager(), ctx.getPlatformAccessFactory(),
                ctx.getMediaObjectFactory(), getSession(),
                application.getRoot().getEntry(),
                application.getRoot(),
                ctx.getCallManager(),
                eventSender, supervision,
                mediaTranslationManager, serviceRequestManager);
        session.setData(ISession.MASTER_EXECUTION_CONTEXT, master.getExecutionContext());
        session.setData(ISession.SERVICE_NAME, serviceName);

        //Create parent stream ( for symmetry )
        //TODO: Unsubscribe from eventreceiver at some point ..
        parentStream = new EventStream();
        EventStream.Injector injector = parentStream.new EventReceiverInjector(EventRules.TRUE_RULE, EventRules.FALSE_RULE, eventSender);
        parentStream.add(injector);

        //Enable sending to stream
        EventStream ccxmlStream = master.getExecutionContext().getEventStream();
        parentStream.swapUpstream(ccxmlStream);


        RuntimeControl rc = new RuntimeControl(ctx.getConfigurationManager(),
                ctx.getPlatformAccessFactory(), ctx.getMediaObjectFactory(),
                getSession(), application, master, ctx.getCallManager(),
                ccxmlStream, eventSender, supervision, mediaTranslationManager, serviceRequestManager);

        EventStream.Extractor runtimeExtractor = ccxmlStream.new Extractor(EventRules.FALSE_RULE, EventRules.TRUE_RULE, rc);
        ccxmlStream.add(runtimeExtractor);

        initialized = true;
        return true;
    }


    public ISession getSession() {
        return session;
    }

    public IEventDispatcher getEventDispatcher() {
        return eventSender;
    }

    public void setSupervision(Supervision supervision) {
        this.supervision = supervision;
    }

    public void terminate() {
        master.getExecutionContext().getEventHub().fireEvent(new EndSessionEvent());
    }


    /**
     * @logs.error "No master execution engine exists, unable to start application" - It has been impossible to create the master execution engine, most likely because any of the VXML/CCXML applications is wrongly configured
     */
    public void start() {
        createRuntimes();
        if (master != null) {
            master.start(master.getEntryModule().getProduct());
        } else {
            log.error("No master execution engine exists, unable to start application");
        }
    }

    public void setSession(ISession session) {
        this.session = session;
    }

    public void debugShutdown() {
        //      master.getE
    }

    public Context getContext() {
        return ctx;

    }

    public EventStream getParentStream() {
        return parentStream;
    }
}
