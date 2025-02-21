/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.products.WaitSetProduct;
import com.mobeon.masp.execution_engine.compiler.products.TransferWaitSetProduct;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author David Looberger
 */
public class Transfer extends InputItemCompiler {
    static ILogger logger = ILoggerFactory.getILogger(Transfer.class);

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Transfer");

        Predicate transfer = createPredicate(parent, null, element);
        transfer.add(Ops.logElement(element));

        transfer.addConstructor(Ops.enterHandlerScope());
        transfer.addDestructor(Ops.leftHandlerScope());
        // Add a property scope operation, but since the doCreate param is false, it
        // will not actually perform the scope creation. The scope will be created by the
        // FIA, in order for the properties within the form item to be available during
        // prompt queueing.
        transfer.addConstructor(Ops.addProperyScope(false));
        transfer.addDestructor(Ops.leaveProperyScope());
        transfer.addConstructor(Ops.registerCatches());
        transfer.setTagType(Constants.VoiceXML.TRANSFER);
        GrammarScopeNode grammar = CompilerMacros.getDTMFGrammar(module, element);
        if (grammar != null) {
            transfer.add(Ops.registerDTMFGrammar(grammar));
        }
        grammar = CompilerMacros.getASRGrammar(module, element);
        if (grammar != null) {
            transfer.add(Ops.registerASRGrammar(grammar));
        }
        String name = element.attributeValue(Constants.VoiceXML.NAME);
        transfer.setName(name);
        String expr = element.attributeValue(Constants.VoiceXML.EXPR);
        transfer.setExpr(expr);
        String cond = element.attributeValue(Constants.VoiceXML.COND);
        transfer.setCond(cond);
        String dest = element.attributeValue(Constants.VoiceXML.DEST);
        String destexpr = element.attributeValue(Constants.VoiceXML.DESTEXPR);
        if (! exactlyOne(dest, destexpr)) {
            return generateBadFetch(parent, element, module, CompilerMacros.bothAreDefinedMessage(element, Constants.VoiceXML.DEST, Constants.VoiceXML.DESTEXPR));
        }

        if (dest != null) {
            // Try parse it here as syntax check
            try {
                new URI(dest);
            } catch (URISyntaxException e) {
                return generateBadFetch(parent, element, module, CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.DEST, dest));
            }
            transfer.add(Ops.text_P(dest));
        } else {
            transfer.add(Ops.evaluateECMA_P(destexpr,module.getDocumentURI(), element.getLine()));
        }

        String bridge = element.attributeValue(Constants.VoiceXML.BRIDGE);
        String type = "blind";
        if (bridge != null && bridge.equals("true")) {
            type = "bridge";
        }

        String connecttimeout = element.attributeValue(Constants.VoiceXML.CONNECTIONTIMEOUT);
        String maxtime = element.attributeValue(Constants.VoiceXML.MAXTIME);
        String transferaudio = element.attributeValue(Constants.VoiceXML.TRANSFERAUDIO);
        String transferaudioexpr = element.attributeValue(Constants.VoiceXML.TRANSFERAUDIOEXPR);
         if (oneOrNone(transferaudio, transferaudioexpr)) {
            return generateBadFetch(parent, element, module, CompilerMacros.bothAreDefinedMessage(element, Constants.VoiceXML.TRANSFERAUDIO, Constants.VoiceXML.TRANSFERAUDIOEXPR));
        }

        if (transferaudio != null) {
            transfer.add(Ops.mark_P());
            transfer.add(Ops.text_P(transferaudio));
        }
        if (transferaudioexpr != null) {
            transfer.add(Ops.mark_P());
            transfer.add(Ops.evaluateECMA_P(transferaudioexpr,module.getDocumentURI(), element.getLine()));
        }

        String aai = element.attributeValue(Constants.VoiceXML.AAI);
        String aaiexpr = element.attributeValue(Constants.VoiceXML.AAIEXPR);
        if (exactlyOne(aai, aaiexpr)) {
            return generateBadFetch(parent, element, module, CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.AAI, aai));
        }

        // We need the <filled> last in the field product.
        Product filledProduct = createProduct(transfer, element);
        filledProduct.setName(transfer.getName());
        compileInputItemChildren(module, compilerPass, filledProduct,
                transfer, transfer, element, element.content());

        transfer.add(Ops.initiateTransfer());
        // This event handler is used for DTMF entered before the transfer
        addEventHandlerDtmfTerm(transfer, element);
        transfer.add(Ops.enterWaitingState());



        //
        //
        // IMPORTANT
        // All operations below may have be aware of the fact that transfer can be terminated
        // before they even execute! This would happen by early DTMF, caught by the event handler above.
        //



        // There may be DTMF entered before we registered the event handlers. Resend it.
        transfer.add(Ops.resendBufferedDTMF());

        // Play queued prompts before initiating the transfer
        transfer.add(Ops.playQueuedPrompts());
        addWaitSetProduct(element, transfer);

        if (transferaudio != null || transferaudioexpr != null) {
            // Play the transferaudio. We want the next operation (after the play) to
            // start immediately
            boolean setEngineInWaitState = false;
            boolean considerTransferTerminationFlag = true;

            transfer.add(Ops.createPlayableObject_TM_P(false, true));
            transfer.add(Ops.playAudio_T(setEngineInWaitState, considerTransferTerminationFlag));

        }
        // The SendTransferEvent operation will cause the engine to wait if successfull
        transfer.add(Ops.sendTransferEvent(transfer.getDebugInfo(), type, maxtime, connecttimeout, aai, aaiexpr));
        transfer.add(Ops.enterTransitioningState());
        transfer.add(filledProduct);

        parent.add(transfer);

        return transfer;
    }

    private boolean oneOrNone(String transferaudio, String transferaudioexpr) {
        int i = 0;

        if (transferaudio != null) {
            i++;
        }
        if (transferaudioexpr != null) {
            i++;
        }
        return i > 1;
    }

    private void addEventHandlers(Predicate transfer, CompilerElement element) {
        Predicate eventHandlerBlind = createPredicate(null, null, element);
        // TODO: Should the queued prompts be played? Is this possible?
        eventHandlerBlind.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "telephone.disconnect.transfer received (blind transfer)"));
        eventHandlerBlind.add(Ops.engineShutdown(true));
        transfer.add(Ops.addEventHandler(Constants.Event.TELEPHONE_DISCONNECT_TRANSFER, eventHandlerBlind, true));
    }

    private void addWaitSetProduct(CompilerElement element, Predicate transfer) {

        WaitSetProduct wsp = new TransferWaitSetProduct(transfer, DebugInfo.getInstance());
        wsp.waitForAll(false);

        // The TransferEventHandler decides itself when waiting is over
        wsp.stopWaitingOnMatch(false);
        wsp.injectOnMatch(false);

        wsp.addWaitFor(Constants.Event.PLAY_FINISHED);
        wsp.addWaitFor(Constants.Event.PLAY_FINISHED_HANGUP);
        wsp.addWaitFor(Constants.Event.PLAY_FAILED);
        wsp.addWaitFor(Constants.Event.DIALOG_TRANSFER_COMPLETE);
        wsp.addWaitFor(Constants.VoiceXML.DTMFUTTERANCE_EVENT);
        boolean prefixMatch = true;
        wsp.addWaitFor(prefixMatch, Constants.Event.ERROR_CONNECTION);
        wsp.addWaitFor(prefixMatch, Constants.Event.ERROR_UNSUPPORTED);

        wsp.addTerminateOn(Constants.Event.CONNECTION_DISCONNECT);
        wsp.addTerminateOn(Constants.Event.CONNECTION_DISCONNECT_HANGUP);


        transfer.add(wsp);
    }

    private void addEventHandlerDtmfTerm(Predicate transferProduct, CompilerElement element) {
        Predicate eventHandler = createPredicate(null, null, element);
        eventHandler.add(new AtomicExecutable(Ops.collectDTMFUtterance(true, false),
                Ops.terminateTransferIfDTMFUtterance()));
        transferProduct.add(Ops.addEventHandler(Constants.VoiceXML.DTMFUTTERANCE_EVENT,
                eventHandler, true));
    }

    private void addEventHandlerASRTerm(Predicate transferProduct, CompilerElement element) {
        Predicate eventHandler = createPredicate(null, null, element);
        eventHandler.add(new AtomicExecutable(Ops.terminateTransferIfASRUtterance()));
        transferProduct.add(Ops.addEventHandler(Constants.VoiceXML.ASRUTTERANCE_EVENT,
                eventHandler, true));
    }

    private boolean exactlyOne(String a, String b) {
        if ((a != null && b != null) ||
                (a == null && b == null)) {
            return false;
        } else {
            return true;
        }
    }

    private Product generateBadFetch(Product parent, CompilerElement element, Module module,
                                     String message) {
        Product product = createProduct(parent, element);
        module.postEvent(Constants.Event.ERROR_BADFETCH, element);
        product.add(Ops.sendEvent(Constants.Event.ERROR_BADFETCH, message,
                product.getDebugInfo()));
        parent.add(product);
        return product;
    }
}
