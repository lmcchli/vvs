#ifndef MovParser_h
#define MovParser_h

#include "movinfo.h"       // Class member
#include "movrtppacket.h"  // MovRtpPacketContainer
#include "MoovAtom.h"

#include "mediaparser.h"   // Inherits
#include "platform.h"

#include "jni.h"
#include <base_include.h>

//using namespace quicktime;

// Forward declarations
class MovReader;
namespace java {
class MediaObject;
}
;

/**
 * A MOV file/MediaObject parser.
 * The MovParser parses the MOV data contained in a MediaObject.
 * The MovParser parses the QuickTime atoms and provides means
 * to retrieve audio data and video frame RTP packages.
 *
 * The MovParser delegates most of the parsing to MovInfo.
 */
class MovParser: public MediaParser
{
public:
    /**
     * The constructor taking a MediaObject containing MOV data
     */
    MovParser(java::MediaObject* mediaObject);

    /**
     * Destructor
     */
    ~MovParser();

    /**
     * Initializes the parser.
     * The purpose is to handle all the exeption throwing stuff here
     * instead of doing it in the constructor.
     */
    void init();

    /**
     * Parsing the MOV data.
     */
    bool parse();

    /**
     * Checks if the parse result is ok.
     *
     * Returns true if the result is ok and false otherwise.
     */
    bool check();

    /**
     * @return The parse result.
     */
    const MovInfo& getMediaInfo();

    /**
     * Retrieves a set of RTP packets for a specific video frame.
     * Given a frame index and an empty MovRtpPacketContainer
     * getFrame() uses the hint track information to extract the
     * corresponding RTP packates. The RTP packets are inserted into
     * rtpPackets.
     *
     * Input:
     *   frameIndex index of the frame to get RTP packets for.
     * Output:
     *   rtpPackets a reference to a container of MovRtpPackets.
     */
    void getFrame(boost::ptr_list<MovRtpPacket>& rtpPackets, int frameIndex);

    /* Documented in base class */
    int getFrameCount();

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

    /**
     * Returns the media duration in milli seconds.
     * If media contains multi media data/tracks the maximum of the
     * durations is returned.
     *
     * @return the media duration in milli seconds.
     */
    unsigned getDuration();

    /* Documented in base class */
    void setCursor(long cursor);

    /* Documented in base class */
    virtual long getCursor();

    /* Doc in baseclass. */
    unsigned getAudioStartTimeOffset();

    /* Doc in baseclass. */
    unsigned getVideoStartTimeOffset();

    /* Methods for determining the amount of required RTP memory */
    unsigned getAudioBlockSize();
    unsigned getAudioPacketCount();
    unsigned getVideoBlockSize();
    unsigned getVideoPacketCount();
    virtual const base::String getAudioCodec();
    void getData(RtpBlockHandler& blockHandler);

private:
	static const char* MOV_CLASSNAME;
    /**
     * Finds the closest intra frame to the cursor and adjusts
     * the cursor to the start time of that frame.
     * 
     * @return Index of the frame closest to cursor.
     */
    unsigned findStartVideoFrame();
    void calculateVideoRtp(unsigned& blockSize, unsigned& packetCount);
    void collectVideoRtp(RtpBlockHandler& blockHandler);

private:
    /**
     * The parse result.
     */
    quicktime::MoovAtom m_moovAtom;
    MovInfo m_movInfo;

    /**
     * MOV data reader.
     */
    MovReader* m_reader;

    /** The packet with index zero starts at this position. */
    size_t mCursor;

    /** 
     * If a cursor is set, this is the frame that should be returned
     * by getFrame when frameIndex is zero.
     */
    unsigned m_startVideoFrame;

    /** 
     * If a cursor is set, this is the chunk that should be returned
     * by getAudioChunk when chunkIndex is zero.
     */
    unsigned m_startAudioChunk;

    /** Converts from a time in milliseconds to an audio sample index. */
    int m_msToSampleIndexFactor;

    base::String m_audioCodec;
    unsigned m_videoBlockSize;
    unsigned m_videoPacketCount;
};
#endif
