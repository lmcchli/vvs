/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <iostream>
#include <stdexcept>
#include <sstream>

#include "movreader.h"
#include "java/mediaobject.h"

#include "int.h"
#include "platform.h"
#include "byteutilities.h"

#include "jlogger.h"
#include "jniutil.h"

using namespace std;

static const char* CLASSNAME = "masjni.medialibrary.MovReader";

MovReader::MovReader(java::MediaObject *mediaObject) throw (MediaLibraryException) :
        MediaObjectReader(mediaObject, Platform::isLittleEndian())
{
    JLogger::jniLogDebug(mediaObject->getJniEnv(), CLASSNAME, "MovReader - create at %#x", this);
}

MovReader::~MovReader()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mediaObject->getJniEnv()), CLASSNAME,
            "~MovReader - delete at %#x", this);
}

const char*
MovReader::getAtomInformation(unsigned& atomLength, unsigned& atomId)
{
    if (readDW(atomLength) == 0)
        return 0;
    if (readDW(atomId) == 0)
        return 0;
    for (int i = 0; i < 4; i++)
        m_buffer[i] = (char) ((atomId >> ((3 - i) * 8)) & 0xff);
    m_buffer[4] = '\0';
    return m_buffer;
}

unsigned MovReader::seek(unsigned offset)
{
    return MediaObjectReader::seek(offset);
}

unsigned MovReader::seek(unsigned offset, unsigned mode)
{
    unsigned position(0);

    switch (mode)
    {
    case SEEK_FORWARD:
        position = MediaObjectReader::jumpForward(offset);
        break;

    case SEEK_BACKWARD:
        position = MediaObjectReader::jumpBackward(offset);
        break;

    case SEEK_SET_POSITION:
        position = MediaObjectReader::seek(offset);
        break;

    default:
        break;
    }

    return position;
}

unsigned MovReader::tell()
{
    return MediaObjectReader::tell();
}

bool MovReader::readByte(unsigned char& byte)
{
    if (MediaObjectReader::readInto((char*) &byte, 1) == 1) {
        // Due to the behaviour of the MediaObjectReader.
        jumpForward(1);
        return true;
    }
    return false;
}

bool MovReader::readW(unsigned short& word)
{
    return MediaObjectReader::readW(word) != 0;
}

bool MovReader::readDW(unsigned& doubleWord)
{
    return MediaObjectReader::readDW(doubleWord) != 0;
}
