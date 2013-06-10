package de.shadowhunt.subversion;

import java.io.InputStream;
import java.net.URI;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpRequest;
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
import org.apache.http.message.BasicHeader;

/**
 * Basic class for all SubversionRequestFactories
 */
public abstract class AbstractRequestFactory {

	/**
	 * Basic implementation of an entity enclosing HTTP request for WebDav
	 */
	public static final class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

		private final Depth depth;

		private final String method;

		/**
		 * Create a new {@link DavTemplateRequest} with default {@link Depth}-level
		 * @param method http method name
		 */
		public DavTemplateRequest(final String method) {
			this(method, null);
		}

		/**
		 * Create a new {@link DavTemplateRequest}
		 * @param method http method name
		 * @param depth {@link Depth}-level of the request
		 */
		public DavTemplateRequest(final String method, @Nullable final Depth depth) {
			this.method = method;
			this.depth = depth;
			if (depth != null) {
				setHeader("Depth", depth.value);
			}
		}

		/**
		 * Returns the {@link Depth}-level of the request
		 * @return {@link Depth}-level of the request
		 */
		@CheckForNull
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

	protected static final long STREAM_WHOLE_CONTENT = -1L;

	protected static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	protected static void addApproveTokenHeader(final HttpRequest request, @Nullable final String lockToken, @Nullable final URI lockTokenTarget) {
		if (lockToken != null) {
			if (lockTokenTarget == null) {
				throw new IllegalArgumentException("lockToken is present, therefor lockTokenTarget must not be null");
			}
			request.addHeader("If", "<" + lockTokenTarget + "> (<" + lockToken + ">)");
		}
	}

	/**
	 * Create a new temporary directory for a transaction
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} creating the new temporary directory for the transaction
	 */
	public HttpUriRequest createActivityRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("MKACTIVITY");
		request.setURI(uri);
		return request;
	}

	/**
	 * Perform authentication, with a cheap http request without any payload, the http connection will be authenticated afterwards
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} performing the authentication
	 */
	public HttpUriRequest createAuthRequest(final URI uri) {
		final HttpOptions request = new HttpOptions(uri);
		request.addHeader("Keep-Alive", "");
		return request;
	}

	/**
	 * Setting a commit message for the current transaction on a resource
	 * @param uri {@link URI} to perform the request against
	 * @param message commit message for the current transaction
	 * @return {@link HttpUriRequest} setting the commit message for the current transaction on the resource
	 */
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

	/**
	 * Copying a resource to a new destination
	 * @param src {@link URI} to copy from
	 * @param target {@link URI} to copy to
	 * @return {@link HttpUriRequest} copying the resource to a new destination
	 */
	public HttpUriRequest createCopyRequest(final URI src, final URI target) {
		final DavTemplateRequest request = new DavTemplateRequest("COPY", Depth.INFINITY);
		request.addHeader(new BasicHeader("Destination", target.toASCIIString()));
		request.addHeader(new BasicHeader("Override", "T"));
		request.setURI(src);
		return request;
	}

	/**
	 * Deleting a resource
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} deleting the resource
	 */
	public HttpUriRequest createDeleteRequest(final URI uri) {
		return new HttpDelete(uri);
	}

	/**
	 * Retrieving the content of a resource
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} retrieving the content of a resource
	 */
	public HttpUriRequest createDownloadRequest(final URI uri) {
		return new HttpGet(uri);
	}

	/**
	 * Check whether a resource exists
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} checking the existence of the resource
	 */
	public HttpUriRequest createExistsRequest(final URI uri) {
		return new HttpHead(uri);
	}

	/**
	 * Request info on a resource an its child resources (depending on depth parameter)
	 * @param uri {@link URI} to perform the request against
	 * @param depth whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
	 * @return {@link HttpUriRequest} requesting info on the resource
	 */
	public HttpUriRequest createInfoRequest(final URI uri, final Depth depth) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", depth);
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propfind xmlns=\"DAV:\"><allprop/></propfind>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	/**
	 * Locking a resource
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} locking the resource
	 */
	public HttpUriRequest createLockRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("LOCK");
		request.setURI(uri);
		request.addHeader("X-SVN-Options", "lock-steal");

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<lockinfo xmlns=\"DAV:\"><lockscope><exclusive/></lockscope><locktype><write/></locktype></lockinfo>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	/**
	 * Request a report containing all properties between startRevision and endRevision
	 * @param uri {@link URI} to perform the request against
	 * @param startRevision the first {@link Revision} of the resource to retrieve (including)
	 * @param endRevision the last {@link Revision} of the resource to retrieve (including)
	 * @return {@link HttpUriRequest} containing all properties
	 */
	public HttpUriRequest createLogRequest(final URI uri, final Revision startRevision, final Revision endRevision) {
		final DavTemplateRequest request = new DavTemplateRequest("REPORT");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<log-report xmlns=\"svn:\"><start-revision>");
		body.append(startRevision);
		body.append("</start-revision><end-revision>");
		body.append(endRevision);
		body.append("</end-revision><discover-changed-paths/><encode-binary-props/><all-revprops/><path/></log-report>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	/**
	 * Creating a new folder
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} creating the new folder
	 */
	public HttpUriRequest createMakeFolderRequest(final URI uri) {
		final DavTemplateRequest request = new DavTemplateRequest("MKCOL");
		request.setURI(uri);
		return request;
	}

	/**
	 * Merge all modifications from previous request
	 * @param uri {@link URI} to perform the request against
	 * @param path absolute resource-path relative to the repository root
	 * @param info current {@link InfoEntry} for the resource
	 * @return {@link HttpUriRequest} merging all modifications from previous request
	 */
	public HttpUriRequest createMergeRequest(final URI uri, final Path path, final InfoEntry info) {
		final DavTemplateRequest request = new DavTemplateRequest("MERGE");
		request.setURI(uri);
		request.setHeader("X-SVN-Options", "release-locks");

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>");
		body.append(StringEscapeUtils.escapeXml(path.getValue()));
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop>");
		final String token = info.getLockToken();
		if (token != null) {
			body.append("<S:lock-token-list xmlns:S=\"svn:\"><S:lock><S:lock-path>");
			body.append(StringEscapeUtils.escapeXml(info.getPath().getValueWithoutLeadingSeparator()));
			body.append("</S:lock-path>");
			body.append("<S:lock-token>");
			body.append(token);
			body.append("</S:lock-token></S:lock></S:lock-token-list>");
		}
		body.append("</merge>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	/**
	 * Remove the given properties form the resource
	 * @param uri {@link URI} to perform the request against
	 * @param lockToken if the resource is locked, the lock-token is used to approve the request, can be {@code null} if the resource is not locked, or if no implicit approval is desired
	 * @param lockTokenTarget the {@link URI} to the resource that has been locked, can be {@code null} if no lockToken is specified
	 * @param properties properties {@link ResourceProperty} to remove
	 * @return {@link HttpUriRequest} removing the given properties form the resource
	 */
	public HttpUriRequest createRemovePropertiesRequest(final URI uri, @Nullable final String lockToken, @Nullable final URI lockTokenTarget, final ResourceProperty... properties) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH");
		request.setURI(uri);

		addApproveTokenHeader(request, lockToken, lockTokenTarget);

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<propertyupdate xmlns=\"DAV:\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\"><remove>");
		for (final ResourceProperty property : properties) {
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

	/**
	 * Set the given properties for the resource (new properties will be added, existing properties will be overridden)
	 * @param uri {@link URI} to perform the request against
	 * @param lockToken if the resource is locked, the lock-token is used to approve the request, can be {@code null} if the resource is not locked, or if no implicit approval is desired
	 * @param lockTokenTarget the {@link URI} to the resource that has been locked, can be {@code null} if no lockToken is specified
	 * @param properties {@link ResourceProperty} to add or override
	 * @return {@link HttpUriRequest} setting the given properties for the resource
	 */
	public HttpUriRequest createSetPropertiesRequest(final URI uri, @Nullable final String lockToken, @Nullable final URI lockTokenTarget, final ResourceProperty... properties) {
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH");
		request.setURI(uri);

		addApproveTokenHeader(request, lockToken, lockTokenTarget);

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<propertyupdate xmlns=\"DAV:\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\"><set>");
		for (final ResourceProperty property : properties) {
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

	/**
	 * Unlocking a resource
	 * @param uri {@link URI} to perform the request against
	 * @param lockToken to unlock the resource the lock-token that was generated during the lock request must be provided
	 * @return {@link HttpUriRequest} unlocking the resource
	 */
	public HttpUriRequest createUnlockRequest(final URI uri, final String lockToken) {
		final DavTemplateRequest request = new DavTemplateRequest("UNLOCK");
		request.setURI(uri);
		request.addHeader("Lock-Token", "<" + lockToken + ">");
		return request;
	}

	/**
	 * Upload content to a resource
	 * @param uri {@link URI} to perform the request against
	 * @param lockToken if the resource is locked, the lock-token is used to approve the request, can be {@code null} if the resource is not locked, or if no implicit approval is desired
	 * @param lockTokenTarget the {@link URI} to the resource that has been locked, can be {@code null} if no lockToken is specified
	 * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
	 * @return {@link HttpUriRequest} uploading content to the resource
	 */
	public HttpUriRequest createUploadRequest(final URI uri, @Nullable final String lockToken, @Nullable final URI lockTokenTarget, final InputStream content) {
		final HttpPut request = new HttpPut(uri);

		addApproveTokenHeader(request, lockToken, lockTokenTarget);
		request.setEntity(new InputStreamEntity(content, STREAM_WHOLE_CONTENT));
		return request;
	}
}
