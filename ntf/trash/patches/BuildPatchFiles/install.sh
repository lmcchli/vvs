#!/bin/sh
NTF_DIR=`pkgparam LMENtf BASEDIR`
PATCH_DIR="$NTF_DIR/patches"
VERSION="This parameter is changed during build."
VERSION_SHORT=`echo $VERSION | cut -f1 -d_`
#Functions

backup_and_install_files(){
    cd $PATCH_DIR/$VERSION/
    BACKUP_FILES=`find . -type f | grep -v uninstallation_files`
    for file in $BACKUP_FILES
      do
      DIR_NAME=`dirname $file`
      
      if [ -f $NTF_DIR/$file ];then
	  mkdir -p uninstallation_files/backup/$DIR_NAME
	  cp $NTF_DIR/$file $PATCH_DIR/$VERSION/uninstallation_files/backup/$DIR_NAME/
	  cp $PATCH_DIR/$VERSION/$file $NTF_DIR/$file
	  echo "$file" >> $PATCH_DIR/$VERSION/uninstallation_files/backup_files.txt
      else
	mkdir -p $NTF_DIR/$DIR_NAME
	cp $PATCH_DIR/$VERSION/$file $NTF_DIR/$file
	echo "$file" >> $PATCH_DIR/$VERSION/uninstallation_files/remove_files.txt
      fi
    done
}

run_configuration_script(){
    if [ -f $PATCH_DIR/$VERSION/uninstallation_files/configuration.sh ];then
	$PATCH_DIR/$VERSION/uninstallation_files/configuration.sh
    fi
}

update_version_file(){
    OLD_VERSION_FILE=`grep "VERSION" $NTF_DIR/VERSION`
    echo "${OLD_VERSION_FILE}, $VERSION" > $NTF_DIR/VERSION
}

#Main
backup_and_install_files
run_configuration_script
update_version_file
