include ../../../../../mas/stream/makefiles/architecture.mk
include ../../../../../mas/stream/makefiles/variables.$(SYSTEM).mk

INCDIRS=
INCDIRS+=.
INCDIRS+=./include
INCDIRS+=./src

liblog4cpp_SOURCES = AbortAppender.cpp Appender.cpp AppenderSkeleton.cpp BasicConfigurator.cpp BasicLayout.cpp Category.cpp \
	CategoryStream.cpp Configurator.cpp DllMain.cpp DummyThreads.cpp FileAppender.cpp Filter.cpp \
	FixedContextCategory.cpp HierarchyMaintainer.cpp IdsaAppender.cpp LayoutAppender.cpp LoggingEvent.cpp \
	MSThreads.cpp NDC.cpp NTEventLogAppender.cpp OmniThreads.cpp OstreamAppender.cpp PatternLayout.cpp \
	PortabilityImpl.cpp Priority.cpp Properties.cpp PropertyConfigurator.cpp PropertyConfiguratorImpl.cpp \
	PThreads.cpp RemoteSyslogAppender.cpp RollingFileAppender.cpp SimpleConfigurator.cpp SimpleLayout.cpp \
	StringQueueAppender.cpp StringUtil.cpp SyslogAppender.cpp TimeStamp.cpp Win32DebugAppender.cpp \

LIBDIR=../../lib
DISTLIBDIR=$(MAS_HOME)/mas/lib
SRC_DIR=src

VPATH+=$(OBJDIR) $(SRC_DIR)

liblog4cpp_OBJS := $(liblog4cpp_SOURCES)
liblog4cpp_OBJS := $(liblog4cpp_OBJS:%.cpp=%.o)
liblog4cpp_OBJS := ${liblog4cpp_OBJS:%.c=%.o}
liblog4cpp_PATHOBJS := $(addprefix $(OBJDIR)/,$(liblog4cpp_OBJS))

CPPUNIT_LIB=$(LIBDIR)/$(LIB_PREFIX)log4cpp$(LIB_SUFFIX)

CPPUNIT_ARCHIVE=$(LIBDIR)/$(STATIC_LIB_PREFIX)log4cpp$(STATIC_LIB_SUFFIX)

CPPUNIT_DIST_LIB=$(DISTLIBDIR)/$(LIB_PREFIX)log4cpp$(LIB_SUFFIX)

.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)
#$(PURIFY) $(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $(OBJDIR)/$(@F)

all: dirs $(CPPUNIT_ARCHIVE) 

archive: dirs (CPPUNIT_ARCHIVE)

clean:
	$(RM) $(CPPUNIT_LIB)
	$(RM) $(CPPUNIT_ARCHIVE)
	$(RM) $(OBJDIR) 

dist: dirs $(CPPUNIT_DIST_LIB)

distclean: clean
	$(RM) $(CPPUNIT_DIST_LIB)

rebuild: clean all

log4cpp: dirs $(CPPUNIT_LIB)

log4cpp_archive: dirs $(CPPUNIT_ARCHIVE)

$(CPPUNIT_DIST_LIB) : $(CPPUNIT_LIB)
	-$(CP) $< $@

$(CPPUNIT_LIB): $(liblog4cpp_OBJS) 
	$(LD) $(LDFLAGS) $(liblog4cpp_PATHOBJS) $(LIBRARIES)

$(CPPUNIT_ARCHIVE): $(liblog4cpp_OBJS) 
	ar -r $@ $(liblog4cpp_PATHOBJS)

dirs: $(OBJDIR) $(LIBDIR)

$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@
