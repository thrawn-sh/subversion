package de.shadowhunt.subversion.internal;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class ListOperation extends AbstractOperation<List<Info>> {

	protected final boolean customProperties;

	protected final Depth depth;

	protected final Resource resource;

	public ListOperation(final URI repository, final Resource resource, final Depth depth, final boolean customProperties) {
		super(repository);
		this.resource = resource;
		this.depth = depth;
		this.customProperties = customProperties;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", uri);
		request.addHeader("Depth", depth.value);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propfind xmlns=\"DAV:\"><allprop/></propfind>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected List<Info> processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return ((List<Info>) (List) InfoImpl.readList(in, customProperties, (Depth.FILES != depth)));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
