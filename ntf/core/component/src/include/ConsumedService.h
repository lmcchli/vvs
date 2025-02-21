/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#ifndef NTFCONSUMEDSERVICE_H
#define NTFCONSUMEDSERVICE_H

#include <string>

using namespace std;

struct ConsumedService {
    ConsumedService(int index);
    int _index;
    string _name;
    int _status;
    int _time;
    int _numSuccess;
    int _numFailures;
};    

#endif
