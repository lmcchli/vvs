VOB_HOME=../../../..
# Determing a type name of the targeted system/architecture
include ../../makefiles/architecture.mk

# Including target specific variables
include ../../makefiles/variables.$(SYSTEM).mk

# The name of the generated library
TARGET_NAME=util

# Defining where to find include files
INCDIRS+=./include
INCDIRS+=../jniadapter/include
INCDIRS+=../logging/include
INCDIRS+=$(JNI_INCDIRS)
INCDIRS+=$(LOGGING_INCDIR)
INCDIRS+=$(BOOST_INCDIR)
INCDIRS+=$(RTSAFE_INCDIR)
INCDIRS+=$(COMMONCPP_INCDIR)
INCDIRS+=$(CCRTP_INCDIR)

# Where the soure files are found
SRC_DIR=src

# The medialibrary source files
SRC+=platform.cpp stackexception.cpp
SRC+=rtpblockhandler.cpp
SRC+=sessiondescription.cpp
SRC+=mediadescription.cpp
SRC+=mediaenvelope.cpp
SRC+=backtrace.cpp

# The home of all (our) libraries
LIBDIR=../lib

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
