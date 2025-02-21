#VOB_HOME=../../../../..
include ../../../makefiles/architecture.mk
include ../../../makefiles/variables.$(SYSTEM).mk

TARGET_NAME=ccrtp

ifeq '$(SYSTEM)' 'SunOS.5.8'
DEFS+=-DBSD_COMP
endif

ifeq '$(SYSTEM)' 'win32'
LDFLAGS=$(LIBLDFLAGS)
endif

INCDIRS+=./src
INCDIRS+=../$(COMMONCPP2_VERSION)/include
INCDIRS+=../../util/include
INCDIRS+=$(BOOST_INCDIR)

SRC=control.cpp data.cpp duplex.cpp incqueue.cpp members.cpp
SRC+=outqueue.cpp pool.cpp queue.cpp rtcppkt.cpp rtppkt.cpp socket.cpp source.cpp
SRC+=outgoingrtppkt.cpp
SRC+=outgoingrtppktlink.cpp

LIBS+=ccgnu2 

LIBDIR=../../lib
DISTLIBDIR=$(MAS_HOME)/lib
SRCDIR=src

OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
PATHOBJS := $(addprefix $(OBJDIR)/,$(OBJS))

VPATH+=$(OBJDIR) $(SRCDIR)

.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)
#$(PURIFY) $(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $(OBJDIR)/$(@F)

all: dirs $(TARGET_ARCHIVE) 

archive: dirs $(TARGET_ARCHIVE) 

clean:
	$(RM) $(TARGET_LIBRARY)
	$(RM) $(TARGET_ARCHIVE)
	$(RM) $(OBJDIR)

dist: dirs $(TARGET_DIST_LIBRARY)

distclean: clean
	$(RM) $(TARGET_DIST_LIBRARY)

rebuild: clean all

$(TARGET_LIBRARY): $(OBJS)
	$(LD) $(LDFLAGS) $(PATHOBJS) $(LIBRARIES)

$(TARGET_ARCHIVE): $(OBJS)
	ar -r $@ $(PATHOBJS)

$(TARGET_DIST_LIBRARY) : $(TARGET_LIBRARY)
	-$(CP) $< $@
	@$(ECHO) "-(CP) < @      = -$(CP) $< $@"

dirs: $(OBJDIR) $(LIBDIR)

$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@
	
ls:
	@ls -al  $(TARGET_LIBRARY) $(TARGET_ARCHIVE) $(TARGET_DIST_LIBRARY)
    
info:
	@$(ECHO) "TARGET_LIBRARY      = $(TARGET_LIBRARY)"
	@$(ECHO) "TARGET_ARCHIVE      = $(TARGET_ARCHIVE)"
	@$(ECHO) "TARGET_DIST_LIBRARY = $(TARGET_DIST_LIBRARY)"
	@$(ECHO) "PATHOBJS = $(PATHOBJS)"
	@$(ECHO) "OBJDIR LIBDIR = $(OBJDIR) $(LIBDIR)"
	@$(ECHO) "OBJS      = $(OBJS)"
	@$(ECHO) "LD      = $(LD)"
	@$(ECHO) "LDFLAGS = $(LDFLAGS)"
	@$(ECHO) "PATHOBJS = $(PATHOBJS)"
	@$(ECHO) "LIBRARIES = $(LIBRARIES)"
