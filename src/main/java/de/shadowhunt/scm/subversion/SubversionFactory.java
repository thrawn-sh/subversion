package de.shadowhunt.scm.subversion;

import java.net.URI;
import java.util.ServiceLoader;

import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code SubversionFactory} creates a new {@link SubversionRepository}
 */
@ThreadSafe
public final class SubversionFactory {

	private static void assertSupportedScheme(final URI uri) {
		final String scheme = uri.getScheme();
		if (!"http".equals(scheme) && !"https".equals(scheme)) {
			throw new SubversionException("unsupported scheme " + scheme + " only http and https are supported");
		}
	}

	/**
	 * Create a new {@link SubversionRepository} for given {@link URI} and {@link ServerVersion}
	 * @param repository {@link URI} to the root of the repository (e.g: http://repository.example.net/svn/test_repo), only http and https scheme are supported
	 * @param trustServerCertificat whether to trust all SSL certificates (see {@code NonValidatingX509TrustManager})
	 * @param version the {@ServerVersion} of the server
	 * @return a new {@link SubversionRepository} for given {@link URI} and {@link ServerVersion}
	 */
	public static final SubversionRepository getInstance(final URI repository, final boolean trustServerCertificat, final ServerVersion version) {
		assertSupportedScheme(repository);

		for (final SubversionRepositoryFactory factory : ServiceLoader.load(SubversionRepositoryFactory.class)) {
			if (factory.isServerVersionSupported(version)) {
				return factory.createRepository(repository, trustServerCertificat);
			}
		}
		throw new SubversionException("no repository found for version " + version);
	}

	private SubversionFactory() {
		// prevent instantiation
	}
}
