Summary: "ccrtp" - a Common C++ class framework for RTP/RTCP
Name: ccrtp
Version: 1.3.6
Release: 1
Epoch: 0
License: GPL
Group: Development/Libraries
URL: http://www.gnu.org/software/ccrtp
Source: ftp://ftp.gnu.org/gnu/ccrtp/ccrtp-%{PACKAGE_VERSION}.tar.gz
BuildRoot: %{_tmppath}/rtp-root
Packager: David Sugar <dyfet@ostel.com>
Requires: commoncpp2 >= 1.1.0
BuildRequires: commoncpp2-devel
BuildRequires: doxygen
BuildRequires: info
BuildRequires: pkgconfig
BuildRequires: libstdc++-devel

%package devel
Requires: %{name} = %{epoch}:%{version}-%{release}
Requires: commoncpp2-devel
Requires(post,postun): info
Group: Development/Libraries
Summary: headers and link libraries for ccrtp

%description
ccRTP is a generic, extensible and efficient C++ framework for
developing applications based on the Real-Time Transport Protocol
(RTP) from the IETF. It is based on Common C++ and provides a full
RTP/RTCP stack for sending and receiving of realtime data by the use
of send and receive packet queues. ccRTP supports unicast,
multi-unicast and multicast, manages multiple sources, handles RTCP
automatically, supports different threading models and is generic as
for underlying network and transport protocols.

%description devel
This package holds documentation, header files, and static link libraries
for building applications that use GNU ccRTP.

%prep
%setup
%build
%configure
make %{?_smp_mflags} CXXFLAGS="-I%{_includedir}/cc++2 $RPM_OPT_FLAGS" LDFLAGS="-s"

%install
rm -fr %{buildroot}

%makeinstall

%clean
rm -fr %{buildroot}

%files
#%exclude %{_libdir}/*.la
%defattr(-,root,root,-)
%doc AUTHORS COPYING COPYING.addendum README
%{_libdir}/libccrtp*.so.*

%files devel
%defattr(-,root,root,-)
%doc doc/html/*.html doc/html/*.*g*
%{_libdir}/libccrtp*.a
%{_libdir}/libccrtp*.so
%{_libdir}/libccrtp*.la
%dir %{_includedir}/ccrtp
%{_includedir}/ccrtp/*.h
%{_infodir}/ccrtp*info*
%{_libdir}/pkgconfig/libccrtp1.pc

%post -p /sbin/ldconfig

%post devel
/sbin/install-info %{_infodir}/ccrtp.info %{_infodir}/dir

%postun -p /sbin/ldconfig

%postun devel
/sbin/install-info --delete %{_infodir}/ccrtp.info %{_infodir}/dir

%changelog
* Tue Jan 16 2004 David Sugar <dyfet[AT]ostel.com> 0:1.1.0-1
- "modernized" for more current RPM systems.
