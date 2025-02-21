#!/bin/sh
#
# This script creates an AMCPM delivery file 
# Remember to set the correct config spec

STARTDIR=`pwd`

if [ -n "$1" ]; then
        echo "Wrong input!"
        echo "Usage:"
        echo "$0"
        echo "Try again!"
        exit 1
fi


# Some global constants.
AMCPM=/vobs/ipms/mas/amcpm
MCM=/vobs/ipms/mas/mediacontentmanager
LOGGING=/vobs/ipms/mas/logging
TMPDIR=./amcpm
PRODID=swu0044.1
PRODNAME=amcpm
OS=solaris10
MAKEPROMPTVERSION=1.3

abort(){
    echo "Aborting!"
    cleanup
    exit 1
}

# Prompt for R-state and allow only valid filename characters.
enter_build_info(){
    clear
    echo "Createing AMCPM Building package"
    echo "*******************************"
    echo ""
    echo "Enter R-State (e.g R1A or R1A_12): \c"
    read RSTATE
    RSTATE=`echo "$RSTATE" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'| /usr/bin/tr '[:lower:]' '[:upper:]' `

    RSTATE_LOWER=`echo $RSTATE | /usr/bin/tr '[:upper:]' '[:lower:]'`
    AMCPMID=$PRODNAME.$RSTATE_LOWER.$PRODID.$OS
    LABEL=MAS_AMCPM_$RSTATE
    SHOULD_LABEL=`ckkeywd -Q -d y -p "Label with $LABEL after successful build? (default: y)" -e "Not a valid choice" y n` || exit $?
}

# Compile grammar tool file
compile() {
	echo "Compiling logging utility ..."
	cd $LOGGING
	ant distjar
	if [ $? -ne 0 ]; then
		echo "Compilation failed!"
		abort
	fi

	echo "Compiling grammar file tool ..."
	cd $MCM
	ant distjar
	if [ $? -ne 0 ]; then
		echo "Compilation failed!"
		abort
	fi
}

# Copy files to the directory structure
# wanted after installation
copy_files(){
	echo "Copy files to temporary directory ..."
	cd $AMCPM
	rm -rf $TMPDIR
	mkdir $TMPDIR
	mkdir -p $TMPDIR/tools/grammarfiletool/lib
	mkdir -p $TMPDIR/tools/grammarfiletool/log
	mkdir -p $TMPDIR/tools/makeprompt
	cp $AMCPM/src/action_mas_package $TMPDIR  # Needed for Deployment server installation
	cp $AMCPM/src/job_mas_package $TMPDIR     # Needed for Deployment server installation
	cp $AMCPM/src/mkapp.sh $TMPDIR 
	cp $AMCPM/src/mkmcp.sh $TMPDIR 
	cp $AMCPM/src/app_properties.cfg $TMPDIR 
	cp $AMCPM/src/mcp_properties.cfg $TMPDIR 
	cp $AMCPM/src/grammarFileTool.sh $TMPDIR/tools/grammarfiletool
	cp $AMCPM/../lib/mobeon_media_content_manager.jar $TMPDIR/tools/grammarfiletool/lib
	cp $AMCPM/../lib/mobeon_logging.jar $TMPDIR/tools/grammarfiletool/lib
	cp $AMCPM/../lib/log4j-1.2.9.jar $TMPDIR/tools/grammarfiletool/lib
	cp $AMCPM/../lib/mobeon_log.xml $TMPDIR/tools/grammarfiletool/lib
	cp /proj/ipms/makeprompt/win32/$MAKEPROMPTVERSION/makeprompt.exe $TMPDIR/tools/makeprompt
	chmod +w $TMPDIR/*_properties.cfg
	chmod 755 $TMPDIR/tools/grammarfiletool/grammarFileTool.sh
	cp $AMCPM/doc/README $TMPDIR 
	echo "Version=${RSTATE}" > $TMPDIR/VERSION
}

# Make the delivery and name the delivery file with the product name,
# id and R-state.
make_delivery(){
	echo "Make delivery file ..."
	cd $AMCPM
	tar cvf $AMCPMID.tar $TMPDIR
	if [ $? != 0 ]; then
	    echo "Making tar file failed!"
	    abort
	fi
}

# Create a label in the vob if requested by the user
do_label(){
    if [ "$SHOULD_LABEL" = "y" ];then 
	echo
	echo "**************************************************"
	echo "Attaching label $LABEL. Please wait... "
	cd $AMCPM
	cleartool lstype -s -kind lbtype | grep -w $LABEL > /dev/null
	if [ ! $? = 0 ];then
	    cleartool mklbtype -c "Label for $LABEL files"  $LABEL > /dev/null
	fi
	cleartool lslock -s | grep -w $LABEL > /dev/null
	if [ $? = 0 ];then
	    cleartool unlock lbtype:$LABEL > /dev/null
	fi
	cleartool mklabel -replace -recurse $LABEL $AMCPM/src/mkapp.sh \
	    $AMCPM/src/mkmcp.sh \
	    $AMCPM/src/grammarFileTool.sh \
	    $AMCPM/mkdeliv.sh \
	    $MCM/src/com/mobeon/masp/mediacontentmanager/grammar/grammarfiletool/GrammarFileTool.java \
            $AMCPM/src/app_properties.cfg $AMCPM/src/mcp_properties.cfg \
            $AMCPM/doc/README > /dev/null 2>&1
	if [ ! $? = 0 ];then
	    echo
	    error "Labeling failed. Exiting."
	    abort
	fi
	echo "Labeling done!"
	echo "Labeling complete and label locked!"
    fi
}

cleanup() {
	rm -rf $TMPDIR
}

finished() {
    echo "Delivery file created, can be found `pwd`/$AMCPMID.tar!"
    exit 0
}

############################
#         MAIN             #
############################
enter_build_info
compile
copy_files
make_delivery
do_label
cleanup
finished
