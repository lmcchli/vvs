# The name of the generated library
#TARGET_TYPE=StaticLibrary
TARGET_NAME=quicktime

ifeq '$(TARGET_VERSION)' ''
	TARGET_VERSION=test
endif

# Determing a type name of the targeted system/architecture
include ../makefiles/architecture.mk

# Including target specific variables
include ../makefiles/variables.$(SYSTEM).mk

#
DISTDIR=delivery/$(TARGET_NAME)/$(TARGET_VERSION)

# Defining where to find include files
INCDIRS+=./include 
#INCDIRS+=../nativestreamhandling/logging/include
#INCDIRS+=../nativestreamhandling/jniadapter/include/
#INCDIRS+=../nativestreamhandling/util/include/
#INCDIRS+=../nativestreamhandling/3pp/commoncpp2-1.3.22/include/cc++/
#INCDIRS+=$(JNI_INCDIRS) 


# Where the soure files are found
SRC_DIR=src

SRC+=Atom.cpp AtomReader.cpp AtomWriter.cpp
SRC+=MdatAtom.cpp
SRC+=MoovAtom.cpp MvhdAtom.cpp TrakAtom.cpp
SRC+=AudioTrackAtom.cpp VideoTrackAtom.cpp HintTrackAtom.cpp
SRC+=AmrTrackAtom.cpp
SRC+=TkhdAtom.cpp MdiaAtom.cpp MdhdAtom.cpp HdlrAtom.cpp TrefAtom.cpp 
SRC+=EdtsAtom.cpp ElstAtom.cpp
SRC+=FtypAtom.cpp
SRC+=MinfAtom.cpp SmhdAtom.cpp VmhdAtom.cpp GmhdAtom.cpp
SRC+=StblAtom.cpp
SRC+=DinfAtom.cpp DrefAtom.cpp
SRC+=StsdAtom.cpp SttsAtom.cpp StssAtom.cpp
SRC+=SampleDescription.cpp
SRC+=AmrSpecificAtom.cpp
SRC+=AmrSampleDescription.cpp
SRC+=H263SampleDescription.cpp
SRC+=PCMSoundSampleDescription.cpp
SRC+=VideoSampleDescription.cpp
SRC+=HintSampleDescription.cpp
SRC+=StscAtom.cpp StszAtom.cpp StcoAtom.cpp

# The home of all (our) libraries
LIBDIR=lib

DELIVERABLES= $(TARGET_NAME)/$(TARGET_VERSION)/lib 
DELIVERABLES+=$(TARGET_NAME)/$(TARGET_VERSION)/include
DELIVERY=$(TARGET_NAME)_$(TARGET_VERSION).tar

# Obtaining object file names from source file names
OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
# Obtaining a list of object files associated with path
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

# Defining where make can find/search files
vpath %.o $(OBJDIR)
vpath %.cpp $(SRC_DIR)

# Defining targets which are not files
.PHONY: dist quicktime archive clean rebuild dirs

# Targets starts here
archive: dirs $(TARGET_ARCHIVE)

quicktime: dirs $(TARGET_LIBRARY) 

clean:
	$(RM) $(TARGET_ARCHIVE)
	$(RM) $(TARGET_LIBRARY)
	$(RM) $(OBJDIR)


# Distribute library/archive
dist: dirs $(DISTDIR) $(TARGET_ARCHIVE)
	#rm -rf delivery/$(TARGET_NAME)
	cp -r $(LIBDIR) delivery/$(TARGET_NAME)/$(TARGET_VERSION)/lib
	cp -r include delivery/$(TARGET_NAME)/$(TARGET_VERSION)/include
	cd delivery && tar cvf $(DELIVERY) $(DELIVERABLES)

distclean: clean
	$(RM) $(DELIVERY)

rebuild: clean all

$(TARGET_LIBRARY): $(OBJS)
	$(LD) $(LDFLAGS) $(PATHOBJS) $(LIBRARIES)

$(TARGET_ARCHIVE): $(OBJS)
	$(AR) $(ARFLAGS) $(PATHOBJS)

$(TARGET_DIST_LIBRARY) : $(TARGET_LIBRARY)
	$(CP) $< $(TARGET_VERSION)/$@

dirs: $(OBJDIR) $(LIBDIR)

info:
	@$(ECHO) "TARGET_TYPE $(TARGET_TYPE)"
	@$(ECHO) "TARGET_LIBRARY $(TARGET_LIBRARY)"
	@$(ECHO) "TARGET_ARCHIVE $(TARGET_ARCHIVE)"
	@$(ECHO) "STREAM_HOME = $(STREAM_HOME)"
	@$(ECHO) "MAS_HOME = $(MAS_HOME)"
	@$(ECHO) "JAVA_HOME = $(JAVA_HOME)"
	@$(ECHO) "LOGGING_3PP_HOME = $(LOGGING_3PP_HOME)"
	@$(ECHO) "LOGGING_3PP_HOME = $(LOGGING_3PP_HOME)"
	@$(ECHO) "QUICKTIME_HOME = $(QUICKTIME_HOME)"
	@$(ECHO) "BOOST_HOME = $(BOOST_HOME)"
	
$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@

$(DISTDIR):
	-mkdir delivery/$(TARGET_NAME)   
	-mkdir delivery/$(TARGET_NAME)/$(TARGET_VERSION)   

# Implicit rules starts here
.SUFFIXES: .cpp .o

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)
