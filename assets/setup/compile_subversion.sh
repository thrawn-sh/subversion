#!/bin/bash
#
# Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e
set -o pipefail
set -u

THIS_PATH="$(readlink --canonicalize-existing "${0}")"
THIS_NAME="$(basename "${THIS_PATH}")"
THIS_DIR="$(dirname "${THIS_PATH}")"

export CC=`which x86_64-linux-gnu-gcc`
DOWNLOAD="/opt/download"

function init() { # {{{1
    local marker="/usr/local/.init"
    if [ -f "${marker}" ]; then
        echo "init already complete"
        return
    fi

    apt update

    apt --yes install   \
        autoconf        \
        build-essential \
        git             \
        libexpat1-dev   \
        libxml2-dev     \
        libxml2-utils   \
        locales         \
        python          \
        python-pip      \
        rsync           \
        uuid-dev        \
        wget            \
        zip             \
        zlib1g-dev

    echo "en_US.UTF-8 UTF-8" > /etc/locale.gen
    locale-gen

    pip install git+https://github.com/larsks/dockerize

    # some manpages are broken => ignore
    cat > /usr/bin/pod2man <<EOF
#!/bin/sh
exit 0
EOF

    touch "${marker}"
} #}}}1
function min_archive() { # {{{1
    local directory="${1}"; shift
    local version="${1}"; shift

    local marker="/usr/local/.docker"
    if [ -f "${marker}" ]; then
        echo "docker already complete"
        return
    fi

    cd /
    rm --force --recursive "${directory}"

    mkdir --parents "${directory}/etc"
    mkdir --parents "${directory}/tmp"
    mkdir --parents "${directory}/var/run"
    mkdir --parents "${directory}/var/www"
    mkdir --parents "${directory}/var/www/htdocs"
    mkdir --parents "${directory}/var/www/svn"

    chmod 1777 "${directory}/tmp"

    ldconfig

    cat > "${directory}/etc/ld.so.conf" <<EOF
/lib
/lib64
/usr/local/lib
EOF

    dockerize -t subversion --filetools --symlinks="copy-all" --user="www-data" --group="www-data" --no-build --output-dir="${directory}" \
        --add-file "${THIS_DIR}/httpd.conf"   /etc/httpd.conf   \
        --add-file "${THIS_DIR}/mime.types"   /etc/mime.types   \
        --add-file "${THIS_DIR}/passwd.basic" /etc/passwd.basic \
        --add-file "${THIS_DIR}/svnpath.auth" /etc/svnpath.auth \
        --add-file "${THIS_DIR}/index.html"   /var/www/htdocs/index.html \
        --entrypoint="/usr/local/bin/httpd -f /etc/httpd.conf -DFOREGROUND" \
        /bin/bash                            \
        /bin/ln                              \
        /bin/mv                              \
        /lib/x86_64-linux-gnu/libnsl.so.1    \
        /lib/x86_64-linux-gnu/libresolv.so.2 \
        /lib/x86_64-linux-gnu/libz.so.1      \
        /sbin/ldconfig                       \
        /usr/bin/ldd                         \
        /usr/local/bin/httpd                 \
        /usr/local/bin/svn                   \
        /usr/local/bin/svnadmin              \
        /usr/local/lib/libcrypto.so.0.9.8    \
        /usr/local/lib/libssl.so.0.9.8       \
        /usr/local/modules/*.so

    ln --symbolic bash "${directory}/bin/sh"
    chown --recursive www-data:www-data "${directory}/var/www/svn"
    set +e
    find "${directory}" -type f -print0 | xargs --no-run-if-empty --null strip --strip-all
    set -e

    # generate ld cache
    chroot "${directory}" /sbin/ldconfig
    rm --force --recursive "${directory}/sbin"

    # verify all dependant shared libraries are available
    cd "${directory}"
    for file in $(find . -type f); do
        set +e
        chroot . /usr/bin/ldd "${file}" | sort --unique | grep "not found"
        local ok="${?}"
        set -e
        if [ "${ok}" -ne 1 ]; then
            return 1
        fi
    done
    cd /
    rm --force --recursive "${directory}/usr/bin"

    "${THIS_DIR}/create_svn_test.sh" "${version}" "${target}/var/www"

    rm --force "${directory}/Dockerfile"
    tar --create --file /tmp/subversion.tar --directory="${directory}" .
    mv /tmp/subversion.tar "${directory}"

    local tag="$(echo "${version}" | cut --delimiter=. --fields=-2)"
    cat > "${directory}/Dockerfile" <<EOF
FROM   scratch
EXPOSE 80/tcp

HEALTHCHECK CMD /usr/local/bin/svn info --username=svnuser --password=svnpass --no-auth-cache --non-interactive http://127.0.0.1/svn-basic/test/trunk/00000000-0000-0000-0000-000000000000/ || exit 1

ADD subversion.tar /

ENTRYPOINT ["/usr/local/bin/httpd", "-f", "/etc/httpd.conf", "-DFOREGROUND"]
EOF

    cat > "${directory}/build.sh" <<EOF
#!/bin/bash

set -e
set -o pipefail
set -u

THIS_PATH="\$(readlink --canonicalize-existing "\${0}")"
THIS_NAME="\$(basename "\${THIS_PATH}")"
THIS_DIR="\$(dirname "\${THIS_PATH}")"

if [ \$# -ne 1 ]; then
    echo "\${0} <DOCKER HUB PASSWORD>" 1>&2
    exit 1
fi
PASS="\${1}"; shift

echo "\${PASS}" | docker login --password-stdin --username="shadowhunt"
docker build --compress --force-rm --quiet --rm --tag shadowhunt/subversion:${tag} "\${THIS_DIR}"
docker push shadowhunt/subversion:${tag}
docker ps --all --filter="status=exited" --no-trunc --quiet | xargs --no-run-if-empty docker rm --force
docker rmi --force shadowhunt/subversion:${tag}
docker images --all --filter "dangling=true" --quiet --no-trunc | xargs --no-run-if-empty docker rmi --force
docker logout
EOF
    chmod 0755 "${directory}/build.sh"

    touch "${marker}"
} #}}}1

function apr() { #{{{1
    local directory="${1}"; shift
    local version="${1}"; shift

    local marker="/usr/local/.install/apr"
    if [ -f "${marker}" ]; then
        echo "apr already installed in /usr/local"
        return
    fi

    local archive="/opt/download/apr-${version}.tar.gz"
    if [ ! -s "${archive}" ]; then
        wget "https://archive.apache.org/dist/apr/apr-${version}.tar.gz" -O "${archive}"
    fi

    cd "${directory}"
    tar -xzf "${archive}"
    cd "apr-${version}"

    ./configure \
        --enable-threads \
        --prefix="/usr/local"
    make
    make install

    touch "${marker}"
} #}}}1
function apr-util() { #{{{1
    local directory="${1}"; shift
    local version="${1}"; shift

    local marker="/usr/local/.install/apr-util"
    if [ -f "${marker}" ]; then
        echo "apr-util already installed in /usr/local"
        return
    fi

    local archive="${DOWNLOAD}/apr-util-${version}.tar.gz"
    if [ ! -s "${archive}" ]; then
        wget "https://archive.apache.org/dist/apr/apr-util-${version}.tar.gz" -O "${archive}"
    fi

    cd "${directory}"
    tar -xzf "${archive}"
    cd "apr-util-${version}"

    if [ "${version}" = "0.9.7" ]; then
    ./configure \
        --prefix="/usr/local" \
        --with-apr="/usr/local/bin/apr-config" \
        --with-openssl="/usr/local"
    else
    ./configure \
        --prefix="/usr/local" \
        --with-apr="/usr/local" \
        --with-openssl="/usr/local"
    fi

    make
    make install

    touch "${marker}"
} #}}}1
function httpd() { #{{{1
    local directory="${1}"; shift
    local version="${1}"; shift

    local marker="/usr/local/.install/httpd"
    if [ -f "${marker}" ]; then
        echo "httpd already installed in /usr/local"
        return
    fi

    local archive="${DOWNLOAD}/httpd-${version}.tar.gz"
    if [ ! -s "${archive}" ]; then
        wget "https://archive.apache.org/dist/httpd/httpd-${version}.tar.gz" -O "${archive}"
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
            --prefix="/usr/local" \
            --with-apr-util="/usr/local/bin/apu-config" \
            --with-apr="/usr/local/bin/apr-config" \
            --with-ssl="/usr/local"
    else
        ./configure \
            --enable-auth-basic=shared \
            --enable-auth-digest=shared \
            --enable-dav=shared \
            --enable-deflate=shared \
            --enable-so \
            --enable-ssl=shared \
            --prefix="/usr/local" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-ssl="/usr/local"
    fi

    make
    make install

    touch "${marker}"
} #}}}1
function neon() { #{{{1
    local directory="${1}"; shift
    local version="${1}"; shift

    local marker="/usr/local/.install/neon"
    if [ -f "${marker}" ]; then
        echo "neon already installed in /usr/local"
        return
    fi

    local archive="${DOWNLOAD}/neon-${version}.tar.gz"
    if [ ! -s "${archive}" ]; then
        wget "http://download.nust.na/pub2/openpkg1/sources/DST/neon/neon-${version}.tar.gz" -O "${archive}"
    fi

    cd "${directory}"
    tar -xzf "${archive}"
    cd "neon-${version}"

    export CPPFLAGS="-I/usr/local/include"
    export LDFLAGS="-L/usr/local/lib"
    ./configure \
        --enable-shared \
        --prefix="/usr/local" \
        --with-ssl=openssl
    make
    make install
    unset CPPFLAGS
    unset LDFLAGS

    touch "${marker}"
} #}}}1
function openssl() { #{{{1
    local directory="${1}"; shift
    local version="${1}"; shift

    local marker="/usr/local/.install/openssl"
    if [ -f "${marker}" ]; then
        echo "openssl already installed in /usr/local"
        return
    fi

    local archive="${DOWNLOAD}/openssl-${version}.tar.gz"
    if [ ! -s "${archive}" ]; then
        wget "https://www.openssl.org/source/openssl-${version}.tar.gz" -O "${archive}"
    fi

    cd "${directory}"
    tar -xzf "${archive}"
    cd "openssl-${version}"

    ./config \
        --prefix="/usr/local" \
        no-asm \
        shared \
        threads \
        zlib-dynamic

    make
    make install

    touch "${marker}"
} #}}}1
function scons() { #{{{1
    shift
    local version="${1}"; shift
    local directory=`mktemp -d`

    local marker="/usr/local/.install/scons"
    if [ -f "${marker}" ]; then
        echo "scons already installed in /usr/local"
        rm --force --recursive "${directory}"
        return
    fi

    local archive="${DOWNLOAD}/scons-local-${version}.tar.gz"
    if [ ! -s "${archive}" ]; then
        wget "https://sourceforge.net/projects/scons/files/scons-local-${version}.tar.gz" -O "${archive}"
    fi

    cd "${directory}"
    tar -xzf "${archive}"
    mv "${directory}" "/usr/local/scons"

    rm --force --recursive "${directory}"

    touch "${marker}"
} #}}}1
function serf() { #{{{1
    local directory="${1}"; shift
    local version="${1}"; shift

    local marker="/usr/local/.install/serf"
    if [ -f "${marker}" ]; then
        echo "serf already installed in /usr/local"
        return
    fi

    local archive="${DOWNLOAD}/serf-${version}.tar.bz2"
    if [ ! -s "${archive}" ]; then
        wget "https://archive.apache.org/dist/serf/serf-${version}.tar.bz2" -O "${archive}"
    fi

    cd "${directory}"
    tar -xjf "${archive}"
    cd "serf-${version}"

    "/usr/local/scons/scons.py" \
        APR="/usr/local" \
        APU="/usr/local" \
        OPENSSL="/usr/local" \
        PREFIX="/usr/local" \
        install

    touch "${marker}"
} #}}}1
function sqllite() { #{{{1
    local directory="${1}"; shift
    local version="${1}"; shift

    local marker="/usr/local/.install/sqllite"
    if [ -f "${marker}" ]; then
        echo "sqllite already installed in /usr/local"
        return
    fi

    local archive="${DOWNLOAD}/sqlite-autoconf-${version}.tar.gz"
    if [ ! -s "${archive}" ]; then
        wget "https://www.sqlite.org/2013/sqlite-autoconf-${version}.tar.gz" -O "${archive}"
    fi

    cd "${directory}"
    tar -xzf "${archive}"
    cd sqlite-autoconf-${version}

    ./configure \
        --prefix="/usr/local"

    make
    make install

    touch "${marker}"
} #}}}1

function subversion_1_0() { #{{{1
    local version="1.0.0"
    local directory=`mktemp -d`
    local prefix="/opt/subversion-${version}"
    mkdir --parents "/usr/local/.install"

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 0.9.7
    apr-util "${directory}" 0.9.7
    httpd    "${directory}" 2.0.65
    sqllite  "${directory}" 3080100
    neon     "${directory}" 0.25.5

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
        return
    fi

    local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
    if [ ! -s "${archive}" ]; then
        wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
    fi

    cd "${directory}"
    tar -xjf "${archive}"
    cd "subversion-${version}"

    ./configure \
        --disable-keychain \
        --prefix="/usr/local" \
        --with-apr="/usr/local/bin/apr-config" \
        --with-apr-util="/usr/local/bin/apu-config" \
        --with-apxs="/usr/local/bin/apxs" \
        --with-neon="/usr/local" \
        --with-sqlite="/usr/local" \
        --without-berkeley-db
    make
    make install

    touch "${marker}"
    rm --force --recursive "${directory}"
} #}}}1
function subversion_1_1() { #{{{1
    local version="1.1.0"
    local directory=`mktemp -d`
    local prefix="/opt/subversion-${version}"
    mkdir --parents "/usr/local/.install"

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 0.9.7
    apr-util "${directory}" 0.9.7
    httpd    "${directory}" 2.0.65
    sqllite  "${directory}" 3080100
    neon     "${directory}" 0.25.5

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
        return
    fi

    local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
    if [ ! -s "${archive}" ]; then
        wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
    fi

    cd "${directory}"
    tar -xjf "${archive}"
    cd "subversion-${version}"

    ./configure \
        --disable-keychain \
        --prefix="/usr/local" \
        --with-apr="/usr/local/bin/apr-config" \
        --with-apr-util="/usr/local/bin/apu-config" \
        --with-apxs="/usr/local/bin/apxs" \
        --with-neon="/usr/local" \
        --with-sqlite="/usr/local" \
        --without-berkeley-db
    make
    make install

    touch "${marker}"
    rm --force --recursive "${directory}"
} #}}}1
function subversion_1_2() { #{{{1
    echo "==============================================================================="
    echo "= 1.2                                                                         ="
    echo "==============================================================================="
    local version="1.2.0"
    local directory=`mktemp -d`
    mkdir --parents "/usr/local-${version}"
    rm --force --recursive /usr/local
    ln --symbolic /usr/local-${version} /usr/local
    mkdir --parents "/usr/local/.install"

    init

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 1.5.0
    apr-util "${directory}" 1.5.3
    httpd    "${directory}" 2.2.26
    sqllite  "${directory}" 3080100
    neon     "${directory}" 0.25.5

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
    else
        local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
        if [ ! -s "${archive}" ]; then
            wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
        fi

        cd "${directory}"
        tar -xjf "${archive}"
        cd "subversion-${version}"

        ./configure \
            --disable-keychain \
            --prefix="/usr/local" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-apxs="/usr/local/bin/apxs" \
            --with-neon="/usr/local" \
            --with-sqlite="/usr/local" \
            --without-berkeley-db

        make
        make install

        touch "${marker}"
    fi
    rm --force --recursive "${directory}"

    target="/var/tmp/${version}"

    min_archive "${target}" "${version}"
} #}}}1
function subversion_1_3() { #{{{1
    echo "==============================================================================="
    echo "= 1.3                                                                         ="
    echo "==============================================================================="
    local version="1.3.0"
    local directory=`mktemp -d`
    mkdir --parents "/usr/local-${version}"
    rm --force --recursive /usr/local
    ln --symbolic /usr/local-${version} /usr/local
    mkdir --parents "/usr/local/.install"

    init

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 1.5.0
    apr-util "${directory}" 1.5.3
    httpd    "${directory}" 2.2.26
    sqllite  "${directory}" 3080100
    neon     "${directory}" 0.25.5

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
    else
        local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
        if [ ! -s "${archive}" ]; then
            wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
        fi

        cd "${directory}"
        tar -xjf "${archive}"
        cd "subversion-${version}"

        ./configure \
            --disable-keychain \
            --prefix="/usr/local" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-apxs="/usr/local/bin/apxs" \
            --with-neon="/usr/local" \
            --with-sqlite="/usr/local" \
            --without-berkeley-db

        make
        make install

        touch "${marker}"
    fi
    rm --force --recursive "${directory}"

    target="/var/tmp/${version}"

    min_archive "${target}" "${version}"
} #}}}1
function subversion_1_4() { #{{{1
    echo "==============================================================================="
    echo "= 1.4                                                                         ="
    echo "==============================================================================="
    local version="1.4.0"
    local directory=`mktemp -d`
    mkdir --parents "/usr/local-${version}"
    rm --force --recursive /usr/local
    ln --symbolic /usr/local-${version} /usr/local
    mkdir --parents "/usr/local/.install"

    init

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 1.5.0
    apr-util "${directory}" 1.5.3
    httpd    "${directory}" 2.2.26
    sqllite  "${directory}" 3080100
    neon     "${directory}" 0.25.5

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
    else
        local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
        if [ ! -s "${archive}" ]; then
            wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
        fi

        cd "${directory}"
        tar -xjf "${archive}"
        cd "subversion-${version}"

        ./configure \
            --disable-keychain \
            --prefix="/usr/local" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-apxs="/usr/local/bin/apxs" \
            --with-neon="/usr/local" \
            --with-sqlite="/usr/local" \
            --without-berkeley-db

        make
        make install

        touch "${marker}"
    fi
    rm --force --recursive "${directory}"

    target="/var/tmp/${version}"

    min_archive "${target}" "${version}"
} #}}}1
function subversion_1_5() { #{{{1
    echo "==============================================================================="
    echo "= 1.5                                                                         ="
    echo "==============================================================================="
    local version="1.5.0"
    local directory=`mktemp -d`
    mkdir --parents "/usr/local-${version}"
    rm --force --recursive /usr/local
    ln --symbolic /usr/local-${version} /usr/local
    mkdir --parents "/usr/local/.install"

    init

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 1.5.0
    apr-util "${directory}" 1.5.3
    httpd    "${directory}" 2.2.26
    sqllite  "${directory}" 3080100
    neon     "${directory}" 0.25.5

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
    else
        local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
        if [ ! -s "${archive}" ]; then
            wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
        fi

        cd "${directory}"
        tar -xjf "${archive}"
        cd "subversion-${version}"

        ./configure \
            --disable-keychain \
            --prefix="/usr/local" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-apxs="/usr/local/bin/apxs" \
            --with-neon="/usr/local" \
            --with-sqlite="/usr/local" \
            --without-berkeley-db

        make
        make install

        touch "${marker}"
    fi
    rm --force --recursive "${directory}"

    target="/var/tmp/${version}"

    min_archive "${target}" "${version}"
} #}}}1
function subversion_1_6() { #{{{1
    echo "==============================================================================="
    echo "= 1.6                                                                         ="
    echo "==============================================================================="
    local version="1.6.0"
    local directory=`mktemp -d`
    mkdir --parents "/usr/local-${version}"
    rm --force --recursive /usr/local
    ln --symbolic /usr/local-${version} /usr/local
    mkdir --parents "/usr/local/.install"

    init

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 1.5.0
    apr-util "${directory}" 1.5.3
    httpd    "${directory}" 2.2.26
    sqllite  "${directory}" 3080100
    neon     "${directory}" 0.25.5

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
    else
        local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
        if [ ! -s "${archive}" ]; then
            wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
        fi
        cd "${directory}"
        tar -xjf "${archive}"
        cd "subversion-${version}"

        ./configure \
            --disable-keychain \
            --prefix="/usr/local" \
            --with-apache-libexecdir="/usr/local/modules" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-apxs="/usr/local/bin/apxs" \
            --with-neon="/usr/local" \
            --with-sqlite="/usr/local" \
            --without-berkeley-db

        make
        make install

        touch "${marker}"
    fi
    rm --force --recursive "${directory}"

    target="/var/tmp/${version}"

    min_archive "${target}" "${version}"
} #}}}1
function subversion_1_7() { #{{{1
    echo "==============================================================================="
    echo "= 1.7                                                                         ="
    echo "==============================================================================="
    local version="1.7.0"
    local directory=`mktemp -d`
    mkdir --parents "/usr/local-${version}"
    rm --force --recursive /usr/local
    ln --symbolic /usr/local-${version} /usr/local
    mkdir --parents "/usr/local/.install"

    init

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 1.5.0
    apr-util "${directory}" 1.5.3
    httpd    "${directory}" 2.2.26
    sqllite  "${directory}" 3080100
    scons    "${directory}" 2.3.0
    serf     "${directory}" 1.3.2

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
    else
        local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
        if [ ! -s "${archive}" ]; then
            wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
        fi

        cd "${directory}"
        tar -xjf "${archive}"
        cd "subversion-${version}"

        ./configure \
            --disable-keychain \
            --prefix="/usr/local" \
            --with-apache-libexecdir="/usr/local/modules" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-apxs="/usr/local/bin/apxs" \
            --with-serf="/usr/local" \
            --with-sqlite="/usr/local" \
            --without-berkeley-db

        make
        make install

        touch "${marker}"
    fi
    rm --force --recursive "${directory}"

    target="/var/tmp/${version}"

    min_archive "${target}" "${version}"
} #}}}1
function subversion_1_8() { #{{{1
    echo "==============================================================================="
    echo "= 1.8                                                                         ="
    echo "==============================================================================="
    local version="1.8.0"
    local directory=`mktemp -d`
    mkdir --parents "/usr/local-${version}"
    rm --force --recursive /usr/local
    ln --symbolic /usr/local-${version} /usr/local
    mkdir --parents "/usr/local/.install"

    init

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 1.5.0
    apr-util "${directory}" 1.5.3
    httpd    "${directory}" 2.2.26
    sqllite  "${directory}" 3080100
    scons    "${directory}" 2.3.0
    serf     "${directory}" 1.3.2

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
    else

        local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
        if [ ! -s "${archive}" ]; then
            wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
        fi

        cd "${directory}"
        tar -xjf "${archive}"
        cd "subversion-${version}"

        ./configure \
            --disable-keychain \
            --prefix="/usr/local" \
            --with-apache-libexecdir="/usr/local/modules" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-apxs="/usr/local/bin/apxs" \
            --with-serf="/usr/local" \
            --with-sqlite="/usr/local" \
            --without-berkeley-db \
            --without-gpg-agent
        make
        make install

        touch "${marker}"
    fi
    rm --force --recursive "${directory}"

    target="/var/tmp/${version}"

    min_archive "${target}" "${version}"
} #}}}1
function subversion_1_9() { #{{{1
    echo "==============================================================================="
    echo "= 1.9                                                                         ="
    echo "==============================================================================="
    local version="1.9.1"
    local directory=`mktemp -d`
    mkdir --parents "/usr/local-${version}"
    rm --force --recursive /usr/local
    ln --symbolic /usr/local-${version} /usr/local
    mkdir --parents "/usr/local/.install"

    init

    openssl  "${directory}" 0.9.8g
    apr      "${directory}" 1.5.0
    apr-util "${directory}" 1.5.3
    httpd    "${directory}" 2.2.26
    sqllite  "${directory}" 3080100
    scons    "${directory}" 2.3.0
    serf     "${directory}" 1.3.4

    local marker="/usr/local/.install/subversion"
    if [ -f "${marker}" ]; then
        echo "subversion already installed in /usr/local"
    else
        local archive="${DOWNLOAD}/subversion-${version}.tar.bz2"
        if [ ! -s "${archive}" ]; then
            wget "https://archive.apache.org/dist/subversion/subversion-${version}.tar.bz2" -O "${archive}"
        fi

        cd "${directory}"
        tar -xjf "${archive}"
        cd "subversion-${version}"

        ./configure \
            --disable-keychain \
            --prefix="/usr/local" \
            --with-apache-libexecdir="/usr/local/modules" \
            --with-apr-util="/usr/local" \
            --with-apr="/usr/local" \
            --with-apxs="/usr/local/bin/apxs" \
            --with-serf="/usr/local" \
            --with-sqlite="/usr/local" \
            --without-berkeley-db \
            --without-gpg-agent
        make
        make install

        touch "${marker}"
    fi
    rm --force --recursive "${directory}"

    target="/var/tmp/${version}"

    min_archive "${target}" "${version}"
} #}}}1

mkdir --parents "${DOWNLOAD}"

subversion_1_9
subversion_1_8
subversion_1_7
subversion_1_6
subversion_1_5
subversion_1_4
subversion_1_3
subversion_1_2
#subversion_1_1 # won't build apache modules
#subversion_1_0 # won't build apache modules
