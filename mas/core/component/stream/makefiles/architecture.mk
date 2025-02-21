#ifeq '${ARCH}' 'sun5'
#	SYSTEM=SunOS.$(shell uname -r)
#else
#	SYSTEM=win32
#endif
#SYSTEM=Linux.$(shell uname -r | sed -r 's/^([0-9]*\.[0-9]*\.[0-9]*)\..*/\1/g')
#system (linux kernel -just the last cp only i.e. 4.3 or 4.4, 
#we do not care about ec releases etc.
#SYSTEM=Linux-kern-$(shell uname -r | sed -r 's/^([0-9]*\.[0-9])\..*/\1/g')
SYSTEM=Linux-kern-$(shell uname -r | cut -d'-' -f 1 | cut -d'.' -f 1,2)
PLATFORM=linux

