#include "streamutil.h"
#include <cc++/thread.h>



void StreamUtil::getTimeOfDay(struct timeval& time) {
	ost::SysTime::gettimeofday(&time,0);
}

bool StreamUtil::timedOut(struct timeval& time, struct timeval& now, 
                                 uint32 timeoutMs) {
    StreamUtil::getTimeOfDay(now);
    long diff = timeDiff(time,now);

    return diff > timeoutMs;
}

long StreamUtil::timeToWait(struct timeval& timeoutTime,uint32 addToTimeoutDelta)
{
	struct timeval now;
    StreamUtil::getTimeOfDay(now);
	int result = timeDiff(now,timeoutTime)+addToTimeoutDelta;;
    return result;
}

long StreamUtil::timeDiff(struct timeval& startTime,struct timeval& endTime)
{
	return endTime.tv_sec*1000+endTime.tv_usec/1000-startTime.tv_sec*1000-startTime.tv_usec/1000;
}

uint64 StreamUtil::timeDiff64(struct timeval& startTime,struct timeval& endTime)
{
	return endTime.tv_sec*1000+endTime.tv_usec/1000-startTime.tv_sec*1000-startTime.tv_usec/1000;
}


void StreamUtil::incTimeval(struct timeval& tv,int deltaMs) {
	
	tv.tv_sec += deltaMs/1000;
	tv.tv_usec += deltaMs*1000;
	if(tv.tv_usec > 999999) {
		tv.tv_sec+=tv.tv_usec/1000000;
		tv.tv_usec %= 1000000;
	}
}

void StreamUtil::formatTimeval(struct timeval& tv, base::String& result) {
    char tmp[40];
    
    time_t timep(tv.tv_sec);
    // Compute milliseconds from microseconds.
    long milliseconds = tv.tv_usec / 1000l;
    
    // Obtain the time of day, and convert it to a tm struct.
    struct tm *ptm = localtime (&timep);
    // Format the date and time, down to a single second.
    strftime (tmp, sizeof (tmp), "%Y-%m-%d:%H:%M:%S.", ptm);

    char tmpAll[80];
    // Add milliseconds
    sprintf(tmpAll,"%s%03ld", tmp, milliseconds);
    result = tmpAll;
}

