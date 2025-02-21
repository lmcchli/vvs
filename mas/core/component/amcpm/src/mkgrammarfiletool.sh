#!/bin/sh 
#
# This script creates a Media Content Package delivery file 
# for a MAS component
#

STARTDIR=`pwd`

build() {
	echo "### Build media content manager ###"
	cd $STARTDIR/../../mediacontentmanager/
	ant distjar
	cd $STARTDIR
}	

copy() {
	echo "### Copy files ###"
	mkdir $STARTDIR/tmpdir
	mkdir $STARTDIR/tmpdir/lib
	cp $STARTDIR/grammarFileTool.sh $STARTDIR/tmpdir
	cp $STARTDIR/mobeon_log.xml $STARTDIR/tmpdir/lib
	cp $STARTDIR/../../lib/mobeon_media_content_manager.jar $STARTDIR/tmpdir/lib
	cp $STARTDIR/../../lib/mobeon_logging.jar $STARTDIR/tmpdir/lib
	cp $STARTDIR/../../lib/log4j-1.2.9.jar $STARTDIR/tmpdir/lib
	
}

create_zip() {

	
	echo "### Create zipfile ###"
	cd $STARTDIR/tmpdir
	zip -r grammarFileTool *
	
        if [ -f "$STARTDIR/../grammarFileTool.zip" ]; then           
        	cleartool co -nc $STARTDIR/../grammarFileTool.zip
        fi
	echo "Save zipfile"
	cp $STARTDIR/tmpdir/grammarFileTool.zip $STARTDIR/..
	cleartool ci -nc $STARTDIR/../grammarFileTool.zip
	cd $STARTDIR
	echo
	echo "   Zip file:grammarFileTool.zip"
	
}

clenup() {
	rm -rf $STARTDIR/tmpdir
}

build
copy
create_zip
clenup





