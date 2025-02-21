/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef COMFORTNOISEGENERATOR_H_
#define COMFORTNOISEGENERATOR_H_

#include <base_std.h>
#include <ccrtp/queuebase.h>
#include <boost/ptr_container/ptr_list.hpp>

#include "streamcontentinfo.h"

using namespace java;
class AudioMediaData;
class StreamRTPSession;

/**
 * Generates comfort noise for a given period of time.
 * 
 * @author Jorgen Terner
 */
class ComfortNoiseGenerator
{
private:
    ComfortNoiseGenerator(ComfortNoiseGenerator &rhs);
    ComfortNoiseGenerator& operator=(const ComfortNoiseGenerator& rhs);

    /** Session data is streamed on. */
    StreamRTPSession& mSession;

    /** Contains RTP payload type for CN. */
    StreamContentInfo& mContentInfo;

    /** Timestamp for the first audio packet in a silent period. */
    uint32 mStartTimestamp;

    struct timeval mLastSentSIDFrameTimestamp;

public:
    /**
     * Creates a new ComfortNoiseGenerator.
     */
    ComfortNoiseGenerator(StreamRTPSession& session, StreamContentInfo& contentInfo);

    /**
     * Destructor.
     */
    virtual ~ComfortNoiseGenerator();

    /**
     * Updates the the CNG with the first/new background noise characteristics.
     */
    void newCNPacket(std::auto_ptr<const ost::AppDataUnit>& adu);

    /**
     * Generates comfort noise, starting at the first silent audio packet 
     * timestamp and ending at the given timestamp, and adds it to the media 
     * data.
     * 
     * @param endTimestamp   End of period.
     * @param mediaData      The resulting data will be appended to this list.
     */
    void generateCN(uint32 endTimestamp, boost::ptr_list<AudioMediaData>& mediaData);

    /**
     * Adds silent packages to the media data.
     * 
     * @param mediaData        The resulting data will be appended to this 
     *                         list.
     * @param lengthMs         Silent period in milliseconds.
     * @param addToFront       If <code>true</code>, silence will be added to
     *                         the beginning of the media, if <code>false</code>,
     *                         silence will be added to the end.
     */
    void addSilence(boost::ptr_list<AudioMediaData>& mediaData, uint32 lengthMs, bool addToFront);

    /**
     * Sets the timestamp for the first silent audio packet.
     * 
     * @param firstTimestamp Timestamp for the first audio packet in a silent 
     *                       period.
     */
    void firstSilentPackage(uint32 firstTimestamp);

    /**
     * Sends a new SID Frame according to DTX algorithm.
     */
    virtual void sendSIDFrame();

private:
    /**
     * DTX algorithm.
     */
    virtual bool shouldSendNewSIDFrame();

    uint8* buildSIDFrame(size_t& size);
};

#endif /*COMFORTNOISEGENERATOR_H_*/
