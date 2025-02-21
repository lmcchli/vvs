TOPLEVEL=../..
PLATFORM:=LIN_2_6
THIS_VU := $(shell cleartool pwv -sho)
THIS_DATE := $(shell date)
DUMMY := $(shell echo Starting top-level build in view $(THIS_VU) at $(THIS_DATE) on $(PLATFORM) 1>&2)


# These are used for compile and link rules.

.SUFFIXES: .o .cpp

DATADIR = $(wildcard /data)
ifeq "$(DATADIR)" "/data"
BUILDDIR := /data/$(USER)/build/$(shell cleartool pwv -sho)/$(PLATFORM)
else
BUILDDIR := $(HOME)/build/$(shell cleartool pwv -sho)/$(PLATFORM)
endif

OBJDIR	:= $(BUILDDIR)/obj
LIBDIR	:= $(BUILDDIR)/lib

dirs: $(OBJDIR) $(LIBDIR)

$(OBJDIR):
		mkdir -p $(OBJDIR)

$(LIBDIR):
	mkdir -p $(LIBDIR)

# Quantify related flags. From Quantify Users guide
QUAN_CACHE_DIR	:= /tmp/PURE_CACHE
QUAN_CACHE_OPTS	:= -cache-dir=$(QUAN_CACHE_DIR) -always-use-cache-dir
QUAN_THREADS	:= -max-threads=500
QUANTIFYOPTIONS	:= $(QUAN_CACHE_OPTS) $(QUAN_THREADS)
QUANTIFY	:= /usr/local/pure/quantify-4.2-solaris2/quantify $(QUANTIFYOPTIONS)

# Purify related flags. From Purify Users guide
PURE_CACHE_DIR	:= /tmp/PURE_CACHE
PURE_CACHE_OPTS	:= -cache-dir=$(PURE_CACHE_DIR) -always-use-cache-dir
PURE_THREADS	:= -max-threads=500
PUREOPTIONS	:= $(PURE_CACHE_OPTS) $(PURE_THREADS)
PURIFY		:= /usr/local/pure/purify-4.2-solaris2/purify $(PUREOPTIONS)

ifeq ($(quantify),1)
    LINK.cc     = $(QUANTIFY) CC
    PQ_EXT            = _q
else # quantify = 0
    ifeq ($(purify),1)
	LINK.cc     = $(PURIFY) CC
        PQ_EXT            = _p
    else # purify = 0
        LINK.cc        = CC
        PQ_EXT            =
    endif
endif

#EMAROOT = /proj/ipms/snmp/snmp15.2.1.9
EMAROOT = /project/moipsoft/component/MoIP_EMS/Emanate/snmp17.1.0.14
MIBDEFS = $(EMAROOT)/src/mibs/common/v2-tc.def

EMAINC = -I. -I$(EMAROOT)/sadk/include
#EMALIBS = -L$(EMAROOT)/objdir/solaris_sun4.lib \
#        -lsubagent -lagent -lemanate -lpack -lmibtable -lsnmp 
EMALIBS = -L$(EMAROOT)/common//linux_x86_64.lib \
        -lmib2 -lsubagent -lagent -lpack -lemanate -lconn_basic -ldevkit -lmibtree


#IPMSBOOM=/proj/ipms/ipmsboom/R1A
IPMSBOOM=/vobs/ipms/ipms_sys2/ipmsboom
IPMSBOOMINC=${IPMSBOOM}/include
IPMSBOOMLIB=${IPMSBOOM}/lib
IPMSBOOMLIBS=-lipms

MIB = ntf

# Defines to enable Emanate find the MIB dependent files.
# We have read-write variables to SET.

EMADEFS = -DMIBDEFS=\"$(MIB)defs.h\" \
                -DMIBPART=\"$(MIB)part.h\" \
                -DMIBSUPP=\"$(MIB)supp.h\" \
                -DMIBTYPE=\"$(MIB)type.h\" \
                -DMIBOID=\"$(MIB)oid.cpp\" \
                -DSETS -DEMANATE -DSUBAGENT -DSR_UDS_IPC 

EMAOBJS =$(MIB)agt.o k_$(MIB).o v_$(MIB).o $(MIB)oid.o \
         oidl.o $main.o

OBJEXT=.o
OBJS	= $(EMAOBJS) 
#FLAGS = -mt +w -D_REENTRANT
FLAGS = -g -fPIC
LIBS = -L$(IPMSBOOMLIB) $(IPMSBOOMLIBS) $(EMALIBS) -lnsl -lpthread -lrt
INC=-I$(TOPLEVEL)/src/include -I$(IPMSBOOMINC) $(EMAINC)
CC = g++
BINDIR=$(TOPLEVEL)/bin
NTFBIN=$(BINDIR)/ntfagent
BIN_SRC=$(shell cat $(TOPLEVEL)/src/ntfagent/ntfagent.list )


CPP_OBJ=$(subst .cpp,$(OBJEXT),$(filter %.cpp, $(foreach file, $(BIN_SRC),$(OBJDIR)/$(file))))

all: dirs ntfagent stop
	cp ntfagent $(BINDIR) && cp stopntf $(BINDIR)

	#/usr/local/rational/releases/PurifyPlusFamily.2003a.06.00.Beta.0109/sun4_solaris2/bin/purify $(CC) $(FLAGS) -o $(NTFBIN) $(CPP_OBJ) $(LIBS)
ntfagent: $(CPP_OBJ)
	$(CC) $(FLAGS) -o ntfagent $(CPP_OBJ) $(LIBS)

$(CPP_OBJ): $(OBJDIR)/%.o : $(TOPLEVEL)/src/ntfagent/%.cpp
	$(CC) $(FLAGS) $(INC) $(EMADEFS) -c $< -o $@

testclient: dirs $(OBJDIR)/testclient.o $(OBJDIR)/UnixSocket.o
	$(CC) $(FLAGS) -o $(BINDIR)/testclient $(OBJDIR)/testclient.o $(OBJDIR)/UnixSocket.o $(LIBS)

$(OBJDIR)/testclient.o:
	$(CC) $(FLAGS) $(INC) -c $(TOPLEVEL)/src/ntfagent/testclient.cpp -o $(OBJDIR)/testclient.o

testserver: dirs $(OBJDIR)/testserver.o $(OBJDIR)/UnixSocket.o
	$(CC) $(FLAGS) -o $(BINDIR)/testserver $(OBJDIR)/testserver.o $(OBJDIR)/UnixSocket.o $(LIBS)

$(OBJDIR)/testserver.o:
	$(CC) $(FLAGS) $(INC) -c $(TOPLEVEL)/src/ntfagent/testserver.cpp -o $(OBJDIR)/testserver.o

stop: dirs $(OBJDIR)/stop.o
	$(CC) $(FLAGS) -o stopntf $(OBJDIR)/stop.o $(OBJDIR)/UnixSocket.o $(OBJDIR)/stringutils.o $(OBJDIR)/Logger.o $(LIBS)

$(OBJDIR)/stop.o:
	$(CC) $(FLAGS) $(INC) -c $(TOPLEVEL)/src/ntfagent/stop.cpp -o $(OBJDIR)/stop.o

clean:
	/bin/rm -rf $(OBJDIR) >/dev/null 2>&1 ; /bin/rm ntfagent >/dev/null 2>&1 ; /bin/rm stopntf >/dev/null 2>&1 ; /bin/rm -rf SunWS_cache >/dev/null 2>&1

#$(MIB): $(MIB).def
#       postmosy -o $(MIB) -agent -traps $(MIB).def $(MIBDEFS)

#$(MIB).def: $(MIB).my
#       mosy $(MIB).my

#$(MIB).my: $(MIB).mib
#       premosy $(MIB).mib $(MIB).my
