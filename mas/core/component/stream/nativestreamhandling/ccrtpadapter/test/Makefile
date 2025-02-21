# Determing a type name of the targeted system/architecture
include ../../../makefiles/architecture.mk

# Including target specific variables
include ../../../makefiles/variables.$(SYSTEM).mk

#
# In order to use purify invoke clearmake with PURIFY=yes 
# Example: clearmake -C gnu PURIFY=yes
#
ifeq '$(PURIFY)' 'yes' 
  PURIFY=${PURIFY_HOME}/bin/purify
endif

INCL=
INCL+=-I.
INCL+=-I../../3pp/$(COMMONCPP2_VERSION)/include
INCL+=-I../../3pp/ccrtp-1.3.6/src
INCL+=-I$(CPPUNIT)/include 
INCL+=-I../include 
INCL+=-I../../util/include
INCL+=-I../../logging/include
INCL+=-I../../jniadaptermock/include
INCL+=-I../../jniadapter/include
INCL+=-I../../medialibrary/include
INCL+=-I../../medialibrary/include/java
INCL+=-I$(BOOST_INCDIR)
INCL+=-I$(LOGGING_3PP_INCDIR)
INCL+=-I$(JAVA_HOME)/include
INCL+=-I$(JAVA_HOME)/include/linux

SRC_DIR=.
SOURCES=
SOURCES+=testjniutil.cpp
SOURCES+=testutil.cpp 
SOURCES+=recordjobtest.cpp
SOURCES+=ccrtpadapter_tests.cpp

PROGDIR=.
LIBDIR=../../../lib
OBJDIR=obj_sol_8

LIBS=
LIBS+=-L$(LIBDIR) 
LIBS+=-L../../lib
LIBS+=-L$(CPPUNIT)/lib
LIBS+=-L$(LOGGING_3PP_LIBDIR)
LIBS+=-L$(JNI_LIBDIR) 
LIBS+=-lccrtpadapter -lmedialibrary 
LIBS+=-lccrtp -lccgnu2 -llog4cxx
LIBS+=-lstdc++
LIBS+=-lpthread
LIBS+=-lrt -lcppunit -ljvm 

OBJS := $(SOURCES)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

EXT_LIBS = $(LIBDIR)/libmedialibrary.so $(LIBDIR)/libccrtpadapter.so

VPATH+=$(OBJDIR) $(SRC_DIR) $(LIBDIR)

.PHONY: test
.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCL) -c $< -o $(OBJDIR)/$(@F)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCL) -c $< -o $(OBJDIR)/$(@F)

all: dirs $(EXT_LIBS) $(PROGDIR)/ccrtpadaptertest com/mobeon/masp/stream/TestUtil.class 

test: all
	$(PROGDIR)/ccrtpadaptertest

info:
	$(ECHO) $(PURIFY_CMD)

clean:
	-rm -f $(PROGDIR)/ccrtpadaptertest
	-rm -rf $(OBJDIR) $(LIBDIR)/SunWS_cache

com/mobeon/masp/stream/TestUtil.class: com/mobeon/masp/stream/TestUtil.java
	$(JAVA_HOME)/bin/javac -classpath .:../../../../lib/activation.jar:../../../classes com/mobeon/masp/stream/TestUtil.java

rebuild: clean all

$(PROGDIR)/ccrtpadaptertest: $(OBJS)
	$(PURIFY_CMD) $(CXX) -o $@ $(PATHOBJS) $(LIBS)

$(LIBDIR)/libmedialibrary.so:
	cd ../../medialibrary; $(MAKE)

$(LIBDIR)/libccrtpadapter.so:
	cd ..; $(MAKE)

dirs:
	@test -d $(OBJDIR) || mkdir $(OBJDIR)
	@test -d $(PROGDIR) || mkdir $(PROGDIR)
