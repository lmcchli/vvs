Name: commoncpp2
Summary: "commoncpp2" - A GNU package for creating portable C++ programs
Version: 1.3.22
Release: 1
Epoch: 0
Copyright: LGPL
Group: Development/Libraries
URL: http://www.gnu.org/software/commoncpp/commoncpp.html
Source0: ftp://ftp.gnu.org/gnu/commoncpp/commoncpp2-%{PACKAGE_VERSION}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root
Requires: libxml2
Requires: zlib
Requires: libstdc++
Prereq: /sbin/install-info
BuildRequires: libxml2-devel
BuildRequires: zlib-devel
BuildRequires: libstdc++-devel
BuildRequires: doxygen
BuildRequires: info

%package devel
Requires: %{name} = %{epoch}:%{version}-%{release}
Requires: libxml2-devel
Requires: zlib-devel
Requires: libstdc++-devel
Requires(post,postun): info
Group: Development/Libraries
Summary: Headers and static link library for commoncpp2

%description
GNU Common C++ offers portable abstraction of system services such as
threads and sockets.  GNU Common C++ also provides a threadsafe class
framework for strings, config file and XML parsing, and object
serialization.

%description devel
This package provides the header files, link libraries and documentation 
for building GNU Common C++ applications.

%prep
%setup
%build
%configure
make %{?_smp_mflags} LDFLAGS="-s" CXXFLAGS="$RPM_OPT_FLAGS"

%install

%makeinstall
rm -rf %{buildroot}/%{_includedir}/cc++
mv %{buildroot}/%{_includedir}/cc++2/cc++ %{buildroot}/%{_includedir}/cc++

%clean
rm -fr %{buildroot}

%files
%defattr(-,root,root,-)
%doc AUTHORS COPYING COPYING.addendum NEWS README TODO ChangeLog
%{_libdir}/*.so.*

%files devel
%defattr(-,root,root,-)
%doc doc/html/*.html doc/html/*.*g*
%{_libdir}/*.a
%{_libdir}/*.so
%{_libdir}/*.la
%dir %{_includedir}/cc++
%{_includedir}/cc++/*.h
%{_bindir}/*
%{_infodir}/*.info*
%{_datadir}/aclocal/*.m4
%{_libdir}/pkgconfig/*.pc

%post -p /sbin/ldconfig

%post devel
/sbin/install-info %{_infodir}/commoncpp2.info %{_infodir}/dir

%postun -p /sbin/ldconfig

%postun devel
/sbin/install-info --delete %{_infodir}/commoncpp2.info %{_infodir}/dir

