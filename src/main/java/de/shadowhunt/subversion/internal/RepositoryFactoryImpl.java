package de.shadowhunt.subversion.internal;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.SubversionException;

public class RepositoryFactoryImpl extends RepositoryFactory {

	@Override
	public Repository createRepository(final URI repository, final HttpClient client, final HttpContext context) throws SubversionException {
		final URI sanitised = sanitise(repository);

		final ProbeServerOperation operation = new ProbeServerOperation(sanitised);
		return operation.execute(client, context);
	}
}
