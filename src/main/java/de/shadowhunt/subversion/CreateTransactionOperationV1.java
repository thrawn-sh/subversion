package de.shadowhunt.subversion;

import java.net.URI;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.util.URIUtils;

public class CreateTransactionOperationV1 extends AbstractOperation<Transaction> {

	private final UUID uuid = UUID.randomUUID();

	public CreateTransactionOperationV1(final URI repository) {
		super(repository);
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, Resource.create("/!svn/act/" + uuid));
		return new DavTemplateRequest("MKACTIVITY", uri);
	}

	@Override
	protected Transaction processResponse(final HttpResponse response) {
		// TODO HttpStatus.SC_CREATED
		return new Transaction(uuid.toString());
	}
}
