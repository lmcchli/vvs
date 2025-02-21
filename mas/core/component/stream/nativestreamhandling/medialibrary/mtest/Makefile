$()# Determing a type name of the targeted system/architecture
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
INCDIRS+=.
INCDIRS+=../include 
INCDIRS+=$(QUICKTIME_INCDIR)
ifeq '$(SYSTEM)' 'win32'
INCDIRS+=$(COMMONCPP2_DIR)/w32
INCDIRS+=$(COMMONCPP2_DIR)/w32/cc++
endif
INCDIRS+=$(BOOST_INCDIR)
INCDIRS+=$(CPPUNIT)/include 
INCDIRS+=$(COMMONCPP2_DIR)/include
INCDIRS+=$(3PP_DIR)/${CCRTP_VERSION}/src 
INCDIRS+=$(JNI_INCDIRS)
INCDIRS+=$(STD_INCDIRS)
INCDIRS+=$(PACKAGE_ROOT)/jniadapter/include
INCDIRS+=$(JNIADAPTERHOME)/include
INCDIRS+=$(UTILHOME)/include
INCDIRS+=$(LOGGINGHOME)/include
INCDIRS+=$(LOGGING_INCDIR)

SRC_DIR=.

SRC=
SRC+=main.cpp
SRC+=TestJNIUtil.cpp
SRC+=MediaObjectTest.cpp
SRC+=ByteUtilitiesTest.cpp
SRC+=MediaBufferTest.cpp
SRC+=MediaObjectReaderTest.cpp
SRC+=MediaObjectWriterTest.cpp
SRC+=MovAudioChunkContainerTest.cpp
SRC+=MovBuilderTest.cpp
SRC+=MovParserTest.cpp
SRC+=MovReaderTest.cpp
SRC+=WavBuilderTest.cpp
SRC+=WavParserTest.cpp
SRC+=WavReaderTest.cpp
SRC+=AmrParserTest.cpp
SRC+=AmrBuilderTest.cpp
SRC+=MediaHandlerTest.cpp
SRC+=MovFile.cpp
SRC+=TestUtil.cpp
SRC+=TestMedia.cpp


PROGDIR=.
LIBDIR=../../lib

#LIBDIRS+=-L$(CPPUNIT)/lib

ifneq '$(SYSTEM)' 'win32'
#LIBS+=ccrtp ccgnu2
#LIBS+=gthread socket
#LIBS+=glib
#LIBS+=umem
#LIBS+=rt
else
#LIBS+=jniadaptermock ccrtpadapter medialibrary logging
endif
LIBS+=cppunit

LIBDIRS+=$(CPPUNIT)/lib 

OBJS := $(SRC)
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

CCRTPADAPTER = $(LIBDIR)/$(LIB_PREFIX)ccrtpadapter$(LIB_SUFFIX)
MEDIALIBRARY = $(LIBDIR)/$(LIB_PREFIX)medialibrary$(LIB_SUFFIX)

WINDOWS_EXECUTABLE=$(PROGDIR)/medialibrary.exe
SOLARIS_EXECUTABLE=$(PROGDIR)/medialibrary

ifeq '$(SYSTEM)' 'win32'
EXECUTABLE=$(WINDOWS_EXECUTABLE)
else
EXECUTABLE=$(SOLARIS_EXECUTABLE)
endif

PATHOBJS += $(MEDIALIBRARY_ARCHIVE)
PATHOBJS += $(QUICKTIME_ARCHIVE)
PATHOBJS += $(LOGGER_ARCHIVE)
#PATHOBJS += $(LOGGING_ARCHIVE)
PATHOBJS += $(JNIADAPTER_ARCHIVE)
PATHOBJS += $(UTIL_ARCHIVE)
PATHOBJS += $(CCGNU2_ARCHIVE)

ifeq '$(SYSTEM)' 'win32'
else
PATHOBJS += $(LIBDIR)/libboostthread.a
endif



VPATH+=$(OBJDIR) $(SRC_DIR) $(LIBDIR)

.PHONY: all dirs
.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

.cpp.o:
	$(PURIFY_CMD) $(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)


all: dirs $(EXECUTABLE)

test: all
	 $(EXECUTABLE)

info:
	@$(ECHO) Libdir:..... $(LIBDIR)
	@$(ECHO) Objdir:..... $(OBJDIR)
	@$(ECHO) Purify cmd:. $(PURIFY_CMD)
	@$(ECHO) Executable:. $(EXECUTABLE)

clean:
	-rm -f $(EXECUTABLE)
	-rm -rf $(OBJDIR)
	cd $(MEDIALIBRARYHOME) && $(MAKE) -C gnu clean
	cd $(CCRTPADAPTERHOME) && $(MAKE) -C gnu clean
	mkdir $(OBJDIR)

rebuild: clean all

#$(SOLARIS_EXECUTABLE): $(MEDIALIBRARY_ARCHIVE) $(CCRTPADAPTER_ARCHIVE) $(OBJS)

$(SOLARIS_EXECUTABLE): $(OBJS)
	$(PURIFY_CMD) $(CXX)  -o $@ -pthreads $(PATHOBJS) $(LIBRARIES)

$(WINDOWS_EXECUTABLE):  $(MEDIALIBRARY) $(CCRTPADAPTER)  $(OBJS)
	$(LD) /OUT:$@ $(PATHOBJS) $(LIBRARIES)
#	$(SHLIB_LD)  /OUT:$@ $(PATHOBJS) $(LIBRARIES)
#	$(LD)  /OUT:test.exe $(PATHOBJS) $(LIBRARIES)

$(MEDIALIBRARY):
	cd $(MEDIALIBRARYHOME) && $(MAKE) -C gnu MOCK_JNI=true

$(CCRTPADAPTER):
	cd $(CCRTPADAPTERHOME) && $(MAKE) -C gnu

$(MEDIALIBRARY_ARCHIVE):
	cd $(MEDIALIBRARYHOME) && $(MAKE) -C gnu MOCK_JNI=true archive

$(CCRTPADAPTER_ARCHIVE):
	cd $(CCRTPADAPTERHOME) && $(MAKE) -C gnu archive

dirs: $(OBJDIR) $(LIBDIR)

$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@

