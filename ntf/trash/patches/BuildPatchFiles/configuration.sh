#! /usr/xpg4/bin/sh
BASEDIR=`pkgparam LMENtf BASEDIR`
. $BASEDIR/bin/common.sh


ntfpkgparam () {
    n=`toupper $1`
    v=`eval echo $"$n"`
    if [ -z "$v" ] ; then
        /bin/pkgparam LMENtf $n
    else
        echo $v
    fi
}

if ! isha ; then
    $BASEDIR/bin/upgradeinstance -d $BASEDIR
fi

