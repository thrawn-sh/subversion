#!/bin/bash -ex

export CC=`which x86_64-linux-gnu-gcc`

function init() { # {{{1
	aptitude -r install \
	apache2 \
	autoconf \
	build-essential \
	git \
	libexpat1-dev \
	libxml2-dev \
	sudo \
	tmux \
	uuid-dev \
	vim \
	wget \
	zip \
	zlib1g-dev
} #}}}1

function apr() { #{{{1
	local directory="${1}"; shift
	local prefix="${1}"; shift
	local version="${1}"; shift

	local maker="${prefix}/.install/apr"
	if [ -f "${maker}" ]; then
		echo "apr already installed in ${prefix}"
		return
	fi

	local archive="/opt/download/apr-${version}.tar.gz"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/apr/apr-${version}.tar.gz" -O "${archive}"
	fi

	cd "${directory}"
	tar -xzf "${archive}"
	cd "apr-${version}"

	./configure \
		--enable-threads \
		--prefix="${prefix}"
	make
	make install

	touch "${maker}"
} #}}}1
function apr-util() { #{{{1
	local directory="${1}"; shift
	local prefix="${1}"; shift
	local version="${1}"; shift

	local maker="${prefix}/.install/apr-util"
	if [ -f "${maker}" ]; then
		echo "apr-util already installed in ${prefix}"
		return
	fi

	local archive="/opt/download/apr-util-${version}.tar.gz"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/apr/apr-util-${version}.tar.gz" -O "${archive}"
	fi

	cd "${directory}"
	tar -xzf "${archive}"
	cd "apr-util-${version}"

	if [ "${version}" = "0.9.7" ]; then 
	./configure \
		--prefix="${prefix}" \
		--with-apr="${prefix}/bin/apr-config" \
		--with-openssl="${prefix}"
	else
	./configure \
		--prefix="${prefix}" \
		--with-apr="${prefix}" \
		--with-openssl="${prefix}"
	fi

	make
	make install

	touch "${maker}"
} #}}}1
function httpd() { #{{{1
	local directory="${1}"; shift
	local prefix="${1}"; shift
	local version="${1}"; shift

	local maker="${prefix}/.install/httpd"
	if [ -f "${maker}" ]; then
		echo "httpd already installed in ${prefix}"
		return
	fi

	local archive="/opt/download/httpd-${version}.tar.gz"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/httpd/httpd-${version}.tar.gz" -O "${archive}"
	fi

	cd "${directory}"
	tar -xzf "${archive}"
	cd "httpd-${version}"

	if [ "${version}" = "2.0.65" ]; then
		./configure \
			--enable-auth-basic=shared \
			--enable-auth-digest=shared \
			--enable-dav=shared \
			--enable-deflate=shared \
			--enable-so \
			--enable-ssl=shared \
			--prefix="${prefix}" \
			--with-apr-util="${prefix}/bin/apu-config" \
			--with-apr="${prefix}/bin/apr-config" \
			--with-ssl="${prefix}"
	else
		./configure \
			--enable-auth-basic=shared \
			--enable-auth-digest=shared \
			--enable-dav=shared \
			--enable-deflate=shared \
			--enable-so \
			--enable-ssl=shared \
			--prefix="${prefix}" \
			--with-apr-util="${prefix}" \
			--with-apr="${prefix}" \
			--with-ssl="${prefix}"
	fi

	make
	make install

	touch "${maker}"
} #}}}1
function neon() { #{{{1
	local directory="${1}"; shift
	local prefix="${1}"; shift
	local version="${1}"; shift

	local maker="${prefix}/.install/neon"
	if [ -f "${maker}" ]; then
		echo "neon already installed in ${prefix}"
		return
	fi

	local archive="/opt/download/neon-${version}.tar.gz"
	if [ ! -f "${archive}" ]; then
		wget "http://www.webdav.org/neon/neon-${version}.tar.gz" -O "${archive}"
	fi

	cd "${directory}"
	tar -xzf "${archive}"
	cd "neon-${version}"

	export CPPFLAGS="-I${prefix}/include"
	export LDFLAGS="-L${prefix}/lib"
	./configure \
		--enable-shared \
		--prefix="${prefix}" \
		--with-ssl=openssl
	make
	make install
	unset CPPFLAGS
	unset LDFLAGS

	touch "${maker}"
} #}}}1
function openssl() { #{{{1
	local directory="${1}"; shift
	local prefix="${1}"; shift
	local version="${1}"; shift

	local maker="${prefix}/.install/openssl"
	if [ -f "${maker}" ]; then
		echo "openssl already installed in ${prefix}"
		return
	fi

	local archive="/opt/download/openssl-${version}.tar.gz"
	if [ ! -f "${archive}" ]; then
		wget "http://www.openssl.org/source/openssl-${version}.tar.gz" -O "${archive}"
	fi

	cd "${directory}"
	tar -xzf "${archive}"
	cd "openssl-${version}"

	./config \
		--prefix="${prefix}" \
		no-asm \
		shared \
		threads \
		zlib-dynamic

	make
	make install

	touch "${maker}"
} #}}}1
function scons() { #{{{1
	shift
	local prefix="${1}"; shift
	local version="${1}"; shift
	local directory=`mktemp -d`

	local maker="${prefix}/.install/scons"
	if [ -f "${maker}" ]; then
		echo "scons already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/scons-local-${version}.tar.gz"
	if [ ! -f "${archive}" ]; then
		wget "http://prdownloads.sourceforge.net/scons/scons-local-${version}.tar.gz" -O "${archive}"
	fi

	cd "${directory}"
	tar -xzf "${archive}"
	mv "${directory}" "${prefix}/scons"

	rm -rf "${directory}"

	touch "${maker}"
} #}}}1
function serf() { #{{{1
	local directory="${1}"; shift
	local prefix="${1}"; shift
	local version="${1}"; shift

	local maker="${prefix}/.install/serf"
	if [ -f "${maker}" ]; then
		echo "serf already installed in ${prefix}"
		return
	fi

	local archive="/opt/download/serf-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://serf.googlecode.com/files/serf-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "serf-${version}"

	"${prefix}/scons/scons.py" \
		APR="${prefix}" \
		APU="${prefix}" \
		OPENSSL="${prefix}" \
		PREFIX="${prefix}" \
		install

	touch "${maker}"
} #}}}1
function sqllite() { #{{{1
	local directory="${1}"; shift
	local prefix="${1}"; shift
	local version="${1}"; shift

	local maker="${prefix}/.install/sqllite"
	if [ -f "${maker}" ]; then
		echo "sqllite already installed in ${prefix}"
		return
	fi

	local archive="/opt/download/sqlite-autoconf-${version}.tar.gz"
	if [ ! -f "${archive}" ]; then
		wget "http://www.sqlite.org/2013/sqlite-autoconf-${version}.tar.gz" -O "${archive}"
	fi

	cd "${directory}"
	tar -xzf "${archive}"
	cd sqlite-autoconf-${version}

	./configure \
		--prefix="${prefix}"

	make
	make install

	touch "${maker}"
} #}}}1

function subversion_1_0() { #{{{1
	local version="1.0.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 0.9.7
	apr-util "${directory}" "${prefix}" 0.9.7
	httpd    "${directory}" "${prefix}" 2.0.65
	sqllite  "${directory}" "${prefix}" 3080100
	neon     "${directory}" "${prefix}" 0.25.5

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apr="${prefix}/bin/apr-config" \
		--with-apr-util="${prefix}/bin/apu-config" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-neon="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db
	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1
function subversion_1_1() { #{{{1
	local version="1.1.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 0.9.7
	apr-util "${directory}" "${prefix}" 0.9.7
	httpd    "${directory}" "${prefix}" 2.0.65
	sqllite  "${directory}" "${prefix}" 3080100
	neon     "${directory}" "${prefix}" 0.25.5

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apr="${prefix}/bin/apr-config" \
		--with-apr-util="${prefix}/bin/apu-config" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-neon="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db
	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1
function subversion_1_2() { #{{{1
	local version="1.2.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 1.5.0
	apr-util "${directory}" "${prefix}" 1.5.3
	httpd    "${directory}" "${prefix}" 2.2.26
	sqllite  "${directory}" "${prefix}" 3080100
	neon     "${directory}" "${prefix}" 0.25.5

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apr-util="${prefix}" \
		--with-apr="${prefix}" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-neon="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db

	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1
function subversion_1_3() { #{{{1
	local version="1.3.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 1.5.0
	apr-util "${directory}" "${prefix}" 1.5.3
	httpd    "${directory}" "${prefix}" 2.2.26
	sqllite  "${directory}" "${prefix}" 3080100
	neon     "${directory}" "${prefix}" 0.25.5

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apr-util="${prefix}" \
		--with-apr="${prefix}" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-neon="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db

	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1
function subversion_1_4() { #{{{1
	local version="1.4.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 1.5.0
	apr-util "${directory}" "${prefix}" 1.5.3
	httpd    "${directory}" "${prefix}" 2.2.26
	sqllite  "${directory}" "${prefix}" 3080100
	neon     "${directory}" "${prefix}" 0.25.5

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apr-util="${prefix}" \
		--with-apr="${prefix}" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-neon="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db

	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1
function subversion_1_5() { #{{{1
	local version="1.5.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 1.5.0
	apr-util "${directory}" "${prefix}" 1.5.3
	httpd    "${directory}" "${prefix}" 2.2.26
	sqllite  "${directory}" "${prefix}" 3080100
	neon     "${directory}" "${prefix}" 0.25.5

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apr-util="${prefix}" \
		--with-apr="${prefix}" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-neon="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db

	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1
function subversion_1_6() { #{{{1
	local version="1.6.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 1.5.0
	apr-util "${directory}" "${prefix}" 1.5.3
	httpd    "${directory}" "${prefix}" 2.2.26
	sqllite  "${directory}" "${prefix}" 3080100
	neon     "${directory}" "${prefix}" 0.25.5

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apache-libexecdir="${prefix}/modules" \
		--with-apr-util="${prefix}" \
		--with-apr="${prefix}" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-neon="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db

	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1
function subversion_1_7() { #{{{1
	local version="1.7.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 1.5.0
	apr-util "${directory}" "${prefix}" 1.5.3
	httpd    "${directory}" "${prefix}" 2.2.26
	sqllite  "${directory}" "${prefix}" 3080100
	scons    "${directory}" "${prefix}" 2.3.0
	serf     "${prefix}" 1.3.2

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apache-libexecdir="${prefix}/modules" \
		--with-apr-util="${prefix}" \
		--with-apr="${prefix}" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-serf="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db
	
	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1
function subversion_1_8() { #{{{1
	local version="1.8.0"
	local directory=`mktemp -d`
	local prefix="/opt/subversion-${version}"
	mkdir -p "${prefix}/.install"

	openssl  "${directory}" "${prefix}" 0.9.8g
	apr      "${directory}" "${prefix}" 1.5.0
	apr-util "${directory}" "${prefix}" 1.5.3
	httpd    "${directory}" "${prefix}" 2.2.26
	sqllite  "${directory}" "${prefix}" 3080100
	scons    "${directory}" "${prefix}" 2.3.0
	serf     "${prefix}" 1.3.2

	local maker="${prefix}/.install/subversion"
	if [ -f "${maker}" ]; then
		echo "subversion already installed in ${prefix}"
		rm -rf "${directory}"
		return
	fi

	local archive="/opt/download/subversion-${version}.tar.bz2"
	if [ ! -f "${archive}" ]; then
		wget "http://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
	fi

	cd "${directory}"
	tar -xjf "${archive}"
	cd "subversion-${version}"

	./configure \
		--disable-keychain \
		--prefix="${prefix}" \
		--with-apache-libexecdir="${prefix}/modules" \
		--with-apr-util="${prefix}" \
		--with-apr="${prefix}" \
		--with-apxs="${prefix}/bin/apxs" \
		--with-serf="${prefix}" \
		--with-sqlite="${prefix}" \
		--without-berkeley-db \
		--without-gpg-agent
	make
	make install

	touch "${maker}"
	rm -rf "${directory}"
} #}}}1

#init

mkdir -p "/opt/download"
#subversion_1_0 # won't build apache modules
#subversion_1_1 # won't build apache modules
subversion_1_2
subversion_1_3
subversion_1_4
subversion_1_5
subversion_1_6
subversion_1_7
subversion_1_8
