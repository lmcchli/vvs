#!/bin/sh
#
# This script creates an Application delivery file 
# for a MAS component
#

STARTDIR=`pwd`

if [ -n "$1" ]; then
    PROPFILE=$1
else
    PROPFILE=./app_properties.cfg
fi
if [ ! -f $PROPFILE ];then
    echo "$PROPFILE not found!"
    echo "usage: $0 [<apppropertiesfile>]"
    exit 1
fi

# The directory where the Application delivery temporarily 
# will be stored.
TMPDIR=./apptmp


abort(){
    echo "Aborting!"
    cleanup
    exit 1
}

# Get this Application properties
get_application_properties() {
    echo "Gather Application Package properties ..."
    VF=`sed -n 's/vxmlfiles=//p' $PROPFILE`
    CF=`sed -n 's/ccxmlfiles=//p' $PROPFILE`
    if [ -z "$CF" ]; then
	echo "Error in $PROPFILE: ccxmlfiles is not defined"
	abort
    fi
    EF=`sed -n 's/ecmafiles=//p' $PROPFILE`
    CoF=`sed -n 's/configfiles=//p' $PROPFILE`
    NF=`sed -n 's/numberanalysisfile=//p' $PROPFILE`
    EDF=`sed -n 's/eventdefinitionfile=//p' $PROPFILE`
    ETF=`sed -n 's/eventtemplatefiles=//p' $PROPFILE`
    CUSTOMER=`sed -n 's/customer=//p' $PROPFILE`
    CUSTOMER=`echo "$CUSTOMER" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'` 
    NAME=`sed -n 's/name=//p' $PROPFILE`
    NAME=`echo "$NAME" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
    if [ -z "$NAME" ]; then
	echo "Error in $PROPFILE: name is not defined"
	abort
    fi
    
    PRODUCTID=`sed -n 's/productid=//p' $PROPFILE`
    PRODUCTID=`echo "$PRODUCTID" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
    if [ -z "$PRODUCTID" ]; then
	echo "Error in $PROPFILE: productid is not defined"
	abort
    fi
    
    RSTATE=`sed -n 's/rstate=//p' $PROPFILE`
    RSTATE=`echo "$RSTATE" | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
    if [ -z "$RSTATE" ]; then
	echo "Error in $PROPFILE: rstate is not defined"
	abort
    fi
    SERVICE=`sed -n 's/service=//p' $PROPFILE`
    if [ -z "$SERVICE" ]; then
	echo "Error in $PROPFILE: no services are defined"
	abort
    fi

    APPID=$NAME.$CUSTOMER.$RSTATE.$PRODUCTID
    echo "About to create $APPID Application Package!"
}

# Copy the files to the correct directory
copy_files(){
    echo "Copy files to temporary directory ..."
    mkdir $TMPDIR
    mkdir $TMPDIR/vxml
    mkdir $TMPDIR/ccxml
    mkdir $TMPDIR/ecma
    mkdir $TMPDIR/cfg
    mkdir $TMPDIR/numberanalysis
    mkdir $TMPDIR/eventdefinition
    mkdir $TMPDIR/eventtemplates
    mkdir $TMPDIR/etc
	# check that if files are specified they
	# really exists before trying to copy them
    if [ -n "$VF" ]; then
	if [ ! -f $VF ]; then
	    echo "Error in $PROPFILE: voicexmlfiles ($VF) not found"
	    abort
	fi
	validatevxml $VF
	cp $VF $TMPDIR/vxml
	chmod +w $TMPDIR/vxml/*
    fi
    
    if [ -n "$CF" ]; then
	if [ ! -f $CF ]; then
	    echo "Error in $PROPFILE: ccxmlfiles ($CF) not found"
	    abort
	fi
	validateccxml $CF
	cp $CF $TMPDIR/ccxml
	chmod +w $TMPDIR/ccxml/*
    fi
    if [ -n "$EF" ]; then
	if [ ! -f $EF ]; then
	    echo "Error in $PROPFILE: ecmafiles ($EF) not found"
	    abort
	fi
	validateecma $EF
	cp $EF $TMPDIR/ecma
	chmod +w $TMPDIR/ecma/*
    fi
    if [ -n "$CoF" ]; then
	if [ ! -f $CoF ]; then
	    echo "Error in $PROPFILE: configfiles ($CoF) not found"
	    abort
	fi
	cp $CoF $TMPDIR/cfg
	chmod +w $TMPDIR/cfg/*
    fi
    if [ -n "$NF" ]; then
	if [ ! -f $NF ]; then
	    echo "Error in $PROPFILE: numberanalysisfile ($NF) not found"
	    abort
	fi
	cp $NF $TMPDIR/numberanalysis
	chmod +w $TMPDIR/numberanalysis/*
    fi
    if [ -n "$EDF" ]; then
	if [ ! -f $EDF ]; then
	    echo "Error in $PROPFILE: eventdefinitionfile ($EDF) not found"
	    abort
	fi
	cp $EDF $TMPDIR/eventdefinition
	chmod +w $TMPDIR/eventdefinition/*
    fi
    if [ -n "$ETF" ]; then
	if [ ! -f $ETF ]; then
	    echo "Error in $PROPFILE: eventtemplatefiles ($ETF) not found"
	    abort
	fi
	cp $ETF $TMPDIR/eventtemplates
	chmod +w $TMPDIR/eventtemplates/*
    fi

    # these files is needed for deployment server installation of the application.
    cp ./action_mas_package $TMPDIR 
    cp ./job_mas_package $TMPDIR 

    # Create the properties file to be included in the delivery and use 
    # only relevant information from the properties file
    # and add a packagetype identifier
    echo "package=amcpm_application" > $TMPDIR/etc/properties.cfg
    cat $PROPFILE | egrep "^name=|^customer=|^productid=|^rstate=|^service=" >> $TMPDIR/etc/properties.cfg
}

make_delivery(){
    echo "Make delivery file ..."
    tar cvf $APPID.tar $TMPDIR
}

validatevxml() {
# parameters are a list of files or filepath to the VoiceXML files
# TODO
    tmp=1
}

validateccxml() {
# parameters are a list of files or filepath to the CCXML files
# TODO
    tmp=1
}

validateecma() {
# parameters are a list of files or filepath to the ECMA files
# TODO
    tmp=1
}

cleanup() {
        echo "Cleaning up ..."
	rm -rf $TMPDIR
}

finished() {
    echo "Delivery file created, can be found `pwd`/$APPID.tar!"
    exit 0
}

############################
#         MAIN             #
############################
get_application_properties
copy_files
make_delivery
cleanup
finished
