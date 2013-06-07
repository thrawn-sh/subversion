package de.shadowhunt.scm.subversion.v1_7;

import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import de.shadowhunt.scm.subversion.ServerVersion;
import de.shadowhunt.scm.subversion.Repository;
import de.shadowhunt.scm.subversion.RepositoryFactory;

/**
 * {@link RepositoryFactory1_7} can create {@link Repository} that support subversion servers of version 1.7.X
 */
@ThreadSafe
public class RepositoryFactory1_7 implements RepositoryFactory {

	@Override
	public Repository createRepository(final URI repository, final boolean trustServerCertificat) {
		return new Repository1_7(repository, trustServerCertificat);
	}

	@Override
	public boolean isServerVersionSupported(final ServerVersion version) {
		return ServerVersion.V1_7 == version;
	}

}
