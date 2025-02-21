#if !defined(BASE_STD_H)

#if !defined(WIN32) && defined(NEED_RTSAFE)
		#include <rtsafe_atomic.h>
	#endif

	#define BASE_STD_H
	#include <memory>
	#include <list>
	#include <set>
	#include <deque>
	#include <map>
	#include <queue>
	#include <iostream> 
	#include <cctype> // for toupper
	#include <algorithm>
	#include <cstdio>
	#include <cstdlib>
	#include <fstream> 


	#if defined(WIN32)
		#include <hash_map>
	#else
		#include <ext/hash_map>
		#define stdext __gnu_cxx
	#endif

#endif
