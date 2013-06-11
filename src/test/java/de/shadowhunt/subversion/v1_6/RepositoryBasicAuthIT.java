package de.shadowhunt.subversion.v1_6;

import java.net.URI;

import de.shadowhunt.subversion.AbstractRepositoryLockedWriteIT;
import de.shadowhunt.subversion.ServerVersion;

public class RepositoryBasicAuthIT extends AbstractRepositoryLockedWriteIT {

	public RepositoryBasicAuthIT() {
		super(URI.create("http://subversion-16.vm.shadowhunt.de/svn-basic/test"), ServerVersion.V1_6, "svnuser", "svnpass", null);
	}
}
