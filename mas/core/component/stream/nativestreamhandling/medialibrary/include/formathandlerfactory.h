/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef FORMATHANDLERFACTORY_H_
#define FORMATHANDLERFACTORY_H_

#include "platform.h"
#include "mediabuilder.h"
#include "jni.h"

#include <base_include.h>
#include <base_std.h>

namespace java {
class MediaObject;
};

class MediaParser;
class MediaObjectWriter;

/**
 * Factory for MediaParser instances.
 * 
 * @author Jorgen Terner
 */
class MEDIALIB_CLASS_EXPORT FormatHandlerFactory
{
public:
    enum FileFormat
    {
        UNDEFINED, WAV_FILE, MOV_FILE, AMR_FILE, AMRWB_FILE, RAW_FILE
    };

private:

    static const char* CONTENT_TYPE_VIDEO_QUICKTIME;
    static const char* CONTENT_TYPE_AUDIO_3GPP;
	static const char* CONTENT_TYPE_AUDIO_3GPP_AMRWB;
	static const char* FILE_EXT_AMRWB_3GPP;
    static const char* CONTENT_TYPE_VIDEO_3GPP;
    static const char* CONTENT_TYPE_AUDIO_WAV;
    static const char* CONTENT_TYPE_AUDIO_PCMU;
    static const char* CONTENT_TYPE_AUDIO_ANY;
	static const char* CONTENT_TYPE_CODEC_PARAMETER;
	static const char* CONTENT_TYPE_CODEC_AMR;
	static const char* CONTENT_TYPE_CODEC_AMRWB;	

    static const char* FILE_EXT_VIDEO_QUICKTIME;
    static const char* FILE_EXT_VIDEO_3GPP;
    static const char* FILE_EXT_AUDIO_WAV;

    static int m_movFileVersion;
public:
    /**
     * @param mo Media.
     * 
     * @return A parser that can handle the given media.
     *         If no parser can be found, <code>NULL</code> is
     *         returned.
     */
    static MediaParser* getParser(java::MediaObject& mo);

    /**
     * @param mo Media.
     * 
     * @return A builder that can handle the given media.
     *         If no builder can be found, <code>NULL</code> is
     *         returned.
     */
    static MediaBuilder* getBuilder(const java::MediaObject& mo);

    /**
     * @param mo Media.
     * 
     * @return A writer that can handle the given media.
     *         If no writer can be found, <code>NULL</code> is
     *         returned.
     */
    static MediaObjectWriter* getWriter(java::MediaObject& mo);

    static base::String trim(const base::String& s);

    /**
     * Setter for the MOV file version.
     *
     * @param movFileVersion the file version (0 == MVAS, MDAT before MOOV atom,
     *        1 == MAS, MOOV before MDAT atom).
     */
    static void setMovFileVersion(int movFileVersion);

    /**
     * Getter for the MOV file version.
     *
     * @return movFileVersion the file version (0 == MVAS, MDAT before MOOV atom,
     *        1 == MAS, MOOV before MDAT atom).
     */
    static int getMovFileVersion();

private:
    static FormatHandlerFactory::FileFormat
    getFileFormat(const java::MediaObject& mo);
};

#endif /*FORMATHANDLERFACTORY_H_*/
