# Determing a type name of the targeted system/architecture
include ../../makefiles/architecture.mk

# Including target specific variables
include ../../makefiles/variables.$(SYSTEM).mk

# Determining if we are builing for windows or solaris
ifeq '$(SYSTEM)' 'win32'
	TARGET_SYSTEM=win32
else
ifeq '$(findstring Linux,$(SYSTEM))' ''
	TARGET_SYSTEM=sun5
else
	TARGET_SYSTEM=linux
endif
endif

# Defining targets which are not files
.PHONY: 3pp win32 linux sun5 clean clean_linux clean_win32 clean_sun5

# Targets starts here
3pp: $(TARGET_SYSTEM)

win32:
	@$(ECHO) "============================================="
	@$(ECHO) "Building 3PP for Stream RTP"
	@$(ECHO) "Target system is: [Windooze]"
	@$(ECHO) "Gnu Common C++:   [$(COMMONCPP2_VERSION)]"
	@$(ECHO) "Gnu ccRTP:        [$(CCRTP_VERSION)]"
	@$(ECHO) "Boost:            [$(BOOST_VERSION)]"
	@$(ECHO) "CppUnit:          [$(CPPUNIT_VERSION)]"
	@$(ECHO) "============================================="
	@$(MAKE) -f Makefile.w32
	@cd boost && $(MAKE) $(MFLAGS)

linux:
	@$(ECHO) "============================================="
	@$(ECHO) "Building 3PP for Stream RTP"              
	@$(ECHO) "Gnu Common C++:   [$(COMMONCPP2_VERSION)]"
	@$(ECHO) "Gnu ccRTP:        [$(CCRTP_VERSION)]"
	@$(ECHO) "Boost:            [$(BOOST_VERSION)]"
	@$(ECHO) "CppUnit:          [$(CPPUNIT_VERSION)]"
	@$(ECHO) "============================================="
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) ccext2_archive
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) ccgnu2_archive
	@cd $(CCRTP_VERSION) && $(MAKE) $(MFLAGS)
	@cd $(CPPUNIT_VERSION) && $(MAKE) $(MFLAGS)
	@cd boost && $(MAKE) $(MFLAGS)
sun5:
	@$(ECHO) "============================================="
	@$(ECHO) "Building 3PP for Stream RTP"              
	@$(ECHO) "Gnu Common C++:   [$(COMMONCPP2_VERSION)]"
	@$(ECHO) "Gnu ccRTP:        [$(CCRTP_VERSION)]"
	@$(ECHO) "Boost:            [$(BOOST_VERSION)]"
	@$(ECHO) "CppUnit:          [$(CPPUNIT_VERSION)]"
	@$(ECHO) "============================================="
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) ccext2_archive
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) ccgnu2_archive
	@cd $(CCRTP_VERSION) && $(MAKE) $(MFLAGS) archive
	@cd boost && $(MAKE) $(MFLAGS)


clean: clean_$(TARGET_SYSTEM)

dist: dist_$(TARGET_SYSTEM)

distclean: distclean_$(TARGET_SYSTEM)

clean_win32:
	@$(ECHO) "Running clean_win32 with flags $(MFLAGS)"
	@$(MAKE) -f Makefile.w32 clean

dist_win32:
	@$(MAKE) -f Makefile.w32 dist

distclean_win32:
	@$(MAKE) -f Makefile.w32 distclean

clean_linux:
	@$(ECHO) "Running clean_linux with flags $(MFLAGS)"
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) clean
	@cd $(CCRTP_VERSION) && $(MAKE) $(MFLAGS) distclean
	@cd $(CPPUNIT_VERSION) && $(MAKE) $(MFLAGS) distclean
	@cd boost && $(MAKE) $(MFLAGS) clean

distclean_linux:
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) distclean
	@cd $(CCRTP_VERSION) && $(MAKE) $(MFLAGS) distclean
	@cd $(CPPUNIT_VERSION) && $(MAKE) $(MFLAGS) distclean
	@cd boost && $(MAKE) $(MFLAGS) clean

dist_linux:
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) dist
	@cd $(CCRTP_VERSION) && $(MAKE) $(MFLAGS) dist
	@cd $(CPPUNIT_VERSION) && $(MAKE) $(MFLAGS) dist
	@cd boost && $(MAKE) $(MFLAGS) dist
clean_sun5:
	@$(ECHO) "Running clean_sun5 with flags $(MFLAGS)"
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) clean
	@cd $(CCRTP_VERSION) && $(MAKE) $(MFLAGS) clean
	@cd $(CPPUNIT_VERSION) && $(MAKE) $(MFLAGS) clean
	@cd boost && $(MAKE) $(MFLAGS) clean

distclean_sun5:
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) distclean
	@cd $(CCRTP_VERSION) && $(MAKE) $(MFLAGS) distclean
	@cd $(CPPUNIT_VERSION) && $(MAKE) $(MFLAGS) distclean
	@cd boost && $(MAKE) $(MFLAGS) clean

dist_sun5:
	@cd $(COMMONCPP2_VERSION) && $(MAKE) $(MFLAGS) dist
	@cd $(CCRTP_VERSION) && $(MAKE) $(MFLAGS) dist
	@cd $(CPPUNIT_VERSION) && $(MAKE) $(MFLAGS) dist
	@cd boost && $(MAKE) $(MFLAGS) dist

