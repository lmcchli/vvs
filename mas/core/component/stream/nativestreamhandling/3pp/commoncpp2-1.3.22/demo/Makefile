# Makefile.in generated automatically by automake 1.4-p6 from Makefile.am

# Copyright (C) 1994, 1995-8, 1999, 2001 Free Software Foundation, Inc.
# This Makefile.in is free software; the Free Software Foundation
# gives unlimited permission to copy and/or distribute it,
# with or without modifications, as long as this notice is preserved.

# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY, to the extent permitted by law; without
# even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE.

# Copyright (C) 1999-2005 Open Source Telecom Corporation.
#
# This file is free software; as a special exception the author gives
# unlimited permission to copy and/or distribute it, with or without
# modifications, as long as this notice is preserved.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY, to the extent permitted by law; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.


SHELL = /bin/sh

srcdir = .
top_srcdir = ..

prefix = /usr/local
exec_prefix = ${prefix}

bindir = ${exec_prefix}/bin
sbindir = ${exec_prefix}/sbin
libexecdir = ${exec_prefix}/libexec
datadir = ${prefix}/share
sysconfdir = ${prefix}/etc
sharedstatedir = ${prefix}/com
localstatedir = ${prefix}/var
libdir = ${exec_prefix}/lib
infodir = ${prefix}/info
mandir = ${prefix}/man
includedir = ${prefix}/include
oldincludedir = /usr/include

DESTDIR =

pkgdatadir = $(datadir)/commoncpp2
pkglibdir = $(libdir)/commoncpp2
pkgincludedir = $(includedir)/commoncpp2

top_builddir = ..

ACLOCAL = /home/mikael/eIII/mas/stream/nativestreamhandling/3pp/commoncpp2-1.3.22/autoconf/missing aclocal-1.4
AUTOCONF = /home/mikael/eIII/mas/stream/nativestreamhandling/3pp/commoncpp2-1.3.22/autoconf/missing autoconf
AUTOMAKE = /home/mikael/eIII/mas/stream/nativestreamhandling/3pp/commoncpp2-1.3.22/autoconf/missing automake-1.4
AUTOHEADER = /home/mikael/eIII/mas/stream/nativestreamhandling/3pp/commoncpp2-1.3.22/autoconf/missing autoheader

INSTALL = /usr/bin/install -c
INSTALL_PROGRAM = ${INSTALL} $(AM_INSTALL_PROGRAM_FLAGS)
INSTALL_DATA = ${INSTALL} -m 644
INSTALL_SCRIPT = ${INSTALL}
transform = s,x,x,

NORMAL_INSTALL = :
PRE_INSTALL = :
POST_INSTALL = :
NORMAL_UNINSTALL = :
PRE_UNINSTALL = :
POST_UNINSTALL = :
build_alias = 
build_triplet = x86_64-unknown-linux-gnu
host_alias = 
host_triplet = x86_64-unknown-linux-gnu
target_alias = 
target_triplet = x86_64-unknown-linux-gnu
AR = ar
AS = @AS@
BASE_LIB = 
CC = gcc
CCXX_DIR = $(top_srcdir)/include
COMMON_FLAGS = -D_GNU_SOURCE
CPP = gcc -E
CPPUNIT_LIBS =  
CXX = g++
CXXCPP = g++ -E
DLLTOOL = @DLLTOOL@
DOXYGEN = no
DYN_LOADER = -ldl
ECHO = echo
EGREP = grep -E
EXEEXT = 
F77 = 
FTPDIR = 
GCJ = @GCJ@
GCJFLAGS = @GCJFLAGS@
GETOPT_LIBS = 
HAVE_LIB = @HAVE_LIB@
KDOC_DIR = $(top_srcdir)/doc
LIB = @LIB@
LIBGETOPTOBJS = 
LIBTOOL = $(SHELL) $(top_builddir)/libtool
LN_S = ln -s
LTLIB = @LTLIB@
LT_ = @LT_@
LT_MAJOR = 1
LT_MINOR = 18
LT_RELEASE = 1.3
LT_SUBVER = 1.18
MAINT = #
MAKEINFO = /home/mikael/eIII/mas/stream/nativestreamhandling/3pp/commoncpp2-1.3.22/autoconf/missing makeinfo
MODULE_FLAGS = -module -shared
OBJDUMP = @OBJDUMP@
OBJEXT = o
PACKAGE = commoncpp2
PTHREAD_CC = 
RANLIB = ranlib
RC = @RC@
SHARED_FLAGS = 
SOCKET_LIBS = 
STAGE2 = 
STRIP = strip
THREAD_FLAGS = 
THREAD_LIBS =   -lrt -pthread
VERSION = 1.3.22
WARN_FLAGS = -pedantic -Wall
WINVERSION = 1,3,22,0
XML2_CONFIG = no
XML_CONFIG = no
XML_FLAGS = 
XML_LIBS = 
ZSTREAM_LIBS = 
ccincludedir = ${includedir}/cc++2
datadir = ${prefix}/share
incprefix = 
infodir = ${prefix}/info
localstatedir = ${prefix}/var
mandir = ${prefix}/man
ost_cv_dynloader = yes
prefix = /usr/local
sysconfdir = ${prefix}/etc
thrprefix = /usr/include

MAINTAINERCLEANFILES = Makefile.in Makefile

EXTRA_DIST = README test.xml Makefile.bcc
AM_CXXFLAGS = $(THREAD_FLAGS) -pedantic -Wall
INCLUDES = -I$(top_srcdir)/include
LDADD = ../src/libccgnu2.la $(THREAD_LIBS) $(DYN_LOADER)
Z_LIBS = -lz

noinst_PROGRAMS = tcp tcpthread tcpservice serial dir str buffer keydump 	urlfetch xmlfetch portsample netdevices slogTest crc32 cmdlineopt

noinst_HEADERS = serialecho.h SampleSocketPort.h

urlfetch_SOURCES = urlfetch.cpp
urlfetch_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

xmlfetch_SOURCES = xmlfetch.cpp
xmlfetch_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

buffer_SOURCES = buffer.cpp
buffer_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

cmdlineopt_SOURCES = cmdlineopt.cpp
cmdlineopt_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

slogTest_SOURCES = slogTest.cpp

dir_SOURCES = dir.cpp

keydump_SOURCES = keydump.cpp

str_SOURCES = str.cpp
str_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

tcp_SOURCES = tcp.cpp

tcpthread_SOURCES = tcpthread.cpp

tcpservice_SOURCES = tcpservice.cpp
tcpservice_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

portsample_SOURCES = SampleSocketPort.cpp
portsample_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

netdevices_SOURCES = netdevices.cpp
netdevices_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

serial_SOURCES = serialmain.cpp serialecho.cpp
serial_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)

crc32_SOURCES = crc32.cpp
crc32_LDADD = ../src/libccext2.la $(XML_LIBS) $(Z_LIBS) $(LDADD)
mkinstalldirs = $(SHELL) $(top_srcdir)/autoconf/mkinstalldirs
CONFIG_HEADER = ../include/config.h
CONFIG_CLEAN_FILES = 
noinst_PROGRAMS =  tcp$(EXEEXT) tcpthread$(EXEEXT) tcpservice$(EXEEXT) \
serial$(EXEEXT) dir$(EXEEXT) str$(EXEEXT) buffer$(EXEEXT) \
keydump$(EXEEXT) urlfetch$(EXEEXT) xmlfetch$(EXEEXT) \
portsample$(EXEEXT) netdevices$(EXEEXT) slogTest$(EXEEXT) \
crc32$(EXEEXT) cmdlineopt$(EXEEXT)
PROGRAMS =  $(noinst_PROGRAMS)


DEFS = -DHAVE_CONFIG_H -I. -I$(srcdir) -I../include
CPPFLAGS = 
LDFLAGS = 
LIBS =  -L/lib
tcp_OBJECTS =  tcp.$(OBJEXT)
tcp_LDADD = $(LDADD)
tcp_DEPENDENCIES =  ../src/libccgnu2.la
tcp_LDFLAGS = 
tcpthread_OBJECTS =  tcpthread.$(OBJEXT)
tcpthread_LDADD = $(LDADD)
tcpthread_DEPENDENCIES =  ../src/libccgnu2.la
tcpthread_LDFLAGS = 
tcpservice_OBJECTS =  tcpservice.$(OBJEXT)
tcpservice_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
tcpservice_LDFLAGS = 
serial_OBJECTS =  serialmain.$(OBJEXT) serialecho.$(OBJEXT)
serial_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
serial_LDFLAGS = 
dir_OBJECTS =  dir.$(OBJEXT)
dir_LDADD = $(LDADD)
dir_DEPENDENCIES =  ../src/libccgnu2.la
dir_LDFLAGS = 
str_OBJECTS =  str.$(OBJEXT)
str_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
str_LDFLAGS = 
buffer_OBJECTS =  buffer.$(OBJEXT)
buffer_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
buffer_LDFLAGS = 
keydump_OBJECTS =  keydump.$(OBJEXT)
keydump_LDADD = $(LDADD)
keydump_DEPENDENCIES =  ../src/libccgnu2.la
keydump_LDFLAGS = 
urlfetch_OBJECTS =  urlfetch.$(OBJEXT)
urlfetch_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
urlfetch_LDFLAGS = 
xmlfetch_OBJECTS =  xmlfetch.$(OBJEXT)
xmlfetch_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
xmlfetch_LDFLAGS = 
portsample_OBJECTS =  SampleSocketPort.$(OBJEXT)
portsample_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
portsample_LDFLAGS = 
netdevices_OBJECTS =  netdevices.$(OBJEXT)
netdevices_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
netdevices_LDFLAGS = 
slogTest_OBJECTS =  slogTest.$(OBJEXT)
slogTest_LDADD = $(LDADD)
slogTest_DEPENDENCIES =  ../src/libccgnu2.la
slogTest_LDFLAGS = 
crc32_OBJECTS =  crc32.$(OBJEXT)
crc32_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
crc32_LDFLAGS = 
cmdlineopt_OBJECTS =  cmdlineopt.$(OBJEXT)
cmdlineopt_DEPENDENCIES =  ../src/libccext2.la ../src/libccgnu2.la
cmdlineopt_LDFLAGS = 
CXXFLAGS =  -g -O2
CXXCOMPILE = $(CXX) $(DEFS) $(INCLUDES) $(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CXXFLAGS) $(CXXFLAGS)
LTCXXCOMPILE = $(LIBTOOL) --mode=compile $(CXX) $(DEFS) $(INCLUDES) $(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CXXFLAGS) $(CXXFLAGS)
CXXLD = $(CXX)
CXXLINK = $(LIBTOOL) --mode=link $(CXXLD) $(AM_CXXFLAGS) $(CXXFLAGS) $(LDFLAGS) -o $@
HEADERS =  $(noinst_HEADERS)

DIST_COMMON =  README Makefile.am Makefile.in


DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS) $(TEXINFOS) $(EXTRA_DIST)

TAR = tar
GZIP_ENV = --best
SOURCES = $(tcp_SOURCES) $(tcpthread_SOURCES) $(tcpservice_SOURCES) $(serial_SOURCES) $(dir_SOURCES) $(str_SOURCES) $(buffer_SOURCES) $(keydump_SOURCES) $(urlfetch_SOURCES) $(xmlfetch_SOURCES) $(portsample_SOURCES) $(netdevices_SOURCES) $(slogTest_SOURCES) $(crc32_SOURCES) $(cmdlineopt_SOURCES)
OBJECTS = $(tcp_OBJECTS) $(tcpthread_OBJECTS) $(tcpservice_OBJECTS) $(serial_OBJECTS) $(dir_OBJECTS) $(str_OBJECTS) $(buffer_OBJECTS) $(keydump_OBJECTS) $(urlfetch_OBJECTS) $(xmlfetch_OBJECTS) $(portsample_OBJECTS) $(netdevices_OBJECTS) $(slogTest_OBJECTS) $(crc32_OBJECTS) $(cmdlineopt_OBJECTS)

all: all-redirect
.SUFFIXES:
.SUFFIXES: .S .c .cpp .lo .o .obj .s
$(srcdir)/Makefile.in: # Makefile.am $(top_srcdir)/configure.ac $(ACLOCAL_M4) 
	cd $(top_srcdir) && $(AUTOMAKE) --gnu --include-deps demo/Makefile

Makefile: $(srcdir)/Makefile.in  $(top_builddir)/config.status
	cd $(top_builddir) \
	  && CONFIG_FILES=$(subdir)/$@ CONFIG_HEADERS= $(SHELL) ./config.status


mostlyclean-noinstPROGRAMS:

clean-noinstPROGRAMS:
	-test -z "$(noinst_PROGRAMS)" || rm -f $(noinst_PROGRAMS)

distclean-noinstPROGRAMS:

maintainer-clean-noinstPROGRAMS:

.c.o:
	$(COMPILE) -c $<

# FIXME: We should only use cygpath when building on Windows,
# and only if it is available.
.c.obj:
	$(COMPILE) -c `cygpath -w $<`

.s.o:
	$(COMPILE) -c $<

.S.o:
	$(COMPILE) -c $<

mostlyclean-compile:
	-rm -f *.o core *.core
	-rm -f *.$(OBJEXT)

clean-compile:

distclean-compile:
	-rm -f *.tab.c

maintainer-clean-compile:

.c.lo:
	$(LIBTOOL) --mode=compile $(COMPILE) -c $<

.s.lo:
	$(LIBTOOL) --mode=compile $(COMPILE) -c $<

.S.lo:
	$(LIBTOOL) --mode=compile $(COMPILE) -c $<

mostlyclean-libtool:
	-rm -f *.lo

clean-libtool:
	-rm -rf .libs _libs

distclean-libtool:

maintainer-clean-libtool:

tcp$(EXEEXT): $(tcp_OBJECTS) $(tcp_DEPENDENCIES)
	@rm -f tcp$(EXEEXT)
	$(CXXLINK) $(tcp_LDFLAGS) $(tcp_OBJECTS) $(tcp_LDADD) $(LIBS)

tcpthread$(EXEEXT): $(tcpthread_OBJECTS) $(tcpthread_DEPENDENCIES)
	@rm -f tcpthread$(EXEEXT)
	$(CXXLINK) $(tcpthread_LDFLAGS) $(tcpthread_OBJECTS) $(tcpthread_LDADD) $(LIBS)

tcpservice$(EXEEXT): $(tcpservice_OBJECTS) $(tcpservice_DEPENDENCIES)
	@rm -f tcpservice$(EXEEXT)
	$(CXXLINK) $(tcpservice_LDFLAGS) $(tcpservice_OBJECTS) $(tcpservice_LDADD) $(LIBS)

serial$(EXEEXT): $(serial_OBJECTS) $(serial_DEPENDENCIES)
	@rm -f serial$(EXEEXT)
	$(CXXLINK) $(serial_LDFLAGS) $(serial_OBJECTS) $(serial_LDADD) $(LIBS)

dir$(EXEEXT): $(dir_OBJECTS) $(dir_DEPENDENCIES)
	@rm -f dir$(EXEEXT)
	$(CXXLINK) $(dir_LDFLAGS) $(dir_OBJECTS) $(dir_LDADD) $(LIBS)

str$(EXEEXT): $(str_OBJECTS) $(str_DEPENDENCIES)
	@rm -f str$(EXEEXT)
	$(CXXLINK) $(str_LDFLAGS) $(str_OBJECTS) $(str_LDADD) $(LIBS)

buffer$(EXEEXT): $(buffer_OBJECTS) $(buffer_DEPENDENCIES)
	@rm -f buffer$(EXEEXT)
	$(CXXLINK) $(buffer_LDFLAGS) $(buffer_OBJECTS) $(buffer_LDADD) $(LIBS)

keydump$(EXEEXT): $(keydump_OBJECTS) $(keydump_DEPENDENCIES)
	@rm -f keydump$(EXEEXT)
	$(CXXLINK) $(keydump_LDFLAGS) $(keydump_OBJECTS) $(keydump_LDADD) $(LIBS)

urlfetch$(EXEEXT): $(urlfetch_OBJECTS) $(urlfetch_DEPENDENCIES)
	@rm -f urlfetch$(EXEEXT)
	$(CXXLINK) $(urlfetch_LDFLAGS) $(urlfetch_OBJECTS) $(urlfetch_LDADD) $(LIBS)

xmlfetch$(EXEEXT): $(xmlfetch_OBJECTS) $(xmlfetch_DEPENDENCIES)
	@rm -f xmlfetch$(EXEEXT)
	$(CXXLINK) $(xmlfetch_LDFLAGS) $(xmlfetch_OBJECTS) $(xmlfetch_LDADD) $(LIBS)

portsample$(EXEEXT): $(portsample_OBJECTS) $(portsample_DEPENDENCIES)
	@rm -f portsample$(EXEEXT)
	$(CXXLINK) $(portsample_LDFLAGS) $(portsample_OBJECTS) $(portsample_LDADD) $(LIBS)

netdevices$(EXEEXT): $(netdevices_OBJECTS) $(netdevices_DEPENDENCIES)
	@rm -f netdevices$(EXEEXT)
	$(CXXLINK) $(netdevices_LDFLAGS) $(netdevices_OBJECTS) $(netdevices_LDADD) $(LIBS)

slogTest$(EXEEXT): $(slogTest_OBJECTS) $(slogTest_DEPENDENCIES)
	@rm -f slogTest$(EXEEXT)
	$(CXXLINK) $(slogTest_LDFLAGS) $(slogTest_OBJECTS) $(slogTest_LDADD) $(LIBS)

crc32$(EXEEXT): $(crc32_OBJECTS) $(crc32_DEPENDENCIES)
	@rm -f crc32$(EXEEXT)
	$(CXXLINK) $(crc32_LDFLAGS) $(crc32_OBJECTS) $(crc32_LDADD) $(LIBS)

cmdlineopt$(EXEEXT): $(cmdlineopt_OBJECTS) $(cmdlineopt_DEPENDENCIES)
	@rm -f cmdlineopt$(EXEEXT)
	$(CXXLINK) $(cmdlineopt_LDFLAGS) $(cmdlineopt_OBJECTS) $(cmdlineopt_LDADD) $(LIBS)
.cpp.o:
	$(CXXCOMPILE) -c $<
.cpp.obj:
	$(CXXCOMPILE) -c `cygpath -w $<`
.cpp.lo:
	$(LTCXXCOMPILE) -c $<

tags: TAGS

ID: $(HEADERS) $(SOURCES) $(LISP)
	list='$(SOURCES) $(HEADERS)'; \
	unique=`for i in $$list; do echo $$i; done | \
	  awk '    { files[$$0] = 1; } \
	       END { for (i in files) print i; }'`; \
	here=`pwd` && cd $(srcdir) \
	  && mkid -f$$here/ID $$unique $(LISP)

TAGS:  $(HEADERS) $(SOURCES)  $(TAGS_DEPENDENCIES) $(LISP)
	tags=; \
	here=`pwd`; \
	list='$(SOURCES) $(HEADERS)'; \
	unique=`for i in $$list; do echo $$i; done | \
	  awk '    { files[$$0] = 1; } \
	       END { for (i in files) print i; }'`; \
	test -z "$(ETAGS_ARGS)$$unique$(LISP)$$tags" \
	  || (cd $(srcdir) && etags -o $$here/TAGS $(ETAGS_ARGS) $$tags  $$unique $(LISP))

mostlyclean-tags:

clean-tags:

distclean-tags:
	-rm -f TAGS ID

maintainer-clean-tags:

distdir = $(top_builddir)/$(PACKAGE)-$(VERSION)/$(subdir)

subdir = demo

distdir: $(DISTFILES)
	@for file in $(DISTFILES); do \
	  d=$(srcdir); \
	  if test -d $$d/$$file; then \
	    cp -pr $$d/$$file $(distdir)/$$file; \
	  else \
	    test -f $(distdir)/$$file \
	    || ln $$d/$$file $(distdir)/$$file 2> /dev/null \
	    || cp -p $$d/$$file $(distdir)/$$file || :; \
	  fi; \
	done

info-am:
info: info-am
dvi-am:
dvi: dvi-am
check-am: all-am
check: check-am
installcheck-am:
installcheck: installcheck-am
install-exec-am:
install-exec: install-exec-am

install-data-am:
install-data: install-data-am

install-am: all-am
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am
install: install-am
uninstall-am:
uninstall: uninstall-am
all-am: Makefile $(PROGRAMS) $(HEADERS)
all-redirect: all-am
install-strip:
	$(MAKE) $(AM_MAKEFLAGS) AM_INSTALL_PROGRAM_FLAGS=-s install
installdirs:


mostlyclean-generic:

clean-generic:

distclean-generic:
	-rm -f Makefile $(CONFIG_CLEAN_FILES)
	-rm -f config.cache config.log stamp-h stamp-h[0-9]*

maintainer-clean-generic:
	-test -z "$(MAINTAINERCLEANFILES)" || rm -f $(MAINTAINERCLEANFILES)
mostlyclean-am:  mostlyclean-noinstPROGRAMS mostlyclean-compile \
		mostlyclean-libtool mostlyclean-tags \
		mostlyclean-generic

mostlyclean: mostlyclean-am

clean-am:  clean-noinstPROGRAMS clean-compile clean-libtool clean-tags \
		clean-generic mostlyclean-am

clean: clean-am

distclean-am:  distclean-noinstPROGRAMS distclean-compile \
		distclean-libtool distclean-tags distclean-generic \
		clean-am
	-rm -f libtool

distclean: distclean-am

maintainer-clean-am:  maintainer-clean-noinstPROGRAMS \
		maintainer-clean-compile maintainer-clean-libtool \
		maintainer-clean-tags maintainer-clean-generic \
		distclean-am
	@echo "This command is intended for maintainers to use;"
	@echo "it deletes files that may require special tools to rebuild."

maintainer-clean: maintainer-clean-am

.PHONY: mostlyclean-noinstPROGRAMS distclean-noinstPROGRAMS \
clean-noinstPROGRAMS maintainer-clean-noinstPROGRAMS \
mostlyclean-compile distclean-compile clean-compile \
maintainer-clean-compile mostlyclean-libtool distclean-libtool \
clean-libtool maintainer-clean-libtool tags mostlyclean-tags \
distclean-tags clean-tags maintainer-clean-tags distdir info-am info \
dvi-am dvi check check-am installcheck-am installcheck install-exec-am \
install-exec install-data-am install-data install-am install \
uninstall-am uninstall all-redirect all-am all installdirs \
mostlyclean-generic distclean-generic clean-generic \
maintainer-clean-generic clean mostlyclean distclean maintainer-clean


# Tell versions [3.59,3.63) of GNU make to not export all variables.
# Otherwise a system limit (for SysV at least) may be exceeded.
.NOEXPORT:
