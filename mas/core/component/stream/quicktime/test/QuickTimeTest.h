/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _QuickTimeTest_h_
#define _QuickTimeTest_h_

#include <cppunit/extensions/HelperMacros.h>

#include <memory>

namespace quicktime {
    class Atom;
};

/**
 * CPPUnit test of QuickTime file format atoms.
 *
 * The purpose of the test cases here in is to test the classes
 * that forms the MOV parser and builder. The test is more or less
 * the same for all the QuickTime classes: 
 * 1) create two instances and see that they are equal
 * 2) save one instance
 * 3) restore another instance
 * 4) verify that they still are equal.
 *
 */ 
class QuickTimeTest : public CppUnit::TestFixture {

    CPPUNIT_TEST_SUITE( QuickTimeTest  );
    CPPUNIT_TEST( testMvhdAtom );
    CPPUNIT_TEST( testMdhdAtom );
    CPPUNIT_TEST( testHdlrAtom );
    CPPUNIT_TEST( testDrefAtom );
    CPPUNIT_TEST( testElstAtom );
    CPPUNIT_TEST( testTkhdAtom );
    CPPUNIT_TEST( testVmhdAtom );
    CPPUNIT_TEST( testGmhdAtom );
    CPPUNIT_TEST( testSmhdAtom );
    CPPUNIT_TEST( testTrefAtom );

    CPPUNIT_TEST( testSoundSampleDescription );
    CPPUNIT_TEST( testVideoSampleDescription );
    CPPUNIT_TEST( testHintSampleDescription );

    CPPUNIT_TEST( testStcoAtom );
    CPPUNIT_TEST( testStscAtom );
    CPPUNIT_TEST( testStsdAtom );
    CPPUNIT_TEST( testStssAtom );
    CPPUNIT_TEST( testStszAtom );
    CPPUNIT_TEST( testSttsAtom );
    CPPUNIT_TEST( testDinfAtom );
    CPPUNIT_TEST( testEdtsAtom );

    CPPUNIT_TEST( testTrakAtom );
    CPPUNIT_TEST( testAudioTrackAtom );
    CPPUNIT_TEST( testVideoTrackAtom );
    CPPUNIT_TEST( testHintTrackAtom );

    CPPUNIT_TEST( testMdatAtom );
    CPPUNIT_TEST( testMoovAtom );
    CPPUNIT_TEST( testMdiaAtom );
    CPPUNIT_TEST( testMinfAtom );

    CPPUNIT_TEST_SUITE_END();      

public:
     
    QuickTimeTest();
    virtual ~QuickTimeTest();
  
    void setUp();
    void tearDown();
    
    /**
     * Template method for testing atom save and restore
     */ 
    template <class A> void testAtom(A& outputAtom, A& inputAtom, 
                                     bool equal=true);

    /**
     * Test metods for the different atoms.
     */
    void testMvhdAtom();
    void testMdhdAtom();
    void testHdlrAtom();
    void testDrefAtom();
    void testElstAtom();
    void testTkhdAtom();
    void testVmhdAtom();
    void testGmhdAtom();
    void testSmhdAtom();
    void testTrefAtom();

    void testSoundSampleDescription();
    void testVideoSampleDescription();
    void testHintSampleDescription();

    void testStblAtom();
    void testStcoAtom();
    void testStscAtom();
    void testStsdAtom();
    void testStssAtom();
    void testStszAtom();
    void testSttsAtom();
    void testDinfAtom();
    void testEdtsAtom();
    void testMdatAtom();
    void testTrakAtom();
    void testMdiaAtom();
    void testMinfAtom();
    void testMoovAtom();
    
    void testAudioTrackAtom();
    void testVideoTrackAtom();
    void testHintTrackAtom();

 private:
    QuickTimeTest( const QuickTimeTest &x );
    QuickTimeTest &operator=(const QuickTimeTest &x );

 private:
    /**
     * The logger used for this class.
     */ 
    //    std::auto_ptr<Logger> mLogger; 
};

#endif

