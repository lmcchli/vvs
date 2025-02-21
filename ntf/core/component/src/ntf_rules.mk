#This file contains all rules for building the module test1.
#----------------------------------------------------------------------------
#                    Platform specific sections for module.
#----------------------------------------------------------------------------

# Global rules for NTF


# Definitions for Solaris 2.6
ifeq ($(PLATFORM),SOL_2_6))

endif


# Definitions for Solaris 2.51
# Solaris 2.51 not supported


# Definitions for Solaris 2.7
# Solaris 2.7 not supported


# Definitions for Windows NT
ifeq ($(PLATFORM),NT_4))

# Put your C Compiler flags here
    NTF_CCFLAGS     :=

# Put your C++ Compiler flags here
    NTF_CXXFLAGS    :=

# Put your Link time flags and directories here.
    NTF_LDFLAGS    :=
endif


PROD_NUM	:=  cxc109084
VER_NUM		:=  1.0

# Distribution file name. This is the tar'ed and compressed file
# that will be placed in the distribution directory.

# Native distribution name
NTF_DISTNAME	:= ntf-$(PROD_NUM)-$(VER_NUM)-$(PLATFORM)$(TAREXT)

ifeq ($(strip $(PLATFORM)),NT_4)
    NTF_SHLIB_VERS =
else
    NTF_SHLIB_VERS	= .1.0.0
endif
