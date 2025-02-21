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

AUTOMAKE_OPTIONS = no-dependencies
EXTRA_DIST = $(M4)
MAINTAINERCLEANFILES = Makefile.in

M4 = ost_prog.m4 ost_cxx.m4 ost_pthread.m4 ost_posix.m4 ost_systime.m4      ost_maint.m4 ost_signal.m4 ost_socket.m4 ost_string.m4 ost_types.m4      ost_win32.m4 ost_dynamic.m4 ost_poll.m4 ost_misc.m4 ost_endian.m4      ost_debug.m4 ost_getopt.m4 ost_reentrant.m4 win32msc.m4      ost_stlport.m4 ost_xml.m4


SOURCES = $(M4)
mkinstalldirs = $(SHELL) $(top_srcdir)/autoconf/mkinstalldirs
CONFIG_HEADER = ../include/config.h
CONFIG_CLEAN_FILES = 
DIST_COMMON =  Makefile.am Makefile.in


DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS) $(TEXINFOS) $(EXTRA_DIST)

TAR = tar
GZIP_ENV = --best
all: all-redirect
.SUFFIXES:
$(srcdir)/Makefile.in: # Makefile.am $(top_srcdir)/configure.ac $(ACLOCAL_M4) 
	cd $(top_srcdir) && $(AUTOMAKE) --gnu m4/Makefile

Makefile: $(srcdir)/Makefile.in  $(top_builddir)/config.status
	cd $(top_builddir) \
	  && CONFIG_FILES=$(subdir)/$@ CONFIG_HEADERS= $(SHELL) ./config.status

tags: TAGS
TAGS:


distdir = $(top_builddir)/$(PACKAGE)-$(VERSION)/$(subdir)

subdir = m4

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
all-am: Makefile all-local
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
mostlyclean-am:  mostlyclean-generic

mostlyclean: mostlyclean-am

clean-am:  clean-generic mostlyclean-am

clean: clean-am

distclean-am:  distclean-generic clean-am
	-rm -f libtool

distclean: distclean-am

maintainer-clean-am:  maintainer-clean-generic distclean-am
	@echo "This command is intended for maintainers to use;"
	@echo "it deletes files that may require special tools to rebuild."

maintainer-clean: maintainer-clean-am

.PHONY: tags distdir info-am info dvi-am dvi check check-am \
installcheck-am installcheck install-exec-am install-exec \
install-data-am install-data install-am install uninstall-am uninstall \
all-local all-redirect all-am all installdirs mostlyclean-generic \
distclean-generic clean-generic maintainer-clean-generic clean \
mostlyclean distclean maintainer-clean


../aclocal.m4: $(M4)
	cd .. ;	$(ACLOCAL) -I m4

all-local:	../aclocal.m4

# Tell versions [3.59,3.63) of GNU make to not export all variables.
# Otherwise a system limit (for SysV at least) may be exceeded.
.NOEXPORT:
