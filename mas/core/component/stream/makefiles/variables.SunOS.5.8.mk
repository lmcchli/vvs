TARGET_LIBRARY=$(LIBDIR)/$(LIB_PREFIX)$(TARGET_NAME)$(LIB_SUFFIX)
TARGET_DIST_LIBRARY=$(DISTLIBDIR)/$(LIB_PREFIX)$(TARGET_NAME)$(LIB_SUFFIX)

SUNOS_VERSION=$(shell uname -r)

CP=cp
RM=rm -rf
ECHO=echo

PURIFY_VERSION=PurifyPlus.2003a.06.13.FixPack.0168
SUNWSPRO=/usr/local/Studio8/SUNWspro
RATIONAL=/usr/local/rational/releases
PURIFY_HOME=$(RATIONAL)/$(PURIFY_VERSION)/sun4_solaris2

#
# In order to use purify invoke clearmake with PURIFY=yes 
# Example: clearmake -C gnu PURIFY=yes
#
#PURIFY_OPTIONS+= -selective
#PURIFY_OPTIONS+= -ignore_signals=SIGPOLL 
#PURIFY_OPTIONS+= --threads=yes 
#PURIFY_OPTIONS+= -use_internal_locks=yes
#PURIFY_OPTIONS+= -force-rebuild=no
PURIFY_OPTIONS+= -handle-calls-to-java
PURIFY_OPTIONS+= -always-use-cache-dir=yes
PURIFY_OPTIONS+= -cache-dir=./purify-cache
#PURIFY_OPTIONS+= -best-effort

ifeq '$(PURIFY)' 'yes' 
  PURIFY_CMD=$(PURIFY_HOME)/bin/purify $(PURIFY_OPTIONS)
endif

CC=$(SUNWSPRO)/bin/cc
CXX=$(SUNWSPRO)/bin/CC
LD=$(CXX)

ifeq '$(TARGET_TYPE)' 'StaticLibrary'
LD=ld
endif 

JAVAC=$(JAVA_HOME)/bin/javac

CPPUNIT=/proj/ipms/cppunit/cppunit-1.8.0
PURIFY=$(PURIFY_HOME)/bin/purify

# JNI
JAVA_HOME=/usr/local/jdk1.5.0_05
JNI_INCDIRS=
JNI_INCDIRS+=$(JAVA_HOME)/include
JNI_INCDIRS+=$(JAVA_HOME)/include/solaris

COMPONENT_INCDIRS+=$(COMMONCPP2_DIR)/include


DEFS+=-DHAVE_CONFIG_H 
DEFS+=-DPTHREAD_H 
DEFS+=-D_POSIX_PTHREAD_SEMANTICS 
DEFS+=-D_REENTRANT 
#DEFS+=-DBSD_COMP
DEFS+=-D_GNU_SOURCE
DEFS+=-DBIG_ENDIAN

CFLAGS=$(DEFS) -O3 -mt -KPIC -DPIC
CXXFLAGS=$(DEFS) -O3 -mt -KPIC -DPIC

ifeq '$(TARGET_TYPE)' 'StaticLibrary'
CFLAGS=$(DEFS) -g
CXXFLAGS=$(DEFS) -g
endif 

OBJDIR=obj_sol_$(SUNOS_VERSION)

LIBDIRS+=$(LIBDIR)
LIBDIRS+=$(SUNWSPRO)/lib
LIBDIRS+=$(CPPUNIT)/lib
LIBDIRS+=$(JAVA_HOME)/jre/lib/sparc 

JCOMPILE=$<
JCLASSFILE=

COMPILE=-c $< 
LDFLAGS=-G -o $@

ifeq '$(TARGET_TYPE)' 'StaticLibrary'
LDFLAGS=-r -o $@
endif 

OBJECTFILE = -o $(OBJDIR)/$(@F)

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

COMMONCPP2_VERSION=commoncpp2-1.3.19
CCRTP_VERSION=ccrtp-1.3.6
CCGNU2_LIB=$(DYNAMIC_LIB_PREFIX)ccgnu2$(DYNAMIC_LIB_SUFFIX)
CCEXT2_LIB=$(DYNAMIC_LIB_PREFIX)ccext2$(DYNAMIC_LIB_SUFFIX)

