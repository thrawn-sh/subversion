#! /bin/sh

# Define LSB log_* functions.
. /lib/lsb/init-functions

VERSION="`basename ${0} | cut -d_ -f2`"

# path to xinit exec
DAEMON="/opt/bin/apachectl_${VERSION}"

# script name
NAME="apache-svn-${VERSION}"

# app name
DESC="Apache Subversion ${VERSION}"

test -x ${DAEMON} || exit 0

do_start() {
	do_status > /dev/null
	if [ "${?}" -eq 0 ]; then
		echo "${DESC} is already running. Use restart."
		exit 0
	fi

	log_daemon_msg "Starting ${DESC}"
	${DAEMON} start
	log_end_msg ${?}
}

do_stop() {
	do_status > /dev/null
	if [ "${?}" -ne 0 ]; then
		echo "${DESC} is already stopped."
		exit 0
	fi

	log_daemon_msg "Stopping ${DESC}"
	${DAEMON} stop
	log_end_msg ${?}
}

do_status() {
	status_of_proc -p "/var/run/apache-subversion-${VERSION}.pid" "${NAME}" "${DESC}" && return 0 || return $?
}

case "${1}" in
	start)
		do_start
		;;
	stop)
		do_stop
		;;
	restart|force-reload)
		${0} stop
		sleep 1
		${0} start
		;;
	status)
		do_status
	;;
	*)
		log_action_msg "Usage: ${SCRIPTNAME} {start|stop|restart|force-reload|status}"
		exit 2
		;;
esac

exit 0
