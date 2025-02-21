#include "CallbackQueue.h"

#include "CallbackQueueHandler.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.ccrtpadapter.CallbackQueue";

CallbackQueue::CallbackQueue(JNIEnv* env) :
        m_queueSwapState(false)
{
    swapQueues();
    CallbackQueueHandler::instance().addQueue(this);

    JLogger::jniLogDebug(env, CLASSNAME, "CallbackQueue - create at %#x", this);
}

CallbackQueue::~CallbackQueue()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~CallbackQueue - delete at %#x", this);
}

void CallbackQueue::push(Callback* callback)
{
    m_pushQueue->push(callback);
}

Callback* CallbackQueue::pop()
{
    Callback* entry;
    // This will block if the queue is empty
    m_semaphore.wait();

    // Critical section starts
    {
        ost::MutexLock lock(m_mutex);
        entry = m_popQueue->front();
        m_popQueue->pop();
    } // Critical section end
      // Decrement atomic ...
    --m_counter;
    return entry;
}

bool CallbackQueue::isPopQueueEmpty()
{
    // The pop queue is considered to be empty when the
    // counter is zero.
    return m_counter == 0;
}

bool CallbackQueue::isPushQueueEmpty()
{
    return m_pushQueue->size() == 0;
}

void CallbackQueue::swapQueues()
{
    // Swapping the queues and setting the counter values
    // to the same value as the queued entry count.
    int pushIndex = m_queueSwapState ? 0 : 1;
    int popIndex = m_queueSwapState ? 1 : 0;

    // This lock is never contended, but ensures
    // Memory synchronization with reading thread
    {
        ost::MutexLock lock(m_mutex);

        m_queueSwapState = !m_queueSwapState;
        m_pushQueue = &(m_queues[pushIndex]);
        m_popQueue = &(m_queues[popIndex]);

        // First incrementing the atomic counter. Juste to ensure
        // a proper value before letting the popper loose.
        for (int counter(m_popQueue->size()); counter > 0; counter--) {
            ++m_counter;
        }
        // Incrementing the semaphore to a proper value
        for (int counter(m_popQueue->size()); counter > 0; counter--) {
            m_semaphore.post();
        }
    } // End of critical section
}

// Test methods
unsigned CallbackQueue::getSizeOfPopQueue()
{
    return m_popQueue->size();
}

unsigned CallbackQueue::getSizeOfPushQueue()
{
    return m_pushQueue->size();
}

unsigned CallbackQueue::getCounter()
{
    return m_counter;
}
