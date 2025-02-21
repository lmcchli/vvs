/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
#include <string.h>
#include "AtomWriter.h"

using namespace quicktime;

AtomWriter::AtomWriter():
    m_variant(QT)
{
}

void
AtomWriter::setQuicktimeVariant(int variant) {
    m_variant = variant;
}

unsigned AtomWriter::
writePascalString(char* str, int maxlen) {
    unsigned char len = strlen(str);
    if (maxlen > 255) {
	maxlen = 255;
    }
    if (len > maxlen) {
	len = maxlen;
    }
    writeByte(len);
    return write(str, len);
}

unsigned
AtomWriter::writeCString(char* str, int maxlen) {
    unsigned char len = strlen(str);
    if (len > maxlen) {
	len = maxlen;
    }
    write(str, len);
    writeByte(0);
    return len;
}

unsigned
AtomWriter::writeString(char* str, int maxlen) {
    switch (m_variant) {
    case MPEG4: return writeCString(str, maxlen);
    default: return writePascalString(str, maxlen);
    }
}

