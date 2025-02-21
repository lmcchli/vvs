#!/bin/sh

SCRIPTNAME=postinstall.sh

log () {
  /bin/logger -t %{name} "$SCRIPTNAME (version %{version}-%{release}) $1"
  echo "%{name} $SCRIPTNAME (version %{version}-%{release}) $1"
}

HOMEREPORT=/opt/moip/reports
PERLSCRIPT=vmReportCronJob.pl
TARGET=${HOMEREPORT}/${PERLSCRIPT}
CRONJOBOM00=" 0 1 1 * * /usr/bin/perl ${TARGET}"
CRONJOBOM01=" 0 2 1 * * /usr/bin/perl ${TARGET}"
TMPFILE=/tmp/cron.info
CRONJOB=""

log "Postinstall for VVS_REPORTS starting "

# Change permissions on the report folder, perl files, conf files
chmod 0775 ${HOMEREPORT}
chmod 0550 ${HOMEREPORT}/*.p?
chmod 0644 ${HOMEREPORT}/vmCounters.txt

log "Configuring on the om-00 and om01 nodes only "

# Configuring on the om-00 and om-01 nodes only
NODE=`hostname`

if [ "${NODE}" == "om-00" ] ; then
   CRONJOB=$CRONJOBOM00
elif [ "${NODE}" == "om-01" ] ; then
   CRONJOB=$CRONJOBOM01
else
   exit 0;
fi

log "Create the reports repository if not already created "
# Create the aggregated folder if not already created
AGGREGATED_REPORTS_FOLDER=/opt/global/perf/moip/aggregated/reports
if [ ! -d ${AGGREGATED_REPORTS_FOLDER} ] ; then
  mkdir -p ${AGGREGATED_REPORTS_FOLDER}
  chmod 0755 ${AGGREGATED_REPORTS_FOLDER}
  chown mmas:mmas ${AGGREGATED_REPORTS_FOLDER}
fi

log "Create the reports folder if not already created "
VVS_OUTPUT_REPORT_FOLDER=${AGGREGATED_REPORTS_FOLDER}/vvs
if [ ! -d ${VVS_OUTPUT_REPORT_FOLDER} ] ; then
  mkdir -p ${VVS_OUTPUT_REPORT_FOLDER}
  chmod 0755 ${VVS_OUTPUT_REPORT_FOLDER}
  chown reports:mmas ${VVS_OUTPUT_REPORT_FOLDER}
fi 

# Add he cron job to the crontable
if [ -f ${TARGET} ] ; then
 
	# Check the job is not already set.
	# If so the line  "0 1 1 * * /usr/bin/perl /opt/moip/reports/vmReportCronJob.pl" will appear.
	PRESENCE=`crontab -l | grep ${PERLSCRIPT} | wc -l`
	if [ ${PRESENCE} -eq 0 ] ; then

		log "add line to crontab "

		# Capture whatever is already in the crontab and append the new job to it
		(crontab -l | grep -v "^#") > ${TMPFILE}
		echo "${CRONJOB}" >> ${TMPFILE}
		crontab -u root ${TMPFILE}

		# Verify the JOB is created
		PRESENCE=`crontab -l | grep ${PERLSCRIPT} | wc -l`  #  Expected previous line displayed

		# Clean the temp file
		rm ${TMPFILE}
		
		log "The cron job ${TARGET} has been successfully installed in the cron tab."
	fi
	if [ ${PRESENCE} -eq 0 ] ; then
		# An error occured, 
		log "==="
		log "=== ERROR: The cron job ${TARGET} has not been successfully installed in the cron tab."
		log "==="
		exit 1;
	fi
	
else
	# An error occured, 
	log "==="
	log "=== ERROR: The script file ${TARGET} is missing, the cron-job cannot be intialized."
	log "==="
	exit 1;

fi


	log "Postinstall for VVS_REPORTS ending "
exit 0;

