SUNOS_VERSION=$(shell uname -r)

CP=cp
RM=rm -rf
ECHO=echo

SUNWSPRO=/usr/local/SUNWspro

DEFS+=-DHAVE_CONFIG_H 
DEFS+=-DPTHREAD_H 
DEFS+=-D_POSIX_PTHREAD_SEMANTICS 
DEFS+=-D_REENTRANT 
DEFS+=-D_GNU_SOURCE 

CFLAGS=$(DEFS) -std=c99
#CXXFLAGS=$(DEFS) -Wno-deprecated -DPIC -DCCXX_EXPORT_LIBRARY
#CXXFLAGS=$(DEFS) -g3 -DPIC -DCCXX_EXPORT_LIBRARY
CXXFLAGS=$(DEFS) -g -DCCXX_EXPORT_LIBRARY

#CPPUNIT=/proj/ipms/cppunit/cppunit-1.8.0
CPPUNIT=/home/tyke/mbeme/cppunit/cppunit-1.10.2
RATIONAL=/usr/local/rational/releases
PURIFY_VERSION=purify.sol.2003a.06.15.FixPack.0188
PURIFY_HOME=$(RATIONAL)/$(PURIFY_VERSION)

#
# In order to use purify invoke clearmake with PURIFY=yes 
# Example: clearmake -C gnu PURIFY=yes
#
PURIFY_OPTIONS+= -demangle-program=/usr/sfw/bin/gc++filt
PURIFY_OPTIONS+= -force-rebuild=no
PURIFY_OPTIONS+= -always-use-cache-dir=yes
PURIFY_OPTIONS+= -cache-dir=./purify-cache
PURIFY_OPTIONS+= -handle-calls-to-java

ifeq '$(PURIFY)' 'yes' 
  PURIFY_CMD=$(PURIFY_HOME)/purify $(PURIFY_OPTIONS)
endif

COMMONCPP2_VERSION=commoncpp2-1.3.22
CCRTP_VERSION=ccrtp-1.3.6

DEFS+=-DBIG_ENDIAN

OBJDIR=obj_sol_$(SUNOS_VERSION)

LIBDIRS+=$(LIBDIR)
LIBDIRS+=/usr/sfw/lib
LIBDIRS+=$(SUNWSPRO)/lib
#LIBDIRS+=$(CPPUNIT)/lib

COMPILE=-c $< 
#LDFLAGS=-G -fpic -fPIC -o $@
#LDFLAGS=-G -o $@
LDFLAGS=-Rlib -R../lib	
OBJECTFILE = -o $(OBJDIR)/$(@F)

INCLUDES = $(addprefix -I,$(INCDIRS))
LIBRARIES+= $(addprefix -L,$(LIBDIRS))
LIBRARIES+= $(addprefix -l,$(LIBS))

LIB_PREFIX=lib
LIB_SUFFIX=.so
COMMONCPP2_VERSION=commoncpp2-1.3.22
CCRTP_VERSION=ccrtp-1.3.6
CCGNU2_LIB=$(LIB_PREFIX)ccgnu2$(LIB_SUFFIX)
CCEXT2_LIB=$(LIB_PREFIX)ccext2$(LIB_SUFFIX)


OPTIONAL_DIST = cp $(SYSLIBS) ../lib
OPTIONAL_DISTCLEAN = rm -f $(addprefix ../lib/, $(notdir $(SYSLIBS)))


