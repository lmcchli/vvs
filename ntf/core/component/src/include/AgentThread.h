/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */
#ifndef AGENTTHREAD_H
#define AGENTTHREAD_H

#include <string>
#include "IPMSThread.h"

using namespace std;

class AgentThread : public IPMSThread {
public:
    static AgentThread* instance();
    void setContext(const string context);
    AgentThread();
    ~AgentThread();
    void run();
    void stop();
    void setStartTrapFlag();
    void setStopTrapFlag();
    void setStopProcessFlag();
    bool getStopProcessFlag();
    
private:
    static AgentThread* _agentThread;
    void* agent_main(); 
    bool _startFlag;
    bool _stopFlag;
    bool _stopProcessFlag;
};
#endif
