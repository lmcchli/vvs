include ../../../makefiles/architecture.mk
include ../../../makefiles/variables.$(SYSTEM).mk

INCDIRS=
INCDIRS+=.
INCDIRS+=./include
INCDIRS+=./src

libcppunit_SOURCES = AdditionalMessage.cpp Asserter.cpp BeOsDynamicLibraryManager.cpp BriefTestProgressListener.cpp \
	CompilerOutputter.cpp DefaultProtector.cpp DynamicLibraryManager.cpp \
	DynamicLibraryManagerException.cpp Exception.cpp Message.cpp PlugInManager.cpp PlugInParameters.cpp \
	ProtectorChain.cpp Protector.cpp RepeatedTest.cpp ShlDynamicLibraryManager.cpp SourceLine.cpp \
	StringTools.cpp SynchronizedObject.cpp TestAssert.cpp TestCase.cpp TestCaseDecorator.cpp \
	TestComposite.cpp Test.cpp TestDecorator.cpp TestFactoryRegistry.cpp TestFailure.cpp \
	TestLeaf.cpp TestNamer.cpp TestPath.cpp TestPlugInDefaultImpl.cpp TestResultCollector.cpp \
	TestResult.cpp TestRunner.cpp TestSetUp.cpp TestSuccessListener.cpp TestSuiteBuilderContext.cpp \
	TestSuite.cpp TextOutputter.cpp TextTestProgressListener.cpp TextTestResult.cpp \
	TextTestRunner.cpp TypeInfoHelper.cpp UnixDynamicLibraryManager.cpp Win32DynamicLibraryManager.cpp \
	XmlDocument.cpp XmlElement.cpp XmlOutputter.cpp XmlOutputterHook.cpp

LIBDIR=../../lib
DISTLIBDIR=../../../../../mas/lib
SRC_DIR=src/cppunit

VPATH+=$(OBJDIR) $(SRC_DIR)

libcppunit_OBJS := $(libcppunit_SOURCES)
libcppunit_OBJS := $(libcppunit_OBJS:%.cpp=%.o)
libcppunit_OBJS := ${libcppunit_OBJS:%.c=%.o}
libcppunit_PATHOBJS := $(addprefix $(OBJDIR)/,$(libcppunit_OBJS))

CPPUNIT_LIB=$(LIBDIR)/$(LIB_PREFIX)cppunit$(LIB_SUFFIX)

CPPUNIT_ARCHIVE=$(LIBDIR)/$(STATIC_LIB_PREFIX)cppunit$(STATIC_LIB_SUFFIX)

CPPUNIT_DIST_LIB=$(DISTLIBDIR)/$(LIB_PREFIX)cppunit$(LIB_SUFFIX)

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

cppunit: dirs $(CPPUNIT_LIB)

cppunit_archive: dirs $(CPPUNIT_ARCHIVE)

$(CPPUNIT_DIST_LIB) : $(CPPUNIT_LIB)
	-$(CP) $< $@

$(CPPUNIT_LIB): $(libcppunit_OBJS) 
	$(LD) $(LDFLAGS) $(libcppunit_PATHOBJS) $(LIBRARIES)

$(CPPUNIT_ARCHIVE): $(libcppunit_OBJS) 
	ar -r $@ $(libcppunit_PATHOBJS)

dirs: $(OBJDIR) $(LIBDIR)

$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@
