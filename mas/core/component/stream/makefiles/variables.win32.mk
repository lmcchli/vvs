# The file and path name of the targeted library
TARGET_LIBRARY=$(LIBDIR)/$(LIB_PREFIX)$(TARGET_NAME)$(LIB_SUFFIX)
TARGET_ARCHIVE=$(LIBDIR)/$(STATIC_LIB_PREFIX)$(TARGET_NAME)$(STATIC_LIB_SUFFIX)
TARGET_DIST_LIBRARY=$(DISTLIBDIR)/$(LIB_PREFIX)$(TARGET_NAME)$(LIB_SUFFIX)

# Setting view root
ifndef VIEWSTORE
VIEWSTORE=$(shell cleartool pwv -root)
endif

# Defining directories, if running snapshot define the SRC_ROOT
MAS_HOME=$(subst \,/,$(VIEWSTORE))/mas


MEDIALIBRARYHOME=$(subst /,\,$(MAS_HOME)/stream/medialibrary)
CCRTPADAPTERHOME=$(subst /,\,$(MAS_HOME)/stream/ccrtpadapter)
 

# Setting up compiler home
ifndef MSVC_HOME
MSVC_DIR=c:/CppCompiler/VCpp
else
MSVC_DIR=$(subst \,/,$(MSVC_HOME))
endif

# Setting up clean command (depends upon cygwin)
ifndef CYGWIN_HOME
RM_CMD=del /f /q
else
RM_CMD=$(subst \,/,$(CYGWIN_HOME))/bin/rm -f
endif

LOGGING_DIR=/proj/ipms/log4cxx/0.9.7
LOGGING_INCDIR=$(MAS_HOME)/logging/cpp/include
LOGGING_ARCHIVE=$(STATIC_LIB_PREFIX)log4cxx$(STATIC_LIB_SUFFIX)
LOGGING_LIB=$(DYNAMIC_LIB_PREFIX)log4cxx$(DYNAMIC_LIB_SUFFIX)

3PP_DIR=$(MAS_HOME)/stream/nativestreamhandling/3pp

COMMONCPP2_DIR=$(MAS_HOME)/stream/nativestreamhandling/3pp/$(COMMONCPP2_VERSION)
STREAM_DIR=$(MAS_HOME)/stream

ifndef JAVA_HOME
JAVA_DIR="c:/Program Files/Java/jdk1.5.0_04"
else
JAVA_DIR="$(subst \,/,$(JAVA_HOME))"
endif
 

# Some JNI stuff
JAVAH=$(JAVA_DIR)/bin/javah
# JNI header paths
JNI_INCDIRS+=$(JAVA_DIR)/include
JNI_INCDIRS+=$(JAVA_DIR)/include/win32

# Shell commands
CP=copy
RM=del /f /q
ECHO=echo

# Compilers and linker
CL=$(MSVC_DIR)/bin/cl
CC=$(CL)
CXX=$(CL)
LD=$(MSVC_DIR)/bin/link
AR=$(MSVC_DIR)/bin/lib

CPPUNIT=$(3PP_DIR)/cppunit-1.10.2
PURIFY=${PURIFY_HOME}/bin/purify

# Some defines
DEFS+=-DWIN32 -D_LIB -DLOG4CXX_STATIC -DCCXX_NO_DLL -D_WINDOWS -DCCRTP_CAPE -DMEDIALIBRARY_NO_DLL -D_USRDLL -DBOOST_THREAD_EXPORTS -DBOOST_THREAD_BUILD_LIB -D_MBCS

# Compiler command line flags
CXXFLAGS+=$(DEFS) /GF /MD /O2 /Ob1
CXXFLAGS+=/EHsc /nologo /W3 /Gd /c

# For DEBUG:
#CXXFLAGS += /ZI /RTC1 /MDd /Od

# Header file directories
COMPONENT_INCDIRS=$(MSVC_DIR)/include 
COMPONENT_INCDIRS+=$(COMMONCPP2_DIR)/w32
COMPONENT_INCDIRS+=$(COMMONCPP2_DIR)/w32/cc++ 
COMPONENT_INCDIRS+=$(COMMONCPP2_DIR)/template
COMPONENT_INCDIRS+=$(COMMONCPP2_DIR)/include
COMPONENT_INCDIRS+=$(JNI_INCDIRS)

INCDIRS=$(MSVC_DIR)/include 

# Path name of the object files
OBJDIR=obj_win32

BOOST_VERSION=1.33.1
BOOST_HOME=$(VIEWSTORE)/ipms_sys/boost/$(BOOST_VERSION)
BOOST_INCDIR=$(BOOST_HOME)
BOOST_THREAD_SRC=$(BOOST_HOME)/libs/thread/src

# Library file paths
LIBDIRS+=$(LIBDIR) 
LIBDIRS+=$(MSVC_DIR)/lib 
LIBDIRS+=$(TARGET_DIR)
LIBDIRS+=$(MAS_HOME)/lib
LIBDIRS+=$(JAVA_DIR)/lib 

# Linker flags
LDFLAGS = /DLL /NOLOGO /OPT:NOREF /OPT:NOICF /INCREMENTAL:No /MACHINE:X86
ARFLAGS = /NOLOGO

# Libraries
LIBS=advapi32 ws2_32 kernel32 user32 gdi32 winspool comdlg32 shell32 uuid ole32 oleaut32 odbc32 odbccp32

# Compile options used in compile rule
COMPILE=/c $< 
OBJECTFILE = /Fo$(OBJDIR)/$(@F)

# Link option used in link rule
LDFLAGS+=/OUT:$@
ARFLAGS+=/OUT:$@

# Defining includes compiler options
INCLUDES = $(addprefix -I,$(INCDIRS))

# Defining linker library path options
LIBRARIES+= $(addprefix -LIBPATH:,$(LIBDIRS))

# Defining linker library file options
LIBRARIES+= $(addsuffix .lib,$(LIBS))

# Defining target specific library namings
STATIC_LIB_PREFIX=
STATIC_LIB_SUFFIX=.lib
DYNAMIC_LIB_PREFIX=
DYNAMIC_LIB_SUFFIX=.dll

LIB_PREFIX=$(DYNAMIC_LIB_PREFIX)
LIB_SUFFIX=$(DYNAMIC_LIB_SUFFIX)

ifeq '$(TARGET_TYPE)' 'StaticLibrary'
LIB_PREFIX=$(STATIC_LIB_PREFIX)
LIB_SUFFIX=$(STATIC_LIB_SUFFIX)
endif 

CCGNU2_LIB=$(DYNAMIC_LIB_PREFIX)ccgnu2$(DYNAMIC_LIB_SUFFIX)
CCEXT2_LIB=$(DYNAMIC_LIB_PREFIX)ccext2$(DYNAMIC_LIB_SUFFIX)

LOGGING_DIR=$(VIEWSTORE)/ipms_sys/log4cxx/
LOGGING_INCDIR=$(MAS_HOME)/logging/cpp/include
LOGGING_ARCHIVE=$(LOGGING_DIR)/lib/$(STATIC_LIB_PREFIX)log4cxx$(STATIC_LIB_SUFFIX)
LOGGING_LIB=$(DYNAMIC_LIB_PREFIX)log4cxx$(DYNAMIC_LIB_SUFFIX)

# These variables are currently not in use. They are left here
# for SunOS make compabilities.
COMMONCPP2_VERSION=commoncpp2-1.3.22
CCRTP_VERSION=ccrtp-1.3.6
CCGNU2_LIB=$(LIB_PREFIX)ccgnu2$(LIB_SUFFIX)
CCEXT2_LIB=$(LIB_PREFIX)ccext2$(LIB_SUFFIX)

BOOST_THREAD_ARCHIVE=libboost_thread-vc71-mt-1_33_1

QUICKTIME_HOME=$(VIEWSTORE)/ipms_sys/quicktime
QUICKTIME_INCDIR=$(QUICKTIME_HOME)/include
QUICKTIME_ARCHIVE=$(QUICKTIME_HOME)/lib/$(STATIC_LIB_PREFIX)quicktime$(STATIC_LIB_SUFFIX)
