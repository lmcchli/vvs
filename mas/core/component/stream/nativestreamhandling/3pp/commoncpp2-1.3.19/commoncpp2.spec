# Copyright (c) 1999-2001 by Open Source Telecom Corporation.
# Verbatim copying and distribution of this entire file is permitted
# in any medium, provided this notice is preserved.

# NOTE: you can set %_config_commoncpp2 = --without-exceptions in your
# ~/.rpmmacros file to build GNU Common C++ without exception handling
# for increased optimization and efficiency.  This will break some
# packages which actually depend on/use C++ exception handling, but is
# very worthwhile if you are only using packages which do not.

Name: commoncpp2
Summary: "commoncpp2" - A GNU package for creating portable C++ programs
Version: 1.3.19
Release: 1.ost1
Epoch: 0
License: GPL
Group: Development/Libraries
URL: http://www.gnu.org/software/commonc++/commonc++.html
Source0: ftp://www.gnu.org/gnu/commonc++/commoncpp2-%{PACKAGE_VERSION}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root
Requires: libxml2
Requires: zlib
Requires: libstdc++
BuildRequires: libxml2-devel
BuildRequires: zlib-devel
BuildRequires: libstdc++-devel
BuildRequires: doxygen
BuildRequires: info
Packager: David Sugar <dyfet@ostel.com>

%package devel
Requires: %{name} = %{epoch}:%{version}-%{release}
Requires: libxml2-devel
Requires: zlib-devel
Requires: libstdc++-devel
Requires(post,postun): info
Group: Development/Libraries
Summary: Headers and static link library for commoncpp2

%description
This is the second major release of GNU Common C++.  GNU Common C++ "2" is
a GNU package which offers portable "abstraction" of system services such as
threads, networks, and sockets.  GNU Common C++ "2" also offers individual
frameworks generally useful to developing portable C++ applications
including a object persistance engine, math libraries, threading, sockets,
etc.  GNU Common C++ "2" is small, and highly portable.  GNU Common C++
"2" will  support most Unix operating systems as well as W32, in addition
to GNU/Linux.

%description devel
This package provides the link libraries and documentation for building
GNU Common C++ applications.

%prep
%setup
%build
%configure %{_config_commoncpp2}
make %{?_smp_mflags}

%install
rm -fr %{buildroot}

%makeinstall

%clean
rm -fr %{buildroot}

%files
#%exclude %{_libdir}/*.la
%defattr(-,root,root,-)
%doc AUTHORS COPYING COPYING.addendum NEWS README TODO ChangeLog
%{_libdir}/libcc*.so.*

%files devel
%defattr(-,root,root,-)
%doc doc/html/*.html doc/html/*.*g*
%{_libdir}/libcc*.a
%{_libdir}/libcc*.so
%{_libdir}/libcc*.la
%dir %{_includedir}/cc++2
%dir %{_includedir}/cc++2/cc++
%{_includedir}/cc++2/cc++/*.h
%{_bindir}/ccgnu2-config
%{_infodir}/commoncpp2.info*
%{_datadir}/aclocal/ost_check2.m4
%{_libdir}/pkgconfig/libccext2.pc
%{_libdir}/pkgconfig/libccgnu2.pc

%post -p /sbin/ldconfig

%post devel
/sbin/install-info %{_infodir}/commoncpp2.info %{_infodir}/dir

%postun -p /sbin/ldconfig

%postun devel
/sbin/install-info --delete %{_infodir}/commoncpp2.info %{_infodir}/dir

%changelog
* Sun Nov  9 2003 David Sugar <dyfet[AT]ostel.com> 0:1.1.0-1
- "modernized" for more current RPM systems.
