#!/bin/sh
BASEDIR=`pkgparam LMENtf BASEDIR`
export BASEDIR
LOG="/apps/logs/ntf/uninstall/log"
export LOG
FAIL="0"

. $BASEDIR/bin/common.sh

setup_logfile(){
    if [ ! -d `dirname $LOG` ]; then
        mkdir -p `dirname $LOG` >> /dev/null 2>&1
    fi
    
    /bin/rm -f $LOG >/dev/null 2>&1
    DATE=`date +%Y_%m_%d_%H%M`
    if [ -d "/apps/logs/ntf/uninstall" ]; then
        UnInstallLogfile="/apps/logs/ntf/uninstall/ntf.$DATE.uninstallog"
    else
        UnInstallLogfile="/var/tmp/ntf.$DATE.uninstallog"
        echo "Could not create /apps/logs/ntf/uninstall. Log is in /var/tmp." 1>&2
    fi
    echo "Start NTF uninstallation: " `date` >> $UnInstallLogfile
    /bin/ln $UnInstallLogfile $LOG;
}

check_root() {
    if [ "`/usr/xpg4/bin/id -u`" != 0 ]; then
        echo "You must be root to uninstall NTF" 1>&2
        exit 1
    fi
}

ask_continue() {
    if isha ; then
        echo "After NTF is uninstalled, the tools to remove instances are gone."
        echo "Remember to remove the instances first if you do not wish to keep them."
        echo
    fi
    echo "Do you want to uninstall the NTF component?\c:"
    continue=`ckkeywd -Q -p "Choice: " -e "Not a valid choice" y n ` || exit $?
    if [ "$continue" = "n" ]; then
        echo "$1 aborted by user!" | tee -a $LOG
        exit 0
    fi
}

stop_ntf(){
    if [ -x "/etc/init.d/rc.ntf" ] ; then
        /etc/init.d/rc.ntf stop
    fi
}

remove_rc_scripts(){
    if [ -f /etc/init.d/rc.ntf ] ; then
        /etc/init.d/rc.ntf disableautostart
        rm /etc/init.d/rc.ntf
    fi
}

uninstallntf() {
    isha || $BASEDIR/bin/rminstance -d $BASEDIR || FAIL="1"
    if pkgrm -a "$BASEDIR/uninstall/adminfile" -n LMENtf ; then
        echo "The NTF package was successfully uninstalled"
        if [ "$1" = "True" ]; then
            cd /
            rm -r $BASEDIR
        fi
    else
        FAIL="1"
        echo "Failed to uninstall the NTF package"
    fi
    return "$FAIL"
}

################################################################
setup_logfile
check_root
ask_continue
isha || stop_ntf                         >> $LOG 2>&1
isha || remove_rc_scripts                >> $LOG 2>&1

Remove="True"
if [ ! `basename $BASEDIR` = "ntf" ] ; then
    echo ""
    echo "Do you want to remove $BASEDIR after uninstallation?"
    Remove=`ckkeywd -Q -p "Choice: " -e "Not a valid choice" y n ` || exit $?
    if [ "$Remove" = "n" ]; then
	Remove="False"
    fi
fi
uninstallntf $Remove >> $LOG 2>&1
if [ "$?" = "0" ] ; then
    echo "NTF has been successfully uninstalled."
else
    echo "Failed to uninstall NTF, at least partly."
fi
rm $LOG
echo "View $UnInstallLogfile for details"
