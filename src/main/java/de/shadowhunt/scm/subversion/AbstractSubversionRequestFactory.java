package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;

public abstract class AbstractSubversionRequestFactory {

	public static final class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

		private final Depth depth;

		private final String method;

		public DavTemplateRequest(final String method) {
			this.method = method;
			depth = Depth.EMPTY;
		}

		public DavTemplateRequest(final String method, final Depth depth) {
			this.method = method;
			this.depth = depth;
			setHeader("Depth", depth.value);
		}

		public Depth getDepth() {
			return depth;
		}

		@Override
		public String getMethod() {
			return method;
		}

		@Override
		public String toString() {
			return method + " " + getURI() + " " + getProtocolVersion();
		}
	}

	protected static final ContentType CONTENT_TYPE_XML = ContentType.create("text/xml", "UTF-8");

	protected static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	public HttpUriRequest createActivityRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("MKACTIVITY");
		request.setURI(uri);
		return request;
	}

	public HttpUriRequest createDeleteRequest(final URI uri) {
		return new HttpDelete(uri);
	}

	public HttpUriRequest createAuthRequest(final URI uri) {
		final HttpOptions request = new HttpOptions(uri);
		request.addHeader("Keep-Alive", "");
		return request;
	}

	public HttpUriRequest createCheckoutRequest(final URI uri, final String path) {
		final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<checkout xmlns=\"DAV:\"><activity-set><href>");
		body.append(StringEscapeUtils.escapeXml(path));
		body.append("</href></activity-set><apply-to-version/></checkout>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createCommitMessageRequest(final URI uri, final String message) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propertyupdate xmlns=\"DAV:\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\"><set><prop><S:log>");
		body.append(StringEscapeUtils.escapeXml(message));
		body.append("</S:log></prop></set></propertyupdate>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createDownloadRequest(final URI uri) {
		return new HttpGet(uri);
	}

	public HttpUriRequest createExistsRequest(final URI uri) {
		return new HttpHead(uri);
	}

	public HttpUriRequest createInfoRequest(final URI uri, final Depth depth) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", depth);
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propfind xmlns=\"DAV:\"><allprop/></propfind>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createLockRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("LOCK");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<lockinfo xmlns=\"DAV:\"><lockscope><exclusive/></lockscope><locktype><write/></locktype></lockinfo>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createLogRequest(final URI uri, final int startVersion, final int endVersion) {
		final DavTemplateRequest request = new DavTemplateRequest("REPORT");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<log-report xmlns=\"svn:\"><start-revision>");
		body.append(startVersion);
		body.append("</start-revision><end-revision>");
		body.append(endVersion);
		body.append("</end-revision><encode-binary-props/><revprop>svn:author</revprop><revprop>svn:date</revprop><revprop>svn:log</revprop><path/></log-report>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createMakeFolderRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("MKCOL");
		request.setURI(uri);
		return request;
	}

	public HttpUriRequest createMergeRequest(final URI uri, final String path, final SubversionInfo info) {
		final DavTemplateRequest request = new DavTemplateRequest("MERGE");
		request.setURI(uri);
		request.setHeader("X-SVN-Options", "release-locks");

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>");
		body.append(StringEscapeUtils.escapeXml(path));
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop>");
		final String token = info.getLockToken();
		if (token != null) {
			body.append("<S:lock-token-list xmlns:S=\"svn:\"><S:lock><S:lock-path>");
			body.append(StringEscapeUtils.escapeXml(info.getRelativePath()));
			body.append("</S:lock-path>");
			body.append("<S:lock-token>");
			body.append(token);
			body.append("</S:lock-token></S:lock></S:lock-token-list>");
		}
		body.append("</merge>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createRemovePropertiesRequest(final URI uri, final URI resource, final SubversionInfo info, final SubversionProperty... properties) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH");
		request.setURI(uri);

		final String token = info.getLockToken();
		if (token != null) {
			request.addHeader("If", "<" + resource + "> (<" + token + ">)");
		}

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<propertyupdate xmlns=\"DAV:\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\"><remove>");
		for (final SubversionProperty property : properties) {
			sb.append("<prop>");
			sb.append('<');
			sb.append(property.getType().getPrefix());
			sb.append(property.getName());
			sb.append("/>");
			sb.append("</prop>");
		}
		sb.append("</remove></propertyupdate>");
		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createSetPropertiesRequest(final URI uri, final URI resource, final SubversionInfo info, final SubversionProperty... properties) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH");
		request.setURI(uri);

		final String token = info.getLockToken();
		if (token != null) {
			request.addHeader("If", "<" + resource + "> (<" + token + ">)");
		}

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
		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createUnlockRequest(final URI uri, final SubversionInfo info) {
		final DavTemplateRequest request = new DavTemplateRequest("UNLOCK");
		request.setURI(uri);
		request.addHeader("Lock-Token", "<" + info.getLockToken() + ">");
		return request;
	}

	public HttpUriRequest createUploadRequest(final URI uri, final URI resource, final SubversionInfo info, final InputStream content) {
		final HttpPut request = new HttpPut(uri);

		final String token = info.getLockToken();
		if (token != null) {
			request.addHeader("If", "<" + resource + "> (<" + token + ">)");
		}
		request.setEntity(new InputStreamEntity(content, -1));
		return request;
	}
}
