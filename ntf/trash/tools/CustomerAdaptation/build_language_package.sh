#!/bin/sh
#
# This script will create an NTF language delivery file (e.g 
# ca_ntf_<customer>_<R-state of CA>.<prod.num>.crh109126.solaris8.sh) in the delivery directory. 
#
# 
# 

INSTALL_DIR=`dirname $0`
cd $INSTALL_DIR
BASE=`pwd`

test_prereq(){
cd $BASE/templates/
if ls *.phr > /dev/null 2>&1;then
	BACKUP_FILES=`echo *.phr`
else
echo "ERROR: No phrase file in $BASE/templates/"
echo "Exiting."
exit 1
fi

for a in $BACKUP_FILES
do
dos2unix $a $a >> /dev/null 2>&1
done
cd $BASE
}




header(){
clear
echo ""
echo "NTF Customer Adaptation"
echo "***********************"
echo ""
}

enter_customer(){
header
echo "Enter Customer name(e.g. TeleOperator): \c"
read CUSTOMER
CUSTOMER_LOWER=`echo $CUSTOMER | /usr/bin/tr '[:upper:]' '[:lower:]'`
}

enter_version(){
header
echo "Enter NTF Customer Adaptation version (e.g. R1A): \c"
read VERSION
VERSION_LOWER=`echo $VERSION | /usr/bin/tr '[:upper:]' '[:lower:]'`
}

enter_prod_num(){
header
echo "Enter product number of the NTF Customer Adaptation:"
echo "(e.g 1/CRH 109 254/2)"
read PRODNUM
PRODNUM=`echo $PRODNUM | /usr/bin/tr '/' '.' | /usr/bin/tr -d ' '  | /usr/bin/tr -c -d '[a-z][A-Z][0-9]_.'`
PRODNUM=`echo $PRODNUM | /usr/bin/tr -s '[:upper:]' '[:lower:]'`
CustomerId=`echo $PRODNUM | cut -f1 -d.`
if [ "$CustomerId" -lt 10 ]; then
CustomerId="0$CustomerId"
fi
}

create_version(){
MCR_VERSION="CA_NTF_${CUSTOMER}_${VERSION}"
}

create_package_name(){
Package_Name="LMENtf${CustomerId}"
}


copy_files(){
echo "Copy files..."
mkdir -p $BASE/delivery/ca/build/templates
mkdir -p $BASE/delivery/ca/temp
cp $BASE/templates/*.phr $BASE/delivery/ca/build/templates
cp -r $BASE/PKG $BASE/delivery/ca/
chmod -R 777 $BASE/delivery/ca
}

configure(){
sed -e s%"$1"%"$2"% < $3 > $3.tmp
mv $3.tmp $3
}

update_PKG(){
configure "PKG=.*"        "PKG=${Package_Name}"      "$BASE/delivery/ca/PKG/pkginfo"
configure "VERSION=.*"    "VERSION=$VERSION"         "$BASE/delivery/ca/PKG/pkginfo"
configure "BACKUP_FILES=.*" "BACKUP_FILES=$BACKUP_FILES"   "$BASE/delivery/ca/PKG/pkginfo"
configure "PKGNAME=.*"    "PKGNAME=${Package_Name}"  "$BASE/delivery/ca/PKG/Makefile"
configure "BASE=.*"       "BASE=$BASE"               "$BASE/delivery/ca/PKG/Makefile"
configure "__PKGNAME__"   "${Package_Name}"          "$BASE/delivery/ca/PKG/install_ca.sh"
configure "__MCR_VERSION__"   "${MCR_VERSION}"       "$BASE/delivery/ca/PKG/postinstall"

chmod -R 777 $BASE/delivery/ca
}

create_delivery(){
FILE="$BASE/delivery/install.tar"
SH_FILE="ca_ntf_${CUSTOMER_LOWER}_${VERSION_LOWER}.${PRODNUM}.solaris8.sh"
echo "Creating delivery..."
cd $BASE/delivery/ca/PKG
clearmake all
echo "Creating delivery file $SH_FILE ..."
cp adminfile $BASE/delivery/ca
cp install_ca.sh $BASE/delivery/ca
cd $BASE/delivery/ca
tar cf $FILE ${Package_Name}.pkg install_ca.sh adminfile
cd $BASE/delivery
rm -r ca
cp $BASE/PKG/self_extracting_head $BASE/delivery/
chmod 777 $BASE/delivery/self_extracting_head
cat self_extracting_head install.tar > $SH_FILE
rm self_extracting_head install.tar
echo "Creating delivery done."
}

completed(){
echo
echo "**************************************************"
echo
echo "COMPLETED"
echo "File can be fetched from $BASE/delivery/"
echo
exit 0
}

############################
#         MAIN             #
############################
test_prereq
enter_customer
enter_version
enter_prod_num
create_version
create_package_name
copy_files
update_PKG
create_delivery
completed



