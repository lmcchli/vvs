/* Copyright (c) 2005 Mobeon AB
 * All rigths reserved
 */

#include "ConsumedServiceInstance.h"
#include <ntfdefs.h>

ConsumedServiceInstance::ConsumedServiceInstance(int serviceIndex, int instanceIndex) :
    _serviceIndex(serviceIndex),
    _instanceIndex(instanceIndex),
    _name(""),
    _status(D_ntfConsumedServiceInstancesStatus_down),
    _hostname(""),
    _port(0),
    _zone("Unspecified") {
}    
