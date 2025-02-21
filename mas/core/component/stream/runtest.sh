#!/bin/ksh

export javaHome="/usr/local/jdk1.5.0_05"
export masHome="/vobs/ipms/mas"
export antHome="/usr/local/ant-1.6.5"

export classPath=${masHome}/stream/classes
export classPath=${classPath}:${masHome}/lib/commons-collections-3.1.jar
export classPath=${classPath}:${masHome}/lib/activation.jar 
export classPath=${classPath}:${masHome}/lib/dom4j-1.6.1.jar 
export classPath=${classPath}:${masHome}/lib/log4j-1.2.9.jar 
export classPath=${classPath}:${antHome}/lib/junit.jar 
export classPath=${classPath}:${masHome}/lib/jmock-1.0.1.jar 

#export LD_LIBRARY_PATH=${masHome}/stream/lib:/usr/lib:/usr/sfw/lib


# Disable default allocator, and use new (up to gcc 3.3 i think)
#export GLIBCPP_FORCE_NEW=1

# Disable default allocator, and use new (newer gcc's)
#export GLIBCXX_FORCE_NEW=1

# Preload umem in case somebody (me) messed-
# up the link-order or, in this case, because
# the JVM might have loadded the wrong library
# before we get loaded
export LD_PRELOAD_32="libumem.so.1"

# If debug given on commandline, enable umem
# memory debugging functions
#if test x"$1" = xdebug ; then
#UMEM_DEBUG="default,firewall=1"
export UMEM_DEBUG="default,audit=19"
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/proj/ipms/log4cxx/0.9.7-rt:/proj/ipms/rtsafe/P1A.000/lib:/vobs/ipms/mas/stream/lib"

#export UMEM_LOGGING=transaction

#UMEM_OPTIONS="backend=mmap"
#export UMEM_OPTIONS

#fi

#shift

${JAVA_HOME}/bin/java -classpath ${classPath} com.mobeon.masp.stream.$1 $2 $3 $4 $5 $6 $7 $8 $9 ${10} ${11} ${12} ${13} ${14} ${15} ${16} 
