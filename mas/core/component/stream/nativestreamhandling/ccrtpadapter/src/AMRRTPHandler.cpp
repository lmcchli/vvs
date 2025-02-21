#include "AMRRTPHandler.h"
#include "jlogger.h"
#include "jniutil.h"
#include "audiomediadata.h"
#include "rtppayload.h"

#include "amrparser.h"
#include "amrbuilder.h"

const char* AMRRTPHandler::CLASSNAME = "masjni.ccrtpadapter.AMRRTPHandler";
const unsigned int AMRRTPHandler::SILENCE_CUTOFF_MARGIN = 900; //ms before considering silent.
const unsigned int AMRRTPHandler::SAMPLE_PERIOD_MS = 20; //each rtp frame time in ms.

AMRRTPHandler::AMRRTPHandler(JNIEnv* env, StreamRTPSession& audioSession,
        java::StreamContentInfo& contentInfo, java::RTPPayload& audioPayload) :
        RTPAudioHandler(env, audioSession, contentInfo, audioPayload), 
		mSilenceDetectionEnabled(false),
		mAmrInfo(new AmrInfo(env))
{
	JLogger::jniLogDebug(env, CLASSNAME,"~AMRRTPHandler - mAmrInfo create at %#x", mAmrInfo);
			
    base::String modeSet = audioPayload.getMediaFormatParameters();
    mStreamMode = SPEECH_MODE;
    mSilenceDetectorState = UNKNOWN_STATE;
    mLastSpeechTimeRef = 0;
    JLogger::jniLogTrace(env, CLASSNAME, "MediaFormatParameters: %s",
            modeSet.c_str());

    static const base::String cModeSet = "mode-set";

    base::String::size_type pos = modeSet.find(cModeSet);
    if (pos != base::String::npos) {
        pos += cModeSet.size();

        if ((pos = modeSet.find("=", pos)) != base::String::npos) {
            ++pos;
            base::String::size_type endPos = modeSet.find(";", pos);
            if (endPos != base::String::npos)
                modeSet = modeSet.substr(pos, endPos - pos);
            else
                modeSet = modeSet.substr(pos);

            pos = 0;
            endPos = pos;
            mConfiguredModeSet = 0;
            while (endPos != base::String::npos) {
                endPos = modeSet.find(",", pos);
                mConfiguredModeSet |= (1
                        << atoi(modeSet.substr(pos, endPos - pos).c_str()));

                if (endPos != base::String::npos)
                    pos = endPos + 1;
            }
        }
    } else {
        mConfiguredModeSet = 0xff;
    }

    JLogger::jniLogTrace(env, CLASSNAME, "mode-set value: %d",
            mConfiguredModeSet);
    mReceivedPacketNonRecording = false;
    mCyclesNonRecording = 0;
    JLogger::jniLogDebug(env, CLASSNAME, "AMRRTPHandler - create at %#x", this);
}

//*constuctor for superclass to set amrinfo.
AMRRTPHandler::AMRRTPHandler(JNIEnv* env, StreamRTPSession& audioSession,
        java::StreamContentInfo& contentInfo, java::RTPPayload& audioPayload,AmrInfo *amrinfo) :
        RTPAudioHandler(env, audioSession, contentInfo, audioPayload), 
		mSilenceDetectionEnabled(false),
		mAmrInfo(amrinfo)
{
	JLogger::jniLogDebug(env, CLASSNAME,"~AMRRTPHandler - mAmrInfo set at %#x", mAmrInfo);
    base::String modeSet = audioPayload.getMediaFormatParameters();
    mStreamMode = SPEECH_MODE;
    mSilenceDetectorState = UNKNOWN_STATE;
    mLastSpeechTimeRef = 0;
    JLogger::jniLogTrace(env, CLASSNAME, "MediaFormatParameters: %s",
            modeSet.c_str());

    static const base::String cModeSet = "mode-set";

    base::String::size_type pos = modeSet.find(cModeSet);
    if (pos != base::String::npos) {
        pos += cModeSet.size();

        if ((pos = modeSet.find("=", pos)) != base::String::npos) {
            ++pos;
            base::String::size_type endPos = modeSet.find(";", pos);
            if (endPos != base::String::npos)
                modeSet = modeSet.substr(pos, endPos - pos);
            else
                modeSet = modeSet.substr(pos);

            pos = 0;
            endPos = pos;
            mConfiguredModeSet = 0;
            while (endPos != base::String::npos) {
                endPos = modeSet.find(",", pos);
                mConfiguredModeSet |= (1
                        << atoi(modeSet.substr(pos, endPos - pos).c_str()));

                if (endPos != base::String::npos)
                    pos = endPos + 1;
            }
        }
    } else {
        mConfiguredModeSet = 0xff;
    }

    JLogger::jniLogTrace(env, CLASSNAME, "mode-set value: %d",
            mConfiguredModeSet);
    mReceivedPacketNonRecording = false;
    mCyclesNonRecording = 0;
    JLogger::jniLogDebug(env, CLASSNAME, "AMRRTPHandler - create at %#x", this);
}


void AMRRTPHandler::initializeRecording()
{
    RTPAudioHandler::initializeRecording();
    mRecordedModeSet = 0;
}

void AMRRTPHandler::onStopRecording()
{
	JNIEnv* env = JNIUtil::getJavaEnvironment();
	
    if (!mSilenceDetectionEnabled) {
        mStreamMode = SPEECH_MODE;
		JLogger::jniLogTrace(env, CLASSNAME, "onStopRecoding, silence Detection Disabled.");
    } else {
		JLogger::jniLogTrace(env, CLASSNAME, "onStopRecoding, silence Detection Enabled.");
        switch (mSilenceDetectorState)
        {
        case UNKNOWN_STATE:
        case SPEECH_STATE:
            mStreamMode = SPEECH_MODE;
            break;
        case INITIAL_SILENCE_STATE:
        case FINAL_SILENCE_STATE:
            mStreamMode = SILENCE_MODE;
            break;
        }
    }
    mSilenceDetectorState = UNKNOWN_STATE;
    mLastSpeechTimeRef = 0;
    mReceivedPacketNonRecording = false;
    mCyclesNonRecording = 0;
}

uint32 AMRRTPHandler::recordAudioPacketImpl(
        std::auto_ptr<const ost::AppDataUnit>& adu, uint32 extendedSeq,
        boost::ptr_list<AudioMediaData>::iterator pos,
        boost::ptr_list<AudioMediaData>& audioData)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
	
    //JLogger::jniLogTrace(env, "Handling AMR RTP packet ...");

    // must be at least a CRM entry
    if (adu->getSize() < 1) {
        JLogger::jniLogWarn(env, CLASSNAME,
                "RTP Packet is to small, probably corrupted. Ignoring...");
        return 0;
    }

    const uint8 *payload = adu->getData();
    const uint8 *payloadEnd = adu->getData() + adu->getSize();


    // CRM
    uint8 crm = *payload; // content header, Basically bits 0-3 are CMR or Codec Mode Request. (RFC 4867)
						  //equiv to Frame type index of the payload.
	//toc : table of contents rfc 4867 - 4.3.2.  The Payload Table of Contents
	const uint8 *tocStart = payload + 1;
    const uint8 *tocPtr = tocStart;

    bool moreFrames = true;

    unsigned short tempRecordedModeSet = 0;
    int totalFrameSize = 0;
	/*	 
    0 1 2 3 4 5
   +-+-+-+-+-+-+
   |F|  FT   |Q|
   +-+-+-+-+-+-+
    0 1111    1 00 0x7c or frame type 15 (F)
	0 1000    1 00 0x44 or sid 0x8
	

   F (1 bit): If set to 1, indicates that this frame is followed by
      another speech frame in this payload; if set to 0, indicates that
      this frame is the last frame in this payload.

   FT (4 bits): Frame type index, indicating either the AMR or AMR-WB
      speech coding mode or comfort noise (SID) mode of the
      corresponding frame carried in this payload.

   The value of FT is defined in Table 1a in [2] for AMR and in Table 1a
   in [4] for AMR-WB.  FT=14 (SPEECH_LOST, only available for AMR-WB)
   and FT=15 (NO_DATA) are used to indicate frames that are either lost
   or not being transmitted in this payload, respectively.

	Q (1 bit): Frame quality indicator.  If set to 0, indicates the
      corresponding frame is severely damaged, and the receiver should
      set the RX_TYPE (see [6]) to either SPEECH_BAD or SID_BAD
      depending on the frame type (FT).
   */
	  
    while (tocPtr < payloadEnd && moreFrames) {
        moreFrames = (*tocPtr & 0x80) != 0; // multi frame in bw efficient mode.
		unsigned frameType = (*tocPtr >> 3) & 0xf; //fetch the frametype 4 masked bits shifted to lsb
        tempRecordedModeSet |= (1 << frameType); //shift 1 into correct bit position to indicate frametype (bit field indicating all modes for toc)
        totalFrameSize += mAmrInfo->getFrameSize(frameType); //calculate the total framesize by adding all the octets needed for all frames, could include empty frames etc.
        ++tocPtr; //move to the next table of contents entry - pointer arithmetic.
    }

    int tocCount = tocPtr - tocStart;  

    if ((int) adu->getSize() != (1 + tocCount + totalFrameSize)) {
        JLogger::jniLogWarn(env, CLASSNAME,
                "RTP Packet size does not match packet content, probably corrupted. Ignoring...");
        return 0;
    }

    mRecordedModeSet |= tempRecordedModeSet;

    const uint8 *framePtr = tocPtr;

    uint32 tstamp = adu->getOriginalTimestamp();

    uint32 packetTime = 0;

    for (int frameNr = 0; frameNr < tocCount; ++frameNr) {
        // toc where bit indicating there is more frames is masked out.
        uint8 toc = *(tocStart + frameNr) & 0x7f; //one toc per frame from the start of the frame.

        unsigned frameType = (toc >> 3) & 0xf;

        if (mSilenceDetectionEnabled) {
            if (frameType == mAmrInfo->get_sid()) {
                if (pos == audioData.end()) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "SID Received in Recording");
                    onReceiveSIDFrame(true);
                } else {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "SID Received in Recording, ignorning because out of sequence");
                }
            } else {
                if ((pos == audioData.end()) && !adu->isMarked()) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "Speech Received in Recording frameType=%d",
                            frameType);

                    onReceiveSpeechFrame(true);
                    mLastSpeechTstamp = tstamp;
                    mLastSpeechExtendedSeq = extendedSeq;
                } else if (adu->isMarked()) {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "Marker Received in Recording, ignoning because marker frameType=%d",
                            frameType);

                } else {
                    JLogger::jniLogTrace(env, CLASSNAME,
                            "Speech Received in Recording, ignoning because out of sequence frameType=%d",
                            frameType);
                }
            }
        }

        int size = mAmrInfo->getFrameSize(frameType);

        uint8 * data = new uint8[2 + size];
        JLogger::jniLogDebug(env, CLASSNAME, "data - create at %#x - size %d",
                data, size);

        data[0] = crm;
        data[1] = toc;
        memcpy(data + 2, framePtr, size);

        audioData.insert(pos,
                new AudioMediaData(env, data, size + 2, tstamp, extendedSeq));

        tstamp += mAmrInfo->get_samplesPer20msFrame();
        framePtr += size;
        packetTime += SAMPLE_PERIOD_MS;
    }

    return packetTime;
}

void AMRRTPHandler::enhanceData(boost::ptr_list<AudioMediaData>& audioData)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
	JLogger::jniLogTrace(env, CLASSNAME, "enhanceData()");
    mLastSpeechFrame = -1;
    mTotalFrames = 0;
	int count=0;
    if (!audioData.empty()) {
        boost::ptr_list<AudioMediaData>::iterator packet = audioData.begin();
		JLogger::jniLogTrace(env, CLASSNAME, "enhanceData() audioData.size %d",audioData.size());
        bool inSilentPeriod = false;
        uint32 expectedTstamp = audioData.front().getRTPTimestamp();
        uint32 silenceStartTstamp(0);
        while (packet != audioData.end()) {
            uint32 tstamp = packet->getRTPTimestamp();
			
            if (inSilentPeriod) {
                JLogger::jniLogTrace(env, CLASSNAME,
                        "enhanceData() In silent period, inserting DTX NO_DATA");
                // insert DTX NO_DATA
                insertDTXNoData(MAX_DROPOUT * mMaxPTime / SAMPLE_PERIOD_MS,
                        silenceStartTstamp, tstamp, packet->getExtendedSeqNum(),
                        packet, audioData);
            } else if (tstamp != expectedTstamp) {
                JLogger::jniLogTrace(env, CLASSNAME,
                        "enhanceData() Missed packet, inserting DTX NO_DATA");

                // insert DTX NO_DATA
                insertDTXNoData(MAX_DROPOUT * mMaxPTime / SAMPLE_PERIOD_MS, expectedTstamp,
                        tstamp, packet->getExtendedSeqNum(), packet, audioData);
            }

            unsigned frameType = (packet->getAudioChunk().getData()[1] >> 3) & 0xf;
            if ((inSilentPeriod = (frameType == mAmrInfo->get_sid()))) {
                silenceStartTstamp = tstamp + mAmrInfo->get_samplesPer20msFrame();
                JLogger::jniLogTrace(env, CLASSNAME, "enhanceData() silenceStartTstamp: %d",
                        silenceStartTstamp);
                mTotalFrames++;
            } else if ((tstamp == mLastSpeechTstamp)
                    && (packet->getExtendedSeqNum() == mLastSpeechExtendedSeq)) {
                mLastSpeechFrame = mTotalFrames;
                mTotalFrames++;
            }

			JLogger::jniLogTrace(env, CLASSNAME,"enhanceData() frame Type: [%d] tstamp %d expectedTstamp %d frame %d",frameType,tstamp,expectedTstamp,++count);

            expectedTstamp = tstamp +  mAmrInfo->get_samplesPer20msFrame();
            ++packet;
        }
        if (mSilenceDetectionEnabled && ( (long)mLastSpeechFrame == -1)) {
            JLogger::jniLogTrace(env, CLASSNAME, "enhanceData() mLastSpeechFrame=-1");
            audioData.clear();
            return;
        }
    }
	JLogger::jniLogTrace(env, CLASSNAME, "enhanceData() finished.");
}

void AMRRTPHandler::trimAudio(boost::ptr_list<AudioMediaData>& mediaData,
        long audioSkipMs, bool onlySkipWholePackets)
{
    long packetsToSkip = audioSkipMs / SAMPLE_PERIOD_MS;
    if (audioSkipMs % SAMPLE_PERIOD_MS > 0)
        ++packetsToSkip;

    int i = 0;
    while (!mediaData.empty() && i < packetsToSkip) {
        mediaData.pop_front();
        ++i;
    }
}

void AMRRTPHandler::insertDTXNoData(uint32 maxFrames, uint32 tstampStart,
        uint32 currentTstamp, uint32 extendedSeq,
        boost::ptr_list<AudioMediaData>::iterator pos,
        boost::ptr_list<AudioMediaData>& audioData)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    static const uint8 CRM = 0xf0;
    static const uint8 DTX_NO_DATA_TOC = 0x7c;

    JLogger::jniLogTrace(env, CLASSNAME, "insertDTXNoData: %d - %d",
            tstampStart, currentTstamp);

    // modulo arithmetic on timestamp, assume max one wrap around
    uint32 noDataFrames = (currentTstamp - tstampStart) /  mAmrInfo->get_samplesPer20msFrame();
	JLogger::jniLogTrace(env, CLASSNAME, "insertDTXNoData: noDataFrames %d maxFrames %d",noDataFrames, maxFrames);

    // Only allow less than maxFrames packets to be inserted
    if (noDataFrames < maxFrames) {
        for (uint32 i = 0; i < noDataFrames; ++i) {
            uint8* dtx(new uint8[2]);
            dtx[0] = CRM;
            dtx[1] = DTX_NO_DATA_TOC;
            JLogger::jniLogDebug(env, CLASSNAME, "insertDTXNoData() dtx - create at %#x - size 2",
                    dtx);
					

            audioData.insert(pos,
                    new AudioMediaData(env, dtx, 2, tstampStart, extendedSeq));
            mTotalFrames++;
			JLogger::jniLogDebug(env, CLASSNAME, "insertDTXNoData() inserted dtx");

            tstampStart +=  mAmrInfo->get_samplesPer20msFrame();
        }
    } else {
        JLogger::jniLogWarn(env, CLASSNAME,
                "insertDTXNoData() Trying to insert %d NO_DATA frames, only %d is allowed!",
                noDataFrames, maxFrames);
    }
}

void AMRRTPHandler::removeSilence(boost::ptr_list<AudioMediaData>& mediaData)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (!mSilenceDetectionEnabled || (mTotalFrames && (long)mLastSpeechFrame == -1)) {
        return;
    }
    unsigned long totalSilenceFrames = mTotalFrames - mLastSpeechFrame;
    unsigned long marginSilenceFrames = SILENCE_CUTOFF_MARGIN / SAMPLE_PERIOD_MS;
    JLogger::jniLogTrace(env, CLASSNAME,
            "mLastSpeechFrame=%d,mTotalFrames=%d,marginSilenceFrames=%d",
            mLastSpeechFrame, mTotalFrames, marginSilenceFrames);
    if (totalSilenceFrames > marginSilenceFrames) {
        cutAudioFile(mediaData, totalSilenceFrames - marginSilenceFrames);
    }
}

void AMRRTPHandler::cutAudioFile(boost::ptr_list<AudioMediaData>& mediaData,
        unsigned long totalPacketsToCut)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();
    JLogger::jniLogTrace(env, CLASSNAME, "packetsToCut=%d", totalPacketsToCut);
    unsigned long packetsToCut = totalPacketsToCut;
    while ((packetsToCut > 0) && !mediaData.empty()) {
        mediaData.pop_back();
        --packetsToCut;
    }
}

void AMRRTPHandler::validateMediaProperties(MediaParser *mediaParser)
{
    JNIEnv* env = mediaParser->getMediaObjectJniEnv();
    JLogger::jniLogTrace(env, CLASSNAME, "ValidateMediaProperties");

    if (mediaParser->getAudioCodec() == "AMR") {
#ifdef WIN32
        AmrParser *p = (AmrParser*)mediaParser;
#else
        AmrParser *p = dynamic_cast<AmrParser*>(mediaParser);
        if (p == NULL) {
            throw ost::Exception("Wrong audio codec, expected AMR!");
        }
#endif

        /*
         unsigned short mode = p->getMediaInfo().getModeSet() & 0xffff;
         JLogger::jniLogTrace(env, CLASSNAME, "mode-set configured: %d; mode-set in media: %d" mConfiguredModeSet, mode);
         for (int i = 0; i < 7; ++i)
         {
         if ((((1 << i) & mode) != 0) && (((1 << i) & mConfiguredModeSet) == 0)) {
         throw ost::Exception("AMR bitrate in media not supported");
         }
         }
         */
    } else {
        throw ost::Exception("Wrong audio codec, expected AMR!");
    }
}

void AMRRTPHandler::defaultPacketHandler(
        std::auto_ptr<const ost::AppDataUnit>& adu)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    //JLogger::jniLogTrace(env, CLASSNAME, "Handling AMR RTP packet ...");

    if (!mSilenceDetectionEnabled) {
        // This method is only used when silence detection is enabled for AMR.
        return;
    }

    // must be at least a CRM entry
    if (adu->getSize() < 1) {
        JLogger::jniLogWarn(env, CLASSNAME,
                "RTP Packet is to small, probably corrupted. Ignoring...");
        return;
    }
    if (adu->getType() != mPayloadType) {
        JLogger::jniLogWarn(env, CLASSNAME,
                "Unexpected payload type: Expected: %d Actual: %d",
                (int) mPayloadType, (int) adu->getType());
        return;
    }

    uint32 extendedSeq;
    bool restart;
    if (!getExtendedSequenceNumber(adu, extendedSeq, restart)) {
        JLogger::jniLogTrace(env, CLASSNAME, "out of sequence packet ignore");
        return;
    }
    const uint8 *payload = adu->getData();
    const uint8 *payloadEnd = adu->getData() + adu->getSize();

    const uint8 *tocStart = payload + 1;
    const uint8 *tocPtr = tocStart;
    bool moreFrames = true;

    int totalFrameSize = 0;
    while (tocPtr < payloadEnd && moreFrames) {
        moreFrames = (*tocPtr & 0x80) != 0; // toc where bit indicating there is more frames is "anded" away
        int frameType = (*tocPtr >> 3) & 0xf; //shift and mask out frame type only.

        totalFrameSize += mAmrInfo->getFrameSize(frameType);
        ++tocPtr;
    }

    int tocCount = tocPtr - tocStart;

    //JLogger::jniLogTrace(env, CLASSNAME, "tocCount: %d - totalFrameSize: %d", tocCount, totalFrameSize);

    if ((int) adu->getSize() != (1 + tocCount + totalFrameSize)) {
        JLogger::jniLogWarn(env, CLASSNAME,
                "RTP Packet size does not match packet content, probably corrupted. Ignoring...");

        return;
    }

    const uint8 *framePtr = tocPtr;

    for (int frameNr = 0; frameNr < tocCount; ++frameNr) {
        // toc where bit indicating there is more frames is "anded" away
        uint8 toc = *(tocStart + frameNr) & 0x7f;

        unsigned frameType = (toc >> 3) & 0xf;
        //JLogger::jniLogTrace(env, CLASSNAME, "FT: %d", frameType);

        if (frameType == mAmrInfo->get_sid()) {
            JLogger::jniLogTrace(env, CLASSNAME,
                    "defaultPacketHandler SID frame extendedSeq=%d",
                    extendedSeq);
            onReceiveSIDFrame(false);
        } else {
            JLogger::jniLogTrace(env, CLASSNAME,
                    "defaultPacketHandler Speech frame extendedSeq=%d",
                    extendedSeq);

            onReceiveSpeechFrame(false);
        }

        unsigned size = mAmrInfo->getFrameSize(frameType);
        framePtr += size;

    }

    return;
}

void AMRRTPHandler::initializeBuilderProperties(MediaBuilder* mediaBuilder)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JLogger::jniLogTrace(env, CLASSNAME,
            "initializeBuilderProperties, recorded mode-set: %d",
            mRecordedModeSet);

#ifdef WIN32
    // TODO dangerous cast, use dynamic_cast if RTTI is enabled.
    AmrBuilder *builder = (AmrBuilder*)mediaBuilder;
#else
    AmrBuilder *builder = dynamic_cast<AmrBuilder*>(mediaBuilder);
    if (builder == 0) {
        throw ost::Exception("Wrong audio builder, expected AmrBuilder!");
    }
#endif
    builder->setModeSet(mRecordedModeSet);
}

void AMRRTPHandler::onTimerTick(uint64 timeref)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    if (!mSilenceDetectionEnabled) {
        // This method is not used when the silence detection mode is not enabled.
        return;
    }

    JLogger::jniLogTrace(env, CLASSNAME, "mLastSpeechTimeRef=%l",
            (long)mLastSpeechTimeRef);
    if (mLastSpeechTimeRef == 0) {
        mLastSpeechTimeRef = timeref;
    }
    switch (mSilenceDetectorState)
    {
    case UNKNOWN_STATE:
    case SPEECH_STATE:
        JLogger::jniLogTrace(env, CLASSNAME, "mSilenceDetectorState=%s",
                ((mSilenceDetectorState == UNKNOWN_STATE) ?
                        "UNKNOWN_STATE" : "SPEECH_STATE"));
        mLastSpeechTimeRef = timeref;
        break;
    case INITIAL_SILENCE_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "mSilenceDetectorState=INITIAL_SILENCE_STATE");
        if ((long)timeref - (long)mLastSpeechTimeRef > (long)mInitialSilenceThreshold) {
            JLogger::jniLogTrace(env, CLASSNAME, "Initial silence timeout");
            setSilenceDetected(true);
        }
        break;
    case FINAL_SILENCE_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "mSilenceDetectorState=FINAL_SILENCE_STATE");
        if ((long)timeref - (long)mLastSpeechTimeRef > (long)mFinalSilenceThreshold) {
            JLogger::jniLogTrace(env, CLASSNAME, "Final silence timeout");
            setSilenceDetected(true);
        }
        break;
    }
}

void AMRRTPHandler::onReceiveSpeechFrame(bool isRecording)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    mStreamMode = SPEECH_MODE;
    if (!isRecording) {
        return;
    }

    switch (mSilenceDetectorState)
    {
    case UNKNOWN_STATE:
        JLogger::jniLogTrace(env, CLASSNAME, "UNKNOWN_STATE --> SPEECH_STATE");
        mSilenceDetectorState = SPEECH_STATE;
        break;
    case INITIAL_SILENCE_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "INITIAL_SILENCE_STATE --> SPEECH_STATE");
        mSilenceDetectorState = SPEECH_STATE;
        mLastSpeechTimeRef = 0;
        break;
    case FINAL_SILENCE_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "FINAL_SILENCE_STATE --> SPEECH_STATE");
        mSilenceDetectorState = SPEECH_STATE;
        mLastSpeechTimeRef = 0;
        break;
    case SPEECH_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "SPEECH_STATE --> SPEECH_STATE");
        break;
    }
}

void AMRRTPHandler::onReceiveSIDFrame(bool isRecording)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    mStreamMode = SILENCE_MODE;
    if (!isRecording) {
        return;
    }

    switch (mSilenceDetectorState)
    {
    case UNKNOWN_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "UNKNOWN_STATE --> INITIAL_SILENCE_STATE");
        mSilenceDetectorState = INITIAL_SILENCE_STATE;
        mLastSpeechTimeRef = 0;
        break;
    case SPEECH_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "SPEECH_STATE --> FINAL_SILENCE_STATE");
        mSilenceDetectorState = FINAL_SILENCE_STATE;
        mLastSpeechTimeRef = 0;
        break;
     case INITIAL_SILENCE_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "INITIAL_SILENCE_STATE --> INITIAL_SILENCE_STATE");
        break;
     case FINAL_SILENCE_STATE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "FINAL_SILENCE_STATE --> FINAL_SILENCE_STATE");
        break;
    }

}

void AMRRTPHandler::initializeSilenceDetection(unsigned long packetdur,
        unsigned long mindur, base::String codec,
        unsigned long initialsilencedur, unsigned long finalsilencedur,
        int mode, int threshold, int initialSilenceFrames, int detectionFrames,
        int signalDeadband, int silenceDeadband, int debugLevel)
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    setSilenceDetected(false);
    mFinalSilenceThreshold = finalsilencedur;
    JLogger::jniLogTrace(env, CLASSNAME, "initialsilencedur=%ul",
            initialsilencedur);
    mInitialSilenceThreshold = initialsilencedur;

    mSilenceDetectionEnabled = (mode != 0);

    switch (mStreamMode)
    {
    case SILENCE_MODE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "SilenceDetection Initial State = INITIAL_SILENCE_STATE");
        mSilenceDetectorState = INITIAL_SILENCE_STATE;
        break;
    case SPEECH_MODE:
        JLogger::jniLogTrace(env, CLASSNAME,
                "SilenceDetection Initial State = UNKNOWN_STATE");
        mSilenceDetectorState = UNKNOWN_STATE;
        break;
    }
}

AMRRTPHandler::~AMRRTPHandler()
{
	if ( mAmrInfo !=0 ) {
		delete mAmrInfo;
		JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME,
            "~AMRRTPHandler - mAmrInfo delete at %#x", this);
		mAmrInfo=0;
	}
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME,
            "~AMRRTPHandler - delete at %#x", this);
}
