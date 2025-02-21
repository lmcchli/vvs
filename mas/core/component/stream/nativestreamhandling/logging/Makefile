VOB_HOME=../../../..
# Determing a type name of the targeted system/architecture
include ../../makefiles/architecture.mk

# Including target specific variables
include ../../makefiles/variables.$(SYSTEM).mk

# The name of the generated library
TARGET_NAME=logging

# Defining where to find include files
INCDIRS+=./include
INCDIRS+=../util/include
INCDIRS+=../jniadapter/include
INCDIRS+=$(JNI_INCDIRS)
INCDIRS+=$(BOOST_INCDIR)
INCDIRS+=$(LOGGING_INCDIR)
INCDIRS+=$(COMMONCPP_INCDIR)

# Where the soure files are found
SRC_DIR=src

# The medialibrary source files
#SRC+=logger.cpp
SRC+=jlogger.cpp

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
