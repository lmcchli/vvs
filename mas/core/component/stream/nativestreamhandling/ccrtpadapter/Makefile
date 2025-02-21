VOB_HOME=../../../..
# Determing a type name of the targeted system/architecture
include ../../makefiles/architecture.mk

# Including target specific variables
include ../../makefiles/variables.${SYSTEM}.mk

# The name of the generated library
TARGET_NAME=ccrtpadapter

# Some specific defines
#ifeq '$(SYSTEM)' 'SunOS.5.10'
#DEFS+=-DBSD_COMP -Wno-deprecated
#endif


#ifeq '$(SYSTEM)' 'SunOS.5.8'
#DEFS+=-DBSD_COMP -Wno-deprecated
#endif

#DEFS+=-Wall -Wno-deprecated

# Defining where to find include files
INCDIRS+=$(BOOST_INCDIR)
INCDIRS+=$(COMPONENT_INCDIRS)
INCDIRS+=./include
INCDIRS+=$(QUICKTIME_INCDIR)
INCDIRS+=$(JNI_INCDIRS)
INCDIRS+=$(RTSAFE_INCDIR)
INCDIRS+=$(LOGGING_INCDIR)
INCDIRS+=$(CCRTP_INCDIR)
INCDIRS+=../medialibrary/include
INCDIRS+=../util/include
INCDIRS+=../logging/include
INCDIRS+=../jniadapter/include
INCDIRS+=./include

# Where the soure files are found
SRC_DIR=src

# The ccrtpadapter source files
SRC+=playjob.cpp
SRC+=stackeventdispatcher.cpp 
SRC+=rtppayload.cpp streamutil.cpp
SRC+=recordingproperties.cpp recordjob.cpp 
SRC+=streamconfiguration.cpp
SRC+=voiceactivitydetector.cpp  comfortnoisegenerator.cpp
SRC+=audiomediadata.cpp
SRC+=streamcontentinfo.cpp controltoken.cpp 
SRC+=dtmfreceiver.cpp dtmfsender.cpp
SRC+=streamrtpsession.cpp 
SRC+=streamconnection.cpp
SRC+=sessionsupport.cpp outboundsession.cpp inboundsession.cpp
SRC+=javamediastream.cpp
SRC+=videomediadata.cpp
SRC+=Processor.cpp InputProcessor.cpp OutputProcessor.cpp
SRC+=Player.cpp Recorder.cpp Command.cpp
SRC+=AutoGlobal.cpp StreamMixer.cpp
SRC+=Callback.cpp CallbackQueue.cpp CallbackQueueHandler.cpp
SRC+=silencedetector.cpp pcmconversiontable.cpp
SRC+=RTPHandler.cpp RTPHandlerFactory.cpp RTPAudioHandler.cpp RTPVideoHandler.cpp
SRC+=AMRRTPHandler.cpp AMRWBRTPHandler.cpp PCMRTPHandler.cpp H263RTPHandler.cpp

# The home of all (our) libraries
LIBDIR=../lib
DISTLIBDIR=$(MAS_HOME)/lib

# Obtaining object file names from source file names
OBJS := $(SRC)
OBJS := $(OBJS:%.cpp=%.o)
OBJS := $(OBJS:%.c=%.o)
# Obtaining a list of object files associated with path
PATHOBJS := $(addprefix $(OBJDIR)/,$(notdir $(OBJS)))

# Defining where make can find/search files
vpath %.o $(OBJDIR)
vpath %.cpp $(SRC_DIR)

include ../../makefiles/libraryrules.mk
