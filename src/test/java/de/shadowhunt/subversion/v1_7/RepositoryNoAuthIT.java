package de.shadowhunt.subversion.v1_7;

import java.net.URI;

import de.shadowhunt.subversion.AbstractRepositoryReadOnlyIT;
import de.shadowhunt.subversion.ServerVersion;

public class RepositoryNoAuthIT extends AbstractRepositoryReadOnlyIT {

	public RepositoryNoAuthIT() {
		super(URI.create("http://subversion-17.vm.shadowhunt.de/svn-non/test"), ServerVersion.V1_7);
	}
}
