package de.shadowhunt.subversion.internal;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.util.URIUtils;
import java.io.InputStream;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class ResolveOperation extends AbstractOperation<Resource> {

	protected final Resource resource;

	protected final Revision revision, expected;

	protected final RepositoryConfig config;

	public ResolveOperation(final URI repository, final Resource resource, final Revision revision, final Revision expected, RepositoryConfig config) {
		super(repository);
		this.resource = resource;
		this.revision = revision;
		this.expected = expected;
		this.config = config;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<get-locations xmlns=\"svn:\"><path/><peg-revision>");
		sb.append(revision);
		sb.append("</peg-revision><location-revision>");
		sb.append(expected);
		sb.append("</location-revision></get-locations>");

		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected Resource processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_OK);

		final InputStream in = getContent(response);
		try {
			final Resolve resolve = Resolve.read(in);
			return config.getVersionedResource(expected).append(resolve.getResource());
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
