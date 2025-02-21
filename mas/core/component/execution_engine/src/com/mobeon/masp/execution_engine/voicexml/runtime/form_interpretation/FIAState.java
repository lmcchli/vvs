/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation;

import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventImpl;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.voicexml.CompilerUtils;
import com.mobeon.masp.execution_engine.voicexml.compiler.PromptImpl;
import com.mobeon.masp.execution_engine.voicexml.grammar.ASRMatcher;
import com.mobeon.masp.execution_engine.voicexml.grammar.DTMFMatcher;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.grammar.MatchType;
import com.mobeon.masp.execution_engine.voicexml.runtime.ControlTokenData;
import com.mobeon.masp.execution_engine.voicexml.runtime.InputAggregator;

import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.*;

import com.mobeon.masp.execution_engine.voicexml.runtime.ShadowVarBase;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.TimeValue;
import com.mobeon.masp.util.TimeValueParser;
import com.mobeon.masp.util.Tools;

import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.ScriptableObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author David Looberger
 */
public class FIAState {
    private TimeValue recordTimeout;
    private long recordStarted;
    private final boolean initialFiaState;

    public boolean hasReceivedSomeInputAndWantsToMatchMore() {
        return matchState.hasOutstandingWakeupEvent();
    }

    public boolean isNoMatch() {
        return this.lastMatch == MatchType.NO_MATCH;
    }

    private static final int MAXPROMPTCOUNT = 999;
    private static final ILogger log = ILoggerFactory.getILogger(FIAState.class);
    public boolean isCollecting = false;
    private int parentPropertyDepth = 0;
    private boolean inhibitRecording = false;
    private String gotoNextItem = null;
    private boolean errorDuringInit = false;
    private boolean isExiting = false;
    private String nlsml_response = null;
    private boolean useNoInputTimeout = true;
    private MatchState matchState = new MatchState();

    private boolean responseSent = false;
    private final Object responseLock = new Object();
    private boolean receivedInput = false;


    public void reset(VXMLExecutionContext ex) {
        errorOccured(false);
        useNoInputTimeout(true);
        matchState.reset();
        responseSent = false;
        receivedInput = false;
        ex.getNoInputSender().unlock();
        recordStarted = 0;
    }

    public void setNextItem(String formItemName) {
        gotoNextItem = formItemName;
    }

    public void errorOccured(boolean b) {
        errorDuringInit = b;
    }

    public boolean getErrorDuringInit() {
        return errorDuringInit;
    }

    public void setIsExiting() {
        isExiting = true;
    }

    public boolean isExiting() {
        return isExiting;
    }

    public void setNLSMLResponse(String nlsml) {
        this.nlsml_response = nlsml;
    }

    public String getNlsml_response() {
        return nlsml_response;
    }

    public String getASRUtterance(GrammarScopeNode gnode) {

        return ASRMatcher.getUtterance(gnode, nlsml_response);

    }

    public void useNoInputTimeout(boolean b) {
        this.useNoInputTimeout = b;
    }

    public boolean getUseNoInputTimeout() {
        return useNoInputTimeout;
    }

    public boolean hasAsrMatch() {
        return nlsml_response != null;
    }

    public void inputReceived(VXMLExecutionContext ex) {
        synchronized (responseLock) {
            ex.getNoInputSender().cancelAndLock();
            receivedInput = true;
        }
    }

    public void markRecordStart(String property) {
        recordTimeout = TimeValueParser.getTime(property);
        recordStarted = System.currentTimeMillis();
    }

    public String getFieldId() {
        if(nextItem != null && nextItem.product != null && nextItem.product.getId() != null)
            return nextItem.product.getId().toString();
        else
            return null;
    }

    public boolean isInitialFiaState() {
        return initialFiaState;
    }


    public enum Phase {
        Initialization, Select, Collect, Process}

    private Phase phase = Phase.Initialization;

    public class NextItem {
        public String name;
        public Product product;

        public NextItem() {
            this.name = null;
            this.product = null;
        }
    }


    private FIAObjects fiaObjects = null;
    protected NextItem nextItem = null;
    protected Hashtable<String, Integer> promptCounters = null;
    protected Hashtable<String, Boolean> justFilledFlags = null;
    private List<String> unfinishedItems = null;
    private List<String> initials = null;
    List<String> inputItems = null;
    private String utterance = null;
    protected List<PromptImpl> prompts = null;
    protected List<PromptImpl> promptQueue = null;
    protected Product theForm = null;
    protected boolean abortPrompts = false;
    protected com.mobeon.masp.execution_engine.voicexml.grammar.MatchType lastMatch = MatchType.NO_MATCH;

    public FIAState() {
        this.initialFiaState = true;
        this.fiaObjects = new FIAObjects();
        this.unfinishedItems = new ArrayList<String>();
        this.unfinishedItems.addAll(fiaObjects.getFormItemOrder());
        this.promptCounters = new Hashtable<String, Integer>();
        this.justFilledFlags = new Hashtable<String, Boolean>();
        this.prompts = new ArrayList<PromptImpl>();
        this.nextItem = new NextItem();
        this.theForm = fiaObjects.getForm();
    }
    public FIAState(FIAObjects fiaObjects) {
        this.initialFiaState = false;
        this.fiaObjects = fiaObjects;
        this.unfinishedItems = new ArrayList<String>();
        this.unfinishedItems.addAll(fiaObjects.getFormItemOrder());
        this.promptCounters = new Hashtable<String, Integer>();
        this.justFilledFlags = new Hashtable<String, Boolean>();
        this.prompts = new ArrayList<PromptImpl>();
        this.nextItem = new NextItem();
        this.theForm = fiaObjects.getForm();
    }

    public Product getForm() {
        return theForm;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    /**
     * Sets the FIA done, that is, no more form items or <filled>
     * will be executed.
     */
    public void setFIADone() {
        // clear the unfished items, will cause us to think there
        // is nothing to do
        this.unfinishedItems.clear();
        nextItem.product = null;
        nextItem.name = null;
    }

    public void incrementPromptcounter(String name) {
        Integer count = promptCounters.get(name);
        if (count != null) {
            count++;
        } else {
            count = 1;
            count++;
        }
        promptCounters.put(name, count);

    }

    public void clearJustFilledFlags() {
        for (String name : justFilledFlags.keySet()) {
            justFilledFlags.put(name, Boolean.FALSE);
        }
    }

    public boolean getJustFilled(String name) {
        try {
            return justFilledFlags.get(name);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAnyJustFilled() {
        boolean result = false;
        for (String name : justFilledFlags.keySet()) {
            if (justFilledFlags.get(name)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void queuePrompts(VXMLExecutionContext ex) {
        List<PromptImpl> prompts = new ArrayList<PromptImpl>();
        List<PromptImpl> potentialPrompts = ex.getFIAState().getPrompts(ex);
        if (potentialPrompts != null) {
            prompts.addAll(potentialPrompts);
        }
        Collections.reverse(prompts);
        PromptQueue(ex).addPromptsToQueue(prompts);
    }

    public void setJustFilled(String name) {
        justFilledFlags.put(name, Boolean.TRUE);
    }

    public List<String> getInputItemNames() {
        if (inputItems == null) {
            inputItems = new ArrayList<String>();
            Hashtable<String, Product> formItems = fiaObjects.getFormItems();
            for (String itemName : formItems.keySet()) {
                if (formItems.get(itemName) instanceof InputItemImpl) {
                    inputItems.add(itemName);
                }
            }
        }
        return inputItems;
    }


    public int getPromptcounter(String name) {
        Integer count = promptCounters.get(name);
        if (count != null) {
            return count;
        } else {
            return 1;
        }
    }

    public void setUtterance(String utterance) {
        this.utterance = utterance;
    }

    public String getUtterance() {
        return utterance;
    }

    public FIAObjects getFIAObjects() {
        return fiaObjects;
    }

    public Set<String> getFormItemNames(VXMLExecutionContext ex) {
        Hashtable<String, Product> formItems = fiaObjects.getFormItems();
        return formItems.keySet();
    }


    public boolean findNext(VXMLExecutionContext ex) {
        if (gotoNextItem != null) {
            boolean ret = findNext(gotoNextItem, ex);
            gotoNextItem = null;
            return ret;
        }
        nextItem.name = null;
        nextItem.product = null;
        Scope scope = ex.getCurrentScope();
        List<String> removeLater = new ArrayList<String>();
        for (String unfinishedItem : unfinishedItems) {
            if (log.isDebugEnabled())
                log.debug("Considering item" + unfinishedItems);
            nextItem.name = unfinishedItem;
            nextItem.product = fiaObjects.getFormItems().get(unfinishedItem);
            if (nextItem.product instanceof Predicate) {
                Predicate predicate = (Predicate) nextItem.product;
                String cond = predicate.getCond();
                if (isUnset(ex, nextItem.name) && (cond == null || ((Boolean) scope.evaluate(cond)))) {
                    if (nextItem.product instanceof Predicate) {
                        String expr = ((Predicate) nextItem.product).getExpr();
                        if (expr != null) {
                            Object result = scope.evaluate(expr);
                            if(log.isDebugEnabled())
                                log.debug("Evaluated "+expr+" to "+result);
                            if (result != scope.getUndefined()) {
                                removeLater.add(nextItem.name);
                                Object wrappedEcmaObject = scope.javaToJS(result);
                                scope.setValue(nextItem.name, wrappedEcmaObject);
                                nextItem.name = null;
                                nextItem.product = null;
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    nextItem.name = null;
                    nextItem.product = null;
                }
            }
        }

        // The COND attribute have been evaluated in the selection phase
        if (!removeLater.isEmpty()) {
            unfinishedItems.removeAll(removeLater);
        }

        // Found no possible item to execute
        return nextItem.name != null;
    }

    public boolean findNext(String itemName, VXMLExecutionContext ex) {
        nextItem.name = null;
        nextItem.product = null;
        Scope scope = ex.getCurrentScope();

        nextItem.product = getFIAObjects().getFormItems().get(itemName);
        if (nextItem.product != null) {
            nextItem.name = itemName;
        }

        // TODO: Might throw badfetch if no item is found?
        // Found no possible item to execute
        return nextItem.name != null;
    }

    public boolean hasItem(String item) {
        return (getFIAObjects().getFormItems().get(item) != null);
    }


    public void finishedExecutingItem(String name) {
        unfinishedItems.remove(name);
    }

    private boolean isUnset(VXMLExecutionContext ex, String name) {
        Scope scope = ex.getCurrentScope();
        return scope.evaluate(name) == scope.getUndefined();
    }

    public NextItem getNextItem() {
        return nextItem;
    }

    public List<PromptImpl> getPrompts(VXMLExecutionContext ex) {
        List<PromptImpl> prompts = new ArrayList<PromptImpl>();

        // Retrieve form item
        Product product = nextItem.product;

        if (product instanceof PredicateImpl) {
            PredicateImpl predicateImpl = (PredicateImpl) product;
            // Retrieve the document ordered list of prompts in the form item
            prompts.addAll(predicateImpl.getPrompts());
            if (prompts.isEmpty())
                return null;
        }

        int validPromptCount = MAXPROMPTCOUNT;
        List<PromptImpl> nonValidPrompt = new ArrayList<PromptImpl>();
        List<PromptImpl> potentialPrompts = new ArrayList<PromptImpl>();

        // Remove all prompts with non-valid conditions
        for (PromptImpl prompt : prompts) {
            if (prompt.getCond() != null &&
                    (ex.getCurrentScope().evaluate(prompt.getCond()) == Boolean.FALSE)) {
                nonValidPrompt.add(prompt);
            }
        }
        prompts.removeAll(nonValidPrompt);
        nonValidPrompt.clear();

        // Find the "correct count": the highest count among the prompt elements still on the list less than or equal to the current count value.
        validPromptCount = 1;
        for (PromptImpl prompt : prompts) {
            if (prompt.getCount() <= getPromptcounter(nextItem.name)) {
                if (prompt.getCount() >= validPromptCount) {
                    validPromptCount = prompt.getCount();
                }
            }
        }
        // Add all prompts with count equal to the validPromptCount
        for (PromptImpl prompt : prompts) {
            if (prompt.getCount() == validPromptCount) {
                potentialPrompts.add(prompt);
            }
        }

        // Increment the promptcounter
        incrementPromptcounter(nextItem.name);
        return potentialPrompts;
    }

    public boolean areAllFormItemsDone(VXMLExecutionContext ex) {
        boolean done = true;
        Scope scope = ex.getCurrentScope();
        List<String> removeLater = new ArrayList<String>();

        for (String item : unfinishedItems) {
            Product prod = fiaObjects.getFormItems().get(item);
            if (prod instanceof Predicate) {
                Predicate predicate = (Predicate) prod;
                String cond = predicate.getCond();
                if (!isUnset(ex, item)) {
                    removeLater.add(item);
                    continue;
                }
                if ((cond == null || ((Boolean) scope.evaluate(cond)))) {
                    // We have unfinished items, with non-false conditions
                    done = false;
                    break;
                }
            }
        }
        unfinishedItems.removeAll(removeLater);
        return done;
    }

    public void collectDTMFUtterance(VXMLExecutionContext ex, boolean justMatch, boolean cancelPlayOnBargeIn, boolean sendDTMFWakeup) {
        if (isCollecting) {
            if (log.isDebugEnabled()) log.debug("Already collecting dtmf, leaving");
            return;
        }
        this.isCollecting = true;
        if (log.isDebugEnabled()) log.debug("Entering collectDTMFUtterance");
        // TODO: Get interdigit and no_input timeouts from properties once those exist.

        if (! CompilerUtils.isInputItem(nextItem.product.getTagType())) {
            if (log.isDebugEnabled()) log.debug(nextItem.product.getTagType() + " is not an input item. Will not collect");
            this.isCollecting = false;
            return;
        }

        InputAggregator ia = ex.getInputAggregator();
        // Wait for and retrieve the first token
        if (!ia.hasControlToken()) {
            if (log.isDebugEnabled()) log.debug("Leaving collectDTMFUtterance. Found no DTMF tokens");
            this.isCollecting = false;
            // TODO why utternce both in this class and executioncontext?
            return;
        }

        cancelWakeupEvent(ex);
        if (cancelPlayOnBargeIn) {
            if (ex.getCall().getIsPlaying() && ex.getProperties().getProperty(Constants.VoiceXML.BARGEIN).equals("true")) {
                if (!ex.getCall().stopPlay()) {
                    if (log.isInfoEnabled()) log.info("(CollectDTMFUtterance) Failed to stop play on outbound stream");
                } else {
                    // TODO! this cancel of play may cause a warning log for "no handler for play.finished"
                    // If the entire FIA completes before the event arrives...I have no easy fix now (ermkese)
                }
                PromptQueue(ex).setAbortPrompts(true);
            }
        }

        GrammarScopeNode grammar = ex.getDTMFGrammar();
        if (grammar == null) {
            String message = "Have no active grammar";
            ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC, message, DebugInfo.getInstance());
            ex.setUtterance(null);
            if (log.isDebugEnabled()) log.debug("Leaving collectDTMFUtterance. " + message);
            this.isCollecting = false;
            return;
        }
        String interdigittimeoutStr = ex.getProperties().getProperty(Constants.VoiceXML.INTERDIGITTIMEOUT);
        String termtimeoutStr = ex.getProperties().getProperty(Constants.VoiceXML.TERMTIMEOUT);
        String termchar = ex.getProperties().getProperty(Constants.VoiceXML.TERMCHAR);
        String termtimeout = ex.getProperties().getProperty(Constants.VoiceXML.TERMTIMEOUT);

        if (termtimeout == null) {
            termtimeout = Constants.VoiceXML.DEFAULT_TERMTIMEOUT;
        }
        TimeValue interdigitTimeout = TimeValueParser.getTime(interdigittimeoutStr);

        if (interdigitTimeout == null) {
            interdigitTimeout = new TimeValue(0, TimeUnit.SECONDS);
        }
        if (interdigitTimeout.getUnit() == TimeUnit.SECONDS)
            interdigitTimeout = new TimeValue(interdigitTimeout.getTime() * 1000, TimeUnit.MILLISECONDS);

        TimeValue termTimeout = TimeValueParser.getTime(termtimeoutStr);
        if (termTimeout == null) {
            termTimeout = new TimeValue(0, TimeUnit.SECONDS);
        }

        if (termTimeout.getUnit() == TimeUnit.SECONDS) {
            termTimeout = new TimeValue(termTimeout.getTime() * 1000, TimeUnit.MILLISECONDS);
        }

        if (termchar == null) {
            termchar = Constants.VoiceXML.DEFAULT_TERMCHAR;
        }

        // Cancel the no input timer.
        inputReceived(ex);

        calculateMatch(ex, interdigitTimeout, termchar, grammar, termTimeout, justMatch, sendDTMFWakeup);
        this.isCollecting = false;
        if (log.isDebugEnabled()) log.debug("Leaving collectDTMFUtterance");
    }

    private void calculateMatch(VXMLExecutionContext ex, TimeValue interdigitTimeout, String termchar,
                                GrammarScopeNode grammar, TimeValue termTimeout, boolean justMatch, boolean sendDTMFWakeup) {
        ControlTokenData ctd;
        // termtimeout not used at this time
        MatchType match = null;
        boolean immediateTermChar = false;
        // String token = localUtterance;
        InputAggregator ia = ex.getInputAggregator();
        ctd = ia.getControlToken();

        boolean done = false;
        // if optional DTMF is not entered withing a timeout, send an event to ourselves,
        // and if we receive it, we sticjk to the match we had befor sending the event.
        boolean doSendWakeUpEvent = false;
        TimeValue timeoutToUse = null;
        while (! done) {
            if (ctd == null) {
                done = true;
            } else {
                String inputToken = stringifyDtmf(ctd.getToken().getToken().digit());
                // Special case: immediate termchar. Check if empty
                // string matches the grammar
                immediateTermChar = isImmediateTermChar(inputToken, termchar, grammar);

                if (immediateTermChar) {
                    done = true;
                    match = DTMFMatcher.match(grammar, "");
                } else {
                    if (inputToken.equals(termchar)) {
                        if (log.isDebugEnabled()) log.debug("Received termchar " + inputToken + ", done matching");
                        done = true;
                        doSendWakeUpEvent = false;
                        String whatTomatch = matchState.getSavedUtterance() == null ? "" : matchState.getSavedUtterance();
                        match = DTMFMatcher.match(grammar, whatTomatch);
                    } else {
                        matchState.appendToSavedUtterance(inputToken);
                        match = DTMFMatcher.match(grammar, matchState.getSavedUtterance());
                        
                        //TR: HU98608 Debug logs in VVS which contains pin numbers of subscribers
                        String utterance_obscured = "";      
                        if (log.isInfoEnabled()) {
                        	if (matchState.getSavedUtterance() != null) {
                                //obscure with X's instead of actual DTMF digits as can display pin numbers etc.
                                utterance_obscured = "obscured:" + StringUtils.repeat("X", matchState.getSavedUtterance().length());
                        	} else {
                                utterance_obscured = "null";
                        	}
                        }
                        
                        switch (match) {
                            case FULL_MATCH:
                                // Full match but more DTMF may be entered
                                if (termTimeout.getTime() != 0 && !"".equals(termchar)) {
                                    timeoutToUse = termTimeout;
                                    doSendWakeUpEvent = true;
                                    ctd = ia.getControlToken();
                                } else {
                                    if (log.isInfoEnabled())
                                        log.info("Full Match in grammar : " + utterance_obscured);
                                    done = true;
                                    doSendWakeUpEvent = false;
                                }
                                break;
                            case MATCH:
                                if (log.isInfoEnabled())
                                    log.info("Match in grammar, but trying to match more : " + utterance_obscured);
                                timeoutToUse = interdigitTimeout;
                                doSendWakeUpEvent = true;
                                ctd = ia.getControlToken();
                                break;
                            case NO_MATCH:
                                if (log.isInfoEnabled())
                                    log.info("NO Match in grammar for token : " + utterance_obscured);
                                doSendWakeUpEvent = false;
                                done = true;
                                break;
                            case PARTIAL_MATCH:
                                if (log.isDebugEnabled())
                                    if (log.isInfoEnabled())
                                        log.info("Partial match in grammar, but trying to match more : " + utterance_obscured);
                                timeoutToUse = interdigitTimeout;
                                doSendWakeUpEvent = true;
                                ctd = ia.getControlToken();
                                break;
                            default:
                        } // end switch
                    }
                }
            }
        }

        if (doSendWakeUpEvent && sendDTMFWakeup) {
            sendWakeupEvent(ex, (int) Tools.toMillis(timeoutToUse));
        } else {
            cancelWakeupEvent(ex);
        }
        setLastMatch(match);
        ex.setUtterance(matchState.getSavedUtterance());

        if (justMatch) {
        } else {
            if (match == MatchType.NO_MATCH) {
                sendNoMatch(ex);
            }
            setShadowVars(ex, nextItem.name);
        }
    }

    private void sendNoMatch(VXMLExecutionContext ex) {
        SimpleEvent ev = new SimpleEventImpl(Constants.VoiceXML.NOMATCH, "No input matched", DebugInfo.getInstance());
        ev.defineTarget(ex.getFieldTargetType(),getFieldId());
        ex.getEventHub().fireEvent(ev);
    }

    private void setShadowVars(VXMLExecutionContext ex, String formItemName) {
        String savedUtterance = matchState.getSavedUtterance();
        if (savedUtterance == null) {
            savedUtterance = "";
        }
        ex.getCurrentScope().evaluate("application.lastresult$.utterance = '" + savedUtterance + "'");
        ex.getCurrentScope().evaluate("application.lastresult$.inputmode = 'dtmf'");
        ex.getCurrentScope().evaluate("application.lastresult$.confidence = '1.0'");
        ex.getCurrentScope().evaluate("application.lastresult$.interpretation = '" + savedUtterance + "'");
        if (formItemName != null) {
            setJustFilled(formItemName);
            if (nextItem.product.getTagType().equals(Constants.VoiceXML.RECORD)) {
                setRecordShadowVar(formItemName, ex, savedUtterance);
            } else {
                setShadowVarBase(formItemName, ex, savedUtterance);
            }
        }
    }

    /**
     * @param event
     * @return true iff event meant that interdigit timeout now is expired
     */
    public boolean onDTMFWakeup(SimpleEvent event) {
        // Is this the wakeup event we are waiting for or is it delayed, and we don't care about it?

        String sendId = event.getSendId();
        if (sendId != null && sendId.equals(matchState.getIdForWakeupEvent())) {
            if (log.isInfoEnabled()) log.info("Interdigit timeout expired");
            matchState.setHasOutstandingWakeupEvent(false);
            return true;
        } else {
            if (log.isDebugEnabled()) log.debug("DTMF wakeup event was not addressed to us");
            return false;
        }
    }

    private void cancelWakeupEvent(ExecutionContext ex) {
        if (matchState.hasOutstandingWakeupEvent()) {
            if (log.isDebugEnabled()) log.debug("Cancelling wakeup event with id " + matchState.getIdForWakeupEvent());
            ex.getEventHub().cancel(matchState.getIdForWakeupEvent(), null);
            matchState.setHasOutstandingWakeupEvent(false);
        }

    }

    private void sendWakeupEvent(VXMLExecutionContext ex, int delay) {
        cancelWakeupEvent(ex); // cancel last if any
        matchState.setHasOutstandingWakeupEvent(true);
        SimpleEvent simpleEvent = new SimpleEventImpl(Constants.Event.DTMF_WAKEUP_EVENT, "Wakeup event", DebugInfo.getInstance());
        matchState.setIdForWakeupEvent(simpleEvent.getSendId());
        if (log.isDebugEnabled()) log.debug("Firing wakeup event with delay " + delay + " milliseconds, sendid " + simpleEvent.getSendId());
        ex.getEventHub().fireContextEvent(simpleEvent, delay);
    }

    public void clearUtterance() {
        matchState.setSavedUtterance("");
    }

    private boolean isImmediateTermChar(String inputToken, String termchar, GrammarScopeNode grammar) {
        boolean isImmediateTermChar = false;
        MatchType match;
        if (inputToken.equals(termchar)) {
            if (matchState.getSavedUtterance() == null || matchState.getSavedUtterance().length() == 0) {
                match = DTMFMatcher.match(grammar, "");
                //match = grammar.match("");
                if (match == MatchType.FULL_MATCH ||
                        match == MatchType.MATCH) {
                    isImmediateTermChar = true;
                    matchState.setSavedUtterance("");
                    if (log.isDebugEnabled())
                        log.debug("Immediate termchar and empty string matched in grammar");
                }
            }
        }
        return isImmediateTermChar;
    }

    private void setShadowVarBase(String formItemName, VXMLExecutionContext ex, String token) {
        ShadowVarBase shadow = new ShadowVarBase();
        String shadowName = formItemName + "$";
        Scope scope = ex.getCurrentScope();
        createOrResetVar(scope, shadowName, shadow);
        scope.evaluate(shadowName + ".utterance = '" + token + "'");
        scope.evaluate(shadowName + ".inputmode = 'dtmf'");
        scope.evaluate(shadowName + ".confidence = '1.0'");
        scope.evaluate(shadowName + ".interpretation = '" + token + "'");
    }

    private void setRecordShadowVar(String formItemName, VXMLExecutionContext ex, String token) {
        ShadowVarBase shadow = new ShadowVarBase();
        String shadowName = formItemName + "$";
        Scope scope = ex.getCurrentScope();
        createOrResetVar(scope, shadowName, shadow);
        scope.evaluate(shadowName + ".utterance = '" + token + "'");
        scope.evaluate(shadowName + ".inputmode = 'dtmf'");
        scope.evaluate(shadowName + ".confidence = '1.0'");
        scope.evaluate(shadowName + ".interpretation = '" + token + "'");
    }

    private void createOrResetVar(Scope scope, String name, ScriptableObject var) {
        if (! scope.isDeclaredInExactlyThisScope(name)) {
            scope.evaluateAndDeclareVariable(name, null);
            scope.setValue(name, var);
        } else if (scope.evaluate(name) == scope.getUndefined()) {
            scope.setValue(name, var);
        }
    }

    private String stringifyDtmf(int i) {

        if (i == 10) return "*";
        if (i == 11) return "#";
        if (i == 12) return "A";
        if (i == 13) return "B";
        if (i == 14) return "C";
        if (i == 15) return "D";
        if (i == 16) return "flash";
        return String.valueOf(i);
    }


    public void getMarkInfo(VXMLExecutionContext ex) {
        String markname = ex.getCurrentMark();
        long marktime = ex.getCurrentMarktime();
        if (markname != null) {
            Scope scope = ex.getCurrentScope();
            scope.evaluate("application.lastresult$.markname = '" + markname + "'");
            scope.evaluate("application.lastresult$.marktime = " + marktime);
            if (log.isDebugEnabled())
                log.debug("Assigning markname/marktime (" + markname + "/" + marktime + ") to application.lastresult$");
            String formItemName = ex.getFIAState().getNextItem().name;
            if (formItemName != null) {
                String shadowName = formItemName + "$";
                if (! scope.isDeclaredInExactlyThisScope(shadowName)) {
                    scope.evaluateAndDeclareVariable(shadowName, null);
                } else if (scope.evaluate(shadowName) == scope.getUndefined()) {
                    scope.setValue(shadowName, new ShadowVarBase());
                }
                scope.evaluate(formItemName + "$.markname = '" + markname + "'");
                scope.evaluate(formItemName + "$.marktime = " + marktime);
                if (log.isDebugEnabled())
                    log.debug("Assigning markname/marktime (" + markname + "/" + marktime + ") to " + shadowName);
            }
        }
    }

    public void clearFormItems(VXMLExecutionContext ex, String namelist) {
        Scope scope = ex.getCurrentScope();
        if (namelist == null || namelist.length() == 0) {
            // Clear all form items
            for (String fi : fiaObjects.getFormItemOrder()) {
                scope.setValue(fi, scope.getUndefined());
                unfinishedItems.add(fi);
            }
        } else {
            StringTokenizer tokenizer = new StringTokenizer(namelist, " ");
            List<String> list = fiaObjects.getFormItemOrder();
            while (tokenizer.hasMoreTokens()) {
                String fi = tokenizer.nextToken();
                if (fi.length() > 0) {
                    scope.setValue(fi, scope.getUndefined());
                    int indexOf = list.indexOf(fi);
                    for (int i = 0; i < unfinishedItems.size(); i++) {
                        if (list.indexOf(unfinishedItems.get(i)) > indexOf) {
                            unfinishedItems.add(i, fi);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void setAllInitials(VXMLExecutionContext ex) {
        if (initials == null) {
            initials = new ArrayList<String>();
            for (String name : getFIAObjects().getFormItems().keySet()) {
                if (getFIAObjects().getFormItems().get(name).getTagType() == Constants.VoiceXML.INITIAL) {
                    initials.add(name);
                }
            }

        }
        for (String initName : initials) {
            if (isUnset(ex, initName)) {
                ex.getCurrentScope().setValue(initName, "1");
            }
        }
    }

    private com.mobeon.masp.execution_engine.voicexml.grammar.MatchType getLastMatch() {
        return lastMatch;
    }

    public void resetLastMatch() {
        setLastMatch(MatchType.NO_MATCH);
    }

    public void setLastMatch(com.mobeon.masp.execution_engine.voicexml.grammar.MatchType lastMatch) {
        this.lastMatch = lastMatch;
    }

    public boolean matchedGrammar() {
        if (matchState.hasOutstandingWakeupEvent) {
            return false;
        }
        return this.lastMatch == MatchType.MATCH || this.lastMatch == MatchType.FULL_MATCH;
    }

    public void setParentPropertyDepth(int depth) {
        this.parentPropertyDepth = depth;
    }

    public int getParentPropertyDepth() {
        return parentPropertyDepth;
    }

    public void setInhibitRecording(boolean b) {
        synchronized (responseLock) {
            if (receivedInput && b)
                receivedInput = false;
        }
        inhibitRecording = b;
    }

    public boolean inhibitRecording() {
        return inhibitRecording;
    }

    public boolean deliverNoMatchResponse(VXMLExecutionContext ex) {
        synchronized (responseLock) {
            if (!responseSent) {
                responseSent = true;
                return true;
            }
        }
        return false;
    }

    public boolean deliverNoInputResponse(VXMLExecutionContext context) {
        synchronized (responseLock) {
            if (!isExiting && !responseSent && !receivedInput && !context.getFinalProcessingState()) {
                //Always cancel the noinput sender to avoid generating unecessary events
                context.getNoInputSender().cancelAndLock();
                responseSent = true;
                return true;
            } else {
                TestEventGenerator.generateEvent(TestEvent.NOINPUT_DISCARDED);
            }
        }
        return false;
    }

    public long readRecordNoInputDelay() {
        if (recordStarted > 0) {
            long timeSpent = System.currentTimeMillis() - recordStarted;
            long timeLeft = TimeUnit.MILLISECONDS.convert(recordTimeout.getTime(), recordTimeout.getUnit()) - timeSpent;
            if (timeLeft > 0)
                return timeLeft;
            else
                return 0;
        }
        return 0;
    }
}
