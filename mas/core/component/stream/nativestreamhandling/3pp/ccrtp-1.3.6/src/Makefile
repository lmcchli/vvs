SUNWSPRO=/usr/local/Studio8/SUNWspro
CC=$(SUNWSPRO)/bin/cc
CXX=$(SUNWSPRO)/bin/CC
LD=$(CXX)
DEFS=-DHAVE_CONFIG_H -D_POSIX_PTHREAD_SEMANTICS -D_REENTRANT -DBSD_COMP
CFLAGS=$(DEFS) -O3 -mt -KPIC -DPIC
CXXFLAGS=$(DEFS) -O3 -mt -KPIC -DPIC
INCL=-I. -I. -I../../commoncpp2-1.3.19/include

SOURCES=control.cpp data.cpp duplex.cpp incqueue.cpp members.cpp \
outqueue.cpp pool.cpp queue.cpp rtcppkt.cpp rtppkt.cpp socket.cpp source.cpp

LIBDIR=../../../lib
OBJDIR=obj_sol_8

LIBS=-L$(LIBDIR) -lccgnu2

OBJS := $(notdir $(SOURCES))
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
PATHOBJS := $(addprefix $(OBJDIR)/,$(OBJS))

VPATH+=$(OBJDIR) $(LIBDIR)

.SUFFIXES: .c .cpp .o

.c.o:
	$(CC) $(TEMPLATEFLAGS) $(CFLAGS) $(INCL) -c $< -o $(OBJDIR)/$(@F)

.cpp.o:
	$(CXX) $(TEMPLATEFLAGS) $(CXXFLAGS) $(INCL) -c $< -o $(OBJDIR)/$(@F)

all: dirs $(LIBDIR)/libccrtp.so

clean:
	-rm -f $(LIBDIR)/libccrtp.so
	-rm -rf $(OBJDIR) $(LIBDIR)/SunWS_cache

rebuild: clean all

$(LIBDIR)/libccrtp.so: $(OBJS)
	$(CXX) $(CXXFLAGS) $(TEMPLATEFLAGS) $(LDFLAGS) -G -PIC \
		-o $@ $(PATHOBJS) $(LIBS)

dirs:
	@test -d $(OBJDIR) || mkdir $(OBJDIR)
	@test -d $(LIBDIR) || mkdir $(LIBDIR)
