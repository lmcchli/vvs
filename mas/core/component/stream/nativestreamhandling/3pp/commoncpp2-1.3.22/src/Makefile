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
EXTRA_DIST = ccgnu2-config.in ccgnu2-config ost_check2.m4 	getopt.h getopt.c getopt1.c *.pc *.pc.in

LT_VERSION = -version-info 1:18
AM_CXXFLAGS = -I../src -DCCXX_EXPORT_LIBRARY $(THREAD_FLAGS)         $(COMMON_FLAGS) $(XML_FLAGS) -I$(CCXX_DIR) $(DEBUG_FLAGS)

AM_CFLAGS = $(THREAD_FLAGS)
#INCLUDES = -I$(CCXX_DIR) 
RELEASE = $(LT_VERSION) -release $(LT_RELEASE)

pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = libccgnu2.pc libccext2.pc
aclocaldir = $(datadir)/aclocal
aclocal_DATA = ost_check2.m4
scriptdir = $(bindir)
script_DATA = ccgnu2-config

lib_LTLIBRARIES = libccgnu2.la libccext2.la

libccgnu2_la_LIBADD =   -lrt -pthread
libccgnu2_la_LDFLAGS = $(RELEASE) $(SHARED_FLAGS)
noinst_DATA = 

libccgnu2_la_SOURCES = thread.cpp mutex.cpp semaphore.cpp threadkey.cpp 	friends.cpp event.cpp slog.cpp dir.cpp file.cpp inaddr.cpp 	peer.cpp timer.cpp socket.cpp strchar.cpp simplesocket.cpp 	mempager.cpp keydata.cpp dso.cpp exception.cpp missing.cpp 	process.cpp string.cpp in6addr.cpp buffer.cpp lockfile.cpp 	nat.cpp runlist.cpp assoc.cpp private.h nat.h


libccext2_la_LIBADD =    -lrt -pthread
libccext2_la_LDFLAGS = $(RELEASE) $(SHARED_FLAGS)

#optincludedir = $(includedir)/cc++2
#optinclude_HEADERS = getopt.h

#libccext2_la_SOURCES = numbers.cpp zstream.cpp socketport.cpp 	url.cpp xml.cpp persist.cpp engine.cpp digest.cpp cmdoptns.cpp 	date.cpp md5.cpp unix.cpp network.cpp serial.cpp urlstring.cpp 	mime.cpp tokenizer.cpp getopt.c getopt1.c
libccext2_la_SOURCES = numbers.cpp zstream.cpp socketport.cpp         url.cpp xml.cpp persist.cpp engine.cpp digest.cpp cmdoptns.cpp         date.cpp md5.cpp unix.cpp network.cpp serial.cpp urlstring.cpp 	tokenizer.cpp mime.cpp
mkinstalldirs = $(SHELL) $(top_srcdir)/autoconf/mkinstalldirs
CONFIG_HEADER = ../include/config.h
CONFIG_CLEAN_FILES =  ccgnu2-config libccext2.pc libccgnu2.pc
LTLIBRARIES =  $(lib_LTLIBRARIES)


DEFS = -DHAVE_CONFIG_H -I. -I$(srcdir) -I../include
CPPFLAGS = 
LDFLAGS = 
LIBS =  -L/lib
libccgnu2_la_DEPENDENCIES = 
libccgnu2_la_OBJECTS =  thread.lo mutex.lo semaphore.lo threadkey.lo \
friends.lo event.lo slog.lo dir.lo file.lo inaddr.lo peer.lo timer.lo \
socket.lo strchar.lo simplesocket.lo mempager.lo keydata.lo dso.lo \
exception.lo missing.lo process.lo string.lo in6addr.lo buffer.lo \
lockfile.lo nat.lo runlist.lo assoc.lo
libccext2_la_DEPENDENCIES = 
libccext2_la_OBJECTS =  numbers.lo zstream.lo \
socketport.lo url.lo xml.lo persist.lo engine.lo \
digest.lo cmdoptns.lo date.lo md5.lo unix.lo \
network.lo serial.lo urlstring.lo tokenizer.lo \
mime.lo
#libccext2_la_OBJECTS =  numbers.lo zstream.lo \
#socketport.lo url.lo xml.lo persist.lo engine.lo \
#digest.lo cmdoptns.lo date.lo md5.lo unix.lo \
#network.lo serial.lo urlstring.lo mime.lo \
#tokenizer.lo getopt.lo getopt1.lo
CXXFLAGS =  -g -O2
CXXCOMPILE = $(CXX) $(DEFS) $(INCLUDES) $(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CXXFLAGS) $(CXXFLAGS)
LTCXXCOMPILE = $(LIBTOOL) --mode=compile $(CXX) $(DEFS) $(INCLUDES) $(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CXXFLAGS) $(CXXFLAGS)
CXXLD = $(CXX)
CXXLINK = $(LIBTOOL) --mode=link $(CXXLD) $(AM_CXXFLAGS) $(CXXFLAGS) $(LDFLAGS) -o $@
CFLAGS = -g -O2
COMPILE = $(CC) $(DEFS) $(INCLUDES) $(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CFLAGS) $(CFLAGS)
LTCOMPILE = $(LIBTOOL) --mode=compile $(CC) $(DEFS) $(INCLUDES) $(AM_CPPFLAGS) $(CPPFLAGS) $(AM_CFLAGS) $(CFLAGS)
CCLD = $(CC)
LINK = $(LIBTOOL) --mode=link $(CCLD) $(AM_CFLAGS) $(CFLAGS) $(LDFLAGS) -o $@
DATA =  $(aclocal_DATA) $(noinst_DATA) $(pkgconfig_DATA) $(script_DATA)

HEADERS =  $(optinclude_HEADERS)

DIST_COMMON =  Makefile.am Makefile.in ccgnu2-config.in libccext2.pc.in \
libccgnu2.pc.in


DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS) $(TEXINFOS) $(EXTRA_DIST)

TAR = tar
GZIP_ENV = --best
SOURCES = $(libccgnu2_la_SOURCES) $(libccext2_la_SOURCES)
OBJECTS = $(libccgnu2_la_OBJECTS) $(libccext2_la_OBJECTS)

all: all-redirect
.SUFFIXES:
.SUFFIXES: .S .c .cpp .lo .o .obj .s
$(srcdir)/Makefile.in: # Makefile.am $(top_srcdir)/configure.ac $(ACLOCAL_M4) 
	cd $(top_srcdir) && $(AUTOMAKE) --gnu --include-deps src/Makefile

Makefile: $(srcdir)/Makefile.in  $(top_builddir)/config.status
	cd $(top_builddir) \
	  && CONFIG_FILES=$(subdir)/$@ CONFIG_HEADERS= $(SHELL) ./config.status

ccgnu2-config: $(top_builddir)/config.status ccgnu2-config.in
	cd $(top_builddir) && CONFIG_FILES=$(subdir)/$@ CONFIG_HEADERS= $(SHELL) ./config.status
libccext2.pc: $(top_builddir)/config.status libccext2.pc.in
	cd $(top_builddir) && CONFIG_FILES=$(subdir)/$@ CONFIG_HEADERS= $(SHELL) ./config.status
libccgnu2.pc: $(top_builddir)/config.status libccgnu2.pc.in
	cd $(top_builddir) && CONFIG_FILES=$(subdir)/$@ CONFIG_HEADERS= $(SHELL) ./config.status

mostlyclean-libLTLIBRARIES:

clean-libLTLIBRARIES:
	-test -z "$(lib_LTLIBRARIES)" || rm -f $(lib_LTLIBRARIES)

distclean-libLTLIBRARIES:

maintainer-clean-libLTLIBRARIES:

install-libLTLIBRARIES: $(lib_LTLIBRARIES)
	@$(NORMAL_INSTALL)
	$(mkinstalldirs) $(DESTDIR)$(libdir)
	@list='$(lib_LTLIBRARIES)'; for p in $$list; do \
	  if test -f $$p; then \
	    echo "$(LIBTOOL)  --mode=install $(INSTALL) $$p $(DESTDIR)$(libdir)/$$p"; \
	    $(LIBTOOL)  --mode=install $(INSTALL) $$p $(DESTDIR)$(libdir)/$$p; \
	  else :; fi; \
	done

uninstall-libLTLIBRARIES:
	@$(NORMAL_UNINSTALL)
	list='$(lib_LTLIBRARIES)'; for p in $$list; do \
	  $(LIBTOOL)  --mode=uninstall rm -f $(DESTDIR)$(libdir)/$$p; \
	done

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

libccgnu2.la: $(libccgnu2_la_OBJECTS) $(libccgnu2_la_DEPENDENCIES)
	$(CXXLINK) -rpath $(libdir) $(libccgnu2_la_LDFLAGS) $(libccgnu2_la_OBJECTS) $(libccgnu2_la_LIBADD) $(LIBS)

libccext2.la: $(libccext2_la_OBJECTS) $(libccext2_la_DEPENDENCIES)
	$(CXXLINK) -rpath $(libdir) $(libccext2_la_LDFLAGS) $(libccext2_la_OBJECTS) $(libccext2_la_LIBADD) $(LIBS)
.cpp.o:
	$(CXXCOMPILE) -c $<
.cpp.obj:
	$(CXXCOMPILE) -c `cygpath -w $<`
.cpp.lo:
	$(LTCXXCOMPILE) -c $<

install-aclocalDATA: $(aclocal_DATA)
	@$(NORMAL_INSTALL)
	$(mkinstalldirs) $(DESTDIR)$(aclocaldir)
	@list='$(aclocal_DATA)'; for p in $$list; do \
	  if test -f $(srcdir)/$$p; then \
	    echo " $(INSTALL_DATA) $(srcdir)/$$p $(DESTDIR)$(aclocaldir)/$$p"; \
	    $(INSTALL_DATA) $(srcdir)/$$p $(DESTDIR)$(aclocaldir)/$$p; \
	  else if test -f $$p; then \
	    echo " $(INSTALL_DATA) $$p $(DESTDIR)$(aclocaldir)/$$p"; \
	    $(INSTALL_DATA) $$p $(DESTDIR)$(aclocaldir)/$$p; \
	  fi; fi; \
	done

uninstall-aclocalDATA:
	@$(NORMAL_UNINSTALL)
	list='$(aclocal_DATA)'; for p in $$list; do \
	  rm -f $(DESTDIR)$(aclocaldir)/$$p; \
	done

install-pkgconfigDATA: $(pkgconfig_DATA)
	@$(NORMAL_INSTALL)
	$(mkinstalldirs) $(DESTDIR)$(pkgconfigdir)
	@list='$(pkgconfig_DATA)'; for p in $$list; do \
	  if test -f $(srcdir)/$$p; then \
	    echo " $(INSTALL_DATA) $(srcdir)/$$p $(DESTDIR)$(pkgconfigdir)/$$p"; \
	    $(INSTALL_DATA) $(srcdir)/$$p $(DESTDIR)$(pkgconfigdir)/$$p; \
	  else if test -f $$p; then \
	    echo " $(INSTALL_DATA) $$p $(DESTDIR)$(pkgconfigdir)/$$p"; \
	    $(INSTALL_DATA) $$p $(DESTDIR)$(pkgconfigdir)/$$p; \
	  fi; fi; \
	done

uninstall-pkgconfigDATA:
	@$(NORMAL_UNINSTALL)
	list='$(pkgconfig_DATA)'; for p in $$list; do \
	  rm -f $(DESTDIR)$(pkgconfigdir)/$$p; \
	done

install-scriptDATA: $(script_DATA)
	@$(NORMAL_INSTALL)
	$(mkinstalldirs) $(DESTDIR)$(scriptdir)
	@list='$(script_DATA)'; for p in $$list; do \
	  if test -f $(srcdir)/$$p; then \
	    echo " $(INSTALL_DATA) $(srcdir)/$$p $(DESTDIR)$(scriptdir)/$$p"; \
	    $(INSTALL_DATA) $(srcdir)/$$p $(DESTDIR)$(scriptdir)/$$p; \
	  else if test -f $$p; then \
	    echo " $(INSTALL_DATA) $$p $(DESTDIR)$(scriptdir)/$$p"; \
	    $(INSTALL_DATA) $$p $(DESTDIR)$(scriptdir)/$$p; \
	  fi; fi; \
	done

uninstall-scriptDATA:
	@$(NORMAL_UNINSTALL)
	list='$(script_DATA)'; for p in $$list; do \
	  rm -f $(DESTDIR)$(scriptdir)/$$p; \
	done

install-optincludeHEADERS: $(optinclude_HEADERS)
	@$(NORMAL_INSTALL)
	$(mkinstalldirs) $(DESTDIR)$(optincludedir)
	@list='$(optinclude_HEADERS)'; for p in $$list; do \
	  if test -f "$$p"; then d= ; else d="$(srcdir)/"; fi; \
	  echo " $(INSTALL_DATA) $$d$$p $(DESTDIR)$(optincludedir)/$$p"; \
	  $(INSTALL_DATA) $$d$$p $(DESTDIR)$(optincludedir)/$$p; \
	done

uninstall-optincludeHEADERS:
	@$(NORMAL_UNINSTALL)
	list='$(optinclude_HEADERS)'; for p in $$list; do \
	  rm -f $(DESTDIR)$(optincludedir)/$$p; \
	done

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

subdir = src

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
assoc.lo assoc.o : assoc.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h \
	../include/cc++/misc.h ../include/cc++/missing.h \
	../include/cc++/strchar.h private.h
buffer.lo buffer.o : buffer.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/buffer.h
cmdoptns.lo cmdoptns.o : cmdoptns.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/exception.h ../include/cc++/export.h \
	../include/cc++/cmdoptns.h
date.lo date.o : date.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/export.h \
	../include/cc++/numbers.h
digest.lo digest.o : digest.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/exception.h \
	../include/cc++/thread.h ../include/cc++/export.h \
	../include/cc++/digest.h
dir.lo dir.o : dir.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/file.h \
	../include/cc++/thread.h private.h
dso.lo dso.o : dso.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/slog.h \
	../include/cc++/thread.h ../include/cc++/file.h private.h
engine.lo engine.o : engine.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/exception.h \
	../include/cc++/export.h ../include/cc++/persist.h
event.lo event.o : event.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h private.h
exception.lo exception.o : exception.cpp ../include/cc++/config.h \
	getopt.h ../include/cc++/export.h ../include/cc++/string.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/exception.h
file.lo file.o : file.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/file.h ../include/cc++/process.h private.h
friends.lo friends.o : friends.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h private.h
in6addr.lo in6addr.o : in6addr.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/address.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/exception.h \
	../include/cc++/string.h private.h
inaddr.lo inaddr.o : inaddr.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/address.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/exception.h \
	../include/cc++/string.h private.h
keydata.lo keydata.o : keydata.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/file.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/misc.h private.h
lockfile.lo lockfile.o : lockfile.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h \
	../include/cc++/process.h ../include/cc++/strchar.h \
	../include/cc++/missing.h
md5.lo md5.o : md5.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/export.h \
	../include/cc++/digest.h
mempager.lo mempager.o : mempager.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/slog.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/misc.h private.h
mime.lo mime.o : mime.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/file.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/socket.h ../include/cc++/address.h \
	../include/cc++/export.h ../include/cc++/mime.h
missing.lo missing.o : missing.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/strchar.h \
	../include/cc++/missing.h
mutex.lo mutex.o : mutex.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h private.h \
	../include/cc++/slog.h
nat.lo nat.o : nat.cpp ../include/cc++/config.h getopt.h nat.h
network.lo network.o : network.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/thread.h ../include/cc++/address.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/socket.h ../include/cc++/export.h \
	../include/cc++/network.h
numbers.lo numbers.o : numbers.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/export.h ../include/cc++/numbers.h
peer.lo peer.o : peer.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h \
	../include/cc++/address.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/socket.h private.h
persist.lo persist.o : persist.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/export.h ../include/cc++/persist.h
process.lo process.o : process.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h \
	../include/cc++/process.h ../include/cc++/strchar.h \
	../include/cc++/missing.h
runlist.lo runlist.o : runlist.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h \
	../include/cc++/misc.h ../include/cc++/missing.h \
	../include/cc++/strchar.h private.h
semaphore.lo semaphore.o : semaphore.cpp ../include/cc++/config.h \
	getopt.h ../include/cc++/export.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h private.h
serial.lo serial.o : serial.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/file.h \
	../include/cc++/export.h ../include/cc++/serial.h private.h
simplesocket.lo simplesocket.o : simplesocket.cpp \
	../include/cc++/config.h getopt.h ../include/cc++/export.h \
	../include/cc++/socket.h ../include/cc++/address.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/exception.h \
	../include/cc++/string.h
slog.lo slog.o : slog.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h \
	../include/cc++/slog.h ../include/cc++/missing.h \
	../include/cc++/strchar.h private.h
socket.lo socket.o : socket.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/exception.h \
	../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/address.h ../include/cc++/socket.h private.h \
	nat.h
socketport.lo socketport.o : socketport.cpp ../include/cc++/config.h \
	getopt.h ../include/cc++/thread.h ../include/cc++/address.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/socket.h ../include/cc++/export.h \
	../include/cc++/socketport.h private.h
strchar.lo strchar.o : strchar.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/missing.h \
	../include/cc++/strchar.h
string.lo string.o : string.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h \
	../include/cc++/misc.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/string.h private.h
thread.lo thread.o : thread.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/missing.h ../include/cc++/strchar.h private.h
threadkey.lo threadkey.o : threadkey.cpp ../include/cc++/config.h \
	getopt.h ../include/cc++/export.h ../include/cc++/thread.h \
	private.h
timer.lo timer.o : timer.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/export.h ../include/cc++/thread.h private.h
tokenizer.lo tokenizer.o : tokenizer.cpp ../include/cc++/config.h \
	getopt.h ../include/cc++/string.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/exception.h \
	../include/cc++/thread.h ../include/cc++/export.h \
	../include/cc++/tokenizer.h private.h
unix.lo unix.o : unix.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/address.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/socket.h ../include/cc++/file.h \
	../include/cc++/export.h ../include/cc++/unix.h
url.lo url.o : url.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/file.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/address.h ../include/cc++/socket.h \
	../include/cc++/export.h ../include/cc++/url.h \
	../include/cc++/mime.h
urlstring.lo urlstring.o : urlstring.cpp ../include/cc++/config.h \
	getopt.h ../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/missing.h ../include/cc++/strchar.h \
	../include/cc++/thread.h ../include/cc++/address.h \
	../include/cc++/socket.h ../include/cc++/export.h \
	../include/cc++/url.h ../include/cc++/mime.h
xml.lo xml.o : xml.cpp ../include/cc++/config.h getopt.h \
	../include/cc++/file.h ../include/cc++/missing.h \
	../include/cc++/strchar.h ../include/cc++/thread.h \
	../include/cc++/exception.h ../include/cc++/string.h \
	../include/cc++/export.h ../include/cc++/xml.h \
	../include/cc++/slog.h
zstream.lo zstream.o : zstream.cpp

info-am:
info: info-am
dvi-am:
dvi: dvi-am
check-am: all-am
check: check-am
installcheck-am:
installcheck: installcheck-am
install-exec-am: install-libLTLIBRARIES
install-exec: install-exec-am

install-data-am: install-aclocalDATA install-pkgconfigDATA \
		install-scriptDATA install-optincludeHEADERS
	@$(NORMAL_INSTALL)
	$(MAKE) $(AM_MAKEFLAGS) install-data-hook
install-data: install-data-am

install-am: all-am
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am
install: install-am
uninstall-am: uninstall-libLTLIBRARIES uninstall-aclocalDATA \
		uninstall-pkgconfigDATA uninstall-scriptDATA \
		uninstall-optincludeHEADERS
uninstall: uninstall-am
all-am: Makefile $(LTLIBRARIES) $(DATA) $(HEADERS)
all-redirect: all-am
install-strip:
	$(MAKE) $(AM_MAKEFLAGS) AM_INSTALL_PROGRAM_FLAGS=-s install
installdirs:
	$(mkinstalldirs)  $(DESTDIR)$(libdir) $(DESTDIR)$(aclocaldir) \
		$(DESTDIR)$(pkgconfigdir) $(DESTDIR)$(scriptdir) \
		$(DESTDIR)$(optincludedir)


mostlyclean-generic:

clean-generic:

distclean-generic:
	-rm -f Makefile $(CONFIG_CLEAN_FILES)
	-rm -f config.cache config.log stamp-h stamp-h[0-9]*

maintainer-clean-generic:
	-test -z "$(MAINTAINERCLEANFILES)" || rm -f $(MAINTAINERCLEANFILES)
mostlyclean-am:  mostlyclean-libLTLIBRARIES mostlyclean-compile \
		mostlyclean-libtool mostlyclean-tags \
		mostlyclean-generic

mostlyclean: mostlyclean-am

clean-am:  clean-libLTLIBRARIES clean-compile clean-libtool clean-tags \
		clean-generic mostlyclean-am

clean: clean-am

distclean-am:  distclean-libLTLIBRARIES distclean-compile \
		distclean-libtool distclean-tags distclean-generic \
		clean-am
	-rm -f libtool

distclean: distclean-am

maintainer-clean-am:  maintainer-clean-libLTLIBRARIES \
		maintainer-clean-compile maintainer-clean-libtool \
		maintainer-clean-tags maintainer-clean-generic \
		distclean-am
	@echo "This command is intended for maintainers to use;"
	@echo "it deletes files that may require special tools to rebuild."

maintainer-clean: maintainer-clean-am

.PHONY: mostlyclean-libLTLIBRARIES distclean-libLTLIBRARIES \
clean-libLTLIBRARIES maintainer-clean-libLTLIBRARIES \
uninstall-libLTLIBRARIES install-libLTLIBRARIES mostlyclean-compile \
distclean-compile clean-compile maintainer-clean-compile \
mostlyclean-libtool distclean-libtool clean-libtool \
maintainer-clean-libtool uninstall-aclocalDATA install-aclocalDATA \
uninstall-pkgconfigDATA install-pkgconfigDATA uninstall-scriptDATA \
install-scriptDATA uninstall-optincludeHEADERS \
install-optincludeHEADERS tags mostlyclean-tags distclean-tags \
clean-tags maintainer-clean-tags distdir info-am info dvi-am dvi check \
check-am installcheck-am installcheck install-exec-am install-exec \
install-data-am install-data install-am install uninstall-am uninstall \
all-redirect all-am all installdirs mostlyclean-generic \
distclean-generic clean-generic maintainer-clean-generic clean \
mostlyclean distclean maintainer-clean


# private.h and nat.h are internal headers

macosx: libccext2.la libccgnu2.la
	rm -rf .libs/*.dylib
	ld -r -o ccgnu2.lo $(libccgnu2_la_OBJECTS)
	../libtool --mode=link c++ -dynamic -dynamiclib -o libccgnu2.la \
		-rpath $(libdir) ccgnu2.lo $(LDFLAGS) -lpthread
	ld -r -o ccext2.lo $(libccext2_la_OBJECTS)
	../libtool --mode=link c++ -dynamic -dynamiclib -o libccext2.la \
		-rpath $(libdir) ccext2.lo $(LDFLAGS) -lpthread
	touch macosx

install-data-hook:
	chmod +x $(DESTDIR)${scriptdir}/ccgnu2-config

# Tell versions [3.59,3.63) of GNU make to not export all variables.
# Otherwise a system limit (for SysV at least) may be exceeded.
.NOEXPORT:
