#!/bin/sh

COMMAND="`basename ${0} | cut -d_ -f1`"
VERSION="`basename ${0} | cut -d_ -f2`"

BASE="/opt/subversion-${VERSION}"
if [ ! -d "${BASE}" ]; then
	echo "${VERSION} not supported" 1>&2
	exit 1
fi

export LD_LIBRARY_PATH="${BASE}/lib:${LD_LIBRARY_PATH}"
"${BASE}/bin/${COMMAND}" "${@}"
