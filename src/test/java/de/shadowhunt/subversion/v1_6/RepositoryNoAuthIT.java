package de.shadowhunt.subversion.v1_6;

import java.net.URI;

import de.shadowhunt.subversion.AbstractRepositoryReadOnlyIT;
import de.shadowhunt.subversion.ServerVersion;

public class RepositoryNoAuthIT extends AbstractRepositoryReadOnlyIT {

	public RepositoryNoAuthIT() {
		super(URI.create("http://subversion-16.vm.shadowhunt.de/svn-non/test"), ServerVersion.V1_6);
	}
}
