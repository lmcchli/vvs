# Determing a type name of the targeted system/architecture
include ../../makefiles/architecture.mk

# Including target specific variables
include ../../makefiles/variables.$(SYSTEM).mk

# The name of the generated library
TARGET_NAME=jniadaptermock

# Defining where to find include files
INCDIRS+=./include
INCDIRS+=../interface
INCDIRS+=../util/include
INCDIRS+=../ccrtpadapter/include
INCDIRS+=../jniadapter/include
INCDIRS+=../logging/include
INCDIRS+=$(COMPONENT_INCDIRS)
INCDIRS+=$(JNI_INCDIRS)
INCDIRS+=$(BOOST_INCDIR)
INCDIRS+=$(LOGGING_INCDIR)

# Where the soure files are found
SRC_DIR=src

# The medialibrary source files
SRC+=MockCCRTPSession.cpp 
SRC+=MockNativeStreamHandling.cpp
SRC+=MockObject.cpp MockMediaStream.cpp 
SRC+=MockRTPPayload.cpp MockStreamContentInfo.cpp MockStackEventNotifier.cpp
SRC+=MockStreamConfiguration.cpp
SRC+=MockRecordingProperties.cpp

SRC+=MockMediaObject.cpp MockMediaObjectNativeAccess.cpp
SRC+=MockMediaProperties.cpp
SRC+=MockMediaObjectIterator.cpp MockLengthUnit.cpp
SRC+=MockByteBuffer.cpp MockMediaLength.cpp
SRC+=MockInputStream.cpp MockMimeType.cpp

SRC+=MockJavaVM.cpp MockJNIUtil.cpp


# The home of all (our) libraries
LIBDIR=../lib

# Obtaining object file names from source file names
OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
# Obtaining a list of object files associated with path
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

TARGET_DIST_LIBRARY = $(TARGET_ARCHIVE)

# Defining where make can find/search files
vpath %.o $(OBJDIR)
vpath %.cpp $(SRC_DIR)

include ../../makefiles/libraryrules.mk
