/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.components.ApplicationComponent;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.platformaccess.PlatformAccessFactory;
import com.mobeon.masp.execution_engine.runtime.event.EventHub;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.operateandmaintainmanager.Supervision;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.common.configuration.IConfigurationManager;

import java.util.List;

public class RuntimeControl implements IEventReceiver {
    private IMediaObjectFactory mediaObjectFactory;
    private ISession session;
    private ApplicationComponent app;
    private EventHub eh;
    private RuntimeFacade masterRuntime;
    private PlatformAccessFactory platformAcessFactory;
    private CallManager callManager;
    private EventStream eventStream;
    private Supervision supervision; 
    private IEventDispatcher eventDispatcher;
    private MediaTranslationManager mediaTranslationManager = null;
    private IServiceRequestManager serviceRequestManager;
    private IConfigurationManager configurationManager;

    public RuntimeControl(IConfigurationManager configurationManager, PlatformAccessFactory platformAcessFactory, IMediaObjectFactory mediaObjectFactory, ISession session, ApplicationComponent app, RuntimeFacade masterRuntime, CallManager callManager, EventStream eventStream, IEventDispatcher eventDispatcher, Supervision supervision, MediaTranslationManager mediaTranslationManager, IServiceRequestManager serviceRequestManager) {
        this.callManager = callManager;
        this.platformAcessFactory = platformAcessFactory;
        this.mediaObjectFactory = mediaObjectFactory;
        this.session = session;
        this.app = app;
        this.masterRuntime = masterRuntime;
        this.eventStream = eventStream;
        this.eventDispatcher = eventDispatcher;
        this.supervision = supervision;
        this.mediaTranslationManager = mediaTranslationManager;
        this.serviceRequestManager = serviceRequestManager;
        this.configurationManager = configurationManager;
    }

    public void doEvent(Event event) {
        if (event instanceof DialogStartEvent) {
            final DialogStartEvent dse = (DialogStartEvent) event;
            RuntimeFactory factory = RuntimeFactories.getInstance(dse.getMimeType());
            if(factory != null) {
                List<ModuleCollection> collections = app.getModuleCollectionListByMimeType(dse.getMimeType());
                String src = dse.getSrc();
                Module entry = null;
                ModuleCollection entryCollection = null;
                for(ModuleCollection collection:collections){
                    entry = collection.get(src);
                    if(entry != null) {
                        entryCollection = collection;
                        break;
                    }
                }

                RuntimeFacade runtime = factory.createRuntime(configurationManager, platformAcessFactory,mediaObjectFactory,
                    session,entry,entryCollection,callManager, eventDispatcher, supervision, mediaTranslationManager,
                        serviceRequestManager);

                DialogExecutionContext dialogExecutionContext = ((DialogExecutionContext) runtime.getExecutionContext());
                dialogExecutionContext.setConnection(dse.getConnection());
                dialogExecutionContext.setDialog(dse.getDialog());
                masterRuntime.wireEventHandling(runtime.getExecutionContext(), Constants.MimeType.VOICEXML_MIMETYPE, null);
                startRuntime(runtime, entry, entryCollection, dialogExecutionContext);
            }
        }
    }

    private void startRuntime(RuntimeFacade runtime, Module entry, ModuleCollection entryCollection, DialogExecutionContext dialogExecutionContext) {
        // If we can not find any initial executables, pass null and
        // let the runtime decide what to do

        if(runtime.getEntryModule() == null){
            runtime.start();
        } else {
            // If a leaf document is about to be started, make sure the
            // root context is initialized first
            String applicationAttr = entry.getDocumentAttribute(Constants.VoiceXML.APPLICATION);
            Product productToStart = null;
            if (applicationAttr == null || applicationAttr.length() == 0) {
                // the module to be started is root
                productToStart = runtime.getEntryModule().getProduct();
            } else {
                if(entryCollection != null){
                    Module rootModule = entryCollection.get(applicationAttr);
                    if(rootModule != null){
                        Product appProduct = rootModule.getSpecialProduct(
                                Module.VXML_PRODUCT);
                        if(appProduct != null){
                            dialogExecutionContext.setProductToBeExecuted(entry.getProduct());
                            productToStart = appProduct;
                        }
                    }
                }
            }
            runtime.start(productToStart);
        }
    }

    public void doGlobalEvent(Event event) {
        //TODO: Tom implementation
    }
}
