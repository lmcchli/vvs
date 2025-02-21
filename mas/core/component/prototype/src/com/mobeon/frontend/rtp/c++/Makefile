
TARGETOS = $(shell uname)

SRC=$(shell ls *.cpp) 

ifeq ($(TARGETOS), Linux)
#POSIX
OSNAME=linux
TARGET=librtplib.so
SRC += $(shell ls pthread/*.cpp)

JAVADIR=/usr/local/jdk1.5.0_02
CCC=g++
LD=g++

STD_INC=/usr/local/include
LIBDIRS = /usr/lib /usr/local/lib
CFLAGS += -g -O2 -pthread
LDFLAGS = -shared $(foreach libdir,$(LIBDIRS),-L$(libdir)) 
LIBS =-lpthread
DEFINES=
PATHSEP=:
COMPILE_CMD=$(CCC) -c -o $@ $(CFLAGS)
LD_CMD=$(LD) -o $@ $(CFLAGS) $(LDFLAGS) 

else
#WIN32 (MSVC++)
OSNAME=win32
TARGET=rtplib.dll
SRC += $(shell ls win32/*.cpp)

JAVADIR=c:/j2sdk1.4.2_04
CCC=c:/vc++/bin/cl
LD=c:/vc++/bin/link
STD_INC=c:/vc++/include c:/msdk/include
LIBDIRS = c:/vc++/lib c:/msdk/lib
LIBS = advapi32.lib ws2_32.lib
DEFINES=WIN32  _LIB
PATHSEP=;
COMPILE_CMD=$(CCC) -c -Fo$@ $(CFLAGS)
LD_CMD=$(LD) -OUT:$@ $(LDFLAGS)
endif
TARGET_DIR=../../../../../..


OBJ=$(SRC:.cpp=.o)

JAVACLASS=../RTPStream.java

JAVA_NATIVE_INTERFACE=com.mobeon.frontend.rtp.RTPStream
JAVA_NATIVE_INTERFACE_H=$(subst .,_,com.mobeon.frontend.rtp.RTPStream).h



JAVAH=$(JAVADIR)/bin/javah
JAVAC=$(JAVADIR)/bin/javac
CLASSPATH="../../../../../../classes$(PATHSEP)../../../../..$(PATHSEP)../../../../../../lib/log4j-1.2.9.jar"

INCDIRS = . $(JAVADIR)/include $(JAVADIR)/include/$(OSNAME) $(STD_INC)
CFLAGS=$(foreach def,$(DEFINES), -D$(def)) $(foreach inc,$(INCDIRS), -I$(inc))


ifeq ($(TARGETOS), Linux)

else
#WIN32 (MSVC++)
CFLAGS += /EHsc /MT

LDFLAGS = -DLL $(foreach lib,$(LIBDIRS),-LIBPATH:$(lib))
endif

.PHONY: all jni clean

all: $(TARGET)
	cp $(TARGET) $(TARGET_DIR)
	@echo "Done!"

info:
	@echo "JNI = $(JAVA_NATIVE_INTERFACE_H)"
jni:
	$(JAVAC) -classpath $(CLASSPATH) $(JAVACLASS)
	$(JAVAH) -classpath $(CLASSPATH) $(JAVA_NATIVE_INTERFACE)

clean:
	-@rm $(OBJ) $(TARGET) $(subst .,_,$(JAVA_NATIVE_INTERFACE)).h *~ 


$(JAVA_NATIVE_INTERFACE_H): jni

$(TARGET) : $(JAVA_NATIVE_INTERFACE_H) $(OBJ)
	$(LD_CMD) $(OBJ) $(LIBS)

%.o : %.cpp
	$(COMPILE_CMD) $< 