#Copyright (c) 2003 Mobeon AB
#All Rights Reserved

################################################################
# CONFIGURATION PARAMETERS
VOBROOT:=../../..
PROD_NO:=none
#
################################################################


################################################################
# SETTING PATHS AND TOOLS
COMPONENT:=$(shell pwd | sed -e "s@$(VOBROOT)/@@" -e "s@/.*@@")
TOPLEVEL:=$(VOBROOT)/ntf/test/smsc
PLATFORM:=SOL_8
JAR_DIR:=$(VOBROOT)/ntf/test/smsc/bin/smsc.jar
THIS_VU := $(shell cleartool pwv -sho)
ifeq ($(VERSION),)
VERSION := $(THIS_VU)
endif
#Java stuff
#JAVAHOME:= /usr/local/jdk1.5.0_05
JAVAHOME:= /usr/local/j2sdk1.4.2_11
JAVAC:= $(JAVAHOME)/bin/javac
JAR:= $(JAVAHOME)/bin/jar
JAVADOC:= $(JAVAHOME)/bin/javadoc
CLASSPATH:=.
CLASSPATH:=$(CLASSPATH):$(VOBROOT)/ntf_oem/xerces/xerces-2_2_1/xmlParserAPIs.jar
CLASSPATH:=$(CLASSPATH):$(VOBROOT)/ntf_oem/xerces/xerces-2_2_1/xercesImpl.jar
JAVAFLAGS:= -O -classpath $(CLASSPATH)

#Build generates files in local disc or home directory instead of the VOB view.
DATADIR:= $(wildcard /data)
ifeq "$(DATADIR)" "/data"
BUILDDIR:= /data/$(USER)/build/$(shell cleartool pwv -sho)/$(PLATFORM)
else
BUILDDIR:= $(HOME)/build/$(shell cleartool pwv -sho)/$(PLATFORM)
endif
OBJDIR:= $(BUILDDIR)/obj
#
################################################################


################################################################
# SETTING FILES
ifndef CHECKFILE
CHECKFILE:= -r $(TOPLEVEL)/src/com/mobeon/smsc/
endif
# List of source files
SMSC_JSRC:=$(shell cat $(TOPLEVEL)/src/smsc_source.list )
################################################################


################################################################
# BUILD TARGETS
all: jar

echo:
	@echo $(VOBROOT)
	@echo $(COMPONENT)
	@echo $(TOPLEVEL)
	@echo $(SMSC_JSRC)
	@echo $(OBJDIR)

jar: $(TOPLEVEL)/smsc.jar

dirs: $(OBJDIR)
$(OBJDIR):
	mkdir -p $(OBJDIR)


$(TOPLEVEL)/smsc.jar: classfiles manifest buildinfo
	-@cd $(OBJDIR); \
	$(JAR) cvmf ../manifest $(JAR_DIR) com ../.buildinfo
	@echo "Coping smsc.jar to " $(JAR_DIR)

classfiles: dirs $(SMSC_JSRC)
	/bin/rm -rf $(OBJDIR)/com/*
	@echo "Compiling Java source"
	$(JAVAC) $(JAVAFLAGS) -d $(OBJDIR) @smsc_source.list

buildinfo:
	@echo "Builder: " $(USER) > $(BUILDDIR)/.buildinfo
	@echo "Build date:" `date` >> $(BUILDDIR)/.buildinfo
	@echo "Build view: " `cleartool pwv -sho` >> $(BUILDDIR)/.buildinfo
	@echo "Config spec: " `cleartool catcs` >> $(BUILDDIR)/.buildinfo

manifest:
	@echo "Implementation-Title: " $(PROD_NO) > $(BUILDDIR)/manifest
	@echo "Implementation-Version: " $(VERSION) >> $(BUILDDIR)/manifest
	@echo "Implementation-Vendor: Mobeon AB" >> $(BUILDDIR)/manifest
	@echo "Main-Class: com.mobeon.smsc.Main" >> $(BUILDDIR)/manifest
	@echo >> $(BUILDDIR)/manifest

################################################################
# SPECIAL TARGETS
# Check that the coding style matches SUNs guidelines
checkstyle:
	java -cp /proj/ipms/checkstyle/checkstyle-3.0/checkstyle-all-3.0.jar:/vobs/ipms/ntf_oem/xerces/xerces-2_2_1/xmlParserAPIs.jar:/vobs/ipms/ntf_oem/xerces/xerces-2_2_1/xercesImpl.jar com.puppycrawl.tools.checkstyle.Main -c mc7checks.xml $(CHECKFILE)
# Create javadoc
doc:
	$(JAVADOC) -classpath $(CLASSPATH) -sourcepath $(TOPLEVEL)/src     \
        -d $(HOME)/public_html/smscjavadoc @$(VOBROOT)/ntf/src/javadoc_options
# Remove intermediate files.
# Unnecessarily long pathname to protect from bad things if OBJDIR should happen to be wrong
clean:
	/bin/rm -rf $(OBJDIR)/com/* $(OBJDIR)/test/com/mobeon/*
#
################################################################
.PHONY: buildinfo manifest all subagent jar classfiles testclasses smscom doc clean_pure clean
