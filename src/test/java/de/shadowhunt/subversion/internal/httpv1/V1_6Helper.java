package de.shadowhunt.subversion.internal.httpv1;

import java.net.URI;
import java.util.UUID;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;

final class V1_6Helper {

	private static final String PASSWORD = "svnpass";

	private static Repository repositoryA, repositoryB;

	private static final URI REPOSITORY_URI = URI.create("http://10.155.50.102/svn-basic/test");

	private static final UUID TEST_ID = UUID.randomUUID();

	private static final String USERNAME_A = "svnuser";

	private static final String USERNAME_B = "svnuser2";

	static Repository getRepositoryA() {
		if (repositoryA == null) {
			final DefaultHttpClient client = new DefaultHttpClient();
			final HttpContext context = new BasicHttpContext();

			final CredentialsProvider cp = new BasicCredentialsProvider();
			final Credentials credentials = new UsernamePasswordCredentials(USERNAME_A, PASSWORD);
			cp.setCredentials(AuthScope.ANY, credentials);
			client.setCredentialsProvider(cp);

			repositoryA = RepositoryFactory.createRepository(REPOSITORY_URI, client, context);

		}
		return repositoryA;
	}

	static Repository getRepositoryB() {
		if (repositoryB == null) {
			final DefaultHttpClient client = new DefaultHttpClient();
			final HttpContext context = new BasicHttpContext();

			final CredentialsProvider cp = new BasicCredentialsProvider();
			final Credentials credentials = new UsernamePasswordCredentials(USERNAME_B, PASSWORD);
			cp.setCredentials(AuthScope.ANY, credentials);
			client.setCredentialsProvider(cp);

			repositoryB = RepositoryFactory.createRepository(REPOSITORY_URI, client, context);

		}
		return repositoryB;
	}

	static UUID getTestId() {
		return TEST_ID;
	}
}
