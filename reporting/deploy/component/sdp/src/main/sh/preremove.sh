#!/bin/sh

SCRIPTNAME=preremove.sh

log () {
  /bin/logger -t %{name} "$SCRIPTNAME (version %{version}-%{release}) $1"
  echo "%{name} $SCRIPTNAME (version %{version}-%{release}) $1"
}

if [ "$1" = "0" ]; then
  PERLSCRIPT=vmReportCronJob.pl
  TARGET=/opt/moip/reports/${PERLSCRIPT}
  TMPFILE=/tmp/cron.info

  # Remove the cron-job
  crontab -l | grep -v "^#.*" | grep -v "${PERLSCRIPT}" > ${TMPFILE}
  crontab -u root ${TMPFILE}
  rm ${TMPFILE}

  # Verify it is removed
  PRESENCE=`crontab -l | grep ${PERLSCRIPT} | wc -l`
  if [ ${PRESENCE} -ne 0 ] ; then
    log "==="
    log "=== ERROR: The cron job ${TARGET} was not removed from the cron tab."
    log "==="
  fi
fi
