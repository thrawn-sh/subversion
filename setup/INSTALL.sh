#!/bin/bash

# install necesarry components
aptitude install apache2 rsync ssl-cert libapache2-mod-proxy-html libxml2-utils

# copy config
rsync -acHv --no-p --no-o --no-g conf/* /

# prepare repository
/sbin/create_svn_test

a2enmod  proxy_http ssl
a2ensite default default-ssl

service apache2 restart
