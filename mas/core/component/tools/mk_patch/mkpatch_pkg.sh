#!/bin/ksh
#

PRIVATE_BUILD=""
NO_JAR_BUILD=""
FILE_LIST=""

export DATA=""
args=$@

for vars in $args
do
        case "$vars" in
        -p)
                PRIVATE_BUILD="TRUE"
                ;;
        -nojarbuild)
                NO_JAR_BUILD="TRUE"
                ;;
	*)
		echo "Unknown option: $vars"
		echo "Usage:"
		echo " $0 [-p] [-nojarbuild]"
		exit 1
		;;
        esac
done


#
#	Setup environment
#
doSetupEnv()
{
	echo "Setting up environment..."
	VOBS_BASEDIR=/vobs/ipms/mas  # vobs base for MAS
	PATCH_FILES=$VOBS_BASEDIR/patches/patch_files.lst # path to where the list of files that shuld be included in the patch
	
	PKG="MOBYmas" 
	BUILDDIR=$VOBS_BASEDIR/$PKG"_patch"   #Directory where the package builder finds necessary files, and where inermediate results can be placed.
	BUILDSRCDIR=$BUILDDIR/src
	PKGNAME="MAS"                         # The short name and description of the application.
	BASEDIR="/apps/mas"	              # Where the base is on target host.	
	OBSOLETES=""
	REQUIRES=""
	INCOMPAT=""

	ERROR=""

}


#
#   Creates a package from patch_build directory
#
doPatchPackage()
{
	echo "Make patch package"
	cd $VOBS_BASEDIR"/"$PKG"_patch"

	VERSION="MAS_"$BASE_VERSION"."$BASE_BUILD
	PATCHID=$PATCH_LABEL

	LOWER_PATCH_LABEL=`echo "$PATCH_LABEL" | awk '{ print tolower($0) }'`
	PATCH_FILENAME=$LOWER_PATCH_LABEL".mas0001.solaris10.zip"
	PATCHFILE=$BUILDDIR"/"$PATCH_FILENAME  # Name and location of the patch zipfile.
	
	#
	# Export variables so that foundation build script can use them
	#
	export PKG
	export BUILDDIR
	export PKGNAME
	export BASEDIR
	export PATCHFILE
	export OBSOLETES    
	export REQUIRES 	
	export INCOMPAT	
	export VERSION
	export PATCHID
	
	echo "PKG = $PKG"
	echo "BUILDDIR = $BUILDDIR"
	echo "PKGNAME = $PKGNAME"
	echo "BASEDIR = $BASEDIR"
	echo "PATCHFILE = $PATCHFILE"
	echo "OBSOLETES = $OBSOLETES"
	echo "REQUIRES = $REQUIRES"
	echo "INCOMPAT = $INCOMPAT"
	echo "VERSION = $VERSION"
	echo "PATCHID = $PATCHID"
	
	# make package
	echo "Make local package..."
	make 
	
	#compile package using foundation	
	echo "Running foundation makepatchpkg..."
	/proj/ipms/pkg/LATEST/patch/makepatchpkg
	
	# copy package file to patchdir
	cp $PATCHFILE $PATCH_DIR

	echo "Patch file can be found at: $PATCHFILE"

}


#
# get patch number
#
doGetPatchNumber()
{
    PATCH_NUMBER=$BUILD
    PATCH_LABEL=$PATCH_ID"_"$COMPONENT"_"$BASE_VERSION"."$PATCH_NUMBER
    LOWER_PATCH_LABEL=`echo "$PATCH_LABEL" | awk '{ print tolower($0) }'`
    PATCH_DIR=$VOBS_BASEDIR/patches/$PATCH_LABEL
    
    set label_exists=`cleartool lstype -short -kind lbtype | grep $PATCH_LABEL`
    if [[ ! -z $label_exists ]]
    then
	echo "WARNING! The patch label $PATCH_LABEL already exists. Maybe you have forgot to increase"
	echo " the patch build number in patch.cfg? Are you sure you want to continue anyway? (y/n) \c"
	read answer
	if [ "$answer" = "y" ] 
	then 
	    echo "Continuing..."
	else
	    echo "Please increase the build number and try again."
	    echo "Patch build aborted. Exiting."
	    exit 0;
	fi
    fi

}


#
#  doCopyFiles
#
doCopyPkgFiles()
{

    echo "Copy package files to dir ${VOBS_BASEDIR}/${PKG}_patch..."
    cd $VOBS_BASEDIR"/"$PKG"_patch"
    
    echo "Copy files..."
    
    echo "   Makefile"	
    cp $VOBS_BASEDIR/tools/mk_patch/Makefile .
    
    # Copy all component specific install/remove scripts
    echo "   Install/remove scripts"
    cp $VOBS_BASEDIR/patches/comp_checkinstall .
    cp $VOBS_BASEDIR/patches/comp_preinstall .
    cp $VOBS_BASEDIR/patches/comp_postinstall .
    cp $VOBS_BASEDIR/patches/comp_checkremove .
    cp $VOBS_BASEDIR/patches/comp_preremove .
    cp $VOBS_BASEDIR/patches/comp_postremove .

}


#
#  doCopyFiles
#
doCopySrcFiles()
{
    echo "Copy source files..."
    if [[ -f $PATCH_DIR/srccopy.sh ]] then
	$PATCH_DIR/srccopy.sh
    fi
}



#
#  Create package directories
#
doCreateDir()
{
    mkdir -p $BUILDDIR
    mkdir -p $BUILDSRCDIR
    mkdir -p $PATCH_DIR

    if [ ! -w $BUILDDIR ] 
    then
	echo " Directory $BUILDDIR not writable."
	exit 1
    fi;

    # Source sub directories
    cd $BUILDSRCDIR
    if [ ! -d "$BUILDSRCDIR/lib" ]; then  mkdir lib; fi
    if [ ! -d "$BUILDSRCDIR/applications" ]; then  mkdir applications; fi
    if [ ! -d "$BUILDSRCDIR/bin" ]; then  mkdir bin; fi
    if [ ! -d "$BUILDSRCDIR/log" ]; then  mkdir log; fi
    if [ ! -d "$BUILDSRCDIR/data" ]; then  mkdir data; fi
    if [ ! -d "$BUILDSRCDIR/etc" ]; then  mkdir etc; fi
    if [ ! -d "$BUILDSRCDIR/tools" ]; then  mkdir tools; fi
    if [ ! -d "$BUILDSRCDIR/tmp" ]; then  mkdir tmp; fi
    if [ ! -d "$BUILDSRCDIR/cfg" ]; then  mkdir cfg; fi
}


doSaveFiles()
{
    cd $VOBS_BASEDIR"/"patches
    
    cp patch.cfg $PATCH_DIR
    cp patch_files.lst $PATCH_DIR
    cp $VOBS_BASEDIR/tools/mk_patch/pkginfo $PATCH_DIR
}



doLableFiles()
{

	if [ "$PRIVATE_BUILD" != "TRUE" ]
	then

		#
		#  
		# labels all files
		# 
		echo "Create label: "$PATCH_LABEL
		cleartool mklbtype $PATCH_LABEL 
		if [ $? -gt 0 ]
		then
		   echo "Error creating label."
		   exit 1
		fi
		
		echo "Label files"
		cleartool mklabel -replace -recurse $PATCH_LABEL $VOBS_BASEDIR > $VOBS_BASEDIR/patches/label.info
		  
	else
		echo "Private build. No labeling"
     	fi
}

doPrintInfo()
{
	echo "   **********************************************"
	echo "   *                                            *"
	echo "   *                                            *"
	echo "   *              PATCH BUILD                   *"    
	echo "   *                                            *"
	echo "   *  This will build a patch to install        *"	
	echo "   *                                            *"
	echo "   *                                            *"
	echo "   *                                            *"
	echo "   **********************************************"
	echo ""
	echo "   Version this patch will be installable on: $BASE_VERSION.$BASE_BUILD"
	echo ""
	echo "   This patch will be named : "$PATCH_LABEL
	echo

	if [ "$PRIVATE_BUILD" = "TRUE" ]; then
	    echo "This is a PRIVATE build, not intended for public release!"
	    echo "The build will not be labeled."
	else
	    echo "This is a RELEASE build, after successful build all files in vob"
	    echo "will be labeled with $PATCH_LABEL."
	fi

	echo

	if [ "$NO_JAR_BUILD" = "TRUE" ]; then
	    echo "The patch build have been started with the NO_JAR_BUILD option. Make sure that all"
	    echo "files included in the patch have been built before proceeding!"
	else
	    echo "Note! Only jar-files included in the patch are automatically built by this script. "
	    echo "Make sure that any other type of files included in the patch have been manually "
	    echo "built before proceeding!"
	fi

	echo
	echo "Are you sure you want to continue? (y/n) \c"
	read answer
	if [ "$answer" = "y" ] ; then
	    echo "Proceeding with patch build..."
	else
	    echo "Patch build aborted. Exiting."
	    echo
	    exit 0;
	fi
	
}


doReadPatchConfig()
{
	echo "Reading patch configuration..."
	file="/vobs/ipms/mas/patches/patch.cfg"
	parseConfig $file
        # set all variables read
	COMPONENT=$_COMPONENT
	CORRECTION=$_CORRECTION
	PATCH_TYPE=$_PATCH_TYPE
	PATCH_ID=$_PATCH_ID
	BUILD=$_BUILD
	BASE_VERSION=$_BASE_VERSION
	BASE_BUILD=$_BASE_BUILD
	SHUTDOWN=$_SHUTDOWN
	STOP=$_STOP
	START=$_START
	UNLOCK=$_UNLOCK
	DESCRIPTION=$_DESCRIPTION
	IMPORTANT_INFO=$_IMPORTANT_INFO
	SYSTEM_IMPACT=$_SYSTEM_IMPACT
	OBSOLETES=$_OBSOLETES
	REQUIRES=$_REQUIRES 
	INCOMPAT=$_INCOMPAT
	DEPENDENCIES=$_DEPENDENCIES
	CONFIGURATION_CHANGES=$_CONFIGURATION_CHANGES
	FIXED_REPORTS=$_FIXED_REPORTS
	PRODUCT_NUMBER=$_PRODUCT_NUMBER

	OBSOLETED_DESCRIPTION=""
	OBSOLETED_FIXED_REPORTS=""
	OBSOLETED_CONFIGURATION_CHANGES=""

	echo "Parsing obsoleted patches..."
	
	for patch in $OBSOLETES
	do
		# get patch file for obsoleted patch
		file="/vobs/ipms/mas/patches/"$patch"/patch.cfg"

		OBSOLETED_DESCRIPTION=$OBSOLETED_DESCRIPTION"$patch:\n"
		OBSOLETED_FIXED_REPORTS=$OBSOLETED_FIXED_REPORTS"$patch:\n"
		OBSOLETED_CONFIGURATION_CHANGES=$OBSOLETED_CONFIGURATION_CHANGES"$patch:\n"

		if [[ -f $file ]] then
		    parseConfig $file
		    OBSOLETED_DESCRIPTION=$OBSOLETED_DESCRIPTION$_DESCRIPTION
		    OBSOLETED_FIXED_REPORTS=$OBSOLETED_FIXED_REPORTS$_FIXED_REPORTS
		    OBSOLETED_CONFIGURATION_CHANGES=$OBSOLETED_CONFIGURATION_CHANGES$_CONFIGURATION_CHANGES
		else
		    notFound="<Could not find info for this obsoleted patch, please edit manually>"
		    echo "Could not locate $file. Please make sure to manually add information for the obsoleted patch in the readme-file"
		    OBSOLETED_DESCRIPTION="${OBSOLETED_DESCRIPTION} $notFound"
		    OBSOLETED_FIXED_REPORTS="${OBSOLETED_FIXED_REPORTS} $notFound"
		    OBSOLETED_CONFIGURATION_CHANGES="${OBSOLETED_CONFIGURATION_CHANGES} $notFound"
		fi
		OBSOLETED_DESCRIPTION=$OBSOLETED_DESCRIPTION"\n"
		OBSOLETED_FIXED_REPORTS=$OBSOLETED_FIXED_REPORTS"\n"
		OBSOLETED_CONFIGURATION_CHANGES=$OBSOLETED_CONFIGURATION_CHANGES"\n"
		
		OBSOLETED_PATCHES=$OBSOLETED_PATCHES"$patch\n"
		    
	done

}

parseConfig()
{
	tmp_file=$1
	# Remove tabs from filename
	file=$(print "$tmp_file" | nawk '{gsub("\t", ""); print}')
	# REmoves space from filename
	file=$(print "$file" | nawk '{gsub(" ", ""); print}')

	echo "Parsing "$file
	
	# Read config file line by line
	while read line
	do
		# check to se if it is a comment line
		comment=`echo $line | nawk '{ if (substr($0,1,1)=="#"){print "YES"}  } '`
		
		
		# skip all empty rows and all comment rows
		if [[ -n $line && -z $comment ]] then
			#echo "Line: "$line "\c"

			case "$line" in 
			"COMPONENT="*)    _COMPONENT=`echo $line | nawk -F= '{ print $2 } '` ;;
			"CORRECTION="*)   _CORRECTION=`echo $line | nawk -F= '{ print $2 } '` ;;
			"PATCH_TYPE="*)   _PATCH_TYPE=`echo $line | nawk -F= '{ print $2 } '` ;;
			"PATCH_ID="*)     _PATCH_ID=`echo $line | nawk -F= '{ print $2 } '` ;;
			"BUILD="*)        _BUILD=`echo $line | nawk -F= '{ print $2 } '` ;;
			"BASE_VERSION="*) _BASE_VERSION=`echo $line | nawk -F= '{ print $2 } '` ;;
			"BASE_BUILD="*)   _BASE_BUILD=`echo $line | nawk -F= '{ print $2 } '` ;;
			"SHUTDOWN="*)     _SHUTDOWN=`echo $line | nawk -F= '{ print $2 } '` ;;
			"STOP="*)         _STOP=`echo $line | nawk -F= '{ print $2 } '` ;;
			"START="*)        _START=`echo $line | nawk -F= '{ print $2 } '` ;;
			"UNLOCK="*)       _UNLOCK=`echo $line | nawk -F= '{ print $2 } '` ;;
			"PRODUCT_NUMBER="*)  _PRODUCT_NUMBER=`echo $line | nawk -F= '{ print $2 } '` ;;

			"<DESCRIPTION>")    readTaggedData "DESCRIPTION" "\n" "y"; _DESCRIPTION=$DATA ;;
			"<IMPORTANT_INFO>") readTaggedData "IMPORTANT_INFO" "\n" "y"; _IMPORTANT_INFO=$DATA ;;
			"<SYSTEM_IMPACT>")  readTaggedData "SYSTEM_IMPACT" "\n" "y"; _SYSTEM_IMPACT=$DATA ;;
			"<OBSOLETES>")      readTaggedData "OBSOLETES" " " "n"; _OBSOLETES=$DATA ;;
			"<REQUIRES>")       readTaggedData "REQUIRES" " " "n"; _REQUIRES=$DATA ;;
			"<INCOMPAT>")       readTaggedData "INCOMPAT" " " "n"; _INCOMPAT=$DATA ;;
			"<DEPENDENCIES>")   readTaggedData "DEPENDENCIES" "\n" "n"; _DEPENDENCIES=$DATA ;;
			"<CONFIGURATION_CHANGES>") readTaggedData "CONFIGURATION_CHANGES" "\n" "y"; _CONFIGURATION_CHANGES=$DATA;;
			"<FIXED_REPORTS>")  readTaggedData "FIXED_REPORTS" "\n" "n"; _FIXED_REPORTS=$DATA ;;
			*) echo "$line NOT PARSED";;
			esac
		fi		
	done < $file

}


readTaggedData()
{
    # syntax readTaggedData <tag name> <row separator> <keep white space YES/NO>
    #echo "PARSING $1"
    DATA=""
    
    if [[ "$3" = [Yy]* ]]
    then
	#echo "keeping white spaces."
	# Changes the default field separator from " " to "%" 
	# This is only done to be able to keep leading white spaces
	IFS="^"
	
    fi
    
    while read line
    do
	if  [[ "$line" = "<$1>" ]]
	then
	    break
	else
	    #echo "readTaggedData"$line
	    DATA="$DATA$line$2"
	fi
    done
    
    # reset to default
    IFS=" "
	
}

doParseFileList()
{
    echo "Start parsing file list."

    echo ""			
    echo "# This contains java components that should be compiled " > $PATCH_DIR/compile.sh
    echo "# before making a patch delivery." >> $PATCH_DIR/compile.sh
    echo "#  " >> $PATCH_DIR/compile.sh
    echo "# Calls ant with path to build.xml file and distjar parameter. " >> $PATCH_DIR/compile.sh
    echo "#  " >> $PATCH_DIR/compile.sh
    echo "# This script must not be changed." >> $PATCH_DIR/compile.sh
    
    # Clear srccopy.sh
    echo "" > $PATCH_DIR/srccopy.sh

    while read line
    do
	# remove CR and LF from line
	# line=`echo $line | nawk '{ print substr($0,1,length($0)-1) } '`
	# check to se if it is a comment line
	comment=`echo $line | nawk '{ if (substr($0,1,1)=="#"){print "YES"}  } '`
	
	# skip all empty rows and all comment rows
	if [[ -n $line && -z $comment ]] then
	    
	    filepath=`echo $line | nawk '{ print $1 } ' `
	    
	    # get filename
	    filename=`basename $filepath`
	    
	    FILE_LIST=$FILE_LIST"$filename\n"
	    # get destination directory
	    destdir=`echo $line | nawk '{ print $2 } '`
	    
	    # get source directory
	    dir=`echo $filepath | nawk -F/ '{ print $1} '`
	    
	    # Check if filename is a jar file. 
	    jar=`echo $filename | nawk -F . '{ print $2 } '`
	    
	    if [[ -z $filename || -z $dir || -z $destdir  ]] 
	    then
		echo " Error parsing file row.. ["$line"]"
		exit 1
	    else
		
	        # Dont add file for build if -nojarbuild is given
		if [ "$NO_JAR_BUILD" != "TRUE" ]
		then
		    # if file is a jar file.
		    if [[ jar = "jar" ]] then
			if [[ -f "$VOBS_BASEDIR"/"$dir"/build.xml ]] then
			    # Adding ant call to compile script
			    echo "ant -buildfile "$VOBS_BASEDIR"/"$dir"/build.xml distclean" >> $PATCH_DIR/compile.sh
			    echo "ant -buildfile "$VOBS_BASEDIR"/"$dir"/build.xml distjar" >> $PATCH_DIR/compile.sh
			fi
		    fi
		fi
		
		cd $VOBS_BASEDIR"/"$PKG"_patch"
		echo "cp "$VOBS_BASEDIR"/"$filepath" "$VOBS_BASEDIR"/"$PKG"_patch/src"$destdir >> $PATCH_DIR/srccopy.sh
		echo "if [[ ! \$? -eq 0 ]]; then" >> $PATCH_DIR/srccopy.sh
		echo "  echo \"Error: Could not copy ${VOBS_BASEDIR}/${filepath} to ${VOBS_BASEDIR}/${PKG}_patch/src${destdir}\"" >> $PATCH_DIR/srccopy.sh
		echo "fi" >> $PATCH_DIR/srccopy.sh
		
	    fi
	    
	fi		
    done < $PATCH_FILES
    
    # Make compile script executable
    chmod 755 $PATCH_DIR/compile.sh
    chmod 755 $PATCH_DIR/srccopy.sh

}

doCompile()
{
    if [[ -f $PATCH_DIR/compile.sh ]] then
	echo "Building jar-files..."
	$PATCH_DIR/compile.sh
    fi
}
	
doClean()
{
    echo "Removing old patch build files..."
    rm -fr $PATCH_DIR
    rm -fr $BUILDDIR
}


doCreateReadme()
{
	
	#readme=$(nawk -v filename="$FILE"  'BEGIN { gsub("\t","",filename); print temp }'

	FILE="$VOBS_BASEDIR/patches/readme_main.txt"
	readme=$(nawk -v filename="$FILE" \
	     -v correction="$CORRECTION" \
	     -v patch_id="$PATCH_ID" \
	     -v component="$COMPONENT" \
	     -v base_version="$BASE_VERSION" \
	     -v patch_number="$PATCH_NUMBER$PRIVATE_EXTENSION" \
	     -v patch_type="$PATCH_TYPE" \
	     -v important_info="$IMPORTANT_INFO" \
	     -v reguires="$REQUIRES" \
	     -v obsoleted_patces="\n$OBSOLETED_PATCHES" \
	     -v description="\n$DESCRIPTION" \
	     -v obsoleted_description="\n$OBSOLETED_DESCRIPTION" \
	     -v fixed_reports="\n$FIXED_REPORTS" \
	     -v obsoleted_fixed_reports="\n$OBSOLETED_FIXED_REPORTS" \
	     -v file_list="\n$FILE_LIST" \
	     -v config_change="\n$CONFIGURATION_CHANGES" \
	     -v obsoleted_config_change="\n$OBSOLETED_CONFIGURATION_CHANGES" \
	     -v product_number="$PRODUCT_NUMBER" \
	     -v system_impact="$SYSTEM_IMPACT" \
	     -v patch_filename="$PATCH_FILENAME" \
	 'BEGIN { 
	 	#FS="£";
	
	    # print filename
	    #((getline myvar < file2) > 0)
	    #while(getline < filename > 0 )
	    while((getline row < filename) > 0)
	    
  	       #print ">"$0"<"
  	       #print row
  	       #print "["$1"]" #>> out.txt
  	       
  	       #print "[$1]"
  	     
  	       #form[n++] = $0
  	       form[n++] = row
	    
	    
	    for ( i = 1 ; i < n ; i++) {
		temp=form[i]
		
		#if ( match(temp,"#_PRODUCT_NUMBER_#") ) then
		gsub("#_PRODUCT_NUMBER_#",product_number,temp)
		gsub("#_CORRECTION_#",correction,temp)
		gsub("#_SEQNUM_#",patch_id,temp)
		gsub("#_COMP_NAME_#",component,temp)
		gsub("#_VERSION_#",base_version,temp)
		gsub("#_BUILD_NUM_#",patch_number,temp)
		gsub("#_PATCH_TYPE_#",patch_type,temp)
		gsub("#_IMPORTANT_INFO_#",important_info,temp)
		gsub("#_OBSOLETES_PATCHES_#",obsoleted_patces,temp)
		gsub("#_PATCH_DESCRIPTION_#",description,temp)
		gsub("#_OSOLETED_PATCH_DESCRIPTION_#",obsoleted_description,temp)
		gsub("#_DEPENDENCIES_#",reguires,temp)
		gsub("#_FIXED_XR_#",fixed_reports,temp)
		gsub("#_OBSOLETED_XR_#",obsoleted_fixed_reports,temp)
		gsub("#_INCLUDED_FILES_#",file_list,temp)
		gsub("#_CONFIGURATION_CHANGES_#",config_change,temp)
		gsub("#_OBSOLETED_CONFIGURATION_CHANGES_#",obsoleted_config_change,temp)
		gsub("#_SYSTEM_IMPACT_#",system_impact,temp)
		gsub("#_PATCH_FILENAME_#",patch_filename,temp)
		
		print temp
	    }
	    #print "done"
	}')

        READMEFILE=$BUILDDIR"/README_"$LOWER_PATCH_LABEL".mas0001.solaris10.template"

	echo "Creating README file template..."
	echo $readme > $READMEFILE 
	echo " README template stored at: $READMEFILE"
}

	
# MAIN

doSetupEnv		# Reads initial parameters
doReadPatchConfig	# Reads the patch.cfg file to determine patch info
doGetPatchNumber	# Get the next patch number for this patch
doPrintInfo		# Prints information on screen
doClean
doCreateDir		# create directories
doParseFileList		# Reads filelist and label files 
doCompile		# Determine if compile should be done.
doSaveFiles		# Copies files to patches directory to be saved.
doCopyPkgFiles		# Copies package files to pkg directory for build.
doCopySrcFiles		# Copies source files to pkg directory for build.
doPatchPackage		# Creates package
doCreateReadme		# Creates readmefile
doLableFiles            # Label files

if [[ -z $ERROR ]] then
    echo "Patch built, please check output for errors or warnings."
else
    echo "Patch did not build sucessfully!"
    echo $ERROR
fi
