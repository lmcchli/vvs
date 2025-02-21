/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _InboundStreamTest_h_
#define _InboundStreamTest_h_

#include "MockCCRTPSession.h"
#include "MockNativeStreamHandling.h"
#include "MockStreamConfiguration.h"

#include <cppunit/extensions/HelperMacros.h>

class InboundStreamTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( InboundStreamTest  );
    CPPUNIT_TEST( testDelete );
    CPPUNIT_TEST_SUITE_END();
public:
    void setUp();
    void tearDown();

    void testDelete();

    MockCCRTPSession mockCCRTPSessionProxy;
    MockNativeStreamHandling mockNativeStreamHandling;
    MockStreamConfiguration mockStreamConfiguration;
};

#endif
