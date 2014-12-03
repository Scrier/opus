Name:	            opus	
Version:          0.1.0
#Version:          %{version}
Release:	        1%{?dist}
#Release:	         %{release}
Summary:	        distributed load generator written in Java.
License:          Apache License, Version 2.0	
URL:		          http://scrier.github.io
Source0: 	        %{name}-%{version}.tar.gz
BuildArch:        noarch

BuildRequires:    maven-local

%description
This is an test to build opus rpm.


%package          javadoc
Summary:          Javadoc for %{name}


%description      javadoc
This package contains the API documentation for %{name}


%prep
%setup -q


%build
%mvn_build


%install
%mvn_install


%clean
%mvn_clean


%files -f .mfiles
%dir %{_javadir}/%{name}
%files javadoc -f .mfiles-javadoc


%changelog
 * Wed Dec 03 2014 Andreas Joelsson <andreas.joelsson@gmail.com> - 0.1.0
   - Initial packaging

