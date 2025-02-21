include ./makefiles/architecture.mk
include ./makefiles/variables.$(SYSTEM).mk

#Turn on multiple threads, improves performance usually even in single threads.
#MAKEFLAGS+=-j 4

all:
	@$(ECHO) "***"
	@$(ECHO) "*** Building Stream C++ stuff for $(SYSTEM)"
	@$(ECHO) "***"
	@$(ECHO) $(MAKE) $(OUR_MAKEFLAGS) $(MAKEFLAGS)
	@cd nativestreamhandling && $(MAKE) $(MAKEFLAGS) $(OUR_MAKEFLAGS)
	@$(ECHO) "***"
	@$(ECHO) "*** Done."
	@$(ECHO) "***"

dist:
	@$(ECHO) "***"
	@$(ECHO) "*** Distributing Stream C++ stuff for $(SYSTEM)" 
	@$(ECHO) "***"
	@cd nativestreamhandling && $(MAKE) $(MAKEFLAGS) $(OUR_MAKEFLAGS) dist
	$(OPTIONAL_DIST)
	@$(ECHO) "***"
	@$(ECHO) "*** Done."
	@$(ECHO) "***"

clean:
	@$(ECHO) "***"
	@$(ECHO) "*** Cleanup of  Stream C++ stuff for $(SYSTEM)"
	@$(ECHO) "***"
	@cd nativestreamhandling && $(MAKE) $(MAKEFLAGS) $(OUR_MAKEFLAGS) distclean
	$(OPTIONAL_DISTCLEAN)
	@$(ECHO) "***"
	@$(ECHO) "*** Done."
	@$(ECHO) "***"

distclean:
	@$(ECHO) "***"
	@$(ECHO) "*** Cleanup of  Stream C++ stuff for $(SYSTEM)"
	@$(ECHO) "***"
	@cd nativestreamhandling && $(MAKE) $(MAKEFLAGS) $(OUR_MAKEFLAGS) distclean
	$(OPTIONAL_DISTCLEAN)
	@$(ECHO) "***"
	@$(ECHO) "*** Done."
	@$(ECHO) "***"

info:
	@$(ECHO) "STREAM_HOME = $(STREAM_HOME)"
	@$(ECHO) "MAS_HOME = $(MAS_HOME)"
	@$(ECHO) "JAVA_HOME = $(JAVA_HOME)"
	@$(ECHO) "LOGGING_3PP_HOME = $(LOGGING_3PP_HOME)"
	@$(ECHO) "LOGGING_3PP_HOME = $(LOGGING_3PP_HOME)"
	@$(ECHO) "QUICKTIME_HOME = $(QUICKTIME_HOME)"
	@$(ECHO) "BOOST_HOME = $(BOOST_HOME)"
