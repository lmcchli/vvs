#ifndef AMRRTPBUILDER_H_
#define AMRRTPBUILDER_H_

#include "amrinfo.h"

#include "movreader.h"
#include "movrtppacketinfo.h"
#include "movrtppacket.h"

#include "rtpblockhandler.h"

#include "jniutil.h"

class AmrRtpPacketBuilder
{
	
private:
	 private:
    typedef struct
    {
        unsigned char toc;
        unsigned dataSize;
        unsigned char *data;
    } PayloadInfo;

public:

    AmrRtpPacketBuilder(JNIEnv* env, unsigned framesPerPacket,AmrInfo *movInfo, MovReader& reader);
	~AmrRtpPacketBuilder();

    void readAndAddFrame(RtpBlockHandler& blockHandler);
 
    void readAndCalculateSizes();

    unsigned readFrameType();


    void flushLastPacket(RtpBlockHandler& blockHandler);
	void AmrRtpPacketBuilderreadAndAddFrame(RtpBlockHandler& blockHandler);
    unsigned getNumberOfPackets();

    unsigned getNumberOfFrames();

    unsigned getPayloadSize();

protected:
    void deletePayloads();

    void addAndResetPacket(RtpBlockHandler& blockHandler);
    void resetPacket();

    unsigned m_framesPerPacket;
    MovReader& m_reader;
    unsigned m_framesInPacket;
    unsigned m_numberOfFrames;
    unsigned m_lastSentFrameNr;
    unsigned m_lastNumberOfFrames;
    unsigned m_numberOfPackets;
    unsigned m_payloadSize;
    char *m_payloadBuffer;
    unsigned m_addedSize;
	AmrInfo *m_movInfo;
    JNIEnv* m_env;
private:
	static const char* CLASSNAME;
    PayloadInfo *m_payloads;
	
};
#endif
