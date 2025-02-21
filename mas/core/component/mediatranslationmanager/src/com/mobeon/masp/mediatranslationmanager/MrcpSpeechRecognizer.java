/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.configuration.SpeechRecognizerConfiguration;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.MrcpEventListener;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RecognizeSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.MrcpEventId;
import com.mobeon.masp.stream.*;
import com.mobeon.masp.util.executor.ExecutorServiceManager;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class MrcpSpeechRecognizer implements SpeechRecognizer, MrcpEventListener {
    enum ServiceState {IDLE, PREPARING, PREPARED, RECOGNIZING, RECOGNIZED }
    private enum Command {PREPARE, RECOGNIZE, STOP, UNJOIN, CANCEL }
    private static ILogger logger = ILoggerFactory.getILogger(MrcpSpeechRecognizer.class);
    private MediaTranslationFactory factory = MediaTranslationFactory.getInstance();
    private IOutboundMediaStream outbound = null;
    private IInboundMediaStream inbound = null;
    private static final String mrcpMimeType = "application/grammar+xml";
    private IEventDispatcher eventDispatcher = null;
    private RecognizeSession recognizeSession = null;
    private ISession session;
    private Map<String, String> grammars;
    private ServiceState serviceState = ServiceState.IDLE;
    private ExecutorService executorService =
            ExecutorServiceManager.getInstance().getExecutorService(MrcpSpeechRecognizer.class);


    public MrcpSpeechRecognizer(Map<String, String> grammars) {
        this.grammars = new HashMap<String, String>(grammars);
    }

    public MrcpSpeechRecognizer(SpeechRecognizer template) {
        if (template instanceof MrcpSpeechRecognizer) {
            MrcpSpeechRecognizer master = (MrcpSpeechRecognizer)template;
            grammars = new HashMap<String, String>(master.getGrammars());
        }
    }

    void setSession(ISession session) {
        this.session = session;
    }

    public ISession getSession() {
        return session;
    }

    public MrcpSpeechRecognizer(ISession session) {
        this.session = session;
    }

    /**
     * Informs the SpeechRecognizer that it's services will soon be needed.
     * Gives the implementation time to set up necessary sessions etc.
     * <strong>Note:</strong> This method is informational only, not calling
     * it before recognize must not be an error.
     */
    public void prepare() {
        AsynchronousExecuter commands = new AsynchronousExecuter();
        if (serviceState != ServiceState.IDLE) return;
        commands.add(Command.PREPARE);
        serviceState = ServiceState.PREPARING;
        executorService.execute(commands);
    }

    /**
     * Activates the recognizer with the given IInboundMediaStream as dataSource.
     * Calling recognize several times in sequence should have the same effect
     * as calling <code>cancel()</code> followed by recognize.
     *
     * @param inboundStream
     */
    public void recognize(IInboundMediaStream inboundStream) {
         if (logger.isDebugEnabled()) logger.debug("--> recognize()");
        AsynchronousExecuter commands = new AsynchronousExecuter();
        switch (serviceState) {
            case IDLE:
                if (logger.isDebugEnabled()) logger.debug("Adding PREPARE");
                commands.add(Command.PREPARE);
                break;

            case RECOGNIZING:
                 if (logger.isDebugEnabled()) logger.debug("Adding STOP");
                commands.add(Command.STOP);
//                commands.add(Command.UNJOIN);
                break;

            case PREPARED:
            case RECOGNIZED:
                break;

            default:
                break;
        }
        this.inbound = inboundStream;
        this.eventDispatcher = inboundStream.getEventDispatcher();
        commands.add(Command.RECOGNIZE);
        executorService.execute(commands);
         if (logger.isDebugEnabled()) logger.debug("<-- recognize()");
    }

    /**
     * Cancels an ongoing recognition and sends an appropriate recognition event.
     */
    public void cancel() {
        AsynchronousExecuter commands = new AsynchronousExecuter();
        switch (serviceState) {
            case IDLE:
                return;

            case RECOGNIZING:
                commands.add(Command.STOP);
                commands.add(Command.UNJOIN);
                break;

            default:
                break;
        }
        commands.add(Command.CANCEL);
        executorService.execute(commands);
    }

    boolean createRecognizeSession() {
        if (logger.isDebugEnabled()) logger.debug("--> createRecognizeSession()");
        SpeechRecognizerConfiguration configuration =
                MediaTranslationConfiguration.getInstance().getSpeechRecognizerConfiguration();
        String recognizerHost = configuration.getHost();
        Collection<RTPPayload> audio = new ArrayList<RTPPayload>();
        audio.add(RTPPayload.get(RTPPayload.AUDIO_PCMU));

        ConnectionProperties connectionProperties = new ConnectionProperties();
        int outboundPort = -1;
        // TODO: get port number from set-up response ...
         if (logger.isDebugEnabled()) logger.info("Open MRCP ASR session");
        try {
            // Creating an RTSP session connected to the RTSP host service port
            RtspSession rtspSession = factory.createRtspSession(recognizerHost, configuration.getPort());
            rtspSession.setSession(session);
            // TODO: should be able to provide pTime
            // Creating an MRCP recognizer session the recognizer session is supposed
            // to be listening to the outbound port.
            recognizeSession = new RecognizeSession(rtspSession, outboundPort);
        } catch (Exception e) {
             if (logger.isDebugEnabled()) logger.debug("To set up a recognize session: " + e);
            return false;
        }
        // Establishing the session
        recognizeSession.setup();
        // Determining status of the session
        if (!recognizeSession.isOk()) {
            logger.error("Failed to open a recognize session");
            logger.error("RTSP Status Code: " + recognizeSession.getLastRtspStatusCode());
            logger.error("RTSP Status Text: " + recognizeSession.getLastRtspStatusText());
            logger.error("MRCP Status Code: " + recognizeSession.getLastMrcpStatusCode());
            logger.error("MRCP Status Text: " + recognizeSession.getLastMrcpStatusText());
            return false;
        }
         if (logger.isDebugEnabled()) logger.debug("Audio server port: " + recognizeSession.getRtpServerPort());
        connectionProperties.setAudioPort(recognizeSession.getRtpServerPort());
        connectionProperties.setAudioHost(recognizerHost);
        // TODO: get pTime/maxPTime from configuration
        connectionProperties.setPTime(20);
        connectionProperties.setMaxPTime(20);
        try {
            outbound = factory.getStreamFactory().getOutboundMediaStream();
            outbound.setEventDispatcher(new DummyEventDispatcher());
            outbound.create(audio, connectionProperties);
        } catch (StackException e) {
            logger.error("Failed to create outbound media stream: " + e);
            return false;
        } catch (Exception e) {
            logger.error("Failed to create outbound media stream: " + e);
            return false;
        }
        recognizeSession.attachMrcpEventListener(this);
         if (logger.isDebugEnabled()) logger.debug("<-- createRecognizeSession()");
        return true;
    }

   void deleteRecognizeSession() {
        if (logger.isDebugEnabled()) logger.debug("--> deleteRecognizeSession()");
        try {
            if (outbound != null) outbound.delete();
        } catch (Exception e) {
            logger.error("Failed to delete outbound media stream: " + e);
        }
       outbound = null;
       if (logger.isDebugEnabled()) logger.debug("<-- deleteRecognizeSession()");
    }

    private void defineGrammars() {
         if (logger.isDebugEnabled()) logger.debug("--> defineGrammars()");
        // TODO: this should be performed in one request ...
        Iterator<Map.Entry<String, String>> iterator = grammars.entrySet().iterator();
        do {
            Map.Entry<String, String> entry = iterator.next();
            setGrammar(entry.getValue(), entry.getKey());
        } while (iterator.hasNext());
         if (logger.isDebugEnabled()) logger.debug("<-- defineGrammars()");
   }


    /**
     * Sets/defines a grammar.
     * A grammar is sent to the ASR engine. The grammar is indentified by a grammar
     * id which is defined by the caller. The id is used when performing a recognition.
     *
     * @param grammar   a grammar XML.
     * @param grammarId the name/id of the grammar.
     */
    private void setGrammar(String grammar, String grammarId) {
        if (logger.isInfoEnabled()) logger.info("MRCP ASR define grammar [" + grammarId + "]");
        if (recognizeSession == null) {
            logger.error("Could not set grammar, MRCP session is null");
            return;
        }
        if (!recognizeSession.defineGrammar(mrcpMimeType, grammar, grammarId)) {
            logger.warn("MRCP ASR failed to define grammar.");
            logger.warn("Grammar: [" + grammar + "]");
            logger.warn("Grammar ID: [" + grammarId + "]");
        }
    }

    /**
     * Performs a recognition.
     * The speech is recognized according to the predefined grammar (gramamr id).
     * Before the recognition can be performed at least one grammar. With
     * other words a grammar must be set before recognition can be performed.
     * This method is asynchrounous and the result of the recognition (success or
     * failure) is distributed through the events {@link RecognitionCompleteEvent}
     * and/or {@link RecognitionFailedEvent}.
     *
     * @param grammarIds the name of the predefined grammar.
     */
    private void recognize(String ... grammarIds) {
        if (logger.isInfoEnabled()) logger.info("Performing MRCP ASR using grammars [" + grammarIds + "]");
        if (recognizeSession == null) {
            logger.error("Could not recognize, MRCP session is null");
            return;
        }
        try {
            if (!outbound.isJoined()) inbound.join(outbound);
        } catch (Exception e) {
            logger.error("Failed to join inbound media stream: " + e);
            outbound.translationFailed("" + e);
            return;
        }
        recognizeSession.recognize(grammarIds);
    }


    public void handleMrcpEvent(MrcpEventId event, String message) {
        switch (event) {
            case RECOGNITION_COMPLETE_SUCCESS:
                if (logger.isInfoEnabled()) logger.info("MRCP ASR success [" + message + "]");
                eventDispatcher.fireEvent(new RecognitionCompleteEvent(message));
                break;

            case RECOGNITION_COMPLETE_FAIL:
                if (logger.isInfoEnabled()) logger.info("MRCP ASR failed [" + message + "]");
                eventDispatcher.fireEvent(new RecognitionFailedEvent(message));
                break;

            case RECOGNITION_COMPLETE_NO_INPUT:
                if (logger.isInfoEnabled()) logger.info("MRCP ASR failed [" + message + "]");
                eventDispatcher.fireEvent(new RecognitionNoInputEvent());
                break;

            case RECOGNITION_COMPLETE_NO_MATCH:
                if (logger.isInfoEnabled()) logger.info("MRCP ASR failed [" + message + "]");
                eventDispatcher.fireEvent(new RecognitionNoMatchEvent());
                break;

            default:
                logger.error("Unhandled MrcpEventId: " + event);
                break;
        }
        serviceState = ServiceState.RECOGNIZED;
        // TODO: on failure close()
    }

    private void executePrepare() {
        if (logger.isDebugEnabled()) logger.debug("--> executePrepare()");
        if (recognizeSession == null) {
            serviceState = ServiceState.PREPARING;
            createRecognizeSession();
            defineGrammars();
            serviceState = ServiceState.PREPARED;
        }
        if (logger.isDebugEnabled()) logger.debug("<-- executePrepare()");
    }

    private void executeRecognize() {
        if (logger.isDebugEnabled()) logger.debug("--> executeRecognize()");
        if (recognizeSession != null) {
            serviceState = ServiceState.RECOGNIZING;
            String[] grammarIds = new String[grammars.size()];
            grammars.keySet().toArray(grammarIds);
            recognize(grammarIds);
        }
        if (logger.isDebugEnabled()) logger.debug("<-- executeRecognize()");
    }

    private void executeStop() {
        if (logger.isDebugEnabled()) logger.debug("--> executeStop()");
        if (recognizeSession != null) {
            recognizeSession.stop();
            if (logger.isInfoEnabled()) logger.info("MRCP ASR stopped/cancelled");
        } else {
            if (logger.isInfoEnabled()) logger.info("Nullpointer session, was it opened?");
        }
        if (logger.isDebugEnabled()) logger.debug("<-- executeStop()");
    }

    private void executeUnjoin() {
        if (logger.isDebugEnabled()) logger.debug("--> executeUnjoin()");
        if (inbound != null && outbound != null && outbound.isJoined()) {
            try {
                inbound.unjoin(outbound);
            } catch (StackException e) {
                logger.error("Failed to unjoin streams: " + e);
            }
        }
        if (logger.isDebugEnabled()) logger.debug("<-- executeUnjoin()");
    }

    private void executeCancel() {
        if (logger.isDebugEnabled()) logger.debug("--> executeCancel()");
        if (logger.isInfoEnabled()) logger.info("Closing MRCP ASR session");
        if (recognizeSession != null) {
            recognizeSession.teardown();
        } else {
            if (logger.isInfoEnabled()) logger.info("Nullpointer session, was it opened?");
        }
        serviceState = ServiceState.IDLE;
        session = null;
        grammars = null;
        if (logger.isDebugEnabled()) logger.debug("<-- executeCancel()");
    }

    Map<String, String> getGrammars() {
        return grammars;
    }

    ServiceState getServiceState() {
        return serviceState;
    }

    class AsynchronousExecuter implements Runnable{
        private Collection<Command> commands = new LinkedList<Command>();

        public void add(Command command) {
            commands.add(command);
        }

        /**
         * This the abstract executor of a recognize.
         * The this thread will terminate when the recognize command is issued.
         */
        public void run() {
            if (logger.isDebugEnabled()) logger.debug("--> AsynchronousExecuter.run()");
            try {
                for (Command command : commands) {
                    if (logger.isDebugEnabled()) logger.debug("Executing command: " + command);
                    switch (command) {
                        case PREPARE:
                            executePrepare();
                            break;

                        case RECOGNIZE:
                            executeRecognize();
                            break;

                        case STOP:
                            executeStop();
                            break;

                        case UNJOIN:
                            executeUnjoin();
                            break;

                        case CANCEL:
                            executeCancel();
                            break;

                        default:
                            if (logger.isInfoEnabled()) logger.info("Unhandled command: " + command);
                            break;
                    }
                }
            } catch (Throwable e) {
                logger.warn("Unknown exeption", e);
            }
            commands.clear();
            if (logger.isDebugEnabled()) logger.debug("<-- AsynchronousExecuter.run()");
        }
    }
}
