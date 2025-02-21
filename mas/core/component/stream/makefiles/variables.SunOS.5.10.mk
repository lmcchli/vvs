TARGET_LIBRARY=$(LIBDIR)/$(LIB_PREFIX)$(TARGET_NAME)$(LIB_SUFFIX)
TARGET_ARCHIVE=$(LIBDIR)/$(STATIC_LIB_PREFIX)$(TARGET_NAME)$(STATIC_LIB_SUFFIX)
TARGET_DIST_LIBRARY=$(DISTLIBDIR)/$(LIB_PREFIX)$(TARGET_NAME)$(LIB_SUFFIX)

# Defining directories, if running snapshot define the SRC_ROOT
MAS_HOME=../..
SUNOS_VERSION=$(shell uname -r)

CP=cp
RM=rm -rf
ECHO=echo
CD=cd

CC=/usr/sfw/bin/gcc
CXX=/usr/sfw/bin/g++
LD=$(CXX)
AR=ar

OUR_MAKEFLAGS+=REALTIME=yes

DEFS+=-DHAVE_CONFIG_H 
DEFS+=-D_POSIX_PTHREAD_SEMANTICS 
DEFS+=-D_REENTRANT 
DEFS+=-D_GNU_SOURCE 
DEFS+=-D_PTHREADS
DEFS+=-DBSD_COMP
DEFS+=-D__SUNPRO_CC
DEFS+=-D_OSF_SOURCE

CFLAGS=$(DEFS) -std=c99

## Last call from Micke A.
ARCH_FLAGS=-mno-faster-structs -mcpu=v8 -mno-app-regs -m32 -fno-omit-frame-pointer -fno-strict-aliasing
TARGET_CODE_FLAGS=-fPIC -DPIC
#MODIFIER_FLAGS=-fno-builtin  -fno-implicit-inline-templates -fno-implement-inlines -fno-default-inline -fno-inline -finline-limit=1

CXXFLAGS=$(OPTIMIZATION_FLAGS)
CXXFLAGS+=$(DEFS) -pthreads $(ARCH_FLAGS) $(MODIFIER_FLAGS) $(TARGET_CODE_FLAGS)
CXXFLAGS+=$(DEBUG_FLAGS)

ifeq '$(REALTIME)' 'yes' 
  CXXFLAGS+=-include $(RTSAFE_INCDIR)/rtsafe_atomic.h
endif

ifeq '$(DEPEND_ON_VOB_ONLY)' 'yes'

DEPEND_ON_VOB_FLAG=DEPEND_ON_VOB_ONLY=yes
RTSAFE_HOME=../../../ipms_sys/rtsafe
RTSAFE_LIBDIR=$(RTSAFE_HOME)/lib
LOGGING_3PP_HOME=/vobs/ipms/ipms_sys/log4cxx
LOGGING_3PP_LIBDIR=$(LOGGING_3PP_HOME)/lib-rt
QUICKTIME_HOME=/vobs/ipms/ipms_sys/quicktime

else

RTSAFE_HOME=/proj/ipms/rtsafe/P1A.000
RTSAFE_LIBDIR=$(RTSAFE_HOME)/lib
LOGGING_3PP_HOME=/proj/ipms/log4cxx/0.9.7-rt
LOGGING_3PP_LIBDIR=$(LOGGING_3PP_HOME)
QUICKTIME_HOME=/proj/ipms/quicktime/test.012
endif

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


ifeq '$(DEBUG)' 'yes' 
DEBUG_FLAGS=-g
OPTIMIZATION_FLAGS=
else
DEBUG_FLAGS=
OPTIMIZATION_FLAGS=-O2
endif

# JNI
JAVA_HOME=/usr/local/jdk1.5.0_05
JNI_INCDIRS=
JNI_INCDIRS+=$(JAVA_HOME)/include
JNI_INCDIRS+=$(JAVA_HOME)/include/solaris

COMPONENT_INCDIRS+=$(COMMONCPP2_DIR)/include

BOOST_VERSION=1.33.1
BOOST_HOME=/proj/ipms/boost/$(BOOST_VERSION)
BOOST_INCDIR=$(BOOST_HOME)
BOOST_THREAD_SRC=$(BOOST_HOME)/libs/thread/src

BOOST_THREAD_ARCHIVE=boostthread

DEFS+=-DLITTLE_ENDIAN

OBJDIR=obj_sol_$(SUNOS_VERSION)

LIBDIRS+=$(LIBDIR)
LIBDIRS+=/usr/lib/libp
LIBDIRS+=$(SUNWSPRO)/lib
LIBDIRS+=$(JAVA_HOME)/jre/lib/sparc 
LIBDIRS+=$(QUICKTIME_HOME)/lib
LIBDIRS+=$(RTSAFE_LIBDIR)
LIBDIRS+=$(LOGGING_3PP_LIBDIR)

COMPILE=-c $< 
LDFLAGS=-shared -pthreads -fPIC -Wl,-B -Wl,symbolic -o $@
OBJECTFILE = -o $(OBJDIR)/$(@F)
ARFLAGS= -r $@

LIBS  =log4cxx quicktime rtsafe
LIBS +=pthread stdc++ socket nsl rt
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

3PP_DIR=$(MAS_HOME)/stream/nativestreamhandling/3pp

COMMONCPP2_VERSION=commoncpp2-1.3.22
CCRTP_VERSION=ccrtp-1.3.6

COMMONCPP2_DIR=$(3PP_DIR)/$(COMMONCPP2_VERSION)
COMMONCPP_INCDIR=$(COMMONCPP2_DIR)/include

SYSLIBS += /usr/local/lib/libstdc++.so.5
SYSLIBS += $(LOGGING_3PP_LIBDIR)/$(LOGGING_3PP_LIB)
SYSLIBS += $(RTSAFE_LIBDIR)/librtsafe.so

OPTIONAL_DIST = cp $(SYSLIBS) $(MAS_HOME)/lib
OPTIONAL_DISTCLEAN = rm -f $(addprefix $(MAS_HOME)/lib/, $(notdir $(SYSLIBS)))

QUICKTIME_INCDIR=$(QUICKTIME_HOME)/include
QUICKTIME_ARCHIVE=$(QUICKTIME_HOME)/lib/$(STATIC_LIB_PREFIX)quicktime$(STATIC_LIB_SUFFIX)

RTSAFE_INCDIR=$(RTSAFE_HOME)/include
RTSAFE_ARCHIVE=$(RTSAFE_HOME)/lib/$(STATIC_LIB_PREFIX)rtsafe$(STATIC_LIB_SUFFIX)
