/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "MovAudioChunkContainerTest.h"

#include <movaudiochunkcontainer.h>
#include <movaudiochunk.h>

#include<cppunit/TestResult.h>
#include<cppunit/Asserter.h>

using namespace std;
using namespace CppUnit;
//using namespace Asserter;

static char* duplicate(const char* original)
{
    int length(strlen(original)+1);
    char* copy(new char[length]);
    memcpy(copy, original, length);
    return copy;
}
                    

MovAudioChunkContainerTest::MovAudioChunkContainerTest():
    mLogger(Logger::getLogger("medialibrary.MovAudioChunkContainerTest"))
{
}

MovAudioChunkContainerTest::~MovAudioChunkContainerTest() 
{
}

void 
MovAudioChunkContainerTest::setUp() 
{
}

void 
MovAudioChunkContainerTest::tearDown() 
{
}

void
MovAudioChunkContainerTest::testRechunkalize()
{
    MovAudioChunkContainer audioChunksDefault;
    MovAudioChunkContainer audioChunksCustom(4, 0xab);

    const int nOfChunks = 5;
    const char* dataChunks[nOfChunks] = {
        "55555", "333", "4444", "333", "333"
    };
    int chunkSizes[nOfChunks] = {5, 3, 4, 3, 3};

    for (int index(0); index < nOfChunks; index++) {
        audioChunksDefault.push_back(new MovAudioChunk(duplicate(dataChunks[index]),
                                                       chunkSizes[index]));
        audioChunksCustom.push_back(new MovAudioChunk(duplicate(dataChunks[index]),
                                                       chunkSizes[index]));
    }
    
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Default hunk size!",
                                 160, audioChunksDefault.getRequestedChunkSize());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Custom hunk size!",
                                 4, audioChunksCustom.getRequestedChunkSize());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Default padding!",
                                 (unsigned char)0xff, audioChunksDefault.getPadding());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Custom padding!",
                                 (unsigned char)0xab, audioChunksCustom.getPadding());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Number of chunks should be equal",
                                 (unsigned)nOfChunks, audioChunksDefault.size());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Number of chunks should be equal",
                                 (unsigned)nOfChunks, audioChunksCustom.size());

    audioChunksDefault.rechunkalize();
    audioChunksCustom.rechunkalize();

    CPPUNIT_ASSERT_EQUAL_MESSAGE("Default number of rechunked!",
                                 (unsigned)1, audioChunksDefault.nOfRechunked());
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Custom number of rechunked!",
                                 (unsigned)5, audioChunksCustom.nOfRechunked());
}


void
MovAudioChunkContainerTest::testGetNextRechunked()
{
    return;
    const int nOfChunks = 5;
    const int defaultChunkSize = 160;
    const int customChunkSize = 4;
    MovAudioChunkContainer audioChunksDefault;
    MovAudioChunkContainer audioChunksCustom(customChunkSize, '0');

    // Here are the input chunks
    const char* dataChunks[nOfChunks] = {
        "55555", "333", "4444", "333", "333"
    };

    int chunkSizes[nOfChunks] = {5, 3, 4, 3, 3};

    // This is the expected result of the default chunkalizing
    const char* defaultDataChunk = "555553334444333333";
    int defaultChunkDataSize = 18;

    // This is the expected result of the custom chunkalizing
    const char* customDataChunk[nOfChunks] = {
        "5555", "5333", "4444", "3333", "3300"
    };

    // Creating the input chunks
    for (int index(0); index < nOfChunks; index++) {
        audioChunksDefault.push_back(new MovAudioChunk(duplicate(dataChunks[index]),
                                                       chunkSizes[index]));
        audioChunksCustom.push_back(new MovAudioChunk(duplicate(dataChunks[index]),
                                                       chunkSizes[index]));
    }
    

    // Perform chunkalizing
    audioChunksDefault.rechunkalize();
    audioChunksCustom.rechunkalize();

    // Verifying the result
    char defaultResultChunk[defaultChunkSize];
    int defaultResultSize = audioChunksDefault.getNextRechunked(defaultResultChunk);
    CPPUNIT_ASSERT_EQUAL_MESSAGE("Default chunk size (without pad)!",
                                 defaultChunkDataSize, defaultResultSize);
    for (int i(0); i < defaultChunkDataSize; i++) {
        char message[80];
        sprintf(message, "Index #%d (of %d) : testing char", i, defaultChunkSize);
        CPPUNIT_ASSERT_EQUAL_MESSAGE(message,
                                     defaultDataChunk[i], defaultResultChunk[i]);
    }

    
    char customResultChunk[customChunkSize];
    for (unsigned int chunkIndex(0); chunkIndex < audioChunksCustom.nOfRechunked(); chunkIndex++) {
        int customResultSize = audioChunksCustom.getNextRechunked(customResultChunk);
        int expectedSize(customChunkSize);
        if (chunkIndex == audioChunksCustom.nOfRechunked()-1) expectedSize = 2;
        CPPUNIT_ASSERT_EQUAL_MESSAGE("Custom chunk size (without pad)!",
                                     expectedSize, customResultSize);
        for (int i(0); i < customChunkSize; i++) {
            char message[128];
            sprintf(message, "Chunk #%d (of %d) at pos #%d (of %d) : testing char", 
                    chunkIndex, audioChunksCustom.nOfRechunked(),
                    i, customChunkSize);
            CPPUNIT_ASSERT_EQUAL_MESSAGE(message,
                                         (customDataChunk[chunkIndex])[i], customResultChunk[i]);
        }
    }
        
}

