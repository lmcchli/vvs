/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.runtime.DialogExecutionContext;
import com.mobeon.masp.execution_engine.runtime.EventProcessor;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.PlayableObject;
import com.mobeon.masp.execution_engine.runtime.values.Pair;
import com.mobeon.masp.execution_engine.runtime.wrapper.Call;
import com.mobeon.masp.execution_engine.runtime.wrapper.MediaTranslator;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.NoInputSender;
import com.mobeon.masp.execution_engine.externaldocument.ResourceLocator;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.ScriptableObject;

public interface VXMLExecutionContext extends DialogExecutionContext {

    Call getCall();


    IMediaObjectFactory getMediaObjectFactory();

    void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory);

    void setExecutionResult(ExecutionResult state);

    void setRepromptPoint(Product repromptPoint);

    Product getRepromptPoint();

    void setCurrentMark(String markname);

    String getCurrentMark();

    long getCurrentMarktime();

    void setExecutingForm(Product form);

    Product getExecutingForm();

    VoiceXMLEventProcessor getEventProcessor();

    VXMLExecutionContext getParent();

    VXMLExecutionContext getContextParentOfAll();


    String getProperty(String prop);

    void setProperty(String prop, String value);

    PropertyStack getProperties();

    VoiceXMLStatics getStatics();

    GrammarScopeNode getDTMFGrammar();

    void setDTMFGrammar(GrammarScopeNode grammar);

    GrammarScopeNode getASR_grammar();

    void setASRGrammar(GrammarScopeNode grammar);

    void setUtterance(String utterance);

    String getUtterance();

    void setParent(VXMLExecutionContext executionContext);

    void shutdownHook(Runnable runnable);

    void subdialog(String src, DebugInfo debugInfo, List<Pair> params, List<PlayableObject> promptQ, String maxage, String fetchTimeout);

    EventProcessor.Entry getEventEntry();

    void setFIAState(FIAState fiaState);

    FIAState getFIAState();

    void setTransitioningState();

    void setWaitingState();

    Constants.VoiceXMLState getVoiceXMLState();

    String getCurrentFormItem();

    void setCurrentFormItem(String name);

    void setFinalProcessingState(boolean val);

    boolean getFinalProcessingState();

    void setInitatedDisconnect();

    boolean initiatedDisconnect();

    boolean isDisconnectReady();

    TransferState getTransferState();

    void setMediaTranslationManager(MediaTranslationManager mediaTranslationManager);

    MediaTranslator getMediaTranslator();

    void waitForEvents();

    VoiceXMLEventHub getVoiceXMLEventHub();


    ScriptableObject getLastResult();

    InputAggregator getInputAggregator();

    Map<String, String> getCurrentPromptProperties();

    void anonymousCall(Product product);

    PromptQueue getPromptQueue();

    NoInputSender getNoInputSender();

    String getFieldTargetType();

    Redirector getRedirector();


    ResourceLocator getResourceLocator();
}
