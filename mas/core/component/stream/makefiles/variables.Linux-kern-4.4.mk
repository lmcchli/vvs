TARGET_LIBRARY=$(LIBDIR)/$(LIB_PREFIX)$(TARGET_NAME)$(LIB_SUFFIX)
TARGET_ARCHIVE=$(LIBDIR)/$(STATIC_LIB_PREFIX)$(TARGET_NAME)$(STATIC_LIB_SUFFIX)
TARGET_DIST_LIBRARY=$(DISTLIBDIR)/$(LIB_PREFIX)$(TARGET_NAME)$(LIB_SUFFIX)

# Defining directories, if running snapshot define the VOB_HOME
# and probably MAS_HOME also 
# JAVA_HOME should always be defined in environment.
STREAM_HOME ?=$(shell pwd | sed 's@stream/.*@stream@')
#Note on the above, will strip all words,numbers,slashes after stream in the path. Linux only.
MAS_HOME = $(STREAM_HOME)/..
DEPEND_ON_VOB_ONLY ?= yes
JAVA_HOME ?= /project/MMS/jdk1.6.0_12_linux/ 

CP=cp
RM=rm -rf
ECHO=echo
CD=cd

CC=gcc
CXX=g++
LD=$(CXX)
AR=ar

OUR_MAKEFLAGS+=REALTIME=yes

ifeq '$(DEBUG)' 'yes'
OPTIMIZATION_FLAGS=-O0
else
OPTIMIZATION_FLAGS=-O2
endif

#enable debug output for 3gpp or mov iso - should got to stdout log
#DEFS+=-DMOV_PRINT_DEBUG

DEFS+=-g
DEFS+=-DHAVE_CONFIG_H
DEFS+=-D_POSIX_PTHREAD_SEMANTICS
DEFS+=-D_REENTRANT
DEFS+=-D_GNU_SOURCE
DEFS+=-D_PTHREADS
DEFS+=-DLINUX
DEFS+=-DLINUX_EPOLL
DEFS+=-DCPPUNIT_HAVE_UNIX_DLL_LOADER
DEFS+= -static -D_FORTIFY_SOURCE=2 -fstack-protector-all
CFLAGS=$(DEFS) -std=c99
DEFS+=-Wall -Wno-deprecated -Wno-unused-result
#add below line to stop on warnings, note there are bugs in some 3pp libarys with fortify source that will stop
#with disable warning _FORTIFY_SOURCE requires compiling with optimization (-O), so cannot turn on by default as no otion to disable this warning
#DEFS+=-Werror 

#DEFS+=-Wall -W

#ARCH_FLAGS=-march=nocona -fno-omit-frame-pointer -fno-strict-aliasing
#https://gcc.gnu.org/onlinedocs/gcc-4.5.3/gcc/i386-and-x86_002d64-Options.html
ARCH_FLAGS=-march=x86-64 -mtune=generic -fno-omit-frame-pointer -fno-strict-aliasing
TARGET_CODE_FLAGS=-fPIC -DPIC
#MODIFIER_FLAGS=-fno-builtin  -fno-implicit-inline-templates -fno-implement-inlines -fno-default-inline -fno-inline -finline-limit=1

ifneq '$(NOOPT)' 'yes'
CXXFLAGS=$(OPTIMIZATION_FLAGS)
else
CXXFLAGS=-D_GLIBCXX_DEBUG_PEDANTIC
endif
CXXFLAGS+=$(DEFS) -pthread $(ARCH_FLAGS) $(MODIFIER_FLAGS) $(TARGET_CODE_FLAGS)
CXXFLAGS+=$(DEBUG_FLAGS)
#CXXFLAGS=-std=gnu++98 

ifeq '$(REALTIME)' 'yes' 
endif

ifeq '$(DEPEND_ON_VOB_ONLY)' 'yes'

DEPEND_ON_VOB_FLAG=DEPEND_ON_VOB_ONLY=yes
LOGGING_3PP_HOME=$(STREAM_HOME)/log4cxx
#LOGGING_3PP_LIBDIR=$(LOGGING_3PP_HOME)/lib
QUICKTIME_HOME=$(STREAM_HOME)/quicktime

else
LOGGING_3PP_HOME=$(STREAM_HOME)/log4cxx
LOGGING_3PP_LIBDIR=$(LOGGING_3PP_HOME)
QUICKTIME_HOME=$(STREAM_HOME)/quicktime
endif

# Defining Logging 3PP version (use LOGGING_3PP... for includes etc)
LOG4CXX_VERSION=0.9.7
LOG4CXX_ROOT=log4cxx-$(LOG4CXX_VERSION)

# Defining where to find include files for logging 3PP
LOGGING_3PP_INCDIR=$(LOGGING_3PP_HOME)/$(LOG4CXX_ROOT)/include

QUICKTIME_INCDIR=$(QUICKTIME_HOME)/include
QUICKTIME_LIBDIR=$(QUICKTIME_HOME)/lib

CPPUNIT_VERSION=cppunit-1.10.2
CPPUNIT=$(STREAM_HOME)/nativestreamhandling/3pp/$(CPPUNIT_VERSION)
RATIONAL=/usr/local/rational/releases
PURIFY_VERSION=purify.sol.2003a.06.15.FixPack.0188
PURIFY_HOME=$(RATIONAL)/$(PURIFY_VERSION)

#
# In order to use purify invoke clearmake with PURIFY=yes 
# Example: clearmake -C gnu PURIFY=yes
#
PURIFY_OPTIONS+= -demangle-program=/usr/sfw/bin/gc++filt
#PURIFY_OPTIONS+= -force-rebuild=no
PURIFY_OPTIONS+= -max_threads=30
PURIFY_OPTIONS+= -threads=yes
PURIFY_OPTIONS+= -always-use-cache-dir=yes
PURIFY_OPTIONS+= -cache-dir=./purify-cache
#PURIFY_OPTIONS+= -handle-calls-to-java

ifeq '$(PURIFY)' 'yes'
PURIFY_CMD=purify $(PURIFY_OPTIONS)
DEBUG=yes
endif

# JNI
JNI_INCDIRS=
JNI_INCDIRS+=$(JAVA_HOME)/include
JNI_INCDIRS+=$(JAVA_HOME)/include/linux

JNI_LIBDIR=$(JAVA_HOME)/jre/lib/amd64/server

COMPONENT_INCDIRS+=$(COMMONCPP2_DIR)/include

BOOST_VERSION=1.33.1
#BOOST_HOME=$(VOB_HOME)/mas/stream/nativestreamhandling/3pp/boost_$(BOOST_VERSION)
#BOOST_HOME=$(VOB_HOME)/ipms_sys2/boost/$(BOOST_VERSION)
BOOST_HOME=$(STREAM_HOME)/boost/$(BOOST_VERSION)

BOOST_INCDIR=$(BOOST_HOME)
BOOST_THREAD_SRC=$(BOOST_HOME)/libs/thread/src

BOOST_THREAD_ARCHIVE=boostthread

OBJDIR=$(SYSTEM)

LIBDIRS+=$(LIBDIR)
LIBDIRS+=$(QUICKTIME_LIBDIR)
LIBDIRS+=$(LOGGING_3PP_LIBDIR)

COMPILE=-c $< 
## to debug ld
##LDFLAGS=--trace -shared -pthread -fPIC -Wl,--verbose -Wl -o $@
LDFLAGS=--trace -shared -pthread -fPIC -o $@
OBJECTFILE = -o $(OBJDIR)/$(@F)
ARFLAGS= -r $@

#LIBS =log4cxx quicktime 
LIBS=quicktime
LIBS +=pthread stdc++ nsl rt
INCLUDES = $(addprefix -I,$(INCDIRS))
LIBRARIES+= $(addprefix -L,$(LIBDIRS))
LIBRARIES+= $(addprefix -l,$(LIBS))

STATIC_LIB_PREFIX=lib
STATIC_LIB_SUFFIX=.a
DYNAMIC_LIB_PREFIX=lib
DYNAMIC_LIB_SUFFIX=.so

LIB_PREFIX=$(DYNAMIC_LIB_PREFIX)
LIB_SUFFIX=$(DYNAMIC_LIB_SUFFIX)

ifeq '$(TARGET_TYPE)' 'StaticLibrary'
LIB_PREFIX=$(STATIC_LIB_PREFIX)
LIB_SUFFIX=$(STATIC_LIB_SUFFIX)
endif 

CCGNU2_LIB=$(DYNAMIC_LIB_PREFIX)ccgnu2$(DYNAMIC_LIB_SUFFIX)
CCEXT2_LIB=$(DYNAMIC_LIB_PREFIX)ccext2$(DYNAMIC_LIB_SUFFIX)

LOGGING_INCDIR=$(MAS_HOME)/logging/cpp/include
LOGGING_3PP_ARCHIVE=$(LOGGING_3PP_LIBDIR)/$(STATIC_LIB_PREFIX)log4cxx$(STATIC_LIB_SUFFIX)
LOGGING_3PP_LIB=$(DYNAMIC_LIB_PREFIX)log4cxx$(DYNAMIC_LIB_SUFFIX)

3PP_DIR=$(STREAM_HOME)/nativestreamhandling/3pp

COMMONCPP2_VERSION=commoncpp2-1.3.22.1
CCRTP_VERSION=ccrtp-1.3.6
CCRTP_INCDIR=$(3PP_DIR)/${CCRTP_VERSION}/src

COMMONCPP2_DIR=$(3PP_DIR)/$(COMMONCPP2_VERSION)
COMMONCPP_INCDIR=$(COMMONCPP2_DIR)/include

SYSLIBS += /usr/local/lib/libstdc++.so.5
SYSLIBS += $(LOGGING_3PP_LIBDIR)/$(LOGGING_3PP_LIB)
