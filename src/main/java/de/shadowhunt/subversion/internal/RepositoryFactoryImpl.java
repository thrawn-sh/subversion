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
		final URI sanatized = sanatize(repository);

		final ProbeServerOperation operation = new ProbeServerOperation(sanatized);
		final RepositoryConfig config = operation.execute(client, context);
		return config.create(sanatized, client, context);
	}
}
