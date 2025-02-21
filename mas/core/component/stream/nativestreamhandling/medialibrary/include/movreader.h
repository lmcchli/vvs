/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef MOVREADER_H_
#define MOVREADER_H_

#include "mediaobjectreader.h"
#include "AtomReader.h"
#include "medialibraryexception.h"
#include "jni.h"

#include <stdlib.h>

namespace java {
class MediaObject;
};

/**
 * This class provide the I/O needed for reading MOV files.
 * 
 * MovReader wraps the MediaObjectReader and implements the AtomReader. 
 * 
 * 
 * TODO: This class should be redesigned to be the inverse of MovWriter.
 */
class MEDIALIB_CLASS_EXPORT MovReader: public MediaObjectReader,
        public quicktime::AtomReader
{
public:
    /**
     * Constructor that takes the MediaObject to read as parameter.
     * 
     * @param mediaObject The MediaObject to read
     * 
     * @throws MediaLibraryException If the data in the passed MediaObject 
     * is not of type MOV
     */
    MovReader(java::MediaObject *mediaObject) throw (MediaLibraryException);

    /**
     * Virtual Destructor
     */
    virtual ~MovReader();

    /**
     * Returns the length and id/name of the current atom.
     * Actually this method read two integers (2*4 octets)
     * assuming that the first integer is the length and that the
     * second is the atom id. All atoms have this as header so as
     * long as we are in sync the assumption is true.
     *
     * @param atomLength the returned length of the atom.
     * @param atomId the returned atom name/id.
     * @return the atom name/id as string (zero terminated) or
     *         NULL length or id could not be retrieved.
     */
    const char* getAtomInformation(unsigned& atomLength, unsigned& atomId);

    /**
     * Modifies the "file" position in the media.
     *
     * Given an offset and a mode the media cursor (file pointer)
     * is modified. When mode is
     * - AtomReader::SEEK_FORWARD the current position is moved forwards by
     *   offset steps.
     * - AtomReader::SEEK_BACKWARD the current position is moved backwars by
     *   offset steps.
     * - AtomReader::SEEK_SET_POSITION the current position is set to offset.
     */
    unsigned seek(unsigned offset, unsigned mode);

    /**
     * Returns the current "file" position in the media.
     */
    unsigned tell();

    /**
     * Reads a byte from the media.
     *
     * Output: byte the result
     * Returns: true if the value is read properly and false if not.
     */
    bool readByte(unsigned char& byte);

    /**
     * Reads an unsigned short (16 bytes) from the media.
     *
     * Output: uw the result
     * Returns: true if the value is read properly and false if not.
     */
    bool readW(unsigned short& uw);

    /**
     * Reads an unsigned int (32 bytes) from the media.
     *
     * Output: udw the result
     * Returns: true if the value is read properly and false if not.
     */
    bool readDW(unsigned& udw);

    /**
     * Sets the current "file" position in the media.
     * Returns the new postion.
     */
    unsigned seek(unsigned offset);

protected:

private:
    /**
     * Internal buffer for the atom name returned by getAtomInformation()
     * TODO: remove this when redesign is performed.
     */
    char m_buffer[5];
};
#endif /*MOVREADER_H_*/
