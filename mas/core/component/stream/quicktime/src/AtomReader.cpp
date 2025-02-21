/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */

#include "AtomReader.h"

using namespace quicktime;

AtomReader::AtomReader():
    m_variant(QT)
{
}

bool
AtomReader::read(unsigned char* buf, int len) {
    bool result = true;
    for (int i = 0; result && i < len ; i++) {
	result = readByte(buf[i]);
    }
    return result;
}

void
AtomReader::setQuicktimeVariant(int variant) {
    m_variant = variant;
}

char* AtomReader::
readPascalString(int maxlen, bool skipTail) {
    unsigned char len;
    readByte(len);
    unsigned readlen;
    if (len > maxlen) {
        readlen = maxlen;
    } else {
        readlen = len;
    }
    char* result = new char[readlen + 1];
    read((unsigned char*) result, readlen);
    result[readlen] = 0;
    if (skipTail) {
        while (readlen < len) {
            unsigned char c;
            readByte(c);
            ++readlen;
        }
    }
    return result;
}

char*
AtomReader::readCString(int maxlen) {
    char* buf = new char[maxlen + 1];
    int i = -1;
    do {
	i++;
	if (i == maxlen) {
	    buf[i] = 0;
	} else {
	    if (!readByte(((unsigned char*)buf)[i])) {
		buf[i] = 0;
	    }
	}
    } while (buf[i]);	
    return buf;
}

char*
AtomReader::readString(int maxlen, bool skipTail) {
    switch (m_variant) {
    case MPEG4: 
        return readCString(maxlen);
    default: 
        return readPascalString(maxlen, skipTail);
    }
}


//Utility function to get the name of an atom as a String 
void AtomReader::getAtomNameAsString(AtomName name, std::string &stringName) {
	if (name == 0) return;
	
	stringName+=(char)name>>24;
	stringName+=(char)name>>16;
	stringName+=(char)name>>8;
	stringName+=(char)name>>0;
}

