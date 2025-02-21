CSRC= ntfWatchdog.cpp Logger.cpp stringutils.cpp

DATADIR = $(wildcard /data)
ifeq "$(DATADIR)" "/data"
BUILDDIR := /data/$(USER)/build/$(shell cleartool pwv -sho)/$(PLATFORM)
else
BUILDDIR := $(HOME)/build/$(shell cleartool pwv -sho)/$(PLATFORM)
endif

OBJDIR  := $(BUILDDIR)/obj

IPMSBOOM=/proj/ipms/ipmsboom/R1A
IPMSBOOMINC=${IPMSBOOM}/include
IPMSBOOMLIB=${IPMSBOOM}/lib
IPMSBOOMLIBS=-lipms

dirs: $(OBJDIR)

$(OBJDIR):
	mkdir -p $(OBJDIR)


FLAGS = -mt -D_REENTRANT
LIBS = -L$(TOPLEVEL)/lib -L$(IPMSBOOMLIB) -lipms \
	-lsocket -lthread -lnsl -lpthread -lrt
INC = -I. -I../include -I$(IPMSBOOMINC)
CC = CC
BINDIR=$(TOPLEVEL)/bin
WATCHDOGBIN=$(BINDIR)/ntfWatchdog


# Obtain list of files to compile
OBJEXT=.o
CPP_OBJ=$(subst .cpp,$(OBJEXT),$(filter %.cpp, $(foreach file, $(CSRC),$(OBJDIR)/$(file))))


all: dirs ntfWatchdog

ntfWatchdog: $(CPP_OBJ)
	$(CC) $(FLAGS) -o $(WATCHDOGBIN) $(CPP_OBJ) $(LIBS)

$(OBJDIR)/%.o: %.cpp
	$(CC) $(FLAGS) $(INC) $(LIBS) -c $< -o $@

clean:
	-rm -f $(WATCHDOGBIN) 
	-rm -rf $(CPP_OBJ) 
	-rm -rf $(OBJDIR)/SunWS_cache
	-rm -rf SunWS_cache

