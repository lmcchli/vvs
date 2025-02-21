#!/usr/xpg4/bin/sh
LOG="/apps/logs/ntf/install/log"
FAIL="0"
TR="/usr/xpg4/bin/tr"

export LOG


absdir() {
    if [ -d "$1" ] ; then
        DIR=$1
    else
        DIR=`dirname $1`
    fi
    OLDDIR=`pwd`
    cd $DIR
    echo "`pwd`$FILE"
    cd $OLDDIR
}

INSTALL_DIR=`absdir $0`
export INSTALL_DIR

err() {
    echo "ERROR   $@" 1>&2
    echo "`date +%Y-%m-%d\ %H:%M:%S\ %Z` ERROR   $@" >>$InstallLogfile
}

info() {
    echo "INFO    $@"
    echo "`date +%Y-%m-%d\ %H:%M:%S\ %Z` INFO    $@" >>$InstallLogfile
}

ask_continue() {
    echo "Starting $1"
    if [ -z "$oldversion" ] ; then
        echo "Installation parameters are taken from $INSTALL_DIR/LMENtf.response."
        HAMODE=`grep -i HAMODE  $INSTALL_DIR/LMENtf.response | grep -i YES`
        if [ -z "$HAMODE" ] ; then
            echo "Please check the IMAP user name:"
            echo "        `grep -i imapusername LMENtf.response`"
        fi
    fi
    continue=`ckkeywd -Q -p "Continue: " -e "Not a valid choice" y n ` || exit $?
    if [ "$continue" = "n" ]; then
        info "$1 aborted by user!"
        exit 0
    fi
}

stop_ntf(){
    if [ -x "/etc/init.d/rc.ntf" ] ; then
        echo "Stopping NTF" | tee -a $LOG
        /etc/init.d/rc.ntf stop >>$LOG 2>&1
    fi
}

setup_logfile(){
    if [ ! -d `dirname $LOG` ]; then
        mkdir -p `dirname $LOG` >> /dev/null 2>&1
    fi

    /bin/rm -f $LOG >/dev/null 2>&1
    DATE=`date +%Y_%m_%d_%H%M`
    if [ -d "/apps/logs/ntf/install" ]; then
        InstallLogfile="/apps/logs/ntf/install/ntf.$DATE.installog"
    else
        InstallLogfile="/var/tmp/ntf.$DATE.installog"
        echo "Could not create /apps/logs/ntf/install. Log is in /var/tmp." 1>&2
    fi
    echo "Start NTF installation: " `date` >> $InstallLogfile
    /bin/ln $InstallLogfile $LOG;
}

#checks that java package is installed and version is 1.4 or greater.
check_java() {
    if pkginfo SUNWj5rt >/dev/null 2>&1 ; then
        VERSION=`pkgparam SUNWj5rt VERSION | sed 's/..\(.\).*/\1/`
        if [ $VERSION -lt 5  ] ; then
            err "Java Version 1.5 or greater must be installed"
            exit 1
        fi
    else
        err "SUNWj5rt package not installed"
        exit 1
    fi
    if pkginfo SUNWj5rtx >/dev/null 2>&1 ; then
        VERSION=`pkgparam SUNWj5rtx VERSION | sed 's/..\(.\).*/\1/`
        if [ $VERSION -lt 4  ] ; then
            err "Java (64bit) Version 1.5 or greater must be installed"
            exit 1
        fi
    else
        err "SUNWj5rtx package not installed"
        exit 1
    fi
}

check_root() {
    if [ "`/usr/xpg4/bin/id -u`" != 0 ]; then
        echo "You must be root to install the NTF component!" 1>&2
        exit 1
    fi
}

upcase_responsefile_keys() {
    mv $INSTALL_DIR/LMENtf.response $INSTALL_DIR/LMENtf.response.temporaryInstall
    cat $INSTALL_DIR/LMENtf.response.temporaryInstall | while read line ; do
        if grep '=' >/dev/null <<EOF
$line
EOF
        then
            key=`echo $line | cut -f 1 -d '=' | $TR '[:lower:]' '[:upper:]'`
            value=`echo $line | cut -f 2- -d '='`
            echo "$key=$value"
        else
            echo $line
        fi
    done > $INSTALL_DIR/LMENtf.response
}

################ MAIN ##########################################
setup_logfile
export InstallLogfile

check_root
check_java
upcase_responsefile_keys

if pkginfo LMENtf >/dev/null 2>&1 ; then
    oldversion=`pkgparam LMENtf VERSION`

    R8=`echo $oldversion|grep NTF_R8A`
    HA=`pkgparam LMENtf -v|grep -i HAMODE|grep -i yes`
    if [ -n "$R8" -a -n "$HA" ] ; then
        echo "Upgrade from NTF_R8A is not supported for HA systems." | tee -a $LOG
        echo "Read \"Installation Guide NTF\"" | tee -a $LOG
        exit 1;
    fi
    ask_continue "upgrade of NTF from $oldversion to `pkgparam -d $INSTALL_DIR/ntf_files/LMENtf.pkg LMENtf VERSION`"
    pkgparam -v LMENtf BASEDIR > $INSTALL_DIR/LMENtf.response #Keep the old BASEDIR
    pkgparam -v LMENtf |grep -i HAMODE | $TR '[:lower:]' '[:upper:]' >> $INSTALL_DIR/LMENtf.response #Keep the old HAMODE
    grep -i 'STARTNTF=' $INSTALL_DIR/LMENtf.response.temporaryInstall | $TR '[:lower:]' '[:upper:]' >> $INSTALL_DIR/LMENtf.response
    echo "OLDVERSION=$oldversion" >> $INSTALL_DIR/LMENtf.response
    stop_ntf
else
    ask_continue "install of NTF `pkgparam -d $INSTALL_DIR/ntf_files/LMENtf.pkg LMENtf VERSION`"
fi

echo "INSTALL_DIR=$INSTALL_DIR" >> $INSTALL_DIR/LMENtf.response
echo "Installing NTF package" | tee -a $LOG
pkgadd -a $INSTALL_DIR/ntf_files/adminfile -r $INSTALL_DIR/LMENtf.response -n -d $INSTALL_DIR/ntf_files/LMENtf.pkg LMENtf || FAIL="1"

mv $INSTALL_DIR/LMENtf.response.temporaryInstall $INSTALL_DIR/LMENtf.response
grep "Failed to register" $LOG >/dev/null 2>&1 && FAIL="1"

if [ "$FAIL" = "0" ] ; then
    echo "NTF has been successfully installed." | tee -a $LOG
else
    echo "Failed to install NTF, at least partly." | tee -a $LOG
fi
echo "View log file $InstallLogfile for details"
rm $LOG
echo "End NTF installation: " `date` >> $InstallLogfile


