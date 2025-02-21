#ifndef _CallbackQueueHandler_h_
#define _CallbackQueueHandler_h_

#include <vector>

class CallbackQueue;

class CallbackQueueHandler
{
private:
    CallbackQueueHandler();
    ~CallbackQueueHandler();

public:
    static CallbackQueueHandler& instance();
    unsigned addQueue(CallbackQueue* queue);
    CallbackQueue& getQueue(unsigned queueId);

public:
    // Test methods
    unsigned getQueueCount();
    static void clean();

private:
    static CallbackQueueHandler* s_callbackQueueHandler;
    std::vector<CallbackQueue*> m_queues;
};

#endif
