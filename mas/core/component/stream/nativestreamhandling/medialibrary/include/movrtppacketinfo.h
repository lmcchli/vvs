#ifndef MovRtpPacketInfo_h
#define MovRtpPacketInfo_h

class MovReader;

class MovRtpPacketInfo
{
public:
    unsigned transmissionTime;
    unsigned short rtpHeaderInfo;
    unsigned short rtpSequenceNumber;
    unsigned short flags;
    unsigned short entryCount;
    void loadInfo(MovReader* reader);
    static int size();
};

#endif
