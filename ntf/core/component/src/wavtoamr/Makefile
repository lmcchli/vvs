# The name of the generated library
OS := $(shell uname -s)
OSVER := $(shell uname -r)

# Including os-specific and os-version specific variables
# The wildcard is just to ensure only existing files are included
include $(wildcard variables.$(OS).*mk) $(wildcard variables.$(OS).$(OSVER).*mk)

# Defining where to find include files
INCDIRS+=include

# Where the source files are found
SRC = $(wildcard src/*.cpp)

# The home of all (our) libraries
LIBDIR=../../lib
LIBS=g7112amr

# Obtaining object file names from source file names
OBJS := $(SRC:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
# Obtaining a list of object files associated with path
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

# Defining where make can find/search files
vpath %.o $(OBJDIR)
vpath %.cpp

# Defining targets which are not files
.PHONY: all clean rebuild dirs

# Targets starts here
all: ../../bin/wavtoamr

echo:
	echo $(SRC)
	echo $(OBJS)
	echo $(OBJDIR)
	echo $(PATHOBJS)
	echo $(OBJECTFILE)
	echo $(COMPILE)

clean:
	$(RM) wavtoamr
	$(RM) $(OBJDIR)

rebuild: clean all

../../bin/wavtoamr: dirs $(PATHOBJS)
	$(LD) -o $@ $(LDFLAGS) $(PATHOBJS) $(LIBRARIES)

dirs: $(OBJDIR)

$(OBJDIR):
	mkdir $@

$(OBJDIR)/%.o: src/%.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

# Implicit rules starts here
.SUFFIXES: .cpp .o

