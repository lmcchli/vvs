/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */        
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.products.WaitSetProduct;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

/**
 * @author David Looberger
 */
public class Record extends InputItemCompiler {
    static ILogger logger = ILoggerFactory.getILogger(Record.class);

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Record");
        boolean compileError = false;
        String compileMessage = "";

        // Basically we will run a record operation and wait for the result.
        // There are event handlers for record Failed and record Finished.
        String name = element.attributeValue(Constants.VoiceXML.NAME);
        // TODO: Should not be needed in the new FIA implementation
        Predicate recordProduct = createInputItem(parent, name, element, true);
        recordProduct.add(Ops.logElement(element));

        recordProduct.addConstructor(Ops.enterHandlerScope());
        recordProduct.addDestructor(Ops.leftHandlerScope());
        // Add a property scope operation, but since the doCreate param is false, it
        // will not actually perform the scope creation. The scope will be created by the
        // FIA, in order for the properties within the form item to be available during
        // prompt queueing.
        recordProduct.addConstructor(Ops.addProperyScope(false));
        recordProduct.addDestructor(Ops.leaveProperyScope());
        recordProduct.addConstructor(Ops.registerCatches());

        recordProduct.setTagType(Constants.VoiceXML.RECORD);
        GrammarScopeNode grammar = CompilerMacros.getDTMFGrammar(module, element);
        if (grammar != null) {
            recordProduct.add(Ops.registerDTMFGrammar(grammar));
        }
        grammar = CompilerMacros.getASRGrammar(module, element);
        if (grammar != null) {
            recordProduct.add(Ops.registerASRGrammar(grammar));
        }
        // Retrieve the attributes
        String cond = element.attributeValue(Constants.VoiceXML.COND);
        if (cond != null) {
            recordProduct.setCond(cond);
        }
        String expr = element.attributeValue(Constants.VoiceXML.EXPR);
        if (expr != null) {
            recordProduct.setExpr(expr);
        }

        unsupportedAttr(Constants.VoiceXML.MODAL, element, module);
        unsupportedAttr(Constants.VoiceXML.BEEP, element, module);

        String maxtime = element.attributeValue(Constants.VoiceXML.MAXTIME);

        if (maxtime != null && !Validate.validateTime(maxtime, "^[0-9]+(s|ms)$")) {
            compileError = true;
            compileMessage = CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.MAXTIME, maxtime);
        }
        String finalsilence = element.attributeValue(Constants.VoiceXML.FINALSILENCE);
        //if (finalsilence == null) {
            // TODO: Retrieve default value from property
        //    finalsilence = "5s";
        //}
        if (finalsilence != null && (!Validate.validateTime(finalsilence, "^[0-9]+(s|ms)$"))) {
            compileError = true;
            compileMessage = CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.FINALSILENCE, finalsilence);
        }

        String type = element.attributeValue(Constants.VoiceXML.TYPE);
        if (type != null) {
            if (!Validate.validateMimeType(type)) {
                compileError = true;
                compileMessage = CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.TYPE, type);
            }
        }

        if (compileError)
            compilationError(parent, element, module, compileMessage);

        // We need the <filled> last in the recordProduct.

        Product filledProduct = createProduct(recordProduct, element);
        filledProduct.setName(recordProduct.getName());
        Product productForOtherChildren = createProduct(parent, element);
        compileInputItemChildren(module, compilerPass, filledProduct,
                recordProduct, recordProduct, element, element.content());
        recordProduct.add(productForOtherChildren);

        // TODO needed? recordProduct.add(productForOtherChildren);
        addEventHandlerForRecordFailed(recordProduct, element);
        addEventHandlerForRecordFinished(recordProduct, element);

        // dtmfterm attribute is supported and handled during pre-compilation
        addEventHandlerDtmfTerm(recordProduct, element);
        recordProduct.add(Ops.setInihibitRecording(false));
        recordProduct.add(Ops.addBargeinHandler());
        recordProduct.add(Ops.enterWaitingState());
        if (maxtime != null) {
            recordProduct.add(Ops.text_P(maxtime));
        } else {
            recordProduct.add(Ops.getProperty_P(Constants.PlatformProperties.PLATFORM_RECORD_MAXTIME));
        }

        if (finalsilence != null) {
            recordProduct.add(Ops.text_P(finalsilence));
        } else {
            recordProduct.add(Ops.getProperty_P(Constants.PlatformProperties.PLATFORM_RECORD_FINALSILENCE));
        }

        // General idea:
        // Since DTMF can arrive at any time and can also already be buffered before we start running,
        // we want to run in a controlled manner, and start by disabling events until we are ready.
        // We poll for buffered DTMF, and if there is buffered DTMF giving match, it will result in noinput event, and
        // the form item variable is not set.

        //Product p = createProduct(recordProduct, element);
        recordProduct.add(Ops.inhibitRecordingIfBufferedDTMF());
        recordProduct.add(Ops.record_P(type)); // record_P will set events to enabled.

        //recordProduct.add(p);
        recordProduct.add(Ops.enterTransitioningState());
        recordProduct.add(filledProduct);


        if (isCompilingFormPredicate(module, recordProduct.getName(), recordProduct)) {
            registerContentsInContainingForm(module, recordProduct.getName(), recordProduct);
            parent.add(recordProduct);
            return recordProduct;
        } else {
            compilationError(parent, element, module, "<record> should be a child of <form>");
            return parent;
        }
    }

    private void addEventHandlerDtmfTerm(Predicate recordProduct, CompilerElement element) {
           Predicate eventHandler = createPredicate(null, null, element);
           eventHandler.add(new AtomicExecutable(Ops.collectDTMFUtterance(true, false),
                   Ops.terminateRecordIfDTMFUtterance(),
                   Ops.waitIfOutstandingPlayFinished()));
           recordProduct.add(Ops.addEventHandler(Constants.VoiceXML.DTMFUTTERANCE_EVENT,
                   eventHandler, true));
       }


    private void addEventHandlerASRTerm(Predicate recordProduct, CompilerElement element) {
        Predicate eventHandler = createPredicate(null, null, element);
        eventHandler.add(new AtomicExecutable(Ops.terminateRecordIfASRUtterance()));
        recordProduct.add(Ops.addEventHandler(Constants.VoiceXML.ASRUTTERANCE_EVENT,
                eventHandler, true));
    }


    private void addEventHandlerForRecordFinished(Predicate recordProduct,
                                                  CompilerElement element) {

        addEventHandlerRecordFinishedHangup(recordProduct, element);

        Predicate eventHandler = createPredicate(null, null, element);
        eventHandler.add(new AtomicExecutable( Ops.setRecordingStopped(),
                Ops.assignToCurrentItem_T(),
                Ops.assignRecordShadowvars_T(),
                Ops.changeExecutionResult(ExecutionResult.DEFAULT)));
        recordProduct.add(Ops.addEventHandler(Constants.Event.RECORD_FINISHED,
                eventHandler, true));

    }

    private void addEventHandlerRecordFinishedHangup(Predicate recordProduct, CompilerElement element) {

        // If we get the record.finished.hangup, connection.disconnect.hangup is around the corner

        WaitSetProduct wsp = new WaitSetProduct(recordProduct, DebugInfo.getInstance());
        wsp.addTerminateOn(Constants.Event.CONNECTION_DISCONNECT);
        wsp.addTerminateOn(Constants.Event.CONNECTION_DISCONNECT_HANGUP);

        Predicate eventHandlerHangup = createPredicate(null, null, element);
        eventHandlerHangup.add(Ops.setEventsEnabled(false));
        eventHandlerHangup.add(Ops.setRecordingStopped());
        eventHandlerHangup.add(Ops.assignToCurrentItem_T());
        eventHandlerHangup.add(Ops.assignRecordShadowvars_T());
        eventHandlerHangup.add(Ops.setEventsEnabled(true));
        eventHandlerHangup.add(Ops.changeExecutionResult(ExecutionResult.EVENT_WAIT));

        recordProduct.add(Ops.addEventHandler(Constants.Event.RECORD_FINISHED_HANGUP,
                eventHandlerHangup, true));
    }

    private void addEventHandlerForRecordFailed(Predicate recordProduct,
                                                CompilerElement element) {

        // If record fails we let the FIA continue. The record form item variable remains unfilled

        Predicate eventHandlerHangup = createPredicate(null, null, element);
        eventHandlerHangup.add(Ops.setRecordingStopped());
        eventHandlerHangup.add(Ops.sendNoInputEvent("Record failed (hangup), sending noinput", DebugInfo.getInstance()));
        eventHandlerHangup.add(Ops.changeExecutionResult(ExecutionResult.DEFAULT));
        recordProduct.add(Ops.addEventHandler(Constants.Event.RECORD_FAILED_HANGUP,
                eventHandlerHangup, true));

        Predicate eventHandler = createPredicate(null, null, element);
        eventHandler.add(Ops.setRecordingStopped());
        eventHandler.add(Ops.sendNoInputEvent("Record failed, sending noinput", DebugInfo.getInstance()));
        eventHandler.add(Ops.changeExecutionResult(ExecutionResult.DEFAULT));
        recordProduct.add(Ops.addEventHandler(Constants.Event.RECORD_FAILED,
                eventHandler, true));
    }  

}
