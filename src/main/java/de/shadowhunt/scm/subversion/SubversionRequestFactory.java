package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;

final class SubversionRequestFactory {

	static class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

		private static final int DEFAULT_DEPTH = 0;

		private int depth;

		private final String method;

		DavTemplateRequest(final String method) {
			this(method, DEFAULT_DEPTH);
		}

		DavTemplateRequest(final String method, final int depth) {
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
	}

	private static final ContentType XML_CONTENT_TYPE = ContentType.create("text/xml", "UTF-8");

	private static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	public static HttpUriRequest createActivityRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("MKACTIVITY");
		request.setURI(uri);
		return request;
	}

	public static HttpUriRequest createAuthRequest(final URI uri) {
		return new HttpOptions(uri);
	}

	public static HttpUriRequest createCheckoutRequest(final URI uri, final UUID uuid) {
		final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<checkout xmlns=\"DAV:\"><activity-set><href>/svn/svntest1/!svn/act/");
		body.append(uuid);
		body.append("</href></activity-set><apply-to-version/></checkout>");

		request.setEntity(new StringEntity(body.toString(), XML_CONTENT_TYPE));
		return request;
	}

	public static HttpUriRequest createCommitMessageRequest(final URI uri, final String message) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propertyupdate xmlns=\"DAV:\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\"><set><prop><S:log>");
		body.append(StringEscapeUtils.escapeXml(message));
		body.append("</S:log></prop></set></propertyupdate>");

		request.setEntity(new StringEntity(body.toString(), XML_CONTENT_TYPE));
		return request;
	}

	public static HttpUriRequest createDownloadRequest(final URI uri) {
		return new HttpGet(uri);
	}

	public static HttpUriRequest createInfoRequest(final URI uri, final int depth) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", depth);
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propfind xmlns=\"DAV:\"><allprop/></propfind>");

		request.setEntity(new StringEntity(body.toString(), XML_CONTENT_TYPE));
		return request;
	}

	public static HttpUriRequest createLockRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("LOCK");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<lockinfo xmlns=\"DAV:\"><lockscope><exclusive/></lockscope><locktype><write/></locktype></lockinfo>");

		request.setEntity(new StringEntity(body.toString(), XML_CONTENT_TYPE));
		return request;
	}

	public static HttpUriRequest createLogRequest(final URI uri, final String start, final String end) {
		final DavTemplateRequest request = new DavTemplateRequest("REPORT");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<log-report xmlns=\"svn:\"><start-revision>");
		body.append(StringEscapeUtils.escapeXml(start));
		body.append("</start-revision><end-revision>");
		body.append(StringEscapeUtils.escapeXml(end));
		body.append("</end-revision><encode-binary-props/><revprop>svn:author</revprop><revprop>svn:date</revprop><revprop>svn:log</revprop><path/></log-report>");

		request.setEntity(new StringEntity(body.toString(), XML_CONTENT_TYPE));
		return request;
	}

	public static HttpUriRequest createMakeFolderRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("MKCOL");
		request.setURI(uri);
		return request;
	}

	public static HttpUriRequest createMergeRequest(final URI uri, final UUID uuid) {
		final DavTemplateRequest request = new DavTemplateRequest("MERGE");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>/svn/svntest1/!svn/act/");
		body.append(uuid);
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop></merge>");
		request.setEntity(new StringEntity(body.toString(), XML_CONTENT_TYPE));
		return request;
	}

	public static HttpUriRequest createSetPropertiesRequest(final URI uri, final Collection<SubversionProperty> properties) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH");
		request.setURI(uri);
		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<propertyupdate xmlns=\"DAV:\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\"><set>");
		for (final SubversionProperty property : properties) {
			final String prefix = property.getType().getPrefix();
			final String name = property.getName();
			sb.append("<prop>");
			sb.append('<');
			sb.append(prefix);
			sb.append(name);
			sb.append('>');
			sb.append(StringEscapeUtils.escapeXml(property.getValue()));
			sb.append("</");
			sb.append(prefix);
			sb.append(name);
			sb.append('>');
			sb.append("</prop>");
		}
		sb.append("</set></propertyupdate>");
		request.setEntity(new StringEntity(sb.toString(), XML_CONTENT_TYPE));
		return request;
	}

	public static HttpUriRequest createUnlockRequest(final URI uri, final String token) {
		final DavTemplateRequest request = new DavTemplateRequest("UNLOCK");
		request.setURI(uri);
		request.addHeader("Lock-Token", token);
		return request;
	}

	public static HttpUriRequest createUploadRequest(final URI uri, final InputStream content) {
		final HttpPut request = new HttpPut(uri);
		request.setEntity(new InputStreamEntity(content, -1));
		return request;
	}

	private SubversionRequestFactory() {
		// prevent instantiation
	}
}
