/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
 
 
/**
 * Type-definitions for integerers used in the medialibrary. 
 * @author Mats Egland
 */  
#ifndef INT_H_
#define INT_H_
typedef unsigned char       uint8_t;
typedef unsigned char       uint8;
typedef unsigned short      uint16_t;
typedef unsigned int        uint32_t;
 
/*typedef          char       int8_t; */
typedef          short      int16_t;
typedef          int        int32_t;
#ifdef WIN32
typedef          /*long*/ __int64       int64_t;
typedef unsigned long int   uint64_t;
#endif /* WIN32 */

#endif /*INT_H_*/

