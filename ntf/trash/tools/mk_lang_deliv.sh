#!/bin/sh
#
# This script will create a language delivery file (e.g 
# ntf_mk_lang.cax1031206.1.solaris8.tar) at the directory 
# /vobs/ipms/ntf/delivery/
#
# Remember to set a correct vobview before executing this script.
# 
# 

VOB=/vobs/ipms/ntf
VOBS_TO_LABEL="$VOB/tools"

write_header(){
clear
echo ""
echo "Creating NTF language building package"
echo "**************************************"
echo ""
}

enter_version(){
echo "Enter NTF language building package version (e.g. P1A_01): \c"
read VERSION
VERSION_LOWER=`echo $VERSION | /usr/bin/tr '[:upper:]' '[:lower:]'`
}

label_vob(){
LABEL="NTF_MK_LANG_$VERSION"
echo "Label with $LABEL after a successful build? [y/n] \c"
read SHOULD_LABEL
}

tar_files(){
FILE="$VOB/delivery/ntf_mk_lang_${VERSION_LOWER}.cax1031206.1.solaris8.tar"
mkdir -p $VOB/delivery/ntf_mk_language/templates
cp -r $VOB/tools/CustomerAdaptation/* $VOB/delivery/ntf_mk_language

cd $VOB/delivery/
chmod -R 777 ntf_mk_language
tar cf $FILE ntf_mk_language
if [ $? != 0 ]; then
echo "Making tar file failed!"
echo "Aborting!"
fi
rm -rf $VOB/delivery/ntf_mk_language/
}

do_label(){
if [ "$SHOULD_LABEL" = "y" ];then 
  echo
  echo "**************************************************"
  echo "Attaching label $LABEL on $VOBS_TO_LABEL. Please wait... \c"

  for VOB_NAME in $VOBS_TO_LABEL
  do
    cd $VOB_NAME
    cleartool lstype -s -kind lbtype | grep -w $LABEL > /dev/null
    if [ ! $? = 0 ];then
      cleartool mklbtype -c "Label for $LABEL files"  $LABEL > /dev/null
    fi
    cleartool lslock -s | grep -w $LABEL > /dev/null
    if [ $? = 0 ];then
      cleartool unlock lbtype:$LABEL > /dev/null
    fi
    cleartool mklabel -replace -recurse $LABEL . > /dev/null 2>&1
    if [ ! $? = 0 ];then
      echo
      error "Labeling failed. Exiting."
    fi
  done

  echo "Labeling done."
  echo "Labeling complete."
fi
}

completed(){
echo
echo "**************************************************"
echo
echo "COMPLETED."
echo "File can be fetched from $VOB/delivery/"
echo
exit 0
}

############################
#         MAIN             #
############################
write_header
enter_version
write_header
label_vob
tar_files
do_label
completed























