# Determing a type name of the targeted system/architecture
include ../../../makefiles/architecture.mk

# Including target specific variables
include ../../../makefiles/variables.$(SYSTEM).mk

#
# In order to use purify invoke clearmake with PURIFY=yes 
# Example: clearmake -C gnu PURIFY=yes
#
ifeq '$(PURIFY)' 'yes'
DEFS+=-DPURIFY
endif

INCL=
INCL+=-I.
INCL+=-I../../3pp/$(COMMONCPP2_VERSION)/include
INCL+=-I$(CPPUNIT)/include 
INCL+=-I../include/java
INCL+=-I../include 
INCL+=-I$(QUICKTIME_INCDIR)
INCL+=-I$(JAVA_HOME)/include
INCL+=-I$(JAVA_HOME)/include/linux
INCL+=-I$(LOGGING_3PP_INCDIR)
INCL+=-I../../util/include/
INCL+=-I../../logging/include/
INCL+=-I../../jniadapter/include/
INCL+=-I$(BOOST_INCDIR)

SRC_DIR=.
SOURCES=
SOURCES+=testjniutil.cpp
SOURCES+=testutil.cpp 
SOURCES+=mediabuffertest.cpp
SOURCES+=mediaobjecttest.cpp 
SOURCES+=medialibrary_tests.cpp
SOURCES+=mediaobjectreadertest.cpp
SOURCES+=mediaobjectwritertest.cpp
SOURCES+=wavreadertest.cpp 
SOURCES+=wavparsertest.cpp
SOURCES+=byteutilitiestest.cpp 
SOURCES+=movreadertest.cpp 
SOURCES+=movparsertest.cpp
SOURCES+=movbuildertest.cpp
SOURCES+=movfile.cpp
#SOURCES+=quicktimetest.cpp
SOURCES+=movaudiochunkcontainertest.cpp

PROGDIR=.
LIBDIR=../../lib

LIBS=
LIBS+=-L$(LIBDIR) 
LIBS+=-L$(CPPUNIT)/lib
LIBS+=-L$(JNI_LIBDIR)
LIBS+=-L$(QUICKTIME_LIBDIR)
LIBS+=-L$(LOGGING_3PP_LIBDIR)
LIBS+=-lccrtpadapter -lccgnu2 -llogging  -lboostthread
LIBS+=-lpthread -lmedialibrary -lstdc++ -lquicktime -llog4cxx -ljniadapter
LIBS+=-lrt -lcppunit -ljvm  -lutil

OBJS := $(SOURCES)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

EXT_LIBS = $(LIBDIR)/libmedialibrary.a $(LIBDIR)/libccrtpadapter.so
#EXT_LIBS = $(LIBDIR)/libmedialibrary.so

VPATH+=$(OBJDIR) $(SRC_DIR) $(LIBDIR)

.PHONY: test
.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCL) -c $< -o $(OBJDIR)/$(@F)

.cpp.o:
	$(PURIFY_CMD) $(CXX) $(CXXFLAGS) $(INCL) -c $< -o $(OBJDIR)/$(@F)

all: dirs $(EXT_LIBS) $(PROGDIR)/medialibrarytest

debug-test: all
	@$(ECHO) set environment LD_LIBRARY_PATH $(LOGGING_3PP_LIBDIR):$(JNI_LIBDIR):$(LIBDIR) > $(PROGDIR)/config.gdb
	@$(ECHO) set environment DEFAULT_VMOPTIONS="-Djava.class.path=$(MAS_HOME)/stream/classes:$(MAS_HOME)/lib/activation.jar:$(MAS_HOME)/lib/log4j-1.2.9.jar" >>  $(PROGDIR)/config.gdb 
	cd $(PROGDIR);gdb -x config.gdb medialibrarytest

test: all
	LD_LIBRARY_PATH=$(LOGGING_3PP_LIBDIR):$(JNI_LIBDIR):$(LIBDIR) \
	DEFAULT_VMOPTIONS="-Djava.class.path=$(MAS_HOME)/stream/classes:$(MAS_HOME)/lib/activation.jar:$(MAS_HOME)/lib/log4j-1.2.9.jar" \
	$(PROGDIR)/medialibrarytest

info:
	$(ECHO) $(PURIFY_CMD)


clean:
	-rm -f $(PROGDIR)/medialibrarytest

rebuild: clean all

$(PROGDIR)/medialibrarytest: $(OBJS)
	$(PURIFY_CMD) $(CXX) -o $@ $(PATHOBJS) $(LIBS)

$(LIBDIR)/libmedialibrary.a:
	cd ../; $(MAKE) -C gnu 

$(LIBDIR)/libccrtpadapter.so:
	cd ../../ccrtpadapter; $(MAKE) -C gnu

dirs:
	@test -d $(OBJDIR) || mkdir $(OBJDIR)
	@test -d $(PROGDIR) || mkdir $(PROGDIR)
