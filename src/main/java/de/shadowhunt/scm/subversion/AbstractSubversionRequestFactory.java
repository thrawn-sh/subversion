package de.shadowhunt.scm.subversion;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public abstract class AbstractSubversionRequestFactory {

	public static final class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

		private static final int DEFAULT_DEPTH = 0;

		private int depth;

		private final String method;

		public DavTemplateRequest(final String method) {
			this(method, DEFAULT_DEPTH);
		}

		public DavTemplateRequest(final String method, final int depth) {
			super();
			this.method = method;
			setDepth(depth);
		}

		public int getDepth() {
			return depth;
		}

		@Override
		public String getMethod() {
			return method;
		}

		public void setDepth(final int depth) {
			this.depth = depth;
			removeHeaders("Depth");
			addHeader("Depth", Integer.toString(depth));
		}

		@Override
		public String toString() {
			return method + " " + getURI() + " " + getProtocolVersion();
		}
	}

	protected static final ContentType CONTENT_TYPE_XML = ContentType.create("text/xml", "UTF-8");

	protected static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	public HttpUriRequest createAuthRequest(final URI uri) {
		return new HttpOptions(uri);
	}

	public HttpUriRequest createExistsRequest(final URI uri) {
		return new HttpHead(uri);
	}

	public HttpUriRequest createLockRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("LOCK");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<lockinfo xmlns=\"DAV:\"><lockscope><exclusive/></lockscope><locktype><write/></locktype></lockinfo>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createLogRequest(final URI uri, final long start, final long end) {
		final DavTemplateRequest request = new DavTemplateRequest("REPORT");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<log-report xmlns=\"svn:\"><start-revision>");
		body.append(start);
		body.append("</start-revision><end-revision>");
		body.append(end);
		body.append("</end-revision><encode-binary-props/><revprop>svn:author</revprop><revprop>svn:date</revprop><revprop>svn:log</revprop><path/></log-report>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createUnlockRequest(final URI uri, final SubversionInfo info) {
		final DavTemplateRequest request = new DavTemplateRequest("UNLOCK");
		request.setURI(uri);
		request.addHeader("Lock-Token", "<" + info.getLockToken() + ">");
		return request;
	}
}
