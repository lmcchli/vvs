/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */


#include "ntfdefs.h"
#include "ConsumedService.h"

ConsumedService::ConsumedService(int index) :
    _index(index),
    _name(""),
    _status(D_ntfConsumedServiceStatus_up),
    _time(0),
    _numSuccess(0),
    _numFailures(0) {
}
