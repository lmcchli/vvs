# Determing a type name of the targeted system/architecture
include ../../../makefiles/architecture.mk

# Including target specific variables
include ../../../makefiles/variables.$(SYSTEM).mk

# The name of the generated library
TARGET_NAME=$(BOOST_THREAD_ARCHIVE)

# Defining where to find include files
#INCDIRS+=./include

INCDIRS+=$(BOOST_INCDIR)

# Where the soure files are found
SRC_DIR=
SRC_DIR+=$(BOOST_THREAD_SRC)


# The medialibrary source files
#SRC+=tss.cpp mutex.cpp exceptions.cpp once.cpp
SRC+=$(notdir $(wildcard $(SRC_DIR)/*.cpp))

# The home of all (our) libraries
LIBDIR=../../lib

ifeq '$(SYSTEM)' 'win32'
STATIC_LIB_PREFIX=lib
endif

# Obtaining object file names from source file names
OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
# Obtaining a list of object files associated with path
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

#TARGET_DIST_LIBRARY = $(TARGET_ARCHIVE)

# Defining where make can find/search files
vpath %.o $(OBJDIR)
vpath %.cpp $(SRC_DIR)

include ../../../makefiles/libraryrules.mk
