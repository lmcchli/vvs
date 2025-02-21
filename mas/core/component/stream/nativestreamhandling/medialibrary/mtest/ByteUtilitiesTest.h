/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef BYTEUTILITIESTEST_H_
#define BYTEUTILITIESTEST_H_

#include <cppunit/extensions/HelperMacros.h>

/**
 * CPPUnit tests for the ByteUtilities class.
 * 
 * @author Mats Egland
 */ 
class ByteUtilitiesTest : public CppUnit::TestFixture  {

    CPPUNIT_TEST_SUITE( ByteUtilitiesTest  );
    CPPUNIT_TEST( testSwapW );
    CPPUNIT_TEST( testSwapDW );
    CPPUNIT_TEST_SUITE_END();      

private:
    
    ByteUtilitiesTest( const ByteUtilitiesTest &x );
    ByteUtilitiesTest &operator=(const ByteUtilitiesTest &x );

public:
     
    ByteUtilitiesTest();
    virtual ~ByteUtilitiesTest();

    void setUp();
    void tearDown();
    
    /**
     * Tests the swapw method.
     * 
     * Test swap 1
     * Condition: 
     * Action: Swap 0x00f0
     * Result: Result should be 0xf000
     * 
     * Test swap 2
     * Condition
     * Action: Swap 0x00f0 two times
     * Result: Should be back to 0x00f0
     */ 
    void testSwapW();
    /**
     * Tests the swapdw method.
     * 
     * Test swap 1
     * Condition: 
     * Action: Swap 0xff000ff0L
     * Result: Result should be 0xf00f00ffL
     * 
     * Test swap 2
     * Condition
     * Action: Swap 0xff000ff0L two times
     * Result: Should be back to 0xff000ff0L
     */
    void testSwapDW();
    
    
};

#endif /*BYTEUTILITIESTEST_H_*/
