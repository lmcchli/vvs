#ifndef RawParser_h
#define RawParser_h

#include "mediaparser.h"   // Inherits
#include "platform.h"

#include "jni.h"

#include <base_include.h>

// Forward declarations
class MediaObjectReader;

/**
 * A raw/PCM audio file/MediaObject parser.
 *
 * The raw audio parser more or less wraps the MediaObjectReader.
 * Since the raw audio the purpose here is to handle chopping data
 * into RTP packet sized chunks.
 */
class RawParser: public MediaParser
{
public:
    /**
     * The constructor taking a MediaObject containing raw data
     */
    RawParser(java::MediaObject* mediaObject, const base::String& codec = "PCMU");

    /**
     * Destructor
     */
    ~RawParser();

    /**
     * Initializes the parser.
     * The purpose is to handle all the exeption throwing stuff here
     * instead of doing it in the constructor.
     */
    void init();

    /**
     * Parsing the raw data.
     */
    bool parse();

    /**
     * @return The parse result.
     */
    //    const RawInfo& getMediaInfo();
    /**
     * Returns a pointer to an audio chunk.
     * Given a chunk index (chunkIndex) getAudioChunk() returns a pointer
     * to the start of that chunk.
     *
     * Input: 
     *   chunkIndex the index of the requested audio chunk.
     * Output:
     *   chunkSize the size of the returned chunk.
     * Returns:
     *   a pointer to the chunk (or NULL).
     */
    const unsigned char* getAudioChunk(unsigned& chunkSize, int chunkIndex);

    /* Documented in base class */
    int getAudioChunkCount();

    /* Documented in base class */
    void setCursor(long cursor);

    /* Documented in base class */
    long getCursor();

    /* Doc in baseclass. */
    unsigned getDuration();

    /* Methods for determining the amount of required RTP memory */
    unsigned getAudioBlockSize();
    unsigned getAudioPacketCount();
    unsigned getVideoBlockSize();
    unsigned getVideoPacketCount();

    void getData(RtpBlockHandler& blockHandler);
    virtual const base::String getAudioCodec();
private:
    void calculatePacketCount();

private:
	static const char* CLASSNAME;
    /**
     * Media object data reader.
     */
    std::auto_ptr<MediaObjectReader> m_reader;

    /** The packet with index zero starts at this position. */
    size_t m_cursor;

    /** The size (total) of the media data */
    unsigned m_dataChunkSize;

    /** A flag indicating if the parser is intialized or not. */
    bool m_isInitialized;

    /** The size (in octets) of an audio chunk (RTP packet). */
    unsigned m_packetSize;

    /** 
     * The number of packets the media data represents (calculated 
     * from the offset _cursor).
     */
    unsigned m_packetCount;

    base::String m_audioCodec;
};

#endif
