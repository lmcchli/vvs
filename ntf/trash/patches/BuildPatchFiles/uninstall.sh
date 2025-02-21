#!/bin/sh
NTF_DIR=`pkgparam LMENtf BASEDIR`
VERSION="This parameter is changed during build."
VERSION_SHORT=`echo $VERSION | cut -f1 -d_`
PATCH_DIR="$NTF_DIR/patches"
#Functions

run_unconfiguration_script(){
    if [ -f $PATCH_DIR/$VERSION/uninstallation_files/unconfiguration.sh ];then
	$PATCH_DIR/$VERSION/uninstallation_files/unconfiguration.sh
    fi
}

restore_and_uninstall_files(){
    if [ -f $PATCH_DIR/$VERSION/uninstallation_files/backup_files.txt ];then
	cd $PATCH_DIR/$VERSION/uninstallation_files/backup
	BACKUP_FILES=`cat $PATCH_DIR/$VERSION/uninstallation_files/backup_files.txt`
	for file in $BACKUP_FILES
	  do
	  DIR_NAME=`dirname $file`
	  cp $PATCH_DIR/$VERSION/uninstallation_files/backup/$file $NTF_DIR/$DIR_NAME/
	done
    fi   
    if [ -f $PATCH_DIR/$VERSION/uninstallation_files/remove_files.txt ];then
	REMOVE_FILES=`cat $PATCH_DIR/$VERSION/uninstallation_files/remove_files.txt`
	for file in $REMOVE_FILES
	  do
	  DIR_NAME=`dirname $file`
	  rm $NTF_DIR/$file
	done
    fi   
}

update_version_file(){
NEW_VERSION_FILE=`grep "VERSION" $NTF_DIR/VERSION | sed s%", $VERSION"%""%`
echo "${NEW_VERSION_FILE}" > $NTF_DIR/VERSION
}

remove_dir(){
cd /tmp
rm -r $PATCH_DIR/$VERSION
}

#Main
run_unconfiguration_script
restore_and_uninstall_files
update_version_file
remove_dir
