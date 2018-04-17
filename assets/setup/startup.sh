#!/bin/bash

set -e
set -o pipefail
set -u

mv /var/www/svn /dev/shm/
ln --symbolic /dev/shm/svn /var/www/svn
exec /usr/local/bin/httpd -f /etc/httpd.conf -DFOREGROUND
