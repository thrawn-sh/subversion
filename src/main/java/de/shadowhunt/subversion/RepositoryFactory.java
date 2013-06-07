package de.shadowhunt.subversion;

import java.net.URI;

/**
 * {@link RepositoryFactory} creates {@link Repository}
 */
public interface RepositoryFactory {

	/**
	 * @param repository {@link URI} to the root of the repository
	 * @param trustServerCertificat whether to trust all SSL certificates (see {@code NonValidatingX509TrustManager})
	 * @return a new {@link Repository} for given {@link URI}
	 */
	public Repository createRepository(URI repository, boolean trustServerCertificat);

	/**
	 * Determine whether the {@link ServerVersion} is supported by the {@link Repository} created by this factory
	 * @param version the {@link ServerVersion} of the server
	 * @return {@code true} if the {@link ServerVersion} is supported, otherwise {@code false}
	 */
	public boolean isServerVersionSupported(ServerVersion version);
}
