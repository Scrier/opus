
## Makefile handle something something.

.PHONY: default
default: help

.PHONY: help
help:
	@echo "=================== Available commands ===================="
	@echo "all          - compile -> test -> doc -> typecheck."
	@echo "build        - build the project." 
	@echo "clean        - clean the project."
	@echo "coverage     - run the coverage tool for the project."
	@echo "doc          - create javadoc for the project."
	@echo "doc-jar      - create jar with the javadoc for the project."
	@echo "install      - install project in local repo."
	@echo "test         - run junit tests for the project."
	@echo "=========================================================="
	@echo "help         - Show this output."

.PHONY: all
all: clean
	mvn package

.PHONY: build
build:
	mvn package

.PHONY: clean
clean:
	mvn clean
	find . -maxdepth 1 -lname '*' -exec rm {} \;

.PHONY: coverage
coverage: clean
	mvn cobertura:cobertura

.PHONY: doc
doc:
	mvn javadoc:javadoc 

.PHONY: doc-jar
doc-jar:
	mvn javadoc:jar

.PHONY: install
install: clean
	mvn install

.PHONY: test
test:
	mvn test

.PHONY: tar
tar:
	mvn -Dmaven.test.skip=true clean package
	./create_tar.sh

###############################################################################
# Packaging

MVN_VERSION 		:= $(shell mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev '(^\[|Download\w+:)')
COMMON_VERSION 	:= $(shell cd common && mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev '(^\[|Download\w+:)')
DUKE_VERSION 		:= $(shell cd duke && mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev '(^\[|Download\w+:)')
NUKE_VERSION 		:= $(shell cd nuke && mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev '(^\[|Download\w+:)')
RPM_DEFINES 	 	:= \
						--define "version $(MVN_VERSION)" \
						--define "common_version $(COMMON_VERSION)" \
						--define "duke_version $(DUKE_VERSION)" \
						--define "nuke_version $(NUKE_VERSION)"

.PHONY: rpm
rpm: tar
	mkdir -p ~/rpmbuild/{RPMS,SRPMS,BUILD,SOURCES,SPECS}
	mv *.tar.gz ~/rpmbuild/SOURCES/
	rpmbuild -bb $(RPM_DEFINES) opus.spec

.PHONY: info
info: 
	@echo "MVN_VERSION $(MVN_VERSION)"
	@echo "RPM_DEFINES $(RPM_DEFINES)"
	@echo "COMMON_VERSION $(COMMON_VERSION)"
	@echo "DUKE_VERSION $(DUKE_VERSION)"
	@echo "NUKE_VERSION $(NUKE_VERSION)"

