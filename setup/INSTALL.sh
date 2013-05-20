#!/bin/bash

# install necesarry components
aptitude install apache2-mpm-worker libapache2-svn libapache2-mod-auth-ntlm-winbind subversion rsync

# copy config
rsync -acHv --no-p --no-o --no-g conf/* /

# prepare repository
/sbin/create_svn_test

# prepare ntlm
service winbind restart
adduser -p `openssl passwd -1 svnpass` svnuser
smbpasswd -a svnuser <<EOF
svnpass
svnpass
EOF
adduser www-data winbindd_priv

a2enmod auth_basic auth_digest auth_ntlm_winbind dav dav_fs dav_lock dav_svn ssl
a2ensite default default-ssl
service apache2 restart
