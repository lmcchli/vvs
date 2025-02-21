package com.mobeon.masp.stream;

import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Call Session Mapper for keeping track of call session information.
 * Since the call session is lost when the C++ domain is callbacking the Java domain
 * the Call Session Mapper holds a reference to the current active call callIdToSession.
 */
public class CallSessionMapper {
    private static final ILogger LOGGER =
            ILoggerFactory.getILogger(CallSessionMapper.class);
    private static final Object lock = new Object();
    private static CallSessionMapper singletonInstance = null;
    private final ConcurrentHashMap<Object,ISession>  callIdToSession;
    private final ConcurrentHashMap<IMediaStream, Object>  streamToCallId;

    /**
     * Constructor.
     */
    private CallSessionMapper() {
        LOGGER.info("Singleton constructor");
        callIdToSession = new ConcurrentHashMap<Object, ISession>();
        streamToCallId = new ConcurrentHashMap<IMediaStream, Object>();
    }

    /**
     * Singleton instance getter.
     * @return the singleton instance.
     */
    public static CallSessionMapper getInstance() {
        if (singletonInstance == null) {
            synchronized(lock) {
                if (singletonInstance == null) {
                    singletonInstance = new CallSessionMapper();
                }
            }
        }
        return singletonInstance;
    }

    /**
     * Inserts a session into the map.
     * @param callId the ID of the call
     * @param session the call session
     */
    public void putSession(Object callId, IMediaStream stream, ISession session) {
//        LOGGER.debug("putSession: [" + callId + "][" + stream + "][" + session +"]");
        if (callId != null && session != null) {
            callIdToSession.put(callId, session);
            streamToCallId.put(stream, callId);
        } else {
            LOGGER.info("Cannot store session");
            if (callId == null) LOGGER.info("callId is null");
            if (session == null) LOGGER.info("session is null");
        }
    }

    /**
     * Retrieves a call session from the map.
     * @param callId a call ID
     * @return the call session (null if the call ID was not found)
     */
    public ISession getSession(Object callId) {
        if (callId == null) {
            LOGGER.warn("Input callId is null");
            return null;
        } if (callId instanceof IMediaStream) {
            IMediaStream stream = (IMediaStream)callId;
            callId = streamToCallId.get(stream);
            if (callId == null) {
                // This is actually not a problem it's just that
                // the stream is deleted (removed from the map)
                // probably due to slamdown and heavy load.
                LOGGER.debug("Could not retrieve stream");
                return null;
            }
        }
        return callIdToSession.get(callId);
    }

    /**
     * Removes a call session from the map.
     * @param stream the stream
     * @return the call session (null if the call ID was not found)
     */
    public ISession popSession(IMediaStream stream) {
        Object callId = streamToCallId.remove(stream);
        ISession session = null;

        if (callId != null) {
            session = callIdToSession.remove(callId);
        }
        return session;
    }

    /**
     * Clean the lists.
     * This is a test purpose method.
     */
    public void clear() {
        streamToCallId.clear();
        callIdToSession.clear();
    }

    /**
     * Size getter
     * This is a test purpose method.
     */
    public int getStreamToCallIdSize() {
        return streamToCallId.size();
    }

    /**
     * Size getter
     * This is a test purpose method.
     */
    public int getCallIdToSessionSize() {
        return callIdToSession.size();
    }
}
