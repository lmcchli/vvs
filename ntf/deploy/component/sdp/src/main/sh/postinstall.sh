#!/bin/sh

SCRIPTNAME=postinstall.sh

log () {
  /bin/logger -t %{name} "$SCRIPTNAME (version %{version}-%{release}) $1"
  echo "%{name} $SCRIPTNAME (version %{version}-%{release}) $1"
}

if [[ ! -d /opt/moip/logs/ntf ]]; then
    mkdir -p /opt/moip/logs/ntf
    chmod 755 /opt/moip/logs -R
    chown mmas:mmas /opt/moip/logs -R
fi

if [[ ! -d /opt/moip/events ]]; then
    mkdir -p /opt/moip/events
    chmod 755 /opt/moip/events
    chown mmas:mmas /opt/moip/events
fi

# This RPM provides a monitrc file.
# It is necessary to reinitialize the running Monit daemon.
/usr/bin/monit reload

TARGET='su -p mmas -c "/opt/moip/ntf/bin/ntf rotateLog"'
TMPFILE=/tmp/cron.info.$$

# minute(0-59) hour(0-23) day-of-month(1-31) month(1-12) day-of-week(0-7), where 0 and 7 is Sunday)
CRONJOB="*/5 * * * * ${TARGET}"

# Add the cron job to the crontable
# Check the job is not already set.
PRESENCE=`crontab -l | grep "${TARGET}" | wc -l`
if [ ${PRESENCE} -eq 0 ] ; then

	# Capture whatever is already in the crontab and append the new job to it
	(crontab -l | grep -v "^#") > ${TMPFILE}
	echo "${CRONJOB}" >> ${TMPFILE}
	crontab -u root ${TMPFILE}

	# Verify the JOB is created
	PRESENCE=`crontab -l | grep "${TARGET}" | wc -l`

	# Clean the temp file
	rm ${TMPFILE}
fi



