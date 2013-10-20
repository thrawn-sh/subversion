package de.shadowhunt.subversion.internal.httpv2;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.internal.AbstractOperation;
import de.shadowhunt.subversion.internal.util.URIUtils;
import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class CreateTransactionOperation extends AbstractOperation<Transaction> {

	private static final HttpEntity entity;

	private static final String HEADER_NAME = "SVN-Txn-Name";

	static {
		final ContentType contentType = ContentType.create("application/vnd.svn-skel");
		entity = new StringEntity("( create-txn )", contentType);
	}

	public CreateTransactionOperation(final URI repository) {
		super(repository);
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, Resource.create("/!svn/me"));
		final HttpPost request = new HttpPost(uri);
		request.setEntity(entity);
		return request;
	}

	@Override
	protected Transaction processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_CREATED);
		final String id = response.getFirstHeader(HEADER_NAME).getValue();
		return new Transaction(id);
	}

}
