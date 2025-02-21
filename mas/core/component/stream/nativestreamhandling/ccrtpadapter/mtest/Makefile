# Determing a type name of the targeted system/architecture
include ../../../makefiles/architecture.mk

# Including target specific variables
include ../../../makefiles/variables.$(SYSTEM).mk

# Internal dependencies
PACKAGE_ROOT=../..
MEDIALIBRARYHOME=$(PACKAGE_ROOT)/medialibrary
CCRTPADAPTERHOME=$(PACKAGE_ROOT)/ccrtpadapter
JNIADAPTERHOME=  $(PACKAGE_ROOT)/jniadaptermock
UTILHOME=        $(PACKAGE_ROOT)/util
LOGGINGHOME=     $(PACKAGE_ROOT)/logging

CCRTPADAPTER_ARCHIVE = $(LIBDIR)/$(STATIC_LIB_PREFIX)ccrtpadapter$(STATIC_LIB_SUFFIX)
MEDIALIBRARY_ARCHIVE = $(LIBDIR)/$(STATIC_LIB_PREFIX)medialibrary$(STATIC_LIB_SUFFIX)
JNIADAPTER_ARCHIVE =   $(LIBDIR)/$(STATIC_LIB_PREFIX)jniadaptermock$(STATIC_LIB_SUFFIX)
LOGGER_ARCHIVE =       $(LIBDIR)/$(STATIC_LIB_PREFIX)logging$(STATIC_LIB_SUFFIX)
UTIL_ARCHIVE =         $(LIBDIR)/$(STATIC_LIB_PREFIX)util$(STATIC_LIB_SUFFIX)

#
# In order to use purify invoke clearmake with PURIFY=yes 
# Example: clearmake -C gnu PURIFY=yes
#
# You must also initialize the purify environment
# source /usr/local/rational/purifyplus_setup.csh
#

# Some specific defines
ifeq '$(SYSTEM)' 'SunOS.5.10'
DEFS+=-DBSD_COMP
endif
ifeq '$(SYSTEM)' 'SunOS.5.8'
DEFS+=-DBSD_COMP
endif

INCDIRS+=.
INCDIRS+=../include 
ifeq '$(SYSTEM)' 'win32'
INCDIRS+=$(COMMONCPP2_DIR)/w32
INCDIRS+=$(COMMONCPP2_DIR)/w32/cc++
endif
INCDIRS+=$(COMPONENT_INCDIRS)
INCDIRS+=$(CPPUNIT)/include 
INCDIRS+=../../3pp/${COMMONCPP2_VERSION}/include
INCDIRS+=../../3pp/${CCRTP_VERSION}/src 
INCDIRS+=../../ccrtpadapter/include
INCDIRS+=../../medialibrary/include
INCDIRS+=../../interface
INCDIRS+=$(JNI_INCDIRS)
INCDIRS+=$(STD_INCDIRS)
INCDIRS+=$(BOOST_INCDIR)
INCDIRS+=$(PACKAGE_ROOT)/jniadapter/include
INCDIRS+=$(JNIADAPTERHOME)/include
INCDIRS+=$(UTILHOME)/include
INCDIRS+=$(LOGGINGHOME)/include
INCDIRS+=$(LOGGING_INCDIR)
INCDIRS+=$(RTSAFE_INCDIR)

SRC_DIR=.
SOURCES=
SOURCES+=main.cpp ccrtpsessionproxy.cpp nativestreamhandling.cpp
SOURCES+=RtpBlockHandlerTest.cpp MockRtpPacket.cpp
SOURCES+=CallbackQueueHandlerTest.cpp CallbackQueueTest.cpp 
SOURCES+=InboundStreamTest.cpp OutboundStreamTest.cpp
SOURCES+=OutputProcessorTest.cpp
SOURCES+=NativeInterfaceTest.cpp

PURIFY_OPTIONS+=-log-file=stream.purify.log
PURIFY_OPTIONS+= -windows=no
#PURIFY_OPTIONS+= -best-effort


PROGDIR=.
LIBDIR=../../lib

ifneq '$(SYSTEM)' 'win32'
#LIBS=
#LIBS+=ccrtpadapter
#LIBS+=ccrtp ccgnu2
#LIBS+=pthread socket
#LIBS+=glib
#LIBS+=rt
#LIBS+=socket
#LIBS+=nsl
#LIBS+=rt
#LIBS+=g2c
#LIBS+=gcc_s
else
LIBS+=ccrtpadapter medialibrary
endif

LIBS+=cppunit
LIBRARIES+=-llog4cxx -lquicktime -ljniadaptermock
LIBDIRS+=$(CPPUNIT)/lib 

OBJS := $(SOURCES)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))


EXT_LIBS = $(LIBDIR)/libmedialibrary.so $(LIBDIR)/libccrtpadapter.so
CCRTPADAPTER = $(LIBDIR)/$(LIB_PREFIX)ccrtpadapter$(LIB_SUFFIX)
MEDIALIBRARY = $(LIBDIR)/$(LIB_PREFIX)medialibrary$(LIB_SUFFIX)
CCRTPADAPTER_ARCHIVE = $(LIBDIR)/$(STATIC_LIB_PREFIX)ccrtpadapter$(STATIC_LIB_SUFFIX)
MEDIALIBRARY_ARCHIVE = $(LIBDIR)/$(STATIC_LIB_PREFIX)medialibrary$(STATIC_LIB_SUFFIX)
CCGNU2_ARCHIVE = $(LIBDIR)/$(STATIC_LIB_PREFIX)ccgnu2$(STATIC_LIB_SUFFIX)
CCEXT2_ARCHIVE = $(LIBDIR)/$(STATIC_LIB_PREFIX)ccext2$(STATIC_LIB_SUFFIX)
CCRTP_ARCHIVE = $(LIBDIR)/$(STATIC_LIB_PREFIX)ccrtp$(STATIC_LIB_SUFFIX)

MEDIALIBRARYHOME=../../medialibrary
CCRTPADAPTERHOME=../../ccrtpadapter

WINDOWS_EXECUTABLE=$(PROGDIR)/mtest.exe
SOLARIS_EXECUTABLE=$(PROGDIR)/mtest

ifeq '$(SYSTEM)' 'win32'
EXECUTABLE=$(WINDOWS_EXECUTABLE)
else
EXECUTABLE=$(SOLARIS_EXECUTABLE)
PATHOBJS += $(CCRTPADAPTER_ARCHIVE)
PATHOBJS += $(MEDIALIBRARY_ARCHIVE)
PATHOBJS += $(QUICKTIME_ARCHIVE)
PATHOBJS += $(CCRTP_ARCHIVE) $(CCGNU2_ARCHIVE) $(CCEXT2_ARCHIVE) 
PATHOBJS+= $(QUICKTIME_ARCHIVE)
#PATHOBJS += $(LOGGING_DIR)/$(LOGGING_ARCHIVE)
PATHOBJS += $(LOGGER_ARCHIVE)
PATHOBJS += $(JNIADAPTER_ARCHIVE)
PATHOBJS += $(UTIL_ARCHIVE)
PATHOBJS += $(LIBDIR)/libboostthread.a
endif

VPATH+=$(OBJDIR) $(SRC_DIR) ../../interface # $(LIBDIR)


.PHONY: all dirs test inco clean 
.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)
#	$(PURIFY_CMD) $(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

all: dirs $(EXECUTABLE)

debug-test: all
	@$(ECHO) set environment LD_LIBRARY_PATH $(LOGGING_3PP_LIBDIR):$(JNI_LIBDIR) > $(PROGDIR)/config.gdb
	@$(ECHO) set args 0 localhost 23000 >> $(PROGDIR)/config.gdb
	cd $(PROGDIR);gdb -x config.gdb $(EXECUTABLE)

valgrind-test-play: all
	LD_LIBRARY_PATH=$(LOGGING_3PP_LIBDIR) valgrind  --leak-check=full --log-file=$(PROGDIR)/valgrind.log $(EXECUTABLE)  1 localhost 7878

debug_prereq:
	@$(ECHO) set environment LD_LIBRARY_PATH $(LOGGING_3PP_LIBDIR):$(JNI_LIBDIR) > $(PROGDIR)/config.gdb

debug-test-play: all debug_prereq
	@$(ECHO) set args 1 localhost 7878 >> $(PROGDIR)/config.gdb
	cd $(PROGDIR);gdb -x config.gdb $(EXECUTABLE)

debug-test-recv: all debug_prereq
	@$(ECHO) set args 3 localhost 7878 >> $(PROGDIR)/config.gdb
	cd $(PROGDIR);gdb -x config.gdb $(EXECUTABLE)

test-play: all
	LD_LIBRARY_PATH=$(LOGGING_3PP_LIBDIR) $(EXECUTABLE)  1 localhost 7878

test-recv: all
	LD_LIBRARY_PATH=$(LOGGING_3PP_LIBDIR) $(EXECUTABLE)  3 localhost 7878

test: all
	LD_LIBRARY_PATH=$(LOGGING_3PP_LIBDIR) $(EXECUTABLE)  0 localhost 23000

info:
	@$(ECHO) Libdir:..... $(LIBDIR)
	@$(ECHO) Objdir:..... $(OBJDIR)
	@$(ECHO) Purify cmd:. $(PURIFY_CMD)

clean:
	-rm -f $(EXECUTABLE)
	rm -rf $(OBJDIR)

clean-all: clean
	cd $(MEDIALIBRARYHOME); $(MAKE) clean
	cd $(CCRTPADAPTERHOME); $(MAKE) clean

rebuild: clean all

#$(SOLARIS_EXECUTABLE): $(MEDIALIBRARY_ARCHIVE) $(CCRTPADAPTER_ARCHIVE) $(OBJS)
$(SOLARIS_EXECUTABLE): $(OBJS)
	$(PURIFY_CMD) $(CXX) -o $@ -pthread -fPIC -static-libgcc -mimpure-text -Wl $(PATHOBJS) $(LIBRARIES)
#	$(PURIFY_CMD) $(CXX) -o $@ -fPIC -pthreads $(PATHOBJS) $(LIBRARIES)

$(WINDOWS_EXECUTABLE):  $(MEDIALIBRARY) $(CCRTPADAPTER)  $(OBJS)
	$(SHLIB_LD)  /OUT:$@ $(PATHOBJS) $(LIBRARIES)
#	$(LD)  /OUT:test.exe $(PATHOBJS) $(LIBRARIES)


$(MEDIALIBRARY):
	cd $(MEDIALIBRARYHOME) && $(MAKE) MOCK_JNI=true

$(CCRTPADAPTER):
	cd $(CCRTPADAPTERHOME) && $(MAKE) 

$(MEDIALIBRARY_ARCHIVE):
	cd $(MEDIALIBRARYHOME) && $(MAKE) MOCK_JNI=true archive

$(CCRTPADAPTER_ARCHIVE):
	cd $(CCRTPADAPTERHOME) && $(MAKE) archive

dirs: $(OBJDIR) $(LIBDIR)
	cd $(JNIADAPTERHOME) && $(MAKE) archive
	cd $(MEDIALIBRARYHOME) && $(MAKE) MOCK_JNI=true archive
  
$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@
