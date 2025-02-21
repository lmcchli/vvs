#ifndef MovTrackInfo_h
#define MovTrackInfo_h

namespace quicktime {
class TrakAtom;
class HdlrAtom;
class StcoAtom;
class SttsAtom;
class StszAtom;
class StscAtom;
};

class MovTrackInfo
{
public:
    MovTrackInfo();
    ~MovTrackInfo();
    void initialize(quicktime::TrakAtom* trackAtom);
    bool check() const;
    unsigned getComponentSubType() const;
    int getChunkOffsetCount() const;
    int getChunkOffset(int index) const;
    unsigned getFrameTime(int index) const;
    unsigned getSamplesPerChunk(int index) const;

private:
    quicktime::TrakAtom* m_trackAtom;
    quicktime::HdlrAtom* m_handlerReferenceAtom;
    quicktime::StcoAtom* m_chunkOffsetAtom;
    quicktime::SttsAtom* m_timeToSampleAtom;
    quicktime::StszAtom* m_sampleSizeAtom;
    quicktime::StscAtom* m_sampleToChunkAtom;
};

#endif
