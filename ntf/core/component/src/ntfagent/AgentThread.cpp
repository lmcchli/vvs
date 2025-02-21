/*
File:		AgentThread.cpp
Originated:	2004-04-15
Author:	Joakim Nilsson
Signature:	ermjnil


Copyright (c) 2004 MOBEON AB

The copyright to the computer program(s) herein is the property of 
MOBEON AB, Sweden. The programs may be used and/or copied only 
with the written permission from MOBEON AB or in accordance with 
the terms and conditions stipulated in the agreement/contract under 
which the programs have been supplied.
 */
//Subagent include files
#include <sr_snmp.h>
#include <comunity.h>
#include <v2clssc.h>
#include <sr_trans.h>
#include <context.h>
#include <method.h>
#include <diag.h>
#include <subagent.h>
#include <agentsoc.h>
#include <compat.h>
#include <signal.h>
#include "ntftrap.h"
#include "AgentThread.h"
#include "Logger.h"
#include "def.h"


// Global variables for this file only
static int stopRequested;
static char agContext[128];

AgentThread* AgentThread::_agentThread = 0;

SR_FILENAME 

IPCFunctionP IPCfp;  /* Emanate IPC functions pointer  */

AgentThread*
AgentThread::instance() {
    if( _agentThread == 0 ) {
        _agentThread = new AgentThread();
    }
    return _agentThread;
}


void
AgentThread::setStartTrapFlag() {
    _startFlag = true;
}

void 
AgentThread::setStopTrapFlag() {
    _stopFlag = true;
}

void
AgentThread::setStopProcessFlag() {
    _stopProcessFlag = true;
}

bool
AgentThread::getStopProcessFlag() {
    return _stopProcessFlag;
}

void
AgentThread::setContext(const string context) {
    strcpy(agContext, context.c_str());
}

AgentThread::AgentThread() {
    _stopFlag = false;
    _startFlag = false;
    _stopProcessFlag = false;
    agContext[0] = 0; //Initialize to empty c string
}

AgentThread::~AgentThread() {}

void
AgentThread::run() {

    SubagentEvent *ev;
    char empty[128] = "";
    bool isConnected = false;

    if (strlen(agContext) == 0)
        strcpy(agContext, "public");

#ifndef SR_UDS_IPC
    InitIPCArrayTCP(&IPCfp);
#else /* SR_UDS_IPC */
    InitIPCArrayUDS(&IPCfp);
#endif /* SR_UDS_IPC */


    if(InitSubagent() == -1) {
        DPRINTF((APERROR, "InitSubagent failed\n"));
        LOG(Logger::ERROR, "AgentThread", "InitSubagent() failed");
        exit(1);
    }

    // Subagent main loop
    while (!stopRequested) {
        if (PollSubagentEvent(500)) {
            ev = GetSubagentEvent();
            switch(ev->type) {
                case Ev_Connect:
                    DefaultAction(ev);
                    isConnected = true;
                    LOG(Logger::INFORMATION, "AgentThread", "Starting EMANATE thread with context '" + string(agContext) + "'.");
                    if(ev->direction == 0){
                        if(RegisterTextSnmpContext(agContext) == -1){
                            DPRINTF((APERROR, "RegisterTextSnmpContext failed.\n"));
                            LOG(Logger::ERROR, "AgentThread", "Register context '" + string(agContext) + "' failed.");
                            if(UnregisterTextSnmpContext(empty) == -1){
                                DPRINTF((APERROR, "UnregisterTextSnmpContext failed.\n"));
                            }
                        }
                    }
                    //FreeSubagentEvent(ev);
                    break;
                case Ev_Signal:
                    switch(ev->Event.Signal.sig) {
                        case SIGINT:
                            setStopTrapFlag();
                            setStopProcessFlag();
                            LOG(Logger::INFORMATION, "AgentThread", "Caught signal SIGINT");
                            break;
                        case SIGTERM:
                            setStopTrapFlag();
                            setStopProcessFlag();
                            LOG(Logger::INFORMATION, "AgentThread", "Caught signal SIGTERM");
                            break;
                        default:
                            LOG(Logger::INFORMATION, "AgentThread", "Caught undefined signal!");
                    }
                    FreeSubagentEvent(ev);
                    break;
                default:
                    DefaultAction(ev);
            }
        }

        if ( _startFlag && isConnected ) {
            send_ntfStarted_trap((VarBind*)NULL, (ContextInfo*)NULL);
            _startFlag = false;
            LOG(Logger::INFORMATION, "AgentThread", "Sending start trap.");
        }

        if ( _stopFlag ) {
            send_ntfStopped_trap((VarBind*)NULL, (ContextInfo*)NULL);
            _stopFlag = false;
            LOG(Logger::INFORMATION, "AgentThread", "Sending stop trap.");
        }

    }

    LOG(Logger::INFORMATION, "AgentThread", "Stopping EMANATE thread.");
    k_terminate();
    EndSubagent();
}

void 
AgentThread::stop() {
    requestStop();
    stopRequested = _stopRequested;
}
