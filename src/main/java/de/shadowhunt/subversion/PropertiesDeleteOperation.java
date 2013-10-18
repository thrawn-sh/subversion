package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.util.URIUtils;

public class PropertiesDeleteOperation extends AbstractOperation<Void> {

	protected static final int PREFIX = 4; // /$svn/{baseline}/{id}/

	protected final String lock;

	protected final ResourceProperty[] properties;

	protected final Resource resource;

	public PropertiesDeleteOperation(final URI repository, final Resource resource, final String lock, final ResourceProperty[] properties) {
		super(repository);
		this.resource = resource;
		this.lock = lock;
		this.properties = properties;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH", uri);

		if (lock != null) {
			final URI lockTarget = URIUtils.createURI(repository, resource.subResource(PREFIX));
			request.addHeader("If", "<" + lockTarget.toASCIIString() + "> (<" + lock + ">)");
		}

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<propertyupdate xmlns=\"DAV:\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\"><remove><prop>");
		for (final ResourceProperty property : properties) {
			sb.append('<');
			sb.append(property.getType().getPrefix());
			sb.append(property.getName());
			sb.append("/>");
		}
		sb.append("</prop></remove></propertyupdate>");
		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected Void processResponse(final HttpResponse response) {
		// TODO SC_MULTI_STATUS
		return null;
	}

}
