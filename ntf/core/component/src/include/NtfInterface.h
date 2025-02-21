/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#ifndef NTFINTERFACE_H
#define NTFINTERFACE_H

#include <string>
#include "IPMSThread.h"
#include "MIBAttribute.h"
#include "UnixSocket.h"
#include "def.h"


using namespace std;

class NtfInterface : public IPMSThread {
 public:
	
    /**
     * Destructor
     */
    ~NtfInterface();

    /**
     * Get the singleton NtfInterface instance.
     *@return the NtfInterface instance.
     */
    static NtfInterface* instance();

    /**
     * This function sends a Get request to host and port
     * Returns SUCCESSFUL if Get request is successfully sent and SYSTEM_ERROR
     * if transmition failed.
     */
    int sendGet();

    /**
     * This function sends a set request of MIB attribute mibattr to host and
     * port
     * Returns SUCCESSFUL if Set request is successfully sent and SYSTEM_ERROR
     * if transmition failed.
     */
    int sendSet(const MIBAttribute &mibattr);

    int setLogLevel(int logLevel);

    int setAdministrativeState(int state);

    int setLoadConfig(int value);

    /**
     * This function sends a Set event with a start request to the managed
     * objects. 
     * Returns SUCCESSFUL if Set event is successfully sent and SYSTEM_ERROR
     * if transmition failed.
     */
    int sendStart();

    /**
     * Thread run method.
     */
    void run();
    
    /**
     * Stops the NtfInterface thread.
     */
    void stop();
    
private:
    static NtfInterface* _pInstance;

    NtfInterface();

    // pointer to socket
    UnixSocket *_socket;

    // Parser for the received event data
    void handleData(string data);

    // Dispatcher for the type of event
    void dispatchEvent(string type, int index, int instanceIndex, AttributeVector &body);
};
#endif
