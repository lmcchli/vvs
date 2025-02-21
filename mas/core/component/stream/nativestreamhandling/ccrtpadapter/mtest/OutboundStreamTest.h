/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _OutboundStreamTest_h_
#define _OutboundStreamTest_h_

#include "MockCCRTPSession.h"
#include "MockNativeStreamHandling.h"
#include "MockStreamConfiguration.h"

#include <cppunit/extensions/HelperMacros.h>

class OutboundStreamTest : public CppUnit::TestFixture {
    CPPUNIT_TEST_SUITE( OutboundStreamTest  );


    CPPUNIT_TEST( testCreate );
    CPPUNIT_TEST( testPlayFinished );
    CPPUNIT_TEST( testPlayStop );
    CPPUNIT_TEST( testPlayCancel );
    CPPUNIT_TEST( testPlayDeleteWithCallback );
    CPPUNIT_TEST( testPlayDeleteWithoutCallback );
    CPPUNIT_TEST( testPlayFailed );
    CPPUNIT_TEST( testDelete );

    CPPUNIT_TEST_SUITE_END();
public:
    void setUp();
    void tearDown();

    void testCreate();
    void testPlayFinished();
    void testPlayStop();
    void testPlayCancel();
    void testPlayDeleteWithCallback();
    void testPlayDeleteWithoutCallback();
    void testPlayFailed();
    void testDelete();

    MockCCRTPSession mockCCRTPSessionProxy;
    MockNativeStreamHandling mockNativeStreamHandling;
    MockStreamConfiguration mockStreamConfiguration;
};

#endif
