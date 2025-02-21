SUNWSPRO=/usr/local/SUNWspro


CXX=/usr/local/SUNWspro/bin/CC
#CXX=g++
#CC=/usr/sfw/bin/gcc
#CXX=/usr/sfw/bin/g++
LD=$(CXX)

# JNI
JAVA_HOME=/usr/local/jdk1.5.0_05
JNI_INCDIRS=
JNI_INCDIRS+=$(JAVA_HOME)/include
JNI_INCDIRS+=$(JAVA_HOME)/include/solaris

#LIBDIRS+=$(JAVA_HOME)/jre/lib/sparc 
