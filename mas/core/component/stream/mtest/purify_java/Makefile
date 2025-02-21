# Before executing set the library path:
#setenv LD_LIBRARY_PATH 
# $JAVA_HOME/jre/lib/sparc/client:$JAVA_HOME/jre/lib/sparc/:
# $JAVA_HOME/jre/lib/sparc/native_threads:`pwd`

# Determing a type name of the targeted system/architecture
include	../../makefiles/architecture.mk

# Including target specific variables
include ../../makefiles/variables.$(SYSTEM).mk

JAVAC=$(JAVA_HOME)/bin/javac

INCDIRS+=$(JNI_INCDIRS)

LIBS+=jvm
LIBS+=thread

all: purifyjava

purifyjava: purifyjava.c
	$(PURIFY_CMD) $(CC) -g $< $(INCLUDES) $(LIBRARIES) -o $@


# Implicit rules starts here
#.SUFFIXES: .java .class .c .cpp .o
#
#.c.o:
#	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)#
#
#.cpp.o:
#	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)
#
#.java.class:
#	$(JAVAC) $(JCOMPILE) $(JCLASSFILE)
