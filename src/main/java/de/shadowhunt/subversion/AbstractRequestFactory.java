/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import java.io.InputStream;
import java.net.URI;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpDelete;
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
		final Header depthHeader = new BasicHeader("Depth", Depth.INFINITY.value);
		final DavTemplateRequest request = new DavTemplateRequest("COPY", depthHeader);
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
		final Header depthHeader = new BasicHeader("Depth", depth.value);
		final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", depthHeader);
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propfind xmlns=\"DAV:\"><allprop/></propfind>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	/**
	 * Locking a resource
	 * @param uri {@link URI} to perform the request against
	 * @param steal if the resource is locked by another user {@code true} will override the lock, otherwise the operation will fail
	 * @return {@link HttpUriRequest} locking the resource
	 */
	public HttpUriRequest createLockRequest(final URI uri, final boolean steal) {
		final DavTemplateRequest request = new DavTemplateRequest("LOCK");
		request.setURI(uri);
		if (steal) {
			request.addHeader("X-SVN-Options", "lock-steal");
		}

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
	 * @param resource absolute resource-path relative to the repository root
	 * @param info current {@link InfoEntry} for the resource
	 * @return {@link HttpUriRequest} merging all modifications from previous request
	 */
	public HttpUriRequest createMergeRequest(final URI uri, final Resource resource, final InfoEntry info) {
		final DavTemplateRequest request = new DavTemplateRequest("MERGE");
		request.setURI(uri);
		request.setHeader("X-SVN-Options", "release-locks");

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>");
		body.append(StringEscapeUtils.escapeXml(resource.getValue()));
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop>");
		final String token = info.getLockToken();
		if (token != null) {
			body.append("<S:lock-token-list xmlns:S=\"svn:\"><S:lock><S:lock-path>");
			body.append(StringEscapeUtils.escapeXml(info.getResource().getValueWithoutLeadingSeparator()));
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
		sb.append("<propertyupdate xmlns=\"DAV:\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\"><set><prop>");
		for (final ResourceProperty property : properties) {
			final String prefix = property.getType().getPrefix();
			final String name = property.getName();
			sb.append('<');
			sb.append(prefix);
			sb.append(name);
			sb.append('>');
			sb.append(StringEscapeUtils.escapeXml(property.getValue()));
			sb.append("</");
			sb.append(prefix);
			sb.append(name);
			sb.append('>');
		}
		sb.append("</prop></set></propertyupdate>");
		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	/**
	 * Unlocking a resource
	 * @param uri {@link URI} to perform the request against
	 * @param lockToken to unlock the resource the lock-token that was generated during the lock request must be provided
	 * @param force the user that created the lock must match the user who wants to delete it, unless force is {@code true}
	 * @return {@link HttpUriRequest} unlocking the resource
	 */
	public HttpUriRequest createUnlockRequest(final URI uri, final String lockToken, final boolean force) {
		final DavTemplateRequest request = new DavTemplateRequest("UNLOCK");
		request.setURI(uri);
		request.addHeader("Lock-Token", "<" + lockToken + ">");
		if (force) {
			request.addHeader("X-SVN-Options", "lock-break");
		}
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
