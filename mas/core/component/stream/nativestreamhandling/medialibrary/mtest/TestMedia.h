#if !defined(TESTMEDIA_H_)
#define TESTMEDIA_H_

#include <wavinfo.h>
#include <MediaHandler.h>
#include <MockMediaObject.h>
#include <movparser.h>
enum { IGNORE_IN_VALIDATION = -1 };

namespace VideoCompressionCode {
	enum VideoCompressionCode {
		NONE = -1,
		UNKNOWN = 0,
		H263 = 1
	};
};

static struct MediaData {
	base::String path;
	base::String fileName;
	base::String contentType;
    base::String extension;
	int fileSize;
	int riffLength;
	CompressionCode::CompressionCode compressionCode;
	int numChannels;
	int sampleRate;
	int byteRate;
	int blockAlign;
	int bitsPerSample;
	int dataChunkSize;
	int audioStreamSize;
	int videoStreamSize;
	int audioBlockCount;
	int videoBlockCount;
	VideoCompressionCode::VideoCompressionCode videoCompressionCode;
	base::String getAudioCodec();
	base::String getVideoCodec();
	base::String getCanonicalFilename();
	void validate(const WavInfo& wavInfo);
	void validate(MediaHandler &mediaHandler);
	void validate(MovParser &movParser);

	MockMediaObject* createMock(int bufferSize); 
	MediaData as(base::String);

} MEDIA_GENERIC_PCMU = { // Generic PCMU media to use for results with .as("result_name") method
	".",						// Path
	"nonexistent_pcmu",			// File name
	"audio/wav",				// Content type
	"wav",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ULAW,		// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	1,							// Block alignment
	8,							// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
},MEDIA_GENERIC_PCMA = { // Generic PCMA media to use for results with .as("result_name") method
	".",						// Path
	"nonexistent_pcma",			// File name
	"audio/wav",				// Content type
	"wav",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ALAW,		// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	1,							// Block alignment
	8,							// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
},MEDIA_TEST_PCMA = { 
	".",						// Path
	"test_pcma",				// File name
	"audio/wav",				// Content type
	"wav",						// Extension
	38570,						// File size
	38562,						// Riff length
	CompressionCode::ALAW,						// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	1,							// Block alignment
	8,							// Bits per sample
	38512						// Size of data-chunk
},MEDIA_TEST_PCMU = { 
	".",						// Path
	"test_pcmu",				// File name
	"audio/wav",				// Content type
	"wav",						// Extension
	38570,						// File size
	38562,						// Riff length
	CompressionCode::ULAW,						// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	1,							// Block alignment
	8,							// Bits per sample
	38512						// Size of data-chunk
},MEDIA_BEEP_PCMA = { 
	".",						// Path
	"beep_pcma",				// File name
	"audio/wav",				// Content type
	"wav",						// Extension
	1420,						// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ALAW,						// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	1,							// Block alignment
	8,							// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
},MEDIA_BEEP_PCMU = { 
	".",						// Path
	"beep_pcmu",				// File name
	"audio/wav",				// Content type
	"wav",						// Extension
	1420,						// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ULAW,						// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	1,							// Block alignment
	8,							// Bits per sample
	IGNORE_IN_VALIDATION,		// Size of data-chunk
	1280,						// Audio stream size
	0,							// Video stream size
	8,							// Audio block count
	0,							// Video block count
	VideoCompressionCode::NONE	// Video compression code

},MEDIA_GILLTY_ADPCM = { 
	".",						// Path
	"gillty_adpcm",				// File name
	"audio/wav",				// Content type
	"wav",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ADPCM,		// Compression Code
	1,							// Number of channels
	11025,						// Sample rate
	5588,						// Byte rate
	256,						// Block alignment
	4,							// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
},MEDIA_ILLEGAL = { 
	".",						// Path
	"illegalfile",				// File name
	"audio/wav",				// Content type
	"wav",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::UNKNOWN,	// Compression Code
	IGNORE_IN_VALIDATION,		// Number of channels
	IGNORE_IN_VALIDATION,		// Sample rate
	IGNORE_IN_VALIDATION,		// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
},MEDIA_TEST_3GP = { 
	".",						// Path
	"test",						// File name
	"video/3gp",				// Content type
	"3gp",						// Extension
	622330,						// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::UNKNOWN,	// Compression Code
	IGNORE_IN_VALIDATION,		// Number of channels
	IGNORE_IN_VALIDATION,		// Sample rate
	IGNORE_IN_VALIDATION,		// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
},MEDIA_CORRUPT_3GP = { 
	".",						// Path
	"corrupt",					// File name
	"video/3gp",				// Content type
	"3gp",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::UNKNOWN,	// Compression Code
	IGNORE_IN_VALIDATION,		// Number of channels
	IGNORE_IN_VALIDATION,		// Sample rate
	IGNORE_IN_VALIDATION,		// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
},MEDIA_CORRUPT_MOV = { 
	".",						// Path
	"corrupt",					// File name
	"video/quicktime",				// Content type
	"mov",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::UNKNOWN,	// Compression Code
	IGNORE_IN_VALIDATION,		// Number of channels
	IGNORE_IN_VALIDATION,		// Sample rate
	IGNORE_IN_VALIDATION,		// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION		// Size of data-chunk
},MEDIA_TEST_PCMU_MOV = {
	".",						// Path
	"test_pcmu",				// File name
	"video/quicktime",			// Content type
	"mov",						// Extension
	62330,						// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ULAW,		// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION,		// Size of data-chunk
	IGNORE_IN_VALIDATION,		// Audio stream size
	IGNORE_IN_VALIDATION,		// Video stream size
	IGNORE_IN_VALIDATION,		// Audio block count
	IGNORE_IN_VALIDATION,		// Video block count
	VideoCompressionCode::H263	// Video compression code
},MEDIA_TEST_PCMA_MOV = {
	".",						// Path
	"test_pcma",				// File name
	"video/quicktime",				// Content type
	"mov",						// Extension
	62330,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ALAW,						// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION,		// Size of data-chunk
	IGNORE_IN_VALIDATION,		// Audio stream size
	IGNORE_IN_VALIDATION,		// Video stream size
	IGNORE_IN_VALIDATION,		// Audio block count
	IGNORE_IN_VALIDATION,		// Video block count
	VideoCompressionCode::H263	// Video compression code
},MEDIA_UM_0604_PCMU_MOV = {
	".",						// Path
	"UM_0604",					// File name
	"video/quicktime",			// Content type
	"mov",						// Extension
	IGNORE_IN_VALIDATION,		// File size
	IGNORE_IN_VALIDATION,		// Riff length
	CompressionCode::ULAW,						// Compression Code
	1,							// Number of channels
	8000,						// Sample rate
	8000,						// Byte rate
	IGNORE_IN_VALIDATION,		// Block alignment
	IGNORE_IN_VALIDATION,		// Bits per sample
	IGNORE_IN_VALIDATION,		// Size of data-chunk
	187200,						// Audio stream size
	100331,						// Video stream size
	1170,						// Audio block count
	200,						// Video block count
	VideoCompressionCode::H263						// Video compression code
};

#endif