package de.shadowhunt.subversion.v1_6;

import java.net.URI;

import de.shadowhunt.subversion.AbstractRepositoryBasicWriteIT;
import de.shadowhunt.subversion.ServerVersion;

public class RepositoryDigestAuthIT extends AbstractRepositoryBasicWriteIT {

	public RepositoryDigestAuthIT() {
		super(URI.create("http://subversion-16.vm.shadowhunt.de/svn-digest/test"), ServerVersion.V1_6, "svnuser", "svnpass", null);
	}
}
