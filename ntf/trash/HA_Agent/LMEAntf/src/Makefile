# Sun Cluster Data Services Builder template version 1.0
#
# Makefile for data service: ntf
#

# Set this to match the path to where your C compiler binary is.
CC=cc
LDFLAGS += -L/usr/cluster/lib -R/usr/cluster/lib
INCLFLAGS = -I/usr/cluster/include -I/usr/cluster/include/rgm
ifneq ($(VERSION),)
V="-v $(VERSION)"
endif

LDLIBS	+= -lc -lnsl -lsocket -lresolv -ldsdev  -ldl

# System commands
RMCMD=/usr/bin/rm
MVCMD=/usr/bin/mv
PKGMKCMD=/usr/bin/pkgmk

WORKING_DIR=../..

# Source and header files for ntf
INCL = ntf.h
COMMON_SRCS = ntf.c
PROG_SRCS = ntf_svc_start.c \
	ntf_svc_stop.c \
	ntf_validate.c \
	ntf_update.c \
	ntf_monitor_start.c \
	ntf_monitor_stop.c \
	ntf_monitor_check.c \
	ntf_probe.c 

# objects
COMMON_OBJS = $(COMMON_SRCS:%.c=%.o)
OBJS = $(PROG_SRCS:%.c=%.o)

PROGS = $(PROG_SRCS:%.c=%)

# Make targets

all: $(PROGS)

# Make install rule will install ntf into WORKING_DIR
pkg: all
	$(PKGMKCMD) $V -o -d ../pkg -f ../etc/prototype -b $(WORKING_DIR)

$(COMMON_OBJS):	$(COMMON_SRCS) $(INCL)
	$(CC) -c ntf.c $(INCLFLAGS)

$(OBJS): $(PROG_SRCS) 
	$(CC) -c $(@:%.o=%.c) $(INCLFLAGS)

$(PROGS): $(COMMON_OBJS) $(OBJS)
	$(CC) $(@:%=%.o) $(COMMON_OBJS) -o $@ $(LDFLAGS) $(LDLIBS)
	$(MVCMD) $@ ../bin

clean:
	$(RMCMD) -f $(OBJS) $(PROGS) $(COMMON_OBJS)

