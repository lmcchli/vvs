#!/bin/sh

SCRIPTNAME=preremove.sh

log () {
  /bin/logger -t %{name} "$SCRIPTNAME (version %{version}-%{release}) $1"
  echo "%{name} $SCRIPTNAME (version %{version}-%{release}) $1"
}

if [ "$1" = "0" ]; then
  TARGET="/opt/moip/mas/bin/mas rotateLog"
  TMPFILE=/tmp/cron.info.$$

  # Remove the cron-job
  crontab -l | grep -v "^#" | grep -v "${TARGET}" > ${TMPFILE}
  crontab -u root ${TMPFILE}
  rm ${TMPFILE}

  # Verify it is removed
  PRESENCE=`crontab -l | grep "${TARGET}" | wc -l`
  if [ ${PRESENCE} -ne 0 ] ; then
    log "==="
    log "=== WARNING: The cron job ${TARGET} was not removed from the cron tab."
    log "==="
  fi
fi
