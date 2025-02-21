#include <movrtppacketinfo.h>

#include <movreader.h>

void MovRtpPacketInfo::loadInfo(MovReader* reader)
{
    reader->readDW(transmissionTime);
    reader->readW(rtpHeaderInfo);
    reader->readW(rtpSequenceNumber);
    reader->readW(flags);
    reader->readW(entryCount);
}

int MovRtpPacketInfo::size()
{
    return sizeof(int) + 4 * sizeof(short);
}
