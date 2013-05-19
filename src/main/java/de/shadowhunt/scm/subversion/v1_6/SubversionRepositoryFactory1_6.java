package de.shadowhunt.scm.subversion.v1_6;

import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import de.shadowhunt.scm.subversion.ServerVersion;
import de.shadowhunt.scm.subversion.SubversionRepository;
import de.shadowhunt.scm.subversion.SubversionRepositoryFactory;

/**
 * {@code SubversionRepositoryFactory1_6} can create {@link SubversionRepository} that support subversion servers of version 1.6.X
 */
@ThreadSafe
public class SubversionRepositoryFactory1_6 implements SubversionRepositoryFactory {

	@Override
	public SubversionRepository createRepository(final URI repository, final boolean trustServerCertificat) {
		return new SubversionRepository1_6(repository, trustServerCertificat);
	}

	@Override
	public boolean isServerVersionSupported(final ServerVersion version) {
		return ServerVersion.V1_6 == version;
	}

}