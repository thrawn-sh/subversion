package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public abstract class AbstractVoidOperation extends AbstractOperation<Void> {

	protected AbstractVoidOperation(final URI repository) {
		super(repository);
	}

	@Override
	protected final Void processResponse(final HttpResponse response) {
		checkResponse(response);
		EntityUtils.consumeQuietly(response.getEntity());
		return null;
	}

	protected abstract void checkResponse(final HttpResponse response);

}
