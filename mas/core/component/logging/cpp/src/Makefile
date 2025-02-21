#CXX=CC -c
#LD=CC
CXX=gcc -c
LD=gcc

LIBDIR=../lib
DESTDIR=../../../lib

LOG4CPP_LIB=-L$(LIBDIR) -llog4cpp

CXXFLAGS=-g -O2  -I../include

all: 3pp archive

3pp:
	cd ../3pp/log4cpp-0.3.5rc3 ; make

archive: $(LIBDIR)/liblogger.so

ilogger.o: ilogger.cpp
	$(CXX) -fPIC $(CXXFLAGS) ilogger.cpp -o ilogger.o

$(LIBDIR)/liblogger.so: ilogger.o
	$(LD) -g -D_REENTRANT -fPIC -shared $(LOG4CPP_LIB) -lpthread -o $(LIBDIR)/liblogger.so ilogger.o
	cp $(LIBDIR)/liblogger.so $(DESTDIR)

clean:
	-rm -f ilogger.o $(LIBDIR)/liblogger.so $(DESTDIR)/liblogger.so
