package de.shadowhunt.subversion.internal;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Version;
import java.io.InputStream;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class ProbeServerOperation extends AbstractOperation<RepositoryConfig> {

	public ProbeServerOperation(final URI repository) {
		super(repository);
	}

	@Override
	protected HttpUriRequest createRequest() {
		final DavTemplateRequest request = new DavTemplateRequest("OPTIONS", repository);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<options xmlns=\"DAV:\"><activity-collection-set/></options>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));

		return request;
	}

	protected static Version determineVersion(final HttpResponse response) {
		for (final Header header : response.getAllHeaders()) {
			if (header.getName().startsWith("SVN")) {
				return Version.HTTPv2;
			}
		}
		return Version.HTTPv1;
	}

	@Override
	protected RepositoryConfig processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_OK);

		final Version version = determineVersion(response);
		final Resource prefix;
		final InputStream in = getContent(response);
		try {
			prefix = Prefix.read(in, version);
		} finally {
			IOUtils.closeQuietly(in);
		}

		if (Version.HTTPv2 == version) {
			return new de.shadowhunt.subversion.internal.httpv2.RepositoryConfig(prefix);
		}
		// oldest protocol is fallback, in case we determine the wrong
		// protocol, server could still provide backwards compatibility
		return new de.shadowhunt.subversion.internal.httpv1.RepositoryConfig(prefix);
	}
}
