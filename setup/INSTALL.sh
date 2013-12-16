#!/bin/bash

# install necesarry components
aptitude install apache2 rsync ssl-cert zip libapache2-mod-proxy-html libxml2-utils

# copy config
rsync -acHv --no-p --no-o --no-g conf/* /

# prepare repository
/sbin/create_svn_test

a2enmod  proxy_http ssl
a2ensite default default-ssl

service apache2 restart

#chkconfig apache-subversion_1.0.0   on
#chkconfig apache-subversion_1.1.0   on
chkconfig apache-subversion_1.2.0    on
chkconfig apache-subversion_1.3.0    on
chkconfig apache-subversion_1.4.0    on
chkconfig apache-subversion_1.5.0    on
chkconfig apache-subversion_1.6.0    on
chkconfig apache-subversion_1.7.0    on
chkconfig apache-subversion_1.8.0    on
chkconfig apache-subversion_frontend on
