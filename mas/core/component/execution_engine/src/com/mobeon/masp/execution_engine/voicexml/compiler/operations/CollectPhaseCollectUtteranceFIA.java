/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.grammar.VirtualRootGrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Looberger
 */
public class CollectPhaseCollectUtteranceFIA extends VXMLOperationBase {
    private static ILogger logger = ILoggerFactory.getILogger(CollectPhaseCollectUtteranceFIA.class);
    private String name;
    private static List<Executable> collectInputOperations = new ArrayList<Executable>();

    public CollectPhaseCollectUtteranceFIA(String id) {
        super();
        name = id;
    }

    static {
        collectInputOperations.add(Ops.collectDTMFUtterance(false, false));
        // collectInputOperations.add(Ops.setValueToDTMFInterpretation());
        // collectInputOperations.add(Ops.getMarkInfo());
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        FIAState.NextItem nextItem = ex.getFIAState().getNextItem();
        if (nextItem.name == null) {
            return;
        }

        // Check the kind of form item
        // TODO: Fix up the activation of grammars and collecting of utterance
        // For now, the form items them self are responsible for:
        // Activating grammars, Collecting utterance and execution of executable content.
        // application.lastresult$ and its corresponding form item shadow variable is set when
        // DTMF utterance is collected.

        // Since the justFilled flags will be set by the collectDTMFUtterance operation executed by some
        // input items, they must be cleared before this happens.
        ex.getFIAState().clearJustFilledFlags();
        ex.setTransitioningState();
        // register ASR grammar

        GrammarScopeNode gnode = ex.getASR_grammar();
        if (gnode != null && !(gnode instanceof VirtualRootGrammarScopeNode)) {
            try {
                ex.getMediaTranslator().prepare(ex.getSession(),gnode);
            } catch (IllegalStateException ise) {
                if(logger.isDebugEnabled())logger.debug("Recognition failed: "+ise.getMessage());
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_NORESOURCE,
                        ise.getMessage(), DebugInfo.getInstance());

            }
        }

        // The STATE_WAITING state will be entered when the executed input item
        // starts waiting for input.
        if (nextItem.product.getTagType().equals(Constants.VoiceXML.FIELD)) {
            nextItem.product.execute(ex);
        } else if (nextItem.product.getTagType().equals(Constants.VoiceXML.RECORD)) {
            nextItem.product.execute(ex);
        } else if (nextItem.product.getTagType().equals(Constants.VoiceXML.BLOCK)) {
            if (logger.isDebugEnabled()) logger.debug("Collecting input for " + nextItem.name);
            ex.anonymousCall(collectInputOperations);
            assignFormItemValue(ex, nextItem);
        } else if (nextItem.product.getTagType().equals(Constants.VoiceXML.SUBDIALOG)) {
            nextItem.product.execute(ex);
        } else if (nextItem.product.getTagType().equals(Constants.VoiceXML.TRANSFER)) {
            nextItem.product.execute(ex);
        } else if (nextItem.product.getTagType().equals(Constants.VoiceXML.INITIAL)) {
            if (logger.isDebugEnabled()) logger.debug("Collecting input for " + nextItem.name);
            ex.anonymousCall(collectInputOperations);
            assignFormItemValue(ex, nextItem);
        } else if (nextItem.product.getTagType().equals(Constants.VoiceXML.OBJECT)) {
            nextItem.product.execute(ex);
        } else {
            if (logger.isDebugEnabled()) logger.debug("Ooops! Strange form item : " + nextItem.product.getTagType());
        }
    }

    private void assignFormItemValue(VXMLExecutionContext ex, FIAState.NextItem nextItem) {
        ex.getFIAState().finishedExecutingItem(nextItem.name);
        ex.getCurrentScope().setValue(nextItem.name, 1);
        ex.anonymousCall(nextItem.product);

    }

    public String arguments() {
        return name;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

