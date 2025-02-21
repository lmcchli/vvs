#!/bin/sh 
#
# This script creates a Media Content Package delivery file 
# for a MAS component
#

STARTDIR=`pwd`

if [ -n "$1" ]; then
    PROPFILE=$1
else
    PROPFILE=./mcp_properties.cfg
fi
if [ ! -f $PROPFILE ];then
    echo "$PROPFILE not found!"
    echo "usage: $0 [<mcppropertiesfile>]"
    exit 1
fi


# The directory where the Application delivery temporarily 
# will be stored.
TMPDIR=./mediatmp


# The operating system

abort(){
    echo "Aborting!"
    cleanup
    exit 1
}

# Get this Media Content Package properties
get_mcp_properties() {
	echo "Gather Media Content Package properties ..."
	MF=`sed -n 's/mediafiles=//p' $PROPFILE`
	MCF=`sed -n 's/mediacontentfiles=//p' $PROPFILE`
	MOF=`sed -n 's/mediaobjectfiles=//p' $PROPFILE`
	GF=`sed -n 's/grammarfile=//p' $PROPFILE`
	CUSTOMER=`sed -n 's/customer=//p' $PROPFILE`
	CUSTOMER=`echo "$CUSTOMER" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	NAME=`sed -n 's/name=//p' $PROPFILE`
	NAME=`echo "$NAME" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	if [ -z "$NAME" ]; then
	    echo "Error in $PROPFILE: name is not defined"
	    abort
	fi
	TYPE=`sed -n 's/type=//p' $PROPFILE`
	TYPE=`echo "$TYPE" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	if [ -z "$TYPE" ]; then
	    echo "Error in $PROPFILE: type is not defined"
	    abort
	fi

	PRODUCTID=`sed -n 's/productid=//p' $PROPFILE`
	PRODUCTID=`echo "$PRODUCTID" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	if [ -z "$PRODUCTID" ]; then
	    echo "Error in $PROPFILE: productid is not defined"
	    abort
	fi
	RSTATE=`sed -n 's/rstate=//p' $PROPFILE `
	RSTATE=`echo "$RSTATE" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	if [ -z "$RSTATE" ]; then
	    echo "Error in $PROPFILE: rstate is not defined"
	    abort
	fi
	LANGUAGE=`sed -n 's/lang=//p' $PROPFILE`
	LANGUAGE=`echo "$LANGUAGE" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	if [ -z "$LANGUAGE" ]; then
	    echo "Error in $PROPFILE: language is not defined"
	    abort
	fi
	VOICEVARIANT=`sed -n 's/voicevariant=//p' $PROPFILE`
	VOICEVARIANT=`echo "$VOICEVARIANT" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	VIDEOVARIANT=`sed -n 's/videovariant=//p' $PROPFILE`
	VIDEOVARIANT=`echo "$VIDEOVARIANT" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	AUDIOENCODING=`sed -n 's/audioencoding=//p' $PROPFILE`
	AUDIOENCODING=`echo "$AUDIOENCODING" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	VIDEOENCODING=`sed -n 's/videoencoding=//p' $PROPFILE`
	VIDEOENCODING=`echo "$VIDEOENCODING" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
	# Check that one of voice and video variant is specified.
	if [ -n "$VOICEVARIANT" ]; then
	    if [ -z "$VIDEOVARIANT" ]; then
		if [ -n "$AUDIOENCODING" ]; then
		    VARIANT=$VOICEVARIANT
		else
		    echo "Error in $PROPFILE: audioencoding must be specified for a voice variant"
		    abort
		fi
	    else
		echo "Error in $PROPFILE: It is not possible to specify both voicevariant and videovariant"
		abort 
	    fi
	else
	    if [ -n "$VIDEOVARIANT" ]; then
		if [ -z "$VIDEOENCODING" -o -z "$AUDIOENCODING" ]; then
		    echo "Error in $PROPFILE: Both videoencoding and audioencoding must be specified for a video variant"
		    abort 
		fi
		VARIANT=$VIDEOVARIANT
	    else
		echo "Error in $PROPFILE: Either voicevariant or videovariant must be specifed"
		abort
	    fi
	fi

	MCPID=$NAME.$TYPE.$LANGUAGE.$VARIANT.$CUSTOMER.$RSTATE.$PRODUCTID
	echo "About to create $MCPID Media Content Package!"

}

# Copy the files to the correct directory
copy_files(){
	echo "Copy files to temporary directory ..."
	mkdir $TMPDIR
	mkdir $TMPDIR/media
	mkdir $TMPDIR/mediacfg
	mkdir $TMPDIR/grammar
	mkdir $TMPDIR/etc
	# check that if files are specified that they
	# really exists before trying to copy them
	if [ -n "$MF" ]; then
	    if [ ! -f $MF ]; then
		echo "Error in $PROPFILE: mediafiles ($MF) not found"
		abort
	    fi
	    cp $MF $TMPDIR/media 
	else
	    echo "Error in $PROPFILE: mediafiles not specified"
	    abort
	fi
	if [ -n "$MCF" ]; then
	    if [ ! -f $MCF ]; then
		echo "Error in $PROPFILE: mediacontentfiles ($MCF) not found"
		abort
	    fi
	    cp $MCF $TMPDIR/mediacfg
	else
	    echo "Error in $PROPFILE: mediacontentfiles not specified"
	    abort  
	fi
	if [ -n "$MOF" ]; then
	    if [ ! -f $MOF ]; then
		echo "Error in $PROPFILE: mediaobjectfiles ($MOF) not found"
		abort
	    fi
	    cp $MOF $TMPDIR/mediacfg
	else
	    echo "Error in $PROPFILE: no mediaobjectfiles specified"
	    abort
	fi
	if [ -n "$GF" ]; then
	    if [ ! -f $GF ]; then
		echo "Error in $PROPFILE: grammarfile ($GF) not found"
		abort
	    fi
	    cp $GF $TMPDIR/grammar
	    else
	    echo "Information: no grammarfile specified"
	fi
	# Add the package type identifier
	echo "package=amcpm_mediacontent" > $TMPDIR/etc/properties.cfg
	# Add the relevant parts from the properties file
	cat $PROPFILE                   |
		egrep -v "mediafiles"       |
		egrep -v "mediacontentfiles" |
		egrep -v "mediaobjectfiles"  |
		egrep -v "^#"                |  
		egrep -v "grammarfile" >> $TMPDIR/etc/properties.cfg
		
    	# these files is needed for deployment server installation of the application.
    	cp ./action_mas_package $TMPDIR 
    	cp ./job_mas_package $TMPDIR 

}

# Workaround for TR 31568, remove this call when the TR has been correctly
# solved (ekensel)
fix_files(){
  for FILE in $MCF
  do
    F=`basename $FILE`
    MCFFILE=$TMPDIR/mediacfg/$F
    TMPFILE1="$MCFFILE".tmp.1
    TMPFILE2="$MCFFILE".tmp.2
    chmod +w $MCFFILE
    echo >> $MCFFILE # sed requires newline at end of file
    rm -f $TMPFILE1 $TMPFILE2
    cat $MCFFILE | sed 's/Masculine/Male/g' > $TMPFILE1
    cat $TMPFILE1 | sed 's/Feminine/Female/g' > $TMPFILE2
    cat $TMPFILE2 | sed 's/Neuter/None/g' > $TMPFILE1
    mv $TMPFILE1 $MCFFILE
  done
}

make_delivery(){
	echo "Make delivery file ..."
	tar cvf $MCPID.tar $TMPDIR
}

cleanup() {
	rm -rf $TMPDIR
}

finished() {
    echo "Delivery file created, can be found `pwd`/$MCPID.tar!"
    exit 0
}

############################
#         MAIN             #
############################
get_mcp_properties
copy_files
fix_files
make_delivery
cleanup
finished
