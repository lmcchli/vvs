/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _CallbackQueueHandlerTest_h_
#define _CallbackQueueHandlerTest_h_

#include <cppunit/extensions/HelperMacros.h>

class CallbackQueue;

class CallbackQueueHandlerTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( CallbackQueueHandlerTest  );
    CPPUNIT_TEST( testInitialize );
    CPPUNIT_TEST_SUITE_END();

public:
    void setUp();
    void tearDown();

    void testInitialize();
    CallbackQueue* m_q1;
    CallbackQueue* m_q2;
};

#endif
