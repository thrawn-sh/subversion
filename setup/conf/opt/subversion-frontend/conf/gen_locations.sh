#!/bin/sh -e

while read version port; do
    cat <<EOF
    <Location /${version}>
        ProxyPass        http://localhost:${port}
        ProxyPassReverse http://localhost:${port}
        <if "(%{REQUEST_URI} != '/${version}/dump.zip') && (%{REQUEST_URI} != '/${version}/svn.zip')" >
            Header           edit Location    localhost:${port} subversion.vm.shadowhunt.de/${version}
            RequestHeader    edit Destination subversion.vm.shadowhunt.de/${version} localhost:${port}
            SetInputFilter   Sed
            InputSed         "s|href>/${version}/|href>/|g"
            InputSed         "s|href><!\[CDATA\[/${version}/|href><!\[CDATA\[/|g"
            InputSed         "s|src-path>/${version}/|src-path>/|g"
            InputSed         "s|subversion.vm.shadowhunt.de/${version}|localhost:${port}|g"
            SetOutputFilter  Sed
            OutputSed        "s|href>/|href>/${version}/|g"
        </if>
    </Location>
EOF
done <<EOF
1.0.0 10000
1.1.0 10100
1.2.0 10200
1.3.0 10300
1.4.0 10400
1.5.0 10500
1.6.0 10600
EOF

while read version port; do
    cat <<EOF
    <Location /${version}>
        ProxyPass        http://localhost:${port}
        ProxyPassReverse http://localhost:${port}
        <if "(%{REQUEST_URI} != '/${version}/dump.zip') && (%{REQUEST_URI} != '/${version}/svn.zip')" >
            Header           edit SVN-Me-Resource     /svn-basic /${version}/svn-basic
            Header           edit SVN-Repository-Root /svn-basic /${version}/svn-basic
            Header           edit SVN-Rev-Root-Stub   /svn-basic /${version}/svn-basic
            Header           edit SVN-Rev-Stub        /svn-basic /${version}/svn-basic
            Header           edit SVN-Txn-Root-Stub   /svn-basic /${version}/svn-basic
            Header           edit SVN-Txn-Stub        /svn-basic /${version}/svn-basic
            Header           edit SVN-VTxn-Root-Stub  /svn-basic /${version}/svn-basic
            Header           edit SVN-VTxn-Stub       /svn-basic /${version}/svn-basic
            Header           edit Location    localhost:${port} subversion.vm.shadowhunt.de/${version}
            RequestHeader    edit Destination subversion.vm.shadowhunt.de/${version} localhost:${port}
            SetInputFilter   Sed
            InputSed         "s|href>/${version}/|href>/|g"
            InputSed         "s|href><!\[CDATA\[/${version}/|href><!\[CDATA\[/|g"
            InputSed         "s|src-path>/${version}/|src-path>/|g"
            InputSed         "s|subversion.vm.shadowhunt.de/${version}|localhost:${port}|g"
            SetOutputFilter  Sed
            OutputSed        "s|href>/|href>/${version}/|g"
        </if>
    </Location>
EOF
done <<EOF
1.7.0 10700
1.8.0 10800
EOF
