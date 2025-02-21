#include "nativestreamhandling.h"

#include "Processor.h"
#include "CallbackQueueHandler.h"
#include "CallbackQueue.h"
#include "Callback.h"

#include "jni.h"

/*
 * This file contains the C++ implementation of the native Java
 * methods of the class NativeStreamHandling.
 */

/*
 * Class:     com_mobeon_masp_stream_jni_NativeStreamHandling
 * Method:    initialize
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_jni_NativeStreamHandling_initialize
(JNIEnv *, jclass, jint nOfOutputProcessors, jint nOfInputProcessors)
{
    // Initialize ProcessorGroup with number of processors
    int nOfOut(nOfOutputProcessors);
    int nOfIn(nOfInputProcessors);
    ProcessorGroup::instance().initialize(nOfOut, nOfIn);
}

/*
 * Class:     com_mobeon_masp_stream_jni_NativeStreamHandling
 * Method:    getNativeCallback
 * Signature: (I[J)V
 */
JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_jni_NativeStreamHandling_getNativeCallback
(JNIEnv *env, jclass, jint queueId, jlongArray jCallback)
{
    jlong jArray[2];
    long array[2] = {0L, 0L};
    Callback* cb(CallbackQueueHandler::instance().getQueue((unsigned)queueId).pop());

    cb->resetJniEnv(env);

    if (cb != 0) {
        cb->getAsLong(array);
        delete cb;
    }

    jArray[0] = array[0];
    jArray[1] = array[1];
    env->SetLongArrayRegion(jCallback, (jsize)0, (jsize)2, jArray);

}

/*
 * Class:     com_mobeon_masp_stream_jni_NativeStreamHandling
 * Method:    postDummyCallback
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_mobeon_masp_stream_jni_NativeStreamHandling_postDummyCallback
(JNIEnv* env, jclass cls, jint queueId, jint requestId)
{
    Callback* callback(new Callback(env, (unsigned)requestId, (unsigned)0xf, (unsigned)0x123, 4711L));

    CallbackQueueHandler::instance().getQueue((unsigned)queueId).push(callback);
}
