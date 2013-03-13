package de.shadowhunt.scm.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.scm.subversion.AbstractSubversionRequestFactory;
import de.shadowhunt.scm.subversion.SubversionProperty;

class SubversionRequestFactory extends AbstractSubversionRequestFactory {

	private static final ContentType CONTENT_TYPE_SVNSKEL = ContentType.create("application/vnd.svn-skel");

	@Override
	public HttpUriRequest createActivityRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("MKACTIVITY");
		request.setURI(uri);
		return request;
	}

	@Override
	public HttpUriRequest createAuthRequest(final URI uri) {
		return new HttpOptions(uri);
	}

	@Override
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

	@Override
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

	@Override
	public HttpUriRequest createDownloadRequest(final URI uri) {
		return new HttpGet(uri);
	}

	@Override
	public HttpUriRequest createExistsRequest(final URI uri) {
		return new HttpHead(uri);
	}

	@Override
	public HttpUriRequest createInfoRequest(final URI uri, final int depth) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", depth);
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propfind xmlns=\"DAV:\"><allprop/></propfind>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	public HttpUriRequest createLockRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("LOCK");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<lockinfo xmlns=\"DAV:\"><lockscope><exclusive/></lockscope><locktype><write/></locktype></lockinfo>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
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

	@Override
	public HttpUriRequest createMakeFolderRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("MKCOL");
		request.setURI(uri);
		return request;
	}

	@Override
	public HttpUriRequest createMergeRequest(final URI uri, final String path) {
		final DavTemplateRequest request = new DavTemplateRequest("MERGE");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>");
		body.append(StringEscapeUtils.escapeXml(path));
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop></merge>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	public HttpUriRequest createPostRequest(final URI uri, final String content) {
		final HttpPost request = new HttpPost(uri);
		request.setEntity(new StringEntity(content, CONTENT_TYPE_SVNSKEL));
		return request;
	}

	@Override
	public HttpUriRequest createRemovePropertiesRequest(final URI uri, final SubversionProperty... properties) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH");
		request.setURI(uri);
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

	@Override
	public HttpUriRequest createSetPropertiesRequest(final URI uri, final SubversionProperty... properties) {
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
		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	public HttpUriRequest createUnlockRequest(final URI uri, final String token) {
		final DavTemplateRequest request = new DavTemplateRequest("UNLOCK");
		request.setURI(uri);
		request.addHeader("Lock-Token", token);
		return request;
	}

	@Override
	public HttpUriRequest createUploadRequest(final URI uri, final InputStream content) {
		final HttpPut request = new HttpPut(uri);
		request.setEntity(new InputStreamEntity(content, -1));
		return request;
	}
}
