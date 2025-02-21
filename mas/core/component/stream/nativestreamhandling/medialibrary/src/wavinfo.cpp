/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "wavinfo.h"
 
void 
WavInfo::setRiffLength(uint32_t rLength) {
    mRiffLength = rLength;
}
uint32_t 
WavInfo::getRiffLength() const {
    return mRiffLength;
}

void 
WavInfo::setCompressionCode(CompressionCode::CompressionCode cc) {
    mCompressionCode = cc;
}
    
CompressionCode::CompressionCode
WavInfo::getCompressionCode() const {
    return mCompressionCode;
}
 
void 
WavInfo::setNumChannels(uint16_t nc) {
    mNumChannels = nc;
}

uint16_t 
WavInfo::getNumChannels() const {
    return mNumChannels;
}

void 
WavInfo::setByteRate(uint32_t br) {
    mByteRate = br;
}

uint32_t 
WavInfo::getByteRate() const {
    return mByteRate;
}

void 
WavInfo::setBlockAlign(uint16_t ba) {
    mBlockAlign = ba;
}
uint16_t 
WavInfo::getBlockAlign() const {
    return mBlockAlign;
}

uint16_t 
WavInfo::getBitsPerSample() const {
    return mBitsPerSample;
}
void 
WavInfo::setBitsPerSample(uint16_t bss) {
    mBitsPerSample = bss;
}
void 
WavInfo::setDataChunkSize(uint32_t dataChunkSize) {
    mDataChunkSize = dataChunkSize;
}
uint32_t
WavInfo::getDataChunkSize() const{
    return mDataChunkSize;
}
void 
WavInfo::setNumberOfSamples(uint32_t nrOfSamples) {
    mNumberOfSamples = nrOfSamples;
}
uint32_t 
WavInfo::getNumberOfSamples() {
    return mNumberOfSamples;
}

