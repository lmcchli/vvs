/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _CallbackQueueTest_h_
#define _CallbackQueueTest_h_

#include <cppunit/extensions/HelperMacros.h>

class CallbackQueueTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( CallbackQueueTest  );
    CPPUNIT_TEST( testInitialize );
    CPPUNIT_TEST_SUITE_END();

public:
    void setUp();
    void tearDown();

    void testInitialize();
};

#endif
