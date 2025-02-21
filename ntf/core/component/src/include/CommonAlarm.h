/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#ifndef NTFCOMMONALARM_H
#define NTFCOMMONALARM_H

#include <string>

using namespace std;

struct CommonAlarm {
    CommonAlarm(int index);
    int _index;
    string _id;
    int _status;
};    

#endif
