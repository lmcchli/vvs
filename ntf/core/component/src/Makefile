#Copyright (c) 2003 Mobeon AB
#All Rights Reserved

################################################################
# CONFIGURABLE VARIABLES
VOBROOT:=../..
PROD_NO:=cxc109127
PLATFORM:=LIN_2_6
LC_CTYPE=sv_SE.ISO8859-15
LC_NUMERIC=sv_SE.ISO8859-15
LC_COLLATE=sv_SE.ISO8859-15
LC_MONETARY=sv_SE.ISO8859-15
LC_MESSAGES=sv_SE.ISO8859-15
#export LC_CTYPE
#export LC_NUMERIC
#export LC_COLLATE
#export LC_MONETARY
#export LC_MESSAGES


################################################################
#GENERATED VARIABLES
COMPONENT:=$(shell pwd | sed -e "s@$(VOBROOT)/@@" -e "s@/.*@@")
TOPLEVEL:=$(VOBROOT)/$(COMPONENT)
THIS_VU := $(shell cleartool pwv -sho)
THIS_DATE := $(shell $(DT_CMD))
ifeq ($(VERSION),)
VERSION := $(THIS_VU)
endif
# List of source files
NTF_SRC		=  $(shell cat $(TOPLEVEL)/src/ntf_source.list )
#Just java files
NTF_JSRC		:= $(filter %.java, $(foreach file, $(NTF_SRC), $(file)))

################################################################
# PATHS
OEM_HOME:= $(VOBROOT)/$(COMPONENT)_oem
BINDIR=$(TOPLEVEL)/bin
#Build generates files in local disc or home directory instead of the VOB view.
DATADIR:= $(wildcard /data)
ifeq "$(DATADIR)" "/data"
BUILDDIR:= /data/$(USER)/build/$(THIS_VU)/$(PLATFORM)
else
BUILDDIR:= $(HOME)/build/$(THIS_VU)/$(PLATFORM)
endif
OBJDIR:= $(BUILDDIR)/obj
EXEDIR:= $(BUILDDIR)/bin
LIBDIR:= $(BUILDDIR)/lib
TESTOBJDIR:=$(BUILDDIR)/test-obj
ifndef CHECKFILE
CHECKFILE:= -r $(TOPLEVEL)/src/com/mobeon
#CHECKFILE:= $(shell find $(TOPLEVEL)/src/com -name test -prune -o -type f -name '*.java' -print)
endif

################################################################
# TOOLS
RM_TOOL		:= /bin/rm

################################################################
# JAVA STUFF
#JAVAHOME:= /usr/local/j2sdk1.4.0  
#JAVAHOME:= /usr/j2se
JAVAHOME:= /project/MMS/jdk1.5.0_05_linux
#JAVAC:= $(JAVAHOME)/bin/javac
#JAVAC=/usr/local/jdk1.5.0_05/bin/javac
JAVAC=$(JAVAHOME)/bin/javac
JAR:= $(JAVAHOME)/bin/jar
JAVADOC:= $(JAVAHOME)/bin/javadoc
JACOBE:=/usr/local/jacobe-7.2.0/jacobe
INPUTS:=$(shell cleartool lsco -all -cview -s | grep .java)
CLASSPATH:=.
CLASSPATH:=$(CLASSPATH):$(BINDIR)/ldapfilt.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/ldapjdk.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/ldapsp.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/xmlParserAPIs.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/xercesImpl.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/activation.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/mail.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/radiusapi.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/ess_api_java.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/apache-activemq-4.1.1.jar
CLASSPATH:=$(CLASSPATH):$(BINDIR)/foundation.jar
JAVAFLAGS:= -O -classpath $(CLASSPATH)
ifdef DEBUG
JAVAFLAGS:= -g $(JAVAFLAGS)
endif
# Target jar file for java classes
JARFILE	= $(COMPONENT).jar

################################################################
# BUILD TARGETS
.SUFFIXES: .class .java

all: jar ntfagent

dirs: $(OBJDIR) $(EXEDIR) $(LIBDIR)
$(OBJDIR):
	mkdir -p $(OBJDIR)
$(EXEDIR):
	mkdir -p $(EXEDIR)
$(LIBDIR):
	mkdir -p $(LIBDIR)

echo:
	echo "BINDIR $(BINDIR)"
	echo "JARFILE $(JARFILE)"
	echo "COMPONENT $(COMPONENT)"

$(BINDIR)/$(JARFILE): $(LIBDIR)/$(JARFILE)
	cp $(LIBDIR)/$(JARFILE) $(BINDIR)/$(JARFILE) 

#Build ntfagent using another Makefile
ntfagent: dirs
	cd ntfagent && clearmake -V -C gnu all TOPLEVEL=$(TOPLEVEL)

jar: $(BINDIR)/$(JARFILE)

manifest:
	@echo "Implementation-Title: CRH 109 127" > $(BUILDDIR)/manifest
	@echo "Implementation-Version:" $(VERSION) >> $(BUILDDIR)/manifest
	@echo "Implementation-Vendor: Mobeon AB" >> $(BUILDDIR)/manifest
	@echo "Main-Class: com.mobeon.ntf.NtfMain" >> $(BUILDDIR)/manifest
	@echo >> $(BUILDDIR)/manifest

buildinfo:
	@echo "Builder: " $(USER) > $(BUILDDIR)/.buildinfo
	@echo "Build date:" `date` >> $(BUILDDIR)/.buildinfo
	@echo "Build view: " `cleartool pwv -sho` >> $(BUILDDIR)/.buildinfo
	@echo "Config spec: \n{`cleartool catcs`}" >> $(BUILDDIR)/.buildinfo

# Compile each java file into one (or more) class files
classfiles: dirs $(NTF_JSRC)
	@echo "Compiling Java source"
	$(JAVAC) $(JAVAFLAGS) -d $(OBJDIR) @$(COMPONENT)_source.list
	@/bin/rm -f $(TOPLEVEL)/src/.LAST_TEST_BUILD >/dev/null 2>&1

# Make jar file from all class files
$(LIBDIR)/$(JARFILE): dirs classfiles manifest buildinfo
	-@cd $(OBJDIR); \
	$(JAR) cvmf $(BUILDDIR)/manifest $@ ../.buildinfo ./com

FORCE:


#This section is for building classes for unit test
TESTCLASSDIR := $(TESTOBJDIR)
TESTCLASSPATH := $(TESTCLASSDIR):$(LIBDIR)/$(JARFILE):/proj/ipms/junit/junit3.8.1/junit.jar:$(CLASSPATH)

LASTB = $(wildcard .LAST_TEST_BUILD)
ifeq "$(LASTB)" ".LAST_TEST_BUILD"
NEWER := "-newer"
endif
TESTDIRS := $(shell find . -type d -name test)
TESTSRC = $(foreach file, $(TESTDIRS), $(shell find $(file) -name '*.java' $(NEWER) $(LASTB)))
TESTSUPPORTDIRS := $(shell find . -type d -name testsupport)
TESTSUPPORTSRC = $(foreach file, $(TESTSUPPORTDIRS), $(shell find $(file) -name '*.java' $(NEWER) $(LASTB)))

$(TESTCLASSDIR):
	mkdir -p $(TESTCLASSDIR)

TESTLOGGERDIR = $(TOPLEVEL)/src/com/mobeon/ntf/testsupport/com/mobeon/ntf/util

testclasses: $(TESTLOGGERDIR)/Logger.java .LAST_TEST_BUILD

$(TESTLOGGERDIR)/Logger.java:
	cp $(TESTLOGGERDIR)/Logger.CpThisToLogger.javaForUseInBasicTest $(TESTLOGGERDIR)/Logger.java

.LAST_TEST_BUILD: $(TESTCLASSDIR) $(TESTSUPPORTSRC) $(TESTSRC)
	$(JAVAC) -g -d $(TESTCLASSDIR) $(TESTSUPPORTSRC)
	$(JAVAC) -g -classpath $(TESTCLASSPATH) -d $(TESTCLASSDIR) $(TESTSRC)
	touch .LAST_TEST_BUILD


#These rules are for building smscom as a separate unit for delivery to other components.
#"make smscom" to create the file smscom.tar in your home directory, with all files needed for smscom.
#Some temporary files are stored in /var/tmp.
SMSCOM = $(TOPLEVEL)/com/mobeon/common/smscom

smscom: $(HOME)/smscom.tar

$(HOME)/smscom.tar: FORCE
	$(JAVAC) $(JAVAFLAGS) -d $(OBJDIR) $(SMSCOM)/SMSCom.java $(SMSCOM)/smpp/SMPPCom.java $(SMSCOM)/cimd2/CIMD2Com.java $(SMSCOM)/charset/Converter.java && \
cd $(OBJDIR) &&	\
$(JAR) cf $(LIBDIR)/smscom.jar com/mobeon/common/smscom && \
cp $(TOPLEVEL)/cfg/charconv.cfg $(TOPLEVEL)/cfg/charconv.cimd2 $(LIBDIR) && \
cd $(LIBDIR) && \
tar cf $(LIBDIR)/smscom.tar smscom.jar charconv.cfg charconv.cimd2 && \
$(RM) smscom.jar charconv.cfg charconv.cimd2


################################################################
#CHECK CODING STYLE
checkstyle:
	java -cp /proj/ipms/checkstyle/checkstyle-3.0/checkstyle-all-3.0.jar:$(CLASSPATH) com.puppycrawl.tools.checkstyle.Main -c stylechecks.xml $(CHECKFILE)

checksomestyle:
	java -cp /proj/ipms/checkstyle/checkstyle-3.0/checkstyle-all-3.0.jar:$(CLASSPATH) com.puppycrawl.tools.checkstyle.Main -c somechecks.xml $(CHECKFILE)

jacobe:
	$(JACOBE) -cfg=jacobe_ntf.cfg -overwrite -noenum $(INPUTS)


################################################################
# GENERATE JAVADOC
doc:
	/bin/sed -e "s/RSTATE/$(RSTATE)/g" <$(TOPLEVEL)/src/javadoc_options >$(TOPLEVEL)/src/javadoc_options.tmp
	$(JAVADOC) -classpath $(CLASSPATH):/proj/ipms/junit/junit3.8.1/junit.jar -sourcepath $(TOPLEVEL)/src     \
        -d $(HOME)/public_html/$(COMPONENT)javadoc @$(TOPLEVEL)/src/javadoc_options.tmp

################################################################
# REMOVE INTERMEDIATE FILES
clean: ntfagent_clean
	$(RM_TOOL) -rf $(BUILDDIR)/*

ntfagent_clean:
	cd ntfagent && clearmake -V -C gnu clean TOPLEVEL=$(TOPLEVEL)


.PHONY: manifest buildinfo all ntfagent jar classfiles FORCE testclasses smscom doc clean

