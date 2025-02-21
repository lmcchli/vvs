package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.compiler.operations.*;
import com.mobeon.masp.execution_engine.compiler.operations.*;
import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.PlayableObjectImpl;
import com.mobeon.masp.execution_engine.runtime.event.Selector;
import com.mobeon.masp.execution_engine.voicexml.compiler.PromptImpl;
import com.mobeon.masp.execution_engine.voicexml.compiler.operations.EnterTransitioningState;
import com.mobeon.masp.execution_engine.voicexml.compiler.operations.EnterWaitingState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.voicexml.compiler.operations.*;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAObjects;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.net.URI;
import java.util.Map;

/**
 * Factory class for creating all known Operations   
 *
 * @author Mikael Andersson
 */
@SuppressWarnings({"ClassWithTooManyMethods"})
public class Ops {

    public static Operation newScope(final String name) {
        return new NewScope(name);
    }

    public static Operation newECMAScope(final String name) {
        return new NewECMAScope(name);
    }

    /**
     * Creates a {@link SendEvent_T} operation instance.
     *
     * @return A sendEvent operation instance
     */
    public static Operation sendEvent_T() {
        return new SendEvent_T();
    }

    /**
     * Creates a {@link SendEvent} operation instance.
     *
     * @param eventName Name of the event this operation will post
     *                  when executed
     * @return A sendEvent operation instance
     */
    public static Operation sendEvent(String eventName, String msg, DebugInfo debugInfo) {
        return new SendEvent(eventName,msg, debugInfo);
    }

    /**
     * Creates a {@link SendNoInputEvent} operation instance.
     *
     * @return A SendNoInputEvent operation instance
     */
    public static Operation sendNoInputEvent(String message, DebugInfo debugInfo) {
        return new SendNoInputEvent(message, debugInfo);
    }

    /**
     * Creates a {@link EvaluateECMA_P} operation instance.
     *
     * @param ecmaScript ECMAScript code fragment to be executed
     * @return An evaluateECMA_P operation instance
     */
    public static Operation evaluateECMA_P(String ecmaScript, URI uri, int lineNumber) {
        return new EvaluateECMA_P(ecmaScript, uri, lineNumber);
    }

    public static Operation evaluateECMA_P(CompilerElement element, String ecmaScript, URI uri, int lineNumber) {
        return new EvaluateECMA_P(element, ecmaScript, uri, lineNumber);
    }

    /**
     * Creates a {@link EvaluateECMA_TP} operation instance.
     *
     *
     * @return An evaluateECMA_TP operation instance
     */
    public static Operation evaluateECMA_TP() {
        return new EvaluateECMA_TP();
    }

    /**
     * Creates a {@link CreateDialogStartByDialogId_T} operation instance.
     *
     * @return A CreateDialogStartByDialogId_T instance
     */
    public static Operation createDialogStartByDialogID_T() {
        return new CreateDialogStartByDialogId_T();
    }

    public static Operation introduceECMAVariable(String name,DebugInfo debugInfo) {
        return new ECMAVar(name,debugInfo);
    }

    public static Operation createLogMessage(String str) {
        return new Log(str);
    }


    public static Operation text_P(String textValue) {
        return new Text_P(textValue);
    }

    public static Operation log_TM() {
        return new Log_TM();
    }

    public static Operation mark_P() {
        return new Mark_P();
    }

    public static Operation assignECMAVar_T(String name) {
        return new AssignECMAVar_T(name);
    }

    public static Operation closeScope() {
        return new CloseScope();
    }

    public static Operation closeECMAScope() {
        return new CloseECMAScope();
    }

    public static Operation registerHandler(String[] states, Selector sel, Predicate predicate) {
        return new RegisterHandler(states, sel, predicate);
    }

    public static Operation registerHandler(Selector sel, Predicate predicate) {
        return new RegisterHandler(null, sel, predicate);
    }

    public static Operation createDialogStartBySrcTypeNamelist_T4P() {
        return new CreateDialogStartBySrcTypeNamelist_T4P();
    }

    public static Operation textArray_P(String ... text) {
        return new TextArray_P(text);
    }

    public static Operation useStateVariable(String varname) {
        return new UseStateVariable(varname);
    }
    public static Operation playAudio_T(boolean setEngineInWaitState, boolean considerTransferTerminationFlag) {
        return new PlayAudio_T(setEngineInWaitState, considerTransferTerminationFlag);
    }

    public static Operation setEventsEnabled(boolean enabled) {
        return new SetEventsEnabled(enabled);
    }

    public static Executable eventVar_P() {
        return new EventVar_P();
    }

    public static Executable setEventVar(String name) {
        return new SetEventVar(name);
    }

    public static Executable connectionAccept_T() {
        return new ConnectionAccept_T();

    }
    
    public static Executable connectionProxy_T3(){
    	return new ConnectionProxy_T3();
    }
    
    public static Operation storeDialogId(String dialogId) {
        return new StoreDialogId_TP(dialogId);
    }

    public static Operation createDialogTerminateByDialogId_T() {
        return new CreateDialogTerminateByDialogId_T();
    }

    public static Executable disconnect_T() {
        return new Disconnect_T();
    }

    public static Executable eventVar_P(String property) {
        return new EventVar_P(property);
    }

    public static Operation unwindAndCall_TM_T(boolean unwindSuppliedProduct) {
        return new UnwindAndCall_TM_T(unwindSuppliedProduct);
    }

    public static Operation changeExecutionResult(ExecutionResult state) {
        return new ChangeExecutionResult(state);
    }

    public static Operation addEventHandler(String event, Predicate handler, boolean setEventsEnabled) {
        return new AddEventHandler(event, handler, setEventsEnabled);
    }

    public static Operation unwindToRepromptPoint() {
        return new UnwindToRepromtPoint();
    }

    public static Operation record_P(String type){
        return new Record_P(type);
    }

    public static Operation registerDTMFGrammar(GrammarScopeNode grammar) {
        return new RegisterDTMFGrammar(grammar);
    }

    public static Operation registerASRGrammar(GrammarScopeNode grammar) {
        return new RegisterASRGrammar(grammar);
    }

    public static Operation sendDialogEvent(String event, String message) {
        return new SendDialogEvent(event, message);
    }

    public static Operation engineShutdown(boolean recursive) {
        return new EngineShutdown(recursive);
    }

    public static Operation retrieveEvalueateAndCacheURIFileContent(URI uri, DebugInfo debugInfo) {
        return new retrieveEvalueateAndCacheURIFileContent(uri, debugInfo);
    }

    public static Operation not_TP() {
        return new Not_TP();
    }

    public static Operation areAllNeededFormItemsDone_P(FormPredicate parent, String namelist, String mode) {
        return new AreAllNeededFormItemsDone_P(parent, namelist, mode);
    }

    public static Operation print_TM(String doc, int lineNo, String label) {
        return new Print_TM(doc, lineNo, label);
    }

    public static Executable playAudio_TM() {
        return new Play_TM();
    }

    public static Operation sendCCXMXLEvent(String event, String eventMessage, DebugInfo debugInfo) {
        return new SendCCXMLEvent(event,eventMessage,debugInfo);
    }

    public static Executable connectionReject_T3() {
        return new ConnectionReject_T3();
    }

    public static Executable analyzeGotoType_TP(Product containingProduct) {
        return new AnalyzeGotoType_TP(containingProduct);
    }

    public static Executable analyzeFormItemGoto_TP() {
        return new AnalyzeFormItemGotoType_TP();
    }

    public static Executable setExecutingForm(Product form){
        return new SetExecutingForm(form);
    }

    public static Executable setExecutingModule(Module module){
        return new SetExecutingModule(module);
    }

    public static Operation collectDTMFUtterance(boolean justMatch, boolean sendDTMFWakeup) {
        return new CollectDTMFUtterance(null, justMatch, sendDTMFWakeup);
    }

    public static Operation onDTMFWakeup() {
        return new OnDTMFWakeup();
    }

    public static Operation getMarkInfo() {
        return new GetMarkInfo();
    }

    public static Executable declareLastResult(String s) {
        return new DeclareLastResult(s);
    }

    public static Operation unwindOrContinue_TP(Product parent) {
        return new UnwindOrContinue_TP(parent);
    }

    public static Operation waitIfUndefined(String name) {
        return new WaitIfUndefined(name);
    }

    public static Operation setValueToDTMFInterpretation() {
        return new SetValueToDTMFInterpretation();
    }

    public static Executable executeSubdialog_TTM(DebugInfo debugInfo) {
        return new ExecuteSubdialog_TTM(debugInfo);

    }

    public static Operation assignRecordShadowvars_T() {
        return new AssignRecordShadowvars_T();
    }

    public static Operation terminateRecordIfDTMFUtterance() {
        return new TerminateRecordIfDTMFUtterance();
    }

    public static VXMLOperationBase terminateTransferIfDTMFUtterance() {
        return new TerminateTransferIfDTMFUtterance();
    }

    public static VXMLOperationBase assignIfBufferedDTMF() {
        return new AssignIfBufferedDTMF();
    }

    public static VXMLOperationBase resendBufferedDTMF() {
        return new ResendBufferedDTMF();
    }

    public static Operation assignECMAVar(String name) {
        return new AssignECMAVar(name);
    }

    public static Executable returnSubdialog(String name,DebugInfo debugInfo) {
        return new ReturnSubdialog(debugInfo,name);
    }

    public static Operation setProperty(String name, String value) {
        return new SetProperty(name, value);
    }

    public static Operation executeDialogTrampoline() {
        return new ExecuteDialogTrampoline();
    }

    public static Executable connectionCreateCall_T4() {
        return new ConnectionCreateCall_T4();
    }
   
    public static Operation addProperyScope(boolean doCreate) {
        return new AddPropertyScope(doCreate);
    }

    public static Operation leaveProperyScope() {
        return new LeaveProperyScope();
   }

    public static Executable sendReturn_T() {
        return new SendReturn_T();
    }

    public static Operation setFIAState(FIAObjects fia) {
        return new SetFIAState(fia);
    }

    public static Operation initializeFormItemsFIA() {
        return new InitializeFormItemsFIA();
    }

    public static Operation selectPhaseFIA(String name) {
        return new SelectPhaseFIA(name);
    }

    public static Operation collectPhaseDeterminePromptsFIA(String id) {
        return new CollectPhaseDeterminePromptsFIA(id);
    }

    public static Operation processPhaseFilledActionsFIA( String id) {
        return new ProcessPhaseFilledActionsFIA(id);
    }

    public static Operation collectPhaseCollectUtteranceFIA(String id) {
        return new CollectPhaseCollectUtteranceFIA(id);
    }

    public static Operation setItemFinished() {
        return new SetItemFinished();
    }

    public static Operation rerunFIAIfUnfinishedItems(FormPredicate form) {
        return new RerunFIAIfUnfinishedItems(form);
    }

    public static Operation catchUnwind(){
        return new CatchUnwind();
    }

    public static Operation clearFormItems(String namelist) {
        return new ClearFormItems(namelist);
    }

    public static Operation playQueuedPrompts() {
        return new PlayQueuedPlayables();
    }

    public static Operation retrieveProperty_P(String timeout) {
        return new RetrieveProperty_P(timeout);
    }

    public static Operation popDtmfUtterance() {
        return new PopDtmfUtterance();
    }

    public static VXMLOperationBase sendTransferEvent(DebugInfo debugInfo, String type, String maxtime, String connecttimeout, String aai, String aaiexpr) {
        return new SendTransferEvent_T(debugInfo, type, maxtime,connecttimeout, aai, aaiexpr);
    }

    public static Executable pair_P(String name, String value) {
        return new Pair_P(name,value);
    }

    public static Executable pair_TP(String name) {
        return new Pair_TP(name);

    }

    public static Executable hasParam_P(String namePart) {
        return new HasParam_P(namePart);

    }

    public static Executable getParam_P(String namePart) {
        return new getParam_P(namePart);

    }

    public static Executable reference(Product filledItems) {
        return new Reference(filledItems);

    }

    public static Operation bargeinHandler() {
        return new BargeinHandler();
    }

    public static Operation onPlayEvent() {
        return new OnPlayEvent();
    }

    public static Operation setCurrentFormItem(String localName) {
        return new SetCurrentFormItem(localName);
    }

    public static Operation getProperty_P(String prop) {
        return new GetProperty_P(prop);  //To change body of created methods use File | Settings | File Templates.
    }

    public static Operation retrieveCurrentEvent() {
        return new RetrieveCurrentEvent();
    }

    public static Operation enterFinalProcessingState(boolean force) {
        return new EnterFinalProcessingState(force);
    }

    public static Executable createPair_T2P() {
        return new CreatePair_T2P();

    }

    public static Executable sendEvent_TMT4(DebugInfo debugInfo) {
        return new SendEvent_TMT4(debugInfo);

    }

    public static Operation queuePrompt(PromptImpl prompt) {
        return new QueuePrompt(prompt);
    }

    public static Operation setPlayingObject(PlayableObjectImpl o) {
        return new SetAndPlayPlayingObject(o);
    }

    public static Operation setPlayObjectToAlternative() {
        return new SetPlayObjectToAlternative();
    }

    public static Operation playObject() {
        return new PlayObject();
    }

    public static Operation createPlayableObject_TM_P(boolean isChildOfPrompt, boolean isInTransfer) {
        return new CreatePlayableObject_TM_P(isChildOfPrompt, isInTransfer);
    }

    public static Operation queuePlayableObject_T() {
        return new QueuePlayableObject_T();
    }

    public static Operation queuePlayableObject_TM() {
        return new QueuePlayavbleObjects_TM();
    }

    public static Operation assignToCurrentItem_T() {
        return new AssignToCurrentItem_T();
    }

    public static Operation setRecordingStopped() {
        return new SetRecordingStopped();
    }

    public static Operation initializeCatchFIA() {
        return new InitializeCatchFIA();
    }

    public static Operation initializeVarFIA() {
        return new InitializeVarFIA();
    }

    public static Operation createPlayableMarkObject_P(String val, boolean doEval) {
        return new CreatePlayableMarkObject_P(val, doEval);
    }

    public static Operation collectPhaseRegisterPropsFIA(String id) {
        return new CollectPhaseRegisterPropsFIA(id);
    }

    public static Executable compileCacheAndEvaluate(String inlineScript, String cacheKey, URI uri, int lineNumber) {
        return new CompileCacheAndEvaluate(inlineScript, cacheKey, uri, lineNumber);
    }

    public static Operation enterHandlerScope() {
        return new EnterHandlerScope();
    }

    public static Operation leftHandlerScope() {
        return new LeftHandlerScope();
    }

    public static Executable atomic(Operation ... op) {
        return new AtomicExecutable(op);
    }

    public static Operation setInihibitRecording(boolean value) {
        return new SetInhibitRecording(value);
    }

    public static Operation initiatedDisconnect() {
        return new InitiatedDisconnect();
    }

    public static Operation setTransferVariables() {
        return new SetTransferVariables();
    }

    public static Operation connectionJoin_T3(DebugInfo instance) {
        return new ConnectionJoin_T3(instance);
    }

    public static Operation cancelEvent_T() {
        return new CancelEvent_T();
    }

    public static Executable sendEvent_T2(DebugInfo instance) {
        return new SendEvent_T2(instance);
    }

    public static Executable sendFieldEvent_T2(DebugInfo instance) {
        return new SendFieldEvent_T2(instance);
    }

    public static Operation setPromptProperties(Map<String, String> promptProps) {
        return new SetPromptProperties(promptProps);
    }

    public static Operation registerCatches() {
        return new RegisterCatchesAndProps();
    }

    public static Operation connectionUnjoin_T2(DebugInfo instance) {
        return new ConnectionUnjoin_T2(instance);
    } 


    public static Operation closeSession() {
        return new CloseSession();
    }

    public static Operation bargeinHandler_P() {
        return new BargeinHandler_P();
    }

    public static Operation changeExecutionResult_T() {
        return new ChangeExecutionResult_T();
    }

    public static Operation log(String s) {
        return new Log(s);
    }

    public static Operation logElement(CompilerElement element) {
        return Ops.log("Executing " + element.getTagHead() + " (line " + element.getLine() + ')');
    }

    public static VXMLOperationBase setIsExiting() {
        return new SetIsExiting();
    }

    public static Operation nop() {
        return new Nop();
    }

    public static Operation initiateTransfer() {
        return new InitiateTransfer();
    }

        public static VXMLOperationBase setValueToASRInterpretation() {
            return new SetValueToASRInterpretation();
        }

    public static VXMLOperationBase terminateRecordIfASRUtterance() {
        return new TerminateRecordIfASRUtterance();
    }

    public static VXMLOperationBase terminateTransferIfASRUtterance() {
        return new TermintateTransferIfASRUtterance();
    }

    public static Executable inhibitRecordingIfBufferedDTMF() {
        return new InhibitRecordingIfBufferedDTMF();

    }

    public static Operation setAbortPrompts() {
        return new SetAbortPrompts();

    }

    public static Operation popValueStack() {
        return new PopValueStack();
    }

    public static Operation waitIfOutstandingPlayFinished() {
        return new WaitIfOutstandingPlayFinished();
    }

    public static Operation addBargeinHandler() {
        return new AddBargeinHandler();
    }

    public static Operation waitForEvents() {
        return new WaitForEvents();
    }

    public static Operation startRecognizer() {
        return new StartRecognizer();
    }

    public static Executable debugOp() {
        return new DebugOp();
    }

    public static Operation enterTransitioningState() {
        return new EnterTransitioningState();
    }

    public static Operation enterWaitingState() {
        return new EnterWaitingState();
    }
}
