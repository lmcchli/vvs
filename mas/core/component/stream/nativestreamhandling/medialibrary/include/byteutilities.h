/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef RAW_H_
#define RAW_H_

#include "int.h"

// Definition for exporting to DLL on windows 
#ifdef WIN32
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
 * This class provides functions for reading values from a memory buffer. 
 * @author Mats Egland
 * @see int.h
 */
class MEDIALIB_CLASS_EXPORT ByteUtilities
{
public:
    /**
     * Swaps the bytes in the passed word (16 bits).
     * @param uw A reference to the word to be swapped
     */
    static void swapW(uint16_t &uw);
    /**
     * Swaps the bytes in the passed double word (32 bits).
     * @param duw Reference to the double word to be swapped.
     */
    static void swapDW(uint32_t &uw);
    /**
     * Reads a word (16 bits) from the given memory position.
     * 
     * @param p Pointer to the value, i.e. the memory position where the
     * value to be read resides. 
     * @param uw Reference to the word that the value is read into
     * @param swap if true the uw parameter is byte-swapped before returned.
     */
    static void readW(const char *p, uint16_t &uw, bool swap);
    /**
     * Reads a double word (32 bits) from the given memory position. 
     * 
     * @param p Pointer to the value, i.e. the memory position where the
     * value to be read resides.
     * @param uw Reference to the word that the value is read into
     * @param swap if true the udw parameter is byte-swapped before returned.
     */
    static void readDW(const char *p, uint32_t &udw, bool swap);

    /**
     * Word-aligns the passed value, i.e. adds 1 to the value
     * if it is odd.
     */
    static void alignW(uint16_t &udw);

    /**
     * Word-aligns the passed value, i.e. adds 1 to the value
     * if it is odd.
     */
    static void alignDW(uint32_t &udw);
};

#endif /*RAW_H_*/
