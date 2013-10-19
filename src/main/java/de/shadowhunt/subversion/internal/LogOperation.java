package de.shadowhunt.subversion.internal;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class LogOperation extends AbstractOperation<List<Log>> {

	protected final int limit;

	protected final Resource resource;

	protected final Revision start, end;

	public LogOperation(final URI repository, final Resource resource, final Revision start, final Revision end, final int limit) {
		super(repository);
		this.resource = resource;
		this.start = start;
		this.end = end;
		this.limit = limit;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<log-report xmlns=\"svn:\"><start-revision>");
		body.append(start);
		body.append("</start-revision><end-revision>");
		body.append(end);
		body.append("</end-revision>");
		if (limit > 0) {
			body.append("<limit>");
			body.append(limit);
			body.append("</limit>");
		}
		body.append("<discover-changed-paths/><encode-binary-props/><all-revprops/><path/></log-report>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected List<Log> processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_OK);

		final InputStream in = getContent(response);
		try {
			return ((List<Log>) (List) LogImpl.read(in));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
