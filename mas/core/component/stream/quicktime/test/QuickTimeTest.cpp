/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "QuickTimeTest.h"

// Simple Leaf atoms
// These atoms does not contain other atoms or data vectors
#include <MvhdAtom.h>
#include <MdhdAtom.h>
#include <HdlrAtom.h>
#include <DrefAtom.h>
#include <ElstAtom.h>
#include <TkhdAtom.h>
#include <VmhdAtom.h>
#include <GmhdAtom.h>
#include <SmhdAtom.h>
#include <TrefAtom.h>

#include <SoundSampleDescription.h> // UlawAtom
#include <VideoSampleDescription.h> // H263Atom
#include <HintSampleDescription.h>  // RtpAtom

// Complex leaf atoms
// These atoms contains "dynamic" data stored in one or more
// vectors. Or, they are composed by other sub atoms.
#include <DinfAtom.h>
#include <EdtsAtom.h>
#include <StcoAtom.h>
#include <StscAtom.h>
#include <StsdAtom.h>
#include <StssAtom.h>
#include <StszAtom.h>
#include <SttsAtom.h>

// Composite atoms
// These atoms are composed of other atoms
#include <MdatAtom.h>
#include <MoovAtom.h>
#include <TrakAtom.h>
#include <MdiaAtom.h>
#include <MinfAtom.h>
#include <StblAtom.h>

// Track 
// Here are specializations of the Trak Atom.
#include <AudioTrackAtom.h>
#include <VideoTrackAtom.h>
#include <HintTrackAtom.h>


#include "MovFile.h"

//#include <logger.h>

//#include <mediaobjectwriter.h>

//#include "testutil.h"

#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>

#include <iostream>

/*
template <class T> void
nisse(T& t1, T& t2)
{
    t1.check(t2);
}
*/

using namespace std;
using namespace CppUnit;
using namespace quicktime;

QuickTimeTest::QuickTimeTest()//:
    //    mLogger(Logger::getLogger("medialibrary.QuickTimeTest"))
{
}

QuickTimeTest::~QuickTimeTest() 
{

}

void 
QuickTimeTest::setUp() 
{
}

void 
QuickTimeTest::tearDown() 
{
}

void
QuickTimeTest::testMvhdAtom()
{
    cout << "Testing atom class MvhdAtom ..." << endl;
    MvhdAtom outputAtom;
    MvhdAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testMdhdAtom()
{
    cout << "Testing atom class MdhdAtom ..." << endl;
    MdhdAtom outputAtom;
    MdhdAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testHdlrAtom()
{
    cout << "Testing atom class HdlrAtom ..." << endl;
    HdlrAtom outputAtom;
    HdlrAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testDrefAtom()
{
    cout << "Testing atom class DrefAtom ..." << endl;
    DrefAtom outputAtom;
    DrefAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testElstAtom()
{
    cout << "Testing atom class ElstAtom ..." << endl;
    ElstAtom outputAtom;
    ElstAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testTkhdAtom()
{
    cout << "Testing atom class TkhdAtom ..." << endl;
    TkhdAtom outputAtom;
    TkhdAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testVmhdAtom()
{
    cout << "Testing atom class VmhdAtom ..." << endl;
    VmhdAtom outputAtom;
    VmhdAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testGmhdAtom()
{
    cout << "Testing atom class GmhdAtom ..." << endl;
    GmhdAtom outputAtom;
    GmhdAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testSmhdAtom()
{
    cout << "Testing atom class SmhdAtom ..." << endl;
    SmhdAtom outputAtom;
    SmhdAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testTrefAtom()
{
    cout << "Testing atom class TrefAtom ..." << endl;
    TrefAtom outputAtom;
    TrefAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testSoundSampleDescription()
{
    cout << "Testing atom class SoundSampleDescription ..." << endl;
    PCMSoundSampleDescription outputAtom;
    PCMSoundSampleDescription inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testVideoSampleDescription()
{
    cout << "Testing atom class VideoSampleDescription ..." << endl;
    VideoSampleDescription outputAtom;
    VideoSampleDescription inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testHintSampleDescription()
{
    cout << "Testing atom class HintSampleDescription ..." << endl;
    HintSampleDescription outputAtom;
    HintSampleDescription inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testDinfAtom()
{
    cout << "Testing atom class DinfAtom ..." << endl;
    DinfAtom outputAtom;
    DinfAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testEdtsAtom()
{
    cout << "Testing atom class EdtsAtom ..." << endl;
    {
        EdtsAtom inputAtom;
        EdtsAtom outputAtom;
        testAtom(outputAtom, inputAtom);
    }
    EdtsAtom inputAtom;
    EdtsAtom outputAtom;
    outputAtom.getEditListAtom().initialize(1);
    outputAtom.getEditListAtom().getEditListEntries()[0].mediaTime = 4711;
    testAtom(outputAtom, inputAtom, false);
}

void
QuickTimeTest::testStcoAtom()
{
    cout << "Testing atom class StcoAtom ..." << endl;
    StcoAtom outputAtom;
    StcoAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testStscAtom()
{
    cout << "Testing atom class StscAtom ..." << endl;
    StscAtom outputAtom;
    StscAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testStsdAtom()
{
    cout << "Testing atom class StsdAtom ..." << endl;
    StsdAtom outputAtom;
    StsdAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testStssAtom()
{
    cout << "Testing atom class StssAtom ..." << endl;
    StssAtom outputAtom;
    StssAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testStszAtom()
{
    cout << "Testing atom class StszAtom ..." << endl;
    StszAtom outputAtom;
    StszAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testSttsAtom()
{
    cout << "Testing atom class SttsAtom ..." << endl;
    SttsAtom outputAtom;
    SttsAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testMdatAtom()
{
    cout << "Testing atom class MdatAtom ..." << endl;
    MdatAtom outputAtom;
    MdatAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testMoovAtom()
{
    cout << "Testing atom class MoovAtom ..." << endl;
    MoovAtom outputAtom;
    MoovAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testTrakAtom()
{
    cout << "Testing atom class TrakAtom ..." << endl;
    TrakAtom outputAtom;
    TrakAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testMdiaAtom()
{
    cout << "Testing atom class MdiaAtom ..." << endl;
    MdiaAtom outputAtom;
    MdiaAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testMinfAtom()
{
    cout << "Testing atom class MinfAtom ..." << endl;
    MinfAtom outputAtom;
    MinfAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testStblAtom()
{
    cout << "Testing atom class StblAtom ..." << endl;
    StblAtom outputAtom;
    StblAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testAudioTrackAtom()
{
    cout << "Testing atom class AudioTrackAtom ..." << endl;
    AudioTrackAtom outputAtom;
    AudioTrackAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testVideoTrackAtom()
{
    cout << "Testing atom class VideoTrackAtom ..." << endl;
    VideoTrackAtom outputAtom;
    VideoTrackAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}

void
QuickTimeTest::testHintTrackAtom()
{
    cout << "Testing atom class HintTrackAtom ..." << endl;
    HintTrackAtom outputAtom;
    HintTrackAtom inputAtom;
    testAtom(outputAtom, inputAtom);
}


template <class A> void 
QuickTimeTest::testAtom(A& outputAtom, A& inputAtom, bool equal)
{
    MovFile file("QuickTimeTest.mov");
    unsigned atomSize(outputAtom.getAtomSize());
    unsigned fileSize;
    unsigned atomId(outputAtom.getName());
    char atomName[5] = {
	(char)(atomId>>0x18)&0xff,
	(char)(atomId>>0x10)&0xff,
	(char)(atomId>>0x08)&0xff,
	(char)(atomId>>0x00)&0xff,
	0
    };
 
    if (equal) {
        // Assuming that two uninitialized objects are equal 
        CPPUNIT_ASSERT_MESSAGE("Should be equal", outputAtom == inputAtom);
    }

    cout << "  1) Saving [" << atomName << "] ..." << endl;
    // Open file as output and save atom
    file.open(MovFile::OPEN_AS_OUTPUT);
    outputAtom.saveGuts(file);
    atomSize = outputAtom.getAtomSize();

    cout << "  2) Reading [" << atomName << "] ..." << endl;
    // Re-open file as input and verify sizes
    file.open(MovFile::OPEN_AS_INPUT);
    fileSize = file.size();
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Different size!", atomSize, fileSize);

    // Read and test atom header
    file.readDW(atomSize);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong size!", 
				 outputAtom.getAtomSize(), 
				 atomSize);
    file.readDW(atomId);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Atom name!", outputAtom.getName(), atomId);

    // Restoring atom from file
    inputAtom.restoreGuts(file, atomSize);
    atomSize = inputAtom.getAtomSize();
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Different size!", atomSize, fileSize);
    file.close();

    // The atoms should still be equal
    CPPUNIT_ASSERT_MESSAGE("Atom mismatch", outputAtom == inputAtom);

    cout << "  Done." << endl;
}


