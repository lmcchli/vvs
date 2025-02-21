package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.runtime.ExecutionContextBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.logging.ILogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Category;

/**
 * Aggregates DTMF and ASR input<br/>
 *
 * <em>Important: Because the controlTokenQ <strong>reference</strong> is updated by pushBackToken
 * all methods touching controlTokenQ MUST be synchronized. </em>
 */
public class InputAggregator {

    private BlockingQueue<ControlTokenData> controlTokenQ = new LinkedBlockingQueue<ControlTokenData>();
    private static final Object controlTokenQLock = new Object();
    private com.mobeon.common.logging.ILogger logger = ILoggerFactory.getILogger(getClass());


    /**
     * Add a control token to the control token queue, if in state allowing
     * addition of control tokens.
     *
     * @param ctd
     */
    public void addControlToken(ControlTokenData ctd) {
        synchronized (controlTokenQLock) {
            if (logger.isDebugEnabled())
                logger.debug("Adding token " + ctd.getToken().getTokenDigit() + " to DTMF queue");
            controlTokenQ.add(ctd);
        }
    }

    /**
     * Retrieve a control token, e.g. DTMF, from the control token queue, if the queue
     * is non-empty. If the block parameter is TRUE, the call will block until data is
     * available in the queue. Otherwise the call will return NULL if the queue is empty.
     *
     * @return Removes and returns the head of the queue if the queue is non-empty, NULL otherwise
     */
    public ControlTokenData getControlToken() {
        synchronized (controlTokenQLock) {
            if (logger.isDebugEnabled()) {
                logger.debug("Trying to take token from DTMF queue, queue size is " + controlTokenQ.size());
            }
            ControlTokenData ret;
            ret = controlTokenQ.poll();
            if (logger.isDebugEnabled()) {
                if (ret != null)
                    logger.debug("Retrieved token " + ret.getToken().getToken().digit() + " from DTMF queue");
                else
                    logger.debug("Failed to retrieve token from DTMF queue.");
            }
            return ret;
        }
    }

    public void pushBackToken(ControlTokenData cdt) {
        synchronized (controlTokenQLock) {
            if (logger.isDebugEnabled())
                logger.debug("Pushing back token " + cdt.getToken().getToken().digit() + " on DTMF queue");
            BlockingQueue<ControlTokenData> reordered = new LinkedBlockingQueue<ControlTokenData>();
            reordered.add(cdt);
            controlTokenQ.drainTo(reordered);
            controlTokenQ = reordered;
        }
    }

    public boolean hasControlToken() {
        synchronized (controlTokenQLock) {
            return !controlTokenQ.isEmpty();
        }
    }

    /**
     * Clear the control token queue.
     */
    public boolean clearControlTokenQ() {
        synchronized (controlTokenQLock) {
            if (controlTokenQ.isEmpty()) {
                return false;
            } else {
                controlTokenQ.clear();
                return true;
            }
        }
    }

    /**
     * Drains a controlTokenQ safely into this instance, ensuring locking
     * both InputAggregators before draining.
     * @param inputAggregator
     */
    public void drainFrom(InputAggregator inputAggregator) {
        synchronized(controlTokenQLock) {
            controlTokenQ.clear();
            inputAggregator.drainTo(this);
        }
    }

    private void drainTo(InputAggregator inputAggregator) {
        synchronized(controlTokenQLock) {
            controlTokenQ.drainTo(inputAggregator.controlTokenQ);
        }
    }
}
