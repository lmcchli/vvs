#ifndef MovRtpPacket_h
#define MovRtpPacket_h

#include "movrtppacketinfo.h"
#include "platform.h"
#include <vector>

class MovRtpPacket;

/**
 * MovRtpPacket holds the RTP video packet information.
 */
class MEDIALIB_CLASS_EXPORT MovRtpPacket
{
    static const int MAX_VIDEO_RTP_PACKET_SPLIT;
    static const int MAX_EACH_VIDEO_RTP_PACKET;

public:
    /**
     * Assignment constructor.
     *
     * Creates a MovRtpPacket from a MovRtpPacketInfo.
     */
    MovRtpPacket(MovRtpPacketInfo& info);

    MovRtpPacket();
    /**
     * Destructor.
     */
    ~MovRtpPacket();

    /**
     * Returns the RTP payload (including payload header).
     */
    unsigned char* getData();

    /**
     * Sets the RTP payload (including payload header).
     */
    void setData(char* data);

    /**
     * Returns the data (payload) length
     */
    unsigned getLength();

    /**
     * Sets the data (payload) length
     */
    void setLength(unsigned length);

    /**
     * Returns the packet frame time.
     */
    unsigned getFrameTime();

    /**
     * Sets the packet frame time.
     */
    void setFrameTime(unsigned frameTime);

    /**
     * Returns the MOV RTP header info.
     */
    unsigned short getHeaderInfo();

    /**
     * Returns the MOV RTP sequence number.
     */
    unsigned short getSequenceNumber();

    /**
     * Returns the packet transmission time.
     */
    unsigned getTransmissionTime();

    /**
     * Equality operator.
     */
    bool operator==(const MovRtpPacket& leftSide);

    /**
     * Inequality operator.
     */
    bool operator!=(const MovRtpPacket& leftSide);

private:
    /**
     * The package information ...
     */
    MovRtpPacketInfo m_info;

    /**
     * Packet data, payload (including payload header).
     */
    unsigned char* m_data;

    //TODO: Remove this member, this is too stupid to describe in words ...
    bool m_ownsData;

    /**
     * Packet data length.
     */
    unsigned m_length;

    /**
     * The frame time of this packet.
     */
    unsigned m_frameTime;

};

#endif
