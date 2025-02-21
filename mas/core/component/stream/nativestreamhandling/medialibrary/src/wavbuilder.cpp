/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

#include <cstdio>
#include <iostream>

#include "wavbuilder.h"
#include "wavinfo.h"
#include "byteutilities.h"
#include "mediaobjectwriter.h"
#include "movaudiochunk.h"
#include "movaudiochunkcontainer.h"
#include "jlogger.h"
#include "jniutil.h"

#include <cc++/exception.h>

using namespace std;

static const char* CLASSNAME = "masjni.medialibrary.WavBuilder";

static const char* RIFF = "RIFF";
static const char* WAVE = "WAVE";
static const char* FMT_CHUNK = "fmt ";
static const char* DATA_CHUNK = "data";
static const int FORMAT_CHUNK_SIZE = 18;
static const int WAV_HEADER_SIZE = 12 + 26 + 8;

WavBuilder::WavBuilder(JNIEnv* env) :
        mAudioChunks(0), mDuration(0), mInfo()
{
    JLogger::jniLogDebug(env, CLASSNAME, "WavBuilder - create at %#x", this);
}

WavBuilder::~WavBuilder()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "WavBuilder - delete at %#x", this);
}

void WavBuilder::setAudioCodec(const base::String& codecName)
{
    if (!(codecName == "PCMU" || codecName == "PCMA" || codecName == "pcmu" || codecName == "pcma")) {
        JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "Bad audio codec name [%s]", codecName.c_str());
        throw ost::Exception("Bad audio codec name [" + codecName + "]");
    }

    if (codecName == "PCMU" || codecName == "pcmu") {
        mAudioCompression = 7;
    }

    if (codecName == "PCMA" || codecName == "pcma") {
        mAudioCompression = 6;
    }
}

void WavBuilder::setAudioChunks(const MovAudioChunkContainer& audioChunks)
{
    mAudioChunks = &audioChunks;
}

bool WavBuilder::store(MediaObjectWriter& writer)
{
    if (mAudioChunks == 0) {
        return false;
    }

    // Calculate Audio duration
    unsigned nOfAudioChunks(0);
    unsigned mediaSize(0);

    MovAudioChunkContainer::const_iterator iter = mAudioChunks->begin();
    for (; iter != mAudioChunks->end(); iter++) {
        // Audio duration is measured in number of samples.
        mediaSize += iter->getLength();
        nOfAudioChunks++;
    }
    mDuration = mediaSize / (mInfo.getAudioSampleRate() / 1000);

    // write Wav-header
    uint8 headerData[WAV_HEADER_SIZE];
    unsigned headerSize(createHeader(mediaSize, headerData));
    writer.write((const char*) headerData, headerSize);

    // write audio data
    for (iter = mAudioChunks->begin(); iter != mAudioChunks->end(); ++iter) {
        writer.write(iter->getData(), iter->getLength());
    }

    return true;
}

unsigned WavBuilder::createHeader(unsigned totalMediaSize, uint8* header)
{

    uint32_t formatChunkSize(FORMAT_CHUNK_SIZE);
    uint32_t totalSize(totalMediaSize + WAV_HEADER_SIZE - 8);
    uint16_t compressionCode(mAudioCompression);
    uint16_t numOfChannels(mInfo.getNumChannels());
    uint32_t sampleRate(mInfo.getAudioSampleRate());
    uint32_t byteRate(mInfo.getByteRate());
    uint16_t blockAlign(mInfo.getBlockAlign());
    uint16_t bitsPerSample(mInfo.getBitsPerSample());
    uint16_t extraFormatBytes(0);

    memcpy(header, RIFF, 4);
    writeDW(header + 4, totalSize);
    memcpy(header + 8, WAVE, 4);
    memcpy(header + 12, FMT_CHUNK, 4);
    writeDW(header + 16, formatChunkSize);
    writeW(header + 20, compressionCode);
    writeW(header + 22, numOfChannels);
    writeDW(header + 24, sampleRate);
    writeDW(header + 28, byteRate);
    writeW(header + 32, blockAlign);
    writeW(header + 34, bitsPerSample);
    writeW(header + 36, extraFormatBytes);
    memcpy(header + 38, DATA_CHUNK, 4);
    writeDW(header + 42, totalMediaSize);

    return WAV_HEADER_SIZE;
}

void WavBuilder::writeDW(uint8 *ptr, uint32_t &udw)
{
    uint8 tmp(udw & 0x000000ff);
    memcpy(ptr, &tmp, 1);
    tmp = (udw & 0x0000ff00) >> 8;
    memcpy(ptr + 1, &tmp, 1);
    tmp = (udw & 0x00ff0000) >> 16;
    memcpy(ptr + 2, &tmp, 1);
    tmp = (udw & 0xff000000) >> 24;
    memcpy(ptr + 3, &tmp, 1);
}
void WavBuilder::writeW(uint8 *ptr, uint16_t &uw)
{
    uint8 tmp(uw & 0x000000ff);
    memcpy(ptr, &tmp, 1);
    tmp = (uw & 0x0000ff00) >> 8;
    memcpy(ptr + 1, &tmp, 1);
}
