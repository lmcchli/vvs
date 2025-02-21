/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _OutputProcessorTest_h_
#define _OutputProcessorTest_h_

#include <cppunit/extensions/HelperMacros.h>

class OutputProcessorTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( OutputProcessorTest  );
    CPPUNIT_TEST( testInitialize );
    CPPUNIT_TEST_SUITE_END();

public:
    void setUp();
    void tearDown();

    void testInitialize();
};

#endif
