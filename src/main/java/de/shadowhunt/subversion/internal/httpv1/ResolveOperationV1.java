package de.shadowhunt.subversion.internal.httpv1;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.internal.Resolve;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.AbstractOperation;
import java.io.InputStream;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class ResolveOperationV1 extends AbstractOperation<Resource> {

	protected final Resource resource;

	protected final Revision revision, head;

	public ResolveOperationV1(final URI repository, final Resource resource, final Revision revision, final Revision head) {
		super(repository);
		this.resource = resource;
		this.revision = revision;
		this.head = head;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<get-locations xmlns=\"svn:\"><path/><peg-revision>");
		sb.append(head);
		sb.append("</peg-revision><location-revision>");
		sb.append(revision);
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
			return Resource.create("/!svn/bc/" + resolve.getRevision()).append(resolve.getResource()); // FIXME
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
