package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.externaldocument.ResourceLocator;
import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;
import com.mobeon.masp.execution_engine.runtime.EventProcessor;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.PlayableObject;
import com.mobeon.masp.execution_engine.runtime.PlayableObjectImpl;
import com.mobeon.masp.execution_engine.runtime.values.Pair;
import com.mobeon.masp.execution_engine.runtime.wrapper.Call;
import com.mobeon.masp.execution_engine.runtime.wrapper.MediaTranslator;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.runtime.*;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.NoInputSender;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mozilla.javascript.ScriptableObject;

/**
 * @author Mikael Andersson
 */
public class VoiceXMLExecutionContextDummy extends ExecutionContextDummy implements VXMLExecutionContext {

    public VoiceXMLExecutionContextDummy(DefaultExpectTarget expectTarget, RuntimeData data) {
        super(expectTarget, data);
    }

    public PromptQueue getPromptQueue() {
        return null;
    }

    public String getFieldTargetType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Connection getCurrentConnection() {
        return null;
    }

    public Redirector getRedirector() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setInitatedDisconnect() {
    }

    public Call getCall() {
        return null;
    }

    public boolean initiatedDisconnect() {
        return false;
    }

    public ControlTokenData getControlToken(long waittime, TimeUnit unit) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public TransferState getTransferState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGotoDebugLog(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getGotoDebugLog() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMediaTranslationManager(MediaTranslationManager mediaTranslationManager) {

    }

    public MediaTranslator getMediaTranslator() {
        // TODO implement
        return null;
    }

    public void waitForEvents() {
    }

    public VoiceXMLEventHub getVoiceXMLEventHub() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public ScriptableObject getLastResult() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InputAggregator getInputAggregator() {
        return null;
    }

    public VoiceXMLRuntimeData getData() {
        return (VoiceXMLRuntimeData)super.getData();
    }

    public VoiceXMLStatics getStatics() {
        return (VoiceXMLStatics)super.getStatics();
    }

    public VoiceXMLExecutionContextDummy() {
    }

    public boolean isAlive() {
        return false;
    }

    public Dialog getDialog() {
        return null;
    }

    public void setDialog(Dialog dialog) {
    }

    public void setCurrentFormItem(String name) {
    }

    public void setFinalProcessingState(boolean val) {
    }

    public boolean getFinalProcessingState() {
        return false;
    }

    public void setPlayingObject(PlayableObject playable) {
    }

    public PlayableObjectImpl getPlayableObject() {
        return null;
    }

    public void addPlayableToQueue(PlayableObject playableObject) {
    }

    public void playQueuedPlayableObjects() {
    }

    public void setPlayableQueue(List<PlayableObject> promptQ) {
    }

    public List<PlayableObject> getPlayableQueue() {
        return null;
    }

    public void setNoInputTimeoutValue(String timeout) {
    }

    public String getNoInputTimeoutValue() {
        return null;
    }

    @MockAction(DELEGATE)
    public VoiceXMLEventProcessor getEventProcessor() {
        return (VoiceXMLEventProcessor)super.getEventProcessor();
    }

    public String getProperty(String prop) {
        return null;
    }

    public void setProperty(String prop, String value) {
    }

    public PropertyStack getProperties() {
        return null;
    }

    public GrammarScopeNode getDTMFGrammar() {
        return null;
    }

    public void setDTMFGrammar(GrammarScopeNode grammar) {
    }

    public GrammarScopeNode getASR_grammar() {
        return null;
    }

    public void setASRGrammar(GrammarScopeNode grammar) {

    }

    public void setUtterance(String utterance) {
    }

    public String getUtterance() {
        return null;
    }

    public void setParent(VXMLExecutionContext executionContext) {
    }

    public void shutdownHook(Runnable runnable) {
    }

    public void subdialog(String src, DebugInfo debugInfo, List<Pair> params, List<PlayableObject> promptQ) {
    }

    public EventProcessor.Entry getEventEntry() {
        return null;
    }

    public void setFIAState(FIAState fiaState) {
    }

    public FIAState getFIAState() {
        return null;
    }

    public void setTransitioningState() {
    }

    public void setWaitingState() {
    }

    public String getCurrentFormItem() {
        return null;
    }

    @MockAction(DELEGATE)
    public IMediaObjectFactory getMediaObjectFactory() {
        return getData().getMediaObjectFactory();
    }

    public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
    }

    public boolean isPromptAllowedToPlay(Product prompt) {
        return false;
    }

    public void clearSelectedPrompts() {
    }

    public void addPromptToSelectedPrompts(Product prompt) {
    }

    public void startNoInputTimeout(int timeout, TimeUnit timeunit) {
    }

    public void cancelNoInputTimeout() {
    }

    public void setExecutionResult(ExecutionResult state) {
    }

    public void setRepromptPoint(Product repromptPoint) {
    }

    public Product getRepromptPoint() {
        return null;
    }

    @MockAction(DELEGATE)
    public Connection getConnection() {
        return getData().getConnection();
    }

    public void setCurrentMark(String markname) {
    }

    public String getCurrentMark() {
        return null;
    }

    public long getCurrentMarktime() {
        return 0;
    }

    public void setExecutingForm(Product form) {
    }

    public Product getExecutingForm() {
        return null;
    }

    public VXMLExecutionContext getParent() {
        return null;
    }

    public VXMLExecutionContext getContextParentOfAll() {
        return null;
    }

    public Map<String, String> getCurrentPromptProperties() {
        return null;
    }

    public void anonymousCall(Product product) {
    }

    public NoInputSender getNoInputSender() {
        return null;
    }

    public boolean isDisconnected() {
        return false;
    }

    public boolean isDisconnectReady() {
       return false;
    }

	@Override
	public ResourceLocator getResourceLocator() {

		return null;
	}

	@Override
	public void subdialog(String src, DebugInfo debugInfo, List<Pair> params,
			List<PlayableObject> promptQ, String maxage, String fetchTimeout) {
		
	}
}
