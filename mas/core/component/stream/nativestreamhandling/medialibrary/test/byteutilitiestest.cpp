/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <byteutilitiestest.h>

#include "byteutilities.h"
#include "int.h"

#include<cppunit/TestResult.h>
#include <ostream>
#include <unistd.h>
#include <algorithm>
#include <sstream>

using namespace std;
ByteUtilitiesTest::ByteUtilitiesTest() 
{
    
}

ByteUtilitiesTest::~ByteUtilitiesTest() 
{

}

void 
ByteUtilitiesTest::setUp() {
}

void 
ByteUtilitiesTest::tearDown() {
}

void
ByteUtilitiesTest::testSwapW() {
    char msg[100];
    const uint16_t uw = 0x00f0;
    uint16_t temp16 = uw;
    
    ByteUtilities::swapW(temp16);
    sprintf(msg, "Value 0x00f0 is not 0xf000 after swapW it is %X", temp16);

    CPPUNIT_ASSERT_MESSAGE((char*)msg,
        temp16 == 0xf000);
        
    ByteUtilities::swapW(temp16); 
    sprintf(msg, "Value 0x00f0 is not back to original value after two swaps it is %X", temp16);   
    CPPUNIT_ASSERT_MESSAGE((char*)msg,
        uw == temp16);
}

void
ByteUtilitiesTest::testSwapDW() {
    char msg[100];
    const uint32_t duw = 0xff000ff0L;
    uint32_t temp32 = duw;
    
    ByteUtilities::swapDW(temp32);
    sprintf(msg, "Value 0xff000ff0L is not 0xf00f00ffL after swapW it is %x", temp32);

    CPPUNIT_ASSERT_MESSAGE((char*)msg,
        temp32 == 0xf00f00ffL);
        
    ByteUtilities::swapDW(temp32); 
    sprintf(msg, "Value 0xff000ff0L is not back to original value after two swaps it is %x", temp32);   
    CPPUNIT_ASSERT_MESSAGE((char*)msg,
        duw == temp32);
}