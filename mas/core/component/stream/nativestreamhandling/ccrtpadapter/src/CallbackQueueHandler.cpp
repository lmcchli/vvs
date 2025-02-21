#include "CallbackQueueHandler.h"

#include "CallbackQueue.h"

CallbackQueueHandler* CallbackQueueHandler::s_callbackQueueHandler = 0;

CallbackQueueHandler::CallbackQueueHandler()
{
}

CallbackQueueHandler::~CallbackQueueHandler()
{
    m_queues.clear();
}

CallbackQueueHandler& CallbackQueueHandler::instance()
{
    if (s_callbackQueueHandler == 0) {
        s_callbackQueueHandler = new CallbackQueueHandler;
    }
    return *s_callbackQueueHandler;
}

unsigned CallbackQueueHandler::addQueue(CallbackQueue* queue)
{
    unsigned id = m_queues.size();
    m_queues.push_back(queue);
    return id;
}

CallbackQueue& CallbackQueueHandler::getQueue(unsigned queueId)
{
    return *(m_queues[queueId]);
}

// Test methods
unsigned CallbackQueueHandler::getQueueCount()
{
    return m_queues.size();
}

void CallbackQueueHandler::clean()
{
    if (s_callbackQueueHandler != 0) {
        delete s_callbackQueueHandler;
        s_callbackQueueHandler = 0;
    }
}
