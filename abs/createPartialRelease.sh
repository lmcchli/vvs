#!/bin/sh
BASEDIR=`pwd`
RELEASE=""
DESTINATIONSUBDIR="temporary_dir"
DESTINATIONDIR="$BASEDIR/$DESTINATIONSUBDIR"
PRODUCT=""
SDPS=""
WRAPPERS=""
RPMS=""
CFG=""

function printUsage(){
   echo "usage example $0 -r EC01 -d /tmp -p MSGCORE [ -sdps \"ERIC-MAP* ERIC-MCDP*\" -wrappers \"ERIC-WRAPMCDP*\"] [-cfg <path-sdp-list>]"
   echo "WARNING - do not enter full sdp or wrapper names, just wildcards, since we use those patterns"
   echo "to both remove old sdps from the old iso structure, and add new sdps to the new partial iso structure"
   exit
}


while [ "$1" != "" ]; do
   if [ "$1" == "-r" ]; then
      shift
      RELEASE=$1
      shift
   elif [ "$1" == "-d" ]; then
      shift
      DESTINATIONSUBDIR=$1
      shift
   elif [ "$1" == "-p" ]; then
      shift
      PRODUCT=$1
      shift
   elif [ "$1" == "-sdps" ]; then
      shift
      SDPS=$1
      shift
   elif [ "$1" == "-wrappers" ]; then
      shift
      WRAPPERS=$1
      shift
   elif [ "$1" == "-cfg" ]; then
      shift
      CFG=$1 
      shift
   elif [ "$1" == "-format" ]; then
      shift
      FORMAT=$1 
      shift
   elif [ "$1" == "-help" ]; then
      shift
      printUsage   
   elif [ "$1" == "--help" ]; then
      shift
      printUsage
   else
      echo "Unknown option $1"
      printUsage
   fi
done

echo "Done parsing"

if [[ -z ${CFG} || ${CFG} =~ ^- ]]
then
    printUsage
    exit 1
elif [ ! -f ${CFG} ]; then
    echo "File not found: ${CFG}...exiting"
    exit 1
fi

echo "Done with cfg"

if [[ -z ${FORMAT} || ${FORMAT} =~ ^- ]]
then
    printUsage
    exit 1
elif [[ ${FORMAT} != "iso" && ${FORMAT} != "tgz" ]]
then
    printUsage
    exit 1
fi

DESTINATIONDIR="$BASEDIR/$DESTINATIONSUBDIR"
packagename=$DESTINATIONDIR/MIO_$PRODUCT-$RELEASE

# Make the structure for taring the files
# && make sdp, put them there
# -----------------------------------
echo Zapping the old $DESTINATIONDIR
rm -fr $DESTINATIONDIR
echo Creating a new empty $DESTINATIONDIR/$PRODUCT/products dir
mkdir -p $DESTINATIONDIR/$PRODUCT/products

if [ "${CFG}" != "" ]; then
    SDPS=`cat ${CFG}  | egrep '^(SDP|CFG):' | sed -e 's,^SDP:,ERIC-,g' -e 's,^CFG:,ERIC-,g' -e 's,-CX.*$,-CX*,' | tr '\n' ' '`
    RPMS=`cat ${CFG}  | egrep '^RPM:' | sed -e 's,^RPM:,,' -e 's,-CX.*$,-CX*,' | tr '\n' ' '`
    WRAPPERS=`cat ${CFG}  | egrep '^WRAPPER:' | sed -e 's,^WRAPPER:,ERIC-,' -e 's,-CX.*$,-CX*,' | tr '\n' ' '`
    MISC=`cat ${CFG} | egrep '^MISC:' | sed -e 's,^MISC:,,' | tr '\n' ' '` 
fi

for i in $SDPS
do
  echo Copying iso/cba/target/iso/$PRODUCT/products/$i to $DESTINATIONDIR/$PRODUCT/products
  cp -f iso/cba/target/iso/$PRODUCT/products/$i $DESTINATIONDIR/$PRODUCT/products
done

for i in $MISC
do
  echo Copying iso/cba/target/iso/$PRODUCT/products/$i to $DESTINATIONDIR/$PRODUCT/products
  cp -f iso/cba/target/iso/$PRODUCT/products/$i $DESTINATIONDIR/$PRODUCT/products
done

ALL_SDPS=`find $DESTINATIONDIR/$PRODUCT/products -name "*.sdp" -exec basename {} \; | xargs`

# handle wrappers same way
echo Creating a new empty $DESTINATIONDIR/$PRODUCT/wrappers dir
mkdir -p $DESTINATIONDIR/$PRODUCT/wrappers
for i in $WRAPPERS
do
  echo Copying iso/cba/target/iso/$PRODUCT/wrappers/$i to $DESTINATIONDIR/$PRODUCT/wrappers
  cp -f iso/cba/target/iso/$PRODUCT/wrappers/$i $DESTINATIONDIR/$PRODUCT/wrappers
done

ALL_WRAPPERS=`find $DESTINATIONDIR/$PRODUCT/wrappers -name "*.sdp" -exec basename {} \; | xargs`

# handling rpms
echo Creating a new empty $DESTINATIONDIR/$PRODUCT/rpms dir
mkdir -p $DESTINATIONDIR/$PRODUCT/rpms
for i in $RPMS
do
  echo RPM is $i
  echo COPYING iso/cba/target/iso/$PRODUCT/rpms/$i to $DESTINATIONDIR/$PRODUCT/rpms
  cp -f iso/cba/target/iso/$PRODUCT/rpms/$i $DESTINATIONDIR/$PRODUCT/rpms
done

# handling cfg
CFGFILES="COMPONENT_LIST.conf"
echo Creating a new empty $DESTINATIONDIR/$PRODUCT/cfg dir
mkdir -p $DESTINATIONDIR/$PRODUCT/cfg
for i in $CFGFILES
do
  echo Copying iso/cba/target/iso/$PRODUCT/cfg/$i to $DESTINATIONDIR/$PRODUCT/cfg
  cp -f iso/cba/target/iso/$PRODUCT/cfg/$i $DESTINATIONDIR/$PRODUCT/cfg
done



ALL_WRAPPERS=`find $DESTINATIONDIR/$PRODUCT/wrappers -name "*.sdp" -exec basename {} \; | xargs`


# List of files/directory to include in the auto extract script
# ----------------------------------------------------------------
listoffiletotar="
        $PRODUCT/products/*sdp
"

listofcfgtotar="
        $PRODUCT/cfg/COMPONENT_LIST.conf
"

listofwrapperfilestotar="
        $PRODUCT/wrappers/*sdp
"

listofrpmfilestotar="
        $PRODUCT/rpms/*rpm
"

cd $DESTINATIONDIR
if [ $FORMAT == "iso" ]; then
    mkisofs -R -ldots -l -D -max-iso9660-filename -allow-multidot -allow-lowercase -no-iso-translate -o $packagename.iso $PRODUCT
elif [ $FORMAT == "tgz" ]; then
    tar -cvzf - --dereference $listoffiletotar $listofwrapperfilestotar $listofrpmfilestotar $listofcfgtotar >> $packagename.tgz
fi

echo
echo Done\! Your partial release directory is in $DESTINATIONDIR

## Append binary data to the script.
## -------------------------------------------
#echo
#echo Creating the auto extract file $packagename
#echo
#cat install.sh.in > $packagename
#sed -i -e "s/SDP_LIST/${SDPS}/g" $packagename
#sed -i -e "s/PRODUCT_NAME/${PRODUCT}/g" $packagename
#sed -i -e "s/WRAPPER_LIST/${WRAPPERS}/g" $packagename
#echo "PAYLOAD:" >> $packagename
#pushd .
#cd $DESTINATIONDIR
#tar -cvzf - --dereference $listoffiletotar $listofwrapperfilestotar >> $packagename
#popd
#chmod a+x  $packagename
#echo
#echo Done\! Your partial release directory is in $DESTINATIONDIR
