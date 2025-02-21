EXECUTABLE=tester

OBJDIR=obj_sol.5.10
SRC_DIR=.

INCDIRS+=.
INCDIRS+=../log4cxx-0.9.7/include
INCLUDES = $(addprefix -I,$(INCDIRS))

SRC=
SRC+=main.cpp

PROGDIR=.
LIBDIR=../../lib

OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

//PATHOBJS+=../lib/liblog4cxx.a

LIBDIRS += ../lib

LIBS += log4cxx
LIBS += socket
LIBS += nsl

LIBRARIES+= $(addprefix -L,$(LIBDIRS))
LIBRARIES+= $(addprefix -l,$(LIBS))

VPATH+=$(OBJDIR) $(SRC_DIR)

.PHONY: all dirs
.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

.cpp.o:
	$(PURIFY_CMD) $(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $(OBJDIR)/$(@F)


all: dirs $(EXECUTABLE)

$(EXECUTABLE): $(OBJS)
	$(PURIFY_CMD) $(CXX)  -o $@ -pthreads $(PATHOBJS) $(LIBRARIES)

dirs: $(OBJDIR)

$(OBJDIR):
	mkdir $@

