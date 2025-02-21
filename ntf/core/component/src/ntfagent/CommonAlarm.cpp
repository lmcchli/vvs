/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */


#include "ntfdefs.h"
#include "CommonAlarm.h"

CommonAlarm::CommonAlarm(int index) :
    _index(index),
    _id(""),
    _status(D_alarmStatus_notAvailable) {
}
