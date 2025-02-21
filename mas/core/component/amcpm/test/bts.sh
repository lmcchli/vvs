#!/bin/sh 
# TC:s are numbered in the following way.
# TC.x.y.z where 
#  x is 1 for application related stuff and 2 for mcp related stuff
#  y is groups within each category
#  z is testcasenumbering
##################################################################
# Set a character set that uses multiple bytes for a character. The
# character set is what is set from the deployment server when a host
# is jumpstarted.
LC_ALL=en_US.UTF-8
export LC_ALL

# Some common stuff used.
APPDELIVERYFILENAME=/vobs/ipms/mas/amcpm/test/ca_vva.company.cis001.r1a_001.myappprodid.1.tar
MCPDELIVERYFILENAME=/vobs/ipms/mas/amcpm/test/ca_vva.prompt.sv.video1.company.cis0001.r1a.001.mymcpprodid.1.tar
MASDIR=/vobs/ipms/mas/amcpm/test/mas
APPPRODID=myappprodid.1
MCPPRODID=mymcpprodid.1
cat ../../tools/appadmin.sh | sed 's/BASEDIR=.*$/BASEDIR=\/vobs\/ipms\/mas\/amcpm\/test\/mas/' > ./mas/bin/appadmin.sh
chmod +xw ./mas/bin/appadmin.sh

cat ../../tools/mcpadmin.sh | sed 's/BASEDIR=.*$/BASEDIR=\/vobs\/ipms\/mas\/amcpm\/test\/mas/' > ./mas/bin/mcpadmin.sh
chmod +xw ./mas/bin/mcpadmin.sh

cat ../../tools/appmcpreg.sh | sed 's/BASEDIR=.*$/BASEDIR=\/vobs\/ipms\/mas\/amcpm\/test\/mas/' > ./mas/bin/appmcpreg.sh
chmod +xw ./mas/bin/appmcpreg.sh

cp ../../tools/createxml.nawk ./mas/bin/createxml.nawk
chmod +w ./mas/bin/createxml.nawk
cp ../../etc/ComponentConfig.xml ./mas/etc/ComponentConfig.xml
chmod +w ./mas/etc/ComponentConfig.xml
cp ../../cfg/mas.xml ./mas/cfg/mas.xml
chmod +w ./mas/cfg/mas.xml

# < - -  A p p l i c a t i o n T e s t c a s e s - - >
# 1.1 Appcreation
# ------------------------------------------
# TC1.1.1 Verfiy that correct filename is created
../src/mkapp.sh
if [ -f "$APPDELIVERYFILENAME" ]; then
	echo "TC1.1.1 passed"
else
	echo "TC1.1.1 failed"
fi

# 1.2 App installation
# ------------------------------------------
# TC1.2.1 Verify that correct path is used when app is installed.
./mas/bin/appadmin.sh install $APPDELIVERYFILENAME
if [ -d "$MASDIR/applications/$APPPRODID" ]; then 
	echo "TC1.2.1 passed"
else
	echo "TC1.2.1 failed"
fi
# ------------------------------------------
# TC1.2.2 Verify that correct path is set in the ComponentConfig.xml when app is installed.
grep -q $APPPRODID ./mas/etc/ComponentConfig.xml
if [ $? = 0 ]; then 
	echo "TC1.2.2 passed"
else
	echo "TC1.2.2 failed"
fi

# ------------------------------------------
# TC1.2.3 Verify that the application .xml configurationfiles are correct.
diff ./mas/applications/incomming_call.xml ./mas/applications/$APPPRODID/incomming_call.xml > /dev/null
if [ $? = 0 ]; then 
	echo "TC1.2.3 passed"
else
	echo "TC1.2.3 failed"
fi

# 1.3 View Application
# ------------------------------------------
# TC1.3.1 Verify that viewapp shows correct id.
PRODID=`./mas/bin/appadmin.sh view|egrep "^Id"|sed 's/Id:[ ]*//'`
if [ "$PRODID" = "$APPPRODID" ]; then 
	echo "TC1.3.1 passed"
else
	echo "TC1.3.1 failed"
fi

# 1.4 Uninstallation
# ------------------------------------------
# TC1.4.1 Verify that if uninstall uses wrong id the uninstallation fails.
./mas/bin/appadmin.sh uninstall kalle 
if [ $? != 0 ];then
	echo "TC1.4.1 passed"
else
	echo "TC1.4.1 failed"
fi

# ------------------------------------------
# TC1.4.2 Verify that uninstall uses the id shown by the view command.
./mas/bin/appadmin.sh uninstall $PRODID
if [ $? = 0 ];then
	echo "TC1.4.2 passed"
else
	echo "TC1.4.2 failed"
fi

# < - - - - MCP T e s t c a s e s - - - - >
# 2.1 MCP creation
# TC2.1.1 Verfiy that correct filename is created
../src/mkmcp.sh
if [ -f "$MCPDELIVERYFILENAME" ]; then
    echo "TC2.1.1 passed"
else
    echo "TC2.1.1 failed"
fi

# 2.2 MCP installation
# ------------------------------------------
# TC2.2.1 Verify that correct path is used when app is installed.
./mas/bin/mcpadmin.sh install $MCPDELIVERYFILENAME
if [ -d "$MASDIR/applications/mediacontentpackages/$MCPPRODID" ]; then
    echo "TC2.2.1 passed"
else
    echo "TC2.2.1 failed"
fi

# 2.3 View MCP
# ------------------------------------------
# TC2.3.1 Verify that viewapp shows correct id.
PRODID=`./mas/bin/mcpadmin.sh view|egrep "^Id"|sed 's/Id:[ ]*//'`
if [ "$PRODID" = "$MCPPRODID" ]; then
    echo "TC2.3.1 passed"
else
    echo "TC2.3.1 failed"
fi

# 2.4 MCP uninstallation
# ------------------------------------------
# TC2.4.1 Verify that if uninstall uses wrong id the uninstallation fails.
./mas/bin/mcpadmin.sh uninstall kalle
if [ $? != 0 ];then
    echo "TC2.4.1 passed"
else
    echo "TC2.4.1 failed"
fi

# ------------------------------------------
# 2.4.2 Verify that uninstall uses the id shown by the view command.
./mas/bin/mcpadmin.sh uninstall $MCPPRODID
if [ $? = 0 ];then
    echo "TC2.4.2 passed"
else
    echo "TC2.4.2 failed"
fi


#cleanup
rm -f $APPDELIVERYFILENAME
rm -f $MCPDELIVERYFILENAME
