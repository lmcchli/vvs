/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include<cppunit/TestResult.h>
#include <MediaBufferTest.h>

#include <ostream>
#if defined(WIN32)
#include <windows.h>
#include <io.h>
#else
#include <unistd.h>
#endif
#include <algorithm>
#include <platform.h>

const char* MediaBufferTest::BUFFER_CONTENT = "Mediabuffer";
const size_t MediaBufferTest::BUFFER_SIZE = strlen(MediaBufferTest::BUFFER_CONTENT) +1; // +1 due to the null terminator


MediaBufferTest::MediaBufferTest() {
    
}

MediaBufferTest::~MediaBufferTest() {

}

void MediaBufferTest::setUp() {
	pMediaBuffer = new MediaBuffer(BUFFER_CONTENT, BUFFER_SIZE, Platform::isLittleEndian());   
    if (pMediaBuffer->getCurrentLocation() != pMediaBuffer->firstByte()) {
        CPPUNIT_FAIL("getCurrentLocation() and firstByte() mismatch after creation"); 
    }
    CPPUNIT_ASSERT_MESSAGE("The distance of lastByte() and firstByte() should be buffersize-1 after creation", 
            (pMediaBuffer->lastByte()-pMediaBuffer->firstByte()) == (BUFFER_SIZE -1));
}

void MediaBufferTest::tearDown() {
    delete pMediaBuffer;
}

void MediaBufferTest::testReadW() {
    uint16_t uw = 0;
    const char* pLoc;
    
    //std::cout << "NrofBytesleft: " << pMediaBuffer->bytesLeft() << std::endl;
     
    // Assert that returned position is equal to location in buffer
    pLoc = pMediaBuffer->readW(uw);
    CPPUNIT_ASSERT_MESSAGE("Location returned from readW in mismatch with buffer location",
        pLoc == pMediaBuffer->getCurrentLocation());
    CPPUNIT_ASSERT_MESSAGE("Bytes read should be 2 after first readW",
        pMediaBuffer->bytesRead() == 2);
    
    // Goto end and assert that the location is set to EOB
    pMediaBuffer->gotoLastByte();
    pMediaBuffer->jumpBackward(1);
    pLoc = pMediaBuffer->readW(uw);
    CPPUNIT_ASSERT_MESSAGE(
        "readW two byte from end of buffer should return EOB",
        pLoc == MediaBuffer::EOB);
    CPPUNIT_ASSERT_MESSAGE(
        "readW two byte from end of buffer should set location to EOB",
        pMediaBuffer->getCurrentLocation() == MediaBuffer::EOB);     
     
    pMediaBuffer->jumpForward(1);
    pLoc = pMediaBuffer->readW(uw);
    CPPUNIT_ASSERT_MESSAGE(
        "readW one byte from end of buffer should return EOB",
        pLoc == MediaBuffer::EOB);
    CPPUNIT_ASSERT_MESSAGE(
        "readW one byte from end of buffer should set location to EOB",
        pMediaBuffer->getCurrentLocation() == MediaBuffer::EOB);   
    
    // Three bytes left of buffer 
    pMediaBuffer->gotoLastByte();
    pMediaBuffer->jumpBackward(2);
    pLoc = pMediaBuffer->readW(uw);
    CPPUNIT_ASSERT_MESSAGE(
        "readW three bytes from end of buffer should be at last byte",
        pLoc == pMediaBuffer->lastByte());
    CPPUNIT_ASSERT_MESSAGE(
        "readW three bytes from end of buffer should set location to last byte",
        pMediaBuffer->getCurrentLocation() == pMediaBuffer->lastByte());   
    
    pMediaBuffer->gotoFirstByte();
    pMediaBuffer->readW(uw);
}
void MediaBufferTest::testReadDW() {
    uint32_t duw = 0;
    const char* pLoc;
    
    //std::cout << "NrofBytesleft: " << pMediaBuffer->bytesLeft() << std::endl;
     
    // Assert that returned position is equal to location in buffer
    pLoc = pMediaBuffer->readDW(duw);
    CPPUNIT_ASSERT_MESSAGE("Location returned from readDW in mismatch with buffer location",
        pLoc == pMediaBuffer->getCurrentLocation());
    CPPUNIT_ASSERT_MESSAGE("Bytes read should be 4 after first readDW",
        pMediaBuffer->bytesRead() == 4);
    
    // 4 bytes left
    pMediaBuffer->gotoLastByte();
    pMediaBuffer->jumpBackward(3);
    pLoc = pMediaBuffer->readDW(duw);
    CPPUNIT_ASSERT_MESSAGE(
        "readDW four bytes from end of buffer should return EOB",
        pLoc == MediaBuffer::EOB);
    CPPUNIT_ASSERT_MESSAGE(
        "readDW four bytes from end of buffer should set location to EOB",
        pMediaBuffer->getCurrentLocation() == MediaBuffer::EOB);     
    
    // 3 bytes left 
    pMediaBuffer->jumpForward(1);
    pLoc = pMediaBuffer->readDW(duw);
    CPPUNIT_ASSERT_MESSAGE(
        "readDW three bytes from end of buffer should return EOB",
        pLoc == MediaBuffer::EOB);
    CPPUNIT_ASSERT_MESSAGE(
        "readDW three bytes from end of buffer should set location to EOB",
        pMediaBuffer->getCurrentLocation() == MediaBuffer::EOB);   
    
    // 5 bytes left
    pMediaBuffer->gotoLastByte();
    pMediaBuffer->jumpBackward(4);
    pLoc = pMediaBuffer->readDW(duw);
    CPPUNIT_ASSERT_MESSAGE(
        "readDW 5 bytes from end of buffer should be at last byte",
        pLoc == pMediaBuffer->lastByte());
    CPPUNIT_ASSERT_MESSAGE(
        "readDW 5 bytes from end of buffer should set location to last byte",
        pMediaBuffer->getCurrentLocation() == pMediaBuffer->lastByte());   
    
    pMediaBuffer->gotoFirstByte();
    pMediaBuffer->readDW(duw);
}
void MediaBufferTest::testJump() {
     
    const char *currentLoc = pMediaBuffer->getCurrentLocation();
    size_t jumpedBytes;
    /*             gotoLastByte()      */
    currentLoc = pMediaBuffer->gotoLastByte();
    CPPUNIT_ASSERT_MESSAGE("gotoLastByte should set location to last byte",
        currentLoc == pMediaBuffer->lastByte() && pMediaBuffer->getCurrentLocation() == currentLoc);
    
    // gotoFirstByte()
    currentLoc = pMediaBuffer->gotoFirstByte();
    CPPUNIT_ASSERT_MESSAGE("gotoFirstByte should set location to first byte",
        currentLoc == pMediaBuffer->firstByte() && pMediaBuffer->getCurrentLocation() == currentLoc);
    CPPUNIT_ASSERT_MESSAGE("Character at first byte is not correct", 
        *currentLoc == BUFFER_CONTENT[0]);   
    /*            JumpForward tests         */
    jumpedBytes = pMediaBuffer->jumpForward(0);
    CPPUNIT_ASSERT_MESSAGE("jumpForward(0) should not affect location", 
        pMediaBuffer->getCurrentLocation() == pMediaBuffer->firstByte());
    CPPUNIT_ASSERT_MESSAGE("jumpForward(0) should return 0", 
        jumpedBytes == 0);
    
    // Jump 1
    jumpedBytes = pMediaBuffer->jumpForward(1);
    CPPUNIT_ASSERT_MESSAGE("jumpForward(1) should return 1", 
        jumpedBytes ==1);
    
    // Jump to EOB
    pMediaBuffer->gotoFirstByte();
    jumpedBytes = pMediaBuffer->jumpForward(BUFFER_SIZE);
    CPPUNIT_ASSERT_MESSAGE("jumpForward(BUFFER_SIZE) is one past boundary and should return BUFFER_SIZE",
        jumpedBytes == BUFFER_SIZE);
    CPPUNIT_ASSERT_MESSAGE("jumpForward(BUFFER_SIZE) at first byte should jump to EOB and return BUFFER_SIZE",
        pMediaBuffer->getCurrentLocation() == MediaBuffer::EOB); 
    // jump one and assert we are at lastBytes
    
    // go back to start and jump one less
    pMediaBuffer->gotoFirstByte();
    jumpedBytes = pMediaBuffer->jumpForward(BUFFER_SIZE-1); 
    CPPUNIT_ASSERT_MESSAGE("jumpForward(BUFFER_SIZE-1) should set location to last byte",
        pMediaBuffer->getCurrentLocation() == pMediaBuffer->lastByte());
    CPPUNIT_ASSERT_MESSAGE("jumpForward(BUFFER_SIZE-1) should return (BUFFER_SIZE-1)",
       jumpedBytes == (BUFFER_SIZE)-1);
    pMediaBuffer->gotoFirstByte(); 
   
    // Iterate to EOB 
    while (pMediaBuffer->getCurrentLocation() != MediaBuffer::EOB) {
        pMediaBuffer->jumpForward(1);
    }
    // should be at EOB
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB",
        pMediaBuffer->getCurrentLocation() == MediaBuffer::EOB); 
    pMediaBuffer->gotoFirstByte();
    
    
   /*            JumpBackward tests         */
    pMediaBuffer->gotoLastByte();
    jumpedBytes = pMediaBuffer->jumpBackward(0);
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(0) should not affect location", 
        pMediaBuffer->getCurrentLocation() == pMediaBuffer->lastByte());
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(0) should return 0", 
        jumpedBytes == 0);
    
    // GOTO EOB and jumpBackward 1
    pMediaBuffer->gotoEOB();
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
        MediaBuffer::EOB == pMediaBuffer->getCurrentLocation());
    jumpedBytes = pMediaBuffer->jumpBackward(1);
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(1) should return 1", 
        jumpedBytes == 1);
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(1) when at EOB should set location to last byte",
        pMediaBuffer->lastByte() == pMediaBuffer->getCurrentLocation());    
        
    // Jump 1
    jumpedBytes = pMediaBuffer->jumpBackward(1);
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(1) should return 1", 
        jumpedBytes == 1);
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(1) should return 1", 
        jumpedBytes == 1);    
    // Goto last byte and jump backward
    pMediaBuffer->gotoLastByte();
    jumpedBytes = pMediaBuffer->jumpBackward(BUFFER_SIZE);
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(BUFFER_SIZE) is one past boundary and should set location to first byte",
         pMediaBuffer->firstByte() == pMediaBuffer->getCurrentLocation()); 
    
    // GOTO EOB
    jumpedBytes = pMediaBuffer->jumpForward(BUFFER_SIZE);
    // should be at EOB
    CPPUNIT_ASSERT_MESSAGE("jumpForward(BUFFER_SIZE) is one past boundary and we should be at EOB",
        MediaBuffer::EOB == pMediaBuffer->getCurrentLocation()); 
    jumpedBytes = pMediaBuffer->jumpBackward(1);
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(1) when at EOB should set location to last byte",
        pMediaBuffer->lastByte() == pMediaBuffer->getCurrentLocation());
    
    // go back to end and jump one less
    pMediaBuffer->gotoLastByte();
    jumpedBytes = pMediaBuffer->jumpBackward(BUFFER_SIZE-1); 
    CPPUNIT_ASSERT_MESSAGE("jumpBackward(BUFFER_SIZE-1) should set location to first byte",
         pMediaBuffer->getCurrentLocation() == pMediaBuffer->firstByte());
    
    
    // GOTO EOB
    size_t count = 0;
    pMediaBuffer->jumpForward(BUFFER_SIZE); 
    // Iterate to first byte
    while (pMediaBuffer->getCurrentLocation() != pMediaBuffer->firstByte()) {
        pMediaBuffer->jumpBackward(1);
        count++;
    }
    // should be at first byte
    CPPUNIT_ASSERT_MESSAGE("a while(jumpBackward(1)) should take us to first byte",
        pMediaBuffer->getCurrentLocation() == pMediaBuffer->firstByte()); 
    CPPUNIT_ASSERT_MESSAGE("The number of jumps does not match size of buffer",
        count == BUFFER_SIZE);
    pMediaBuffer->gotoFirstByte();
    
    // GOTO EOB, jumpBackward more than buffersize and assert returnvalue
    pMediaBuffer->gotoFirstByte();
    jumpedBytes = pMediaBuffer->jumpForward(BUFFER_SIZE+1);    
    CPPUNIT_ASSERT_MESSAGE("Should be at EOB", 
        MediaBuffer::EOB == pMediaBuffer->getCurrentLocation());  
    CPPUNIT_ASSERT_MESSAGE("return value mismatch from jumpForward", 
        BUFFER_SIZE == jumpedBytes);
    jumpedBytes = pMediaBuffer->jumpBackward(BUFFER_SIZE+1);
    CPPUNIT_ASSERT_MESSAGE("return value mismatch from jumpBackward", 
        BUFFER_SIZE == jumpedBytes);  
        
    // Test with 1-length buffer
    delete pMediaBuffer;
    pMediaBuffer = new MediaBuffer(BUFFER_CONTENT, 1, false);
    if (pMediaBuffer->getCurrentLocation() != pMediaBuffer->firstByte()) {
        CPPUNIT_FAIL("getCurrentLocation() and firstByte() mismatch after creation"); 
    }
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte length MediaBuffer, firstByte and lastByte should be same",
         pMediaBuffer->firstByte() == pMediaBuffer->lastByte());
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer,bytesleft should be 1 after creation",
         pMediaBuffer->bytesLeft() == 1);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer,bytesRead should be 0 after creation",
         pMediaBuffer->bytesRead() == 0);      
    jumpedBytes = pMediaBuffer->jumpForward(1);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer,bytesleft should be 0 after jumpForward(1)" ,
         pMediaBuffer->bytesLeft() == 0);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer,bytesRead should be 1 after jumpForward(1)",
         pMediaBuffer->bytesRead() == 1);         
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, a jumpForward(1) should return 1",
         jumpedBytes == 1);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, the location should be at EOB after a jumpForward(1)",
         pMediaBuffer->getCurrentLocation() == MediaBuffer::EOB); 
    jumpedBytes = pMediaBuffer->jumpBackward(10);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, a jumpBackward(10) at EOB should return 1",
         jumpedBytes == 1);    
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, bytesLeft should be 1 at first byte",
         pMediaBuffer->bytesLeft() == 1);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, bytesRead should be 0 at first byte",
         pMediaBuffer->bytesRead() == 0);  
    jumpedBytes = pMediaBuffer->jumpForward(1);
    jumpedBytes = pMediaBuffer->jumpBackward(1);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, a jumpBackward(1) at EOB should return 1",
         jumpedBytes == 1);    
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, bytesLeft should be 1 at first byte",
         pMediaBuffer->bytesLeft() == 1);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, bytesRead should be 0 at first byte",
         pMediaBuffer->bytesRead() == 0);
    jumpedBytes = pMediaBuffer->jumpForward(pMediaBuffer->getBufferSize()+10);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer,bytesleft should be 0 after jumpForward(1)" ,
         pMediaBuffer->bytesLeft() == 0);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer,bytesRead should be 1 after jumpForward(1)",
         pMediaBuffer->bytesRead() == 1);         
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, a jumpForward(1) should return 1",
         jumpedBytes == 1);
    CPPUNIT_ASSERT_MESSAGE("In a 1-byte MediaBuffer, the location should be at EOB after a jumpForward(1)",
         pMediaBuffer->getCurrentLocation() == MediaBuffer::EOB);   
                  
}
