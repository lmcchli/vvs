#ifndef OUTPUTPROCESSOR_H_
#define OUTPUTPROCESSOR_H_

#include <base_std.h>
#include "Processor.h"
#include "outboundsession.h"
#include "CallbackQueue.h"

//#define OUTPUT_SCALING_FUNCTION(x) x
#define TIMESLOT_RESOLUTION 10
#define FUTURE_LENGTH 250
#define OUTPUT_CONTROL_TICK_CHECK 200

class OutputProcessor: public Processor
{
public:
    OutputProcessor(int id);
    virtual ~OutputProcessor();

    static void setupProcessors(boost::ptr_vector<Processor> &processors, int nOfProcessors);
    static void shutdownProcessors(boost::ptr_vector<Processor> &processors);

    void registerSession(SessionSupport& session);
    void unRegisterSession(SessionSupport& session, int requestId);

protected:
    virtual void initial();
    virtual void process();
    void sendAndSchedule(OutboundSession &session);

    std::list<OutboundSession*> mNewSessions;

    std::deque<std::list<OutboundSession*> > mFutureTimeslots;

    uint64 mNextSlot;

    unsigned mMaxSendList;

    std::auto_ptr<CallbackQueue> m_callbacks;
};

#endif /*OUTPUTPROCESSOR_H_*/
