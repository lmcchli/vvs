

# Defining targets which are not files
.PHONY: archive library clean rebuild dirs

# Targets starts here
archive: dirs $(TARGET_ARCHIVE)

library: dirs $(TARGET_LIBRARY) 

clean:
	$(RM) $(TARGET_ARCHIVE)
	$(RM) $(TARGET_LIBRARY)
	$(RM) $(OBJDIR)

dist: dirs $(TARGET_DIST_LIBRARY)

distclean: clean
	$(RM) $(TARGET_DIST_LIBRARY)

rebuild: clean all

$(TARGET_LIBRARY): $(OBJS)
	$(LD) $(LDFLAGS) $(PATHOBJS) $(LIBRARIES)

$(TARGET_ARCHIVE): $(OBJS)
	$(AR) $(ARFLAGS) $(PATHOBJS)

dirs: $(OBJDIR) $(LIBDIR)

$(OBJDIR):
	mkdir $@

$(LIBDIR):
	mkdir $@

# Implicit rules starts here
.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(CFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(COMPILE) $(OBJECTFILE)
