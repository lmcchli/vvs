package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.products.ProductImpl;
import com.mobeon.masp.execution_engine.compiler.products.WaitSetProduct;
import com.mobeon.masp.execution_engine.runtime.PlayableObject;
import com.mobeon.masp.execution_engine.runtime.StartNoInputTimer;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.voicexml.compiler.PromptImpl;
import com.mobeon.masp.execution_engine.voicexml.compiler.operations.SetAndPlayObject;
import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.InputAggregator;
import static com.mobeon.masp.execution_engine.voicexml.runtime.Redirector.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-18
 * Time: 13:02:15
 * To change this template use File | Settings | File Templates.
 */
public class PromptQueue {

    private Redirector redirector;

    private final ILogger logger = ILoggerFactory.getILogger(PromptQueue.class);


    protected List<PromptImpl> promptQueue = new ArrayList<PromptImpl>();
    protected List<PlayableObject> playableQueue = new ArrayList<PlayableObject>();
    protected boolean abortPrompts = false;
    protected boolean abortPlayables = false;

    protected Object playableLock = new Object();

    private PlayableObject playingObject = null;
    private int promptCounter = 1;

    public void setAbortPrompts(boolean val) {
        abortPrompts = val;
    }

    public boolean getAbortPrompts() {
        return abortPrompts;
    }


    public void setPromptQueue(List<PromptImpl> promptQ) {
        this.promptQueue = promptQ;
    }

    public List<PromptImpl> getPromptQueue() {
        return promptQueue;
    }

    public void setPlayingObject(PlayableObject playable) {
        this.playingObject = playable;

    }

    public PlayableObject getPlayableObject() {
        return playingObject;
    }

    public void addPlayableToQueue(PlayableObject playableObject) {
        playableObject.retrieveCurrentPropSettings(VXMLExecutionContext(redirector));
        playableQueue.add(playableObject);
        if (logger.isDebugEnabled())
            logger.debug("Adding playable object to queue. Size of queue is now " + playableQueue.size());

    }

    public void playQueuedPlayableObjects() {

        FIAState fiaState = VXMLExecutionContext(redirector).getFIAState();
        if (fiaState == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("FIA state is null, no FIA has been started. Hence nothing to play");
            }
            return;
        }

        // Instanciate a bargein handler, interrupt play if bargein == true, remove DTMF token if bargein == false
        // TODO: Should be done when the content is played
        setAbortPrompts(false);
        Map<String, String> origProps = VXMLExecutionContext(redirector).getCurrentPromptProperties();


        // Do not return early here since we need at least the
        // StartNoInputTimer to be added to prompt queue
        TestEventGenerator.generateEvent(TestEvent.PROMPT_QUEUE_PLAY, playableQueue.size());

        Product playList = new ProductImpl(null, null, DebugInfo.getInstance());
        boolean addNoInputTimeout = false;
        VXMLExecutionContext vxmlExecutionContext = VXMLExecutionContext(redirector);

        String timeout = vxmlExecutionContext.getProperties().getProperty(Constants.VoiceXML.TIMEOUT);
        for (PlayableObject playable : playableQueue) {
            SetAndPlayObject spo = new SetAndPlayObject(playable);
            playList.add(spo);
            if (playable.isInputItemChild()) {
                addNoInputTimeout = true;
                timeout = playable.getTimeout();
            } else {
                addNoInputTimeout = false;
            }
        }
        playList.add(Ops.startRecognizer());

        // use timeout of last prompt or the property?
        String timeoutToUse = null;
        if (addNoInputTimeout) {
            timeoutToUse = timeout;
        } else {
            timeoutToUse = vxmlExecutionContext.getProperties().getProperty(Constants.VoiceXML.TIMEOUT);
        }
        // Add a special playable object which the PlayableObjectPlayer can
        // use to see that all prompts are played and that it is time to
        // start the noinput-timeout.
        if (fiaState.isExiting()) {
            if (logger.isDebugEnabled())
                logger.debug("Does not start a noinput timer, since we are exiting");
        } else {
            playList.add(new SetAndPlayObject(new StartNoInputTimer(timeoutToUse)));
        }
        playList.add(Ops.setPromptProperties(origProps));
        vxmlExecutionContext.getEngine().call(playList);
        playableQueue.clear();
    }


    public void addPromptsToQueue(List<PromptImpl> prompts) {
        if (prompts.size() > 0) {
            for (PromptImpl prompt : prompts) {
                VXMLExecutionContext(redirector).getEngine().call(prompt);
            }
        }
    }

    public void setPlayableQueue(List<PlayableObject> playableQ) {
        this.playableQueue = playableQ;
    }

    public List<PlayableObject> getPlayableQueue() {
        return playableQueue;
    }

    public int getPromptCounter() {
        return promptCounter;
    }

    public void resetPromptCounter() {
        promptCounter = 1;
    }

    public synchronized void incrementPromptCounter() {
        promptCounter++;
    }


    public void init(Redirector redirector) {
        this.redirector = redirector;
    }

    public void clearPlayableObjects(){
        playableQueue.clear();
    }
}
