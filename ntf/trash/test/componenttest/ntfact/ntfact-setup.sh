#!/bin/sh
#
# This script will prompt and setup the environment for
# automatic component tests for NTF on a single lab machine.
#

error(){
    echo $PGM: ERROR: "$*" 1>&2
    exit 1
}

usage(){
    echo "$PGM: Wrong parameters. Use: $PGM [-h|-ex|-mur|-users|-ms|-ntf]" 1>&2
    echo "No parameters (default) eqals -ex, -mur, -users, -ms, -ntf"
    exit 1
}

extract(){
    DEF_NTF=`ls -t1 $BASE/ntf*.tar.gz | head -1` 
    DEF_MS=`ls -t1 $BASE/ms*.tar.gz | head -1` 
    DEF_MS_JES=`ls -t1 $BASE/ms*jes*.tar | head -1` 
    DEF_MUR=`ls -t1 $BASE/mur*.tar.gz | head -1`
    DEF_SMSC=`ls -t1 $BASE/smsc*.tar.gz | head -1`
    DEF_MER=`ls -t1 $BASE/mer*.tar.gz | head -1`
    
    echo "Use the following NTF package: $DEF_NTF?"
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        echo "Enter full path to NTF package:"
        read DEF_NTF
    fi
    
    echo "Use the following MS package: $DEF_MS?"
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        echo "Enter full path to MS package:"
        read DEF_MS
    fi

    echo "Use the following MS JES package: $DEF_MS_JES?"
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        echo "Enter full path to MS JES package:"
        read DEF_MS_JES
    fi
    
    echo "Use the following MUR package: $DEF_MUR?"
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        echo "Enter full path to MUR package:"
        read DEF_MUR
    fi

    echo "Use the following SMSC package: $DEF_SMSC?"
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        echo "Enter full path to SMSC package:"
        read DEF_SMSC
    fi

    echo "Use the following MER package: $DEF_MER?"
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        echo "Enter full path to SMSC package:"
        read DEF_MER
    fi
    
    cd $BASE
    rm -rf ntf mur ms smsc mer
    mkdir ntf mur ms smsc mer
    # NTF
    cd ntf
    echo "Extracting: $DEF_NTF"
    gunzip -t $DEF_NTF 
    if [ $? != 0 ]; then
        "Error extracting $DEF_NTF"
        exit 1
    fi
    gzip -dc $DEF_NTF | tar xf -
    # MS
    cd ../ms
    echo "Extracting: $DEF_MS"
    gunzip -t $DEF_MS 
    if [ $? != 0 ]; then
        "Error extracting $DEF_MS"
        exit 1
    fi
    gzip -dc $DEF_MS | tar xf -
    # MS JES
    echo "Extracting: $DEF_MS_JES"
    tar tf $DEF_MS_JES 
    if [ $? != 0 ]; then
        "Error extracting $DEF_MS_JES"
        exit 1
    fi
    tar xf $DEF_MS_JES
    # MUR
    cd ../mur
    echo "Extracting: $DEF_MUR"
    gunzip -t $DEF_MUR 
    if [ $? != 0 ]; then
        "Error extracting $DEF_MUR"
        exit 1
    fi
    gzip -dc $DEF_MUR | tar xf -
    # MER
    cd ../mer
    echo "Extracting: $DEF_MER"
    gunzip -t $DEF_MER 
    if [ $? != 0 ]; then
        "Error extracting $DEF_MER"
        exit 1
    fi
    gzip -dc $DEF_MER | tar xf -
    # SMSC
    # Note: This is the install, extract to /apps at once
    cd /apps/smsc
    echo "Extracting: $DEF_SMSC"
    gunzip -t $DEF_SMSC
    if [ $? != 0 ]; then
        "Error extracting $DEF_SMSC"
        exit 1
    fi
    gzip -dc $DEF_SMSC | tar xf -

    

    cd $PWD
    echo "Extraction of NTF, MUR, MS, MS-JES, SMSC complete"
    echo "-------------------------------------------------"

}

install_ntf() {
# Edit response file and install
cd $BASE/ntf
cp LMENtf.response LMENtf.response.tmp
echo "s@IMAPHost=\(.*\)@IMAPHost=$HOST.$DOMAIN@" > tmp
sed -f tmp LMENtf.response.tmp > LMENtf.response
rm tmp
sed 's@IMAPUserName=\(.*\)@IMAPUserName=gnotification1@' LMENtf.response > LMENtf.response.tmp
sed 's@SearchBase=o=\(.*\)@SearchBase=o=mobeon.com@' LMENtf.response.tmp > LMENtf.response
sed 's@SMEPassword=\(.*\)@SMEPassword=AMPCLIENT@' LMENtf.response > LMENtf.response.tmp
sed 's@SMESystemId=\(.*\)@SMESystemId=AMPCLIENT@' LMENtf.response.tmp > LMENtf.response
sed 's@smesystemtype=\(.*\)@smesystemtype=NTFACT@' LMENtf.response > LMENtf.response.tmp
cp LMENtf.response.tmp LMENtf.response
echo "SMESystemType=NTFACT" >> LMENtf.response

./install.sh
cd $PWD
echo "*** NTF installed ***"
}

install_ms() {
MSCONF=/data/ms/SUNWmsgsr/config
cd $BASE/ms
# JES
./install.pl /apps/msgsrv
# MS
./install.sh

cd /apps/ms/tools/add_instance
# Config and add instance
cp silentconfig.state silentconfig.tmp
echo "s@hostname=\(.*\)@hostname=$HOST@" > tmp
sed -f tmp silentconfig.tmp > silentconfig.state
rm -f tmp
echo "s/postmaster=\(.*\)/postmaster=root@$HOST.$DOMAIN/" > tmp
sed -f tmp silentconfig.state > silentconfig.tmp 
rm -f tmp
echo "s@cfghost=\(.*\)@cfghost=$HOST.$DOMAIN@" > tmp
sed -f tmp silentconfig.tmp > silentconfig.state
rm -f tmp
echo "s@murhost=\(.*\)@murhost=$HOST.$DOMAIN@" > tmp
sed -f tmp silentconfig.state > silentconfig.tmp 
rm -f tmp
echo "s@mcr_hostname=\(.*\)@mcr_hostname=$HOST.$DOMAIN@" > tmp
sed -f tmp silentconfig.tmp > silentconfig.state
rm -f tmp
echo "s@full_logical_host=\(.*\)@full_logical_host=$HOST.$DOMAIN@" > tmp
sed -f tmp silentconfig.state > silentconfig.tmp 
rm -f tmp
cp silentconfig.tmp silentconfig.state
rm silentconfig.tmp

./add_instance.pl silentconfig.state

cd ..

# Make filter & point it out
./mknotif_sieve -o 0 1
cp notify.filter $MSCONF/.
cd $MSCONF
cp imta.cnf imta.tmp
sed 's/destinationfilter\(.*\)/destinationfilter file:IMTA_TABLE:notify.filter/' imta.tmp > imta.cnf
rm -f imta.tmp

cd $PWD
echo "*** MS installed ***"
}

install_mur() {
    cd $BASE/mur
    cd `ls`
    # preconditions
    echo "Is the following mcrhost correct? (Blank line below is not good!)"
    grep mcrhost /etc/hosts
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        exit 1
    fi

    echo "Is the following murhost correct? (Blank line below is not good!)"
    grep murhost /etc/hosts
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        exit 1
    fi


    echo "Is the following mcrhostwrite correct? (Blank line below is not good!)"
    grep mcrhostwrite /etc/hosts
    answer=`ckkeywd -Q -d y -p "Choice (default: y)" -e "Not a valid choice" y n ` || exit $?
    if [ "$answer" = "n" ]; then
        exit 1
    fi
    echo "---"
    echo "Installing MUR..."
    ./install.sh
    cd $PWD    
    echo "*** MUR installed ***"
}


install_users() {
    # Enable provisioning
    /apps/ms/tools/enable_provisioning.pl -p emmanager -m on
    # Add users using ldif
    echo "Adding users ntfact1-ntfact6 using raw LDIF. "
    # First replace with actual host.domain
    rm -f tmp
    echo "s@renault.lab.mobeon.com@$HOST.$DOMAIN@g" > tmp
    rm -f real.ldif
    sed -f tmp ../lib/ntfact.ldif > real.ldif
    # Overwrite entries
    ldapadd -f real.ldif -w emmanager -D "cn=Directory Manager" -B "o=mobeon.com" -q
    echo "*** Users installed ***"
    rm -f tmp
}

install_smsc() {
    # Reg in MCR for current host
#    ./addservicetomcr -m $HOST.$DOMAIN -s yes ShortMessage $HOST.$DOMAIN 5016
IP=`nslookup $HOST.$DOMAIN | tail -2 | grep Address | cut -c 10-40 | tr -d ' '`

    ldapmodify -h $HOST.$DOMAIN -p 389 -D "uid=IComponent,emRegisterName=MessagingComponentRegister,o=config" -w abc123 <<EOF
dn: emServiceName=ShortMessage, emRegisterName=MessagingComponentRegister, o=config
changetype: add
emServiceName: ShortMessage
objectClass: emservice
objectClass: top
creatorsname: uid=$MCR_USER,ou=Administrators,ou=TopologyManagement,o=NetscapeRoot
modifiersname: uid=$MCR_USER,ou=Administrators,ou=TopologyManagement,o=NetscapeRoot
EOF
    ldapmodify -h $HOST.$DOMAIN -r -p 389 -D "uid=IComponent,emRegisterName=MessagingComponentRegister,o=config" -w abc123 <<EOF
dn: emcomponentname=NTFACT, emServiceName=ShortMessage, emRegisterName=MessagingComponentRegister, o=config
changetype: add
ipServiceProtocol: smpp
emlogicalzone: qlenras
emcomponenttype: smsc
emhostnumber: $IP
emcomponentversion: X8A_08
objectClass: emcomponent
objectClass: top
ipServicePort: 5016
emcomponentname: NTFACT
emhostname: $HOST.$DOMAIN
EOF

}

install_mer() {
## WARNING!
    echo "WARNING! Untested function install_mer. More post configuration may be neccessary!"
    ./install.sh
}


#######
# MAIN
#######
PGM=`basename $0`
BASE=/apps/dist
#BASE=/tmp/qtolu
HOST=`hostname`
DOMAIN=lab.mobeon.com
PWD=`pwd`

if [ $# -gt 2 ];then usage;fi

case $1 in
    "-h")
        usage
        ;;
    "-ex")
        extract
        ;;
    "-mur")
        install_mur
        ;;
    "-users")
        install_users
        ;;
    "-ms")
        install_ms
        ;;
    "-ntf")
        install_ntf
        ;;
    "-smsc")
        install_smsc
        ;;
    "-mer")
        install_smsc
        ;;

    *)
        extract
        install_mur
        install_ntf
        install_ms
        install_users
        install_smsc
        install_mer
        ;;
esac

