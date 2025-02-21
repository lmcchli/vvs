/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#ifndef NTFCONSUMEDSERVICEINSTANCE_H
#define NTFCONSUMEDSERVICEINSTANCE_H

#include <string>

using namespace std;

struct ConsumedServiceInstance {
    ConsumedServiceInstance(int serviceIndex, int instanceIndex);
    int _serviceIndex;
    int _instanceIndex;
    string _name;
    int _status;
    string _hostname;
    int _port;
    string _zone;
};

#endif
