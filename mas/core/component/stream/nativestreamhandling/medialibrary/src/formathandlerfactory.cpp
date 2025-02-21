/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <string>
#include "formathandlerfactory.h"
#include "movparser.h"
#include "wavparser.h"
#include "rawparser.h"
#include "mediaparser.h"
#include "java/mediaobject.h"
#include "mediaobjectwriter.h"
#include "mediabuilder.h"
#include "wavbuilder.h"
#include "movbuilder.h"
#include "movwriter.h"
#include "amrbuilder.h"
#include "amrwbbuilder.h"
#include "amrparser.h"
#include "amrwbparser.h"
#include "jlogger.h"
#include "jniutil.h"

static const char* CLASSNAME = "masjni.medialibrary.FormatHandlerFactory";

const char* FormatHandlerFactory::CONTENT_TYPE_VIDEO_QUICKTIME = "video/quicktime";
const char* FormatHandlerFactory::CONTENT_TYPE_AUDIO_3GPP = "audio/3gpp";
const char* FormatHandlerFactory::CONTENT_TYPE_VIDEO_3GPP = "video/3gpp";
const char* FormatHandlerFactory::CONTENT_TYPE_AUDIO_WAV = "audio/wav";
const char* FormatHandlerFactory::CONTENT_TYPE_AUDIO_PCMU = "audio/pcmu";
const char* FormatHandlerFactory::CONTENT_TYPE_CODEC_PARAMETER = "codec=";
const char* FormatHandlerFactory::CONTENT_TYPE_CODEC_AMR = "samr"; //amr-nb
const char* FormatHandlerFactory::CONTENT_TYPE_CODEC_AMRWB = "sawb"; //amr-wb

const char* FormatHandlerFactory::FILE_EXT_VIDEO_QUICKTIME = "mov"; //this is actually audio or video (quicktime container)
const char* FormatHandlerFactory::FILE_EXT_VIDEO_3GPP = "3gp"; //This is actually audio and or video (3gpp/iso container)
const char* FormatHandlerFactory::FILE_EXT_AUDIO_WAV = "wav";

int FormatHandlerFactory::m_movFileVersion = 1; // MOOV atom first

using namespace std;

//using java::MediaObject;

MediaParser*
FormatHandlerFactory::getParser(java::MediaObject& mo)
{
    MediaParser* parser(0);

    switch (getFileFormat(mo))
    {
    case FormatHandlerFactory::WAV_FILE:
        parser = new WavParser(&mo);
        break;

    case FormatHandlerFactory::MOV_FILE:
        parser = new MovParser(&mo);
        break;

    case FormatHandlerFactory::AMR_FILE:
        parser = new AmrParser(&mo);
        break;
	case FormatHandlerFactory::AMRWB_FILE:
        parser = new AmrwbParser(&mo);
        break;

    case FormatHandlerFactory::RAW_FILE:
        parser = new RawParser(&mo);
        break; // caught via Coverity

    default:
        JLogger::jniLogError(mo.getJniEnv(), CLASSNAME, "Could not determine which kind of parser to create.");
        break;
    }

    return parser;
}

MediaBuilder*
FormatHandlerFactory::getBuilder(const java::MediaObject& mo)
{
    MediaBuilder* builder(0);
    JNIEnv* env = mo.getJniEnv();

    switch (getFileFormat(mo))
    {
    case FormatHandlerFactory::WAV_FILE:
	JLogger::jniLogTrace(env, CLASSNAME, "getBuilder() WAV_FILE");
        builder = new WavBuilder(env);
        break;

    case FormatHandlerFactory::MOV_FILE:
        builder = new MovBuilder(env);
        JLogger::jniLogTrace(env, CLASSNAME, "getBuilder() MOV_FILE MOV file version = %d", m_movFileVersion);
        ((MovBuilder*) builder)->setMoovAtomFirst(m_movFileVersion == 1);
        break;

    case FormatHandlerFactory::AMR_FILE:
        builder = new AmrBuilder(env);
        {
            base::String contentType = mo.getContentType();
            base::String trimmedContentType(trim(contentType));

            JLogger::jniLogTrace(env, CLASSNAME, "getBuilder()  AMR_FILE Determining file format, content type: %s",
                    contentType.c_str());
            JLogger::jniLogTrace(env, CLASSNAME, "getBuilder() AMR_FILE Determining file format, trimmed content type: %s",
                    trimmedContentType.c_str());

            ((AmrBuilder*) builder)->setAudioOnlyBuilder(trimmedContentType.find(CONTENT_TYPE_AUDIO_3GPP) == 0);
        }
				
        break;
	case FormatHandlerFactory::AMRWB_FILE:
        builder = new AmrwbBuilder(env);
        {
            base::String contentType = mo.getContentType();
            base::String trimmedContentType(trim(contentType));

            JLogger::jniLogTrace(env, CLASSNAME, "getBuilder() AMRWB_FILE  Determining file format, content type: %s",
                    contentType.c_str());
            JLogger::jniLogTrace(env, CLASSNAME, "getBuilder() AMRWB_FILE Determining file format, trimmed content type: %s",
                    trimmedContentType.c_str());

            ((AmrwbBuilder*) builder)->setAudioOnlyBuilder(trimmedContentType.find(CONTENT_TYPE_AUDIO_3GPP) == 0);
        }
        break;	
    default:
        JLogger::jniLogError(env, CLASSNAME, "getBuilder() Could not determine which kind of builder to create.");
        break;
    }

    return builder;
}

MediaObjectWriter*
FormatHandlerFactory::getWriter(java::MediaObject& mo)
{
    MediaObjectWriter* writer(0);

    switch (getFileFormat(mo))
    {
    case FormatHandlerFactory::WAV_FILE:
        writer = new MediaObjectWriter(&mo);
        break;

    case FormatHandlerFactory::MOV_FILE:
        writer = new MovWriter(&mo);
        break;

    case FormatHandlerFactory::AMR_FILE:
		JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "getWriter() AMR_FILE, create movWriter" );
		writer = new MovWriter(&mo);
        break;
	case FormatHandlerFactory::AMRWB_FILE:
		JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "getWriter() AMRWB_FILE, create movWriter" );
		writer = new MovWriter(&mo);
        break;
	
    default:
        JLogger::jniLogError(mo.getJniEnv(), CLASSNAME, "getWriter() Could not determine which kind of writer to create.");
        break;
    }

    return writer;
}

inline base::String FormatHandlerFactory::trim(const base::String& s)
{
    if (s.empty())
        return s;
    int end = s.length() - 1;
    int start;
	//trim front of string
	//basically if the first charcter is a space or tab remove it by moving start up one.
    for (start = 0; start <= end; start++) {
        int c = s.at(start);
        if (c != ' ' && c != '\t') 
            break;
    }
    if (start == end)
        return "";
	//trim end of string working backwards.
	//basically if the end character is a space or tab skip end back one (remove it)
    for (; end >= 0; end--) {
        int c = s.at(start);
        if (c != ' ' && c != '\t') 
            break;
    }
	//take the new end or start.
    return s.substr(start, end - start + 1);
}

void FormatHandlerFactory::setMovFileVersion(int movFileVersion)
{
    m_movFileVersion = movFileVersion;
}

int FormatHandlerFactory::getMovFileVersion()
{
    return m_movFileVersion;
}

FormatHandlerFactory::FileFormat FormatHandlerFactory::getFileFormat(const java::MediaObject& mo)
{
    base::String fileExtension = mo.getFileExtension();
    FileFormat fileFormat(FormatHandlerFactory::UNDEFINED);

    JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "Determining file format, extension: %s", fileExtension.c_str());

    // First attempting to determine file type from extension
    if (fileExtension == FILE_EXT_AUDIO_WAV) {
        fileFormat = FormatHandlerFactory::WAV_FILE;
    } else if (fileExtension == FILE_EXT_VIDEO_QUICKTIME) {
        fileFormat = FormatHandlerFactory::MOV_FILE;
    } else if (fileExtension == FILE_EXT_VIDEO_3GPP) {
		//here we do not know it the content is AMR or AMR-WB
		//so we have to check the content type, so we ignore
		//it and parse by content type instead below.
		fileFormat = FormatHandlerFactory::UNDEFINED;
		   JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME,
                "Could not determine file format from the fileExtension (3GPP), checking contentType instead. File extension=%s",
                fileExtension.c_str());
    } else {
		fileFormat = FormatHandlerFactory::UNDEFINED;
	} 
	if ( fileFormat == FormatHandlerFactory::UNDEFINED) {
        // Failed to determine file type from extension
        // due to unknown or undefined file extension
		//FIXME here we can decide if it's amr or amr-wb by using an internal mimetype
		//to determine the diff, we will can pass CONTENT_TYPE_AUDIO_3GPP_AMRWB, otherwise we
		//have to determine the type by looking for samr or sawb
		
        JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME,
                "Could not determine file format from the fileExtension. checking contentType instead. File extension=%s",
                fileExtension.c_str());
        base::String contentType = mo.getContentType();
        base::String trimmedContentType(trim(contentType));

        JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "Determining file format, content type: %s",
                contentType.c_str());
        JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "Determining file format, trimmed content type: %s",
                trimmedContentType.c_str());

        if (trimmedContentType.find(CONTENT_TYPE_AUDIO_WAV) != string::npos ) {
            fileFormat = FormatHandlerFactory::WAV_FILE;
        } else if (trimmedContentType.find(CONTENT_TYPE_VIDEO_QUICKTIME) != string::npos ) {
            fileFormat = FormatHandlerFactory::MOV_FILE;
        } else if (trimmedContentType.find(CONTENT_TYPE_AUDIO_3GPP) != string::npos) {
            
			/*We need to check if amr(samr) or amr-wb (sawb)
			 *Strictly if we are to parse according to rfc 6381 we should check for a semi colon followed
			 *by codec, but we are taking a shortcut to do less parsing.
			 * 
			 */
			JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "FileFormat() trimmed content type is audio 3gpp - looking for codec parameter");
			size_t position=trimmedContentType.find(CONTENT_TYPE_CODEC_PARAMETER); 
			if (position != string::npos ) {
				position+=6; //add length of codec=
				//we have codec in the string starting at position.
				JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "FileFormat() found codec tag in string - matching codec");
				if (trimmedContentType.find(CONTENT_TYPE_CODEC_AMR,position) != string::npos ) {
					fileFormat = FormatHandlerFactory::AMR_FILE;
					JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME,
						"FileFormat() From codec samr, File Type is 3gpp amr (nb)");
				} else if (trimmedContentType.find(CONTENT_TYPE_CODEC_AMRWB,position) != string::npos ) {
					fileFormat = FormatHandlerFactory::AMRWB_FILE;
						JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME,
						"FileFormat() From codec sawb, File Type is 3gpp amr-wb (wb)");
				} else {
					fileFormat = FormatHandlerFactory::UNDEFINED;
					JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "FileFormat() From codec not matched; fileFormat is set to undefined");
				}
			} else {
				//Assume narrow band if codec parameter not added.
				fileFormat = FormatHandlerFactory::AMR_FILE;
				JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME,
						"FileFormat() From null codec, File Type is 3gpp amr (nb)");
			}
        } else if (trimmedContentType.find(CONTENT_TYPE_VIDEO_3GPP) != string::npos) {
            fileFormat = FormatHandlerFactory::AMR_FILE;
			//for now we only support amr-nb for video.. So assume it's 
			//narrow band.
        } else if (trimmedContentType.find(CONTENT_TYPE_AUDIO_PCMU) != string::npos ) {
            fileFormat = FormatHandlerFactory::RAW_FILE;
        } else {
            JLogger::jniLogWarn(mo.getJniEnv(), CLASSNAME, "Could not determine file format from the content type.");
        }
    }

    JLogger::jniLogTrace(mo.getJniEnv(), CLASSNAME, "FileFormat() File format: %d", fileFormat);
    return fileFormat;
}
