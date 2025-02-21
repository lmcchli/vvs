#include "MockNativeStreamHandling.h"

#include <nativestreamhandling.h>
#include "Callback.h"
#include "CallbackQueue.h"
#include "CallbackQueueHandler.h"

#include <jni.h>

/*
* This is the implementation of the mocked Java class CCRTPSession.
*/
MockNativeStreamHandling::MockNativeStreamHandling()
{
}

MockNativeStreamHandling::~MockNativeStreamHandling()
{
}

void MockNativeStreamHandling::initialize()
{
    Java_com_mobeon_masp_stream_jni_NativeStreamHandling_initialize(0, 0, 1, 2);
}

Callback* MockNativeStreamHandling::getCallback(int queueId)
{
    Callback* cb(CallbackQueueHandler::instance().getQueue((unsigned)queueId).pop());
    return cb;
}
