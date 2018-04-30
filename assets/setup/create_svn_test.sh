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

export LC_ALL="en_US.UTF8"

THIS_PATH="$(readlink --canonicalize-existing "${0}")"
THIS_NAME="$(basename "${THIS_PATH}")"
THIS_DIR="$(dirname "${THIS_PATH}")"

VERSION="${1}"; shift
if [ -z "${VERSION}" ]; then
    echo "VERSION not set" 1>&2
    exit 1;
fi
TARGET="${1}"; shift
if [ -z "${TARGET}" ]; then
    echo "TARGET not set" 1>&2
    exit 1;
fi
SVN="/usr/local/bin/svn"
if [ ! -f "${SVN}" ]; then
    echo "${SVN} not found" 1>&2
    exit 1;
fi
SVNADMIN="/usr/local/bin/svnadmin"
if [ ! -f "${SVNADMIN}" ]; then
    echo "${SVNADMIN} not found" 1>&2
    exit 1;
fi

TMP_DIR="`mktemp -d`"
LOCAL="${TMP_DIR}/local"
DUMP="${TMP_DIR}/dump"
BASE="${LOCAL}/trunk/00000000-0000-0000-0000-000000000000"

REPO="${TMP_DIR}/svn"

create_file() { #{{{1
    local file="${1}"; shift
    local delete="${1}"; shift

    mkdir -p `dirname "${file}"`
    touch "${file}"
    add   "${file}"
    local content
    for content in A B C; do
        echo "${content}" >                                        "${file}"
        "${SVN}" propset --quiet svn:mime-type        text/plain   "${file}"
        "${SVN}" propset --quiet modifiedProperty     "${content}" "${file}"
        "${SVN}" propset --quiet "property${content}" "${content}" "${file}"
        commit "${file} with content: ${content}"
    done

    if [ "${delete}" -eq 1 ]; then
        # copy
        local copy="`echo ${file} | sed 's:delete:copy:g'`"
        "${SVN}" copy "${file}"  "${copy}"
        add                      "${copy}"
        commit "copied ${file} -> ${copy}"
        # move
        local tmp="`echo ${file} | sed 's:delete:tmp:g'`"
        local move="`echo ${file} | sed 's:delete:move:g'`"
        "${SVN}" copy "${file}"  "${tmp}"
        add                      "${tmp}"
        commit "copied ${file} -> ${tmp}"
        "${SVN}" mv "${tmp}"   "${move}"
        add                    "${move}"
        commit "moved ${tmp} -> ${move}"
        # delete
        rm -f "${file}"
        "${SVN}" remove --quiet  "${file}"
        commit "deleted ${file}" "${file}"
    fi
} #}}}1
create_folder() { #{{{1
    local folder="${1}"; shift
    local delete="${1}"; shift

    mkdir -p "${folder}"
    add                                              "${folder}"
    "${SVN}" propset --quiet svn:ignore      "*.bin" "${folder}"
    "${SVN}" propset --quiet chustomProperty "test"  "${folder}"
    commit "created ${folder}"                       "${folder}"

    if [ "${delete}" -eq 1 ]; then
        # copy
        local copy="`echo ${folder} | sed 's:delete:copy:g'`"
        "${SVN}" copy "${folder}"  "${copy}"
        add                        "${copy}"
        commit "copied ${folder} -> ${copy}"
        # move
        local tmp="`echo ${folder} | sed 's:delete:tmp:g'`"
        local move="`echo ${folder} | sed 's:delete:move:g'`"
        "${SVN}" copy "${folder}"  "${tmp}"
        add                        "${tmp}"
        commit "copied ${folder} -> ${tmp}"
        "${SVN}" mv "${tmp}"   "${move}"
        add                    "${move}"
        commit "moved ${tmp} -> ${move}"
        # delete
        rm -rf "${folder}"
        "${SVN}" remove --quiet    "${folder}"
        commit "deleted ${folder}" "${folder}"
    fi
} #}}}1

add() { #{{{1
    local resource="${1}"; shift
    case "${VERSION}" in
        "1.0.0" | "1.1.0" | "1.2.0" | "1.3.0" | "1.4.0")
            if [ "${LOCAL}" == "${resource}" ]; then
                return
            fi
            local parent="`dirname "${resource}"`"
            add "${parent}"
            "${SVN}" add --non-recursive --quiet --force "${resource}"
            ;;
        *)
            "${SVN}" add --non-recursive --quiet --force --parents "${resource}"
            ;;
    esac
} #}}}1
commit() { #{{{1
    local message="${1}"; shift
    message=`echo "${message}" | sed "s:${LOCAL}::g"`

    "${SVN}" commit --message "${message}" "${LOCAL}"

    local version=`"${SVN}" info "file://${REPO}" | grep ^Revision: | awk '{print $2}'`
    local src
    while IFS= read -r -d $'\0' src; do
        local target="`echo "${src}" | sed "s:^${LOCAL}:${DUMP}/${version}:g"`"
        if [ -d "${src}" ]; then
            mkdir -p "${target}"
        else
            cp "${src}" "${target}"
        fi
        if [ "${target}" = "${DUMP}/${version}" ]; then
            target="${target}/ROOT"
        fi

        local rsrc="`echo ${src} | sed 's:%:%25:g' | sed 's: :%20:g' | sed 's:\^:%5e:g' | sed "s:^${LOCAL}:file\://${REPO}:g"`"
        "${SVN}" log --xml "${rsrc}" | xmllint --format - | sed "s:${REPO}::g" | sed -E "s:[0-9]{3}Z</date>:Z</date>:g" > "${target}.log"
        info               "${rsrc}"                                                                                      "${target}.info"
        proplist           "${rsrc}"                                                                                      "${target}.proplist"
    done < <(find "${LOCAL}" -not -wholename "*/.svn*" -print0)
} #}}}1
info() { #{{{1
    local resource="${1}"; shift
    local output="${1}"; shift

    local bin="${SVN}"
    case "${VERSION}" in
        "1.0.0" | "1.1.0" | "1.2.0")
            export LD_LIBRARY_PATH="/var/tmp/1.3.0/usr/local/lib"  
            bin="/var/tmp/1.3.0/usr/local/bin/svn"
            ;;
    esac
    "${bin}" info --xml "${resource}" | xmllint --format - | sed "s:${REPO}::g" | sed -E "s:[0-9]{3}Z</date>:Z</date>:g" > "${output}"
    unset LD_LIBRARY_PATH
} #}}}1
proplist() { #{{{1
    local resource="${1}"; shift
    local output="${1}"; shift

    local bin="${SVN}"
    case "${VERSION}" in
        "1.0.0" | "1.1.0" | "1.2.0" | "1.3.0" | "1.4.0")
            export LD_LIBRARY_PATH="/var/tmp/1.5.0/usr/local/lib"
            bin="/var/tmp/1.5.0/usr/local/bin/svn"
            ;;
    esac
    "${bin}" proplist --verbose --xml "${resource}" | xmllint --format - | sed "s:${REPO}::g" > "${output}"
    unset LD_LIBRARY_PATH
} #}}}1

### init {{{1
mkdir -p "${REPO}" "${DUMP}"

"${SVNADMIN}" create "${REPO}"
"${SVN}" checkout --quiet "file://${REPO}" "${LOCAL}"

### prepare repo {{{1
mkdir -p "${BASE}"
mkdir -p "${LOCAL}/branches"
mkdir -p "${LOCAL}/tags"
add "${BASE}"
add "${LOCAL}/branches"
add "${LOCAL}/tags"
commit "create structure"

### copy {{{1
create_file   "${BASE}/copy/file.txt" 0
create_file   "${BASE}/copy/file_delete.txt" 1
create_folder "${BASE}/copy/folder" 0
create_folder "${BASE}/copy/folder_delete" 1

### download {{{1
create_file "${BASE}/download/file.txt" 0
create_file "${BASE}/download/file_delete.txt" 1

### exists {{{1
create_file   "${BASE}/exists/file.txt" 0
create_file   "${BASE}/exists/file_delete.txt" 1
create_folder "${BASE}/exists/folder" 0
create_folder "${BASE}/exists/folder_delete" 1

### info {{{1
# TODO properties
create_file   "${BASE}/info/file.txt" 0
create_file   "${BASE}/info/file_delete.txt" 1
create_folder "${BASE}/info/folder" 0
create_folder "${BASE}/info/folder_delete" 1

### list {{{1
create_file   "${BASE}/list/file.txt" 0
create_file   "${BASE}/list/file_delete.txt" 1
create_folder "${BASE}/list/folder" 0
create_folder "${BASE}/list/folder_delete" 1

### lock {{{1
mkdir -p      "${BASE}/lock"
echo A >      "${BASE}/lock/file.txt"
add           "${BASE}/lock/file.txt"
commit        "${BASE}/lock/file.txt with content: A"
echo B >      "${BASE}/lock/file.txt"
"${SVN}" lock "${BASE}/lock/file.txt"
commit        "${BASE}/lock/file.txt with content: B + lock"

### log {{{1
create_file   "${BASE}/log/file.txt" 0
create_file   "${BASE}/log/file_delete.txt" 1

### encoding {{{1
### utf8
create_file   "${BASE}/encoding/file_日本国.txt" 0
create_folder "${BASE}/encoding/folder_中华人民共和国" 0

### xml
create_file   "${BASE}/encoding/file_<&>'\".txt" 0
create_folder "${BASE}/encoding/folder_<&>'\"" 0

### uri
create_file   "${BASE}/encoding/file ^%.txt" 0
create_folder "${BASE}/encoding/folder ^%" 0

### combinded
create_file   "${BASE}/encoding/file_日本国_<&>'\"_ ^%.txt" 0
create_folder "${BASE}/encoding/folder_中华人民共和国_<&>'\"_ ^%" 0

### custom properties with namespace name {{{1
create_folder                                     "${BASE}/namespace_properties" 0
touch                                             "${BASE}/namespace_properties/file.txt"
add                                               "${BASE}/namespace_properties/file.txt"
"${SVN}" propset --quiet "namespace:name" "value" "${BASE}/namespace_properties/file.txt"
commit                                            "${BASE}/namespace_properties/file.txt with namespace:name property" "${BASE}/namespace_properties/file.txt"

### package {{{1
cd "${TMP_DIR}"
for i in dump svn; do
    zip_file="${TARGET}/htdocs/${i}.zip"
    zip --quiet --recurse-paths "${zip_file}" "${i}"
    md5sum    "${zip_file}" | awk '{print $1}' > "${zip_file}.md5"
    sha1sum   "${zip_file}" | awk '{print $1}' > "${zip_file}.sha1"
    sha256sum "${zip_file}" | awk '{print $1}' > "${zip_file}.sha256"
    sha512sum "${zip_file}" | awk '{print $1}' > "${zip_file}.sha512"
done
mv svn "${TARGET}/svn-template/test"
chown --recursive www-data:www-data "${TARGET}/svn-template/test"

"${SVNADMIN}" create "${TARGET}/svn-template/empty"
chown --recursive www-data:www-data "${TARGET}/svn-template/empty"
