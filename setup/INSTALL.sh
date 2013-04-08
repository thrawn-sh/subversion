#!/bin/bash

# install necesarry components
aptitude install apache2-mpm-worker libapache2-svn libapache2-mod-auth-ntlm-winbind subversion rsync

# prepare repository
mkdir -p /var/www/svn
svnadmin create /var/www/svn/test
svn co file:///var/www/svn/test /tmp/test
mkdir   /tmp/test/tags /tmp/test/branches /tmp/test/trunk
svn add /tmp/test/tags
svn add /tmp/test/branches
svn add /tmp/test/trunk
svn ci  /tmp/test -m "create SVN structure"
rm -rf  /tmp/test
chown -R www-data:www-data /var/www/svn

# rsync config
rsync -acHv --no-p --no-o --no-g conf/* /

# prepare ntlm
service winbind restart
echo "Pass: svntest"
adduser svnuser
echo "Pass: svntest"
smbpasswd -a svnuser

a2enmod auth_basic auth_digest auth_ntlm_winbind dav dav_fs dav_lock dav_svn ssl
a2ensite default default-ssl
service apache2 restart
