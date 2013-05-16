package de.shadowhunt.scm.subversion.v1_6;

import java.net.URI;

import javax.annotation.concurrent.ThreadSafe;

import de.shadowhunt.scm.subversion.ServerVersion;
import de.shadowhunt.scm.subversion.SubversionRepository;
import de.shadowhunt.scm.subversion.SubversionRepositoryFactory;

@ThreadSafe
public class SubversionRepositoryFactory1_6 implements SubversionRepositoryFactory {

	@Override
	public boolean isServerVersionSupported(final ServerVersion version) {
		return ServerVersion.V1_6 == version;
	}

	@Override
	public SubversionRepository createRepository(final URI repository, final boolean trustServerCertificat) {
		return new SubversionRepository1_6(repository, trustServerCertificat);
	}

}
