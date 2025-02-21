/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef _MovWriter_h_
#define _MovWriter_h_

#include "AtomWriter.h"
#include "mediaobjectwriter.h"
#include "platform.h"

//namespace java { class MediaObject; };

class MEDIALIB_CLASS_EXPORT MovWriter: public MediaObjectWriter,
        public quicktime::AtomWriter
{
public:
    MovWriter(java::MediaObject *mediaObject);
    virtual ~MovWriter();
    bool open();
    bool close();
    unsigned write(const char* data, unsigned size);
    unsigned writeByte(unsigned char byte);
    unsigned writeW(unsigned short word);
    unsigned writeDW(unsigned word);
    unsigned tell();
    unsigned seek(unsigned offset, unsigned mode);

private:
    unsigned m_position;
};

#endif
