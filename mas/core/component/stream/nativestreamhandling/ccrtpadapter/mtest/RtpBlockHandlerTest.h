/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _RtpBlockHandlerTest_h_
#define _RtpBlockHandlerTest_h_

#include <cppunit/extensions/HelperMacros.h>

class RtpBlockHandlerTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( RtpBlockHandlerTest  );

    CPPUNIT_TEST( testInitialize );
    CPPUNIT_TEST( testPayloadHandling );
    CPPUNIT_TEST( testPacketHandling );
    CPPUNIT_TEST( testNativeAllocation );

    CPPUNIT_TEST_SUITE_END();

public:
    void setUp();
    void tearDown();

    void testInitialize();
    void testPayloadHandling();
    void testPacketHandling();
    void testNativeAllocation();
};

#endif
