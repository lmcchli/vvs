#ifndef _CallbackQueue_h_
#define _CallbackQueue_h_

#include <queue>
#include <cc++/thread.h>

#include "jni.h"
#include "Callback.h"

class CallbackQueue
{
public:
    CallbackQueue(JNIEnv* env);
    ~CallbackQueue();
    void push(Callback* callback);
    Callback* pop();
    bool isPopQueueEmpty();
    bool isPushQueueEmpty();
    void swapQueues();

public:
    // Test methods
    unsigned getSizeOfPopQueue();
    unsigned getSizeOfPushQueue();
    unsigned getCounter();

private:
    ost::Semaphore m_semaphore;
    ost::Mutex m_mutex;
    ost::AtomicCounter m_counter;
    bool m_queueSwapState;
    std::queue<Callback*> m_queues[2];
    std::queue<Callback*>* m_popQueue;
    std::queue<Callback*>* m_pushQueue;
};

#endif
