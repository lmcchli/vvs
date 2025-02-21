#include "CallbackQueueTest.h"

#include "CallbackQueue.h"
#include "Callback.h"

void 
CallbackQueueTest::setUp()
{
}

void 
CallbackQueueTest::tearDown()
{
}

void
CallbackQueueTest::testInitialize()
{
    CallbackQueue queue;
    Callback* callback;

    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getCounter());
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getSizeOfPushQueue());
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getSizeOfPopQueue());
    queue.swapQueues();
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getCounter());
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getSizeOfPushQueue());
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getSizeOfPopQueue());
    queue.push(new Callback(17, 1, 200));
    queue.push(new Callback(19, 1, 200));
    queue.push(new Callback(23, 1, 200));
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getCounter());
    CPPUNIT_ASSERT_EQUAL((unsigned)3, queue.getSizeOfPushQueue());
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getSizeOfPopQueue());
    queue.swapQueues();
    CPPUNIT_ASSERT_EQUAL((unsigned)3, queue.getCounter());
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getSizeOfPushQueue());
    CPPUNIT_ASSERT_EQUAL((unsigned)3, queue.getSizeOfPopQueue());
    queue.push(new Callback(42, 1, 200));
    queue.push(new Callback(4711, 1, 200));
    CPPUNIT_ASSERT_EQUAL((unsigned)3, queue.getCounter());
    CPPUNIT_ASSERT_EQUAL((unsigned)2, queue.getSizeOfPushQueue());
    CPPUNIT_ASSERT_EQUAL((unsigned)3, queue.getSizeOfPopQueue());
    CPPUNIT_ASSERT((callback = queue.pop()) != 0);
    CPPUNIT_ASSERT_EQUAL(17L, callback->requestId);
    CPPUNIT_ASSERT_EQUAL((unsigned)2, queue.getCounter());
    CPPUNIT_ASSERT_EQUAL((unsigned)2, queue.getSizeOfPushQueue());
    CPPUNIT_ASSERT_EQUAL((unsigned)2, queue.getSizeOfPopQueue());
    CPPUNIT_ASSERT((callback = queue.pop()) != 0);
    CPPUNIT_ASSERT_EQUAL(19L, callback->requestId);
    CPPUNIT_ASSERT((callback = queue.pop()) != 0);
    CPPUNIT_ASSERT_EQUAL(23L, callback->requestId);
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getCounter());
    CPPUNIT_ASSERT_EQUAL((unsigned)2, queue.getSizeOfPushQueue());
    CPPUNIT_ASSERT_EQUAL((unsigned)0, queue.getSizeOfPopQueue());
}

