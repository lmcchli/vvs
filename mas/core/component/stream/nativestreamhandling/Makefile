include ../makefiles/architecture.mk
include ../makefiles/variables.$(SYSTEM).mk

TARGET_NAME=ccrtpadapter

.PHONY=library test buldArchive buildTest clean dist distclean

# JNI
# The native implementation of Stream JNI reside in a couple
# of cpp files where the declaration (header files) are
# generated from the corresponding java class files.
# In order to be able to compile C++ you must compile Java
# typically: ant compile.
JAVAC=$(JAVA_HOME)/bin/javac
CLASSPATH=../../target/classes/com/mobeon/masp/stream
NAMESPACE=com.mobeon.masp.stream
TARGET_DIR=../../target

CCRTPSESSIONPROXY_H=ccrtpsessionproxy.h
CCRTPSESSIONPROXY_CLASSPATH=../../target/mas.jar
CCRTPSESSIONPROXY_JAVA=../src/com/mobeon/masp/stream/CCRTPSession.java


NATIVE_STREAM_HANDLING_H=nativestreamhandling.h
NATIVE_STREAM_HANDLING_CLASSPATH=../../target/mas.jar
NATIVE_STREAM_HANDLING_JAVA=../src/com/mobeon/masp/stream/jni/NativeStreamHandling.java

JNI_HEADER_FILES=java_gen_1 java_gen_2

# Defining where to find include files
INCDIRS+=$(BOOST_INCDIR)
INCDIRS+=$(COMPONENT_INCDIRS)
INCDIRS+=./interface
INCDIRS+=$(3PP_DIR)/$(CCRTP_VERSION)/src 
INCDIRS+=./medialibrary/include 
INCDIRS+=./ccrtpadapter/include 
INCDIRS+=./logging/include 
INCDIRS+=./util/include 
INCDIRS+=./jniadapter/include 
INCDIRS+=$(QUICKTIME_INCDIR)
INCDIRS+=$(LOGGING_INCDIR)
INCDIRS+=$(JNI_INCDIRS)

# Where the soure files are found
SRC_DIR=interface

# The ccrtpadapter source files
SRC+=ccrtpsessionproxy.cpp nativestreamhandling.cpp


# The home of all (our) libraries
LIBDIR=../lib
DISTLIBDIR=$(MAS_HOME)/lib

# Obtaining object file names from source file names
OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
# Obtaining a list of object files associated with path
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

ARCHIVES+=ccrtpadapter
ARCHIVES+=medialibrary
ARCHIVES+=jniadapter
ARCHIVES+=util
ARCHIVES+=logging
ARCHIVES+=$(BOOST_THREAD_ARCHIVE)
ARCHIVES+=ccrtp
ARCHIVES+=ccgnu2
ARCHIVES+=ccext2

PATHOBJS += $(addprefix ./lib/$(STATIC_LIB_PREFIX),$(addsuffix $(STATIC_LIB_SUFFIX),$(ARCHIVES)))
#PATHOBJS += ./lib/libccrtp.so

ifeq '$(REALTIME)' 'yes'
REALTIME_FLAG=REALTIME=yes
endif

ifeq '$(DEPEND_ON_VOB_ONLY)' 'yes'
DEPEND_ON_VOB_FLAG=DEPEND_ON_VOB_ONLY=yes
endif

# Defining where make can find/search files
vpath %.o $(OBJDIR)
vpath %.cpp $(SRC_DIR)

ifeq '$(DEPEND_ON_VOB_ONLY)' 'yes'
all: depended-on buildArchives library
else
all: buildArchives library
endif

depended-on:

library: dirs $(JNI_HEADER_FILES) $(TARGET_LIBRARY) 
    

info:
	@$(ECHO) "JNI_HEADER_FILES:     $(JNI_HEADER_FILES)"
	@$(ECHO) "TARGET_LIBRARY:      $(TARGET_LIBRARY)"
	@$(ECHO) "TARGET_DIST_LIBRARY: $(TARGET_DIST_LIBRARY)"
	@$(ECHO) "LIBDIR:              $(LIBDIR)"
	@$(ECHO) " "
	@$(ECHO) "PATHOBJS:     $(PATHOBJS)"
	@$(ECHO) "LIBRARIES:     $(LIBRARIES)"
	@$(ECHO) "OBJS:     $(OBJS)"
	@$(ECHO) "OBJDIR:     $(OBJDIR)"
	@$(ECHO) "TARGET_LIBRARY:	$(TARGET_LIBRARY)"
	@$(ECHO) "MFLAGS:	$(MFLAGS)"
	@$(ECHO) "MAKE:	$(MAKE)"
	@$(ECHO) "STREAM_HOME = $(STREAM_HOME)"
	@$(ECHO) "MAS_HOME = $(MAS_HOME)"
	@$(ECHO) "JAVA_HOME = $(JAVA_HOME)"
	@$(ECHO) "LOGGING_3PP_HOME = $(LOGGING_3PP_HOME)"
	@$(ECHO) "QUICKTIME_HOME = $(QUICKTIME_HOME)"
	@$(ECHO) "BOOST_HOME = $(BOOST_HOME)"
	@$(ECHO) "DEPEND_ON_VOB_ONLY = $(DEPEND_ON_VOB_ONLY)"
	@$(ECHO) "LIBRARIES = $(LIBRARIES)"
	@$(ECHO) "LDFLAGS = $(LDFLAGS)"
	@$(ECHO) "LD = $(LD)"


test: buildTest

dist: all $(TARGET_DIST_LIBRARY)

ifeq '$(DEPEND_ON_VOB_ONLY)' 'yes'
clean: cleanArchives
	$(RM) $(JNI_HEADER_FILES)
	$(RM) $(SYSTEM)
	$(RM) $(TARGET_LIBRARY)
	$(CD) $(QUICKTIME_HOME);make  $(REALTIME_FLAG) $(DEPEND_ON_VOB_FLAG) clean
else
clean: cleanArchives
	$(RM) $(JNI_HEADER_FILES)
	$(RM) $(TARGET_LIBRARY)
endif
distclean: clean
	$(RM) $(TARGET_DIST_LIBRARY) 


buildArchives:
	@$(ECHO) "***"
	@$(ECHO) "*** Building Native Stream Handling for $(SYSTEM)" 
	@$(ECHO) "***"
	@cd $(QUICKTIME_HOME) && $(MAKE) $(MFLAGS)
	@cd 3pp && $(MAKE) $(MFLAGS)
	@cd util && $(MAKE) $(MFLAGS)
	@cd logging && $(MAKE) $(MFLAGS)
	@cd jniadapter && $(MAKE) $(MFLAGS)
	@cd medialibrary && $(MAKE) $(MFLAGS)
	@cd ccrtpadapter && $(MAKE) $(MFLAGS)
	@$(ECHO) "*** Done".

buildTest:
	@$(ECHO) "***"
	@$(ECHO) "*** Building Mocked Native Stream Handling for $(SYSTEM)" 
	@$(ECHO) "***"
	@cd $(QUICKTIME_HOME) && $(MAKE) $(MFLAGS)
	@cd 3pp && $(MAKE) $(MFLAGS) 3pp
	@cd logging && $(MAKE) $(MFLAGS)
	@cd util && $(MAKE) $(MFLAGS)
	@cd jniadaptermock && $(MAKE) $(MFLAGS)
	@cd medialibrary && $(MAKE) $(MFLAGS)
	@cd ccrtpadapter && $(MAKE) $(MFLAGS)
	@$(ECHO) "*** Done."


cleanArchives:
	@$(ECHO) "***"
	@$(ECHO) "*** Cleaning (Mocked) Native Stream Handling for $(SYSTEM)" 
	@$(ECHO) "***"
	@$(ECHO) "Running $(MAKE) $(MFLAGS) clean"
	@cd $(QUICKTIME_HOME) && $(MAKE) $(MFLAGS) clean
	@cd 3pp && $(MAKE) $(MFLAGS) clean
	@cd logging && $(MAKE) $(MFLAGS) clean
	@cd util && $(MAKE) $(MFLAGS) clean
	@cd jniadapter && $(MAKE) $(MFLAGS) clean
	@cd jniadaptermock && $(MAKE) $(MFLAGS) clean
	@cd medialibrary && $(MAKE) $(MFLAGS) clean
	@cd ccrtpadapter && $(MAKE) $(MFLAGS) clean
	@$(ECHO) "*** Done."


$(TARGET_LIBRARY): $(OBJS)
	$(LD) $(LDFLAGS) $(PATHOBJS) $(LIBRARIES)

$(TARGET_ARCHIVE): $(OBJS)
	$(AR) $(ARFLAGS) $(PATHOBJS)

$(TARGET_DIST_LIBRARY): $(TARGET_LIBRARY)
	$(CP) $< $@

java_gen_1:
	$(JAVAC) -classpath $(CCRTPSESSIONPROXY_CLASSPATH) -h interface  $(CCRTPSESSIONPROXY_JAVA) -d ${TARGET_DIR}
	@mv interface/com_mobeon_masp_stream_CCRTPSession.h interface/$(CCRTPSESSIONPROXY_H)

java_gen_2:
	$(JAVAC) -classpath $(NATIVE_STREAM_HANDLING_CLASSPATH) -h interface  $(NATIVE_STREAM_HANDLING_JAVA) -d ${TARGET_DIR}
	@mv interface/com_mobeon_masp_stream_jni_NativeStreamHandling.h interface/$(NATIVE_STREAM_HANDLING_H)

dirs: $(OBJDIR) $(LIBDIR)

$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@

# Implicit rules starts here
.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

