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

mv /var/www/svn /dev/shm/
ln --symbolic /dev/shm/svn /var/www/svn
exec /usr/local/bin/httpd -f /etc/httpd.conf -DFOREGROUND
