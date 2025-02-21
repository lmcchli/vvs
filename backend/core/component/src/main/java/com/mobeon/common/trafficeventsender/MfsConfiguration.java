/**
 *
 */
package com.mobeon.common.trafficeventsender;

import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author egeobli
 *
 */
public class MfsConfiguration {
    public static final String SLAMDOWN_QUEUE_SIZE = "slamdownQueueSize";
    public static final String SLAMDOWN_THRESHOLD = "slamdownThreshold";
    public static final String SLAMDOWN_TIMEOUT = "slamdownTimeout";
    public static final String SLAMDOWN_WORKERS = "slamdownWorkers";
    public static final String MCN_QUEUE_SIZE = "missedCallNotificationQueueSize";
    public static final String MCN_THRESHOLD = "missedCallNotificationThreshold";
    public static final String MCN_TIMEOUT = "missedCallNotificationTimeout";
    public static final String MCN_WORKERS = "missedCallNotificationWorkers";

    private static final ILogger logger = ILoggerFactory.getILogger(MfsConfiguration.class);

    /** Slamdown Event queue maximum size. */
    private int slamdownEventQueueSize = 100;
    /** Slamdown Event timeout in seconds. */
    private int slamdownEventTimeout = 100;
    /** Number of slamdown events before flushing queue. */
    private int slamdownEventThreshold = 3;
    /** Number of slamdown workers */
    private int slamdownWorkers = 10;
    /** Mcn Event queue maximum size. */
    private int mcnEventQueueSize = 100;
    /** Mcn Event timeout in seconds. */
    private int mcnEventTimeout = 100;
    /** Number of Mcn events before flushing queue. */
    private int mcnEventThreshold = 3;
    /** Number of Mcn workers. */
    private int mcnWorkers = 10;

	public void readConfiguration(IGroup configuration) throws ConfigurationException {

		slamdownEventQueueSize = configuration.getInteger(SLAMDOWN_QUEUE_SIZE);
		slamdownEventTimeout = configuration.getInteger(SLAMDOWN_TIMEOUT);
		slamdownEventThreshold = configuration.getInteger(SLAMDOWN_THRESHOLD);
		slamdownWorkers = configuration.getInteger(SLAMDOWN_WORKERS);
		mcnEventQueueSize = configuration.getInteger(MCN_QUEUE_SIZE);
		mcnEventTimeout = configuration.getInteger(MCN_TIMEOUT);
		mcnEventThreshold = configuration.getInteger(MCN_THRESHOLD);
		mcnWorkers = configuration.getInteger(MCN_WORKERS);

		if (logger.isDebugEnabled()) {
			logger.debug("MFS Traffic Event Queue configuration: " +
					SLAMDOWN_QUEUE_SIZE + "=" + slamdownEventQueueSize + "; " +
					SLAMDOWN_TIMEOUT + "=" + slamdownEventTimeout + "; " +
					SLAMDOWN_THRESHOLD + "=" + slamdownEventThreshold + "; " +
					SLAMDOWN_WORKERS + "=" + slamdownWorkers + "; " +
					MCN_QUEUE_SIZE + "=" + mcnEventQueueSize + "; " +
					MCN_TIMEOUT + "=" + mcnEventTimeout + "; " +
					MCN_THRESHOLD + "=" + mcnEventThreshold  + "; " +
					MCN_WORKERS + "=" + mcnWorkers
			);
		}
	}

	public void setSlamdownEventQueueSize(int slamdownEventQueueSize) {
		this.slamdownEventQueueSize = slamdownEventQueueSize;
	}

	public int getSlamdownEventQueueSize() {
		return slamdownEventQueueSize;
	}

	public void setSlamdownEventTimeout(int slamdownEventTimeout) {
		this.slamdownEventTimeout = slamdownEventTimeout;
	}

	public int getSlamdownEventTimeout() {
		return slamdownEventTimeout;
	}

	public void setSlamdownEventThreshold(int slamdownEventThreshold) {
		this.slamdownEventThreshold = slamdownEventThreshold;
	}

	public int getSlamdownEventThreshold() {
		return slamdownEventThreshold;
	}

	public void setSlamdownWorkers(int slamdownWorkers) {
	    this.slamdownWorkers = slamdownWorkers;
	}

	public int getSlamdownWorkers() {
	    return slamdownWorkers;
	}

	public void setMcnEventQueueSize(int mcnEventQueueSize) {
		this.mcnEventQueueSize = mcnEventQueueSize;
	}

	public int getMcnEventQueueSize() {
		return mcnEventQueueSize;
	}

	public void setMcnEventTimeout(int mcnEventTimeout) {
		this.mcnEventTimeout = mcnEventTimeout;
	}

	public int getMcnEventTimeout() {
		return mcnEventTimeout;
	}

    public void setMcnEventThreshold(int mcnEventThreshold) {
        this.mcnEventThreshold = mcnEventThreshold;
    }

    public int getMcnEventThreshold() {
        return mcnEventThreshold;
    }

    public void setMcnWorkers(int mcnWorkers) {
        this.mcnWorkers = mcnWorkers;
    }

    public int getMcnWorkers() {
        return mcnWorkers;
    }
}
