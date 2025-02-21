#ifndef INPUTPROCESSOR_H_
#define INPUTPROCESSOR_H_

#include <cc++/thread.h>
#include <boost/scoped_array.hpp>
#include <base_std.h>

#include <sys/epoll.h>
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#define INPUT_FDALLOC   8192
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>

#include "inboundsession.h"
#include "Processor.h"

#define INPUT_POLL_YIELD_MICROS (10*1000)
#define INPUT_POLL_YIELD_NANOS (INPUT_POLL_YIELD_MICROS*1000)
//#define INPUT_SCALING_FUNCTION(x) (4*x)

class ReceptionAdapter
{
public:
    ReceptionAdapter(InboundSession &session, SOCKET so, bool isVideo);
    virtual ~ReceptionAdapter();

    InboundSession& getSession();
    SOCKET getSocket();

    bool isVideo();

    virtual void handleReception(uint64 timeRef);

protected:
    bool mIsVideo;
    InboundSession& mSession;
    SOCKET mSock;
};

class ControlReceptionAdapter: public ReceptionAdapter
{
public:
    ControlReceptionAdapter(InboundSession &session, bool isVideo);
    virtual ~ControlReceptionAdapter();

    virtual void handleReception(uint64 timeRef);
};

class DataReceptionAdapter: public ReceptionAdapter
{
public:
    DataReceptionAdapter(InboundSession &session, bool isVideo);
    virtual ~DataReceptionAdapter();

    virtual void handleReception(uint64 timeRef);
};

class InputProcessor: public Processor
{
public:
    InputProcessor(int id);
    virtual ~InputProcessor();
    static void setupProcessors(boost::ptr_vector<Processor> &processors, int nOfProcessors);
    static void shutdownProcessors(boost::ptr_vector<Processor> &processors);

    virtual void registerSession(SessionSupport& session);
    virtual void unRegisterSession(SessionSupport& session, int requestId);

protected:

    virtual void initial();
    virtual void process();

    void associateSession(InboundSession &session);
    void dissociateSession(InboundSession &session);

    void updatePolledSet();

    std::list<InboundSession*> mNewSessions;
    std::list<InboundSession*> mToDeleteSessions;

    int mSocketCount;

#if defined(WIN32) || defined(LINUX_SELECT)
    std::map<int,ReceptionAdapter*> mAdapters;

    fd_set mFdReadSet;
    fd_set mFdErrorSet;

    void poll();
    void associate(SOCKET sock,ReceptionAdapter& adapter);
    void checkFdSet(SOCKET sock,bool isError);
#else
#if defined(LINUX_EPOLL)
    boost::scoped_array<struct epoll_event> mPortEventArray;

#else
    boost::scoped_array<port_event_t> mPortEventArray;
#endif
    int mPort;
    void markAsDeleted(InboundSession &session);
    void processDeletedSessions();
    void poll();
    void associate(SOCKET sock, ReceptionAdapter& adapter);
    void reassociate(int, ReceptionAdapter& adapter);
#endif
};

#endif /*INPUTPROCESSOR_H_*/
