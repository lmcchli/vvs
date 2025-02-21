/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef STREAMUTIL_H_
#define STREAMUTIL_H_

#include <base_std.h>
#include <base_include.h>

#ifdef WIN32
#include <config.h> // För att __EXPORT ska vara definierad (Pointer.h)
#else
#include <cc++/config.h>
#endif
#include <time.h>

/**
 * Utility methods used in stream.
 * 
 * @author Jörgen Terner
 */
class StreamUtil {
private:
public:
    /**
     * Gets the current timestamp.
     * 
     * @param time Storage place for the timestamp.
     */
    static void getTimeOfDay(struct timeval& time);
    
    /**
     * Checks if <code>now</code> is <code>timeoutMs</code> milliseconds past
     * <code>time</code>.
     * 
     * @param time      Timestamp.
     * @param now       Current time is written here.
     * @param timeoutMs Timeout in milliseconds
     * 
     * @return <code>true</code> in the case of a timeout, <code>false</code>
     *         otherwise.
     */
    static bool timedOut(struct timeval& time, struct timeval& now, 
                         uint32 timeoutMs);
       
    static long timeToWait(struct timeval& timeoutTime,uint32 addToTimeoutDelta);

	static long timeDiff(struct timeval& startTime,struct timeval& endTime);

	static uint64 timeDiff64(struct timeval& startTime,struct timeval& endTime);

    static void incTimeval(struct timeval& tv,int deltaMs);
    
    /**
     * Formats a timeval into a readable format. Useful in debug-messages.
     */
    static void formatTimeval(struct timeval& tv, base::String& result);
};
#endif /*STREAMUTIL_H_*/
