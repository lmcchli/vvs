/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef PLATFORM_H_
#define PLATFORM_H_

#include <base_include.h>

// Definition for exporting to DLL on windows 
#ifdef WIN32
	#pragma warning( disable : 4251 )
	#ifndef MEDIALIBRARY_NO_DLL
		#ifdef MEDIALIBRARY_EXPORTS
			#define MEDIALIB_CLASS_EXPORT    __declspec(dllexport)
		#else 
			#define MEDIALIB_CLASS_EXPORT    __declspec(dllimport)   
		#endif
	#else
		#define MEDIALIB_CLASS_EXPORT
	#endif
#else
    #define MEDIALIB_CLASS_EXPORT
#endif

/**
 * Class that provides functions to decide what platform the code is run on.
 */ 
class MEDIALIB_CLASS_EXPORT Platform {

public:
    /**
     * Returns whether the code is executed on a big-endian machine. 
     * @return true if platform is big-endian
     */ 
    static bool isBigEndian();
    /**
     * Returns whether the code is executed on a little-endian machine. 
     * @return true if platform is little-endian
     */
    static bool isLittleEndian();
    
private:

};
#endif /*PLATFORM_H_*/
