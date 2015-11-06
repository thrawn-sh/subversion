#!/bin/bash

# install necessary components
aptitude install \
    cron \
    chkconfig \
    libxml2-utils \
    rsync \
    ssl-cert \
    zip

# compile environment
THIS=`readlink -f ${0}`
DIR=`dirname ${THIS}`
${DIR}/compile_subversion.sh

# copy config
rsync -acHv --no-p --no-o --no-g conf/* /

# enable autostart
#chkconfig apache-subversion_1.0.0   on
#chkconfig apache-subversion_1.1.0   on
chkconfig apache-subversion_1.2.0    on
chkconfig apache-subversion_1.3.0    on
chkconfig apache-subversion_1.4.0    on
chkconfig apache-subversion_1.5.0    on
chkconfig apache-subversion_1.6.0    on
chkconfig apache-subversion_1.7.0    on
chkconfig apache-subversion_1.8.0    on
chkconfig apache-subversion_1.9.1    on
chkconfig apache-subversion_frontend on

# start environment
/opt/bin/apache_all.sh start

# initial create of repositories
/etc/cron.daily/create_svn_test
