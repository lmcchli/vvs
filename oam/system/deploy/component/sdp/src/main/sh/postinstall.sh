#!/bin/bash

SCRIPTNAME=postinstall.sh

log () {
  /bin/logger -t %{name} "$SCRIPTNAME (version %{version}-%{release}) $1"
  echo "%{name} $SCRIPTNAME (version %{version}-%{release}) $1"
}

log "Setup /opt/moip/logs and /opt/moip/perf for moip_oam"
mkdir /opt/moip/logs
chmod 755 /opt/moip/logs
chown mmas:mmas /opt/moip/logs
mkdir /opt/moip/perf
chmod 755 /opt/moip/perf
chown mmas:mmas /opt/moip/perf

# This RPM provides a monitrc file.
# It is necessary to reinitialize the running Monit daemon.
/usr/bin/monit reload

# Install the cron job for log rotation

TARGET='su -p mmas -c "/opt/msgcore/oam/bin/oam rotateLog moip"'
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

if [ ${PRESENCE} -eq 0 ] ; then
    # An error occured, 
    log "==="
    log "=== WARNING: The cron job "${TARGET}" has not been successfully installed in the cron tab."
    log "===          As a consequence, the runtime log file will not be automatically rotated."
    log "==="
    exit 0;
fi

exit 0
