# Determing a type name of the targeted system/architecture
include ../makefiles/architecture.mk

# Including target specific variables
include ../makefiles/variables.$(SYSTEM).mk

#
# In order to use purify invoke clearmake with PURIFY=yes 
# Example: clearmake -C gnu PURIFY=yes
#

INCDIRS+=.
INCDIRS+=../include 
INCDIRS+=$(CPPUNIT)/include 

SRC_DIR=.

SOURCES=
SOURCES+=QuickTimeTestSuite.cpp
SOURCES+=QuickTimeTest.cpp
SOURCES+=MovFile.cpp

PROGRAM=quicktimetestsuite
PROGDIR=.
LIBDIR=../lib

LIBS+=quicktime
LIBS+=cppunit

OBJS := $(SOURCES)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

VPATH+=$(OBJDIR) $(SRC_DIR) $(LIBDIR)

.PHONY: test
.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) -c $< -o $(OBJDIR)/$(@F)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $(OBJDIR)/$(@F)

all: dirs $(EXT_LIBS) $(PROGDIR)/$(PROGRAM)

test: all
	$(PROGDIR)/$(PROGRAM)

info:
	$(ECHO) $(PURIFY_CMD)


clean:
	-rm -f $(PROGDIR)/$(PROGRAM)
	-rm -rf $(OBJDIR) $(LIBDIR)/SunWS_cache

rebuild: clean all

$(PROGDIR)/$(PROGRAM): $(OBJS)
	$(PURIFY_CMD) $(CXX) -o $@ $(PATHOBJS) $(LIBRARIES)

dirs:
	@test -d $(OBJDIR) || mkdir $(OBJDIR)
	@test -d $(PROGDIR) || mkdir $(PROGDIR)
