VOB_HOME=../../../..
# Determing a type name of the targeted system/architecture
include ../../makefiles/architecture.mk

# Including target specific variables
include ../../makefiles/variables.$(SYSTEM).mk

# The name of the generated library
TARGET_NAME=medialibrary

# Some specific defines
ifeq '$(SYSTEM)' 'win32'
#DEFS+=-DMEDIALIBRARY_EXPORTS
endif

# Enabler for debug print outs
#DEFS+=-DMOV_PRINT_DEBUG

# Defining where to find include files
INCDIRS+=./include
INCDIRS+=../util/include
INCDIRS+=../logging/include
INCDIRS+=../jniadapter/include
INCDIRS+=$(LOGGING_INCDIR)

INCDIRS+=$(QUICKTIME_INCDIR)
ifeq '$(SYSTEM)' 'win32'
INCDIRS+=$(COMMONCPP2_DIR)/w32
INCDIRS+=$(COMMONCPP2_DIR)/w32/cc++
endif
INCDIRS+=$(COMMONCPP2_DIR)/include
INCDIRS+=$(JNI_INCDIRS)
INCDIRS+=$(STD_INCDIRS)
INCDIRS+=$(BOOST_INCDIR)

# Where the soure files are found
SRC_DIR=src

# The medialibrary source files
SRC+=mediaobject.cpp
SRC+=mediaobjectreader.cpp mediaobjectwriter.cpp
SRC+=mediaparser.cpp
SRC+=wavreader.cpp wavparser.cpp wavbuilder.cpp
SRC+=mediabuffer.cpp byteutilities.cpp wavinfo.cpp
SRC+=movparser.cpp movreader.cpp
SRC+=movinfo.cpp movtrackinfo.cpp movrtppacketinfo.cpp 
SRC+=movrtppacket.cpp
SRC+=movbuilder.cpp movwriter.cpp
SRC+=movaudiochunk.cpp movaudiochunkcontainer.cpp
SRC+=rawparser.cpp
SRC+=amrrtpbuilder.cpp amrparser.cpp amrwbparser.cpp 
SRC+=amrbuilder.cpp amrwbbuilder.cpp
SRC+=formathandlerfactory.cpp
SRC+=mediahandler.cpp
SRC+=amrinfo.cpp amrwbinfo.cpp
SRC+=amrtrackinfo.cpp

# The home of all (our) libraries
LIBDIR=../lib
DISTLIBDIR=../../../../mas/lib

ifeq '$(SYSTEM)' 'win32'

LIB_PREFIX=
LIB_SUFFIX=.lib

# Depends upon these libraries
LIBS+=libccgnu2 
else
# These libraries are not for win32
# Depends upon these libraries
LIBS+=log4cxx
LIBS+=ccgnu2 
LIBS+=util
endif

#ifeq '$(SYSTEM)' 'SunOS.5.8'
LIBS+=pthread 
LIBS+=Cstd 
LIBS+=rt
#endif

# Obtaining object file names from source file names
OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
# Obtaining a list of object files associated with path
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

TARGET_DIST_LIBRARY = $(TARGET_ARCHIVE)

# Defining where make can find/search files
vpath %.o $(OBJDIR)
vpath %.cpp $(SRC_DIR)

include ../../makefiles/libraryrules.mk
