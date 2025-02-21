/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _NativeInterfaceTest_h_
#define _NativeInterfaceTest_h_

#include <cppunit/extensions/HelperMacros.h>

class NativeInterfaceTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( NativeInterfaceTest  );
    CPPUNIT_TEST( testInitialize );
    CPPUNIT_TEST_SUITE_END();

public:
    void setUp();
    void tearDown();

    void testInitialize();
};

#endif
