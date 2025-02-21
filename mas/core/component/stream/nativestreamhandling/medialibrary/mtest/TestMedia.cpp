#include <TestMedia.h>
#include <wavinfo.h>
#include <cppunit/TestResult.h>
#include <cppunit/TestAssert.h>
using namespace CppUnit;

base::String MediaData::getAudioCodec() {
	switch(compressionCode) {
		case CompressionCode::ULAW:
			return "PCMU";
		case CompressionCode::ALAW:
			return "PCMA";
		case CompressionCode::PCM:
			return "PCM";
		case CompressionCode::ADPCM:
			return "ADPCM";
		default:
			return "";
	}
}

base::String MediaData::getVideoCodec() {
	switch(videoCompressionCode) {
		case VideoCompressionCode::H263:
			return "H263";
		default:
			return "";
	}
}

base::String MediaData::getCanonicalFilename() {
	return path + "/" + fileName + "." + extension;
}


void MediaData::validate(const WavInfo& wavInfo)
{
	if(riffLength != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong riffLength", (uint32_t)riffLength, wavInfo.getRiffLength());
	
	if(compressionCode != CompressionCode::UNKNOWN)
		CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong compression code", compressionCode, wavInfo.getCompressionCode());   

	if(numChannels != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong number of channels", (uint16_t)numChannels, wavInfo.getNumChannels());   

	if(sampleRate != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong sampleRate", (uint32_t)sampleRate, wavInfo.getAudioSampleRate());

	if(byteRate != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong ByteRate", (uint32_t)byteRate, wavInfo.getByteRate());

	if(blockAlign != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong Block Align", (uint16_t)blockAlign, wavInfo.getBlockAlign());   

	if(bitsPerSample != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong bits per sample", (uint16_t)bitsPerSample, wavInfo.getBitsPerSample());

	if(dataChunkSize != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL_MESSAGE("Wrong data chunk size", (uint32_t)dataChunkSize, wavInfo.getDataChunkSize());   
}

void MediaData::validate(MediaHandler &mediaHandler)
{ 
	if(compressionCode != CompressionCode::UNKNOWN)
		CPPUNIT_ASSERT_EQUAL(getAudioCodec(), mediaHandler.getAudioCodec());

	if(audioStreamSize != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL((unsigned)audioStreamSize, mediaHandler.getAudioBlockSize());

	if(audioBlockCount != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL((unsigned)audioBlockCount, mediaHandler.getAudioPacketCount());

	if(videoCompressionCode != VideoCompressionCode::UNKNOWN)
		CPPUNIT_ASSERT_EQUAL(getVideoCodec(), mediaHandler.getVideoCodec());

	if(videoStreamSize != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL((unsigned)videoStreamSize, mediaHandler.getVideoBlockSize());

	if(videoBlockCount != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL((unsigned)videoBlockCount, mediaHandler.getVideoPacketCount());
}


void MediaData::validate(MovParser &movParser) {
	if(compressionCode != CompressionCode::UNKNOWN)
		CPPUNIT_ASSERT_EQUAL(getAudioCodec(), movParser.getAudioCodec());

	if(audioStreamSize != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL((unsigned)audioStreamSize, movParser.getAudioBlockSize());

	if(audioBlockCount != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL((unsigned)audioBlockCount, movParser.getAudioPacketCount());

	if(videoCompressionCode != VideoCompressionCode::UNKNOWN)
		CPPUNIT_ASSERT_EQUAL(getVideoCodec(), movParser.getVideoCodec());

	if(videoStreamSize != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL((unsigned)videoStreamSize, movParser.getVideoBlockSize());

	if(videoBlockCount != IGNORE_IN_VALIDATION)
		CPPUNIT_ASSERT_EQUAL((unsigned)videoBlockCount, movParser.getVideoPacketCount());
}

MockMediaObject* MediaData::createMock(int bufferSize) {
	return new MockMediaObject(path,fileName,extension,contentType,bufferSize);
}

MediaData MediaData::as(base::String fileName) {
	MediaData clone = *this;
	clone.fileName = fileName;
	return clone;
}

