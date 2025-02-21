include ../../../makefiles/architecture.mk
include ../../../makefiles/variables.$(SYSTEM).mk

INCDIRS=
INCDIRS+=.
INCDIRS+=./include
INCDIRS+=./src
INCDIRS+=/usr/include/libxml2

libccext2_SOURCES = numbers.cpp zstream.cpp socketport.cpp \
	url.cpp xml.cpp persist.cpp engine.cpp digest.cpp cmdoptns.cpp \
	date.cpp md5.cpp unix.cpp network.cpp serial.cpp urlstring.cpp \
	mime.cpp tokenizer.cpp getopt.c getopt1.c

libccgnu2_SOURCES = thread.cpp mutex.cpp semaphore.cpp threadkey.cpp \
	friends.cpp event.cpp slog.cpp dir.cpp file.cpp inaddr.cpp \
	peer.cpp timer.cpp socket.cpp strchar.cpp simplesocket.cpp \
	mempager.cpp keydata.cpp dso.cpp exception.cpp missing.cpp \
	process.cpp in6addr.cpp buffer.cpp lockfile.cpp \
	nat.cpp runlist.cpp assoc.cpp

LIBDIR=../../lib
DISTLIBDIR=../../../../../mas/lib
SRC_DIR=src

VPATH+=$(OBJDIR) $(SRC_DIR)

libccext2_OBJS := $(libccext2_SOURCES)
libccext2_OBJS := $(libccext2_OBJS:%.cpp=%.o)
libccext2_OBJS := ${libccext2_OBJS:%.c=%.o}
libccext2_PATHOBJS := $(addprefix $(OBJDIR)/,$(libccext2_OBJS))

libccgnu2_OBJS := $(libccgnu2_SOURCES)
libccgnu2_OBJS := ${libccgnu2_OBJS:%.cpp=%.o}
libccgnu2_OBJS := ${libccgnu2_OBJS:%.c=%.o}
libccgnu2_PATHOBJS := $(addprefix $(OBJDIR)/,$(libccgnu2_OBJS))


CCGNU2_LIB=$(LIBDIR)/$(LIB_PREFIX)ccgnu2$(LIB_SUFFIX)
CCEXT2_LIB=$(LIBDIR)/$(LIB_PREFIX)ccext2$(LIB_SUFFIX)

CCGNU2_ARCHIVE=$(LIBDIR)/$(STATIC_LIB_PREFIX)ccgnu2$(STATIC_LIB_SUFFIX)
CCEXT2_ARCHIVE=$(LIBDIR)/$(STATIC_LIB_PREFIX)ccext2$(STATIC_LIB_SUFFIX)

CCGNU2_DIST_LIB=$(DISTLIBDIR)/$(LIB_PREFIX)ccgnu2$(LIB_SUFFIX)
CCEXT2_DIST_LIB=$(DISTLIBDIR)/$(LIB_PREFIX)ccext2$(LIB_SUFFIX)

.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)
#$(PURIFY) $(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $(OBJDIR)/$(@F)

all: dirs $(CCGNU2_LIB) $(CCEXT2_LIB)

archive: dirs $(CCGNU2_ARCHIVE) $(CCEXT2_ARCHIVE)

clean:
	$(RM) $(CCGNU2_LIB) $(CCEXT2_LIB)
	$(RM) $(CCGNU2_ARCHIVE) $(CCEXT2_ARCHIVE)
	$(RM) $(OBJDIR) 

dist: dirs $(CCGNU2_DIST_LIB) $(CCEXT2_DIST_LIB)

distclean: clean
	$(RM) $(CCGNU2_DIST_LIB) $(CCEXT2_DIST_LIB)

rebuild: clean all

ccext2: dirs $(CCEXT2_LIB)
ccgnu2: dirs $(CCGNU2_LIB)

ccext2_archive: dirs $(CCEXT2_ARCHIVE)
ccgnu2_archive: dirs $(CCGNU2_ARCHIVE)

$(CCGNU2_DIST_LIB) : $(CCGNU2_LIB)
	-$(CP) $< $@

$(CCEXT2_DIST_LIB) : $(CCEXT2_LIB)
	-$(CP) $< $@

$(CCEXT2_LIB): $(libccext2_OBJS) 
	$(LD) $(LDFLAGS) $(libccext2_PATHOBJS) $(LIBRARIES)

$(CCGNU2_LIB): $(libccgnu2_OBJS) 
	$(LD) $(LDFLAGS) $(libccgnu2_PATHOBJS) $(LIBRARIES)

$(CCEXT2_ARCHIVE): $(libccext2_OBJS) 
	ar -r $@ $(libccext2_PATHOBJS)

$(CCGNU2_ARCHIVE): $(libccgnu2_OBJS) 
	ar -r $@ $(libccgnu2_PATHOBJS)

dirs: $(OBJDIR) $(LIBDIR)

$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@
