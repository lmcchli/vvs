# Determing a type name of the targeted system/architecture
include ../makefiles/architecture.mk

# Including target specific variables
include ../makefiles/variables.$(SYSTEM).mk

TARGET_NAME=log4cxx
TARGET_LIBRARY_NAME=$(DYNAMIC_LIB_PREFIX)$(TARGET_NAME)$(DYNAMIC_LIB_SUFFIX)
TARGET_ARCHIVE_NAME=$(STATIC_LIB_PREFIX)$(TARGET_NAME)$(STATIC_LIB_SUFFIX)
TARGET_LIBRARY=$(LIBDIR)/$(TARGET_LIBRARY_NAME)
TARGET_ARCHIVE=$(LIBDIR)/$(TARGET_ARCHIVE_NAME)

TARGET_DIST_LIBRARY=$(DISTLIBDIR)/$(TARGET_LIBRARY_NAME)
TARGET_DIST_ARCHIVE=$(DISTLIBDIR)/$(TARGET_ARCHIVE_NAME)

TARGET_FILES=$(TARGET_LIBRARY)
TARGET_FILES+=$(TARGET_ARCHIVE)

#DEFS += -DHAVE_CONFIG_H
#DEFS += -D_REENTRANT

ifdef REALTIME 
ifeq '$(PLATFORM)' 'solaris'
DEFS+=-DSOLARIS_RTSAFE_LOCKS
REALTIME_SUFFIX=-rt
endif
endif

ifndef TARGET_VERSION
TARGET_3PP_VERSION=$(LOG4CXX_VERSION)$(REALTIME_SUFFIX)
TARGET_VERSION=$(TARGET_3PP_VERSION)/P1A.000
endif

.PHONY=depended-on library test buldArchive buildTest clean dist distclean

LOG4CXX_VERSION=0.11.0
LOG4CXX_ROOT=log4cxx-$(LOG4CXX_VERSION)

# Defining where to find include files
INCDIRS+=$(LOG4CXX_ROOT)/include

LIBS=pthread stdc++ rt

INCLUDES = $(addprefix -I,$(INCDIRS))
LIBRARIES+= $(addprefix -L,$(LIBDIRS))
LIBRARIES+= $(addprefix -l,$(LIBS))

# Where the soure files are found
SRC_DIR=$(LOG4CXX_ROOT)/src

# The ccrtpadapter source files
SRC+=$(notdir $(wildcard $(SRC_DIR)/*.cpp))
ifeq '$(SYSTEM)' 'win32'
SRC+=static.cpp
endif

# The home of all (our) libraries
OBJDIR=$(SYSTEM)
LIBDIR=./lib$(REALTIME_SUFFIX)
DISTLIBDIR=/proj/ipms/log4cxx/$(TARGET_VERSION)

# Obtaining object file names from source file names
OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)

# Obtaining a list of object files associated with path
PATHOBJS =$(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

# Defining where make can find/search files
vpath %.o $(OBJDIR)
vpath %.cpp $(SRC_DIR)
ifeq '$(SYSTEM)' 'win32'
vpath %.cpp $(LOG4CXX_ROOT)/msvc/static
endif


# Conditional all and clean based on vob/proj dependency
ifeq '$(DEPEND_ON_VOB_ONLY)' 'yes'

all: archive library

clean: 
	$(RM) $(PATHOBJS)
	$(RM) $(TARGET_FILES)
else

all: archive library
clean: 
	$(RM) $(PATHOBJS)
	$(RM) $(TARGET_FILES)
endif


archive: dirs $(TARGET_ARCHIVE) 

library: dirs $(TARGET_LIBRARY) 

info:
	@$(ECHO) "SRC_DIR:             $(SRC_DIR)"
	@$(ECHO) "SRC:                 $(SRC)"
	@$(ECHO) "OBJDIR:              $(OBJDIR)"
	@$(ECHO) "OBJS:                $(OBJS)"
	@$(ECHO) "TARGET_LIBRARY: $(TARGET_LIBRARY)"
	@$(ECHO) "TARGET_ARCHIVE: $(TARGET_ARCHIVE)"
	@$(ECHO) "TARGET_DIST_LIBRARY: $(TARGET_DIST_LIBRARY)"
	@$(ECHO) "TARGET_DIST_ARCHIVE: $(TARGET_DIST_ARCHIVE)"
	@$(ECHO) "LIBDIR:              $(LIBDIR)"
	@$(ECHO) "STREAM_HOME = $(STREAM_HOME)"
	@$(ECHO) "MAS_HOME = $(MAS_HOME)"
	@$(ECHO) "JAVA_HOME = $(JAVA_HOME)"
	@$(ECHO) "LOGGING_3PP_HOME = $(LOGGING_3PP_HOME)"
	@$(ECHO) "LOGGING_3PP_HOME = $(LOGGING_3PP_HOME)"
	@$(ECHO) "QUICKTIME_HOME = $(QUICKTIME_HOME)"
	@$(ECHO) "BOOST_HOME = $(BOOST_HOME)"

# Distribute library/archive
dist: dirs $(TARGET_DIST_LIBRARY) $(TARGET_DIST_ARCHIVE)
        #cp -r $(LIBDIR) delivery/$(TARGET_NAME)/$(TARGET_3PP_VERSION)/$(TARGET_VERSION)/lib
        #cp -r include delivery/$(TARGET_NAME)/$(TARGET_3PP_VERSION)/$(TARGET_VERSION)/include
        #cd delivery && tar cvf $(DELIVERY) $(DELIVERABLES)

distclean: clean
	$(RM) $(TARGET_DIST_ARCHIVE) 
	$(RM) $(TARGET_DIST_LIBRARY) 


$(TARGET_LIBRARY): $(OBJS)
	$(LD) $(LDFLAGS) $(PATHOBJS) $(LIBRARIES)

$(TARGET_ARCHIVE): $(OBJS)
	$(AR) $(ARFLAGS) $(PATHOBJS)

$(TARGET_DIST_LIBRARY): $(TARGET_LIBRARY)
	$(CP) $< $@

$(TARGET_DIST_ARCHIVE): $(TARGET_ARCHIVE)
	$(CP) $< $@

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

