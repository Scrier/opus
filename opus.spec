Name:	            opus	
#Version:          0.1.0
Version:          %{version}
Release:	        1%{?dist}
#Release:	         %{release}
Summary:	        distributed load generator written in Java.
License:          Apache License, Version 2.0	
URL:		          http://scrier.github.io
Source0: 	        %{name}-%{version}.tar.gz
BuildArch:        noarch

BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root

%description
This is an test to build opus rpm.
opus version: %{version}
common version: %{common_version}
duke version: %{duke_version}
nuke version: %{nuke_version}


%package          javadoc
Summary:          Javadoc for %{name}


%description      javadoc
This package contains the API documentation for %{name}


%prep
%setup -q


%build


%install
rm -rf %{buildroot}
mkdir -p  %{buildroot}

cp -a * %{buildroot}
rm %{buildroot}/setup.sh
mv %{buildroot}%{_initrddir}/duke.sh %{buildroot}%{_initrddir}/duke
mv %{buildroot}%{_initrddir}/nuke.sh %{buildroot}%{_initrddir}/nuke


%clean
rm -rf %{buildroot}


%post
ln -sf %{_javadir}/%{name}/common-%{common_version}.jar %{_javadir}/%{name}/common.jar
ln -sf %{_javadir}/%{name}/duke-%{duke_version}.jar %{_javadir}/%{name}/duke.jar
ln -sf %{_javadir}/%{name}/nuke-%{nuke_version}.jar %{_javadir}/%{name}/nuke.jar


%files
%defattr(-,steven,steven,-)

%{_initrddir}/duke
%{_initrddir}/nuke
%{_javadir}/%{name}/log4j2duke.xml
%{_javadir}/%{name}/log4j2nuke.xml
%{_javadir}/%{name}/common-%{common_version}.jar
%{_javadir}/%{name}/nuke-%{nuke_version}.jar
%{_javadir}/%{name}/duke-%{duke_version}.jar


%changelog
 * Wed Dec 03 2014 Andreas Joelsson <andreas.joelsson@gmail.com> - 0.1.0
   - Initial packaging

