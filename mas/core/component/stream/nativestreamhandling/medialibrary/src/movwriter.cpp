/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include "movwriter.h"
#include "mediaobjectwriter.h"
#include "jlogger.h"
#include "jniutil.h"

#ifdef WIN32
#include <io.h>
#include <stdio.h>
#else
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#endif
#include <fcntl.h>

static const char* CLASSNAME = "masjni.medialibrary.MovWriter";

MovWriter::MovWriter(java::MediaObject* mo) :
        MediaObjectWriter(mo), m_position(0)
{
    JLogger::jniLogDebug(mo->getJniEnv(), CLASSNAME, "MovWriter - create at %#x", this);
}

MovWriter::~MovWriter()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~MovWriter - delete at %#x", this);
}

bool MovWriter::open()
{
    m_position = 0;
    return MediaObjectWriter::open();
}

bool MovWriter::close()
{
    return MediaObjectWriter::close();
}

unsigned MovWriter::write(const char* data, unsigned size)
{
    MediaObjectWriter::write(data, size);
    m_position += size;
    return size;
}

unsigned MovWriter::writeByte(unsigned char byte)
{
    return write((char*) &byte, 1);
}

unsigned MovWriter::writeW(unsigned short word)
{
    const unsigned size(2);
    uint8 buffer[size];

    /***
     * The BIG_ENDIAN define is a hangover from the FD02 codebase.
     * MOIP used the define "BIG_ENDIAN" which was find in Solaris
     * as it is not used anywhere else.  In Linix, it is used for
     * BSD sockets, so it ends up being defined even though we do not
     * want it defined./
     *
     * To avoid this name clash, I have renamed it to VM_BIG_ENDIAN
     * For now, in VM we do not want it defined!
     * lmcjhut (Jonathan Hutchinson)
     */

#ifdef VM_BIG_ENDIAN
    buffer[0] = (uint8)(word>>0)&0xff;
    buffer[1] = (uint8)(word>>8)&0xff;
#else
    buffer[0] = (uint8) (word >> 8) & 0xff;
    buffer[1] = (uint8) (word >> 0) & 0xff;
#endif

    MediaObjectWriter::write((const char*) buffer, size);
    m_position += size;
    return size;
}

unsigned MovWriter::writeDW(unsigned word)
{
    const unsigned size(4);
    uint8 buffer[size];

#ifdef VM_BIG_ENDIAN
    buffer[0] = (uint8)(word>>0)&0xff;
    buffer[1] = (uint8)(word>>8)&0xff;
    buffer[2] = (uint8)(word>>16)&0xff;
    buffer[3] = (uint8)(word>>24)&0xff;
#else
    buffer[0] = (uint8) (word >> 24) & 0xff;
    buffer[1] = (uint8) (word >> 16) & 0xff;
    buffer[2] = (uint8) (word >> 8) & 0xff;
    buffer[3] = (uint8) (word >> 0) & 0xff;
#endif

    MediaObjectWriter::write((const char*) buffer, size);
    m_position += size;
    return size;
}

unsigned MovWriter::tell()
{
    return m_position;
}

unsigned MovWriter::seek(unsigned offset, unsigned mode)
{
    char zero(0);

    switch (mode)
    {
    case AtomWriter::SEEK_FORWARD:
        for (unsigned i(0); i < offset; i++) {
            MediaObjectWriter::write((const char*) &zero, 1);
        }
        m_position += offset;
        return offset;
        break;

    default:
        break;
    }
    return (unsigned) -1;
}
