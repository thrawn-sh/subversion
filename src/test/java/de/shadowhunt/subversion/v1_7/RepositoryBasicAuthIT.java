package de.shadowhunt.subversion.v1_7;

import java.net.URI;

import de.shadowhunt.subversion.AbstractRepositoryLockedWriteIT;
import de.shadowhunt.subversion.ServerVersion;

public class RepositoryBasicAuthIT extends AbstractRepositoryLockedWriteIT {

	public RepositoryBasicAuthIT() {
		super(URI.create("http://subversion-17.vm.shadowhunt.de/svn-basic/test"), ServerVersion.V1_7, "svnuser", "svnpass", null);
	}
}
