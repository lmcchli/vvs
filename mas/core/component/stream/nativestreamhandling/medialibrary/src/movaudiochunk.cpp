#include "movaudiochunk.h"

MovAudioChunk::MovAudioChunk(char* data, int length) :
        m_data(NULL), m_length(0)
{
    setData(data, length);
}
MovAudioChunk::~MovAudioChunk()
{
    if (m_data != NULL) {
        delete[] m_data;
        m_length = 0;
        m_data = NULL;
    }
}

void MovAudioChunk::setData(char* data, int length)
{
    if (m_data != NULL) {
        delete[] m_data;
    }

    m_data = data;
    m_length = length;
}

bool MovAudioChunk::operator==(const MovAudioChunk& leftSide)
{
    if (m_length != leftSide.m_length)
        return false;
    for (int i(0); i < m_length; i++) {
        if (m_data[i] != leftSide.m_data[i])
            return false;
    }
    return true;
}

bool MovAudioChunk::operator!=(const MovAudioChunk& leftSide)
{
    return !(*this == leftSide);
}

const char* MovAudioChunk::getData() const
{
    return m_data;
}

int MovAudioChunk::getLength() const
{
    return m_length;
}
