package de.shadowhunt.subversion;

import java.net.URI;
import java.util.ServiceLoader;

import javax.annotation.concurrent.ThreadSafe;

/**
 * {@link SubversionFactory} creates a new {@link Repository}
 */
@ThreadSafe
public final class SubversionFactory {

	static void assertSupportedScheme(final URI uri) {
		final String scheme = uri.getScheme();
		if (!"http".equals(scheme) && !"https".equals(scheme)) {
			throw new SubversionException("unsupported scheme " + scheme + " only http and https are supported");
		}
	}

	/**
	 * Create a new {@link Repository} for given {@link URI} and {@link ServerVersion}
	 * @param repository {@link URI} to the root of the repository (e.g: http://repository.example.net/svn/test_repo), only http and https scheme are supported
	 * @param trustServerCertificat whether to trust all SSL certificates (see {@code NonValidatingX509TrustManager})
	 * @param version the {@link ServerVersion} of the server
	 * @return a new {@link Repository} for given {@link URI} and {@link ServerVersion}
	 */
	public static final Repository getInstance(final URI repository, final boolean trustServerCertificat, final ServerVersion version) {
		assertSupportedScheme(repository);

		final URI cleaned = removeEndingSlash(repository);
		for (final RepositoryFactory factory : ServiceLoader.load(RepositoryFactory.class)) {
			if (factory.isServerVersionSupported(version)) {
				return factory.createRepository(cleaned, trustServerCertificat);
			}
		}
		throw new SubversionException("no repository found for version " + version);
	}

	static URI removeEndingSlash(final URI uri) {
		final String string = uri.toString();
		final int lastChar = string.length() - 1;
		if (string.charAt(lastChar) == '/') {
			return URI.create(string.substring(0, lastChar));
		}
		return uri;
	}

	private SubversionFactory() {
		// prevent instantiation
	}
}
