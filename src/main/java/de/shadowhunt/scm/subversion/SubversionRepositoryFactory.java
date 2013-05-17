package de.shadowhunt.scm.subversion;

import java.net.URI;

<<<<<<< HEAD
=======
import de.shadowhunt.http.conn.ssl.NonValidatingX509TrustManager;

/**
 * {@code SubversionRepositoryFactory} creates {@link SubversionRepository}
 */
>>>>>>> d300e93f712bdea984a6c85f658dd0cbb2b243b9
public interface SubversionRepositoryFactory {

	/**
	 * Determine whether the {@link ServerVersion} is supported by the {@link SubversionRepository} created by this factory
	 * @param version the {@ServerVersion} of the server
	 * @return {@code true} if the {@link ServerVersion} is supported, otherwise {@code false}
	 */
	public boolean isServerVersionSupported(ServerVersion version);

	/**
	 * @param repository {@link URI} to the root of the repository
	 * @param trustServerCertificat whether to trust all SSL certificates (see {@code NonValidatingX509TrustManager})
	 * @return a new {@link SubversionRepository} for given {@link URI}
	 */
	public SubversionRepository createRepository(URI repository, boolean trustServerCertificat);
}
