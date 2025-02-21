#include "CallbackQueueHandlerTest.h"

#include "CallbackQueueHandler.h"
#include "CallbackQueue.h"

void 
CallbackQueueHandlerTest::setUp()
{
    CallbackQueueHandler::clean();
}

void 
CallbackQueueHandlerTest::tearDown()
{
    CallbackQueueHandler::clean();
}

void
CallbackQueueHandlerTest::testInitialize()
{
    CallbackQueueHandler& handler(CallbackQueueHandler::instance());

    CPPUNIT_ASSERT_EQUAL(unsigned(0), handler.getQueueCount());
    m_q1 = new CallbackQueue;
    m_q2 = new CallbackQueue;
    CPPUNIT_ASSERT_EQUAL(unsigned(2), handler.getQueueCount());
    CPPUNIT_ASSERT_EQUAL(m_q1, &(handler.getQueue(0)));
    CPPUNIT_ASSERT_EQUAL(m_q2, &(handler.getQueue(1)));
    delete m_q1;
    delete m_q2;
}
