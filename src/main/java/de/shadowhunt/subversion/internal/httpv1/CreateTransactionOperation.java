package de.shadowhunt.subversion.internal.httpv1;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.internal.AbstractOperation;
import de.shadowhunt.subversion.internal.util.URIUtils;
import java.net.URI;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

public class CreateTransactionOperation extends AbstractOperation<Transaction> {

	private final UUID uuid = UUID.randomUUID();

	public CreateTransactionOperation(final URI repository) {
		super(repository);
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, Resource.create("/!svn/act/" + uuid));
		return new DavTemplateRequest("MKACTIVITY", uri);
	}

	@Override
	protected Transaction processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_CREATED);
		return new Transaction(uuid.toString());
	}
}
