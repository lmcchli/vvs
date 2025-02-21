#!/bin/ksh

#================================================================================"
# This script will create Broadcast Announcement.
#    The primary reason for this script is for the system level BAs that
#    need to span multiple OpCOs.
#  INPUT
#    The OpCOs will be configured in a configuration file
#    The BA information will be provided in an input file.
#  -
#  -
#================================================================================"

#set DEBUG to "true" to have log statements on stdout
DEBUG=true

setupTestEnv() {
	echo "    Creating test environment"
	createBATemplate
	createOpcoInputFile $OPCO_FILE
}

createBATemplate(){
echo 'identity:ba:testdemo
identity:muid:testdemo
MOIPBroadcastAnnouncementPlayOnce:no
MOIPBroadcastAnnouncementDescription:demo
MOIPBroadcastAnnouncementLanguage:en
objectClass:indexedprofile
objectClass:broadcastannouncement
objectClass:top
objectClass:MOIPBroadcastAnnouncement
MOIPBroadcastAnnouncementStartDate:2009-01-01 00:00
MOIPBroadcastAnnouncementEndDate:2010-12-31 00:00
MOIPBroadcastAnnouncementAudience:SYSTEM
modifiersName:cn=Directory Manager,cn=Root DNs,cn=config' > $BA_TEMPLATE
}

createOpcoInputFile() {
    FILE=$1
    echo "    Creating opco file $OPCO_FILE"
echo '
bl5Opco=172.30.242.208
bl6Opco=172.30.242.218
bl7Opco=172.30.242.228
' > $FILE
    echo "    Created opco file $OPCO_FILE"

}

