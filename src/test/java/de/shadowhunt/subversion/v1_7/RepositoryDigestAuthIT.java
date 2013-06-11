package de.shadowhunt.subversion.v1_7;

import java.net.URI;

import de.shadowhunt.subversion.AbstractRepositoryLockedWriteIT;
import de.shadowhunt.subversion.ServerVersion;

public class RepositoryDigestAuthIT extends AbstractRepositoryLockedWriteIT {

	public RepositoryDigestAuthIT() {
		super(URI.create("http://subversion-17.vm.shadowhunt.de/svn-digest/test"), ServerVersion.V1_7, "svnuser", "svnpass", null);
	}
}
