/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#pragma warning(disable:4786)	// debug information truncated to 255 characters
#pragma warning(disable:4503)	// decorated name length exceeded, name was truncated

#ifdef WIN32
#include <winsock2.h>
#include <windows.h>
#include <process.h>
#else
#include <pthread.h>
#include <unistd.h>
#endif

#ifndef PTHREAD_H
#define pthread_mutex_t CRITICAL_SECTION
#define pthread_mutex_init(mutex, attr) InitializeCriticalSection(mutex)
#define pthread_mutex_destroy(mutex) DeleteCriticalSection(mutex)
#define pthread_mutex_lock(mutex) EnterCriticalSection(mutex)
#define pthread_mutex_unlock(mutex) LeaveCriticalSection(mutex)
#endif 


